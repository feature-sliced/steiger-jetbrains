package org.featuresliced.steiger.lsp

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.LspServerSupportProvider

/**
 * Starts the bundled steiger language server when a file steiger can lint is opened.
 * Registered via the `com.intellij.platform.lsp.serverSupportProvider` extension point.
 */
class SteigerLspServerSupportProvider : LspServerSupportProvider {

    override fun fileOpened(
        project: Project,
        file: VirtualFile,
        serverStarter: LspServerSupportProvider.LspServerStarter,
    ) {
        // fileOpened fires for every opened file; only start the server for steiger's file types.
        if (file.extension?.lowercase() in SteigerFileTypes.EXTENSIONS) {
            serverStarter.ensureServerStarted(SteigerLspServerDescriptor(project))
        }
    }
}
