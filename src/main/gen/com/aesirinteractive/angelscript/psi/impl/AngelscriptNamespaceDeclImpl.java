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

public class AngelscriptNamespaceDeclImpl extends AngelscriptNamedElementImpl implements AngelscriptNamespaceDecl {

  public AngelscriptNamespaceDeclImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull AngelscriptVisitor visitor) {
    visitor.visitNamespaceDecl(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof AngelscriptVisitor) accept((AngelscriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<AngelscriptAssetDecl> getAssetDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptAssetDecl.class);
  }

  @Override
  @NotNull
  public List<AngelscriptClassDecl> getClassDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptClassDecl.class);
  }

  @Override
  @NotNull
  public List<AngelscriptComment> getCommentList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptComment.class);
  }

  @Override
  @NotNull
  public List<AngelscriptDefaultValueDecl> getDefaultValueDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptDefaultValueDecl.class);
  }

  @Override
  @NotNull
  public List<AngelscriptDelegateDecl> getDelegateDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptDelegateDecl.class);
  }

  @Override
  @NotNull
  public List<AngelscriptEnumDecl> getEnumDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptEnumDecl.class);
  }

  @Override
  @NotNull
  public List<AngelscriptEventDecl> getEventDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptEventDecl.class);
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
  public List<AngelscriptInterfaceDecl> getInterfaceDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptInterfaceDecl.class);
  }

  @Override
  @NotNull
  public List<AngelscriptMixinDecl> getMixinDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptMixinDecl.class);
  }

  @Override
  @NotNull
  public List<AngelscriptNamespaceDecl> getNamespaceDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptNamespaceDecl.class);
  }

  @Override
  @NotNull
  public List<AngelscriptStatement> getStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptStatement.class);
  }

  @Override
  @NotNull
  public List<AngelscriptStructDecl> getStructDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptStructDecl.class);
  }

  @Override
  @NotNull
  public List<AngelscriptTypedefDecl> getTypedefDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptTypedefDecl.class);
  }

  @Override
  @NotNull
  public List<AngelscriptVariableDecl> getVariableDeclList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptVariableDecl.class);
  }

}
