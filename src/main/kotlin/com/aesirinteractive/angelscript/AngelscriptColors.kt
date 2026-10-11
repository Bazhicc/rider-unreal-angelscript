package com.aesirinteractive.angelscript

import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as Colors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.EditorHighlightingProvidingColorSettingsPage
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType
import org.jetbrains.plugins.textmate.TextMateService
import org.jetbrains.plugins.textmate.language.syntax.highlighting.TextMateHighlighter
import org.jetbrains.plugins.textmate.language.syntax.highlighting.TextMateEditorHighlighterProvider
import org.jetbrains.plugins.textmate.language.syntax.lexer.TextMateHighlightingLexer
import com.intellij.testFramework.LightVirtualFile

internal object AngelscriptColors {
    private fun key(name: String, fallback: TextAttributesKey) =
        TextAttributesKey.createTextAttributesKey("ANGELSCRIPT_$name", fallback)

    val lexical = mapOf(
        AngelscriptTokens.IDENTIFIER to key("IDENTIFIER", Colors.IDENTIFIER),
        AngelscriptTokens.KEYWORD to key("KEYWORD", Colors.KEYWORD),
        AngelscriptTokens.NUMBER to key("NUMBER_LITERAL", Colors.NUMBER),
        AngelscriptTokens.STRING to key("STRING_LITERAL", Colors.STRING),
        AngelscriptTokens.COMMENT to key("COMMENT", Colors.LINE_COMMENT),
        AngelscriptTokens.SYMBOL to key("OPERATION_SIGN", Colors.OPERATION_SIGN))

    // Names/order match the official semantic_highlighting.ts legend. Custom
    // as_* tokens otherwise have no useful default mapping in Rider.
    val semantic = mapOf(
        "namespace" to Colors.IDENTIFIER,
        "template_base_type" to Colors.CLASS_REFERENCE,
        "parameter" to Colors.PARAMETER,
        "local_variable" to Colors.LOCAL_VARIABLE,
        "member_variable" to Colors.INSTANCE_FIELD,
        "member_accessor" to Colors.INSTANCE_FIELD,
        "global_variable" to Colors.GLOBAL_VARIABLE,
        "global_accessor" to Colors.GLOBAL_VARIABLE,
        "member_function" to Colors.INSTANCE_METHOD,
        "global_function" to Colors.FUNCTION_CALL,
        "unknown_error" to Colors.IDENTIFIER,
        "typename" to Colors.CLASS_NAME,
        "typename_actor" to Colors.CLASS_NAME,
        "typename_component" to Colors.CLASS_NAME,
        "typename_struct" to Colors.CLASS_NAME,
        "typename_event" to Colors.CLASS_NAME,
        "typename_delegate" to Colors.CLASS_NAME,
        "typename_primitive" to Colors.KEYWORD,
        "access_specifier" to Colors.KEYWORD,
    ).mapKeys { "as_${it.key}" }.mapValues { (name, fallback) -> key(name.uppercase(), fallback) }

    fun highlighter(): com.intellij.openapi.fileTypes.SyntaxHighlighter {
        val descriptor = TextMateService.getInstance()?.getLanguageDescriptorByFileName("example.as")
        return if (descriptor != null) TextMateHighlighter(TextMateHighlightingLexer(descriptor, 20000))
        else AngelscriptSyntaxHighlighter()
    }
}

internal class AngelscriptSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?) = AngelscriptColors.highlighter()
}

internal class AngelscriptColorSettingsPage : EditorHighlightingProvidingColorSettingsPage {
    override fun getDisplayName() = "AngelScript"
    override fun getIcon() = AllIcons.FileTypes.AS
    override fun getHighlighter() = AngelscriptColors.highlighter()
    override fun createEditorHighlighter(scheme: EditorColorsScheme) =
        TextMateEditorHighlighterProvider().getEditorHighlighter(
            null, AngelscriptFileType.INSTANCE, LightVirtualFile("preview.as"), scheme)
    override fun getDemoText() = """
        // Official TextMate grammar plus LSP semantic colors
        UCLASS()
        class <as_typename_actor>ATestActor</as_typename_actor> : AActor
        {
            int <as_member_variable>Count</as_member_variable> = 0;
            void <as_member_function>Tick</as_member_function>(float <as_parameter>DeltaTime</as_parameter>)
            {
                <as_member_variable>Count</as_member_variable> += 1;
                DefaultTarget(Third: 7, First: 19);
            }
        }
    """.trimIndent()
    override fun getAdditionalHighlightingTagToDescriptorMap() = AngelscriptColors.semantic
    override fun getAttributeDescriptors(): Array<AttributesDescriptor> =
        (AngelscriptSyntaxHighlighter.TOKEN_KEYS.values.flatMap { it.toList() }.map { it.externalName to it } +
            AngelscriptSyntaxHighlighter.SEMANTIC_KEYS.toList() + AngelscriptColors.semantic.toList())
            .distinctBy { it.second.externalName }
            .map { (name, key) -> AttributesDescriptor(name.removePrefix("ANGELSCRIPT_").removePrefix("as_"), key) }
            .toTypedArray()
    override fun getColorDescriptors(): Array<ColorDescriptor> = emptyArray()
}
