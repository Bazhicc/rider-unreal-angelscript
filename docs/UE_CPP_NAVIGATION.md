# Rider .17：完整路径使用悬停提示

2026-10-11，插件版本 `1.9.4-rider.17`。

沿用 `.16` 的公开头文件别名优先规则，修复 `FVector` 类型及 X/Y/Z 成员被同名反射/工具类型抢占的问题。此前 `.16` 的构建、测试、结构和 Rider 兼容性检查已通过，桌面验证被用户停止后尚未安装；本次合并路径提示修改交付 `.17`。

多个源码目标的候选弹窗显示完整函数签名、所属类型、文件名和行号；完整文件路径及行号放到每项的 tooltip。脚本函数重载和 C++ 源码候选共用此显示方式，单一目标继续直接导航。弹窗保留键盘选择和按签名/文件名筛选，打开所选目标时仍使用原始源码偏移。

# Rider .16：公开头文件别名优先

2026-10-11，插件版本 `1.9.4-rider.16`。

主 Rider 安装 `.15` 后，实际 XGame 的四个 Map 迭代器方法均进入正确实现，但 `Location.X` 的候选只有 `NoExportTypes.h:581` 的反射声明和 `TextureShareSDKUnrealEngineContainers.h:67` 的同名工具类型。此前别名回归没有同时加载这两处真实冲突文件。

查找现在先读取公开头文件及其直接 include 中的类型别名，再搜索其他同名定义；例如从 `Math/Vector.h` 的直接 include `Math/MathFwd.h` 取得 `FVector` → `UE::Math::TVector<double>`，继续按限定名查找。别名筛选遵守绑定/类型的命名空间。此规则不预设数学类型名称，也不递归展开整个引擎 include 图。

新增合成冲突用例，真实引擎回归同时加载 `NoExportTypes.h`、可用的 TextureShare 工具头和 `Bind_FVector.cpp`，检查类型及 X/Y/Z 成员的最终位置。修改前两项回归均复现错误。

# Rider .15：绑定实现的 C++ 命名空间

2026-10-11，插件版本 `1.9.4-rider.15`。

`.14` 安装后，实际 XGame 的 `It.GetKey()` 已有 C++ 目标，但主 Rider 跳到 `Public/Atoc/Iterator.h:157` 的 `Atoc::FMapIterator`。绑定文件实际注册的是全局 `FMapIterator`；按照文件名优先查找并忽略命名空间会把同名包装类当作绑定实现。新增回归直接包含真实 `Atoc/Iterator.h`，避免仅使用精选绑定头文件漏掉冲突。

`.15` 根据绑定中的 C++ 限定名或绑定声明的词法命名空间筛选实现类型；C++ 别名和基类保留限定名，继续查找时按各自命名空间解析。测试覆盖全局/同名命名空间类型、显式限定绑定、命名空间内绑定，以及 `FNativeVector` → `UE::Math::TVector` 的别名，防止修正迭代器后影响 UE 数学类型。

真实 UE 5.8.2 源码的 73 项 JVM 测试、7 项 Node 测试通过，0 失败、0 跳过。`check buildPlugin verifyPluginStructure verifyPlugin` 通过，Rider `RD-261.27258.81` 结果 Compatible。安装包自身 314 个类全部来自 Kotlin，0 Java 源码/实现类。

最终包 `build/distributions/UnrealAngelscript-1.9.4-rider.15.zip`，SHA256：`ad409469c07729f480bc26dad7e20c479353d7d308b321548fba4db141f81f5d`。内嵌 LSP SHA256：`757a36c4cad1f309cdc89eed39e512bb68a19be6cf50b0d82ca272aa34370163`。

记录：`build/cpp-navigation-namespace-final.log`、`build/cpp-navigation-namespace-ue58-tests.json`、`build/cpp-navigation-namespace-package-audit.json`；实际 XGame 离线符号探针为 `build/cpp-navigation-xgame-offline-probe.json`。

