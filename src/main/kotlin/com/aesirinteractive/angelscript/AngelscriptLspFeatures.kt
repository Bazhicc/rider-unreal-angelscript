package com.aesirinteractive.angelscript

import org.eclipse.lsp4j.InlineValue
import org.eclipse.lsp4j.Range

internal data class AngelscriptInlineExpression(val expression: String, val line: Int)

/** LSP positions and IntelliJ document offsets both count UTF-16 code units. */
internal fun inlineExpressions(values: List<InlineValue>, text: String): List<AngelscriptInlineExpression> {
    val lines = text.split('\n')
    fun source(range: Range): String? {
        val start = range.start
        val end = range.end
        if (start.line != end.line || start.line !in lines.indices) return null
        val line = lines[start.line].removeSuffix("\r")
        if (start.character < 0 || end.character < start.character || end.character > line.length) return null
        return line.substring(start.character, end.character)
    }
    return values.mapNotNull { value ->
        val range: Range
        val expression: String?
        when {
            value.isInlineValueVariableLookup -> {
                range = value.inlineValueVariableLookup.range
                expression = value.inlineValueVariableLookup.variableName ?: source(range)
            }
            value.isInlineValueEvaluatableExpression -> {
                range = value.inlineValueEvaluatableExpression.range
                expression = value.inlineValueEvaluatableExpression.expression ?: source(range)
            }
            else -> return@mapNotNull null
        }
        if (expression.isNullOrBlank() || range.start.line !in lines.indices) null
        else AngelscriptInlineExpression(expression, range.start.line)
    }.distinctBy { it.expression }
}
