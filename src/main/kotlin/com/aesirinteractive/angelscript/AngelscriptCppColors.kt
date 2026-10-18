package com.aesirinteractive.angelscript

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.jetbrains.rider.cpp.fileType.CppTextAttributeKeys as Cpp
import com.jetbrains.rider.colors.RiderLanguageTextAttributeKeys as Rider

/** Use Rider's actual C++ color identities, including user overrides, rather than
 * copying RGB values or inheriting stale ANGELSCRIPT_* palette overrides.
 */
internal object AngelscriptCppColors {
    // Semantic C++ keys arrive lazily from the backend. find() alone can create
    // a key without inheritance, so a theme's baseAttributes marker resolves to
    // empty attributes. Complete only missing links using Rider's own registered
    // fallback identities (CppHighlightingAttributeRegistration), never RGB values
    // or replacements for an already registered fallback.
    private fun cpp(name: String, fallback: TextAttributesKey): TextAttributesKey {
        val externalName = "ReSharper.CPP_$name"
        val existing = TextAttributesKey.find(externalName)
        return if (existing.fallbackAttributeKey != null) existing
            else TextAttributesKey.createTextAttributesKey(externalName, fallback)
    }

    val KEYWORD = Cpp.KEYWORD
    val CONTROL_FLOW = Cpp.CONTROL_FLOW_KEYWORD
    val CONTROL_TRANSFER = Cpp.CONTROL_TRANSFER_KEYWORD
    val BUILTIN_TYPE = Cpp.BUILTIN_TYPE_KEYWORD
    val THIS = cpp("THIS_KEYWORD", KEYWORD)
    val INTEGER = Cpp.INTEGER_LITERAL
    val FLOAT = Cpp.FLOAT_LITERAL
    val STRING = Cpp.STRING_LITERAL
    val RAW_STRING = Cpp.RAW_STRING_LITERAL
    val VALID_ESCAPE = Cpp.VALID_STRING_ESCAPE
    val INVALID_ESCAPE = Cpp.INVALID_STRING_ESCAPE
    val DOC_COMMENT = Cpp.DOXYGEN_COMMENT
    val DOC_COMMAND = cpp("DOXYGEN_COMMAND", Rider.DOC_COMMENT_ATTRIBUTE)
    val LINE_COMMENT = Cpp.LINE_COMMENT
    val BLOCK_COMMENT = Cpp.BLOCK_COMMENT
    val DIRECTIVE = Cpp.DIRECTIVE
    val OPERATOR = Cpp.OPERATOR_SIGN
    val DOT = Cpp.DOT
    val COMMA = Cpp.COMMA
    val SEMICOLON = Cpp.SEMICOLON
    val PARENTHESES = Cpp.PARENTHESES
    val BRACKETS = Cpp.BRACKETS
    val BRACES = Cpp.BRACES
    val LOCAL = cpp("LOCAL_VARIABLE_IDENTIFIER", Rider.LOCAL_VARIABLE)
    val GLOBAL = cpp("GLOBAL_VARIABLE_IDENTIFIER", Rider.IDENTIFIER)
    val PARAMETER = cpp("PARAMETER_VARIABLE_IDENTIFIER", Rider.PARAMETER)
    val FIELD = cpp("CLASS_FIELD_IDENTIFIER", Rider.FIELD)
    val STRUCT_FIELD = cpp("STRUCT_FIELD_IDENTIFIER", Rider.FIELD)
    val FUNCTION = cpp("GLOBAL_FUNCTION_IDENTIFIER", Rider.FUNCTION)
    val METHOD = cpp("MEMBER_FUNCTION_IDENTIFIER", Rider.METHOD)
    val OVERLOADED_OPERATOR = cpp("OVERLOADED_OPERATOR_IDENTIFIER", Rider.OVERLOADED_OPERATOR)
    val CLASS = cpp("CLASS_IDENTIFIER", Rider.CLASS)
    val STRUCT = cpp("STRUCT_IDENTIFIER", Rider.STRUCT)
    val ENUM = cpp("ENUM_IDENTIFIER", Rider.ENUM)
    val TYPEDEF = cpp("TYPEDEF_IDENTIFIER", Rider.CLASS)
    val NAMESPACE = cpp("NAMESPACE_IDENTIFIER", Rider.NAMESPACE)
    val CONSTANT = cpp("CONSTANT_IDENTIFIER", Rider.CONSTANT)
    val ENUM_CONSTANT = cpp("ENUM_ENUMERATOR_IDENTIFIER", CONSTANT)
    val MACRO = cpp("PREPROCESSOR_MACRO_IDENTIFIER", Rider.IDENTIFIER)
    val REFLECTION_SPECIFIER = cpp("UE4_REFLECTION_SPECIFIER_IDENTIFIER", Rider.IDENTIFIER)
}
