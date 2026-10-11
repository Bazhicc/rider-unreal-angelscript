// This is a generated file. Not intended for manual editing.
package com.aesirinteractive.angelscript;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.aesirinteractive.angelscript.AngelscriptTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class AngelscriptParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType root_, PsiBuilder builder_) {
    parseLight(root_, builder_);
    return builder_.getTreeBuilt();
  }

  public void parseLight(IElementType root_, PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, EXTENDS_SETS_);
    Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    result_ = parse_root_(root_, builder_);
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType root_, PsiBuilder builder_) {
    return parse_root_(root_, builder_, 0);
  }

  static boolean parse_root_(IElementType root_, PsiBuilder builder_, int level_) {
    return Script(builder_, level_ + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(ADD_EXPR, ASSIGNMENT_EXPR, BITWISE_AND_EXPR, CAST_EXPR,
      COMMA_EXPR, CONDITIONAL_EXPR, EQUALITY_EXPR, EXCLUSIVE_OR_EXPR,
      EXPR, F_STRING_EXPR, INCLUSIVE_OR_EXPR, INIT_LIST_EXPR,
      LOGICAL_AND_EXPR, LOGICAL_OR_EXPR, MULTIPLY_EXPR, PARENTHESIZED_EXPR,
      POSTFIX_EXPR, PRIMARY_EXPR, RELATIONAL_EXPR, SHIFT_EXPR,
      STRING_CONCAT_EXPR, UNARY_EXPR, VARIABLE_ACCESS_EXPR),
  };

  /* ********************************************************** */
  // PRIVATE_KW | PROTECTED_KW
  static boolean AccessSpecifier(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AccessSpecifier")) return false;
    if (!nextTokenIs(builder_, "", PRIVATE_KW, PROTECTED_KW)) return false;
    boolean result_;
    result_ = consumeToken(builder_, PRIVATE_KW);
    if (!result_) result_ = consumeToken(builder_, PROTECTED_KW);
    return result_;
  }

  /* ********************************************************** */
  // MultiplyExpr ((PLUS | MINUS) MultiplyExpr)*
  public static boolean AddExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AddExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, ADD_EXPR, "<add expr>");
    result_ = MultiplyExpr(builder_, level_ + 1);
    result_ = result_ && AddExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ((PLUS | MINUS) MultiplyExpr)*
  private static boolean AddExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AddExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!AddExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "AddExpr_1", pos_)) break;
    }
    return true;
  }

  // (PLUS | MINUS) MultiplyExpr
  private static boolean AddExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AddExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = AddExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && MultiplyExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // PLUS | MINUS
  private static boolean AddExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AddExpr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, PLUS);
    if (!result_) result_ = consumeToken(builder_, MINUS);
    return result_;
  }

  /* ********************************************************** */
  // LPAREN [ Argument (COMMA Argument)* ] RPAREN
  public static boolean ArgList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ArgList")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && ArgList_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, ARG_LIST, result_);
    return result_;
  }

  // [ Argument (COMMA Argument)* ]
  private static boolean ArgList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ArgList_1")) return false;
    ArgList_1_0(builder_, level_ + 1);
    return true;
  }

  // Argument (COMMA Argument)*
  private static boolean ArgList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ArgList_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Argument(builder_, level_ + 1);
    result_ = result_ && ArgList_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA Argument)*
  private static boolean ArgList_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ArgList_1_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!ArgList_1_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ArgList_1_0_1", pos_)) break;
    }
    return true;
  }

  // COMMA Argument
  private static boolean ArgList_1_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ArgList_1_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && Argument(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // [ identifier COLON ] AssignmentExpr
  public static boolean Argument(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Argument")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ARGUMENT, "<argument>");
    result_ = Argument_0(builder_, level_ + 1);
    result_ = result_ && AssignmentExpr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // [ identifier COLON ]
  private static boolean Argument_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Argument_0")) return false;
    Argument_0_0(builder_, level_ + 1);
    return true;
  }

  // identifier COLON
  private static boolean Argument_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Argument_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = identifier(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ASSET_KW identifier OF_KW TypeRef CompoundStatement SEMICOLON?
  public static boolean AssetDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AssetDecl")) return false;
    if (!nextTokenIs(builder_, ASSET_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ASSET_KW);
    result_ = result_ && identifier(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, OF_KW);
    result_ = result_ && TypeRef(builder_, level_ + 1);
    result_ = result_ && CompoundStatement(builder_, level_ + 1);
    result_ = result_ && AssetDecl_5(builder_, level_ + 1);
    exit_section_(builder_, marker_, ASSET_DECL, result_);
    return result_;
  }

  // SEMICOLON?
  private static boolean AssetDecl_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AssetDecl_5")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // ConditionalExpr (AssignmentOp AssignmentExpr)?
  public static boolean AssignmentExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AssignmentExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, ASSIGNMENT_EXPR, "<assignment expr>");
    result_ = ConditionalExpr(builder_, level_ + 1);
    result_ = result_ && AssignmentExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (AssignmentOp AssignmentExpr)?
  private static boolean AssignmentExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AssignmentExpr_1")) return false;
    AssignmentExpr_1_0(builder_, level_ + 1);
    return true;
  }

  // AssignmentOp AssignmentExpr
  private static boolean AssignmentExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AssignmentExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = AssignmentOp(builder_, level_ + 1);
    result_ = result_ && AssignmentExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // EQ | MULEQ | POWEREQ | DIVEQ | REMEQ | PLUSEQ | MINUSEQ
  //                        | LSHIFTEQ | RSHIFTEQ | RRSHIFTEQ | ANDEQ | XOREQ | OREQ
  static boolean AssignmentOp(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "AssignmentOp")) return false;
    boolean result_;
    result_ = consumeToken(builder_, EQ);
    if (!result_) result_ = consumeToken(builder_, MULEQ);
    if (!result_) result_ = consumeToken(builder_, POWEREQ);
    if (!result_) result_ = consumeToken(builder_, DIVEQ);
    if (!result_) result_ = consumeToken(builder_, REMEQ);
    if (!result_) result_ = consumeToken(builder_, PLUSEQ);
    if (!result_) result_ = consumeToken(builder_, MINUSEQ);
    if (!result_) result_ = consumeToken(builder_, LSHIFTEQ);
    if (!result_) result_ = consumeToken(builder_, RSHIFTEQ);
    if (!result_) result_ = consumeToken(builder_, RRSHIFTEQ);
    if (!result_) result_ = consumeToken(builder_, ANDEQ);
    if (!result_) result_ = consumeToken(builder_, XOREQ);
    if (!result_) result_ = consumeToken(builder_, OREQ);
    return result_;
  }

  /* ********************************************************** */
  // EqualityExpr (AND EqualityExpr)*
  public static boolean BitwiseAndExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BitwiseAndExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, BITWISE_AND_EXPR, "<bitwise and expr>");
    result_ = EqualityExpr(builder_, level_ + 1);
    result_ = result_ && BitwiseAndExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (AND EqualityExpr)*
  private static boolean BitwiseAndExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BitwiseAndExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!BitwiseAndExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "BitwiseAndExpr_1", pos_)) break;
    }
    return true;
  }

  // AND EqualityExpr
  private static boolean BitwiseAndExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BitwiseAndExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, AND);
    result_ = result_ && EqualityExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // IfDefBlock | Declaration | Statement | Comment
  static boolean BlockItem(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BlockItem")) return false;
    boolean result_;
    result_ = IfDefBlock(builder_, level_ + 1);
    if (!result_) result_ = Declaration(builder_, level_ + 1);
    if (!result_) result_ = Statement(builder_, level_ + 1);
    if (!result_) result_ = Comment(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // !(RBRACE | IF | WHILE | FOR | RETURN | BREAK | CONTINUE
  //                                | SWITCH_KW | DO_KW | CASE_KW | DEFAULT_KW | LBRACE
  //                                | CONST | VOID_KW
  //                                | INT_KW | INT8_KW | INT16_KW | INT32_KW | INT64_KW
  //                                | UINT_KW | UINT8_KW | UINT16_KW | UINT32_KW | UINT64_KW
  //                                | FLOAT_KW | DOUBLE_KW | BOOL_KW | AUTO_KW | AUTO
  //                                | PLUSPLUS | MINUSMINUS | MINUS | EXCL | AT | TILDE | LPAREN
  //                                | NUMBER_LITERAL | STRING_LITERAL | FSTRING_START | BOOL_LITERAL | NULLPTR_KW
  //                                | IDENTIFIER | COMMENT
  //                                | PP_IF | PP_ELIF | PP_ELSE | PP_ENDIF | PP_DEFINE | <<eof>>)
  static boolean BlockItem_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BlockItem_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !BlockItem_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // RBRACE | IF | WHILE | FOR | RETURN | BREAK | CONTINUE
  //                                | SWITCH_KW | DO_KW | CASE_KW | DEFAULT_KW | LBRACE
  //                                | CONST | VOID_KW
  //                                | INT_KW | INT8_KW | INT16_KW | INT32_KW | INT64_KW
  //                                | UINT_KW | UINT8_KW | UINT16_KW | UINT32_KW | UINT64_KW
  //                                | FLOAT_KW | DOUBLE_KW | BOOL_KW | AUTO_KW | AUTO
  //                                | PLUSPLUS | MINUSMINUS | MINUS | EXCL | AT | TILDE | LPAREN
  //                                | NUMBER_LITERAL | STRING_LITERAL | FSTRING_START | BOOL_LITERAL | NULLPTR_KW
  //                                | IDENTIFIER | COMMENT
  //                                | PP_IF | PP_ELIF | PP_ELSE | PP_ENDIF | PP_DEFINE | <<eof>>
  private static boolean BlockItem_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BlockItem_recover_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = consumeToken(builder_, IF);
    if (!result_) result_ = consumeToken(builder_, WHILE);
    if (!result_) result_ = consumeToken(builder_, FOR);
    if (!result_) result_ = consumeToken(builder_, RETURN);
    if (!result_) result_ = consumeToken(builder_, BREAK);
    if (!result_) result_ = consumeToken(builder_, CONTINUE);
    if (!result_) result_ = consumeToken(builder_, SWITCH_KW);
    if (!result_) result_ = consumeToken(builder_, DO_KW);
    if (!result_) result_ = consumeToken(builder_, CASE_KW);
    if (!result_) result_ = consumeToken(builder_, DEFAULT_KW);
    if (!result_) result_ = consumeToken(builder_, LBRACE);
    if (!result_) result_ = consumeToken(builder_, CONST);
    if (!result_) result_ = consumeToken(builder_, VOID_KW);
    if (!result_) result_ = consumeToken(builder_, INT_KW);
    if (!result_) result_ = consumeToken(builder_, INT8_KW);
    if (!result_) result_ = consumeToken(builder_, INT16_KW);
    if (!result_) result_ = consumeToken(builder_, INT32_KW);
    if (!result_) result_ = consumeToken(builder_, INT64_KW);
    if (!result_) result_ = consumeToken(builder_, UINT_KW);
    if (!result_) result_ = consumeToken(builder_, UINT8_KW);
    if (!result_) result_ = consumeToken(builder_, UINT16_KW);
    if (!result_) result_ = consumeToken(builder_, UINT32_KW);
    if (!result_) result_ = consumeToken(builder_, UINT64_KW);
    if (!result_) result_ = consumeToken(builder_, FLOAT_KW);
    if (!result_) result_ = consumeToken(builder_, DOUBLE_KW);
    if (!result_) result_ = consumeToken(builder_, BOOL_KW);
    if (!result_) result_ = consumeToken(builder_, AUTO_KW);
    if (!result_) result_ = consumeToken(builder_, AUTO);
    if (!result_) result_ = consumeToken(builder_, PLUSPLUS);
    if (!result_) result_ = consumeToken(builder_, MINUSMINUS);
    if (!result_) result_ = consumeToken(builder_, MINUS);
    if (!result_) result_ = consumeToken(builder_, EXCL);
    if (!result_) result_ = consumeToken(builder_, AT);
    if (!result_) result_ = consumeToken(builder_, TILDE);
    if (!result_) result_ = consumeToken(builder_, LPAREN);
    if (!result_) result_ = consumeToken(builder_, NUMBER_LITERAL);
    if (!result_) result_ = consumeToken(builder_, STRING_LITERAL);
    if (!result_) result_ = consumeToken(builder_, FSTRING_START);
    if (!result_) result_ = consumeToken(builder_, BOOL_LITERAL);
    if (!result_) result_ = consumeToken(builder_, NULLPTR_KW);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = consumeToken(builder_, COMMENT);
    if (!result_) result_ = consumeToken(builder_, PP_IF);
    if (!result_) result_ = consumeToken(builder_, PP_ELIF);
    if (!result_) result_ = consumeToken(builder_, PP_ELSE);
    if (!result_) result_ = consumeToken(builder_, PP_ENDIF);
    if (!result_) result_ = consumeToken(builder_, PP_DEFINE);
    if (!result_) result_ = eof(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(RBRACE | <<eof>>) BlockItem
  static boolean BlockItem_with_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BlockItem_with_recover")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = BlockItem_with_recover_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && BlockItem(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, AngelscriptParser::BlockItem_recover);
    return result_ || pinned_;
  }

  // !(RBRACE | <<eof>>)
  private static boolean BlockItem_with_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BlockItem_with_recover_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !BlockItem_with_recover_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // RBRACE | <<eof>>
  private static boolean BlockItem_with_recover_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BlockItem_with_recover_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = eof(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // BREAK    SEMICOLON
  public static boolean BreakStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "BreakStatement")) return false;
    if (!nextTokenIs(builder_, BREAK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, BREAK, SEMICOLON);
    exit_section_(builder_, marker_, BREAK_STATEMENT, result_);
    return result_;
  }

  /* ********************************************************** */
  // TypeArgument? ArgList
  static boolean CallSuffix(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CallSuffix")) return false;
    if (!nextTokenIs(builder_, "", LPAREN, LT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = CallSuffix_0(builder_, level_ + 1);
    result_ = result_ && ArgList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // TypeArgument?
  private static boolean CallSuffix_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CallSuffix_0")) return false;
    TypeArgument(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // ( CASE_KW Expr | DEFAULT_KW ) COLON (NonCaseStatement | VariableDecl)*
  public static boolean CaseStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CaseStatement")) return false;
    if (!nextTokenIs(builder_, "<case statement>", CASE_KW, DEFAULT_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CASE_STATEMENT, "<case statement>");
    result_ = CaseStatement_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && CaseStatement_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // CASE_KW Expr | DEFAULT_KW
  private static boolean CaseStatement_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CaseStatement_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = CaseStatement_0_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, DEFAULT_KW);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // CASE_KW Expr
  private static boolean CaseStatement_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CaseStatement_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CASE_KW);
    result_ = result_ && Expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (NonCaseStatement | VariableDecl)*
  private static boolean CaseStatement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CaseStatement_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!CaseStatement_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "CaseStatement_2", pos_)) break;
    }
    return true;
  }

  // NonCaseStatement | VariableDecl
  private static boolean CaseStatement_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CaseStatement_2_0")) return false;
    boolean result_;
    result_ = NonCaseStatement(builder_, level_ + 1);
    if (!result_) result_ = VariableDecl(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // LPAREN TypeRef RPAREN UnaryExpr
  //            | PrimitiveType LPAREN Expr RPAREN
  public static boolean CastExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CastExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CAST_EXPR, "<cast expr>");
    result_ = CastExpr_0(builder_, level_ + 1);
    if (!result_) result_ = CastExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // LPAREN TypeRef RPAREN UnaryExpr
  private static boolean CastExpr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CastExpr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && TypeRef(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    result_ = result_ && UnaryExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // PrimitiveType LPAREN Expr RPAREN
  private static boolean CastExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CastExpr_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = PrimitiveType(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LPAREN);
    result_ = result_ && Expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // UClassDecl? CLASS identifier InheritanceList? LBRACE ClassMember_with_recover* RBRACE SEMICOLON?
  public static boolean ClassDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ClassDecl")) return false;
    if (!nextTokenIs(builder_, "<class decl>", CLASS, UCLASS_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CLASS_DECL, "<class decl>");
    result_ = ClassDecl_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, CLASS);
    result_ = result_ && identifier(builder_, level_ + 1);
    result_ = result_ && ClassDecl_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LBRACE);
    result_ = result_ && ClassDecl_5(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    result_ = result_ && ClassDecl_7(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // UClassDecl?
  private static boolean ClassDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ClassDecl_0")) return false;
    UClassDecl(builder_, level_ + 1);
    return true;
  }

  // InheritanceList?
  private static boolean ClassDecl_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ClassDecl_3")) return false;
    InheritanceList(builder_, level_ + 1);
    return true;
  }

  // ClassMember_with_recover*
  private static boolean ClassDecl_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ClassDecl_5")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!ClassMember_with_recover(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ClassDecl_5", pos_)) break;
    }
    return true;
  }

  // SEMICOLON?
  private static boolean ClassDecl_7(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ClassDecl_7")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // IfDefBlock | ConstructorDecl | FunctionDecl | DefaultValueDecl | VariableDecl | Comment
  static boolean ClassMember(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ClassMember")) return false;
    boolean result_;
    result_ = IfDefBlock(builder_, level_ + 1);
    if (!result_) result_ = ConstructorDecl(builder_, level_ + 1);
    if (!result_) result_ = FunctionDecl(builder_, level_ + 1);
    if (!result_) result_ = DefaultValueDecl(builder_, level_ + 1);
    if (!result_) result_ = VariableDecl(builder_, level_ + 1);
    if (!result_) result_ = Comment(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // !(RBRACE | UFUNCTION_KW | UPROPERTY_KW | PRIVATE_KW | PROTECTED_KW
  //                                 | STATIC | CONST | SHARED_KW | EXTERNAL_KW | DEFAULT_KW | VOID_KW
  //                                 | INT_KW | INT8_KW | INT16_KW | INT32_KW | INT64_KW
  //                                 | UINT_KW | UINT8_KW | UINT16_KW | UINT32_KW | UINT64_KW
  //                                 | FLOAT_KW | DOUBLE_KW | BOOL_KW | AUTO_KW | AUTO
  //                                 | TILDE | IDENTIFIER | COMMENT
  //                                 | PP_IF | PP_ELIF | PP_ELSE | PP_ENDIF | PP_DEFINE | <<eof>>)
  static boolean ClassMember_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ClassMember_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !ClassMember_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // RBRACE | UFUNCTION_KW | UPROPERTY_KW | PRIVATE_KW | PROTECTED_KW
  //                                 | STATIC | CONST | SHARED_KW | EXTERNAL_KW | DEFAULT_KW | VOID_KW
  //                                 | INT_KW | INT8_KW | INT16_KW | INT32_KW | INT64_KW
  //                                 | UINT_KW | UINT8_KW | UINT16_KW | UINT32_KW | UINT64_KW
  //                                 | FLOAT_KW | DOUBLE_KW | BOOL_KW | AUTO_KW | AUTO
  //                                 | TILDE | IDENTIFIER | COMMENT
  //                                 | PP_IF | PP_ELIF | PP_ELSE | PP_ENDIF | PP_DEFINE | <<eof>>
  private static boolean ClassMember_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ClassMember_recover_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = consumeToken(builder_, UFUNCTION_KW);
    if (!result_) result_ = consumeToken(builder_, UPROPERTY_KW);
    if (!result_) result_ = consumeToken(builder_, PRIVATE_KW);
    if (!result_) result_ = consumeToken(builder_, PROTECTED_KW);
    if (!result_) result_ = consumeToken(builder_, STATIC);
    if (!result_) result_ = consumeToken(builder_, CONST);
    if (!result_) result_ = consumeToken(builder_, SHARED_KW);
    if (!result_) result_ = consumeToken(builder_, EXTERNAL_KW);
    if (!result_) result_ = consumeToken(builder_, DEFAULT_KW);
    if (!result_) result_ = consumeToken(builder_, VOID_KW);
    if (!result_) result_ = consumeToken(builder_, INT_KW);
    if (!result_) result_ = consumeToken(builder_, INT8_KW);
    if (!result_) result_ = consumeToken(builder_, INT16_KW);
    if (!result_) result_ = consumeToken(builder_, INT32_KW);
    if (!result_) result_ = consumeToken(builder_, INT64_KW);
    if (!result_) result_ = consumeToken(builder_, UINT_KW);
    if (!result_) result_ = consumeToken(builder_, UINT8_KW);
    if (!result_) result_ = consumeToken(builder_, UINT16_KW);
    if (!result_) result_ = consumeToken(builder_, UINT32_KW);
    if (!result_) result_ = consumeToken(builder_, UINT64_KW);
    if (!result_) result_ = consumeToken(builder_, FLOAT_KW);
    if (!result_) result_ = consumeToken(builder_, DOUBLE_KW);
    if (!result_) result_ = consumeToken(builder_, BOOL_KW);
    if (!result_) result_ = consumeToken(builder_, AUTO_KW);
    if (!result_) result_ = consumeToken(builder_, AUTO);
    if (!result_) result_ = consumeToken(builder_, TILDE);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = consumeToken(builder_, COMMENT);
    if (!result_) result_ = consumeToken(builder_, PP_IF);
    if (!result_) result_ = consumeToken(builder_, PP_ELIF);
    if (!result_) result_ = consumeToken(builder_, PP_ELSE);
    if (!result_) result_ = consumeToken(builder_, PP_ENDIF);
    if (!result_) result_ = consumeToken(builder_, PP_DEFINE);
    if (!result_) result_ = eof(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(RBRACE | <<eof>>) ClassMember
  static boolean ClassMember_with_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ClassMember_with_recover")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = ClassMember_with_recover_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && ClassMember(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, AngelscriptParser::ClassMember_recover);
    return result_ || pinned_;
  }

  // !(RBRACE | <<eof>>)
  private static boolean ClassMember_with_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ClassMember_with_recover_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !ClassMember_with_recover_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // RBRACE | <<eof>>
  private static boolean ClassMember_with_recover_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ClassMember_with_recover_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = eof(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // AssignmentExpr (COMMA AssignmentExpr)*
  public static boolean CommaExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CommaExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, COMMA_EXPR, "<comma expr>");
    result_ = AssignmentExpr(builder_, level_ + 1);
    result_ = result_ && CommaExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (COMMA AssignmentExpr)*
  private static boolean CommaExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CommaExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!CommaExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "CommaExpr_1", pos_)) break;
    }
    return true;
  }

  // COMMA AssignmentExpr
  private static boolean CommaExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CommaExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && AssignmentExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // COMMENT
  public static boolean Comment(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Comment")) return false;
    if (!nextTokenIs(builder_, COMMENT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMENT);
    exit_section_(builder_, marker_, COMMENT_NODE, result_);
    return result_;
  }

  /* ********************************************************** */
  // LBRACE BlockItem_with_recover* RBRACE
  public static boolean CompoundStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CompoundStatement")) return false;
    if (!nextTokenIs(builder_, LBRACE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACE);
    result_ = result_ && CompoundStatement_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, COMPOUND_STATEMENT, result_);
    return result_;
  }

  // BlockItem_with_recover*
  private static boolean CompoundStatement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "CompoundStatement_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!BlockItem_with_recover(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "CompoundStatement_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // LogicalOrExpr (QUEST Expr? COLON AssignmentExpr)?
  public static boolean ConditionalExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConditionalExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, CONDITIONAL_EXPR, "<conditional expr>");
    result_ = LogicalOrExpr(builder_, level_ + 1);
    result_ = result_ && ConditionalExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (QUEST Expr? COLON AssignmentExpr)?
  private static boolean ConditionalExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConditionalExpr_1")) return false;
    ConditionalExpr_1_0(builder_, level_ + 1);
    return true;
  }

  // QUEST Expr? COLON AssignmentExpr
  private static boolean ConditionalExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConditionalExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, QUEST);
    result_ = result_ && ConditionalExpr_1_0_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && AssignmentExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // Expr?
  private static boolean ConditionalExpr_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConditionalExpr_1_0_1")) return false;
    Expr(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // AccessSpecifier?
  //                     identifier
  //                     ParameterList
  //                     (SEMICOLON | CompoundStatement SEMICOLON?)
  public static boolean ConstructorDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstructorDecl")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CONSTRUCTOR_DECL, "<constructor decl>");
    result_ = ConstructorDecl_0(builder_, level_ + 1);
    result_ = result_ && identifier(builder_, level_ + 1);
    result_ = result_ && ParameterList(builder_, level_ + 1);
    result_ = result_ && ConstructorDecl_3(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // AccessSpecifier?
  private static boolean ConstructorDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstructorDecl_0")) return false;
    AccessSpecifier(builder_, level_ + 1);
    return true;
  }

  // SEMICOLON | CompoundStatement SEMICOLON?
  private static boolean ConstructorDecl_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstructorDecl_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = ConstructorDecl_3_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // CompoundStatement SEMICOLON?
  private static boolean ConstructorDecl_3_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstructorDecl_3_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = CompoundStatement(builder_, level_ + 1);
    result_ = result_ && ConstructorDecl_3_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // SEMICOLON?
  private static boolean ConstructorDecl_3_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ConstructorDecl_3_1_1")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // CONTINUE SEMICOLON
  public static boolean ContinueStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ContinueStatement")) return false;
    if (!nextTokenIs(builder_, CONTINUE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, CONTINUE, SEMICOLON);
    exit_section_(builder_, marker_, CONTINUE_STATEMENT, result_);
    return result_;
  }

  /* ********************************************************** */
  // PrimitiveType | QUEST | AUTO_KW | AUTO | identifier
  static boolean DataType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "DataType")) return false;
    boolean result_;
    result_ = PrimitiveType(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, QUEST);
    if (!result_) result_ = consumeToken(builder_, AUTO_KW);
    if (!result_) result_ = consumeToken(builder_, AUTO);
    if (!result_) result_ = identifier(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // NamespaceDecl
  //                       | AssetDecl
  //                       | EnumDecl
  //                       | TypedefDecl
  //                       | ClassDecl
  //                       | StructDecl
  //                       | InterfaceDecl
  //                       | EventDecl
  //                       | DelegateDecl
  //                       | MixinDecl
  //                       | FunctionDecl
  //                       | DefaultValueDecl
  //                       | VariableDecl
  //                       | Comment
  static boolean Declaration(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Declaration")) return false;
    boolean result_;
    result_ = NamespaceDecl(builder_, level_ + 1);
    if (!result_) result_ = AssetDecl(builder_, level_ + 1);
    if (!result_) result_ = EnumDecl(builder_, level_ + 1);
    if (!result_) result_ = TypedefDecl(builder_, level_ + 1);
    if (!result_) result_ = ClassDecl(builder_, level_ + 1);
    if (!result_) result_ = StructDecl(builder_, level_ + 1);
    if (!result_) result_ = InterfaceDecl(builder_, level_ + 1);
    if (!result_) result_ = EventDecl(builder_, level_ + 1);
    if (!result_) result_ = DelegateDecl(builder_, level_ + 1);
    if (!result_) result_ = MixinDecl(builder_, level_ + 1);
    if (!result_) result_ = FunctionDecl(builder_, level_ + 1);
    if (!result_) result_ = DefaultValueDecl(builder_, level_ + 1);
    if (!result_) result_ = VariableDecl(builder_, level_ + 1);
    if (!result_) result_ = Comment(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // TypeRef TypeMod?
  static boolean DeclarationSpecifier(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "DeclarationSpecifier")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeRef(builder_, level_ + 1);
    result_ = result_ && DeclarationSpecifier_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // TypeMod?
  private static boolean DeclarationSpecifier_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "DeclarationSpecifier_1")) return false;
    TypeMod(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // DEFAULT_KW Expr SEMICOLON
  public static boolean DefaultValueDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "DefaultValueDecl")) return false;
    if (!nextTokenIs(builder_, DEFAULT_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DEFAULT_KW);
    result_ = result_ && Expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, DEFAULT_VALUE_DECL, result_);
    return result_;
  }

  /* ********************************************************** */
  // DELEGATE_KW FunctionDecl
  public static boolean DelegateDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "DelegateDecl")) return false;
    if (!nextTokenIs(builder_, DELEGATE_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DELEGATE_KW);
    result_ = result_ && FunctionDecl(builder_, level_ + 1);
    exit_section_(builder_, marker_, DELEGATE_DECL, result_);
    return result_;
  }

  /* ********************************************************** */
  // DO_KW Statement WHILE ParenthesizedExpr SEMICOLON
  public static boolean DoStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "DoStatement")) return false;
    if (!nextTokenIs(builder_, DO_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DO_KW);
    result_ = result_ && Statement(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, WHILE);
    result_ = result_ && ParenthesizedExpr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, DO_STATEMENT, result_);
    return result_;
  }

  /* ********************************************************** */
  // ELSE Statement
  public static boolean ElseClause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ElseClause")) return false;
    if (!nextTokenIs(builder_, ELSE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ELSE);
    result_ = result_ && Statement(builder_, level_ + 1);
    exit_section_(builder_, marker_, ELSE_CLAUSE, result_);
    return result_;
  }

  /* ********************************************************** */
  // UEnumDecl? ENUM identifier LBRACE EnumVariant_with_recover* RBRACE SEMICOLON?
  public static boolean EnumDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EnumDecl")) return false;
    if (!nextTokenIs(builder_, "<enum decl>", ENUM, UENUM_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ENUM_DECL, "<enum decl>");
    result_ = EnumDecl_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ENUM);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, identifier(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, LBRACE)) && result_;
    result_ = pinned_ && report_error_(builder_, EnumDecl_4(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, RBRACE)) && result_;
    result_ = pinned_ && EnumDecl_6(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // UEnumDecl?
  private static boolean EnumDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EnumDecl_0")) return false;
    UEnumDecl(builder_, level_ + 1);
    return true;
  }

  // EnumVariant_with_recover*
  private static boolean EnumDecl_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EnumDecl_4")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!EnumVariant_with_recover(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "EnumDecl_4", pos_)) break;
    }
    return true;
  }

  // SEMICOLON?
  private static boolean EnumDecl_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EnumDecl_6")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // identifier (EQ Expr)? COMMA?
  public static boolean EnumVariant(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EnumVariant")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = identifier(builder_, level_ + 1);
    result_ = result_ && EnumVariant_1(builder_, level_ + 1);
    result_ = result_ && EnumVariant_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, ENUM_VARIANT, result_);
    return result_;
  }

  // (EQ Expr)?
  private static boolean EnumVariant_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EnumVariant_1")) return false;
    EnumVariant_1_0(builder_, level_ + 1);
    return true;
  }

  // EQ Expr
  private static boolean EnumVariant_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EnumVariant_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EQ);
    result_ = result_ && Expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // COMMA?
  private static boolean EnumVariant_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EnumVariant_2")) return false;
    consumeToken(builder_, COMMA);
    return true;
  }

  /* ********************************************************** */
  // !(RBRACE | IDENTIFIER | <<eof>>)
  static boolean EnumVariant_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EnumVariant_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !EnumVariant_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // RBRACE | IDENTIFIER | <<eof>>
  private static boolean EnumVariant_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EnumVariant_recover_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = eof(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(RBRACE | <<eof>>) EnumVariant
  static boolean EnumVariant_with_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EnumVariant_with_recover")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = EnumVariant_with_recover_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && EnumVariant(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, AngelscriptParser::EnumVariant_recover);
    return result_ || pinned_;
  }

  // !(RBRACE | <<eof>>)
  private static boolean EnumVariant_with_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EnumVariant_with_recover_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !EnumVariant_with_recover_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // RBRACE | <<eof>>
  private static boolean EnumVariant_with_recover_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EnumVariant_with_recover_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = eof(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // RelationalExpr ((EQEQ | EXCLEQ) RelationalExpr)*
  public static boolean EqualityExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EqualityExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EQUALITY_EXPR, "<equality expr>");
    result_ = RelationalExpr(builder_, level_ + 1);
    result_ = result_ && EqualityExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ((EQEQ | EXCLEQ) RelationalExpr)*
  private static boolean EqualityExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EqualityExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!EqualityExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "EqualityExpr_1", pos_)) break;
    }
    return true;
  }

  // (EQEQ | EXCLEQ) RelationalExpr
  private static boolean EqualityExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EqualityExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = EqualityExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && RelationalExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // EQEQ | EXCLEQ
  private static boolean EqualityExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EqualityExpr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, EQEQ);
    if (!result_) result_ = consumeToken(builder_, EXCLEQ);
    return result_;
  }

  /* ********************************************************** */
  // EVENT_KW FunctionDecl
  public static boolean EventDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "EventDecl")) return false;
    if (!nextTokenIs(builder_, EVENT_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EVENT_KW);
    result_ = result_ && FunctionDecl(builder_, level_ + 1);
    exit_section_(builder_, marker_, EVENT_DECL, result_);
    return result_;
  }

  /* ********************************************************** */
  // BitwiseAndExpr ((XOR | XOR_KW) BitwiseAndExpr)*
  public static boolean ExclusiveOrExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExclusiveOrExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXCLUSIVE_OR_EXPR, "<exclusive or expr>");
    result_ = BitwiseAndExpr(builder_, level_ + 1);
    result_ = result_ && ExclusiveOrExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ((XOR | XOR_KW) BitwiseAndExpr)*
  private static boolean ExclusiveOrExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExclusiveOrExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!ExclusiveOrExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ExclusiveOrExpr_1", pos_)) break;
    }
    return true;
  }

  // (XOR | XOR_KW) BitwiseAndExpr
  private static boolean ExclusiveOrExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExclusiveOrExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ExclusiveOrExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && BitwiseAndExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // XOR | XOR_KW
  private static boolean ExclusiveOrExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExclusiveOrExpr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, XOR);
    if (!result_) result_ = consumeToken(builder_, XOR_KW);
    return result_;
  }

  /* ********************************************************** */
  // CommaExpr
  public static boolean Expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPR, "<expr>");
    result_ = CommaExpr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // Expr SEMICOLON
  public static boolean ExpressionStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ExpressionStatement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPRESSION_STATEMENT, "<expression statement>");
    result_ = Expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // FSTRING_START fstring_content* FSTRING_END
  public static boolean FStringExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FStringExpr")) return false;
    if (!nextTokenIs(builder_, FSTRING_START)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, F_STRING_EXPR, null);
    result_ = consumeToken(builder_, FSTRING_START);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, FStringExpr_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, FSTRING_END) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // fstring_content*
  private static boolean FStringExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FStringExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!fstring_content(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "FStringExpr_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // FSTRING_COLON FSTRING_FORMAT_TEXT?
  public static boolean FStringFormatSpec(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FStringFormatSpec")) return false;
    if (!nextTokenIs(builder_, FSTRING_COLON)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, F_STRING_FORMAT_SPEC, null);
    result_ = consumeToken(builder_, FSTRING_COLON);
    pinned_ = result_; // pin = 1
    result_ = result_ && FStringFormatSpec_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // FSTRING_FORMAT_TEXT?
  private static boolean FStringFormatSpec_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FStringFormatSpec_1")) return false;
    consumeToken(builder_, FSTRING_FORMAT_TEXT);
    return true;
  }

  /* ********************************************************** */
  // FSTRING_LBRACE Expr EQ? FStringFormatSpec? FSTRING_RBRACE
  public static boolean FStringInterp(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FStringInterp")) return false;
    if (!nextTokenIs(builder_, FSTRING_LBRACE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, F_STRING_INTERP, null);
    result_ = consumeToken(builder_, FSTRING_LBRACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, Expr(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, FStringInterp_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, FStringInterp_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, FSTRING_RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // EQ?
  private static boolean FStringInterp_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FStringInterp_2")) return false;
    consumeToken(builder_, EQ);
    return true;
  }

  // FStringFormatSpec?
  private static boolean FStringInterp_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FStringInterp_3")) return false;
    FStringFormatSpec(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // DOT identifier
  static boolean FieldSuffix(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FieldSuffix")) return false;
    if (!nextTokenIs(builder_, DOT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DOT);
    result_ = result_ && identifier(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ( VariableDecl | Expr? SEMICOLON ) Expr? SEMICOLON Expr?
  static boolean ForHeader(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForHeader")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ForHeader_0(builder_, level_ + 1);
    result_ = result_ && ForHeader_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    result_ = result_ && ForHeader_3(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // VariableDecl | Expr? SEMICOLON
  private static boolean ForHeader_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForHeader_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = VariableDecl(builder_, level_ + 1);
    if (!result_) result_ = ForHeader_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // Expr? SEMICOLON
  private static boolean ForHeader_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForHeader_0_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ForHeader_0_1_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // Expr?
  private static boolean ForHeader_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForHeader_0_1_0")) return false;
    Expr(builder_, level_ + 1);
    return true;
  }

  // Expr?
  private static boolean ForHeader_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForHeader_1")) return false;
    Expr(builder_, level_ + 1);
    return true;
  }

  // Expr?
  private static boolean ForHeader_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForHeader_3")) return false;
    Expr(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // FOR LPAREN ForHeader RPAREN Statement
  public static boolean ForStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForStatement")) return false;
    if (!nextTokenIs(builder_, FOR)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, FOR, LPAREN);
    result_ = result_ && ForHeader(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    result_ = result_ && Statement(builder_, level_ + 1);
    exit_section_(builder_, marker_, FOR_STATEMENT, result_);
    return result_;
  }

  /* ********************************************************** */
  // FOR LPAREN ForeachVarDecl COLON Expr RPAREN Statement
  public static boolean ForeachStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForeachStatement")) return false;
    if (!nextTokenIs(builder_, FOR)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, FOR, LPAREN);
    result_ = result_ && ForeachVarDecl(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && Expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    result_ = result_ && Statement(builder_, level_ + 1);
    exit_section_(builder_, marker_, FOREACH_STATEMENT, result_);
    return result_;
  }

  /* ********************************************************** */
  // DeclarationSpecifier identifier
  static boolean ForeachVarDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ForeachVarDecl")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = DeclarationSpecifier(builder_, level_ + 1);
    result_ = result_ && identifier(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // OVERRIDE_KW | FINAL_KW | EXPLICIT_KW | PROPERTY_KW
  static boolean FuncAttr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FuncAttr")) return false;
    boolean result_;
    result_ = consumeToken(builder_, OVERRIDE_KW);
    if (!result_) result_ = consumeToken(builder_, FINAL_KW);
    if (!result_) result_ = consumeToken(builder_, EXPLICIT_KW);
    if (!result_) result_ = consumeToken(builder_, PROPERTY_KW);
    return result_;
  }

  /* ********************************************************** */
  // AccessSpecifier?
  //                  UFunctionDecl?
  //                  AccessSpecifier?
  //                  FunctionLocation?
  //                  AccessSpecifier?
  //                  (TypeRef AND? | TILDE)?
  //                  identifier
  //                  ParameterList
  //                  CONST?
  //                  FuncAttr?
  //                  (SEMICOLON | CompoundStatement SEMICOLON?)
  public static boolean FunctionDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FUNCTION_DECL, "<function decl>");
    result_ = FunctionDecl_0(builder_, level_ + 1);
    result_ = result_ && FunctionDecl_1(builder_, level_ + 1);
    result_ = result_ && FunctionDecl_2(builder_, level_ + 1);
    result_ = result_ && FunctionDecl_3(builder_, level_ + 1);
    result_ = result_ && FunctionDecl_4(builder_, level_ + 1);
    result_ = result_ && FunctionDecl_5(builder_, level_ + 1);
    result_ = result_ && identifier(builder_, level_ + 1);
    result_ = result_ && ParameterList(builder_, level_ + 1);
    result_ = result_ && FunctionDecl_8(builder_, level_ + 1);
    result_ = result_ && FunctionDecl_9(builder_, level_ + 1);
    result_ = result_ && FunctionDecl_10(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // AccessSpecifier?
  private static boolean FunctionDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl_0")) return false;
    AccessSpecifier(builder_, level_ + 1);
    return true;
  }

  // UFunctionDecl?
  private static boolean FunctionDecl_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl_1")) return false;
    UFunctionDecl(builder_, level_ + 1);
    return true;
  }

  // AccessSpecifier?
  private static boolean FunctionDecl_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl_2")) return false;
    AccessSpecifier(builder_, level_ + 1);
    return true;
  }

  // FunctionLocation?
  private static boolean FunctionDecl_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl_3")) return false;
    FunctionLocation(builder_, level_ + 1);
    return true;
  }

  // AccessSpecifier?
  private static boolean FunctionDecl_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl_4")) return false;
    AccessSpecifier(builder_, level_ + 1);
    return true;
  }

  // (TypeRef AND? | TILDE)?
  private static boolean FunctionDecl_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl_5")) return false;
    FunctionDecl_5_0(builder_, level_ + 1);
    return true;
  }

  // TypeRef AND? | TILDE
  private static boolean FunctionDecl_5_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl_5_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = FunctionDecl_5_0_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, TILDE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // TypeRef AND?
  private static boolean FunctionDecl_5_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl_5_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeRef(builder_, level_ + 1);
    result_ = result_ && FunctionDecl_5_0_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // AND?
  private static boolean FunctionDecl_5_0_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl_5_0_0_1")) return false;
    consumeToken(builder_, AND);
    return true;
  }

  // CONST?
  private static boolean FunctionDecl_8(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl_8")) return false;
    consumeToken(builder_, CONST);
    return true;
  }

  // FuncAttr?
  private static boolean FunctionDecl_9(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl_9")) return false;
    FuncAttr(builder_, level_ + 1);
    return true;
  }

  // SEMICOLON | CompoundStatement SEMICOLON?
  private static boolean FunctionDecl_10(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl_10")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEMICOLON);
    if (!result_) result_ = FunctionDecl_10_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // CompoundStatement SEMICOLON?
  private static boolean FunctionDecl_10_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl_10_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = CompoundStatement(builder_, level_ + 1);
    result_ = result_ && FunctionDecl_10_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // SEMICOLON?
  private static boolean FunctionDecl_10_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionDecl_10_1_1")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // (SHARED_KW | EXTERNAL_KW)+
  static boolean FunctionLocation(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionLocation")) return false;
    if (!nextTokenIs(builder_, "", EXTERNAL_KW, SHARED_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = FunctionLocation_0(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!FunctionLocation_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "FunctionLocation", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // SHARED_KW | EXTERNAL_KW
  private static boolean FunctionLocation_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "FunctionLocation_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, SHARED_KW);
    if (!result_) result_ = consumeToken(builder_, EXTERNAL_KW);
    return result_;
  }

  /* ********************************************************** */
  // PP_IF IfDefBranch (PP_ELIF IfDefBranch)* (PP_ELSE IfDefBranch)? PP_ENDIF
  public static boolean IfDefBlock(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IfDefBlock")) return false;
    if (!nextTokenIs(builder_, PP_IF)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IF_DEF_BLOCK, null);
    result_ = consumeToken(builder_, PP_IF);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, IfDefBranch(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, IfDefBlock_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, IfDefBlock_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, PP_ENDIF) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (PP_ELIF IfDefBranch)*
  private static boolean IfDefBlock_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IfDefBlock_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!IfDefBlock_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "IfDefBlock_2", pos_)) break;
    }
    return true;
  }

  // PP_ELIF IfDefBranch
  private static boolean IfDefBlock_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IfDefBlock_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PP_ELIF);
    result_ = result_ && IfDefBranch(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (PP_ELSE IfDefBranch)?
  private static boolean IfDefBlock_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IfDefBlock_3")) return false;
    IfDefBlock_3_0(builder_, level_ + 1);
    return true;
  }

  // PP_ELSE IfDefBranch
  private static boolean IfDefBlock_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IfDefBlock_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PP_ELSE);
    result_ = result_ && IfDefBranch(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // Item*
  public static boolean IfDefBranch(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IfDefBranch")) return false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IF_DEF_BRANCH, "<if def branch>");
    while (true) {
      int pos_ = current_position_(builder_);
      if (!Item(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "IfDefBranch", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // IF ParenthesizedExpr Statement ElseClause?
  public static boolean IfStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IfStatement")) return false;
    if (!nextTokenIs(builder_, IF)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IF);
    result_ = result_ && ParenthesizedExpr(builder_, level_ + 1);
    result_ = result_ && Statement(builder_, level_ + 1);
    result_ = result_ && IfStatement_3(builder_, level_ + 1);
    exit_section_(builder_, marker_, IF_STATEMENT, result_);
    return result_;
  }

  // ElseClause?
  private static boolean IfStatement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "IfStatement_3")) return false;
    ElseClause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // ExclusiveOrExpr (OR ExclusiveOrExpr)*
  public static boolean InclusiveOrExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InclusiveOrExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, INCLUSIVE_OR_EXPR, "<inclusive or expr>");
    result_ = ExclusiveOrExpr(builder_, level_ + 1);
    result_ = result_ && InclusiveOrExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (OR ExclusiveOrExpr)*
  private static boolean InclusiveOrExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InclusiveOrExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!InclusiveOrExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "InclusiveOrExpr_1", pos_)) break;
    }
    return true;
  }

  // OR ExclusiveOrExpr
  private static boolean InclusiveOrExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InclusiveOrExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, OR);
    result_ = result_ && ExclusiveOrExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // COLON TypeRef (COMMA TypeRef)*
  static boolean InheritanceList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InheritanceList")) return false;
    if (!nextTokenIs(builder_, COLON)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COLON);
    result_ = result_ && TypeRef(builder_, level_ + 1);
    result_ = result_ && InheritanceList_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA TypeRef)*
  private static boolean InheritanceList_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InheritanceList_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!InheritanceList_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "InheritanceList_2", pos_)) break;
    }
    return true;
  }

  // COMMA TypeRef
  private static boolean InheritanceList_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InheritanceList_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && TypeRef(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // [TypeRef EQ] LBRACE [ Expr (COMMA Expr)* ] RBRACE
  public static boolean InitListExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InitListExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INIT_LIST_EXPR, "<init list expr>");
    result_ = InitListExpr_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LBRACE);
    result_ = result_ && InitListExpr_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // [TypeRef EQ]
  private static boolean InitListExpr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InitListExpr_0")) return false;
    InitListExpr_0_0(builder_, level_ + 1);
    return true;
  }

  // TypeRef EQ
  private static boolean InitListExpr_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InitListExpr_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = TypeRef(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, EQ);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [ Expr (COMMA Expr)* ]
  private static boolean InitListExpr_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InitListExpr_2")) return false;
    InitListExpr_2_0(builder_, level_ + 1);
    return true;
  }

  // Expr (COMMA Expr)*
  private static boolean InitListExpr_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InitListExpr_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = Expr(builder_, level_ + 1);
    result_ = result_ && InitListExpr_2_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA Expr)*
  private static boolean InitListExpr_2_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InitListExpr_2_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!InitListExpr_2_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "InitListExpr_2_0_1", pos_)) break;
    }
    return true;
  }

  // COMMA Expr
  private static boolean InitListExpr_2_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InitListExpr_2_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && Expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // UInterfaceDecl? INTERFACE_KW identifier InheritanceList? LBRACE ClassMember_with_recover* RBRACE SEMICOLON?
  public static boolean InterfaceDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InterfaceDecl")) return false;
    if (!nextTokenIs(builder_, "<interface decl>", INTERFACE_KW, UINTERFACE_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, INTERFACE_DECL, "<interface decl>");
    result_ = InterfaceDecl_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, INTERFACE_KW);
    result_ = result_ && identifier(builder_, level_ + 1);
    result_ = result_ && InterfaceDecl_3(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LBRACE);
    result_ = result_ && InterfaceDecl_5(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    result_ = result_ && InterfaceDecl_7(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // UInterfaceDecl?
  private static boolean InterfaceDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InterfaceDecl_0")) return false;
    UInterfaceDecl(builder_, level_ + 1);
    return true;
  }

  // InheritanceList?
  private static boolean InterfaceDecl_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InterfaceDecl_3")) return false;
    InheritanceList(builder_, level_ + 1);
    return true;
  }

  // ClassMember_with_recover*
  private static boolean InterfaceDecl_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InterfaceDecl_5")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!ClassMember_with_recover(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "InterfaceDecl_5", pos_)) break;
    }
    return true;
  }

  // SEMICOLON?
  private static boolean InterfaceDecl_7(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "InterfaceDecl_7")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // IfDefBlock
  //                | Declaration
  //                | Statement
  //                | Comment
  //                | SEMICOLON
  static boolean Item(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Item")) return false;
    boolean result_;
    result_ = IfDefBlock(builder_, level_ + 1);
    if (!result_) result_ = Declaration(builder_, level_ + 1);
    if (!result_) result_ = Statement(builder_, level_ + 1);
    if (!result_) result_ = Comment(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, SEMICOLON);
    return result_;
  }

  /* ********************************************************** */
  // InclusiveOrExpr ((ANDAND | AND_KW) InclusiveOrExpr)*
  public static boolean LogicalAndExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalAndExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, LOGICAL_AND_EXPR, "<logical and expr>");
    result_ = InclusiveOrExpr(builder_, level_ + 1);
    result_ = result_ && LogicalAndExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ((ANDAND | AND_KW) InclusiveOrExpr)*
  private static boolean LogicalAndExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalAndExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!LogicalAndExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "LogicalAndExpr_1", pos_)) break;
    }
    return true;
  }

  // (ANDAND | AND_KW) InclusiveOrExpr
  private static boolean LogicalAndExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalAndExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = LogicalAndExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && InclusiveOrExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ANDAND | AND_KW
  private static boolean LogicalAndExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalAndExpr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, ANDAND);
    if (!result_) result_ = consumeToken(builder_, AND_KW);
    return result_;
  }

  /* ********************************************************** */
  // LogicalAndExpr ((OROR | OR_KW) LogicalAndExpr)*
  public static boolean LogicalOrExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalOrExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, LOGICAL_OR_EXPR, "<logical or expr>");
    result_ = LogicalAndExpr(builder_, level_ + 1);
    result_ = result_ && LogicalOrExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ((OROR | OR_KW) LogicalAndExpr)*
  private static boolean LogicalOrExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalOrExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!LogicalOrExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "LogicalOrExpr_1", pos_)) break;
    }
    return true;
  }

  // (OROR | OR_KW) LogicalAndExpr
  private static boolean LogicalOrExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalOrExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = LogicalOrExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && LogicalAndExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // OROR | OR_KW
  private static boolean LogicalOrExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "LogicalOrExpr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, OROR);
    if (!result_) result_ = consumeToken(builder_, OR_KW);
    return result_;
  }

  /* ********************************************************** */
  // identifier (EQ MetaArgValue)?
  static boolean MetaArg(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MetaArg")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = identifier(builder_, level_ + 1);
    result_ = result_ && MetaArg_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (EQ MetaArgValue)?
  private static boolean MetaArg_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MetaArg_1")) return false;
    MetaArg_1_0(builder_, level_ + 1);
    return true;
  }

  // EQ MetaArgValue
  private static boolean MetaArg_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MetaArg_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EQ);
    result_ = result_ && MetaArgValue(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LPAREN [ MetaArg (COMMA MetaArg)* COMMA? ] RPAREN
  static boolean MetaArgList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MetaArgList")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && MetaArgList_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [ MetaArg (COMMA MetaArg)* COMMA? ]
  private static boolean MetaArgList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MetaArgList_1")) return false;
    MetaArgList_1_0(builder_, level_ + 1);
    return true;
  }

  // MetaArg (COMMA MetaArg)* COMMA?
  private static boolean MetaArgList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MetaArgList_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = MetaArg(builder_, level_ + 1);
    result_ = result_ && MetaArgList_1_0_1(builder_, level_ + 1);
    result_ = result_ && MetaArgList_1_0_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA MetaArg)*
  private static boolean MetaArgList_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MetaArgList_1_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!MetaArgList_1_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "MetaArgList_1_0_1", pos_)) break;
    }
    return true;
  }

  // COMMA MetaArg
  private static boolean MetaArgList_1_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MetaArgList_1_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && MetaArg(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // COMMA?
  private static boolean MetaArgList_1_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MetaArgList_1_0_2")) return false;
    consumeToken(builder_, COMMA);
    return true;
  }

  /* ********************************************************** */
  // LPAREN [ MetaArg (COMMA MetaArg)* COMMA? ] RPAREN
  //                        | Expr
  static boolean MetaArgValue(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MetaArgValue")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = MetaArgValue_0(builder_, level_ + 1);
    if (!result_) result_ = Expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // LPAREN [ MetaArg (COMMA MetaArg)* COMMA? ] RPAREN
  private static boolean MetaArgValue_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MetaArgValue_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && MetaArgValue_0_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [ MetaArg (COMMA MetaArg)* COMMA? ]
  private static boolean MetaArgValue_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MetaArgValue_0_1")) return false;
    MetaArgValue_0_1_0(builder_, level_ + 1);
    return true;
  }

  // MetaArg (COMMA MetaArg)* COMMA?
  private static boolean MetaArgValue_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MetaArgValue_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = MetaArg(builder_, level_ + 1);
    result_ = result_ && MetaArgValue_0_1_0_1(builder_, level_ + 1);
    result_ = result_ && MetaArgValue_0_1_0_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA MetaArg)*
  private static boolean MetaArgValue_0_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MetaArgValue_0_1_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!MetaArgValue_0_1_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "MetaArgValue_0_1_0_1", pos_)) break;
    }
    return true;
  }

  // COMMA MetaArg
  private static boolean MetaArgValue_0_1_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MetaArgValue_0_1_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && MetaArg(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // COMMA?
  private static boolean MetaArgValue_0_1_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MetaArgValue_0_1_0_2")) return false;
    consumeToken(builder_, COMMA);
    return true;
  }

  /* ********************************************************** */
  // UFunctionDecl? MIXIN_KW FunctionDecl
  public static boolean MixinDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MixinDecl")) return false;
    if (!nextTokenIs(builder_, "<mixin decl>", MIXIN_KW, UFUNCTION_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MIXIN_DECL, "<mixin decl>");
    result_ = MixinDecl_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, MIXIN_KW);
    result_ = result_ && FunctionDecl(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // UFunctionDecl?
  private static boolean MixinDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MixinDecl_0")) return false;
    UFunctionDecl(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // UnaryExpr ((MUL | POWER | DIV | REM) UnaryExpr)*
  public static boolean MultiplyExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MultiplyExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, MULTIPLY_EXPR, "<multiply expr>");
    result_ = UnaryExpr(builder_, level_ + 1);
    result_ = result_ && MultiplyExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ((MUL | POWER | DIV | REM) UnaryExpr)*
  private static boolean MultiplyExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MultiplyExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!MultiplyExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "MultiplyExpr_1", pos_)) break;
    }
    return true;
  }

  // (MUL | POWER | DIV | REM) UnaryExpr
  private static boolean MultiplyExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MultiplyExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = MultiplyExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && UnaryExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // MUL | POWER | DIV | REM
  private static boolean MultiplyExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "MultiplyExpr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, MUL);
    if (!result_) result_ = consumeToken(builder_, POWER);
    if (!result_) result_ = consumeToken(builder_, DIV);
    if (!result_) result_ = consumeToken(builder_, REM);
    return result_;
  }

  /* ********************************************************** */
  // NAMESPACE_KW NamespacePath LBRACE NamespaceItem_with_recover* RBRACE SEMICOLON?
  public static boolean NamespaceDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NamespaceDecl")) return false;
    if (!nextTokenIs(builder_, NAMESPACE_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, NAMESPACE_DECL, null);
    result_ = consumeToken(builder_, NAMESPACE_KW);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, NamespacePath(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, LBRACE)) && result_;
    result_ = pinned_ && report_error_(builder_, NamespaceDecl_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, RBRACE)) && result_;
    result_ = pinned_ && NamespaceDecl_5(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // NamespaceItem_with_recover*
  private static boolean NamespaceDecl_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NamespaceDecl_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!NamespaceItem_with_recover(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "NamespaceDecl_3", pos_)) break;
    }
    return true;
  }

  // SEMICOLON?
  private static boolean NamespaceDecl_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NamespaceDecl_5")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // !(RBRACE | CLASS | STRUCT | ENUM | INTERFACE_KW | EVENT_KW | DELEGATE_KW | MIXIN_KW | ASSET_KW
  //                                   | TYPEDEF_KW | NAMESPACE_KW | UCLASS_KW | USTRUCT_KW | UENUM_KW | UINTERFACE_KW | UFUNCTION_KW | UPROPERTY_KW
  //                                   | DEFAULT_KW | IDENTIFIER | CONST | VOID_KW
  //                                   | INT_KW | INT8_KW | INT16_KW | INT32_KW | INT64_KW
  //                                   | UINT_KW | UINT8_KW | UINT16_KW | UINT32_KW | UINT64_KW
  //                                   | FLOAT_KW | DOUBLE_KW | BOOL_KW | AUTO_KW | AUTO
  //                                   | PP_IF | PP_ELIF | PP_ELSE | PP_ENDIF | PP_DEFINE | <<eof>>)
  static boolean NamespaceItem_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NamespaceItem_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !NamespaceItem_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // RBRACE | CLASS | STRUCT | ENUM | INTERFACE_KW | EVENT_KW | DELEGATE_KW | MIXIN_KW | ASSET_KW
  //                                   | TYPEDEF_KW | NAMESPACE_KW | UCLASS_KW | USTRUCT_KW | UENUM_KW | UINTERFACE_KW | UFUNCTION_KW | UPROPERTY_KW
  //                                   | DEFAULT_KW | IDENTIFIER | CONST | VOID_KW
  //                                   | INT_KW | INT8_KW | INT16_KW | INT32_KW | INT64_KW
  //                                   | UINT_KW | UINT8_KW | UINT16_KW | UINT32_KW | UINT64_KW
  //                                   | FLOAT_KW | DOUBLE_KW | BOOL_KW | AUTO_KW | AUTO
  //                                   | PP_IF | PP_ELIF | PP_ELSE | PP_ENDIF | PP_DEFINE | <<eof>>
  private static boolean NamespaceItem_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NamespaceItem_recover_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = consumeToken(builder_, CLASS);
    if (!result_) result_ = consumeToken(builder_, STRUCT);
    if (!result_) result_ = consumeToken(builder_, ENUM);
    if (!result_) result_ = consumeToken(builder_, INTERFACE_KW);
    if (!result_) result_ = consumeToken(builder_, EVENT_KW);
    if (!result_) result_ = consumeToken(builder_, DELEGATE_KW);
    if (!result_) result_ = consumeToken(builder_, MIXIN_KW);
    if (!result_) result_ = consumeToken(builder_, ASSET_KW);
    if (!result_) result_ = consumeToken(builder_, TYPEDEF_KW);
    if (!result_) result_ = consumeToken(builder_, NAMESPACE_KW);
    if (!result_) result_ = consumeToken(builder_, UCLASS_KW);
    if (!result_) result_ = consumeToken(builder_, USTRUCT_KW);
    if (!result_) result_ = consumeToken(builder_, UENUM_KW);
    if (!result_) result_ = consumeToken(builder_, UINTERFACE_KW);
    if (!result_) result_ = consumeToken(builder_, UFUNCTION_KW);
    if (!result_) result_ = consumeToken(builder_, UPROPERTY_KW);
    if (!result_) result_ = consumeToken(builder_, DEFAULT_KW);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = consumeToken(builder_, CONST);
    if (!result_) result_ = consumeToken(builder_, VOID_KW);
    if (!result_) result_ = consumeToken(builder_, INT_KW);
    if (!result_) result_ = consumeToken(builder_, INT8_KW);
    if (!result_) result_ = consumeToken(builder_, INT16_KW);
    if (!result_) result_ = consumeToken(builder_, INT32_KW);
    if (!result_) result_ = consumeToken(builder_, INT64_KW);
    if (!result_) result_ = consumeToken(builder_, UINT_KW);
    if (!result_) result_ = consumeToken(builder_, UINT8_KW);
    if (!result_) result_ = consumeToken(builder_, UINT16_KW);
    if (!result_) result_ = consumeToken(builder_, UINT32_KW);
    if (!result_) result_ = consumeToken(builder_, UINT64_KW);
    if (!result_) result_ = consumeToken(builder_, FLOAT_KW);
    if (!result_) result_ = consumeToken(builder_, DOUBLE_KW);
    if (!result_) result_ = consumeToken(builder_, BOOL_KW);
    if (!result_) result_ = consumeToken(builder_, AUTO_KW);
    if (!result_) result_ = consumeToken(builder_, AUTO);
    if (!result_) result_ = consumeToken(builder_, PP_IF);
    if (!result_) result_ = consumeToken(builder_, PP_ELIF);
    if (!result_) result_ = consumeToken(builder_, PP_ELSE);
    if (!result_) result_ = consumeToken(builder_, PP_ENDIF);
    if (!result_) result_ = consumeToken(builder_, PP_DEFINE);
    if (!result_) result_ = eof(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(RBRACE | <<eof>>) Item
  static boolean NamespaceItem_with_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NamespaceItem_with_recover")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = NamespaceItem_with_recover_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && Item(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, AngelscriptParser::NamespaceItem_recover);
    return result_ || pinned_;
  }

  // !(RBRACE | <<eof>>)
  private static boolean NamespaceItem_with_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NamespaceItem_with_recover_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !NamespaceItem_with_recover_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // RBRACE | <<eof>>
  private static boolean NamespaceItem_with_recover_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NamespaceItem_with_recover_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = eof(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier (COLONCOLON identifier)*
  static boolean NamespacePath(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NamespacePath")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = identifier(builder_, level_ + 1);
    result_ = result_ && NamespacePath_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COLONCOLON identifier)*
  private static boolean NamespacePath_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NamespacePath_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!NamespacePath_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "NamespacePath_1", pos_)) break;
    }
    return true;
  }

  // COLONCOLON identifier
  private static boolean NamespacePath_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NamespacePath_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COLONCOLON);
    result_ = result_ && identifier(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // CompoundStatement
  //                            | ExpressionStatement
  //                            | IfStatement
  //                            | SwitchStatement
  //                            | DoStatement
  //                            | WhileStatement
  //                            | ForStatement
  //                            | ForeachStatement
  //                            | ReturnStatement
  //                            | BreakStatement
  //                            | ContinueStatement
  //                            | VariableDecl
  static boolean NonCaseStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "NonCaseStatement")) return false;
    boolean result_;
    result_ = CompoundStatement(builder_, level_ + 1);
    if (!result_) result_ = ExpressionStatement(builder_, level_ + 1);
    if (!result_) result_ = IfStatement(builder_, level_ + 1);
    if (!result_) result_ = SwitchStatement(builder_, level_ + 1);
    if (!result_) result_ = DoStatement(builder_, level_ + 1);
    if (!result_) result_ = WhileStatement(builder_, level_ + 1);
    if (!result_) result_ = ForStatement(builder_, level_ + 1);
    if (!result_) result_ = ForeachStatement(builder_, level_ + 1);
    if (!result_) result_ = ReturnStatement(builder_, level_ + 1);
    if (!result_) result_ = BreakStatement(builder_, level_ + 1);
    if (!result_) result_ = ContinueStatement(builder_, level_ + 1);
    if (!result_) result_ = VariableDecl(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // PP_DEFINE
  public static boolean PP_DefineDirective(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PP_DefineDirective")) return false;
    if (!nextTokenIs(builder_, PP_DEFINE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PP_DEFINE);
    exit_section_(builder_, marker_, PP_DEFINE_DIRECTIVE, result_);
    return result_;
  }

  /* ********************************************************** */
  // DeclarationSpecifier identifier [ EQ ConditionalExpr ]
  public static boolean ParameterDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterDecl")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARAMETER_DECL, "<parameter decl>");
    result_ = DeclarationSpecifier(builder_, level_ + 1);
    result_ = result_ && identifier(builder_, level_ + 1);
    result_ = result_ && ParameterDecl_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // [ EQ ConditionalExpr ]
  private static boolean ParameterDecl_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterDecl_2")) return false;
    ParameterDecl_2_0(builder_, level_ + 1);
    return true;
  }

  // EQ ConditionalExpr
  private static boolean ParameterDecl_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterDecl_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EQ);
    result_ = result_ && ConditionalExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LPAREN [ VOID_KW | ParameterDecl (COMMA ParameterDecl)* COMMA? ] RPAREN
  public static boolean ParameterList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterList")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && ParameterList_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, PARAMETER_LIST, result_);
    return result_;
  }

  // [ VOID_KW | ParameterDecl (COMMA ParameterDecl)* COMMA? ]
  private static boolean ParameterList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterList_1")) return false;
    ParameterList_1_0(builder_, level_ + 1);
    return true;
  }

  // VOID_KW | ParameterDecl (COMMA ParameterDecl)* COMMA?
  private static boolean ParameterList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterList_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, VOID_KW);
    if (!result_) result_ = ParameterList_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ParameterDecl (COMMA ParameterDecl)* COMMA?
  private static boolean ParameterList_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterList_1_0_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ParameterDecl(builder_, level_ + 1);
    result_ = result_ && ParameterList_1_0_1_1(builder_, level_ + 1);
    result_ = result_ && ParameterList_1_0_1_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA ParameterDecl)*
  private static boolean ParameterList_1_0_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterList_1_0_1_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!ParameterList_1_0_1_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ParameterList_1_0_1_1", pos_)) break;
    }
    return true;
  }

  // COMMA ParameterDecl
  private static boolean ParameterList_1_0_1_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterList_1_0_1_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && ParameterDecl(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // COMMA?
  private static boolean ParameterList_1_0_1_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParameterList_1_0_1_2")) return false;
    consumeToken(builder_, COMMA);
    return true;
  }

  /* ********************************************************** */
  // LPAREN Expr RPAREN
  public static boolean ParenthesizedExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ParenthesizedExpr")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && Expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, PARENTHESIZED_EXPR, result_);
    return result_;
  }

  /* ********************************************************** */
  // PrimaryExpr PostfixPart* PostfixUpdate?
  public static boolean PostfixExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PostfixExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, POSTFIX_EXPR, "<postfix expr>");
    result_ = PrimaryExpr(builder_, level_ + 1);
    result_ = result_ && PostfixExpr_1(builder_, level_ + 1);
    result_ = result_ && PostfixExpr_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // PostfixPart*
  private static boolean PostfixExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PostfixExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!PostfixPart(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "PostfixExpr_1", pos_)) break;
    }
    return true;
  }

  // PostfixUpdate?
  private static boolean PostfixExpr_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PostfixExpr_2")) return false;
    PostfixUpdate(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // ScopeSuffix
  //                       | FieldSuffix
  //                       | SubscriptSuffix
  //                       | CallSuffix
  static boolean PostfixPart(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PostfixPart")) return false;
    boolean result_;
    result_ = ScopeSuffix(builder_, level_ + 1);
    if (!result_) result_ = FieldSuffix(builder_, level_ + 1);
    if (!result_) result_ = SubscriptSuffix(builder_, level_ + 1);
    if (!result_) result_ = CallSuffix(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // MINUSMINUS | PLUSPLUS
  static boolean PostfixUpdate(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PostfixUpdate")) return false;
    if (!nextTokenIs(builder_, "", MINUSMINUS, PLUSPLUS)) return false;
    boolean result_;
    result_ = consumeToken(builder_, MINUSMINUS);
    if (!result_) result_ = consumeToken(builder_, PLUSPLUS);
    return result_;
  }

  /* ********************************************************** */
  // AT | NOT_KW | EXCL | TILDE | MINUS | PLUS
  static boolean PrefixOp(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PrefixOp")) return false;
    boolean result_;
    result_ = consumeToken(builder_, AT);
    if (!result_) result_ = consumeToken(builder_, NOT_KW);
    if (!result_) result_ = consumeToken(builder_, EXCL);
    if (!result_) result_ = consumeToken(builder_, TILDE);
    if (!result_) result_ = consumeToken(builder_, MINUS);
    if (!result_) result_ = consumeToken(builder_, PLUS);
    return result_;
  }

  /* ********************************************************** */
  // (MINUSMINUS | PLUSPLUS) UnaryExpr
  static boolean PrefixUpdateExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PrefixUpdateExpr")) return false;
    if (!nextTokenIs(builder_, "", MINUSMINUS, PLUSPLUS)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = PrefixUpdateExpr_0(builder_, level_ + 1);
    result_ = result_ && UnaryExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // MINUSMINUS | PLUSPLUS
  private static boolean PrefixUpdateExpr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PrefixUpdateExpr_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, MINUSMINUS);
    if (!result_) result_ = consumeToken(builder_, PLUSPLUS);
    return result_;
  }

  /* ********************************************************** */
  // VariableAccessExpr
  //               | InitListExpr
  //               | StringConcatExpr
  //               | FStringExpr
  //               | number_literal
  //               | string_literal
  //               | bool_literal
  //               | null_literal
  //               | ParenthesizedExpr
  public static boolean PrimaryExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PrimaryExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, PRIMARY_EXPR, "<primary expr>");
    result_ = VariableAccessExpr(builder_, level_ + 1);
    if (!result_) result_ = InitListExpr(builder_, level_ + 1);
    if (!result_) result_ = StringConcatExpr(builder_, level_ + 1);
    if (!result_) result_ = FStringExpr(builder_, level_ + 1);
    if (!result_) result_ = number_literal(builder_, level_ + 1);
    if (!result_) result_ = string_literal(builder_, level_ + 1);
    if (!result_) result_ = bool_literal(builder_, level_ + 1);
    if (!result_) result_ = null_literal(builder_, level_ + 1);
    if (!result_) result_ = ParenthesizedExpr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // VOID_KW
  //                         | INT_KW | INT8_KW | INT16_KW | INT32_KW | INT64_KW
  //                         | UINT_KW | UINT8_KW | UINT16_KW | UINT32_KW | UINT64_KW
  //                         | FLOAT_KW | DOUBLE_KW | BOOL_KW
  static boolean PrimitiveType(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "PrimitiveType")) return false;
    boolean result_;
    result_ = consumeToken(builder_, VOID_KW);
    if (!result_) result_ = consumeToken(builder_, INT_KW);
    if (!result_) result_ = consumeToken(builder_, INT8_KW);
    if (!result_) result_ = consumeToken(builder_, INT16_KW);
    if (!result_) result_ = consumeToken(builder_, INT32_KW);
    if (!result_) result_ = consumeToken(builder_, INT64_KW);
    if (!result_) result_ = consumeToken(builder_, UINT_KW);
    if (!result_) result_ = consumeToken(builder_, UINT8_KW);
    if (!result_) result_ = consumeToken(builder_, UINT16_KW);
    if (!result_) result_ = consumeToken(builder_, UINT32_KW);
    if (!result_) result_ = consumeToken(builder_, UINT64_KW);
    if (!result_) result_ = consumeToken(builder_, FLOAT_KW);
    if (!result_) result_ = consumeToken(builder_, DOUBLE_KW);
    if (!result_) result_ = consumeToken(builder_, BOOL_KW);
    return result_;
  }

  /* ********************************************************** */
  // ShiftExpr ((GT | GTEQ | LTEQ | LT | IS_KW | EXCL IS_KW) ShiftExpr)*
  public static boolean RelationalExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RelationalExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, RELATIONAL_EXPR, "<relational expr>");
    result_ = ShiftExpr(builder_, level_ + 1);
    result_ = result_ && RelationalExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ((GT | GTEQ | LTEQ | LT | IS_KW | EXCL IS_KW) ShiftExpr)*
  private static boolean RelationalExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RelationalExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!RelationalExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "RelationalExpr_1", pos_)) break;
    }
    return true;
  }

  // (GT | GTEQ | LTEQ | LT | IS_KW | EXCL IS_KW) ShiftExpr
  private static boolean RelationalExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RelationalExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = RelationalExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && ShiftExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // GT | GTEQ | LTEQ | LT | IS_KW | EXCL IS_KW
  private static boolean RelationalExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "RelationalExpr_1_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, GT);
    if (!result_) result_ = consumeToken(builder_, GTEQ);
    if (!result_) result_ = consumeToken(builder_, LTEQ);
    if (!result_) result_ = consumeToken(builder_, LT);
    if (!result_) result_ = consumeToken(builder_, IS_KW);
    if (!result_) result_ = parseTokens(builder_, 0, EXCL, IS_KW);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // RETURN Expr? SEMICOLON
  public static boolean ReturnStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ReturnStatement")) return false;
    if (!nextTokenIs(builder_, RETURN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RETURN);
    result_ = result_ && ReturnStatement_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, RETURN_STATEMENT, result_);
    return result_;
  }

  // Expr?
  private static boolean ReturnStatement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ReturnStatement_1")) return false;
    Expr(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // (identifier TypeArgument? COLONCOLON)+ | COLONCOLON
  public static boolean ScopeRef(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ScopeRef")) return false;
    if (!nextTokenIs(builder_, "<scope ref>", COLONCOLON, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SCOPE_REF, "<scope ref>");
    result_ = ScopeRef_0(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, COLONCOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (identifier TypeArgument? COLONCOLON)+
  private static boolean ScopeRef_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ScopeRef_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ScopeRef_0_0(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!ScopeRef_0_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ScopeRef_0", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // identifier TypeArgument? COLONCOLON
  private static boolean ScopeRef_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ScopeRef_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = identifier(builder_, level_ + 1);
    result_ = result_ && ScopeRef_0_0_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLONCOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // TypeArgument?
  private static boolean ScopeRef_0_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ScopeRef_0_0_1")) return false;
    TypeArgument(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // COLONCOLON identifier
  static boolean ScopeSuffix(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ScopeSuffix")) return false;
    if (!nextTokenIs(builder_, COLONCOLON)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COLONCOLON);
    result_ = result_ && identifier(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ScriptItem*
  static boolean Script(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Script")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!ScriptItem(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "Script", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // !(<<eof>>) Item
  static boolean ScriptItem(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ScriptItem")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = ScriptItem_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && Item(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, AngelscriptParser::ScriptItem_recover);
    return result_ || pinned_;
  }

  // !(<<eof>>)
  private static boolean ScriptItem_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ScriptItem_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !ScriptItem_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // <<eof>>
  private static boolean ScriptItem_0_0(PsiBuilder builder_, int level_) {
    return eof(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // !(CLASS | STRUCT | ENUM | INTERFACE_KW | EVENT_KW | DELEGATE_KW | MIXIN_KW | ASSET_KW
  //                                | TYPEDEF_KW | NAMESPACE_KW | UCLASS_KW | USTRUCT_KW | UENUM_KW | UINTERFACE_KW | UFUNCTION_KW | UPROPERTY_KW
  //                                | DEFAULT_KW | IDENTIFIER | CONST | VOID_KW
  //                                | INT_KW | INT8_KW | INT16_KW | INT32_KW | INT64_KW
  //                                | UINT_KW | UINT8_KW | UINT16_KW | UINT32_KW | UINT64_KW
  //                                | FLOAT_KW | DOUBLE_KW | BOOL_KW | AUTO_KW | AUTO
  //                                | PP_IF | PP_ELIF | PP_ELSE | PP_ENDIF | PP_DEFINE | <<eof>>)
  static boolean ScriptItem_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ScriptItem_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !ScriptItem_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // CLASS | STRUCT | ENUM | INTERFACE_KW | EVENT_KW | DELEGATE_KW | MIXIN_KW | ASSET_KW
  //                                | TYPEDEF_KW | NAMESPACE_KW | UCLASS_KW | USTRUCT_KW | UENUM_KW | UINTERFACE_KW | UFUNCTION_KW | UPROPERTY_KW
  //                                | DEFAULT_KW | IDENTIFIER | CONST | VOID_KW
  //                                | INT_KW | INT8_KW | INT16_KW | INT32_KW | INT64_KW
  //                                | UINT_KW | UINT8_KW | UINT16_KW | UINT32_KW | UINT64_KW
  //                                | FLOAT_KW | DOUBLE_KW | BOOL_KW | AUTO_KW | AUTO
  //                                | PP_IF | PP_ELIF | PP_ELSE | PP_ENDIF | PP_DEFINE | <<eof>>
  private static boolean ScriptItem_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ScriptItem_recover_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CLASS);
    if (!result_) result_ = consumeToken(builder_, STRUCT);
    if (!result_) result_ = consumeToken(builder_, ENUM);
    if (!result_) result_ = consumeToken(builder_, INTERFACE_KW);
    if (!result_) result_ = consumeToken(builder_, EVENT_KW);
    if (!result_) result_ = consumeToken(builder_, DELEGATE_KW);
    if (!result_) result_ = consumeToken(builder_, MIXIN_KW);
    if (!result_) result_ = consumeToken(builder_, ASSET_KW);
    if (!result_) result_ = consumeToken(builder_, TYPEDEF_KW);
    if (!result_) result_ = consumeToken(builder_, NAMESPACE_KW);
    if (!result_) result_ = consumeToken(builder_, UCLASS_KW);
    if (!result_) result_ = consumeToken(builder_, USTRUCT_KW);
    if (!result_) result_ = consumeToken(builder_, UENUM_KW);
    if (!result_) result_ = consumeToken(builder_, UINTERFACE_KW);
    if (!result_) result_ = consumeToken(builder_, UFUNCTION_KW);
    if (!result_) result_ = consumeToken(builder_, UPROPERTY_KW);
    if (!result_) result_ = consumeToken(builder_, DEFAULT_KW);
    if (!result_) result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = consumeToken(builder_, CONST);
    if (!result_) result_ = consumeToken(builder_, VOID_KW);
    if (!result_) result_ = consumeToken(builder_, INT_KW);
    if (!result_) result_ = consumeToken(builder_, INT8_KW);
    if (!result_) result_ = consumeToken(builder_, INT16_KW);
    if (!result_) result_ = consumeToken(builder_, INT32_KW);
    if (!result_) result_ = consumeToken(builder_, INT64_KW);
    if (!result_) result_ = consumeToken(builder_, UINT_KW);
    if (!result_) result_ = consumeToken(builder_, UINT8_KW);
    if (!result_) result_ = consumeToken(builder_, UINT16_KW);
    if (!result_) result_ = consumeToken(builder_, UINT32_KW);
    if (!result_) result_ = consumeToken(builder_, UINT64_KW);
    if (!result_) result_ = consumeToken(builder_, FLOAT_KW);
    if (!result_) result_ = consumeToken(builder_, DOUBLE_KW);
    if (!result_) result_ = consumeToken(builder_, BOOL_KW);
    if (!result_) result_ = consumeToken(builder_, AUTO_KW);
    if (!result_) result_ = consumeToken(builder_, AUTO);
    if (!result_) result_ = consumeToken(builder_, PP_IF);
    if (!result_) result_ = consumeToken(builder_, PP_ELIF);
    if (!result_) result_ = consumeToken(builder_, PP_ELSE);
    if (!result_) result_ = consumeToken(builder_, PP_ENDIF);
    if (!result_) result_ = consumeToken(builder_, PP_DEFINE);
    if (!result_) result_ = eof(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // AddExpr ((LTLT | GT GT) AddExpr)*
  public static boolean ShiftExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ShiftExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, SHIFT_EXPR, "<shift expr>");
    result_ = AddExpr(builder_, level_ + 1);
    result_ = result_ && ShiftExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ((LTLT | GT GT) AddExpr)*
  private static boolean ShiftExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ShiftExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!ShiftExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ShiftExpr_1", pos_)) break;
    }
    return true;
  }

  // (LTLT | GT GT) AddExpr
  private static boolean ShiftExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ShiftExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = ShiftExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && AddExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // LTLT | GT GT
  private static boolean ShiftExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ShiftExpr_1_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LTLT);
    if (!result_) result_ = parseTokens(builder_, 0, GT, GT);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // CaseStatement | NonCaseStatement
  public static boolean Statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "Statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STATEMENT, "<statement>");
    result_ = CaseStatement(builder_, level_ + 1);
    if (!result_) result_ = NonCaseStatement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // string_literal string_literal+
  public static boolean StringConcatExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StringConcatExpr")) return false;
    if (!nextTokenIs(builder_, STRING_LITERAL)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = string_literal(builder_, level_ + 1);
    result_ = result_ && StringConcatExpr_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, STRING_CONCAT_EXPR, result_);
    return result_;
  }

  // string_literal+
  private static boolean StringConcatExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StringConcatExpr_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = string_literal(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!string_literal(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "StringConcatExpr_1", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // UStructDecl? STRUCT identifier LBRACE StructMember_with_recover* RBRACE SEMICOLON?
  public static boolean StructDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StructDecl")) return false;
    if (!nextTokenIs(builder_, "<struct decl>", STRUCT, USTRUCT_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STRUCT_DECL, "<struct decl>");
    result_ = StructDecl_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, STRUCT);
    result_ = result_ && identifier(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LBRACE);
    result_ = result_ && StructDecl_4(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    result_ = result_ && StructDecl_6(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // UStructDecl?
  private static boolean StructDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StructDecl_0")) return false;
    UStructDecl(builder_, level_ + 1);
    return true;
  }

  // StructMember_with_recover*
  private static boolean StructDecl_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StructDecl_4")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!StructMember_with_recover(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "StructDecl_4", pos_)) break;
    }
    return true;
  }

  // SEMICOLON?
  private static boolean StructDecl_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StructDecl_6")) return false;
    consumeToken(builder_, SEMICOLON);
    return true;
  }

  /* ********************************************************** */
  // IfDefBlock | ConstructorDecl | FunctionDecl | DefaultValueDecl | VariableDecl | Comment
  static boolean StructMember(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StructMember")) return false;
    boolean result_;
    result_ = IfDefBlock(builder_, level_ + 1);
    if (!result_) result_ = ConstructorDecl(builder_, level_ + 1);
    if (!result_) result_ = FunctionDecl(builder_, level_ + 1);
    if (!result_) result_ = DefaultValueDecl(builder_, level_ + 1);
    if (!result_) result_ = VariableDecl(builder_, level_ + 1);
    if (!result_) result_ = Comment(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // !(RBRACE | <<eof>>) StructMember
  static boolean StructMember_with_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StructMember_with_recover")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = StructMember_with_recover_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && StructMember(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, AngelscriptParser::ClassMember_recover);
    return result_ || pinned_;
  }

  // !(RBRACE | <<eof>>)
  private static boolean StructMember_with_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StructMember_with_recover_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !StructMember_with_recover_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // RBRACE | <<eof>>
  private static boolean StructMember_with_recover_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "StructMember_with_recover_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, RBRACE);
    if (!result_) result_ = eof(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LBRACK Expr RBRACK
  static boolean SubscriptSuffix(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SubscriptSuffix")) return false;
    if (!nextTokenIs(builder_, LBRACK)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LBRACK);
    result_ = result_ && Expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // SWITCH_KW ParenthesizedExpr CompoundStatement
  public static boolean SwitchStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "SwitchStatement")) return false;
    if (!nextTokenIs(builder_, SWITCH_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SWITCH_KW);
    result_ = result_ && ParenthesizedExpr(builder_, level_ + 1);
    result_ = result_ && CompoundStatement(builder_, level_ + 1);
    exit_section_(builder_, marker_, SWITCH_STATEMENT, result_);
    return result_;
  }

  /* ********************************************************** */
  // LT TypeRef (COMMA TypeRef)* GT
  public static boolean TypeArgument(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeArgument")) return false;
    if (!nextTokenIs(builder_, LT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LT);
    result_ = result_ && TypeRef(builder_, level_ + 1);
    result_ = result_ && TypeArgument_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, GT);
    exit_section_(builder_, marker_, TYPE_ARGUMENT, result_);
    return result_;
  }

  // (COMMA TypeRef)*
  private static boolean TypeArgument_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeArgument_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!TypeArgument_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "TypeArgument_2", pos_)) break;
    }
    return true;
  }

  // COMMA TypeRef
  private static boolean TypeArgument_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeArgument_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && TypeRef(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // AND (IN | OUT | INOUT_KW)?
  static boolean TypeMod(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeMod")) return false;
    if (!nextTokenIs(builder_, AND)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, AND);
    result_ = result_ && TypeMod_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (IN | OUT | INOUT_KW)?
  private static boolean TypeMod_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeMod_1")) return false;
    TypeMod_1_0(builder_, level_ + 1);
    return true;
  }

  // IN | OUT | INOUT_KW
  private static boolean TypeMod_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeMod_1_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, IN);
    if (!result_) result_ = consumeToken(builder_, OUT);
    if (!result_) result_ = consumeToken(builder_, INOUT_KW);
    return result_;
  }

  /* ********************************************************** */
  // CONST?
  //             ScopeRef?
  //             DataType
  //             TypeArgument*
  //             ( LBRACK RBRACK | AT CONST? )*
  public static boolean TypeRef(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeRef")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TYPE_REF, "<type ref>");
    result_ = TypeRef_0(builder_, level_ + 1);
    result_ = result_ && TypeRef_1(builder_, level_ + 1);
    result_ = result_ && DataType(builder_, level_ + 1);
    result_ = result_ && TypeRef_3(builder_, level_ + 1);
    result_ = result_ && TypeRef_4(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // CONST?
  private static boolean TypeRef_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeRef_0")) return false;
    consumeToken(builder_, CONST);
    return true;
  }

  // ScopeRef?
  private static boolean TypeRef_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeRef_1")) return false;
    ScopeRef(builder_, level_ + 1);
    return true;
  }

  // TypeArgument*
  private static boolean TypeRef_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeRef_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!TypeArgument(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "TypeRef_3", pos_)) break;
    }
    return true;
  }

  // ( LBRACK RBRACK | AT CONST? )*
  private static boolean TypeRef_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeRef_4")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!TypeRef_4_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "TypeRef_4", pos_)) break;
    }
    return true;
  }

  // LBRACK RBRACK | AT CONST?
  private static boolean TypeRef_4_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeRef_4_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = parseTokens(builder_, 0, LBRACK, RBRACK);
    if (!result_) result_ = TypeRef_4_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // AT CONST?
  private static boolean TypeRef_4_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeRef_4_0_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, AT);
    result_ = result_ && TypeRef_4_0_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // CONST?
  private static boolean TypeRef_4_0_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypeRef_4_0_1_1")) return false;
    consumeToken(builder_, CONST);
    return true;
  }

  /* ********************************************************** */
  // TYPEDEF_KW PrimitiveType identifier SEMICOLON
  public static boolean TypedefDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "TypedefDecl")) return false;
    if (!nextTokenIs(builder_, TYPEDEF_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, TYPEDEF_KW);
    result_ = result_ && PrimitiveType(builder_, level_ + 1);
    result_ = result_ && identifier(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, marker_, TYPEDEF_DECL, result_);
    return result_;
  }

  /* ********************************************************** */
  // UCLASS_KW    MetaArgList
  public static boolean UClassDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UClassDecl")) return false;
    if (!nextTokenIs(builder_, UCLASS_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, UCLASS_KW);
    result_ = result_ && MetaArgList(builder_, level_ + 1);
    exit_section_(builder_, marker_, U_CLASS_DECL, result_);
    return result_;
  }

  /* ********************************************************** */
  // UENUM_KW     MetaArgList
  public static boolean UEnumDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UEnumDecl")) return false;
    if (!nextTokenIs(builder_, UENUM_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, UENUM_KW);
    result_ = result_ && MetaArgList(builder_, level_ + 1);
    exit_section_(builder_, marker_, U_ENUM_DECL, result_);
    return result_;
  }

  /* ********************************************************** */
  // UFUNCTION_KW MetaArgList
  public static boolean UFunctionDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UFunctionDecl")) return false;
    if (!nextTokenIs(builder_, UFUNCTION_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, UFUNCTION_KW);
    result_ = result_ && MetaArgList(builder_, level_ + 1);
    exit_section_(builder_, marker_, U_FUNCTION_DECL, result_);
    return result_;
  }

  /* ********************************************************** */
  // UINTERFACE_KW MetaArgList
  public static boolean UInterfaceDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UInterfaceDecl")) return false;
    if (!nextTokenIs(builder_, UINTERFACE_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, UINTERFACE_KW);
    result_ = result_ && MetaArgList(builder_, level_ + 1);
    exit_section_(builder_, marker_, U_INTERFACE_DECL, result_);
    return result_;
  }

  /* ********************************************************** */
  // UPROPERTY_KW MetaArgList
  public static boolean UPropertyDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UPropertyDecl")) return false;
    if (!nextTokenIs(builder_, UPROPERTY_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, UPROPERTY_KW);
    result_ = result_ && MetaArgList(builder_, level_ + 1);
    exit_section_(builder_, marker_, U_PROPERTY_DECL, result_);
    return result_;
  }

  /* ********************************************************** */
  // USTRUCT_KW   MetaArgList
  public static boolean UStructDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UStructDecl")) return false;
    if (!nextTokenIs(builder_, USTRUCT_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, USTRUCT_KW);
    result_ = result_ && MetaArgList(builder_, level_ + 1);
    exit_section_(builder_, marker_, U_STRUCT_DECL, result_);
    return result_;
  }

  /* ********************************************************** */
  // PrefixOp UnaryExpr
  //             | PrefixUpdateExpr
  //             | CastExpr
  //             | PostfixExpr
  public static boolean UnaryExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UnaryExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, UNARY_EXPR, "<unary expr>");
    result_ = UnaryExpr_0(builder_, level_ + 1);
    if (!result_) result_ = PrefixUpdateExpr(builder_, level_ + 1);
    if (!result_) result_ = CastExpr(builder_, level_ + 1);
    if (!result_) result_ = PostfixExpr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // PrefixOp UnaryExpr
  private static boolean UnaryExpr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "UnaryExpr_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = PrefixOp(builder_, level_ + 1);
    result_ = result_ && UnaryExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ScopeRef? identifier
  public static boolean VariableAccessExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VariableAccessExpr")) return false;
    if (!nextTokenIs(builder_, "<variable access expr>", COLONCOLON, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, VARIABLE_ACCESS_EXPR, "<variable access expr>");
    result_ = VariableAccessExpr_0(builder_, level_ + 1);
    result_ = result_ && identifier(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ScopeRef?
  private static boolean VariableAccessExpr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VariableAccessExpr_0")) return false;
    ScopeRef(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // AccessSpecifier?
  //                  UPropertyDecl?
  //                  AccessSpecifier?
  //                  TypeRef
  //                  TypeMod?
  //                  VariableItem (COMMA VariableItem)*
  //                  SEMICOLON
  public static boolean VariableDecl(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VariableDecl")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, VARIABLE_DECL, "<variable decl>");
    result_ = VariableDecl_0(builder_, level_ + 1);
    result_ = result_ && VariableDecl_1(builder_, level_ + 1);
    result_ = result_ && VariableDecl_2(builder_, level_ + 1);
    result_ = result_ && TypeRef(builder_, level_ + 1);
    result_ = result_ && VariableDecl_4(builder_, level_ + 1);
    result_ = result_ && VariableItem(builder_, level_ + 1);
    result_ = result_ && VariableDecl_6(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // AccessSpecifier?
  private static boolean VariableDecl_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VariableDecl_0")) return false;
    AccessSpecifier(builder_, level_ + 1);
    return true;
  }

  // UPropertyDecl?
  private static boolean VariableDecl_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VariableDecl_1")) return false;
    UPropertyDecl(builder_, level_ + 1);
    return true;
  }

  // AccessSpecifier?
  private static boolean VariableDecl_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VariableDecl_2")) return false;
    AccessSpecifier(builder_, level_ + 1);
    return true;
  }

  // TypeMod?
  private static boolean VariableDecl_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VariableDecl_4")) return false;
    TypeMod(builder_, level_ + 1);
    return true;
  }

  // (COMMA VariableItem)*
  private static boolean VariableDecl_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VariableDecl_6")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!VariableDecl_6_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "VariableDecl_6", pos_)) break;
    }
    return true;
  }

  // COMMA VariableItem
  private static boolean VariableDecl_6_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VariableDecl_6_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && VariableItem(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // identifier ( EQ Expr | ArgList )?
  static boolean VariableItem(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VariableItem")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = identifier(builder_, level_ + 1);
    result_ = result_ && VariableItem_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ( EQ Expr | ArgList )?
  private static boolean VariableItem_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VariableItem_1")) return false;
    VariableItem_1_0(builder_, level_ + 1);
    return true;
  }

  // EQ Expr | ArgList
  private static boolean VariableItem_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VariableItem_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = VariableItem_1_0_0(builder_, level_ + 1);
    if (!result_) result_ = ArgList(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // EQ Expr
  private static boolean VariableItem_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "VariableItem_1_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, EQ);
    result_ = result_ && Expr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // WHILE ParenthesizedExpr Statement
  public static boolean WhileStatement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "WhileStatement")) return false;
    if (!nextTokenIs(builder_, WHILE)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, WHILE);
    result_ = result_ && ParenthesizedExpr(builder_, level_ + 1);
    result_ = result_ && Statement(builder_, level_ + 1);
    exit_section_(builder_, marker_, WHILE_STATEMENT, result_);
    return result_;
  }

  /* ********************************************************** */
  // BOOL_LITERAL
  static boolean bool_literal(PsiBuilder builder_, int level_) {
    return consumeToken(builder_, BOOL_LITERAL);
  }

  /* ********************************************************** */
  // FSTRING_TEXT | FStringInterp
  static boolean fstring_content(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "fstring_content")) return false;
    if (!nextTokenIs(builder_, "", FSTRING_LBRACE, FSTRING_TEXT)) return false;
    boolean result_;
    result_ = consumeToken(builder_, FSTRING_TEXT);
    if (!result_) result_ = FStringInterp(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // IDENTIFIER
  static boolean identifier(PsiBuilder builder_, int level_) {
    return consumeToken(builder_, IDENTIFIER);
  }

  /* ********************************************************** */
  // NULLPTR_KW
  static boolean null_literal(PsiBuilder builder_, int level_) {
    return consumeToken(builder_, NULLPTR_KW);
  }

  /* ********************************************************** */
  // NUMBER_LITERAL
  static boolean number_literal(PsiBuilder builder_, int level_) {
    return consumeToken(builder_, NUMBER_LITERAL);
  }

  /* ********************************************************** */
  // STRING_LITERAL
  static boolean string_literal(PsiBuilder builder_, int level_) {
    return consumeToken(builder_, STRING_LITERAL);
  }

}
