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

internal enum class CppSearchKind { FUNCTION, CLASS, ENUM, NONE }

/** Optional indexed header fallback, shared by native PSI and declaration navigation. */
class AngelscriptCppNavigation : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor): Array<PsiElement>? {
        val file = sourceElement?.containingFile ?: return null
        if (file.language != AngelscriptLanguage.INSTANCE) return null
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
            val match = regex.find(psiFile.text) ?: return@mapNotNull null
            val nameOffset = match.value.lastIndexOf(name)
            if (nameOffset < 0) null else psiFile.findElementAt(match.range.first + nameOffset)
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
