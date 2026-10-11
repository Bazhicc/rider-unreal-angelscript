package com.aesirinteractive.angelscript

import com.intellij.openapi.application.invokeAndWaitIfNeeded
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import com.intellij.openapi.startup.ProjectActivity

/**
 * Startup activity that detects Unreal Engine projects (by presence of a .uproject file in the
 * project root) and automatically registers the "Script" subfolder as a source root on the module.
 * Does nothing if Script/ does not exist or is already registered.
 */
class AngelscriptProjectSetupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        @Suppress("DEPRECATION")
        val baseDir = project.baseDir ?: return

        // Only proceed if this looks like an Unreal project
        if (baseDir.children.none { it.extension == "uproject" }) return

        // Script/ must already exist on disk
        val scriptDir = baseDir.findChild("Script") ?: return

        val module = ModuleManager.getInstance(project).modules.firstOrNull() ?: return

        invokeAndWaitIfNeeded {
            runWriteAction {
                if (project.isDisposed) return@runWriteAction
                ProjectRootManagerEx.getInstanceEx(project).mergeRootsChangesDuring {
                    ModuleRootModificationUtil.updateModel(module) { model ->
                        // Script/ is a subdirectory — add it as a source folder inside the
                        // existing content entry that covers the project root, not as a new entry.
                        val entry = model.contentEntries.find { contentEntry ->
                            contentEntry.file?.let { scriptDir.path.startsWith(it.path) } == true
                        } ?: return@updateModel
                        val alreadyAdded = entry.sourceFolders.any { it.url == scriptDir.url }
                        if (!alreadyAdded) {
                            entry.addSourceFolder(scriptDir.url, false)
                        }
                    }
                }
            }
        }
    }
}
