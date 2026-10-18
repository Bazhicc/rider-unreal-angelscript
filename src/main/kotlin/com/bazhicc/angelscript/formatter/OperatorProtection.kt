package com.bazhicc.angelscript.formatter

/** Clang parses C++, whose operator tokens differ from AngelScript's. */
internal class OperatorProtection(private val text: String, private val syntax: AngelScriptSyntax = AngelScriptSyntax(text)) {
    private data class Operator(val start: Int, val end: Int, val value: String)
    private val operators = syntax.tokens.filter { it.kind == AngelScriptSyntax.Kind.SYMBOL }
        .map { Operator(it.start, it.end, it.value) }

    fun adjust(edit: Replacement): Replacement? {
        if (syntax.protected(edit.start, edit.end)) return null
        if (edit.text.any { !it.isWhitespace() } || text.substring(edit.start, edit.end).any { !it.isWhitespace() })
            return edit
        val index = operators.binarySearch { it.start.compareTo(edit.start) }.let { if (it < 0) -it - 2 else it }
        val token = operators.getOrNull(index)
        if (token != null && edit.start > token.start && edit.start < token.end) return null
        val left = token?.takeIf { it.end == edit.start }
        val right = (if (token?.start == edit.start) token else operators.getOrNull(index + 1))
            ?.takeIf { it.start == edit.end }
        if (edit.text.isEmpty() && left != null && right != null &&
            split(left.value + right.value) != listOf(left.value, right.value)) return null
        // C++ treats ** as pointer/dereference punctuation and removes binary-operator spacing.
        // Keep at least one space when clang tries to remove a gap beside an AS-only operator.
        if (edit.text.isEmpty() && edit.end > edit.start &&
            (left?.value in AS_ONLY || right?.value in AS_ONLY)) return edit.copy(text = " ")
        return edit
    }

    fun spacing(edits: List<Replacement>, beforeAssignment: Boolean): List<Replacement> {
        val result = edits.toMutableList()
        for (token in operators.filter { it.value in AS_ONLY }) {
            if (syntax.disabled.any { token.start in it.start until it.end }) continue
            var start = token.start
            while (start > 0 && text[start - 1].isWhitespace()) start--
            var end = token.end
            while (end < text.length && text[end].isWhitespace()) end++
            for ((gap, before) in listOf(FormatRange(start, token.start) to true, FormatRange(token.end, end) to false)) {
                if (gap.start == 0 || gap.end == text.length) continue
                val original = text.substring(gap.start, gap.end)
                val proposed = edits.firstOrNull { it.start == gap.start && it.end == gap.end }?.text
                val value = when {
                    proposed != null && '\n' in proposed -> proposed
                    proposed == null && '\n' in original -> original
                    before && token.value in listOf("**=", ">>>=") && !beforeAssignment -> ""
                    else -> " "
                }
                result.removeAll { it.start >= gap.start && it.end <= gap.end }
                if (original != value) result += Replacement(gap.start, gap.end, value)
            }
        }
        return result.distinct()
    }

    companion object {
        private val AS_ONLY = setOf("**", "**=", ">>>", ">>>=", "^^", "!is")
        // Longest match, as in the AngelScript tokenizer. Comment openers also must not be formed
        // by removing whitespace between separate division/multiplication operators.
        private fun split(text: String): List<String> {
            val result = mutableListOf<String>()
            var i = 0
            while (i < text.length) {
                val value = AngelScriptSyntax.symbol(text, i)
                result += value
                i += value.length
            }
            return result
        }

    }
}
