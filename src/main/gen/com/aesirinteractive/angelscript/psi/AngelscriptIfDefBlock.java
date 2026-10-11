// This is a generated file. Not intended for manual editing.
package com.aesirinteractive.angelscript.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface AngelscriptIfDefBlock extends PsiElement {

  @NotNull
  List<AngelscriptIfDefBranch> getIfDefBranchList();

  @Nullable
  PsiElement getPpElse();

  @Nullable
  PsiElement getPpEndif();

  @NotNull
  PsiElement getPpIf();

}
