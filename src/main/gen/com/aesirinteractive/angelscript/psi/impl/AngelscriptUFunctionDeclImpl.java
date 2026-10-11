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

public class AngelscriptUFunctionDeclImpl extends ASTWrapperPsiElement implements AngelscriptUFunctionDecl {

  public AngelscriptUFunctionDeclImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull AngelscriptVisitor visitor) {
    visitor.visitUFunctionDecl(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof AngelscriptVisitor) accept((AngelscriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<AngelscriptExpr> getExprList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr.class);
  }

}
