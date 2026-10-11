package com.aesirinteractive.angelscript

/** Source views for UE's define-injected headers, with mappings back to original UTF-16 offsets. */
internal class AngelscriptCppIncludes<F>(
    private val text: (F) -> String,
    private val include: (F, String) -> List<F>,
    private val checkCanceled: () -> Unit,
) {
    data class View<F>(val file: F, val processed: AngelscriptCppPreprocessor.Result, val injected: Boolean, val directIncludes: List<F>) {
        val source get() = processed.source.text
        fun originalOffset(offset: Int) = processed.source.originalOffset(offset)
        fun expand(name: String, offset: Int): String {
            val definitions = processed.contexts.last { it.offset <= offset }.definitions
            fun evaluate(value: String, visited: Set<String>): String? {
                val expression = value.trim()
                if (visited.size > 32) return null
                // UE_JOIN is Unreal's token-pasting primitive, not a list of supported types.
                if (expression.startsWith("UE_JOIN(") && expression.endsWith(')'))
                    return expression.substringAfter('(').dropLast(1).split(',').map {
                        evaluate(it, visited) ?: return null
                    }.joinToString("")
                if ("##" in expression) return expression.split("##").map {
                    evaluate(it, visited) ?: return null
                }.joinToString("")
                if (!expression.matches(Regex("[\\p{L}_][\\p{L}\\p{N}_]*"))) return null
                val replacement = definitions[expression]?.takeIf { it.parameters == null }?.body ?: return expression
                if (expression in visited) return null
                return evaluate(replacement, visited + expression)
            }
            return evaluate(name, emptySet()) ?: name
        }
    }

    fun views(file: F): List<View<F>> = visit(file, emptyMap(), emptySet(), false)

    private fun visit(file: F, inherited: Map<String, AngelscriptCppPreprocessor.Macro>, stack: Set<F>, injected: Boolean): List<View<F>> {
        checkCanceled()
        if (file in stack || stack.size >= 32) return emptyList()
        val fragments = mutableListOf<View<F>>()
        val directIncludes = mutableListOf<F>()
        val processed = AngelscriptCppPreprocessor.process(text(file), inherited, { path, definitions ->
            var imported = definitions
            val children = include(file, path)
            directIncludes.addAll(children)
            for (child in children) {
                checkCanceled()
                if (path.endsWith(".inl") || path.endsWith(".inc")) {
                    val views = visit(child, imported, stack + file, true)
                    fragments.addAll(views)
                    imported = views.firstOrNull()?.processed?.definitions ?: imported
                } else if (child !in stack && child != file) {
                    // Import direct header macros; never expand the transitive engine include graph.
                    imported = AngelscriptCppPreprocessor.process(text(child), imported, checkCanceled = checkCanceled, collectOnly = true).definitions
                }
            }
            imported
        }, checkCanceled)
        return listOf(View(file, processed, injected, directIncludes.distinct())) + fragments
    }
}
