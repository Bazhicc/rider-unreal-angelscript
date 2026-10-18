# 原生 PSI 语法覆盖

本次检查日期：2026-10-18。依据当前 UE AngelScript 引擎的
`ThirdParty/source/as_parser.cpp`、项目内的 PEG 语言服务器语法，以及 XGame 的实际脚本。
这里的通过表示 Rider 原生 PSI 能解析输入，不代表脚本经过 UE 编译或运行验收。

## 本次修复

- 将 `local` 识别为独立关键字并提供原生高亮；`localValue`、`Local` 等仍是标识符。
- 全局及命名空间函数接受 `local`，保留原有函数 PSI、名称、引用和结构视图。
- 函数体和类成员不接受普通 `local` 函数声明；补上顶层及命名空间的错误恢复边界。
- 实际语料发现类内 `access Name = private;` 未进入已有访问掩码规则，已将类和结构体接入。
  工作区原有的属性访问器、多函数修饰符和访问掩码实现保留。

## 固定回归范围

`AngelscriptSyntaxCoverageTest` 和 `src/test/resources/syntax-coverage/` 的用例在普通测试中运行，
不依赖本机 XGame 路径。它们补充迁移时的 token/语法树快照；快照一致不能单独证明一段合法代码没有误报。

| 类别 | 覆盖内容 |
| --- | --- |
| local 函数 | 全局、命名空间、泛型返回值、引用返回值、默认参数、声明、UFUNCTION 前缀 |
| 编辑器行为 | 函数名称及 UTF-16 位置、同文件调用解析、结构视图、关键字边界及高亮 |
| 错误输入 | local 变量、成员和嵌套函数的诊断，损坏声明后恢复，逐字符截断编辑 |
| 声明 | 命名空间、枚举、typedef、接口、类、结构体、构造/析构、event、delegate、mixin、asset |
| UE 扩展 | UCLASS/UPROPERTY/UFUNCTION、default、访问掩码和 access 注解、属性方法及 get/set 函数体 |
| 类型及表达式 | 基本类型及别名、引用方向、嵌套泛型、初始化列表、构造、auto、cast、命名参数、三目和逗号表达式 |
| 运算符 | 算术、幂、位运算、逻辑和身份比较、左右移位及复合赋值、布尔异或 |
| 控制流和文字 | for/while/do/switch、break/continue、字符与转义、名称/文本/原始/相邻字符串、带格式的插值 |

语言服务器另有 `global local and mixin declarations preserve scoping, names and signatures`，
检查 `local`/`mixin` 的 AST 作用域及签名不会退化。

## 实际项目语料检查

通过 Gradle 参数 `-PtestScriptRoot=<脚本目录>`，`testOptionalProjectScriptCorpusHasNoErrors` 递归解析该目录所有 `.as`，
检查源码保留和语法错误。失败时将语法树及摘要写入 `build/syntax-audit/`；不会改写项目脚本。

本次检查了 `F:/Projects/UE-Angelscript-Hotfix/XGame/Script` 的 52 个文件：51 个无语法错误。
剩余的 `Tests/TestEvaluation.as` 当前在函数 `TestEvaluation()` 中途结束，缺少函数的结束大括号，
产生 12 条连带诊断。该次检查保留失败结果，没有把损坏输入列为合法代码或修改外部工程。
证据为 `build/local-syntax-project-audit.log`、`build/syntax-audit/Script.summary.txt`。

## 验证边界

后续修复（2026-10-18）：`AngelscriptFormatterTest` 的两个 Unicode/多处编辑测试已迁移到
当前 `FormatterEngine.replacements` 的 XML 替换协议和 UTF-16 起止范围，保留原有结果断言。
不再使用隔离脚本，执行完整常规检查：

```powershell
.\gradlew.bat check '-PlocalPlatformPath=D:/Program Files/JetBrains/JetBrains Rider' '-PtestEngineSource=F:/Projects/UE-Angelscript-Hotfix/Engine/Source' --console=plain
```

结果：159 项 JVM 测试通过，0 失败、0 错误、0 跳过，包含两个格式化器测试和原先因缺少
UE 源码而跳过的 7 项检查；9 项 Node/LSP 测试及原生 Find Usages 后端检查通过。
本轮未启用上文独立的外部项目脚本语料审计。测试报告位于 `build/reports/tests/test/index.html`。

