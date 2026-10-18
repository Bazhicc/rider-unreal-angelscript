package com.bazhicc.angelscript.formatter

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.ExternalFormatProcessor

/** Rider's native Reformat Code action also calls this API, bypassing FormattingService. */
class AngelscriptExternalFormatProcessor : ExternalFormatProcessor {
    override fun getId() = "ANGELSCRIPT_CLANG_FORMAT"
    override fun activeForFile(source: PsiFile) = AngelscriptFormattingService.isAngelScript(source) &&
        FormatterSettings.get(source.project).enabled
    override fun indent(source: PsiFile, lineStartOffset: Int): String? = null

    override fun format(source: PsiFile, range: TextRange, canChangeWhiteSpacesOnly: Boolean,
                        keepLineBreaks: Boolean, enableBulkUpdate: Boolean, cursorOffset: Int): TextRange? {
        if (!activeForFile(source)) return null
        val document = source.viewProvider.document ?: return null
        val text = document.text
        val filename = source.virtualFile?.path ?: return null
        val options = FormatterSettings.get(source.project).copy()
        val log = Logger.getInstance(AngelscriptExternalFormatProcessor::class.java)
        val started = System.nanoTime()
        var rulesMillis = 0L
        var formatMillis = 0L
        var applyMillis = 0L
        try {
            var phase = System.nanoTime()
            val rules = RiderFormattingRules.load(source.project, filename)
            rulesMillis = (System.nanoTime() - phase) / 1_000_000
            phase = System.nanoTime()
            val edits = FormatterRuntime.formatEdits(text, filename, source.project.basePath, options,
                listOf(FormatRange(range.startOffset, range.endOffset)), preserveNewlines = keepLineBreaks || rules.preserveLineBreaks,
                whitespaceOnly = canChangeWhiteSpacesOnly, bracePolicy = rules.braces)
            formatMillis = (System.nanoTime() - phase) / 1_000_000
            // This API is synchronous and is called inside the platform's formatting write command.
            // Individual replacements preserve the editor's caret and range markers.
            val apply = Runnable { edits.asReversed().forEach { document.replaceString(it.start, it.end, it.text) } }
            if (edits.isNotEmpty()) {
                phase = System.nanoTime()
                WriteCommandAction.runWriteCommandAction(source.project, "Reformat AngelScript", null, apply)
                applyMillis = (System.nanoTime() - phase) / 1_000_000
            }
            val totalMillis = (System.nanoTime() - started) / 1_000_000
            log.info("Formatted $filename with AngelScript Clang-Format: ${edits.size} replacements " +
                "(rules=${rulesMillis}ms, formatter=${formatMillis}ms, apply=${applyMillis}ms, total=${totalMillis}ms)")
            val delta = edits.sumOf { it.text.length - (it.end - it.start) }
            return TextRange(range.startOffset, range.endOffset + delta)
        } catch (cancelled: ProcessCanceledException) {
            throw cancelled
        } catch (cancelled: java.util.concurrent.CancellationException) {
            throw ProcessCanceledException(cancelled)
        } catch (error: Exception) {
            log.warn("AngelScript formatting failed for $filename", error)
            NotificationGroupManager.getInstance().getNotificationGroup("AngelScript Clang-Format")
                .createNotification("AngelScript formatting failed", error.message ?: error.javaClass.simpleName,
                    NotificationType.ERROR).notify(source.project)
            return null
        }
    }
}
