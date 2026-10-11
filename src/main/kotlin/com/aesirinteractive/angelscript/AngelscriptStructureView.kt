package com.aesirinteractive.angelscript

import com.aesirinteractive.angelscript.psi.AngelscriptClassDecl
import com.aesirinteractive.angelscript.psi.AngelscriptAssetDecl
import com.aesirinteractive.angelscript.psi.AngelscriptConstructorDecl
import com.aesirinteractive.angelscript.psi.AngelscriptDelegateDecl
import com.aesirinteractive.angelscript.psi.AngelscriptEnumDecl
import com.aesirinteractive.angelscript.psi.AngelscriptEnumVariant
import com.aesirinteractive.angelscript.psi.AngelscriptEventDecl
import com.aesirinteractive.angelscript.psi.AngelscriptFunctionDecl
import com.aesirinteractive.angelscript.psi.AngelscriptInterfaceDecl
import com.aesirinteractive.angelscript.psi.AngelscriptMixinDecl
import com.aesirinteractive.angelscript.psi.AngelscriptNamespaceDecl
import com.aesirinteractive.angelscript.psi.AngelscriptStructDecl
import com.aesirinteractive.angelscript.psi.AngelscriptTypedefDecl
import com.aesirinteractive.angelscript.psi.AngelscriptVariableDecl
import com.intellij.icons.AllIcons
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import javax.swing.Icon

class AngelscriptStructureViewFactory : PsiStructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile) =
        object : TreeBasedStructureViewBuilder() {
            override fun createStructureViewModel(editor: Editor?) =
                AngelscriptStructureViewModel(psiFile, editor)
        }
}

class AngelscriptStructureViewModel(psiFile: PsiFile, editor: Editor?)
    : StructureViewModelBase(psiFile, editor, AngelscriptStructureViewElement(psiFile)),
      StructureViewModel.ElementInfoProvider {

    init {
        withSuitableClasses(
            AngelscriptNamespaceDecl::class.java,
            AngelscriptAssetDecl::class.java,
            AngelscriptClassDecl::class.java,
            AngelscriptStructDecl::class.java,
            AngelscriptInterfaceDecl::class.java,
            AngelscriptEnumDecl::class.java,
            AngelscriptEnumVariant::class.java,
            AngelscriptFunctionDecl::class.java,
            AngelscriptConstructorDecl::class.java,
            AngelscriptEventDecl::class.java,
            AngelscriptDelegateDecl::class.java,
            AngelscriptMixinDecl::class.java,
            AngelscriptTypedefDecl::class.java,
            AngelscriptVariableDecl::class.java,
        )
    }

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement) = false

    override fun isAlwaysLeaf(element: StructureViewTreeElement) = when (element.value) {
        is AngelscriptFunctionDecl,
        is AngelscriptAssetDecl,
        is AngelscriptConstructorDecl,
        is AngelscriptEventDecl,
        is AngelscriptDelegateDecl,
        is AngelscriptMixinDecl,
        is AngelscriptTypedefDecl,
        is AngelscriptVariableDecl,
        is AngelscriptEnumVariant -> true
        else -> false
    }
}

class AngelscriptStructureViewElement(private val psi: PsiElement) : StructureViewTreeElement {

    override fun getValue(): PsiElement = psi

    override fun navigate(requestFocus: Boolean) { (psi as? Navigatable)?.navigate(requestFocus) }
    override fun canNavigate(): Boolean = (psi as? Navigatable)?.canNavigate() ?: false
    override fun canNavigateToSource(): Boolean = (psi as? Navigatable)?.canNavigateToSource() ?: false

    override fun getPresentation(): ItemPresentation = object : ItemPresentation {
        override fun getPresentableText(): String? = when (psi) {
            is PsiFile                 -> psi.name
            is AngelscriptNamedElement -> psi.name
            else                       -> null
        }
        override fun getLocationString(): String? = null
        override fun getIcon(unused: Boolean): Icon? = iconFor(psi)
    }

