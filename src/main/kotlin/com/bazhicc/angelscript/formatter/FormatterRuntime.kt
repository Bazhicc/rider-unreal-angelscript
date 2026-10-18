package com.bazhicc.angelscript.formatter

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.util.SystemInfo
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CancellationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class FormatterProcess {
    private val cancelled = AtomicBoolean()
    private val process = AtomicReference<Process?>()
    fun cancel(): Boolean {
        cancelled.set(true)
        process.get()?.destroyForcibly()
        return true
    }

    fun run(executable: String, args: List<String>, text: String, directory: Path?, timeoutMillis: Long = 20_000): String {
        if (cancelled.get()) throw CancellationException()
        val child = ProcessBuilder(listOf(executable) + args).apply {
            if (directory != null && Files.isDirectory(directory)) directory(directory.toFile())
        }.start()
        process.set(child)
        val stdout = CompletableFuture.supplyAsync { child.inputStream.use { it.readAllBytes().toString(Charsets.UTF_8) } }
        val stderr = CompletableFuture.supplyAsync { child.errorStream.use { it.readAllBytes().toString(Charsets.UTF_8) } }
        val stdin = CompletableFuture.runAsync { child.outputStream.use { it.write(text.toByteArray(Charsets.UTF_8)) } }
        val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis)
        try {
            while (true) {
                if (cancelled.get() || Thread.currentThread().isInterrupted) throw CancellationException()
                if (System.nanoTime() >= deadline) error("Clang-Format timed out")
                if (child.waitFor(50, TimeUnit.MILLISECONDS)) break
            }
            val errorOutput = stderr.get(1, TimeUnit.SECONDS)
            check(child.exitValue() == 0) { errorOutput.ifBlank { "Clang-Format exited with code ${child.exitValue()}" } }
            stdin.get(1, TimeUnit.SECONDS)
            return stdout.get(1, TimeUnit.SECONDS)
        } finally {
            child.destroyForcibly()
            process.compareAndSet(child, null)
        }
    }
}

object FormatterRuntime {
    private data class FileSnapshot(val path: Path, val size: Long, val modifiedNanos: Long)
    private data class StyleCacheKey(val executable: FileSnapshot, val styleFiles: List<FileSnapshot>, val extension: String)
    private data class ResolvedStyle(
        val effective: Path,
        val oneLineOff: String,
        val disabled: Boolean,
        val spaceBeforeAssignment: Boolean,
    )

    private val resolvedStyles = ConcurrentHashMap<StyleCacheKey, ResolvedStyle>()

    private fun snapshot(path: Path): FileSnapshot {
        val normalized = path.toAbsolutePath().normalize()
        return FileSnapshot(normalized, Files.size(normalized), Files.getLastModifiedTime(normalized).to(TimeUnit.NANOSECONDS))
    }

    /** Include ancestor styles because a child config may use BasedOnStyle: InheritParentConfig. */
    private fun styleFiles(path: Path): List<FileSnapshot> = buildList {
        add(snapshot(path))
        var parent = path.toAbsolutePath().normalize().parent
        while (parent != null) {
            for (name in listOf(".clang-format", "_clang-format")) {
                val candidate = parent.resolve(name)
                if (Files.isRegularFile(candidate)) add(snapshot(candidate))
            }
            parent = parent.parent
        }
    }

    private fun resolvedStyle(executable: String, style: Path, filename: String, directory: Path,
                              process: FormatterProcess): ResolvedStyle {
        val key = StyleCacheKey(snapshot(Path.of(executable)), styleFiles(style),
            Path.of(filename).fileName.toString().substringAfterLast('.', "").lowercase())
        val result = resolvedStyles.computeIfAbsent(key) {
            val configured = process.run(executable, listOf("-dump-config", "-style=file:$style",
                "-assume-filename=$filename"), "", directory)
            val disabled = Regex("(?m)^DisableFormat:\\s*true\\s*$").containsMatchIn(configured)
            var safeStyle = configured
            for (name in listOf("InsertBraces", "RemoveBracesLLVM", "RemoveSemicolon", "BreakStringLiterals", "FixNamespaceComments"))
                safeStyle = safeStyle.replace(Regex("(?m)^$name:.*$"), "$name: false")
            safeStyle = safeStyle.replace(Regex("(?m)^QualifierAlignment:.*$"), "QualifierAlignment: Leave")
                .replace(Regex("(?m)^RemoveParentheses:.*$"), "RemoveParentheses: Leave")
                .replace(Regex("(?ms)^RawStringFormats:.*?(?=^\\S|\\z)"), "RawStringFormats: []\n")
            val oneLineOff = Regex("(?m)^OneLineFormatOffRegex:\\s*(.*)$").find(configured)?.groupValues?.get(1)?.trim().orEmpty()
                .let { value -> if (value.startsWith("'") && value.endsWith("'")) value.substring(1, value.length - 1).replace("''", "'")
                    else if (value.startsWith('"') && value.endsWith('"')) value.substring(1, value.length - 1)
                        .replace("\\\\", "\\").replace("\\\"", "\"") else value }
            ResolvedStyle(effectiveStyle(safeStyle), oneLineOff, disabled,
                !Regex("(?m)^SpaceBeforeAssignmentOperators:\\s*false\\s*$").containsMatchIn(configured))
        }
        // Bound the cache while retaining the entry just resolved.
        if (resolvedStyles.size > 32) {
            resolvedStyles.keys.firstOrNull { it != key }?.let(resolvedStyles::remove)
        }
        return result
    }

