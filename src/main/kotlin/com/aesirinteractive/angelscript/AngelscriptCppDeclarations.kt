package com.aesirinteractive.angelscript

/** A scoped header fallback, not a C++ semantic parser. Offsets remain UTF-16 source offsets. */
internal object AngelscriptCppDeclarations {
    // Possessive repetition keeps long C++ literals off the regex engine's recursive backtracking stack.
    private val trivia = Regex("""//[^\r\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*+"|'(?:\\.|[^'\\])*+'""")

    private fun code(text: String) = trivia.replace(text) { match ->
        match.value.map { if (it == '\n' || it == '\r') it else ' ' }.joinToString("")
    }

    fun baseName(name: String) = name.substringBefore('<').trim().removePrefix("const ").trimEnd('&', '*', ' ')

    data class TypeDeclaration(val name: String, val offset: Int, val body: IntRange, val bases: List<String>)

    /** Accept a definition header, never an elaborated type use, friend or template parameter. */
    fun types(text: String, owners: Set<String>, expand: (String, Int) -> String = { name, _ -> name },
              namespace: String? = null): List<TypeDeclaration> {
        val source = code(text)
        val result = mutableListOf<TypeDeclaration>()
        val head = Regex("""\b(?:enum\s+(?:class\s+|struct\s+)?|class\s+|struct\s+)(?:(?:alignas\s*\([^;{}]*?\)|\[\[[^;{}]*?\]\]|\w+_API)\s+)*([\p{L}_][\p{L}\p{N}_]*)\b""")
        fun skipSpace(at: Int): Int { var i = at; while (i < source.length && source[i].isWhitespace()) ++i; return i }
        for (match in head.findAll(source)) {
            val name = match.groups[1]!!
            val expandedName = expand(name.value, name.range.first)
            val matching = owners.filter { it.substringAfterLast("::") == expandedName }
            if (matching.isEmpty()) continue
            val currentNamespace = if (namespace != null || matching.any { "::" in it }) namespaceAt(source, name.range.first) else null
            if (namespace != null && currentNamespace != namespace) continue
            if (matching.none { "::" !in it || it.substringBeforeLast("::").removePrefix("::") == currentNamespace }) continue
            var i = skipSpace(match.range.last + 1)
            if (source.getOrNull(i) == '<') {
                var angle = 1
                ++i
                while (i < source.length && angle > 0) { if (source[i] == '<') ++angle; if (source[i] == '>') --angle; ++i }
                i = skipSpace(i)
            }
            if (source.startsWith("final", i) && source.getOrNull(i + 5)?.isJavaIdentifierPart() != true) i = skipSpace(i + 5)
            val bases = mutableListOf<String>()
            if (source.getOrNull(i) == ':') {
                val start = ++i
                var angle = 0
                while (i < source.length && source[i] !in ";{}") {
                    if (source[i] == '<') ++angle
                    if (source[i] == '>') { if (angle == 0) break; --angle }
                    ++i
                }
                if (source.getOrNull(i) != '{') continue
                val inheritance = source.substring(start, i)
                var depth = 0
                val plain = inheritance.map { c ->
                    when (c) { '<' -> { ++depth; ' ' }; '>' -> { --depth; ' ' }; else -> if (depth == 0) c else ' ' }
                }.joinToString("")
                for (base in plain.split(',')) {
                    val value = base.replace(Regex("""\b(?:public|protected|private|virtual)\b"""), "").trim()
                    if (value.matches(Regex("""[\w:]+"""))) bases.add(
                        value.substringBeforeLast("::", "").let { prefix ->
                            (if ("::" in value) "$prefix::" else "") + expand(value.substringAfterLast("::"), start)
                        })
                }
            }
            if (source.getOrNull(i) != '{') continue
            val bodyStart = i
            var braces = 1
            while (++i < source.length && braces > 0) { if (source[i] == '{') ++braces; if (source[i] == '}') --braces }
            if (braces == 0) result.add(TypeDeclaration(expandedName, name.range.first, bodyStart until i, bases))
        }
        return result
    }

