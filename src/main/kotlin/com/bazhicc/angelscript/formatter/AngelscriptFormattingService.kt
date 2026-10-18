package com.bazhicc.angelscript.formatter

import com.intellij.formatting.FormattingContext
import com.intellij.formatting.service.AsyncDocumentFormattingService
import com.intellij.formatting.service.AsyncFormattingRequest
import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiFile
import java.util.concurrent.CancellationException

class AngelscriptFormattingService : AsyncDocumentFormattingService() {
    override fun getFeatures() = setOf(FormattingService.Feature.FORMAT_FRAGMENTS)
    override fun canFormat(file: PsiFile) = isAngelScript(file) && FormatterSettings.get(file.project).enabled
    override fun getNotificationGroupId() = "AngelScript Clang-Format"
    override fun getName() = "AngelScript Clang-Format"
    // Input comes from the document snapshot; formatting must not save unsaved changes.
    override fun prepareForFormatting(document: Document, context: FormattingContext) = Unit

    override fun createFormattingTask(request: AsyncFormattingRequest): FormattingTask {
        val project = request.context.project
        val options = FormatterSettings.get(project).copy()
        val filename = request.context.virtualFile?.path ?: "${project.basePath ?: "."}/untitled.as"
        val projectRoot = project.basePath
        val text = request.documentText
        val ranges = request.formattingRanges.map { FormatRange(it.startOffset, it.endOffset) }
        val rules = RiderFormattingRules.load(project, filename)
        return object : FormattingTask {
            private val process = FormatterProcess()
            override fun cancel() = process.cancel()
            override fun run() {
                try {
                    request.onTextReady(FormatterRuntime.format(text, filename, projectRoot, options, ranges,
                        process, preserveNewlines = rules.preserveLineBreaks,
                        whitespaceOnly = request.canChangeWhitespaceOnly(), bracePolicy = rules.braces))
                } catch (_: CancellationException) {
                    request.onTextReady(null)
                } catch (error: Exception) {
                    request.onError("AngelScript formatting failed", error.message ?: error.javaClass.simpleName)
                }
            }
        }
    }

    companion object {
        fun isAngelScript(file: PsiFile) = file.virtualFile?.extension.equals("as", ignoreCase = true) ||
            file.language.id.equals("AngelScript", ignoreCase = true)
    }
}
