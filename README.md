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
- UE/C++ 类型、函数及成员跳转：使用语言服务器提供的所属类型定位已索引头文件，支持继承、绑定别名及 `FVector` 等 UE5 类型别名；反射符号可转交 UE 导航。
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
UE/C++ 语义跳转同样需要 UE 提供类型数据库。请在 Rider 打开 UE 工程并完成源码索引；仅打开 `Script` 文件夹通常没有引擎头文件索引。
静态类方法、全局/命名空间函数、泛型容器与迭代器均走同一条语义导航链路；C++ 绑定中的类型/函数改名、函数指针及 Lambda 从源码注册声明解析，不维护函数名白名单。没有独立 C++ 声明的 Lambda 跳到绑定实现。
新增脚本声明随编辑更新；UE 类型库刷新后重新解析新增绑定，源码注册缓存随文件修改失效，无需为新增函数改插件。C++ 函数须已暴露给 AngelScript，相关源码须包含在 Rider 的索引范围。
反射函数和类属性的兜底导航使用 UE 配置的源码编辑器，请选择 Rider；结构体字段、模板类型和手工绑定需要相关头文件及绑定源码已索引。
原有可选正则头文件搜索仍可在设置中启用；上述语义跳转默认可用。验证范围见 [UE/C++ 导航记录](docs/UE_CPP_NAVIGATION.md)。
Rider 的 LSP 支持范围以 JetBrains 平台为准；VS Code 的专用 API 浏览器和数据断点界面尚未移植。

## 实现方式

Rider 插件全部使用 **Kotlin**，包括完整词法器、解析器、结构化 PSI、引用和调试器，不包含 Java 源码或 Java 代码生成步骤。高亮复用原始 `extension/syntaxes/angelscript.tmLanguage.json`，
通过 Rider 内置 TextMate 引擎加载；补全、悬停、引用、重命名、结构与语义分析复用本仓库的
TypeScript 语言服务器；原生 PSI 提供引用、声明、作用域和离线结构视图。
19 种官方 `as_*` 语义标记映射到 Rider 颜色设置。

Kotlin 解析器保留原有完整语法规则、运算符优先级、错误恢复和 PSI 类型；Unicode DFA 表保留原有词法规则。
迁移前的 Java 版词法和语法树快照作为回归基线；LSP 继续负责完整语义分析和 UE 类型信息。
Kotlin 插件运行在 Rider 自带 JVM 上，构建仍需 JDK，使用插件无需另外安装运行时。

共享服务器语法同时兼容 `DefaultTarget(Third: 7, First: 19)` 和原有 `Third = 7` 写法，
支持嵌套调用、三元表达式及命名空间访问。其他核心语言分析保持官方实现。

## 构建

需要 Node.js 22 或更新版本、JDK 21 或更新版本。无需全局安装 Gradle。

```powershell
$env:JAVA_HOME = '你的 JDK 或 Rider 的 jbr 目录'
.\gradlew.bat buildPlugin
```

首次构建自动下载 Gradle、目标 Rider SDK 和 npm 依赖，并构建、打包本工程的语言服务器。
也可以复用已经安装的 Rider，避免下载完整 SDK：

```powershell
.\gradlew.bat buildPlugin '-PlocalPlatformPath=D:\Program Files\JetBrains\JetBrains Rider'
```

生成的安装包位于 `build/distributions/UnrealAngelscript-1.9.4-rider.9.zip`。
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

停止 AngelScript 调试会话只断开调试连接，不退出 Unreal Editor。
求值和变量请求在会话内按回复类型排队，回调始终属于发起请求的栈帧。
继续执行会取消旧请求；已发送请求的迟到回复被丢弃。请求超过 5 秒无回复时重建连接，
防止没有请求编号的 UE 协议串用结果。关闭自动重连时，此情况会停止调试会话。
停止会话或关闭工程会解除监听并清理待处理请求、行内值和连接。

## 验证与开发

```powershell
# 结构化 PSI、原生引用与跳转入口、结构视图、TextMate、格式化、调试协议和 stdio LSP 集成测试
.\gradlew.bat check '-PlocalPlatformPath=D:\Program Files\JetBrains\JetBrains Rider'

# 使用 Plugin Verifier 检查本地 Rider 的二进制 API 兼容性
.\gradlew.bat verifyPlugin '-PlocalPlatformPath=D:\Program Files\JetBrains\JetBrains Rider'

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

自动化覆盖模拟 UE 协议及 LSP 客户端，真实 UE 的断点、热重载和重连需要另外联调。
本轮按用户要求暂不执行真实 UE 联调，步骤见 [UE 调试验收](docs/UE_DEBUG_ACCEPTANCE.md)。

```text
src/main/kotlin/       完整 Kotlin lexer/parser/PSI、Rider 集成、LSP、调试与格式化
src/main/resources/   插件注册与 TextMate bundle 描述
src/test/kotlin/       原生引用、结构视图、工作区、TextMate、调试和格式化测试
src/test/resources/    迁移前词法和语法树对照基线
language-server/       Hazelight TypeScript 语言服务器及 stdio 集成测试
extension/             原有 VS Code 扩展
build/distributions/  可从磁盘安装的 Rider 插件 ZIP
```

来源与许可见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)；原 Hazelight MIT 许可保留在 `LICENSE`。
