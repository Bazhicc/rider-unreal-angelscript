package com.bazhicc.angelscript.formatter

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory

enum class BraceMode { NOT_REQUIRED, REQUIRED, BODY_MULTILINE, STATEMENT_MULTILINE, IF_ANY_REQUIRES }

/** Defaults verified against Rider 2026.1's CppCodeStyleSettingsKey. */
data class BracePolicy(val ifMode: BraceMode = BraceMode.NOT_REQUIRED,
                       val forMode: BraceMode = BraceMode.NOT_REQUIRED,
                       val whileMode: BraceMode = BraceMode.NOT_REQUIRED,
                       val doMode: BraceMode = BraceMode.REQUIRED,
                       val removeRedundant: Boolean = false) {
    constructor(ifRequired: Boolean, forRequired: Boolean, whileRequired: Boolean, doRequired: Boolean) :
        this(always(ifRequired), always(forRequired), always(whileRequired), always(doRequired))
    val ifRequired: Boolean get() = ifMode == BraceMode.REQUIRED
    val forRequired: Boolean get() = forMode == BraceMode.REQUIRED
    val whileRequired: Boolean get() = whileMode == BraceMode.REQUIRED
    val doRequired: Boolean get() = doMode == BraceMode.REQUIRED
    val enabled: Boolean get() = removeRedundant || listOf("if", "for", "while", "do").any { mode(it) != BraceMode.NOT_REQUIRED }
    fun mode(keyword: String): BraceMode = when (keyword) {
        "if", "else" -> ifMode
        "for", "foreach" -> forMode
        "while" -> whileMode
        "do" -> doMode
        else -> BraceMode.NOT_REQUIRED
    }
    fun required(keyword: String): Boolean = when (keyword) {
        "if", "else" -> ifRequired
        "for", "foreach" -> forRequired
        "while" -> whileRequired
        "do" -> doRequired
        else -> false
    }

    companion object {
        private fun always(required: Boolean) = if (required) BraceMode.REQUIRED else BraceMode.NOT_REQUIRED
        val NONE = BracePolicy(doMode = BraceMode.NOT_REQUIRED)
        val ALL = BracePolicy(true, true, true, true)
    }
}

/** Read each saved C++ brace preference independently, in Rider settings-layer order. */
object RiderBraceSettings {
    private const val PREFIX = "/Default/CodeStyle/CodeFormatting/CppCodeStyle/"

    fun load(project: Project): BracePolicy = load((project.basePath ?: project.baseDir?.path)?.let(Path::of),
        Path.of(PathManager.getConfigPath()), project.name)

    internal fun values(project: Project): Map<String, String> = values((project.basePath ?: project.baseDir?.path)?.let(Path::of),
        Path.of(PathManager.getConfigPath()), project.name)

    fun load(projectRoot: Path?, configRoot: Path, projectName: String): BracePolicy = policy(values(projectRoot, configRoot, projectName))

    private fun values(projectRoot: Path?, configRoot: Path, projectName: String): Map<String, String> {
        val files = mutableListOf(configRoot.resolve("resharper-host/GlobalSettingsStorage.DotSettings"))
        if (projectRoot != null && Files.isDirectory(projectRoot)) {
            val shared = Files.newDirectoryStream(projectRoot, "*.DotSettings*").use { files ->
                files.filter { Files.isRegularFile(it) }.map { it.resolveSibling(it.fileName.toString().removeSuffix(".user")) }
                    .filter { it.fileName.toString().matches(Regex(".+\\.(sln|slnx|uproject)\\.DotSettings", RegexOption.IGNORE_CASE)) }
                    .distinct()
            }
            val settings = shared.firstOrNull { it.fileName.toString().matches(Regex("${Regex.escape(projectName)}\\.(sln|slnx|uproject)\\.DotSettings", RegexOption.IGNORE_CASE)) }
                ?: shared.singleOrNull()
            // A personal layer can exist even if its corresponding shared layer has never been saved.
                ?: projectRoot.resolve("$projectName.sln.DotSettings")
            files.add(settings)
            files.add(settings.resolveSibling("${settings.fileName}.user"))
        }
        val values = mutableMapOf<String, String>()
        files.forEach { values.putAll(read(it)) }
        return values
    }

    internal fun policy(values: Map<String, String>): BracePolicy {
        fun mode(name: String, default: BraceMode = BraceMode.NOT_REQUIRED) = when (values["BracesIn${name}Statement"]) {
            "Required" -> BraceMode.REQUIRED
            "RequiredForMultiline" -> BraceMode.BODY_MULTILINE
            "RequiredForMultilineStatement" -> BraceMode.STATEMENT_MULTILINE
            "NotRequiredForBoth" -> BraceMode.IF_ANY_REQUIRES
            "NotRequired" -> BraceMode.NOT_REQUIRED
            else -> default
        }
        return BracePolicy(mode("If"), mode("For"), mode("While"), mode("Do", BraceMode.REQUIRED),
            values["BracesRedundant"].equals("True", true))
    }

    private fun read(path: Path): Map<String, String> {
        if (!Files.isRegularFile(path)) return emptyMap()
        return try {
            val factory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
                setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                setFeature("http://xml.org/sax/features/external-general-entities", false)
                setFeature("http://xml.org/sax/features/external-parameter-entities", false)
            }
            val document = Files.newInputStream(path).use { factory.newDocumentBuilder().parse(it) }
            val elements = document.getElementsByTagName("*")
            (0 until elements.length).mapNotNull { index ->
                val element = elements.item(index) as org.w3c.dom.Element
                val key = element.getAttributeNS("http://schemas.microsoft.com/winfx/2006/xaml", "Key")
                if ((key.startsWith(PREFIX) || key.startsWith("/Default/CodeStyle/CodeFormatting/CppFormatting/") ||
                    key.endsWith("/EnableEditorConfigSupport/@EntryValue")) && key.endsWith("/@EntryValue"))
                    key.removeSuffix("/@EntryValue").substringAfterLast('/') to element.textContent.trim()
                else null
            }.toMap()
        } catch (error: Exception) {
            Logger.getInstance(RiderBraceSettings::class.java).debug("Cannot read Rider settings: $path", error)
            emptyMap()
        }
    }
}
