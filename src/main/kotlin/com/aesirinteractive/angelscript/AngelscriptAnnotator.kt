package com.aesirinteractive.angelscript

import com.aesirinteractive.angelscript.psi.AngelscriptArgList
import com.aesirinteractive.angelscript.psi.AngelscriptClassDecl
import com.aesirinteractive.angelscript.psi.AngelscriptConstructorDecl
import com.aesirinteractive.angelscript.psi.AngelscriptEnumDecl
import com.aesirinteractive.angelscript.psi.AngelscriptFunctionDecl
import com.aesirinteractive.angelscript.psi.AngelscriptParameterDecl
import com.aesirinteractive.angelscript.psi.AngelscriptPostfixExpr
import com.aesirinteractive.angelscript.psi.AngelscriptScopeRef
import com.aesirinteractive.angelscript.psi.AngelscriptStructDecl
import com.aesirinteractive.angelscript.psi.AngelscriptTypeArgument
import com.aesirinteractive.angelscript.psi.AngelscriptTypeRef
import com.aesirinteractive.angelscript.psi.AngelscriptVariableAccessExpr
import com.aesirinteractive.angelscript.psi.AngelscriptVariableDecl
import com.aesirinteractive.angelscript.psi.AngelscriptEnumVariant
import com.aesirinteractive.angelscript.psi.AngelscriptForeachStatement
import com.aesirinteractive.angelscript.psi.AngelscriptInterfaceDecl
import com.aesirinteractive.angelscript.psi.AngelscriptNamespaceDecl
import com.aesirinteractive.angelscript.psi.AngelscriptUClassDecl
import com.aesirinteractive.angelscript.psi.AngelscriptUFunctionDecl
import com.aesirinteractive.angelscript.psi.AngelscriptUPropertyDecl
import com.aesirinteractive.angelscript.psi.AngelscriptUStructDecl
import com.aesirinteractive.angelscript.psi.AngelscriptUEnumDecl
import com.aesirinteractive.angelscript.psi.AngelscriptUInterfaceDecl
import com.aesirinteractive.angelscript.psi.AngelscriptTypedefDecl
import com.aesirinteractive.angelscript.psi.AngelscriptAssetDecl
import com.aesirinteractive.angelscript.psi.AngelscriptArgument
import com.aesirinteractive.angelscript.psi.AngelscriptDelegateDecl
import com.aesirinteractive.angelscript.psi.AngelscriptEventDecl
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.PsiRecursiveElementWalkingVisitor

class AngelscriptAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Metadata values have their own syntax; do not recolor their identifiers
        // as ordinary variable accesses on a later annotator visit.
        if (generateSequence(element.parent) { it.parent }.any { isMetadata(it) }) return
        when (element) {
            is AngelscriptFunctionDecl       -> annotateFunctionDecl(element, holder)
            is AngelscriptConstructorDecl    -> highlight(element.identifier, holder, AngelscriptSyntaxHighlighter.MEMBER_FUNCTION_KEY)
            is AngelscriptClassDecl          -> annotateNamedDecl(element, holder)
            is AngelscriptStructDecl         -> annotateNamedDecl(element, holder)
            is AngelscriptEnumDecl           -> annotateNamedDecl(element, holder)
            is AngelscriptInterfaceDecl      -> annotateNamedDecl(element, holder)
            is AngelscriptTypedefDecl        -> annotateNamedDecl(element, holder)
            is AngelscriptAssetDecl          -> highlight(element.identifier, holder, AngelscriptSyntaxHighlighter.GLOBAL_VARIABLE_KEY)
            is AngelscriptArgument           -> element.identifier?.let { highlight(it, holder, AngelscriptSyntaxHighlighter.PARAMETER_KEY) }
            is AngelscriptEnumVariant        -> highlight(element.identifier, holder, AngelscriptSyntaxHighlighter.ENUM_CONSTANT_KEY)
            is AngelscriptForeachStatement   -> highlight(element.identifier, holder, AngelscriptSyntaxHighlighter.LOCAL_VARIABLE_KEY)
            is AngelscriptNamespaceDecl      -> element.node.getChildren(null)
                .takeWhile { it.elementType != AngelscriptTypes.LBRACE }
                .filter { it.elementType == AngelscriptTypes.IDENTIFIER }
                .forEach { highlight(it.psi, holder, AngelscriptSyntaxHighlighter.NAMESPACE_KEY) }
            is AngelscriptUClassDecl, is AngelscriptUFunctionDecl, is AngelscriptUPropertyDecl,
            is AngelscriptUStructDecl, is AngelscriptUEnumDecl, is AngelscriptUInterfaceDecl -> annotateMetadata(element, holder)
            is AngelscriptParameterDecl      -> highlight(element.identifier, holder, AngelscriptSyntaxHighlighter.PARAMETER_KEY)
            is AngelscriptVariableDecl       -> annotateVariableDecl(element, holder)
            is AngelscriptTypeRef            -> annotateTypeRef(element, holder)
            is AngelscriptScopeRef           -> annotateScopeRef(element, holder)
            is AngelscriptVariableAccessExpr -> annotateVariableAccessExpr(element, holder)
            is AngelscriptPostfixExpr        -> annotatePostfixExpr(element, holder)
        }
    }

    private fun isMetadata(element: PsiElement) = element is AngelscriptUClassDecl ||
        element is AngelscriptUFunctionDecl || element is AngelscriptUPropertyDecl ||
        element is AngelscriptUStructDecl || element is AngelscriptUEnumDecl || element is AngelscriptUInterfaceDecl

    private fun highlight(element: PsiElement, holder: AnnotationHolder, key: TextAttributesKey) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(element.textRange)
            .textAttributes(key)
            .create()
    }

    private fun annotateFunctionDecl(decl: AngelscriptFunctionDecl, holder: AnnotationHolder) {
        val key = if (decl.parent is AngelscriptDelegateDecl || decl.parent is AngelscriptEventDecl)
            AngelscriptCppColors.TYPEDEF
        else if (AngelscriptHighlightingSymbols.isMemberDeclaration(decl)) AngelscriptSyntaxHighlighter.MEMBER_FUNCTION_KEY
        else AngelscriptSyntaxHighlighter.FUNCTION_DECLARATION_KEY
        highlight(decl.identifier, holder, key)
    }

    // ClassDecl / StructDecl: highlight the class/struct name token
    private fun annotateNamedDecl(element: PsiElement, holder: AnnotationHolder) {
        val nameNode = element.node.findChildByType(AngelscriptTypes.IDENTIFIER) ?: return
        highlight(nameNode.psi, holder, AngelscriptHighlightingSymbols.declarationTypeKey(element))
    }

    private fun annotateVariableDecl(decl: AngelscriptVariableDecl, holder: AnnotationHolder) {
        val key = AngelscriptHighlightingSymbols.variableKey(decl)
        AngelscriptHighlightingSymbols.variableNames(decl).forEach { highlight(it, holder, key) }
        for (child in decl.node.getChildren(null).dropWhile { it.elementType != AngelscriptTypes.LBRACE }) {
            if (child.elementType == AngelscriptTypes.IDENTIFIER && child.text in setOf("get", "set"))
                highlight(child.psi, holder, AngelscriptSyntaxHighlighter.TOKEN_KEYS.getValue(AngelscriptTypes.PROPERTY_KW).single())
        }
    }

    private fun annotateMetadata(element: PsiElement, holder: AnnotationHolder) {
        element.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element.node.elementType == AngelscriptTypes.IDENTIFIER)
                    highlight(element, holder, AngelscriptSyntaxHighlighter.METADATA_SPECIFIER_KEY)
                super.visitElement(element)
            }
        })
    }

    private fun annotateTypeRef(typeRef: AngelscriptTypeRef, holder: AnnotationHolder) {
        // DataType is a private rule — its content is inlined as flat ASTNode children of TypeRef.
        // Must use node.getChildren() to see leaf tokens; .children only returns composite PSI nodes
        // and would miss the IDENTIFIER leaf entirely.
        // Walk past any leading CONST token and ScopeRef composite, then check the DataType slot.
        for (child in typeRef.node.getChildren(null)) {
            val type = child.elementType
            if (type == AngelscriptTypes.CONST || type == TokenType.WHITE_SPACE) continue
            if (child.psi is AngelscriptScopeRef) continue
            if (type == AngelscriptTypes.IDENTIFIER) {
                val key = AngelscriptHighlightingSymbols.forSession(holder.currentAnnotationSession).keyFor(typeRef)
                    ?: typeKeyFor(child.text)
                highlight(child.psi, holder, key)
            }
            // PrimitiveType keyword, '?', 'auto', or TypeArgument — stop either way
            break
        }
    }

    private fun typeKeyFor(name: String): TextAttributesKey =
        if (AngelscriptPsiUtil.isUnrealEnumName(name)) AngelscriptCppColors.ENUM
        else if (name.length > 1 && name[0] == 'F' && name[1].isUpperCase()) AngelscriptCppColors.STRUCT
        else AngelscriptSyntaxHighlighter.TYPE_REF_KEY

    private fun annotateScopeRef(scopeRef: AngelscriptScopeRef, holder: AnnotationHolder) {
        for (child in scopeRef.node.getChildren(null)) {
            if (child.elementType == AngelscriptTypes.IDENTIFIER) {
                val key = if (AngelscriptPsiUtil.isUnrealTypeName(child.text)) typeKeyFor(child.text)
                    else AngelscriptSyntaxHighlighter.NAMESPACE_KEY
                highlight(child.psi, holder, key)
            }
        }
    }

    private fun annotateVariableAccessExpr(expr: AngelscriptVariableAccessExpr, holder: AnnotationHolder) {
        if (expr.identifier.text in setOf("this", "Super", "Cast")) return // lexical keyword color
        val parent = expr.parent
        val resolved = AngelscriptHighlightingSymbols.forSession(holder.currentAnnotationSession).keyFor(expr)
        val key = if (resolved != null) resolved
        else if (parent is AngelscriptPostfixExpr && AngelscriptPsiUtil.isCallPosition(expr)) {
            AngelscriptSyntaxHighlighter.FUNCTION_CALL_KEY
        } else {
            val name = expr.identifier.text
            val typeKey = typeKeyFor(name)
            if (AngelscriptPsiUtil.isUnrealTypeName(name)) typeKey
            else AngelscriptSyntaxHighlighter.LOCAL_VARIABLE_KEY
        }
        highlight(expr.identifier, holder, key)
    }

    // PostfixExpr: annotate DOT+IDENTIFIER pairs as field access or method calls,
    // and COLONCOLON+IDENTIFIER pairs as namespace/scope references.
    private fun annotatePostfixExpr(expr: AngelscriptPostfixExpr, holder: AnnotationHolder) {
        val children = expr.node.getChildren(null)
        var i = 0
        while (i < children.size) {
            val child = children[i]
            when (child.elementType) {
                AngelscriptTypes.DOT -> {
                    // Peek at the next non-whitespace sibling: should be IDENTIFIER
                    var j = i + 1
                    while (j < children.size && children[j].elementType == TokenType.WHITE_SPACE) j++
                    if (j >= children.size || children[j].elementType != AngelscriptTypes.IDENTIFIER) {
                        i = j
                        continue
                    }
                    val ident = children[j]
                    // Determine if followed by a call suffix (TypeArgument or ArgList)
                    var k = j + 1
                    while (k < children.size && children[k].elementType == TokenType.WHITE_SPACE) k++
                    val afterIdent = if (k < children.size) children[k] else null
                    val isCall = afterIdent?.psi is AngelscriptTypeArgument
                              || afterIdent?.psi is AngelscriptArgList
                    highlight(
                        ident.psi,
                        holder,
                        if (isCall) AngelscriptSyntaxHighlighter.MEMBER_FUNCTION_KEY
                        else AngelscriptSyntaxHighlighter.INSTANCE_FIELD_KEY
                    )
                    i = j + 1
                    continue
                }
                AngelscriptTypes.COLONCOLON -> {
                    // ScopeSuffix: ::IDENTIFIER — highlight as a type/namespace reference
                    var j = i + 1
                    while (j < children.size && children[j].elementType == TokenType.WHITE_SPACE) j++
                    if (j < children.size && children[j].elementType == AngelscriptTypes.IDENTIFIER) {
                        highlight(children[j].psi, holder, AngelscriptSyntaxHighlighter.TYPE_REF_KEY)
                        i = j + 1
                        continue
                    }
                }
            }
            i++
        }
    }
}
