// This is a generated file. Not intended for manual editing.
package com.aesirinteractive.angelscript.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.aesirinteractive.angelscript.AngelscriptNamedElement;

public interface AngelscriptFunctionDecl extends AngelscriptNamedElement {

  @Nullable
  AngelscriptCompoundStatement getCompoundStatement();

  @NotNull
  AngelscriptParameterList getParameterList();

  @Nullable
  AngelscriptTypeRef getTypeRef();

  @Nullable
  AngelscriptUFunctionDecl getUFunctionDecl();

  @NotNull
  PsiElement getIdentifier();

}
