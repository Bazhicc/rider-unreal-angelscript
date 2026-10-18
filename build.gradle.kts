import java.net.URI
import java.security.MessageDigest
import java.util.Base64
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.10"
    id("org.jetbrains.intellij.platform") version "2.12.0"
}

group = "com.bazhicc"
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        val localPath = providers.gradleProperty("localPlatformPath").orNull
        if (localPath != null) local(localPath)
        else rider(providers.gradleProperty("platformVersion").get()) { useInstaller.set(false) }
        bundledPlugin("com.intellij.modules.json")
        bundledModule("intellij.rider.cpp.core")
        bundledPlugin("org.editorconfig.editorconfigjetbrains")
        testFramework(TestFrameworkType.Platform)
        pluginVerifier()
    }
    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "261.25134"
            untilBuild = "261.*"
        }
        changeNotes = "Reuse Rider's C++ color identities for native and LSP highlighting, including theme and user customizations. Remove fixed AngelScript palettes and distinguish macros from Unreal reflection specifiers. " +
            "Remove the TextMate dependency and bundle; use Rider's native lexer, PSI and color scheme system for all AngelScript highlighting. " +
            "Use the native lexer consistently for editing and color previews. Share C++ color keys between native and LSP highlighting for Unreal metadata, scoped variables, parameters, enum constants, namespaces and all operators without requiring a ready language server. " +
            "Distinguish interpolation format suffixes from named-argument and conditional-expression colons. Preserve function calls, nested ternaries, subscripts, namespaces, comments and string literals inside format expressions, and reset interpolation context when restarting the lexer. " +
            "Normalize unopened native sources to editor UTF-16/LF coordinates while preserving unsaved documents. Fix CRLF-dependent caret drift for C++ declarations, fields, overload choices and macro/include source mappings; derive candidate line numbers without loading native PSI documents. " +
            "Cache native word/header lookups, prepared source views, bindings and exact targets with edit, VFS, roots and index invalidation. Use one LSP navigation request. Integrate semantic AS references into native Rider C++ Find Usages through a minimal C# backend, with declaration identities, overload signatures, scope filtering and UTF-16/CRLF mappings. " +
            "Remove duplicate native/LSP Find Usages targets so searches start directly without an extra target picker. Preserve semantic reference locations, search scopes and offline PSI fallback. " +
            "Show usages at script declaration names and navigate to definitions at references. Register native Find Usages with semantic LSP references and offline PSI fallback, respect search scopes, and exclude declarations from usage results. " +
            "Fix ambiguous C++ and script overload navigation popups by using non-PSI presentation and file-location snapshots. Preserve signatures, full-path tooltips, filtering and exact source offsets. " +
            "Resolve native type, alias and inheritance references in their lexical scopes, preserving relative and global qualification, nested template owners and every base branch. Prevent namespaced, class-local and function-local declarations from hijacking unrelated types without special-casing container names. Keep indexed native source navigation available when a loaded UE database is partial or stale. Resolve nested native types and class-scoped aliases. Expand local and directly included function macros with original source mappings, including token pasting, stringification and variadic arguments. Exclude known inactive conditional branches while retaining unknown build configurations. Refresh macro bindings from current header contents. Parse arithmetic right shift and boolean xor in Kotlin PSI and the embedded language server. Resolve Unreal-prefixed public headers before global native lookups, including FString's define-injected implementation. Follow indirectly included indexed aliases when no public definition exists. Show complete source paths in navigation candidate tooltips, keeping signatures and file/line labels in the list. Prefer public header aliases over unrelated reflection/tool definitions for native type and member navigation. Respect native binding implementation namespaces and qualified aliases/bases when locating C++ declarations. Infer offline auto/foreach receivers and chained member returns from current native binding signatures, including nested generic arguments. Support UE 5.8 define-injected container declarations in included source fragments, preserve source offsets and inheritance, and bound C++ alias lookup. Fix C++ navigation aborting with StackOverflowError on long literals. Preserve registered implementations, shared binding helpers and detailed navigation choices. " +
            "Preserve the complete Kotlin lexer/parser/PSI, native references, atomic rename/undo, LSP, debugger and formatter support."
    }
    pluginVerification {
        ides {
            val localPath = providers.gradleProperty("localPlatformPath").orNull
            if (localPath != null) local(localPath)
            else {
                create(IntelliJPlatformType.Rider, providers.gradleProperty("platformVersion").get())
                // Check the baseline and newest stable release within the declared build range.
                create(ProductReleasesValueSource {
                    types = listOf(IntelliJPlatformType.Rider)
                    channels = listOf(ProductRelease.Channel.RELEASE)
                    sinceBuild = "261.25134"
                    untilBuild = "261.*"
                }.map { releases ->
                    releases.sortedWith { left, right ->
                        val a = Regex("\\d+").findAll(left).map { it.value.toInt() }.toList()
                        val b = Regex("\\d+").findAll(right).map { it.value.toInt() }.toList()
                        a.zip(b).firstOrNull { it.first != it.second }?.let { it.first.compareTo(it.second) }
                            ?: a.size.compareTo(b.size)
                    }.takeLast(1)
                })
            }
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21) }
}
// Kotlin is the sole JVM implementation language. Reject accidental Java additions.
val verifyNoJavaSources by tasks.registering {
    inputs.files(fileTree("src") { include("**/*.java", "**/*.flex", "**/*.bnf") })
    doLast {
        check(inputs.files.isEmpty) { "Java sources and obsolete Java generation inputs are not allowed: ${inputs.files.files}" }
    }
}
tasks.compileKotlin { dependsOn(verifyNoJavaSources) }
// Use this checkout's server; no submodule or VS Code installation is needed.
val npmPrefix = if (System.getProperty("os.name").startsWith("Windows"))
    listOf("cmd", "/d", "/c", "npm") else listOf("npm")

