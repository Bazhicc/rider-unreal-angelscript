package com.aesirinteractive.angelscript

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.*
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerState
import com.intellij.platform.lsp.api.lsWidget.LspServerWidgetItem
import com.intellij.platform.lsp.api.LspServerSupportProvider
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import com.intellij.platform.lsp.api.customization.LspCustomization
import com.intellij.platform.lsp.api.customization.LspSemanticTokensSupport
import com.intellij.platform.lsp.api.customization.LspDocumentSymbolSupport
import com.intellij.platform.lsp.api.customization.LspCommandsSupport
import com.intellij.platform.lsp.api.customization.LspRenameSupport
import com.intellij.psi.PsiFile
import com.intellij.openapi.editor.colors.TextAttributesKey
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.WorkspaceFolder

private val LOG = logger<AngelscriptLspServerSupportProvider>()

internal class AngelscriptLspServerSupportProvider : LspServerSupportProvider {
    private class AngelLspServerDescriptor(project: Project) : ProjectWideLspServerDescriptor(project, "AngelScript") {
        override val lspCustomization: LspCustomization = AngelscriptLspCustomization()

        override fun createLsp4jClient(handler: LspServerNotificationsHandler): Lsp4jClient =
            AngelscriptLanguageClient(handler, project, this)

        override fun getLanguageId(file: VirtualFile) = "angelscript"

        override fun createInitializeParams(): InitializeParams = super.createInitializeParams().also { params ->
            project.basePath?.let { base ->
                val roots = AngelscriptWorkspace.scriptRoots(Path.of(base))
                params.workspaceFolders = roots.map { WorkspaceFolder(it.toUri().toString(), it.fileName.toString()) }
                params.rootUri = roots.first().toUri().toString()
                params.rootPath = roots.first().toString()
            }
        }

        override fun createInitializationOptions(): Any {
            val settings = AngelscriptSettings.getInstance()
            return mapOf("unrealAngelscript" to mapOf(
                "unrealConnectionHost" to settings.debugHost,
                "unrealConnectionPort" to settings.debugPort,
            ))
        }

        override fun isSupportedFile(file: VirtualFile) =
            file.extension != null && file.extension in AngelscriptSettings.getInstance().parsedFileExtensions()

        override fun createCommandLine(): GeneralCommandLine {
            project.service<AngelscriptUnrealStatusService>().status = null
            val settings = AngelscriptSettings.getInstance()

            if (settings.lspPathKind == LspPathKind.CommandLine) {
                val cmd = settings.customCommandLine
                if (cmd.isBlank()) {
                    val msg = "AngelScript LSP: custom command line is empty — cannot start server"
                    LOG.warn(msg)
                    notifyError("Language server not configured", msg)
                    throw ExecutionException(msg)
                }
                val tokens = ParametersList.parse(cmd)
                LOG.info("AngelScript LSP starting (CommandLine mode): $cmd")
                return GeneralCommandLine(tokens.toList())
            }

            val node = AngelscriptWorkspace.nodeExecutable(settings.nodePath)
            val lsp = when (settings.lspPathKind) {
                LspPathKind.Bundled -> {
                    val path = extractBundledLspServer()
                    if (path == null) {
                        val msg = "AngelScript LSP: bundled server.js not found in plugin resources — server cannot start"
                        LOG.error(msg)
                        notifyError("Bundled language server not found", msg)
                        throw ExecutionException(msg)
                    }
                    path
                }
                LspPathKind.VsCode -> {
                    val path = findDefaultLspPath()
                    if (path == null) {
                        val vsExt = Path.of(System.getProperty("user.home") ?: "~", ".vscode", "extensions").toString()
                        val msg = "AngelScript LSP: VS Code extension 'hazelight.unreal-angelscript-*' not found under $vsExt"
                        LOG.warn(msg)
                        notifyError("VS Code extension not found", msg)
                        throw ExecutionException(msg)
                    }
                    path
                }
                LspPathKind.Custom -> {
                    val path = settings.lspPath
                    if (path.isBlank()) {
                        val msg = "AngelScript LSP: custom server path is empty — cannot start server"
                        LOG.warn(msg)
                        notifyError("Language server path not configured", msg)
                        throw ExecutionException(msg)
                    }
                    if (!Files.exists(Path.of(path))) {
                        val msg = "AngelScript LSP: custom server path does not exist: $path"
                        LOG.warn(msg)
                        notifyError("Language server not found", msg)
                        throw ExecutionException(msg)
                    }
                    path
                }
                LspPathKind.CommandLine -> "" // handled above
            }

            // Verify node is resolvable (best-effort: check absolute path exists)
            if (node != "node" && !Files.exists(Path.of(node))) {
                val msg = "AngelScript LSP: node binary not found at '$node' — server may fail to start"
                LOG.warn(msg)
                notifyWarning("Node not found", msg)
            }

            LOG.info("AngelScript LSP starting (${settings.lspPathKind}): node='$node' server='$lsp'")
            return GeneralCommandLine(node, lsp, "--stdio")
        }

