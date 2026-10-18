package com.bazhicc.angelscript.formatter

/** Statement boundaries, rather than C++ formatting guesses, determine authorized brace edits. */
internal class ControlFlowParser(private val syntax: AngelScriptSyntax) {
    private val text = syntax.text
    private val tokens = syntax.tokens.filter { it.kind != AngelScriptSyntax.Kind.COMMENT }
    private val matching = mutableMapOf<Int, Int>()
    private val statements = mutableMapOf<Int, Statement>()
    private val doTails = mutableSetOf<Int>()
    private var valid = syntax.complete
    private data class Statement(val start: Int, val end: Int, val kind: String,
                                 val headerEnd: Int = start, val body: Statement? = null,
                                 val elseHeader: Int? = null, val otherwise: Statement? = null)
    private data class Branch(val header: Int, val headerEnd: Int, val body: Statement, val danglingElse: Boolean = false)

    init {
        val stack = ArrayDeque<Int>()
        for ((index, token) in tokens.withIndex()) {
            if (token.kind != AngelScriptSyntax.Kind.SYMBOL) continue
            when (token.value) {
                "(", "[", "{" -> stack.addLast(index)
                ")", "]", "}" -> {
                    val open = stack.removeLastOrNull()
                    if (open == null || tokens[open].value != when (token.value) { ")" -> "("; "]" -> "["; else -> "{" }) valid = false
                    else { matching[open] = index; matching[index] = open }
                }
            }
        }
        if (stack.isNotEmpty()) valid = false
    }

    private fun value(index: Int) = tokens.getOrNull(index)?.value
    private fun parse(start: Int, depth: Int = 0): Statement? {
        statements[start]?.let { return it }
        if (depth > 128 || start !in tokens.indices || tokens[start].kind == AngelScriptSyntax.Kind.DIRECTIVE) return null
        val keyword = value(start)!!
        val statement = when (keyword) {
            "{" -> Statement(start, (matching[start] ?: return null) + 1, "block")
            "if", "for", "foreach", "while", "switch" -> {
                if (value(start + 1) != "(") return null
                val headerEnd = (matching[start + 1] ?: return null) + 1
                val body = parse(headerEnd, depth + 1) ?: return null
                if (keyword == "switch" && body.kind != "block") return null
                val elseHeader = if (keyword == "if" && value(body.end) == "else") body.end else null
                val other = elseHeader?.let { parse(it + 1, depth + 1) ?: return null }
                Statement(start, other?.end ?: body.end, keyword, headerEnd, body, elseHeader, other)
            }
            "do" -> {
                val body = parse(start + 1, depth + 1) ?: return null
                if (value(body.end) != "while" || value(body.end + 1) != "(") return null
                val close = matching[body.end + 1] ?: return null
                if (value(close + 1) != ";") return null
                doTails += body.end
                Statement(start, close + 2, keyword, start + 1, body)
            }
            "try" -> {
                var end = parse(start + 1, depth + 1)?.takeIf { it.kind == "block" }?.end ?: return null
                if (value(end) != "catch") return null
                while (value(end) == "catch") {
                    var body = end + 1
                    if (value(body) == "(") body = (matching[body] ?: return null) + 1
                    end = parse(body, depth + 1)?.takeIf { it.kind == "block" }?.end ?: return null
                }
                Statement(start, end, keyword)
            }
            else -> {
                var end = start
                while (end < tokens.size && value(end) != ";") {
                    if (value(end) in listOf("}", "else") || tokens[end].kind == AngelScriptSyntax.Kind.DIRECTIVE) return null
                    end = matching[end]?.takeIf { it > end }?.plus(1) ?: (end + 1)
                }
                if (end == tokens.size) return null
                Statement(start, end + 1, "simple")
            }
        }
        statements[start] = statement
        return statement
    }

    private fun children(block: Statement): List<Statement>? {
        val result = mutableListOf<Statement>()
        var index = block.start + 1
        while (index < block.end - 1) {
            val child = parse(index) ?: return null
            result += child
            index = child.end
        }
        return result
    }

    private fun declaration(statement: Statement): Boolean {
        val first = value(statement.start)
        val next = value(statement.start + 1)
        if (first in listOf("return", "break", "continue")) return false
        return first in listOf("const", "auto", "local", "class", "struct", "enum") ||
            (tokens[statement.start].kind == AngelScriptSyntax.Kind.WORD &&
                (tokens.getOrNull(statement.start + 1)?.kind == AngelScriptSyntax.Kind.WORD || next in listOf("<", "@", "&", "[")))
    }

    private fun absorbsElse(statement: Statement): Boolean = when (statement.kind) {
        "if" -> statement.otherwise?.let(::absorbsElse) ?: true
        "for", "foreach", "while" -> statement.body?.let(::absorbsElse) ?: false
        else -> false
    }

