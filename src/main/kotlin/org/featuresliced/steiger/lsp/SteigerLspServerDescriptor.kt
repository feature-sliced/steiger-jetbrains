package org.featuresliced.steiger.lsp

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import org.featuresliced.steiger.settings.SteigerInterpreter
import org.featuresliced.steiger.settings.SteigerSettings
import java.nio.file.Files
import java.nio.file.Path

/**
 * File extensions steiger lints: the JS/TS language family, matching the steiger VS Code extension.
 * Checked by extension instead of referencing e.g. JavaScriptFileType, so this plugin has no
 * compile-time dependency on the JavaScript plugin.
 */
object SteigerFileTypes {
    val EXTENSIONS = setOf("js", "jsx", "ts", "tsx", "mjs", "cjs", "vue", "svelte", "astro")
}

/**
 * steiger analyzes the whole project file structure, so the descriptor is project-wide:
 * the server is started once per project, not per file.
 */
class SteigerLspServerDescriptor(project: Project) :
    ProjectWideLspServerDescriptor(project, "steiger") {

    override fun createCommandLine(): GeneralCommandLine {
        val script = resolveServerScript()
        val projectDir = project.basePath
            ?: throw ExecutionException("Project has no base directory")
        val node = SteigerInterpreter.resolveNodePath(project)
        return GeneralCommandLine(node, script.toString())
            .withWorkDirectory(projectDir)
            .withEnvironment("NODE_OPTIONS", "--enable-source-maps")
    }

    override fun isSupportedFile(file: VirtualFile): Boolean =
        file.extension?.lowercase() in SteigerFileTypes.EXTENSIONS

    /**
     * A custom server is a Node.js script executed with the interpreter resolved from settings,
     * exactly like the bundled one.
     */
    private fun resolveServerScript(): Path {
        val settings = SteigerSettings.getInstance(project)
        val customPath = settings.customLanguageServerPath?.trim()
        if (!settings.useBundledLanguageServer && !customPath.isNullOrBlank()) {
            val path = Path.of(customPath)
            if (!Files.isRegularFile(path)) {
                throw ExecutionException("Custom steiger language server not found: $customPath")
            }
            return path
        }
        return SteigerServerBundle.languageServerFile()
            ?: throw ExecutionException("Bundled steiger language server is missing from the plugin")
    }
}
