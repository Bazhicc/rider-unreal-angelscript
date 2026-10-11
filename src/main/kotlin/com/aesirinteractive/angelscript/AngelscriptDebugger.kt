package com.aesirinteractive.angelscript

import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.executors.DefaultDebugExecutor
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.runners.GenericProgramRunner
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.execution.ui.RunContentManager
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.platform.lsp.api.LspServerState
import org.eclipse.lsp4j.*
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicBoolean
import com.intellij.openapi.util.Disposer
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.ui.ColoredTextContainer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.xdebugger.DefaultDebugProcessHandler
import com.intellij.xdebugger.XDebugProcess
import com.intellij.xdebugger.XDebugProcessStarter
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.XDebuggerUtil
import com.intellij.xdebugger.XExpression
import com.intellij.xdebugger.XSourcePosition
import com.intellij.xdebugger.breakpoints.XBreakpointHandler
import com.intellij.xdebugger.breakpoints.XBreakpointProperties
import com.intellij.xdebugger.breakpoints.XLineBreakpoint
import com.intellij.xdebugger.breakpoints.XLineBreakpointType
import com.intellij.xdebugger.evaluation.EvaluationMode
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator
import com.intellij.xdebugger.frame.XCompositeNode
import com.intellij.xdebugger.frame.XExecutionStack
import com.intellij.xdebugger.frame.XExecutionStack.XStackFrameContainer
import com.intellij.xdebugger.frame.XNamedValue
import com.intellij.xdebugger.frame.XStackFrame
import com.intellij.xdebugger.frame.XSuspendContext
import com.intellij.xdebugger.frame.XValueChildrenList
import com.intellij.xdebugger.frame.XValueGroup
import com.intellij.xdebugger.frame.XValueNode
import com.intellij.xdebugger.frame.XValuePlace
import com.intellij.xdebugger.frame.presentation.XStringValuePresentation
import org.jetbrains.annotations.Nls
import javax.swing.JComponent
import javax.swing.JPanel

class AngelScriptDebugState(environment: ExecutionEnvironment, runConfiguration: AngelScriptRunConfiguration) : RunProfileState {
    override fun execute(p0: Executor?, p1: ProgramRunner<*>): ExecutionResult? {
        return null
    }
}

