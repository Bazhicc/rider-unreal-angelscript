package com.aesirinteractive.angelscript

import com.intellij.openapi.application.ApplicationManager
import com.intellij.platform.lsp.api.LspClient
import com.intellij.platform.lsp.api.LspServerState
import com.intellij.psi.PsiElement
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.impl.FakePsiElement
import org.eclipse.lsp4j.ImplementationParams
import org.eclipse.lsp4j.TextDocumentPositionParams
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest
import org.eclipse.lsp4j.services.LanguageServer
import java.util.concurrent.CompletableFuture

/** Resolve native ownership without asking UE to open an editor during Ctrl+hover. */
interface AngelscriptLanguageServer : LanguageServer {
    @JsonRequest("angelscript/cppSymbol")
    fun cppSymbol(params: TextDocumentPositionParams): CompletableFuture<AngelscriptCppSymbol?>
}

data class AngelscriptCppSymbol(
    val typeName: String,
    val symbolName: String,
    val cppName: String,
    val kind: String,
    val canNavigateViaUnreal: Boolean,
    val alternatives: List<AngelscriptCppSymbol>? = null,
    val sourceOnly: Boolean = false,
)

/** UE owns the binding-to-UFunction mapping and invokes its configured source-code accessor. */
internal class AngelscriptUnrealTarget(
    private val source: PsiElement,
    private val symbol: AngelscriptCppSymbol,
    private val open: () -> Unit,
) : FakePsiElement(), NavigatablePsiElement {
    override fun getParent(): PsiElement = source
    override fun getName(): String = "${symbol.typeName}::${symbol.symbolName}"
    override fun canNavigate() = source.isValid
    override fun canNavigateToSource() = canNavigate()
    override fun navigate(requestFocus: Boolean) = open()

    companion object {
        fun create(source: PsiElement, symbol: AngelscriptCppSymbol, server: LspClient,
                   params: TextDocumentPositionParams): AngelscriptUnrealTarget =
            AngelscriptUnrealTarget(source, symbol) {
                ApplicationManager.getApplication().executeOnPooledThread {
                    if (!source.project.isDisposed && server.state == LspServerState.Running) {
                        server.sendRequestSync(3000) {
                            it.textDocumentService.implementation(ImplementationParams(params.textDocument, params.position))
                        }
                    }
                }
            }
    }
}
