package com.aesirinteractive.angelscript

import org.junit.Assert.*
import org.junit.Assume.assumeNotNull
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

class AngelscriptNativeSourceResolverTest {
    private fun resolver(files: Map<String, String>, searchedWords: MutableList<String>? = null) = AngelscriptNativeSourceResolver<String>(
        { names -> files.keys.filter { it.substringAfterLast('/') in names } },
        { word -> searchedWords?.add(word); files.keys.filter { Regex("\\b${Regex.escape(word)}\\b").containsMatchIn(files.getValue(it)) } },
        files::getValue, include = { file, path ->
            val relative = file.substringBeforeLast('/', "").let { if (it.isEmpty()) path else "$it/$path" }
            if (relative in files) listOf(relative) else files.keys.filter { it == path || it.endsWith("/$path") }
        })

    private fun symbol(owner: String, name: String, kind: String = "function") =
        AngelscriptCppSymbol(owner, name, name, kind, false)

    @Test fun unrelatedNamespaceAndClassAliasesCannotHijackUnqualifiedTypes() {
        val files = mapOf(
            "Core/Bag.h" to "struct FBag { void Contains(); };",
            "Tool/Bag.h" to "namespace Tool { struct FBagG { void Contains(); }; using FBag = FBagG; }",
            "Nested/Bag.h" to "struct FOuter { struct FBagG {}; using FBag = FBagG; };")
        val lookup = resolver(files)
        for (owner in listOf("FBag", "::FBag")) {
            assertEquals("Core/Bag.h", lookup.resolve(symbol(owner, "FBag", "type")).single().file)
            assertEquals("Core/Bag.h", lookup.resolve(symbol(owner, "Contains")).single().file)
        }
        assertEquals("Tool/Bag.h", lookup.resolve(symbol("Tool::FBag", "FBag", "type")).single().file)
        assertEquals("Nested/Bag.h", lookup.resolve(symbol("FOuter::FBag", "FBag", "type")).single().file)
    }

    @Test fun relativeAliasesSearchEnclosingScopesAndHonorExplicitGlobalNames() {
        val source = """
            struct FItem { int Global; };
            namespace Shared { struct FItem { int GlobalShared; }; }
            namespace Native {
                struct FItem { int Native; };
                namespace Shared { struct FItem { int NativeShared; }; }
                struct FOuter {
                    using FNearest = FItem;
                    using FRelative = Shared::FItem;
                    using FGlobal = ::FItem;
                    using FGlobalShared = ::Shared::FItem;
                };
            }
        """.trimIndent()
        val lookup = resolver(mapOf("Item.h" to source))
        for ((alias, member) in listOf("FNearest" to "Native", "FRelative" to "NativeShared",
            "FGlobal" to "Global", "FGlobalShared" to "GlobalShared")) {
            val target = lookup.resolve(symbol("Native::FOuter::$alias", member, "property")).single()
            assertEquals(source.indexOf("$member;"), target.offset)
        }
    }

    @Test fun inheritedNamesRetainTheirDeclaringScopeIncludingNestedAndGlobalBases() {
        val source = """
            struct FGlobal { void GlobalCall(); };
            namespace Native {
                struct FBase { void Ping(); };
                struct FOuter {
                    struct FLocalBase { void LocalCall(); };
                    struct FDerived : FBase, FLocalBase, ::FGlobal {};
                };
            }
            namespace Wrong { struct FBase { void Ping(); }; struct FLocalBase { void LocalCall(); }; }
        """.trimIndent()
        val lookup = resolver(mapOf("Derived.h" to source))
        for ((method, declaringOwner) in listOf("Ping" to "Native::FBase", "LocalCall" to "Native::FOuter::FLocalBase",
            "GlobalCall" to "FGlobal")) {
            val target = lookup.resolve(symbol("Native::FOuter::FDerived", method)).single()
            assertEquals(source.indexOf("$method()"), target.offset)
            assertEquals("void $declaringOwner::$method()", target.label)
        }
    }

    @Test fun anExistingInnerTypeStopsLookupEvenWhenItDoesNotHaveTheRequestedMember() {
        val source = """
            struct FBase { void Wrong(); };
            namespace Native { struct FBase {}; struct FDerived : FBase {}; }
        """.trimIndent()
        assertTrue(resolver(mapOf("Derived.h" to source)).resolve(symbol("Native::FDerived", "Wrong")).isEmpty())
    }

    @Test fun allBaseBranchesResolveAliasesBeforeCollectingMemberCandidates() {
        val source = """
            namespace Native {
                struct FFirst { void Call(int Value); };
                struct FSecond { void Call(float Value); };
                using FFirstAlias = FFirst;
                struct FDerived : FFirstAlias, FSecond {};
            }
            namespace Wrong { struct FFirst { void Call(double Value); }; }
        """.trimIndent()
        val targets = resolver(mapOf("Derived.h" to source)).resolve(symbol("Native::FDerived", "Call"))
        assertEquals(setOf("void Native::FFirst::Call(int Value)", "void Native::FSecond::Call(float Value)"),
            targets.map { it.label }.toSet())
    }

    @Test fun functionLocalAliasesAndTypesDoNotBecomeGlobalDeclarations() {
        val source = """
            struct FThing {};
            struct FWrong {};
            void Helper() { using FThing = FWrong; struct FLocal {}; }
        """.trimIndent()
        val lookup = resolver(mapOf("Thing.h" to source))
        assertEquals(source.indexOf("FThing {}"), lookup.resolve(symbol("FThing", "FThing", "type")).single().offset)
        assertTrue(lookup.resolve(symbol("FLocal", "FLocal", "type")).isEmpty())
    }

