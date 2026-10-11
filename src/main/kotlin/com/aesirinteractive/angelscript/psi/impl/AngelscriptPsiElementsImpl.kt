// Native Kotlin PSI; preserves the complete grammar and tree layout of the pre-migration parser.
package com.aesirinteractive.angelscript.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.aesirinteractive.angelscript.*
import com.aesirinteractive.angelscript.psi.*

open class AngelscriptAddExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptAddExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitAddExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptArgListImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptArgList {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitArgList(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val argumentList: List<AngelscriptArgument>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptArgument::class.java)
}

open class AngelscriptArgumentImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptArgument {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitArgument(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val expr: AngelscriptExpr
        get() = findNotNullChildByClass(AngelscriptExpr::class.java)

    override val identifier: PsiElement?
        get() = findChildByType(AngelscriptTypes.IDENTIFIER)
}

open class AngelscriptAssetDeclImpl(node: ASTNode) : AngelscriptNamedElementImpl(node), AngelscriptAssetDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitAssetDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val compoundStatement: AngelscriptCompoundStatement
        get() = findNotNullChildByClass(AngelscriptCompoundStatement::class.java)

    override val typeRef: AngelscriptTypeRef
        get() = findNotNullChildByClass(AngelscriptTypeRef::class.java)

    override val identifier: PsiElement
        get() = findNotNullChildByType(AngelscriptTypes.IDENTIFIER)
}

open class AngelscriptAssignmentExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptAssignmentExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitAssignmentExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptBitwiseAndExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptBitwiseAndExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitBitwiseAndExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptBreakStatementImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptBreakStatement {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitBreakStatement(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
}

open class AngelscriptCaseStatementImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptCaseStatement {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitCaseStatement(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val breakStatementList: List<AngelscriptBreakStatement>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptBreakStatement::class.java)

    override val compoundStatementList: List<AngelscriptCompoundStatement>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptCompoundStatement::class.java)

    override val continueStatementList: List<AngelscriptContinueStatement>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptContinueStatement::class.java)

    override val doStatementList: List<AngelscriptDoStatement>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptDoStatement::class.java)

    override val expr: AngelscriptExpr?
        get() = findChildByClass(AngelscriptExpr::class.java)

    override val expressionStatementList: List<AngelscriptExpressionStatement>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpressionStatement::class.java)

    override val forStatementList: List<AngelscriptForStatement>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptForStatement::class.java)

    override val foreachStatementList: List<AngelscriptForeachStatement>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptForeachStatement::class.java)

    override val ifStatementList: List<AngelscriptIfStatement>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptIfStatement::class.java)

    override val returnStatementList: List<AngelscriptReturnStatement>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptReturnStatement::class.java)

    override val switchStatementList: List<AngelscriptSwitchStatement>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptSwitchStatement::class.java)

    override val variableDeclList: List<AngelscriptVariableDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptVariableDecl::class.java)

    override val whileStatementList: List<AngelscriptWhileStatement>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptWhileStatement::class.java)
}

open class AngelscriptCastExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptCastExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitCastExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val expr: AngelscriptExpr
        get() = findNotNullChildByClass(AngelscriptExpr::class.java)

    override val typeRef: AngelscriptTypeRef?
        get() = findChildByClass(AngelscriptTypeRef::class.java)
}

open class AngelscriptClassDeclImpl(node: ASTNode) : AngelscriptNamedElementImpl(node), AngelscriptClassDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitClassDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val commentList: List<AngelscriptComment>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptComment::class.java)

    override val constructorDeclList: List<AngelscriptConstructorDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptConstructorDecl::class.java)

    override val defaultValueDeclList: List<AngelscriptDefaultValueDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptDefaultValueDecl::class.java)

    override val functionDeclList: List<AngelscriptFunctionDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptFunctionDecl::class.java)

    override val ifDefBlockList: List<AngelscriptIfDefBlock>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptIfDefBlock::class.java)

    override val typeRefList: List<AngelscriptTypeRef>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptTypeRef::class.java)

    override val uClassDecl: AngelscriptUClassDecl?
        get() = findChildByClass(AngelscriptUClassDecl::class.java)

    override val variableDeclList: List<AngelscriptVariableDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptVariableDecl::class.java)

    override val identifier: PsiElement
        get() = findNotNullChildByType(AngelscriptTypes.IDENTIFIER)
}