UE 5.4.4 源码的 73 项 JVM 回归同样通过，0 失败、0 跳过。主 Rider 14:02:45 日志确认加载 `.15`，已安装 JAR 与最终包逐个 SHA256 一致。实际 XGame 中 `It.GetKey()` → `Bind_TMap.h:548`、`It.RemoveCurrent()` → `:537`、`It.GetValue()` → `:559`、`It.SetValue()` → `:570`，均属于全局 `FMapIterator`。继续验证 `FVector.X` 时发现上述别名优先级问题，因此继续修复为 `.16`。

# Rider .14：无 UE 类型库的 auto/foreach 接收者

2026-10-11，插件版本 `1.9.4-rider.14`。

主 Rider 安装 `.13` 后，用户 `ReplaceDynamicScores` 中的 `It.GetKey()` 仍提示找不到声明。原因是未连接 UE 类型数据库时，离线解析把 `auto` 循环变量当成集合表达式本身，无法取得 `Iterator().Iterate()`（旧协议 `Proceed()`）的结果类型。此前迭代器集成用例注入了模拟 UE 类型库，未覆盖此状态。

LSP 现在传递接收者的声明类型及源码操作链；Kotlin 从当前索引的绑定签名读取泛型参数和方法/属性返回类型，逐步实例化接收者，再复用原生源码导航。覆盖 foreach、显式 Iterator 调用、auto 别名、连续方法与属性访问以及嵌套泛型；Array/Set 循环取元素类型，Map 循环取其实际迭代协议返回类型。仅迭代协议的名称来自语言规则，没有容器、项目函数或迭代器名称白名单。无法证实返回类型时不猜测同名成员。

回归在无 UE 数据库状态下检查这些 LSP 返回信息；Kotlin 用未预设的 FutureBag/FutureCursor/FutureSequence 类型验证规则，并对真实 UE 5.8/5.4 Map 绑定检查 GetKey、GetValue、RemoveCurrent、SetValue 的最终实现。只读 stdio 探针对实际 XGame 的 `FixtureActor.as:130/132/135/138` 均返回了正确的集合声明和 foreach 来源，同时保留 Actor、项目静态函数及 SpawnActor 的原生符号信息。

自动化：两版引擎各 72 项 JVM 测试、7 项 LSP 测试通过；构建、结构与 Rider 兼容检查通过，插件自身 314 个类全部来自 Kotlin。主 Rider 13:54 已加载 `.14`，已安装 JAR 与构建包逐个 SHA256 一致；实际 GetKey 目标仍存在上述命名空间问题，因此继续修复为 `.15`。

# Rider .13：实际 XGame 的 UE 5.8 容器声明

2026-10-11，插件版本 `1.9.4-rider.13`。

安装 `.12` 并重启主 Rider 后，实际 XGame 的 `AActor`、`GetActorLocation`、`SetActorLocation`、项目 `UAtocNativeCallFixture::Invoke` 和全局 `SpawnActor` 已能跳到相应 C++ 声明，未再出现栈溢出。继续验证发现 `TMap` 跳到 Chaos 独立兼容类型，`TSet` 长时间停在 Resolving reference；JVM 线程转储确认卡在插件的别名解析，而非 Rider 导航动作路由。

实际工程使用 `F:/Projects/UE-Angelscript-Hotfix/Engine`，版本 5.8.2。此前源码检查使用的是 `E:/Engines/UE_5.4.4_Angelscript/Engine`。5.8 的公开容器头文件通过宏注入 `.h.inl`，没有字面的 `class TMap`/`class TSet`；仅识别字面名称会漏掉真正的声明，随后选中同名兼容类或扫描大量使用文件。

`.13` 沿公开头文件的 `.inl`/`.inc` 引用保留每次 include 的对象宏上下文，识别 `UE_JOIN` 和 token paste，展开声明名及基类名，同时保留原文件 UTF-16 偏移。宏注入的真实声明优先于独立兼容定义，继承方法继续在同一源码上下文中查找。别名解析限定在单条声明内，并从末尾读取 typedef 名称，避免正则重复扫描长标识符。没有加入类型或函数白名单。

