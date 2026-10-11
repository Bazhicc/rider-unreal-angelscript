package com.aesirinteractive.angelscript

import org.junit.Assert.*
import org.junit.Test
import org.junit.Assume.assumeNotNull
import java.nio.file.Files
import java.nio.file.Path

class AngelscriptCppNavigationTest {
    @Test fun optionalServerExtensionIsNegotiated() {
        assertTrue(supportsCppSymbols(mapOf("angelscriptCppSymbol" to true)))
        assertTrue(supportsCppSymbols(com.google.gson.JsonParser.parseString("{\"angelscriptCppSymbol\":true}")))
        assertFalse(supportsCppSymbols(null))
        assertFalse(supportsCppSymbols(emptyMap<String, Any>()))
        assertTrue(org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints.getSupportedMethods(AngelscriptLanguageServer::class.java)
            .containsKey("angelscript/cppSymbol"))
    }

    @Test fun functionsAndFieldsStayInsideTheirDeclaringType() {
        val source = """
            class Wrong { void GetActorLocation(); float X; };
            class ENGINE_API AActor {
                // void GetActorLocation();
                const char* Text = "void GetActorLocation();";
                FORCEINLINE FVector GetActorLocation() const { return Other.GetActorLocation(); }
                bool SetActorLocation(const FVector& Location, bool Sweep = false);
                void Nested() { float X; }
            };
            struct TVector {
                union { struct { T X; T Y; T Z; }; };
                T GetX() const { return X; }
            };
        """.trimIndent()
        fun offsets(owner: String, name: String, kind: String) =
            AngelscriptCppDeclarations.offsets(source, setOf(owner), setOf(name), kind)
        assertEquals(listOf(source.indexOf("GetActorLocation() const")), offsets("AActor", "GetActorLocation", "function"))
        assertEquals(listOf(source.indexOf("SetActorLocation(const")), offsets("AActor", "SetActorLocation", "function"))
        assertEquals(listOf(source.indexOf("X; T Y")), offsets("TVector", "X", "property"))
        assertTrue(offsets("AActor", "X", "property").isEmpty())
    }

    @Test fun aliasesResolveTheActualTemplateDeclaration() {
        val source = """
            using FVector = UE::Math::TVector<double>;
            typedef UE::Math::TVector<float> FVector3f;
            // using FVector = Wrong;
        """.trimIndent()
        assertEquals(listOf("TVector"), AngelscriptCppDeclarations.aliases(source, "FVector"))
        assertEquals(listOf("TVector"), AngelscriptCppDeclarations.aliases(source, "FVector3f"))
    }

    @Test fun ufunctionAliasesAndOverloadsArePreserved() {
        val source = "class AActor { FVector K2_GetActorLocation() const; FVector GetActorLocation() const; };"
        assertEquals(2, AngelscriptCppDeclarations.offsets(source, setOf("AActor"),
            setOf("GetActorLocation", "K2_GetActorLocation"), "function").size)
        assertEquals(listOf(source.indexOf("K2_GetActorLocation")), AngelscriptCppDeclarations.offsets(source, setOf("AActor"),
            AngelscriptCppSymbol("AActor", "GetActorLocation", "K2_GetActorLocation", "function", true)))
    }

    @Test fun nativeTypesAndEnumsNavigateToTheirNames() {
        val source = "class ENGINE_API AActor {}; struct TVector {}; enum class ETeleportType { None, TeleportPhysics };"
        for ((owner, kind) in listOf("AActor" to "type", "TVector" to "type", "ETeleportType" to "enum"))
            assertEquals(listOf(source.indexOf(owner)),
                AngelscriptCppDeclarations.offsets(source, setOf(owner), setOf(owner), kind))
    }

    @Test fun namespacesGlobalsAndEnumValuesHaveScopedDeclarations() {
        val source = """
            int NewGlobal();
            namespace MathFixture { double Length(double Value); int Clock; }
            namespace Wrong { double Length(double Value); }
            class WrongOwner { int NewGlobal(); };
            void Caller() { NewGlobal(); }
            enum class EFixtureMode { First, Second = 2, Third };
        """.trimIndent()
        assertEquals(listOf(source.indexOf("NewGlobal()")),
            AngelscriptCppDeclarations.namespaceOffsets(source, "", setOf("NewGlobal"), "function"))
        assertEquals(listOf(source.indexOf("Length(double")),
            AngelscriptCppDeclarations.namespaceOffsets(source, "MathFixture", setOf("Length"), "function"))
        assertEquals(listOf(source.indexOf("Second =")),
            AngelscriptCppDeclarations.offsets(source, setOf("EFixtureMode"), setOf("Second"), "enumValue"))
    }