open class AngelscriptCommaExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptCommaExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitCommaExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptCommentImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptComment {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitComment(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val comment: PsiElement
        get() = findNotNullChildByType(AngelscriptTypes.COMMENT)
}

open class AngelscriptCompoundStatementImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptCompoundStatement {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitCompoundStatement(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val assetDeclList: List<AngelscriptAssetDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptAssetDecl::class.java)

    override val classDeclList: List<AngelscriptClassDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptClassDecl::class.java)

    override val commentList: List<AngelscriptComment>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptComment::class.java)

    override val defaultValueDeclList: List<AngelscriptDefaultValueDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptDefaultValueDecl::class.java)

    override val delegateDeclList: List<AngelscriptDelegateDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptDelegateDecl::class.java)

    override val enumDeclList: List<AngelscriptEnumDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptEnumDecl::class.java)

    override val eventDeclList: List<AngelscriptEventDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptEventDecl::class.java)

    override val functionDeclList: List<AngelscriptFunctionDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptFunctionDecl::class.java)

    override val ifDefBlockList: List<AngelscriptIfDefBlock>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptIfDefBlock::class.java)

    override val interfaceDeclList: List<AngelscriptInterfaceDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptInterfaceDecl::class.java)

    override val mixinDeclList: List<AngelscriptMixinDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptMixinDecl::class.java)

    override val namespaceDeclList: List<AngelscriptNamespaceDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptNamespaceDecl::class.java)

    override val statementList: List<AngelscriptStatement>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptStatement::class.java)

    override val structDeclList: List<AngelscriptStructDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptStructDecl::class.java)

    override val typedefDeclList: List<AngelscriptTypedefDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptTypedefDecl::class.java)

    override val variableDeclList: List<AngelscriptVariableDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptVariableDecl::class.java)
}

open class AngelscriptConditionalExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptConditionalExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitConditionalExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptConstructorDeclImpl(node: ASTNode) : AngelscriptNamedElementImpl(node), AngelscriptConstructorDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitConstructorDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val compoundStatement: AngelscriptCompoundStatement?
        get() = findChildByClass(AngelscriptCompoundStatement::class.java)

    override val parameterList: AngelscriptParameterList
        get() = findNotNullChildByClass(AngelscriptParameterList::class.java)

    override val identifier: PsiElement
        get() = findNotNullChildByType(AngelscriptTypes.IDENTIFIER)
}

open class AngelscriptContinueStatementImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptContinueStatement {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitContinueStatement(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
}

open class AngelscriptDefaultValueDeclImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptDefaultValueDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitDefaultValueDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val expr: AngelscriptExpr
        get() = findNotNullChildByClass(AngelscriptExpr::class.java)
}

open class AngelscriptDelegateDeclImpl(node: ASTNode) : AngelscriptNamedElementImpl(node), AngelscriptDelegateDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitDelegateDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val functionDecl: AngelscriptFunctionDecl
        get() = findNotNullChildByClass(AngelscriptFunctionDecl::class.java)
}

open class AngelscriptDoStatementImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptDoStatement {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitDoStatement(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val parenthesizedExpr: AngelscriptParenthesizedExpr
        get() = findNotNullChildByClass(AngelscriptParenthesizedExpr::class.java)

    override val statement: AngelscriptStatement
        get() = findNotNullChildByClass(AngelscriptStatement::class.java)
}

open class AngelscriptElseClauseImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptElseClause {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitElseClause(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val statement: AngelscriptStatement
        get() = findNotNullChildByClass(AngelscriptStatement::class.java)
}

open class AngelscriptEnumDeclImpl(node: ASTNode) : AngelscriptNamedElementImpl(node), AngelscriptEnumDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitEnumDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val enumVariantList: List<AngelscriptEnumVariant>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptEnumVariant::class.java)

