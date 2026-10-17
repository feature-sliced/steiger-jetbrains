package org.featuresliced.steiger.settings

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.panel
import org.featuresliced.steiger.lsp.SteigerLspServerSupportProvider
import javax.swing.JComponent

/**
 * JavaScript-plugin-backed settings UI with the same Node interpreter selector
 * as built-in run configurations.
 *
 * Loaded only via the `settingsUi` extension point registered in
 * `META-INF/steiger-js.xml`, which is merged only when the JavaScript plugin
 * is present. Never reference this class from always-loaded code.
 */
class SteigerJsSettingsUi : SteigerSettingsUi {

    override fun createPanel(project: Project, settings: SteigerSettings): SteigerSettingsPanel =
        SteigerJsSettingsPanel(project, settings)

    private class SteigerJsSettingsPanel(
        private val project: Project,
        private val settings: SteigerSettings,
    ) : SteigerSettingsPanel {
        private val useDefaultCheckBox = JBCheckBox("Use the project default Node interpreter")
        private val interpreterField = NodeJsInterpreterField(project)
        private val useBundledServerCheckBox = JBCheckBox("Use the bundled language server")
        private val serverPathField = TextFieldWithBrowseButton().apply {
            addBrowseFolderListener(project, FileChooserDescriptorFactory.singleFile())
        }

        init {
            useDefaultCheckBox.addItemListener { interpreterField.isEnabled = !useDefaultCheckBox.isSelected }
            useBundledServerCheckBox.addItemListener { serverPathField.isEnabled = !useBundledServerCheckBox.isSelected }
        }

        override val component: JComponent = panel {
            row { cell(useDefaultCheckBox) }
            row("Node interpreter:") {
                cell(interpreterField).resizableColumn()
            }
            row { cell(useBundledServerCheckBox) }
            row("Language server:") {
                cell(serverPathField).resizableColumn()
            }.comment("A custom steiger language server (a Node.js script, run with the interpreter above)")
        }
        override fun isModified(): Boolean =
            useDefaultCheckBox.isSelected != settings.useProjectDefault ||
                currentRefName() != settings.interpreterRefName ||
                useBundledServerCheckBox.isSelected != settings.useBundledLanguageServer ||
                (!useBundledServerCheckBox.isSelected && serverPathField.text.trim() != (settings.customLanguageServerPath ?: ""))

        override fun reset() {
            useDefaultCheckBox.isSelected = settings.useProjectDefault
            val ref = settings.interpreterRefName?.let { NodeJsInterpreterRef.create(it) }
                ?: NodeJsInterpreterManager.getInstance(project).interpreterRef
                ?: NodeJsInterpreterRef.create("node")
            interpreterField.setInterpreterRef(ref)
            interpreterField.setPreferredWidthToFitText()
            interpreterField.isEnabled = !useDefaultCheckBox.isSelected
            useBundledServerCheckBox.isSelected = settings.useBundledLanguageServer
            serverPathField.text = settings.customLanguageServerPath ?: ""
            serverPathField.isEnabled = !useBundledServerCheckBox.isSelected
        }

        override fun apply() {
            settings.useProjectDefault = useDefaultCheckBox.isSelected
            settings.interpreterRefName = currentRefName()
            settings.useBundledLanguageServer = useBundledServerCheckBox.isSelected
            settings.customLanguageServerPath =
                if (useBundledServerCheckBox.isSelected) null
                else serverPathField.text.trim().ifBlank { null }
            LspServerManager.getInstance(project)
                .stopAndRestartIfNeeded(SteigerLspServerSupportProvider::class.java)
        }

        private fun currentRefName(): String? =
            if (useDefaultCheckBox.isSelected) null else interpreterField.interpreterRef?.referenceName
    }
}
