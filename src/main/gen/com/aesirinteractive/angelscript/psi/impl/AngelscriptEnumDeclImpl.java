// This is a generated file. Not intended for manual editing.
package com.aesirinteractive.angelscript.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.aesirinteractive.angelscript.AngelscriptTypes.*;
import com.aesirinteractive.angelscript.AngelscriptNamedElementImpl;
import com.aesirinteractive.angelscript.psi.*;

public class AngelscriptEnumDeclImpl extends AngelscriptNamedElementImpl implements AngelscriptEnumDecl {

  public AngelscriptEnumDeclImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull AngelscriptVisitor visitor) {
    visitor.visitEnumDecl(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof AngelscriptVisitor) accept((AngelscriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<AngelscriptEnumVariant> getEnumVariantList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptEnumVariant.class);
  }

  @Override
  @Nullable
  public AngelscriptUEnumDecl getUEnumDecl() {
    return findChildByClass(AngelscriptUEnumDecl.class);
  }

  @Override
  @Nullable
  public PsiElement getIdentifier() {
    return findChildByType(IDENTIFIER);
  }

}
