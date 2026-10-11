# Rider 1.9.4-rider.6 功能恢复验证

本文件保留 `.6` 的历史验证证据；当前 `.7` 已将完整原生解析器与 PSI 迁移为 Kotlin，见 [Kotlin 迁移记录](KOTLIN_MIGRATION.md)。

执行日期：2026-10-11。环境：Windows，Rider 2026.1.3，build `261.25134.178`。
本次使用独立 Rider 沙箱打开 `build/rider-goto-fixture`，没有替换用户日常 Rider 的插件，也没有启动 UE。

## 根因与修复

迁移删除了原生 Java lexer/parser/PSI，将文件改成粗粒度 token 平铺，导致原有 `PsiReference`、
命名声明、作用域及结构视图丢失。此前 LSP 测试和构建通过，没有覆盖这条 Rider 集成链路。
现在恢复 BNF/Flex 及其生成的 Java PSI，手写集成继续使用 Kotlin，保留 `.5` 的 LSP、调试、格式化与协议改进。

恢复解析器后，实际 Rider 日志又揭示第二个根因：`.as` 的 PSI 语言仍是 `textmate`。
`TextMateBackedFileType` 让 Rider 使用 TextMate 的文件视图，跳过 AngelScript 原生 PSI，
声明跳转处理器也因为语言不匹配提前退出。移除该标记后，日志确认语言为 `AngelScript`；
官方 TextMate 语法通过独立的编辑器高亮入口保留。新增回归断言防止重新引入该标记。
临时导航探针日志已从最终源码移除。

跨文件重命名检查进一步发现：Rider 的 `LspRenameSupport.shouldRunRename` 默认仅接受
`TEXT` 和 `textmate` PSI。恢复原生 PSI 后需要插件提供语义重命名入口，否则补全和 Goto
正常时，跨文件重命名仍可能没有响应。此前尝试启用平台内联重命名虽能修改两个文件，
实际撤销却只恢复目标声明，调用文件仍保留新名称。
现改为公开的 `RenameHandler`：请求 LSP 完整编辑，校验范围、文件版本及可写状态后，
通过 `WriteCommandAction.withGlobalUndo()` 一次应用所有修改。原生 PSI 的平台内联入口关闭，
自定义扩展名的普通文本仍保留平台默认重命名；没有运行 LSP 时保留原生 PSI 重命名回退。

另一个空操作原因位于离线语言服务器：全局命名空间在 UE 类型尚未到达时没有声明记录，
引用搜索/重命名的候选模块因此为空。现对全局命名空间搜索所有已加载模块，仍按语义符号过滤；
stdio 回归断言覆盖函数声明与跨文件调用的两处引用、两个文件的重命名编辑。

同时修复命名空间 ScopeRef 的贪婪解析、限定名与局部变量的冲突、内层作用域优先级、
COMMENT token 与节点冲突，保留冒号/等号命名参数，并补齐官方 `asset Name of Type { … }` 的原生语法和结构项。

## 真实 Rider 操作结果

沙箱键位为 IntelliJ：声明跳转命令 `Navigate > Declaration or Usages` 对应 `Ctrl+B`，
文件结构对应 `Ctrl+F12`；其他键位方案应以菜单显示为准。

| 操作 | 实际结果 |
| --- | --- |
| 命名参数调用 `DefaultTarget(Third: 7, First: 19)` | 从第 7 行跳到第 1 行函数声明；无原先的命名参数语法报错。 |
| 跨文件 `RemoteValue()` | 从 `Example.as` 第 11 行跳到 `Remote.as` 第 1 行。探针确认原生引用无目标后，运行中的 LSP 返回跨文件定义。 |
| 局部变量 `Local` | 从第 19 行引用跳到第 18 行声明；探针确认使用原生引用目标。 |
| 成员方法 `Actor.Tick()` | 从第 25 行跳到第 16 行方法声明；使用 LSP 补充原生 PSI 未覆盖的成员调用。 |
| 文件结构 | 弹窗正确显示全局函数、类、成员字段及方法。 |
| 重命名局部变量 | `Shift+F6` 将声明及引用同时改为 `RenamedLocal`；随后撤销，恢复测试文件。 |
| 跨文件语义重命名及撤销 | 在 `Example.as` 的调用处 `Shift+F6` 改为 `RenamedRemote`，调用与 `Remote.as` 声明同时更新；再次 `Ctrl+B` 正确跳到改名后的声明。从 `Remote.as` 按一次 `Ctrl+Z`，声明与调用同时恢复 `RemoteValue`。退出沙箱后再次核对磁盘中的两个文件，均已恢复。 |
| 成员补全 | 经 `Code > Code Completion > Basic` 触发后显示 `Count` 与 `Tick`。 |
| 悬停/快速文档 | `Ctrl+Q` 正确显示 `int Local`。 |

