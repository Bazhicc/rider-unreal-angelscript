package com.aesirinteractive.angelscript

/** Bounded source fallback preprocessing. Unknown build conditions retain all possible branches. */
internal object AngelscriptCppPreprocessor {
    data class Macro(val parameters: List<String>?, val body: String)
    data class Context(val offset: Int, val definitions: Map<String, Macro>)
    data class Source(val text: String, private val origins: IntArray) {
        fun originalOffset(offset: Int) = origins.getOrNull(offset) ?: origins.lastOrNull() ?: 0
    }
    data class Result(val source: Source, val contexts: List<Context>, val definitions: Map<String, Macro>)
    private data class Token(val value: String, val offset: Int, val literal: Boolean = true)
    private val lexer = Regex("""//[^\r\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*+"|'(?:\\.|[^'\\])*+'|[\p{L}_][\p{L}\p{N}_]*|##|\s+|[^\s]""")
    private val trivia = Regex("""//[^\r\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*+"|'(?:\\.|[^'\\])*+'""")
    private val directive = Regex("""(?m)^[ \t]*#[ \t]*(\w+)\b((?:[^\r\n]*\\\r?\n)*[^\r\n]*)""")
    private val definition = Regex("""(\w+)(\([^)]*\))?[ \t]*(.*)""")

    private class Output {
        val text = StringBuilder()
        private var origins = IntArray(1024)
        fun append(value: String, offset: Int, literal: Boolean = true) {
            val start = text.length
            if (start + value.length > origins.size) origins = origins.copyOf(maxOf(origins.size * 2, start + value.length))
            text.append(value)
            for (i in value.indices) origins[start + i] = offset + if (literal) i else 0
        }
        fun source() = Source(text.toString(), origins.copyOf(text.length))
    }

    fun process(text: String, inherited: Map<String, Macro> = emptyMap(),
                include: (String, Map<String, Macro>) -> Map<String, Macro>? = { _, _ -> null },
                checkCanceled: () -> Unit = {}, collectOnly: Boolean = false): Result {
        val definitions = inherited.toMutableMap()
        val undefined = mutableSetOf<String>()
        val output = Output()
        val contexts = mutableListOf(Context(0, definitions.toMap()))
        data class Branch(val parent: Boolean?, var seen: Boolean?, val initial: Map<String, Macro>,
                          val initialUndefined: Set<String>, val outcomes: MutableList<Map<String, Macro>> = mutableListOf(),
                          val undefinedOutcomes: MutableList<Set<String>> = mutableListOf(), var hasElse: Boolean = false)
        val branches = mutableListOf<Branch>()
        var active: Boolean? = true
        fun and(left: Boolean?, right: Boolean?): Boolean? = if (left == false || right == false) false else if (left == true && right == true) true else null
        fun or(left: Boolean?, right: Boolean?): Boolean? = if (left == true || right == true) true else if (left == false && right == false) false else null
        fun remember(branch: Branch) {
            if (active != false) { branch.outcomes.add(definitions.toMap()); branch.undefinedOutcomes.add(undefined.toSet()) }
            definitions.clear(); definitions.putAll(branch.initial)
            undefined.clear(); undefined.addAll(branch.initialUndefined)
        }
        fun emit(start: Int, end: Int) {
            if (start >= end || collectOnly) return
            val value = text.substring(start, end)
            if (active == false) output.append(blank(value), start)
            else {
                val tokens = lexer.findAll(value).map { Token(it.value, start + it.range.first) }.toList()
                for (token in expand(tokens, definitions, emptySet(), checkCanceled)) output.append(token.value, token.offset, token.literal)
            }
        }
        var start = 0
        var headerPreamble = true
        val ignored = trivia.findAll(text).map { it.range }.iterator()
        var ignoredRange = if (ignored.hasNext()) ignored.next() else null
        for (match in directive.findAll(text)) {
            checkCanceled()
            while (ignoredRange != null && ignoredRange.last < match.range.first)
                ignoredRange = if (ignored.hasNext()) ignored.next() else null
            if (ignoredRange?.contains(match.range.first) == true) continue
            if (headerPreamble && trivia.replace(text.substring(start, match.range.first), "").isNotBlank()) headerPreamble = false
            emit(start, match.range.first)
            val api = match.groupValues[1]
            val value = match.groupValues[2].replace(Regex("\\\\\r?\n"), " ").substringBefore("//").trim()
            when (api) {
                "if", "ifdef", "ifndef" -> {
                    val condition = if (api == "if") condition(value, definitions, undefined) else {
                        val known = if (value in definitions) true else if (value in undefined) false else null
                        // The conventional opening #ifndef/#define pair is a header guard, not a build flag.
                        val guard = api == "ifndef" && known == null && headerPreamble &&
                            Regex("""\s*#\s*define\s+${Regex.escape(value)}\b""").find(text, match.range.last + 1)?.range?.first == match.range.last + 1
                        if (guard) true else if (api == "ifndef") known?.not() else known
                    }
                    branches.add(Branch(active, condition, definitions.toMap(), undefined.toSet()))
                    active = and(active, condition)
                }
                "elif", "else" -> branches.lastOrNull()?.let { branch ->
                    remember(branch)
                    val next = if (api == "else") true else condition(value, definitions, undefined)
                    active = and(branch.parent, and(branch.seen?.not(), next))
                    branch.seen = or(branch.seen, next)
                    branch.hasElse = api == "else"
                }
                "endif" -> if (branches.isNotEmpty()) {
                    val branch = branches.removeAt(branches.lastIndex)
                    remember(branch)
                    if (!branch.hasElse && branch.seen != true) {
                        branch.outcomes.add(branch.initial); branch.undefinedOutcomes.add(branch.initialUndefined)
                    }
                    if (branch.outcomes.isNotEmpty()) {
                        definitions.clear()
                        definitions.putAll(branch.outcomes.first().filter { (name, macro) -> branch.outcomes.all { it[name] == macro } })
                        undefined.clear(); undefined.addAll(branch.undefinedOutcomes.first().filter { name -> branch.undefinedOutcomes.all { name in it } })
                    }
                    active = branch.parent
                }
                "define" -> if (active != false) definition.matchEntire(value)?.let {
                    val params = it.groups[2]?.value?.drop(1)?.dropLast(1)?.let { args ->
                        if (args.isBlank()) emptyList() else args.split(',').map(String::trim)
                    }
                    definitions[it.groupValues[1]] = Macro(params, it.groupValues[3])
                    undefined.remove(it.groupValues[1])
                }
                "undef" -> if (active != false) { definitions.remove(value); undefined.add(value) }
                "include" -> if (active != false) Regex("\"([^\"]+)\"").matchEntire(value)?.let {
                    include(it.groupValues[1], definitions.toMap())?.let { imported -> definitions.clear(); definitions.putAll(imported) }
                }
            }
            if (api != "pragma" || value != "once") headerPreamble = false
            if (!collectOnly) {
                output.append(blank(match.value), match.range.first)
                contexts.add(Context(output.text.length, definitions.toMap()))
            }
            start = match.range.last + 1
        }
        emit(start, text.length)
        return Result(output.source(), contexts, definitions.toMap())
    }

