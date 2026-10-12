package com.aesirinteractive.angelscript

/** One generation of indexed native sources. Replacing it also discards negative lookups. */
internal class AngelscriptNativeIndex<F>(
    headers: (Set<String>) -> List<F>,
    words: (String) -> List<F>,
    private val text: (F) -> String,
    private val checkCanceled: () -> Unit = {},
    include: (F, String) -> List<F>,
    private val fileName: (F) -> String = { it.toString().replace('\\', '/').substringAfterLast('/') },
) {
    private val headerFiles = AngelscriptNativeMemo<Set<String>, List<F>>(128)
    private val wordFiles = AngelscriptNativeMemo<String, List<F>>(256)
    private val sources = AngelscriptNativeMemo<F, List<AngelscriptCppIncludes.View<F>>>(128)
    private val results = AngelscriptNativeMemo<AngelscriptCppSymbol, List<AngelscriptNativeSourceResolver.Target<F>>>(512)
    private val includes = AngelscriptCppIncludes(text, include, checkCanceled)
    private val bindings = AngelscriptNativeMemo<F, List<AngelscriptNativeSourceResolver.LocatedBinding<F>>>(256)
    private val wordLookup: (String) -> List<F> = { word -> wordFiles.getOrPut(word) { words(word) } }
    private fun views(file: F) = sources.getOrPut(file) { includes.views(file) }
    private fun bindings(file: F) = bindings.getOrPut(file) {
        views(file).flatMap { view -> AngelscriptNativeBindings.parsePrepared(view.source).map { binding ->
            AngelscriptNativeSourceResolver.LocatedBinding(view.file, binding.copy(offset = view.originalOffset(binding.offset))) {
                AngelscriptCppDeclarations.namespaceAt(view.source, binding.offset)
            }
        } }
    }
    private val resolver = AngelscriptNativeSourceResolver<F>(
        { names -> headerFiles.getOrPut(names) { headers(names) } },
        wordLookup, text, checkCanceled, include, ::views, ::bindings,
    )

    fun resolve(symbol: AngelscriptCppSymbol): List<AngelscriptNativeSourceResolver.Target<F>> {
        val key = symbol.copy(alternatives = null)
        return results.getOrPut(key) { resolver.resolve(key) }
    }

    fun matches(symbol: AngelscriptCppSymbol, declaration: (F, Int) -> Boolean): Boolean =
        (listOf(symbol) + symbol.alternatives.orEmpty()).any { candidate -> resolve(candidate).any { declaration(it.file, it.offset) } }

    private val aliasNames = AngelscriptNativeMemo<String, List<String>>(256)
    fun scriptNames(cppName: String): List<String> = aliasNames.getOrPut(cppName) {
        val candidates = wordLookup(cppName).toSet()
        val registrations = registrationWords.flatMap(wordLookup).toSet()
        val files = candidates.filterTo(mutableSetOf()) { it in registrations }
        // Intersect the word indexes before reading any includer. A global include-parent walk
        // reaches most of Unreal through common headers even when only one native name is wanted.
        // A caller whose registration macro comes from a header still contains the native argument.
        for (header in registrations) {
            checkCanceled()
            val name = fileName(header)
            if (name.substringAfterLast('.') !in setOf("h", "hpp", "hh", "hxx", "inl", "inc")) continue
            for (caller in wordLookup(name.substringBefore('.'))) {
                checkCanceled()
                if (caller in candidates && caller !in files && text(caller).contains(name)) files.add(caller)
            }
        }
        (files.flatMap(::bindings).map { it.binding }
            .filter { it.cppName == cppName }.map { it.name } + cppName).distinct()
    }

    companion object {
        val registrationWords = listOf("ValueClass", "ReferenceClass", "ExistingClass", "Enum", "BindGlobalFunction", "BindGlobalProperty",
            "FAngelscriptBinds", "AngelscriptBinds")
    }
}

/** Bound memory and keep parsing outside the monitor, so concurrent editor reads never wait on a parse lock. */
internal class AngelscriptNativeMemo<K, V : Any>(private val limit: Int) {
    private val values = LinkedHashMap<K, V>(16, 0.75f, true)
    fun getOrPut(key: K, load: () -> V): V {
        synchronized(values) { values[key]?.let { return it } }
        val value = load()
        synchronized(values) {
            values[key]?.let { return it }
            values[key] = value
            if (values.size > limit) values.remove(values.keys.first())
        }
        return value
    }
}
