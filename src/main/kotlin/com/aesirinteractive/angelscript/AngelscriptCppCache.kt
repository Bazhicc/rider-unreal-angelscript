package com.aesirinteractive.angelscript

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class AngelscriptCppCache(private val project: Project) {

    data class CacheKey(
        val name: String,
        val pattern: String,
    )

    private val cache = ConcurrentHashMap<CacheKey, List<SmartPsiElementPointer<PsiElement>>>()

    companion object {
        fun getInstance(project: Project): AngelscriptCppCache = project.service()
    }

    fun get(key: CacheKey): List<SmartPsiElementPointer<PsiElement>>? = cache[key]

    fun put(key: CacheKey, pointers: List<SmartPsiElementPointer<PsiElement>>) {
        cache[key] = pointers
    }

    fun clear() { cache.clear() }
}

class AngelscriptCppCacheVfsListener(private val project: Project) : BulkFileListener {
    override fun after(events: List<VFileEvent>) {
        val affectsHeaders = events.any { event ->
            val fileName = event.file?.name ?: event.path.substringAfterLast('/')
            fileName.endsWith(".h") || fileName.endsWith(".hpp")
        }
        if (affectsHeaders) AngelscriptCppCache.getInstance(project).clear()
    }
}