val installLanguageServer by tasks.registering(Exec::class) {
    workingDir("language-server")
    commandLine(npmPrefix + listOf("ci", "--no-audit", "--no-fund"))
    inputs.files("language-server/package.json", "language-server/package-lock.json")
    outputs.dir("language-server/node_modules")
}

val buildLanguageServer by tasks.registering(Exec::class) {
    dependsOn(installLanguageServer)
    workingDir("language-server")
    commandLine(npmPrefix + listOf("run", "build", "--", "--production"))
    inputs.files(fileTree("language-server/src"), fileTree("language-server/grammar"),
        fileTree("language-server/pegjs"), file("language-server/esbuild.js"))
    outputs.file("language-server/dist/server.js")
}

// Bundle the same pinned LLVM 21.1.8 binaries as the standalone formatter plugin.
val clangArchive = layout.buildDirectory.file("downloads/clang-format-node-2.0.7.tgz")
val downloadClangFormat by tasks.registering {
    outputs.file(clangArchive)
    doLast {
        val archive = clangArchive.get().asFile
        archive.parentFile.mkdirs()
        URI("https://registry.npmjs.org/clang-format-node/-/clang-format-node-2.0.7.tgz")
            .toURL().openStream().use { input -> archive.outputStream().use { input.copyTo(it) } }
    }
}
val verifyClangFormat by tasks.registering {
    dependsOn(downloadClangFormat)
    inputs.file(clangArchive)
    doLast {
        val actual = Base64.getEncoder().encodeToString(
            MessageDigest.getInstance("SHA-512").digest(clangArchive.get().asFile.readBytes()))
        check(actual == "lgYsU/SXYEiLay58CqaNiMht4DDVhseerteYnbZzGOWXXR7+w6DwhBd5Ks6tLmU99XrpdWR5LwMMDL/ZPlA84g==") {
            "clang-format archive checksum mismatch"
        }
    }
}

tasks.processResources {
    dependsOn(buildLanguageServer)
    dependsOn(verifyClangFormat)
    from("language-server/dist/server.js") { into("lsp") }
    from("resources/file-icon-dark-theme.svg") { into("icons") }
    from(listOf("LICENSE", "THIRD_PARTY_NOTICES.md")) { into("META-INF") }
    from({ tarTree(resources.gzip(clangArchive)) }) {
        include("package/bin/**/clang-format", "package/bin/**/clang-format.exe")
        eachFile { path = path.removePrefix("package/") }
        includeEmptyDirs = false
    }
    from("LICENSE-LLVM.txt") { into("META-INF") }
}

tasks.test {
    systemProperty("java.awt.headless", "true")
    providers.gradleProperty("testEngineSource").orNull?.let { systemProperty("angelscript.test.engineSource", it) }
    providers.gradleProperty("testScriptRoot").orNull?.let { systemProperty("angelscript.test.scriptRoot", it) }
}

// Exercise native editor highlighting without relying on a bundled grammar plugin.
tasks.prepareTestSandbox {
    disabledPlugins.add("org.jetbrains.plugins.textmate")
}

// Build against the same installed Rider SDK used for JVM compilation; ship only our backend assembly.
val buildNativeBackend by tasks.registering(Exec::class) {
    val riderPath = intellijPlatform.platformPath
    commandLine("dotnet", "build", "backend/UnrealAngelscript.Rider.csproj", "--nologo", "--ignore-failed-sources",
        "-p:RiderBackendPath=${riderPath.resolve("lib/ReSharperHost")}")
    inputs.files(fileTree("backend") { include("*.cs", "*.csproj", "*.props") })
    inputs.property("riderPath", riderPath.toString())
    outputs.file("build/backend/bin/net9.0/ReSharperPlugin.UnrealAngelscript.dll")
}
tasks.prepareSandbox {
    dependsOn(buildNativeBackend)
    from("build/backend/bin/net9.0/ReSharperPlugin.UnrealAngelscript.dll") { into("${rootProject.name}/dotnet") }
}

val buildNativeBackendTests by tasks.registering(Exec::class) {
    val riderPath = intellijPlatform.platformPath
    commandLine("dotnet", "build", "backend/tests/Backend.Tests.csproj", "--nologo", "--ignore-failed-sources",
        "-p:RiderBackendPath=${riderPath.resolve("lib/ReSharperHost")}")
    inputs.files(fileTree("backend") { include("**/*.cs", "**/*.csproj", "**/*.props"); exclude("**/obj/**") })
    inputs.property("riderPath", riderPath.toString())
    outputs.file("build/backend-tests/bin/net9.0/UnrealAngelscript.Backend.Tests.dll")
}
val testNativeBackend by tasks.registering(Exec::class) {
    dependsOn(buildNativeBackendTests)
    commandLine("dotnet", "build/backend-tests/bin/net9.0/UnrealAngelscript.Backend.Tests.dll",
        intellijPlatform.platformPath.resolve("lib/ReSharperHost").toString())
}
tasks.test {
    dependsOn(buildNativeBackendTests)
    systemProperty("angelscript.test.riderBackend", intellijPlatform.platformPath.resolve("lib/ReSharperHost").toString())
    systemProperty("angelscript.test.backendHarness", layout.buildDirectory.file("backend-tests/bin/net9.0/UnrealAngelscript.Backend.Tests.dll").get().asFile.absolutePath)
}

val testLanguageServer by tasks.registering(Exec::class) {
    dependsOn(buildLanguageServer)
    workingDir("language-server")
    commandLine("node", "--test", "test/rider-lsp.test.cjs", "test/native-types.test.cjs")
}
tasks.check { dependsOn(testLanguageServer, testNativeBackend) }
