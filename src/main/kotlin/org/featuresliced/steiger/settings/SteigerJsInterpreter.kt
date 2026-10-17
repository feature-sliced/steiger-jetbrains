package org.featuresliced.steiger.settings

import com.intellij.execution.ExecutionException
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterManager
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef
import com.intellij.openapi.project.Project

/**
 * JavaScript-plugin-based interpreter resolution. Only loaded when the
 * JavaScript plugin is enabled; never reference this class unguarded.
 */
object SteigerJsInterpreter {

    fun resolve(project: Project, settings: SteigerSettings): String = when {
        settings.useProjectDefault || settings.interpreterRefName.isNullOrBlank() -> defaultInterpreterPath(project)
        else -> NodeJsInterpreterRef.create(settings.interpreterRefName!!)
            .resolveAsLocal(project) // throws ExecutionException for invalid or remote interpreters
            .interpreterSystemDependentPath
    }

    private fun defaultInterpreterPath(project: Project): String {
        val manager = NodeJsInterpreterManager.getInstance(project)
        if (!manager.isInterpreterAvailable) return "node"
        return try {
            manager.interpreterRef?.resolveAsLocal(project)?.interpreterSystemDependentPath ?: "node"
        } catch (_: ExecutionException) {
            "node" // configured interpreter is remote or broken; fall back to PATH
        }
    }
}
