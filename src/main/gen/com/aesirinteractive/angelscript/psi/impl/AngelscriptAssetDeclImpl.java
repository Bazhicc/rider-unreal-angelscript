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

public class AngelscriptAssetDeclImpl extends AngelscriptNamedElementImpl implements AngelscriptAssetDecl {

  public AngelscriptAssetDeclImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull AngelscriptVisitor visitor) {
    visitor.visitAssetDecl(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof AngelscriptVisitor) accept((AngelscriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public AngelscriptCompoundStatement getCompoundStatement() {
    return findNotNullChildByClass(AngelscriptCompoundStatement.class);
  }

  @Override
  @NotNull
  public AngelscriptTypeRef getTypeRef() {
    return findNotNullChildByClass(AngelscriptTypeRef.class);
  }

  @Override
  @NotNull
  public PsiElement getIdentifier() {
    return findNotNullChildByType(IDENTIFIER);
  }

}