    private fun blank(value: String) = value.map { if (it in "\r\n") it else ' ' }.joinToString("")

    private fun expand(tokens: List<Token>, definitions: Map<String, Macro>, disabled: Set<String>, checkCanceled: () -> Unit, depth: Int = 0): List<Token> {
        if (depth >= 32 || tokens.size > 200_000) return tokens
        val result = mutableListOf<Token>()
        var i = 0
        while (i < tokens.size) {
            if (i % 256 == 0) checkCanceled()
            val token = tokens[i]
            val macro = definitions[token.value]
            val params = macro?.parameters
            var open = i + 1
            while (tokens.getOrNull(open)?.value?.isBlank() == true) ++open
            // These structural binding wrappers carry the native target for the binding reader.
            if (params == null || token.value in disabled || tokens.getOrNull(open)?.value != "(" ||
                token.value.startsWith("METHOD") || token.value.startsWith("FUNC")) { result.add(token); ++i; continue }
            val args = mutableListOf<List<Token>>()
            var end = open + 1
            var from = end
            var depth = 1
            while (end < tokens.size && depth > 0) {
                when (tokens[end].value) {
                    "(" -> ++depth
                    ")" -> if (--depth == 0) { if (end > from || args.isNotEmpty()) args.add(tokens.subList(from, end)); break }
                    "," -> if (depth == 1) { args.add(tokens.subList(from, end)); from = end + 1 }
                }
                ++end
            }
            val variadic = params.lastOrNull() == "..."
            if (depth != 0 || (!variadic && args.size != params.size) || (variadic && args.size < params.size - 1)) { result.add(token); ++i; continue }
            val values = params.mapIndexed { index, name ->
                (if (name == "...") "__VA_ARGS__" else name) to if (name == "...") args.drop(index).flatMapIndexed { n, part ->
                    (if (n == 0) emptyList() else listOf(Token(",", token.offset, false))) + part
                } else args[index]
            }.toMap()
            val prescanned = mutableMapOf<String, List<Token>>()
            val body = lexer.findAll(macro.body).map { Token(it.value, token.offset, false) }.toList()
            val replaced = mutableListOf<Token>()
            var at = 0
            while (at < body.size) {
                val part = body[at]
                var next = at + 1
                while (body.getOrNull(next)?.value?.isBlank() == true) ++next
                val stringified = if (part.value == "#") values[body.getOrNull(next)?.value] else null
                if (stringified != null) {
                    val value = stringified.joinToString("") { it.value }.trim().replace(Regex("\\s+"), " ")
                        .replace("\\", "\\\\").replace("\"", "\\\"")
                    replaced.add(Token("\"$value\"", stringified.firstOrNull { it.value.isNotBlank() }?.offset ?: token.offset, false))
                    at = next + 1
                } else {
                    val argument = values[part.value]
                    var previous = at - 1
                    while (previous >= 0 && body[previous].value.isBlank()) --previous
                    val pasted = body.getOrNull(previous)?.value == "##" || body.getOrNull(next)?.value == "##"
                    replaced.addAll(if (argument == null) listOf(part) else if (pasted) argument else
                        prescanned.getOrPut(part.value) { expand(argument, definitions, disabled, checkCanceled, depth + 1) })
                    ++at
                }
            }
            at = 0
            while (at < replaced.size) {
                if (replaced[at].value != "##") { ++at; continue }
                var left = at - 1
                var right = at + 1
                while (left >= 0 && replaced[left].value.isBlank()) --left
                while (right < replaced.size && replaced[right].value.isBlank()) ++right
                if (left < 0 || right >= replaced.size) { ++at; continue }
                val l = replaced[left]; val r = replaced[right]
                val pasted = Token(l.value + r.value, if (r.literal) r.offset else l.offset, false)
                replaced.subList(left, right + 1).clear(); replaced.add(left, pasted); at = left + 1
            }
            val expanded = expand(replaced, definitions, disabled + token.value, checkCanceled, depth + 1)
            if (result.size + expanded.size > 200_000) { result.addAll(tokens.subList(i, tokens.size)); break }
            result.addAll(expanded)
            i = end + 1
        }
        return result
    }