这是源码导航回退，不是完整 C++ 预处理器；不会假定 Rider 后端的活动编译宏。`TSet` 的稀疏和紧凑条件实现均保留为候选，显示公开类型名、具体文件和行号。真实 UE 进程仍未启动。

自动化分别使用真实 UE 5.8.2 和 5.4.4 源码，各 71 项 JVM 测试通过，0 失败、0 跳过；7 项 Node 测试通过。`check buildPlugin verifyPluginStructure verifyPlugin` 通过，Rider `RD-261.27258.81` 结果 Compatible。安装包自身 310 个类全部来自 Kotlin，0 Java 源码/实现类，内嵌 LSP 未变。

最终包 `build/distributions/UnrealAngelscript-1.9.4-rider.13.zip`，SHA256：`f38802e612731822456a570832318e2d5773fb49447a6419f39a77fc216c003c`。主 Rider 13:42 重启后日志确认已加载 `.13`。

记录：`build/cpp-navigation-ue58-final.log`、`build/cpp-navigation-ue54-regression.log`、`build/cpp-navigation-ue58-tests.json`、`build/cpp-navigation-ue54-tests.json`、`build/cpp-navigation-ue58-package-audit.json`、`build/rider-tset-thread-dump.txt`。

主 Rider 的实际 XGame 验证：`TMap` → Core `Map.h.inl:1179`；`TSet` → 含路径和行号的两个条件实现候选，选择后到 `SparseSet.h.inl:58`；`DynamicKeys.Add` → `Bind_TSet.h:367`；`ScoresById.Add` → `Bind_TMap.h:666`。继续检查用户的 `ReplaceDynamicScores` 时，`auto It` 的 `GetKey` 仍找不到声明，因此 `.13` 没有作为完整修复交付；无数据库迭代器问题见 `.14` 记录。

# Rider .12：C++ 查找过程中的栈溢出

2026-10-11，插件版本 `1.9.4-rider.12`。

用户安装 `.11` 后报告 C++ 跳转全部失败。读取主 Rider 日志确认已加载 `.11`，`Go to Declaration or Usages` 操作在 13:09 连续抛出 `java.lang.StackOverflowError`，栈内重复出现正则字符分支与循环。相关日志摘录保存在 `build/cpp-navigation-runtime-excerpt.log`。

声明解析和绑定解析使用的字符串正则含重复分支，长字面量会耗尽正则回溯栈。新增两个端到端源码查找用例，分别在头文件和绑定文件中放入 90,000 字符的转义字符串；修改前两个用例均以相同栈溢出失败。两处重复改为占有式量词：字符串内部不可能匹配结束引号，无需回溯，不改变返回的源码偏移或跳转目标。

回归验证覆盖两个长字符串用例、真实引擎 `Private/Binds` 下全部 `.cpp` 及 `Public/AngelscriptBinds` 下全部 `.h` 的绑定解析，以及 `SpawnActor`、`AActor.GetActorLocation`、`AActor.SetActorLocation` 的最终源码目标。69 项 JVM 测试、7 项 Node 测试通过，0 失败、0 跳过。

此前 `.11` 的沙箱验证使用精选源码副本，没有覆盖长字面量导致查找中断的情况。后续已在用户主 Rider 安装 `.12` 并验证上述 Actor、项目静态函数及 SpawnActor；容器的 UE 5.8 差异见 `.13` 记录。真实 UE 进程仍未启动。

`check buildPlugin verifyPluginStructure verifyPlugin` 通过，Rider `RD-261.27258.81` 检查结果 Compatible。安装包审计确认自身 304 个类仍全部来自 Kotlin，0 Java 源码/实现类，内嵌 LSP 未变。

安装包 `build/distributions/UnrealAngelscript-1.9.4-rider.12.zip`，SHA256：`87cca9ff6a238fc5f17414410a20ee83bcd2a43703f9e563a67c622ea538e754`。

记录：`build/cpp-navigation-stack-red.log`、`build/cpp-navigation-stack-targeted.log`、`build/cpp-navigation-stack-final.log`、`build/cpp-navigation-stack-package-audit.json`。

# Rider .11：容器、共用绑定与候选显示

2026-10-11，插件版本 `1.9.4-rider.11`。

