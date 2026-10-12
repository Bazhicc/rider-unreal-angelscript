package com.aesirinteractive.angelscript

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.psi.PsiFile

@Service(Service.Level.PROJECT)
internal class AngelscriptNativeNavigationCache(private val project: Project) : Disposable {
    @Volatile private var index: AngelscriptNativeIndex<PsiFile>? = null
    private var roots = -1L
    private var indexing = -1L

    init {
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                if (FileDocumentManager.getInstance().getFile(event.document)?.extension in nativeSourceExtensions) {
                    clear()
                    project.getServiceIfCreated(AngelscriptCppCache::class.java)?.clear()
                }
            }
        }, this)
    }

    @Synchronized fun current(): AngelscriptNativeIndex<PsiFile> {
        val stamp = ProjectRootModificationTracker.getInstance(project).modificationCount
        val indexStamp = DumbService.getInstance(project).modificationTracker.modificationCount
        if (stamp != roots || indexing != indexStamp) { index = null; roots = stamp; indexing = indexStamp }
        return index ?: AngelscriptCppNavigation.createNativeIndex(project).also { index = it }
    }

    @Synchronized fun clear() { index = null }
    override fun dispose() { clear() }
}

internal val nativeSourceExtensions = setOf("h", "hpp", "hh", "hxx", "inl", "inc", "cpp", "cc", "cxx")
