package com.bazhicc.angelscript.formatter

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JComponent
import javax.swing.JPanel

class FormatterConfigurable(private val project: Project) : Configurable {
    private val enabled = JBCheckBox("Use Clang-Format for AngelScript (.as) files")
    private val onType = JBCheckBox("Format locally when typing '(' or Enter")
    private val executable = JBTextField(40)
    private val style = JBTextField(40)

    override fun getDisplayName() = "AngelScript Clang-Format"
    override fun createComponent(): JComponent = JPanel(GridBagLayout()).apply {
        fun row(component: JComponent, y: Int) {
            add(component, GridBagConstraints().apply {
                gridx = 0; gridy = y; weightx = 1.0; fill = GridBagConstraints.HORIZONTAL
                anchor = GridBagConstraints.NORTHWEST; insets = Insets(4, 4, 4, 4)
            })
        }
        row(enabled, 0)
        row(onType, 1)
        row(JBLabel("Clang-Format executable (empty = bundled; relative paths use the project root):"), 2)
        row(executable, 3)
        row(JBLabel("Style file (empty = nearest .clang-format, then bundled AngelScript style):"), 4)
        row(style, 5)
        add(JPanel(), GridBagConstraints().apply { gridy = 6; weighty = 1.0 })
        reset()
    }

    override fun isModified(): Boolean {
        val state = FormatterSettings.get(project)
        return enabled.isSelected != state.enabled || onType.isSelected != state.formatOnType ||
            executable.text.trim() != state.executable || style.text.trim() != state.style
    }

    override fun reset() {
        val state = FormatterSettings.get(project)
        enabled.isSelected = state.enabled; onType.isSelected = state.formatOnType
        executable.text = state.executable; style.text = state.style
    }

    override fun apply() {
        for (value in listOf(executable.text.trim(), style.text.trim()).filter { it.isNotEmpty() }) {
            if (!FormatterRuntime.resolvePath(value, project.basePath).toFile().isFile)
                throw ConfigurationException("File does not exist: $value")
        }
        FormatterSettings.get(project).apply {
            enabled = this@FormatterConfigurable.enabled.isSelected
            formatOnType = onType.isSelected
            executable = this@FormatterConfigurable.executable.text.trim()
            style = this@FormatterConfigurable.style.text.trim()
        }
    }
}
