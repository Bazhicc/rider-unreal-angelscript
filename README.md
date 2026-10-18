# Unreal AngelScript for Rider

A JetBrains Rider plugin for Unreal Engine projects that use [Hazelight's Unreal AngelScript plugin](https://angelscript.hazelight.se). It bundles this repository's Hazelight 1.9.4 language server, so you do not need to install VS Code or configure a separate language-server checkout.

The Rider plugin is based on [aesirinteractive/rider-unreal-angelscript](https://github.com/aesirinteractive/rider-unreal-angelscript) at `bf2841c37a3e60326b3f1dde2a4418a3bff33e2f`.

## Features

- AngelScript file support, syntax and semantic highlighting, code structure, completion, hover, navigation, references, rename, code actions, folding, and Unreal compile diagnostics.
- Native and LSP highlighting reuse Rider's C++ color settings for Unreal macros and metadata, variables, parameters, types, enum constants, namespaces, and operators. Colors follow the active theme and your **Settings | Editor | Color Scheme | C++** customizations, including before the language server is ready.
- Debugging for a running Unreal Editor or game: breakpoints, stepping, call stack, variables, and expression evaluation.
- Navigation between AngelScript and indexed Unreal C++ declarations and bindings, plus C++ Find Usages results for AngelScript references.
- Unreal-aware types, overloads, containers, inheritance, aliases, macros, and included declarations.
- AngelScript-aware formatting through bundled Clang-Format, including protected syntax, selected ranges, Rider brace preferences, optional EditorConfig rules, and on-type formatting.
- Open a `.uproject` directory or a `Script` directory. Project and plugin `Script` roots are detected automatically.
- UE asset edits are saved only after the editor confirms that the change was applied.

The plugin uses a Kotlin lexer, parser, PSI, and debugger, Hazelight's TypeScript language server, and a small C# ReSharper backend for native C++ usage searches. It preserves the original VS Code extension source in [`extension/`](extension/README.md).

Some features require an Unreal Editor with AngelScript enabled. C++ navigation and usage search also require the UE project to be indexed, with relevant headers and binding sources in scope. Rider's LSP capabilities define the supported feature set; the VS Code API browser and data-breakpoint UI are not included.

## Build

Requirements: Node.js 22+, JDK 21+, and .NET SDK 9+ (for `net9.0`). Gradle, the Rider SDK, and npm dependencies are downloaded automatically when needed. Users run the plugin on Rider's bundled runtimes.

```powershell
$env:JAVA_HOME = 'C:\Path\To\JDK'
.\gradlew.bat buildPlugin
```

To use an installed Rider SDK instead of downloading one:

```powershell
.\gradlew.bat buildPlugin '-PlocalPlatformPath=C:\Program Files\JetBrains\JetBrains Rider'
```

The installable ZIP is written to `build/distributions/`. The current version is `1.0.0`. The repository retains the three most recently successful, distinct plugin versions; failed builds do not replace them.

On Linux or macOS, use `./gradlew` (or `bash gradlew` if the checkout lacks the executable bit).

## Install and configure

1. In Rider, open **Settings | Plugins**, choose **Install Plugin from Disk**, and select the ZIP from `build/distributions/`. Restart Rider.
2. Disable any other AngelScript plugin that registers `.as` files or debugging. If the standalone **Unreal AngelScript Clang-Format** plugin is installed, uninstall or disable it to prevent two formatters from handling the same file. Highlighting uses Rider's native lexer/PSI and color scheme system; **TextMate Bundles** is not required.
3. Open the Unreal project or its `Script` folder, then open an `.as` file.
4. In **Settings | Tools | AngelScript**, use the **Bundled** language server. Rider detects Node.js in common locations; if detection fails, set the full path to `node.exe`.
5. Configure formatting in **Settings | Tools | AngelScript Clang-Format**. The plugin bundles Clang-Format; you can override its executable or style file per project. The default AngelScript style is used when the project has no `.clang-format` file.
6. Make sure the connection address and port match Unreal. The default is `127.0.0.1:27099`; Unreal can use `-asdebugport=<port>` to select a port. Changing the setting restarts the language server; reconnect active debug sessions.
7. To debug, create an **AngelScript** Run/Debug Configuration, start Unreal Editor, then launch the configuration.

Use **Navigate | Declaration or Usages** for navigation (`Ctrl+B` in the default IntelliJ keymap). Stopping the debugger disconnects the debug session; it does not close Unreal Editor.

## Release

Push a tag matching `pluginVersion` in `gradle.properties` to create a GitHub Release. CI builds and tests on Windows and Linux and runs Plugin Verifier on Linux before publishing the installable ZIP. Branch and pull-request builds validate changes but do not publish releases.

```powershell
git tag -a v1.9.4-rider.29 -m "Release 1.9.4-rider.29"
git push origin v1.9.4-rider.29
```

Update and commit `pluginVersion` before tagging a new release. Release notes can be added under `docs/releases/`; otherwise GitHub generates them. Download the plugin ZIP from [GitHub Releases](https://github.com/Bazhicc/rider-unreal-angelscript/releases), not the automatically generated source archive.

## Development and verification

```powershell
# Build and run the project checks
.\gradlew.bat check '-PlocalPlatformPath=C:\Program Files\JetBrains\JetBrains Rider'

# Include optional, read-only UE source regression checks
.\gradlew.bat test '-PlocalPlatformPath=C:\Program Files\JetBrains\JetBrains Rider' '-PtestEngineSource=C:\UE\Engine\Source'

# Check Rider API compatibility
.\gradlew.bat verifyPlugin '-PlocalPlatformPath=C:\Program Files\JetBrains\JetBrains Rider'

# Run the plugin in a Rider sandbox
.\gradlew.bat runIde '-PlocalPlatformPath=C:\Program Files\JetBrains\JetBrains Rider'

# Test the language server
npm --prefix language-server ci
npm --prefix language-server test
```

The `check` task covers the Kotlin plugin and language-server integration. `testNativeBackend` runs the focused C++ usage-search tests. After changing the shared PEG grammar, regenerate the TypeScript parser with `npm --prefix language-server run generate:grammar`.

The declared Rider compatibility range is `261.25134` through `261.*`; CI uses Rider 2026.1.3. A passing automated check does not replace hands-on validation in Rider and Unreal. In particular, debugging behavior and navigation results in a real UE project still need in-editor verification.

## Project layout

| Path | Contents |
| --- | --- |
| `src/main/kotlin/` | Kotlin lexer, parser, PSI, Rider integration, LSP, debugger, and formatter |
| `src/main/resources/` | Plugin registration and formatter styles |
| `src/test/` | Kotlin tests and parser migration baselines |
| `language-server/` | Hazelight TypeScript language server and integration tests |
| `backend/` | C# ReSharper C++ usage-search extension |
| `extension/` | Original VS Code extension |
| `docs/` | Implementation notes, validation records, and release notes |
| `build/distributions/` | Installable Rider plugin ZIPs |

Third-party sources and licenses are listed in [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md). The original Hazelight MIT license is preserved in [`LICENSE`](LICENSE).