## 修复内容

- 类型查找只接受实际定义，排除模板参数、前置声明、friend 和类型使用；支持 `alignas`、C++ 属性与 API 宏。`TMap`/`TSet` 优先跳到公开模板定义。
- 方法先按 AngelScript 注册定位真实实现；没有匹配绑定时沿 C++ 基类查找。避免把脚本 `TMap.Add`、`TSet.Add` 直接当成同名 C++ 模板重载。UE 提供的原生别名继续优先使用。
- 共用绑定辅助函数中的 `FAngelscriptBinds&` 参数会继承调用处的类型，可继续传递到下一层辅助函数；修复软对象指针等批量注册的方法遗漏。
- 多个原生或脚本函数目标显示签名、声明所属类型、文件路径及行号；单个脚本目标保留原始 PSI。
- 先求所属类型与绑定 API 的文件索引交集，再读取源码，避免解析大量仅使用 `TMap`/`TArray` 的文件。绑定缓存继续按文件修改时间失效。

## 回归范围

`AngelscriptNativeSourceResolverTest` 直接使用与 Rider 相同的解析链，验证最终文件、偏移和候选签名。真实 UE 5.4.4 源码以只读方式提供给测试，包含：

- `TMap`、`TSet` 类型和实际绑定文件中的全部方法，以及 Map/Set 迭代器；无绑定源码时，`TMap.Add` 沿两层基类找到八个独立 C++ 重载。
- `TArray`、数组迭代器、`TOptional` 的注册方法；`TSoftObjectPtr`/`TSoftClassPtr` 共用辅助函数中的方法。
- `TSubclassOf`、`TLazyObjectPtr`、`TSharedPtr`、`TSharedRef`、`TWeakPtr` 的类型声明；`FVector`、`FQuat`、`FRotator` 的 UE5 别名。
- 模板参数伪定义、继承、同名但不同绑定实现、Lambda、多个重载、共用辅助函数嵌套及原生/脚本候选显示。

stdio LSP 测试覆盖未加载 UE 类型库和已加载模拟类型库两种状态下的 Map/Set 类型、Add/Contains/Remove/Empty/Num、FindOrAdd、Set 迭代器；保留先前新增函数与数据库刷新测试。

上述测试验证插件的查找规则和返回位置；真实 UE 进程联调仍按此前确认的“暂不联调”处理。

自动化：66 项 JVM 测试、7 项 Node 测试通过，0 失败、0 跳过；插件结构与 Rider `RD-261.27258.81` 兼容性检查通过。安装包审计确认插件自身 304 个类均来自 Kotlin，0 Java 源码/实现类，内置 LSP 与当前服务器构建一致。

## Rider 沙箱实测

使用 `Rider 2026.1.5.2` 独立沙箱打开 `build/rider-goto-fixture`，索引真实引擎头文件/绑定源码的副本；没有启动 UE，也未修改用户主 Rider 工程。

| 脚本引用 | 实际目标 |
| --- | --- |
| `TMap<int, int>` | `Map.h:1162`，`class TMap` |
| `TSet<int>` | `Set.h:285`，`class TSet` |
| `Values.Add(1, 2)` | `Bind_TMap.h:644`，`FAngelscriptMapBinds::Add` |
| `UniqueValues.Add(1)` | `Bind_TSet.h:367`，`FAngelscriptSetBinds::Add` |
| `Values.FindOrAdd(1)` 的第二个候选 | 弹窗完整显示签名及相对路径/行号，选择后跳到 `Bind_TMap.h:688`；第一个候选为 `FindOrAdd_Defaulted`，第 687 行 |

最终安装包 `build/distributions/UnrealAngelscript-1.9.4-rider.11.zip`，SHA256：`bd842cff1c31f3304b9a574a157e2716a8d6d5a35590c30cbc1aecd40e8807ba`。

记录：`build/container-navigation-final.log`、`build/container-navigation-rider.log`、`build/container-navigation-rider-final.log`、`build/container-navigation-package-audit.json`。真实用户 UE 工程的端到端行为未宣称完成验收。

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