    @Test fun nestedTemplateOwnersStayQualifiedThroughAliasesAndInheritance() {
        val source = """
            template<class T> struct FOuter { struct FInner { void Call(); }; };
            namespace Native {
                using FNested = ::FOuter<int>::FInner;
                struct FDerived : ::FOuter<float>::FInner {};
            }
        """.trimIndent()
        val lookup = resolver(mapOf("Outer.h" to source))
        for (owner in listOf("FOuter<int>::FInner", "Native::FNested", "Native::FDerived")) {
            val target = lookup.resolve(symbol(owner, "Call")).single()
            assertEquals(source.indexOf("Call()"), target.offset)
            assertEquals("void FOuter::FInner::Call()", target.label)
        }
    }

    @Test fun bindingNamespacesResolveRelativeQualifiedOwnersFromTheirLexicalScope() {
        val source = """
            namespace Native::Impl { struct FReal { void Call(); }; }
            namespace Impl { struct FReal { void Call(); }; }
            namespace Native::Bindings {
                auto Type = FAngelscriptBinds::ValueClass("FScript");
                Type.Method("void Call()", METHOD(Impl::FReal, Call));
            }
        """.trimIndent()
        val target = resolver(mapOf("Real.h" to source)).resolve(symbol("FScript", "Call")).single()
        assertEquals(source.indexOf("Call()"), target.offset)
        assertEquals("void Native::Impl::FReal::Call()", target.label)
    }

    @Test(timeout = 5000) fun scopedAliasAndInheritanceCyclesTerminateWithoutEscapingTheirScope() {
        val source = """
            struct FLoop { void Wrong(); };
            namespace Native {
                using FLoop = FOther;
                using FOther = FLoop;
                struct FFirst : FSecond {};
                struct FSecond : FFirst {};
            }
        """.trimIndent()
        val lookup = resolver(mapOf("Loop.h" to source))
        assertTrue(lookup.resolve(symbol("Native::FLoop", "Wrong")).isEmpty())
        assertTrue(lookup.resolve(symbol("Native::FFirst", "Wrong")).isEmpty())
    }

    @Test fun nestedNativeTypesAndAliasesRespectClassAndNamespaceScopes() {
        val source = """
            namespace Native {
                struct alignas(16) CORE_API FOuter {
                    struct FInner { int X; void Call(int Value); };
                    using FNestedAlias = FInner;
                };
                struct FInner { double X; };
            }
            namespace Other { struct FOuter { struct FInner { float X; }; }; }
        """.trimIndent()
        val lookup = resolver(mapOf("Outer.h" to source))
        for (owner in listOf("Native::FOuter::FInner", "Native::FOuter::FNestedAlias")) {
            assertEquals(source.indexOf("FInner { int"), lookup.resolve(symbol(owner, "FInner", "type")).single().offset)
            assertEquals(source.indexOf("X; void"), lookup.resolve(symbol(owner, "X", "property")).single().offset)
            assertEquals(source.indexOf("Call(int"), lookup.resolve(symbol(owner, "Call")).single().offset)
        }
        assertEquals(source.indexOf("X; };\n}"), lookup.resolve(symbol("Native::FInner", "X", "property")).single().offset)
    }

    @Test fun functionMacrosGenerateDeclarationsAndKeepInvocationSourceOffsets() {
        val source = """
            #define DECLARE_FIELD(Name) int Name;
            #define DECLARE_NATIVE(Name, Field) \
                struct Name { DECLARE_FIELD(Field) void Call(int Value); };
            DECLARE_NATIVE(FGenerated, Score)
            // DECLARE_NATIVE(FComment, Score)
            const char* Text = "DECLARE_NATIVE(FStringLiteral, Score)";
        """.trimIndent()
        val lookup = resolver(mapOf("Generated.h" to source))
        assertEquals(source.indexOf("FGenerated,"), lookup.resolve(symbol("FGenerated", "FGenerated", "type")).single().offset)
        assertEquals(source.indexOf("Score)"), lookup.resolve(symbol("FGenerated", "Score", "property")).single().offset)
        assertEquals(source.indexOf("DECLARE_NATIVE(FGenerated"), lookup.resolve(symbol("FGenerated", "Call")).single().offset)
        assertTrue(lookup.resolve(symbol("FComment", "FComment", "type")).isEmpty())
        assertTrue(lookup.resolve(symbol("FStringLiteral", "FStringLiteral", "type")).isEmpty())
    }

    @Test fun pastedMacroNamesAndHeaderDefinedBindingMacrosResolveWithoutWhitelists() {
        val files = mapOf(
            "Generated.h" to "#define NATIVE(Type) struct F##Type { int X; };\nNATIVE(Generated)",
            "Register.h" to "#define REGISTER(Bind, Name) Bind.Method(\"void \" #Name \"()\", &FGenerated::Name);",
            "Binds.cpp" to "#include \"Register.h\"\nauto Type = FAngelscriptBinds::ExistingClass(\"FGenerated\");\nREGISTER(Type, AddedLater)")
        val lookup = resolver(files)
        assertEquals(files.getValue("Generated.h").indexOf("Generated)"), lookup.resolve(symbol("FGenerated", "FGenerated", "type")).single().offset)
        val target = lookup.resolve(symbol("FGenerated", "AddedLater")).single()
        assertEquals("Binds.cpp", target.file)
        assertTrue(target.offset in files.getValue(target.file).indexOf("REGISTER(Type") until files.getValue(target.file).length)
        assertEquals("void FGenerated::AddedLater()", target.label)
    }

