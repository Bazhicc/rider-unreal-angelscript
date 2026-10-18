package com.bazhicc.angelscript.formatter

/** UTF-16 source ranges shared by formatting, structural edits and typing handlers. */
internal class AngelScriptSyntax(val text: String, extraDisabled: List<FormatRange> = emptyList()) {
    enum class Kind { WORD, NUMBER, SYMBOL, STRING, COMMENT, DIRECTIVE }
    data class Token(val start: Int, val end: Int, val value: String, val kind: Kind)
    val tokens: List<Token>
    val disabled: List<FormatRange>
    val complete: Boolean

    init {
        val result = mutableListOf<Token>()
        val off = mutableListOf<FormatRange>()
        var disabledStart: Int? = null
        var valid = true
        var i = 0
        while (i < text.length) {
            val start = i
            val kind: Kind
            when {
                text[i].isWhitespace() || text[i] == '\uFEFF' -> { i++; continue }
                text.startsWith("//", i) -> {
                    i = text.indexOf('\n', i + 2).let { if (it < 0) text.length else it }
                    kind = Kind.COMMENT
                }
                text.startsWith("/*", i) -> {
                    val end = text.indexOf("*/", i + 2)
                    if (end < 0) valid = false
                    i = if (end < 0) text.length else end + 2
                    kind = Kind.COMMENT
                }
                text[i] == '#' && text.substring(text.lastIndexOf('\n', i - 1) + 1, i).isBlank() -> {
                    do {
                        val end = text.indexOf('\n', i).let { if (it < 0) text.length else it }
                        val continuation = text.substring(i, end).trimEnd().endsWith('\\')
                        i = if (continuation && end < text.length) end + 1 else end
                    } while (continuation && i < text.length)
                    kind = Kind.DIRECTIVE
                }
                quoteAt(i) != null -> {
                    val end = stringEnd(i)
                    valid = valid && end.second
                    i = end.first
                    kind = Kind.STRING
                }
                text[i].isDigit() || (text[i] == '.' && text.getOrNull(i + 1)?.isDigit() == true) -> {
                    val number = NUMBER.find(text, i)!!
                    i = number.range.last + 1
                    kind = Kind.NUMBER
                }
                text[i].isLetter() || text[i] == '_' || text[i].code >= 128 -> {
                    i++
                    while (i < text.length && identifier(text[i])) i++
                    kind = Kind.WORD
                }
                else -> {
                    i += symbol(text, i).length
                    kind = Kind.SYMBOL
                }
            }
            val value = text.substring(start, i)
            result += Token(start, i, value, kind)
            if (kind == Kind.COMMENT) {
                if (OFF.containsMatchIn(value) && disabledStart == null) disabledStart = start
                if (ON.containsMatchIn(value) && disabledStart != null) {
                    off += FormatRange(disabledStart, i)
                    disabledStart = null
                }
            }
        }
        disabledStart?.let { off += FormatRange(it, text.length) }
        tokens = result
        disabled = off + extraDisabled
        complete = valid
    }

    private fun quoteAt(start: Int): Int? {
        if (text[start] == '"' || text[start] == '\'') return start
        for (prefix in listOf("u8R", "uR", "UR", "LR", "u8", "f", "n", "R", "L", "u", "U")) {
            val quote = start + prefix.length
            if (text.startsWith(prefix, start) && text.getOrNull(quote) in listOf('"', '\'')) return quote
        }
        return null
    }

    private fun stringEnd(start: Int): Pair<Int, Boolean> {
        val quote = quoteAt(start) ?: return start + 1 to false
        if (text.startsWith("\"\"\"", quote)) {
            val end = text.indexOf("\"\"\"", quote + 3)
            return if (end < 0) text.length to false else end + 3 to true
        }
        if (quote > start && text[quote - 1] == 'R' && text[quote] == '"') {
            val delimiter = text.indexOf('(', quote + 1)
            if (delimiter < 0 || delimiter - quote > 17) return text.length to false
            val closing = ")" + text.substring(quote + 1, delimiter) + "\""
            val end = text.indexOf(closing, delimiter + 1)
            return if (end < 0) text.length to false else end + closing.length to true
        }
        val formatted = quote == start + 1 && text[start] == 'f'
        var depth = 0
        var i = quote + 1
        while (i < text.length) {
            when {
                text[i] == '\\' -> i = (i + 2).coerceAtMost(text.length)
                formatted && depth == 0 && (text.startsWith("{{", i) || text.startsWith("}}", i)) -> i += 2
                formatted && text[i] == '{' -> { depth++; i++ }
                formatted && depth > 0 && text[i] == '}' -> { depth--; i++ }
                formatted && depth > 0 && text.startsWith("//", i) ->
                    i = text.indexOf('\n', i + 2).let { if (it < 0) text.length else it }
                formatted && depth > 0 && text.startsWith("/*", i) ->
                    i = text.indexOf("*/", i + 2).let { if (it < 0) text.length else it + 2 }
                formatted && depth > 0 && quoteAt(i) != null -> {
                    val nested = stringEnd(i)
                    if (!nested.second) return nested
                    i = nested.first
                }
                depth == 0 && text[i] == text[quote] -> return i + 1 to true
                else -> i++
            }
        }
        return text.length to false
    }

