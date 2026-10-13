package com.aesirinteractive.angelscript

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.platform.lsp.api.customization.LspGoToDefinitionDisabled
import com.intellij.testFramework.ParsingTestCase
import java.lang.reflect.Proxy

/** Exercises native PSI and both installed declaration handlers without a Rider backend solution. */
class AngelscriptNavigationDirectionTest : ParsingTestCase("", "as", AngelscriptParserDefinition()) {
    override fun getTestDataPath() = ""

    override fun setUp() {
        super.setUp()
        registerLspTestManager(project) { emptyList() }
    }

    private fun outcome(source: String, findUsages: Boolean) {
        val offset = source.indexOf("<caret>")
        val file = parseFile("Direction", source.replace("<caret>", ""))
        val document = DocumentImpl(file.text)
        val editor = Proxy.newProxyInstance(Editor::class.java.classLoader, arrayOf(Editor::class.java)) { _, method, _ ->
            when (method.name) {
                "getDocument" -> document
                "getProject" -> project
                else -> error("Unexpected editor access: ${method.name}")
            }
        } as Editor
        val leaf = file.findElementAt(offset)!!
        val declaration = angelscriptDeclarationAt(leaf, offset)
        val targets = AngelscriptLspNavigation().getGotoDeclarationTargets(leaf, offset, editor)
        if (findUsages) {
            assertNotNull("Recognize the declaration name, not an enclosing declaration's whole range", declaration)
            assertTrue(AngelscriptFindUsagesProvider().canFindUsagesFor(declaration!!))
            val factory = AngelscriptFindUsagesHandlerFactory()
            assertTrue(factory.canFindUsages(declaration))
            assertTrue(factory.createFindUsagesHandler(declaration, false) is AngelscriptFindUsagesHandler)
            assertNull("Leave the declaration to Rider's Show Usages action", targets)
            assertNull("The legacy C++ handler must also yield at declarations",
                AngelscriptCppNavigation().getGotoDeclarationTargets(leaf, offset, editor))
        } else {
            assertNull("A reference must not become Find Usages just because it is inside a function", declaration)
            assertNotNull("References must still navigate to definitions", targets)
            assertTrue(targets!!.all { it.textOffset != offset })
        }
    }

    fun testFunctionDefinitionFindsUsages() = outcome(
        "int Tar<caret>get() { return 1; } int Use() { return Target(); }", true)

    fun testFunctionCallGoesToDefinition() = outcome(
        "int Target() { return 1; } int Use() { return Tar<caret>get(); }", false)

    fun testOverloadDefinitionFindsUsages() = outcome(
        "int Call(int Value) { return Value; } float Ca<caret>ll(float Value) { return Value; } void Use() { Call(1); }",
        true)

    fun testMemberDefinitionFindsUsages() = outcome(
        "class Actor { int Co<caret>unt() { return 1; } int Use() { return Count(); } }", true)

    fun testRecursiveCallGoesToDefinition() = outcome(
        "int Recurse(int N) { return N > 0 ? Rec<caret>urse(N - 1) : 0; }", false)

    fun testReturnTypeGoesToTypeDefinition() = outcome(
        "class Actor {} Act<caret>or Spawn() { return Actor(); }", false)

    fun testDefaultArgumentCallGoesToDefinition() = outcome(
        "int Target() { return 1; } void Use(int Value = Tar<caret>get()) {}", false)

    fun testDefinitionDoesNotRequestAnyLspOrCppTargets() {
        val source = "class Actor { int Target() { return Target(); } }"
        val file = parseFile("DeclarationGuard", source)
        // A declaration must be left to usages even when an LSP manager is installed.
        registerLspTestManager(project) {
            error("A declaration must not request LSP definitions")
        }
        val editor = Proxy.newProxyInstance(Editor::class.java.classLoader, arrayOf(Editor::class.java)) { _, method, _ ->
            error("A declaration must not run the C++ fallback: ${method.name}")
        } as Editor
        val offset = source.indexOf("Target")
        for (position in offset..offset + "Target".lastIndex) {
            val leaf = file.findElementAt(position)!!
            assertNull(AngelscriptLspNavigation().getGotoDeclarationTargets(leaf, position, editor))
            assertNull(AngelscriptCppNavigation().getGotoDeclarationTargets(leaf, position, editor))
        }
        assertSame(LspGoToDefinitionDisabled, AngelscriptLspCustomization().goToDefinitionCustomizer)
        val call = file.findElementAt(source.lastIndexOf("Target"))!!
        assertNull("A call inside a function is not its declaration", angelscriptDeclarationAt(call, call.textOffset))
        assertFalse(AngelscriptFindUsagesProvider().canFindUsagesFor(call.parent))
    }
}
