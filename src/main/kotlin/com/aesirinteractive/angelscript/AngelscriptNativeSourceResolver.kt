package com.aesirinteractive.angelscript

/** Shares the complete source lookup between Rider's word index and source-backed regression tests. */
internal class AngelscriptNativeSourceResolver<F>(
    private val headers: (Set<String>) -> List<F>,
    private val filesWithWord: (String) -> List<F>,
    private val text: (F) -> String,
    private val bindings: (F) -> List<AngelscriptNativeBindings.Binding>,
    private val checkCanceled: () -> Unit = {},
    private val include: (F, String) -> List<F> = { _, path -> headers(setOf(path.substringAfterLast('/'))) },
) {
    data class Target<F>(val file: F, val offset: Int, val label: String)

    fun resolve(symbol: AngelscriptCppSymbol): List<Target<F>> {
        val registrationFiles by lazy {
            listOf("ValueClass", "ReferenceClass", "ExistingClass", "Enum", "BindGlobalFunction", "BindGlobalProperty")
                .flatMap(filesWithWord).toSet()
        }
        fun ownerBindings(owner: String) = filesWithWord(owner).filter { it in registrationFiles }.flatMap { file ->
            checkCanceled()
            bindings(file).filter { it.owner == owner }
        }
        symbol.receiver?.let { receiver ->
            return AngelscriptNativeReceiverTypes.resolve(receiver, ::ownerBindings).flatMap { type ->
                checkCanceled()
                resolve(symbol.copy(typeName = type, receiver = null))
            }.distinctBy { it.file to it.offset }
        }
        val includes = AngelscriptCppIncludes(text, include, checkCanceled)
        val views = mutableMapOf<F, List<AngelscriptCppIncludes.View<F>>>()
        fun sources(file: F) = views.getOrPut(file) { includes.views(file) }
        val quotedIncludes = Regex("""(?m)^\s*#\s*include\s+"([^"]+)"""")
        fun directHeaders(file: F) = quotedIncludes.findAll(text(file)).flatMap {
            checkCanceled()
            include(file, it.groupValues[1])
        }.toList()
        fun declarations(query: AngelscriptCppSymbol, ownerNamespace: String? = null): List<Target<F>> {
            val namespace = query.typeName.takeIf { it.startsWith("__") }?.removePrefix("__")
            if (namespace != null) return filesWithWord(query.cppName).flatMap { file ->
                checkCanceled()
                AngelscriptCppDeclarations.namespaceOffsets(text(file), namespace, setOf(query.cppName), query.kind).map {
                    Target(file, it, AngelscriptCppDeclarations.signature(text(file), it, namespace, query.kind))
                }
            }
            var owners = setOf(AngelscriptCppDeclarations.baseName(query.typeName))
            val visited = mutableSetOf<String>()
            while (owners.isNotEmpty()) {
                val next = linkedSetOf<String>()
                val targets = owners.filter { visited.add(it) }.flatMap ownerLookup@{ qualifiedOwner ->
                    checkCanceled()
                    val owner = qualifiedOwner.substringAfterLast("::")
                    val lookupNamespace = if ("::" in qualifiedOwner) qualifiedOwner.substringBeforeLast("::").removePrefix("::")
                        else ownerNamespace
                    val names = owner.indices.filter { it <= 1 || owner[it].isUpperCase() && owner[it - 1].isLowerCase() }
                        .map { owner.substring(it) }.flatMap { name ->
                        listOf("$name.h", "$name.hpp", "$name.hh", "$name.hxx")
                    }.toSet()
                    val candidates = headers(names)
                    // A compatibility header that forwards to another candidate is not the public declaration.
                    val forwarded = candidates.flatMap { file ->
                        Regex("""(?m)^\s*#\s*include\s+"([^"]+)"""").findAll(text(file))
                            .map { it.groupValues[1] }.filter { it.substringAfterLast('/') in names }
                            .flatMap { include(file, it) }.toList()
                    }.filter { it in candidates }.toSet()
                    val preferred = candidates.filter { it in forwarded }.ifEmpty { candidates }
                    val known = views.values.flatten().distinct()
                    val preferredSources = (known + preferred.flatMap(::sources)).distinct()
                    // Public headers often define a type through a directly included alias header.
                    // Follow that alias before accepting same-named reflection/tool definitions elsewhere.
                    val publicAliases = (preferred.flatMap(::sources).map { it.file } + preferred.flatMap(::directHeaders))
                        .distinct().flatMap { file -> AngelscriptCppDeclarations.aliases(text(file), owner, lookupNamespace) }
                    if (publicAliases.isNotEmpty()) {
                        next.addAll(publicAliases)
                        return@ownerLookup emptyList()
                    }
                    fun defines(view: AngelscriptCppIncludes.View<F>) =
                        AngelscriptCppDeclarations.types(text(view.file), setOf(owner), view::expand, lookupNamespace).isNotEmpty()
                    val definitions = preferredSources.filter(::defines)
                    // An exact definition in a conventional header avoids searching every use of TMap/Add/etc.
                    // Define-injected public headers take precedence over standalone compatibility definitions.
                    val files = if (definitions.isNotEmpty()) definitions.filter { it.injected }.ifEmpty { definitions }
                        else (preferredSources + filesWithWord(owner).flatMap(::sources)).distinct()
                    // Older headers may include their alias indirectly. Read indexed aliases before
                    // accepting unrelated fallback definitions, without expanding the include graph.
                    if (definitions.isEmpty()) {
                        val aliases = files.flatMap { view ->
                            checkCanceled()
                            AngelscriptCppDeclarations.aliases(text(view.file), owner, lookupNamespace ?: "")
                        }
                        if (aliases.isNotEmpty()) {
                            next.addAll(aliases)
                            return@ownerLookup emptyList()
                        }
                    }
                    files.flatMap { view ->
                        checkCanceled()
                        val file = view.file
                        val source = text(file)
                        next.addAll(AngelscriptCppDeclarations.aliases(source, owner, lookupNamespace))
                        if (query.kind !in setOf("type", "enum"))
                            next.addAll(AngelscriptCppDeclarations.types(source, setOf(owner), view::expand, lookupNamespace).flatMap { it.bases })
                        AngelscriptCppDeclarations.offsets(source, setOf(owner), query, view::expand, lookupNamespace).map {
                            Target(file, it, if (query.kind in setOf("type", "enum")) owner
                                else AngelscriptCppDeclarations.signature(source, it,
                                    lookupNamespace?.takeIf(String::isNotEmpty)?.let { "$it::$owner" } ?: owner, query.kind))
                        }
                    }
                }
                if (targets.isNotEmpty()) return targets.distinctBy { it.file to it.offset }
                owners = next - visited
            }
            return emptyList()
        }

        // A semantic UE alias is more precise than a similarly named script registration.
        // Type names refer to the public C++ type, not a binding's erased storage type (FScriptMap/FScriptSet).
        if (symbol.kind in setOf("type", "enum", "namespace") || symbol.cppName != symbol.symbolName)
            declarations(symbol).takeIf { it.isNotEmpty() }?.let { return it }
        val owner = AngelscriptCppDeclarations.baseName(symbol.typeName.removePrefix("__"))
        val candidates = filesWithWord(if (owner.isEmpty()) symbol.symbolName else owner.substringAfterLast("::"))
        // Intersect indexes before loading source: common owners such as TArray/TMap occur in thousands of files.
        val registered = candidates.filter { it in registrationFiles }.flatMap { file ->
            checkCanceled()
            bindings(file).filter {
                it.owner == owner && (it.name == symbol.symbolName || it.name == symbol.cppName) &&
                    (it.kind == symbol.kind || symbol.kind in setOf("type", "enum") && it.kind == "type")
            }.map { file to it }
        }
        if (registered.isNotEmpty()) return registered.flatMap { (file, binding) ->
            val namespace = binding.cppOwner?.let { owner ->
                if ("::" in owner) owner.substringBeforeLast("::").removePrefix("::")
                else AngelscriptCppDeclarations.namespaceAt(text(file), binding.offset)
            }
            val implementation = if (binding.cppName == null) emptyList() else declarations(symbol.copy(
                typeName = binding.cppOwner ?: "__", symbolName = binding.cppName, cppName = binding.cppName,
                kind = if (symbol.kind == "enum") "type" else symbol.kind), namespace)
            implementation.ifEmpty {
                val label = binding.signature.replaceFirst(Regex("\\b${Regex.escape(binding.name)}\\b"),
                    binding.owner.takeIf { it.isNotEmpty() }?.let { "$it::${binding.name}" } ?: binding.name)
                listOf(Target(file, binding.offset, label))
            }
        }.distinctBy { it.file to it.offset }
        return declarations(symbol)
    }
}
