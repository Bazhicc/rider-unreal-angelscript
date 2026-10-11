package com.aesirinteractive.angelscript

import org.eclipse.lsp4j.InlineValue
import org.eclipse.lsp4j.InlineValueEvaluatableExpression
import org.eclipse.lsp4j.InlineValueVariableLookup
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.junit.Assert.*
import org.junit.Test

class AngelscriptEditorSupportTest {
    private fun expression(source: String): String? {
        val offset = source.indexOf('|')
        val text = source.replace("|", "")
        val range = AngelscriptDebugExpressions.range(text, offset) ?: return null
        return text.substring(range.startOffset, range.endOffset)
    }

    @Test fun debuggerMemberAndIndexExpressions() {
        assertEquals("Actor.Location", expression("Actor.Loc|ation.X"))
        assertEquals("this.Items[Index].Count", expression("this.Items[Index].Co|unt"))
        assertEquals("Items[Indices[Index]]", expression("Items|[Indices[Index]]"))
        assertEquals("Index", expression("Items[In|dex]"))
        assertEquals("Namespace::Value", expression("Namespace::Va|lue"))
        assertEquals("对象.值", expression("// 😀\n对象.|值"))
        assertEquals("Count", expression("DefaultTarget(Third: Cou|nt, First: 19)"))
    }

    @Test fun debuggerRejectsCallsSideEffectsStringsAndComments() {
        assertNull(expression("GetActor().Co|unt"))
        assertNull(expression("Items[GetIndex()].Co|unt"))
        assertNull(expression("Items[Index++].Co|unt"))
        assertNull(expression("Items[Index = 1].Co|unt"))
        assertNull(expression("Items[Index <<= 1].Co|unt"))
        assertNull(expression("Items[GetIndex<int>()].Co|unt"))
        assertNull(expression("\"Actor.Co|unt\""))
        assertNull(expression("// Actor.Co|unt"))
        assertNull(expression("/* Actor.Co|unt */"))
        assertNull(expression("Items|[Index"))
    }

    @Test fun lexerKeepsStringAndCommentBoundaries() {
        val tokens = angelscriptLexemes("f\"{Actor.Count}\" n\"Name\" /* Count */ \"\"\"line\nCount\"\"\" Actor.Count")
        assertEquals(3, tokens.count { it.type == AngelscriptTokens.STRING })
        assertEquals(listOf("Actor", ".", "Count"), tokens.takeLast(3).map { it.text })
    }

    @Test fun inlineValuesUseOfficialExpressionsAndUtf16Ranges() {
        fun range(line: Int, start: Int, end: Int) = Range(Position(line, start), Position(line, end))
        val text = "// 😀\n    Count = 1;\r\n    Other = 2;"
        val values = listOf(
            InlineValue(InlineValueVariableLookup(range(1, 4, 9), true)),
            InlineValue(InlineValueEvaluatableExpression(range(2, 4, 9), "this.Other")),
            InlineValue(InlineValueVariableLookup(range(2, 4, 9), true, "Count")),
            InlineValue(InlineValueVariableLookup(range(8, 0, 0), true, "Invalid")),
            InlineValue(InlineValueVariableLookup(range(0, 0, 999), true)))
        assertEquals(listOf(AngelscriptInlineExpression("Count", 1), AngelscriptInlineExpression("this.Other", 2)),
            inlineExpressions(values, text))
    }

    @Test fun cppFallbackClassifiesOnlySupportedSymbols() {
        assertEquals(CppSearchKind.FUNCTION, AngelscriptCppNavigation.searchKind("GetActor", "("))
        assertEquals(CppSearchKind.ENUM, AngelscriptCppNavigation.searchKind("EMode", "::"))
        assertEquals(CppSearchKind.CLASS, AngelscriptCppNavigation.searchKind("TArray", "<"))
        assertEquals(CppSearchKind.CLASS, AngelscriptCppNavigation.searchKind("FVector", null))
        assertEquals(CppSearchKind.NONE, AngelscriptCppNavigation.searchKind("Count", ";"))
    }

    @Test fun officialSaveNotificationIsRegisteredWithLsp4j() {
        val methods = org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints.getSupportedMethods(AngelscriptLanguageClient::class.java)
        assertTrue(methods.containsKey("angelscript/wantSave"))
    }
}
