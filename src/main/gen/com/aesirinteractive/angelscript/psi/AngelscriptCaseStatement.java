// This is a generated file. Not intended for manual editing.
package com.aesirinteractive.angelscript.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface AngelscriptCaseStatement extends PsiElement {

  @NotNull
  List<AngelscriptBreakStatement> getBreakStatementList();

  @NotNull
  List<AngelscriptCompoundStatement> getCompoundStatementList();

  @NotNull
  List<AngelscriptContinueStatement> getContinueStatementList();

  @NotNull
  List<AngelscriptDoStatement> getDoStatementList();

  @Nullable
  AngelscriptExpr getExpr();

  @NotNull
  List<AngelscriptExpressionStatement> getExpressionStatementList();

  @NotNull
  List<AngelscriptForStatement> getForStatementList();

  @NotNull
  List<AngelscriptForeachStatement> getForeachStatementList();

  @NotNull
  List<AngelscriptIfStatement> getIfStatementList();

  @NotNull
  List<AngelscriptReturnStatement> getReturnStatementList();

  @NotNull
  List<AngelscriptSwitchStatement> getSwitchStatementList();

  @NotNull
  List<AngelscriptVariableDecl> getVariableDeclList();

  @NotNull
  List<AngelscriptWhileStatement> getWhileStatementList();

}