探针证据位于本机 `.intellijPlatform/sandbox/RD-2026.1.3/log/idea.log`，
其中 01:00 的旧状态为 `language=textmate`，01:05 起为 `language=AngelScript`。
沙箱操作已完成并退出；首次最终构建因运行中的沙箱锁定插件 JAR 而失败，退出后重新构建。
该次失败日志保留在 `build/psi-restore-locked-sandbox.log`。

## 自动化、包与范围

| 检查 | 最终结果 |
| --- | --- |
| JVM 测试 | 46 项通过，0 失败；包含真实 PSI 引用及声明入口、重命名编辑规划与 UTF-16/无效范围、错误输入、结构视图、TextMate、调试协议、格式化和工作区。 |
| Node/LSP 集成 | 4 项通过，0 失败；包含离线脚本、UE 类型状态变化、未保存缓冲区及跨文件导航。 |
| 插件结构与构建 | 最终日志 `build/atomic-rename-release-validation.log`：`BUILD SUCCESSFUL`，26 项任务；此前原生 PSI 恢复日志为 `build/psi-restore-final-validation.log`。 |
| Plugin Verifier | Rider `261.25134.178`：Compatible；6 项弃用、13 项公开实验性 API 提示，无内部 API 用法。 |
| 安装包内容 | 包含原生 lexer/parser/reference/structure view、声明跳转入口及原子重命名入口；内嵌 LSP SHA-256 与当前 `language-server/dist/server.js` 一致。 |
| 原功能保留 | 恢复前 `.5` 快照与当前源文件逐项核对，无集成文件缺失；修改的集成文件用于接回原生 PSI。详细范围见 [功能恢复与保留清单](RIDER_FEATURE_PARITY.md)。 |

安装包：`build/distributions/UnrealAngelscript-1.9.4-rider.6.zip`。
SHA-256：`7B49FE38B8FBB9DC45133EA3DBA511EB39DEC06EBA8506859283C861A466E876`。
内嵌服务器 SHA-256：`DAB390759BD434DC7795CD93CB792077D0B7770BE7E36C54568EECACE30D2062`。

真实 UE 的断点、热重载、重连、引擎原生类型及 C++ 跳转，按用户先前选择“暂不联调”，
本次没有执行实机验收。模拟协议测试、源码保留核对和 Plugin Verifier 不等同于这些运行时功能已经通过。
VS Code 专用 API 浏览器和数据断点界面此前尚未移植，未将其列为本次恢复成功的功能。

## Rider 1.9.4-rider.5 先前验证记录

以下为此前记录。其构建/LSP 通过结论不代表真实 Rider 跳转通过；当时的 UI 验收尚未完成。

执行日期：2026-10-11。环境：Windows，Rider 2026.1.3，build `261.25134.178`。

在用户实际安装的 `1.9.4-rider.4` 中复现：`XGame/Script/Tests/Fixture.as` 第 29 行
`DefaultTarget(Third: 7, First: 19)` 按 F12 提示 `Cannot find declaration to go to`。
透明 stdio 代理记录显示，Rider 已发送 `didOpen`，但 F12 没有发出 `textDocument/definition`。
同一工程直接调用修复后的服务器可以取得第 14 行的函数定义；这不能替代实际编辑器验收。

本次修改：

- 增加 Kotlin `GotoDeclarationHandler`，通过 Rider 公开 LSP API 请求定义并映射到 PSI 目标。
- 未运行 UE 时解析脚本自身的函数、类、成员、局部变量及跨文件引用。
- UE 类型到达后重新解析当前缓冲区，保留未保存内容；离线时不报告缺少 UE 类型导致的推测诊断。
- 统一 Windows 根目录匹配的大小写，避免扫描与 `didOpen` 为同一文件创建两个模块和重复定义。