        // Each bundled version gets immutable storage; simultaneous projects never rewrite a running server.
        private fun extractBundledLspServer(): String? {
            val stream = AngelscriptLspServerSupportProvider::class.java.getResourceAsStream("/lsp/server.js")
            if (stream == null) {
                LOG.error("AngelScript LSP: /lsp/server.js not found in plugin JAR resources")
                return null
            }
            val bytes = stream.use { it.readBytes() }
            val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
            val target = Path.of(PathManager.getSystemPath(), "unreal-angelscript", "lsp", hash, "server.js")
            synchronized(AngelscriptLspServerSupportProvider::class.java) {
                Files.createDirectories(target.parent)
                if (!Files.exists(target)) Files.write(target, bytes)
            }
            return target.toString()
        }

        private fun findDefaultLspPath(): String? {
            val home = System.getProperty("user.home") ?: return null
            val pattern = "hazelight.unreal-angelscript-*"
            val matcher = FileSystems.getDefault().getPathMatcher("glob:$pattern")
            val base = Path.of(home, ".vscode", "extensions")
            LOG.info("AngelScript LSP: searching for VS Code extension under $base")
            if (!Files.isDirectory(base)) {
                LOG.warn("AngelScript LSP: VS Code extensions directory not found: $base")
                return null
            }
            return Files.list(base).use { candidates ->
                candidates.filter { matcher.matches(it.fileName) }
                    .map { it.resolve("language-server/dist/server.js") }
                    .filter { Files.isRegularFile(it) }
                    .max(Comparator.comparingLong { Files.getLastModifiedTime(it).toMillis() })
                    .map(Path::toString).orElse(null)
            }
        }

        private fun notifyWarning(title: String, content: String) =
            NotificationGroupManager.getInstance()
                .getNotificationGroup("AngelScript LSP")
                .createNotification(title, content, NotificationType.WARNING)
                .notify(project)

        private fun notifyError(title: String, content: String) =
            NotificationGroupManager.getInstance()
                .getNotificationGroup("AngelScript LSP")
                .createNotification(title, content, NotificationType.ERROR)
                .notify(project)
    }

    override fun fileOpened(project: Project, file: VirtualFile, serverStarter: LspServerSupportProvider.LspServerStarter) {
        if (file.extension != null && file.extension in AngelscriptSettings.getInstance().parsedFileExtensions()) {
            serverStarter.ensureServerStarted(AngelLspServerDescriptor(project))
        }
    }

    override fun createLspServerWidgetItem(lspServer: LspServer, currentFile: VirtualFile?): LspServerWidgetItem =
        object : LspServerWidgetItem(lspServer, currentFile, AngelscriptFileType.INSTANCE.icon, AngelscriptSettingsConfigurable::class.java) {
            override val statusBarTooltip: String
                get() {
                    if (lspServer.state != LspServerState.Running) return super.statusBarTooltip
                    val status = lspServer.project.service<AngelscriptUnrealStatusService>().status
                    val detail = when (status?.state) {
                        "ready" -> "UE types loaded"
                        "loading" -> "Loading UE types"
                        "disconnected" -> "UE disconnected; retrying"
                        else -> "UE connection status unavailable"
                    }
                    val endpoint = status?.let { " (${it.host}:${it.port})" } ?: ""
                    return "${super.statusBarTooltip} — $detail$endpoint"
                }
        }
}

/** Restart the server after settings used by its command line change. */
internal fun restartAngelscriptLspServers() {
    for (project in ProjectManager.getInstance().openProjects) {
        if (project.isDisposed) continue
        LspServerManager.getInstance(project)
            .stopAndRestartIfNeeded(AngelscriptLspServerSupportProvider::class.java)
    }
}

class AngelscriptLspCustomization : LspCustomization() {
    // Our public RenameHandler applies the complete workspace edit as one global undo command.
    // Rider's inline LSP rename otherwise leaves the initiating file changed after cross-file undo.
    override val renameCustomizer = object : LspRenameSupport() {
        override fun shouldRunRename(psiFile: PsiFile) =
            psiFile.language != AngelscriptLanguage.INSTANCE && super.shouldRunRename(psiFile)
    }
    override val semanticTokensCustomizer = object : LspSemanticTokensSupport() {
        override val tokenTypes: List<String> = AngelscriptColors.semantic.keys.toList()
        override fun getTextAttributesKey(tokenType: String, modifiers: List<String>): TextAttributesKey? =
            AngelscriptColors.semantic[tokenType] ?: super.getTextAttributesKey(tokenType, modifiers)
    }
    override val documentSymbolCustomizer = LspDocumentSymbolSupport()
    override val commandsCustomizer = LspCommandsSupport()
}
