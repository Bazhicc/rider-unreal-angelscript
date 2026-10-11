package com.aesirinteractive.angelscript

/** Source views for UE's define-injected headers. Never rewrites text or its UTF-16 offsets. */
internal class AngelscriptCppIncludes<F>(
    private val text: (F) -> String,
    private val include: (F, String) -> List<F>,
    private val checkCanceled: () -> Unit,
) {
    data class Context(val offset: Int, val definitions: Map<String, String>)
    data class View<F>(val file: F, val contexts: List<Context>, val injected: Boolean) {
        fun expand(name: String, offset: Int): String {
            val definitions = contexts.last { it.offset <= offset }.definitions
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
                val replacement = definitions[expression] ?: return expression
                if (expression in visited) return null
                return evaluate(replacement, visited + expression)
            }
            return evaluate(name, emptySet()) ?: name
        }
    }

    private val directive = Regex("(?m)^[ \\t]*#[ \\t]*(define|undef|include)[ \\t]+([^\\r\\n]+)")
    private val objectDefinition = Regex("([\\w]+)(?:[ \\t]+(.*))?")
    private val quotedInclude = Regex("\"([^\"]+)\"")

    fun views(file: F): List<View<F>> = visit(file, emptyMap(), emptySet(), false)

    private fun visit(file: F, inherited: Map<String, String>, stack: Set<F>, injected: Boolean): List<View<F>> {
        checkCanceled()
        if (file in stack) return emptyList()
        val definitions = inherited.toMutableMap()
        val contexts = mutableListOf(Context(0, definitions.toMap()))
        val fragments = mutableListOf<View<F>>()
        for (match in directive.findAll(text(file))) {
            checkCanceled()
            val value = match.groupValues[2].substringBefore("//").trim()
            when (match.groupValues[1]) {
                "define" -> objectDefinition.matchEntire(value)?.let {
                    definitions[it.groupValues[1]] = it.groupValues[2].trim()
                    contexts.add(Context(match.range.last + 1, definitions.toMap()))
                }
                "undef" -> {
                    definitions.remove(value)
                    contexts.add(Context(match.range.last + 1, definitions.toMap()))
                }
                "include" -> quotedInclude.matchEntire(value)?.groupValues?.get(1)?.let { path ->
                    // Follow implementation fragments, not the entire transitive engine include graph.
                    if (path.endsWith(".inl") || path.endsWith(".inc")) for (child in include(file, path))
                        fragments.addAll(visit(child, definitions, stack + file, true))
                }
            }
        }
        return listOf(View(file, contexts, injected)) + fragments
    }
}
