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

public class AngelscriptClassDeclImpl extends AngelscriptNamedElementImpl implements AngelscriptClassDecl {

  public AngelscriptClassDeclImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull AngelscriptVisitor visitor) {
    visitor.visitClassDecl(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof AngelscriptVisitor) accept((AngelscriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<AngelscriptComment> getCommentList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptComment.class);
  }

  @Override
  @NotNull
  public List<AngelscriptConstructorDecl> getConstructorDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptConstructorDecl.class);
  }

  @Override
  @NotNull
  public List<AngelscriptDefaultValueDecl> getDefaultValueDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptDefaultValueDecl.class);
  }

  @Override
  @NotNull
  public List<AngelscriptFunctionDecl> getFunctionDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptFunctionDecl.class);
  }

  @Override
  @NotNull
  public List<AngelscriptIfDefBlock> getIfDefBlockList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptIfDefBlock.class);
  }

  @Override
  @NotNull
  public List<AngelscriptTypeRef> getTypeRefList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptTypeRef.class);
  }

  @Override
  @Nullable
  public AngelscriptUClassDecl getUClassDecl() {
    return findChildByClass(AngelscriptUClassDecl.class);
  }

  @Override
  @NotNull
  public List<AngelscriptVariableDecl> getVariableDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptVariableDecl.class);
  }

  @Override
  @NotNull
  public PsiElement getIdentifier() {
    return findNotNullChildByType(IDENTIFIER);
  }

}
