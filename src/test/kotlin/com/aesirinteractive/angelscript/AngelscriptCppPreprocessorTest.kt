package com.aesirinteractive.angelscript

import org.junit.Assert.*
import org.junit.Test

class AngelscriptCppPreprocessorTest {
    @Test fun numericDefinitionsAndNestedBranchesSelectOnlyKnownActiveCode() {
        val source = """
            #define VERSION 0x20UL
            #undef DISABLED
            #if VERSION == 32 && !defined(DISABLED) && 010 == 8 && 0b10 == 2
            #if 0 || (3 * 2 != 6)
            Wrong
            #else
            Correct
            #endif
            #elif 1
            OtherWrong
            #endif
        """.trimIndent()
        val result = AngelscriptCppPreprocessor.process(source).source
        assertTrue(result.text.contains("Correct"))
        assertFalse(result.text.contains("Wrong"))
        assertEquals(source.indexOf("Correct"), result.originalOffset(result.text.indexOf("Correct")))
    }

    @Test fun unknownBranchesDoNotLeakConflictingDefinitionsAcrossTheirJoin() {
        val source = """
            #if EXTERNAL_BUILD_FLAG
            #define CONFLICT 1
            #define SHARED 7
            First
            #else
            #define CONFLICT 2
            #define SHARED 7
            Second
            #endif
        """.trimIndent()
        val result = AngelscriptCppPreprocessor.process(source)
        assertTrue(result.source.text.contains("First"))
        assertTrue(result.source.text.contains("Second"))
        assertFalse(result.definitions.containsKey("CONFLICT"))
        assertEquals("7", result.definitions["SHARED"]?.body)
    }

    @Test fun guardedDirectHeadersExportFunctionMacros() {
        val source = """
            // Header comment
            #pragma once
            #ifndef CUSTOM_HEADER_INCLUDED
            #define CUSTOM_HEADER_INCLUDED
            #define DECLARE(Type) struct Type {};
            #endif
        """.trimIndent()
        val definitions = AngelscriptCppPreprocessor.process(source, collectOnly = true).definitions
        val call = "DECLARE(FGuarded)"
        val result = AngelscriptCppPreprocessor.process(call, definitions).source
        assertEquals("struct FGuarded {};", result.text)
        assertEquals(call.indexOf("FGuarded"), result.originalOffset(result.text.indexOf("FGuarded")))
    }

    @Test fun directivesAndMacroCallsInsideCommentsAndStringsRemainTrivia() {
        val source = """
            /*
            #define DECLARE(Type) struct Type {};
            */
            DECLARE(FMissing)
            #define FIELD(Name) int Name;
            // FIELD(Comment)
            const char* Value = "FIELD(String)";
            FIELD(Actual)
        """.trimIndent()
        val result = AngelscriptCppPreprocessor.process(source).source
        assertTrue(result.text.contains("DECLARE(FMissing)"))
        assertTrue(result.text.contains("// FIELD(Comment)"))
        assertTrue(result.text.contains("\"FIELD(String)\""))
        assertTrue(result.text.contains("int Actual;"))
        assertEquals(source.indexOf("Actual"), result.originalOffset(result.text.indexOf("Actual")))
    }

    @Test fun recursiveAndVariadicMacrosStayBoundedAndPreserveArguments() {
        val source = """
            #define RECURSE(Name) RECURSE(Name)
            #define DECLARE(Name, ...) struct Name { __VA_ARGS__ };
            RECURSE(Stop)
            DECLARE(FEmpty)
            DECLARE(FFields, int X;, void Call();)
        """.trimIndent()
        val result = AngelscriptCppPreprocessor.process(source).source.text
        assertTrue(result.contains("RECURSE(Stop)"))
        assertTrue(result.contains("struct FEmpty {  };"))
        assertTrue(result.contains("struct FFields {  int X;, void Call(); };"))
    }

    @Test fun nestedArgumentsAreExpandedBeforeDisablingTheOuterMacro() {
        val source = "#define ID(Value)Value\n#define PASTE(Type) F##Type\nID(ID(struct PASTE(Nested) { int X; };))"
        val result = AngelscriptCppPreprocessor.process(source).source
        assertTrue(result.text.contains("struct FNested { int X; };"))
        assertEquals(source.indexOf("Nested)"), result.originalOffset(result.text.indexOf("FNested")))
        val deep = "#define ID(Value) Value\n" + "ID(".repeat(500) + "Stop" + ")".repeat(500)
        assertTrue(AngelscriptCppPreprocessor.process(deep).source.text.contains("Stop"))
    }

    @Test fun inactiveNativeBindingsCannotBecomeNavigationCandidates() {
        val source = """
            auto Type = FAngelscriptBinds::ExistingClass("FProbe");
            #if 0
            Type.Method("void Call()", &FInvalid::Call);
            #else
            Type.Method("void Call()", &FCurrent::Call);
            #endif
        """.trimIndent()
        val binding = AngelscriptNativeBindings.parse(source).single { it.kind == "function" }
        assertEquals("FCurrent", binding.cppOwner)
        assertEquals(source.indexOf("Call()", source.indexOf("#else")), binding.offset)
    }
}