    @Test fun knownInactiveBranchesAreExcludedAndUnknownBuildFlagsStaySearchable() {
        val source = """
            #define LOCAL_ENABLED 1
            #if 0
            struct FConditional { int Wrong; int X; };
            #elif LOCAL_ENABLED && !0
            struct FConditional { double X; };
            #else
            struct FConditional { float X; };
            #endif
            #if UNKNOWN_BUILD_FLAG
            struct FUnknown { int X; };
            #else
            struct FUnknown { double X; };
            #endif
        """.trimIndent()
        val lookup = resolver(mapOf("Conditional.h" to source))
        assertEquals(source.indexOf("X; };", source.indexOf("double X")), lookup.resolve(symbol("FConditional", "X", "property")).single().offset)
        assertTrue(lookup.resolve(symbol("FConditional", "Wrong", "property")).isEmpty())
        assertEquals(2, lookup.resolve(symbol("FUnknown", "X", "property")).size)
    }

    @Test fun macroOnlyBindingSitesAndChangedHeaderMacrosAreReadFromCurrentSources() {
        val files = mutableMapOf(
            "Register.h" to "#define REGISTER_NATIVE(Name) auto Type = FAngelscriptBinds::ExistingClass(#Name); Type.Method(\"void Before()\", [](Name&) {});",
            "Binds.cpp" to "#include \"Register.h\"\nREGISTER_NATIVE(FGenerated)")
        val lookup = resolver(files)
        val before = lookup.resolve(symbol("FGenerated", "Before")).single()
        assertEquals("Binds.cpp", before.file)
        assertEquals(files.getValue(before.file).indexOf("REGISTER_NATIVE"), before.offset)
        files["Register.h"] = files.getValue("Register.h").replace("Before", "LaterAdded")
        assertTrue(lookup.resolve(symbol("FGenerated", "Before")).isEmpty())
        assertEquals("void FGenerated::LaterAdded()", lookup.resolve(symbol("FGenerated", "LaterAdded")).single().label)
    }

    @Test fun inactivePublicIncludesCannotRedirectTypesThroughAnUnrelatedAlias() {
        val files = mapOf(
            "Core/Probe.h" to "#if 0\n#include \"WrongAliases.h\"\n#endif\nstruct FProbe { int X; };",
            "Compat/Probe.h" to "#if !EXTERNAL_WITHOUT_NATIVE\n#include \"Core/Probe.h\"\n#else\nstruct FProbe { float X; };\n#endif",
            "WrongAliases.h" to "using FProbe = FWrong;",
            "Wrong.h" to "struct FWrong { double X; };")
        val lookup = resolver(files)
        for (kind in listOf("type", "property")) {
            val target = lookup.resolve(symbol("FProbe", if (kind == "type") "FProbe" else "X", kind)).single()
            assertEquals("Core/Probe.h", target.file)
            assertEquals(files.getValue(target.file).indexOf(if (kind == "type") "FProbe {" else "X;"), target.offset)
        }
    }

    @Test fun publicHeaderAliasesBeatUnrelatedSameNamedDefinitions() {
        val files = mapOf(
            "Math/Vector.h" to """
                #include "Math/MathFwd.h"
                namespace UE::Math { template<class T> struct TVector { T X; }; }
            """.trimIndent(),
            "Math/MathFwd.h" to "using FVector = UE::Math::TVector<double>;",
            "NoExportTypes.h" to "struct FVector { double X; };",
            "ToolContainers.h" to "namespace TextureShare { struct FVector { double X; }; }",
            "Binds.cpp" to """
                auto Vector = FAngelscriptBinds::ValueClass("FVector");
                Vector.Property("double X", &FVector::X);
            """.trimIndent())
        val lookup = resolver(files)
        for (kind in listOf("type", "property")) {
            val target = lookup.resolve(symbol("FVector", if (kind == "type") "FVector" else "X", kind)).single()
            assertEquals("Math/Vector.h", target.file)
            assertEquals(files.getValue(target.file).indexOf(if (kind == "type") "TVector {" else "X;"), target.offset)
        }
        assertTrue(AngelscriptCppDeclarations.aliases(
            "namespace Other { using FVector = Decoy; }", "FVector", "").isEmpty())
    }

    @Test fun bindingImplementationDoesNotResolveToAnUnrelatedNamespacedClassInAConventionalHeader() {
        val files = mapOf(
            "Iterator.h" to "namespace Atoc { class FMapIterator { public: template<class T> T& GetKey(); }; }",
            "Bind_TMap.h" to "struct FMapIterator { void* GetKey(); };",
            "NativeVector.h" to "using FNativeVector = UE::Math::TVector<double>; struct TVector { void Length(); };",
            "MathVector.h" to "namespace UE::Math { template<class T> struct TVector { double Length(); }; }",
            "Bindings.cpp" to """
                auto Iterator = FAngelscriptBinds::ValueClass("TMapIterator<class K, class V>");
                Iterator.Method("const K& GetKey() const", METHOD(FMapIterator, GetKey));
                namespace Atoc {
                    auto Other = FAngelscriptBinds::ValueClass("TOtherIterator<class T>");
                    Other.Method("T& GetKey()", METHOD(FMapIterator, GetKey));
                }
                auto Qualified = FAngelscriptBinds::ValueClass("TQualifiedIterator<class T>");
                Qualified.Method("T& GetKey()", METHOD(Atoc::FMapIterator, GetKey));
                auto Vector = FAngelscriptBinds::ValueClass("FNativeVector");
                Vector.Method("double Length()", METHOD(FNativeVector, Length));
            """.trimIndent())
        val lookup = resolver(files)
        assertEquals("Bind_TMap.h", lookup.resolve(symbol("TMapIterator", "GetKey")).single().file)
        assertEquals("Iterator.h", lookup.resolve(symbol("TOtherIterator", "GetKey")).single().file)
        assertEquals("Iterator.h", lookup.resolve(symbol("TQualifiedIterator", "GetKey")).single().file)
        assertEquals("MathVector.h", lookup.resolve(symbol("FNativeVector", "Length")).single().file)
        assertEquals("Outer::Inner", AngelscriptCppDeclarations.namespaceAt(
            "namespace Outer::Inner { struct Owner { void Call() { here; } }; }", 57))
    }

