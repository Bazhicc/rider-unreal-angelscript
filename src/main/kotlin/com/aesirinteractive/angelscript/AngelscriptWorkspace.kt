package com.aesirinteractive.angelscript

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

/** Unreal module names are relative to Script/, not the .uproject directory. */
internal object AngelscriptWorkspace {
    fun scriptRoots(base: Path): List<Path> {
        val root = base.toAbsolutePath().normalize()
        if (root.fileName?.toString() == "Script") return listOf(root)
        val roots = mutableListOf<Path>()
        root.resolve("Script").takeIf(Files::isDirectory)?.let(roots::add)
        val plugins = root.resolve("Plugins")
        if (Files.isDirectory(plugins)) {
            val ignored = setOf("Binaries", "Intermediate", "Content", "Saved", ".git", "node_modules")
            Files.walkFileTree(plugins, emptySet(), 8, object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    val name = dir.fileName.toString()
                    if (name in ignored) return FileVisitResult.SKIP_SUBTREE
                    if (name == "Script") {
                        roots.add(dir)
                        return FileVisitResult.SKIP_SUBTREE
                    }
                    return FileVisitResult.CONTINUE
                }

                override fun visitFileFailed(file: Path, error: java.io.IOException) = FileVisitResult.SKIP_SUBTREE
            })
        }
        return roots.distinct().sortedBy(Path::toString).ifEmpty { listOf(root) }
    }

    fun nodeExecutable(configured: String): String {
        val value = configured.trim().removeSurrounding("\"")
        if (value.isNotEmpty() && value != "node" && value != "node.exe") return value
        val home = System.getProperty("user.home")
        val windows = System.getProperty("os.name").startsWith("Windows")
        val executable = if (windows) "node.exe" else "node"
        val fromPath = (System.getenv("PATH") ?: System.getenv("Path") ?: "")
            .split(java.io.File.pathSeparator).filter(String::isNotBlank)
            .map { Path.of(it.removeSurrounding("\""), executable) }
            .firstOrNull(Files::isRegularFile)
        if (fromPath != null) return fromPath.toString()
        val candidates = listOfNotNull(
            System.getenv("LOCALAPPDATA")?.let { Path.of(it, "Programs", "nodejs", "node.exe") },
            System.getenv("ProgramFiles")?.let { Path.of(it, "nodejs", "node.exe") },
            home?.let { Path.of(it, ".volta", "bin", "node.exe") },
            home?.let { Path.of(it, ".volta", "bin", "node") },
            Path.of("/opt/homebrew/bin/node"),
            Path.of("/usr/local/bin/node"),
            Path.of("/usr/bin/node"),
        )
        return candidates.firstOrNull(Files::isRegularFile)?.toString() ?: "node"
    }
}