    /** Lexical namespace of a binding/declaration; class and function scopes do not change it. */
    fun namespaceAt(text: String, offset: Int): String {
        val tokens = Regex("""[\p{L}_][\p{L}\p{N}_]*|::|[^\s]""").findAll(code(text).take(offset)).map { it.value }.toList()
        val scopes = mutableListOf<String?>()
        for (i in tokens.indices) when (tokens[i]) {
            "{" -> {
                var from = i - 1
                while (from >= 0 && (tokens[from] == "::" || tokens[from].matches(Regex("\\w+")))) --from
                val head = tokens.subList(from + 1, i)
                val at = head.lastIndexOf("namespace")
                scopes.add(if (at < 0) null else head.drop(at + 1).joinToString(""))
            }
            "}" -> if (scopes.isNotEmpty()) scopes.removeAt(scopes.lastIndex)
        }
        return scopes.filterNotNull().filter { it.isNotEmpty() }.joinToString("::")
    }

    /** The chooser needs the actual declaring owner and full parameter list, not the query name. */
    fun signature(text: String, offset: Int, owner: String, kind: String): String {
        val source = code(text)
        val name = Regex("""[\p{L}_][\p{L}\p{N}_]*""").find(source, offset)?.takeIf { it.range.first == offset }?.value ?: owner
        if (kind in setOf("type", "enum", "namespace")) return name
        val boundary = (offset - 1 downTo 0).firstOrNull { source[it] in ";{}" }?.plus(1) ?: 0
        var start = boundary
        Regex("""\b(?:public|protected|private)\s*:""").findAll(source.substring(boundary, offset)).lastOrNull()?.let {
            start = boundary + it.range.last + 1
        }
        var end = offset + name.length
        if (kind == "function") {
            while (end < source.length && source[end].isWhitespace()) ++end
            if (source.getOrNull(end) == '(') {
                var parens = 1
                while (++end < source.length && parens > 0) { if (source[end] == '(') ++parens; if (source[end] == ')') --parens }
                while (end < source.length && source[end] !in "{;") ++end
            }
        }
        val prefix = source.substring(start, offset).replace(Regex("(?m)^\\s*#.*$"), "").trim()
        return (prefix + " " + owner.takeIf { it.isNotEmpty() }?.let { "$it::" }.orEmpty() + text.substring(offset, end))
            .replace(Regex("\\s+"), " ").trim()
    }

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

    fun aliases(text: String, name: String, namespace: String? = null): List<String> {
        val source = code(text)
        val result = linkedSetOf<String>()
        // Bound each alias to its declaration before parsing its type; never backtrack over a whole header.
        for (match in Regex("""\b(?:using|typedef)\b""").findAll(source)) {
            val start = match.range.last + 1
            var end = start
            while (end < source.length && source[end] !in ";{}") ++end
            if (source.getOrNull(end) != ';') continue
            val declaration = source.substring(start, end).trim()
            val target = if (match.value == "using") {
                if (declaration.substringBefore('=').trim() != name) continue
                declaration.substringAfter('=', "")
            } else {
                var aliasStart = declaration.length
                while (aliasStart > 0 && declaration[aliasStart - 1].isJavaIdentifierPart()) --aliasStart
                if (declaration.substring(aliasStart) != name) continue
                declaration.substring(0, aliasStart)
            }
            if (namespace != null && namespaceAt(source, match.range.first) != namespace) continue
            Regex("""(?:::)?\w+(?:::\w+)*""").findAll(target.substringBefore('<')).lastOrNull()?.value?.let(result::add)
        }
        return result.toList()
    }

    fun offsets(text: String, owners: Set<String>, symbol: AngelscriptCppSymbol,
                expand: (String, Int) -> String = { name, _ -> name }, namespace: String? = null): List<Int> =
        offsets(text, owners, setOf(symbol.cppName), symbol.kind, expand, namespace).ifEmpty {
            if (symbol.cppName == symbol.symbolName) emptyList()
            else offsets(text, owners, setOf(symbol.symbolName), symbol.kind, expand, namespace)
        }

    fun offsets(text: String, owners: Set<String>, names: Set<String>, kind: String,
                expand: (String, Int) -> String = { name, _ -> name }, namespace: String? = null): List<Int> {
        val declarations = types(text, owners, expand, namespace)
        if (kind in setOf("type", "enum")) return declarations.map { it.offset }
        if (declarations.isEmpty()) return emptyList()
        val source = code(text)
        val tokens = Regex("""[\p{L}_][\p{L}\p{N}_]*|::|->|[^\s]""").findAll(source).toList()
        val result = mutableListOf<Int>()
        for (declaration in declarations) {
                val start = tokens.indexOfFirst { it.range.first == declaration.body.first }
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
        return result.distinct().sorted()
    }
}
