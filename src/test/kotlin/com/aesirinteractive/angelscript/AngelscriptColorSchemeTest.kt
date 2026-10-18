package com.aesirinteractive.angelscript

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.impl.EditorColorsSchemeImpl
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.openapi.editor.highlighter.EditorHighlighter
import com.intellij.openapi.editor.highlighter.HighlighterClient
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMUtil
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.testFramework.ApplicationRule
import com.intellij.testFramework.EdtTestUtil
import com.intellij.testFramework.LightVirtualFile
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.nio.file.Path
import java.util.zip.ZipFile

class AngelscriptColorSchemeTest {
    @Rule @JvmField val application = ApplicationRule()

    @Test fun editorAndPreviewUseNativeLexer() {
        val scheme = EditorColorsManager.getInstance().globalScheme
        val highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(
            LightVirtualFile("Test.as", AngelscriptFileType.INSTANCE, ""), scheme, null)
        highlighter.setText("UCLASS() class AExample { void Tick() { Count++; } }")
        val iterator = highlighter.createIterator(0)
        assertSame(AngelscriptTypes.UCLASS_KW, iterator.tokenType)
        val preview = AngelscriptColorSettingsPage().createEditorHighlighter(scheme)
        preview.setText("UPROPERTY(EditAnywhere) int Count;")
        assertSame(AngelscriptTypes.UPROPERTY_KW, preview.createIterator(0).tokenType)
    }

    @Test fun highlightingDoesNotLoadTheTextMatePlugin() {
        assertFalse("Run highlighting tests with TextMate disabled in the test sandbox",
            PluginManagerCore.loadedPlugins.any { it.pluginId.idString == "org.jetbrains.plugins.textmate" })
        assertSame(AngelscriptLanguage.INSTANCE, AngelscriptFileType.INSTANCE.language)
    }

    @Test fun bundledLightAndDarkPalettesResolveToVisibleSharedColors() {
        val manager = EditorColorsManager.getInstance()
        val requiredSchemes = listOf("Default", "Darcula", "Rider Islands Dark")
        val keys = AngelscriptSyntaxHighlighter.SEMANTIC_KEYS.values
        for (name in requiredSchemes) {
            val scheme = manager.getScheme(name) ?: loadRiderScheme("RiderIslandsDark")
            assertEquals(name, scheme.name)
            for (key in keys) {
                val foreground = scheme.getAttributes(key)?.foregroundColor
                assertNotNull("$name: ${key.externalName} has no foreground", foreground)
                assertFalse("$name: ${key.externalName} is indistinguishable from plain text", foreground == scheme.defaultForeground)
            }
            val customizer = AngelscriptLspCustomization().semanticTokensCustomizer
            for ((type, native) in mapOf("local_variable" to AngelscriptSyntaxHighlighter.LOCAL_VARIABLE_KEY,
                "global_variable" to AngelscriptSyntaxHighlighter.GLOBAL_VARIABLE_KEY,
                "member_variable" to AngelscriptSyntaxHighlighter.INSTANCE_FIELD_KEY,
                "parameter" to AngelscriptSyntaxHighlighter.PARAMETER_KEY)) {
                val semantic = customizer.getTextAttributesKey("as_$type", emptyList())!!
                assertEquals("$name: online/offline mismatch for $type", scheme.getAttributes(native), scheme.getAttributes(semantic))
            }
        }
        assertNull(AngelscriptLspCustomization().semanticTokensCustomizer.getTextAttributesKey("as_unknown_error", emptyList()))
    }

    // Application-only tests load the base schemes but not Rider's UI theme pack.
    // Read the installed SDK's real scheme so its parent/fallback behavior is still tested.
    private fun loadRiderScheme(resource: String): EditorColorsScheme {
        val archive = Path.of(PathManager.getHomePath(), "plugins", "rider-theme-pack", "lib", "rider-theme-pack.jar")
        return ZipFile(archive.toFile()).use { zip ->
            val xml = zip.getInputStream(zip.getEntry("colorSchemes/$resource.xml")).use { JDOMUtil.load(it) }
            val parent = EditorColorsManager.getInstance().getScheme(xml.getAttributeValue("parent_scheme"))!!
            EditorColorsSchemeImpl(parent).apply { readExternal(xml) }
        }
    }

    @Test fun incrementalInterpolationEditsMatchFreshHighlighting() {
        val scheme = EditorColorsManager.getInstance().globalScheme
        val factory = EditorHighlighterFactory.getInstance()
        val document = DocumentImpl("void Tick() { Print(f\"{Target(Third: Count, First: 2):04d} {Count}\"); Count++; }")
        val highlighter = factory.createEditorHighlighter(AngelscriptColors.highlighter(), scheme)
        highlighter.setEditor(object : HighlighterClient {
            override fun getProject(): Project? = null
            override fun repaint(startOffset: Int, endOffset: Int) {}
            override fun getDocument() = document
        })
        highlighter.setText(document.immutableCharSequence)
        document.addDocumentListener(highlighter)
        fun tokens(editor: EditorHighlighter): List<Any> {
            val iterator = editor.createIterator(0)
            val tokens = ArrayList<Any>()
            while (!iterator.atEnd()) {
                tokens.add(listOf(iterator.start, iterator.end, iterator.tokenType, iterator.textAttributes))
                iterator.advance()
            }
            return tokens
        }
        for ((before, after) in listOf("Third: Count" to "Third: (Count > 0 ? Count : 0)",
            ":04d}" to ":08d}", "{Count}" to "{Count + 1}", "Count++;" to "Count >>= 1;")) {
            val offset = document.text.indexOf(before)
            assertTrue(before, offset >= 0)
            EdtTestUtil.runInEdtAndWait<RuntimeException> {
                ApplicationManager.getApplication().runWriteAction {
                    CommandProcessor.getInstance().runUndoTransparentAction {
                        document.replaceString(offset, offset + before.length, after)
                    }
                }
            }
            val fresh = factory.createEditorHighlighter(AngelscriptColors.highlighter(), scheme)
            fresh.setText(document.immutableCharSequence)
            assertEquals("Incremental edit $before -> $after", tokens(fresh), tokens(highlighter))
        }
    }
}
