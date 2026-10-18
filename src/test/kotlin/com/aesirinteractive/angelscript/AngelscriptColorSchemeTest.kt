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
import com.intellij.ide.ui.AntialiasingType
import com.intellij.ide.ui.UISettings
import com.intellij.testFramework.ApplicationRule
import com.intellij.testFramework.EdtTestUtil
import com.intellij.testFramework.LightVirtualFile
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.nio.file.Path
import java.util.zip.ZipFile
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import java.awt.Container
import java.awt.image.BufferedImage
import java.nio.file.Files
import javax.imageio.ImageIO

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
                "parameter" to AngelscriptSyntaxHighlighter.PARAMETER_KEY,
                "typename_enum" to AngelscriptCppColors.ENUM,
                "enum_constant" to AngelscriptCppColors.ENUM_CONSTANT,
                "struct_member_variable" to AngelscriptCppColors.STRUCT_FIELD,
                "struct_member_accessor" to AngelscriptCppColors.STRUCT_FIELD,
                "overloaded_operator" to AngelscriptCppColors.OVERLOADED_OPERATOR)) {
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
        for ((index, key) in AngelscriptColors.semantic.values.distinct().withIndex()) {
            if (!key.externalName.startsWith("ReSharper.CPP_")) continue
            val override = TextAttributes().apply { foregroundColor = Color(20 + index, 120, 170) }
            scheme.setAttributes(TextAttributesKey.find(key.externalName), override)
            assertEquals("C++ customization did not reach ${key.externalName}", override, scheme.getAttributes(key))
        }
        assertFalse(AngelscriptSyntaxHighlighter.TOKEN_KEYS.values.any { keys ->
            keys.any { it.externalName.startsWith("ANGELSCRIPT_") }
        })
    }

    // Application-only tests load the base schemes but not Rider's UI theme pack.
    // Read the installed SDK's real scheme so its parent/fallback behavior is still tested.
    internal fun loadRiderScheme(resource: String): EditorColorsScheme {
        val archive = Path.of(PathManager.getHomePath(), "plugins", "rider-theme-pack", "lib", "rider-theme-pack.jar")
        return ZipFile(archive.toFile()).use { zip ->
            val xml = zip.getInputStream(zip.getEntry("colorSchemes/$resource.xml")).use { JDOMUtil.load(it) }
            val parent = EditorColorsManager.getInstance().getScheme(xml.getAttributeValue("parent_scheme"))!!
            EditorColorsSchemeImpl(parent).apply { readExternal(xml) }
        }
    }

    @Test fun resolvedForegroundColorsMatchTheActualRiderCppTheme() {
        val scheme = loadRiderScheme("RiderIslandsDark")
        val expected = mapOf(
            AngelscriptCppColors.CLASS to 0xC191FF, AngelscriptCppColors.TYPEDEF to 0xC191FF,
            AngelscriptCppColors.STRUCT to 0xE1BFFF, AngelscriptCppColors.ENUM to 0xE1BFFF,
            AngelscriptCppColors.FUNCTION to 0x39CC9B, AngelscriptCppColors.METHOD to 0x39CC9B,
            AngelscriptCppColors.FIELD to 0x66C3CC, AngelscriptCppColors.STRUCT_FIELD to 0xE1BFFF,
            AngelscriptCppColors.NAMESPACE to 0xC191FF, AngelscriptCppColors.THIS to 0x6C95EB,
            AngelscriptCppColors.MACRO to 0x6C95EB, AngelscriptCppColors.REFLECTION_SPECIFIER to 0xC191FF,
            AngelscriptCppColors.ENUM_CONSTANT to 0xE1BFFF, AngelscriptCppColors.OVERLOADED_OPERATOR to 0x39CC9B,
            AngelscriptCppColors.LOCAL to 0xBDBDBD, AngelscriptCppColors.GLOBAL to 0xBDBDBD,
            AngelscriptCppColors.PARAMETER to 0xBDBDBD, AngelscriptCppColors.OPERATOR to 0xBDBDBD,
            AngelscriptCppColors.INTEGER to 0xED94C0, AngelscriptCppColors.STRING to 0xC9A26D,
            AngelscriptCppColors.RAW_STRING to 0xC9A26D, AngelscriptCppColors.VALID_ESCAPE to 0xD688D4,
            AngelscriptCppColors.INVALID_ESCAPE to 0x85C46C, AngelscriptCppColors.DOC_COMMENT to 0x85C46C,
            AngelscriptCppColors.DOC_COMMAND to 0x487D34, AngelscriptCppColors.LINE_COMMENT to 0x85C46C)
        for ((key, rgb) in expected) {
            val foreground = scheme.getAttributes(key)?.foregroundColor
            assertNotNull("Unresolved foreground: ${key.externalName}", foreground)
            assertEquals(key.externalName, rgb, foreground!!.rgb and 0xFFFFFF)
        }
        for (name in listOf("Default", "Darcula")) {
            val base = EditorColorsManager.getInstance().getScheme(name)!!
            for (key in expected.keys) {
                assertNotNull("$name: missing fallback for ${key.externalName}", key.fallbackAttributeKey)
                val visible = base.getAttributes(key)?.foregroundColor ?: base.defaultForeground
                assertNotEquals("$name: invisible ${key.externalName}", base.defaultBackground, visible)
            }
        }
    }

    @Test fun incrementalInterpolationEditsMatchFreshHighlighting() {
        val scheme = EditorColorsManager.getInstance().globalScheme
        val factory = EditorHighlighterFactory.getInstance()
        val document = DocumentImpl("void Tick() { Print(f\"{Target(Third: Count, First: 2):04d} {Count}\"); Count++; }\n/** @param Test docs */\n\"\"\"raw\\n\"\"\"")
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
            ":04d}" to ":08d}", "{Count}" to "{Count + 1}", "Count++;" to "Count >>= 1;",
            "/**" to "/*", "/*" to "/**", "raw\\n" to "raw\\t")) {
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
    @Test fun finalEditorRenderingContainsTheResolvedSemanticAndLexicalColors() {
        val source = """
            /** @brief C++ colors shared by native and semantic highlighting. */
            namespace Gameplay {
                int GlobalCount = 1;
                UCLASS(Abstract)
                class AExample {
                    int Count = 0;
                    void Tick(int Delta) {
                        int Local = Delta;
                        Count += Local;
                        Print("Hello\n");
                    }
                }
                struct FSample { int Field; }
                enum EMode { Idle, Running }
            }
        """.trimIndent()

        val scheme = loadRiderScheme("RiderIslandsDark")
        scheme.editorFontSize = 20
        EdtTestUtil.runInEdtAndWait<RuntimeException> {
            val project = com.intellij.openapi.project.ProjectManager.getInstance().defaultProject
            val file = com.intellij.psi.PsiFileFactory.getInstance(project).createFileFromText("Rendered.as", AngelscriptFileType.INSTANCE, source)
            val holder = com.intellij.codeInsight.daemon.impl.AnnotationHolderImpl(com.intellij.lang.annotation.AnnotationSession(file), false)
            val annotator = AngelscriptAnnotator()
            file.accept(object : com.intellij.psi.PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: com.intellij.psi.PsiElement) {
                    holder.runAnnotatorWithContext(element, annotator)
                    super.visitElement(element)
                }
            })
            val annotations = holder.associate { com.intellij.openapi.util.TextRange(it.startOffset, it.endOffset) to it.textAttributes }
            val factory = EditorFactory.getInstance()
            val uiSettings = UISettings.getInstance()
            val previousAntialiasing = uiSettings.editorAAType
            // Test the foreground itself, independently of platform font coverage.
            // A thin escape glyph may never reach the full color with antialiasing.
            uiSettings.editorAAType = AntialiasingType.OFF
            val editor = factory.createEditor(factory.createDocument(source)) as EditorEx
            try {
                editor.colorsScheme = scheme
                editor.highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(AngelscriptColors.highlighter(), scheme)
                editor.highlighter.setText(source)
                for ((range, key) in annotations) {
                    editor.markupModel.addRangeHighlighter(range.startOffset, range.endOffset,
                        HighlighterLayer.ADDITIONAL_SYNTAX, scheme.getAttributes(key), HighlighterTargetArea.EXACT_RANGE)
                    // Assert the actual foreground after lexical + semantic composition.
                    if (key in setOf(AngelscriptCppColors.CLASS, AngelscriptCppColors.METHOD, AngelscriptCppColors.FIELD,
                            AngelscriptCppColors.STRUCT, AngelscriptCppColors.ENUM, AngelscriptCppColors.NAMESPACE)) {
                        assertNotNull("Missing visible foreground at $range", scheme.getAttributes(key)?.foregroundColor)
                    }
                }
                editor.component.setSize(1250, 650)
                fun layout(container: Container) {
                    container.doLayout()
                    container.components.filterIsInstance<Container>().forEach { layout(it) }
                }
                layout(editor.component)
                val image = BufferedImage(1250, 650, BufferedImage.TYPE_INT_RGB)
                val graphics = image.createGraphics()
                try { editor.component.paint(graphics) } finally { graphics.dispose() }
                val output = Path.of("build/color-audit/final-editor.png")
                Files.createDirectories(output.parent)
                ImageIO.write(image, "png", output.toFile())
                val pixels = image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
                    .map { it and 0xFFFFFF }.toSet()
                for (rgb in listOf(0xC191FF, 0x39CC9B, 0x66C3CC, 0xE1BFFF, 0xBDBDBD, 0xED94C0,
                        0xC9A26D, 0x85C46C, 0xD688D4, 0x487D34))
                    assertTrue("Rendered editor is missing #%06X; see $output".format(rgb), rgb in pixels)
            } finally {
                factory.releaseEditor(editor)
                uiSettings.editorAAType = previousAntialiasing
            }
        }
    }
}
