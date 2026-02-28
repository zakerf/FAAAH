package nl.rekaz.faaah

import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.execution.testframework.TestStatusListener

class TestFailureSoundListener : TestStatusListener() {
    override fun testSuiteFinished(root: AbstractTestProxy?) {
        if (root != null) {
            if (root.isPassed) {
                TestFailureSoundPlayer.playSuccessSound()
            } else {
                TestFailureSoundPlayer.playFailureSound()
            }
        }
    }
}
