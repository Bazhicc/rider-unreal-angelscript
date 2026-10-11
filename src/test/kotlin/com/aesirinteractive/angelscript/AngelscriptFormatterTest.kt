package com.aesirinteractive.angelscript

import org.junit.Assert.assertEquals
import org.junit.Test

class AngelscriptFormatterTest {
    @Test fun preservesChineseAndSupplementaryUnicodeBeforeEdits() {
        val original = "// 角色😀\nint Count=1;\n"
        val byteOffset = original.substringBefore("=").toByteArray(Charsets.UTF_8).size
        val replacements = toDocumentReplacements(original, listOf(Triple(byteOffset, 1, " = ")))
        val result = StringBuilder(original)
        for ((offset, length, replacement) in replacements.asReversed()) {
            result.replace(offset, offset + length, replacement)
        }
        assertEquals("// 角色😀\nint Count = 1;\n", result.toString())
    }

    @Test fun convertsMultipleEditsIncludingWhitespaceAfterUnicode() {
        val original = "// 角色\nint A=1; int B=2;"
        val offsets = original.indices.filter { original[it] == '=' }
        val edits = offsets.map { Triple(original.substring(0, it).toByteArray(Charsets.UTF_8).size, 1, " = ") }
        val result = StringBuilder(original)
        for ((offset, length, replacement) in toDocumentReplacements(original, edits).asReversed()) {
            result.replace(offset, offset + length, replacement)
        }
        assertEquals("// 角色\nint A = 1; int B = 2;", result.toString())
    }
}
