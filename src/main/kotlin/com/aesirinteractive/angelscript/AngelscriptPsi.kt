package com.aesirinteractive.angelscript

import com.intellij.lang.Commenter
import com.intellij.lexer.LexerBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

internal object AngelscriptTokens {
    val IDENTIFIER = IElementType("identifier", AngelscriptLanguage.INSTANCE)
    val KEYWORD = IElementType("keyword", AngelscriptLanguage.INSTANCE)
    val NUMBER = IElementType("number", AngelscriptLanguage.INSTANCE)
    val STRING = IElementType("string", AngelscriptLanguage.INSTANCE)
    val COMMENT = IElementType("comment", AngelscriptLanguage.INSTANCE)
    val SYMBOL = IElementType("symbol", AngelscriptLanguage.INSTANCE)
}

/** Coarse tokens for safe debugger expression extraction; PSI uses the generated lexer. */
internal class AngelscriptExpressionLexer : LexerBase() {
    private var buffer: CharSequence = ""
    private var limit = 0
    private var from = 0
    private var to = 0
    private var type: IElementType? = null

    override fun start(text: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        buffer = text; limit = endOffset; from = startOffset; to = from
        advance()
    }
    override fun getState() = 0
    override fun getTokenType() = type
    override fun getTokenStart() = from
    override fun getTokenEnd() = to
    override fun getBufferSequence() = buffer
    override fun getBufferEnd() = limit

    override fun advance() {
        from = to
        if (from >= limit) { type = null; return }
        var i = from
        val c = buffer[i]
        fun starts(value: String) = i + value.length <= limit &&
            value.indices.all { buffer[i + it] == value[it] }
        type = when {
            c.isWhitespace() -> {
                while (i < limit && buffer[i].isWhitespace()) i++
                TokenType.WHITE_SPACE
            }
            starts("//") -> {
                while (i < limit && buffer[i] != '\n') i++
                AngelscriptTokens.COMMENT
            }
            starts("/*") -> {
                i += 2
                while (i < limit && !starts("*/")) i++
                if (i < limit) i += 2
                AngelscriptTokens.COMMENT
            }
            c == '"' || c == '\'' || (c in "fn" && i + 1 < limit && buffer[i + 1] == '"') -> {
                if (c != '"' && c != '\'') i++
                val quote = buffer[i]
                if (quote == '"' && starts("\"\"\"")) {
                    i += 3
                    while (i < limit && !starts("\"\"\"")) i++
                    if (i < limit) i += 3
                } else {
                    i++
                    while (i < limit) {
                        if (buffer[i] == '\\') { i = (i + 2).coerceAtMost(limit); continue }
                        if (buffer[i++] == quote) break
                    }
                }
                AngelscriptTokens.STRING
            }
            Character.isUnicodeIdentifierStart(Character.codePointAt(buffer, i)) || c == '_' -> {
                i += Character.charCount(Character.codePointAt(buffer, i))
                while (i < limit && Character.isUnicodeIdentifierPart(Character.codePointAt(buffer, i)))
                    i += Character.charCount(Character.codePointAt(buffer, i))
                if (buffer.subSequence(from, i).toString() in KEYWORDS) AngelscriptTokens.KEYWORD
                else AngelscriptTokens.IDENTIFIER
            }
            c.isDigit() -> {
                i++
                while (i < limit && (buffer[i].isLetterOrDigit() || buffer[i] == '.' || buffer[i] == '_')) i++
                AngelscriptTokens.NUMBER
            }
            else -> {
                val operator = OPERATORS.firstOrNull { starts(it) }
                i += operator?.length ?: 1
                AngelscriptTokens.SYMBOL
            }
        }
        to = i
    }

    companion object {
        private val OPERATORS = listOf(">>>=", "**=", ">>=", "<<=", ">>>", "^^", "::", "++", "--", "**", "==", "!=",
            "<=", ">=", "&&", "||", "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=", "<<", ">>")
        private val KEYWORDS = ("if else for while do switch case default break continue return class struct enum " +
            "namespace interface delegate event mixin typedef import const auto void bool int int8 int16 int32 int64 " +
            "uint uint8 uint16 uint32 uint64 float double true false nullptr private protected public property " +
            "final override abstract super this and or xor not is in out inout").split(' ').toSet()
    }
}

class AngelscriptCommenter : Commenter {
    override fun getLineCommentPrefix() = "//"
    override fun getBlockCommentPrefix() = "/*"
    override fun getBlockCommentSuffix() = "*/"
    override fun getCommentedBlockCommentPrefix(): String? = null
    override fun getCommentedBlockCommentSuffix(): String? = null
}
