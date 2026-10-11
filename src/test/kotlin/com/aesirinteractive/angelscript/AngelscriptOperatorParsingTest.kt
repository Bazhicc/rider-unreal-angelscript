package com.aesirinteractive.angelscript

import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.ParsingTestCase

class AngelscriptOperatorParsingTest : ParsingTestCase("", "as", AngelscriptParserDefinition()) {
    override fun getTestDataPath() = ""

    private fun parsed(source: String) = parseFile("Operators", source).also { file ->
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        assertTrue(errors.joinToString("\n") { "${it.textOffset}: ${it.errorDescription}" }, errors.isEmpty())
    }

    fun testArithmeticShiftRight() {
        val file = parsed("int ArithmeticShiftRight() { int Value = -4; return Value >>> 1; }")
        val shift = PsiTreeUtil.findChildrenOfType(file, com.aesirinteractive.angelscript.psi.AngelscriptShiftExpr::class.java).single()
        assertEquals("Value >>> 1", shift.text)
    }

    fun testBinaryBooleanXorOrder() {
        parsed("""
            int BinaryBooleanXorOrder()
            {
                int Order = 0;
                bool Value = (FixtureSupport::RecordArgumentOrderValue(7, Order) > 0)
                    ^^ (FixtureSupport::RecordArgumentOrderValue(8, Order) > 0);
                return Order * 10 + (Value ? 1 : 0);
            }
        """.trimIndent())
        val file = parsed("bool Mixed(bool A, bool B, bool C) { return A && B ^^ C || A; }")
        val equality = PsiTreeUtil.findChildrenOfType(file, com.aesirinteractive.angelscript.psi.AngelscriptEqualityExpr::class.java).single()
        assertEquals("B ^^ C", equality.text)
    }

    fun testShiftsKeepNestedTemplateClosersAndOtherOperators() {
        parsed("""
            TMap<int, TArray<TMap<int, FString>>> Values;
            int Shift(int Value) { Value >>>= 1; Value >>= 1; return (Value >>> 1) + (Value >> 2) + (Value << 3); }
            int Bitwise(int A, int B) { return A ^ B; }
            bool Boolean(bool A, bool B) { return A ^^ B; }
        """.trimIndent())
    }
}
