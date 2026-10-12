package com.aesirinteractive.angelscript

internal object AngelscriptNativeSignatures {
    fun parameters(signature: String, name: String? = null): List<String>? {
        val from = if (name == null) 0 else Regex("\\b${Regex.escape(name)}\\s*\\(").find(signature)?.range?.first ?: return null
        val open = signature.indexOf('(', from)
        if (open < 0) return null
        val result = mutableListOf<String>()
        var depth = 0
        var start = open + 1
        for (i in start until signature.length) {
            when (signature[i]) {
                '(', '<', '[', '{' -> ++depth
                ')' -> if (depth == 0) {
                    if (i > start) result.add(signature.substring(start, i))
                    return result.map(::type).filter { it != "void" }
                } else --depth
                '>', ']', '}' -> --depth
                ',' -> if (depth == 0) { result.add(signature.substring(start, i)); start = i + 1 }
            }
        }
        return null
    }

    private fun type(parameter: String): String {
        val value = parameter.substringBefore('=').trim()
        val tail = Regex("([\\p{L}_][\\p{L}\\p{N}_]*)$").find(value)
        val prefix = tail?.let { value.substring(0, it.range.first).trimEnd() }.orEmpty()
        val spelling = if (tail != null && prefix.isNotEmpty() &&
            tail.value !in setOf("int", "char", "long", "short", "double", "float", "bool") &&
            prefix !in setOf("const", "volatile", "unsigned", "signed", "struct", "class", "typename")) prefix else value
        return normalize(spelling)
    }

    fun normalize(type: String): String = type.replace(Regex("&(inout|in|out)\\b"), "&")
        .replace(Regex("\\b(int32|uint32|float32|float64)\\b")) {
            mapOf("int32" to "int", "uint32" to "uint", "float32" to "float", "float64" to "double").getValue(it.value)
        }.replace(Regex("\\s+"), "")
}
