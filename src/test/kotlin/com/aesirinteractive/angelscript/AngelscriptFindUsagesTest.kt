@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package com.aesirinteractive.angelscript

import com.intellij.codeInsight.navigation.impl.SymbolNavigationServiceImpl
import com.intellij.ide.util.ModuleRendererFactory
import com.intellij.ide.IconProvider
import com.intellij.find.actions.FindUsagesAction
import com.intellij.find.actions.UsageVariantHandler
import com.intellij.find.actions.findShowUsages
import com.intellij.find.actions.targetVariants
import com.intellij.find.findUsages.FindUsagesOptions
import com.intellij.find.findUsages.PsiElement2UsageTargetAdapter
import com.intellij.find.usages.api.SearchTarget
import com.intellij.mock.MockPsiDocumentManager
import com.intellij.mock.MockSmartPointerManager
import com.intellij.navigation.SymbolNavigationService
import com.intellij.codeInsight.multiverse.CodeInsightContext
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.module.Module
import com.intellij.openapi.vcs.FileStatus
import com.intellij.openapi.vcs.FileStatusManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerDescriptor
import com.intellij.platform.lsp.api.LspServerState
import com.intellij.platform.lsp.api.customization.LspCustomization
import com.intellij.platform.lsp.api.customization.LspFindReferencesSupport
import com.intellij.problems.Problem
import com.intellij.problems.WolfTheProblemSolver
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeListener
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiFileRange
import com.intellij.psi.search.LocalSearchScope
import com.intellij.testFramework.ParsingTestCase
import com.intellij.ui.awt.RelativePoint
import com.intellij.usageView.UsageInfo
import com.intellij.usages.UsageInfo2UsageAdapter
import com.intellij.usages.UsageView
import com.intellij.util.Processor
import com.intellij.util.TextWithIcon
import org.eclipse.lsp4j.Location
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.services.LanguageServer
import org.eclipse.lsp4j.services.TextDocumentService
import java.awt.Point
import java.lang.reflect.Proxy
import java.util.concurrent.CompletableFuture
import javax.swing.JPanel

/** Runs Rider's target collection and dispatch, without creating a backend solution. */
class AngelscriptFindUsagesTest : ParsingTestCase("", "as", AngelscriptParserDefinition()) {
    override fun getTestDataPath() = ""

    override fun setUp() {
        super.setUp()
        project.registerService(SmartPointerManager::class.java, object : MockSmartPointerManager(project) {
            override fun createSmartPsiFileRangePointer(file: PsiFile, range: TextRange): SmartPsiFileRange =
                object : SmartPsiFileRange {
                    override fun getElement() = file
                    override fun getContainingFile() = file
                    override fun getProject() = file.project
                    override fun getVirtualFile() = file.virtualFile
                    override fun getRange() = range
                    override fun getPsiRange() = range
                }
        })
        application.registerService(SymbolNavigationService::class.java, SymbolNavigationServiceImpl())
        registerExtension(ExtensionPointName.create("com.intellij.moduleRendererFactory"), object : ModuleRendererFactory() {
            override fun getModuleTextWithIcon(element: Any): TextWithIcon? = null
        })
        registerExtensionPoint(EditorTabColorProvider.EP_NAME, EditorTabColorProvider::class.java)
        registerExtensionPoint(IconProvider.EXTENSION_POINT_NAME, IconProvider::class.java)
        val status = Proxy.newProxyInstance(FileStatus::class.java.classLoader, arrayOf(FileStatus::class.java)) { _, method, _ ->
            check(method.name == "getColor")
            null
        } as FileStatus
        project.registerService(FileStatusManager::class.java, object : FileStatusManager() {
            override fun getStatus(file: VirtualFile) = status
            override fun getRecursiveStatus(file: VirtualFile) = status
            override fun fileStatusesChanged() = Unit
            override fun fileStatusChanged(file: VirtualFile?) = Unit
        })
        project.registerService(WolfTheProblemSolver::class.java, object : WolfTheProblemSolver() {
            override fun isProblemFile(file: VirtualFile) = false
            override fun weHaveGotProblems(file: VirtualFile, problems: List<Problem>) = Unit
            override fun weHaveGotNonIgnorableProblems(file: VirtualFile, problems: List<Problem>) = Unit
            override fun clearProblems(file: VirtualFile) = Unit
            override fun hasProblemFilesBeneath(condition: Condition<in VirtualFile>) = false
            override fun hasProblemFilesBeneath(module: Module) = false
            override fun convertToProblem(file: VirtualFile, line: Int, column: Int, messages: Array<out String>): Problem = error("Unused")
            override fun reportProblems(file: VirtualFile, problems: Collection<Problem>) = Unit
            override fun hasSyntaxErrors(file: VirtualFile) = false
            override fun reportProblemsFromExternalSource(file: VirtualFile, source: Any) = Unit
            override fun clearProblemsFromExternalSource(file: VirtualFile, source: Any) = Unit
            override fun queue(file: VirtualFile) = Unit
        })
    }

