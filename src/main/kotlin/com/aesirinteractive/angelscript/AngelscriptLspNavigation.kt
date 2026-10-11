package com.aesirinteractive.angelscript

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.platform.lsp.api.LspServerManager
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
        val reference = file.findReferenceAt(offset)
        val nativeTargets = when (reference) {
            is PsiPolyVariantReference -> reference.multiResolve(false).mapNotNull { it.element }
            null -> emptyList()
            else -> listOfNotNull(reference.resolve())
        }
        if (nativeTargets.isNotEmpty()) return nativeTargets.toTypedArray()
        val virtualFile = file.virtualFile ?: return null
        val document = editor.document
        if (offset !in 0 until document.textLength) return null
        val line = document.getLineNumber(offset)
        val position = Position(line, offset - document.getLineStartOffset(line))
        val targets = mutableListOf<PsiElement>()
        for (server in LspServerManager.getInstance(file.project)
            .getServersForProvider(AngelscriptLspServerSupportProvider::class.java)) {
            ProgressManager.checkCanceled()
            if (server.state != LspServerState.Running || !server.descriptor.isSupportedFile(virtualFile)) continue
            val params = DefinitionParams(server.getDocumentIdentifier(virtualFile), position)
            val response = server.sendRequestSync(3000) { it.textDocumentService.definition(params) } ?: continue
            val locations = if (response.isLeft) response.left.map { it.uri to it.range.start }
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
        }
        return targets.distinctBy { it.containingFile.virtualFile to it.textOffset }
            .toTypedArray().takeIf { it.isNotEmpty() }
    }
}
