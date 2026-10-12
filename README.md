# Unreal AngelScript for Rider

面向 **JetBrains Rider 2026.1** 的 Unreal AngelScript 插件，直接打包本仓库的
Hazelight 1.9.4 语言服务器。无需安装 VS Code 或另外配置语言服务器仓库。
Rider 前端基于 [aesirinteractive/rider-unreal-angelscript](https://github.com/aesirinteractive/rider-unreal-angelscript)
的 `bf2841c37a3e60326b3f1dde2a4418a3bff33e2f` 版本改造。

## 功能

- `.as` 文件识别、官方 TextMate 语法高亮、原生 PSI 高亮、LSP 语义颜色和原生结构视图。
- 通过 Rider LSP 提供补全、悬停、定义跳转、引用搜索、重命名、代码操作、代码块折叠、逐层扩大选择和 UE 编译诊断。
- AngelScript 调试配置：附加到运行中的 UE，支持断点、暂停、继续、单步、调用栈、变量及表达式求值。
- 调试悬停支持成员链与下标；行内变量使用官方语言服务器提供的参数、局部变量和成员赋值信息。
- UE/C++ 类型、函数及成员跳转：结合语言服务器的所属类型信息与已索引源码，支持绑定实现、类型别名、嵌套类型及继承；反射符号可转交 UE 导航。
- 使用 Rider 的 Language Services 状态栏提示显示 LSP 状态及 UE 类型加载状态，提供原生重启和设置入口。
- UE 资产编辑得到客户端成功确认后再保存；未成功应用时显示错误并且不主动保存。
- 支持直接打开 `Script` 文件夹，也支持打开 `.uproject` 所在工程目录；自动识别工程和插件的 `Script` 根目录。
- 文件 URI 保留正确转义，跳转、重命名和诊断支持包含空格、中文及 `#` 的目录。
- 使用 Rider 自带的 clang-format，可选择自定义路径和 `.clang-format-angelscript`。
- 保留原有 VS Code 扩展源码，其说明位于 [extension/README.md](extension/README.md)。

脚本内部及跨 `.as` 文件的定义跳转、补全、悬停和重命名无需启动 Unreal Editor。
语言服务器运行时，定义跳转优先使用接收者类型、作用域和调用参数解析目标；离线时保留原生 PSI 引用回退。
语义重命名通过公开 Rider 接口请求完整编辑，在同一个全局撤销事务中更新声明和引用；跨文件改名可一次撤销。
局部变量、参数、成员、类型及脚本声明保留原生解析路径，避免语言服务器状态影响基本跳转。
UE 原生类型补全和编译诊断需要运行带 AngelScript 的 Unreal Editor；收到类型数据库后会重新解析当前缓冲区，保留未保存编辑。
Rider 的 LSP 支持范围以 JetBrains 平台为准；VS Code 的专用 API 浏览器和数据断点界面尚未移植。

## 新增与优化

截至 `1.9.4-rider.29`，本工程在原 Rider 插件基础上完成以下改进：

| 方向 | 功能与优化 |
| --- | --- |
| Kotlin 实现与原生编辑能力 | 词法器、解析器、结构化 PSI、引用、重命名和调试器全部使用 Kotlin，移除 Java 源码及旧代码生成链；保留原有语法规则、错误恢复、Unicode 词法规则和迁移前快照回归。 |
| AngelScript 语法兼容 | 支持冒号和等号命名参数、`asset` 声明、算术右移 `>>>` 和布尔异或 `^^`；格式化字符串插值支持函数调用、嵌套三元表达式、命名参数及 `:04d` 格式说明，保留插值中的导航和引用查找。Kotlin PSI 与内嵌语言服务器共同覆盖相关语法，同时保留嵌套泛型解析。 |
| AS → UE/C++ 导航 | 覆盖类型、实例/静态函数、全局/命名空间函数、访问器、字段和枚举值；类型优先定位公开 C++ 定义，成员优先定位 AngelScript 注册的实际实现，再沿基类查找。支持函数指针、绑定别名、共享绑定辅助函数和 Lambda；没有独立声明的 Lambda 定位到绑定源码。 |
| C++ 光标位置 | 未打开源码先统一为编辑器使用的 UTF-16/LF 文本，修复 CRLF 换行累计造成的光标偏移；已缓存文档优先保留未保存内容。类型、字段、函数、重载候选和宏/包含片段使用相同的位置基准，候选行号也无需加载原生 PSI 文档。 |
| C++ 导航速度 | 内嵌 LSP 用一次请求返回脚本定义或原生符号；按工程缓存词索引、头文件、宏展开、绑定及目标，多个调用复用解析结果。缓存有数量上限，C++ 未保存修改、文件增删/重命名、包含片段、工程根目录及索引重建会使缓存失效，缺失结果也会刷新。 |
| C++ → AS 用法 | 通过 Rider/ReSharper 原生搜索扩展，把 AS 语义调用加入 C++ Find Usages/Show Usages 的结果；核对 C++ 声明文件及位置，支持绑定别名、继承、包装函数和明确的重载签名，不增加选择目标的入口。按模块缓存 AS 引用，代码或类型库更新后重新解析；搜索结果遵守原生搜索范围，使用 UTF-16 行列位置兼容 CRLF。 |
| 搜索取消与界面响应 | 原生用法查询使用可取消、让出写操作的后台读动作，单阶段和整个请求均有超时限制，超时后取消工作。反向绑定查询先通过词索引筛选当前名称和绑定头文件的交集，再读取源码，避免遍历全引擎包含关系；已打开的文档保留未保存内容，其他源码直接读取 VFS。 |
| 搜索语法树稳定性 | 源码回退只遍历语法节点并跳过已访问节点，避免 `for` 初始化语句中语义注解的循环引用引发栈溢出，导致整个工作区的 C++ 用法查询失败。 |
| 容器、迭代器与成员链 | 支持 `TMap`、`TSet`、`TArray` 的类型及成员导航，从绑定签名推导 `auto`、`foreach`、迭代器和连续成员访问的类型，保留嵌套泛型参数；覆盖 Map 迭代器的 `GetKey`、`GetValue`、`SetValue`、`RemoveCurrent`。 |
| 类型别名与作用域 | 统一按命名空间和所属类解析类型、别名及继承，支持相对限定名、全局 `::`、嵌套模板类型和多继承分支；修复全局容器误跳到 `uLang::TMapG/TSetG/TArrayG`、局部别名干扰和基类查找丢失作用域的问题，同时保留合法的 `uLang` 类型及 `FVector → UE::Math::TVector` 等别名。 |
| UE 头文件与宏声明 | 支持 UE 5.8 通过 `.inl`/`.inc` 注入的容器声明、`FString` 的公开头文件及宏实现、`FVector.X/Y/Z` 等字段；有限展开当前文件和直接包含头文件中的函数式宏，支持字符串化、拼接及可变参数，保留跳转到原始源码的位置。排除能确定无效的条件分支及 include。 |
| 部分类型库回退与自动刷新 | UE 类型库尚未到达或缺少当前符号时，明确的类型、接收者、继承及静态/全局调用仍可使用源码回退。新增脚本声明随编辑更新；UE 类型库刷新后重新解析新增绑定，源码查询读取当前绑定和头文件内容，无需为新增函数维护名称白名单。 |
| 跳转候选与路径提示 | 多个目标显示完整函数签名、实际声明所属类型、文件名和行号；完整路径放在悬停提示中。列表使用不持有 PSI 的独立模型，修复 Rider 多目标跳转弹窗断言。保留按签名/文件名筛选及键盘选择，单一目标直接跳转。 |
| 定义与用法方向 | 在函数声明名称上执行 Ctrl+B/Ctrl+点击时查找用法，在函数调用处导航到定义。Find Usages 使用单一原生入口，消除原生 PSI 与通用 LSP 同时注册造成的多余目标选择框；在线请求 LSP 语义引用，离线使用原生 PSI 引用，结果遵守搜索范围并排除声明本身。返回类型、默认参数表达式和递归调用仍按引用导航。 |

UE 类型数据库用于完整的绑定、重载及泛型返回类型语义导航；源码回退不需要开启 Experimental 正则搜索。
请在 Rider 打开 UE 工程并完成源码索引，仅打开 `Script` 文件夹通常没有引擎头文件索引。
C++ 函数须已暴露给 AngelScript，相关头文件和绑定源码须在索引范围内。反射符号的兜底导航使用 UE 配置的源码编辑器，请选择 Rider。

源码回退采用有限解析，没有读取完整 C++ 编译命令；无法确定的外部宏条件可能保留多个实现候选。
详细实现、回归用例及验证边界见 [UE/C++ 导航记录](docs/UE_CPP_NAVIGATION.md) 和 [功能保留清单](docs/RIDER_FEATURE_PARITY.md)。

## 实现方式

Rider **前端使用 Kotlin**，包括完整词法器、解析器、结构化 PSI、引用和调试器，不包含 Java 源码或 Java 代码生成步骤。C++ 原生用法搜索增加一小层 **C# ReSharper 后端**，复用 Kotlin 的绑定解析和语言服务器语义引用；两端通过仅监听本机回环地址、带随机令牌的工程级接口通信。[Rider SDK 架构说明](https://www.jetbrains.com/help/resharper/sdk/Rider.html)。高亮复用原始 `extension/syntaxes/angelscript.tmLanguage.json`，
通过 Rider 内置 TextMate 引擎加载；补全、悬停、引用、重命名、结构与语义分析复用本仓库的
TypeScript 语言服务器；原生 PSI 提供引用、声明、作用域和离线结构视图。
19 种官方 `as_*` 语义标记映射到 Rider 颜色设置。

Kotlin 解析器保留原有完整语法规则、运算符优先级、错误恢复和 PSI 类型；Unicode DFA 表保留原有词法规则。
迁移前的 Java 版词法和语法树快照作为回归基线；LSP 继续负责完整语义分析和 UE 类型信息。
Kotlin 插件运行在 Rider 自带 JVM 上，构建仍需 JDK，使用插件无需另外安装运行时。

共享服务器语法同时兼容 `DefaultTarget(Third: 7, First: 19)` 和原有 `Third = 7` 写法，
支持嵌套调用、三元表达式及命名空间访问。其他核心语言分析保持官方实现。

## 构建

需要 Node.js 22 或更新版本、JDK 21 或更新版本，以及能构建 `net9.0` 的 .NET SDK（9 或更新版本）。无需全局安装 Gradle；安装插件后使用 Rider 自带的 JVM 和 .NET 后端运行时。

```powershell
$env:JAVA_HOME = '你的 JDK 或 Rider 的 jbr 目录'
.\gradlew.bat buildPlugin
```

首次构建自动下载 Gradle、目标 Rider SDK 和 npm 依赖，并构建、打包本工程的语言服务器。
也可以复用已经安装的 Rider，避免下载完整 SDK：

```powershell
.\gradlew.bat buildPlugin '-PlocalPlatformPath=D:\Program Files\JetBrains\JetBrains Rider'
```

生成的安装包位于 `build/distributions/`，文件名包含 `gradle.properties` 中的插件版本；
当前版本为 `UnrealAngelscript-1.9.4-rider.29.zip`。
按 [项目构建产物规则](AGENTS.md)，只保留最近成功构建的 3 个不同插件版本及其配套产物；
同一版本重复构建不增加版本数量，失败构建不替换已保留版本，测试记录不参与清理。
Linux/macOS 使用 `./gradlew`；如果检出后缺少执行权限，执行 `bash gradlew`。

## 安装和使用

1. 在 Rider 中打开 `Settings > Plugins > 齿轮 > Install Plugin from Disk`，选择构建得到的 ZIP，然后重启。
2. 如果已安装其他 AngelScript 插件，先禁用它，避免同时注册 `.as` 文件类型和调试配置。
   Rider 内置的 TextMate Bundles 插件需要保持启用。
3. 打开 UE 工程或 `Script` 文件夹，打开一个 `.as` 文件以启动语言服务器。
4. 在 `Settings > Tools > AngelScript` 中保留 `Bundled` 语言服务器模式。
   Node.js 默认自动探测常见安装路径；探测失败时填写 `node.exe` 的完整路径。
   定义跳转使用 `Navigate > Declaration or Usages`；快捷键取决于键位方案，IntelliJ 默认是 `Ctrl+B`。
5. 确认连接地址和端口与 UE 一致，默认 `127.0.0.1:27099`。修改后语言服务器自动重启；
   正在运行的调试会话需重新附加。UE 可以通过 `-asdebugport=端口` 指定端口。
6. 调试时添加 `AngelScript` Run/Debug Configuration，运行 Unreal Editor，然后启动该调试配置。

在 C++ 声明上使用原生 Find Usages/Show Usages，会把匹配的 AS 引用加入同一结果列表。
此功能需要打开包含 C++ 与 AS 源文件的 UE 工程，完成索引，并使用支持原生引用查询的内嵌语言服务器。
搜索根据 AS 语义、绑定源码和 C++ 声明位置匹配；字符串反射调用不作为直接函数引用。
`.24` 新增原生后端，安装后须重启 Rider。

停止 AngelScript 调试会话只断开调试连接，不退出 Unreal Editor。
求值和变量请求在会话内按回复类型排队，回调始终属于发起请求的栈帧。
继续执行会取消旧请求；已发送请求的迟到回复被丢弃。请求超过 5 秒无回复时重建连接，
防止没有请求编号的 UE 协议串用结果。关闭自动重连时，此情况会停止调试会话。
停止会话或关闭工程会解除监听并清理待处理请求、行内值和连接。

## 验证与开发

```powershell
# 结构化 PSI、原生引用与跳转入口、结构视图、TextMate、格式化、调试协议和 stdio LSP 集成测试
.\gradlew.bat check '-PlocalPlatformPath=D:\Program Files\JetBrains\JetBrains Rider'

# 可选：加入本地 UE 头文件和 AngelScript 绑定源码回归，仅只读源码，不启动 UE
.\gradlew.bat test '-PlocalPlatformPath=D:\Program Files\JetBrains\JetBrains Rider' '-PtestEngineSource=D:\UE-Angelscript\Engine\Source'

# 使用 Plugin Verifier 检查本地 Rider 的二进制 API 兼容性
.\gradlew.bat verifyPlugin '-PlocalPlatformPath=D:\Program Files\JetBrains\JetBrains Rider'

# 单独验证 C++ 原生用法搜索扩展的搜索阶段、范围、去重和位置转换
.\gradlew.bat testNativeBackend '-PlocalPlatformPath=D:\Program Files\JetBrains\JetBrains Rider'

# 在独立的 Rider 沙箱中启动插件
.\gradlew.bat runIde '-PlocalPlatformPath=D:\Program Files\JetBrains\JetBrains Rider'

# 单独验证语言服务器
npm --prefix language-server ci
npm --prefix language-server test

# 修改共享 PEG 语法后重新生成 TypeScript 服务器使用的 JavaScript 解析器
npm --prefix language-server run generate:grammar

```

原生语法直接维护 `AngelscriptParser.kt`、`AngelscriptLexer.kt` 和 `psi/` 下的 Kotlin 类型；
词法 DFA 数据位于 `AngelscriptLexerTables.kt`。修改规则后运行解析、引用和基线测试。
`verifyNoJavaSources` 构建检查阻止重新加入 Java 源码及旧 BNF/Flex 生成输入。

`gradle.properties` 固定插件和目标 Rider 版本；当前兼容声明为 `261.25134` 至 `261.*`。
调整目标大版本时，应同步检查 Rider API 和兼容范围。
CI 分别在 Windows/Linux 构建并测试，在 Linux 使用 Plugin Verifier 检查基线版本及
兼容范围内最新稳定版 Rider。CI 是否通过以对应 Actions 运行结果为准。

最近一次构建验证（2026-10-12，`1.9.4-rider.29`）：使用 UE 5.8 源码通过 141 项 JVM 测试，新增 10 项格式化字符串回归，覆盖函数调用、三元表达式、命名参数、下标、单双引号、注释、连续插值、未完成输入后重启词法器及后续语句；LSP 验证插值中每个函数调用的定义和引用位置。同时保留 C++ CRLF/混合换行、Unicode、宏与包含片段、重载候选、未保存内容、后台读写竞争、超时取消、导航缓存、绑定别名与重载、Kotlin/C# HTTP 通信，以及声明/引用方向、用法行号和弹窗模型回归，
0 失败、0 跳过；8 项 LSP 测试通过。构建、插件结构检查及 Plugin Verifier 通过，
本地 Rider `RD-261.27258.81` 结果为 Compatible，弃用/实验 API 提示为 54/13 项。
后端专项验证覆盖真实 SDK 搜索接口、搜索范围、去重、声明身份和 UTF-16/CRLF 位置。
安装包自身的 353 个 JVM 插件实现类全部来自 Kotlin，0 Java 源码/实现类；仅随包附带本工程的 C# 后端 DLL。
具体记录见 [UE/C++ 导航验证](docs/UE_CPP_NAVIGATION.md)。

这些自动化与源码检查不代表真实 Rider/UE 的全部功能已完成验收。`.27` 已在主 Rider 恢复启动并加载语言服务；`.29` 包含此前光标定位修复及本次格式化字符串修复，由用户继续实机测试。真实界面的跳转落点、C++ 原生搜索中的 AS 文件映射和结果展示仍待验收。
自动化包含模拟 UE 协议及 LSP 客户端，真实 UE 的断点、热重载和重连仍需联调，步骤见 [UE 调试验收](docs/UE_DEBUG_ACCEPTANCE.md)。

```text
src/main/kotlin/       完整 Kotlin lexer/parser/PSI、Rider 集成、LSP、调试与格式化
src/main/resources/   插件注册与 TextMate bundle 描述
src/test/kotlin/       原生引用、结构视图、工作区、TextMate、调试和格式化测试
src/test/resources/    迁移前词法和语法树对照基线
language-server/       Hazelight TypeScript 语言服务器及 stdio 集成测试
backend/               C# 原生 C++ 用法搜索扩展及 Rider SDK 专项验证
extension/             原有 VS Code 扩展
build/distributions/  可从磁盘安装的 Rider 插件 ZIP
```

来源与许可见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)；原 Hazelight MIT 许可保留在 `LICENSE`。
