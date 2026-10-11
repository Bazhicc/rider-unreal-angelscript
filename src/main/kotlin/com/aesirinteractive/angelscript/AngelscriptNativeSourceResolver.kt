package com.aesirinteractive.angelscript

/** Shares the complete source lookup between Rider's word index and source-backed regression tests. */
internal class AngelscriptNativeSourceResolver<F>(
    private val headers: (Set<String>) -> List<F>,
    private val filesWithWord: (String) -> List<F>,
    private val text: (F) -> String,
    private val checkCanceled: () -> Unit = {},
    private val include: (F, String) -> List<F> = { _, path -> headers(setOf(path.substringAfterLast('/'))) },
) {
    data class Target<F>(val file: F, val offset: Int, val label: String)
    private data class LocatedBinding<F>(val file: F, val binding: AngelscriptNativeBindings.Binding, val namespace: () -> String)

    fun resolve(symbol: AngelscriptCppSymbol): List<Target<F>> {
        val includes = AngelscriptCppIncludes(text, include, checkCanceled)
        val views = mutableMapOf<F, List<AngelscriptCppIncludes.View<F>>>()
        fun sources(file: F) = views.getOrPut(file) { includes.views(file) }
        val parsedBindings = mutableMapOf<F, List<LocatedBinding<F>>>()
        fun sourceBindings(file: F) = parsedBindings.getOrPut(file) {
            sources(file).flatMap { view ->
                AngelscriptNativeBindings.parsePrepared(view.source).map {
                    LocatedBinding(view.file, it.copy(offset = view.originalOffset(it.offset))) {
                        AngelscriptCppDeclarations.namespaceAt(view.source, it.offset)
                    }
                }
            }
        }
        val registrationFiles by lazy {
            listOf("ValueClass", "ReferenceClass", "ExistingClass", "Enum", "BindGlobalFunction", "BindGlobalProperty")
                .flatMap(filesWithWord).toSet()
        }
        fun ownerBindings(owner: String): List<AngelscriptNativeBindings.Binding> {
            val candidates = filesWithWord(owner)
            fun find(files: List<F>) = files.flatMap { file ->
                checkCanceled()
                sourceBindings(file).map { it.binding }.filter { it.owner == owner }
            }
            return find(candidates.filter { it in registrationFiles }).ifEmpty {
                find(candidates.filter { it !in registrationFiles })
            }
        }
        symbol.receiver?.let { receiver ->
            return AngelscriptNativeReceiverTypes.resolve(receiver, ::ownerBindings).flatMap { type ->
                checkCanceled()
                resolve(symbol.copy(typeName = type, receiver = null))
            }.distinctBy { it.file to it.offset }
        }
        fun directHeaders(file: F) = sources(file).firstOrNull()?.directIncludes.orEmpty()
        fun declarations(query: AngelscriptCppSymbol, ownerNamespace: String? = null): List<Target<F>> {
            val namespace = query.typeName.takeIf { it.startsWith("__") }?.removePrefix("__")
            if (namespace != null) return filesWithWord(query.cppName).flatMap { file ->
                checkCanceled()
                sources(file).flatMap { view ->
                    AngelscriptCppDeclarations.namespaceOffsets(view.source, namespace, setOf(query.cppName), query.kind).map {
                        Target(view.file, view.originalOffset(it), AngelscriptCppDeclarations.signature(view.source, it, namespace, query.kind))
                    }
                }
            }
            data class Declarations(val aliases: List<AngelscriptCppDeclarations.TypeReference>,
                                    val sources: List<AngelscriptCppIncludes.View<F>>)
            fun find(qualifiedOwner: String): Declarations {
                checkCanceled()
                val owner = qualifiedOwner.substringAfterLast("::")
                val lookupNamespace = qualifiedOwner.substringBeforeLast("::", "")
                val names = owner.indices.filter { it <= 1 || owner[it].isUpperCase() && owner[it - 1].isLowerCase() }
                    .map { owner.substring(it) }.flatMap { name ->
                    listOf("$name.h", "$name.hpp", "$name.hh", "$name.hxx", "Unreal$name.h")
                }.toSet()
                val candidates = headers(names)
                // A compatibility header that forwards to another candidate is not the public declaration.
                val forwarded = candidates.flatMap(::directHeaders).filter { it in candidates }.toSet()
                val preferred = candidates.filter { it in forwarded }.ifEmpty { candidates }
                // Inspecting includes also reads forwarding headers. Do not reintroduce those
                // compatibility definitions through the previously loaded source views.
                val known = views.values.flatten().filter { it.file !in candidates || it.file in preferred }.distinct()
                val preferredSources = (known + preferred.flatMap(::sources)).distinct()
                // Public headers often define a type through a directly included alias header.
                // Follow that alias before accepting same-named reflection/tool definitions elsewhere.
                val publicAliases = (preferred.flatMap(::sources).map { it.file } + preferred.flatMap(::directHeaders))
                    .distinct().flatMap { file -> sources(file).flatMap { AngelscriptCppDeclarations.aliasReferences(it.source, owner, lookupNamespace) } }
                if (publicAliases.isNotEmpty()) return Declarations(publicAliases.distinct(), emptyList())
                fun defines(view: AngelscriptCppIncludes.View<F>) =
                    AngelscriptCppDeclarations.types(view.source, setOf(owner), view::expand, lookupNamespace).isNotEmpty()
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
                        AngelscriptCppDeclarations.aliasReferences(view.source, owner, lookupNamespace)
                    }
                    if (aliases.isNotEmpty()) return Declarations(aliases.distinct(), emptyList())
                }
                return Declarations(emptyList(), files.filter(::defines))
            }
            fun lookup(reference: AngelscriptCppDeclarations.TypeReference, trail: Set<String>): List<Target<F>> {
                checkCanceled()
                if (trail.size >= 64) return emptyList()
                for (qualifiedOwner in reference.candidates()) {
                    // A declaration found in an inner scope hides outer names even if its alias
                    // is cyclic or it does not have the requested member.
                    if (qualifiedOwner in trail) return emptyList()
                    val declarations = find(qualifiedOwner)
                    val path = trail + qualifiedOwner
                    if (declarations.aliases.isNotEmpty())
                        return declarations.aliases.flatMap { lookup(it, path) }
                    if (declarations.sources.isEmpty()) continue
                    val owner = qualifiedOwner.substringAfterLast("::")
                    val scope = qualifiedOwner.substringBeforeLast("::", "")
                    val targets = declarations.sources.flatMap { view ->
                        checkCanceled()
                        AngelscriptCppDeclarations.offsets(view.source, setOf(owner), query, view::expand, scope).map {
                            Target(view.file, view.originalOffset(it), if (query.kind in setOf("type", "enum")) qualifiedOwner
                                else AngelscriptCppDeclarations.signature(view.source, it, qualifiedOwner, query.kind))
                        }
                    }
                    if (targets.isNotEmpty()) return targets
                    // Resolve every base branch independently. An alias on one branch must not
                    // disappear just because a different branch has an immediate member match.
                    return declarations.sources.flatMap { view ->
                        AngelscriptCppDeclarations.types(view.source, setOf(owner), view::expand, scope).flatMap { declaration ->
                            declaration.bases.flatMap { base -> lookup(AngelscriptCppDeclarations.TypeReference(base, declaration.scope), path) }
                        }
                    }
                }
                return emptyList()
            }
            return lookup(AngelscriptCppDeclarations.TypeReference(query.typeName, ownerNamespace.orEmpty()), emptySet())
                .distinctBy { it.file to it.offset }
        }

        // A semantic UE alias is more precise than a similarly named script registration.
        // Type names refer to the public C++ type, not a binding's erased storage type (FScriptMap/FScriptSet).
        if (symbol.kind in setOf("type", "enum", "namespace") || symbol.cppName != symbol.symbolName)
            declarations(symbol).takeIf { it.isNotEmpty() }?.let { return it }
        val owner = AngelscriptCppDeclarations.baseName(symbol.typeName.removePrefix("__"))
        val candidates = filesWithWord(if (owner.isEmpty()) symbol.symbolName else owner.substringAfterLast("::"))
        // Intersect indexes before loading source: common owners such as TArray/TMap occur in thousands of files.
        fun registered(files: List<F>) = files.flatMap { file ->
            checkCanceled()
            sourceBindings(file).filter { located ->
                val it = located.binding
                it.owner == owner && (it.name == symbol.symbolName || it.name == symbol.cppName) &&
                    (it.kind == symbol.kind || symbol.kind in setOf("type", "enum") && it.kind == "type")
            }
        }
        fun bindingTargets(registered: List<LocatedBinding<F>>) = registered.flatMap { (file, binding, bindingNamespace) ->
            val namespace = bindingNamespace()
            val implementation = if (binding.cppName == null) emptyList() else declarations(symbol.copy(
                typeName = binding.cppOwner ?: "__", symbolName = binding.cppName, cppName = binding.cppName,
                kind = if (symbol.kind == "enum") "type" else symbol.kind), namespace)
            implementation.ifEmpty {
                val label = binding.signature.replaceFirst(Regex("\\b${Regex.escape(binding.name)}\\b"),
                    binding.owner.takeIf { it.isNotEmpty() }?.let { "$it::${binding.name}" } ?: binding.name)
                listOf(Target(file, binding.offset, label))
            }
        }.distinctBy { it.file to it.offset }
        registered(candidates.filter { it in registrationFiles }).takeIf { it.isNotEmpty() }?.let { return bindingTargets(it) }
        declarations(symbol).takeIf { it.isNotEmpty() }?.let { return it }
        // A macro-only registration site may contain neither ValueClass nor Method in its raw text.
        // Expand only already indexed owner candidates, after the direct paths have failed.
        return bindingTargets(registered(candidates.filter { it !in registrationFiles }))
    }
}
