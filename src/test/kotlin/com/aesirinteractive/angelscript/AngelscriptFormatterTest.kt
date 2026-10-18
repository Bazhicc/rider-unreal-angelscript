package com.aesirinteractive.angelscript

import com.bazhicc.angelscript.formatter.FormatterEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class AngelscriptFormatterTest {
    @Test fun preservesChineseAndSupplementaryUnicodeBeforeEdits() {
        val original = "// 角色😀\nint Count=1;\n"
        val byteOffset = original.substringBefore("=").toByteArray(Charsets.UTF_8).size
        val replacements = FormatterEngine.replacements(original,
            """<replacements><replacement offset="$byteOffset" length="1"> = </replacement></replacements>""")
        val result = StringBuilder(original)
        for ((start, end, replacement) in replacements.asReversed()) {
            result.replace(start, end, replacement)
        }
        assertEquals("// 角色😀\nint Count = 1;\n", result.toString())
    }

    @Test fun convertsMultipleEditsIncludingWhitespaceAfterUnicode() {
        val original = "// 角色\nint A=1; int B=2;"
        val offsets = original.indices.filter { original[it] == '=' }
        val xml = offsets.joinToString("", "<replacements>", "</replacements>") {
            val byteOffset = original.substring(0, it).toByteArray(Charsets.UTF_8).size
            """<replacement offset="$byteOffset" length="1"> = </replacement>"""
        }
        val result = StringBuilder(original)
        for ((start, end, replacement) in FormatterEngine.replacements(original, xml).asReversed()) {
            result.replace(start, end, replacement)
        }
        assertEquals("// 角色\nint A = 1; int B = 2;", result.toString())
    }
}
