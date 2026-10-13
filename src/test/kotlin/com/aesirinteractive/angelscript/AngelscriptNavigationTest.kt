package com.aesirinteractive.angelscript

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.testFramework.ParsingTestCase
import java.lang.reflect.Proxy
import java.awt.event.MouseEvent
import javax.swing.JList

/** Exercises the installed parser, real PSI references and Rider's declaration entry point. */
class AngelscriptNavigationTest : ParsingTestCase("", "as", AngelscriptParserDefinition()) {
    override fun getTestDataPath() = ""

    override fun setUp() {
        super.setUp()
        registerLspTestManager(project) { emptyList() }
    }

    fun testNativeChooserTargetsIncludeSignatureOwnerAndFileLine() {
        val source = "class Base {\n int Call(int Value);\n float Call(float Value);\n}"
        val file = parseFile("NativeOverloads", source)
        val targets = AngelscriptCppDeclarations.offsets(source, setOf("Base"), setOf("Call"), "function").map {
            AngelscriptCppSourceTarget(file, it, AngelscriptCppDeclarations.signature(source, it, "Base", "function"))
        }
        assertEquals(2, targets.map { it.name }.distinct().size)
        assertTrue(targets[0].name.contains("int Base::Call(int Value)"))
        assertTrue(targets[1].name.contains("float Base::Call(float Value)"))
        assertTrue(targets[0].name.contains("NativeOverloads.as:2"))
        assertEquals("int Base::Call(int Value)", targets[0].presentation!!.presentableText)
        assertEquals("NativeOverloads.as:3", targets[1].presentation!!.locationString)
        val chooser = declarationNavigationTargets(targets)!!.single() as AngelscriptNavigationChooserTarget
        assertEquals(targets, chooser.targets)
        assertSame(targets[0], declarationNavigationTargets(listOf(targets[0]))!!.single())
        assertNull(declarationNavigationTargets(emptyList()))
        val items = targets.map(::AngelscriptNavigationItem)
        val renderer = AngelscriptNavigationRenderer()
        for (target in items) {
            renderer.getListCellRendererComponent(JList<Any>(), target, 0, false, false)
            assertEquals(target.name, renderer.text)
            assertFalse(renderer.text.contains(file.virtualFile.path))
            assertEquals(target.sourceLocation, renderer.toolTipText)
            assertTrue(renderer.toolTipText.startsWith(file.virtualFile.path + ":"))
        }
        val list = JList(items.toTypedArray())
        list.cellRenderer = renderer
        list.setSize(800, 100)
        val row = list.getCellBounds(0, 0)
        val hover = MouseEvent(list, MouseEvent.MOUSE_MOVED, 0, 0, row.x + 5, row.y + row.height / 2, 0, false)
        assertEquals("The list must expose the renderer's path as a hover tooltip",
            targets[0].sourceLocation, list.getToolTipText(hover))
        renderer.getListCellRendererComponent(JList<Any>(), "Other item", 0, false, false)
        assertNull("Do not reuse the previous item's path tooltip", renderer.toolTipText)
    }

    fun testScriptOverloadChooserIncludesTheContainingTypeAndArguments() {
        val source = "class Actor {\n int Call(int Value) { return Value; }\n float Call(float Value) { return Value; }\n}"
        val file = parseFile("ScriptOverloads", source)
        val targets = Regex("Call\\(").findAll(source).map { file.findElementAt(it.range.first)!! }.toList()
        val presented = presentNavigationTargets(targets).map { (it as AngelscriptCppSourceTarget).name }
        assertTrue(presented[0].contains("int Actor::Call(int Value)"))
        assertTrue(presented[1].contains("float Actor::Call(float Value)"))
        assertEquals(2, presented.distinct().size)
        assertSame(targets.singleOrNull() ?: targets[0], presentNavigationTargets(listOf(targets[0])).single())
    }

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
        // With no running server, native navigation must return before accessing the editor.
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