    private fun declaration(): PsiElement {
        val file = parseFile("FindUsages.as", "int Target() { return 1; } int Use() { return Target(); }")
        val offset = file.text.indexOf("Target")
        return angelscriptDeclarationAt(file.findElementAt(offset)!!, offset)!!
    }

    private fun context(declaration: PsiElement): DataContext {
        val customization: LspCustomization = AngelscriptLspCustomization()
        val nativeTarget = PsiElement2UsageTargetAdapter(declaration,
            FindUsagesOptions(LocalSearchScope(declaration.containingFile)), false)
        // This is the eligibility check used by Rider's LspSearchTargetsRule. When enabled,
        // it advertises a second target for the same editor position, alongside native PSI.
        val lspTarget = Proxy.newProxyInstance(SearchTarget::class.java.classLoader,
            arrayOf(SearchTarget::class.java)) { _, method, _ ->
            when (method.name) {
                "presentation" -> TargetPresentation.builder("Target").presentation()
                else -> error("Unexpected generic LSP target access: ${method.name}")
            }
        } as SearchTarget
        return DataContext { key ->
            when (key) {
                UsageView.USAGE_TARGETS_KEY.name -> arrayOf(nativeTarget)
                FindUsagesAction.SEARCH_TARGETS.name ->
                    if (customization.findReferencesCustomizer is LspFindReferencesSupport)
                        listOf(lspTarget) else null
                else -> null
            }
        }
    }

    fun testFindUsagesCollectsOnlyOneTarget() {
        val declaration = declaration()
        assertEquals("A symbol must not need a native-versus-LSP target chooser", 1,
            targetVariants(context(declaration)).size)
    }

    fun testShowUsagesDispatchesDirectlyToTheDeclaration() {
        val declaration = declaration()
        var searched: PsiElement? = null
        // No popup factory is installed: attempting to choose a target will fail this test.
        findShowUsages(project, null, RelativePoint(JPanel(), Point(0, 0)), targetVariants(context(declaration)),
            "Unexpected target chooser", object : UsageVariantHandler {
                override fun handlePsi(element: PsiElement) { searched = element }
                override fun handleTarget(target: SearchTarget) { fail("Use the native semantic usages handler") }
            })
        assertSame(declaration, searched)
    }