后续 C++ 配色修复（2026-10-18）：移除固定明暗调色板，原生与 LSP 高亮直接复用 Rider C++
颜色项。新增检查覆盖 C++ 自定义颜色、旧 AngelScript 配色覆盖的隔离、整数/浮点数、控制流、
宏与分段块注释。再次执行上述完整 `check`，结果为 160 项 JVM 测试通过，0 失败、0 错误、
0 跳过；9 项 Node/LSP 测试和原生后端检查通过。`buildPlugin verifyPlugin` 成功，
本机 Rider `RD-261.27258.81` 判定 Compatible（73 处弃用 API、13 处实验 API 使用）。
安装包已通过命令行安装至本机 Rider2026.1，3 个安装文件的 SHA-256 均与安装包一致。
以上为自动化验证；本轮未进行交互式编辑器视觉验收。

后续颜色继承回归修复（2026-10-18）：前一版使用 `TextAttributesKey.find()` 取得语义颜色项，
但缺少回退链，主题的 `baseAttributes` 标记解析成无前景色的属性。现按本机 Rider 的
`CppHighlightingAttributeRegistration` 和 `RiderLanguageTextAttributeKeys` 补齐缺失关系，
保留已注册的继承和用户 C++ 覆盖，不写入 RGB 调色板。LSP 分类追加枚举类型、枚举值、
结构体字段/属性和重载操作符，保留原有分类编号；操作符范围独立存储、按位置去重，
不参与引用或重命名。原生层补齐结构体字段链、原始字符串、有效/无效转义和文档注释。

执行完整 `check buildPlugin verifyPlugin`，继续提供上述 UE 源码参数，未使用测试筛选或
隔离脚本：164 项 JVM 测试全部通过，0 失败、0 错误、0 跳过；包含旧格式化器的两项测试。
9 项 Node/LSP 测试和原生后端检查通过。真实 Rider Islands Dark 主题的最终前景色按 RGB
精确断言；在完整测试应用中创建实际编辑器、应用 lexer 和 annotator 的 markup 后绘图，
验证绘制像素（仅容许字体抗锯齿带来的 3 色阶偏差）。渲染图为
`build/color-audit/final-editor.png`，完整执行日志为 `build/color-audit/full-check-build.log`。
此项为 SDK 编辑器渲染检查，不代表对用户打开的工程进行实时 UI 验收。

本机 Rider `RD-261.27258.81` 的 Plugin Verifier 结果为 Compatible（73 处弃用 API、
13 处实验 API 使用）。新包已通过命令行覆盖安装到 Rider2026.1，3 个文件的 SHA-256
全部一致，记录见 `build/color-audit/install-verification.json`；启动 Rider 后加载新版本。
构建目录继续保留 3 个不同成功版本，无需清理其他版本。

### 早期隔离检查（历史记录）

此前工作区的 `AngelscriptFormatterTest.kt` 仍引用已被格式化器迁移删除的
`AngelscriptExternalFormatter.toDocumentReplacements`，普通全量测试在编译阶段失败，
见 `build/local-syntax-initial.log`。
本次通过独立 Gradle init 文件仅编译语法覆盖、解析、迁移快照、导航测试和所需测试服务，
没有删除或修改该旧测试，也不将隔离结果称为全量 `check` 通过。
隔离配置位于 `build/local-syntax-tests.init.gradle`，执行记录位于 `build/local-syntax-final-validation.log`。

最终隔离验证：31 项 JVM 测试无失败（其中项目语料检查为显式启用项，本轮默认执行未启用），
9 项 Node/LSP 测试通过，原生 Find Usages 后端检查通过，插件构建及结构检查通过。
Plugin Verifier 对本机 Rider `RD-261.27258.81` 判定 Compatible，报告 74 处弃用 API 和 13 处实验 API 使用。
没有替换用户 Rider 的已安装插件，也没有进行本次修复的实际 UI 操作验收。

安装包为 `build/distributions/UnrealAngelscript-1.0.0.zip`，继续使用工作区当前版本号。
构建产物目录仍只有最近 3 个版本，无需删除其他产物。

尚不能声称覆盖 AngelScript 的全部语法组合。对照引擎源码，原生 PSI 尚缺少
`import ... from`、原生 `funcdef`、`function(...) { ... }` 匿名函数的专门解析入口；
当前 XGame 语料没有这些形式，已有测试也没有证明它们受支持。
内联属性访问器的无函数体形式（`get;`/`set;`）及访问器上的 override/final 组合同样没有完整覆盖。
这些是本次审阅发现的后续覆盖缺口，未将它们冒充为本次已修复功能。
