package com.aesirinteractive.angelscript

import com.aesirinteractive.angelscript.psi.AngelscriptArgList
import com.aesirinteractive.angelscript.psi.AngelscriptFStringFormatSpec
import com.aesirinteractive.angelscript.psi.AngelscriptVariableAccessExpr
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.ParsingTestCase

class AngelscriptFormatStringTest : ParsingTestCase("", "as", AngelscriptParserDefinition()) {
    override fun getTestDataPath() = ""

    private fun parsed(source: String) = parseFile("FormatString", source).also { file ->
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        assertTrue(errors.joinToString("\n") { "${it.textOffset}: ${it.errorDescription}" }, errors.isEmpty())
    }

    fun testPlainFunctionCall() { parsed("FString Probe() { return Call(1); }") }

    fun testSimpleInterpolation() { parsed("FString Probe() { return f\"{Value}\"; }") }

    fun testFunctionCallInterpolation() { parsed("FString Probe() { return f\"{Call(1)}\"; }") }

    private fun tokens(source: String): List<Triple<String, String, Int>> {
        val lexer = AngelscriptLexerAdapter()
        lexer.start(source)
        return buildList {
            while (lexer.tokenType != null) {
                add(Triple(lexer.tokenType.toString(), lexer.tokenText, lexer.tokenStart))
                lexer.advance()
            }
        }
    }

    fun testSuppliedFormatStringReturnKeepsTernaryAndFormatSpecifier() {
        val source = """
            FString FormatStringReturn(bool Fail, int&out Order)
            {
                Order = 0;
                return f"R{RecordFormatOrder(1, Order)}:{(Fail ? ThrowFormatOrder(2, Order) : RecordFormatOrder(2, Order)) :04d}";
            }
        """.trimIndent()
        val file = parsed(source)
        assertEquals(3, PsiTreeUtil.findChildrenOfType(file, AngelscriptArgList::class.java).size)
        assertEquals(listOf(":04d"), PsiTreeUtil.findChildrenOfType(file, AngelscriptFStringFormatSpec::class.java).map { it.text })
        assertEquals(listOf("RecordFormatOrder", "ThrowFormatOrder", "RecordFormatOrder"),
            PsiTreeUtil.findChildrenOfType(file, AngelscriptVariableAccessExpr::class.java)
                .map { it.text }.filter { it.endsWith("FormatOrder") })
        val lexemes = tokens(source)
        assertEquals(listOf("04d"), lexemes.filter { it.first == AngelscriptTypes.FSTRING_FORMAT_TEXT.toString() }.map { it.second })
        for (match in Regex("(?:Record|Throw)FormatOrder").findAll(source)) {
            assertEquals(AngelscriptTypes.IDENTIFIER.toString(), lexemes.single { it.third == match.range.first }.first)
        }
    }

    fun testNamedArgumentsAndNamespacesInsideFormattedExpressions() {
        parsed("""
            FString Probe(bool Flag) {
                return f"{Target(First: 1, Other: Flag ? Support::First() : Support::Second()):04d}";
            }
        """.trimIndent())
    }

    fun testParenthesizedTernariesWithoutFormatAndMultipleInterpolations() {
        parsed("""
            FString Probe(bool A, bool B) {
                return f"{(A ? First() : (B ? Second() : Third()))} {Next():04d} {Last()}";
            }
        """.trimIndent())
    }

    fun testTopLevelTernariesKeepTheirColons() {
        for (quote in listOf('"', '\'')) {
            val source = "FString Probe(bool A, bool B) { return f${quote}{A ? First() : B ? Second() : Third():04d}${quote}; }"
            parsed(source)
            val lexemes = tokens(source)
            assertEquals(2, lexemes.count { it.first == AngelscriptTypes.COLON.toString() })
            assertEquals(1, lexemes.count { it.first == AngelscriptTypes.FSTRING_COLON.toString() })
        }
    }

    fun testBracketsStringsAndCommentsDoNotStartFormatSuffix() {
        parsed("""
            FString Probe(bool Flag) {
                return f"{Values[Flag ? First() : Second()]:04d} {Target(Text: ")?:", Other: 1 /* )?: */):04d}";
            }
        """.trimIndent())
    }

    fun testLexerCanRestartAfterIncompleteInterpolation() {
        val lexer = AngelscriptLexerAdapter()
        lexer.start("f\"{Call(")
        while (lexer.tokenType != null) lexer.advance()
        val source = "FString Probe() { return f\"{Value:04d}\"; }"
        lexer.start(source)
        val formats = mutableListOf<String>()
        while (lexer.tokenType != null) {
            if (lexer.tokenType == AngelscriptTypes.FSTRING_FORMAT_TEXT) formats.add(lexer.tokenText)
            lexer.advance()
        }
        assertEquals(listOf("04d"), formats)
        parsed(source)
    }

    fun testFollowingStatementIsStillParsed() {
        val file = parsed("""
            FString Probe(bool Flag) {
                FString Text = f"{Target(First: Flag ? One() : Two()):04d}";
                AfterFormat();
                return Text;
            }
        """.trimIndent())
        assertTrue(PsiTreeUtil.findChildrenOfType(file, AngelscriptVariableAccessExpr::class.java).any { it.text == "AfterFormat" })
    }
}
