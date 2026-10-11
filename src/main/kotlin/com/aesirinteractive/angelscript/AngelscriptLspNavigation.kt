package com.aesirinteractive.angelscript

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.platform.lsp.api.LspServerState
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiPolyVariantReference
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.Position

/** Native references remain available even when Rider has no running language server. */
class AngelscriptLspNavigation : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor): Array<PsiElement>? {
        val file = sourceElement?.containingFile ?: return null
        if (file.language != AngelscriptLanguage.INSTANCE) return null
        fun fallback(): Array<PsiElement>? {
            val reference = file.findReferenceAt(offset)
            val targets = when (reference) {
                is PsiPolyVariantReference -> reference.multiResolve(false).mapNotNull { it.element }
                null -> emptyList()
                else -> listOfNotNull(reference.resolve())
            }
            return targets.toTypedArray().takeIf { it.isNotEmpty() }
        }
        val virtualFile = file.virtualFile ?: return fallback()
        val servers = file.project.getService(LspClientManager::class.java)?.getClients(AngelscriptLspServerSupportProvider::class.java)
            .orEmpty()
            .filter { it.state == LspServerState.Running && it.descriptor.isSupportedFile(virtualFile) }
        if (servers.isEmpty()) return fallback()
        val document = editor.document
        if (offset !in 0 until document.textLength) return null
        val line = document.getLineNumber(offset)
        val position = Position(line, offset - document.getLineStartOffset(line))
        val targets = mutableListOf<PsiElement>()
        var resolvedNativeSymbol = false
        for (server in servers) {
            ProgressManager.checkCanceled()
            if (server.state != LspServerState.Running || !server.descriptor.isSupportedFile(virtualFile)) continue
            val params = DefinitionParams(server.getDocumentIdentifier(virtualFile), position)
            val response = server.sendRequestSync(3000) { it.textDocumentService.definition(params) }
            val locations = if (response == null) emptyList() else if (response.isLeft) response.left.map { it.uri to it.range.start }
                else response.right.map { it.targetUri to it.targetSelectionRange.start }
            for ((uri, start) in locations) {
                ProgressManager.checkCanceled()
                val targetFile = server.descriptor.findFileByUri(uri) ?: continue
                val targetDocument = FileDocumentManager.getInstance().getDocument(targetFile) ?: continue
                if (start.line !in 0 until targetDocument.lineCount || start.character < 0) continue
                val targetOffset = targetDocument.getLineStartOffset(start.line) + start.character
                if (targetOffset > targetDocument.getLineEndOffset(start.line)) continue
                val psi = PsiManager.getInstance(file.project).findFile(targetFile) ?: continue
                psi.findElementAt(targetOffset)?.let(targets::add)
            }
            if (targets.isNotEmpty()) continue
            if (!supportsCppSymbols(server.initializeResult?.capabilities?.experimental)) continue
            // The official definition request only returns .as locations. Native UE symbols
            // need their declaring type (including inherited members and binding aliases).
            val symbol = server.sendRequestSync(3000) {
                (it as AngelscriptLanguageServer).cppSymbol(params)
            } ?: continue
            resolvedNativeSymbol = true
            val headers = (listOf(symbol) + symbol.alternatives.orEmpty()).flatMap {
                AngelscriptCppNavigation.resolveNativeSymbol(file.project, it)
            }
            if (headers.isNotEmpty()) targets.addAll(headers)
            else if (symbol.canNavigateViaUnreal) {
                targets.add(AngelscriptUnrealTarget.create(file, symbol, server, params))
            }
        }
        // The LSP knows the receiver and overload context. A name-only PSI fallback
        // must not preempt it with an unrelated class member from another file.
        if (targets.isEmpty() && !resolvedNativeSymbol) {
            return fallback()
        }
        return targets.distinctBy { Triple(it.containingFile.virtualFile, it.textOffset, (it as? AngelscriptUnrealTarget)?.name) }
            .toTypedArray().takeIf { it.isNotEmpty() }
    }
}

internal fun supportsCppSymbols(experimental: Any?): Boolean = when (experimental) {
    is com.google.gson.JsonObject -> experimental["angelscriptCppSymbol"]?.let { it.isJsonPrimitive && it.asString == "true" } == true
    is Map<*, *> -> experimental["angelscriptCppSymbol"] == true
    else -> false
}
