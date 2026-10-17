package org.featuresliced.steiger.settings

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent
import javax.swing.JLabel

/**
 * A settings panel abstraction so the JavaScript-plugin-backed UI is only loaded
 * when that optional plugin is present (see `META-INF/steiger-js.xml`).
 */
interface SteigerSettingsUi {
    fun createPanel(project: Project, settings: SteigerSettings): SteigerSettingsPanel

    companion object {
        val EP_NAME: ExtensionPointName<SteigerSettingsUi> =
            ExtensionPointName.create("org.feature-sliced.steiger.jetbrains.settingsUi")
    }
}

interface SteigerSettingsPanel {
    val component: JComponent
    fun isModified(): Boolean
    fun reset()
    fun apply()
}

/**
 * Fallback UI used when the JavaScript plugin is unavailable: a plain
 * executable path field.
 */
class SteigerPlainSettingsUi : SteigerSettingsUi {
        private val pathField = JBTextField().apply { columns = 36 }
    override fun createPanel(project: Project, settings: SteigerSettings): SteigerSettingsPanel =
        SteigerPlainSettingsPanel(settings)

    private class SteigerPlainSettingsPanel(private val settings: SteigerSettings) : SteigerSettingsPanel {
        private val pathField = JBTextField()

        override val component: JComponent = panel {
            row("Node interpreter:") {
                cell(pathField).resizableColumn()
            }.resizableRow().comment(
                "JavaScript support is unavailable in this IDE, so the interpreter selector is disabled. " +
                    "Enter a Node.js executable path, or leave empty to use node from PATH."
            )
        }

        override fun isModified(): Boolean = pathField.text.trim() != (settings.nodePath ?: "")

        override fun reset() {
            pathField.text = settings.nodePath ?: ""
        }

        override fun apply() {
            settings.nodePath = pathField.text.trim().ifBlank { null }
        }
    }
}
