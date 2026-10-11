// This is a generated file. Not intended for manual editing.
package com.aesirinteractive.angelscript.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.aesirinteractive.angelscript.AngelscriptTypes.*;
import com.aesirinteractive.angelscript.AngelscriptTypeRefMixin;
import com.aesirinteractive.angelscript.psi.*;

public class AngelscriptTypeRefImpl extends AngelscriptTypeRefMixin implements AngelscriptTypeRef {

  public AngelscriptTypeRefImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull AngelscriptVisitor visitor) {
    visitor.visitTypeRef(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof AngelscriptVisitor) accept((AngelscriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public AngelscriptScopeRef getScopeRef() {
    return findChildByClass(AngelscriptScopeRef.class);
  }

  @Override
  @NotNull
  public List<AngelscriptTypeArgument> getTypeArgumentList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptTypeArgument.class);
  }

  @Override
  @Nullable
  public PsiElement getIdentifier() {
    return findChildByType(IDENTIFIER);
  }

}
