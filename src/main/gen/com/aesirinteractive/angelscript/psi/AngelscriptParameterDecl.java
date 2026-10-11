// This is a generated file. Not intended for manual editing.
package com.aesirinteractive.angelscript.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.aesirinteractive.angelscript.AngelscriptNamedElement;

public interface AngelscriptParameterDecl extends AngelscriptNamedElement {

  @Nullable
  AngelscriptExpr getExpr();

  @NotNull
  AngelscriptTypeRef getTypeRef();

  @NotNull
  PsiElement getIdentifier();

}
