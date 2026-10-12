package com.aesirinteractive.angelscript

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerBase
import com.intellij.openapi.fileEditor.impl.LoadTextUtil
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.ParsingTestCase

/** Exercise VFS reads against the platform's byte-to-document conversion without a Rider backend. */
class AngelscriptCppSourcePositionTest : ParsingTestCase("", "as", AngelscriptParserDefinition()) {
    override fun getTestDataPath() = ""
    private fun sourceFile(name: String, source: String): VirtualFile =
        LightVirtualFile(name, PlainTextFileType.INSTANCE, source).apply { charset = Charsets.UTF_8 }

    private fun index(files: List<VirtualFile>) = AngelscriptNativeIndex<VirtualFile>(
        { names -> files.filter { it.name in names } },
        { word -> files.filter { Regex("\\b${Regex.escape(word)}\\b").containsMatchIn(nativeSourceText(it)) } },
        ::nativeSourceText, include = { _, path -> files.filter { it.name == path } }, fileName = { it.name })

    private fun query(name: String, kind: String = "function") =
        AngelscriptCppSymbol("FProbe", name, name, kind, false)

    private fun checkPosition(target: AngelscriptNativeSourceResolver.Target<VirtualFile>, expected: Int, name: String) {
        val document = FileDocumentManager.getInstance().getCachedDocument(target.file)
            ?: DocumentImpl(LoadTextUtil.getTextByBinaryPresentation(target.file.contentsToByteArray(), Charsets.UTF_8))
        assertEquals("Native offsets must address the normalized editor document", expected, target.offset)
        assertEquals(name, document.charsSequence.subSequence(target.offset, target.offset + name.length).toString())
        val file = parseFile("NativePosition", document.text)
        val navigation = AngelscriptCppSourceTarget(file, target.offset, target.label)
        assertEquals(expected, navigation.textOffset)
        assertEquals("The overload chooser must preserve the same exact offset", expected, AngelscriptNavigationItem(navigation).offset)
        assertTrue(navigation.sourceLocation.endsWith(":" + (document.getLineNumber(expected) + 1)))
    }

    fun testUnopenedCrLfHeaderUsesEditorOffsetsForTypesFieldsAndOverloads() {
        val source = (1..180).joinToString("\r\n") { "// 注释 😀 $it" } +
            "\r\nstruct FProbe {\r\n\tint Score;\r\n\tvoid Call(int Value);\r\n\tvoid Call(float Value);\r\n};\r\n"
        val normalized = StringUtil.convertLineSeparators(source)
        val file = sourceFile("Probe.h", source)
        val documents = FileDocumentManager.getInstance()
        assertNull(documents.getCachedDocument(file))
        val nativeIndex = index(listOf(file))
        val type = nativeIndex.resolve(query("FProbe", "type")).single()
        val field = nativeIndex.resolve(query("Score", "property")).single()
        val calls = nativeIndex.resolve(query("Call"))
        assertNull("Resolving unopened native sources must not load editor documents", documents.getCachedDocument(file))
        assertEquals(normalized.indexOf("FProbe"), type.offset)
        assertEquals(normalized.indexOf("Score"), field.offset)
        assertEquals(2, calls.size)
        checkPosition(type, normalized.indexOf("FProbe"), "FProbe")
        checkPosition(field, normalized.indexOf("Score"), "Score")
        for (call in calls) {
            val token = if (call.label.contains("int Value")) "Call(int" else "Call(float"
            checkPosition(call, normalized.indexOf(token), "Call")
        }
        val document = DocumentImpl(LoadTextUtil.getTextByBinaryPresentation(file.contentsToByteArray(), Charsets.UTF_8))
        FileDocumentManagerBase.registerDocument(document, file)
        assertSame(document, documents.getCachedDocument(file))
        assertEquals("Opening the file must not change the coordinate system",
            normalized, nativeSourceText(file))
    }

    fun testMixedLineSeparatorsAndMacroOriginsUseTheSameEditorCoordinates() {
        val source = "// 中文 😀\r\n#define DECLARE(Name) void Name();\rstruct FProbe {\n\tDECLARE(Call)\r\n};\r"
        val normalized = StringUtil.convertLineSeparators(source)
        val file = sourceFile("Probe.h", source)
        assertEquals(normalized, nativeSourceText(file))
        val target = index(listOf(file)).resolve(query("Call")).single()
        val expected = normalized.indexOf("Call)")
        assertEquals(expected, target.offset)
        checkPosition(target, expected, "Call")
    }

    fun testUnsavedNativeEditsTakePrecedenceOverDiskOffsets() {
        val file = sourceFile("Probe.h", "// disk\r\nstruct FProbe { void Call(); };\r\n")
        val document = DocumentImpl("// 未保存 😀\n\n" + LoadTextUtil.getTextByBinaryPresentation(file.contentsToByteArray(), Charsets.UTF_8))
        FileDocumentManagerBase.registerDocument(document, file)
        val expected = document.text.indexOf("Call()")
        val target = index(listOf(file)).resolve(query("Call")).single()
        assertEquals(expected, target.offset)
        assertEquals(document.text, nativeSourceText(file))
        checkPosition(target, expected, "Call")
    }

    fun testDefineInjectedIncludeRetainsItsOwnDocumentOffsets() {
        val header = sourceFile("Probe.h", "// header\r\n#define OWNER FProbe\r\n#include \"Probe.inc\"\r\n")
        val fragment = sourceFile("Probe.inc", "// fragment 😀\r\n\r\nstruct OWNER {\r\n\tint Score;\r\n\tvoid Call();\r\n};\r\n")
        val normalized = StringUtil.convertLineSeparators(String(fragment.contentsToByteArray(), Charsets.UTF_8))
        val nativeIndex = index(listOf(header, fragment))
        for ((name, kind) in listOf("Score" to "property", "Call" to "function")) {
            val target = nativeIndex.resolve(query(name, kind)).single()
            assertSame(fragment, target.file)
            checkPosition(target, normalized.indexOf(name), name)
        }
    }
}
