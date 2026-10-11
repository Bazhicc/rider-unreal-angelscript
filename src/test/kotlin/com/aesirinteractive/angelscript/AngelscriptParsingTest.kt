package com.aesirinteractive.angelscript

import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.ParsingTestCase

// ParsingTestCase exercises the actual platform PSI using a mock project, without
// requiring Rider's unrelated .NET solution host to create a light IDE project.
class AngelscriptParsingTest : ParsingTestCase("", "as", AngelscriptParserDefinition()) {
    override fun getTestDataPath() = ""

    fun testNamedArgumentsWithDefaultParameters() {
        val file = parseFile("DefaultNamed", """
            int DefaultNamed()
            {
                return DefaultTarget(Third: 7, First: 19);
            }
        """.trimIndent())
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        assertTrue(errors.joinToString { "${it.errorDescription} at ${it.textOffset}" }, errors.isEmpty())
        val first = file.text.indexOf("First:")
        assertEquals("First", file.findElementAt(first)!!.text)
        assertEquals(first, file.findElementAt(first)!!.textOffset)
    }

    fun testNestedNamedArgumentsAndConditionalValues() {
        val file = parseFile("NestedNamed", """
            int NestedNamed(bool Flag)
            {
                return DefaultTarget(Third: Flag ? Inner(Value: 7, Other: 8) : 9, First: 19);
            }
        """.trimIndent())
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        assertTrue(errors.joinToString { "${it.errorDescription} at ${it.textOffset}" }, errors.isEmpty())
        val other = file.text.indexOf("Other:")
        assertEquals("Other", file.findElementAt(other)!!.text)
    }

    fun testPositionalArgumentsAndParenthesizedCommaExpression() {
        val file = parseFile("Positional", """
            int Positional(bool Flag)
            {
                return Target(7, Flag ? 8 : 9, (10, 11));
            }
        """.trimIndent())
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        assertTrue(errors.joinToString { "${it.errorDescription} at ${it.textOffset}" }, errors.isEmpty())
        assertTrue(file.text.contains("Target(7, Flag ? 8 : 9, (10, 11))"))
    }

    fun testUnrealClassAndCompoundAssignments() {
        val file = parseFile("Example", """
            UCLASS()
            class ATestActor : AActor
            {
                UPROPERTY()
                int Count = 0;
                UFUNCTION(BlueprintOverride)
                void Tick(float DeltaSeconds)
                {
                    Count += 1;
                    Count >>= 1;
                    if (Count > 0) { Count -= 1; }
                }
            }
        """.trimIndent())
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        assertTrue(errors.joinToString { "${it.errorDescription} at ${it.textOffset}" }, errors.isEmpty())
    }

    fun testBracesAreIndependentTokens() {
        val lexer = AngelscriptLexerAdapter()
        lexer.start("{ value += 1; value >>= 2; }")
        val tokens = mutableListOf<String>()
        while (lexer.tokenType != null) {
            tokens.add(lexer.tokenText)
            lexer.advance()
        }
        assertContainsElements(tokens, "{", "+=", ">>=", "}")
    }

    fun testIncompleteSourceRemainsEditableAndKeepsUnicodeOffsets() {
        val text = "// 中文 😀\nvoid Test() { DefaultTarget(Third: ;"
        val file = parseFile("Incomplete", text)
        assertEquals(text, file.text)
        val target = text.indexOf("DefaultTarget")
        assertEquals(target, file.findElementAt(target)!!.textOffset)
        assertEquals("DefaultTarget", file.findElementAt(target)!!.text)
        assertFalse("The parser must detect malformed syntax",
            PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java).isEmpty())
    }

    fun testStructureViewRetainsDeclarationKinds() {
        val file = parseFile("Structure", """
            namespace Outer
            {
                enum EMode { First, Second }
                struct FData { int Value; }
                interface IWorker { void Work(); }
                class AWorker { int Count; AWorker() {} void Work() {} }
                event void OnChanged(int Value);
                delegate void FCallback(int Value);
                mixin void Extra(int Value) {}
                typedef int Number;
                int Global;
                void Main() {}
            }
        """.trimIndent())
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        assertTrue(errors.joinToString { it.errorDescription }, errors.isEmpty())
        val outer = AngelscriptStructureViewElement(file).children.single() as AngelscriptStructureViewElement
        assertEquals("Outer", outer.presentation.presentableText)
        val children = outer.children.map { it as AngelscriptStructureViewElement }
        assertEquals(listOf("EMode", "FData", "IWorker", "AWorker", "OnChanged", "FCallback", "Extra", "Number", "Global", "Main"),
            children.map { it.presentation.presentableText })
        val worker = children.first { it.presentation.presentableText == "AWorker" }
        assertEquals(listOf("Count", "AWorker", "Work"), worker.children.map { (it as AngelscriptStructureViewElement).presentation.presentableText })
        assertEquals(listOf("First", "Second"), children.first().children.map { (it as AngelscriptStructureViewElement).presentation.presentableText })
    }

    fun testOfficialAssetSyntaxAndStructureRemainSupported() {
        val file = parseFile("Asset", "class AExampleActor { int Count; } asset ExampleAsset of AExampleActor { Count = 1; }")
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        assertTrue(errors.joinToString { "${it.errorDescription} at ${it.textOffset}" } + "\n" +
            com.intellij.psi.impl.DebugUtil.psiToString(file, true), errors.isEmpty())
        assertEquals(listOf("AExampleActor", "ExampleAsset"), AngelscriptStructureViewElement(file).children.map {
            (it as AngelscriptStructureViewElement).presentation.presentableText
        })
    }
}
