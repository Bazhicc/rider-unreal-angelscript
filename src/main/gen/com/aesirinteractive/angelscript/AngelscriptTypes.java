// This is a generated file. Not intended for manual editing.
package com.aesirinteractive.angelscript;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.aesirinteractive.angelscript.psi.impl.*;

public interface AngelscriptTypes {

  IElementType ADD_EXPR = new AngelscriptElementType("ADD_EXPR");
  IElementType ARGUMENT = new AngelscriptElementType("ARGUMENT");
  IElementType ARG_LIST = new AngelscriptElementType("ARG_LIST");
  IElementType ASSET_DECL = new AngelscriptElementType("ASSET_DECL");
  IElementType ASSIGNMENT_EXPR = new AngelscriptElementType("ASSIGNMENT_EXPR");
  IElementType BITWISE_AND_EXPR = new AngelscriptElementType("BITWISE_AND_EXPR");
  IElementType BREAK_STATEMENT = new AngelscriptElementType("BREAK_STATEMENT");
  IElementType CASE_STATEMENT = new AngelscriptElementType("CASE_STATEMENT");
  IElementType CAST_EXPR = new AngelscriptElementType("CAST_EXPR");
  IElementType CLASS_DECL = new AngelscriptElementType("CLASS_DECL");
  IElementType COMMA_EXPR = new AngelscriptElementType("COMMA_EXPR");
  IElementType COMMENT_NODE = new AngelscriptElementType("COMMENT_NODE");
  IElementType COMPOUND_STATEMENT = new AngelscriptElementType("COMPOUND_STATEMENT");
  IElementType CONDITIONAL_EXPR = new AngelscriptElementType("CONDITIONAL_EXPR");
  IElementType CONSTRUCTOR_DECL = new AngelscriptElementType("CONSTRUCTOR_DECL");
  IElementType CONTINUE_STATEMENT = new AngelscriptElementType("CONTINUE_STATEMENT");
  IElementType DEFAULT_VALUE_DECL = new AngelscriptElementType("DEFAULT_VALUE_DECL");
  IElementType DELEGATE_DECL = new AngelscriptElementType("DELEGATE_DECL");
  IElementType DO_STATEMENT = new AngelscriptElementType("DO_STATEMENT");
  IElementType ELSE_CLAUSE = new AngelscriptElementType("ELSE_CLAUSE");
  IElementType ENUM_DECL = new AngelscriptElementType("ENUM_DECL");
  IElementType ENUM_VARIANT = new AngelscriptElementType("ENUM_VARIANT");
  IElementType EQUALITY_EXPR = new AngelscriptElementType("EQUALITY_EXPR");
  IElementType EVENT_DECL = new AngelscriptElementType("EVENT_DECL");
  IElementType EXCLUSIVE_OR_EXPR = new AngelscriptElementType("EXCLUSIVE_OR_EXPR");
  IElementType EXPR = new AngelscriptElementType("EXPR");
  IElementType EXPRESSION_STATEMENT = new AngelscriptElementType("EXPRESSION_STATEMENT");
  IElementType FOREACH_STATEMENT = new AngelscriptElementType("FOREACH_STATEMENT");
  IElementType FOR_STATEMENT = new AngelscriptElementType("FOR_STATEMENT");
  IElementType FUNCTION_DECL = new AngelscriptElementType("FUNCTION_DECL");
  IElementType F_STRING_EXPR = new AngelscriptElementType("F_STRING_EXPR");
  IElementType F_STRING_FORMAT_SPEC = new AngelscriptElementType("F_STRING_FORMAT_SPEC");
  IElementType F_STRING_INTERP = new AngelscriptElementType("F_STRING_INTERP");
  IElementType IF_DEF_BLOCK = new AngelscriptElementType("IF_DEF_BLOCK");
  IElementType IF_DEF_BRANCH = new AngelscriptElementType("IF_DEF_BRANCH");
  IElementType IF_STATEMENT = new AngelscriptElementType("IF_STATEMENT");
  IElementType INCLUSIVE_OR_EXPR = new AngelscriptElementType("INCLUSIVE_OR_EXPR");
  IElementType INIT_LIST_EXPR = new AngelscriptElementType("INIT_LIST_EXPR");
  IElementType INTERFACE_DECL = new AngelscriptElementType("INTERFACE_DECL");
  IElementType LOGICAL_AND_EXPR = new AngelscriptElementType("LOGICAL_AND_EXPR");
  IElementType LOGICAL_OR_EXPR = new AngelscriptElementType("LOGICAL_OR_EXPR");
  IElementType MIXIN_DECL = new AngelscriptElementType("MIXIN_DECL");
  IElementType MULTIPLY_EXPR = new AngelscriptElementType("MULTIPLY_EXPR");
  IElementType NAMESPACE_DECL = new AngelscriptElementType("NAMESPACE_DECL");
  IElementType PARAMETER_DECL = new AngelscriptElementType("PARAMETER_DECL");
  IElementType PARAMETER_LIST = new AngelscriptElementType("PARAMETER_LIST");
  IElementType PARENTHESIZED_EXPR = new AngelscriptElementType("PARENTHESIZED_EXPR");
  IElementType POSTFIX_EXPR = new AngelscriptElementType("POSTFIX_EXPR");
  IElementType PP_DEFINE_DIRECTIVE = new AngelscriptElementType("PP_DEFINE_DIRECTIVE");
  IElementType PRIMARY_EXPR = new AngelscriptElementType("PRIMARY_EXPR");
  IElementType RELATIONAL_EXPR = new AngelscriptElementType("RELATIONAL_EXPR");
  IElementType RETURN_STATEMENT = new AngelscriptElementType("RETURN_STATEMENT");
  IElementType SCOPE_REF = new AngelscriptElementType("SCOPE_REF");
  IElementType SHIFT_EXPR = new AngelscriptElementType("SHIFT_EXPR");
  IElementType STATEMENT = new AngelscriptElementType("STATEMENT");
  IElementType STRING_CONCAT_EXPR = new AngelscriptElementType("STRING_CONCAT_EXPR");
  IElementType STRUCT_DECL = new AngelscriptElementType("STRUCT_DECL");
  IElementType SWITCH_STATEMENT = new AngelscriptElementType("SWITCH_STATEMENT");
  IElementType TYPEDEF_DECL = new AngelscriptElementType("TYPEDEF_DECL");
  IElementType TYPE_ARGUMENT = new AngelscriptElementType("TYPE_ARGUMENT");
  IElementType TYPE_REF = new AngelscriptElementType("TYPE_REF");
  IElementType UNARY_EXPR = new AngelscriptElementType("UNARY_EXPR");
  IElementType U_CLASS_DECL = new AngelscriptElementType("U_CLASS_DECL");
  IElementType U_ENUM_DECL = new AngelscriptElementType("U_ENUM_DECL");
  IElementType U_FUNCTION_DECL = new AngelscriptElementType("U_FUNCTION_DECL");
  IElementType U_INTERFACE_DECL = new AngelscriptElementType("U_INTERFACE_DECL");
  IElementType U_PROPERTY_DECL = new AngelscriptElementType("U_PROPERTY_DECL");
  IElementType U_STRUCT_DECL = new AngelscriptElementType("U_STRUCT_DECL");
  IElementType VARIABLE_ACCESS_EXPR = new AngelscriptElementType("VARIABLE_ACCESS_EXPR");
  IElementType VARIABLE_DECL = new AngelscriptElementType("VARIABLE_DECL");
  IElementType WHILE_STATEMENT = new AngelscriptElementType("WHILE_STATEMENT");

