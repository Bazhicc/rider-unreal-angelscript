package com.aesirinteractive.angelscript

/** Read the binding declarations themselves; no list of known script functions or UE types. */
internal object AngelscriptNativeBindings {
    data class Binding(val owner: String, val name: String, val kind: String, val offset: Int,
                       val cppOwner: String? = null, val cppName: String? = null, val signature: String = name)
    private data class Token(val value: String, val offset: Int)
    private data class Owner(val variable: String, val name: String, val scope: Int, val at: Int)
    // A single long literal must not abort every native lookup with StackOverflowError.
    private val lexer = Regex("""//[^\r\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*+"|'(?:\\.|[^'\\])*+'|[\p{L}_][\p{L}\p{N}_]*|::|[^\s]""")

    fun parse(text: String): List<Binding> {
        val processed = AngelscriptCppPreprocessor.process(text).source
        return parsePrepared(processed.text).map { it.copy(offset = processed.originalOffset(it.offset)) }
    }

    fun parsePrepared(text: String): List<Binding> {
        val tokens = lexer.findAll(text).filterNot { it.value.startsWith("//") || it.value.startsWith("/*") }
            .map { Token(it.value, it.range.first) }.toList()
        val pairs = mutableMapOf<Int, Int>()
        val stack = mutableListOf<Int>()
        val scopes = IntArray(tokens.size) { -1 }
        val parents = mutableMapOf<Int, Int>()
        var scope = -1
        for (i in tokens.indices) {
            scopes[i] = scope
            when (tokens[i].value) {
                "(", "[", "{" -> {
                    stack.add(i)
                    if (tokens[i].value == "{") { parents[i] = scope; scope = i }
                }
                ")", "]", "}" -> if (stack.isNotEmpty()) {
                    val open = stack.removeAt(stack.lastIndex)
                    pairs[open] = i
                    if (tokens[i].value == "}") scope = parents[open] ?: -1
                }
            }
        }
        fun visible(declared: Int, at: Int): Boolean {
            var current = scopes[at]
            while (current != declared && current != -1) current = parents[current] ?: -1
            return current == declared
        }
        fun arguments(open: Int): List<IntRange> {
            val end = pairs[open] ?: return emptyList()
            val result = mutableListOf<IntRange>()
            var i = open + 1
            var start = i
            var angle = 0
            while (i < end) {
                val value = tokens[i].value
                if (value in setOf("(", "[", "{")) { i = (pairs[i] ?: i) + 1; continue }
                if (value == "<") ++angle
                if (value == ">" && angle > 0) --angle
                if (value == "," && angle == 0) { result.add(start until i); start = i + 1 }
                ++i
            }
            if (start < end) result.add(start until end)
            return result
        }
        fun literal(range: IntRange): String? = range.filter { tokens[it].value.startsWith('"') }
            .map { tokens[it].value.drop(1).dropLast(1).replace("\\\"", "\"") }.takeIf { it.isNotEmpty() }?.joinToString("")
        fun stringValue(range: IntRange): String? {
            literal(range)?.let { return it }
            if (range.count() != 1) return null
            val variable = tokens[range.first].value
            for (i in range.first - 1 downTo 1) {
                if (tokens[i - 1].value != variable || tokens[i].value != "=" || !visible(scopes[i], range.first)) continue
                val end = (i + 1 until range.first).firstOrNull { tokens[it].value == ";" } ?: continue
                return literal(i + 1 until end)
            }
            return null
        }
        fun functionName(signature: String) = Regex("""([\p{L}_][\p{L}\p{N}_]*)\s*\(""")
            .find(signature)?.groupValues?.get(1)
        fun nativeTarget(range: IntRange): Pair<String?, String?> {
            if (tokens[range.first].value == "[") return null to null
            for (i in range) {
                val macro = tokens[i].value
                if ((macro.startsWith("METHOD") || macro.startsWith("FUNC")) && tokens.getOrNull(i + 1)?.value == "(") {
                    val args = arguments(i + 1).map { part -> part.joinToString("") { tokens[it].value } }
                    if (macro.startsWith("METHOD")) {
                        val first = if (macro.startsWith("METHODPR")) 1 else 0
                        if (args.size > first + 1) return args[first] to args[first + 1]
                    } else {
                        val target = args.getOrNull(if (macro.startsWith("FUNCPR")) 1 else 0) ?: continue
                        return target.substringBeforeLast("::", "").ifEmpty { null } to target.substringAfterLast("::")
                    }
                }
                if (macro == "&") {
                    val path = (i + 1..range.last).takeWhile { tokens[it].value.matches(Regex("\\w+|::")) }
                        .joinToString("") { tokens[it].value }
                    if (path.isNotEmpty()) return path.substringBeforeLast("::", "").ifEmpty { null } to path.substringAfterLast("::")
                }
            }
            return null to null // A lambda still has a precise binding source location.
        }
        val owners = mutableListOf<Owner>()
        val namespaces = mutableListOf<Owner>()
        val result = mutableListOf<Binding>()
        for (i in tokens.indices) {
            val api = tokens[i].value
            var open = i + 1
            var cppType: String? = null
            if (tokens.getOrNull(open)?.value == "<") {
                var depth = 1
                val start = ++open
                while (open < tokens.size && depth > 0) {
                    if (tokens[open].value == "<") ++depth
                    if (tokens[open].value == ">") --depth
                    ++open
                }
                cppType = (start until open - 1).joinToString("") { tokens[it].value }.substringBefore('<')
            }
            if (api == "FNamespace") {
                if (tokens.getOrNull(open)?.value != "(") ++open // RAII variable name
                if (tokens.getOrNull(open)?.value == "(") {
                    val arg = arguments(open).firstOrNull() ?: continue
                    stringValue(arg)?.let { namespaces.add(Owner("", it, scopes[i], i)) }
                }
                continue
            }
            if (tokens.getOrNull(open)?.value != "(") continue
            if (api in setOf("ValueClass", "ReferenceClass", "ExistingClass", "Enum")) {
                val arg = arguments(open).firstOrNull() ?: continue
                val signature = stringValue(arg) ?: continue
                val name = signature.substringBefore('<').trim()
                val assignment = (i - 1 downTo 0).takeWhile { tokens[it].value !in setOf(";", "{", "}") }
                    .firstOrNull { tokens[it].value == "=" }
                if (assignment != null && assignment > 0) owners.add(Owner(tokens[assignment - 1].value, name, scopes[i], i))
                val offset = arg.firstOrNull { tokens[it].value.startsWith('"') }?.let { tokens[it].offset + 1 } ?: tokens[i].offset
                result.add(Binding(name, name, "type", offset, cppType, cppType?.substringAfterLast("::"), signature))
                continue
            }
        }
        // Binding libraries often share a helper taking FAngelscriptBinds& (e.g. both soft pointer types).
        // Carry the registered owners into the helper parameter's lexical scope, including helper-to-helper calls.
        data class Helper(val name: String, val open: Int, val body: Int, val parameters: Map<Int, String>)
        val helpers = tokens.indices.mapNotNull { open ->
            if (tokens[open].value != "(" || tokens.getOrNull((pairs[open] ?: return@mapNotNull null) + 1)?.value != "{") return@mapNotNull null
            val params = arguments(open).mapIndexedNotNull { index, part ->
                if (part.none { tokens[it].value == "FAngelscriptBinds" }) null
                else part.lastOrNull { tokens[it].value.matches(Regex("\\w+")) }?.let { index to tokens[it].value }
            }.toMap()
            if (params.isEmpty()) null else Helper(tokens.getOrNull(open - 1)?.value ?: "", open, pairs.getValue(open) + 1, params)
        }
        repeat(helpers.size + 1) {
            for (helper in helpers) for (i in tokens.indices) {
                if (tokens[i].value != helper.name || tokens.getOrNull(i + 1)?.value != "(" || i + 1 == helper.open) continue
                val args = arguments(i + 1)
                for ((index, variable) in helper.parameters) {
                    val arg = args.getOrNull(index)?.takeIf { it.count() == 1 } ?: continue
                    for (owner in owners.filter { it.variable == tokens[arg.first].value && it.at < i && visible(it.scope, i) }) {
                        val alias = Owner(variable, owner.name, helper.body, helper.body)
                        if (alias !in owners) owners.add(alias)
                    }
                }
            }
        }
        for (i in tokens.indices) {
            val api = tokens[i].value
            if (api !in setOf("Method", "Property", "Constructor", "Destructor", "Value", "BindGlobalFunction", "BindGlobalProperty") || tokens.getOrNull(i + 1)?.value != "(") continue
            val args = arguments(i + 1)
            val arg = args.firstOrNull() ?: continue
            val signature = stringValue(arg) ?: continue
            val global = api.startsWith("BindGlobal")
            val memberOwners = if (global) listOf(namespaces.lastOrNull { it.at < i && visible(it.scope, i) }?.name.orEmpty())
                else owners.filter { it.variable == tokens.getOrNull(i - 2)?.value && it.at < i && visible(it.scope, i) }
                    .let { visibleOwners ->
                        val nearest = visibleOwners.lastOrNull()?.scope
                        val inScope = visibleOwners.filter { it.scope == nearest }
                        val latest = inScope.maxOfOrNull { it.at }
                        inScope.filter { it.at == latest }.map { it.name }.distinct()
                    }
            if (memberOwners.isEmpty()) continue
            val kind = if (api in setOf("Property", "BindGlobalProperty")) "property" else if (api == "Value") "enumValue" else "function"
            val name = if (kind == "function") functionName(signature) else Regex("\\w+$").find(signature.trim())?.value
            if (name == null) continue
            val offset = arg.firstOrNull { tokens[it].value.startsWith('"') }?.let { tokens[it].offset + 1 + tokens[it].value.indexOf(name).coerceAtLeast(1) - 1 }
                ?: tokens[i].offset
            val target = args.getOrNull(1)?.let(::nativeTarget) ?: (null to null)
            for (owner in memberOwners) result.add(Binding(owner, name, kind, offset, target.first, target.second, signature))
        }
        return result
    }
}