    override val uEnumDecl: AngelscriptUEnumDecl?
        get() = findChildByClass(AngelscriptUEnumDecl::class.java)

    override val identifier: PsiElement?
        get() = findChildByType(AngelscriptTypes.IDENTIFIER)
}

open class AngelscriptEnumVariantImpl(node: ASTNode) : AngelscriptNamedElementImpl(node), AngelscriptEnumVariant {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitEnumVariant(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val expr: AngelscriptExpr?
        get() = findChildByClass(AngelscriptExpr::class.java)

    override val identifier: PsiElement
        get() = findNotNullChildByType(AngelscriptTypes.IDENTIFIER)
}

open class AngelscriptEqualityExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptEqualityExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitEqualityExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptEventDeclImpl(node: ASTNode) : AngelscriptNamedElementImpl(node), AngelscriptEventDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitEventDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val functionDecl: AngelscriptFunctionDecl
        get() = findNotNullChildByClass(AngelscriptFunctionDecl::class.java)
}

open class AngelscriptExclusiveOrExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptExclusiveOrExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitExclusiveOrExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptExpressionStatementImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptExpressionStatement {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitExpressionStatement(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val expr: AngelscriptExpr
        get() = findNotNullChildByClass(AngelscriptExpr::class.java)
}

abstract class AngelscriptExprImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptExpr {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
}

open class AngelscriptForeachStatementImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptForeachStatement {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitForeachStatement(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val expr: AngelscriptExpr
        get() = findNotNullChildByClass(AngelscriptExpr::class.java)

    override val statement: AngelscriptStatement
        get() = findNotNullChildByClass(AngelscriptStatement::class.java)

    override val typeRef: AngelscriptTypeRef
        get() = findNotNullChildByClass(AngelscriptTypeRef::class.java)

    override val identifier: PsiElement
        get() = findNotNullChildByType(AngelscriptTypes.IDENTIFIER)
}

open class AngelscriptForStatementImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptForStatement {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitForStatement(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)

    override val statement: AngelscriptStatement
        get() = findNotNullChildByClass(AngelscriptStatement::class.java)

    override val variableDecl: AngelscriptVariableDecl?
        get() = findChildByClass(AngelscriptVariableDecl::class.java)
}

open class AngelscriptFStringExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptFStringExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitFStringExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val fStringInterpList: List<AngelscriptFStringInterp>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptFStringInterp::class.java)

    override val fstringEnd: PsiElement?
        get() = findChildByType(AngelscriptTypes.FSTRING_END)

    override val fstringStart: PsiElement
        get() = findNotNullChildByType(AngelscriptTypes.FSTRING_START)
}

open class AngelscriptFStringFormatSpecImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptFStringFormatSpec {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitFStringFormatSpec(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val fstringColon: PsiElement
        get() = findNotNullChildByType(AngelscriptTypes.FSTRING_COLON)

    override val fstringFormatText: PsiElement?
        get() = findChildByType(AngelscriptTypes.FSTRING_FORMAT_TEXT)
}

open class AngelscriptFStringInterpImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptFStringInterp {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitFStringInterp(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val expr: AngelscriptExpr?
        get() = findChildByClass(AngelscriptExpr::class.java)

    override val fStringFormatSpec: AngelscriptFStringFormatSpec?
        get() = findChildByClass(AngelscriptFStringFormatSpec::class.java)

    override val fstringLbrace: PsiElement
        get() = findNotNullChildByType(AngelscriptTypes.FSTRING_LBRACE)

    override val fstringRbrace: PsiElement?
        get() = findChildByType(AngelscriptTypes.FSTRING_RBRACE)
}

open class AngelscriptFunctionDeclImpl(node: ASTNode) : AngelscriptNamedElementImpl(node), AngelscriptFunctionDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitFunctionDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val compoundStatement: AngelscriptCompoundStatement?
        get() = findChildByClass(AngelscriptCompoundStatement::class.java)