    @Synchronized
    private fun effectiveStyle(content: String): Path {
        val hash = java.security.MessageDigest.getInstance("SHA-256").digest(content.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        val path = Path.of(PathManager.getSystemPath(), "angelscript-clang-format", "effective-styles", "$hash.clang-format")
        if (!Files.isRegularFile(path)) {
            Files.createDirectories(path.parent)
            Files.writeString(path, content)
        }
        return path
    }

    fun resolvePath(value: String, projectRoot: String?): Path {
        val path = Path.of(value)
        return (if (path.isAbsolute) path else Path.of(projectRoot ?: ".").resolve(path)).normalize().toAbsolutePath()
    }

    @Synchronized
    private fun resourceFile(resource: String, name: String, executable: Boolean = false): Path {
        extracted[resource]?.let { return it }
        val content = javaClass.getResourceAsStream(resource).use { input ->
            checkNotNull(input) { "Bundled resource is unavailable: $resource. Configure a custom executable in Settings | Tools | AngelScript Clang-Format." }
            input.readAllBytes()
        }
        val hash = java.security.MessageDigest.getInstance("SHA-256").digest(content).joinToString("") { "%02x".format(it) }
        val target = Path.of(PathManager.getSystemPath(), "angelscript-clang-format", "bundled", hash, name)
        if (!Files.isRegularFile(target)) {
            Files.createDirectories(target.parent)
            Files.write(target, content)
        }
        if (executable && !SystemInfo.isWindows) check(target.toFile().setExecutable(true)) { "Cannot make $target executable" }
        extracted[resource] = target
        return target
    }
    private val extracted = mutableMapOf<String, Path>()

    fun executable(options: FormatterSettings.Options, projectRoot: String?): String {
        if (options.executable.isNotBlank()) {
            val path = resolvePath(options.executable, projectRoot)
            check(Files.isRegularFile(path)) { "Clang-Format executable does not exist: $path" }
            return path.toString()
        }
        val arch = when (System.getProperty("os.arch").lowercase()) {
            "amd64", "x86_64" -> "x64"
            "aarch64", "arm64" -> "arm64"
            "arm" -> "arm"
            else -> System.getProperty("os.arch").lowercase()
        }
        val os = when { SystemInfo.isWindows -> "win32"; SystemInfo.isMac -> "darwin"; SystemInfo.isLinux -> "linux"; else -> "unknown" }
        val file = if (SystemInfo.isWindows) "clang-format.exe" else "clang-format"
        return resourceFile("/bin/cfn-$os-$arch/$file", "cfn-$os-$arch/$file", true).toString()
    }

    fun style(options: FormatterSettings.Options, filename: String, projectRoot: String?): Path {
        if (options.style.isNotBlank()) {
            val path = resolvePath(options.style, projectRoot)
            check(Files.isRegularFile(path)) { "Style file does not exist: $path" }
            return path
        }
        var directory = Path.of(filename).toAbsolutePath().parent
        while (directory != null) {
            for (name in listOf(".clang-format", "_clang-format")) {
                val candidate = directory.resolve(name)
                if (Files.isRegularFile(candidate)) return candidate
            }
            directory = directory.parent
        }
        return resourceFile("/style/.clang-format", ".clang-format")
    }

    fun format(text: String, filename: String, projectRoot: String?, options: FormatterSettings.Options,
               ranges: List<FormatRange>, process: FormatterProcess = FormatterProcess(),
               preserveNewlines: Boolean = false, whitespaceOnly: Boolean = false,
               bracePolicy: BracePolicy = BracePolicy.NONE): String {
        val result = StringBuilder(text)
        formatEdits(text, filename, projectRoot, options, ranges, process, preserveNewlines, whitespaceOnly, bracePolicy)
            .asReversed().forEach { result.replace(it.start, it.end, it.text) }
        return result.toString()
    }

    fun formatEdits(text: String, filename: String, projectRoot: String?, options: FormatterSettings.Options,
                   ranges: List<FormatRange>, process: FormatterProcess = FormatterProcess(),
                   preserveNewlines: Boolean = false, whitespaceOnly: Boolean = false,
                   bracePolicy: BracePolicy = BracePolicy.NONE): List<Replacement> {
        val executable = executable(options, projectRoot)
        val directory = Path.of(filename).toAbsolutePath().parent
        val config = resolvedStyle(executable, style(options, filename, projectRoot), filename, directory, process)
        if (config.disabled) return emptyList()
        fun disabled(source: String) = AngelScriptSyntax(source).disabledLines(config.oneLineOff)
        fun whitespace(source: String, selected: List<FormatRange>): List<Replacement> {
            val args = mutableListOf("-output-replacements-xml", "-style=file:${config.effective}", "-assume-filename=$filename")
            selected.forEach {
                val (offset, length) = FormatterEngine.byteRange(source, it)
                args += "-offset=$offset"; args += "-length=$length"
            }
            val xml = process.run(executable, args, AngelScriptSyntax(source).clangText(), directory)
            return FormatterEngine.allowedReplacements(source, xml, selected, preserveNewlines, whitespaceOnly,
                config.spaceBeforeAssignment, disabled(source))
        }
        val formatted = whitespace(text, ranges)
        val structural = if (bracePolicy.enabled) ControlFlowParser(AngelScriptSyntax(text, disabled(text))).edits(ranges, bracePolicy, formatted) else emptyList()
        if (structural.isEmpty()) return formatted
        val intermediate = StringBuilder(text).apply {
            structural.asReversed().forEach { replace(it.start, it.end, it.text) }
        }.toString()
        return ControlFlowBraces.compose(text, structural,
            whitespace(intermediate, ControlFlowBraces.shiftedRanges(ranges, structural)))
    }
}
