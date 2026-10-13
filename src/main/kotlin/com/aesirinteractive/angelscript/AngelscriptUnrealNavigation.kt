package com.aesirinteractive.angelscript

import com.intellij.openapi.application.ApplicationManager
import com.intellij.platform.lsp.api.LspServer
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
    @JsonRequest("angelscript/navigation")
    fun navigation(params: TextDocumentPositionParams): CompletableFuture<AngelscriptNavigationResponse?>
    @JsonRequest("angelscript/nativeReferences")
    fun nativeReferences(params: AngelscriptNativeReferencesParams): CompletableFuture<List<AngelscriptNativeReference>?>
}

data class AngelscriptNavigationResponse(val definitions: List<org.eclipse.lsp4j.Location>, val symbol: AngelscriptCppSymbol?)
data class AngelscriptNativeReferencesParams(val names: List<String>, val cppName: String)
data class AngelscriptNativeReference(val location: org.eclipse.lsp4j.Location, val symbol: AngelscriptCppSymbol, val text: String)

data class AngelscriptCppSymbol(
    val typeName: String,
    val symbolName: String,
    val cppName: String,
    val kind: String,
    val canNavigateViaUnreal: Boolean,
    val alternatives: List<AngelscriptCppSymbol>? = null,
    val sourceOnly: Boolean = false,
    val receiver: AngelscriptNativeReceiver? = null,
    val parameterTypes: List<String>? = null,
)

/** Source-derived receiver operations whose return types are supplied by native binding signatures. */
data class AngelscriptNativeReceiver(val typeName: String, val steps: List<AngelscriptReceiverStep>)
data class AngelscriptReceiverStep(val kind: String, val name: String = "")

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
        fun create(source: PsiElement, symbol: AngelscriptCppSymbol, server: LspServer,
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
