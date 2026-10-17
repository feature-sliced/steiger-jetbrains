package org.featuresliced.steiger.lsp

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.extensions.PluginId
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Extracts the bundled steiger language server (single-file Node.js bundle) from the plugin jar
 * into a system cache directory, since Node.js cannot execute files stored inside a jar.
 *
 * The cache directory is keyed by plugin version, so a plugin update always refreshes
 * the extracted server.
 */
object SteigerServerBundle {

    private const val PLUGIN_ID = "org.featuresliced.steiger"
    private const val RESOURCE_PATH = "/steiger/language-server.js"

    // THROWAWAY (dev only): re-extract the server on every IDE session start, even if a cached
    // copy for this plugin version already exists. MUST be removed before release.
    private const val ALWAYS_REPLACE = true

    @Volatile
    private var extracted: Path? = null

    fun languageServerFile(): Path? {
        extracted?.let { return it }
        synchronized(this) {
            extracted?.let { return it }
            val version = PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID))?.version ?: return null
            val dir = PathManager.getSystemDir().resolve("steiger-lsp").resolve(version)
            val target = dir.resolve("language-server.js")
            if (ALWAYS_REPLACE || !Files.isRegularFile(target)) {
                Files.createDirectories(dir)
                val resource = SteigerServerBundle::class.java.getResource(RESOURCE_PATH) ?: return null
                val tmp = dir.resolve("language-server.js.tmp")
                resource.openStream().use { input -> Files.copy(input, tmp, StandardCopyOption.REPLACE_EXISTING) }
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING)
            }
            extracted = target
            return target
        }
    }
}