    @Test fun indirectlyIncludedAliasesBeatFallbackDefinitionsWithoutUsingNamespacedAliases() {
        val files = mapOf(
            "Math/Rotation.h" to """
                #include "Math/Vector.h"
                namespace UE::Math { template<class T> struct TRotation { T X; }; }
            """.trimIndent(),
            "Math/Vector.h" to "#include \"Math/MathFwd.h\"",
            "Math/MathFwd.h" to "using FRotation = UE::Math::TRotation<double>;",
            "NoExportTypes.h" to "struct FRotation { double X; };",
            "ToolContainers.h" to "namespace Tool { using FRotation = WrongRotation; struct WrongRotation { double X; }; }")
        val lookup = resolver(files)
        for (kind in listOf("type", "property")) {
            val target = lookup.resolve(symbol("FRotation", if (kind == "type") "FRotation" else "X", kind)).single()
            assertEquals("Math/Rotation.h", target.file)
            assertEquals(files.getValue(target.file).indexOf(if (kind == "type") "TRotation {" else "X;"), target.offset)
        }
    }

    @Test fun offlineReceiversUseGenericBindingReturnTypesInsteadOfContainerNameHeuristics() {
        val files = mapOf("Binds.cpp" to """
            auto Bag = FAngelscriptBinds::ValueClass("FutureBag<class K, class V>");
            Bag.Method("FutureCursor<K,V> Iterator()", []() {});
            auto Cursor = FAngelscriptBinds::ValueClass("FutureCursor<class K, class V>");
            Cursor.Method("FutureCursor<K,V>& Iterate()", []() {});
            Cursor.Method("V& GetValue()", []() {});
            Cursor.Method("void NewlyAdded()", []() {});
            auto Sequence = FAngelscriptBinds::ValueClass("FutureSequence<class T>");
            Sequence.Method("FutureElementCursor<T> Iterator()", []() {});
            auto Element = FAngelscriptBinds::ValueClass("FutureElementCursor<class T>");
            Element.Method("T& Proceed()", []() {});
            auto Vector = FAngelscriptBinds::ValueClass("FVector");
            Vector.Property("double X", &FVector::X);
        """.trimIndent(), "Vector.h" to "struct FVector { double X; };")
        val lookup = resolver(files)
        fun query(type: String, steps: List<AngelscriptReceiverStep>, name: String, kind: String = "function") =
            lookup.resolve(symbol(type.substringBefore('<'), name, kind).copy(sourceOnly = true,
                receiver = AngelscriptNativeReceiver(type, steps)))
        val loop = listOf(AngelscriptReceiverStep("foreach"))
        assertTrue(query("FutureBag<FName,FVector>", loop, "NewlyAdded").single().label.contains("FutureCursor::NewlyAdded"))
        for (steps in listOf(loop, listOf(AngelscriptReceiverStep("method", "Iterator")))) {
            val value = query("FutureBag<FName,FVector>", steps + AngelscriptReceiverStep("method", "GetValue"), "X", "property").single()
            assertEquals("Vector.h", value.file)
        }
        assertEquals("Vector.h", query("FutureSequence<FVector>", loop, "X", "property").single().file)
        assertTrue(query("FutureBag<FName,FVector>", listOf(AngelscriptReceiverStep("method", "Missing")), "X", "property").isEmpty())
        val bindings = files.values.flatMap(AngelscriptNativeBindings::parse)
        assertEquals(setOf("FutureCursor<FName,TArray<FVector>>"), AngelscriptNativeReceiverTypes.resolve(
            AngelscriptNativeReceiver("FutureBag<FName,TArray<FVector>>", loop)) { owner -> bindings.filter { it.owner == owner } })
    }

    @Test fun injectedTypesBeatCompatibilityDefinitionsAndKeepMacroContextsAndInheritance() {
        val files = mapOf(
            "Core/Map.h" to """
                #define UE_PREFIX T
                #include "Map.h.inl"
                #undef UE_PREFIX
                #define UE_PREFIX TSparse
                #include "Map.h.inl"
                #undef UE_PREFIX
            """.trimIndent(),
            "Chaos/Map.h" to "class TMap {};",
            "Core/Map.h.inl" to """
                #define BASE UE_JOIN(UE_PREFIX, MapBase)
                #define MAP UE_JOIN(UE_PREFIX, Map)
                class BASE { public: int NewMethod(int Key); int NewMethod(float Key); };
                class MAP : public BASE<int> {};
            """.trimIndent(),
            "Set.h" to """
                #define INJECTED_SET TSet
                #include "Set.h.inl"
                #undef INJECTED_SET
            """.trimIndent(),
            "Set.h.inl" to "class INJECTED_SET { public: bool NewlyAdded(int Value); };")
        val lookup = resolver(files)
        for (owner in listOf("TMap", "TSparseMap")) {
            val type = lookup.resolve(symbol(owner, owner, "type")).single()
            assertEquals("Core/Map.h.inl", type.file)
            assertEquals(files.getValue(type.file).indexOf("MAP :"), type.offset)
            assertEquals(owner, type.label)
            val methods = lookup.resolve(symbol(owner, "NewMethod"))
            assertEquals(2, methods.size)
            assertTrue(methods.all { it.label.contains("${owner}Base::NewMethod(") })
        }
        val type = lookup.resolve(symbol("TSet", "TSet", "type")).single()
        assertEquals("Set.h.inl", type.file)
        assertEquals(6, type.offset)
        assertEquals("TSet", type.label)
        assertEquals("bool TSet::NewlyAdded(int Value)", lookup.resolve(symbol("TSet", "NewlyAdded")).single().label)
    }

