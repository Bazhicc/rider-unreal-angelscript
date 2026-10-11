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
import com.intellij.openapi.util.Key

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
    private val bindingCache = Key.create<Pair<Long, List<AngelscriptNativeBindings.Binding>>>("angelscript.native.bindings")
    private val sourceExtensions = setOf("h", "hpp", "hh", "hxx", "inl", "inc", "cpp", "cc", "cxx")

    fun resolveNativeSymbol(project: Project, symbol: AngelscriptCppSymbol): List<PsiElement> {
        if (DumbService.isDumb(project)) return emptyList()
        val scope = GlobalSearchScope.allScope(project)
        val manager = PsiManager.getInstance(project)
        fun headers(names: Set<String>): List<PsiFile> = names.flatMap { name ->
            FilenameIndex.getVirtualFilesByName(name, scope).mapNotNull(manager::findFile)
        }.distinctBy { it.virtualFile.path }
        val indexed = mutableMapOf<String, List<PsiFile>>()
        fun filesWithWord(word: String): List<PsiFile> = indexed.getOrPut(word) {
            val files = mutableListOf<PsiFile>()
            if (word.matches(Regex("[\\p{L}_][\\p{L}\\p{N}_]*")))
                PsiSearchHelper.getInstance(project).processAllFilesWithWord(word, scope, { file ->
                    ProgressManager.checkCanceled()
                    if (file.virtualFile?.extension in sourceExtensions) files.add(file)
                    true
                }, true)
            files.distinctBy { it.virtualFile.path }
        }
        fun target(file: PsiFile, offset: Int) = AngelscriptCppSourceTarget(file, offset, "${symbol.typeName}::${symbol.symbolName}")
        fun declarations(query: AngelscriptCppSymbol): List<PsiElement> {
            val namespace = query.typeName.takeIf { it.startsWith("__") }?.removePrefix("__")
            val initial = AngelscriptCppDeclarations.baseName(query.typeName).substringAfterLast("::")
            val owners = linkedSetOf(initial)
            val files = linkedMapOf<String, PsiFile>()
            fun addFiles(additions: List<PsiFile>) { for (file in additions) files[file.virtualFile.path] = file }
            fun filenames(owner: String) = listOf(owner, owner.drop(1)).flatMap { name ->
                listOf("$name.h", "$name.hpp", "$name.hh", "$name.hxx")
            }.toSet()
            fun find(): List<PsiElement> = files.values.flatMap { file ->
                ProgressManager.checkCanceled()
                val offsets = if (namespace != null)
                    AngelscriptCppDeclarations.namespaceOffsets(file.text, namespace, setOf(query.cppName), query.kind)
                else AngelscriptCppDeclarations.offsets(file.text, owners, query)
                offsets.map { target(file, it) }
            }
            if (namespace != null) {
                addFiles(filesWithWord(if (query.kind == "namespace") initial else query.cppName))
                return find()
            }
            addFiles(headers(filenames(initial)))
            find().takeIf { it.isNotEmpty() }?.let { return it }
            // Aliases can live in any indexed file, not just a known UE forwarding header.
            val pending = ArrayDeque<String>()
            pending.add(initial)
            while (pending.isNotEmpty()) {
                val owner = pending.removeFirst()
                addFiles(filesWithWord(owner))
                for (file in files.values.toList()) {
                    ProgressManager.checkCanceled()
                    for (alias in AngelscriptCppDeclarations.aliases(file.text, owner)) {
                        if (owners.add(alias)) { pending.add(alias); addFiles(headers(filenames(alias))) }
                    }
                }
            }
            return find()
        }
        declarations(symbol).takeIf { it.isNotEmpty() }?.let { return it }
        val owner = AngelscriptCppDeclarations.baseName(symbol.typeName.removePrefix("__"))
        val candidates = if (owner.isEmpty()) filesWithWord(symbol.symbolName)
            else (filesWithWord(owner.substringAfterLast("::")) + filesWithWord(symbol.symbolName)).distinctBy { it.virtualFile.path }
        val bindings = candidates.flatMap { file ->
            ProgressManager.checkCanceled()
            val cached = file.getUserData(bindingCache)
            val records = if (cached?.first == file.modificationStamp) cached.second else
                AngelscriptNativeBindings.parse(file.text).also { file.putUserData(bindingCache, file.modificationStamp to it) }
            records.filter { it.owner == owner && (it.name == symbol.symbolName || it.name == symbol.cppName) &&
                (it.kind == symbol.kind || symbol.kind in setOf("type", "enum") && it.kind == "type") }.map { file to it }
        }
        val implementations = bindings.flatMap { (_, binding) ->
            if (binding.cppName == null) emptyList() else declarations(symbol.copy(
                typeName = binding.cppOwner ?: "__", cppName = binding.cppName,
                symbolName = binding.cppName, kind = if (symbol.kind == "enum") "type" else symbol.kind))
        }
        return implementations.ifEmpty { bindings.map { (file, binding) -> target(file, binding.offset) } }
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

/** C++ files can have coarse frontend PSI in Rider; navigate to the exact document offset. */
internal class AngelscriptCppSourceTarget(
    private val file: PsiFile,
    private val offset: Int,
    private val label: String,
) : FakePsiElement(), NavigatablePsiElement {
    override fun getParent(): PsiElement = file
    override fun getName() = label
    override fun getTextOffset() = offset
    override fun canNavigate() = file.isValid && file.virtualFile.isValid
    override fun canNavigateToSource() = canNavigate()
    override fun navigate(requestFocus: Boolean) = OpenFileDescriptor(file.project, file.virtualFile, offset).navigate(requestFocus)
}