    override fun getChildren(): Array<TreeElement> = when (psi) {
        is PsiFile -> PsiTreeUtil.getChildrenOfAnyType(
            psi,
            AngelscriptNamespaceDecl::class.java,
            AngelscriptAssetDecl::class.java,
            AngelscriptClassDecl::class.java,
            AngelscriptStructDecl::class.java,
            AngelscriptInterfaceDecl::class.java,
            AngelscriptEnumDecl::class.java,
            AngelscriptFunctionDecl::class.java,
            AngelscriptEventDecl::class.java,
            AngelscriptDelegateDecl::class.java,
            AngelscriptMixinDecl::class.java,
            AngelscriptTypedefDecl::class.java,
            AngelscriptVariableDecl::class.java,
        ).map { AngelscriptStructureViewElement(it) }.toTypedArray()

        is AngelscriptNamespaceDecl -> namespaceChildren(psi)
        is AngelscriptClassDecl     -> typeMembers(psi.constructorDeclList, psi.functionDeclList, psi.variableDeclList)
        is AngelscriptStructDecl    -> typeMembers(psi.constructorDeclList, psi.functionDeclList, psi.variableDeclList)
        is AngelscriptInterfaceDecl -> typeMembers(psi.constructorDeclList, psi.functionDeclList, psi.variableDeclList)
        is AngelscriptEnumDecl      -> psi.enumVariantList.map { AngelscriptStructureViewElement(it) }.toTypedArray()
        else -> emptyArray()
    }

    private fun namespaceChildren(psi: AngelscriptNamespaceDecl): Array<TreeElement> =
        (psi.namespaceDeclList +
         psi.assetDeclList +
         psi.classDeclList +
         psi.structDeclList +
         psi.interfaceDeclList +
         psi.enumDeclList +
         psi.functionDeclList +
         psi.eventDeclList +
         psi.delegateDeclList +
         psi.mixinDeclList +
         psi.typedefDeclList +
         psi.variableDeclList)
            .sortedBy { it.textOffset }
            .map { AngelscriptStructureViewElement(it) }
            .toTypedArray()

    private fun typeMembers(
        constructors: List<AngelscriptConstructorDecl>,
        functions: List<AngelscriptFunctionDecl>,
        variables: List<AngelscriptVariableDecl>,
    ): Array<TreeElement> = (constructors + functions + variables)
        .sortedBy { it.textOffset }
        .map { AngelscriptStructureViewElement(it) }
        .toTypedArray()

    companion object {
        fun iconFor(psi: PsiElement): Icon? = when (psi) {
            is AngelscriptNamespaceDecl             -> AllIcons.Nodes.Package
            is AngelscriptAssetDecl                 -> AllIcons.Nodes.Variable
            is AngelscriptClassDecl                 -> AllIcons.Nodes.Class
            is AngelscriptInterfaceDecl             -> AllIcons.Nodes.Interface
            is AngelscriptStructDecl                -> AllIcons.Nodes.Record
            is AngelscriptEnumDecl                  -> AllIcons.Nodes.Enum
            is AngelscriptEnumVariant               -> AllIcons.Nodes.Constant
            is AngelscriptFunctionDecl,
            is AngelscriptConstructorDecl           -> AllIcons.Nodes.Function
            is AngelscriptEventDecl,
            is AngelscriptDelegateDecl,
            is AngelscriptMixinDecl                 -> AllIcons.Nodes.Function
            is AngelscriptTypedefDecl               -> AllIcons.Nodes.Alias
            is AngelscriptVariableDecl              -> {
                val p = psi.parent
                if (p is AngelscriptClassDecl || p is AngelscriptStructDecl || p is AngelscriptInterfaceDecl)
                    AllIcons.Nodes.Field else AllIcons.Nodes.Variable
            }
            else -> null
        }
    }
}