  IElementType AND = new AngelscriptTokenType("&");
  IElementType ANDAND = new AngelscriptTokenType("&&");
  IElementType ANDEQ = new AngelscriptTokenType("&=");
  IElementType AND_KW = new AngelscriptTokenType("and");
  IElementType ASSET_KW = new AngelscriptTokenType("asset");
  IElementType AT = new AngelscriptTokenType("@");
  IElementType AUTO = new AngelscriptTokenType("AUTO");
  IElementType AUTO_KW = new AngelscriptTokenType("auto");
  IElementType BOOL_KW = new AngelscriptTokenType("bool");
  IElementType BOOL_LITERAL = new AngelscriptTokenType("BOOL_LITERAL");
  IElementType BREAK = new AngelscriptTokenType("break");
  IElementType CASE_KW = new AngelscriptTokenType("case");
  IElementType CLASS = new AngelscriptTokenType("class");
  IElementType COLON = new AngelscriptTokenType(":");
  IElementType COLONCOLON = new AngelscriptTokenType("::");
  IElementType COMMA = new AngelscriptTokenType(",");
  IElementType COMMENT = new AngelscriptTokenType("COMMENT");
  IElementType CONST = new AngelscriptTokenType("const");
  IElementType CONTINUE = new AngelscriptTokenType("continue");
  IElementType DEFAULT_KW = new AngelscriptTokenType("default");
  IElementType DELEGATE_KW = new AngelscriptTokenType("delegate");
  IElementType DIV = new AngelscriptTokenType("/");
  IElementType DIVEQ = new AngelscriptTokenType("/=");
  IElementType DOT = new AngelscriptTokenType(".");
  IElementType DOUBLE_KW = new AngelscriptTokenType("double");
  IElementType DO_KW = new AngelscriptTokenType("do");
  IElementType ELSE = new AngelscriptTokenType("else");
  IElementType ENUM = new AngelscriptTokenType("enum");
  IElementType EQ = new AngelscriptTokenType("=");
  IElementType EQEQ = new AngelscriptTokenType("==");
  IElementType EVENT_KW = new AngelscriptTokenType("event");
  IElementType EXCL = new AngelscriptTokenType("!");
  IElementType EXCLEQ = new AngelscriptTokenType("!=");
  IElementType EXPLICIT_KW = new AngelscriptTokenType("explicit");
  IElementType EXTERNAL_KW = new AngelscriptTokenType("external");
  IElementType FINAL_KW = new AngelscriptTokenType("final");
  IElementType FLOAT_KW = new AngelscriptTokenType("float");
  IElementType FOR = new AngelscriptTokenType("for");
  IElementType FSTRING_COLON = new AngelscriptTokenType("FSTRING_COLON");
  IElementType FSTRING_END = new AngelscriptTokenType("FSTRING_END");
  IElementType FSTRING_FORMAT_TEXT = new AngelscriptTokenType("FSTRING_FORMAT_TEXT");
  IElementType FSTRING_LBRACE = new AngelscriptTokenType("FSTRING_LBRACE");
  IElementType FSTRING_RBRACE = new AngelscriptTokenType("FSTRING_RBRACE");
  IElementType FSTRING_START = new AngelscriptTokenType("FSTRING_START");
  IElementType FSTRING_TEXT = new AngelscriptTokenType("FSTRING_TEXT");
  IElementType GT = new AngelscriptTokenType(">");
  IElementType GTEQ = new AngelscriptTokenType(">=");
  IElementType IDENTIFIER = new AngelscriptTokenType("IDENTIFIER");
  IElementType IF = new AngelscriptTokenType("if");
  IElementType IN = new AngelscriptTokenType("in");
  IElementType INOUT_KW = new AngelscriptTokenType("inout");
  IElementType INT16_KW = new AngelscriptTokenType("int16");
  IElementType INT32_KW = new AngelscriptTokenType("int32");
  IElementType INT64_KW = new AngelscriptTokenType("int64");
  IElementType INT8_KW = new AngelscriptTokenType("int8");
  IElementType INTERFACE_KW = new AngelscriptTokenType("interface");
  IElementType INT_KW = new AngelscriptTokenType("int");
  IElementType IS_KW = new AngelscriptTokenType("is");
  IElementType LBRACE = new AngelscriptTokenType("{");
  IElementType LBRACK = new AngelscriptTokenType("[");
  IElementType LPAREN = new AngelscriptTokenType("(");
  IElementType LSHIFTEQ = new AngelscriptTokenType("<<=");
  IElementType LT = new AngelscriptTokenType("<");
  IElementType LTEQ = new AngelscriptTokenType("<=");
  IElementType LTLT = new AngelscriptTokenType("<<");
  IElementType MINUS = new AngelscriptTokenType("-");
  IElementType MINUSEQ = new AngelscriptTokenType("-=");
  IElementType MINUSMINUS = new AngelscriptTokenType("--");
  IElementType MIXIN_KW = new AngelscriptTokenType("mixin");
  IElementType MUL = new AngelscriptTokenType("*");
  IElementType MULEQ = new AngelscriptTokenType("*=");
  IElementType NAMESPACE_KW = new AngelscriptTokenType("namespace");
  IElementType NOT_KW = new AngelscriptTokenType("not");
  IElementType NULLPTR_KW = new AngelscriptTokenType("nullptr");
  IElementType NUMBER_LITERAL = new AngelscriptTokenType("NUMBER_LITERAL");
  IElementType OF_KW = new AngelscriptTokenType("of");
  IElementType OR = new AngelscriptTokenType("|");
  IElementType OREQ = new AngelscriptTokenType("|=");
  IElementType OROR = new AngelscriptTokenType("||");
  IElementType OR_KW = new AngelscriptTokenType("or");
  IElementType OUT = new AngelscriptTokenType("out");
  IElementType OVERRIDE_KW = new AngelscriptTokenType("override");
  IElementType PLUS = new AngelscriptTokenType("+");
  IElementType PLUSEQ = new AngelscriptTokenType("+=");
  IElementType PLUSPLUS = new AngelscriptTokenType("++");
  IElementType POWER = new AngelscriptTokenType("**");
  IElementType POWEREQ = new AngelscriptTokenType("**=");
  IElementType PP_DEFINE = new AngelscriptTokenType("PP_DEFINE");
  IElementType PP_ELIF = new AngelscriptTokenType("PP_ELIF");
  IElementType PP_ELSE = new AngelscriptTokenType("PP_ELSE");
  IElementType PP_ENDIF = new AngelscriptTokenType("PP_ENDIF");
  IElementType PP_IF = new AngelscriptTokenType("PP_IF");
  IElementType PRIVATE_KW = new AngelscriptTokenType("private");
  IElementType PROPERTY_KW = new AngelscriptTokenType("property");
  IElementType PROTECTED_KW = new AngelscriptTokenType("protected");
  IElementType PUBLIC = new AngelscriptTokenType("public");
  IElementType QUEST = new AngelscriptTokenType("?");
  IElementType RBRACE = new AngelscriptTokenType("}");
  IElementType RBRACK = new AngelscriptTokenType("]");
  IElementType REM = new AngelscriptTokenType("%");
  IElementType REMEQ = new AngelscriptTokenType("%=");
  IElementType RETURN = new AngelscriptTokenType("return");
  IElementType RPAREN = new AngelscriptTokenType(")");
  IElementType RRSHIFTEQ = new AngelscriptTokenType(">>>=");
  IElementType RSHIFTEQ = new AngelscriptTokenType(">>=");
  IElementType SEMICOLON = new AngelscriptTokenType(";");
  IElementType SHA = new AngelscriptTokenType("#");
  IElementType SHARED_KW = new AngelscriptTokenType("shared");
  IElementType STATIC = new AngelscriptTokenType("static");
  IElementType STRING_LITERAL = new AngelscriptTokenType("STRING_LITERAL");
  IElementType STRUCT = new AngelscriptTokenType("struct");
  IElementType SUPER = new AngelscriptTokenType("super");
  IElementType SWITCH_KW = new AngelscriptTokenType("switch");
  IElementType TILDE = new AngelscriptTokenType("~");
  IElementType TYPEDEF_KW = new AngelscriptTokenType("typedef");
  IElementType UCLASS_KW = new AngelscriptTokenType("UCLASS");
  IElementType UENUM_KW = new AngelscriptTokenType("UENUM");
  IElementType UFUNCTION_KW = new AngelscriptTokenType("UFUNCTION");
  IElementType UINT16_KW = new AngelscriptTokenType("uint16");
  IElementType UINT32_KW = new AngelscriptTokenType("uint32");
  IElementType UINT64_KW = new AngelscriptTokenType("uint64");
  IElementType UINT8_KW = new AngelscriptTokenType("uint8");
  IElementType UINTERFACE_KW = new AngelscriptTokenType("UINTERFACE");
  IElementType UINT_KW = new AngelscriptTokenType("uint");
  IElementType UPROPERTY_KW = new AngelscriptTokenType("UPROPERTY");
  IElementType USTRUCT_KW = new AngelscriptTokenType("USTRUCT");
  IElementType VOID_KW = new AngelscriptTokenType("void");
  IElementType WHILE = new AngelscriptTokenType("while");
  IElementType XOR = new AngelscriptTokenType("^");
  IElementType XOREQ = new AngelscriptTokenType("^=");
  IElementType XOR_KW = new AngelscriptTokenType("xor");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ADD_EXPR) {
        return new AngelscriptAddExprImpl(node);
      }
      else if (type == ARGUMENT) {
        return new AngelscriptArgumentImpl(node);
      }
      else if (type == ARG_LIST) {
        return new AngelscriptArgListImpl(node);
      }
      else if (type == ASSET_DECL) {
        return new AngelscriptAssetDeclImpl(node);
      }
      else if (type == ASSIGNMENT_EXPR) {
        return new AngelscriptAssignmentExprImpl(node);
      }
      else if (type == BITWISE_AND_EXPR) {
        return new AngelscriptBitwiseAndExprImpl(node);
      }
      else if (type == BREAK_STATEMENT) {
        return new AngelscriptBreakStatementImpl(node);
      }
      else if (type == CASE_STATEMENT) {
        return new AngelscriptCaseStatementImpl(node);
      }
      else if (type == CAST_EXPR) {
        return new AngelscriptCastExprImpl(node);
      }
      else if (type == CLASS_DECL) {
        return new AngelscriptClassDeclImpl(node);
      }
      else if (type == COMMA_EXPR) {
        return new AngelscriptCommaExprImpl(node);
      }
      else if (type == COMMENT_NODE) {
        return new AngelscriptCommentImpl(node);
      }
      else if (type == COMPOUND_STATEMENT) {
        return new AngelscriptCompoundStatementImpl(node);
      }
      else if (type == CONDITIONAL_EXPR) {
        return new AngelscriptConditionalExprImpl(node);
      }
      else if (type == CONSTRUCTOR_DECL) {
        return new AngelscriptConstructorDeclImpl(node);
      }
      else if (type == CONTINUE_STATEMENT) {
        return new AngelscriptContinueStatementImpl(node);
      }
      else if (type == DEFAULT_VALUE_DECL) {
        return new AngelscriptDefaultValueDeclImpl(node);
      }
      else if (type == DELEGATE_DECL) {
        return new AngelscriptDelegateDeclImpl(node);
      }
      else if (type == DO_STATEMENT) {
        return new AngelscriptDoStatementImpl(node);
      }
      else if (type == ELSE_CLAUSE) {
        return new AngelscriptElseClauseImpl(node);
      }
      else if (type == ENUM_DECL) {
        return new AngelscriptEnumDeclImpl(node);
      }
      else if (type == ENUM_VARIANT) {
        return new AngelscriptEnumVariantImpl(node);
      }
      else if (type == EQUALITY_EXPR) {
        return new AngelscriptEqualityExprImpl(node);
      }
      else if (type == EVENT_DECL) {
        return new AngelscriptEventDeclImpl(node);
      }
      else if (type == EXCLUSIVE_OR_EXPR) {
        return new AngelscriptExclusiveOrExprImpl(node);
      }
      else if (type == EXPRESSION_STATEMENT) {
        return new AngelscriptExpressionStatementImpl(node);
      }
      else if (type == FOREACH_STATEMENT) {
        return new AngelscriptForeachStatementImpl(node);
      }
      else if (type == FOR_STATEMENT) {
        return new AngelscriptForStatementImpl(node);
      }
      else if (type == FUNCTION_DECL) {
        return new AngelscriptFunctionDeclImpl(node);
      }
      else if (type == F_STRING_EXPR) {
        return new AngelscriptFStringExprImpl(node);
      }
      else if (type == F_STRING_FORMAT_SPEC) {
        return new AngelscriptFStringFormatSpecImpl(node);
      }
      else if (type == F_STRING_INTERP) {
        return new AngelscriptFStringInterpImpl(node);
      }
      else if (type == IF_DEF_BLOCK) {
        return new AngelscriptIfDefBlockImpl(node);
      }
      else if (type == IF_DEF_BRANCH) {
        return new AngelscriptIfDefBranchImpl(node);
      }
      else if (type == IF_STATEMENT) {
        return new AngelscriptIfStatementImpl(node);
      }
      else if (type == INCLUSIVE_OR_EXPR) {
        return new AngelscriptInclusiveOrExprImpl(node);
      }
      else if (type == INIT_LIST_EXPR) {
        return new AngelscriptInitListExprImpl(node);
      }
      else if (type == INTERFACE_DECL) {
        return new AngelscriptInterfaceDeclImpl(node);
      }
      else if (type == LOGICAL_AND_EXPR) {
        return new AngelscriptLogicalAndExprImpl(node);
      }
      else if (type == LOGICAL_OR_EXPR) {
        return new AngelscriptLogicalOrExprImpl(node);
      }
      else if (type == MIXIN_DECL) {
        return new AngelscriptMixinDeclImpl(node);
      }
      else if (type == MULTIPLY_EXPR) {
        return new AngelscriptMultiplyExprImpl(node);
      }
      else if (type == NAMESPACE_DECL) {
        return new AngelscriptNamespaceDeclImpl(node);
      }
      else if (type == PARAMETER_DECL) {
        return new AngelscriptParameterDeclImpl(node);
      }
      else if (type == PARAMETER_LIST) {
        return new AngelscriptParameterListImpl(node);
      }
      else if (type == PARENTHESIZED_EXPR) {
        return new AngelscriptParenthesizedExprImpl(node);
      }
      else if (type == POSTFIX_EXPR) {
        return new AngelscriptPostfixExprImpl(node);
      }
      else if (type == PP_DEFINE_DIRECTIVE) {
        return new AngelscriptPPDefineDirectiveImpl(node);
      }
      else if (type == PRIMARY_EXPR) {
        return new AngelscriptPrimaryExprImpl(node);
      }
      else if (type == RELATIONAL_EXPR) {
        return new AngelscriptRelationalExprImpl(node);
      }
      else if (type == RETURN_STATEMENT) {
        return new AngelscriptReturnStatementImpl(node);
      }
      else if (type == SCOPE_REF) {
        return new AngelscriptScopeRefImpl(node);
      }
      else if (type == SHIFT_EXPR) {
        return new AngelscriptShiftExprImpl(node);
      }
      else if (type == STATEMENT) {
        return new AngelscriptStatementImpl(node);
      }
      else if (type == STRING_CONCAT_EXPR) {
        return new AngelscriptStringConcatExprImpl(node);
      }
      else if (type == STRUCT_DECL) {
        return new AngelscriptStructDeclImpl(node);
      }
      else if (type == SWITCH_STATEMENT) {
        return new AngelscriptSwitchStatementImpl(node);
      }
      else if (type == TYPEDEF_DECL) {
        return new AngelscriptTypedefDeclImpl(node);
      }
      else if (type == TYPE_ARGUMENT) {
        return new AngelscriptTypeArgumentImpl(node);
      }
      else if (type == TYPE_REF) {
        return new AngelscriptTypeRefImpl(node);
      }
      else if (type == UNARY_EXPR) {
        return new AngelscriptUnaryExprImpl(node);
      }
      else if (type == U_CLASS_DECL) {
        return new AngelscriptUClassDeclImpl(node);
      }
      else if (type == U_ENUM_DECL) {
        return new AngelscriptUEnumDeclImpl(node);
      }
      else if (type == U_FUNCTION_DECL) {
        return new AngelscriptUFunctionDeclImpl(node);
      }
      else if (type == U_INTERFACE_DECL) {
        return new AngelscriptUInterfaceDeclImpl(node);
      }
      else if (type == U_PROPERTY_DECL) {
        return new AngelscriptUPropertyDeclImpl(node);
      }
      else if (type == U_STRUCT_DECL) {
        return new AngelscriptUStructDeclImpl(node);
      }
      else if (type == VARIABLE_ACCESS_EXPR) {
        return new AngelscriptVariableAccessExprImpl(node);
      }
      else if (type == VARIABLE_DECL) {
        return new AngelscriptVariableDeclImpl(node);
      }
      else if (type == WHILE_STATEMENT) {
        return new AngelscriptWhileStatementImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
