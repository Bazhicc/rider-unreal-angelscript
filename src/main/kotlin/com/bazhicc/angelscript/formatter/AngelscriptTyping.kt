package com.bazhicc.angelscript.formatter

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

data class TypingRange(val range: FormatRange, val preserveNewlines: Boolean = false)

object TypingRules {
    fun parentheses(text: String, offset: Int): FormatRange? {
        val syntax = AngelScriptSyntax(text)
        val open = syntax.tokens.indexOfFirst { it.value == "(" && it.end == offset }
        val keyword = syntax.tokens.getOrNull(open - 1) ?: return null
        if (keyword.value !in listOf("if", "for", "foreach", "switch", "while", "catch") ||
            !syntax.codeAt(keyword.end)) return null
        return FormatRange(keyword.end, offset - 1)
    }

    fun enter(text: String, offset: Int): TypingRange? {
        val currentLineStart = if (offset == 0) 0 else text.lastIndexOf('\n', offset - 1) + 1
        if (currentLineStart == 0) return null
        val previousStart = if (currentLineStart <= 1) 0 else text.lastIndexOf('\n', currentLineStart - 2) + 1
        val previous = text.substring(previousStart, currentLineStart - 1).removeSuffix("\r")
        Regex("([A-Za-z0-9_)])(\\s*)\\{\\s*$").find(previous)?.let { match ->
            val start = previousStart + match.range.first + match.groupValues[1].length
            if (!AngelScriptSyntax(text).codeAt(start)) return null
            return TypingRange(FormatRange(start, start + match.groupValues[2].length))
        }
        Regex("[(,|&]\\s*$").find(previous)?.let { match ->
            if (!AngelScriptSyntax(text).codeAt(previousStart + match.range.first)) return null
            var end = currentLineStart
            while (end < text.length && text[end] in " \t") end++
            return TypingRange(FormatRange(previousStart + match.range.first + 1, end), true)
        }
        return null
    }
}

class AngelscriptTypedHandler : TypedHandlerDelegate() {
    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (c != '(' || !typingEnabled(file) || editor.caretModel.caretCount != 1) return Result.CONTINUE
        val offset = editor.caretModel.offset
        val range = TypingRules.parentheses(editor.document.text, offset) ?: return Result.CONTINUE
        formatTyping(file, editor, TypingRange(range))
        return Result.CONTINUE
    }
}

private fun typingEnabled(file: PsiFile) = AngelscriptFormattingService.isAngelScript(file) &&
    FormatterSettings.get(file.project).let { it.enabled && it.formatOnType }

class AngelscriptEnterHandler : EnterHandlerDelegate {
    override fun postProcessEnter(file: PsiFile, editor: Editor, dataContext: DataContext): EnterHandlerDelegate.Result {
        if (!typingEnabled(file) || editor.caretModel.caretCount != 1) return EnterHandlerDelegate.Result.Continue
        val request = TypingRules.enter(editor.document.text, editor.caretModel.offset) ?: return EnterHandlerDelegate.Result.Continue
        formatTyping(file, editor, request)
        return EnterHandlerDelegate.Result.Continue
    }
}

private fun formatTyping(file: PsiFile, editor: Editor, request: TypingRange) {
        val document = editor.document
        val text = document.text
        val stamp = document.modificationStamp
        val project = file.project
        val projectRoot = project.basePath
        val filename = file.virtualFile?.path ?: return
        val options = FormatterSettings.get(project).copy()
        val app = ApplicationManager.getApplication()
        app.executeOnPooledThread {
            try {
                val formatted = FormatterRuntime.format(text, filename, projectRoot, options, listOf(request.range),
                    preserveNewlines = request.preserveNewlines, whitespaceOnly = true)
                if (formatted != text) app.invokeLater {
                    if (!project.isDisposed && !editor.isDisposed && document.modificationStamp == stamp) {
                        WriteCommandAction.runWriteCommandAction(project, "Format AngelScript on Enter", null, Runnable {
                            // Keep range markers, carets and folds outside this small whitespace edit intact.
                            val suffixLength = text.length - request.range.end
                            val replacementEnd = formatted.length - suffixLength
                            document.replaceString(request.range.start, request.range.end,
                                formatted.substring(request.range.start, replacementEnd))
                        })
                    }
                }
            } catch (error: Exception) {
                Logger.getInstance(AngelscriptTypedHandler::class.java).debug("On-type formatting failed", error)
            }
        }
}