    override val parameterList: AngelscriptParameterList
        get() = findNotNullChildByClass(AngelscriptParameterList::class.java)

    override val typeRef: AngelscriptTypeRef?
        get() = findChildByClass(AngelscriptTypeRef::class.java)

    override val uFunctionDecl: AngelscriptUFunctionDecl?
        get() = findChildByClass(AngelscriptUFunctionDecl::class.java)

    override val identifier: PsiElement
        get() = findNotNullChildByType(AngelscriptTypes.IDENTIFIER)
}

open class AngelscriptIfDefBlockImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptIfDefBlock {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitIfDefBlock(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val ifDefBranchList: List<AngelscriptIfDefBranch>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptIfDefBranch::class.java)

    override val ppElse: PsiElement?
        get() = findChildByType(AngelscriptTypes.PP_ELSE)

    override val ppEndif: PsiElement?
        get() = findChildByType(AngelscriptTypes.PP_ENDIF)

    override val ppIf: PsiElement
        get() = findNotNullChildByType(AngelscriptTypes.PP_IF)
}

open class AngelscriptIfDefBranchImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptIfDefBranch {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitIfDefBranch(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val assetDeclList: List<AngelscriptAssetDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptAssetDecl::class.java)

    override val classDeclList: List<AngelscriptClassDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptClassDecl::class.java)

    override val commentList: List<AngelscriptComment>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptComment::class.java)

    override val defaultValueDeclList: List<AngelscriptDefaultValueDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptDefaultValueDecl::class.java)

    override val delegateDeclList: List<AngelscriptDelegateDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptDelegateDecl::class.java)

    override val enumDeclList: List<AngelscriptEnumDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptEnumDecl::class.java)

    override val eventDeclList: List<AngelscriptEventDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptEventDecl::class.java)

    override val functionDeclList: List<AngelscriptFunctionDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptFunctionDecl::class.java)

    override val ifDefBlockList: List<AngelscriptIfDefBlock>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptIfDefBlock::class.java)

    override val interfaceDeclList: List<AngelscriptInterfaceDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptInterfaceDecl::class.java)

    override val mixinDeclList: List<AngelscriptMixinDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptMixinDecl::class.java)

    override val namespaceDeclList: List<AngelscriptNamespaceDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptNamespaceDecl::class.java)

    override val statementList: List<AngelscriptStatement>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptStatement::class.java)

    override val structDeclList: List<AngelscriptStructDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptStructDecl::class.java)

    override val typedefDeclList: List<AngelscriptTypedefDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptTypedefDecl::class.java)

    override val variableDeclList: List<AngelscriptVariableDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptVariableDecl::class.java)
}

open class AngelscriptIfStatementImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptIfStatement {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitIfStatement(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val elseClause: AngelscriptElseClause?
        get() = findChildByClass(AngelscriptElseClause::class.java)

    override val parenthesizedExpr: AngelscriptParenthesizedExpr
        get() = findNotNullChildByClass(AngelscriptParenthesizedExpr::class.java)

    override val statement: AngelscriptStatement
        get() = findNotNullChildByClass(AngelscriptStatement::class.java)
}

open class AngelscriptInclusiveOrExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptInclusiveOrExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitInclusiveOrExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptInitListExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptInitListExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitInitListExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)

    override val typeRef: AngelscriptTypeRef?
        get() = findChildByClass(AngelscriptTypeRef::class.java)
}

