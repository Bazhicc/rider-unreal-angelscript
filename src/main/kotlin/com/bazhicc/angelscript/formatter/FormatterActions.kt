package com.bazhicc.angelscript.formatter

import com.intellij.formatting.FormattingContext
import com.intellij.formatting.service.FormattingService
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.application.options.CodeStyle
import java.nio.file.Path

class FormatAction : DumbAwareAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT
    override fun update(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.PSI_FILE)
        event.presentation.isEnabledAndVisible = file != null && event.getData(CommonDataKeys.EDITOR) != null &&
            AngelscriptFormattingService.isAngelScript(file) && FormatterSettings.get(file.project).enabled
    }

    override fun actionPerformed(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.PSI_FILE) ?: return
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val selection = editor.selectionModel
        val range = if (selection.hasSelection()) TextRange(selection.selectionStart, selection.selectionEnd)
            else TextRange(0, editor.document.textLength)
        val service = FormattingService.EP_NAME.extensionList.filterIsInstance<AngelscriptFormattingService>().first()
        val context = FormattingContext.create(file, CodeStyle.getSettings(file))
        service.formatDocument(editor.document, listOf(range), context, true, false)
    }
}

class GenerateConfigAction : DumbAwareAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT
    override fun update(event: AnActionEvent) { event.presentation.isEnabled = event.project?.basePath != null }
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val basePath = project.basePath ?: return
        val root = VfsUtil.findFile(Path.of(basePath), true) ?: return
        val existing = root.findChild(".clang-format")
        if (existing != null && Messages.showYesNoDialog(project,
                "Overwrite ${existing.path}?", "Generate AngelScript Clang-Format Config",
                Messages.getWarningIcon()) != Messages.YES) return
        val content = javaClass.getResourceAsStream("/style/.clang-format")!!.bufferedReader().use { it.readText() }
        var created = existing
        WriteCommandAction.runWriteCommandAction(project, "Generate AngelScript Clang-Format Config", null, Runnable {
            val target = existing ?: root.createChildData(this, ".clang-format")
            VfsUtil.saveText(target, content)
            created = target
        })
        created?.let { FileEditorManager.getInstance(project).openFile(it, true) }
    }
}
