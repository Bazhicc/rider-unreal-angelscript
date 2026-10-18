package com.aesirinteractive.angelscript

import com.intellij.codeInsight.daemon.impl.AnnotationHolderImpl
import com.intellij.lang.annotation.AnnotationSession
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.ParsingTestCase

class AngelscriptHighlightingTest : ParsingTestCase("", "as", AngelscriptParserDefinition()) {
    override fun getTestDataPath() = ""

    private fun annotations(source: String): Map<TextRange, TextAttributesKey> {
        val file = parseFile("Highlighting", source)
        assertEmpty(PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java))
        val holder = AnnotationHolderImpl(AnnotationSession(file), false)
        val annotator = AngelscriptAnnotator()
        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                holder.runAnnotatorWithContext(element, annotator)
                super.visitElement(element)
            }
        })
        return holder.associate { TextRange(it.startOffset, it.endOffset) to it.textAttributes }
    }

    fun testVariableDeclarationsAndReferencesRespectScopesWithoutLsp() {
        val source = """
            int Global = 0, SecondGlobal = 1;
            namespace Gameplay {
                int Total = 3;
                class AExample {
                    int Field = 1, OtherField = 2;
                    void Tick(int Parameter) {
                        int Local = Parameter;
                        Global += SecondGlobal;
                        Field += OtherField;
                        Total += Local;
                        { int Field = 3; Local += Field; }
                        Local += Field;
                        Gameplay::Total += Local;
                        int FValue = 4;
                        Local += FValue;
                        Print(f"{Local} {Parameter} {Field}");
                    }
                }
            }
        """.trimIndent()
        val colors = annotations(source)
        fun expect(name: String, vararg keys: TextAttributesKey) {
            val offsets = Regex("\\b${Regex.escape(name)}\\b").findAll(source).map { it.range.first }.toList()
            assertEquals(name, keys.size, offsets.size)
            offsets.zip(keys.toList()).forEach { (offset, key) ->
                assertEquals("$name at $offset", key, colors[TextRange.from(offset, name.length)])
            }
        }
        val global = AngelscriptSyntaxHighlighter.GLOBAL_VARIABLE_KEY
        val field = AngelscriptSyntaxHighlighter.INSTANCE_FIELD_KEY
        val local = AngelscriptSyntaxHighlighter.LOCAL_VARIABLE_KEY
        val parameter = AngelscriptSyntaxHighlighter.PARAMETER_KEY
        expect("Global", global, global)
        expect("SecondGlobal", global, global)
        expect("Total", global, global, global)
        expect("Field", field, field, local, local, field, field)
        expect("OtherField", field, field)
        expect("Parameter", parameter, parameter, parameter)
        expect("Local", *Array(7) { local })
        expect("FValue", local, local) // Unreal-like names must not override a resolved variable.
    }

    fun testLoopsParametersConstructorsAndQualifiedGlobals() {
        val source = """
            int Item = 0;
            namespace Outer {
                namespace Values { int Total = 0; }
                class Example {
                    int Field;
                    Example(int Seed) { Field = Seed; }
                    void Run(TArray<int> Items) {
                        for (int Index = 0; Index < 3; Index++) { Field += Index; }
                        for (int Item : Items) { Field += Item; }
                        Item += Values::Total;
                        int GlobalCopy = ::Item;
                    }
                }
            }
        """.trimIndent()
        val colors = annotations(source)
        fun keyAt(fragment: String, name: String): TextAttributesKey? {
            val offset = source.indexOf(fragment) + fragment.indexOf(name)
            return colors[TextRange.from(offset, name.length)]
        }
        assertEquals(AngelscriptSyntaxHighlighter.PARAMETER_KEY, keyAt("Field = Seed", "Seed"))
        assertEquals(AngelscriptSyntaxHighlighter.LOCAL_VARIABLE_KEY, keyAt("Field += Index", "Index"))
        assertEquals(AngelscriptSyntaxHighlighter.LOCAL_VARIABLE_KEY, keyAt("Field += Item", "Item"))
        assertEquals(AngelscriptSyntaxHighlighter.GLOBAL_VARIABLE_KEY, keyAt("Item += Values", "Item"))
        assertEquals(AngelscriptSyntaxHighlighter.GLOBAL_VARIABLE_KEY, keyAt("Values::Total", "Total"))
        assertEquals(AngelscriptSyntaxHighlighter.GLOBAL_VARIABLE_KEY, keyAt("::Item", "Item"))
    }

    fun testMetadataEnumConstantsAndNamespacesHaveColors() {
        val source = """
            namespace Gameplay {
                UCLASS(Abstract, Meta = (DisplayName = "UE", CustomFlag = true))
                class AExample {
                    UPROPERTY(EditAnywhere, Category = "Example") int Count;
                    UFUNCTION(BlueprintOverride) void Tick() { Count++; }
                    int Alias { get const { return Count; } set { Count = value; } }
                }
                enum EMode { Idle, Running }
                EMode Mode = EMode::Idle;
            }
        """.trimIndent()
        val colors = annotations(source)
        for (name in listOf("Abstract", "Meta", "DisplayName", "CustomFlag", "EditAnywhere", "Category", "BlueprintOverride")) {
            val offset = source.indexOf(name)
            assertEquals(name, AngelscriptSyntaxHighlighter.METADATA_SPECIFIER_KEY, colors[TextRange.from(offset, name.length)])
        }
        for (match in Regex("\\bIdle\\b").findAll(source))
            assertEquals(AngelscriptSyntaxHighlighter.ENUM_CONSTANT_KEY, colors[TextRange.from(match.range.first, 4)])
        for (match in Regex("\\b(get|set)\\b").findAll(source))
            assertEquals(AngelscriptSyntaxHighlighter.TOKEN_KEYS.getValue(AngelscriptTypes.PROPERTY_KW).single(),
                colors[TextRange.from(match.range.first, 3)])
        assertEquals(AngelscriptSyntaxHighlighter.NAMESPACE_KEY,
            colors[TextRange.from(source.indexOf("Gameplay"), 8)])
    }

    fun testEveryEmittedKeywordAndOperatorHasALexicalColor() {
        val source = """
            UCLASS USTRUCT UENUM UINTERFACE UPROPERTY UFUNCTION UMETA
            class struct enum interface namespace void bool int int8 int16 int32 int64 uint uint8 uint16 uint32 uint64
            float double float32 float64 const auto in out inout local shared external static private protected public
            if else for while do switch case default break continue return true false nullptr this Super Cast
            delegate event mixin typedef property final override explicit of super not and or xor is
            = == != ! + - * / % & | ^ < > >= <= << >> >>> ** && || ^^ += -= *= **= /= %= &= |= ^= <<= >>= >>>=
            ++ -- ? : :: ~ @ # . , ; ( ) [ ] { }
        """.trimIndent()
        val highlighter = AngelscriptColors.highlighter()
        val lexer = highlighter.highlightingLexer
        lexer.start(source)
        while (lexer.tokenType != null) {
            if (lexer.tokenType != TokenType.WHITE_SPACE)
                assertTrue("No color for ${lexer.tokenText} (${lexer.tokenType})",
                    highlighter.getTokenHighlights(lexer.tokenType!!).isNotEmpty())
            lexer.advance()
        }
    }

    fun testOtherNamedSyntaxAndMultiVariableInitializersKeepTheirColors() {
        val source = """
            typedef int32 FIndex;
            delegate void FCallback(int Code);
            event void FChanged(int Value);
            asset ExampleAsset of UObject {}
            namespace Values { int Total; }
            namespace Values {
                void Use() {
                    FIndex First = (1, 2), Second = Target(Third: 3, First: 4);
                    Print(ExampleAsset);
                    Total += Second;
                }
            }
        """.trimIndent()
        val colors = annotations(source)
        fun expect(fragment: String, name: String, key: TextAttributesKey) {
            val offset = source.indexOf(fragment) + fragment.indexOf(name)
            assertEquals(name, key, colors[TextRange.from(offset, name.length)])
        }
        expect("typedef int32 FIndex", "FIndex", AngelscriptCppColors.TYPEDEF)
        expect("void FCallback", "FCallback", AngelscriptCppColors.TYPEDEF)
        expect("void FChanged", "FChanged", AngelscriptCppColors.TYPEDEF)
        expect("asset ExampleAsset", "ExampleAsset", AngelscriptSyntaxHighlighter.GLOBAL_VARIABLE_KEY)
        expect("Print(ExampleAsset)", "ExampleAsset", AngelscriptSyntaxHighlighter.GLOBAL_VARIABLE_KEY)
        expect("Third: 3", "Third", AngelscriptSyntaxHighlighter.PARAMETER_KEY)
        expect("First: 4", "First", AngelscriptSyntaxHighlighter.PARAMETER_KEY)
        expect("FIndex First", "First", AngelscriptSyntaxHighlighter.LOCAL_VARIABLE_KEY)
        expect("Second = Target", "Second", AngelscriptSyntaxHighlighter.LOCAL_VARIABLE_KEY)
        expect("Total += Second", "Total", AngelscriptSyntaxHighlighter.GLOBAL_VARIABLE_KEY)
    }

    fun testStringsAndCommentsDoNotAcquireIdentifierOrOperatorColors() {
        val source = "// UCLASS Count ++\n/* Global += 1 */\n\"UPROPERTY ++ Local\" n\"Count\" \"\"\"int Global;\"\"\""
        val highlighter = AngelscriptColors.highlighter()
        val lexer = highlighter.highlightingLexer
        lexer.start(source)
        while (lexer.tokenType != null) {
            if (lexer.tokenType != TokenType.WHITE_SPACE) assertTrue(lexer.tokenText,
                lexer.tokenType == AngelscriptTypes.COMMENT || lexer.tokenType == AngelscriptTypes.STRING_LITERAL ||
                    highlighter.getTokenHighlights(lexer.tokenType!!).singleOrNull() == AngelscriptCppColors.BLOCK_COMMENT)
            lexer.advance()
        }
    }
}