    @Test(timeout = 5000) fun largeAliasDeclarationsStayBounded() {
        val source = "typedef " + "LongNamespace".repeat(20_000) + " OtherName;\n" +
            "using Wanted = UE::Math::TVector<double>;\n" +
            "typedef UE::Math::TVector<float> WantedFloat;"
        assertEquals(listOf("UE::Math::TVector"), AngelscriptCppDeclarations.aliases(source, "Wanted"))
        assertEquals(listOf("UE::Math::TVector"), AngelscriptCppDeclarations.aliases(source, "WantedFloat"))
        assertTrue(AngelscriptCppDeclarations.aliases(source, "Missing").isEmpty())
    }

    @Test fun unrealPrefixedPublicHeadersBeatNestedSameNamedAliases() {
        val files = mapOf(
            "Containers/UnrealMessage.h" to """
                #define MESSAGE_CLASS FMessage
                #include "Containers/Message.h.inl"
                #undef MESSAGE_CLASS
            """.trimIndent(),
            "Containers/Message.h.inl" to "class MESSAGE_CLASS { public: int Len() const; };",
            "Platform.h" to "class FPlatform { using FMessage = WrongMessage; }; class WrongMessage {};",
            "Tool.h" to "namespace Tool { class FMessage {}; }")
        val lookup = resolver(files)
        assertEquals("Containers/Message.h.inl", lookup.resolve(symbol("FMessage", "FMessage", "type")).single().file)
        assertEquals("Containers/Message.h.inl", lookup.resolve(symbol("FMessage", "Len")).single().file)
    }

    @Test fun availableEngineStringTypeAndMembersUseTheirPublicDeclaration() {
        val path = System.getProperty("angelscript.test.engineSource")
        assumeNotNull(path)
        val root = Path.of(path!!)
        val paths = listOf("Runtime/Core/Public/Containers/UnrealString.h",
            "Runtime/Core/Public/Containers/UnrealString.h.inl",
            "Runtime/Core/Public/Containers/UnrealStringIncludes.h.inl",
            "Runtime/Core/Public/Android/AndroidJavaEnv.h")
        val files = paths.map(root::resolve).filter(Files::exists).associate {
            it.toString().replace('\\', '/') to Files.readString(it)
        }
        val expected = if (paths[1].let { Files.exists(root.resolve(it)) }) "/UnrealString.h.inl" else "/UnrealString.h"
        val searchedWords = mutableListOf<String>()
        val lookup = resolver(files, searchedWords)
        val type = lookup.resolve(symbol("FString", "FString", "type")).single()
        assertTrue(type.toString(), type.file.endsWith(expected))
        assertFalse("Resolve the public string header before searching every FString use", "FString" in searchedWords)
        for (name in listOf("Len", "IsEmpty", "Empty")) {
            val targets = lookup.resolve(symbol("FString", name))
            assertFalse(name, targets.isEmpty())
            assertTrue("$name: $targets", targets.all { it.file.endsWith(expected) && it.label.contains("FString::$name") })
        }
    }

    @Test fun longCppLiteralsDoNotAbortTypeOrMemberNavigation() {
        val source = "const char* Message = \"" + "x\\n".repeat(30_000) + "\";\nclass FProbe { public: void Call(); };"
        val lookup = resolver(mapOf("Probe.h" to source))
        val type = lookup.resolve(symbol("FProbe", "FProbe", "type")).single()
        assertEquals(source.indexOf("FProbe"), type.offset)
        val member = lookup.resolve(symbol("FProbe", "Call")).single()
        assertEquals(source.indexOf("Call"), member.offset)
        assertEquals("void FProbe::Call()", member.label)
    }

    @Test fun longCppLiteralsDoNotAbortBindingNavigation() {
        val source = "const char* Message = \"" + "x\\n".repeat(30_000) + "\";\n" +
            "auto Type = FAngelscriptBinds::ExistingClass(\"FProbe\");\n" +
            "Type.Method(\"void Call()\", []() {});"
        val target = resolver(mapOf("Binds.cpp" to source)).resolve(symbol("FProbe", "Call")).single()
        assertEquals(source.indexOf("Call()"), target.offset)
        assertEquals("void FProbe::Call()", target.label)
    }