open class AngelscriptInterfaceDeclImpl(node: ASTNode) : AngelscriptNamedElementImpl(node), AngelscriptInterfaceDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitInterfaceDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val commentList: List<AngelscriptComment>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptComment::class.java)

    override val constructorDeclList: List<AngelscriptConstructorDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptConstructorDecl::class.java)

    override val defaultValueDeclList: List<AngelscriptDefaultValueDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptDefaultValueDecl::class.java)

    override val functionDeclList: List<AngelscriptFunctionDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptFunctionDecl::class.java)

    override val ifDefBlockList: List<AngelscriptIfDefBlock>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptIfDefBlock::class.java)

    override val typeRefList: List<AngelscriptTypeRef>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptTypeRef::class.java)

    override val uInterfaceDecl: AngelscriptUInterfaceDecl?
        get() = findChildByClass(AngelscriptUInterfaceDecl::class.java)

    override val variableDeclList: List<AngelscriptVariableDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptVariableDecl::class.java)

    override val identifier: PsiElement
        get() = findNotNullChildByType(AngelscriptTypes.IDENTIFIER)
}

open class AngelscriptLogicalAndExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptLogicalAndExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitLogicalAndExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptLogicalOrExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptLogicalOrExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitLogicalOrExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptMixinDeclImpl(node: ASTNode) : AngelscriptNamedElementImpl(node), AngelscriptMixinDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitMixinDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val functionDecl: AngelscriptFunctionDecl
        get() = findNotNullChildByClass(AngelscriptFunctionDecl::class.java)

    override val uFunctionDecl: AngelscriptUFunctionDecl?
        get() = findChildByClass(AngelscriptUFunctionDecl::class.java)
}

open class AngelscriptMultiplyExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptMultiplyExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitMultiplyExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptNamespaceDeclImpl(node: ASTNode) : AngelscriptNamedElementImpl(node), AngelscriptNamespaceDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitNamespaceDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val assetDeclList: List<AngelscriptAssetDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptAssetDecl::class.java)

    override val classDeclList: List<AngelscriptClassDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptClassDecl::class.java)

    override val commentList: List<AngelscriptComment>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptComment::class.java)

    override val defaultValueDeclList: List<AngelscriptDefaultValueDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptDefaultValueDecl::class.java)

    override val delegateDeclList: List<AngelscriptDelegateDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptDelegateDecl::class.java)

    override val enumDeclList: List<AngelscriptEnumDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptEnumDecl::class.java)

    override val eventDeclList: List<AngelscriptEventDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptEventDecl::class.java)

    override val functionDeclList: List<AngelscriptFunctionDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptFunctionDecl::class.java)

    override val ifDefBlockList: List<AngelscriptIfDefBlock>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptIfDefBlock::class.java)

    override val interfaceDeclList: List<AngelscriptInterfaceDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptInterfaceDecl::class.java)

    override val mixinDeclList: List<AngelscriptMixinDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptMixinDecl::class.java)

    override val namespaceDeclList: List<AngelscriptNamespaceDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptNamespaceDecl::class.java)

    override val statementList: List<AngelscriptStatement>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptStatement::class.java)

    override val structDeclList: List<AngelscriptStructDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptStructDecl::class.java)

    override val typedefDeclList: List<AngelscriptTypedefDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptTypedefDecl::class.java)

    override val variableDeclList: List<AngelscriptVariableDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptVariableDecl::class.java)
}

open class AngelscriptParameterDeclImpl(node: ASTNode) : AngelscriptNamedElementImpl(node), AngelscriptParameterDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitParameterDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val expr: AngelscriptExpr?
        get() = findChildByClass(AngelscriptExpr::class.java)

    override val typeRef: AngelscriptTypeRef
        get() = findNotNullChildByClass(AngelscriptTypeRef::class.java)

    override val identifier: PsiElement
        get() = findNotNullChildByType(AngelscriptTypes.IDENTIFIER)
}

open class AngelscriptParameterListImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptParameterList {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitParameterList(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val parameterDeclList: List<AngelscriptParameterDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptParameterDecl::class.java)
}

open class AngelscriptParenthesizedExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptParenthesizedExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitParenthesizedExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val expr: AngelscriptExpr
        get() = findNotNullChildByClass(AngelscriptExpr::class.java)
}

