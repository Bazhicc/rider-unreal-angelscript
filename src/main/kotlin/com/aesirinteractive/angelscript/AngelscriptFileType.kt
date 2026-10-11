package com.aesirinteractive.angelscript

import com.intellij.icons.AllIcons
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import org.jetbrains.annotations.NonNls
import javax.swing.Icon

class AngelscriptLanguage : Language("AngelScript") {

    companion object {
        val INSTANCE: AngelscriptLanguage = AngelscriptLanguage()
    }
}

// TextMate supplies editor highlighting only; its file-type marker would replace native PSI.
class AngelscriptFileType : LanguageFileType(AngelscriptLanguage.INSTANCE) {
    override fun getName(): @NonNls String {
        return "AngelScript"
    }

    override fun getDescription(): @NlsContexts.Label String {
        return "AngelScript file"
    }

    override fun getDefaultExtension(): @NlsSafe String {
        return "as"
    }

    override fun getIcon(): Icon {
        return AllIcons.FileTypes.AS
    }

    companion object {
        val INSTANCE: AngelscriptFileType = AngelscriptFileType()
    }
}
