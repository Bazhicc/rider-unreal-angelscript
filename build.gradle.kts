import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease

plugins {
    java
    id("org.jetbrains.kotlin.jvm") version "2.3.10"
    id("org.jetbrains.intellij.platform") version "2.12.0"
    id("org.jetbrains.intellij.platform.grammarkit") version "2.12.0"
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
        else rider(providers.gradleProperty("platformVersion").get())
        bundledPlugin("com.intellij.modules.json")
        bundledPlugin("org.jetbrains.plugins.textmate")
        testFramework(TestFrameworkType.Platform)
        pluginVerifier()
        jflex("1.9.2")
    }
    testImplementation("junit:junit:4.13.2")
    // 2.12.0's grammarKit helper incorrectly selects JFlex; use the tool coordinate directly.
    add("intellijPlatformGrammarKit", "org.jetbrains:grammar-kit:2022.3.2")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "261.25134"
            untilBuild = "261.*"
        }
        changeNotes = "Restore structured GrammarKit/JFlex PSI, native references, declaration navigation, rename hooks and structure view. " +
            "Fix named argument and qualified namespace parsing while preserving TextMate, LSP, debugger and formatter improvements."
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
// GrammarKit/JFlex output is checked in alongside its BNF/Flex sources.
sourceSets.main { java.srcDir("src/main/gen") }
tasks.generateParser {
    sourceFile.set(file("src/main/kotlin/com/aesirinteractive/angelscript/Angelscript.bnf"))
    targetRootOutputDir.set(layout.buildDirectory.dir("generated/angelscript-parser"))
    pathToParser.set("com/aesirinteractive/angelscript/AngelscriptParser.java")
    pathToPsiRoot.set("com/aesirinteractive/angelscript/psi")
    purgeOldFiles.set(false)
    // Grammar-Kit's 2022 CLI treats its source argument as a pattern; absolute
    // Windows paths do not match. The task below uses a relative pattern.
    enabled = false
}
val regenerateParser by tasks.registering(JavaExec::class) {
    group = "intellij platform"
    description = "Regenerate AngelScript parser/PSI using a Windows-safe relative grammar pattern"
    mainClass.set("org.intellij.grammar.Main")
    classpath = tasks.generateParser.get().classpath
    workingDir("src/main/kotlin/com/aesirinteractive/angelscript")
    args(layout.buildDirectory.dir("generated/angelscript-parser").get().asFile.absolutePath, "Angelscript.bnf")
    inputs.file("src/main/kotlin/com/aesirinteractive/angelscript/Angelscript.bnf")
    outputs.dir(layout.buildDirectory.dir("generated/angelscript-parser"))
}
tasks.generateLexer {
    sourceFile.set(file("src/main/kotlin/com/aesirinteractive/angelscript/Angelscript.flex"))
    targetOutputDir.set(layout.buildDirectory.dir("generated/angelscript-lexer"))
    purgeOldFiles.set(false)
}
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

tasks.test { systemProperty("java.awt.headless", "true") }

val testLanguageServer by tasks.registering(Exec::class) {
    dependsOn(buildLanguageServer)
    workingDir("language-server")
    commandLine("node", "--test", "test/rider-lsp.test.cjs")
}
tasks.check { dependsOn(testLanguageServer) }
