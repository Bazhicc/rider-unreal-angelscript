package com.aesirinteractive.angelscript

/** Instantiate script return types from the project's current binding declarations. */
internal object AngelscriptNativeReceiverTypes {
    private val identifier = Regex("[\\p{L}_][\\p{L}\\p{N}_]*")
    private fun clean(type: String) = type.trim().removePrefix("const ").trim()
        .replace(Regex("[&@]\\s*(?:inout|in|out)?\\s*$"), "").trim()
    private fun arguments(type: String): List<String> {
        val start = type.indexOf('<')
        val end = type.lastIndexOf('>')
        if (start < 0 || end <= start) return emptyList()
        val result = mutableListOf<String>()
        var depth = 0
        var from = start + 1
        for (i in start + 1 until end) when (type[i]) {
            '<' -> ++depth
            '>' -> --depth
            ',' -> if (depth == 0) { result.add(type.substring(from, i).trim()); from = i + 1 }
        }
        result.add(type.substring(from, end).trim())
        return result
    }

    fun resolve(receiver: AngelscriptNativeReceiver,
                bindings: (String) -> List<AngelscriptNativeBindings.Binding>): Set<String> {
        fun returns(type: String, name: String, kind: String): Set<String> {
            val owner = clean(type).substringBefore('<')
            val declarations = bindings(owner)
            val parameters = declarations.filter { it.kind == "type" }.flatMap { arguments(it.signature) }
                .map { identifier.findAll(it).lastOrNull()?.value.orEmpty() }.distinct()
            val substitutions = parameters.zip(arguments(type)).toMap()
            return declarations.filter { it.kind == kind && it.name == name }.mapNotNull { binding ->
                val at = Regex("\\b${Regex.escape(name)}\\b" + if (kind == "function") "\\s*\\(" else "\\s*$")
                    .find(binding.signature)?.range?.first ?: return@mapNotNull null
                val result = clean(identifier.replace(binding.signature.substring(0, at)) {
                    substitutions[it.value] ?: it.value
                })
                result.takeIf { it.isNotEmpty() && it != "void" &&
                    identifier.findAll(it).none { word -> word.value in parameters && word.value !in substitutions } }
            }.toSet()
        }
        var types = setOf(clean(receiver.typeName))
        for (step in receiver.steps) {
            types = types.flatMap { type -> when (step.kind) {
                "method" -> returns(type, step.name, "function")
                "property" -> returns(type, step.name, "property").ifEmpty {
                    returns(type, "get_${step.name}", "function")
                }
                // These are AngelScript's foreach protocol methods, not container-specific heuristics.
                "foreach" -> returns(type, "Iterator", "function").flatMap { iterator ->
                    returns(iterator, "Iterate", "function").ifEmpty { returns(iterator, "Proceed", "function") }
                }
                else -> emptySet()
            } }.toSet()
            if (types.isEmpty()) break
        }
        return types
    }
}