    private fun condition(expression: String, definitions: Map<String, Macro>, undefined: Set<String>): Boolean? =
        evaluate(expression, definitions, undefined)?.let { it != 0L }

    private fun evaluate(expression: String, definitions: Map<String, Macro>, undefined: Set<String>, visited: Set<String> = emptySet()): Long? {
        if (visited.size >= 32 || expression.length > 4096) return null
        val tokens = Regex("""defined|0[xX][\da-fA-F]+[uUlL]*|0[bB][01]+[uUlL]*|\d+[uUlL]*|\w+|&&|\|\||==|!=|<=|>=|[^\s]""").findAll(expression).map { it.value }.toList()
        if (tokens.count { it in setOf("(", "!", "~", "+", "-") } > 64) return null
        var at = 0
        fun truth(value: Long?) = value?.let { it != 0L }
        val precedence = mapOf("||" to 1, "&&" to 2, "|" to 3, "^" to 4, "&" to 5, "==" to 6, "!=" to 6, "<" to 7, ">" to 7, "<=" to 7, ">=" to 7, "+" to 8, "-" to 8, "*" to 9, "/" to 9, "%" to 9)
        lateinit var parse: (Int) -> Long?
        fun unary(): Long? {
            val token = tokens.getOrNull(at++) ?: return null
            if (token in setOf("!", "~", "+", "-")) return unary()?.let { when (token) { "!" -> if (it == 0L) 1 else 0; "~" -> it.inv(); "-" -> -it; else -> it } }
            if (token == "(") { val value = parse(1); if (tokens.getOrNull(at++) != ")") return null; return value }
            if (token == "defined") {
                val paren = tokens.getOrNull(at) == "("
                if (paren) ++at
                val name = tokens.getOrNull(at++) ?: return null
                if (paren && tokens.getOrNull(at++) != ")") return null
                return if (name in definitions) 1 else if (name in undefined) 0 else null
            }
            token.trimEnd('u', 'U', 'l', 'L').let { number ->
                (when {
                    number.startsWith("0x", true) -> number.drop(2).toLongOrNull(16)
                    number.startsWith("0b", true) -> number.drop(2).toLongOrNull(2)
                    number.startsWith('0') && number.length > 1 -> number.toLongOrNull(8)
                    else -> number.toLongOrNull()
                })?.let { return it }
            }
            if (token in undefined) return 0
            val macro = definitions[token]?.takeIf { it.parameters == null && token !in visited } ?: return null
            return evaluate(macro.body, definitions, undefined, visited + token)
        }
        parse = { minimum ->
            var left = unary()
            while ((precedence[tokens.getOrNull(at)] ?: 0) >= minimum) {
                val operator = tokens[at++]
                val right = parse(precedence.getValue(operator) + 1)
                val value = left
                left = when {
                    operator == "&&" -> if (truth(left) == false || truth(right) == false) 0 else if (left != null && right != null) 1 else null
                    operator == "||" -> if (truth(left) == true || truth(right) == true) 1 else if (left != null && right != null) 0 else null
                    value == null || right == null -> null
                    else -> when (operator) {
                        "==" -> if (value == right) 1 else 0; "!=" -> if (value != right) 1 else 0
                        "<" -> if (value < right) 1 else 0; ">" -> if (value > right) 1 else 0; "<=" -> if (value <= right) 1 else 0; ">=" -> if (value >= right) 1 else 0
                        "+" -> value + right; "-" -> value - right; "*" -> value * right
                        "/" -> if (right != 0L) value / right else null; "%" -> if (right != 0L) value % right else null
                        "|" -> value or right; "&" -> value and right; "^" -> value xor right; else -> null
                    }
                }
            }
            left
        }
        val value = parse(1)
        return if (at == tokens.size) value else null
    }
}
