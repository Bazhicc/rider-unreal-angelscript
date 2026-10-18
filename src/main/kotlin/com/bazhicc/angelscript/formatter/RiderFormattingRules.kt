package com.bazhicc.angelscript.formatter

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import org.ec4j.core.Resource
import org.ec4j.core.ResourcePropertiesService
import java.nio.file.Path

data class RiderFormattingRules(val braces: BracePolicy, val preserveLineBreaks: Boolean) {
    companion object {
        fun load(project: Project, filename: String): RiderFormattingRules {
            val values = RiderBraceSettings.values(project)
            val editorConfig = if (!values["EnableEditorConfigSupport"].equals("False", true) &&
                PluginManagerCore.getPlugin(PluginId.getId("org.editorconfig.editorconfigjetbrains"))?.isEnabled == true)
                EditorConfigRules.values(Path.of(filename)) else emptyMap()
            val keep = listOf("resharper_cpp_keep_user_linebreaks", "cpp_keep_user_linebreaks",
                "resharper_keep_user_linebreaks", "keep_user_linebreaks")
                .firstNotNullOfOrNull { editorConfig[it]?.toBooleanStrictOrNull() }
                ?: values["KEEP_USER_LINEBREAKS"].equals("True", true)
            return RiderFormattingRules(RiderBraceSettings.policy(values), keep)
        }
    }
}

/** Loaded only when Rider's optional EditorConfig plugin is enabled; uses its standard glob parser. */
internal object EditorConfigRules {
    fun values(filename: Path): Map<String, String> = ResourcePropertiesService.default_()
        .queryProperties(Resource.Resources.ofPath(filename.toAbsolutePath(), Charsets.UTF_8))
        .properties.filterValues { !it.isUnset }.mapValues { it.value.sourceValue }
}