    fun protected(start: Int, end: Int): Boolean = disabled.any { start < it.end && end > it.start || start == end && start in it.start until it.end } ||
        tokens.any { it.kind in listOf(Kind.STRING, Kind.DIRECTIVE) &&
            (start < it.end && end > it.start || start == end && start > it.start && start < it.end) }

    fun codeAt(offset: Int): Boolean = !disabled.any { offset in it.start until it.end } &&
        tokens.none { it.kind in listOf(Kind.STRING, Kind.COMMENT, Kind.DIRECTIVE) && offset > it.start && offset < it.end }

    fun signature(): List<Pair<Kind, String>> = tokens.filter { it.kind != Kind.COMMENT }.map { it.kind to it.value }

    fun disabledLines(pattern: String): List<FormatRange> {
        if (pattern.isEmpty()) return emptyList()
        val expression = try { Regex(pattern) } catch (_: java.util.regex.PatternSyntaxException) {
            // An unsupported regex dialect must not permit structural changes in excluded code.
            return listOf(FormatRange(0, text.length))
        }
        return tokens.filter { expression.containsMatchIn(it.value) }.map { token ->
            val start = text.lastIndexOf('\n', token.start - 1) + 1
            var end = text.indexOf('\n', token.end).let { if (it < 0) text.length else it + 1 }
            if (token.kind == Kind.COMMENT && text.substring(start, token.start).isBlank() &&
                text.substring(token.end, end).isBlank())
                end = text.indexOf('\n', end).let { if (it < 0) text.length else it + 1 }
            FormatRange(start, end)
        }
    }

    /** Hide AS literals from C++ parsing while preserving every UTF-8 and UTF-16 offset. */
    fun clangText(): String {
        val result = text.toCharArray()
        for (token in tokens.filter { it.kind == Kind.STRING }) {
            val raw = token.value.contains('\n') && token.value.length >= 5 &&
                token.value.take(3).all { it.code < 128 && it !in "\r\n" } &&
                token.value.takeLast(2).all { it.code < 128 && it !in "\r\n" }
            for (index in token.start until token.end) {
                if (result[index].code < 128 && !(raw && result[index] in "\r\n")) result[index] = 'x'
            }
            if (raw) {
                "R\"(".forEachIndexed { index, c -> result[token.start + index] = c }
                result[token.end - 2] = ')'
            }
            result[token.start] = if (raw) 'R' else '"'
            result[token.end - 1] = '"'
        }
        for (token in tokens.filter { it.kind == Kind.SYMBOL }) {
            val cpp = when (token.value) {
                "**" -> "<<"
                "**=" -> "<<="
                "^^" -> "||"
                ">>>" -> ">>="
                ">>>=" -> ">>= "
                "!is" -> "!= "
                else -> continue
            }
            cpp.forEachIndexed { index, c -> result[token.start + index] = c }
        }
        return result.concatToString()
    }

    companion object {
        private val NUMBER = Regex("(?:0[xX][0-9a-fA-F]+|0[bB][01]+|0[oO][0-7]+|0[dD][0-9]+|(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?[fF]?)")
        private val OFF = Regex("(?:clang-format\\s+off|@formatter\\s*:\\s*off)")
        private val ON = Regex("(?:clang-format\\s+on|@formatter\\s*:\\s*on)")
        private val SYMBOLS = listOf(">>>=", "**=", "<<=", ">>=", "...", "!is", ">>>", "**", "^^", "++", "--",
            "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=", "<<", ">>", "==", "!=", "<=", ">=", "&&", "||", "::", "//", "/*", "*/")
        fun identifier(c: Char) = c.isLetterOrDigit() || c == '_' || c.code >= 128
        fun symbol(text: String, offset: Int): String = SYMBOLS.firstOrNull {
            text.startsWith(it, offset) && (it != "!is" || text.getOrNull(offset + 3)?.let(::identifier) != true)
        } ?: text[offset].toString()
    }
}
