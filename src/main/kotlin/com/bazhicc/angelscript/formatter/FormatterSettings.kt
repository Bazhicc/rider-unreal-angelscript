package com.bazhicc.angelscript.formatter

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@State(name = "AngelScriptClangFormat", storages = [Storage("angelscript-clang-format.xml")])
class FormatterSettings : PersistentStateComponent<FormatterSettings.Options> {
    data class Options(
        var enabled: Boolean = true,
        var executable: String = "",
        var style: String = "",
        var formatOnType: Boolean = true,
    )

    private var options = Options()
    override fun getState() = options
    override fun loadState(state: Options) { options = state }

    companion object {
        fun get(project: Project): Options = project.getService(FormatterSettings::class.java).state
    }
}
