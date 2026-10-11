package com.aesirinteractive.angelscript

/** A scoped header fallback, not a C++ semantic parser. Offsets remain UTF-16 source offsets. */
internal object AngelscriptCppDeclarations {
    private val trivia = Regex("""//[^\r\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'""")

    private fun code(text: String) = trivia.replace(text) { match ->
        match.value.map { if (it == '\n' || it == '\r') it else ' ' }.joinToString("")
    }

    fun baseName(name: String) = name.substringBefore('<').trim().removePrefix("const ").trimEnd('&', '*', ' ')

    /** Namespace/global declarations, excluding classes, function bodies and call expressions. */
    fun namespaceOffsets(text: String, namespace: String, names: Set<String>, kind: String): List<Int> {
        val source = code(text)
        val tokens = Regex("""[\p{L}_][\p{L}\p{N}_]*|::|->|[^\s]""").findAll(source).toList()
        val scopes = mutableListOf<String?>()
        val result = mutableListOf<Int>()
        var parentheses = 0
        for (i in tokens.indices) {
            val token = tokens[i].value
            if (token == "{") {
                val start = (i - 1 downTo 0).takeWhile { tokens[it].value !in setOf(";", "{", "}") }.lastOrNull() ?: i
                val declaration = tokens.subList(start, i).joinToString("") { it.value }
                scopes.add(Regex("""^(?:inline)?namespace([\w:]+)?$""").matchEntire(declaration)?.groupValues?.get(1))
                continue
            }
            if (token == "}") { if (scopes.isNotEmpty()) scopes.removeAt(scopes.lastIndex); continue }
            if (token == "(") ++parentheses
            if (token == ")") --parentheses
            if (parentheses != 0 || scopes.any { it == null } || token !in names) continue
            val current = scopes.filterNotNull().filter { it.isNotEmpty() }.joinToString("::")
            val previous = tokens.getOrNull(i - 1)?.value
            val next = tokens.getOrNull(i + 1)?.value
            if (kind == "namespace" && previous == "namespace" && (current + "::" + token).trim(':') == namespace) {
                result.add(tokens[i].range.first); continue
            }
            if (current != namespace || previous in setOf(".", "->", "::", "=", "return")) continue
            if (kind == "function" && next == "(" || kind == "property" && next in setOf(";", "=", "[", ",", ":"))
                result.add(tokens[i].range.first)
        }
        return result
    }

    fun aliases(text: String, name: String): List<String> {
        val source = code(text)
        val escaped = Regex.escape(name)
        val using = Regex("""\busing\s+$escaped\s*=\s*(?:\w+\s*::\s*)*(\w+)""")
        val typedef = Regex("""\btypedef\s+(?:\w+\s*::\s*)*(\w+)(?:\s*<[^;]+>)?\s+$escaped\s*;""")
        return (using.findAll(source) + typedef.findAll(source)).map { it.groupValues[1] }.distinct().toList()
    }

    fun offsets(text: String, owners: Set<String>, symbol: AngelscriptCppSymbol): List<Int> =
        offsets(text, owners, setOf(symbol.cppName), symbol.kind).ifEmpty {
            if (symbol.cppName == symbol.symbolName) emptyList()
            else offsets(text, owners, setOf(symbol.symbolName), symbol.kind)
        }

    fun offsets(text: String, owners: Set<String>, names: Set<String>, kind: String): List<Int> {
        val source = code(text)
        val tokens = Regex("""[\p{L}_][\p{L}\p{N}_]*|::|->|[^\s]""").findAll(source).toList()
        val result = mutableListOf<Int>()
        for (owner in owners) {
            val declaration = Regex("""\b(?:class|struct|enum(?:\s+class)?)\s+(?:\w+_API\s+)?${Regex.escape(owner)}\b[^;{]*\{""")
            for (match in declaration.findAll(source)) {
                val nameOffset = Regex("""\b${Regex.escape(owner)}\b""").find(source, match.range.first)!!.range.first
                if (kind == "type" || kind == "enum") { result.add(nameOffset); continue }
                val start = tokens.indexOfFirst { it.range.first == match.range.last }
                if (start < 0) continue
                val scopes = mutableListOf(true)
                var parentheses = 0
                for (i in start + 1 until tokens.size) {
                    val token = tokens[i].value
                    if (token == "}") {
                        scopes.removeAt(scopes.lastIndex)
                        if (scopes.isEmpty()) break
                        continue
                    }
                    if (token == "{") {
                        // Anonymous unions/structs expose fields in the containing class.
                        scopes.add(scopes.all { it } && tokens[i - 1].value in setOf("union", "struct"))
                        continue
                    }
                    if (token == "(") ++parentheses
                    if (token == ")") --parentheses
                    if (parentheses != 0 || !scopes.all { it } || token !in names) continue
                    val next = tokens.getOrNull(i + 1)?.value
                    val previous = tokens.getOrNull(i - 1)?.value
                    if (previous in setOf(".", "->", "::", "=", "return")) continue
                    if (kind == "function" && next == "(" ||
                        kind == "property" && next in setOf(";", "=", "[", ",", ":", "{") ||
                        kind == "enumValue" && scopes.size == 1 && previous in setOf("{", ",") && next in setOf("=", ",", "}"))
                        result.add(tokens[i].range.first)
                }
            }
        }
        return result.distinct().sorted()
    }
}
