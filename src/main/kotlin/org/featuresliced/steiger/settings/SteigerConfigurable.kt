package org.featuresliced.steiger.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

/**
 * Settings → Tools → Steiger. Delegates to the JavaScript-plugin-backed panel
 * when available, or the plain path field otherwise.
 */
class SteigerConfigurable(private val project: Project) : Configurable {

    private val settings = SteigerSettings.getInstance(project)
    private var delegate: SteigerSettingsPanel? = null

    override fun getDisplayName() = "Steiger"

    override fun createComponent(): JComponent? {
        val ui = SteigerSettingsUi.EP_NAME.extensionList.firstOrNull()
            ?: SteigerPlainSettingsUi()
        val panel = ui.createPanel(project, settings)
        delegate = panel
        return panel.component
    }

    override fun isModified(): Boolean = delegate?.isModified() ?: false

    override fun reset() {
        delegate?.reset()
    }

    override fun apply() {
        delegate?.apply()
    }
}
