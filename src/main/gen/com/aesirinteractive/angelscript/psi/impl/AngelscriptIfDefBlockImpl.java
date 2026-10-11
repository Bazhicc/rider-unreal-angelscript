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

public class AngelscriptIfDefBlockImpl extends ASTWrapperPsiElement implements AngelscriptIfDefBlock {

  public AngelscriptIfDefBlockImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull AngelscriptVisitor visitor) {
    visitor.visitIfDefBlock(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof AngelscriptVisitor) accept((AngelscriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<AngelscriptIfDefBranch> getIfDefBranchList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptIfDefBranch.class);
  }

  @Override
  @Nullable
  public PsiElement getPpElse() {
    return findChildByType(PP_ELSE);
  }

  @Override
  @Nullable
  public PsiElement getPpEndif() {
    return findChildByType(PP_ENDIF);
  }

  @Override
  @NotNull
  public PsiElement getPpIf() {
    return findNotNullChildByType(PP_IF);
  }

}
