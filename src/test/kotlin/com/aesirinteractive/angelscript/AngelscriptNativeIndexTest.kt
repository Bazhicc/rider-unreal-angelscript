package com.aesirinteractive.angelscript

import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assume.assumeNotNull

class AngelscriptNativeIndexTest {
    private class Fixture(val files: MutableMap<String, String>) {
        var reads = 0
        var words = 0
        var headers = 0
        fun index() = AngelscriptNativeIndex<String>(
            { names -> ++headers; files.keys.filter { it.substringAfterLast('/') in names } },
            { word -> ++words; files.keys.filter { Regex("\\b${Regex.escape(word)}\\b").containsMatchIn(files.getValue(it)) } },
            { file -> ++reads; files.getValue(file) }, include = { _, path -> files.keys.filter { it == path || it.endsWith("/$path") } })
    }
    private fun symbol(name: String) = AngelscriptCppSymbol("FProbe", name, name, "function", false)

    @Test fun repeatedNavigationReusesIndexesSourceViewsAndExactTargets() {
        val fixture = Fixture(mutableMapOf("Probe.h" to "struct FProbe { void First(); void Second(); };"))
        val index = fixture.index()
        val first = index.resolve(symbol("First"))
        assertEquals("Probe.h", first.single().file)
        val cold = Triple(fixture.reads, fixture.words, fixture.headers)
        repeat(50) { assertEquals(first, index.resolve(symbol("First"))) }
        assertEquals("Warm navigation must not read sources or query the platform index", cold,
            Triple(fixture.reads, fixture.words, fixture.headers))
        index.resolve(symbol("Second"))
        assertEquals("Another member must reuse the prepared owner source", cold.first, fixture.reads)
        assertEquals(cold.second, fixture.words)
    }

    @Test fun freshGenerationDiscoversPreviouslyMissingFunctionsAndChangedIncludeMacros() {
        val fixture = Fixture(mutableMapOf("Probe.h" to "#define OWNER FProbe\n#include \"Probe.inc\"",
            "Probe.inc" to "struct OWNER { void First(); };"))
        val old = fixture.index()
        assertTrue(old.resolve(symbol("Later")).isEmpty())
        fixture.files["Probe.inc"] = "struct OWNER { void Later(); };"
        val fresh = fixture.index()
        assertFalse(fresh.resolve(symbol("Later")).isEmpty())
        assertTrue(fresh.resolve(symbol("First")).isEmpty())
        assertEquals("An in-flight old generation cannot publish results into the replacement", 1,
            fresh.resolve(symbol("Later")).size)
    }

    @Test fun bindingAliasesAndOverloadsUseTheirRegisteredNativePrototype() {
        val source = "struct FProbe { void Native(int Value); void Native(float Value); };"
        val binding = """
            auto B = FAngelscriptBinds::ValueClass<FProbe>("FProbe");
            B.Method("void Friendly(int Value)", METHODPR(void, FProbe, Native, (int)));
            B.Method("void Friendly(float Value)", METHODPR(void, FProbe, Native, (float)));
        """.trimIndent()
        val fixture = Fixture(mutableMapOf("Probe.h" to source, "Bind_Probe.cpp" to binding))
        val index = fixture.index()
        assertTrue("Reverse lookup must discover AS aliases", "Friendly" in index.scriptNames("Native"))
        for ((type, parameter) in listOf("int32" to "int", "float32" to "float")) {
            val query = symbol("Friendly").copy(parameterTypes = listOf(type))
            val target = index.resolve(query).single()
            assertEquals(source.indexOf("Native($parameter"), target.offset)
            assertTrue(target.label.contains("Native($parameter"))
            assertTrue(index.matches(query) { file, offset -> file == "Probe.h" && offset == target.offset })
            assertFalse("Another file's same-named function is not a usage", index.matches(query) { file, _ -> file == "Other.h" })
            val other = if (parameter == "int") "float" else "int"
            assertFalse("Another overload is not the selected declaration", index.matches(query) { _, offset -> offset == source.indexOf("Native($other") })
        }
    }

    @Test fun boundedMemoEvictsLeastRecentlyUsedEntriesAndDoesNotCacheFailures() {
        val cache = AngelscriptNativeMemo<String, String>(2)
        cache.getOrPut("one") { "1" }; cache.getOrPut("two") { "2" }
        assertEquals("1", cache.getOrPut("one") { error("must be cached") })
        cache.getOrPut("three") { "3" }
        assertEquals("reloaded", cache.getOrPut("two") { "reloaded" })
        try { cache.getOrPut("failed") { error("cancelled") }; fail() } catch (_: IllegalStateException) { }
        assertEquals("retry", cache.getOrPut("failed") { "retry" })
    }

