package nl.rekaz.faaah

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager

class PlayTestFailureSoundAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        TestFailureSoundPlayer.playFailureSound()
    }

    override fun update(e: AnActionEvent) {
        // Only enabled when in test mode (internal mode) and not in production
        val isInternal = ApplicationManager.getApplication().isInternal
        e.presentation.isEnabledAndVisible = isInternal
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class PlayTestSuccessSoundAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        TestFailureSoundPlayer.playSuccessSound()
    }

    override fun update(e: AnActionEvent) {
        // Only enabled when in test mode (internal mode) and not in production
        val isInternal = ApplicationManager.getApplication().isInternal
        e.presentation.isEnabledAndVisible = isInternal
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
