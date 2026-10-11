package com.aesirinteractive.angelscript

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.ExternalFormatProcessor
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.TimeUnit
import javax.xml.parsers.SAXParserFactory

private val LOG = logger<AngelscriptExternalFormatter>()

class AngelscriptExternalFormatter : ExternalFormatProcessor {

    companion object {
        @Volatile private var cachedClangFormatPath: String? = null
        @Volatile private var cachedClangFormatPathKind: ClangFormatPathKind? = null
        @Volatile private var cachedClangFormatCustomPath: String? = null

        fun clearClangFormatCache() {
            cachedClangFormatPath = null
            cachedClangFormatPathKind = null
            cachedClangFormatCustomPath = null
            LOG.info("clang-format: cache cleared")
        }

        private val FORMAT_STRING_PREFIX = Regex("""^[A-Za-z_]['"].*""")
        private val IN_OUT_REF           = Regex("""^.?&(in|out).*""")
        private val START_LINE_TYPED     = Regex("""^\s*\w+\s+\w+\s*\(.*""")
        private val START_LINE_TYPE_ONLY = Regex("""^\s*\w+\r?\n.*""")
        private val ACCESS_LINE          = Regex("""^\s*access\s*:.*""")

        private data class KeywordRules(
            val hasIdent: Regex,
            val splitAtKeyword: Regex,
            val splitAtIdent: Regex
        )

        private val KEYWORD_RULES: List<KeywordRules> =
            listOf("private", "protected", "delegate", "event").map { kw ->
                KeywordRules(
                    hasIdent       = Regex("""^\s*$kw\s+\w+.*"""),
                    splitAtKeyword = Regex("""^\s*$kw\r?\n.*"""),
                    splitAtIdent   = Regex("""^\s*$kw\s+\w+\r?\n.*""")
                )
            }
    }

    override fun getId(): String = "ANGELSCRIPT_CLANGD_FORMATTER"

    override fun activeForFile(source: PsiFile): Boolean =
        source.language == AngelscriptLanguage.INSTANCE

    override fun indent(p0: PsiFile, p1: Int): String? {
        return null
    }

    override fun format(
        psi: PsiFile,
        range: TextRange,
        canChangeWhiteSpacesOnly: Boolean,
        keepLineBreaks: Boolean,
        p4: Boolean,
        p5: Int
    ): TextRange? {
        val vf = psi.virtualFile!!
        val document = FileDocumentManager.getInstance().getDocument(vf) ?: return null
        val project = psi.project
        val content = document.text

        val clangFormatPath = resolveClangFormat() ?: return null
        val filename = vf.path

        val xmlOutput = runClangFormat(clangFormatPath, filename, content, project.basePath) ?: return null
        val replacements = toDocumentReplacements(content, parseReplacements(xmlOutput)).filter { (offset, length, text) ->
            offset >= range.startOffset && offset + length <= range.endOffset &&
                allowEdit(content, offset, offset + length, text)
        }

        if (replacements.isEmpty()) return range

        // Apply from end to start so earlier offsets remain valid
        val sorted = replacements.sortedByDescending { it.first }

        WriteCommandAction.runWriteCommandAction(project) {
            for ((offset, length, text) in sorted) {
                if (offset < 0 || offset + length > document.textLength) continue
                document.replaceString(offset, offset + length, text)
            }
        }

        return range
    }

