package com.aesirinteractive.angelscript

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiFile
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.impl.FakePsiElement
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.VirtualFile

internal enum class CppSearchKind { FUNCTION, CLASS, ENUM, NONE }

/** Optional indexed header fallback, shared by native PSI and declaration navigation. */
class AngelscriptCppNavigation : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor): Array<PsiElement>? {
        val file = sourceElement?.containingFile ?: return null
        if (file.language != AngelscriptLanguage.INSTANCE) return null
        if (angelscriptDeclarationAt(sourceElement, offset) != null) return null
        val settings = AngelscriptSettings.getInstance()
        if (!settings.cppHeaderResolutionEnabled) return null
        val tokens = angelscriptLexemes(editor.document.charsSequence)
        val index = tokens.indexOfFirst { offset >= it.start && offset < it.end }
        if (index < 0 || tokens[index].type != AngelscriptTokens.IDENTIFIER) return null
        val name = tokens[index].text
        val kind = searchKind(name, tokens.getOrNull(index + 1)?.text)
        return resolveDeclarations(file.project, name, kind).toTypedArray().takeIf { it.isNotEmpty() }
    }

    internal companion object {
    fun resolveNativeSymbol(project: Project, symbol: AngelscriptCppSymbol): List<PsiElement> {
        if (DumbService.isDumb(project)) return emptyList()
        return project.service<AngelscriptNativeNavigationCache>().current().resolve(symbol)
            .map { AngelscriptCppSourceTarget(it.file, it.offset, it.label) }
    }

    fun createNativeIndex(project: Project): AngelscriptNativeIndex<PsiFile> {
        val scope = GlobalSearchScope.allScope(project)
        val manager = PsiManager.getInstance(project)
        val headerFiles = AngelscriptNativeMemo<Set<String>, List<PsiFile>>(256)
        fun headers(names: Set<String>): List<PsiFile> = headerFiles.getOrPut(names) { names.flatMap { name ->
            FilenameIndex.getVirtualFilesByName(name, scope).mapNotNull(manager::findFile)
        }.distinctBy { it.virtualFile.path } }
        fun filesWithWord(word: String): List<PsiFile> {
            val files = mutableListOf<PsiFile>()
            if (word.matches(Regex("[\\p{L}_][\\p{L}\\p{N}_]*")))
                PsiSearchHelper.getInstance(project).processAllFilesWithWord(word, scope, { file ->
                    ProgressManager.checkCanceled()
                    if (file.virtualFile?.extension in nativeSourceExtensions) files.add(file)
                    true
                }, true)
            return files.distinctBy { it.virtualFile.path }
        }
        return AngelscriptNativeIndex<PsiFile>(::headers, ::filesWithWord, { nativeSourceText(it.virtualFile) },
            ProgressManager::checkCanceled, { file, path ->
            var directory = file.virtualFile.parent
            var relative = directory?.findFileByRelativePath(path)
            while (relative == null && '/' in path && directory != null) {
                ProgressManager.checkCanceled()
                directory = directory.parent
                relative = directory?.findFileByRelativePath(path)
            }
            if (relative != null) listOfNotNull(manager.findFile(relative))
            else headers(setOf(path.substringAfterLast('/'))).filter {
                path.indexOf('/') < 0 || it.virtualFile.path.endsWith("/$path")
            }
        }, { it.name })
    }

    fun resolveDeclarations(project: Project, name: String, kind: CppSearchKind): List<PsiElement> {
        val settings = AngelscriptSettings.getInstance()
        if (!settings.cppHeaderResolutionEnabled || DumbService.isDumb(project)) return emptyList()
        val pattern = when (kind) {
            CppSearchKind.FUNCTION -> settings.cppFunctionPattern
            CppSearchKind.CLASS -> settings.cppClassPattern
            CppSearchKind.ENUM -> settings.cppEnumPattern
            CppSearchKind.NONE -> return emptyList()
        }
        val cache = AngelscriptCppCache.getInstance(project)
        val key = AngelscriptCppCache.CacheKey(name, pattern)
        cache.get(key)?.let { pointers ->
            val live = pointers.mapNotNull { it.element?.takeIf(PsiElement::isValid) }
            if (pointers.isEmpty() || live.isNotEmpty()) return live
        }
        val regex = try { settings.compileCppPattern(pattern, name) }
            catch (_: IllegalArgumentException) { return emptyList() }
        val scope = GlobalSearchScope.projectScope(project)
        val candidates = mutableListOf<com.intellij.psi.PsiFile>()
        // Use the platform's word index before loading text; no full-project header scan or worker wait.
        PsiSearchHelper.getInstance(project).processAllFilesWithWord(name, scope, { candidate ->
            ProgressManager.checkCanceled()
            if (candidate.virtualFile?.extension in setOf("h", "hpp")) candidates.add(candidate)
            true
        }, true)
        val targets = candidates.distinctBy { it.virtualFile.path }.sortedBy { it.virtualFile.path }.mapNotNull { psiFile ->
            ProgressManager.checkCanceled()
            val processed = AngelscriptCppPreprocessor.process(psiFile.text, checkCanceled = ProgressManager::checkCanceled).source
            val match = regex.find(processed.text) ?: return@mapNotNull null
            val nameOffset = match.value.lastIndexOf(name)
            if (nameOffset < 0) null else psiFile.findElementAt(processed.originalOffset(match.range.first + nameOffset))
        }
        cache.put(key, targets.map { SmartPointerManager.getInstance(project).createSmartPsiElementPointer(it) })
        return targets
    }

        fun searchKind(name: String, next: String?): CppSearchKind = when {
            next == "(" -> CppSearchKind.FUNCTION
            name.length > 1 && name[0] == 'E' && name[1].isUpperCase() -> CppSearchKind.ENUM
            name.length > 1 && name[0] in "UAFTI" && name[1].isUpperCase() -> CppSearchKind.CLASS
            else -> CppSearchKind.NONE
        }
    }
}

/** Use editor UTF-16/LF coordinates without asking Rider's backend to create a PSI document. */
internal fun nativeSourceText(file: VirtualFile): String {
    ProgressManager.checkCanceled()
    val text = FileDocumentManager.getInstance().getCachedDocument(file)?.text
        ?: com.intellij.openapi.util.text.StringUtil.convertLineSeparators(com.intellij.openapi.vfs.VfsUtilCore.loadText(file))
    ProgressManager.checkCanceled()
    return text
}

/** C++ files can have coarse frontend PSI in Rider; navigate to the exact document offset. */
internal class AngelscriptCppSourceTarget(
    private val file: PsiFile,
    private val offset: Int,
    private val label: String,
) : FakePsiElement(), NavigatablePsiElement {
    private val line = FileDocumentManager.getInstance().getCachedDocument(file.virtualFile)?.let {
        it.getLineNumber(offset.coerceIn(0, it.textLength)) + 1
    } ?: nativeSourceText(file.virtualFile).let { it.take(offset.coerceIn(0, it.length)).count { char -> char == '\n' } + 1 }
    val sourceLocation = "${file.virtualFile.path}:$line"
    override fun getParent(): PsiElement = file
    // Some Rider chooser renderers use name directly rather than ItemPresentation.
    override fun getName() = "$label — ${file.name}:$line"
    override fun getPresentableText() = label
    override fun getLocationString() = "${file.name}:$line"
    override fun getTextOffset() = offset
    override fun canNavigate() = file.isValid && file.virtualFile.isValid
    override fun canNavigateToSource() = canNavigate()
    override fun navigate(requestFocus: Boolean) = OpenFileDescriptor(file.project, file.virtualFile, offset).navigate(requestFocus)
}
