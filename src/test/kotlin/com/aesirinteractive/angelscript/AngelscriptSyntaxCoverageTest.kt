package com.aesirinteractive.angelscript

import com.aesirinteractive.angelscript.psi.AngelscriptFunctionDecl
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.ParsingTestCase
import java.nio.file.Files
import java.nio.file.Path

class AngelscriptSyntaxCoverageTest : ParsingTestCase("", "as", AngelscriptParserDefinition()) {
    override fun getTestDataPath() = ""

    fun testLocalFunctionsKeepNamesReferencesAndStructure() {
        val text = """
            // 中文 😀
            local int TestLocalSyntax() { return 6; }
            namespace Helpers {
                local TArray<int> Values(int Count = 2) { return TArray<int>(); }
                local int Forward();
                local int& Reference(int& Value) { return Value; }
            }
            UFUNCTION() local int Annotated() { return 1; }
            int Use() { return TestLocalSyntax(); }
        """.trimIndent()
        val file = parseFile("LocalFunctions", text)
        assertNoErrors(file)
        val functions = PsiTreeUtil.findChildrenOfType(file, AngelscriptFunctionDecl::class.java)
        assertEquals(listOf("TestLocalSyntax", "Values", "Forward", "Reference", "Annotated", "Use"), functions.map { it.name })
        val declaration = functions.first()
        assertEquals(text.indexOf("TestLocalSyntax"), declaration.nameIdentifier!!.textOffset)
        val reference = file.findReferenceAt(text.lastIndexOf("TestLocalSyntax"))
        assertNotNull(reference)
        assertEquals(declaration, reference!!.resolve())
        assertEquals(listOf("TestLocalSyntax", "Helpers", "Annotated", "Use"),
            AngelscriptStructureViewElement(file).children.map { it.presentation.presentableText })
    }

    fun testLocalKeywordBoundariesAndHighlighting() {
        val lexer = AngelscriptLexerAdapter()
        lexer.start("local localValue Local local_1")
        val tokens = mutableListOf<Pair<String, com.intellij.psi.tree.IElementType>>()
        while (lexer.tokenType != null) {
            if (lexer.tokenType != com.intellij.psi.TokenType.WHITE_SPACE)
                tokens.add(lexer.tokenText to lexer.tokenType!!)
            lexer.advance()
        }
        assertEquals(listOf("local", "localValue", "Local", "local_1"), tokens.map { it.first })
        assertEquals(AngelscriptTypes.LOCAL_KW, tokens.first().second)
        assertTrue(tokens.drop(1).all { it.second == AngelscriptTypes.IDENTIFIER })
        assertTrue(AngelscriptSyntaxHighlighter().getTokenHighlights(AngelscriptTypes.LOCAL_KW).isNotEmpty())
    }