    @Test fun registrationsMapArbitraryScriptNamesToNativeImplementations() {
        val source = """
            // FAngelscriptBinds::BindGlobalFunction("void Commented()", &Wrong);
            FAngelscriptBinds::BindGlobalFunction("AActor SpawnActor(const TSubclassOf<AActor>& Class)", FUNC(FAngelscriptActorBinds::SpawnActor));
            {
                auto Iter = FAngelscriptBinds::ValueClass<FMapIterator>("TMapIterator<class K, class V>", Flags);
                Iter.Method("const K& GetKey() const", METHOD(FMapIterator, GetKey));
                Iter.Property("bool CanProceed", &FMapIterator::bCanProceed);
                Iter.Method("void LambdaCall()", [](FMapIterator& Self) { Self.Call(); });
            }
            {
                auto Iter = FAngelscriptBinds::ExistingClass("OtherIterator");
                Iter.Method("void GetKey()", METHOD(FOther, Different));
            }
            {
                FAngelscriptBinds::FNamespace ns("StaticFixture");
                FAngelscriptBinds::BindGlobalFunction("int NewStatic(int Value)", &UStaticFixture::Call);
            }
            FAngelscriptBinds::BindGlobalFunction("void RootAgain()", &Root);
        """.trimIndent()
        val bindings = AngelscriptNativeBindings.parse(source)
        assertFalse(bindings.any { it.name == "Commented" })
        assertEquals("FAngelscriptActorBinds", bindings.single { it.name == "SpawnActor" }.cppOwner)
        assertEquals("FMapIterator", bindings.single { it.kind == "type" && it.name == "TMapIterator" }.cppOwner)
        assertEquals("FMapIterator", bindings.single { it.owner == "TMapIterator" && it.name == "GetKey" }.cppOwner)
        assertEquals("Different", bindings.single { it.owner == "OtherIterator" && it.name == "GetKey" }.cppName)
        assertEquals("bCanProceed", bindings.single { it.name == "CanProceed" }.cppName)
        assertNull(bindings.single { it.name == "LambdaCall" }.cppName)
        assertEquals("StaticFixture", bindings.single { it.name == "NewStatic" }.owner)
        assertEquals("", bindings.single { it.name == "RootAgain" }.owner)
        for (binding in bindings) assertTrue("Offset must identify the AS declaration: $binding",
            source.substring(binding.offset).startsWith(binding.name))
    }

    @Test fun newlyWrittenBindingsRequireNoFunctionWhitelist() {
        for (i in 1..32) {
            val name = "NewFunction$i"
            val source = """
                auto Type = FAngelscriptBinds::ExistingClass("FNewType$i");
                Type.Method("int $name(int Value)", METHOD(FImpl$i, Native$i));
            """.trimIndent()
            val binding = AngelscriptNativeBindings.parse(source).single { it.kind == "function" }
            assertEquals("FNewType$i", binding.owner)
            assertEquals(name, binding.name)
            assertEquals("Native$i", binding.cppName)
        }
    }

    /** Optional local source check; portable fixtures above always run in CI. */
    @Test fun suppliedMoveOtherExampleAgainstAvailableUeHeaders() {
        val path = System.getProperty("angelscript.test.engineSource")
        assumeNotNull(path)
        val root = Path.of(path!!)
        val actor = Files.readString(root.resolve("Runtime/Engine/Classes/GameFramework/Actor.h"))
        val vector = Files.readString(root.resolve("Runtime/Core/Public/Math/Vector.h"))
        val aliases = Files.readString(root.resolve("Runtime/Core/Public/Math/MathFwd.h"))
        val owner = AngelscriptCppDeclarations.aliases(aliases, "FVector").single()
        assertEquals(1, AngelscriptCppDeclarations.offsets(actor, setOf("AActor"), setOf("AActor"), "type").size)
        assertEquals(1, AngelscriptCppDeclarations.offsets(vector, setOf(owner), setOf("FVector"), "type").size)
        for (name in listOf("GetActorLocation", "SetActorLocation")) {
            val targets = AngelscriptCppDeclarations.offsets(actor, setOf("AActor"), setOf(name), "function")
            assertFalse("AActor::$name must resolve in Actor.h", targets.isEmpty())
            assertTrue(targets.all { actor.substring(it).startsWith(name) })
        }
        val target = AngelscriptCppDeclarations.offsets(vector, setOf(owner), setOf("X"), "property")
        assertEquals(1, target.size)
        assertTrue(vector.substring(target.single()).startsWith("X;"))
        val binds = root.resolve("../Plugins/Angelscript/Source/AngelscriptCode/Private/Binds")
        val map = AngelscriptNativeBindings.parse(Files.readString(binds.resolve("Bind_TMap.cpp")))
        for (name in listOf("GetKey", "GetValue", "SetValue", "RemoveCurrent")) {
            val binding = map.single { it.owner == "TMapIterator" && it.name == name }
            assertEquals("FMapIterator", binding.cppOwner)
            assertEquals(name, binding.cppName)
        }
        val actorBinds = AngelscriptNativeBindings.parse(Files.readString(binds.resolve("Bind_AActor.cpp")))
        assertEquals("FAngelscriptActorBinds", actorBinds.single { it.owner == "" && it.name == "SpawnActor" }.cppOwner)
    }
}