    private fun endOffset(body: Statement): Int {
        var end = tokens[body.end - 1].end
        for (comment in syntax.tokens) {
            if (comment.start < end || comment.kind != AngelScriptSyntax.Kind.COMMENT) continue
            if (text.substring(end, comment.start).any { !it.isWhitespace() || it == '\n' }) break
            end = comment.end
        }
        return end
    }

    fun edits(ranges: List<FormatRange>, policy: BracePolicy, formatting: List<Replacement>): List<Replacement> {
        if (!valid) return emptyList()
        for (index in tokens.indices) {
            if (value(index) in listOf("if", "for", "foreach", "while", "do") && index !in doTails && parse(index) == null)
                return emptyList() // Preprocessor-dependent/incomplete bodies cannot safely establish else or do-tail binding.
        }
        val controls = statements.values.filter { it.kind in listOf("if", "for", "foreach", "while", "do") && it.start !in doTails }
        val elseIfs = controls.mapNotNull { it.otherwise?.takeIf { other -> other.kind == "if" }?.start }.toSet()
        val branches = mutableListOf<Pair<Branch, Boolean>>()
        fun requiredBlock(body: Statement): Boolean = body.kind == "block" &&
            (children(body)?.let { it.size > 1 || it.singleOrNull()?.let(::declaration) == true } ?: true)
        fun chain(statement: Statement): List<Branch> {
            val first = Branch(statement.start, statement.headerEnd, statement.body!!, statement.otherwise != null)
            val rest = when (statement.otherwise?.kind) {
                null -> emptyList()
                "if" -> chain(statement.otherwise)
                else -> listOf(Branch(statement.elseHeader!!, statement.elseHeader + 1, statement.otherwise))
            }
            return listOf(first) + rest
        }
        for (statement in controls.sortedBy { it.start }) {
            if (statement.kind == "if") {
                if (statement.start in elseIfs) continue
                val chain = chain(statement)
                val anyRequired = chain.any { branch -> requiredBlock(branch.body) ||
                    (branch.danglingElse && branch.body.kind == "block" &&
                        children(branch.body)?.singleOrNull()?.let(::absorbsElse) == true) }
                branches += chain.map { it to anyRequired }
            } else branches += Branch(statement.start, statement.headerEnd, statement.body!!) to false
        }
        fun multiline(start: Int, end: Int): Boolean {
            val projected = StringBuilder(text.substring(start, end))
            formatting.filter { it.start >= start && it.end <= end }.asReversed().forEach {
                projected.replace(it.start - start, it.end - start, it.text)
            }
            return '\n' in projected
        }
        val result = mutableListOf<Replacement>()
        for ((branch, anyRequired) in branches) {
            val body = branch.body
            val headerStart = tokens[branch.header].start
            val bodyEnd = endOffset(body)
            val statementEnd = tokens[body.end - 1].end
            if (ranges.isNotEmpty() && ranges.none { it.contains(headerStart, bodyEnd) }) continue
            if (syntax.disabled.any { headerStart < it.end && bodyEnd > it.start } ||
                tokens.subList(branch.header, body.end).any { it.kind == AngelScriptSyntax.Kind.DIRECTIVE }) continue
            val inner = if (body.kind == "block") children(body) else null
            val bodyStart = if (body.kind == "block") tokens.getOrNull(body.start + 1)?.start ?: statementEnd else tokens[body.start].start
            val contentEnd = if (body.kind == "block") tokens[body.end - 2].end else statementEnd
            val require = when (policy.mode(tokens[branch.header].value)) {
                BraceMode.REQUIRED -> true
                BraceMode.BODY_MULTILINE -> multiline(bodyStart.coerceAtMost(contentEnd), contentEnd)
                BraceMode.STATEMENT_MULTILINE -> multiline(headerStart, statementEnd)
                BraceMode.IF_ANY_REQUIRES -> anyRequired
                BraceMode.NOT_REQUIRED -> false
            }
            if (body.kind != "block" && require) {
                val open = tokens[branch.headerEnd - 1].end
                result += Replacement(open, open, " {")
                result += Replacement(bodyEnd, bodyEnd, "\n}")
            } else if (body.kind == "block" && !require && policy.removeRedundant && inner?.size == 1 &&
                !declaration(inner.single()) && !(branch.danglingElse && absorbsElse(inner.single()))) {
                result += Replacement(tokens[body.start].start, tokens[body.start].end, "")
                result += Replacement(tokens[body.end - 1].start, tokens[body.end - 1].end, "")
            }
        }
        // Nested statements can share a closing offset. Inner closes must come first.
        return result.sortedWith(compareBy<Replacement> { it.start }.thenBy { it.text != "\n}" })
            .groupBy { it.start to it.end }.map { (range, edits) ->
                Replacement(range.first, range.second, edits.joinToString("") { it.text })
            }.sortedBy { it.start }
    }
}
