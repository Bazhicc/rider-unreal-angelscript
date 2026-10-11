package com.aesirinteractive.angelscript

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.platform.lsp.api.Lsp4jClient
import com.intellij.platform.lsp.api.LspServerDescriptor
import com.intellij.platform.lsp.api.LspServerNotificationsHandler
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification

/** Bridge the official asset-edit workflow's save notification. */
class AngelscriptLanguageClient(
    handler: LspServerNotificationsHandler,
    private val project: Project,
    private val descriptor: LspServerDescriptor,
) : Lsp4jClient(handler) {
    @JsonNotification("angelscript/unrealStatus")
    fun unrealStatus(status: AngelscriptUnrealStatus) {
        if (!project.isDisposed) project.service<AngelscriptUnrealStatusService>().status = status
    }

    @JsonNotification("angelscript/wantSave")
    fun wantSave(uris: List<String>) {
        ApplicationManager.getApplication().invokeLater {
            if (project.isDisposed) return@invokeLater
            val documents = FileDocumentManager.getInstance()
            for (uri in uris) {
                val file = descriptor.findFileByUri(uri) ?: continue
                if (!descriptor.isSupportedFile(file)) continue
                val document = documents.getCachedDocument(file) ?: continue
                documents.saveDocument(document)
            }
        }
    }
}

data class AngelscriptUnrealStatus(val state: String, val host: String, val port: Int)

@Service(Service.Level.PROJECT)
class AngelscriptUnrealStatusService {
    @Volatile var status: AngelscriptUnrealStatus? = null
}
