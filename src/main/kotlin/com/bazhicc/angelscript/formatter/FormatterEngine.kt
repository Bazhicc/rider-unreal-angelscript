package com.bazhicc.angelscript.formatter

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.ByteArrayInputStream
import javax.xml.parsers.SAXParserFactory

data class FormatRange(val start: Int, val end: Int) {
    init { require(start >= 0 && end >= start) }
    fun contains(start: Int, end: Int) = start >= this.start && end <= this.end
}

data class Replacement(val start: Int, val end: Int, val text: String)

object FormatterEngine {
    fun byteRange(text: String, range: FormatRange): Pair<Int, Int> {
        require(range.end <= text.length)
        return text.substring(0, range.start).toByteArray(Charsets.UTF_8).size to
            text.substring(range.start, range.end).toByteArray(Charsets.UTF_8).size
    }

    fun replacements(text: String, xml: String): List<Replacement> {
        val bytes = text.toByteArray(Charsets.UTF_8)
        val result = mutableListOf<Replacement>()
        var byteCursor = 0
        var charCursor = 0
        val factory = SAXParserFactory.newInstance().apply {
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://xml.org/sax/features/external-parameter-entities", false)
        }
        factory.newSAXParser().parse(ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8)), object : DefaultHandler() {
            var start = -1
            var end = -1
            val replacement = StringBuilder()
            override fun startElement(uri: String?, localName: String?, name: String, attributes: Attributes) {
                when (name) {
                    "replacements" -> Unit
                    "replacement" -> {
                        require(start == -1) { "Nested clang-format replacement" }
                        val offset = attributes.getValue("offset").toInt()
                        val length = attributes.getValue("length").toInt()
                        require(offset >= 0 && length >= 0 && offset.toLong() + length <= bytes.size)
                        fun utf16(index: Int): Int {
                            require(index == bytes.size || bytes[index].toInt() and 0xc0 != 0x80) {
                                "clang-format returned an offset inside a UTF-8 character"
                            }
                            val converted = if (index >= byteCursor)
                                charCursor + String(bytes, byteCursor, index - byteCursor, Charsets.UTF_8).length
                            else String(bytes, 0, index, Charsets.UTF_8).length
                            byteCursor = index; charCursor = converted
                            return converted
                        }
                        start = utf16(offset); end = utf16(offset + length)
                        replacement.setLength(0)
                    }
                    else -> error("Unexpected clang-format element: $name")
                }
            }
            override fun characters(chars: CharArray, offset: Int, length: Int) {
                if (start >= 0) replacement.append(chars, offset, length)
            }
            override fun endElement(uri: String?, localName: String?, name: String) {
                if (name == "replacement") {
                    result += Replacement(start, end, replacement.toString())
                    start = -1
                }
            }
        })
        val sorted = result.sortedBy { it.start }
        sorted.zipWithNext().forEach { (left, right) -> require(left.end <= right.start) { "Overlapping replacements" } }
        return sorted
    }

    fun allowEdit(text: String, edit: Replacement): Boolean {
        val lineStart = if (edit.start == 0) 0 else text.lastIndexOf('\n', edit.start - 1) + 1
        val lineEnd = text.indexOf('\n', edit.start).let { if (it < 0) text.length else it }
        val line = text.substring(lineStart, lineEnd)
        if (edit.end > lineEnd) return true
        val column = edit.start - lineStart
        if (edit.start == edit.end && edit.text == " " && column > 0) {
            val context = line.substring(column - 1)
            if (Regex("^[A-Za-z_]['\"]").containsMatchIn(context) ||
                Regex("^&(in|out|inout)\\b").containsMatchIn(context)) return false
        }
        if ('\n' !in edit.text) return true
        val changed = line.substring(0, column) + edit.text + line.substring(edit.end - lineStart)
        if (Regex("^\\s*[A-Za-z_]\\w*\\s+\\w+\\s*\\(").containsMatchIn(line) &&
            Regex("^\\s*[A-Za-z_]\\w*\\r?\\n\\s*\\w+\\s*\\(").containsMatchIn(changed)) return false
        for (keyword in listOf("private", "protected", "delegate", "event")) {
            if (Regex("^\\s*$keyword\\s+\\w+").containsMatchIn(line) &&
                Regex("^\\s*$keyword(?:\\s+\\w+)?\\r?\\n").containsMatchIn(changed)) return false
        }
        return true
    }

    fun allowedReplacements(text: String, xml: String, ranges: List<FormatRange> = emptyList(),
                            preserveNewlines: Boolean = false, whitespaceOnly: Boolean = false,
                            spaceBeforeAssignment: Boolean = true, disabled: List<FormatRange> = emptyList()): List<Replacement> {
        val syntax = AngelScriptSyntax(text, disabled)
        val operators = OperatorProtection(text, syntax)
        val raw = replacements(text, xml)
        val access = syntax.tokens.windowed(3).filter { (a, colon, name) -> a.value == "access" && colon.value == ":" && name.kind == AngelScriptSyntax.Kind.WORD }
        val edits = operators.spacing(raw.mapNotNull(operators::adjust), spaceBeforeAssignment).mapNotNull { edit ->
            if (access.any { (a, _, name) -> edit.start >= a.end && edit.start < name.end }) null
            else edit
        }.toMutableList()
        // C++ treats access:Name as an ordinary label and can join it to the next declaration.
        // Preserve the AS qualifier line, but format the following member at the qualifier's indent.
        for ((a, _, name) in access) {
            val next = syntax.tokens.firstOrNull { it.start >= name.end } ?: continue
            if (text.substring(name.end, next.start).all(Char::isWhitespace) && '\n' in text.substring(name.end, next.start)) {
                val leading = raw.lastOrNull { it.end == a.start && it.text.all(Char::isWhitespace) }?.text?.substringAfterLast('\n')
                    ?: text.substring(text.lastIndexOf('\n', a.start - 1) + 1, a.start)
                if (leading.all(Char::isWhitespace)) {
                    edits.removeAll { it.start >= name.end && it.end <= next.start }
                    edits += Replacement(name.end, next.start, "\n$leading")
                }
            }
        }
        val whitespace = edits.sortedBy { it.start }.filter { edit ->
            !syntax.protected(edit.start, edit.end) && allowEdit(text, edit) && (ranges.isEmpty() || ranges.any { it.contains(edit.start, edit.end) }) &&
                (!preserveNewlines || edit.text.count { it == '\n' } >= text.substring(edit.start, edit.end).count { it == '\n' }) &&
                // Structural code changes are performed only by the AS statement parser.
                text.substring(edit.start, edit.end).all(Char::isWhitespace) && edit.text.all(Char::isWhitespace) &&
                text.substring(edit.start, edit.end) != edit.text
        }
        // Compact only complete simple AS accessors, including when Rider preserves other line breaks.
        val allowed = PropertyAccessorFormatting.adjust(syntax, whitespace, ranges)
        val rendered = StringBuilder(text).apply { allowed.asReversed().forEach { replace(it.start, it.end, it.text) } }.toString()
        // This invariant also catches unanticipated C++ rewrites, not just listed AS exceptions.
        return if (AngelScriptSyntax(rendered).signature() == syntax.signature()) allowed else emptyList()
    }

    fun apply(text: String, xml: String, ranges: List<FormatRange> = emptyList(),
              preserveNewlines: Boolean = false, whitespaceOnly: Boolean = false): String {
        val result = StringBuilder(text)
        allowedReplacements(text, xml, ranges, preserveNewlines, whitespaceOnly).asReversed()
            .forEach { result.replace(it.start, it.end, it.text) }
        return result.toString()
    }
}
