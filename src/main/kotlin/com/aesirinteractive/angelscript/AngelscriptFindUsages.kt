package com.aesirinteractive.angelscript

import com.aesirinteractive.angelscript.psi.*
import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.find.findUsages.FindUsagesHandlerFactory
import com.intellij.find.findUsages.FindUsagesOptions
import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.platform.lsp.api.LspClientManager
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.LspServerState
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.tree.TokenSet
import com.intellij.usageView.UsageInfo
import com.intellij.util.Processor
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.ReferenceContext
import org.eclipse.lsp4j.ReferenceParams

/** Enables Rider's standard declaration-or-usages action for native AngelScript PSI. */
class AngelscriptFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner() = DefaultWordsScanner(AngelscriptLexerAdapter(),
        AngelscriptTokenSets.IDENTIFIERS, AngelscriptTokenSets.COMMENTS,
        TokenSet.create(AngelscriptTypes.STRING_LITERAL, AngelscriptTypes.FSTRING_TEXT))

    override fun canFindUsagesFor(element: PsiElement) = element is AngelscriptNamedElement
    override fun getHelpId(element: PsiElement): String? = null
    override fun getType(element: PsiElement) = when (element) {
        is AngelscriptFunctionDecl, is AngelscriptConstructorDecl, is AngelscriptMixinDecl -> "function"
        is AngelscriptClassDecl -> "class"
        is AngelscriptStructDecl -> "struct"
        is AngelscriptInterfaceDecl -> "interface"
        is AngelscriptNamespaceDecl -> "namespace"
        is AngelscriptParameterDecl -> "parameter"
        else -> "symbol"
    }
    override fun getDescriptiveName(element: PsiElement) = (element as? AngelscriptNamedElement)?.name.orEmpty()
    override fun getNodeText(element: PsiElement, useFullName: Boolean) = getDescriptiveName(element)
}

/** A single native Find Usages entry queries semantic LSP references, with offline PSI fallback. */
class AngelscriptFindUsagesHandlerFactory : FindUsagesHandlerFactory() {
    override fun canFindUsages(element: PsiElement) = element is AngelscriptNamedElement
    override fun createFindUsagesHandler(element: PsiElement, forHighlightUsages: Boolean): FindUsagesHandler? =
        if (canFindUsages(element)) AngelscriptFindUsagesHandler(element) else null
}

internal class AngelscriptFindUsagesHandler(element: PsiElement) : FindUsagesHandler(element) {
    override fun processElementUsages(element: PsiElement, processor: Processor<in UsageInfo>, options: FindUsagesOptions): Boolean {
        if (!options.isUsages) return super.processElementUsages(element, processor, options)
        val requests = ReadAction.computeBlocking<List<Pair<LspClient, ReferenceParams>>, RuntimeException> {
            if (!element.isValid) return@computeBlocking emptyList()
            val virtualFile = element.containingFile?.virtualFile ?: return@computeBlocking emptyList()
            val servers = project.getService(LspClientManager::class.java)
                ?.getClients(AngelscriptLspServerSupportProvider::class.java).orEmpty()
                .filter { it.state == LspServerState.Running && it.descriptor.isSupportedFile(virtualFile) }
            if (servers.isEmpty()) return@computeBlocking emptyList()
            val document = FileDocumentManager.getInstance().getDocument(virtualFile) ?: return@computeBlocking emptyList()
            val offset = element.textOffset
            val line = document.getLineNumber(offset)
            val position = Position(line, offset - document.getLineStartOffset(line))
            servers.map { it to ReferenceParams(it.getDocumentIdentifier(virtualFile), position, ReferenceContext(false)) }
        }
        if (requests.isEmpty()) return super.processElementUsages(element, processor, options)
        val seen = mutableSetOf<Triple<String, Int, Int>>()
        var receivedResponse = false
        for ((server, params) in requests) {
            ProgressManager.checkCanceled()
            // Wait without holding a read lock so typing can continue during the search.
            val locations = server.sendRequestSync(10000) { it.textDocumentService.references(params) } ?: continue
            receivedResponse = true
            for (location in locations) {
                ProgressManager.checkCanceled()
                val usage = ReadAction.computeBlocking<UsageInfo?, RuntimeException> {
                    val targetFile = server.descriptor.findFileByUri(location.uri) ?: return@computeBlocking null
                    if (!options.searchScope.contains(targetFile)) return@computeBlocking null
                    val targetDocument = FileDocumentManager.getInstance().getDocument(targetFile) ?: return@computeBlocking null
                    fun toOffset(position: Position): Int? {
                        if (position.line !in 0 until targetDocument.lineCount || position.character < 0) return null
                        val result = targetDocument.getLineStartOffset(position.line) + position.character
                        return result.takeIf { it <= targetDocument.getLineEndOffset(position.line) }
                    }
                    val start = toOffset(location.range.start) ?: return@computeBlocking null
                    val end = toOffset(location.range.end)?.takeIf { it >= start } ?: return@computeBlocking null
                    val targetPsi = PsiManager.getInstance(project).findFile(targetFile) ?: return@computeBlocking null
                    val scope = options.searchScope
                    if (scope is LocalSearchScope && !scope.containsRange(targetPsi, com.intellij.openapi.util.TextRange(start, end))) return@computeBlocking null
                    if (!seen.add(Triple(targetFile.url, start, end))) return@computeBlocking null
                    UsageInfo(targetPsi, start, end)
                } ?: continue
                if (!processor.process(usage)) return false
            }
        }
        if (!receivedResponse) return super.processElementUsages(element, processor, options)
        val scope = options.searchScope
        return !options.isSearchForTextOccurrences || scope !is GlobalSearchScope || processUsagesInText(element, processor, scope)
    }
}
