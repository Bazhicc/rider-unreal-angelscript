package com.aesirinteractive.angelscript

import com.intellij.openapi.editor.impl.DocumentImpl
import org.eclipse.lsp4j.*
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.junit.Assert.*
import org.junit.Test

class AngelscriptRenameEditsTest {
    private fun edit(line: Int, start: Int, end: Int, text: String) =
        TextEdit(Range(Position(line, start), Position(line, end)), text)

    @Test fun plansUtf16EditsInReverseOrderBeforeAnyWrite() {
        val source = "// 😀 Count\nint Count;\nCount += Count;"
        val edits = AngelscriptRenameEdits.plan(DocumentImpl(source), listOf(
            edit(2, 0, 5, "RenamedCount"), edit(1, 4, 9, "RenamedCount"), edit(2, 9, 14, "RenamedCount")))
        val result = StringBuilder(source)
        for (item in edits) result.replace(item.start, item.end, item.text)
        assertEquals("// 😀 Count\nint RenamedCount;\nRenamedCount += RenamedCount;", result.toString())
        assertEquals(listOf(RenameTextEdit(6, 11, "Other")),
            AngelscriptRenameEdits.plan(DocumentImpl("// 😀 Count"), listOf(edit(0, 6, 11, "Other"))))
    }

    @Test fun rejectsInvalidAndOverlappingRangesBeforeWriting() {
        val document = DocumentImpl("Count")
        for (edits in listOf(listOf(edit(1, 0, 1, "X")), listOf(edit(0, 0, 6, "X")),
            listOf(edit(0, 4, 2, "X")), listOf(edit(0, 0, 4, "X"), edit(0, 3, 5, "Y")))) {
            assertThrows(IllegalArgumentException::class.java) { AngelscriptRenameEdits.plan(document, edits) }
        }
        assertEquals("Count", document.immutableCharSequence.toString())
    }

    @Test fun acceptsBothWorkspaceEditFormsAndPreservesVersions() {
        val textEdits = listOf(edit(0, 4, 9, "Other"))
        val first = WorkspaceEdit(mapOf("file:///First.as" to textEdits, "file:///Second.as" to textEdits))
        assertEquals(2, AngelscriptRenameEdits.changes(first).size)
        val second = WorkspaceEdit(listOf(Either.forLeft<TextDocumentEdit, ResourceOperation>(
            TextDocumentEdit(VersionedTextDocumentIdentifier("file:///First.as", 7), textEdits))))
        assertEquals(listOf(RenameFileEdits("file:///First.as", 7, textEdits)), AngelscriptRenameEdits.changes(second))
    }

    @Test fun rejectsUnsupportedResourceOperationsAndInvalidNames() {
        val operation = WorkspaceEdit(listOf(Either.forRight<TextDocumentEdit, ResourceOperation>(DeleteFile("file:///First.as"))))
        assertThrows(IllegalArgumentException::class.java) { AngelscriptRenameEdits.changes(operation) }
        for (name in listOf("", "int", "class", "a b", "Count()", "7Count")) assertFalse(name, AngelscriptRenameEdits.isIdentifier(name))
        for (name in listOf("Count", "_Count2")) assertTrue(name, AngelscriptRenameEdits.isIdentifier(name))
    }
}
