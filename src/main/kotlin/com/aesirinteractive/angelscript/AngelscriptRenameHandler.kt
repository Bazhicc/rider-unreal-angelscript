package com.aesirinteractive.angelscript

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.ThrowableComputable
import com.intellij.openapi.vfs.ReadonlyStatusHandler
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.platform.lsp.api.LspServerState
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.rename.RenameHandler
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PrepareRenameParams
import org.eclipse.lsp4j.RenameParams
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.WorkspaceEdit

/** Apply semantic rename before modifying the initiating editor, so all files share one undo. */
class AngelscriptRenameHandler : RenameHandler {
    override fun isAvailableOnDataContext(dataContext: DataContext): Boolean {
        val file = CommonDataKeys.PSI_FILE.getData(dataContext) ?: return false
        val editor = CommonDataKeys.EDITOR.getData(dataContext) ?: return false
        return identifierAt(file, editor.caretModel.offset) != null && serverFor(file) != null
    }

    override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext) {
        val editor = CommonDataKeys.EDITOR.getData(dataContext) ?: return
        val file = CommonDataKeys.PSI_FILE.getData(dataContext) ?: return
        invoke(project, editor, file, dataContext)
    }

    override fun invoke(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext) {
        val identifier = identifierAt(file, editor.caretModel.offset) ?: return
        val server = serverFor(file) ?: return
        val virtualFile = file.virtualFile ?: return
        val document = editor.document
        val stamp = document.modificationStamp
        val offset = identifier.textOffset
        val line = document.getLineNumber(offset)
        val position = Position(line, offset - document.getLineStartOffset(line))
        val oldName = identifier.text
        try {
            if (server.initializeResult?.capabilities?.renameProvider?.right?.prepareProvider == true) {
                val prepared = request(project) {
                    server.sendRequestSync(5000) {
                        it.textDocumentService.prepareRename(PrepareRenameParams(server.getDocumentIdentifier(virtualFile), position))
                    }
                }
                require(prepared != null && (!prepared.isThird || prepared.third.isDefaultBehavior)) {
                    "This symbol cannot be renamed."
                }
            }
            val newName = Messages.showInputDialog(project, "New name for $oldName:", "Rename AngelScript Symbol",
                null, oldName, object : InputValidator {
                    override fun checkInput(inputString: String) = AngelscriptRenameEdits.isIdentifier(inputString)
                    override fun canClose(inputString: String) = checkInput(inputString)
                }) ?: return
            if (newName == oldName) return
            require(document.modificationStamp == stamp) { "The file changed. Retry rename from its current contents." }
            val documentManager = FileDocumentManager.getInstance()
            val stamps = (documentManager.unsavedDocuments.toList() + document).associateWith { it.modificationStamp }
            val edit = request(project) {
                server.sendRequestSync(10000) {
                    it.textDocumentService.rename(RenameParams(server.getDocumentIdentifier(virtualFile), position, newName))
                }
            } ?: error("The language server did not return a rename result.")
            val targets = AngelscriptRenameEdits.changes(edit).map { change ->
                val targetFile = server.descriptor.findFileByUri(change.uri)
                    ?: error("Cannot open rename target: ${change.uri}")
                val targetDocument = documentManager.getDocument(targetFile)
                    ?: error("Cannot edit rename target: ${targetFile.name}")
                require(stamps[targetDocument]?.let { it == targetDocument.modificationStamp } != false &&
                    (change.version == null || change.version == server.getDocumentVersion(targetDocument))) {
                    "${targetFile.name} changed. Retry rename from its current contents."
                }
                RenameTarget(targetFile, targetDocument, targetDocument.modificationStamp,
                    AngelscriptRenameEdits.plan(targetDocument, change.edits))
            }.filter { it.edits.isNotEmpty() }
            require(targets.isNotEmpty()) { "No references were found to rename." }
            if (ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(targets.map { it.file }).hasReadonlyFiles()) return
            WriteCommandAction.writeCommandAction(project).withName("Rename $oldName to $newName").withGlobalUndo().run<RuntimeException> {
                // Check every file before the first write; never leave a partially applied rename.
                require(targets.all { it.document.modificationStamp == it.stamp && it.document.isWritable }) {
                    "A rename target changed or became read-only. Retry rename."
                }
                for (target in targets) for (textEdit in target.edits) {
                    target.document.replaceString(textEdit.start, textEdit.end, textEdit.text)
                }
                PsiDocumentManager.getInstance(project).commitAllDocuments()
            }
        } catch (canceled: ProcessCanceledException) {
            throw canceled
        } catch (error: Exception) {
            logger<AngelscriptRenameHandler>().warn("AngelScript rename failed", error)
            Messages.showErrorDialog(project, error.message ?: "Rename failed.", "Rename AngelScript Symbol")
        }
    }

    private fun <T> request(project: Project, action: () -> T): T =
        ProgressManager.getInstance().runProcessWithProgressSynchronously(
            ThrowableComputable<T, Exception> { action() }, "Rename AngelScript Symbol", true, project)

    private fun identifierAt(file: PsiFile, offset: Int): PsiElement? {
        if (file.language != AngelscriptLanguage.INSTANCE || offset !in 0..file.textLength) return null
        val atCaret = file.findElementAt(offset)
        return (atCaret?.takeIf { it.node.elementType == AngelscriptTypes.IDENTIFIER }
            ?: if (offset > 0) file.findElementAt(offset - 1)?.takeIf { it.node.elementType == AngelscriptTypes.IDENTIFIER } else null)
    }

    private fun serverFor(file: PsiFile): LspServer? {
        val virtualFile = file.virtualFile ?: return null
        return LspServerManager.getInstance(file.project)
            .getServersForProvider(AngelscriptLspServerSupportProvider::class.java).firstOrNull {
                val rename = it.initializeResult?.capabilities?.renameProvider
                it.state == LspServerState.Running && it.descriptor.isSupportedFile(virtualFile) &&
                    rename != null && (rename.isRight || rename.left == true)
            }
    }

    private data class RenameTarget(val file: VirtualFile, val document: Document, val stamp: Long, val edits: List<RenameTextEdit>)
}

