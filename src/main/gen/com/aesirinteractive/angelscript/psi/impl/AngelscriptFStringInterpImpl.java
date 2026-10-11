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

public class AngelscriptFStringInterpImpl extends ASTWrapperPsiElement implements AngelscriptFStringInterp {

  public AngelscriptFStringInterpImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull AngelscriptVisitor visitor) {
    visitor.visitFStringInterp(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof AngelscriptVisitor) accept((AngelscriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public AngelscriptExpr getExpr() {
    return findChildByClass(AngelscriptExpr.class);
  }

  @Override
  @Nullable
  public AngelscriptFStringFormatSpec getFStringFormatSpec() {
    return findChildByClass(AngelscriptFStringFormatSpec.class);
  }

  @Override
  @NotNull
  public PsiElement getFstringLbrace() {
    return findNotNullChildByType(FSTRING_LBRACE);
  }

  @Override
  @Nullable
  public PsiElement getFstringRbrace() {
    return findChildByType(FSTRING_RBRACE);
  }

}
