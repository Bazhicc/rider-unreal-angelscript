package com.aesirinteractive.angelscript

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.testFramework.ParsingTestCase
import java.lang.reflect.Proxy

/** Exercises the installed parser, real PSI references and Rider's declaration entry point. */
class AngelscriptNavigationTest : ParsingTestCase("", "as", AngelscriptParserDefinition()) {
    override fun getTestDataPath() = ""

    fun testLspRenameUsesAtomicWorkspaceHandler() {
        val file = parseFile("Rename", "int RemoteValue() { return 42; }")
        assertFalse("The inline platform handler must not split the workspace undo transaction",
            AngelscriptLspCustomization().renameCustomizer.shouldRunRename(file))
    }

    private fun checkTarget(source: String, declarationText: String): PsiElement {
        val offset = source.indexOf("<caret>")
        assertTrue(offset >= 0)
        val text = source.replace("<caret>", "")
        val file = parseFile("Navigation", text)
        val reference = file.findReferenceAt(offset)
        assertNotNull("A native PSI reference must exist at $offset\n" +
            com.intellij.psi.impl.DebugUtil.psiToString(file, true), reference)
        val declaration = reference!!.resolve()
        assertNotNull("The reference must resolve without an LSP server", declaration)
        assertEquals(text.indexOf(declarationText), declaration!!.textOffset)
        // Native navigation must return before accessing an editor or LSP service.
        val editor = Proxy.newProxyInstance(Editor::class.java.classLoader, arrayOf(Editor::class.java)) { _, method, _ ->
            error("Unexpected editor access: ${method.name}")
        } as Editor
        val targets = AngelscriptLspNavigation().getGotoDeclarationTargets(file.findElementAt(offset), offset, editor)
        assertNotNull(targets)
        assertEquals(declaration, targets!!.single())
        return declaration
    }

    fun testLocalDeclarationWithoutLanguageServer() {
        checkTarget("int Test(int Parameter) { int Local = Parameter; return Loc<caret>al; }", "Local =")
    }

    fun testParameter() {
        checkTarget("int Test(int Parameter) { return Para<caret>meter; }", "Parameter)")
    }

    fun testInnerLocalShadowsParameterAndOuterLocal() {
        checkTarget("int Test(int Value) { { int Value = 2; return Va<caret>lue; } }", "Value =")
    }

    fun testMemberFieldInsideFunction() {
        checkTarget("class Actor { int Count; int Test() { return Co<caret>unt; } }", "Count;")
    }

    fun testConstructorParameter() {
        checkTarget("class Actor { Actor(int Parameter) { int Value = Para<caret>meter; } }", "Parameter)")
    }

    fun testNamedArgumentCallTarget() {
        checkTarget("int DefaultTarget(int First = 1, int Third = 3) { return First; } " +
            "int DefaultNamed() { return DefaultTar<caret>get(Third: 7, First: 19); }", "DefaultTarget(")
    }

    fun testTypeReference() {
        checkTarget("class Actor {} void Use(Act<caret>or Value) {}", "Actor {}")
    }

    fun testNestedNamespaceDeclaration() {
        checkTarget("namespace Outer { namespace Inner { int Target() { return 1; } } } " +
            "int Use() { return Outer::Inner::Tar<caret>get(); }", "Target()")
    }

    fun testQualifiedCallDisambiguatesNamespaces() {
        checkTarget("namespace Wrong { int Target() { return 0; } } " +
            "namespace Right { int Target(int Value) { return Value; } } " +
            "int Use() { return Right::Tar<caret>get(7); }", "Target(int")
    }

    fun testQualifiedReferenceDoesNotResolveToSameNamedLocal() {
        checkTarget("namespace Right { int Value = 1; } " +
            "int Use() { int Value = 2; return Right::Val<caret>ue; }", "Value = 1")
    }
}
