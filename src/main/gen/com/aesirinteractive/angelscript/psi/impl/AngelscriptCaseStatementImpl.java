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

public class AngelscriptCaseStatementImpl extends ASTWrapperPsiElement implements AngelscriptCaseStatement {

  public AngelscriptCaseStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull AngelscriptVisitor visitor) {
    visitor.visitCaseStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof AngelscriptVisitor) accept((AngelscriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<AngelscriptBreakStatement> getBreakStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptBreakStatement.class);
  }

  @Override
  @NotNull
  public List<AngelscriptCompoundStatement> getCompoundStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptCompoundStatement.class);
  }

  @Override
  @NotNull
  public List<AngelscriptContinueStatement> getContinueStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptContinueStatement.class);
  }

  @Override
  @NotNull
  public List<AngelscriptDoStatement> getDoStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptDoStatement.class);
  }

  @Override
  @Nullable
  public AngelscriptExpr getExpr() {
    return findChildByClass(AngelscriptExpr.class);
  }

  @Override
  @NotNull
  public List<AngelscriptExpressionStatement> getExpressionStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpressionStatement.class);
  }

  @Override
  @NotNull
  public List<AngelscriptForStatement> getForStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptForStatement.class);
  }

  @Override
  @NotNull
  public List<AngelscriptForeachStatement> getForeachStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptForeachStatement.class);
  }

  @Override
  @NotNull
  public List<AngelscriptIfStatement> getIfStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptIfStatement.class);
  }

  @Override
  @NotNull
  public List<AngelscriptReturnStatement> getReturnStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptReturnStatement.class);
  }

  @Override
  @NotNull
  public List<AngelscriptSwitchStatement> getSwitchStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptSwitchStatement.class);
  }

  @Override
  @NotNull
  public List<AngelscriptVariableDecl> getVariableDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptVariableDecl.class);
  }

  @Override
  @NotNull
  public List<AngelscriptWhileStatement> getWhileStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptWhileStatement.class);
  }

}
