package com.bazhicc.angelscript.formatter

/** A local layout rule for AS virtual properties; ordinary C++ short-block settings stay intact. */
internal object PropertyAccessorFormatting {
    fun adjust(syntax: AngelScriptSyntax, edits: List<Replacement>, ranges: List<FormatRange>): List<Replacement> {
        if (!syntax.complete) return edits
        val text = syntax.text
        val tokens = syntax.tokens
        val closing = mutableMapOf<Int, Int>()
        val stack = ArrayDeque<Int>()
        for ((index, token) in tokens.withIndex()) {
            if (token.kind != AngelScriptSyntax.Kind.SYMBOL) continue
            when (token.value) {
                "{" -> stack.addLast(index)
                "}" -> {
                    if (stack.isEmpty()) return edits
                    closing[stack.removeLast()] = index
                }
            }
        }
        if (stack.isNotEmpty()) return edits

        val overrides = mutableListOf<Replacement>()
        for ((open, close) in closing) {
            // A virtual-property body consists exclusively of get/set accessors, optionally
            // declarations. In particular, get()/set() methods and property functions do not match.
            if (tokens.getOrNull(open - 1)?.kind != AngelScriptSyntax.Kind.WORD) continue
            val accessors = mutableListOf<Pair<Int, Int>>()
            var cursor = open + 1
            while (cursor < close) {
                if (tokens[cursor].kind == AngelScriptSyntax.Kind.COMMENT) { cursor++; continue }
                val start = cursor
                if (tokens[cursor].value !in listOf("get", "set")) break
                cursor++
                while (cursor < close && (tokens[cursor].value in listOf("const", "final", "override") ||
                            tokens[cursor].kind == AngelScriptSyntax.Kind.COMMENT)) cursor++
                if (tokens.getOrNull(cursor)?.value == ";") { cursor++; continue }
                val end = closing[cursor] ?: break
                accessors += start to cursor
                cursor = end + 1
            }
            if (cursor != close) continue
            for ((start, body) in accessors) {
                val end = closing.getValue(body)
                val span = FormatRange(tokens[start].start, tokens[end].end)
                if (ranges.isNotEmpty() && ranges.none { it.contains(span.start, span.end) }) continue
                if (syntax.disabled.any { it.start < span.end && it.end > span.start }) continue
                val accessor = tokens.subList(start, end + 1)
                if (accessor.any { it.kind in listOf(AngelScriptSyntax.Kind.COMMENT, AngelScriptSyntax.Kind.DIRECTIVE) ||
                            '\n' in it.value || '\r' in it.value }) continue
                val statement = tokens.subList(body + 1, end)
                // Keep empty, multi-statement, nested and control-flow bodies on the normal path.
                if (statement.isEmpty() || statement.last().value != ";" || statement.count { it.value == ";" } != 1 ||
                    statement.any { it.value in listOf("{", "}") } ||
                    statement.first().value in listOf("if", "else", "for", "foreach", "while", "do", "switch", "try", "catch") ||
                    statement.getOrNull(1)?.value == ":") continue
                for (index in start until end) {
                    val left = tokens[index]
                    val right = tokens[index + 1]
                    val gap = text.substring(left.end, right.start)
                    if (!gap.all(Char::isWhitespace) || syntax.protected(left.end, right.start)) continue
                    val formatted = StringBuilder(gap).apply {
                        edits.filter { it.start >= left.end && it.end <= right.start }.asReversed().forEach {
                            replace(it.start - left.end, it.end - left.end, it.text)
                        }
                    }.toString()
                    val value = if (index <= body || index + 1 == end || '\n' in formatted || '\r' in formatted) " " else formatted
                    overrides += Replacement(left.end, right.start, value)
                }
            }
        }
        return (edits.filterNot { edit -> overrides.any { it.start <= edit.start && it.end >= edit.end } } + overrides)
            .filter { text.substring(it.start, it.end) != it.text }.sortedBy { it.start }
    }
}
