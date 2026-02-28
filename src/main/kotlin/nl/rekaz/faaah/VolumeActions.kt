package nl.rekaz.faaah

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.*

class AdjustVolumeAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val settings = SoundSettings.getInstance().state
        val dialog = SoundConfigDialog(settings)
        if (dialog.showAndGet()) {
            settings.failureSettings = dialog.getFailureSettings()
            settings.successSettings = dialog.getSuccessSettings()
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

private class SoundConfigDialog(state: SoundSettings.State) : DialogWrapper(true) {
    private val failureTab = SoundConfigTab("fail", state.failureSettings)
    private val successTab = SoundConfigTab("pass", state.successSettings)

    init {
        title = SoundBundle.message("dialog.configure.sounds.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val tabbedPane = JBTabbedPane()
        tabbedPane.addTab(SoundBundle.message("dialog.configure.sounds.failure.tab"), failureTab.createPanel())
        tabbedPane.addTab(SoundBundle.message("dialog.configure.sounds.success.tab"), successTab.createPanel())
        return tabbedPane
    }

    override fun doValidate(): ValidationInfo? {
        return failureTab.validate() ?: successTab.validate()
    }

    fun getFailureSettings() = failureTab.getSettings()
    fun getSuccessSettings() = successTab.getSettings()
}

private class SoundConfigTab(private val type: String, initialSettings: SoundSettings.SoundConfig) {
    private val enabledCheckbox = JBCheckBox(SoundBundle.message("dialog.configure.sounds.enabled"), initialSettings.isEnabled)
    private val volumeSlider = JSlider(0, 100, (initialSettings.volume * 100).toInt())
    
    private val defaultRadio = JBRadioButton(SoundBundle.message("dialog.configure.sounds.mode.default"))
    private val customRadio = JBRadioButton(SoundBundle.message("dialog.configure.sounds.mode.custom"))
    
    private val defaultSoundsMap = TestFailureSoundPlayer.getDefaultSounds(type)
    private val defaultList = ComboBox(defaultSoundsMap.keys.toTypedArray())
    private val customPathField = TextFieldWithBrowseButton()

    private val defaultPanel = JPanel(BorderLayout())
    private val customPanel = JPanel(BorderLayout())

    init {
        val bg = ButtonGroup()
        bg.add(defaultRadio)
        bg.add(customRadio)

        val isDefault = initialSettings.soundPath?.startsWith("$type/") ?: true
        if (isDefault) {
            defaultRadio.isSelected = true
            val path = initialSettings.soundPath ?: defaultSoundsMap.values.first()
            defaultList.selectedItem = defaultSoundsMap.entries.find { it.value == path }?.key
        } else {
            customRadio.isSelected = true
            customPathField.text = initialSettings.soundPath ?: ""
        }

        customPathField.addBrowseFolderListener(
            null,
            FileChooserDescriptorFactory.createSingleFileDescriptor().withTitle(SoundBundle.message("dialog.configure.sounds.mode.custom"))
        )

        updateVisibility()
        defaultRadio.addActionListener { updateVisibility() }
        customRadio.addActionListener { updateVisibility() }
    }

    private fun updateVisibility() {
        defaultPanel.isVisible = defaultRadio.isSelected
        customPanel.isVisible = customRadio.isSelected
    }

    fun createPanel(): JComponent {
        volumeSlider.majorTickSpacing = 20
        volumeSlider.minorTickSpacing = 5
        volumeSlider.paintTicks = true
        volumeSlider.paintLabels = true

        val verifyButton = JButton(SoundBundle.message("dialog.configure.sounds.verify"))
        verifyButton.addActionListener {
            TestFailureSoundPlayer.play(getSoundPath(), getVolume())
        }
        val verifyPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        verifyPanel.add(verifyButton)

        val radioPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        radioPanel.add(defaultRadio)
        radioPanel.add(Box.createHorizontalStrut(20))
        radioPanel.add(customRadio)

        defaultPanel.add(defaultList, BorderLayout.CENTER)
        customPanel.add(customPathField, BorderLayout.CENTER)

        return FormBuilder.createFormBuilder()
            .addComponent(enabledCheckbox)
            .addLabeledComponent(SoundBundle.message("dialog.configure.sounds.volume"), volumeSlider)
            .addSeparator()
            .addComponent(radioPanel)
            .addComponent(defaultPanel)
            .addComponent(customPanel)
            .addComponent(verifyPanel)
            .panel
    }

    fun validate(): ValidationInfo? {
        if (customRadio.isSelected) {
            val path = customPathField.text
            if (path.isEmpty() || !TestFailureSoundPlayer.isValidSoundFile(path)) {
                return ValidationInfo(SoundBundle.message("dialog.configure.sounds.invalid.file"), customPathField)
            }
        }
        return null
    }

    fun getSettings(): SoundSettings.SoundConfig {
        return SoundSettings.SoundConfig(
            isEnabled = enabledCheckbox.isSelected,
            volume = getVolume(),
            soundPath = getSoundPath()
        )
    }

    private fun getVolume(): Float = volumeSlider.value / 100.0f
    private fun getSoundPath(): String? {
        return if (defaultRadio.isSelected) {
            val selectedPrettyName = defaultList.selectedItem as? String
            defaultSoundsMap[selectedPrettyName]
        } else {
            customPathField.text.takeIf { it.isNotEmpty() }
        }
    }
}

class ToggleTestFailSoundAction : ToggleAction() {
    override fun isSelected(e: AnActionEvent): Boolean {
        return SoundSettings.getInstance().state.failureSettings.isEnabled
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        SoundSettings.getInstance().state.failureSettings.isEnabled = state
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}

class ToggleSoundSuccessSoundAction : ToggleAction() {
    override fun isSelected(e: AnActionEvent): Boolean {
        return SoundSettings.getInstance().state.successSettings.isEnabled
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        SoundSettings.getInstance().state.successSettings.isEnabled = state
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}
