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
import com.aesirinteractive.angelscript.psi.*
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.Position

/** Native references remain available even when Rider has no running language server. */
class AngelscriptLspNavigation : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor): Array<PsiElement>? {
        val file = sourceElement?.containingFile ?: return null
        if (file.language != AngelscriptLanguage.INSTANCE) return null
        // Returning a definition here preempts Rider's declaration-or-usages action.
        if (angelscriptDeclarationAt(sourceElement, offset) != null) return null
        fun fallback(): Array<PsiElement>? {
            val reference = file.findReferenceAt(offset)
            val targets = when (reference) {
                is PsiPolyVariantReference -> reference.multiResolve(false).mapNotNull { it.element }
                null -> emptyList()
                else -> listOfNotNull(reference.resolve())
            }
            return declarationNavigationTargets(targets)
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
        return declarationNavigationTargets(targets.distinctBy { Triple(it.containingFile.virtualFile, it.textOffset, (it as? AngelscriptUnrealTarget)?.name) })
    }
}

/** Only the declaration's name is a declaration; its return type, arguments and body are references. */
internal fun angelscriptDeclarationAt(sourceElement: PsiElement, offset: Int): AngelscriptNamedElement? =
    generateSequence(sourceElement) { it.parent }.filterIsInstance<AngelscriptNamedElement>().firstOrNull {
        val name = it.nameIdentifier ?: return@firstOrNull false
        offset in name.textRange.startOffset until name.textRange.endOffset ||
            (name == sourceElement && offset == name.textRange.endOffset)
    }

/** Keep single script targets as real PSI; ambiguous function lists need the same detail as native lists. */
internal fun presentNavigationTargets(targets: List<PsiElement>): List<PsiElement> {
    if (targets.size < 2) return targets
    return targets.map { target ->
        if (target.containingFile?.language != AngelscriptLanguage.INSTANCE || target is AngelscriptCppSourceTarget) return@map target
        val declaration = generateSequence(target) { it.parent }.firstOrNull { it is AngelscriptFunctionDecl }
            as? AngelscriptFunctionDecl ?: return@map target
        val owner = generateSequence(declaration.parent) { it.parent }.filter {
            it is AngelscriptClassDecl || it is AngelscriptStructDecl || it is AngelscriptInterfaceDecl || it is AngelscriptNamespaceDecl
        }.mapNotNull { (it as? AngelscriptNamedElement)?.name }.toList().asReversed().joinToString("::")
        val file = target.containingFile
        AngelscriptCppSourceTarget(file, declaration.textOffset,
            AngelscriptCppDeclarations.signature(file.text, declaration.textOffset, owner, "function"))
    }
}

internal fun supportsCppSymbols(experimental: Any?): Boolean = when (experimental) {
    is com.google.gson.JsonObject -> experimental["angelscriptCppSymbol"]?.let { it.isJsonPrimitive && it.asString == "true" } == true
    is Map<*, *> -> experimental["angelscriptCppSymbol"] == true
    else -> false
}
