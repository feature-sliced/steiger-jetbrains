package org.featuresliced.steiger.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Per-project plugin settings, persisted to `.idea/steiger.xml`.
 */
@State(name = "SteigerSettings", storages = [Storage("steiger.xml")])
@Service(Service.Level.PROJECT)
class SteigerSettings : PersistentStateComponent<SteigerSettings> {

    /** Use the project's default Node.js interpreter (Settings → Languages & Frameworks → Node.js). */
    var useProjectDefault: Boolean = true

    /** Reference name of the explicitly selected interpreter, when [useProjectDefault] is false. */
    var interpreterRefName: String? = null

    /** Plain Node.js executable path, used when the JavaScript plugin is unavailable. */
    var nodePath: String? = null

    /** Run the language server bundled with the plugin instead of a custom one. */
    var useBundledLanguageServer: Boolean = true

    /** Path to a custom steiger language server (a Node.js script), when [useBundledLanguageServer] is false. */
    var customLanguageServerPath: String? = null

    override fun getState(): SteigerSettings = this

    override fun loadState(state: SteigerSettings) = XmlSerializerUtil.copyBean(state, this)

    companion object {
        @JvmStatic
        fun getInstance(project: Project): SteigerSettings = project.getService(SteigerSettings::class.java)
    }
}
