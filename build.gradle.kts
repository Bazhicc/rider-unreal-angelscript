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
        changeNotes = """
            <h3>1.0.1</h3>
            <ul>
                <li>Fix false syntax errors for #ifdef and #ifndef conditional branches.</li>
                <li>Preserve escaped quotes in string literals, default parameters, and interpolation expressions.</li>
                <li>Keep string token boundaries and offsets stable when restarting the lexer.</li>
                <li>Add syntax regression coverage and clarify the plugin description and setup requirements.</li>
            </ul>
        """.trimIndent()
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
