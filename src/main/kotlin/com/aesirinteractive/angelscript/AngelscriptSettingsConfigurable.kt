package com.aesirinteractive.angelscript

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.Color
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.UIManager
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

private val LOG = logger<AngelscriptSettingsConfigurable>()

class AngelscriptSettingsConfigurable : Configurable {

    private val nodePathLabel = JBLabel("Node.js path:")
    private val nodePathField = JBTextField()
    private val lspPathKindCombo = ComboBox(LspPathKind.entries.toTypedArray())
    private val lspPathLabel = JBLabel("Language server path:")
    private val lspPathField = JBTextField()
    private val customCommandLineLabel = JBLabel("Command line:")
    private val customCommandLineField = JBTextField()
    private val fileExtensionsField = JBTextField()
    private val debugHostField = JBTextField()
    private val debugPortField = JBTextField()
    private val autoReconnectCheckBox = JBCheckBox("Auto-reconnect debugger when connection is lost (unlimited retries)")
    private val reconnectDelayField = JBTextField()
    private val logDebugMessagesCheckBox = JBCheckBox("Log debug protocol messages to console")
    private val focusRiderWhenBreakingCheckBox = JBCheckBox("Focus Rider when hitting a breakpoint in angelscript")
    private val cppHeaderResolutionCheckBox = JBCheckBox("Experimental: Resolve AngelScript symbols in C++ header files (.h/.hpp)")
    private val cppFunctionPatternField = JBTextField()
    private val cppFunctionPatternError = JBLabel().also { it.foreground = JBColor.RED; it.isVisible = false }
    private val cppClassPatternField = JBTextField()
    private val cppClassPatternError = JBLabel().also { it.foreground = JBColor.RED; it.isVisible = false }
    private val cppEnumPatternField = JBTextField()
    private val cppEnumPatternError = JBLabel().also { it.foreground = JBColor.RED; it.isVisible = false }
    private val cppButtonsPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).also { panel ->
        val resetButton = JButton("Reset patterns to defaults").also { btn ->
            btn.addActionListener {
                val defaults = AngelscriptSettings.State()
                cppFunctionPatternField.text = defaults.cppFunctionPattern
                cppClassPatternField.text    = defaults.cppClassPattern
                cppEnumPatternField.text     = defaults.cppEnumPattern
            }
        }
        val clearCacheButton = JButton("Clear C++ symbol cache").also { btn ->
            btn.addActionListener {
                ProjectManager.getInstance().openProjects.forEach { AngelscriptCppCache.getInstance(it).clear() }
            }
        }
        panel.add(resetButton)
        panel.add(clearCacheButton)
    }
    private var panel: JPanel? = null

    override fun getDisplayName() = "AngelScript"

    override fun createComponent(): JComponent {
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(nodePathLabel, nodePathField, 1, false)
            .addLabeledComponent(JBLabel("Language server source:"), lspPathKindCombo, 1, false)
            .addLabeledComponent(lspPathLabel, lspPathField, 1, false)
            .addLabeledComponent(customCommandLineLabel, customCommandLineField, 1, false)
            .addLabeledComponent(JBLabel("File extensions (comma-separated):"), fileExtensionsField, 1, false)
            .addSeparator()
            .addLabeledComponent(JBLabel("Debug server host:"), debugHostField, 1, false)
            .addLabeledComponent(JBLabel("Debug server port:"), debugPortField, 1, false)
            .addComponent(autoReconnectCheckBox, 1)
            .addLabeledComponent(JBLabel("Reconnect delay (ms):"), reconnectDelayField, 1, false)
            .addComponent(logDebugMessagesCheckBox, 1)
            .addComponent(focusRiderWhenBreakingCheckBox, 1)
            .addSeparator()
            .addComponent(cppHeaderResolutionCheckBox, 1)
            .addLabeledComponent(JBLabel("Function pattern (use NAME as placeholder):"), cppFunctionPatternField, 1, false)
            .addComponent(cppFunctionPatternError, 1)
            .addLabeledComponent(JBLabel("Class/struct pattern:"), cppClassPatternField, 1, false)
            .addComponent(cppClassPatternError, 1)
            .addLabeledComponent(JBLabel("Enum pattern:"), cppEnumPatternField, 1, false)
            .addComponent(cppEnumPatternError, 1)
            .addComponent(cppButtonsPanel, 1)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        attachRegexValidator(cppFunctionPatternField, cppFunctionPatternError)
        attachRegexValidator(cppClassPatternField, cppClassPatternError)
        attachRegexValidator(cppEnumPatternField, cppEnumPatternError)

        lspPathKindCombo.addActionListener { updateLspPathVisibility() }
        updateLspPathVisibility()

        return panel!!
    }

    /** Validates [field] as a regex template on every keystroke, showing errors in [errorLabel]. */
    private fun attachRegexValidator(field: JBTextField, errorLabel: JBLabel) {
        val defaultBackground = field.background
        fun validate() {
            val error = validateRegexTemplate(field.text)
            val isValid = error == null
            field.background = if (isValid) defaultBackground else UIManager.getColor("TextField.errorBackground") ?: Color(100, 50, 50)
            errorLabel.text = error ?: ""
            errorLabel.isVisible = !isValid
            panel?.revalidate()
        }
        field.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = validate()
            override fun removeUpdate(e: DocumentEvent) = validate()
            override fun changedUpdate(e: DocumentEvent) = validate()
        })
    }

    /** Returns an error message if [template] is not a valid regex (with NAME substituted), or null if valid. */
    private fun validateRegexTemplate(template: String): String? =
        try { Regex(template.replace("NAME", "X")); null }
        catch (e: Exception) { e.message }

    private fun updateLspPathVisibility() {
        val kind = lspPathKindCombo.selectedItem as? LspPathKind
        val isCommandLine = kind == LspPathKind.CommandLine
        val isCustom = kind == LspPathKind.Custom
        nodePathLabel.isVisible = !isCommandLine
        nodePathField.isVisible = !isCommandLine
        lspPathLabel.isVisible = isCustom
        lspPathField.isVisible = isCustom
        customCommandLineLabel.isVisible = isCommandLine
        customCommandLineField.isVisible = isCommandLine
        panel?.revalidate()
        panel?.repaint()
    }

    override fun isModified(): Boolean {
        val settings = AngelscriptSettings.getInstance()
        return nodePathField.text != settings.nodePath
            || lspPathField.text != settings.lspPath
            || lspPathKindCombo.selectedItem != settings.lspPathKind
            || customCommandLineField.text != settings.customCommandLine
            || fileExtensionsField.text != settings.fileExtensions
            || debugHostField.text != settings.debugHost
            || debugPortField.text != settings.debugPort.toString()
            || autoReconnectCheckBox.isSelected != settings.autoReconnectDebugger
            || reconnectDelayField.text.toLongOrNull() != settings.debugReconnectDelayMs
            || logDebugMessagesCheckBox.isSelected != settings.logDebugMessages
            || focusRiderWhenBreakingCheckBox.isSelected != settings.focusRiderWhenBreaking
            || cppHeaderResolutionCheckBox.isSelected != settings.cppHeaderResolutionEnabled
            || cppFunctionPatternField.text != settings.cppFunctionPattern
            || cppClassPatternField.text != settings.cppClassPattern
            || cppEnumPatternField.text != settings.cppEnumPattern
    }

    override fun apply() {
        if (debugHostField.text.isBlank()) throw ConfigurationException("Debug host must not be empty")
        val debugPort = debugPortField.text.toIntOrNull()
        if (debugPort == null || debugPort !in 1..65535)
            throw ConfigurationException("Debug port must be between 1 and 65535")
        validateRegexTemplate(cppFunctionPatternField.text)
            ?.let { throw ConfigurationException("Function pattern: $it") }
        validateRegexTemplate(cppClassPatternField.text)
            ?.let { throw ConfigurationException("Class/struct pattern: $it") }
        validateRegexTemplate(cppEnumPatternField.text)
            ?.let { throw ConfigurationException("Enum pattern: $it") }

        val settings = AngelscriptSettings.getInstance()

        // Read the old values before the write, to find out if the server command line changes.
        val oldNodePath = settings.nodePath
        val oldLspPath = settings.lspPath
        val oldLspPathKind = settings.lspPathKind
        val oldCustomCommandLine = settings.customCommandLine
        val oldFileExtensions = settings.fileExtensions
        val oldDebugHost = settings.debugHost
        val oldDebugPort = settings.debugPort

        settings.nodePath = nodePathField.text
        settings.lspPath = lspPathField.text
        settings.lspPathKind = lspPathKindCombo.selectedItem as LspPathKind
        settings.customCommandLine = customCommandLineField.text
        settings.fileExtensions = fileExtensionsField.text
        settings.debugHost = debugHostField.text.trim()
        settings.debugPort = debugPort
        settings.autoReconnectDebugger = autoReconnectCheckBox.isSelected
        settings.debugReconnectDelayMs = reconnectDelayField.text.toLongOrNull()?.coerceAtLeast(100L) ?: 2000L
        settings.logDebugMessages = logDebugMessagesCheckBox.isSelected
        settings.focusRiderWhenBreaking = focusRiderWhenBreakingCheckBox.isSelected
        settings.cppHeaderResolutionEnabled = cppHeaderResolutionCheckBox.isSelected
        settings.cppFunctionPattern = cppFunctionPatternField.text
        settings.cppClassPattern = cppClassPatternField.text
        settings.cppEnumPattern = cppEnumPatternField.text
        ProjectManager.getInstance().openProjects.forEach { AngelscriptCppCache.getInstance(it).clear() }

        val lspSettingsChanged = oldNodePath != settings.nodePath
            || oldLspPath != settings.lspPath
            || oldLspPathKind != settings.lspPathKind
            || oldCustomCommandLine != settings.customCommandLine
            || oldFileExtensions != settings.fileExtensions
            || oldDebugHost != settings.debugHost
            || oldDebugPort != settings.debugPort
        if (lspSettingsChanged) {
            LOG.info("AngelScript LSP settings changed — restarting the language server")
            restartAngelscriptLspServers()
        }
    }

    override fun reset() {
        val settings = AngelscriptSettings.getInstance()
        nodePathField.text = settings.nodePath
        lspPathField.text = settings.lspPath
        lspPathKindCombo.selectedItem = settings.lspPathKind
        customCommandLineField.text = settings.customCommandLine
        fileExtensionsField.text = settings.fileExtensions
        debugHostField.text = settings.debugHost
        debugPortField.text = settings.debugPort.toString()
        autoReconnectCheckBox.isSelected = settings.autoReconnectDebugger
        reconnectDelayField.text = settings.debugReconnectDelayMs.toString()
        logDebugMessagesCheckBox.isSelected = settings.logDebugMessages
        focusRiderWhenBreakingCheckBox.isSelected = settings.focusRiderWhenBreaking
        cppHeaderResolutionCheckBox.isSelected = settings.cppHeaderResolutionEnabled
        cppFunctionPatternField.text = settings.cppFunctionPattern
        cppClassPatternField.text = settings.cppClassPattern
        cppEnumPatternField.text = settings.cppEnumPattern
        updateLspPathVisibility()
    }
}