| 检查 | 结果 |
| --- | --- |
| 构建、插件结构 | `build/goto-validation.log`：通过。 |
| JVM 测试 | 29 项通过，0 失败。 |
| Node/LSP 测试 | 4 项通过；同时覆盖 UE 未启动、连接后类型尚未到达及类型到达后未保存缓冲区的跳转。 |
| 跳转回归范围 | 同文件函数、局部变量、成员字段、成员方法及跨文件函数；Windows 每个函数只返回一个定义。 |
| 原工程只读检查 | `DefaultTarget` 直接 LSP 请求返回唯一目标：`Fixture.as` 第 14 行。 |
| Plugin Verifier | Compatible；0 内部 API，6 项弃用、13 项公开实验性 API 提示；见 `build/goto-final-validation.log`。 |
| 最终安装包 | 内嵌服务器哈希与当前源码构建一致，包含并注册新的跳转入口。 |
| 实际 Rider 修复版 UI | 独立沙箱已启动，等待用户确认其 `Trust and Open` 提示后验收。 |

用户 Rider 的临时语言服务诊断代理已停用，模式恢复为 `Bundled`；实际安装版本仍为 `.4`。
修复版安装包：`build/distributions/UnrealAngelscript-1.9.4-rider.5.zip`。
SHA-256：`1D3119DCB52DE338649E20E73BB80E41CFBB09AE4D8579DCE0357A4AA2A1C534`。
真实 UE 联调依用户先前选择暂不执行，C++ 原生符号和调试功能不计入本次通过范围。

## Rider 1.9.4-rider.4 先前验证记录

执行日期：2026-10-10。环境：Windows，本机 Rider 2026.1.3，build `261.25134.178`，使用 Rider 自带 JBR。

本轮完成：调试求值与变量回复统一排队并保留栈帧归属；取消、超时、停止及断线清理；
会话监听器绑定 Disposable；带连接代次的 socket 重试与关闭；连续字节缓冲和异常消息检查；
资产编辑成功确认后保存；C++ 单词索引导航；LSP 代码块折叠、逐层选择及 UE 状态提示；
语言服务器按内容哈希缓存；使用公开调试启动和断点接口；Windows/Linux CI 与 Plugin Verifier 配置。

执行命令：

```powershell
$env:JAVA_HOME = 'D:\Program Files\JetBrains\JetBrains Rider\jbr'
.\gradlew.bat buildPlugin check verifyPluginStructure verifyPlugin '-PlocalPlatformPath=D:\Program Files\JetBrains\JetBrains Rider' --console=plain
```

| 检查 | 结果 |
| --- | --- |
| 构建、插件结构 | 通过；最终日志 `build/optimization-final-validation.log`。 |
| JVM 测试 | 29 项通过，0 失败；包含跨帧回调、迟到回复、超时、断线、分片/合包及 Unicode。 |
| Node/LSP 测试 | 3 项通过；包含共享语法、语言功能、折叠、选择、状态通知、延迟应用资产编辑及编辑被拒绝。 |
| Rider 设置加载 | 已在独立沙箱运行 searchable options 收集；加载日志没有 SEVERE/ERROR。此项不覆盖实际调试 UI 与 UE 联调。 |
| Plugin Verifier | 本机 Rider 2026.1.3 判定 Compatible；0 内部 API 用法，6 项弃用 API、13 项公开实验性 API 提示。 |
| 安装包 | 内嵌服务器与当前 `language-server/dist/server.js` 哈希一致；没有旧 Java parser/PSI 或被删除的调试类。 |
| 源码格式检查 | `git diff --check` 通过。 |

Plugin Verifier 报告在 `build/reports/pluginVerifier/RD-261.25134.178/report.html`。
保留的提示包括 TextMate lexer、旧 LSP root 字段、ReadAction，以及公开的调试会话 builder 和外部格式化接口。
这些提示没有造成当前版本的二进制兼容错误；后续 Rider 升级继续通过 Verifier 核实。

安装包：`build/distributions/UnrealAngelscript-1.9.4-rider.4.zip`。
SHA-256：`EA572EC2291FDDB1E388E858F0D0AEA08052A6E0AC3E295A79AC6B71C946AB1B`。

Windows/Linux GitHub Actions 与兼容范围内最新稳定版 Rider 的检查已配置，本轮没有推送触发远程 CI，
不能将本机结果视为这些远程检查已通过。未进行大工程性能计时。
按用户要求暂不进行真实 UE 联调，待执行步骤见 [UE 调试验收](UE_DEBUG_ACCEPTANCE.md)。