internal data class RenameTextEdit(val start: Int, val end: Int, val text: String)
internal data class RenameFileEdits(val uri: String, val version: Int?, val edits: List<TextEdit>)

internal object AngelscriptRenameEdits {
    fun isIdentifier(name: String): Boolean = AngelscriptLexerAdapter().let { lexer ->
        lexer.start(name)
        lexer.tokenType == AngelscriptTypes.IDENTIFIER && lexer.tokenEnd == name.length
    }

    fun changes(edit: WorkspaceEdit): List<RenameFileEdits> {
        require(edit.changes.isNullOrEmpty() || edit.documentChanges.isNullOrEmpty()) { "Ambiguous rename result." }
        val changes = if (!edit.documentChanges.isNullOrEmpty()) edit.documentChanges.map {
            require(it.isLeft) { "Rename requires an unsupported file operation." }
            RenameFileEdits(it.left.textDocument.uri, it.left.textDocument.version, it.left.edits)
        } else edit.changes.orEmpty().map { RenameFileEdits(it.key, null, it.value) }
        require(changes.map { it.uri }.distinct().size == changes.size) { "Duplicate rename target." }
        return changes
    }

    /** LSP positions and IntelliJ document offsets both count UTF-16 code units. */
    fun plan(document: Document, edits: List<TextEdit>): List<RenameTextEdit> {
        fun offset(position: Position): Int {
            require(position.line in 0 until document.lineCount && position.character >= 0) { "Invalid rename range." }
            val start = document.getLineStartOffset(position.line)
            require(position.character <= document.getLineEndOffset(position.line) - start) { "Invalid rename range." }
            return start + position.character
        }
        val result = edits.map {
            val start = offset(it.range.start)
            val end = offset(it.range.end)
            require(start <= end) { "Invalid rename range." }
            RenameTextEdit(start, end, it.newText)
        }.sortedByDescending { it.start }
        require(result.zipWithNext().all { (later, earlier) -> earlier.end <= later.start && earlier.start != later.start }) {
            "Overlapping rename edits."
        }
        return result
    }
}
