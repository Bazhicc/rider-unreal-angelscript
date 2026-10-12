package com.aesirinteractive.angelscript

import com.google.gson.Gson
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.platform.lsp.api.LspServerState
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

internal data class AngelscriptNativeDeclaration(val path: String, val offset: Int, val line: Int? = null, val character: Int? = null)
internal data class AngelscriptNativeUsageQuery(val cppName: String, val declarations: List<AngelscriptNativeDeclaration>)
internal data class AngelscriptNativeUsageRange(val path: String, val start: Int, val end: Int,
    val startLine: Int, val startCharacter: Int, val endLine: Int, val endCharacter: Int)
internal data class AngelscriptNativeUsageResponse(val usages: List<AngelscriptNativeUsageRange>)

/** The native backend supplies declaration identities; the existing Kotlin resolver checks each AS target. */
@Service(Service.Level.PROJECT)
internal class AngelscriptNativeUsageBridge(private val project: Project) : Disposable {
    private val gson = Gson()
    private val token = UUID.randomUUID().toString()
    private val executor = Executors.newFixedThreadPool(2) { runnable ->
        Thread(runnable, "AngelScript native usages").apply { isDaemon = true }
    }
    private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        executor = this@AngelscriptNativeUsageBridge.executor
        createContext("/references") { exchange ->
            exchange.use {
                if (project.isDisposed || exchange.requestMethod != "POST" ||
                    exchange.requestHeaders.getFirst("Authorization") != "Bearer $token") {
                    exchange.sendResponseHeaders(403, -1)
                    return@createContext
                }
                try {
                    val body = exchange.requestBody.readNBytes(1024 * 1024 + 1)
                    require(body.size <= 1024 * 1024) { "Native usage request is too large" }
                    val query = gson.fromJson(body.toString(Charsets.UTF_8), AngelscriptNativeUsageQuery::class.java)
                    val result = gson.toJson(AngelscriptNativeUsageResponse(find(query))).toByteArray(Charsets.UTF_8)
                    exchange.responseHeaders.set("Content-Type", "application/json; charset=utf-8")
                    exchange.sendResponseHeaders(200, result.size.toLong())
                    exchange.responseBody.write(result)
                } catch (exception: Exception) {
                    logger<AngelscriptNativeUsageBridge>().warn("Unable to search native AngelScript usages", exception)
                    exchange.sendResponseHeaders(503, -1)
                }
            }
        }
        start()
    }

    init {
        val directory = Path.of(requireNotNull(project.basePath), ".idea")
        Files.createDirectories(directory)
        Files.writeString(directory.resolve("angelscript-native-bridge.json"), gson.toJson(mapOf(
            "url" to "http://127.0.0.1:${server.address.port}/references", "token" to token)))
    }

    internal fun find(query: AngelscriptNativeUsageQuery): List<AngelscriptNativeUsageRange> {
        val requestDeadline = System.nanoTime() + 25_000_000_000L
        fun remainingMillis(limit: Long): Long {
            val remaining = TimeUnit.NANOSECONDS.toMillis(requestDeadline - System.nanoTime())
            check(remaining > 0) { "Native reference search timed out" }
            return minOf(limit, remaining)
        }
        require(query.cppName.matches(Regex("[\\p{L}_][\\p{L}\\p{N}_]*")))
        check(!DumbService.isDumb(project)) { "Rider native source indexing is in progress" }
        val index = nativeUsageRead(project, remainingMillis(10_000)) {
            project.service<AngelscriptNativeNavigationCache>().current()
        }
        val names = nativeUsageRead(project, remainingMillis(10_000)) { index.scriptNames(query.cppName) }
        val manager = LspClientManager.getInstance(project)
        if (manager.getClients(AngelscriptLspServerSupportProvider::class.java).none { it.state == LspServerState.Running }) {
            ApplicationManager.getApplication().invokeLater {
                if (!project.isDisposed) AngelscriptLspServerSupportProvider.ensureStarted(project)
            }
        }
        val deadline = minOf(requestDeadline, System.nanoTime() + 15_000_000_000L)
        var clients = manager.getClients(AngelscriptLspServerSupportProvider::class.java).filter { it.state == LspServerState.Running }
        while (clients.isEmpty() && !project.isDisposed && System.nanoTime() < deadline) {
            Thread.sleep(50)
            clients = manager.getClients(AngelscriptLspServerSupportProvider::class.java).filter { it.state == LspServerState.Running }
        }
        check(clients.isNotEmpty()) { "AngelScript language server is unavailable" }
        val usages = clients.flatMap { client ->
            check(supportsAngelscriptCapability(client.initializeResult?.capabilities?.experimental, "angelscriptNativeReferences")) {
                "The configured language server does not support native reference searches"
            }
            client.sendRequestSync(remainingMillis(15_000).toInt()) { (it as AngelscriptLanguageServer).nativeReferences(AngelscriptNativeReferencesParams(names, query.cppName)) }
                ?: error("AngelScript native reference search did not complete")
        }
        return nativeUsageRead(project, remainingMillis(10_000)) {
            check(index === project.service<AngelscriptNativeNavigationCache>().current()) { "Native sources changed during search; retry Find Usages" }
            val declarations = query.declarations.mapNotNull { declaration ->
                val file = LocalFileSystem.getInstance().findFileByPath(declaration.path) ?: return@mapNotNull null
                val document = FileDocumentManager.getInstance().getDocument(file) ?: return@mapNotNull null
                val line = declaration.line
                val character = declaration.character
                val offset = if (line != null && character != null) {
                    if (line !in 0 until document.lineCount || character < 0 ||
                        character > document.getLineEndOffset(line) - document.getLineStartOffset(line)) return@mapNotNull null
                    document.getLineStartOffset(line) + character
                } else declaration.offset
                normalizeNativePath(file.path) to offset
            }.toSet()
            usages.mapNotNull { usage ->
                if (!index.matches(usage.symbol) { file, offset -> normalizeNativePath(file.virtualFile.path) to offset in declarations }) return@mapNotNull null
                val file = LocalFileSystem.getInstance().findFileByPath(java.net.URI(usage.location.uri).let { Path.of(it).toString() }) ?: return@mapNotNull null
                val document = FileDocumentManager.getInstance().getDocument(file) ?: return@mapNotNull null
                val range = usage.location.range
                if (range.start.line !in 0 until document.lineCount || range.end.line !in 0 until document.lineCount) return@mapNotNull null
                val start = document.getLineStartOffset(range.start.line) + range.start.character
                val end = document.getLineStartOffset(range.end.line) + range.end.character
                if (range.start.character < 0 || range.end.character < 0 || end <= start ||
                    start > document.getLineEndOffset(range.start.line) || end > document.getLineEndOffset(range.end.line) ||
                    document.charsSequence.subSequence(start, end).toString() != usage.text) return@mapNotNull null
                AngelscriptNativeUsageRange(file.path, start, end, range.start.line, range.start.character, range.end.line, range.end.character)
            }.distinct()
        }
    }

    override fun dispose() { server.stop(0); executor.shutdownNow() }
}

/** HTTP workers have no platform progress context. Install a cancellable read that yields to writes. */
internal fun <T : Any> nativeUsageRead(lifetime: Disposable, timeoutMillis: Long = 10_000, action: () -> T): T {
    val promise = ReadAction.nonBlocking(Callable { action() }).expireWith(lifetime)
        .submit(AppExecutorUtil.getAppExecutorService())
    try {
        return promise.get(timeoutMillis, TimeUnit.MILLISECONDS)
    } finally {
        // A timed-out/disconnected backend search must not leave a read action running indefinitely.
        promise.cancel()
    }
}

internal fun normalizeNativePath(path: String) = path.replace('\\', '/').let {
    if (com.intellij.openapi.util.SystemInfoRt.isFileSystemCaseSensitive) it else it.lowercase(java.util.Locale.ROOT)
}

class AngelscriptNativeUsageStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        if (project.basePath != null) project.service<AngelscriptNativeUsageBridge>()
    }
}
