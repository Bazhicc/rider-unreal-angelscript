package com.aesirinteractive.angelscript

import com.aesirinteractive.angelscript.AngelscriptSyntaxHighlighter.Companion.TOKEN_KEYS
import com.intellij.icons.AllIcons
import com.intellij.lexer.Lexer
import com.intellij.lexer.FlexAdapter
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.util.containers.stream
import javax.swing.Icon


class AngelscriptSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer {
        // These names remain identifiers in PSI for contextual parsing, but have
        // dedicated lexical colors in the editor, including while code is incomplete.
        return object : FlexAdapter(AngelscriptLexer()) {
            override fun getTokenType(): IElementType? {
                val type = super.getTokenType()
                if (type == null) return null
                val text = bufferSequence.subSequence(tokenStart, tokenEnd).toString()
                if (type == AngelscriptTypes.COMMENT && (text.startsWith("/*") || state == AngelscriptLexer.IN_BLOCK_COMMENT))
                    return BLOCK_COMMENT_TOKEN
                if (type == AngelscriptTypes.NUMBER_LITERAL && !text.startsWith("0x", true) &&
                    ('.' in text || 'e' in text.lowercase() || text.lastOrNull()?.lowercaseChar() in listOf('f', 'd')))
                    return FLOAT_TOKEN
                if (type != AngelscriptTypes.IDENTIFIER) return type
                return when (text) {
                    "this", "Super" -> AngelscriptTypes.SUPER
                    "Cast" -> AngelscriptTokens.KEYWORD
                    "float32", "float64" -> AngelscriptTypes.FLOAT_KW
                    "UMETA" -> AngelscriptTypes.UENUM_KW
                    else -> type
                }
            }
        }
    }

    public override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        if (tokenType == TokenType.BAD_CHARACTER) return BAD_CHAR_KEYS
        return TOKEN_KEYS[tokenType] ?: EMPTY_KEYS
    }

    companion object {
        private val BLOCK_COMMENT_TOKEN = AngelscriptTokenType("BLOCK_COMMENT_HIGHLIGHT")
        private val FLOAT_TOKEN = AngelscriptTokenType("FLOAT_HIGHLIGHT")
        private val BAD_CHARACTER: TextAttributesKey =
            createTextAttributesKey("ANGELSCRIPT_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        private val COMMENT: TextAttributesKey =
            AngelscriptCppColors.LINE_COMMENT

        private fun uniqueKey(typeName: String, fallback: TextAttributesKey): Array<TextAttributesKey> {
            return arrayOf(when (typeName) {
                "IF", "ELSE", "FOR", "WHILE", "SWITCH_KW", "CASE_KW", "DEFAULT_KW", "DO_KW" -> AngelscriptCppColors.CONTROL_FLOW
                "BREAK", "CONTINUE", "RETURN" -> AngelscriptCppColors.CONTROL_TRANSFER
                "VOID_KW", "INT_KW", "INT8_KW", "INT16_KW", "INT32_KW", "INT64_KW",
                "UINT_KW", "UINT8_KW", "UINT16_KW", "UINT32_KW", "UINT64_KW", "FLOAT_KW", "DOUBLE_KW", "BOOL_KW" -> AngelscriptCppColors.BUILTIN_TYPE
                "SUPER" -> AngelscriptCppColors.THIS
                else -> fallback
            })
        }

        private val PREPROCESSOR: TextAttributesKey =
            AngelscriptCppColors.DIRECTIVE

        val OPERATION_SIGN_KEY: TextAttributesKey =
            AngelscriptCppColors.OPERATOR
        val METADATA_KEY: TextAttributesKey =
            AngelscriptCppColors.MACRO
        val METADATA_SPECIFIER_KEY: TextAttributesKey = AngelscriptCppColors.REFLECTION_SPECIFIER
        val MEMBER_FUNCTION_KEY: TextAttributesKey = AngelscriptCppColors.METHOD

        private val BAD_CHAR_KEYS = arrayOf<TextAttributesKey>(BAD_CHARACTER)
        private val COMMENT_KEYS = arrayOf<TextAttributesKey>(COMMENT)
        private val PREPROCESSOR_KEYS = arrayOf<TextAttributesKey>(PREPROCESSOR)
        private val EMPTY_KEYS = emptyArray<TextAttributesKey>()

        // Lexer-level token highlighting (token types only — no PSI node types here)
        public val TOKEN_KEYS: Map<IElementType, Array<TextAttributesKey>> = mapOf(
            BLOCK_COMMENT_TOKEN to arrayOf(AngelscriptCppColors.BLOCK_COMMENT),
            FLOAT_TOKEN to arrayOf(AngelscriptCppColors.FLOAT),
            AngelscriptTokens.KEYWORD to arrayOf(AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.IDENTIFIER to uniqueKey("IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER),
            AngelscriptTypes.NUMBER_LITERAL to uniqueKey("NUMBER_LITERAL", AngelscriptCppColors.INTEGER),
            AngelscriptTypes.STRING_LITERAL to uniqueKey("STRING_LITERAL", AngelscriptCppColors.STRING),
            AngelscriptTypes.FSTRING_START to uniqueKey("FSTRING_START", AngelscriptCppColors.STRING),
            AngelscriptTypes.FSTRING_END to uniqueKey("FSTRING_END", AngelscriptCppColors.STRING),
            AngelscriptTypes.FSTRING_TEXT to uniqueKey("FSTRING_TEXT", AngelscriptCppColors.STRING),
            AngelscriptTypes.FSTRING_LBRACE to uniqueKey("FSTRING_LBRACE", AngelscriptCppColors.BRACES),
            AngelscriptTypes.FSTRING_RBRACE to uniqueKey("FSTRING_RBRACE", AngelscriptCppColors.BRACES),
            AngelscriptTypes.FSTRING_COLON to uniqueKey("FSTRING_COLON", AngelscriptCppColors.STRING),
            AngelscriptTypes.FSTRING_FORMAT_TEXT to uniqueKey("FSTRING_FORMAT_TEXT", AngelscriptCppColors.STRING),
            AngelscriptTypes.BOOL_LITERAL to uniqueKey("BOOL_LITERAL", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.NULLPTR_KW to uniqueKey("NULLPTR_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.COMMENT to COMMENT_KEYS,
            // Control flow
            AngelscriptTypes.BREAK to uniqueKey("BREAK", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.CONTINUE to uniqueKey("CONTINUE", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.ELSE to uniqueKey("ELSE", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.FOR to uniqueKey("FOR", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.IF to uniqueKey("IF", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.RETURN to uniqueKey("RETURN", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.WHILE to uniqueKey("WHILE", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.SWITCH_KW to uniqueKey("SWITCH_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.CASE_KW to uniqueKey("CASE_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.DEFAULT_KW to uniqueKey("DEFAULT_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.DO_KW to uniqueKey("DO_KW", AngelscriptCppColors.KEYWORD),
            // Declaration keywords
            AngelscriptTypes.CONST to uniqueKey("CONST", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.ENUM to uniqueKey("ENUM", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.PUBLIC to uniqueKey("PUBLIC", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.STATIC to uniqueKey("STATIC", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.STRUCT to uniqueKey("STRUCT", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.CLASS to uniqueKey("CLASS", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.SUPER to uniqueKey("SUPER", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.TYPEDEF_KW to uniqueKey("TYPEDEF_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.NAMESPACE_KW to uniqueKey("NAMESPACE_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.EVENT_KW to uniqueKey("EVENT_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.DELEGATE_KW to uniqueKey("DELEGATE_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.MIXIN_KW to uniqueKey("MIXIN_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.LOCAL_KW to uniqueKey("LOCAL_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.ASSET_KW to uniqueKey("ASSET_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.OF_KW to uniqueKey("OF_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.SHARED_KW to uniqueKey("SHARED_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.EXTERNAL_KW to uniqueKey("EXTERNAL_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.PRIVATE_KW to uniqueKey("PRIVATE_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.PROTECTED_KW to uniqueKey("PROTECTED_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.OVERRIDE_KW to uniqueKey("OVERRIDE_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.FINAL_KW to uniqueKey("FINAL_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.EXPLICIT_KW to uniqueKey("EXPLICIT_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.PROPERTY_KW to uniqueKey("PROPERTY_KW", AngelscriptCppColors.KEYWORD),
            // Type modifier keywords
            AngelscriptTypes.IN to uniqueKey("IN", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.OUT to uniqueKey("OUT", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.INOUT_KW to uniqueKey("INOUT_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.AUTO to uniqueKey("AUTO", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.AUTO_KW to uniqueKey("AUTO_KW", AngelscriptCppColors.KEYWORD),
            // Primitive type keywords
            AngelscriptTypes.VOID_KW to uniqueKey("VOID_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.INT_KW to uniqueKey("INT_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.INT8_KW to uniqueKey("INT8_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.INT16_KW to uniqueKey("INT16_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.INT32_KW to uniqueKey("INT32_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.INT64_KW to uniqueKey("INT64_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.UINT_KW to uniqueKey("UINT_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.UINT8_KW to uniqueKey("UINT8_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.UINT16_KW to uniqueKey("UINT16_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.UINT32_KW to uniqueKey("UINT32_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.UINT64_KW to uniqueKey("UINT64_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.FLOAT_KW to uniqueKey("FLOAT_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.DOUBLE_KW to uniqueKey("DOUBLE_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.BOOL_KW to uniqueKey("BOOL_KW", AngelscriptCppColors.KEYWORD),
            // Word operators
            AngelscriptTypes.NOT_KW to uniqueKey("NOT_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.AND_KW to uniqueKey("AND_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.OR_KW to uniqueKey("OR_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.XOR_KW to uniqueKey("XOR_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.IS_KW to uniqueKey("IS_KW", AngelscriptCppColors.KEYWORD),
            // Unreal metadata
            AngelscriptTypes.UFUNCTION_KW to uniqueKey("UFUNCTION_KW", METADATA_KEY),
            AngelscriptTypes.UPROPERTY_KW to uniqueKey("UPROPERTY_KW", METADATA_KEY),
            AngelscriptTypes.UCLASS_KW to uniqueKey("UCLASS_KW", METADATA_KEY),
            AngelscriptTypes.USTRUCT_KW to uniqueKey("USTRUCT_KW", METADATA_KEY),
            AngelscriptTypes.UENUM_KW to uniqueKey("UENUM_KW", METADATA_KEY),
            AngelscriptTypes.UINTERFACE_KW to uniqueKey("UINTERFACE_KW", METADATA_KEY),
            AngelscriptTypes.INTERFACE_KW to uniqueKey("INTERFACE_KW", AngelscriptCppColors.KEYWORD),
            AngelscriptTypes.PLUSPLUS to uniqueKey("PLUSPLUS", OPERATION_SIGN_KEY),
            AngelscriptTypes.MINUSMINUS to uniqueKey("MINUSMINUS", OPERATION_SIGN_KEY),
            AngelscriptTypes.QUEST to uniqueKey("QUEST", OPERATION_SIGN_KEY),
            AngelscriptTypes.TILDE to uniqueKey("TILDE", OPERATION_SIGN_KEY),
            AngelscriptTypes.AT to uniqueKey("AT", OPERATION_SIGN_KEY),
            AngelscriptTypes.SHA to PREPROCESSOR_KEYS,
            AngelscriptTypes.EQ to uniqueKey("EQ", OPERATION_SIGN_KEY),
            AngelscriptTypes.EQEQ to uniqueKey("EQEQ", OPERATION_SIGN_KEY),
            AngelscriptTypes.EXCLEQ to uniqueKey("EXCLEQ", OPERATION_SIGN_KEY),
            AngelscriptTypes.EXCL to uniqueKey("EXCL", OPERATION_SIGN_KEY),
            AngelscriptTypes.PLUS to uniqueKey("PLUS", OPERATION_SIGN_KEY),
            AngelscriptTypes.MINUS to uniqueKey("MINUS", OPERATION_SIGN_KEY),
            AngelscriptTypes.MUL to uniqueKey("MUL", OPERATION_SIGN_KEY),
            AngelscriptTypes.DIV to uniqueKey("DIV", OPERATION_SIGN_KEY),
            AngelscriptTypes.REM to uniqueKey("REM", OPERATION_SIGN_KEY),
            AngelscriptTypes.AND to uniqueKey("AND", OPERATION_SIGN_KEY),
            AngelscriptTypes.OR to uniqueKey("OR", OPERATION_SIGN_KEY),
            AngelscriptTypes.XOR to uniqueKey("XOR", OPERATION_SIGN_KEY),
            AngelscriptTypes.LT to uniqueKey("LT", OPERATION_SIGN_KEY),
            AngelscriptTypes.GT to uniqueKey("GT", OPERATION_SIGN_KEY),
            AngelscriptTypes.GTEQ to uniqueKey("GTEQ", OPERATION_SIGN_KEY),
            AngelscriptTypes.LTEQ to uniqueKey("LTEQ", OPERATION_SIGN_KEY),
            AngelscriptTypes.LTLT to uniqueKey("LTLT", OPERATION_SIGN_KEY),
            AngelscriptTypes.POWER to uniqueKey("POWER", OPERATION_SIGN_KEY),
            AngelscriptTypes.ANDAND to uniqueKey("ANDAND", OPERATION_SIGN_KEY),
            AngelscriptTypes.OROR to uniqueKey("OROR", OPERATION_SIGN_KEY),
            AngelscriptTypes.PLUSEQ to uniqueKey("PLUSEQ", OPERATION_SIGN_KEY),
            AngelscriptTypes.MINUSEQ to uniqueKey("MINUSEQ", OPERATION_SIGN_KEY),
            AngelscriptTypes.MULEQ to uniqueKey("MULEQ", OPERATION_SIGN_KEY),
            AngelscriptTypes.POWEREQ to uniqueKey("POWEREQ", OPERATION_SIGN_KEY),
            AngelscriptTypes.DIVEQ to uniqueKey("DIVEQ", OPERATION_SIGN_KEY),
            AngelscriptTypes.REMEQ to uniqueKey("REMEQ", OPERATION_SIGN_KEY),
            AngelscriptTypes.ANDEQ to uniqueKey("ANDEQ", OPERATION_SIGN_KEY),
            AngelscriptTypes.OREQ to uniqueKey("OREQ", OPERATION_SIGN_KEY),
            AngelscriptTypes.XOREQ to uniqueKey("XOREQ", OPERATION_SIGN_KEY),
            AngelscriptTypes.LSHIFTEQ to uniqueKey("LSHIFTEQ", OPERATION_SIGN_KEY),
            AngelscriptTypes.RSHIFTEQ to uniqueKey("RSHIFTEQ", OPERATION_SIGN_KEY),
            AngelscriptTypes.RRSHIFTEQ to uniqueKey("RRSHIFTEQ", OPERATION_SIGN_KEY),
            AngelscriptTypes.COLON to uniqueKey("COLON", OPERATION_SIGN_KEY),
            AngelscriptTypes.COLONCOLON to uniqueKey("COLONCOLON", OPERATION_SIGN_KEY),
            AngelscriptTypes.DOT to uniqueKey("DOT", AngelscriptCppColors.DOT),
            AngelscriptTypes.COMMA to uniqueKey("COMMA", AngelscriptCppColors.COMMA),
            AngelscriptTypes.SEMICOLON to uniqueKey("SEMICOLON", AngelscriptCppColors.SEMICOLON),
            AngelscriptTypes.LBRACE to uniqueKey("LBRACE", AngelscriptCppColors.BRACES),
            AngelscriptTypes.RBRACE to uniqueKey("RBRACE", AngelscriptCppColors.BRACES),
            AngelscriptTypes.LPAREN to uniqueKey("LPAREN", AngelscriptCppColors.PARENTHESES),
            AngelscriptTypes.RPAREN to uniqueKey("RPAREN", AngelscriptCppColors.PARENTHESES),
            AngelscriptTypes.LBRACK to uniqueKey("LBRACK", AngelscriptCppColors.BRACKETS),
            AngelscriptTypes.RBRACK to uniqueKey("RBRACK", AngelscriptCppColors.BRACKETS),
            // Preprocessor directives
            AngelscriptTypes.PP_IF to PREPROCESSOR_KEYS,
            AngelscriptTypes.PP_ELIF to PREPROCESSOR_KEYS,
            AngelscriptTypes.PP_ELSE to PREPROCESSOR_KEYS,
            AngelscriptTypes.PP_ENDIF to PREPROCESSOR_KEYS,
            AngelscriptTypes.PP_DEFINE to PREPROCESSOR_KEYS,
        )

        // Semantic color keys — used by AngelscriptAnnotator, not by the lexer highlighter
        val CLASS_NAME_KEY: TextAttributesKey =
            AngelscriptCppColors.CLASS
        val FUNCTION_DECLARATION_KEY: TextAttributesKey =
            AngelscriptCppColors.FUNCTION
        val FUNCTION_CALL_KEY: TextAttributesKey =
            AngelscriptCppColors.FUNCTION
        val PARAMETER_KEY: TextAttributesKey =
            AngelscriptCppColors.PARAMETER
        val INSTANCE_FIELD_KEY: TextAttributesKey =
            AngelscriptCppColors.FIELD
        val TYPE_REF_KEY: TextAttributesKey =
            AngelscriptCppColors.CLASS
        val UNREAL_TYPE_KEY: TextAttributesKey =
            AngelscriptCppColors.CLASS
        val LOCAL_VARIABLE_KEY: TextAttributesKey =
            AngelscriptCppColors.LOCAL
        val GLOBAL_VARIABLE_KEY: TextAttributesKey =
            AngelscriptCppColors.GLOBAL
        val NAMESPACE_KEY: TextAttributesKey =
            AngelscriptCppColors.NAMESPACE
        val ENUM_CONSTANT_KEY: TextAttributesKey =
            AngelscriptCppColors.ENUM_CONSTANT

        val SEMANTIC_KEYS: Map<String, TextAttributesKey> = mapOf(
            "Class/struct name"       to CLASS_NAME_KEY,
            "Function declaration"    to FUNCTION_DECLARATION_KEY,
            "Function call"           to FUNCTION_CALL_KEY,
            "Member function"         to MEMBER_FUNCTION_KEY,
            "Struct type"             to AngelscriptCppColors.STRUCT,
            "Enum type"               to AngelscriptCppColors.ENUM,
            "Type alias"              to AngelscriptCppColors.TYPEDEF,
            "Parameter"               to PARAMETER_KEY,
            "Instance field"          to INSTANCE_FIELD_KEY,
            "Type reference"          to TYPE_REF_KEY,
            "Unreal type (U/A/F/E/T)" to UNREAL_TYPE_KEY,
            "Local variable"          to LOCAL_VARIABLE_KEY,
            "Global variable"         to GLOBAL_VARIABLE_KEY,
            "Namespace"               to NAMESPACE_KEY,
            "Enum constant"           to ENUM_CONSTANT_KEY,
            "Unreal macro"            to METADATA_KEY,
            "Unreal reflection specifier" to METADATA_SPECIFIER_KEY,
            "Operator"                to OPERATION_SIGN_KEY,
        )
    }
}

