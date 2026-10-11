// This is a generated file. Not intended for manual editing.
package com.aesirinteractive.angelscript.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface AngelscriptTypeRef extends PsiElement {

  @Nullable
  AngelscriptScopeRef getScopeRef();

  @NotNull
  List<AngelscriptTypeArgument> getTypeArgumentList();

  @Nullable
  PsiElement getIdentifier();

}