open class AngelscriptPostfixExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptPostfixExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitPostfixExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val argListList: List<AngelscriptArgList>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptArgList::class.java)

    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)

    override val typeArgumentList: List<AngelscriptTypeArgument>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptTypeArgument::class.java)
}

open class AngelscriptPPDefineDirectiveImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptPPDefineDirective {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitPPDefineDirective(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val ppDefine: PsiElement
        get() = findNotNullChildByType(AngelscriptTypes.PP_DEFINE)
}

open class AngelscriptPrimaryExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptPrimaryExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitPrimaryExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val boolLiteral: PsiElement?
        get() = findChildByType(AngelscriptTypes.BOOL_LITERAL)

    override val numberLiteral: PsiElement?
        get() = findChildByType(AngelscriptTypes.NUMBER_LITERAL)

    override val stringLiteral: PsiElement?
        get() = findChildByType(AngelscriptTypes.STRING_LITERAL)
}

open class AngelscriptRelationalExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptRelationalExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitRelationalExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptReturnStatementImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptReturnStatement {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitReturnStatement(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val expr: AngelscriptExpr?
        get() = findChildByClass(AngelscriptExpr::class.java)
}

open class AngelscriptScopeRefImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptScopeRef {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitScopeRef(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val typeArgumentList: List<AngelscriptTypeArgument>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptTypeArgument::class.java)
}

open class AngelscriptShiftExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptShiftExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitShiftExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptStatementImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptStatement {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitStatement(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val breakStatement: AngelscriptBreakStatement?
        get() = findChildByClass(AngelscriptBreakStatement::class.java)

    override val caseStatement: AngelscriptCaseStatement?
        get() = findChildByClass(AngelscriptCaseStatement::class.java)

    override val compoundStatement: AngelscriptCompoundStatement?
        get() = findChildByClass(AngelscriptCompoundStatement::class.java)

    override val continueStatement: AngelscriptContinueStatement?
        get() = findChildByClass(AngelscriptContinueStatement::class.java)

    override val doStatement: AngelscriptDoStatement?
        get() = findChildByClass(AngelscriptDoStatement::class.java)

    override val expressionStatement: AngelscriptExpressionStatement?
        get() = findChildByClass(AngelscriptExpressionStatement::class.java)

    override val forStatement: AngelscriptForStatement?
        get() = findChildByClass(AngelscriptForStatement::class.java)

    override val foreachStatement: AngelscriptForeachStatement?
        get() = findChildByClass(AngelscriptForeachStatement::class.java)

    override val ifStatement: AngelscriptIfStatement?
        get() = findChildByClass(AngelscriptIfStatement::class.java)

    override val returnStatement: AngelscriptReturnStatement?
        get() = findChildByClass(AngelscriptReturnStatement::class.java)

    override val switchStatement: AngelscriptSwitchStatement?
        get() = findChildByClass(AngelscriptSwitchStatement::class.java)

    override val variableDecl: AngelscriptVariableDecl?
        get() = findChildByClass(AngelscriptVariableDecl::class.java)

    override val whileStatement: AngelscriptWhileStatement?
        get() = findChildByClass(AngelscriptWhileStatement::class.java)
}

open class AngelscriptStringConcatExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptStringConcatExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitStringConcatExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
}

open class AngelscriptStructDeclImpl(node: ASTNode) : AngelscriptNamedElementImpl(node), AngelscriptStructDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitStructDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val commentList: List<AngelscriptComment>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptComment::class.java)

    override val constructorDeclList: List<AngelscriptConstructorDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptConstructorDecl::class.java)

    override val defaultValueDeclList: List<AngelscriptDefaultValueDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptDefaultValueDecl::class.java)

    override val functionDeclList: List<AngelscriptFunctionDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptFunctionDecl::class.java)

    override val ifDefBlockList: List<AngelscriptIfDefBlock>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptIfDefBlock::class.java)

    override val uStructDecl: AngelscriptUStructDecl?
        get() = findChildByClass(AngelscriptUStructDecl::class.java)

    override val variableDeclList: List<AngelscriptVariableDecl>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptVariableDecl::class.java)

    override val identifier: PsiElement
        get() = findNotNullChildByType(AngelscriptTypes.IDENTIFIER)
}

open class AngelscriptSwitchStatementImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptSwitchStatement {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitSwitchStatement(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val compoundStatement: AngelscriptCompoundStatement
        get() = findNotNullChildByClass(AngelscriptCompoundStatement::class.java)

    override val parenthesizedExpr: AngelscriptParenthesizedExpr
        get() = findNotNullChildByClass(AngelscriptParenthesizedExpr::class.java)
}

open class AngelscriptTypeArgumentImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptTypeArgument {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitTypeArgument(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val typeRefList: List<AngelscriptTypeRef>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptTypeRef::class.java)
}

open class AngelscriptTypedefDeclImpl(node: ASTNode) : AngelscriptNamedElementImpl(node), AngelscriptTypedefDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitTypedefDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val identifier: PsiElement
        get() = findNotNullChildByType(AngelscriptTypes.IDENTIFIER)
}

open class AngelscriptTypeRefImpl(node: ASTNode) : AngelscriptTypeRefMixin(node), AngelscriptTypeRef {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitTypeRef(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val scopeRef: AngelscriptScopeRef?
        get() = findChildByClass(AngelscriptScopeRef::class.java)

    override val typeArgumentList: List<AngelscriptTypeArgument>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptTypeArgument::class.java)

    override val identifier: PsiElement?
        get() = findChildByType(AngelscriptTypes.IDENTIFIER)
}

open class AngelscriptUClassDeclImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptUClassDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitUClassDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptUEnumDeclImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptUEnumDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitUEnumDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptUFunctionDeclImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptUFunctionDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitUFunctionDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptUInterfaceDeclImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptUInterfaceDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitUInterfaceDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptUnaryExprImpl(node: ASTNode) : AngelscriptExprImpl(node), AngelscriptUnaryExpr {
    override fun accept(visitor: AngelscriptVisitor) { visitor.visitUnaryExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val expr: AngelscriptExpr?
        get() = findChildByClass(AngelscriptExpr::class.java)
}

open class AngelscriptUPropertyDeclImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptUPropertyDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitUPropertyDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptUStructDeclImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptUStructDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitUStructDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)
}

open class AngelscriptVariableAccessExprImpl(node: ASTNode) : AngelscriptVariableAccessExprMixin(node), AngelscriptVariableAccessExpr {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitVariableAccessExpr(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val scopeRef: AngelscriptScopeRef?
        get() = findChildByClass(AngelscriptScopeRef::class.java)

    override val identifier: PsiElement
        get() = findNotNullChildByType(AngelscriptTypes.IDENTIFIER)
}

open class AngelscriptVariableDeclImpl(node: ASTNode) : AngelscriptNamedElementImpl(node), AngelscriptVariableDecl {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitVariableDecl(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val argListList: List<AngelscriptArgList>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptArgList::class.java)

    override val exprList: List<AngelscriptExpr>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, AngelscriptExpr::class.java)

    override val typeRef: AngelscriptTypeRef
        get() = findNotNullChildByClass(AngelscriptTypeRef::class.java)

    override val uPropertyDecl: AngelscriptUPropertyDecl?
        get() = findChildByClass(AngelscriptUPropertyDecl::class.java)
}

open class AngelscriptWhileStatementImpl(node: ASTNode) : ASTWrapperPsiElement(node), AngelscriptWhileStatement {
    open fun accept(visitor: AngelscriptVisitor) { visitor.visitWhileStatement(this) }
    override fun accept(visitor: PsiElementVisitor) {
        if (visitor is AngelscriptVisitor) accept(visitor) else super.accept(visitor)
    }
    override val parenthesizedExpr: AngelscriptParenthesizedExpr
        get() = findNotNullChildByClass(AngelscriptParenthesizedExpr::class.java)

    override val statement: AngelscriptStatement
        get() = findNotNullChildByClass(AngelscriptStatement::class.java)
}
