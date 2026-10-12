package com.aesirinteractive.angelscript

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiErrorElement
import com.intellij.testFramework.ParsingTestCase
import java.nio.file.Files
import java.nio.file.Path

/** Migration snapshots, with the strings baseline corrected for named arguments inside interpolations. */
class AngelscriptParserBaselineTest : ParsingTestCase("", "as", AngelscriptParserDefinition()) {
    override fun getTestDataPath() = ""

    fun testCompleteAndIncompleteSyntaxMatchesBaseline() {
        val data = Path.of("src/test/resources/parser-baseline")
        Files.list(data).use { paths ->
            paths.filter { it.toString().endsWith(".as") }.sorted().forEach { path ->
                val source = Files.readString(path).replace("\r\n", "\n")
                val actual = buildString {
                    // Include incomplete edits to protect error recovery, token offsets and scopes.
                    val lengths = (source.indices step 13).toList() + source.length
                    for (length in lengths) {
                        val text = source.take(length)
                        appendLine("INPUT $length")
                        val lexer = AngelscriptLexerAdapter()
                        lexer.start(text)
                        var count = 0
                        while (lexer.tokenType != null) {
                            check(count++ <= text.length * 2 + 10) { "Lexer did not advance" }
                            appendLine("TOKEN ${lexer.tokenType} ${lexer.tokenStart}:${lexer.tokenEnd} ${lexer.state}")
                            lexer.advance()
                        }
                        val file = parseFile(path.fileName.toString(), text)
                        fun appendNode(node: ASTNode, depth: Int) {
                            append(" ".repeat(depth)).append(node.elementType).append(' ')
                                .append(node.startOffset).append(':').append(node.textLength)
                            (node.psi as? PsiErrorElement)?.let { append(' ').append(it.errorDescription) }
                            appendLine()
                            var child = node.firstChildNode
                            while (child != null) { appendNode(child, depth + 1); child = child.treeNext }
                        }
                        appendNode(file.node, 0)
                    }
                }
                val expected = path.resolveSibling(path.fileName.toString().removeSuffix(".as") + ".snapshot")
                assertEquals("Parser/lexer changed for ${path.fileName}", Files.readString(expected).replace("\r\n", "\n"), actual)
            }
        }
    }
}