    fun testLocalIsRestrictedToGlobalFunctionsAndMalformedEditsRecover() {
        for (source in listOf(
            "local int Value;",
            "class Example { local int Method() { return 1; } }",
            "void Use() { local int Nested() { return 1; } }",
            "local int Broken( ;\nlocal int Following() { return 2; }"
        )) {
            val file = parseFile("InvalidLocal", source)
            assertEquals(source, file.text)
            assertFalse(source, PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java).isEmpty())
            if (source.contains("Following")) {
                assertTrue(PsiTreeUtil.findChildrenOfType(file, AngelscriptFunctionDecl::class.java)
                    .any { it.name == "Following" })
            }
        }
    }

    fun testLocalFunctionRemainsEditableAtEveryTruncation() {
        val source = "namespace Helpers { local int Count(int Value = 2) { return Value; } }"
        for (length in 0..source.length) {
            val text = source.take(length)
            val file = parseFile("LocalEdit", text)
            assertEquals("Truncated at $length", text, file.text)
            assertTrue(PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
                .all { it.textRange.endOffset <= text.length })
        }
    }

    fun testPortableSyntaxFixturesHaveNoErrors() {
        assertScripts(Path.of("src/test/resources/syntax-coverage"))
    }

    fun testConditionalDirectivesKeepBranchesAndFollowingDeclarations() {
        val diskSource = "#ifdef PROBE\r\nint Enabled;\r\n#ifndef OTHER\r\nint Nested;\r\n#endif\r\n#else\r\nint Disabled;\r\n#endif\r\nvoid Following() {}"
        val source = diskSource.replace("\r\n", "\n") // IDE documents use LF; the lexer also accepts disk CRLF.
        val file = parseFile("ConditionalProbes", source)
        assertNoErrors(file)
        assertEquals(source, file.text)
        assertEquals(listOf("Following"),
            PsiTreeUtil.findChildrenOfType(file, AngelscriptFunctionDecl::class.java).map { it.name })
        val lexer = AngelscriptLexerAdapter()
        lexer.start(diskSource)
        val directives = mutableListOf<String>()
        while (lexer.tokenType != null) {
            if (lexer.tokenType == AngelscriptTypes.PP_IF) directives.add(lexer.tokenText)
            lexer.advance()
        }
        assertEquals(listOf("#ifdef PROBE", "#ifndef OTHER"), directives)
        for (text in listOf("#ifdefOther PROBE", "#ifndefOther PROBE")) {
            lexer.start(text)
            assertEquals(AngelscriptTypes.SHA, lexer.tokenType)
        }
        val incomplete = parseFile("MissingEndif", "#ifdef PROBE\nvoid Inside() {}")
        assertFalse(PsiTreeUtil.findChildrenOfType(incomplete, PsiErrorElement::class.java).isEmpty())
    }

    fun testEscapedQuotesKeepStringBoundariesAndLexerRestart() {
        for (quote in listOf('"', '\'')) {
            for (slashes in 1..4) {
                val body = "A" + "\\".repeat(slashes) + if (slashes % 2 == 1) "$quote B" else ""
                val source = "$quote$body$quote; After();"
                val lexer = AngelscriptLexerAdapter()
                lexer.start(source)
                val tokens = mutableListOf<Triple<Int, Int, Int>>()
                while (lexer.tokenType != null) {
                    tokens.add(Triple(lexer.tokenStart, lexer.tokenEnd, lexer.state))
                    if (lexer.tokenStart < body.length + 2) {
                        assertEquals(source, AngelscriptTypes.STRING_LITERAL, lexer.tokenType)
                    }
                    if (lexer.tokenText == "After") assertEquals(AngelscriptTypes.IDENTIFIER, lexer.tokenType)
                    lexer.advance()
                }
                assertEquals(listOf(0 to 1, 1 to body.length + 1, body.length + 1 to body.length + 2),
                    tokens.take(3).map { it.first to it.second })
                // Restarting at every token boundary must preserve the same offsets and states.
                for ((index, token) in tokens.withIndex()) {
                    lexer.start(source, token.first, source.length, token.third)
                    for (expected in tokens.drop(index)) {
                        assertEquals(expected, Triple(lexer.tokenStart, lexer.tokenEnd, lexer.state))
                        lexer.advance()
                    }
                    assertNull(lexer.tokenType)
                }
                assertNoErrors(parseFile("EscapedQuotes", "void Probe() { Print($quote$body$quote); After(); }"))
            }
        }
    }

    // Opt-in audit of real project scripts; portable regressions above run on every machine.
    fun testOptionalProjectScriptCorpusHasNoErrors() {
        val root = System.getProperty("angelscript.test.scriptRoot") ?: return
        assertScripts(Path.of(root))
    }

    private fun assertScripts(root: Path) {
        val failures = mutableListOf<String>()
        Files.walk(root).use { paths ->
            val scripts = paths.filter { Files.isRegularFile(it) && it.toString().endsWith(".as") }.sorted().toList()
            assertFalse("No .as scripts in $root", scripts.isEmpty())
            for (path in scripts) {
                val source = Files.readString(path).replace("\r\n", "\n")
                val file = parseFile(path.fileName.toString(), source)
                assertEquals(path.toString(), source, file.text)
                val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
                if (errors.isNotEmpty()) {
                    val report = Path.of("build/syntax-audit", path.fileName.toString() + ".tree.txt")
                    Files.createDirectories(report.parent)
                    Files.writeString(report, com.intellij.psi.impl.DebugUtil.psiToString(file, true))
                }
                for (error in errors) {
                    val line = source.take(error.textOffset).count { it == '\n' } + 1
                    failures.add("${path.fileName}:$line ${error.errorDescription}")
                }
            }
            val report = Path.of("build/syntax-audit", root.fileName.toString() + ".summary.txt")
            Files.createDirectories(report.parent)
            Files.writeString(report, "Root: ${root.toAbsolutePath()}\nScripts: ${scripts.size}\n" +
                "Syntax errors: ${failures.size}\n" + failures.joinToString("\n"))
        }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }

    private fun assertNoErrors(file: com.intellij.psi.PsiFile) {
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
        assertTrue(errors.joinToString { "${it.errorDescription} at ${it.textOffset}" } + "\n" +
            com.intellij.psi.impl.DebugUtil.psiToString(file, true), errors.isEmpty())
    }
}
