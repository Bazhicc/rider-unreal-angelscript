// This is a generated file. Not intended for manual editing.
package com.aesirinteractive.angelscript.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.aesirinteractive.angelscript.AngelscriptNamedElement;

public interface AngelscriptInterfaceDecl extends AngelscriptNamedElement {

  @NotNull
  List<AngelscriptComment> getCommentList();

  @NotNull
  List<AngelscriptConstructorDecl> getConstructorDeclList();

  @NotNull
  List<AngelscriptDefaultValueDecl> getDefaultValueDeclList();

  @NotNull
  List<AngelscriptFunctionDecl> getFunctionDeclList();

  @NotNull
  List<AngelscriptIfDefBlock> getIfDefBlockList();

  @NotNull
  List<AngelscriptTypeRef> getTypeRefList();

  @Nullable
  AngelscriptUInterfaceDecl getUInterfaceDecl();

  @NotNull
  List<AngelscriptVariableDecl> getVariableDeclList();

  @NotNull
  PsiElement getIdentifier();

}
