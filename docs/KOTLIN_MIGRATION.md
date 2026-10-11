# Rider .7：移除 Java 的迁移与验证

2026-10-11，插件版本 `1.9.4-rider.7`。

Rider 插件已完整迁移到 Kotlin：移除 146 个 Java 源文件、BNF/Flex 输入、GrammarKit/JFlex 构建依赖和 Java 生成任务。
保留原生 PSI、声明、引用、作用域、结构视图、语法错误恢复及全部既有 LSP、调试和格式化实现。
共享语言服务器继续使用 TypeScript；Kotlin 插件及 Rider 本身仍运行在 JVM 上，构建需要 JDK。

## 迁移方式

- `AngelscriptParser.kt` 保留原有递归下降规则、运算符优先级、固定解析节点及恢复规则。
- `AngelscriptLexer.kt`、`AngelscriptLexerTables.kt` 保留原有 Unicode DFA、字符串、注释及插值字符串状态。
- `psi/` 中的 Kotlin 接口、实现与访问器保留原有 71 种 PSI 类型和属性接口。
- `AngelscriptTypes.kt` 保留 token、节点类型和 PSI 工厂映射；引用和命名元素仍使用原有原生实现。
- 这些 Kotlin 文件现在是直接维护的源码，构建不生成 Java；`verifyNoJavaSources` 阻止 `src/` 中重新出现 Java 或旧 BNF/Flex 输入。

迁移前已保存可工作的 Java 版编译结果。使用它采集 `src/test/resources/parser-baseline/` 下的对照数据后，
移除了旧 Java 编译目录和旧插桩产物，再重新编译 Kotlin 版。基线测试仅比较结果，没有自动更新或覆盖基线的入口。

## 自动化结果

本机 Rider 更新后，测试与沙箱使用 **2026.1.5.2 / RD-261.27258.81**。
插件声明仍为 `261.25134` 至 `261.*`；本轮未重新运行 2026.1.3，历史 `.6` 的验证记录单独保留。

| 检查 | 结果 |
| --- | --- |
| JVM 测试 | 47 项，0 失败、0 错误 |
| Java/Kotlin 行为对照 | 10 组代码、172 份完整/截断输入；token 类型、UTF-16 范围、状态、语法树节点及错误信息完全一致 |
| 对照内容 | 命名参数、UE 类/属性/函数/资产、声明类型、控制流、表达式优先级、模板类型、命名空间、Unicode、字符串/插值字符串及错误恢复 |
| stdio LSP 集成 | 4 项通过，包含无 UE 的脚本功能和模拟 UE 诊断 |
| 构建与插件结构 | `check buildPlugin verifyPluginStructure` 通过 |
| Plugin Verifier | Compatible；54 处弃用 API、13 处实验 API 使用；没有兼容性错误或内部 API 使用报告 |
| 安装包检查 | 285 个插件类的 SourceFile 均为 `.kt`；没有 Java 源文件或 Java 编译类；没有重复 JAR 条目 |
| 内嵌服务器 | 与源码构建的 `server.js` 一致，且与 `.6` 的服务器哈希一致 |

构建命令（使用本机 SDK）：

```powershell
.\gradlew.bat check buildPlugin verifyPluginStructure '-PlocalPlatformPath=D:\Program Files\JetBrains\JetBrains Rider'
.\gradlew.bat verifyPlugin '-PlocalPlatformPath=D:\Program Files\JetBrains\JetBrains Rider'
```

本机完整日志：`build/kotlin-migration-check.log`、`build/kotlin-rider-ui.log`、`build/kotlin-package-audit.json`。
远程 Windows/Linux CI 尚未执行。

## 实际 Rider 操作

在独立沙箱打开 `build/rider-goto-fixture`，安装的是本轮 Kotlin 版插件。没有修改用户主 Rider 工程。

| 操作 | 观察结果 |
| --- | --- |
| `DefaultTarget(Third: 7, First: 19)` 中 Ctrl+B | 跳转到第 1 行 `DefaultTarget` 声明；文件无语法错误标记 |
| 第 19 行 `Local` 引用 Ctrl+B | 跳转到第 18 行局部变量声明 |
| 第 11 行 `RemoteValue()` Ctrl+B | 打开 `Remote.as`，定位函数声明 |
| 第 25 行 `Actor.Tick()` Ctrl+B | 跳转到第 16 行方法声明 |
| 在 `Remote.as` 声明处 Shift+F6 | `RemoteValue` 改为 `RenamedRemote`，两个文件同时更新 |
| 改名后的调用 Ctrl+B | 仍能跳转到改名后的跨文件声明 |
| 在目标声明处 Ctrl+Z 一次 | 两个文件同时恢复 `RemoteValue`；退出沙箱后核对磁盘内容一致 |
| Ctrl+F12 | 显示 DefaultTarget、DefaultNamed、CrossFile、AExampleActor、Count、Tick、UseActor |
| `Actor.` 后调用基本补全 | 显示 Count、Tick |
| `Local` 上 Ctrl+Q | 显示 `int Local` |

沙箱已正常退出。真实 UE 的附加调试、断点、热重载及真实 UE/C++ 导航继续按用户确认的“暂不联调”处理；
协议模拟和既有自动化检查不能代替这些真实 UE 验收。其余功能的覆盖和边界见 `RIDER_FEATURE_PARITY.md`。

## 安装包

`build/distributions/UnrealAngelscript-1.9.4-rider.7.zip`

SHA256：`0EB4B9E32222EA886674E0B8D6B3CB655E759AEC01C5B1FD7A2669D7E60A2B5E`

内嵌服务器 SHA256：`DAB390759BD434DC7795CD93CB792077D0B7770BE7E36C54568EECACE30D2062`
