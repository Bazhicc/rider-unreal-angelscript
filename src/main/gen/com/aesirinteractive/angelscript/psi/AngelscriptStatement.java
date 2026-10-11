// This is a generated file. Not intended for manual editing.
package com.aesirinteractive.angelscript.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface AngelscriptStatement extends PsiElement {

  @Nullable
  AngelscriptBreakStatement getBreakStatement();

  @Nullable
  AngelscriptCaseStatement getCaseStatement();

  @Nullable
  AngelscriptCompoundStatement getCompoundStatement();

  @Nullable
  AngelscriptContinueStatement getContinueStatement();

  @Nullable
  AngelscriptDoStatement getDoStatement();

  @Nullable
  AngelscriptExpressionStatement getExpressionStatement();

  @Nullable
  AngelscriptForStatement getForStatement();

  @Nullable
  AngelscriptForeachStatement getForeachStatement();

  @Nullable
  AngelscriptIfStatement getIfStatement();

  @Nullable
  AngelscriptReturnStatement getReturnStatement();

  @Nullable
  AngelscriptSwitchStatement getSwitchStatement();

  @Nullable
  AngelscriptVariableDecl getVariableDecl();

  @Nullable
  AngelscriptWhileStatement getWhileStatement();

}