class AngelScriptRunConfiguration(
    project: Project,
    factory: ConfigurationFactory
) : RunConfigurationBase<Any>(project, factory, "AngelScript") {

    var scriptPath: String = ""

    override fun getType(): ConfigurationType {
        return AngelscriptConfigurationType()
    }

    override fun getState(
        executor: Executor,
        environment: ExecutionEnvironment
    ): RunProfileState {
        return AngelScriptDebugState(environment, this)
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration?> {
        return AngelScriptRunConfigurationEditor()
    }
}

private class AngelScriptRunConfigurationEditor : SettingsEditor<AngelScriptRunConfiguration>() {
    private val panel = JPanel()
    override fun createEditor() = panel
    override fun resetEditorFrom(s: AngelScriptRunConfiguration) {}
    override fun applyEditorTo(s: AngelScriptRunConfiguration) {}
}

class AngelscriptConfigurationFactory(config: AngelscriptConfigurationType) : ConfigurationFactory(config) {
    override fun createTemplateConfiguration(p0: Project): RunConfiguration {
        return AngelScriptRunConfiguration(p0, this)
    }

    override fun getId() = "AngelscriptConfigurationFactory"
}

class AngelscriptConfigurationType : ConfigurationType {

    override fun getDisplayName() = "AngelScript"

    override fun getConfigurationTypeDescription() = "Angelscript debugger run config"

    override fun getIcon() = AllIcons.RunConfigurations.Application

    override fun getId() = "ANGELSCRIPT_DEBUGGER"

    override fun getConfigurationFactories(): Array<out ConfigurationFactory?> =
        arrayOf(AngelscriptConfigurationFactory(this))
}

class AngelscriptProgramRunner : GenericProgramRunner<RunnerSettings>() {

    override fun getRunnerId() = "AngelscriptDebugger"

    override fun canRun(executorId: String, profile: RunProfile): Boolean {
        return executorId == DefaultDebugExecutor.EXECUTOR_ID && profile is AngelScriptRunConfiguration
    }

    override fun doExecute(state: RunProfileState, environment: ExecutionEnvironment): RunContentDescriptor? {
        val settings = AngelscriptSettings.getInstance()
        return XDebuggerManager.getInstance(environment.project)
            .newSessionBuilder(AngelscriptDebugProcessStarter(settings.debugHost, settings.debugPort))
            .environment(environment)
            .sessionName(environment.runProfile.name)
            .contentToReuse(environment.contentToReuse)
            .showTab(true)
            .startSession().runContentDescriptor
    }
}

class AngelscriptDebugProcessStarter(
    private val host: String = "127.0.0.1",
    private val port: Int = 27099
) : XDebugProcessStarter() {
    override fun start(session: XDebugSession): XDebugProcess {
        return AngelscriptProcess(session, host, port)
    }
}

class AngelscriptDebuggerEditorsProvider : XDebuggerEditorsProvider() {
    override fun getFileType(): FileType {
        return AngelscriptFileType.INSTANCE
    }

    override fun createDocument(project: Project, expression: XExpression, sourcePosition: XSourcePosition?, mode: EvaluationMode): Document {
        return EditorFactory.getInstance().createDocument(expression.expression.toCharArray())
    }
}

class AngelscriptBreakpointHandler(
    private val process: AngelscriptProcess
) : XBreakpointHandler<XLineBreakpoint<AngelscriptBreakpointProperties>>(
    AngelscriptBreakpointType::class.java
) {
    private val breakpointIds = LinkedHashMap<XLineBreakpoint<AngelscriptBreakpointProperties>, Int>()
    private var nextId = 1
    // Paths with a pending deferred flush; deduplicated so only one invokeLater fires per path per EDT cycle
    private val pendingFlush = HashSet<String>()
    // Paths whose files have unsaved changes; breakpoints are not sent to the server while dirty
    internal val dirtyPaths = HashSet<String>()

    private fun dbgLog(msg: String) {
        if (AngelscriptSettings.getInstance().logDebugMessages) {
            process.session.consoleView?.print("[dbg] $msg\n",
                com.intellij.execution.ui.ConsoleViewContentType.SYSTEM_OUTPUT)
        }
    }

    private fun scheduleFlush(path: String, module: String) {
        val shouldSchedule = synchronized(pendingFlush) { pendingFlush.add(path) }
        if (shouldSchedule) {
            ApplicationManager.getApplication().invokeLater {
                synchronized(pendingFlush) { pendingFlush.remove(path) }
                flushFile(path, module)
            }
        }
    }

    private fun flushFile(path: String, module: String) {
        val bpsForFile = synchronized(breakpointIds) {
            breakpointIds.entries.filter { it.key.fileUrl.removePrefix("file://") == path }.toList()
        }
        val isDirty = synchronized(dirtyPaths) { path in dirtyPaths }
        dbgLog("flushFile path=$path count=${bpsForFile.size}, dirty=$isDirty")
        process.client.sendClearBreakpoints(path, module)
        if (!isDirty) {
            for ((bp, id) in bpsForFile) {
                ApplicationManager.getApplication().invokeLater {
                    process.session.setBreakpointVerified(bp)
                }
                process.client.sendSetBreakpoint(id, path, bp.line + 1, module)
            }
        }
    }

    fun markFileDirty(path: String) {
        synchronized(dirtyPaths) { dirtyPaths.add(path) }
        val module = process.moduleNameForPath(path)
        val bpsForFile = synchronized(breakpointIds) {
            breakpointIds.entries.filter { it.key.fileUrl.removePrefix("file://") == path }.toList()
        }
        dbgLog("markFileDirty path=$path — invalidating ${bpsForFile.size} breakpoint(s)")
        if (bpsForFile.isNotEmpty()) {
            process.client.sendClearBreakpoints(path, module)
            ApplicationManager.getApplication().invokeLater {
                for ((bp, _) in bpsForFile) {
                    process.session.setBreakpointInvalid(bp, "File has unsaved changes")
                }
            }
        }
    }

    fun syncFile(path: String) {
        val module = process.moduleNameForPath(path)
        val bpsForFile = synchronized(breakpointIds) {
            breakpointIds.entries.filter { it.key.fileUrl.removePrefix("file://") == path }.toList()
        }
        dbgLog("syncFile path=$path count=${bpsForFile.size}")
        val toSend = synchronized(breakpointIds) {
            bpsForFile.map { (bp, _) ->
                val newId = nextId++
                breakpointIds[bp] = newId
                Pair(bp, newId)
            }
        }
        process.client.sendClearBreakpoints(path, module)
        for ((bp, id) in toSend) {
            ApplicationManager.getApplication().invokeLater {
                process.session.setBreakpointVerified(bp)
            }
            process.client.sendSetBreakpoint(id, path, bp.line + 1, module)
        }
    }

    override fun registerBreakpoint(bp: XLineBreakpoint<AngelscriptBreakpointProperties>) {
        val path = bp.fileUrl.removePrefix("file://")
        val line = bp.line + 1 // XDebugger is 0-based, Unreal expects 1-based
        val module = process.moduleNameForPath(path)
        // Reuse the ID of any existing breakpoint at the same line (VS Code pattern),
        // otherwise assign a new one.
        val id = synchronized(breakpointIds) {
            val existing = breakpointIds.entries
                .find { it.key.fileUrl.removePrefix("file://") == path && it.key.line + 1 == line }
            val resolvedId = existing?.value ?: nextId++
            breakpointIds[bp] = resolvedId
            resolvedId
        }
        val isDirty = synchronized(dirtyPaths) { path in dirtyPaths }
        dbgLog("register id=$id path=$path line=$line dirty=$isDirty")
        if (isDirty) {
            ApplicationManager.getApplication().invokeLater {
                process.session.setBreakpointInvalid(bp, "File has unsaved changes")
            }
        } else {
            ApplicationManager.getApplication().invokeLater {
                process.session.setBreakpointVerified(bp)
            }
            scheduleFlush(path, module)
        }
    }

    override fun unregisterBreakpoint(bp: XLineBreakpoint<AngelscriptBreakpointProperties>, temporary: Boolean) {
        val path = bp.fileUrl.removePrefix("file://")
        val line = bp.line + 1
        val module = process.moduleNameForPath(path)
        val id = synchronized(breakpointIds) { breakpointIds.remove(bp) }
        val isDirty = synchronized(dirtyPaths) { path in dirtyPaths }
        dbgLog("unregister id=$id path=$path line=$line temporary=$temporary dirty=$isDirty")
        scheduleFlush(path, module)
    }

    fun syncAllBreakpoints() {
        val snapshot = synchronized(breakpointIds) {
            nextId = 1
            breakpointIds.entries.map { (bp, _) ->
                val newId = nextId++
                breakpointIds[bp] = newId
                Triple(bp, newId, bp.fileUrl.removePrefix("file://"))
            }
        }
        dbgLog("syncAllBreakpoints count=${snapshot.size}")
        // Clear per file first (matches VS Code launchRequest pattern), then set each
        val byFile = snapshot.groupBy { (_, _, path) -> path }
        for ((path, entries) in byFile) {
            val module = process.moduleNameForPath(path)
            process.client.sendClearBreakpoints(path, module)
            for ((bp, newId, _) in entries) {
                ApplicationManager.getApplication().invokeLater {
                    process.session.setBreakpointVerified(bp)
                }
                process.client.sendSetBreakpoint(newId, path, bp.line + 1, module)
            }
        }
    }

    fun findBreakpointById(id: Int): XLineBreakpoint<AngelscriptBreakpointProperties>? {
        return synchronized(breakpointIds) {
            breakpointIds.entries.find { it.value == id }?.key
        }
    }

    fun hasBreakpointAt(path: String, line: Int): Boolean {
        return synchronized(breakpointIds) {
            breakpointIds.keys.any { it.fileUrl.removePrefix("file://") == path && it.line + 1 == line }
        }
    }
}

class AngelscriptProcessHandler : DefaultDebugProcessHandler() {
    override fun destroyProcessImpl() {
        super.destroyProcessImpl()
    }

    override fun detachProcessImpl() {
        super.detachProcessImpl()
    }
}

class AngelscriptSuspendContext(
    private val process: AngelscriptProcess,
    frames: List<CallStackFrame>
) : XSuspendContext() {
    private val stack = AngelscriptStack(process, frames)

    override fun getActiveExecutionStack(): XExecutionStack = stack

    override fun getExecutionStacks(): Array<out XExecutionStack> = arrayOf(stack)
}

class AngelscriptStack(
    private val process: AngelscriptProcess,
    frames: List<CallStackFrame>
) : XExecutionStack("AngelScript") {

    private val stackFrames: List<AngelscriptStackFrame> = frames.mapIndexed { idx, f ->
        AngelscriptStackFrame(idx, f.name, f.sourcePath, f.line, process)
    }

    override fun getTopFrame(): XStackFrame? = stackFrames.firstOrNull()

    override fun computeStackFrames(firstFrameIndex: Int, container: XStackFrameContainer?) {
        if (firstFrameIndex == 0) {
            container?.addStackFrames(stackFrames, true)
        }
    }
}

class AngelscriptScopeGroup(
    label: String,
    private val scopePath: String,   // e.g. "0:%local%"
    private val process: AngelscriptProcess
) : XValueGroup(label) {
    private val generation = process.debugGeneration.get()
    override fun isAutoExpand() = scopePath in process.expandedPaths

    override fun computeChildren(node: XCompositeNode) {
        process.expandedPaths.add(scopePath)
        process.requestVariables(node, scopePath, last = true, generation)
    }
}

class AngelscriptNamedValue(
    private val name: String,
    private val value: String,
    private val type: String,
    private val hasMembers: Boolean,
    // Full dotted path used to request child variables, e.g. "0:%local%.myVar"
    private val variablePath: String,
    private val process: AngelscriptProcess
) : XNamedValue(name) {
    private val generation = process.debugGeneration.get()
    override fun computePresentation(node: XValueNode, place: XValuePlace) {
        node.setPresentation(null, XStringValuePresentation(value), hasMembers)
    }

    override fun computeChildren(node: XCompositeNode) {
        if (hasMembers) {
            process.expandedPaths.add(variablePath)
            process.requestVariables(node, variablePath, last = true, generation)
        }
    }

//    override fun isAutoExpandNode() = hasMembers && variablePath in process.expandedPaths
}

class AngelscriptEvaluator(
    private val frameIndex: Int,
    private val process: AngelscriptProcess
) : XDebuggerEvaluator() {

    private val generation = process.debugGeneration.get()

    override fun evaluate(expression: String, callback: XEvaluationCallback, expressionPosition: XSourcePosition?) {
        process.requestEvaluation(expression, frameIndex, generation, { result ->
            if (result.value.isEmpty()) callback.errorOccurred("No result for '$expression'")
            else callback.evaluated(AngelscriptNamedValue(result.name.ifEmpty { expression }, result.value,
                result.type, result.hasMembers, "$frameIndex:$expression", process))
        }, callback::errorOccurred)
    }

    fun evaluateInline(name: String, onResult: (name: String, value: String) -> Unit) {
        process.requestEvaluation(name, frameIndex, generation, { result ->
            if (result.value.isNotEmpty()) onResult(name, result.value)
        }, {})
    }

    override fun getExpressionRangeAtOffset(project: Project, document: Document, offset: Int, sideEffectsAllowed: Boolean): com.intellij.openapi.util.TextRange? =
        AngelscriptDebugExpressions.range(document, offset)

}

class AngelscriptStackFrame(
    private val index: Int,
    private val functionName: String,
    private val sourcePath: String,
    private val lineNumber: Int,
    private val process: AngelscriptProcess
) : XStackFrame() {
    val evaluator = AngelscriptEvaluator(index, process)

    override fun getEvaluator(): XDebuggerEvaluator = evaluator

    override fun getSourcePosition(): XSourcePosition? {
        if (sourcePath.isEmpty() || sourcePath.startsWith("::")) return null
        val vFile = LocalFileSystem.getInstance().findFileByPath(sourcePath) ?: return null
        // XSourcePosition lines are 0-based, Unreal sends 1-based
        return XDebuggerUtil.getInstance().createPosition(vFile, (lineNumber - 1).coerceAtLeast(0))
    }

    override fun customizePresentation(component: ColoredTextContainer) {
        if (sourcePath.isNotEmpty() && !sourcePath.startsWith("::")) {
            val fileName = sourcePath.substringAfterLast('/').substringAfterLast('\\')
            component.append(functionName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
            component.append(" ($fileName:$lineNumber)", SimpleTextAttributes.GRAYED_ATTRIBUTES)
        } else {
            component.append(functionName, SimpleTextAttributes.REGULAR_ATTRIBUTES)
        }
        component.setIcon(AllIcons.Debugger.Frame)
    }

    override fun computeChildren(node: XCompositeNode) {
        val scopes = XValueChildrenList()
        scopes.addTopGroup(AngelscriptScopeGroup("Local", "$index:%local%", process))
        scopes.addTopGroup(AngelscriptScopeGroup("This", "$index:%this%", process))
        scopes.addTopGroup(AngelscriptScopeGroup("Globals", "$index:%module%", process))
        node.addChildren(scopes, true)
    }
}

class AngelscriptProcess(
    session: XDebugSession,
    val host: String,
    val port: Int
) : XDebugProcess(session) {

    val client = UnrealDebugClient(host, port)

    private val stopped = AtomicBoolean()
    private val reconnecting = AtomicBoolean(true)
    private val sessionDisposable = Disposer.newDisposable("AngelScript debug session")
    internal val debugGeneration = AtomicLong()
    private val evaluationRequests = OrderedDebugRequests<DebugVariable>(
        AppExecutorUtil.getAppScheduledExecutorService(), ::requestTimedOut)
    private val variableRequests = OrderedDebugRequests<List<DebugVariable>>(
        AppExecutorUtil.getAppScheduledExecutorService(), ::requestTimedOut)

    init { Disposer.register(session.project, sessionDisposable); Disposer.register(sessionDisposable, { shutdown() }) }

    // Paths the user has explicitly expanded; keyed by variable path e.g. "0:%local%", "0:%local%.myVar"
    val expandedPaths: MutableSet<String> = java.util.concurrent.ConcurrentHashMap.newKeySet()

    // Evaluator of the currently selected frame; updated on positionReached
    @Volatile var activeEvaluator: AngelscriptEvaluator? = null

    private val breakpointHandler = AngelscriptBreakpointHandler(this)

    private fun deliver(generation: Long, action: () -> Unit) {
        ApplicationManager.getApplication().invokeLater {
            if (!stopped.get() && !session.project.isDisposed && generation == debugGeneration.get()) action()
        }
    }

    internal fun requestEvaluation(expression: String, frame: Int, generation: Long,
                                   result: (DebugVariable) -> Unit, error: (String) -> Unit) {
        if (stopped.get() || generation != debugGeneration.get()) {
            error("Stack frame is no longer suspended")
            return
        }
        evaluationRequests.submit({ client.sendRequestEvaluate(expression, frame) },
            { value -> deliver(generation) { result(value) } },
            { message -> ApplicationManager.getApplication().invokeLater { if (!session.project.isDisposed) error(message) } })
    }

    fun requestVariables(node: XCompositeNode, path: String, last: Boolean, generation: Long) {
        if (stopped.get() || generation != debugGeneration.get()) return
        variableRequests.submit({ client.sendRequestVariables(path) }, { variables ->
            deliver(generation) {
                val children = XValueChildrenList()
                for (v in variables) children.add(AngelscriptNamedValue(v.name, v.value, v.type, v.hasMembers, "${path}.${v.name}", this))
                node.addChildren(children, last)
            }
        }, { message -> ApplicationManager.getApplication().invokeLater {
            if (!session.project.isDisposed && !node.isObsolete) node.setErrorMessage(message)
        } })
    }

    private fun invalidateSuspension(reason: String) {
        debugGeneration.incrementAndGet()
        inlineGeneration.incrementAndGet()
        activeEvaluator = null
        evaluationRequests.invalidate(reason)
        variableRequests.invalidate(reason)
        if (!session.project.isDisposed) AngelscriptInlineValuesService.getInstance(session.project).clear()
    }

    private fun requestTimedOut() {
        connectionLost(closeTransport = true)
    }

    private fun connectionLost(closeTransport: Boolean = false) {
        if (stopped.get() || session.project.isDisposed || !reconnecting.compareAndSet(false, true)) return
        if (closeTransport) client.disconnect()
        invalidateSuspension("Debugger disconnected")
        evaluationRequests.reset("Debugger disconnected", false)
        variableRequests.reset("Debugger disconnected", false)
        expandedPaths.clear()
        runToPath = null
        runToLine = -1
        val settings = AngelscriptSettings.getInstance()
        ApplicationManager.getApplication().invokeLater {
            if (stopped.get() || session.project.isDisposed) return@invokeLater
            if (settings.autoReconnectDebugger) {
                session.sessionResumed()
                session.consoleView?.print("Disconnected from Unreal AngelScript debug server. Reconnecting...\n",
                    com.intellij.execution.ui.ConsoleViewContentType.SYSTEM_OUTPUT)
                startConnecting(true, settings.debugReconnectDelayMs)
            } else session.stop()
        }
    }

    fun moduleNameForPath(path: String): String {
        // Convert a file path to a module name (e.g. Script/MyGame/Foo.as → Script.MyGame.Foo)
        return path.substringAfterLast("/Script/")
            .replace('/', '.')
            .replace('\\', '.')
            .removeSuffix(".as")
    }

    override fun sessionInitialized() {
        if (stopped.get()) return
        session.setPauseActionSupported(true);

        client.onServerVersion = { version ->
            session.consoleView?.print("AngelScript debug server version: $version\n",
                com.intellij.execution.ui.ConsoleViewContentType.SYSTEM_OUTPUT)
        }

        client.onStopped = { reason, _, _ ->
            client.sendRequestCallStack()
        }

        client.onContinued = {
            invalidateSuspension("Execution resumed")
            ApplicationManager.getApplication().invokeLater {
                if (!stopped.get() && !session.project.isDisposed) session.sessionResumed()
            }
        }

        client.onCallStack = { frames ->
            invalidateSuspension("Stack updated")
            // If we planted a run-to-cursor temporary breakpoint, remove it when we stop there
            val rtp = runToPath
            val rtl = runToLine
            val topFrame = frames.firstOrNull()
            if (rtp != null && topFrame != null && topFrame.sourcePath == rtp && topFrame.line == rtl) {
                runToPath = null
                runToLine = -1
                // syncFile clears then re-sends only real breakpoints, removing the ID-0 temporary one
                breakpointHandler.syncFile(rtp)
            }
            // Prune expansion state for frame indices that no longer exist
            val maxFrameIdx = frames.size - 1
            expandedPaths.removeAll { path ->
                val frameIdx = path.substringBefore(":").toIntOrNull()
                frameIdx != null && frameIdx > maxFrameIdx
            }
            val context = AngelscriptSuspendContext(this, frames)
            // Top frame (index 0) is the active one for watch evaluation
            activeEvaluator = (context.activeExecutionStack.topFrame as? AngelscriptStackFrame)?.evaluator
            // Collect and evaluate inline variable values for the stopped location
            collectInlineValues(frames)
            deliver(debugGeneration.get()) {
                if (AngelscriptSettings.getInstance().focusRiderWhenBreaking) {
                    WindowManager.getInstance().getFrame(session.project)?.apply {
                        toFront()
                        requestFocus()
                    }
                    val content = RunContentManager.getInstance(session.project)
                    val executor = DefaultDebugExecutor.getDebugExecutorInstance()
                    content.findContentDescriptor(executor, processHandler)?.let { content.toFrontRunContent(executor, it) }
                }
                session.positionReached(context)
            }
        }

        client.onEvaluate = { name, value, type, hasMembers ->
            evaluationRequests.receive(DebugVariable(name, value, type, hasMembers))
        }

        client.onScriptRecompiled = {
            val recompiledPaths = synchronized(breakpointHandler.dirtyPaths) {
                breakpointHandler.dirtyPaths.toList().also { breakpointHandler.dirtyPaths.clear() }
            }
            if (recompiledPaths.isEmpty()) {
                breakpointHandler.syncAllBreakpoints()
            } else {
                for (path in recompiledPaths) {
                    breakpointHandler.syncFile(path)
                }
            }
        }

        client.onBreakpointAdjusted = handler@{ id, path, adjustedLine ->
            val bp = breakpointHandler.findBreakpointById(id) ?: return@handler
            ApplicationManager.getApplication().invokeLater {
                when {
                    adjustedLine == -1 -> {
                        session.setBreakpointInvalid(bp, "No executable code at this line")
                    }
                    adjustedLine != bp.line + 1 -> {
                        // Server placed the breakpoint at a different line (script not yet recompiled).
                        // Check if another breakpoint already exists at the server's line.
                        val bpType = bp.type as? AngelscriptBreakpointType ?: return@invokeLater
                        val vFile = LocalFileSystem.getInstance().findFileByPath(path)
                            ?: return@invokeLater
                        val serverLine = adjustedLine - 1 // server is 1-based, IDE is 0-based
                        val mgr = XDebuggerManager.getInstance(session.project).breakpointManager
                        val existing = mgr.findBreakpointsAtLine(bpType, vFile, serverLine)
                        if (existing.any { it !== bp }) {
                            // Overlap — this bp is redundant, mark it invalid
                            session.setBreakpointInvalid(bp, "Overlaps existing breakpoint at line $adjustedLine")
                        } else {
                            // Move the IDE marker to the server's confirmed line.
                            // removeBreakpoint + addLineBreakpoint triggers unregister+register
                            // which will flush a new SetBreakpoint at the confirmed line.
                            mgr.removeBreakpoint(bp)
                            mgr.addLineBreakpoint(bpType, bp.fileUrl, serverLine, bp.properties)
                        }
                    }
                    else -> {
                        session.setBreakpointVerified(bp)
                    }
                }
            }
        }

        session.addSessionListener(object : com.intellij.xdebugger.XDebugSessionListener {
            override fun stackFrameChanged() {
                val frame = session.currentStackFrame
                activeEvaluator = (frame as? AngelscriptStackFrame)?.evaluator
            }
            override fun sessionStopped() { stop() }
        }, sessionDisposable)

        client.onVariables = variableRequests::receive

        val settings = AngelscriptSettings.getInstance()
        val autoReconnect = settings.autoReconnectDebugger
        val reconnectDelayMs = settings.debugReconnectDelayMs

        client.onMessageLog = { direction, type ->
            if (AngelscriptSettings.getInstance().logDebugMessages) {
                session.consoleView?.print("[dbg] $direction $type\n",
                    com.intellij.execution.ui.ConsoleViewContentType.SYSTEM_OUTPUT)
            }
        }

        client.onClosed = { connectionLost() }

        // Add a default watch for GetWorldDebugString() if not already present
//        val defaultExprText = "GetWorldDebugString()"
//        val managerImpl = XDebuggerManager.getInstance(session.project)
//            as? com.intellij.xdebugger.impl.XDebuggerManagerImpl
//        val watchesManager = managerImpl?.watchesManager
//        if (watchesManager != null &&
//            watchesManager.watchExpressions.none { it.expression == defaultExprText }) {
//            val expr = com.intellij.xdebugger.impl.breakpoints.XExpressionImpl(
//                defaultExprText, null, null, EvaluationMode.EXPRESSION
//            )
//            watchesManager.addWatchExpression(
//                session as com.intellij.xdebugger.impl.XDebugSessionImpl,
//                expr, -1, false
//            )
//        }

        // Invalidate breakpoints when an .as file is modified (before recompile)
        EditorFactory.getInstance().eventMulticaster
            .addDocumentListener(object : com.intellij.openapi.editor.event.DocumentListener {
                override fun documentChanged(event: com.intellij.openapi.editor.event.DocumentEvent) {
                    if (stopped.get()) return
                    val vFile = FileDocumentManager.getInstance().getFile(event.document) ?: return
                    if (vFile.extension != "as") return
                    val path = vFile.path
                    val alreadyDirty = synchronized(breakpointHandler.dirtyPaths) {
                        path in breakpointHandler.dirtyPaths
                    }
                    if (!alreadyDirty) {
                        breakpointHandler.markFileDirty(path)
                    }
                }
            }, sessionDisposable)

        startConnecting(autoReconnect, reconnectDelayMs)
    }

    private fun startConnecting(autoReconnect: Boolean, retryDelayMs: Long) {
        if (stopped.get() || session.project.isDisposed) return
        val consoleView = session.consoleView
        val maxRetries = if (autoReconnect) -1 else 30
        client.connectWithRetry(
            maxRetries = maxRetries,
            retryDelayMs = retryDelayMs,
            onConnected = {
                reconnecting.set(false)
                evaluationRequests.reset("Connection replaced", true)
                variableRequests.reset("Connection replaced", true)
                consoleView?.print("Connected to Unreal AngelScript debug server at $host:$port\n",
                    com.intellij.execution.ui.ConsoleViewContentType.SYSTEM_OUTPUT)
                client.sendStartDebugging(2)
                breakpointHandler.syncAllBreakpoints()
            },
            onRetry = { attempt ->
                if (autoReconnect) {
                    consoleView?.print("Waiting for Unreal Engine... (attempt $attempt)\n",
                        com.intellij.execution.ui.ConsoleViewContentType.SYSTEM_OUTPUT)
                } else {
                    consoleView?.print("Waiting for Unreal Engine... (attempt $attempt/30)\n",
                        com.intellij.execution.ui.ConsoleViewContentType.SYSTEM_OUTPUT)
                }
            },
            onFailed = {
                consoleView?.print("Failed to connect to Unreal AngelScript debug server at $host:$port\n",
                    com.intellij.execution.ui.ConsoleViewContentType.ERROR_OUTPUT)
                ApplicationManager.getApplication().invokeLater {
                    session.stop()
                }
            }
        )
    }

    override fun doGetProcessHandler(): ProcessHandler = AngelscriptProcessHandler()

    override fun checkCanInitBreakpoints(): Boolean = true

    override fun getBreakpointHandlers(): Array<out XBreakpointHandler<*>> {
        return arrayOf(breakpointHandler)
    }

    override fun getEditorsProvider(): XDebuggerEditorsProvider {
        return AngelscriptDebuggerEditorsProvider()
    }

    override fun startPausing() {
        client.sendPause()
    }

    override fun startStepOver(context: XSuspendContext?) {
        invalidateSuspension("Stepping")
        client.sendStepOver()
    }

    override fun startStepInto(context: XSuspendContext?) {
        invalidateSuspension("Stepping")
        client.sendStepIn()
    }

    override fun startStepOut(context: XSuspendContext?) {
        invalidateSuspension("Stepping")
        client.sendStepOut()
    }

    override fun resume(context: XSuspendContext?) {
        invalidateSuspension("Execution resumed")
        client.sendContinue()
    }

    // Tracks a temporary "run to cursor" breakpoint; null when not active
    @Volatile private var runToPath: String? = null
    @Volatile private var runToLine: Int = -1  // 1-based

    override fun runToPosition(position: XSourcePosition, context: XSuspendContext?) {
        invalidateSuspension("Execution resumed")
        val path = position.file.path
        val line = position.line + 1  // XSourcePosition is 0-based, Unreal expects 1-based
        val module = moduleNameForPath(path)
        // Only plant a temporary breakpoint if there isn't already a user breakpoint at this line
        if (!breakpointHandler.hasBreakpointAt(path, line)) {
            runToPath = path
            runToLine = line
            // Use ID 0 — reserved for run-to-cursor, never used by the regular nextId counter (starts at 1)
            client.sendSetBreakpoint(0, path, line, module)
        }
        client.sendContinue()
    }

    override fun stop() {
        shutdown()
        if (!Disposer.isDisposed(sessionDisposable)) Disposer.dispose(sessionDisposable)
    }

    private fun shutdown() {
        if (!stopped.compareAndSet(false, true)) return
        client.sendStopDebugging()
        client.disconnect()
        invalidateSuspension("Debug session stopped")
        evaluationRequests.reset("Debug session stopped", false)
        variableRequests.reset("Debug session stopped", false)
        expandedPaths.clear()
    }

    private val inlineGeneration = AtomicLong()

    private fun collectInlineValues(frames: List<CallStackFrame>) {
        val generation = inlineGeneration.incrementAndGet()
        val topFrame = frames.firstOrNull() ?: return
        val vFile = LocalFileSystem.getInstance().findFileByPath(topFrame.sourcePath) ?: return
        val evaluator = activeEvaluator ?: return
        val stopLine = topFrame.line - 1
        val service = AngelscriptInlineValuesService.getInstance(session.project)
        ApplicationManager.getApplication().executeOnPooledThread {
            if (stopped.get() || session.project.isDisposed) return@executeOnPooledThread
            val snapshot = ReadAction.compute<Pair<String, Long>?, RuntimeException> {
                val document = FileDocumentManager.getInstance().getDocument(vFile) ?: return@compute null
                if (stopLine !in 0 until document.lineCount) return@compute null
                document.text to document.modificationStamp
            } ?: return@executeOnPooledThread
            if (stopped.get() || session.project.isDisposed) return@executeOnPooledThread
            val server = LspServerManager.getInstance(session.project)
                .getServersForProvider(AngelscriptLspServerSupportProvider::class.java)
                .firstOrNull { it.state == LspServerState.Running && it.descriptor.isSupportedFile(vFile) }
                ?: return@executeOnPooledThread
            val stopped = org.eclipse.lsp4j.Range(Position(stopLine, 0), Position(stopLine, 0))
            val params = InlineValueParams(server.getDocumentIdentifier(vFile), stopped, InlineValueContext(0, stopped))
            val values = try {
                // Never hold a PSI read lock or block the EDT while querying the server.
                server.sendRequestSync(2000) { it.textDocumentService.inlineValue(params) }
            } catch (e: com.intellij.openapi.progress.ProcessCanceledException) {
                throw e
            } catch (_: Exception) { return@executeOnPooledThread } ?: return@executeOnPooledThread
            val expressions = inlineExpressions(values, snapshot.first)
            ApplicationManager.getApplication().invokeLater {
                if (generation != inlineGeneration.get() || session.project.isDisposed) return@invokeLater
                val document = FileDocumentManager.getInstance().getDocument(vFile) ?: return@invokeLater
                if (document.modificationStamp != snapshot.second) return@invokeLater
                for (entry in expressions) {
                    evaluator.evaluateInline(entry.expression) { name, value ->
                        if (generation == inlineGeneration.get() && document.modificationStamp == snapshot.second)
                            service.putValue(vFile, entry.line, "$name = $value")
                    }
                }
            }
        }
    }
}

class AngelscriptBreakpointProperties : XBreakpointProperties<Any>() {
    override fun getState(): Any? = null
    override fun loadState(p0: Any) {}
}

class AngelscriptBreakpointType : XLineBreakpointType<AngelscriptBreakpointProperties>(
    "angelscriptBreakpoint",
    "Angelscript Breakpoint"
) {
    override fun createBreakpointProperties(
        p0: com.intellij.openapi.vfs.VirtualFile,
        p1: Int
    ): AngelscriptBreakpointProperties = AngelscriptBreakpointProperties()

    override fun canPutAt(file: com.intellij.openapi.vfs.VirtualFile, line: Int, project: Project): Boolean {
        return file.extension == "as"
    }

    override fun getDisplayText(p0: XLineBreakpoint<AngelscriptBreakpointProperties>?): @Nls String =
        "Angelscript breakpoint"

}
