package com.aesirinteractive.angelscript

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

enum class LspPathKind { Bundled, VsCode, Custom, CommandLine }

@Service(Service.Level.APP)
@State(name = "AngelscriptSettings", storages = [Storage("angelscript.xml")])
class AngelscriptSettings : PersistentStateComponent<AngelscriptSettings.State> {

    data class State(
        var nodePath: String = "node",
        var lspPath: String = "",
        var lspPathKind: LspPathKind = LspPathKind.Bundled,
        var customCommandLine: String = "",
        var fileExtensions: String = "as",
        var debugHost: String = "127.0.0.1",
        var debugPort: Int = 27099,
        var autoReconnectDebugger: Boolean = true,
        var debugReconnectDelayMs: Long = 10000L,
        var logDebugMessages: Boolean = false,
        var focusRiderWhenBreaking: Boolean = true,
        var cppHeaderResolutionEnabled: Boolean = false,
        var cppFunctionPattern: String = """UFUNCTION\(.*?\r?\n.*?NAME\s*\(""",
        var cppClassPattern: String = """U(?:CLASS|STRUCT|INTERFACE)\(.*?\r?\n.*?(?:class|struct)\s+(?:\w+\s+)?NAME\b""",
        var cppEnumPattern: String = """UENUM\(.*?\r?\n.*?enum(?:\s+class)?\s+NAME\b""",
        var cppHeaderScanParallelism: Int = 10,
    )

    private var state = State()

    override fun getState() = state

    override fun loadState(state: State) {
        this.state = state
    }

    var nodePath: String
        get() = state.nodePath
        set(value) { state.nodePath = value }

    var lspPath: String
        get() = state.lspPath
        set(value) { state.lspPath = value }

    var lspPathKind: LspPathKind
        get() = state.lspPathKind
        set(value) { state.lspPathKind = value }

    var customCommandLine: String
        get() = state.customCommandLine
        set(value) { state.customCommandLine = value }

    var fileExtensions: String
        get() = state.fileExtensions
        set(value) { state.fileExtensions = value }

    var debugHost: String
        get() = state.debugHost
        set(value) { state.debugHost = value }

    var debugPort: Int
        get() = state.debugPort
        set(value) { state.debugPort = value }

    var autoReconnectDebugger: Boolean
        get() = state.autoReconnectDebugger
        set(value) { state.autoReconnectDebugger = value }

    var debugReconnectDelayMs: Long
        get() = state.debugReconnectDelayMs
        set(value) { state.debugReconnectDelayMs = value }

    var logDebugMessages: Boolean
        get() = state.logDebugMessages
        set(value) { state.logDebugMessages = value }

    var focusRiderWhenBreaking: Boolean
        get() = state.focusRiderWhenBreaking
        set(value) { state.focusRiderWhenBreaking = value }

    var cppHeaderResolutionEnabled: Boolean
        get() = state.cppHeaderResolutionEnabled
        set(value) { state.cppHeaderResolutionEnabled = value }

    var cppFunctionPattern: String
        get() = state.cppFunctionPattern
        set(value) { state.cppFunctionPattern = value }

    var cppClassPattern: String
        get() = state.cppClassPattern
        set(value) { state.cppClassPattern = value }

    var cppEnumPattern: String
        get() = state.cppEnumPattern
        set(value) { state.cppEnumPattern = value }

    var cppHeaderScanParallelism: Int
        get() = state.cppHeaderScanParallelism
        set(value) { state.cppHeaderScanParallelism = value }

    /** Converts a pattern template (using NAME as placeholder) to a compiled Regex for [name]. */
    fun compileCppPattern(template: String, name: String): Regex =
        Regex(template.replace("NAME", Regex.escape(name)))

    fun parsedFileExtensions(): Set<String> =
        fileExtensions.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

    companion object {
        fun getInstance(): AngelscriptSettings =
            ApplicationManager.getApplication().getService(AngelscriptSettings::class.java)
    }
}
