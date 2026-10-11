// This is a generated file. Not intended for manual editing.
package com.aesirinteractive.angelscript.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.aesirinteractive.angelscript.AngelscriptTypes.*;
import com.aesirinteractive.angelscript.psi.*;

public class AngelscriptPostfixExprImpl extends AngelscriptExprImpl implements AngelscriptPostfixExpr {

  public AngelscriptPostfixExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull AngelscriptVisitor visitor) {
    visitor.visitPostfixExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof AngelscriptVisitor) accept((AngelscriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<AngelscriptArgList> getArgListList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptArgList.class);
  }

  @Override
  @NotNull
  public List<AngelscriptExpr> getExprList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr.class);
  }

  @Override
  @NotNull
  public List<AngelscriptTypeArgument> getTypeArgumentList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptTypeArgument.class);
  }

}
