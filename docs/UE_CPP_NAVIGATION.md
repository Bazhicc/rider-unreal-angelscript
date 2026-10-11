# Rider .9：通用符号导航

2026-10-11，插件版本 `1.9.4-rider.9`。本次针对未覆盖的符号类别修复通用解析规则，不维护引擎函数名白名单。

## 修复链路

- 语言服务器运行时先使用接收者、作用域和调用参数解析符号，防止原生 PSI 按名称搜索到无关的同名脚本成员；离线脚本导航回退保留。
- 静态类命名空间、全局/命名空间函数、泛型基类型、访问器和枚举值均返回原生声明信息。泛型实例保留结构体标记、绑定名和声明元数据。
- 平衡解析泛型参数并递归代换，支持 `TMap<K,TArray<V>>` 等嵌套容器；迭代器返回值继续参与 `auto` 和链式表达式推断。
- 结合完整调用的参数数量、命名参数、默认值和参数类型筛选重载；结果类型推断和跳转使用相同规则。决定返回类型的参数按声明名称映射，支持具名参数、参数重排及直接返回值成员链。
- Kotlin 在索引源码中读取 `ValueClass`、`ReferenceClass`、`ExistingClass`、`Method`、`Property`、全局注册及 `FNamespace`；从函数指针、`FUNC`/`METHOD` 宏提取实际 C++ 名称，Lambda 定位到绑定实现。
- 绑定缓存按源码文件修改时间失效；UE 数据库刷新时重新生成受影响的泛型实例、重新解析脚本缓冲区。新增并注册的函数不需要修改插件规则。

## 自动化验证

56 项 JVM 测试通过，0 失败、0 跳过；7 项 Node 测试通过。旧的 172 份词法/语法基线保留。
只读检查本机 UE 5.4.4 的真实头文件及 `Bind_AActor.cpp`、`Bind_TMap.cpp`；没有改动引擎或用户工程。

回归文件 `language-server/test/fixtures/NativeNavigation.as` 包含用户提供的 `SpawnFixtureActor`、`NativeCallProbe`、`ReplaceDynamicScores`，并覆盖：

- 静态函数、根全局/命名空间函数、同名脚本/原生重载、继承成员、访问器、类型和枚举值。
- `TMap` 迭代器的 GetKey/GetValue/SetValue/RemoveCurrent、迭代器副本、数组迭代、嵌套 Map/Array、返回值成员链。
- 运行中追加 UE 静态方法及泛型成员，以及未保存缓冲区中新加的脚本函数，无需重启服务器。
- 迭代器和 SpawnActor 结果类型的悬停信息、成员补全及原有签名/重命名等 LSP 功能。

标准 IntelliJ Platform 单测没有 Rider ReSharper solution 会话，因此实际文件索引到编辑器这一层单独使用 Rider 沙箱验证，不将纯解析测试冒充为真实跳转。

## Rider 沙箱实测

使用 `.9` 沙箱构建，在 `build/rider-goto-fixture` 中连接模拟 UE 数据库并实际执行 Ctrl+B：

| 脚本符号 | 结果 |
| --- | --- |
| `UAtocNativeCallFixture::Invoke` | 从用户工程只读复制的 `NativeCallFixture.h` 第 14 行 `Invoke` |
| `SpawnActor(AActorFixture)` | 从真实引擎只读复制的 `Bind_Actor.h` 第 39 行 `FAngelscriptActorBinds::SpawnActor` |
| `It.GetKey()` | 真实 `Bind_TMap.h` 第 544 行 `FMapIterator::GetKey` |
| `It.SetValue(Replacement)` | 真实 `Bind_TMap.h` 第 566 行 `FMapIterator::SetValue` |
| 新增 `Iterator.LaterAdded()` | 首次查询无目标；运行中加入 UE 绑定和源码后，定位 `NewBindings.cpp` 第 3 行 Lambda 绑定 |
| 新增 `UAtocNativeCallFixture::LaterAdded(Actor)` | 首次查询无目标；运行中增加类型信息和声明后，定位 `NativeCallFixture.h` 第 15 行 |

新增函数测试没有重启 Rider 或插件。测试通过工具在 IDE 外写入源码后，使用 Rider Reload All from Disk 刷新外部文件索引；类型数据库通过原连接增量推送。此结果证明插件不需要新增函数名规则，不代表跳过 UE 类型库或 IDE 索引更新也能定位声明。

沙箱验证后补充修复具名参数的输出类型映射；先用 `NamedSpawned.AddScore` 复现失败，再通过最终安装包的完整自动化检查。该补充未重复 UI 验证，Kotlin 导航入口没有变化。

`check buildPlugin verifyPluginStructure verifyPlugin` 通过；RD-261.27258.81 Compatible，50 处弃用 API 使用、13 处实验 API 使用。
安装包内 296 个插件类的 SourceFile 均为 `.kt`；没有 Java 源文件，内嵌语言服务器与构建产物一致。
安装包 `build/distributions/UnrealAngelscript-1.9.4-rider.9.zip`，SHA256：`310b1b5b82c61aa7db45104a9dbf6a097cb69c9c3ed09307a8d6354c205a41dc`。
日志：`build/native-navigation-final.log`、`build/native-navigation-lsp.log`、`build/native-navigation-rider.log`、`build/native-navigation-mock.jsonl`、`build/native-navigation-package-audit.json`。

