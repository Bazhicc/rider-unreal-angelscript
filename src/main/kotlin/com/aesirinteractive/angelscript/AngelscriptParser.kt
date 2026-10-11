// Native Kotlin PSI; preserves the complete grammar and tree layout of the pre-migration parser.
package com.aesirinteractive.angelscript

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.lang.LightPsiParser
import com.intellij.lang.parser.GeneratedParserUtilBase.*
import com.intellij.psi.tree.IElementType

/** Recursive descent parser with full precedence, pinned sections and recovery rules. */
class AngelscriptParser : PsiParser, LightPsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        parseLight(root, builder)
        return builder.treeBuilt
    }

    override fun parseLight(root: IElementType, originalBuilder: PsiBuilder) {
        val builder = adapt_builder_(root, originalBuilder, this, EXTENDS_SETS)
        val marker = enter_section_(builder, 0, _COLLAPSE_, null)
        val result = Script(builder, 1)
        exit_section_(builder, 0, marker, root, result, true, TRUE_CONDITION)
    }

    companion object {
        private val EXTENDS_SETS = arrayOf(
    create_token_set_(AngelscriptTypes.ADD_EXPR, AngelscriptTypes.ASSIGNMENT_EXPR, AngelscriptTypes.BITWISE_AND_EXPR, AngelscriptTypes.CAST_EXPR,
      AngelscriptTypes.COMMA_EXPR, AngelscriptTypes.CONDITIONAL_EXPR, AngelscriptTypes.EQUALITY_EXPR, AngelscriptTypes.EXCLUSIVE_OR_EXPR,
      AngelscriptTypes.EXPR, AngelscriptTypes.F_STRING_EXPR, AngelscriptTypes.INCLUSIVE_OR_EXPR, AngelscriptTypes.INIT_LIST_EXPR,
      AngelscriptTypes.LOGICAL_AND_EXPR, AngelscriptTypes.LOGICAL_OR_EXPR, AngelscriptTypes.MULTIPLY_EXPR, AngelscriptTypes.PARENTHESIZED_EXPR,
      AngelscriptTypes.POSTFIX_EXPR, AngelscriptTypes.PRIMARY_EXPR, AngelscriptTypes.RELATIONAL_EXPR, AngelscriptTypes.SHIFT_EXPR,
      AngelscriptTypes.STRING_CONCAT_EXPR, AngelscriptTypes.UNARY_EXPR, AngelscriptTypes.VARIABLE_ACCESS_EXPR),
        )
      /* ********************************************************** */
      // AngelscriptTypes.PRIVATE_KW | AngelscriptTypes.PROTECTED_KW
      private fun AccessSpecifier(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "AccessSpecifier")) return false
        if (!nextTokenIs(builder_, "", AngelscriptTypes.PRIVATE_KW, AngelscriptTypes.PROTECTED_KW)) return false
        var result_: Boolean
        result_ = consumeToken(builder_, AngelscriptTypes.PRIVATE_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PROTECTED_KW)
        return result_
      }

      /* ********************************************************** */
      // MultiplyExpr ((AngelscriptTypes.PLUS | AngelscriptTypes.MINUS) MultiplyExpr)*
      private fun AddExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "AddExpr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _COLLAPSE_, AngelscriptTypes.ADD_EXPR, "<add expr>")
        result_ = MultiplyExpr(builder_, level_ + 1)
        result_ = result_ && AddExpr_1(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // ((AngelscriptTypes.PLUS | AngelscriptTypes.MINUS) MultiplyExpr)*
      private fun AddExpr_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "AddExpr_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!AddExpr_1_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "AddExpr_1", pos_)) break
        }
        return true
      }

      // (AngelscriptTypes.PLUS | AngelscriptTypes.MINUS) MultiplyExpr
      private fun AddExpr_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "AddExpr_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = AddExpr_1_0_0(builder_, level_ + 1)
        result_ = result_ && MultiplyExpr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.PLUS | AngelscriptTypes.MINUS
      private fun AddExpr_1_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "AddExpr_1_0_0")) return false
        var result_: Boolean
        result_ = consumeToken(builder_, AngelscriptTypes.PLUS)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.MINUS)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.LPAREN [ Argument (AngelscriptTypes.COMMA Argument)* ] AngelscriptTypes.RPAREN
      private fun ArgList(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ArgList")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.LPAREN)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.LPAREN)
        result_ = result_ && ArgList_1(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.RPAREN)
        exit_section_(builder_, marker_, AngelscriptTypes.ARG_LIST, result_)
        return result_
      }

      // [ Argument (AngelscriptTypes.COMMA Argument)* ]
      private fun ArgList_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ArgList_1")) return false
        ArgList_1_0(builder_, level_ + 1)
        return true
      }

      // Argument (AngelscriptTypes.COMMA Argument)*
      private fun ArgList_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ArgList_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = Argument(builder_, level_ + 1)
        result_ = result_ && ArgList_1_0_1(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // (AngelscriptTypes.COMMA Argument)*
      private fun ArgList_1_0_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ArgList_1_0_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!ArgList_1_0_1_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "ArgList_1_0_1", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.COMMA Argument
      private fun ArgList_1_0_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ArgList_1_0_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.COMMA)
        result_ = result_ && Argument(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // [ identifier AngelscriptTypes.COLON ] AssignmentExpr
      private fun Argument(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "Argument")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.ARGUMENT, "<argument>")
        result_ = Argument_0(builder_, level_ + 1)
        result_ = result_ && AssignmentExpr(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // [ identifier AngelscriptTypes.COLON ]
      private fun Argument_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "Argument_0")) return false
        Argument_0_0(builder_, level_ + 1)
        return true
      }

      // identifier AngelscriptTypes.COLON
      private fun Argument_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "Argument_0_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = identifier(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.COLON)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.ASSET_KW identifier AngelscriptTypes.OF_KW TypeRef CompoundStatement AngelscriptTypes.SEMICOLON?
      private fun AssetDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "AssetDecl")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.ASSET_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.ASSET_KW)
        result_ = result_ && identifier(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.OF_KW)
        result_ = result_ && TypeRef(builder_, level_ + 1)
        result_ = result_ && CompoundStatement(builder_, level_ + 1)
        result_ = result_ && AssetDecl_5(builder_, level_ + 1)
        exit_section_(builder_, marker_, AngelscriptTypes.ASSET_DECL, result_)
        return result_
      }

      // AngelscriptTypes.SEMICOLON?
      private fun AssetDecl_5(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "AssetDecl_5")) return false
        consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        return true
      }

      /* ********************************************************** */
      // ConditionalExpr (AssignmentOp AssignmentExpr)?
      private fun AssignmentExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "AssignmentExpr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _COLLAPSE_, AngelscriptTypes.ASSIGNMENT_EXPR, "<assignment expr>")
        result_ = ConditionalExpr(builder_, level_ + 1)
        result_ = result_ && AssignmentExpr_1(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // (AssignmentOp AssignmentExpr)?
      private fun AssignmentExpr_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "AssignmentExpr_1")) return false
        AssignmentExpr_1_0(builder_, level_ + 1)
        return true
      }

      // AssignmentOp AssignmentExpr
      private fun AssignmentExpr_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "AssignmentExpr_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = AssignmentOp(builder_, level_ + 1)
        result_ = result_ && AssignmentExpr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.EQ | AngelscriptTypes.MULEQ | AngelscriptTypes.POWEREQ | AngelscriptTypes.DIVEQ | AngelscriptTypes.REMEQ | AngelscriptTypes.PLUSEQ | AngelscriptTypes.MINUSEQ

      //                        | AngelscriptTypes.LSHIFTEQ | AngelscriptTypes.RSHIFTEQ | AngelscriptTypes.RRSHIFTEQ | AngelscriptTypes.ANDEQ | AngelscriptTypes.XOREQ | AngelscriptTypes.OREQ
      private fun AssignmentOp(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "AssignmentOp")) return false
        var result_: Boolean
        result_ = consumeToken(builder_, AngelscriptTypes.EQ)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.MULEQ)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.POWEREQ)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.DIVEQ)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.REMEQ)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PLUSEQ)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.MINUSEQ)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.LSHIFTEQ)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.RSHIFTEQ)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.RRSHIFTEQ)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.ANDEQ)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.XOREQ)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.OREQ)
        return result_
      }

      /* ********************************************************** */
      // EqualityExpr (AngelscriptTypes.AND EqualityExpr)*
      private fun BitwiseAndExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "BitwiseAndExpr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _COLLAPSE_, AngelscriptTypes.BITWISE_AND_EXPR, "<bitwise and expr>")
        result_ = EqualityExpr(builder_, level_ + 1)
        result_ = result_ && BitwiseAndExpr_1(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // (AngelscriptTypes.AND EqualityExpr)*
      private fun BitwiseAndExpr_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "BitwiseAndExpr_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!BitwiseAndExpr_1_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "BitwiseAndExpr_1", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.AND EqualityExpr
      private fun BitwiseAndExpr_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "BitwiseAndExpr_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.AND)
        result_ = result_ && EqualityExpr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // IfDefBlock | Declaration | Statement | Comment
      private fun BlockItem(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "BlockItem")) return false
        var result_: Boolean
        result_ = IfDefBlock(builder_, level_ + 1)
        if (!result_) result_ = Declaration(builder_, level_ + 1)
        if (!result_) result_ = Statement(builder_, level_ + 1)
        if (!result_) result_ = Comment(builder_, level_ + 1)
        return result_
      }

      /* ********************************************************** */
      // !(AngelscriptTypes.RBRACE | AngelscriptTypes.IF | AngelscriptTypes.WHILE | AngelscriptTypes.FOR | AngelscriptTypes.RETURN | AngelscriptTypes.BREAK | AngelscriptTypes.CONTINUE

      //                                | AngelscriptTypes.SWITCH_KW | AngelscriptTypes.DO_KW | AngelscriptTypes.CASE_KW | AngelscriptTypes.DEFAULT_KW | AngelscriptTypes.LBRACE

      //                                | AngelscriptTypes.CONST | AngelscriptTypes.VOID_KW

      //                                | AngelscriptTypes.INT_KW | AngelscriptTypes.INT8_KW | AngelscriptTypes.INT16_KW | AngelscriptTypes.INT32_KW | AngelscriptTypes.INT64_KW

      //                                | AngelscriptTypes.UINT_KW | AngelscriptTypes.UINT8_KW | AngelscriptTypes.UINT16_KW | AngelscriptTypes.UINT32_KW | AngelscriptTypes.UINT64_KW

      //                                | AngelscriptTypes.FLOAT_KW | AngelscriptTypes.DOUBLE_KW | AngelscriptTypes.BOOL_KW | AngelscriptTypes.AUTO_KW | AngelscriptTypes.AUTO

      //                                | AngelscriptTypes.PLUSPLUS | AngelscriptTypes.MINUSMINUS | AngelscriptTypes.MINUS | AngelscriptTypes.EXCL | AngelscriptTypes.AT | AngelscriptTypes.TILDE | AngelscriptTypes.LPAREN

      //                                | AngelscriptTypes.NUMBER_LITERAL | AngelscriptTypes.STRING_LITERAL | AngelscriptTypes.FSTRING_START | AngelscriptTypes.BOOL_LITERAL | AngelscriptTypes.NULLPTR_KW

      //                                | AngelscriptTypes.IDENTIFIER | AngelscriptTypes.COMMENT

      //                                | AngelscriptTypes.PP_IF | AngelscriptTypes.PP_ELIF | AngelscriptTypes.PP_ELSE | AngelscriptTypes.PP_ENDIF | AngelscriptTypes.PP_DEFINE | <<eof>>)
      private fun BlockItem_recover(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "BlockItem_recover")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NOT_)
        result_ = !BlockItem_recover_0(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // AngelscriptTypes.RBRACE | AngelscriptTypes.IF | AngelscriptTypes.WHILE | AngelscriptTypes.FOR | AngelscriptTypes.RETURN | AngelscriptTypes.BREAK | AngelscriptTypes.CONTINUE

      //                                | AngelscriptTypes.SWITCH_KW | AngelscriptTypes.DO_KW | AngelscriptTypes.CASE_KW | AngelscriptTypes.DEFAULT_KW | AngelscriptTypes.LBRACE

      //                                | AngelscriptTypes.CONST | AngelscriptTypes.VOID_KW

      //                                | AngelscriptTypes.INT_KW | AngelscriptTypes.INT8_KW | AngelscriptTypes.INT16_KW | AngelscriptTypes.INT32_KW | AngelscriptTypes.INT64_KW

      //                                | AngelscriptTypes.UINT_KW | AngelscriptTypes.UINT8_KW | AngelscriptTypes.UINT16_KW | AngelscriptTypes.UINT32_KW | AngelscriptTypes.UINT64_KW

      //                                | AngelscriptTypes.FLOAT_KW | AngelscriptTypes.DOUBLE_KW | AngelscriptTypes.BOOL_KW | AngelscriptTypes.AUTO_KW | AngelscriptTypes.AUTO

      //                                | AngelscriptTypes.PLUSPLUS | AngelscriptTypes.MINUSMINUS | AngelscriptTypes.MINUS | AngelscriptTypes.EXCL | AngelscriptTypes.AT | AngelscriptTypes.TILDE | AngelscriptTypes.LPAREN

      //                                | AngelscriptTypes.NUMBER_LITERAL | AngelscriptTypes.STRING_LITERAL | AngelscriptTypes.FSTRING_START | AngelscriptTypes.BOOL_LITERAL | AngelscriptTypes.NULLPTR_KW

      //                                | AngelscriptTypes.IDENTIFIER | AngelscriptTypes.COMMENT

      //                                | AngelscriptTypes.PP_IF | AngelscriptTypes.PP_ELIF | AngelscriptTypes.PP_ELSE | AngelscriptTypes.PP_ENDIF | AngelscriptTypes.PP_DEFINE | <<eof>>
      private fun BlockItem_recover_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "BlockItem_recover_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.RBRACE)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.IF)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.WHILE)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.FOR)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.RETURN)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.BREAK)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.CONTINUE)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.SWITCH_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.DO_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.CASE_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.DEFAULT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.LBRACE)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.CONST)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.VOID_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT8_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT16_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT32_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT64_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT8_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT16_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT32_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT64_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.FLOAT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.DOUBLE_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.BOOL_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.AUTO_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.AUTO)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PLUSPLUS)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.MINUSMINUS)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.MINUS)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.EXCL)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.AT)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.TILDE)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.LPAREN)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.NUMBER_LITERAL)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.STRING_LITERAL)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.FSTRING_START)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.BOOL_LITERAL)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.NULLPTR_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.IDENTIFIER)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.COMMENT)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_IF)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_ELIF)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_ELSE)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_ENDIF)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_DEFINE)
        if (!result_) result_ = eof(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // !(AngelscriptTypes.RBRACE | <<eof>>) BlockItem
      private fun BlockItem_with_recover(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "BlockItem_with_recover")) return false
        var result_: Boolean
        var pinned_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_)
        result_ = BlockItem_with_recover_0(builder_, level_ + 1)
        pinned_ = result_ // pin = 1
        result_ = result_ && BlockItem(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, pinned_, Parser { b, l -> BlockItem_recover(b, l) })
        return result_ || pinned_
      }

      // !(AngelscriptTypes.RBRACE | <<eof>>)
      private fun BlockItem_with_recover_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "BlockItem_with_recover_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NOT_)
        result_ = !BlockItem_with_recover_0_0(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // AngelscriptTypes.RBRACE | <<eof>>
      private fun BlockItem_with_recover_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "BlockItem_with_recover_0_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.RBRACE)
        if (!result_) result_ = eof(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.BREAK    AngelscriptTypes.SEMICOLON
      private fun BreakStatement(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "BreakStatement")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.BREAK)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeTokens(builder_, 0, AngelscriptTypes.BREAK, AngelscriptTypes.SEMICOLON)
        exit_section_(builder_, marker_, AngelscriptTypes.BREAK_STATEMENT, result_)
        return result_
      }

      /* ********************************************************** */
      // TypeArgument? ArgList
      private fun CallSuffix(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "CallSuffix")) return false
        if (!nextTokenIs(builder_, "", AngelscriptTypes.LPAREN, AngelscriptTypes.LT)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = CallSuffix_0(builder_, level_ + 1)
        result_ = result_ && ArgList(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // TypeArgument?
      private fun CallSuffix_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "CallSuffix_0")) return false
        TypeArgument(builder_, level_ + 1)
        return true
      }

      /* ********************************************************** */
      // ( AngelscriptTypes.CASE_KW Expr | AngelscriptTypes.DEFAULT_KW ) AngelscriptTypes.COLON (NonCaseStatement | VariableDecl)*
      private fun CaseStatement(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "CaseStatement")) return false
        if (!nextTokenIs(builder_, "<case statement>", AngelscriptTypes.CASE_KW, AngelscriptTypes.DEFAULT_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.CASE_STATEMENT, "<case statement>")
        result_ = CaseStatement_0(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.COLON)
        result_ = result_ && CaseStatement_2(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // AngelscriptTypes.CASE_KW Expr | AngelscriptTypes.DEFAULT_KW
      private fun CaseStatement_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "CaseStatement_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = CaseStatement_0_0(builder_, level_ + 1)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.DEFAULT_KW)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.CASE_KW Expr
      private fun CaseStatement_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "CaseStatement_0_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.CASE_KW)
        result_ = result_ && Expr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // (NonCaseStatement | VariableDecl)*
      private fun CaseStatement_2(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "CaseStatement_2")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!CaseStatement_2_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "CaseStatement_2", pos_)) break
        }
        return true
      }

      // NonCaseStatement | VariableDecl
      private fun CaseStatement_2_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "CaseStatement_2_0")) return false
        var result_: Boolean
        result_ = NonCaseStatement(builder_, level_ + 1)
        if (!result_) result_ = VariableDecl(builder_, level_ + 1)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.LPAREN TypeRef AngelscriptTypes.RPAREN UnaryExpr

      //            | PrimitiveType AngelscriptTypes.LPAREN Expr AngelscriptTypes.RPAREN
      private fun CastExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "CastExpr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.CAST_EXPR, "<cast expr>")
        result_ = CastExpr_0(builder_, level_ + 1)
        if (!result_) result_ = CastExpr_1(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // AngelscriptTypes.LPAREN TypeRef AngelscriptTypes.RPAREN UnaryExpr
      private fun CastExpr_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "CastExpr_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.LPAREN)
        result_ = result_ && TypeRef(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.RPAREN)
        result_ = result_ && UnaryExpr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // PrimitiveType AngelscriptTypes.LPAREN Expr AngelscriptTypes.RPAREN
      private fun CastExpr_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "CastExpr_1")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = PrimitiveType(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.LPAREN)
        result_ = result_ && Expr(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.RPAREN)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // UClassDecl? AngelscriptTypes.CLASS identifier InheritanceList? AngelscriptTypes.LBRACE ClassMember_with_recover* AngelscriptTypes.RBRACE AngelscriptTypes.SEMICOLON?
      private fun ClassDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ClassDecl")) return false
        if (!nextTokenIs(builder_, "<class decl>", AngelscriptTypes.CLASS, AngelscriptTypes.UCLASS_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.CLASS_DECL, "<class decl>")
        result_ = ClassDecl_0(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.CLASS)
        result_ = result_ && identifier(builder_, level_ + 1)
        result_ = result_ && ClassDecl_3(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.LBRACE)
        result_ = result_ && ClassDecl_5(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.RBRACE)
        result_ = result_ && ClassDecl_7(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // UClassDecl?
      private fun ClassDecl_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ClassDecl_0")) return false
        UClassDecl(builder_, level_ + 1)
        return true
      }

      // InheritanceList?
      private fun ClassDecl_3(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ClassDecl_3")) return false
        InheritanceList(builder_, level_ + 1)
        return true
      }

      // ClassMember_with_recover*
      private fun ClassDecl_5(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ClassDecl_5")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!ClassMember_with_recover(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "ClassDecl_5", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.SEMICOLON?
      private fun ClassDecl_7(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ClassDecl_7")) return false
        consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        return true
      }

      /* ********************************************************** */
      // IfDefBlock | ConstructorDecl | FunctionDecl | DefaultValueDecl | VariableDecl | Comment
      private fun ClassMember(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ClassMember")) return false
        var result_: Boolean
        result_ = IfDefBlock(builder_, level_ + 1)
        if (!result_) result_ = ConstructorDecl(builder_, level_ + 1)
        if (!result_) result_ = FunctionDecl(builder_, level_ + 1)
        if (!result_) result_ = DefaultValueDecl(builder_, level_ + 1)
        if (!result_) result_ = VariableDecl(builder_, level_ + 1)
        if (!result_) result_ = Comment(builder_, level_ + 1)
        return result_
      }

      /* ********************************************************** */
      // !(AngelscriptTypes.RBRACE | AngelscriptTypes.UFUNCTION_KW | AngelscriptTypes.UPROPERTY_KW | AngelscriptTypes.PRIVATE_KW | AngelscriptTypes.PROTECTED_KW

      //                                 | AngelscriptTypes.STATIC | AngelscriptTypes.CONST | AngelscriptTypes.SHARED_KW | AngelscriptTypes.EXTERNAL_KW | AngelscriptTypes.DEFAULT_KW | AngelscriptTypes.VOID_KW

      //                                 | AngelscriptTypes.INT_KW | AngelscriptTypes.INT8_KW | AngelscriptTypes.INT16_KW | AngelscriptTypes.INT32_KW | AngelscriptTypes.INT64_KW

      //                                 | AngelscriptTypes.UINT_KW | AngelscriptTypes.UINT8_KW | AngelscriptTypes.UINT16_KW | AngelscriptTypes.UINT32_KW | AngelscriptTypes.UINT64_KW

      //                                 | AngelscriptTypes.FLOAT_KW | AngelscriptTypes.DOUBLE_KW | AngelscriptTypes.BOOL_KW | AngelscriptTypes.AUTO_KW | AngelscriptTypes.AUTO

      //                                 | AngelscriptTypes.TILDE | AngelscriptTypes.IDENTIFIER | AngelscriptTypes.COMMENT

      //                                 | AngelscriptTypes.PP_IF | AngelscriptTypes.PP_ELIF | AngelscriptTypes.PP_ELSE | AngelscriptTypes.PP_ENDIF | AngelscriptTypes.PP_DEFINE | <<eof>>)
      private fun ClassMember_recover(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ClassMember_recover")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NOT_)
        result_ = !ClassMember_recover_0(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // AngelscriptTypes.RBRACE | AngelscriptTypes.UFUNCTION_KW | AngelscriptTypes.UPROPERTY_KW | AngelscriptTypes.PRIVATE_KW | AngelscriptTypes.PROTECTED_KW

      //                                 | AngelscriptTypes.STATIC | AngelscriptTypes.CONST | AngelscriptTypes.SHARED_KW | AngelscriptTypes.EXTERNAL_KW | AngelscriptTypes.DEFAULT_KW | AngelscriptTypes.VOID_KW

      //                                 | AngelscriptTypes.INT_KW | AngelscriptTypes.INT8_KW | AngelscriptTypes.INT16_KW | AngelscriptTypes.INT32_KW | AngelscriptTypes.INT64_KW

      //                                 | AngelscriptTypes.UINT_KW | AngelscriptTypes.UINT8_KW | AngelscriptTypes.UINT16_KW | AngelscriptTypes.UINT32_KW | AngelscriptTypes.UINT64_KW

      //                                 | AngelscriptTypes.FLOAT_KW | AngelscriptTypes.DOUBLE_KW | AngelscriptTypes.BOOL_KW | AngelscriptTypes.AUTO_KW | AngelscriptTypes.AUTO

      //                                 | AngelscriptTypes.TILDE | AngelscriptTypes.IDENTIFIER | AngelscriptTypes.COMMENT

      //                                 | AngelscriptTypes.PP_IF | AngelscriptTypes.PP_ELIF | AngelscriptTypes.PP_ELSE | AngelscriptTypes.PP_ENDIF | AngelscriptTypes.PP_DEFINE | <<eof>>
      private fun ClassMember_recover_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ClassMember_recover_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.RBRACE)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UFUNCTION_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UPROPERTY_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PRIVATE_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PROTECTED_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.STATIC)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.CONST)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.SHARED_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.EXTERNAL_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.DEFAULT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.VOID_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT8_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT16_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT32_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT64_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT8_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT16_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT32_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT64_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.FLOAT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.DOUBLE_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.BOOL_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.AUTO_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.AUTO)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.TILDE)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.IDENTIFIER)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.COMMENT)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_IF)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_ELIF)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_ELSE)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_ENDIF)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_DEFINE)
        if (!result_) result_ = eof(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // !(AngelscriptTypes.RBRACE | <<eof>>) ClassMember
      private fun ClassMember_with_recover(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ClassMember_with_recover")) return false
        var result_: Boolean
        var pinned_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_)
        result_ = ClassMember_with_recover_0(builder_, level_ + 1)
        pinned_ = result_ // pin = 1
        result_ = result_ && ClassMember(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, pinned_, Parser { b, l -> ClassMember_recover(b, l) })
        return result_ || pinned_
      }

      // !(AngelscriptTypes.RBRACE | <<eof>>)
      private fun ClassMember_with_recover_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ClassMember_with_recover_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NOT_)
        result_ = !ClassMember_with_recover_0_0(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // AngelscriptTypes.RBRACE | <<eof>>
      private fun ClassMember_with_recover_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ClassMember_with_recover_0_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.RBRACE)
        if (!result_) result_ = eof(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // AssignmentExpr (AngelscriptTypes.COMMA AssignmentExpr)*
      private fun CommaExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "CommaExpr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _COLLAPSE_, AngelscriptTypes.COMMA_EXPR, "<comma expr>")
        result_ = AssignmentExpr(builder_, level_ + 1)
        result_ = result_ && CommaExpr_1(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // (AngelscriptTypes.COMMA AssignmentExpr)*
      private fun CommaExpr_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "CommaExpr_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!CommaExpr_1_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "CommaExpr_1", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.COMMA AssignmentExpr
      private fun CommaExpr_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "CommaExpr_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.COMMA)
        result_ = result_ && AssignmentExpr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.COMMENT
      private fun Comment(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "Comment")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.COMMENT)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.COMMENT)
        exit_section_(builder_, marker_, AngelscriptTypes.COMMENT_NODE, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.LBRACE BlockItem_with_recover* AngelscriptTypes.RBRACE
      private fun CompoundStatement(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "CompoundStatement")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.LBRACE)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.LBRACE)
        result_ = result_ && CompoundStatement_1(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.RBRACE)
        exit_section_(builder_, marker_, AngelscriptTypes.COMPOUND_STATEMENT, result_)
        return result_
      }

      // BlockItem_with_recover*
      private fun CompoundStatement_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "CompoundStatement_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!BlockItem_with_recover(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "CompoundStatement_1", pos_)) break
        }
        return true
      }

      /* ********************************************************** */
      // LogicalOrExpr (AngelscriptTypes.QUEST Expr? AngelscriptTypes.COLON AssignmentExpr)?
      private fun ConditionalExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ConditionalExpr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _COLLAPSE_, AngelscriptTypes.CONDITIONAL_EXPR, "<conditional expr>")
        result_ = LogicalOrExpr(builder_, level_ + 1)
        result_ = result_ && ConditionalExpr_1(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // (AngelscriptTypes.QUEST Expr? AngelscriptTypes.COLON AssignmentExpr)?
      private fun ConditionalExpr_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ConditionalExpr_1")) return false
        ConditionalExpr_1_0(builder_, level_ + 1)
        return true
      }

      // AngelscriptTypes.QUEST Expr? AngelscriptTypes.COLON AssignmentExpr
      private fun ConditionalExpr_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ConditionalExpr_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.QUEST)
        result_ = result_ && ConditionalExpr_1_0_1(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.COLON)
        result_ = result_ && AssignmentExpr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // Expr?
      private fun ConditionalExpr_1_0_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ConditionalExpr_1_0_1")) return false
        Expr(builder_, level_ + 1)
        return true
      }

      /* ********************************************************** */
      // AccessSpecifier?

      //                     identifier

      //                     ParameterList

      //                     (AngelscriptTypes.SEMICOLON | CompoundStatement AngelscriptTypes.SEMICOLON?)
      private fun ConstructorDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ConstructorDecl")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.CONSTRUCTOR_DECL, "<constructor decl>")
        result_ = ConstructorDecl_0(builder_, level_ + 1)
        result_ = result_ && identifier(builder_, level_ + 1)
        result_ = result_ && ParameterList(builder_, level_ + 1)
        result_ = result_ && ConstructorDecl_3(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // AccessSpecifier?
      private fun ConstructorDecl_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ConstructorDecl_0")) return false
        AccessSpecifier(builder_, level_ + 1)
        return true
      }

      // AngelscriptTypes.SEMICOLON | CompoundStatement AngelscriptTypes.SEMICOLON?
      private fun ConstructorDecl_3(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ConstructorDecl_3")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        if (!result_) result_ = ConstructorDecl_3_1(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // CompoundStatement AngelscriptTypes.SEMICOLON?
      private fun ConstructorDecl_3_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ConstructorDecl_3_1")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = CompoundStatement(builder_, level_ + 1)
        result_ = result_ && ConstructorDecl_3_1_1(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.SEMICOLON?
      private fun ConstructorDecl_3_1_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ConstructorDecl_3_1_1")) return false
        consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        return true
      }

      /* ********************************************************** */
      // AngelscriptTypes.CONTINUE AngelscriptTypes.SEMICOLON
      private fun ContinueStatement(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ContinueStatement")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.CONTINUE)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeTokens(builder_, 0, AngelscriptTypes.CONTINUE, AngelscriptTypes.SEMICOLON)
        exit_section_(builder_, marker_, AngelscriptTypes.CONTINUE_STATEMENT, result_)
        return result_
      }

      /* ********************************************************** */
      // PrimitiveType | AngelscriptTypes.QUEST | AngelscriptTypes.AUTO_KW | AngelscriptTypes.AUTO | identifier
      private fun DataType(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "DataType")) return false
        var result_: Boolean
        result_ = PrimitiveType(builder_, level_ + 1)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.QUEST)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.AUTO_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.AUTO)
        if (!result_) result_ = identifier(builder_, level_ + 1)
        return result_
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
      private fun Declaration(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "Declaration")) return false
        var result_: Boolean
        result_ = NamespaceDecl(builder_, level_ + 1)
        if (!result_) result_ = AssetDecl(builder_, level_ + 1)
        if (!result_) result_ = EnumDecl(builder_, level_ + 1)
        if (!result_) result_ = TypedefDecl(builder_, level_ + 1)
        if (!result_) result_ = ClassDecl(builder_, level_ + 1)
        if (!result_) result_ = StructDecl(builder_, level_ + 1)
        if (!result_) result_ = InterfaceDecl(builder_, level_ + 1)
        if (!result_) result_ = EventDecl(builder_, level_ + 1)
        if (!result_) result_ = DelegateDecl(builder_, level_ + 1)
        if (!result_) result_ = MixinDecl(builder_, level_ + 1)
        if (!result_) result_ = FunctionDecl(builder_, level_ + 1)
        if (!result_) result_ = DefaultValueDecl(builder_, level_ + 1)
        if (!result_) result_ = VariableDecl(builder_, level_ + 1)
        if (!result_) result_ = Comment(builder_, level_ + 1)
        return result_
      }

      /* ********************************************************** */
      // TypeRef TypeMod?
      private fun DeclarationSpecifier(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "DeclarationSpecifier")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = TypeRef(builder_, level_ + 1)
        result_ = result_ && DeclarationSpecifier_1(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // TypeMod?
      private fun DeclarationSpecifier_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "DeclarationSpecifier_1")) return false
        TypeMod(builder_, level_ + 1)
        return true
      }

      /* ********************************************************** */
      // AngelscriptTypes.DEFAULT_KW Expr AngelscriptTypes.SEMICOLON
      private fun DefaultValueDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "DefaultValueDecl")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.DEFAULT_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.DEFAULT_KW)
        result_ = result_ && Expr(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        exit_section_(builder_, marker_, AngelscriptTypes.DEFAULT_VALUE_DECL, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.DELEGATE_KW FunctionDecl
      private fun DelegateDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "DelegateDecl")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.DELEGATE_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.DELEGATE_KW)
        result_ = result_ && FunctionDecl(builder_, level_ + 1)
        exit_section_(builder_, marker_, AngelscriptTypes.DELEGATE_DECL, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.DO_KW Statement AngelscriptTypes.WHILE ParenthesizedExpr AngelscriptTypes.SEMICOLON
      private fun DoStatement(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "DoStatement")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.DO_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.DO_KW)
        result_ = result_ && Statement(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.WHILE)
        result_ = result_ && ParenthesizedExpr(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        exit_section_(builder_, marker_, AngelscriptTypes.DO_STATEMENT, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.ELSE Statement
      private fun ElseClause(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ElseClause")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.ELSE)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.ELSE)
        result_ = result_ && Statement(builder_, level_ + 1)
        exit_section_(builder_, marker_, AngelscriptTypes.ELSE_CLAUSE, result_)
        return result_
      }

      /* ********************************************************** */
      // UEnumDecl? AngelscriptTypes.ENUM identifier AngelscriptTypes.LBRACE EnumVariant_with_recover* AngelscriptTypes.RBRACE AngelscriptTypes.SEMICOLON?
      private fun EnumDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "EnumDecl")) return false
        if (!nextTokenIs(builder_, "<enum decl>", AngelscriptTypes.ENUM, AngelscriptTypes.UENUM_KW)) return false
        var result_: Boolean
        var pinned_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.ENUM_DECL, "<enum decl>")
        result_ = EnumDecl_0(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.ENUM)
        pinned_ = result_ // pin = 2
        result_ = result_ && report_error_(builder_, identifier(builder_, level_ + 1))
        result_ = pinned_ && report_error_(builder_, consumeToken(builder_, AngelscriptTypes.LBRACE)) && result_
        result_ = pinned_ && report_error_(builder_, EnumDecl_4(builder_, level_ + 1)) && result_
        result_ = pinned_ && report_error_(builder_, consumeToken(builder_, AngelscriptTypes.RBRACE)) && result_
        result_ = pinned_ && EnumDecl_6(builder_, level_ + 1) && result_
        exit_section_(builder_, level_, marker_, result_, pinned_, null)
        return result_ || pinned_
      }

      // UEnumDecl?
      private fun EnumDecl_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "EnumDecl_0")) return false
        UEnumDecl(builder_, level_ + 1)
        return true
      }

      // EnumVariant_with_recover*
      private fun EnumDecl_4(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "EnumDecl_4")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!EnumVariant_with_recover(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "EnumDecl_4", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.SEMICOLON?
      private fun EnumDecl_6(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "EnumDecl_6")) return false
        consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        return true
      }

      /* ********************************************************** */
      // identifier (AngelscriptTypes.EQ Expr)? AngelscriptTypes.COMMA?
      private fun EnumVariant(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "EnumVariant")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.IDENTIFIER)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = identifier(builder_, level_ + 1)
        result_ = result_ && EnumVariant_1(builder_, level_ + 1)
        result_ = result_ && EnumVariant_2(builder_, level_ + 1)
        exit_section_(builder_, marker_, AngelscriptTypes.ENUM_VARIANT, result_)
        return result_
      }

      // (AngelscriptTypes.EQ Expr)?
      private fun EnumVariant_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "EnumVariant_1")) return false
        EnumVariant_1_0(builder_, level_ + 1)
        return true
      }

      // AngelscriptTypes.EQ Expr
      private fun EnumVariant_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "EnumVariant_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.EQ)
        result_ = result_ && Expr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.COMMA?
      private fun EnumVariant_2(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "EnumVariant_2")) return false
        consumeToken(builder_, AngelscriptTypes.COMMA)
        return true
      }

      /* ********************************************************** */
      // !(AngelscriptTypes.RBRACE | AngelscriptTypes.IDENTIFIER | <<eof>>)
      private fun EnumVariant_recover(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "EnumVariant_recover")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NOT_)
        result_ = !EnumVariant_recover_0(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // AngelscriptTypes.RBRACE | AngelscriptTypes.IDENTIFIER | <<eof>>
      private fun EnumVariant_recover_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "EnumVariant_recover_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.RBRACE)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.IDENTIFIER)
        if (!result_) result_ = eof(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // !(AngelscriptTypes.RBRACE | <<eof>>) EnumVariant
      private fun EnumVariant_with_recover(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "EnumVariant_with_recover")) return false
        var result_: Boolean
        var pinned_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_)
        result_ = EnumVariant_with_recover_0(builder_, level_ + 1)
        pinned_ = result_ // pin = 1
        result_ = result_ && EnumVariant(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, pinned_, Parser { b, l -> EnumVariant_recover(b, l) })
        return result_ || pinned_
      }

      // !(AngelscriptTypes.RBRACE | <<eof>>)
      private fun EnumVariant_with_recover_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "EnumVariant_with_recover_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NOT_)
        result_ = !EnumVariant_with_recover_0_0(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // AngelscriptTypes.RBRACE | <<eof>>
      private fun EnumVariant_with_recover_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "EnumVariant_with_recover_0_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.RBRACE)
        if (!result_) result_ = eof(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // RelationalExpr ((AngelscriptTypes.EQEQ | AngelscriptTypes.EXCLEQ) RelationalExpr)*
      private fun EqualityExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "EqualityExpr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _COLLAPSE_, AngelscriptTypes.EQUALITY_EXPR, "<equality expr>")
        result_ = RelationalExpr(builder_, level_ + 1)
        result_ = result_ && EqualityExpr_1(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // ((AngelscriptTypes.EQEQ | AngelscriptTypes.EXCLEQ) RelationalExpr)*
      private fun EqualityExpr_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "EqualityExpr_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!EqualityExpr_1_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "EqualityExpr_1", pos_)) break
        }
        return true
      }

      // (AngelscriptTypes.EQEQ | AngelscriptTypes.EXCLEQ) RelationalExpr
      private fun EqualityExpr_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "EqualityExpr_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = EqualityExpr_1_0_0(builder_, level_ + 1)
        result_ = result_ && RelationalExpr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.EQEQ | AngelscriptTypes.EXCLEQ
      private fun EqualityExpr_1_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "EqualityExpr_1_0_0")) return false
        var result_: Boolean
        result_ = consumeToken(builder_, AngelscriptTypes.EQEQ)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.EXCLEQ)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.EVENT_KW FunctionDecl
      private fun EventDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "EventDecl")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.EVENT_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.EVENT_KW)
        result_ = result_ && FunctionDecl(builder_, level_ + 1)
        exit_section_(builder_, marker_, AngelscriptTypes.EVENT_DECL, result_)
        return result_
      }

      /* ********************************************************** */
      // BitwiseAndExpr ((AngelscriptTypes.XOR | AngelscriptTypes.XOR_KW) BitwiseAndExpr)*
      private fun ExclusiveOrExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ExclusiveOrExpr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _COLLAPSE_, AngelscriptTypes.EXCLUSIVE_OR_EXPR, "<exclusive or expr>")
        result_ = BitwiseAndExpr(builder_, level_ + 1)
        result_ = result_ && ExclusiveOrExpr_1(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // ((AngelscriptTypes.XOR | AngelscriptTypes.XOR_KW) BitwiseAndExpr)*
      private fun ExclusiveOrExpr_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ExclusiveOrExpr_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!ExclusiveOrExpr_1_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "ExclusiveOrExpr_1", pos_)) break
        }
        return true
      }

      // (AngelscriptTypes.XOR | AngelscriptTypes.XOR_KW) BitwiseAndExpr
      private fun ExclusiveOrExpr_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ExclusiveOrExpr_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = ExclusiveOrExpr_1_0_0(builder_, level_ + 1)
        result_ = result_ && BitwiseAndExpr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.XOR | AngelscriptTypes.XOR_KW
      private fun ExclusiveOrExpr_1_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ExclusiveOrExpr_1_0_0")) return false
        var result_: Boolean
        result_ = consumeToken(builder_, AngelscriptTypes.XOR)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.XOR_KW)
        return result_
      }

      /* ********************************************************** */
      // CommaExpr
      private fun Expr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "Expr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _COLLAPSE_, AngelscriptTypes.EXPR, "<expr>")
        result_ = CommaExpr(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      /* ********************************************************** */
      // Expr AngelscriptTypes.SEMICOLON
      private fun ExpressionStatement(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ExpressionStatement")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.EXPRESSION_STATEMENT, "<expression statement>")
        result_ = Expr(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.FSTRING_START fstring_content* AngelscriptTypes.FSTRING_END
      private fun FStringExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FStringExpr")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.FSTRING_START)) return false
        var result_: Boolean
        var pinned_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.F_STRING_EXPR, null)
        result_ = consumeToken(builder_, AngelscriptTypes.FSTRING_START)
        pinned_ = result_ // pin = 1
        result_ = result_ && report_error_(builder_, FStringExpr_1(builder_, level_ + 1))
        result_ = pinned_ && consumeToken(builder_, AngelscriptTypes.FSTRING_END) && result_
        exit_section_(builder_, level_, marker_, result_, pinned_, null)
        return result_ || pinned_
      }

      // fstring_content*
      private fun FStringExpr_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FStringExpr_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!fstring_content(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "FStringExpr_1", pos_)) break
        }
        return true
      }

      /* ********************************************************** */
      // AngelscriptTypes.FSTRING_COLON AngelscriptTypes.FSTRING_FORMAT_TEXT?
      private fun FStringFormatSpec(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FStringFormatSpec")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.FSTRING_COLON)) return false
        var result_: Boolean
        var pinned_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.F_STRING_FORMAT_SPEC, null)
        result_ = consumeToken(builder_, AngelscriptTypes.FSTRING_COLON)
        pinned_ = result_ // pin = 1
        result_ = result_ && FStringFormatSpec_1(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, pinned_, null)
        return result_ || pinned_
      }

      // AngelscriptTypes.FSTRING_FORMAT_TEXT?
      private fun FStringFormatSpec_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FStringFormatSpec_1")) return false
        consumeToken(builder_, AngelscriptTypes.FSTRING_FORMAT_TEXT)
        return true
      }

      /* ********************************************************** */
      // AngelscriptTypes.FSTRING_LBRACE Expr AngelscriptTypes.EQ? FStringFormatSpec? AngelscriptTypes.FSTRING_RBRACE
      private fun FStringInterp(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FStringInterp")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.FSTRING_LBRACE)) return false
        var result_: Boolean
        var pinned_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.F_STRING_INTERP, null)
        result_ = consumeToken(builder_, AngelscriptTypes.FSTRING_LBRACE)
        pinned_ = result_ // pin = 1
        result_ = result_ && report_error_(builder_, Expr(builder_, level_ + 1))
        result_ = pinned_ && report_error_(builder_, FStringInterp_2(builder_, level_ + 1)) && result_
        result_ = pinned_ && report_error_(builder_, FStringInterp_3(builder_, level_ + 1)) && result_
        result_ = pinned_ && consumeToken(builder_, AngelscriptTypes.FSTRING_RBRACE) && result_
        exit_section_(builder_, level_, marker_, result_, pinned_, null)
        return result_ || pinned_
      }

      // AngelscriptTypes.EQ?
      private fun FStringInterp_2(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FStringInterp_2")) return false
        consumeToken(builder_, AngelscriptTypes.EQ)
        return true
      }

      // FStringFormatSpec?
      private fun FStringInterp_3(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FStringInterp_3")) return false
        FStringFormatSpec(builder_, level_ + 1)
        return true
      }

      /* ********************************************************** */
      // AngelscriptTypes.DOT identifier
      private fun FieldSuffix(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FieldSuffix")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.DOT)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.DOT)
        result_ = result_ && identifier(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // ( VariableDecl | Expr? AngelscriptTypes.SEMICOLON ) Expr? AngelscriptTypes.SEMICOLON Expr?
      private fun ForHeader(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ForHeader")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = ForHeader_0(builder_, level_ + 1)
        result_ = result_ && ForHeader_1(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        result_ = result_ && ForHeader_3(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // VariableDecl | Expr? AngelscriptTypes.SEMICOLON
      private fun ForHeader_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ForHeader_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = VariableDecl(builder_, level_ + 1)
        if (!result_) result_ = ForHeader_0_1(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // Expr? AngelscriptTypes.SEMICOLON
      private fun ForHeader_0_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ForHeader_0_1")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = ForHeader_0_1_0(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // Expr?
      private fun ForHeader_0_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ForHeader_0_1_0")) return false
        Expr(builder_, level_ + 1)
        return true
      }

      // Expr?
      private fun ForHeader_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ForHeader_1")) return false
        Expr(builder_, level_ + 1)
        return true
      }

      // Expr?
      private fun ForHeader_3(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ForHeader_3")) return false
        Expr(builder_, level_ + 1)
        return true
      }

      /* ********************************************************** */
      // AngelscriptTypes.FOR AngelscriptTypes.LPAREN ForHeader AngelscriptTypes.RPAREN Statement
      private fun ForStatement(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ForStatement")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.FOR)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeTokens(builder_, 0, AngelscriptTypes.FOR, AngelscriptTypes.LPAREN)
        result_ = result_ && ForHeader(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.RPAREN)
        result_ = result_ && Statement(builder_, level_ + 1)
        exit_section_(builder_, marker_, AngelscriptTypes.FOR_STATEMENT, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.FOR AngelscriptTypes.LPAREN ForeachVarDecl AngelscriptTypes.COLON Expr AngelscriptTypes.RPAREN Statement
      private fun ForeachStatement(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ForeachStatement")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.FOR)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeTokens(builder_, 0, AngelscriptTypes.FOR, AngelscriptTypes.LPAREN)
        result_ = result_ && ForeachVarDecl(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.COLON)
        result_ = result_ && Expr(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.RPAREN)
        result_ = result_ && Statement(builder_, level_ + 1)
        exit_section_(builder_, marker_, AngelscriptTypes.FOREACH_STATEMENT, result_)
        return result_
      }

      /* ********************************************************** */
      // DeclarationSpecifier identifier
      private fun ForeachVarDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ForeachVarDecl")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = DeclarationSpecifier(builder_, level_ + 1)
        result_ = result_ && identifier(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.OVERRIDE_KW | AngelscriptTypes.FINAL_KW | AngelscriptTypes.EXPLICIT_KW | AngelscriptTypes.PROPERTY_KW
      private fun FuncAttr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FuncAttr")) return false
        var result_: Boolean
        result_ = consumeToken(builder_, AngelscriptTypes.OVERRIDE_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.FINAL_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.EXPLICIT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PROPERTY_KW)
        return result_
      }

      /* ********************************************************** */
      // AccessSpecifier?

      //                  UFunctionDecl?

      //                  AccessSpecifier?

      //                  FunctionLocation?

      //                  AccessSpecifier?

      //                  (TypeRef AngelscriptTypes.AND? | AngelscriptTypes.TILDE)?

      //                  identifier

      //                  ParameterList

      //                  AngelscriptTypes.CONST?

      //                  FuncAttr?

      //                  (AngelscriptTypes.SEMICOLON | CompoundStatement AngelscriptTypes.SEMICOLON?)
      private fun FunctionDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FunctionDecl")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.FUNCTION_DECL, "<function decl>")
        result_ = FunctionDecl_0(builder_, level_ + 1)
        result_ = result_ && FunctionDecl_1(builder_, level_ + 1)
        result_ = result_ && FunctionDecl_2(builder_, level_ + 1)
        result_ = result_ && FunctionDecl_3(builder_, level_ + 1)
        result_ = result_ && FunctionDecl_4(builder_, level_ + 1)
        result_ = result_ && FunctionDecl_5(builder_, level_ + 1)
        result_ = result_ && identifier(builder_, level_ + 1)
        result_ = result_ && ParameterList(builder_, level_ + 1)
        result_ = result_ && FunctionDecl_8(builder_, level_ + 1)
        result_ = result_ && FunctionDecl_9(builder_, level_ + 1)
        result_ = result_ && FunctionDecl_10(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // AccessSpecifier?
      private fun FunctionDecl_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FunctionDecl_0")) return false
        AccessSpecifier(builder_, level_ + 1)
        return true
      }

      // UFunctionDecl?
      private fun FunctionDecl_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FunctionDecl_1")) return false
        UFunctionDecl(builder_, level_ + 1)
        return true
      }

      // AccessSpecifier?
      private fun FunctionDecl_2(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FunctionDecl_2")) return false
        AccessSpecifier(builder_, level_ + 1)
        return true
      }

      // FunctionLocation?
      private fun FunctionDecl_3(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FunctionDecl_3")) return false
        FunctionLocation(builder_, level_ + 1)
        return true
      }

      // AccessSpecifier?
      private fun FunctionDecl_4(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FunctionDecl_4")) return false
        AccessSpecifier(builder_, level_ + 1)
        return true
      }

      // (TypeRef AngelscriptTypes.AND? | AngelscriptTypes.TILDE)?
      private fun FunctionDecl_5(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FunctionDecl_5")) return false
        FunctionDecl_5_0(builder_, level_ + 1)
        return true
      }

      // TypeRef AngelscriptTypes.AND? | AngelscriptTypes.TILDE
      private fun FunctionDecl_5_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FunctionDecl_5_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = FunctionDecl_5_0_0(builder_, level_ + 1)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.TILDE)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // TypeRef AngelscriptTypes.AND?
      private fun FunctionDecl_5_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FunctionDecl_5_0_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = TypeRef(builder_, level_ + 1)
        result_ = result_ && FunctionDecl_5_0_0_1(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.AND?
      private fun FunctionDecl_5_0_0_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FunctionDecl_5_0_0_1")) return false
        consumeToken(builder_, AngelscriptTypes.AND)
        return true
      }

      // AngelscriptTypes.CONST?
      private fun FunctionDecl_8(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FunctionDecl_8")) return false
        consumeToken(builder_, AngelscriptTypes.CONST)
        return true
      }

      // FuncAttr?
      private fun FunctionDecl_9(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FunctionDecl_9")) return false
        FuncAttr(builder_, level_ + 1)
        return true
      }

      // AngelscriptTypes.SEMICOLON | CompoundStatement AngelscriptTypes.SEMICOLON?
      private fun FunctionDecl_10(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FunctionDecl_10")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        if (!result_) result_ = FunctionDecl_10_1(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // CompoundStatement AngelscriptTypes.SEMICOLON?
      private fun FunctionDecl_10_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FunctionDecl_10_1")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = CompoundStatement(builder_, level_ + 1)
        result_ = result_ && FunctionDecl_10_1_1(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.SEMICOLON?
      private fun FunctionDecl_10_1_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FunctionDecl_10_1_1")) return false
        consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        return true
      }

      /* ********************************************************** */
      // (AngelscriptTypes.SHARED_KW | AngelscriptTypes.EXTERNAL_KW)+
      private fun FunctionLocation(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FunctionLocation")) return false
        if (!nextTokenIs(builder_, "", AngelscriptTypes.EXTERNAL_KW, AngelscriptTypes.SHARED_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = FunctionLocation_0(builder_, level_ + 1)
        while (result_) {
          val pos_ = current_position_(builder_)
          if (!FunctionLocation_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "FunctionLocation", pos_)) break
        }
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.SHARED_KW | AngelscriptTypes.EXTERNAL_KW
      private fun FunctionLocation_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "FunctionLocation_0")) return false
        var result_: Boolean
        result_ = consumeToken(builder_, AngelscriptTypes.SHARED_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.EXTERNAL_KW)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.PP_IF IfDefBranch (AngelscriptTypes.PP_ELIF IfDefBranch)* (AngelscriptTypes.PP_ELSE IfDefBranch)? AngelscriptTypes.PP_ENDIF
      private fun IfDefBlock(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "IfDefBlock")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.PP_IF)) return false
        var result_: Boolean
        var pinned_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.IF_DEF_BLOCK, null)
        result_ = consumeToken(builder_, AngelscriptTypes.PP_IF)
        pinned_ = result_ // pin = 1
        result_ = result_ && report_error_(builder_, IfDefBranch(builder_, level_ + 1))
        result_ = pinned_ && report_error_(builder_, IfDefBlock_2(builder_, level_ + 1)) && result_
        result_ = pinned_ && report_error_(builder_, IfDefBlock_3(builder_, level_ + 1)) && result_
        result_ = pinned_ && consumeToken(builder_, AngelscriptTypes.PP_ENDIF) && result_
        exit_section_(builder_, level_, marker_, result_, pinned_, null)
        return result_ || pinned_
      }

      // (AngelscriptTypes.PP_ELIF IfDefBranch)*
      private fun IfDefBlock_2(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "IfDefBlock_2")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!IfDefBlock_2_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "IfDefBlock_2", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.PP_ELIF IfDefBranch
      private fun IfDefBlock_2_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "IfDefBlock_2_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.PP_ELIF)
        result_ = result_ && IfDefBranch(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // (AngelscriptTypes.PP_ELSE IfDefBranch)?
      private fun IfDefBlock_3(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "IfDefBlock_3")) return false
        IfDefBlock_3_0(builder_, level_ + 1)
        return true
      }

      // AngelscriptTypes.PP_ELSE IfDefBranch
      private fun IfDefBlock_3_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "IfDefBlock_3_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.PP_ELSE)
        result_ = result_ && IfDefBranch(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // Item*
      private fun IfDefBranch(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "IfDefBranch")) return false
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.IF_DEF_BRANCH, "<if def branch>")
        while (true) {
          val pos_ = current_position_(builder_)
          if (!Item(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "IfDefBranch", pos_)) break
        }
        exit_section_(builder_, level_, marker_, true, false, null)
        return true
      }

      /* ********************************************************** */
      // AngelscriptTypes.IF ParenthesizedExpr Statement ElseClause?
      private fun IfStatement(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "IfStatement")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.IF)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.IF)
        result_ = result_ && ParenthesizedExpr(builder_, level_ + 1)
        result_ = result_ && Statement(builder_, level_ + 1)
        result_ = result_ && IfStatement_3(builder_, level_ + 1)
        exit_section_(builder_, marker_, AngelscriptTypes.IF_STATEMENT, result_)
        return result_
      }

      // ElseClause?
      private fun IfStatement_3(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "IfStatement_3")) return false
        ElseClause(builder_, level_ + 1)
        return true
      }

      /* ********************************************************** */
      // ExclusiveOrExpr (AngelscriptTypes.OR ExclusiveOrExpr)*
      private fun InclusiveOrExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "InclusiveOrExpr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _COLLAPSE_, AngelscriptTypes.INCLUSIVE_OR_EXPR, "<inclusive or expr>")
        result_ = ExclusiveOrExpr(builder_, level_ + 1)
        result_ = result_ && InclusiveOrExpr_1(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // (AngelscriptTypes.OR ExclusiveOrExpr)*
      private fun InclusiveOrExpr_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "InclusiveOrExpr_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!InclusiveOrExpr_1_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "InclusiveOrExpr_1", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.OR ExclusiveOrExpr
      private fun InclusiveOrExpr_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "InclusiveOrExpr_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.OR)
        result_ = result_ && ExclusiveOrExpr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.COLON TypeRef (AngelscriptTypes.COMMA TypeRef)*
      private fun InheritanceList(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "InheritanceList")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.COLON)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.COLON)
        result_ = result_ && TypeRef(builder_, level_ + 1)
        result_ = result_ && InheritanceList_2(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // (AngelscriptTypes.COMMA TypeRef)*
      private fun InheritanceList_2(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "InheritanceList_2")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!InheritanceList_2_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "InheritanceList_2", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.COMMA TypeRef
      private fun InheritanceList_2_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "InheritanceList_2_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.COMMA)
        result_ = result_ && TypeRef(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // [TypeRef AngelscriptTypes.EQ] AngelscriptTypes.LBRACE [ Expr (AngelscriptTypes.COMMA Expr)* ] AngelscriptTypes.RBRACE
      private fun InitListExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "InitListExpr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.INIT_LIST_EXPR, "<init list expr>")
        result_ = InitListExpr_0(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.LBRACE)
        result_ = result_ && InitListExpr_2(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.RBRACE)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // [TypeRef AngelscriptTypes.EQ]
      private fun InitListExpr_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "InitListExpr_0")) return false
        InitListExpr_0_0(builder_, level_ + 1)
        return true
      }

      // TypeRef AngelscriptTypes.EQ
      private fun InitListExpr_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "InitListExpr_0_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = TypeRef(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.EQ)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // [ Expr (AngelscriptTypes.COMMA Expr)* ]
      private fun InitListExpr_2(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "InitListExpr_2")) return false
        InitListExpr_2_0(builder_, level_ + 1)
        return true
      }

      // Expr (AngelscriptTypes.COMMA Expr)*
      private fun InitListExpr_2_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "InitListExpr_2_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = Expr(builder_, level_ + 1)
        result_ = result_ && InitListExpr_2_0_1(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // (AngelscriptTypes.COMMA Expr)*
      private fun InitListExpr_2_0_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "InitListExpr_2_0_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!InitListExpr_2_0_1_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "InitListExpr_2_0_1", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.COMMA Expr
      private fun InitListExpr_2_0_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "InitListExpr_2_0_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.COMMA)
        result_ = result_ && Expr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // UInterfaceDecl? AngelscriptTypes.INTERFACE_KW identifier InheritanceList? AngelscriptTypes.LBRACE ClassMember_with_recover* AngelscriptTypes.RBRACE AngelscriptTypes.SEMICOLON?
      private fun InterfaceDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "InterfaceDecl")) return false
        if (!nextTokenIs(builder_, "<interface decl>", AngelscriptTypes.INTERFACE_KW, AngelscriptTypes.UINTERFACE_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.INTERFACE_DECL, "<interface decl>")
        result_ = InterfaceDecl_0(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.INTERFACE_KW)
        result_ = result_ && identifier(builder_, level_ + 1)
        result_ = result_ && InterfaceDecl_3(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.LBRACE)
        result_ = result_ && InterfaceDecl_5(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.RBRACE)
        result_ = result_ && InterfaceDecl_7(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // UInterfaceDecl?
      private fun InterfaceDecl_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "InterfaceDecl_0")) return false
        UInterfaceDecl(builder_, level_ + 1)
        return true
      }

      // InheritanceList?
      private fun InterfaceDecl_3(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "InterfaceDecl_3")) return false
        InheritanceList(builder_, level_ + 1)
        return true
      }

      // ClassMember_with_recover*
      private fun InterfaceDecl_5(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "InterfaceDecl_5")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!ClassMember_with_recover(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "InterfaceDecl_5", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.SEMICOLON?
      private fun InterfaceDecl_7(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "InterfaceDecl_7")) return false
        consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        return true
      }

      /* ********************************************************** */
      // IfDefBlock

      //                | Declaration

      //                | Statement

      //                | Comment

      //                | AngelscriptTypes.SEMICOLON
      private fun Item(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "Item")) return false
        var result_: Boolean
        result_ = IfDefBlock(builder_, level_ + 1)
        if (!result_) result_ = Declaration(builder_, level_ + 1)
        if (!result_) result_ = Statement(builder_, level_ + 1)
        if (!result_) result_ = Comment(builder_, level_ + 1)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        return result_
      }

      /* ********************************************************** */
      // InclusiveOrExpr ((AngelscriptTypes.ANDAND | AngelscriptTypes.AND_KW) InclusiveOrExpr)*
      private fun LogicalAndExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "LogicalAndExpr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _COLLAPSE_, AngelscriptTypes.LOGICAL_AND_EXPR, "<logical and expr>")
        result_ = InclusiveOrExpr(builder_, level_ + 1)
        result_ = result_ && LogicalAndExpr_1(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // ((AngelscriptTypes.ANDAND | AngelscriptTypes.AND_KW) InclusiveOrExpr)*
      private fun LogicalAndExpr_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "LogicalAndExpr_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!LogicalAndExpr_1_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "LogicalAndExpr_1", pos_)) break
        }
        return true
      }

      // (AngelscriptTypes.ANDAND | AngelscriptTypes.AND_KW) InclusiveOrExpr
      private fun LogicalAndExpr_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "LogicalAndExpr_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = LogicalAndExpr_1_0_0(builder_, level_ + 1)
        result_ = result_ && InclusiveOrExpr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.ANDAND | AngelscriptTypes.AND_KW
      private fun LogicalAndExpr_1_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "LogicalAndExpr_1_0_0")) return false
        var result_: Boolean
        result_ = consumeToken(builder_, AngelscriptTypes.ANDAND)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.AND_KW)
        return result_
      }

      /* ********************************************************** */
      // LogicalAndExpr ((AngelscriptTypes.OROR | AngelscriptTypes.OR_KW) LogicalAndExpr)*
      private fun LogicalOrExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "LogicalOrExpr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _COLLAPSE_, AngelscriptTypes.LOGICAL_OR_EXPR, "<logical or expr>")
        result_ = LogicalAndExpr(builder_, level_ + 1)
        result_ = result_ && LogicalOrExpr_1(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // ((AngelscriptTypes.OROR | AngelscriptTypes.OR_KW) LogicalAndExpr)*
      private fun LogicalOrExpr_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "LogicalOrExpr_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!LogicalOrExpr_1_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "LogicalOrExpr_1", pos_)) break
        }
        return true
      }

      // (AngelscriptTypes.OROR | AngelscriptTypes.OR_KW) LogicalAndExpr
      private fun LogicalOrExpr_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "LogicalOrExpr_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = LogicalOrExpr_1_0_0(builder_, level_ + 1)
        result_ = result_ && LogicalAndExpr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.OROR | AngelscriptTypes.OR_KW
      private fun LogicalOrExpr_1_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "LogicalOrExpr_1_0_0")) return false
        var result_: Boolean
        result_ = consumeToken(builder_, AngelscriptTypes.OROR)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.OR_KW)
        return result_
      }

      /* ********************************************************** */
      // identifier (AngelscriptTypes.EQ MetaArgValue)?
      private fun MetaArg(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MetaArg")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.IDENTIFIER)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = identifier(builder_, level_ + 1)
        result_ = result_ && MetaArg_1(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // (AngelscriptTypes.EQ MetaArgValue)?
      private fun MetaArg_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MetaArg_1")) return false
        MetaArg_1_0(builder_, level_ + 1)
        return true
      }

      // AngelscriptTypes.EQ MetaArgValue
      private fun MetaArg_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MetaArg_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.EQ)
        result_ = result_ && MetaArgValue(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.LPAREN [ MetaArg (AngelscriptTypes.COMMA MetaArg)* AngelscriptTypes.COMMA? ] AngelscriptTypes.RPAREN
      private fun MetaArgList(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MetaArgList")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.LPAREN)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.LPAREN)
        result_ = result_ && MetaArgList_1(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.RPAREN)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // [ MetaArg (AngelscriptTypes.COMMA MetaArg)* AngelscriptTypes.COMMA? ]
      private fun MetaArgList_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MetaArgList_1")) return false
        MetaArgList_1_0(builder_, level_ + 1)
        return true
      }

      // MetaArg (AngelscriptTypes.COMMA MetaArg)* AngelscriptTypes.COMMA?
      private fun MetaArgList_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MetaArgList_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = MetaArg(builder_, level_ + 1)
        result_ = result_ && MetaArgList_1_0_1(builder_, level_ + 1)
        result_ = result_ && MetaArgList_1_0_2(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // (AngelscriptTypes.COMMA MetaArg)*
      private fun MetaArgList_1_0_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MetaArgList_1_0_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!MetaArgList_1_0_1_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "MetaArgList_1_0_1", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.COMMA MetaArg
      private fun MetaArgList_1_0_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MetaArgList_1_0_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.COMMA)
        result_ = result_ && MetaArg(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.COMMA?
      private fun MetaArgList_1_0_2(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MetaArgList_1_0_2")) return false
        consumeToken(builder_, AngelscriptTypes.COMMA)
        return true
      }

      /* ********************************************************** */
      // AngelscriptTypes.LPAREN [ MetaArg (AngelscriptTypes.COMMA MetaArg)* AngelscriptTypes.COMMA? ] AngelscriptTypes.RPAREN

      //                        | Expr
      private fun MetaArgValue(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MetaArgValue")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = MetaArgValue_0(builder_, level_ + 1)
        if (!result_) result_ = Expr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.LPAREN [ MetaArg (AngelscriptTypes.COMMA MetaArg)* AngelscriptTypes.COMMA? ] AngelscriptTypes.RPAREN
      private fun MetaArgValue_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MetaArgValue_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.LPAREN)
        result_ = result_ && MetaArgValue_0_1(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.RPAREN)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // [ MetaArg (AngelscriptTypes.COMMA MetaArg)* AngelscriptTypes.COMMA? ]
      private fun MetaArgValue_0_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MetaArgValue_0_1")) return false
        MetaArgValue_0_1_0(builder_, level_ + 1)
        return true
      }

      // MetaArg (AngelscriptTypes.COMMA MetaArg)* AngelscriptTypes.COMMA?
      private fun MetaArgValue_0_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MetaArgValue_0_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = MetaArg(builder_, level_ + 1)
        result_ = result_ && MetaArgValue_0_1_0_1(builder_, level_ + 1)
        result_ = result_ && MetaArgValue_0_1_0_2(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // (AngelscriptTypes.COMMA MetaArg)*
      private fun MetaArgValue_0_1_0_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MetaArgValue_0_1_0_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!MetaArgValue_0_1_0_1_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "MetaArgValue_0_1_0_1", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.COMMA MetaArg
      private fun MetaArgValue_0_1_0_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MetaArgValue_0_1_0_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.COMMA)
        result_ = result_ && MetaArg(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.COMMA?
      private fun MetaArgValue_0_1_0_2(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MetaArgValue_0_1_0_2")) return false
        consumeToken(builder_, AngelscriptTypes.COMMA)
        return true
      }

      /* ********************************************************** */
      // UFunctionDecl? AngelscriptTypes.MIXIN_KW FunctionDecl
      private fun MixinDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MixinDecl")) return false
        if (!nextTokenIs(builder_, "<mixin decl>", AngelscriptTypes.MIXIN_KW, AngelscriptTypes.UFUNCTION_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.MIXIN_DECL, "<mixin decl>")
        result_ = MixinDecl_0(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.MIXIN_KW)
        result_ = result_ && FunctionDecl(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // UFunctionDecl?
      private fun MixinDecl_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MixinDecl_0")) return false
        UFunctionDecl(builder_, level_ + 1)
        return true
      }

      /* ********************************************************** */
      // UnaryExpr ((AngelscriptTypes.MUL | AngelscriptTypes.POWER | AngelscriptTypes.DIV | AngelscriptTypes.REM) UnaryExpr)*
      private fun MultiplyExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MultiplyExpr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _COLLAPSE_, AngelscriptTypes.MULTIPLY_EXPR, "<multiply expr>")
        result_ = UnaryExpr(builder_, level_ + 1)
        result_ = result_ && MultiplyExpr_1(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // ((AngelscriptTypes.MUL | AngelscriptTypes.POWER | AngelscriptTypes.DIV | AngelscriptTypes.REM) UnaryExpr)*
      private fun MultiplyExpr_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MultiplyExpr_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!MultiplyExpr_1_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "MultiplyExpr_1", pos_)) break
        }
        return true
      }

      // (AngelscriptTypes.MUL | AngelscriptTypes.POWER | AngelscriptTypes.DIV | AngelscriptTypes.REM) UnaryExpr
      private fun MultiplyExpr_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MultiplyExpr_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = MultiplyExpr_1_0_0(builder_, level_ + 1)
        result_ = result_ && UnaryExpr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.MUL | AngelscriptTypes.POWER | AngelscriptTypes.DIV | AngelscriptTypes.REM
      private fun MultiplyExpr_1_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "MultiplyExpr_1_0_0")) return false
        var result_: Boolean
        result_ = consumeToken(builder_, AngelscriptTypes.MUL)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.POWER)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.DIV)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.REM)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.NAMESPACE_KW NamespacePath AngelscriptTypes.LBRACE NamespaceItem_with_recover* AngelscriptTypes.RBRACE AngelscriptTypes.SEMICOLON?
      private fun NamespaceDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "NamespaceDecl")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.NAMESPACE_KW)) return false
        var result_: Boolean
        var pinned_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.NAMESPACE_DECL, null)
        result_ = consumeToken(builder_, AngelscriptTypes.NAMESPACE_KW)
        pinned_ = result_ // pin = 1
        result_ = result_ && report_error_(builder_, NamespacePath(builder_, level_ + 1))
        result_ = pinned_ && report_error_(builder_, consumeToken(builder_, AngelscriptTypes.LBRACE)) && result_
        result_ = pinned_ && report_error_(builder_, NamespaceDecl_3(builder_, level_ + 1)) && result_
        result_ = pinned_ && report_error_(builder_, consumeToken(builder_, AngelscriptTypes.RBRACE)) && result_
        result_ = pinned_ && NamespaceDecl_5(builder_, level_ + 1) && result_
        exit_section_(builder_, level_, marker_, result_, pinned_, null)
        return result_ || pinned_
      }

      // NamespaceItem_with_recover*
      private fun NamespaceDecl_3(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "NamespaceDecl_3")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!NamespaceItem_with_recover(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "NamespaceDecl_3", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.SEMICOLON?
      private fun NamespaceDecl_5(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "NamespaceDecl_5")) return false
        consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        return true
      }

      /* ********************************************************** */
      // !(AngelscriptTypes.RBRACE | AngelscriptTypes.CLASS | AngelscriptTypes.STRUCT | AngelscriptTypes.ENUM | AngelscriptTypes.INTERFACE_KW | AngelscriptTypes.EVENT_KW | AngelscriptTypes.DELEGATE_KW | AngelscriptTypes.MIXIN_KW | AngelscriptTypes.ASSET_KW

      //                                   | AngelscriptTypes.TYPEDEF_KW | AngelscriptTypes.NAMESPACE_KW | AngelscriptTypes.UCLASS_KW | AngelscriptTypes.USTRUCT_KW | AngelscriptTypes.UENUM_KW | AngelscriptTypes.UINTERFACE_KW | AngelscriptTypes.UFUNCTION_KW | AngelscriptTypes.UPROPERTY_KW

      //                                   | AngelscriptTypes.DEFAULT_KW | AngelscriptTypes.IDENTIFIER | AngelscriptTypes.CONST | AngelscriptTypes.VOID_KW

      //                                   | AngelscriptTypes.INT_KW | AngelscriptTypes.INT8_KW | AngelscriptTypes.INT16_KW | AngelscriptTypes.INT32_KW | AngelscriptTypes.INT64_KW

      //                                   | AngelscriptTypes.UINT_KW | AngelscriptTypes.UINT8_KW | AngelscriptTypes.UINT16_KW | AngelscriptTypes.UINT32_KW | AngelscriptTypes.UINT64_KW

      //                                   | AngelscriptTypes.FLOAT_KW | AngelscriptTypes.DOUBLE_KW | AngelscriptTypes.BOOL_KW | AngelscriptTypes.AUTO_KW | AngelscriptTypes.AUTO

      //                                   | AngelscriptTypes.PP_IF | AngelscriptTypes.PP_ELIF | AngelscriptTypes.PP_ELSE | AngelscriptTypes.PP_ENDIF | AngelscriptTypes.PP_DEFINE | <<eof>>)
      private fun NamespaceItem_recover(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "NamespaceItem_recover")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NOT_)
        result_ = !NamespaceItem_recover_0(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // AngelscriptTypes.RBRACE | AngelscriptTypes.CLASS | AngelscriptTypes.STRUCT | AngelscriptTypes.ENUM | AngelscriptTypes.INTERFACE_KW | AngelscriptTypes.EVENT_KW | AngelscriptTypes.DELEGATE_KW | AngelscriptTypes.MIXIN_KW | AngelscriptTypes.ASSET_KW

      //                                   | AngelscriptTypes.TYPEDEF_KW | AngelscriptTypes.NAMESPACE_KW | AngelscriptTypes.UCLASS_KW | AngelscriptTypes.USTRUCT_KW | AngelscriptTypes.UENUM_KW | AngelscriptTypes.UINTERFACE_KW | AngelscriptTypes.UFUNCTION_KW | AngelscriptTypes.UPROPERTY_KW

      //                                   | AngelscriptTypes.DEFAULT_KW | AngelscriptTypes.IDENTIFIER | AngelscriptTypes.CONST | AngelscriptTypes.VOID_KW

      //                                   | AngelscriptTypes.INT_KW | AngelscriptTypes.INT8_KW | AngelscriptTypes.INT16_KW | AngelscriptTypes.INT32_KW | AngelscriptTypes.INT64_KW

      //                                   | AngelscriptTypes.UINT_KW | AngelscriptTypes.UINT8_KW | AngelscriptTypes.UINT16_KW | AngelscriptTypes.UINT32_KW | AngelscriptTypes.UINT64_KW

      //                                   | AngelscriptTypes.FLOAT_KW | AngelscriptTypes.DOUBLE_KW | AngelscriptTypes.BOOL_KW | AngelscriptTypes.AUTO_KW | AngelscriptTypes.AUTO

      //                                   | AngelscriptTypes.PP_IF | AngelscriptTypes.PP_ELIF | AngelscriptTypes.PP_ELSE | AngelscriptTypes.PP_ENDIF | AngelscriptTypes.PP_DEFINE | <<eof>>
      private fun NamespaceItem_recover_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "NamespaceItem_recover_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.RBRACE)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.CLASS)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.STRUCT)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.ENUM)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INTERFACE_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.EVENT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.DELEGATE_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.MIXIN_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.ASSET_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.TYPEDEF_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.NAMESPACE_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UCLASS_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.USTRUCT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UENUM_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINTERFACE_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UFUNCTION_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UPROPERTY_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.DEFAULT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.IDENTIFIER)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.CONST)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.VOID_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT8_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT16_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT32_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT64_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT8_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT16_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT32_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT64_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.FLOAT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.DOUBLE_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.BOOL_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.AUTO_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.AUTO)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_IF)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_ELIF)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_ELSE)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_ENDIF)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_DEFINE)
        if (!result_) result_ = eof(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // !(AngelscriptTypes.RBRACE | <<eof>>) Item
      private fun NamespaceItem_with_recover(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "NamespaceItem_with_recover")) return false
        var result_: Boolean
        var pinned_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_)
        result_ = NamespaceItem_with_recover_0(builder_, level_ + 1)
        pinned_ = result_ // pin = 1
        result_ = result_ && Item(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, pinned_, Parser { b, l -> NamespaceItem_recover(b, l) })
        return result_ || pinned_
      }

      // !(AngelscriptTypes.RBRACE | <<eof>>)
      private fun NamespaceItem_with_recover_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "NamespaceItem_with_recover_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NOT_)
        result_ = !NamespaceItem_with_recover_0_0(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // AngelscriptTypes.RBRACE | <<eof>>
      private fun NamespaceItem_with_recover_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "NamespaceItem_with_recover_0_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.RBRACE)
        if (!result_) result_ = eof(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // identifier (AngelscriptTypes.COLONCOLON identifier)*
      private fun NamespacePath(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "NamespacePath")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.IDENTIFIER)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = identifier(builder_, level_ + 1)
        result_ = result_ && NamespacePath_1(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // (AngelscriptTypes.COLONCOLON identifier)*
      private fun NamespacePath_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "NamespacePath_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!NamespacePath_1_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "NamespacePath_1", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.COLONCOLON identifier
      private fun NamespacePath_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "NamespacePath_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.COLONCOLON)
        result_ = result_ && identifier(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
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
      private fun NonCaseStatement(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "NonCaseStatement")) return false
        var result_: Boolean
        result_ = CompoundStatement(builder_, level_ + 1)
        if (!result_) result_ = ExpressionStatement(builder_, level_ + 1)
        if (!result_) result_ = IfStatement(builder_, level_ + 1)
        if (!result_) result_ = SwitchStatement(builder_, level_ + 1)
        if (!result_) result_ = DoStatement(builder_, level_ + 1)
        if (!result_) result_ = WhileStatement(builder_, level_ + 1)
        if (!result_) result_ = ForStatement(builder_, level_ + 1)
        if (!result_) result_ = ForeachStatement(builder_, level_ + 1)
        if (!result_) result_ = ReturnStatement(builder_, level_ + 1)
        if (!result_) result_ = BreakStatement(builder_, level_ + 1)
        if (!result_) result_ = ContinueStatement(builder_, level_ + 1)
        if (!result_) result_ = VariableDecl(builder_, level_ + 1)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.PP_DEFINE
      private fun PP_DefineDirective(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "PP_DefineDirective")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.PP_DEFINE)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.PP_DEFINE)
        exit_section_(builder_, marker_, AngelscriptTypes.PP_DEFINE_DIRECTIVE, result_)
        return result_
      }

      /* ********************************************************** */
      // DeclarationSpecifier identifier [ AngelscriptTypes.EQ ConditionalExpr ]
      private fun ParameterDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ParameterDecl")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.PARAMETER_DECL, "<parameter decl>")
        result_ = DeclarationSpecifier(builder_, level_ + 1)
        result_ = result_ && identifier(builder_, level_ + 1)
        result_ = result_ && ParameterDecl_2(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // [ AngelscriptTypes.EQ ConditionalExpr ]
      private fun ParameterDecl_2(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ParameterDecl_2")) return false
        ParameterDecl_2_0(builder_, level_ + 1)
        return true
      }

      // AngelscriptTypes.EQ ConditionalExpr
      private fun ParameterDecl_2_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ParameterDecl_2_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.EQ)
        result_ = result_ && ConditionalExpr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.LPAREN [ AngelscriptTypes.VOID_KW | ParameterDecl (AngelscriptTypes.COMMA ParameterDecl)* AngelscriptTypes.COMMA? ] AngelscriptTypes.RPAREN
      private fun ParameterList(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ParameterList")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.LPAREN)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.LPAREN)
        result_ = result_ && ParameterList_1(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.RPAREN)
        exit_section_(builder_, marker_, AngelscriptTypes.PARAMETER_LIST, result_)
        return result_
      }

      // [ AngelscriptTypes.VOID_KW | ParameterDecl (AngelscriptTypes.COMMA ParameterDecl)* AngelscriptTypes.COMMA? ]
      private fun ParameterList_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ParameterList_1")) return false
        ParameterList_1_0(builder_, level_ + 1)
        return true
      }

      // AngelscriptTypes.VOID_KW | ParameterDecl (AngelscriptTypes.COMMA ParameterDecl)* AngelscriptTypes.COMMA?
      private fun ParameterList_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ParameterList_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.VOID_KW)
        if (!result_) result_ = ParameterList_1_0_1(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // ParameterDecl (AngelscriptTypes.COMMA ParameterDecl)* AngelscriptTypes.COMMA?
      private fun ParameterList_1_0_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ParameterList_1_0_1")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = ParameterDecl(builder_, level_ + 1)
        result_ = result_ && ParameterList_1_0_1_1(builder_, level_ + 1)
        result_ = result_ && ParameterList_1_0_1_2(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // (AngelscriptTypes.COMMA ParameterDecl)*
      private fun ParameterList_1_0_1_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ParameterList_1_0_1_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!ParameterList_1_0_1_1_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "ParameterList_1_0_1_1", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.COMMA ParameterDecl
      private fun ParameterList_1_0_1_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ParameterList_1_0_1_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.COMMA)
        result_ = result_ && ParameterDecl(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.COMMA?
      private fun ParameterList_1_0_1_2(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ParameterList_1_0_1_2")) return false
        consumeToken(builder_, AngelscriptTypes.COMMA)
        return true
      }

      /* ********************************************************** */
      // AngelscriptTypes.LPAREN Expr AngelscriptTypes.RPAREN
      private fun ParenthesizedExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ParenthesizedExpr")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.LPAREN)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.LPAREN)
        result_ = result_ && Expr(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.RPAREN)
        exit_section_(builder_, marker_, AngelscriptTypes.PARENTHESIZED_EXPR, result_)
        return result_
      }

      /* ********************************************************** */
      // PrimaryExpr PostfixPart* PostfixUpdate?
      private fun PostfixExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "PostfixExpr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _COLLAPSE_, AngelscriptTypes.POSTFIX_EXPR, "<postfix expr>")
        result_ = PrimaryExpr(builder_, level_ + 1)
        result_ = result_ && PostfixExpr_1(builder_, level_ + 1)
        result_ = result_ && PostfixExpr_2(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // PostfixPart*
      private fun PostfixExpr_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "PostfixExpr_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!PostfixPart(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "PostfixExpr_1", pos_)) break
        }
        return true
      }

      // PostfixUpdate?
      private fun PostfixExpr_2(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "PostfixExpr_2")) return false
        PostfixUpdate(builder_, level_ + 1)
        return true
      }

      /* ********************************************************** */
      // ScopeSuffix

      //                       | FieldSuffix

      //                       | SubscriptSuffix

      //                       | CallSuffix
      private fun PostfixPart(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "PostfixPart")) return false
        var result_: Boolean
        result_ = ScopeSuffix(builder_, level_ + 1)
        if (!result_) result_ = FieldSuffix(builder_, level_ + 1)
        if (!result_) result_ = SubscriptSuffix(builder_, level_ + 1)
        if (!result_) result_ = CallSuffix(builder_, level_ + 1)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.MINUSMINUS | AngelscriptTypes.PLUSPLUS
      private fun PostfixUpdate(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "PostfixUpdate")) return false
        if (!nextTokenIs(builder_, "", AngelscriptTypes.MINUSMINUS, AngelscriptTypes.PLUSPLUS)) return false
        var result_: Boolean
        result_ = consumeToken(builder_, AngelscriptTypes.MINUSMINUS)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PLUSPLUS)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.AT | AngelscriptTypes.NOT_KW | AngelscriptTypes.EXCL | AngelscriptTypes.TILDE | AngelscriptTypes.MINUS | AngelscriptTypes.PLUS
      private fun PrefixOp(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "PrefixOp")) return false
        var result_: Boolean
        result_ = consumeToken(builder_, AngelscriptTypes.AT)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.NOT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.EXCL)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.TILDE)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.MINUS)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PLUS)
        return result_
      }

      /* ********************************************************** */
      // (AngelscriptTypes.MINUSMINUS | AngelscriptTypes.PLUSPLUS) UnaryExpr
      private fun PrefixUpdateExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "PrefixUpdateExpr")) return false
        if (!nextTokenIs(builder_, "", AngelscriptTypes.MINUSMINUS, AngelscriptTypes.PLUSPLUS)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = PrefixUpdateExpr_0(builder_, level_ + 1)
        result_ = result_ && UnaryExpr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.MINUSMINUS | AngelscriptTypes.PLUSPLUS
      private fun PrefixUpdateExpr_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "PrefixUpdateExpr_0")) return false
        var result_: Boolean
        result_ = consumeToken(builder_, AngelscriptTypes.MINUSMINUS)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PLUSPLUS)
        return result_
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
      private fun PrimaryExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "PrimaryExpr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _COLLAPSE_, AngelscriptTypes.PRIMARY_EXPR, "<primary expr>")
        result_ = VariableAccessExpr(builder_, level_ + 1)
        if (!result_) result_ = InitListExpr(builder_, level_ + 1)
        if (!result_) result_ = StringConcatExpr(builder_, level_ + 1)
        if (!result_) result_ = FStringExpr(builder_, level_ + 1)
        if (!result_) result_ = number_literal(builder_, level_ + 1)
        if (!result_) result_ = string_literal(builder_, level_ + 1)
        if (!result_) result_ = bool_literal(builder_, level_ + 1)
        if (!result_) result_ = null_literal(builder_, level_ + 1)
        if (!result_) result_ = ParenthesizedExpr(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.VOID_KW

      //                         | AngelscriptTypes.INT_KW | AngelscriptTypes.INT8_KW | AngelscriptTypes.INT16_KW | AngelscriptTypes.INT32_KW | AngelscriptTypes.INT64_KW

      //                         | AngelscriptTypes.UINT_KW | AngelscriptTypes.UINT8_KW | AngelscriptTypes.UINT16_KW | AngelscriptTypes.UINT32_KW | AngelscriptTypes.UINT64_KW

      //                         | AngelscriptTypes.FLOAT_KW | AngelscriptTypes.DOUBLE_KW | AngelscriptTypes.BOOL_KW
      private fun PrimitiveType(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "PrimitiveType")) return false
        var result_: Boolean
        result_ = consumeToken(builder_, AngelscriptTypes.VOID_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT8_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT16_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT32_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT64_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT8_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT16_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT32_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT64_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.FLOAT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.DOUBLE_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.BOOL_KW)
        return result_
      }

      /* ********************************************************** */
      // ShiftExpr ((AngelscriptTypes.GT | AngelscriptTypes.GTEQ | AngelscriptTypes.LTEQ | AngelscriptTypes.LT | AngelscriptTypes.IS_KW | AngelscriptTypes.EXCL AngelscriptTypes.IS_KW) ShiftExpr)*
      private fun RelationalExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "RelationalExpr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _COLLAPSE_, AngelscriptTypes.RELATIONAL_EXPR, "<relational expr>")
        result_ = ShiftExpr(builder_, level_ + 1)
        result_ = result_ && RelationalExpr_1(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // ((AngelscriptTypes.GT | AngelscriptTypes.GTEQ | AngelscriptTypes.LTEQ | AngelscriptTypes.LT | AngelscriptTypes.IS_KW | AngelscriptTypes.EXCL AngelscriptTypes.IS_KW) ShiftExpr)*
      private fun RelationalExpr_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "RelationalExpr_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!RelationalExpr_1_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "RelationalExpr_1", pos_)) break
        }
        return true
      }

      // (AngelscriptTypes.GT | AngelscriptTypes.GTEQ | AngelscriptTypes.LTEQ | AngelscriptTypes.LT | AngelscriptTypes.IS_KW | AngelscriptTypes.EXCL AngelscriptTypes.IS_KW) ShiftExpr
      private fun RelationalExpr_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "RelationalExpr_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = RelationalExpr_1_0_0(builder_, level_ + 1)
        result_ = result_ && ShiftExpr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.GT | AngelscriptTypes.GTEQ | AngelscriptTypes.LTEQ | AngelscriptTypes.LT | AngelscriptTypes.IS_KW | AngelscriptTypes.EXCL AngelscriptTypes.IS_KW
      private fun RelationalExpr_1_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "RelationalExpr_1_0_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.GT)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.GTEQ)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.LTEQ)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.LT)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.IS_KW)
        if (!result_) result_ = parseTokens(builder_, 0, AngelscriptTypes.EXCL, AngelscriptTypes.IS_KW)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.RETURN Expr? AngelscriptTypes.SEMICOLON
      private fun ReturnStatement(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ReturnStatement")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.RETURN)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.RETURN)
        result_ = result_ && ReturnStatement_1(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        exit_section_(builder_, marker_, AngelscriptTypes.RETURN_STATEMENT, result_)
        return result_
      }

      // Expr?
      private fun ReturnStatement_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ReturnStatement_1")) return false
        Expr(builder_, level_ + 1)
        return true
      }

      /* ********************************************************** */
      // (identifier TypeArgument? AngelscriptTypes.COLONCOLON)+ | AngelscriptTypes.COLONCOLON
      private fun ScopeRef(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ScopeRef")) return false
        if (!nextTokenIs(builder_, "<scope ref>", AngelscriptTypes.COLONCOLON, AngelscriptTypes.IDENTIFIER)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.SCOPE_REF, "<scope ref>")
        result_ = ScopeRef_0(builder_, level_ + 1)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.COLONCOLON)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // (identifier TypeArgument? AngelscriptTypes.COLONCOLON)+
      private fun ScopeRef_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ScopeRef_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = ScopeRef_0_0(builder_, level_ + 1)
        while (result_) {
          val pos_ = current_position_(builder_)
          if (!ScopeRef_0_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "ScopeRef_0", pos_)) break
        }
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // identifier TypeArgument? AngelscriptTypes.COLONCOLON
      private fun ScopeRef_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ScopeRef_0_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = identifier(builder_, level_ + 1)
        result_ = result_ && ScopeRef_0_0_1(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.COLONCOLON)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // TypeArgument?
      private fun ScopeRef_0_0_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ScopeRef_0_0_1")) return false
        TypeArgument(builder_, level_ + 1)
        return true
      }

      /* ********************************************************** */
      // AngelscriptTypes.COLONCOLON identifier
      private fun ScopeSuffix(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ScopeSuffix")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.COLONCOLON)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.COLONCOLON)
        result_ = result_ && identifier(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // ScriptItem*
      private fun Script(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "Script")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!ScriptItem(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "Script", pos_)) break
        }
        return true
      }

      /* ********************************************************** */
      // !(<<eof>>) Item
      private fun ScriptItem(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ScriptItem")) return false
        var result_: Boolean
        var pinned_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_)
        result_ = ScriptItem_0(builder_, level_ + 1)
        pinned_ = result_ // pin = 1
        result_ = result_ && Item(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, pinned_, Parser { b, l -> ScriptItem_recover(b, l) })
        return result_ || pinned_
      }

      // !(<<eof>>)
      private fun ScriptItem_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ScriptItem_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NOT_)
        result_ = !ScriptItem_0_0(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // <<eof>>
      private fun ScriptItem_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        return eof(builder_, level_ + 1)
      }

      /* ********************************************************** */
      // !(AngelscriptTypes.CLASS | AngelscriptTypes.STRUCT | AngelscriptTypes.ENUM | AngelscriptTypes.INTERFACE_KW | AngelscriptTypes.EVENT_KW | AngelscriptTypes.DELEGATE_KW | AngelscriptTypes.MIXIN_KW | AngelscriptTypes.ASSET_KW

      //                                | AngelscriptTypes.TYPEDEF_KW | AngelscriptTypes.NAMESPACE_KW | AngelscriptTypes.UCLASS_KW | AngelscriptTypes.USTRUCT_KW | AngelscriptTypes.UENUM_KW | AngelscriptTypes.UINTERFACE_KW | AngelscriptTypes.UFUNCTION_KW | AngelscriptTypes.UPROPERTY_KW

      //                                | AngelscriptTypes.DEFAULT_KW | AngelscriptTypes.IDENTIFIER | AngelscriptTypes.CONST | AngelscriptTypes.VOID_KW

      //                                | AngelscriptTypes.INT_KW | AngelscriptTypes.INT8_KW | AngelscriptTypes.INT16_KW | AngelscriptTypes.INT32_KW | AngelscriptTypes.INT64_KW

      //                                | AngelscriptTypes.UINT_KW | AngelscriptTypes.UINT8_KW | AngelscriptTypes.UINT16_KW | AngelscriptTypes.UINT32_KW | AngelscriptTypes.UINT64_KW

      //                                | AngelscriptTypes.FLOAT_KW | AngelscriptTypes.DOUBLE_KW | AngelscriptTypes.BOOL_KW | AngelscriptTypes.AUTO_KW | AngelscriptTypes.AUTO

      //                                | AngelscriptTypes.PP_IF | AngelscriptTypes.PP_ELIF | AngelscriptTypes.PP_ELSE | AngelscriptTypes.PP_ENDIF | AngelscriptTypes.PP_DEFINE | <<eof>>)
      private fun ScriptItem_recover(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ScriptItem_recover")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NOT_)
        result_ = !ScriptItem_recover_0(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // AngelscriptTypes.CLASS | AngelscriptTypes.STRUCT | AngelscriptTypes.ENUM | AngelscriptTypes.INTERFACE_KW | AngelscriptTypes.EVENT_KW | AngelscriptTypes.DELEGATE_KW | AngelscriptTypes.MIXIN_KW | AngelscriptTypes.ASSET_KW

      //                                | AngelscriptTypes.TYPEDEF_KW | AngelscriptTypes.NAMESPACE_KW | AngelscriptTypes.UCLASS_KW | AngelscriptTypes.USTRUCT_KW | AngelscriptTypes.UENUM_KW | AngelscriptTypes.UINTERFACE_KW | AngelscriptTypes.UFUNCTION_KW | AngelscriptTypes.UPROPERTY_KW

      //                                | AngelscriptTypes.DEFAULT_KW | AngelscriptTypes.IDENTIFIER | AngelscriptTypes.CONST | AngelscriptTypes.VOID_KW

      //                                | AngelscriptTypes.INT_KW | AngelscriptTypes.INT8_KW | AngelscriptTypes.INT16_KW | AngelscriptTypes.INT32_KW | AngelscriptTypes.INT64_KW

      //                                | AngelscriptTypes.UINT_KW | AngelscriptTypes.UINT8_KW | AngelscriptTypes.UINT16_KW | AngelscriptTypes.UINT32_KW | AngelscriptTypes.UINT64_KW

      //                                | AngelscriptTypes.FLOAT_KW | AngelscriptTypes.DOUBLE_KW | AngelscriptTypes.BOOL_KW | AngelscriptTypes.AUTO_KW | AngelscriptTypes.AUTO

      //                                | AngelscriptTypes.PP_IF | AngelscriptTypes.PP_ELIF | AngelscriptTypes.PP_ELSE | AngelscriptTypes.PP_ENDIF | AngelscriptTypes.PP_DEFINE | <<eof>>
      private fun ScriptItem_recover_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ScriptItem_recover_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.CLASS)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.STRUCT)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.ENUM)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INTERFACE_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.EVENT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.DELEGATE_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.MIXIN_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.ASSET_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.TYPEDEF_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.NAMESPACE_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UCLASS_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.USTRUCT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UENUM_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINTERFACE_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UFUNCTION_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UPROPERTY_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.DEFAULT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.IDENTIFIER)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.CONST)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.VOID_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT8_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT16_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT32_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INT64_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT8_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT16_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT32_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.UINT64_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.FLOAT_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.DOUBLE_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.BOOL_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.AUTO_KW)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.AUTO)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_IF)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_ELIF)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_ELSE)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_ENDIF)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.PP_DEFINE)
        if (!result_) result_ = eof(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // AddExpr ((AngelscriptTypes.LTLT | AngelscriptTypes.GT AngelscriptTypes.GT) AddExpr)*
      private fun ShiftExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ShiftExpr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _COLLAPSE_, AngelscriptTypes.SHIFT_EXPR, "<shift expr>")
        result_ = AddExpr(builder_, level_ + 1)
        result_ = result_ && ShiftExpr_1(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // ((AngelscriptTypes.LTLT | AngelscriptTypes.GT AngelscriptTypes.GT) AddExpr)*
      private fun ShiftExpr_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ShiftExpr_1")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!ShiftExpr_1_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "ShiftExpr_1", pos_)) break
        }
        return true
      }

      // (AngelscriptTypes.LTLT | AngelscriptTypes.GT AngelscriptTypes.GT) AddExpr
      private fun ShiftExpr_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ShiftExpr_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = ShiftExpr_1_0_0(builder_, level_ + 1)
        result_ = result_ && AddExpr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.LTLT | AngelscriptTypes.GT AngelscriptTypes.GT
      private fun ShiftExpr_1_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "ShiftExpr_1_0_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.LTLT)
        if (!result_) result_ = parseTokens(builder_, 0, AngelscriptTypes.GT, AngelscriptTypes.GT)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // CaseStatement | NonCaseStatement
      private fun Statement(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "Statement")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.STATEMENT, "<statement>")
        result_ = CaseStatement(builder_, level_ + 1)
        if (!result_) result_ = NonCaseStatement(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      /* ********************************************************** */
      // string_literal string_literal+
      private fun StringConcatExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "StringConcatExpr")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.STRING_LITERAL)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = string_literal(builder_, level_ + 1)
        result_ = result_ && StringConcatExpr_1(builder_, level_ + 1)
        exit_section_(builder_, marker_, AngelscriptTypes.STRING_CONCAT_EXPR, result_)
        return result_
      }

      // string_literal+
      private fun StringConcatExpr_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "StringConcatExpr_1")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = string_literal(builder_, level_ + 1)
        while (result_) {
          val pos_ = current_position_(builder_)
          if (!string_literal(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "StringConcatExpr_1", pos_)) break
        }
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // UStructDecl? AngelscriptTypes.STRUCT identifier AngelscriptTypes.LBRACE StructMember_with_recover* AngelscriptTypes.RBRACE AngelscriptTypes.SEMICOLON?
      private fun StructDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "StructDecl")) return false
        if (!nextTokenIs(builder_, "<struct decl>", AngelscriptTypes.STRUCT, AngelscriptTypes.USTRUCT_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.STRUCT_DECL, "<struct decl>")
        result_ = StructDecl_0(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.STRUCT)
        result_ = result_ && identifier(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.LBRACE)
        result_ = result_ && StructDecl_4(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.RBRACE)
        result_ = result_ && StructDecl_6(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // UStructDecl?
      private fun StructDecl_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "StructDecl_0")) return false
        UStructDecl(builder_, level_ + 1)
        return true
      }

      // StructMember_with_recover*
      private fun StructDecl_4(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "StructDecl_4")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!StructMember_with_recover(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "StructDecl_4", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.SEMICOLON?
      private fun StructDecl_6(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "StructDecl_6")) return false
        consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        return true
      }

      /* ********************************************************** */
      // IfDefBlock | ConstructorDecl | FunctionDecl | DefaultValueDecl | VariableDecl | Comment
      private fun StructMember(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "StructMember")) return false
        var result_: Boolean
        result_ = IfDefBlock(builder_, level_ + 1)
        if (!result_) result_ = ConstructorDecl(builder_, level_ + 1)
        if (!result_) result_ = FunctionDecl(builder_, level_ + 1)
        if (!result_) result_ = DefaultValueDecl(builder_, level_ + 1)
        if (!result_) result_ = VariableDecl(builder_, level_ + 1)
        if (!result_) result_ = Comment(builder_, level_ + 1)
        return result_
      }

      /* ********************************************************** */
      // !(AngelscriptTypes.RBRACE | <<eof>>) StructMember
      private fun StructMember_with_recover(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "StructMember_with_recover")) return false
        var result_: Boolean
        var pinned_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_)
        result_ = StructMember_with_recover_0(builder_, level_ + 1)
        pinned_ = result_ // pin = 1
        result_ = result_ && StructMember(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, pinned_, Parser { b, l -> ClassMember_recover(b, l) })
        return result_ || pinned_
      }

      // !(AngelscriptTypes.RBRACE | <<eof>>)
      private fun StructMember_with_recover_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "StructMember_with_recover_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NOT_)
        result_ = !StructMember_with_recover_0_0(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // AngelscriptTypes.RBRACE | <<eof>>
      private fun StructMember_with_recover_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "StructMember_with_recover_0_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.RBRACE)
        if (!result_) result_ = eof(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.LBRACK Expr AngelscriptTypes.RBRACK
      private fun SubscriptSuffix(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "SubscriptSuffix")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.LBRACK)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.LBRACK)
        result_ = result_ && Expr(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.RBRACK)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.SWITCH_KW ParenthesizedExpr CompoundStatement
      private fun SwitchStatement(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "SwitchStatement")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.SWITCH_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.SWITCH_KW)
        result_ = result_ && ParenthesizedExpr(builder_, level_ + 1)
        result_ = result_ && CompoundStatement(builder_, level_ + 1)
        exit_section_(builder_, marker_, AngelscriptTypes.SWITCH_STATEMENT, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.LT TypeRef (AngelscriptTypes.COMMA TypeRef)* AngelscriptTypes.GT
      private fun TypeArgument(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "TypeArgument")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.LT)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.LT)
        result_ = result_ && TypeRef(builder_, level_ + 1)
        result_ = result_ && TypeArgument_2(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.GT)
        exit_section_(builder_, marker_, AngelscriptTypes.TYPE_ARGUMENT, result_)
        return result_
      }

      // (AngelscriptTypes.COMMA TypeRef)*
      private fun TypeArgument_2(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "TypeArgument_2")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!TypeArgument_2_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "TypeArgument_2", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.COMMA TypeRef
      private fun TypeArgument_2_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "TypeArgument_2_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.COMMA)
        result_ = result_ && TypeRef(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.AND (AngelscriptTypes.IN | AngelscriptTypes.OUT | AngelscriptTypes.INOUT_KW)?
      private fun TypeMod(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "TypeMod")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.AND)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.AND)
        result_ = result_ && TypeMod_1(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // (AngelscriptTypes.IN | AngelscriptTypes.OUT | AngelscriptTypes.INOUT_KW)?
      private fun TypeMod_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "TypeMod_1")) return false
        TypeMod_1_0(builder_, level_ + 1)
        return true
      }

      // AngelscriptTypes.IN | AngelscriptTypes.OUT | AngelscriptTypes.INOUT_KW
      private fun TypeMod_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "TypeMod_1_0")) return false
        var result_: Boolean
        result_ = consumeToken(builder_, AngelscriptTypes.IN)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.OUT)
        if (!result_) result_ = consumeToken(builder_, AngelscriptTypes.INOUT_KW)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.CONST?

      //             ScopeRef?

      //             DataType

      //             TypeArgument*

      //             ( AngelscriptTypes.LBRACK AngelscriptTypes.RBRACK | AngelscriptTypes.AT AngelscriptTypes.CONST? )*
      private fun TypeRef(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "TypeRef")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.TYPE_REF, "<type ref>")
        result_ = TypeRef_0(builder_, level_ + 1)
        result_ = result_ && TypeRef_1(builder_, level_ + 1)
        result_ = result_ && DataType(builder_, level_ + 1)
        result_ = result_ && TypeRef_3(builder_, level_ + 1)
        result_ = result_ && TypeRef_4(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // AngelscriptTypes.CONST?
      private fun TypeRef_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "TypeRef_0")) return false
        consumeToken(builder_, AngelscriptTypes.CONST)
        return true
      }

      // ScopeRef?
      private fun TypeRef_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "TypeRef_1")) return false
        ScopeRef(builder_, level_ + 1)
        return true
      }

      // TypeArgument*
      private fun TypeRef_3(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "TypeRef_3")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!TypeArgument(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "TypeRef_3", pos_)) break
        }
        return true
      }

      // ( AngelscriptTypes.LBRACK AngelscriptTypes.RBRACK | AngelscriptTypes.AT AngelscriptTypes.CONST? )*
      private fun TypeRef_4(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "TypeRef_4")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!TypeRef_4_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "TypeRef_4", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.LBRACK AngelscriptTypes.RBRACK | AngelscriptTypes.AT AngelscriptTypes.CONST?
      private fun TypeRef_4_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "TypeRef_4_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = parseTokens(builder_, 0, AngelscriptTypes.LBRACK, AngelscriptTypes.RBRACK)
        if (!result_) result_ = TypeRef_4_0_1(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.AT AngelscriptTypes.CONST?
      private fun TypeRef_4_0_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "TypeRef_4_0_1")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.AT)
        result_ = result_ && TypeRef_4_0_1_1(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.CONST?
      private fun TypeRef_4_0_1_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "TypeRef_4_0_1_1")) return false
        consumeToken(builder_, AngelscriptTypes.CONST)
        return true
      }

      /* ********************************************************** */
      // AngelscriptTypes.TYPEDEF_KW PrimitiveType identifier AngelscriptTypes.SEMICOLON
      private fun TypedefDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "TypedefDecl")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.TYPEDEF_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.TYPEDEF_KW)
        result_ = result_ && PrimitiveType(builder_, level_ + 1)
        result_ = result_ && identifier(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        exit_section_(builder_, marker_, AngelscriptTypes.TYPEDEF_DECL, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.UCLASS_KW    MetaArgList
      private fun UClassDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "UClassDecl")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.UCLASS_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.UCLASS_KW)
        result_ = result_ && MetaArgList(builder_, level_ + 1)
        exit_section_(builder_, marker_, AngelscriptTypes.U_CLASS_DECL, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.UENUM_KW     MetaArgList
      private fun UEnumDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "UEnumDecl")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.UENUM_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.UENUM_KW)
        result_ = result_ && MetaArgList(builder_, level_ + 1)
        exit_section_(builder_, marker_, AngelscriptTypes.U_ENUM_DECL, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.UFUNCTION_KW MetaArgList
      private fun UFunctionDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "UFunctionDecl")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.UFUNCTION_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.UFUNCTION_KW)
        result_ = result_ && MetaArgList(builder_, level_ + 1)
        exit_section_(builder_, marker_, AngelscriptTypes.U_FUNCTION_DECL, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.UINTERFACE_KW MetaArgList
      private fun UInterfaceDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "UInterfaceDecl")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.UINTERFACE_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.UINTERFACE_KW)
        result_ = result_ && MetaArgList(builder_, level_ + 1)
        exit_section_(builder_, marker_, AngelscriptTypes.U_INTERFACE_DECL, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.UPROPERTY_KW MetaArgList
      private fun UPropertyDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "UPropertyDecl")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.UPROPERTY_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.UPROPERTY_KW)
        result_ = result_ && MetaArgList(builder_, level_ + 1)
        exit_section_(builder_, marker_, AngelscriptTypes.U_PROPERTY_DECL, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.USTRUCT_KW   MetaArgList
      private fun UStructDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "UStructDecl")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.USTRUCT_KW)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.USTRUCT_KW)
        result_ = result_ && MetaArgList(builder_, level_ + 1)
        exit_section_(builder_, marker_, AngelscriptTypes.U_STRUCT_DECL, result_)
        return result_
      }

      /* ********************************************************** */
      // PrefixOp UnaryExpr

      //             | PrefixUpdateExpr

      //             | CastExpr

      //             | PostfixExpr
      private fun UnaryExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "UnaryExpr")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _COLLAPSE_, AngelscriptTypes.UNARY_EXPR, "<unary expr>")
        result_ = UnaryExpr_0(builder_, level_ + 1)
        if (!result_) result_ = PrefixUpdateExpr(builder_, level_ + 1)
        if (!result_) result_ = CastExpr(builder_, level_ + 1)
        if (!result_) result_ = PostfixExpr(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // PrefixOp UnaryExpr
      private fun UnaryExpr_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "UnaryExpr_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = PrefixOp(builder_, level_ + 1)
        result_ = result_ && UnaryExpr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // ScopeRef? identifier
      private fun VariableAccessExpr(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "VariableAccessExpr")) return false
        if (!nextTokenIs(builder_, "<variable access expr>", AngelscriptTypes.COLONCOLON, AngelscriptTypes.IDENTIFIER)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.VARIABLE_ACCESS_EXPR, "<variable access expr>")
        result_ = VariableAccessExpr_0(builder_, level_ + 1)
        result_ = result_ && identifier(builder_, level_ + 1)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // ScopeRef?
      private fun VariableAccessExpr_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "VariableAccessExpr_0")) return false
        ScopeRef(builder_, level_ + 1)
        return true
      }

      /* ********************************************************** */
      // AccessSpecifier?

      //                  UPropertyDecl?

      //                  AccessSpecifier?

      //                  TypeRef

      //                  TypeMod?

      //                  VariableItem (AngelscriptTypes.COMMA VariableItem)*

      //                  AngelscriptTypes.SEMICOLON
      private fun VariableDecl(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "VariableDecl")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_, level_, _NONE_, AngelscriptTypes.VARIABLE_DECL, "<variable decl>")
        result_ = VariableDecl_0(builder_, level_ + 1)
        result_ = result_ && VariableDecl_1(builder_, level_ + 1)
        result_ = result_ && VariableDecl_2(builder_, level_ + 1)
        result_ = result_ && TypeRef(builder_, level_ + 1)
        result_ = result_ && VariableDecl_4(builder_, level_ + 1)
        result_ = result_ && VariableItem(builder_, level_ + 1)
        result_ = result_ && VariableDecl_6(builder_, level_ + 1)
        result_ = result_ && consumeToken(builder_, AngelscriptTypes.SEMICOLON)
        exit_section_(builder_, level_, marker_, result_, false, null)
        return result_
      }

      // AccessSpecifier?
      private fun VariableDecl_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "VariableDecl_0")) return false
        AccessSpecifier(builder_, level_ + 1)
        return true
      }

      // UPropertyDecl?
      private fun VariableDecl_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "VariableDecl_1")) return false
        UPropertyDecl(builder_, level_ + 1)
        return true
      }

      // AccessSpecifier?
      private fun VariableDecl_2(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "VariableDecl_2")) return false
        AccessSpecifier(builder_, level_ + 1)
        return true
      }

      // TypeMod?
      private fun VariableDecl_4(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "VariableDecl_4")) return false
        TypeMod(builder_, level_ + 1)
        return true
      }

      // (AngelscriptTypes.COMMA VariableItem)*
      private fun VariableDecl_6(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "VariableDecl_6")) return false
        while (true) {
          val pos_ = current_position_(builder_)
          if (!VariableDecl_6_0(builder_, level_ + 1)) break
          if (!empty_element_parsed_guard_(builder_, "VariableDecl_6", pos_)) break
        }
        return true
      }

      // AngelscriptTypes.COMMA VariableItem
      private fun VariableDecl_6_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "VariableDecl_6_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.COMMA)
        result_ = result_ && VariableItem(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // identifier ( AngelscriptTypes.EQ Expr | ArgList )?
      private fun VariableItem(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "VariableItem")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.IDENTIFIER)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = identifier(builder_, level_ + 1)
        result_ = result_ && VariableItem_1(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // ( AngelscriptTypes.EQ Expr | ArgList )?
      private fun VariableItem_1(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "VariableItem_1")) return false
        VariableItem_1_0(builder_, level_ + 1)
        return true
      }

      // AngelscriptTypes.EQ Expr | ArgList
      private fun VariableItem_1_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "VariableItem_1_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = VariableItem_1_0_0(builder_, level_ + 1)
        if (!result_) result_ = ArgList(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      // AngelscriptTypes.EQ Expr
      private fun VariableItem_1_0_0(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "VariableItem_1_0_0")) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.EQ)
        result_ = result_ && Expr(builder_, level_ + 1)
        exit_section_(builder_, marker_, null, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.WHILE ParenthesizedExpr Statement
      private fun WhileStatement(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "WhileStatement")) return false
        if (!nextTokenIs(builder_, AngelscriptTypes.WHILE)) return false
        var result_: Boolean
        val marker_ = enter_section_(builder_)
        result_ = consumeToken(builder_, AngelscriptTypes.WHILE)
        result_ = result_ && ParenthesizedExpr(builder_, level_ + 1)
        result_ = result_ && Statement(builder_, level_ + 1)
        exit_section_(builder_, marker_, AngelscriptTypes.WHILE_STATEMENT, result_)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.BOOL_LITERAL
      private fun bool_literal(builder_: PsiBuilder, level_: Int): Boolean {
        return consumeToken(builder_, AngelscriptTypes.BOOL_LITERAL)
      }

      /* ********************************************************** */
      // AngelscriptTypes.FSTRING_TEXT | FStringInterp
      private fun fstring_content(builder_: PsiBuilder, level_: Int): Boolean {
        if (!recursion_guard_(builder_, level_, "fstring_content")) return false
        if (!nextTokenIs(builder_, "", AngelscriptTypes.FSTRING_LBRACE, AngelscriptTypes.FSTRING_TEXT)) return false
        var result_: Boolean
        result_ = consumeToken(builder_, AngelscriptTypes.FSTRING_TEXT)
        if (!result_) result_ = FStringInterp(builder_, level_ + 1)
        return result_
      }

      /* ********************************************************** */
      // AngelscriptTypes.IDENTIFIER
      private fun identifier(builder_: PsiBuilder, level_: Int): Boolean {
        return consumeToken(builder_, AngelscriptTypes.IDENTIFIER)
      }

      /* ********************************************************** */
      // AngelscriptTypes.NULLPTR_KW
      private fun null_literal(builder_: PsiBuilder, level_: Int): Boolean {
        return consumeToken(builder_, AngelscriptTypes.NULLPTR_KW)
      }

      /* ********************************************************** */
      // AngelscriptTypes.NUMBER_LITERAL
      private fun number_literal(builder_: PsiBuilder, level_: Int): Boolean {
        return consumeToken(builder_, AngelscriptTypes.NUMBER_LITERAL)
      }

      /* ********************************************************** */
      // AngelscriptTypes.STRING_LITERAL
      private fun string_literal(builder_: PsiBuilder, level_: Int): Boolean {
        return consumeToken(builder_, AngelscriptTypes.STRING_LITERAL)
      }

    }
}
