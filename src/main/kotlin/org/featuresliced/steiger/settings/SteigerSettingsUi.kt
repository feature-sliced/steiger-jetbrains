package org.featuresliced.steiger.settings

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import org.featuresliced.steiger.lsp.SteigerLspServerSupportProvider
import javax.swing.JComponent

/**
 * A settings panel abstraction so the JavaScript-plugin-backed UI is only loaded
 * when that optional plugin is present (see `META-INF/steiger-js.xml`).
 */
interface SteigerSettingsUi {
    fun createPanel(project: Project, settings: SteigerSettings): SteigerSettingsPanel

    companion object {
        val EP_NAME: ExtensionPointName<SteigerSettingsUi> =
            ExtensionPointName.create("org.featuresliced.steiger.settingsUi")
    }
}

interface SteigerSettingsPanel {
    val component: JComponent
    fun isModified(): Boolean
    fun reset()
    fun apply()
}

/**
 * Fallback UI used when the JavaScript plugin is unavailable: plain path fields
 * for the Node interpreter and the language server.
 */
class SteigerPlainSettingsUi : SteigerSettingsUi {
    override fun createPanel(project: Project, settings: SteigerSettings): SteigerSettingsPanel =
        SteigerPlainSettingsPanel(project, settings)

    private class SteigerPlainSettingsPanel(
        private val project: Project,
        private val settings: SteigerSettings,
    ) : SteigerSettingsPanel {
        private val pathField = JBTextField()
        private val useBundledServerCheckBox = JBCheckBox("Use the bundled language server")
        private val serverPathField = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(project, FileChooserDescriptorFactory.singleFile())
        }

        init {
            useBundledServerCheckBox.addItemListener { serverPathField.isEnabled = !useBundledServerCheckBox.isSelected }
        }

        override val component: JComponent = panel {
            row("Node interpreter:") {
                cell(pathField).resizableColumn()
            }.comment(
                "JavaScript support is unavailable in this IDE, so the interpreter selector is disabled. " +
                    "Enter a Node.js executable path, or leave empty to use node from PATH."
            )
            row { cell(useBundledServerCheckBox) }
            row("Language server:") {
                cell(serverPathField).resizableColumn()
            }.comment("A custom steiger language server (a Node.js script, run with the interpreter above)")
        }

        override fun isModified(): Boolean =
            pathField.text.trim() != (settings.nodePath ?: "") ||
                useBundledServerCheckBox.isSelected != settings.useBundledLanguageServer ||
                (!useBundledServerCheckBox.isSelected && serverPathField.text.trim() != (settings.customLanguageServerPath ?: ""))

        override fun reset() {
            pathField.text = settings.nodePath ?: ""
            useBundledServerCheckBox.isSelected = settings.useBundledLanguageServer
            serverPathField.text = settings.customLanguageServerPath ?: ""
            serverPathField.isEnabled = !useBundledServerCheckBox.isSelected
        }

        override fun apply() {
            settings.nodePath = pathField.text.trim().ifBlank { null }
            settings.useBundledLanguageServer = useBundledServerCheckBox.isSelected
            settings.customLanguageServerPath =
                if (useBundledServerCheckBox.isSelected) null
                else serverPathField.text.trim().ifBlank { null }
            LspServerManager.getInstance(project)
                .stopAndRestartIfNeeded(SteigerLspServerSupportProvider::class.java)
        }
    }
}
