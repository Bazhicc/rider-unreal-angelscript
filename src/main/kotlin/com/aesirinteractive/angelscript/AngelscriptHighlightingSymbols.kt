package com.aesirinteractive.angelscript

import com.aesirinteractive.angelscript.psi.*
import com.intellij.lang.annotation.AnnotationSession
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.util.PsiTreeUtil

/** File-local fallback only: highlighting must never trigger project/C++ navigation scans.
 * Build once per annotation pass so every reference does not rescan the file.
 */
internal class AngelscriptHighlightingSymbols private constructor(file: PsiFile) {
    private data class Binding(val identifier: PsiElement, val key: TextAttributesKey, val ordered: Boolean)
    private val scopes = HashMap<PsiElement, MutableMap<String, MutableList<Binding>>>()
    private val qualified = HashMap<String, MutableList<Binding>>()

    init {
        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                when (element) {
                    is AngelscriptVariableDecl -> {
                        val key = variableKey(element)
                        for (identifier in variableNames(element)) add(element, identifier, key, key == AngelscriptSyntaxHighlighter.LOCAL_VARIABLE_KEY)
                    }
                    is AngelscriptParameterDecl -> add(element, element.identifier, AngelscriptSyntaxHighlighter.PARAMETER_KEY)
                    is AngelscriptForeachStatement -> add(element, element.identifier, AngelscriptSyntaxHighlighter.LOCAL_VARIABLE_KEY, true, element)
                    is AngelscriptEnumVariant -> add(element, element.identifier, AngelscriptSyntaxHighlighter.ENUM_CONSTANT_KEY)
                    is AngelscriptAssetDecl -> add(element, element.identifier, AngelscriptSyntaxHighlighter.GLOBAL_VARIABLE_KEY)
                    is AngelscriptFunctionDecl -> if (element.parent !is AngelscriptDelegateDecl && element.parent !is AngelscriptEventDecl) {
                        add(element, element.identifier, if (isMemberDeclaration(element))
                            AngelscriptCppColors.METHOD else AngelscriptCppColors.FUNCTION)
                    }
                    is AngelscriptClassDecl, is AngelscriptStructDecl, is AngelscriptEnumDecl,
                    is AngelscriptInterfaceDecl, is AngelscriptTypedefDecl,
                    is AngelscriptDelegateDecl, is AngelscriptEventDecl -> element.nameIdentifier?.let {
                        add(element, it, declarationTypeKey(element))
                    }
                }
                super.visitElement(element)
            }
        })
    }

    private fun add(declaration: PsiElement, identifier: PsiElement, key: TextAttributesKey,
                    ordered: Boolean = false, owner: PsiElement = scopeOf(declaration)) {
        val binding = Binding(identifier, key, ordered)
        scopes.getOrPut(owner) { HashMap() }.getOrPut(identifier.text) { ArrayList() }.add(binding)
        if (owner is PsiFile || owner is AngelscriptNamespaceDecl || owner is AngelscriptEnumDecl) {
            qualified.getOrPut(qualifiedName(owner, identifier.text)) { ArrayList() }.add(binding)
        }
    }

    fun keyFor(expr: AngelscriptVariableAccessExpr): TextAttributesKey? = bindingFor(expr, expr.identifier.text, expr.scopeRef)?.key

    fun memberFieldKey(receiver: AngelscriptVariableAccessExpr, precedingMembers: List<String>): TextAttributesKey {
        val declaration = bindingFor(receiver, receiver.identifier.text, receiver.scopeRef)?.identifier?.parent
        fun declaredType(declaration: PsiElement?): AngelscriptTypeRef? = when (declaration) {
            is AngelscriptVariableDecl -> declaration.typeRef
            is AngelscriptParameterDecl -> declaration.typeRef
            is AngelscriptFunctionDecl -> declaration.typeRef
            else -> null
        }
        fun typeDeclaration(type: AngelscriptTypeRef?): PsiElement? = type?.identifier?.let {
            bindingFor(type, it.text, type.scopeRef)?.identifier?.parent
        }
        var type = declaredType(declaration)
        var owner = if (receiver.identifier.text == "this") generateSequence(receiver.parent) { it.parent }
            .firstOrNull { it is AngelscriptClassDecl || it is AngelscriptStructDecl } else typeDeclaration(type)
        for (name in precedingMembers) {
            val member = scopes[owner]?.get(name)?.lastOrNull()?.identifier?.parent
            type = declaredType(member)
            owner = typeDeclaration(type)
        }
        val isStruct = owner is AngelscriptStructDecl || type?.let { keyFor(it) == AngelscriptCppColors.STRUCT } == true
        return if (isStruct) AngelscriptCppColors.STRUCT_FIELD else AngelscriptCppColors.FIELD
    }

    fun keyFor(type: AngelscriptTypeRef): TextAttributesKey? = type.identifier?.let {
        bindingFor(type, it.text, type.scopeRef)?.key?.takeIf { key -> key in setOf(
            AngelscriptCppColors.CLASS, AngelscriptCppColors.STRUCT, AngelscriptCppColors.ENUM, AngelscriptCppColors.TYPEDEF) }
    }

    private fun bindingFor(expr: PsiElement, name: String, scopeRef: AngelscriptScopeRef?): Binding? {
        val qualifier = scopeRef?.text?.replace(Regex("\\s+"), "")
        if (qualifier != null) {
            val suffix = qualifier.removeSuffix("::").removePrefix("::")
            if (qualifier.startsWith("::")) return qualified[(if (suffix.isEmpty()) "" else "$suffix::") + name]?.singleOrNull()
            // Try the enclosing namespace before the absolute name (relative qualification).
            for (scope in generateSequence(expr.parent) { it.parent }) {
                if (scope is AngelscriptNamespaceDecl || scope is PsiFile) {
                    val prefix = qualifiedName(scope, "")
                    qualified["$prefix$suffix::$name"]?.singleOrNull()?.let { return it }
                }
            }
            return null
        }
        for (scope in generateSequence(expr.parent) { it.parent }) {
            val bindings = scopes[scope]?.get(name)
                ?: (if (scope is AngelscriptNamespaceDecl) qualified[qualifiedName(scope, name)] else null)
                ?: continue
            // A foreach variable is visible in the body, not in its collection expression.
            if (scope is AngelscriptForeachStatement && !PsiTreeUtil.isAncestor(scope.statement, expr, false)) continue
            val binding = bindings.lastOrNull { !it.ordered || it.identifier.textOffset < expr.textOffset }
            if (binding != null) return binding
        }
        return null
    }

    private fun qualifiedName(scope: PsiElement, name: String): String {
        val prefix = generateSequence(scope) { it.parent }
            .filter { it is AngelscriptNamespaceDecl || it is AngelscriptEnumDecl }
            .mapNotNull { (it as AngelscriptNamedElement).name }.toList().asReversed().joinToString("::")
        return if (prefix.isEmpty()) name else "$prefix::$name"
    }

    companion object {
        fun declarationTypeKey(element: PsiElement): TextAttributesKey = when (element) {
            is AngelscriptStructDecl -> AngelscriptCppColors.STRUCT
            is AngelscriptEnumDecl -> AngelscriptCppColors.ENUM
            is AngelscriptTypedefDecl, is AngelscriptDelegateDecl, is AngelscriptEventDecl -> AngelscriptCppColors.TYPEDEF
            else -> AngelscriptCppColors.CLASS
        }

        fun isMemberDeclaration(element: PsiElement) = scopeOf(element).let {
            it is AngelscriptClassDecl || it is AngelscriptStructDecl || it is AngelscriptInterfaceDecl
        }
        private val SESSION_KEY = Key.create<AngelscriptHighlightingSymbols>("angelscript.highlighting.symbols")

        fun forSession(session: AnnotationSession): AngelscriptHighlightingSymbols =
            session.getUserData(SESSION_KEY) ?: AngelscriptHighlightingSymbols(session.file).also {
                session.putUserData(SESSION_KEY, it)
            }

        fun variableNames(decl: AngelscriptVariableDecl): List<PsiElement> =
            decl.node.getChildren(null).dropWhile { it.psi !is AngelscriptTypeRef }.drop(1)
                .takeWhile { it.elementType != AngelscriptTypes.LBRACE } // inline property get/set are not variable names
                .filter { it.elementType == AngelscriptTypes.IDENTIFIER }.map { it.psi }

        fun variableKey(decl: AngelscriptVariableDecl): TextAttributesKey = when (scopeOf(decl)) {
            is AngelscriptStructDecl -> AngelscriptCppColors.STRUCT_FIELD
            is AngelscriptClassDecl, is AngelscriptInterfaceDecl -> AngelscriptSyntaxHighlighter.INSTANCE_FIELD_KEY
            is AngelscriptNamespaceDecl, is PsiFile -> AngelscriptSyntaxHighlighter.GLOBAL_VARIABLE_KEY
            else -> AngelscriptSyntaxHighlighter.LOCAL_VARIABLE_KEY
        }

        private fun scopeOf(declaration: PsiElement): PsiElement = generateSequence(declaration.parent) { it.parent }
            .first { it is AngelscriptCompoundStatement || it is AngelscriptFunctionDecl ||
                it is AngelscriptConstructorDecl || it is AngelscriptForStatement || it is AngelscriptForeachStatement ||
                it is AngelscriptClassDecl || it is AngelscriptStructDecl || it is AngelscriptInterfaceDecl ||
                it is AngelscriptNamespaceDecl || it is AngelscriptEnumDecl || it is PsiFile }
    }
}