    @Test fun availableEngineBindingSourcesAndActorDeclarationsRemainParseable() {
        val path = System.getProperty("angelscript.test.engineSource")
        assumeNotNull(path)
        val root = Path.of(path!!)
        val plugin = root.resolve("../Plugins/Angelscript/Source/AngelscriptCode")
        val files = linkedMapOf<String, String>()
        for (directory in listOf("Private/Binds", "Public/AngelscriptBinds")) {
            Files.list(plugin.resolve(directory)).use { entries ->
                entries.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(if (directory.startsWith("Private")) ".cpp" else ".h") }
                    .sorted().forEach { file ->
                        val source = Files.readString(file)
                        try { AngelscriptNativeBindings.parse(source) }
                        catch (error: StackOverflowError) { throw AssertionError("Native binding parser overflowed: $file", error) }
                        files[file.toString().replace('\\', '/')] = source
                    }
            }
        }
        val actor = root.resolve("Runtime/Engine/Classes/GameFramework/Actor.h")
        files[actor.toString().replace('\\', '/')] = Files.readString(actor)
        val lookup = resolver(files)
        for ((owner, name, header) in listOf(Triple("__", "SpawnActor", "Bind_Actor.h"),
            Triple("AActor", "GetActorLocation", "Actor.h"), Triple("AActor", "SetActorLocation", "Actor.h"))) {
            val targets = lookup.resolve(symbol(owner, name))
            assertFalse("$owner::$name", targets.isEmpty())
            assertTrue("$owner::$name: $targets", targets.all { it.file.endsWith(header) })
        }
    }

    @Test fun templateParametersAndTypeUsesAreNeverDefinitions() {
        val source = """
            template<class TMap> struct Unrelated { void Add(); };
            template<class TSet = int> void Helper() { int Value; }
            class TSet* Pointer;
            struct Holder { friend class TMap; };
            template<class K, class V> class TMap final : public Base<K, V> {};
            template<class T> class TSet {};
        """.trimIndent()
        for (owner in listOf("TMap", "TSet")) {
            assertEquals(listOf(source.lastIndexOf("class $owner") + 6),
                AngelscriptCppDeclarations.offsets(source, setOf(owner), setOf(owner), "type"))
        }
        assertTrue(AngelscriptCppDeclarations.offsets(source, setOf("TMap"), setOf("Add"), "function").isEmpty())
    }

    @Test fun alignedAndAttributedTypesRemainNavigable() {
        val source = "struct alignas(16) TQuat { float X; }; class [[deprecated]] CORE_API Aligned { void Call(); };"
        for (owner in listOf("TQuat", "Aligned")) assertEquals(listOf(source.indexOf(owner)),
            AngelscriptCppDeclarations.offsets(source, setOf(owner), setOf(owner), "type"))
    }

    @Test fun exactContainerTypesAndRegistrationsTakePrecedenceOverStorageAndSameNamedMethods() {
        val files = mapOf(
            "Map.h" to "class TMap : public Base<int> {}; class Base { int Add(int Key); }; struct FScriptMap {};",
            "Set.h" to "class TSet { bool Contains(int Value); }; struct FScriptSet {};",
            "Binds.cpp" to """
                auto Map = FAngelscriptBinds::ValueClass<FScriptMap>("TMap<class K, class V>", Flags);
                Map.Method("void Add(const K& Key, const V& Value)", &MapImpl::Insert);
                auto Set = FAngelscriptBinds::ValueClass<FScriptSet>("TSet<class T>", Flags);
                Set.Method("bool Contains(const T& Value) const", &SetImpl::HasValue);
            """.trimIndent(),
            "Impl.h" to "struct MapImpl { static void Insert(void* Key, void* Value); }; struct SetImpl { static bool HasValue(void* Value); };")
        val lookup = resolver(files)
        for ((owner, header) in listOf("TMap" to "Map.h", "TSet" to "Set.h")) {
            val target = lookup.resolve(symbol(owner, owner, "type")).single()
            assertEquals(header, target.file)
            assertEquals(files.getValue(header).indexOf(owner), target.offset)
        }
        assertTrue(lookup.resolve(symbol("TMap", "Add")).single().label.contains("MapImpl::Insert(void* Key, void* Value)"))
        assertTrue(lookup.resolve(symbol("TSet", "Contains")).single().label.contains("SetImpl::HasValue(void* Value)"))
    }

    @Test fun inheritedMembersAndOverloadsKeepTheirActualOwnerAndSignature() {
        val files = mapOf("Leaf.h" to "class Leaf : public Middle<int, float> {};",
            "Middle.h" to "template<class K, class V> class Middle : public Base {};",
            "Base.h" to "class Base { public: int Add(int Key); float Add(const FString& Key) const; };")
        val targets = resolver(files).resolve(symbol("Leaf", "Add"))
        assertEquals(2, targets.size)
        assertEquals(setOf("int Base::Add(int Key)", "float Base::Add(const FString& Key) const"), targets.map { it.label }.toSet())
        assertTrue(targets.all { it.file == "Base.h" })
    }

    @Test fun bindingOverloadsAndLambdasKeepTheirOwnSignaturesAndOffsets() {
        val source = """
            auto Type = FAngelscriptBinds::ExistingClass("FProbe");
            Type.Method("void Call(int Value)", [](int Value) {});
            Type.Method("void Call(FString Value)", [](FString Value) {});
        """.trimIndent()
        val targets = resolver(mapOf("Binds.cpp" to source)).resolve(symbol("FProbe", "Call"))
        assertEquals(setOf("void FProbe::Call(int Value)", "void FProbe::Call(FString Value)"), targets.map { it.label }.toSet())
        assertEquals(2, targets.map { it.offset }.distinct().size)
    }

    @Test fun sharedBindingHelpersPropagateOwnersWithoutUsingHelperNamesAsAWhitelist() {
        val source = """
            void Inner(FAngelscriptBinds& Shared) { Shared.Method("void NewMethod(int Value)", [](int Value) {}); }
            void Outer(FAngelscriptBinds& Type) { Inner(Type); }
            auto First = FAngelscriptBinds::ExistingClass("FirstType");
            auto Second = FAngelscriptBinds::ExistingClass("SecondType");
            Outer(First); Outer(Second);
        """.trimIndent()
        for (owner in listOf("FirstType", "SecondType")) {
            val target = resolver(mapOf("Bind.cpp" to source)).resolve(symbol(owner, "NewMethod")).single()
            assertEquals(source.indexOf("NewMethod(int"), target.offset)
            assertEquals("void $owner::NewMethod(int Value)", target.label)
        }
    }

    @Test fun otherAvailableUeTemplateTypesAliasesAndBindingsUseTheSameLookupRules() {
        val path = System.getProperty("angelscript.test.engineSource")
        assumeNotNull(path)
        val root = Path.of(path!!)
        val plugin = root.resolve("../Plugins/Angelscript/Source/AngelscriptCode")
        val files = linkedMapOf<String, String>()
        val paths = listOf("Runtime/Core/Public/Containers/Array.h", "Runtime/Core/Public/Misc/Optional.h",
            "Runtime/Core/Public/Templates/SharedPointer.h", "Runtime/Core/Public/Math/MathFwd.h",
            "Runtime/Core/Public/Math/Vector.h", "Runtime/Core/Public/Math/Quat.h", "Runtime/Core/Public/Math/Rotator.h",
            "Runtime/CoreUObject/Public/Templates/SubclassOf.h", "Runtime/CoreUObject/Public/UObject/SoftObjectPtr.h",
            "Runtime/CoreUObject/Public/UObject/PersistentObjectPtr.h", "Runtime/CoreUObject/Public/UObject/LazyObjectPtr.h",
            "Runtime/CoreUObject/Public/UObject/NoExportTypes.h")
        for (file in paths.map(root::resolve) + listOf("TArray.cpp", "TOptional.cpp", "TSoftObjectPtr.cpp").map {
            plugin.resolve("Private/Binds/Bind_$it")
        } + listOf("TArray.h", "TArray_Functions.h", "TArray_Structs.h", "TOptional.h").map {
            plugin.resolve("Public/AngelscriptBinds/Bind_$it")
        } + listOf(plugin.resolve("Private/Binds/Bind_FVector.cpp")))
            files[file.toString().replace('\\', '/')] = Files.readString(file)
        val toolVector = root.resolve("Programs/VirtualProduction/TextureShare/Public/Containers/UnrealEngine/TextureShareSDKUnrealEngineContainers.h")
        if (Files.exists(toolVector)) files[toolVector.toString().replace('\\', '/')] = Files.readString(toolVector)
        val uLangArray = root.resolve("Runtime/Solaris/uLangCore/Public/uLang/Common/Containers/Array.h")
        if (Files.exists(uLangArray)) files[uLangArray.toString().replace('\\', '/')] = Files.readString(uLangArray)
        val lookup = resolver(files)
        for ((owner, filename) in listOf("TArray" to "Array.h", "TOptional" to "Optional.h", "TSubclassOf" to "SubclassOf.h",
            "TSoftObjectPtr" to "SoftObjectPtr.h", "TSoftClassPtr" to "SoftObjectPtr.h", "TLazyObjectPtr" to "LazyObjectPtr.h",
            "TSharedPtr" to "SharedPointer.h", "TSharedRef" to "SharedPointer.h", "TWeakPtr" to "SharedPointer.h",
            "FVector" to "Vector.h", "FQuat" to "Quat.h", "FRotator" to "Rotator.h")) {
            val targets = lookup.resolve(symbol(owner, owner, "type"))
            assertEquals("$owner: $targets", 1, targets.size)
            assertTrue("$owner: $targets", targets.single().file.endsWith("/$filename"))
        }
        for ((owner, names) in listOf("TArray" to listOf("Add", "Num", "Empty", "FindIndex", "Iterator"),
            "TArrayIterator" to listOf("Proceed"), "TOptional" to listOf("IsSet", "Set", "GetValue", "Reset"))) {
            for (name in names) {
                val targets = lookup.resolve(symbol(owner, name))
                assertFalse("$owner::$name", targets.isEmpty())
                assertTrue("$owner::$name: $targets", targets.all { it.file.contains("AngelscriptCode/") })
            }
        }
        for (owner in listOf("TSoftObjectPtr", "TSoftClassPtr")) for (name in listOf("ToString", "GetAssetName", "IsValid", "IsNull", "Reset")) {
            val target = lookup.resolve(symbol(owner, name)).single()
            assertTrue("$owner::$name: $target", target.file.endsWith("Bind_TSoftObjectPtr.cpp"))
            assertTrue(target.label.contains("$owner::$name("))
        }
        for (name in listOf("X", "Y", "Z")) {
            val target = lookup.resolve(symbol("FVector", name, "property")).single()
            assertTrue("FVector::$name: $target", target.file.endsWith("/Math/Vector.h"))
            assertEquals(name, files.getValue(target.file).substring(target.offset, target.offset + name.length))
            assertTrue(target.label.contains("UE::Math::TVector::$name"))
        }
    }

    @Test fun allAvailableUeMapAndSetBindingsResolveToTheirRegisteredImplementations() {
        val path = System.getProperty("angelscript.test.engineSource")
        assumeNotNull(path)
        val root = Path.of(path!!)
        val plugin = root.resolve("../Plugins/Angelscript/Source/AngelscriptCode")
        val files = linkedMapOf<String, String>()
        for (container in listOf("Map", "Set")) {
            for (file in listOf(root.resolve("Runtime/Core/Public/Containers/$container.h"),
                plugin.resolve("Private/Binds/Bind_T$container.cpp"), plugin.resolve("Public/AngelscriptBinds/Bind_T$container.h")))
                files[file.toString().replace('\\', '/')] = Files.readString(file)
        }
        for (name in listOf("Map.h.inl", "SparseSet.h.inl", "CompactSet.h.inl")) {
            val file = root.resolve("Runtime/Core/Public/Containers/$name")
            if (Files.exists(file)) files[file.toString().replace('\\', '/')] = Files.readString(file)
        }
        val compatibility = root.resolve("Runtime/Experimental/Chaos/Public/Chaos/Map.h")
        if (Files.exists(compatibility)) files[compatibility.toString().replace('\\', '/')] = Files.readString(compatibility)
        val atocIterator = plugin.resolve("Public/Atoc/Iterator.h")
        if (Files.exists(atocIterator)) files[atocIterator.toString().replace('\\', '/')] = Files.readString(atocIterator)
        for (container in listOf("Map", "Set")) {
            val uLang = root.resolve("Runtime/Solaris/uLangCore/Public/uLang/Common/Containers/$container.h")
            if (Files.exists(uLang)) files[uLang.toString().replace('\\', '/')] = Files.readString(uLang)
        }
        val lookup = resolver(files)
        val nativeOnly = resolver(files.filterKeys { it.contains("/Containers/") })
        for (name in listOf("GetKey", "GetValue", "RemoveCurrent", "SetValue")) {
            val targets = lookup.resolve(symbol("TMap", name).copy(sourceOnly = true,
                receiver = AngelscriptNativeReceiver("TMap<FName,FVector>", listOf(AngelscriptReceiverStep("foreach")))))
            assertFalse("offline foreach $name", targets.isEmpty())
            assertTrue("offline foreach $name: $targets", targets.all { it.file.endsWith("Bind_TMap.h") })
        }
        val inheritedAdd = nativeOnly.resolve(symbol("TMap", "Add"))
        assertEquals(8, inheritedAdd.size)
        assertTrue(inheritedAdd.all { it.label.contains("TMapBase::Add(") })
        assertEquals(8, inheritedAdd.map { it.label }.distinct().size)
        for (container in listOf("Map", "Set")) {
            val owner = "T$container"
            val targets = lookup.resolve(symbol(owner, owner, "type"))
            val injected = files.keys.any { it.endsWith("Map.h.inl") }
            assertEquals(if (injected && container == "Set") 2 else 1, targets.size)
            for (target in targets) {
                assertTrue("$owner: $target", target.file.contains("/Core/Public/Containers/"))
                assertEquals(owner, target.label)
                if (injected) {
                    val token = if (container == "Map") "TMAP" else if (target.file.endsWith("SparseSet.h.inl")) "UE_TSPARSE_SET" else "UE_TCOMPACT_SET"
                    assertTrue(target.file.endsWith(".h.inl"))
                    assertTrue(files.getValue(target.file).substring(target.offset).startsWith(token))
                } else assertEquals(Regex("class $owner\\s*(?=[:{])").find(files.getValue(target.file))!!.range.first + 6, target.offset)
            }
            val registered = files.filterKeys { it.endsWith("Bind_T$container.cpp") }.values
                .flatMap(AngelscriptNativeBindings::parse).filter { it.kind == "function" && it.name != "f" }
            assertTrue(registered.isNotEmpty())
            for (binding in registered) {
                val targets = lookup.resolve(symbol(binding.owner, binding.name))
                assertFalse("${binding.owner}::${binding.name}", targets.isEmpty())
                assertTrue("${binding.owner}::${binding.name}: $targets", targets.all { it.file.endsWith("Bind_T$container.h") })
                assertTrue(targets.any { it.label.contains("${binding.cppOwner}::${binding.cppName}(") })
            }
        }
    }

    @Test fun availableUeContainerNamesAndUnboundMembersIgnoreOtherNamespaceAliases() {
        val path = System.getProperty("angelscript.test.engineSource")
        assumeNotNull(path)
        val root = Path.of(path!!)
        val files = linkedMapOf<String, String>()
        for (name in listOf("Map.h", "Map.h.inl", "Set.h", "SparseSet.h.inl", "CompactSet.h.inl", "Array.h")) {
            val file = root.resolve("Runtime/Core/Public/Containers/$name")
            if (Files.exists(file)) files[file.toString().replace('\\', '/')] = Files.readString(file)
        }
        for (owner in listOf("Map", "Set", "Array")) {
            val file = root.resolve("Runtime/Solaris/uLangCore/Public/uLang/Common/Containers/$owner.h")
            if (Files.exists(file)) files[file.toString().replace('\\', '/')] = Files.readString(file)
        }
        val lookup = resolver(files)
        for (owner in listOf("TMap", "TSet", "TArray")) {
            for ((name, kind) in listOf(owner to "type", "Contains" to "function")) {
                val targets = lookup.resolve(symbol(owner, name, kind))
                assertFalse("$owner::$name", targets.isEmpty())
                assertTrue("$owner::$name: $targets", targets.all { it.file.contains("/Core/Public/Containers/") })
            }
            if (files.keys.any { it.contains("/uLang/") }) {
                val targets = lookup.resolve(symbol("uLang::$owner", owner, "type"))
                assertFalse("qualified uLang::$owner", targets.isEmpty())
                assertTrue("qualified uLang::$owner: $targets", targets.all { it.file.contains("/uLang/") })
            }
        }
    }
}
