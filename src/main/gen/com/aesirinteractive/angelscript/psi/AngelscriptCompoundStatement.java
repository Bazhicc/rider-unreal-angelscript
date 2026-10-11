// This is a generated file. Not intended for manual editing.
package com.aesirinteractive.angelscript.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface AngelscriptCompoundStatement extends PsiElement {

  @NotNull
  List<AngelscriptAssetDecl> getAssetDeclList();

  @NotNull
  List<AngelscriptClassDecl> getClassDeclList();

  @NotNull
  List<AngelscriptComment> getCommentList();

  @NotNull
  List<AngelscriptDefaultValueDecl> getDefaultValueDeclList();

  @NotNull
  List<AngelscriptDelegateDecl> getDelegateDeclList();

  @NotNull
  List<AngelscriptEnumDecl> getEnumDeclList();

  @NotNull
  List<AngelscriptEventDecl> getEventDeclList();

  @NotNull
  List<AngelscriptFunctionDecl> getFunctionDeclList();

  @NotNull
  List<AngelscriptIfDefBlock> getIfDefBlockList();

  @NotNull
  List<AngelscriptInterfaceDecl> getInterfaceDeclList();

  @NotNull
  List<AngelscriptMixinDecl> getMixinDeclList();

  @NotNull
  List<AngelscriptNamespaceDecl> getNamespaceDeclList();

  @NotNull
  List<AngelscriptStatement> getStatementList();

  @NotNull
  List<AngelscriptStructDecl> getStructDeclList();

  @NotNull
  List<AngelscriptTypedefDecl> getTypedefDeclList();

  @NotNull
  List<AngelscriptVariableDecl> getVariableDeclList();

}
