package com.aesirinteractive.angelscript

import com.intellij.openapi.application.PathManager
import org.jetbrains.plugins.textmate.api.TextMateBundleProvider
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

/** Supplies the unmodified official grammar to Rider's bundled TextMate engine. */
class AngelscriptTextMateBundleProvider : TextMateBundleProvider {
    override fun getBundles(): List<TextMateBundleProvider.PluginBundle> {
        val files = listOf("package.json", "language-configuration.json", "syntaxes/angelscript.tmLanguage.json")
            .associateWith { name ->
                javaClass.getResourceAsStream("/textmate/$name")!!.use { it.readBytes() }
            }
        // Immutable content-addressed directories avoid stale grammar caches on update.
        val digest = MessageDigest.getInstance("SHA-256")
        files.values.forEach { digest.update(it) }
        val hash = digest.digest().joinToString("") { "%02x".format(it) }.take(16)
        val root = Path.of(PathManager.getSystemPath(), "unreal-angelscript", "textmate", hash)
        for ((name, content) in files) {
            val path = root.resolve(name)
            Files.createDirectories(path.parent)
            if (!Files.exists(path)) Files.write(path, content)
        }
        return listOf(TextMateBundleProvider.PluginBundle("Unreal AngelScript", root))
    }
}
