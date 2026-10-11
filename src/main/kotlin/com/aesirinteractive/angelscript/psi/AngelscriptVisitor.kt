// Native Kotlin PSI; preserves the complete grammar and tree layout of the pre-migration parser.
package com.aesirinteractive.angelscript.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.aesirinteractive.angelscript.AngelscriptNamedElement

open class AngelscriptVisitor : PsiElementVisitor() {
    open fun visitAddExpr(o: AngelscriptAddExpr) { visitExpr(o) }
    open fun visitArgList(o: AngelscriptArgList) { visitPsiElement(o) }
    open fun visitArgument(o: AngelscriptArgument) { visitPsiElement(o) }
    open fun visitAssetDecl(o: AngelscriptAssetDecl) { visitNamedElement(o) }
    open fun visitAssignmentExpr(o: AngelscriptAssignmentExpr) { visitExpr(o) }
    open fun visitBitwiseAndExpr(o: AngelscriptBitwiseAndExpr) { visitExpr(o) }
    open fun visitBreakStatement(o: AngelscriptBreakStatement) { visitPsiElement(o) }
    open fun visitCaseStatement(o: AngelscriptCaseStatement) { visitPsiElement(o) }
    open fun visitCastExpr(o: AngelscriptCastExpr) { visitExpr(o) }
    open fun visitClassDecl(o: AngelscriptClassDecl) { visitNamedElement(o) }
    open fun visitCommaExpr(o: AngelscriptCommaExpr) { visitExpr(o) }
    open fun visitComment(o: AngelscriptComment) { visitPsiElement(o) }
    open fun visitCompoundStatement(o: AngelscriptCompoundStatement) { visitPsiElement(o) }
    open fun visitConditionalExpr(o: AngelscriptConditionalExpr) { visitExpr(o) }
    open fun visitConstructorDecl(o: AngelscriptConstructorDecl) { visitNamedElement(o) }
    open fun visitContinueStatement(o: AngelscriptContinueStatement) { visitPsiElement(o) }
    open fun visitDefaultValueDecl(o: AngelscriptDefaultValueDecl) { visitPsiElement(o) }
    open fun visitDelegateDecl(o: AngelscriptDelegateDecl) { visitNamedElement(o) }
    open fun visitDoStatement(o: AngelscriptDoStatement) { visitPsiElement(o) }
    open fun visitElseClause(o: AngelscriptElseClause) { visitPsiElement(o) }
    open fun visitEnumDecl(o: AngelscriptEnumDecl) { visitNamedElement(o) }
    open fun visitEnumVariant(o: AngelscriptEnumVariant) { visitNamedElement(o) }
    open fun visitEqualityExpr(o: AngelscriptEqualityExpr) { visitExpr(o) }
    open fun visitEventDecl(o: AngelscriptEventDecl) { visitNamedElement(o) }
    open fun visitExclusiveOrExpr(o: AngelscriptExclusiveOrExpr) { visitExpr(o) }
    open fun visitExpr(o: AngelscriptExpr) { visitPsiElement(o) }
    open fun visitExpressionStatement(o: AngelscriptExpressionStatement) { visitPsiElement(o) }
    open fun visitFStringExpr(o: AngelscriptFStringExpr) { visitExpr(o) }
    open fun visitFStringFormatSpec(o: AngelscriptFStringFormatSpec) { visitPsiElement(o) }
    open fun visitFStringInterp(o: AngelscriptFStringInterp) { visitPsiElement(o) }
    open fun visitForStatement(o: AngelscriptForStatement) { visitPsiElement(o) }
    open fun visitForeachStatement(o: AngelscriptForeachStatement) { visitPsiElement(o) }
    open fun visitFunctionDecl(o: AngelscriptFunctionDecl) { visitNamedElement(o) }
    open fun visitIfDefBlock(o: AngelscriptIfDefBlock) { visitPsiElement(o) }
    open fun visitIfDefBranch(o: AngelscriptIfDefBranch) { visitPsiElement(o) }
    open fun visitIfStatement(o: AngelscriptIfStatement) { visitPsiElement(o) }
    open fun visitInclusiveOrExpr(o: AngelscriptInclusiveOrExpr) { visitExpr(o) }
    open fun visitInitListExpr(o: AngelscriptInitListExpr) { visitExpr(o) }
    open fun visitInterfaceDecl(o: AngelscriptInterfaceDecl) { visitNamedElement(o) }
    open fun visitLogicalAndExpr(o: AngelscriptLogicalAndExpr) { visitExpr(o) }
    open fun visitLogicalOrExpr(o: AngelscriptLogicalOrExpr) { visitExpr(o) }
    open fun visitMixinDecl(o: AngelscriptMixinDecl) { visitNamedElement(o) }
    open fun visitMultiplyExpr(o: AngelscriptMultiplyExpr) { visitExpr(o) }
    open fun visitNamespaceDecl(o: AngelscriptNamespaceDecl) { visitNamedElement(o) }
    open fun visitPPDefineDirective(o: AngelscriptPPDefineDirective) { visitPsiElement(o) }
    open fun visitParameterDecl(o: AngelscriptParameterDecl) { visitNamedElement(o) }
    open fun visitParameterList(o: AngelscriptParameterList) { visitPsiElement(o) }
    open fun visitParenthesizedExpr(o: AngelscriptParenthesizedExpr) { visitExpr(o) }
    open fun visitPostfixExpr(o: AngelscriptPostfixExpr) { visitExpr(o) }
    open fun visitPrimaryExpr(o: AngelscriptPrimaryExpr) { visitExpr(o) }
    open fun visitRelationalExpr(o: AngelscriptRelationalExpr) { visitExpr(o) }
    open fun visitReturnStatement(o: AngelscriptReturnStatement) { visitPsiElement(o) }
    open fun visitScopeRef(o: AngelscriptScopeRef) { visitPsiElement(o) }
    open fun visitShiftExpr(o: AngelscriptShiftExpr) { visitExpr(o) }
    open fun visitStatement(o: AngelscriptStatement) { visitPsiElement(o) }
    open fun visitStringConcatExpr(o: AngelscriptStringConcatExpr) { visitExpr(o) }
    open fun visitStructDecl(o: AngelscriptStructDecl) { visitNamedElement(o) }
    open fun visitSwitchStatement(o: AngelscriptSwitchStatement) { visitPsiElement(o) }
    open fun visitTypeArgument(o: AngelscriptTypeArgument) { visitPsiElement(o) }
    open fun visitTypeRef(o: AngelscriptTypeRef) { visitPsiElement(o) }
    open fun visitTypedefDecl(o: AngelscriptTypedefDecl) { visitNamedElement(o) }
    open fun visitUClassDecl(o: AngelscriptUClassDecl) { visitPsiElement(o) }
    open fun visitUEnumDecl(o: AngelscriptUEnumDecl) { visitPsiElement(o) }
    open fun visitUFunctionDecl(o: AngelscriptUFunctionDecl) { visitPsiElement(o) }
    open fun visitUInterfaceDecl(o: AngelscriptUInterfaceDecl) { visitPsiElement(o) }
    open fun visitUPropertyDecl(o: AngelscriptUPropertyDecl) { visitPsiElement(o) }
    open fun visitUStructDecl(o: AngelscriptUStructDecl) { visitPsiElement(o) }
    open fun visitUnaryExpr(o: AngelscriptUnaryExpr) { visitExpr(o) }
    open fun visitVariableAccessExpr(o: AngelscriptVariableAccessExpr) { visitExpr(o) }
    open fun visitVariableDecl(o: AngelscriptVariableDecl) { visitNamedElement(o) }
    open fun visitWhileStatement(o: AngelscriptWhileStatement) { visitPsiElement(o) }
    open fun visitNamedElement(o: AngelscriptNamedElement) { visitPsiElement(o) }
    open fun visitPsiElement(o: PsiElement) { visitElement(o) }
}
