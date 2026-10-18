package com.aesirinteractive.angelscript

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color
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
        val syntax = AngelscriptColors.highlighter()
        val lexer = syntax.highlightingLexer
        val source = "UCLASS() int Count = 0xff + 1.5; if (Count) return; /* block */ // line"
        lexer.start(source)
        val expected = mapOf(
            "UCLASS" to "PREPROCESSOR_MACRO_IDENTIFIER", "int" to "BUILTIN_TYPE_KEYWORD",
            "0xff" to "INTEGER_LITERAL", "1.5" to "FLOAT_LITERAL", "+" to "OPERATOR_SIGN",
            "if" to "CONTROL_FLOW_KEYWORD", "return" to "CONTROL_TRANSFER_KEYWORD",
            "/* block */" to "BLOCK_COMMENT", "// line" to "LINE_COMMENT")
        val seen = mutableSetOf<String>()
        while (lexer.tokenType != null) {
            val text = if (lexer.tokenStart in source.indexOf("/*") until source.indexOf("*/") + 2)
                "/* block */" else lexer.tokenText
            expected[text]?.let { cpp ->
                assertEquals(lexer.tokenText, "ReSharper.CPP_$cpp",
                    syntax.getTokenHighlights(lexer.tokenType!!).single().externalName)
                seen.add(text)
            }
            lexer.advance()
        }
        assertEquals(expected.keys, seen)
    }

    @Test fun highlightingDoesNotLoadTheTextMatePlugin() {
        assertFalse("Run highlighting tests with TextMate disabled in the test sandbox",
            PluginManagerCore.loadedPlugins.any { it.pluginId.idString == "org.jetbrains.plugins.textmate" })
        assertSame(AngelscriptLanguage.INSTANCE, AngelscriptFileType.INSTANCE.language)
    }

    @Test fun nativeAndSemanticHighlightingUseTheCppThemeColors() {
        val manager = EditorColorsManager.getInstance()
        val requiredSchemes = listOf("Default", "Darcula", "Rider Islands Dark")
        val cppKeys = mapOf(
            AngelscriptSyntaxHighlighter.LOCAL_VARIABLE_KEY to "ReSharper.CPP_LOCAL_VARIABLE_IDENTIFIER",
            AngelscriptSyntaxHighlighter.GLOBAL_VARIABLE_KEY to "ReSharper.CPP_GLOBAL_VARIABLE_IDENTIFIER",
            AngelscriptSyntaxHighlighter.PARAMETER_KEY to "ReSharper.CPP_PARAMETER_VARIABLE_IDENTIFIER",
            AngelscriptSyntaxHighlighter.INSTANCE_FIELD_KEY to "ReSharper.CPP_CLASS_FIELD_IDENTIFIER",
            AngelscriptSyntaxHighlighter.FUNCTION_CALL_KEY to "ReSharper.CPP_GLOBAL_FUNCTION_IDENTIFIER",
            AngelscriptSyntaxHighlighter.MEMBER_FUNCTION_KEY to "ReSharper.CPP_MEMBER_FUNCTION_IDENTIFIER",
            AngelscriptSyntaxHighlighter.METADATA_KEY to "ReSharper.CPP_PREPROCESSOR_MACRO_IDENTIFIER",
            AngelscriptSyntaxHighlighter.METADATA_SPECIFIER_KEY to "ReSharper.CPP_UE4_REFLECTION_SPECIFIER_IDENTIFIER",
            AngelscriptSyntaxHighlighter.OPERATION_SIGN_KEY to "ReSharper.CPP_OPERATOR_SIGN",
            AngelscriptCppColors.STRUCT to "ReSharper.CPP_STRUCT_IDENTIFIER",
            AngelscriptCppColors.ENUM to "ReSharper.CPP_ENUM_IDENTIFIER")
        for (name in requiredSchemes) {
            val scheme = manager.getScheme(name) ?: loadRiderScheme("RiderIslandsDark")
            assertEquals(name, scheme.name)
            for ((key, cppName) in cppKeys) {
                assertEquals(cppName, key.externalName)
                val cpp = TextAttributesKey.find(cppName)
                if (name == "Rider Islands Dark") assertNotNull("Missing C++ theme color: $cppName", scheme.getAttributes(cpp))
                assertEquals("$name: differs from C++ $cppName", scheme.getAttributes(cpp), scheme.getAttributes(key))
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

    @Test fun cppCustomizationIsSharedAndOldAngelScriptPaletteOverridesAreIgnored() {
        val scheme = EditorColorsSchemeImpl(EditorColorsManager.getInstance().getScheme("Darcula")!!)
        val customized = TextAttributes().apply { foregroundColor = Color(27, 132, 154) }
        val obsolete = TextAttributes().apply { foregroundColor = Color.RED }
        scheme.setAttributes(TextAttributesKey.createTextAttributesKey("ReSharper.CPP_LOCAL_VARIABLE_IDENTIFIER"), customized)
        scheme.setAttributes(TextAttributesKey.createTextAttributesKey("ANGELSCRIPT_LOCAL_VARIABLE"), obsolete)
        scheme.setAttributes(TextAttributesKey.createTextAttributesKey("ANGELSCRIPT_AS_LOCAL_VARIABLE"), obsolete)
        assertEquals(customized, scheme.getAttributes(AngelscriptSyntaxHighlighter.LOCAL_VARIABLE_KEY))
        val semantic = AngelscriptLspCustomization().semanticTokensCustomizer.getTextAttributesKey("as_local_variable", emptyList())!!
        assertEquals(customized, scheme.getAttributes(semantic))
        assertFalse(AngelscriptSyntaxHighlighter.TOKEN_KEYS.values.any { keys ->
            keys.any { it.externalName.startsWith("ANGELSCRIPT_") }
        })
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