    fun testNativeHandlerUsesSemanticReferencesWithExactFileLinesAndColumns() {
        val definitionFile = parseFile("Definition.as", "// 声明\n\nint Target() { return 1; }\nint Use()\n{\n    return Target();\n}\n")
        val otherFile = parseFile("Other.as", "// 跨文件用法\n\nvoid Other()\n{\n    Print(\"中文🙂\"); Target(); Target();\n}\n")
        val files = listOf(definitionFile, otherFile)
        val documents = files.associateWith { FileDocumentManager.getInstance().getDocument(it.virtualFile)!! }
        val originalManager = psiManager
        // ParsingTestCase's final MockPsiManager deliberately returns null from findFile.
        // Supply the parsed files while retaining its other behavior.
        val manager = object : PsiManager() {
            override fun getProject() = originalManager.project
            override fun findFile(file: VirtualFile) = files.firstOrNull { it.virtualFile == file }
            override fun findFile(file: VirtualFile, context: CodeInsightContext) = findFile(file)
            override fun findViewProvider(file: VirtualFile) = findFile(file)?.viewProvider
            override fun findViewProvider(file: VirtualFile, context: CodeInsightContext) = findViewProvider(file)
            override fun findCachedViewProvider(file: VirtualFile) = findViewProvider(file)
            override fun findDirectory(file: VirtualFile) = originalManager.findDirectory(file)
            override fun areElementsEquivalent(first: PsiElement?, second: PsiElement?) = first == second
            override fun reloadFromDisk(file: PsiFile) = originalManager.reloadFromDisk(file)
            override fun addPsiTreeChangeListener(listener: PsiTreeChangeListener) = originalManager.addPsiTreeChangeListener(listener)
            override fun addPsiTreeChangeListener(listener: PsiTreeChangeListener, parent: Disposable) = originalManager.addPsiTreeChangeListener(listener, parent)
            override fun addPsiTreeChangeListenerBackgroundable(listener: PsiTreeChangeListener, parent: Disposable) = originalManager.addPsiTreeChangeListenerBackgroundable(listener, parent)
            override fun removePsiTreeChangeListener(listener: PsiTreeChangeListener) = originalManager.removePsiTreeChangeListener(listener)
            override fun getModificationTracker() = originalManager.modificationTracker
            override fun startBatchFilesProcessingMode() = originalManager.startBatchFilesProcessingMode()
            override fun finishBatchFilesProcessingMode() = originalManager.finishBatchFilesProcessingMode()
            override fun <T : Any?> runInBatchFilesMode(computable: Computable<T>): T = originalManager.runInBatchFilesMode(computable)
            override fun isDisposed() = originalManager.isDisposed
            override fun dropResolveCaches() = originalManager.dropResolveCaches()
            override fun dropPsiCaches() = originalManager.dropPsiCaches()
            override fun isInProject(element: PsiElement) = originalManager.isInProject(element)
        }
        project.picoContainer.unregisterComponent(PsiManager::class.java.name)
        project.registerService(PsiManager::class.java, manager)
        project.picoContainer.unregisterComponent(PsiDocumentManager::class.java.name)
        project.registerService(PsiDocumentManager::class.java, object : MockPsiDocumentManager() {
            override fun getDocument(file: PsiFile) = documents[file]
        })
        val definitionOffset = definitionFile.text.indexOf("Target")
        val definition = angelscriptDeclarationAt(definitionFile.findElementAt(definitionOffset)!!, definitionOffset)!!
        val expected = files.flatMap { file ->
            Regex("Target\\(").findAll(file.text).map { file to it.range.first }.toList()
        }.filter { (file, offset) -> file != definitionFile || offset != definitionOffset }
        val locations = expected.map { (file, offset) ->
            val document = documents.getValue(file)
            val line = document.getLineNumber(offset)
            val column = offset - document.getLineStartOffset(line)
            Location(file.virtualFile.url, Range(Position(line, column), Position(line, column + "Target".length)))
        }
        val requests = mutableListOf<ReferenceParams>()
        val textService = Proxy.newProxyInstance(TextDocumentService::class.java.classLoader,
            arrayOf(TextDocumentService::class.java)) { _, method, args ->
            check(method.name == "references")
            requests += args!![0] as ReferenceParams
            CompletableFuture.completedFuture(locations + locations.first() +
                Location(otherFile.virtualFile.url, Range(Position(999, 0), Position(999, 6))))
        } as TextDocumentService
        val languageServer = Proxy.newProxyInstance(LanguageServer::class.java.classLoader,
            arrayOf(LanguageServer::class.java)) { _, method, _ ->
            check(method.name == "getTextDocumentService")
            textService
        } as LanguageServer
        val descriptor = object : LspServerDescriptor(project, "AngelScript test") {
            override val lspCustomization = AngelscriptLspCustomization()
            override fun isSupportedFile(file: VirtualFile) = file.extension == "as"
            override fun findFileByUri(fileUri: String) = files.firstOrNull { it.virtualFile.url == fileUri }?.virtualFile
        }
        val client = Proxy.newProxyInstance(LspServer::class.java.classLoader, arrayOf(LspServer::class.java)) { _, method, args ->
            when (method.name) {
                "getState" -> LspServerState.Running
                "getDescriptor" -> descriptor
                "getDocumentIdentifier" -> TextDocumentIdentifier((args!![0] as VirtualFile).url)
                "sendRequestSync" -> {
                    @Suppress("UNCHECKED_CAST")
                    val request = args!![1] as (LanguageServer) -> CompletableFuture<*>
                    request(languageServer).join()
                }
                else -> error("Unexpected LSP client access: ${method.name}")
            }
        } as LspServer
        registerLspTestManager(project) {
            listOf(client)
        }
        val handler = AngelscriptFindUsagesHandler(definition)
        val options = FindUsagesOptions(LocalSearchScope(files.toTypedArray())).apply {
            isUsages = true
            isSearchForTextOccurrences = false
        }
        val usages = mutableListOf<UsageInfo>()
        val processor = Processor<UsageInfo> { usages += it; true }
        for ((file, offset) in expected) {
            assertSame("The fixture PSI must be registered", file, manager.findFile(file.virtualFile))
            assertTrue("The fixture file must be inside the search scope", options.searchScope.contains(file.virtualFile))
            assertTrue((options.searchScope as LocalSearchScope).containsRange(file, TextRange(offset, offset + 6)))
        }
        assertTrue(handler.processElementUsages(definition, processor, options))
        assertEquals("The native handler must issue a semantic references request", 1, requests.size)
        assertEquals("Deduplicate locations and reject invalid lines", expected.size, usages.size)
        for ((index, usage) in usages.withIndex()) {
            val (file, offset) = expected[index]
            val adapter = UsageInfo2UsageAdapter(usage)
            assertSame(file, usage.file)
            assertEquals(offset, usage.navigationOffset)
            assertEquals(offset, adapter.navigationOffset)
            assertEquals(offset + "Target".length, adapter.navigationRange!!.endOffset)
            assertEquals(documents.getValue(file).getLineNumber(offset), adapter.line)
        }
        options.searchScope = LocalSearchScope(definitionFile)
        usages.clear()
        assertTrue(handler.processElementUsages(definition, processor, options))
        assertEquals(1, usages.size)
        assertSame(definitionFile, usages.single().file)
        assertEquals(2, requests.size)
        assertTrue(requests.all { !it.context.isIncludeDeclaration })
        assertTrue(requests.all { it.position == Position(2, 4) })
    }
}