使用条件：函数须已暴露给 AngelScript，UE 类型库已刷新，Rider 已索引工程/引擎头文件及手工绑定源码。真实 UE 进程和源码访问器的端到端联调继续暂缓。

# Rider .8：UE/C++ 声明跳转记录

2026-10-11，插件版本 `1.9.4-rider.8`，本机 Rider `2026.1.5.2 / RD-261.27258.81`。

此前 Ctrl+B 仅请求 `textDocument/definition`，官方语言服务器的该接口只返回脚本位置。
UE 导航位于 `textDocument/implementation`；原有可选头文件搜索默认关闭，只识别函数、类型和枚举，缺少普通字段。

现在脚本定义仍优先。没有脚本目标时，Bundled 语言服务器通过无副作用的 `angelscript/cppSymbol` 请求返回原生符号的声明类型、种类及 C++ 绑定名。
Kotlin 导航器使用包含引擎库的索引范围查找头文件，在所属类型范围内定位声明，避免同名成员相互干扰。
继承成员使用实际声明类型；存在 `unrealname` 时优先使用绑定名，例如 `GetActorLocation` 对应 `K2_GetActorLocation`。
读取 UE5 `MathFwd.h` 的类型别名后，可以定位 `FVector` 的 `TVector<double>` 声明和匿名结构/联合体中的 `X` 字段。
头文件位置使用明确的文件偏移导航，支持 Rider 前端只有粗粒度 C++ PSI 的情况。

没有头文件目标时，反射类、函数及类属性保留 UE 导航兜底。查询目标或 Ctrl+悬停不发送打开编辑器命令，仅实际导航时请求 implementation。
本机 UE 的协议不能直接导航结构体字段及枚举，因此这些符号需要对应头文件已索引。
该扩展通过 LSP capability 协商；外部旧语言服务器不会被调用不支持的自定义请求。

## 验证

- 53 项 JVM 测试通过，0 失败、0 错误、0 跳过；保留的 172 份词法/语法行为基线通过。
- 4 项 stdio LSP 集成测试通过；模拟 UE 覆盖原生方法、类属性、结构体字段、类/结构体/枚举类型、继承成员、绑定名及实际 GoToDefinition 数据包。
- 只读使用本机 UE 5.4.4 的 `Actor.h`、`Vector.h` 和 `MathFwd.h` 验证用户的 MoveOther 用例；没有启动或修改引擎。
- `check buildPlugin verifyPluginStructure verifyPlugin` 通过；Plugin Verifier 对上述 Rider 报告 Compatible，57 处弃用 API 使用、13 处实验 API 使用。
- 安装包中 292 个插件类的 SourceFile 全部为 Kotlin，没有 Java 源文件；内嵌服务器与本次源码构建结果一致。

在独立 Rider 沙箱中打开 `build/rider-goto-fixture`，使用模拟 UE 类型数据库及隔离的测试头文件，实际执行 Ctrl+B：

| AngelScript 位置 | 实际结果 |
| --- | --- |
| `AActor` 类型 | `Actor.h` 第 1 行类声明 |
| `FVector` 类型 | `Vector.h` 第 3 行 `TVector` 声明 |
| `Location.X` | `Vector.h` 第 9 行 `X` 字段 |
| `Other.GetActorLocation()` | `Actor.h` 第 4 行 `K2_GetActorLocation`，直接跳转，无别名重复候选 |
| `Other.SetActorLocation(Location)` | `Actor.h` 第 5 行 `K2_SetActorLocation`，直接跳转 |
| `ETeleportType` 类型 | `EngineTypes.h` 第 1 行枚举声明 |
| 无测试头文件的 `UOnlyBridge` | 模拟 UE 收到 TypeName 为空、SymbolName 为 `UOnlyBridge` 的导航帧；没有伪称已打开真实 UE 源码 |

沙箱测试没有修改用户主 Rider 工程。真实 UE 进程及源码访问器端到端联调继续按此前确认的“暂不联调”处理。
日志：`build/ue-navigation-final.log`、`build/ue-navigation-rider.log`、`build/ue-nav-mock.jsonl`、`build/ue-navigation-package-audit.json`。

## 使用条件

1. 安装 `.8`，在 `Settings > Tools > AngelScript` 使用 Bundled 语言服务器。
2. 打开带 AngelScript 的 UE，让语言服务状态显示 UE types loaded。
3. 在 Rider 打开 UE 工程并完成引擎/项目头文件索引；仅打开 Script 文件夹通常没有引擎源码索引。
4. 使用 Navigate > Declaration or Usages，IntelliJ 键位默认 Ctrl+B；语义导航不需要打开 Experimental 头文件正则搜索开关。
5. 使用 UE 导航兜底时，将 UE 的源码编辑器设置为 Rider。

可重复的本机源码检查：

```powershell
.\gradlew.bat check buildPlugin verifyPluginStructure verifyPlugin '-PlocalPlatformPath=D:\Program Files\JetBrains\JetBrains Rider' '-PtestEngineSource=E:\Engines\UE_5.4.4_Angelscript\Engine\Source'
```

安装包：`build/distributions/UnrealAngelscript-1.9.4-rider.8.zip`。

SHA256：`f934a20e99584179f3d611607ccd5729f415e36c7a4e8bf1626aa30e22af2681`。
