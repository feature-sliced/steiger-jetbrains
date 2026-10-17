package org.featuresliced.steiger.settings

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project

/**
 * Resolves the Node.js executable used to run the steiger language server.
 *
 * Uses the JavaScript plugin's interpreter infrastructure when that plugin is
 * available (WebStorm, or IDEs with an active subscription). Otherwise falls
 * back to a plain path from settings, and finally to `node` from PATH.
 *
 * Must stay free of JavaScript plugin class references: it is always loaded.
 * The JS-dependent logic lives in [SteigerJsInterpreter], which is only touched
 * after [isJavaScriptAvailable] returns true.
 */
object SteigerInterpreter {

    private const val JAVASCRIPT_PLUGIN_ID = "JavaScript"

    fun isJavaScriptAvailable(): Boolean =
        PluginManagerCore.getPlugin(PluginId.getId(JAVASCRIPT_PLUGIN_ID))?.isEnabled == true

    fun resolveNodePath(project: Project): String {
        val settings = SteigerSettings.getInstance(project)
        if (isJavaScriptAvailable()) {
            return try {
                SteigerJsInterpreter.resolve(project, settings)
            } catch (_: LinkageError) {
                // Plugin reported enabled but its classes are unavailable (e.g. headless tooling)
                settings.nodePath?.takeIf { it.isNotBlank() } ?: "node"
            }
        }
        return settings.nodePath?.takeIf { it.isNotBlank() } ?: "node"
    }
}
