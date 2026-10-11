package com.aesirinteractive.angelscript

import com.intellij.ide.ui.NotRoamableUiSettings
import com.intellij.ide.ui.UISettings
import com.intellij.ide.ui.UISettingsStateDefaultsProvider
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.psi.PsiElement
import com.intellij.testFramework.ParsingTestCase
import com.intellij.ui.ExperimentalUI
import com.intellij.ui.popup.PopupFactoryImpl
import javax.swing.JLabel
import javax.swing.JList

/** Exercise the real popup factory without requiring a Rider backend solution. */
class AngelscriptNavigationPopupTest : ParsingTestCase("", "as", AngelscriptParserDefinition()) {
    override fun getTestDataPath() = ""

    override fun setUp() {
        super.setUp()
        application.registerService(UISettingsStateDefaultsProvider::class.java, object : UISettingsStateDefaultsProvider {
            override val searchEverywherePreviewDefault = false
        })
        application.registerService(UISettings::class.java, UISettings(NotRoamableUiSettings()))
        application.registerService(ExperimentalUI::class.java, object : ExperimentalUI() {
            override fun earlyInitValue() = false
            override fun saveCurrentValueAndReapplyDefaultLaf() = Unit
        })
        application.registerService(JBPopupFactory::class.java, PopupFactoryImpl())
    }

    fun testNativeChooserPopupDoesNotUsePsiModel() {
        val source = "class Base { int Call(int Value); float Call(float Value); }"
        val file = parseFile("NativePopup", source)
        val targets = AngelscriptCppDeclarations.offsets(source, setOf("Base"), setOf("Call"), "function").map {
            AngelscriptCppSourceTarget(file, it, AngelscriptCppDeclarations.signature(source, it, "Base", "function"))
        }
        checkPopup(targets)
    }

    fun testScriptOverloadPopupDoesNotUsePsiModel() {
        val source = "class Actor { int Call(int Value) { return Value; } float Call(float Value) { return Value; } }"
        val file = parseFile("ScriptPopup", source)
        val targets = Regex("Call\\(").findAll(source).map { file.findElementAt(it.range.first)!! }.toList()
        checkPopup(targets)
    }

    private fun checkPopup(targets: List<PsiElement>) {
        val chooser = declarationNavigationTargets(targets)!!.single() as AngelscriptNavigationChooserTarget
        val builder = chooser.createPopupBuilder(chooser.targets, false) as PopupChooserBuilder<*>
        @Suppress("UNCHECKED_CAST")
        val list = builder.chooserComponent as JList<Any>
        assertEquals(2, list.model.size)
        for (index in 0 until list.model.size) {
            // PopupFactoryImpl skips this production assertion in unit test mode.
            assertFalse("Rider forbids PSI elements in a popup model", list.model.getElementAt(index) is PsiElement)
            val item = list.model.getElementAt(index) as AngelscriptNavigationItem
            val target = chooser.targets[index]
            assertEquals(target.name, item.name)
            assertEquals(target.sourceLocation, item.sourceLocation)
            assertSame(target.containingFile.virtualFile, item.file)
            assertEquals(target.textOffset, item.offset)
            val label = list.cellRenderer.getListCellRendererComponent(list, item, index, false, false) as JLabel
            assertEquals(item.name, label.text)
            assertEquals(item.sourceLocation, label.toolTipText)
        }
    }
}
