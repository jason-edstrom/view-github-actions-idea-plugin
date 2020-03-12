package org.github.otanikotani.workflow

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.DataProvider
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.ui.ListUtil
import com.intellij.ui.ScrollingUtil
import com.intellij.ui.components.JBList
import com.intellij.util.text.DateFormatUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListUiUtil
import com.intellij.util.ui.UIUtil
import com.jetbrains.rd.swing.pressOutside
import icons.GithubIcons
import net.miginfocom.layout.CC
import net.miginfocom.layout.LC
import net.miginfocom.swing.MigLayout
import org.github.otanikotani.api.GitHubWorkflow
import org.github.otanikotani.api.GitHubWorkflowRun
import org.github.otanikotani.workflow.action.GitHubWorkflowActionKeys
import org.jetbrains.concurrency.cancelledPromise
import org.jetbrains.plugins.github.api.data.pullrequest.GHPullRequestState
import java.awt.Component
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.*

class GitHubWorkflowRunList(model: ListModel<GitHubWorkflowRun>)
    : JBList<GitHubWorkflowRun>(model), DataProvider {

    init {
        selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION

        val renderer = WorkflowRunsListCellRenderer()
        cellRenderer = renderer
        UIUtil.putClientProperty(this, UIUtil.NOT_IN_HIERARCHY_COMPONENTS, listOf(renderer))

        ScrollingUtil.installActions(this)
    }

    override fun getToolTipText(event: MouseEvent): String? {
        val childComponent = ListUtil.getDeepestRendererChildComponentAt(this, event.point)
        if (childComponent !is JComponent) return null
        return childComponent.toolTipText
    }

    override fun getData(dataId: String): Any? = when {
        PlatformDataKeys.COPY_PROVIDER.`is`(dataId) -> this
        GitHubWorkflowActionKeys.SELECTED_WORKFLOW.`is`(dataId) -> selectedValue
        else -> null
    }

    private inner class WorkflowRunsListCellRenderer : ListCellRenderer<GitHubWorkflowRun>, JPanel() {

        private val stateIcon = JLabel()
        private val title = JLabel()
        private val info = JLabel()
        private val date = JLabel()
        private val labels = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
        }
        private val assignees = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
        }
        init {
            border = JBUI.Borders.empty(5, 8)

            layout = MigLayout(LC().gridGap("0", "0")
                .insets("0", "0", "0", "0")
                .fillX())

            val gapAfter = "${JBUI.scale(5)}px"
            add(stateIcon, CC()
                .gapAfter(gapAfter))
            add(title, CC()
                .minWidth("pref/2px")
                .growX()
                .pushX())
            add(date, CC()
                .minWidth("0px")
                .spanX(2))
            add(labels, CC()
                .growX()
                .pushX()
                .minWidth("0px")
                .gapAfter(gapAfter))
            add(assignees, CC()
                .spanY(2)
                .wrap())
            add(info, CC()
                .minWidth("0px")
                .skip(1)
                .spanX(2))
        }

        override fun getListCellRendererComponent(list: JList<out GitHubWorkflowRun>,
                                                  value: GitHubWorkflowRun,
                                                  index: Int,
                                                  isSelected: Boolean,
                                                  cellHasFocus: Boolean): Component {
            UIUtil.setBackgroundRecursively(this, ListUiUtil.WithTallRow.background(list, isSelected, list.hasFocus()))
            val primaryTextColor = ListUiUtil.WithTallRow.foreground(isSelected, list.hasFocus())
            val secondaryTextColor = ListUiUtil.WithTallRow.secondaryForeground(list, isSelected)

            stateIcon.apply {
                icon = when (value.status) {
                    "completed" -> {
                        when (value.conclusion) {
                            "success" ->  AllIcons.Actions.Checked
                            else -> AllIcons.Process.Step_mask
                        }
                    }//,
//                    "queued" ->
//                    "in progress" ->
//                    "neutral" ->
//                    "success" ->
//                    "failure" ->
//                    "cancelled" ->
//                    "action required" ->
//                    "timed out" ->
//                    "skipped" ->
//                    "stale" ->
                        else -> AllIcons.Process.Step_mask
                }
            }
            title.apply {
                text = value.head_commit.message
                foreground = primaryTextColor
            }
            info.apply {
                text = "${value.workflowName} #${value.run_number}: pushed by ${value.head_commit.author.name}"
                foreground = secondaryTextColor
            }
            date.apply {
                text = DateFormatUtil.formatPrettyDateTime(value.updated_at ?: Date())
                foreground = secondaryTextColor
            }
            return this
        }
    }
}