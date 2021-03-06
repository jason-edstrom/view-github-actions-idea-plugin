package org.github.otanikotani.workflow.action

import com.intellij.icons.AllIcons
import com.intellij.ide.actions.RefreshAction
import com.intellij.openapi.actionSystem.AnActionEvent

class GitHubWorkflowRunReloadListAction : RefreshAction("Refresh Workflow Runs", null, AllIcons.Actions.Refresh) {
    override fun update(e: AnActionEvent) {
        val context = e.getData(GitHubWorkflowRunActionKeys.ACTION_DATA_CONTEXT)
        e.presentation.isEnabled = context != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        e.getRequiredData(GitHubWorkflowRunActionKeys.ACTION_DATA_CONTEXT).resetAllData()
    }
}