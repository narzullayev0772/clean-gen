package com.naviy.uz.clean_gen.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup


class ActionGroup : DefaultActionGroup() {
    override fun update(event: AnActionEvent) {
        // Enable/disable depending on whether user is editing
        val psi = event.getData(CommonDataKeys.PSI_ELEMENT)
        //event.presentation.isEnabled = project != null
        // Always make visible.
        event.presentation.isVisible = psi != null
        // Take this opportunity to set an icon for the menu entry.
        event.presentation.icon = AllIcons.Actions.NewFolder
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return super.getActionUpdateThread()
    }
}