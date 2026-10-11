package com.aesirinteractive.angelscript

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

internal data class AngelscriptLexeme(val type: IElementType, val start: Int, val end: Int, val text: String)

internal fun angelscriptLexemes(text: CharSequence): List<AngelscriptLexeme> {
    val lexer = AngelscriptExpressionLexer()
    lexer.start(text)
    val result = ArrayList<AngelscriptLexeme>()
    while (lexer.tokenType != null) {
        if (lexer.tokenType != TokenType.WHITE_SPACE && lexer.tokenType != AngelscriptTokens.COMMENT)
            result.add(AngelscriptLexeme(lexer.tokenType!!, lexer.tokenStart, lexer.tokenEnd, lexer.tokenText))
        lexer.advance()
    }
    return result
}

/** Only extracts side-effect-free debugger expressions; not a language parser. */
internal object AngelscriptDebugExpressions {
    private data class Snapshot(val stamp: Long, val tokens: List<AngelscriptLexeme>)
    private val snapshotKey = Key.create<Snapshot>("angelscript.debug.expression.tokens")

    fun range(document: Document, offset: Int): TextRange? {
        val cached = document.getUserData(snapshotKey)?.takeIf { it.stamp == document.modificationStamp }
            ?: Snapshot(document.modificationStamp, angelscriptLexemes(document.charsSequence)).also {
                document.putUserData(snapshotKey, it)
            }
        return range(cached.tokens, offset)
    }

    fun range(text: CharSequence, offset: Int): TextRange? = range(angelscriptLexemes(text), offset)

    private fun range(tokens: List<AngelscriptLexeme>, offset: Int): TextRange? {
        val index = tokens.indexOfFirst { offset >= it.start && offset < it.end }
        if (index < 0) return null
        val token = tokens[index]
        if (token.type == AngelscriptTokens.STRING) return null
        val pairs = HashMap<Int, Int>()
        val stack = ArrayList<Int>()
        for (i in tokens.indices) {
            when (tokens[i].text) {
                "[", "(" -> stack.add(i)
                "]", ")" -> {
                    val open = stack.lastOrNull() ?: continue
                    if ((tokens[open].text == "[" && tokens[i].text == "]") ||
                        (tokens[open].text == "(" && tokens[i].text == ")")) {
                        stack.removeAt(stack.lastIndex); pairs[open] = i; pairs[i] = open
                    }
                }
            }
        }
        fun unsafe(from: Int, to: Int): Boolean = (from..to).any { i ->
            tokens[i].text in setOf("++", "--", "=", "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=",
                "<<=", ">>=", ">>>=", "**=", "new", "delete") ||
                (tokens[i].text == "(" && i > from &&
                    (tokens[i - 1].type == AngelscriptTokens.IDENTIFIER || tokens[i - 1].text in setOf("]", ")", ">", ">>")))
        }
        fun startAt(end: Int): Int? {
            var cursor = end
            while (cursor >= 0) {
                val current = tokens[cursor]
                when {
                    current.text == "]" -> {
                        val open = pairs[cursor] ?: return null
                        if (unsafe(open + 1, cursor - 1)) return null
                        cursor = open - 1
                        continue
                    }
                    current.type == AngelscriptTokens.IDENTIFIER || current.text in setOf("this", "super") -> {
                        if (cursor > 1 && tokens[cursor - 1].text in setOf(".", "::")) {
                            cursor -= 2
                            continue
                        }
                        return cursor
                    }
                    // Do not invoke GetActor().Field or evaluate a partial member name.
                    else -> return null
                }
            }
            return null
        }
        val endIndex = when {
            token.text == "[" -> pairs[index] ?: return null
            token.text == "]" -> index
            token.type == AngelscriptTokens.IDENTIFIER || token.text in setOf("this", "super") -> index
            else -> return null
        }
        val start = startAt(endIndex) ?: return null
        return TextRange(tokens[start].start, tokens[endIndex].end)
    }
}
