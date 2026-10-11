package com.aesirinteractive.angelscript

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Files

class AngelscriptWorkspaceTest {
    @get:Rule val temporary = TemporaryFolder()

    @Test fun findsProjectAndNestedPluginScriptRoots() {
        val project = temporary.newFolder("Game with spaces").toPath()
        val main = Files.createDirectories(project.resolve("Script"))
        val plugin = Files.createDirectories(project.resolve("Plugins/Gameplay/Inventory/Script"))
        Files.createDirectories(project.resolve("Plugins/Inventory/Intermediate/Script"))
        assertEquals(setOf(main, plugin), AngelscriptWorkspace.scriptRoots(project).toSet())
    }

    @Test fun supportsOpeningScriptFolderDirectly() {
        val script = temporary.newFolder("Script").toPath()
        assertEquals(listOf(script), AngelscriptWorkspace.scriptRoots(script))
    }

    @Test fun supportsStandaloneScriptWorkspace() {
        val folder = temporary.newFolder("Scripts").toPath()
        assertEquals(listOf(folder), AngelscriptWorkspace.scriptRoots(folder))
    }

    @Test fun preservesExplicitNodePathIncludingSpaces() {
        assertEquals("C:/Tools/Node JS/node.exe", AngelscriptWorkspace.nodeExecutable("\"C:/Tools/Node JS/node.exe\""))
    }
}
