// This is a generated file. Not intended for manual editing.
package com.aesirinteractive.angelscript.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.aesirinteractive.angelscript.AngelscriptTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.aesirinteractive.angelscript.psi.*;

public class AngelscriptStatementImpl extends ASTWrapperPsiElement implements AngelscriptStatement {

  public AngelscriptStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull AngelscriptVisitor visitor) {
    visitor.visitStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof AngelscriptVisitor) accept((AngelscriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public AngelscriptBreakStatement getBreakStatement() {
    return findChildByClass(AngelscriptBreakStatement.class);
  }

  @Override
  @Nullable
  public AngelscriptCaseStatement getCaseStatement() {
    return findChildByClass(AngelscriptCaseStatement.class);
  }

  @Override
  @Nullable
  public AngelscriptCompoundStatement getCompoundStatement() {
    return findChildByClass(AngelscriptCompoundStatement.class);
  }

  @Override
  @Nullable
  public AngelscriptContinueStatement getContinueStatement() {
    return findChildByClass(AngelscriptContinueStatement.class);
  }

  @Override
  @Nullable
  public AngelscriptDoStatement getDoStatement() {
    return findChildByClass(AngelscriptDoStatement.class);
  }

  @Override
  @Nullable
  public AngelscriptExpressionStatement getExpressionStatement() {
    return findChildByClass(AngelscriptExpressionStatement.class);
  }

  @Override
  @Nullable
  public AngelscriptForStatement getForStatement() {
    return findChildByClass(AngelscriptForStatement.class);
  }

  @Override
  @Nullable
  public AngelscriptForeachStatement getForeachStatement() {
    return findChildByClass(AngelscriptForeachStatement.class);
  }

  @Override
  @Nullable
  public AngelscriptIfStatement getIfStatement() {
    return findChildByClass(AngelscriptIfStatement.class);
  }

  @Override
  @Nullable
  public AngelscriptReturnStatement getReturnStatement() {
    return findChildByClass(AngelscriptReturnStatement.class);
  }

  @Override
  @Nullable
  public AngelscriptSwitchStatement getSwitchStatement() {
    return findChildByClass(AngelscriptSwitchStatement.class);
  }

  @Override
  @Nullable
  public AngelscriptVariableDecl getVariableDecl() {
    return findChildByClass(AngelscriptVariableDecl.class);
  }

  @Override
  @Nullable
  public AngelscriptWhileStatement getWhileStatement() {
    return findChildByClass(AngelscriptWhileStatement.class);
  }

}
