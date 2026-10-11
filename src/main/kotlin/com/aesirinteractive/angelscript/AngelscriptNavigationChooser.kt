package com.aesirinteractive.angelscript

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.IPopupChooserBuilder
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

internal fun declarationNavigationTargets(targets: List<PsiElement>): Array<PsiElement>? {
    val presented = presentNavigationTargets(targets)
    if (presented.isEmpty()) return null
    val sources = presented.filterIsInstance<AngelscriptCppSourceTarget>()
    return if (sources.size > 1 && sources.size == presented.size)
        arrayOf(AngelscriptNavigationChooserTarget(sources))
    else presented.toTypedArray()
}

/** Keep the platform's navigation entry point; only ambiguous source targets need a tooltip-aware list. */
internal class AngelscriptNavigationChooserTarget(
    val targets: List<AngelscriptCppSourceTarget>,
) : FakePsiElement(), NavigatablePsiElement {
    override fun getParent(): PsiElement = targets.first().parent
    override fun getName() = "Choose Declaration"
    override fun getTextOffset() = targets.first().textOffset
    override fun canNavigate() = targets.any { it.canNavigate() }
    override fun canNavigateToSource() = canNavigate()
    override fun navigate(requestFocus: Boolean) {
        val available = targets.filter { it.canNavigate() }
        if (available.isEmpty()) return
        if (available.size == 1) {
            available.single().navigate(requestFocus)
            return
        }
        val popup = createPopupBuilder(available, requestFocus).createPopup()
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        if (editor != null) popup.showInBestPositionFor(editor) else popup.showInFocusCenter()
    }

    internal fun createPopupBuilder(available: List<AngelscriptCppSourceTarget>, requestFocus: Boolean): IPopupChooserBuilder<AngelscriptNavigationItem> =
        JBPopupFactory.getInstance().createPopupChooserBuilder(available.map(::AngelscriptNavigationItem))
            .setTitle("Choose Declaration")
            .setRenderer(AngelscriptNavigationRenderer())
            .setNamerForFiltering { it.name }
            .setItemChosenCallback { it.navigate(requestFocus) }
}

/** Popup models must not contain PSI. Snapshot presentation and the exact source location. */
internal class AngelscriptNavigationItem(target: AngelscriptCppSourceTarget) {
    val name = target.name
    val sourceLocation = target.sourceLocation
    val file = target.containingFile.virtualFile
    val offset = target.textOffset
    private val project = target.project

    fun navigate(requestFocus: Boolean) {
        if (!project.isDisposed && file.isValid)
            OpenFileDescriptor(project, file, offset).navigate(requestFocus)
    }
}

internal class AngelscriptNavigationRenderer : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int,
                                              isSelected: Boolean, cellHasFocus: Boolean): Component {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        val target = value as? AngelscriptNavigationItem
        text = target?.name ?: value?.toString().orEmpty()
        toolTipText = target?.sourceLocation
        return this
    }
}