    @Test fun reverseAliasLookupDoesNotParseUnrelatedRegisteredApis() {
        val files = mutableMapOf("Probe.h" to "struct FProbe { void Native(); };",
            "Bind_Probe.cpp" to """auto B = FAngelscriptBinds::ValueClass<FProbe>("FProbe");
                B.Method("void Friendly()", METHOD(FProbe, Native));""")
        repeat(200) { files["Bind_Other$it.cpp"] = "FAngelscriptBinds::ValueClass<FOther$it>(\"FOther$it\");" }
        val reads = mutableSetOf<String>()
        val index = AngelscriptNativeIndex<String>({ names -> files.keys.filter { it in names } },
            { word -> files.keys.filter { Regex("\\b${Regex.escape(word)}\\b").containsMatchIn(files.getValue(it)) } },
            { file -> reads.add(file); files.getValue(file) }, include = { _, _ -> emptyList() })
        assertTrue("Friendly" in index.scriptNames("Native"))
        assertEquals(setOf("Bind_Probe.cpp"), reads)
        val before = reads.toSet()
        assertTrue("Friendly" in index.scriptNames("Native"))
        assertEquals(before, reads)
    }

    @Test fun reverseAliasesDiscoverBindingMacroCallersThroughIncludes() {
        val fixture = Fixture(mutableMapOf("Registration.h" to """
            #define REGISTER(Name) auto B = FAngelscriptBinds::ValueClass<FProbe>("FProbe"); B.Method("void Friendly()", METHOD(FProbe, Name));
        """.trimIndent(), "Use.cpp" to "#include \"Registration.h\"\nREGISTER(Native)"))
        assertTrue("Friendly" in fixture.index().scriptNames("Native"))
    }

    @Test fun reverseAliasesDoNotReadUnrelatedIncludersOrExpandTheirIncludeGraph() {
        val files = mutableMapOf("Registration.h" to """
            #define REGISTER(Name) auto B = FAngelscriptBinds::ValueClass<FProbe>("FProbe"); B.Method("void Friendly()", METHOD(FProbe, Name));
        """.trimIndent(), "Use.cpp" to "#include \"Registration.h\"\nREGISTER(Native)")
        repeat(200) {
            files["Other$it.h"] = "#include \"Registration.h\"\nstruct FOther$it {};"
            files["Other$it.cpp"] = "#include \"Other$it.h\"\nvoid Other$it() {}"
        }
        val reads = mutableSetOf<String>()
        val words = mutableSetOf<String>()
        val index = AngelscriptNativeIndex<String>({ names -> files.keys.filter { it in names } },
            { word -> words.add(word); files.keys.filter { Regex("\\b${Regex.escape(word)}\\b").containsMatchIn(files.getValue(it)) } },
            { file -> reads.add(file); files.getValue(file) },
            include = { _, path -> files.keys.filter { it == path } })
        assertTrue("Friendly" in index.scriptNames("Native"))
        assertEquals(setOf("Use.cpp", "Registration.h"), reads)
        assertFalse("Unrelated includes must not trigger recursive index searches", words.any { it.startsWith("Other") })
    }

    @Test fun actualUeVectorNavigationRecordsColdAndWarmCosts() {
        val path = System.getProperty("angelscript.test.engineSource")
        assumeNotNull(path)
        val root = Path.of(path!!)
        val fixture = Fixture(mutableMapOf())
        for (name in listOf("Vector.h", "MathFwd.h")) {
            val file = root.resolve("Runtime/Core/Public/Math/$name")
            fixture.files[file.toString().replace('\\', '/')] = Files.readString(file)
        }
        val index = fixture.index()
        val query = AngelscriptCppSymbol("FVector", "X", "X", "property", false)
        val begin = System.nanoTime()
        val cold = index.resolve(query)
        val coldMs = (System.nanoTime() - begin) / 1_000_000.0
        assertFalse(cold.isEmpty())
        val counters = Triple(fixture.reads, fixture.words, fixture.headers)
        val warmBegin = System.nanoTime()
        repeat(100) { assertEquals(cold, index.resolve(query)) }
        val warmMs = (System.nanoTime() - warmBegin) / 1_000_000.0 / 100
        assertEquals(counters, Triple(fixture.reads, fixture.words, fixture.headers))
        println("UE_NATIVE_CACHE coldMs=$coldMs warmMeanMs=$warmMs sourceReads=${fixture.reads} wordQueries=${fixture.words}")
    }
}
