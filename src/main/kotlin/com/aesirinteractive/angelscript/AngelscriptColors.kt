package com.aesirinteractive.angelscript

import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as Colors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.EditorHighlightingProvidingColorSettingsPage
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.editor.ex.util.LexerEditorHighlighter

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
        "namespace" to AngelscriptSyntaxHighlighter.NAMESPACE_KEY,
        "template_base_type" to AngelscriptSyntaxHighlighter.TYPE_REF_KEY,
        "parameter" to AngelscriptSyntaxHighlighter.PARAMETER_KEY,
        "local_variable" to AngelscriptSyntaxHighlighter.LOCAL_VARIABLE_KEY,
        "member_variable" to AngelscriptSyntaxHighlighter.INSTANCE_FIELD_KEY,
        "member_accessor" to AngelscriptSyntaxHighlighter.INSTANCE_FIELD_KEY,
        "global_variable" to AngelscriptSyntaxHighlighter.GLOBAL_VARIABLE_KEY,
        "global_accessor" to AngelscriptSyntaxHighlighter.GLOBAL_VARIABLE_KEY,
        "member_function" to AngelscriptSyntaxHighlighter.FUNCTION_CALL_KEY,
        "global_function" to AngelscriptSyntaxHighlighter.FUNCTION_CALL_KEY,
        "unknown_error" to Colors.IDENTIFIER,
        "typename" to AngelscriptSyntaxHighlighter.CLASS_NAME_KEY,
        "typename_actor" to AngelscriptSyntaxHighlighter.UNREAL_TYPE_KEY,
        "typename_component" to AngelscriptSyntaxHighlighter.UNREAL_TYPE_KEY,
        "typename_struct" to AngelscriptSyntaxHighlighter.UNREAL_TYPE_KEY,
        "typename_event" to AngelscriptSyntaxHighlighter.CLASS_NAME_KEY,
        "typename_delegate" to AngelscriptSyntaxHighlighter.CLASS_NAME_KEY,
        "typename_primitive" to Colors.KEYWORD,
        "access_specifier" to Colors.KEYWORD,
    ).mapKeys { "as_${it.key}" }.mapValues { (name, fallback) -> key(name.uppercase(), fallback) }

    // The editor and preview share the native PSI lexer and AngelScript color keys.
    fun highlighter() = AngelscriptSyntaxHighlighter()
}

internal class AngelscriptSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?) = AngelscriptColors.highlighter()
}

internal class AngelscriptColorSettingsPage : EditorHighlightingProvidingColorSettingsPage {
    override fun getDisplayName() = "AngelScript"
    override fun getIcon() = AllIcons.FileTypes.AS
    override fun getHighlighter() = AngelscriptColors.highlighter()
    override fun createEditorHighlighter(scheme: EditorColorsScheme) = LexerEditorHighlighter(highlighter, scheme)
    override fun getDemoText() = """
        // Native syntax colors and LSP semantic colors share the same palette
        namespace <namespace>Gameplay</namespace> {
        int <global>GlobalCount</global> = 0;
        UCLASS(<metadata>Abstract</metadata>, <metadata>Meta</metadata> = (<metadata>DisplayName</metadata> = "Example"))
        class <as_typename_actor>ATestActor</as_typename_actor> : <type>AActor</type>
        {
            int <as_member_variable>Count</as_member_variable> = 0;
            UPROPERTY(<metadata>EditAnywhere</metadata>, <metadata>Category</metadata> = "Example")
            float <as_member_variable>Speed</as_member_variable> = 1.0f;
            UFUNCTION(<metadata>BlueprintOverride</metadata>)
            void <as_member_function>Tick</as_member_function>(float <as_parameter>DeltaTime</as_parameter>)
            {
                int <local>LocalCount</local> = <as_member_variable>Count</as_member_variable> + 1;
                <global>GlobalCount</global> += <local>LocalCount</local>;
                <as_member_variable>Count</as_member_variable>++;
                <as_global_function>Print</as_global_function>(f"Count: {<local>LocalCount</local>:04d}");
            }
        }
        enum <type>EMode</type> { <enum>Idle</enum>, <enum>Running</enum> }
        }
    """.trimIndent()
    override fun getAdditionalHighlightingTagToDescriptorMap() = AngelscriptColors.semantic + mapOf(
        "local" to AngelscriptSyntaxHighlighter.LOCAL_VARIABLE_KEY,
        "global" to AngelscriptSyntaxHighlighter.GLOBAL_VARIABLE_KEY,
        "namespace" to AngelscriptSyntaxHighlighter.NAMESPACE_KEY,
        "type" to AngelscriptSyntaxHighlighter.UNREAL_TYPE_KEY,
        "metadata" to AngelscriptSyntaxHighlighter.METADATA_KEY,
        "enum" to AngelscriptSyntaxHighlighter.ENUM_CONSTANT_KEY)
    override fun getAttributeDescriptors(): Array<AttributesDescriptor> =
        (AngelscriptSyntaxHighlighter.TOKEN_KEYS.values.flatMap { it.toList() }.map { it.externalName to it } +
            AngelscriptSyntaxHighlighter.SEMANTIC_KEYS.toList() + AngelscriptColors.semantic.toList())
            .distinctBy { it.second.externalName }
            .map { (name, key) -> AttributesDescriptor(name.removePrefix("ANGELSCRIPT_").removePrefix("as_"), key) }
            .toTypedArray()
    override fun getColorDescriptors(): Array<ColorDescriptor> = emptyArray()
}
