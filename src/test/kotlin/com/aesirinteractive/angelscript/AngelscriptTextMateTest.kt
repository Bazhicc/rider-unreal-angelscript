package com.aesirinteractive.angelscript

import org.jetbrains.plugins.textmate.TextMateBackedFileType
import org.jetbrains.plugins.textmate.bundles.readVSCBundle
import org.jetbrains.plugins.textmate.bundles.TextMateNioResourceReader
import org.jetbrains.plugins.textmate.plist.JsonPlistReader
import org.jetbrains.plugins.textmate.language.TextMateInterner
import org.jetbrains.plugins.textmate.language.syntax.TextMateSyntaxTableBuilder
import org.jetbrains.plugins.textmate.language.syntax.lexer.TextMateElementType
import org.jetbrains.plugins.textmate.language.syntax.lexer.TextMateHighlightingLexer
import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files

class AngelscriptTextMateTest {
    @Test fun bundledOfficialGrammarLoadsInRidersTextMateEngine() {
        val bundle = AngelscriptTextMateBundleProvider().getBundles().single()
        val reader = readVSCBundle(JsonPlistReader(), TextMateNioResourceReader(bundle.path))!!
        val grammar = reader.readGrammars().single()
        val builder = TextMateSyntaxTableBuilder(object : TextMateInterner {
            override fun intern(name: String) = name
            override fun clear() {}
        })
        val scope = checkNotNull(builder.addSyntax(grammar.plist.value))
        val descriptor = builder.build().getLanguageDescriptor(scope)!!
        assertEquals("source.angelscript", descriptor.rootScopeName.toString())
        assertFalse("TextMate must not replace the AngelScript PSI language",
            TextMateBackedFileType::class.java.isInstance(AngelscriptFileType.INSTANCE))
        assertSame(AngelscriptLanguage.INSTANCE, AngelscriptFileType.INSTANCE.language)
        assertArrayEquals(javaClass.getResourceAsStream("/textmate/syntaxes/angelscript.tmLanguage.json")!!.readBytes(),
            Files.readAllBytes(bundle.path.resolve("syntaxes/angelscript.tmLanguage.json")))
        val lexer = TextMateHighlightingLexer(descriptor, 20000)
        val source = "// 中文 😀\nclass AActor { int Count = 7; FString Name = \"Text\"; }"
        lexer.start(source)
        val scopes = ArrayList<Pair<String, String>>()
        var end = 0
        while (lexer.tokenType != null) {
            assertEquals(end, lexer.tokenStart)
            scopes.add(lexer.tokenText to (lexer.tokenType as TextMateElementType).scope.toString())
            end = lexer.tokenEnd
            lexer.advance()
        }
        assertEquals(source.length, end)
        for (scopeName in listOf("comment", "keyword", "constant.numeric", "string"))
            assertTrue("Missing $scopeName in $scopes", scopes.any { it.second.contains(scopeName) })
    }
}
