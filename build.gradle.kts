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
        bundledPlugin("org.jetbrains.plugins.textmate")
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
        changeNotes = "Restore indexed C++ source navigation before UE types load, using explicit script types, receivers and static/global call contexts; preserve UE semantic navigation and live symbol refresh. " +
            "Preserve the complete Kotlin lexer/parser/PSI, native references, atomic rename/undo, TextMate, LSP, debugger and formatter support."
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

tasks.processResources {
    dependsOn(buildLanguageServer)
    from("language-server/dist/server.js") { into("lsp") }
    from("resources/file-icon-dark-theme.svg") { into("icons") }
    from("extension/syntaxes/angelscript.tmLanguage.json") { into("textmate/syntaxes") }
    from("extension/language-configuration.json") { into("textmate") }
    from(listOf("LICENSE", "THIRD_PARTY_NOTICES.md")) { into("META-INF") }
}

tasks.test {
    systemProperty("java.awt.headless", "true")
    providers.gradleProperty("testEngineSource").orNull?.let { systemProperty("angelscript.test.engineSource", it) }
}

val testLanguageServer by tasks.registering(Exec::class) {
    dependsOn(buildLanguageServer)
    workingDir("language-server")
    commandLine("node", "--test", "test/rider-lsp.test.cjs", "test/native-types.test.cjs")
}
tasks.check { dependsOn(testLanguageServer) }
