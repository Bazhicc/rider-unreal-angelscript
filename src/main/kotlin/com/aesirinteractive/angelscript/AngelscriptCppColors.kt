package com.aesirinteractive.angelscript

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.jetbrains.rider.cpp.fileType.CppTextAttributeKeys as Cpp

/** Use Rider's actual C++ color identities, including user overrides, rather than
 * copying RGB values or inheriting stale ANGELSCRIPT_* palette overrides.
 */
internal object AngelscriptCppColors {
    // Rider owns these keys and their defaults. Adding our own fallbacks could
    // alter C++ itself when its key registration happens after this plugin loads.
    private fun cpp(name: String) = TextAttributesKey.find("ReSharper.CPP_$name")

    val KEYWORD = Cpp.KEYWORD
    val CONTROL_FLOW = Cpp.CONTROL_FLOW_KEYWORD
    val CONTROL_TRANSFER = Cpp.CONTROL_TRANSFER_KEYWORD
    val BUILTIN_TYPE = Cpp.BUILTIN_TYPE_KEYWORD
    val THIS = cpp("THIS_KEYWORD")
    val INTEGER = Cpp.INTEGER_LITERAL
    val FLOAT = Cpp.FLOAT_LITERAL
    val STRING = Cpp.STRING_LITERAL
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
    val LOCAL = cpp("LOCAL_VARIABLE_IDENTIFIER")
    val GLOBAL = cpp("GLOBAL_VARIABLE_IDENTIFIER")
    val PARAMETER = cpp("PARAMETER_VARIABLE_IDENTIFIER")
    val FIELD = cpp("CLASS_FIELD_IDENTIFIER")
    val FUNCTION = cpp("GLOBAL_FUNCTION_IDENTIFIER")
    val METHOD = cpp("MEMBER_FUNCTION_IDENTIFIER")
    val CLASS = cpp("CLASS_IDENTIFIER")
    val STRUCT = cpp("STRUCT_IDENTIFIER")
    val ENUM = cpp("ENUM_IDENTIFIER")
    val TYPEDEF = cpp("TYPEDEF_IDENTIFIER")
    val NAMESPACE = cpp("NAMESPACE_IDENTIFIER")
    val ENUM_CONSTANT = cpp("ENUM_ENUMERATOR_IDENTIFIER")
    val MACRO = cpp("PREPROCESSOR_MACRO_IDENTIFIER")
    val REFLECTION_SPECIFIER = cpp("UE4_REFLECTION_SPECIFIER_IDENTIFIER")
}
