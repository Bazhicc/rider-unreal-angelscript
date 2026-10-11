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
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType

class AngelscriptAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        when (element) {
            is AngelscriptFunctionDecl       -> annotateFunctionDecl(element, holder)
            is AngelscriptConstructorDecl    -> highlight(element.identifier, holder, AngelscriptSyntaxHighlighter.FUNCTION_DECLARATION_KEY)
            is AngelscriptClassDecl          -> annotateNamedDecl(element, holder)
            is AngelscriptStructDecl         -> annotateNamedDecl(element, holder)
            is AngelscriptEnumDecl           -> annotateNamedDecl(element, holder)
            is AngelscriptParameterDecl      -> highlight(element.identifier, holder, AngelscriptSyntaxHighlighter.PARAMETER_KEY)
            is AngelscriptVariableDecl       -> annotateVariableDecl(element, holder)
            is AngelscriptTypeRef            -> annotateTypeRef(element, holder)
            is AngelscriptVariableAccessExpr -> annotateVariableAccessExpr(element, holder)
            is AngelscriptPostfixExpr        -> annotatePostfixExpr(element, holder)
        }
    }

    private fun highlight(element: PsiElement, holder: AnnotationHolder, key: TextAttributesKey) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(element.textRange)
            .textAttributes(key)
            .create()
    }

    private fun annotateFunctionDecl(decl: AngelscriptFunctionDecl, holder: AnnotationHolder) {
        highlight(decl.identifier, holder, AngelscriptSyntaxHighlighter.FUNCTION_DECLARATION_KEY)
    }

    // ClassDecl / StructDecl: highlight the class/struct name token
    private fun annotateNamedDecl(element: PsiElement, holder: AnnotationHolder) {
        val nameNode = element.node.findChildByType(AngelscriptTypes.IDENTIFIER) ?: return
        highlight(nameNode.psi, holder, typeKeyFor(nameNode.text))
    }

    // VariableDecl: VariableItem is a private rule, so its IDENTIFIERs are inlined as flat
    // children of VariableDecl after the TypeRef. Walk them and highlight each name.
    // Use INSTANCE_FIELD_KEY when the decl is a class/struct member, LOCAL_VARIABLE_KEY otherwise.
    private fun annotateVariableDecl(decl: AngelscriptVariableDecl, holder: AnnotationHolder) {
        val key = if (isMemberContext(decl)) AngelscriptSyntaxHighlighter.INSTANCE_FIELD_KEY
                  else AngelscriptSyntaxHighlighter.LOCAL_VARIABLE_KEY
        // Skip the leading TypeRef (and optional UPropertyDecl/AccessSpecifier tokens).
        // Everything after the TypeRef until SEMICOLON follows the pattern:
        //   IDENTIFIER (EQ Expr | ArgList)? (COMMA IDENTIFIER ...)*
        var pastTypeRef = false
        for (child in decl.node.getChildren(null)) {
            if (!pastTypeRef) {
                if (child.psi is AngelscriptTypeRef) pastTypeRef = true
                continue
            }
            if (child.elementType == AngelscriptTypes.IDENTIFIER) {
                highlight(child.psi, holder, key)
            }
        }
    }

    // Returns true if the nearest enclosing declaration scope is a class, struct, or enum body
    // (i.e. the VariableDecl is a member field, not a local variable).
    private fun isMemberContext(decl: AngelscriptVariableDecl): Boolean {
        var node = decl.parent
        while (node != null) {
            when (node) {
                is AngelscriptClassDecl,
                is AngelscriptStructDecl,
                is AngelscriptEnumDecl -> return true
                // A function or constructor body — it's a local
                is AngelscriptFunctionDecl,
                is AngelscriptConstructorDecl -> return false
            }
            node = node.parent
        }
        return false
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
                highlight(child.psi, holder, typeKeyFor(child.text))
            }
            // PrimitiveType keyword, '?', 'auto', or TypeArgument — stop either way
            break
        }
    }

    private fun typeKeyFor(name: String): TextAttributesKey =
        if (AngelscriptPsiUtil.isUnrealTypeName(name)) AngelscriptSyntaxHighlighter.UNREAL_TYPE_KEY
        else if (AngelscriptPsiUtil.isUnrealEnumName(name)) AngelscriptSyntaxHighlighter.UNREAL_TYPE_KEY
        else AngelscriptSyntaxHighlighter.TYPE_REF_KEY

    // VariableAccessExpr: the base name of a PostfixExpr chain, optionally preceded by a ScopeRef.
    // - ScopeRef identifiers (e.g. "FMath" in "FMath::Abs(t)") get typeKeyFor coloring.
    // - If the PostfixExpr's first suffix is a call → FUNCTION_CALL_KEY on the main identifier.
    // - If the name looks like a type (Unreal prefix) → UNREAL_TYPE_KEY / TYPE_REF_KEY.
    // - Otherwise → LOCAL_VARIABLE_KEY.
    private fun annotateVariableAccessExpr(expr: AngelscriptVariableAccessExpr, holder: AnnotationHolder) {
        // Highlight each identifier inside the ScopeRef using type coloring
        val scopeRef = expr.scopeRef
        if (scopeRef != null) {
            for (child in scopeRef.node.getChildren(null)) {
                if (child.elementType == AngelscriptTypes.IDENTIFIER) {
                    highlight(child.psi, holder, typeKeyFor(child.text))
                }
            }
        }

        val parent = expr.parent
        val key = if (parent is AngelscriptPostfixExpr && AngelscriptPsiUtil.isCallPosition(expr)) {
            AngelscriptSyntaxHighlighter.FUNCTION_CALL_KEY
        } else {
            val name = expr.identifier.text
            val typeKey = typeKeyFor(name)
            if (typeKey != AngelscriptSyntaxHighlighter.TYPE_REF_KEY) typeKey
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
                        if (isCall) AngelscriptSyntaxHighlighter.FUNCTION_CALL_KEY
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
