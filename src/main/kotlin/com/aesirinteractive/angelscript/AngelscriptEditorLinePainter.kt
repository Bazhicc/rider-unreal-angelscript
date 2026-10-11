package com.aesirinteractive.angelscript

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorLinePainter
import com.intellij.openapi.editor.LineExtensionInfo
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.awt.Color
import java.awt.Font

class AngelscriptEditorLinePainter : EditorLinePainter() {

    override fun getLineExtensions(project: Project, file: VirtualFile, lineNumber: Int): Collection<LineExtensionInfo?>? {
        if (file.extension != "as") return null

        val text = AngelscriptInlineValuesService.getInstance(project)
            .getLineText(file, lineNumber) ?: return null

        val color: Color = EditorColorsManager.getInstance().globalScheme
            .getColor(EditorColors.LINE_NUMBERS_COLOR) ?: Color(128, 128, 128)
        val attrs = TextAttributes().apply {
            foregroundColor = color
            fontType = Font.ITALIC
        }
        return listOf(LineExtensionInfo("    // $text", attrs))
    }
}
