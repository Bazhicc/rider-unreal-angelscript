// Native Kotlin PSI; preserves the complete grammar and tree layout of the pre-migration parser.
package com.aesirinteractive.angelscript.psi

import com.intellij.psi.PsiElement
import com.aesirinteractive.angelscript.AngelscriptNamedElement

interface AngelscriptAddExpr : AngelscriptExpr {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptArgList : PsiElement {
    val argumentList: List<AngelscriptArgument>
}

interface AngelscriptArgument : PsiElement {
    val expr: AngelscriptExpr
    val identifier: PsiElement?
}

interface AngelscriptAssetDecl : AngelscriptNamedElement {
    val compoundStatement: AngelscriptCompoundStatement
    val typeRef: AngelscriptTypeRef
    val identifier: PsiElement
}

interface AngelscriptAssignmentExpr : AngelscriptExpr {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptBitwiseAndExpr : AngelscriptExpr {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptBreakStatement : PsiElement {
}

interface AngelscriptCaseStatement : PsiElement {
    val breakStatementList: List<AngelscriptBreakStatement>
    val compoundStatementList: List<AngelscriptCompoundStatement>
    val continueStatementList: List<AngelscriptContinueStatement>
    val doStatementList: List<AngelscriptDoStatement>
    val expr: AngelscriptExpr?
    val expressionStatementList: List<AngelscriptExpressionStatement>
    val forStatementList: List<AngelscriptForStatement>
    val foreachStatementList: List<AngelscriptForeachStatement>
    val ifStatementList: List<AngelscriptIfStatement>
    val returnStatementList: List<AngelscriptReturnStatement>
    val switchStatementList: List<AngelscriptSwitchStatement>
    val variableDeclList: List<AngelscriptVariableDecl>
    val whileStatementList: List<AngelscriptWhileStatement>
}

interface AngelscriptCastExpr : AngelscriptExpr {
    val expr: AngelscriptExpr
    val typeRef: AngelscriptTypeRef?
}

interface AngelscriptClassDecl : AngelscriptNamedElement {
    val commentList: List<AngelscriptComment>
    val constructorDeclList: List<AngelscriptConstructorDecl>
    val defaultValueDeclList: List<AngelscriptDefaultValueDecl>
    val functionDeclList: List<AngelscriptFunctionDecl>
    val ifDefBlockList: List<AngelscriptIfDefBlock>
    val typeRefList: List<AngelscriptTypeRef>
    val uClassDecl: AngelscriptUClassDecl?
    val variableDeclList: List<AngelscriptVariableDecl>
    val identifier: PsiElement
}

interface AngelscriptCommaExpr : AngelscriptExpr {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptComment : PsiElement {
    val comment: PsiElement
}

interface AngelscriptCompoundStatement : PsiElement {
    val assetDeclList: List<AngelscriptAssetDecl>
    val classDeclList: List<AngelscriptClassDecl>
    val commentList: List<AngelscriptComment>
    val defaultValueDeclList: List<AngelscriptDefaultValueDecl>
    val delegateDeclList: List<AngelscriptDelegateDecl>
    val enumDeclList: List<AngelscriptEnumDecl>
    val eventDeclList: List<AngelscriptEventDecl>
    val functionDeclList: List<AngelscriptFunctionDecl>
    val ifDefBlockList: List<AngelscriptIfDefBlock>
    val interfaceDeclList: List<AngelscriptInterfaceDecl>
    val mixinDeclList: List<AngelscriptMixinDecl>
    val namespaceDeclList: List<AngelscriptNamespaceDecl>
    val statementList: List<AngelscriptStatement>
    val structDeclList: List<AngelscriptStructDecl>
    val typedefDeclList: List<AngelscriptTypedefDecl>
    val variableDeclList: List<AngelscriptVariableDecl>
}

interface AngelscriptConditionalExpr : AngelscriptExpr {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptConstructorDecl : AngelscriptNamedElement {
    val compoundStatement: AngelscriptCompoundStatement?
    val parameterList: AngelscriptParameterList
    val identifier: PsiElement
}

interface AngelscriptContinueStatement : PsiElement {
}

interface AngelscriptDefaultValueDecl : PsiElement {
    val expr: AngelscriptExpr
}

interface AngelscriptDelegateDecl : AngelscriptNamedElement {
    val functionDecl: AngelscriptFunctionDecl
}

interface AngelscriptDoStatement : PsiElement {
    val parenthesizedExpr: AngelscriptParenthesizedExpr
    val statement: AngelscriptStatement
}

interface AngelscriptElseClause : PsiElement {
    val statement: AngelscriptStatement
}

interface AngelscriptEnumDecl : AngelscriptNamedElement {
    val enumVariantList: List<AngelscriptEnumVariant>
    val uEnumDecl: AngelscriptUEnumDecl?
    val identifier: PsiElement?
}

interface AngelscriptEnumVariant : AngelscriptNamedElement {
    val expr: AngelscriptExpr?
    val identifier: PsiElement
}

interface AngelscriptEqualityExpr : AngelscriptExpr {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptEventDecl : AngelscriptNamedElement {
    val functionDecl: AngelscriptFunctionDecl
}

interface AngelscriptExclusiveOrExpr : AngelscriptExpr {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptExpr : PsiElement {
}

interface AngelscriptExpressionStatement : PsiElement {
    val expr: AngelscriptExpr
}

interface AngelscriptForeachStatement : PsiElement {
    val expr: AngelscriptExpr
    val statement: AngelscriptStatement
    val typeRef: AngelscriptTypeRef
    val identifier: PsiElement
}

interface AngelscriptForStatement : PsiElement {
    val exprList: List<AngelscriptExpr>
    val statement: AngelscriptStatement
    val variableDecl: AngelscriptVariableDecl?
}

interface AngelscriptFStringExpr : AngelscriptExpr {
    val fStringInterpList: List<AngelscriptFStringInterp>
    val fstringEnd: PsiElement?
    val fstringStart: PsiElement
}

interface AngelscriptFStringFormatSpec : PsiElement {
    val fstringColon: PsiElement
    val fstringFormatText: PsiElement?
}

interface AngelscriptFStringInterp : PsiElement {
    val expr: AngelscriptExpr?
    val fStringFormatSpec: AngelscriptFStringFormatSpec?
    val fstringLbrace: PsiElement
    val fstringRbrace: PsiElement?
}

interface AngelscriptFunctionDecl : AngelscriptNamedElement {
    val compoundStatement: AngelscriptCompoundStatement?
    val parameterList: AngelscriptParameterList
    val typeRef: AngelscriptTypeRef?
    val uFunctionDecl: AngelscriptUFunctionDecl?
    val identifier: PsiElement
}

interface AngelscriptIfDefBlock : PsiElement {
    val ifDefBranchList: List<AngelscriptIfDefBranch>
    val ppElse: PsiElement?
    val ppEndif: PsiElement?
    val ppIf: PsiElement
}

interface AngelscriptIfDefBranch : PsiElement {
    val assetDeclList: List<AngelscriptAssetDecl>
    val classDeclList: List<AngelscriptClassDecl>
    val commentList: List<AngelscriptComment>
    val defaultValueDeclList: List<AngelscriptDefaultValueDecl>
    val delegateDeclList: List<AngelscriptDelegateDecl>
    val enumDeclList: List<AngelscriptEnumDecl>
    val eventDeclList: List<AngelscriptEventDecl>
    val functionDeclList: List<AngelscriptFunctionDecl>
    val ifDefBlockList: List<AngelscriptIfDefBlock>
    val interfaceDeclList: List<AngelscriptInterfaceDecl>
    val mixinDeclList: List<AngelscriptMixinDecl>
    val namespaceDeclList: List<AngelscriptNamespaceDecl>
    val statementList: List<AngelscriptStatement>
    val structDeclList: List<AngelscriptStructDecl>
    val typedefDeclList: List<AngelscriptTypedefDecl>
    val variableDeclList: List<AngelscriptVariableDecl>
}

interface AngelscriptIfStatement : PsiElement {
    val elseClause: AngelscriptElseClause?
    val parenthesizedExpr: AngelscriptParenthesizedExpr
    val statement: AngelscriptStatement
}

interface AngelscriptInclusiveOrExpr : AngelscriptExpr {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptInitListExpr : AngelscriptExpr {
    val exprList: List<AngelscriptExpr>
    val typeRef: AngelscriptTypeRef?
}

interface AngelscriptInterfaceDecl : AngelscriptNamedElement {
    val commentList: List<AngelscriptComment>
    val constructorDeclList: List<AngelscriptConstructorDecl>
    val defaultValueDeclList: List<AngelscriptDefaultValueDecl>
    val functionDeclList: List<AngelscriptFunctionDecl>
    val ifDefBlockList: List<AngelscriptIfDefBlock>
    val typeRefList: List<AngelscriptTypeRef>
    val uInterfaceDecl: AngelscriptUInterfaceDecl?
    val variableDeclList: List<AngelscriptVariableDecl>
    val identifier: PsiElement
}

interface AngelscriptLogicalAndExpr : AngelscriptExpr {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptLogicalOrExpr : AngelscriptExpr {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptMixinDecl : AngelscriptNamedElement {
    val functionDecl: AngelscriptFunctionDecl
    val uFunctionDecl: AngelscriptUFunctionDecl?
}

interface AngelscriptMultiplyExpr : AngelscriptExpr {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptNamespaceDecl : AngelscriptNamedElement {
    val assetDeclList: List<AngelscriptAssetDecl>
    val classDeclList: List<AngelscriptClassDecl>
    val commentList: List<AngelscriptComment>
    val defaultValueDeclList: List<AngelscriptDefaultValueDecl>
    val delegateDeclList: List<AngelscriptDelegateDecl>
    val enumDeclList: List<AngelscriptEnumDecl>
    val eventDeclList: List<AngelscriptEventDecl>
    val functionDeclList: List<AngelscriptFunctionDecl>
    val ifDefBlockList: List<AngelscriptIfDefBlock>
    val interfaceDeclList: List<AngelscriptInterfaceDecl>
    val mixinDeclList: List<AngelscriptMixinDecl>
    val namespaceDeclList: List<AngelscriptNamespaceDecl>
    val statementList: List<AngelscriptStatement>
    val structDeclList: List<AngelscriptStructDecl>
    val typedefDeclList: List<AngelscriptTypedefDecl>
    val variableDeclList: List<AngelscriptVariableDecl>
}

interface AngelscriptParameterDecl : AngelscriptNamedElement {
    val expr: AngelscriptExpr?
    val typeRef: AngelscriptTypeRef
    val identifier: PsiElement
}

interface AngelscriptParameterList : PsiElement {
    val parameterDeclList: List<AngelscriptParameterDecl>
}

interface AngelscriptParenthesizedExpr : AngelscriptExpr {
    val expr: AngelscriptExpr
}

interface AngelscriptPostfixExpr : AngelscriptExpr {
    val argListList: List<AngelscriptArgList>
    val exprList: List<AngelscriptExpr>
    val typeArgumentList: List<AngelscriptTypeArgument>
}

interface AngelscriptPPDefineDirective : PsiElement {
    val ppDefine: PsiElement
}

interface AngelscriptPrimaryExpr : AngelscriptExpr {
    val boolLiteral: PsiElement?
    val numberLiteral: PsiElement?
    val stringLiteral: PsiElement?
}

interface AngelscriptRelationalExpr : AngelscriptExpr {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptReturnStatement : PsiElement {
    val expr: AngelscriptExpr?
}

interface AngelscriptScopeRef : PsiElement {
    val typeArgumentList: List<AngelscriptTypeArgument>
}

interface AngelscriptShiftExpr : AngelscriptExpr {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptStatement : PsiElement {
    val breakStatement: AngelscriptBreakStatement?
    val caseStatement: AngelscriptCaseStatement?
    val compoundStatement: AngelscriptCompoundStatement?
    val continueStatement: AngelscriptContinueStatement?
    val doStatement: AngelscriptDoStatement?
    val expressionStatement: AngelscriptExpressionStatement?
    val forStatement: AngelscriptForStatement?
    val foreachStatement: AngelscriptForeachStatement?
    val ifStatement: AngelscriptIfStatement?
    val returnStatement: AngelscriptReturnStatement?
    val switchStatement: AngelscriptSwitchStatement?
    val variableDecl: AngelscriptVariableDecl?
    val whileStatement: AngelscriptWhileStatement?
}

interface AngelscriptStringConcatExpr : AngelscriptExpr {
}

interface AngelscriptStructDecl : AngelscriptNamedElement {
    val commentList: List<AngelscriptComment>
    val constructorDeclList: List<AngelscriptConstructorDecl>
    val defaultValueDeclList: List<AngelscriptDefaultValueDecl>
    val functionDeclList: List<AngelscriptFunctionDecl>
    val ifDefBlockList: List<AngelscriptIfDefBlock>
    val uStructDecl: AngelscriptUStructDecl?
    val variableDeclList: List<AngelscriptVariableDecl>
    val identifier: PsiElement
}

interface AngelscriptSwitchStatement : PsiElement {
    val compoundStatement: AngelscriptCompoundStatement
    val parenthesizedExpr: AngelscriptParenthesizedExpr
}

interface AngelscriptTypeArgument : PsiElement {
    val typeRefList: List<AngelscriptTypeRef>
}

interface AngelscriptTypedefDecl : AngelscriptNamedElement {
    val identifier: PsiElement
}

interface AngelscriptTypeRef : PsiElement {
    val scopeRef: AngelscriptScopeRef?
    val typeArgumentList: List<AngelscriptTypeArgument>
    val identifier: PsiElement?
}

interface AngelscriptUClassDecl : PsiElement {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptUEnumDecl : PsiElement {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptUFunctionDecl : PsiElement {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptUInterfaceDecl : PsiElement {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptUnaryExpr : AngelscriptExpr {
    val expr: AngelscriptExpr?
}

interface AngelscriptUPropertyDecl : PsiElement {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptUStructDecl : PsiElement {
    val exprList: List<AngelscriptExpr>
}

interface AngelscriptVariableAccessExpr : AngelscriptExpr {
    val scopeRef: AngelscriptScopeRef?
    val identifier: PsiElement
}

interface AngelscriptVariableDecl : AngelscriptNamedElement {
    val argListList: List<AngelscriptArgList>
    val exprList: List<AngelscriptExpr>
    val typeRef: AngelscriptTypeRef
    val uPropertyDecl: AngelscriptUPropertyDecl?
}

interface AngelscriptWhileStatement : PsiElement {
    val parenthesizedExpr: AngelscriptParenthesizedExpr
    val statement: AngelscriptStatement
}