    private fun allowEdit(content: String, startOffset: Int, endOffset: Int, replacementText: String): Boolean {
        val lineStart = content.lastIndexOf('\n', startOffset - 1) + 1
        val lineEnd = content.indexOf('\n', startOffset).let { if (it == -1) content.length else it }
        val startLine = content.substring(lineStart, lineEnd)
        val startColumn = startOffset - lineStart

        val endLineStart = content.lastIndexOf('\n', endOffset - 1) + 1
        val isSameLine = endLineStart == lineStart

        if (isSameLine) {
            val endColumn = endOffset - lineStart

            // 1. Column bounds check
            if (startColumn > startLine.length || endColumn > startLine.length) return false

            val startLineAfterEdit = startLine.substring(0, startColumn) +
                replacementText +
                startLine.substring(endColumn.coerceAtMost(startLine.length))

            if (startOffset == endOffset && startColumn > 0) {
                val context = startLine.substring(startColumn - 1)
                // 2. Format-string prefix guard: prevent space between prefix letter and quote
                if (FORMAT_STRING_PREFIX.containsMatchIn(context)) return false
                // 3. &in / &out guard: prevent space between & and in/out
                if (IN_OUT_REF.containsMatchIn(context)) return false
            }

            if ('\n' in replacementText) {
                // 4. Return-type newline guard
                if (START_LINE_TYPED.matches(startLine) && START_LINE_TYPE_ONLY.matches(startLineAfterEdit))
                    return false

                // 5. Keyword + identifier newline guard
                for (kw in KEYWORD_RULES) {
                    if (kw.hasIdent.matches(startLine) &&
                        (kw.splitAtKeyword.matches(startLineAfterEdit) || kw.splitAtIdent.matches(startLineAfterEdit)))
                        return false
                }
            }
        }

        // 6. access: line guard
        if (ACCESS_LINE.matches(startLine)) return false

        return true
    }

    private fun resolveClangFormat(): String? {
        val settings = AngelscriptSettings.getInstance()
        val kind = settings.clangFormatPathKind

        // Return cached path if the kind (and for Custom, the path string) hasn't changed
        val cached = cachedClangFormatPath
        if (cached != null && cachedClangFormatPathKind == kind) {
            if (kind != ClangFormatPathKind.Custom || cachedClangFormatCustomPath == settings.clangFormatPath) {
                LOG.info("clang-format: using cached path: $cached")
                return cached
            }
        }

        val resolved = when (kind) {
            ClangFormatPathKind.Bundled -> extractBundledClangFormat()
            ClangFormatPathKind.Rider -> findRiderClangFormat()
            ClangFormatPathKind.VisualStudio -> findVisualStudioClangFormat()
            ClangFormatPathKind.Custom -> {
                val path = settings.clangFormatPath
                when {
                    path.isBlank() -> {
                        val msg = "clang-format: custom path is empty"
                        LOG.warn(msg)
                        notifyError(msg)
                        null
                    }
                    !Files.exists(Path.of(path)) -> {
                        val msg = "clang-format: custom path does not exist: $path"
                        LOG.warn(msg)
                        notifyError(msg)
                        null
                    }
                    else -> {
                        LOG.info("clang-format: using custom path: $path")
                        path
                    }
                }
            }
        }

        if (resolved != null) {
            cachedClangFormatPath = resolved
            cachedClangFormatPathKind = kind
            cachedClangFormatCustomPath = if (kind == ClangFormatPathKind.Custom) settings.clangFormatPath else null
        }

        return resolved
    }

    private fun extractBundledClangFormat(): String? {
        val exe = if (SystemInfo.isWindows) "clang-format.exe" else "clang-format"
        val target = Path.of(PathManager.getPluginsPath(), "AngelscriptLsp", "clang-format", exe)
        LOG.info("clang-format: bundled extraction target: $target")
        if (Files.exists(target) && Files.size(target) > 0) {
            LOG.info("clang-format: using cached bundled binary at $target (${Files.size(target)} bytes)")
            return target.toString()
        }
        val resource = "/clang-format/$exe"
        val stream = AngelscriptExternalFormatter::class.java.getResourceAsStream(resource)
        if (stream == null) {
            val msg = "clang-format: bundled binary not found in plugin JAR at $resource"
            LOG.error(msg)
            notifyError(msg)
            return null
        }
        Files.createDirectories(target.parent)
        stream.use { Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING) }
        if (SystemInfo.isUnix) target.toFile().setExecutable(true)
        LOG.info("clang-format: extracted bundled binary to $target (${Files.size(target)} bytes)")
        return target.toString()
    }

    private fun findRiderClangFormat(): String? {
        val exe = if (SystemInfo.isWindows) "clang-format.exe" else "clang-format"
        val riderHome = Path.of(PathManager.getHomePath())
        LOG.info("clang-format: searching for Rider-bundled clang-format under $riderHome")

        // Toolbox / local install: <Rider>/lib/ReSharperHost/windows-x64/clang-format.exe
        val platformDir = when {
            SystemInfo.isWindows -> "windows-x64"
            SystemInfo.isMac -> "macos-arm64" // Rider ships both; try arm64 first, fall through to x64
            else -> "linux-x64"
        }
        val resharpPath = riderHome.resolve("lib/ReSharperHost/$platformDir/$exe")
        if (Files.exists(resharpPath)) {
            LOG.info("clang-format: found Rider ReSharperHost binary at $resharpPath")
            return resharpPath.toString()
        }
        if (SystemInfo.isMac) {
            val x64 = riderHome.resolve("lib/ReSharperHost/macos-x64/$exe")
            if (Files.exists(x64)) {
                LOG.info("clang-format: found Rider ReSharperHost binary at $x64")
                return x64.toString()
            }
        }

        // Program Files install: <Rider>/r2r/<version>/<hash>/windows-x64/clang-format.exe
        val r2rBase = riderHome.resolve("r2r")
        if (Files.isDirectory(r2rBase)) {
            LOG.info("clang-format: searching r2r directory $r2rBase")
            try {
                val candidate = Files.walk(r2rBase, 3)
                    .filter { it.fileName.toString() == exe && Files.isRegularFile(it) }
                    .max(Comparator.comparingLong { Files.getLastModifiedTime(it).toMillis() })
                if (candidate.isPresent) {
                    LOG.info("clang-format: found Rider r2r binary at ${candidate.get()}")
                    return candidate.get().toString()
                }
            } catch (e: Exception) {
                LOG.warn("clang-format: error walking r2r directory: ${e.message}")
            }
        }

        val msg = "clang-format: could not find Rider-bundled clang-format under $riderHome"
        LOG.warn(msg)
        notifyError(msg)
        return null
    }

    private fun findVisualStudioClangFormat(): String? {
        val searchRoots = listOf(
            Path.of("C:/Program Files/Microsoft Visual Studio"),
            Path.of("C:/Program Files (x86)/Microsoft Visual Studio"),
        ).filter { Files.isDirectory(it) }

        if (searchRoots.isEmpty()) {
            val msg = "clang-format: Visual Studio not found in Program Files or Program Files (x86)"
            LOG.warn(msg)
            notifyError(msg)
            return null
        }

        // Prefer x64/bin over plain Llvm\bin (32-bit on some installs)
        for (vsBase in searchRoots) {
            LOG.info("clang-format: searching for Visual Studio clang-format under $vsBase")
            try {
                val candidate = Files.walk(vsBase, 7)
                    .filter { it.fileName.toString() == "clang-format.exe" && Files.isRegularFile(it) }
                    .max(Comparator.comparingInt { p: Path ->
                        when {
                            p.toString().contains("x64") -> 2
                            p.toString().contains("Llvm\\bin") -> 1
                            else -> 0
                        }
                    }.thenComparingLong { Files.getLastModifiedTime(it).toMillis() })
                if (candidate.isPresent) {
                    LOG.info("clang-format: found Visual Studio clang-format at ${candidate.get()}")
                    return candidate.get().toString()
                }
            } catch (e: Exception) {
                LOG.warn("clang-format: error searching Visual Studio directory $vsBase: ${e.message}")
            }
        }

        val msg = "clang-format: clang-format.exe not found under any Visual Studio installation"
        LOG.warn(msg)
        notifyError(msg)
        return null
    }

    private fun notifyError(content: String) =
        NotificationGroupManager.getInstance()
            .getNotificationGroup("AngelScript LSP")
            .createNotification("clang-format error", content, NotificationType.ERROR)
            .notify(null)

    private fun runClangFormat(executable: String, filename: String, content: String, projectBasePath: String?): String? {
        try {
            val settings = AngelscriptSettings.getInstance()
            val rawClangFormatFile = settings.clangFormatFile
            val clangFormatFile = if (rawClangFormatFile.isNotBlank() && !Path.of(rawClangFormatFile).isAbsolute && projectBasePath != null) {
                Path.of(projectBasePath, rawClangFormatFile).toString()
            } else {
                rawClangFormatFile
            }
            val process = if (Files.exists(Path.of(clangFormatFile))) {
                ProcessBuilder(executable, "--output-replacements-xml", "--assume-filename=$filename", "--style=file:$clangFormatFile")
                    .redirectErrorStream(false)
                    .start()
            } else {
                ProcessBuilder(executable, "--output-replacements-xml", "--assume-filename=$filename")
                    .redirectErrorStream(false)
                    .start()
            }

            process.outputStream.bufferedWriter().use { it.write(content) }

            val stdout = process.inputStream.bufferedReader().readText()
            val stderr = process.errorStream.bufferedReader().readText()

            val exited = process.waitFor(10, TimeUnit.SECONDS)
            if (!exited) {
                process.destroyForcibly()
                val msg = "clang-format timed out after 10s for $filename"
                LOG.warn(msg)
                notifyError(msg)
                return null
            }

            if (process.exitValue() != 0) {
                LOG.warn("clang-format exited with ${process.exitValue()} for $filename: $stderr")
                notifyError("clang-format exited with ${process.exitValue()} for $filename: $stderr")
                return null
            }

            return stdout
        } catch (e: Exception) {
            LOG.warn("Failed to run clang-format: ${e.message}")
            notifyError("Failed to run clang-format: ${e.message}")
            return null
        }
    }

    // Returns list of (offset, length, replacementText)
    private fun parseReplacements(xml: String): List<Triple<Int, Int, String>> {
        val results = mutableListOf<Triple<Int, Int, String>>()
        try {
            val factory = SAXParserFactory.newInstance()
            val parser = factory.newSAXParser()

            var currentOffset = 0
            var currentLength = 0
            val currentText = StringBuilder()
            var inReplacement = false

            val handler = object : DefaultHandler() {
                override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
                    if (qName.equals("replacement", ignoreCase = true)) {
                        currentOffset = attributes.getValue("offset")?.toIntOrNull() ?: 0
                        currentLength = attributes.getValue("length")?.toIntOrNull() ?: 0
                        currentText.clear()
                        inReplacement = true
                    }
                }

                override fun characters(ch: CharArray, start: Int, length: Int) {
                    if (inReplacement) currentText.append(ch, start, length)
                }

                override fun endElement(uri: String, localName: String, qName: String) {
                    if (qName.equals("replacement", ignoreCase = true)) {
                        results.add(Triple(currentOffset, currentLength, currentText.toString()))
                        inReplacement = false
                    }
                }
            }

            parser.parse(ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8)), handler)
        } catch (e: Exception) {
            LOG.warn("Failed to parse clang-format XML: ${e.message}")
        }
        return results
    }
}

/** clang-format reports UTF-8 byte offsets; IntelliJ documents use UTF-16 offsets. */
internal fun toDocumentReplacements(
    content: String,
    replacements: List<Triple<Int, Int, String>>,
): List<Triple<Int, Int, String>> {
    val bytes = content.toByteArray(Charsets.UTF_8)
    var previousByte = 0
    var previousChar = 0
    return replacements.sortedBy { it.first }.map { (offset, length, text) ->
        require(offset >= previousByte && length >= 0 && offset <= bytes.size - length) {
            "Invalid clang-format replacement range: $offset + $length"
        }
        val start = previousChar + String(bytes, previousByte, offset - previousByte, Charsets.UTF_8).length
        val charLength = String(bytes, offset, length, Charsets.UTF_8).length
        previousByte = offset + length
        previousChar = start + charLength
        Triple(start, charLength, text)
    }
}
