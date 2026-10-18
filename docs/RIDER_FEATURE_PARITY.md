# Rider 功能恢复与保留清单

范围：迁移前 `1.9.4-rider.2` 的 Rider 功能，以及迁移后截至 `.5` 已加入的功能。
依据：`build/migration-baseline-rider2.zip`、恢复前快照 `build/migration-before-psi-restore-rider5.zip`、当前源码和实际测试结果。
真实 UE 联调继续遵循用户已确认的“暂不联调”；模拟 UE 检查不能替代真实 UE 验收。

| 功能 | `.10` 实现 | 验证依据及边界 |
| --- | --- | --- |
| `.as` 文件识别、结构化语法树、语法错误定位 | 完整 Kotlin lexer/parser/PSI，保留原语法规则、Unicode DFA 和错误恢复；不使用会替换 PSI 语言的 TextMate 文件类型标记 | `AngelscriptParsingTest`；`AngelscriptParserBaselineTest` 对照迁移前词法及语法树；实际 Rider 验证记录见下方链接 |
| 冒号和等号命名参数、嵌套调用、三元表达式 | 原生参数列表使用独立 Argument；保留共享 PEG 修复 | PSI 解析测试与 stdio 语法测试 |
| 原生定义跳转、局部变量、参数、构造函数参数、成员、类型 | 保留 `PsiReference` 离线回退；语言服务器运行时先使用完整语义，防止无关同名成员抢占结果 | `AngelscriptNavigationTest` 核对离线声明偏移；LSP 覆盖错误同名成员与脚本/原生重载冲突 |
| 作用域遮蔽、多层命名空间、限定名 | 内层作用域优先；修复 ScopeRef 语法；限定名按容器过滤 | 原生引用测试包含同名命名空间及限定名与局部变量冲突 |
| 跨文件、成员链和重载等完整语义导航 | 原生项目引用解析与官方 LSP 补充 | LSP 集成测试覆盖跨文件及成员；实际 Rider 验证跨文件函数与成员方法跳转；本次文件夹模式跨文件目标由 LSP 返回 |
| 命名元素、引用重命名、引用搜索 | 恢复 PsiNameIdentifierOwner/setName/handleElementRename；原生 PSI 使用公开 RenameHandler 请求 LSP，并在一个全局撤销事务应用全部修改；修复离线全局命名空间搜索范围 | 实际 Rider 验证局部变量、跨文件全局函数的声明及引用同时改名，且改名后跨文件 Goto 正常；最终包从目标声明处一次撤销同时恢复两个文件；LSP 集成断言覆盖引用与编辑；其他种类的完整 UI 重命名未逐一验收 |
| 结构视图 | 恢复原生结构视图；保留 LSP document symbols | 实际 Rider 文件结构弹窗显示函数、类及字段/方法；测试覆盖命名空间、类、结构、接口、枚举/枚举值、函数、构造函数、变量、事件、委托、mixin、typedef |
| 官方 asset 声明 | 在 Kotlin 原生解析器中支持 `asset Name of Type { … }`，结构视图包含资产 | 原生资产语法及结构测试；LSP 原有资产处理保留 |
| 词法和语义高亮、颜色设置 | 原生 Kotlin lexer/annotator 与 19 种 LSP 语义分类直接复用 Rider C++ 色键，跟随主题和 C++ 自定义设置；已移除硬编码配色及 TextMate 依赖 | 原生高亮、作用域分类、C++ 配色一致性、自定义覆盖与增量编辑测试；关闭 TextMate 的独立测试沙箱验证 |
| 补全、悬停、签名、代码操作、UE 诊断 | 保留官方 TypeScript LSP、客户端设置及配置界面 | 实际 Rider 验证成员补全和快速文档；stdio 集成测试覆盖补全、悬停、签名和模拟 UE 诊断；代码操作具体 UI 未验收 |
| 折叠、扩大选择、调试行内表达式 | 保留 LSP foldingRange/selectionRange/inlineValue | stdio 集成测试及 `AngelscriptEditorSupportTest` |
| 注释、格式化 | 保留 commenter、自带/自定义 clang-format、专用配置和 Unicode 处理 | `AngelscriptFormatterTest`；完整 UI 格式化仍须验收 |
| UE/C++ 类型、静态/实例/全局函数、访问器、字段和枚举值跳转 | UE 类型库可用时优先语义导航；类型库缺失符号时，类型、明确接收者、继承及静态/全局调用按源码上下文回退；Kotlin 从索引源码解析限定/嵌套类型、别名、手工绑定及 Lambda，有限展开函数式宏并排除已知无效条件分支；泛型实例保留绑定元数据；当前头文件及 UE 数据库更新自动反映到查询 | 无 UE/部分 UE 类型库的 LSP 回归、真实引擎头文件/绑定的只读测试、宏原始位置及头文件修改回归、包含用户三段代码及嵌套容器的 LSP 矩阵、独立 Rider 沙箱；未知外部编译条件仍可能多候选，真实 UE 联调未执行，详见 `UE_CPP_NAVIGATION.md` |
| 工程/插件 Script 根目录与特殊路径 | 保留工作区识别、URI 转义、Windows 大小写匹配修复 | `AngelscriptWorkspaceTest`、含空格/中文/# 路径的 LSP 集成测试 |
| 无 UE 的脚本分析、UE 类型到达后的刷新 | 保留离线符号解析及未保存缓冲区保护 | 无 UE、连接后等待类型、收到类型后的 stdio 测试 |
| 调试配置、附加、自动连接/重连、断点、暂停/继续/单步、调用栈、变量与求值 | 保留 `.5` 的调试器实现 | `UnrealDebugProtocolTest`、`OrderedDebugRequestsTest`；真实 UE 行为依用户选择暂不验收 |
| 跨帧归属、取消/超时、迟到回复、断线/停止清理 | 保留有序请求队列、连接代次和 Disposable 清理 | 协议和有序请求测试 |
| 调试悬停成员链/下标、行内值 | 保留无副作用表达式提取器及官方 inlineValue 桥接 | 编辑器表达式与 stdio 测试 |
| 资产编辑确认后保存 | 保留 LSP 客户端成功确认与 wantSave 通知 | stdio 测试覆盖延迟应用和拒绝编辑 |
| LSP 状态/UE 类型状态、重启与设置 | 保留 Rider 原生状态组件及配置界面 | 配置收集、LSP 状态通知测试；完整交互界面须验收 |
| 构建、许可、CI 和目标 Rider 兼容性 | 保留内嵌服务器、来源说明、Windows/Linux CI 与 Plugin Verifier | 本机最终构建/结构/Verifier 日志；远程 CI 未执行 |

Rider 插件的实现语言全部为 Kotlin；Java 源码、BNF/Flex 及 GrammarKit/JFlex 生成链已移除。
调试表达式提取器的粗粒度词法标记仅用于安全求值范围，编辑器 PSI 使用完整的 Kotlin 结构化解析器。
Kotlin 和 Rider 平台仍运行在 JVM 上；构建使用 JDK 不代表插件使用 Java 实现。

保留原计划的范围边界：VS Code 专用 API 浏览器和数据断点界面此前尚未移植，本次没有将其冒充为已完成功能。

`.20` 统一类型/别名/继承的词法作用域与同名容器冲突、`.19` 部分类型库、嵌套类型、函数式宏及条件分支、`.18` 运算符与 FString 导航、`.17` 路径悬停提示、`.16` 公开头文件别名优先、`.15` C++ 绑定实现的命名空间、`.14` 无 UE 类型库的 auto/foreach 与绑定返回类型、`.13` UE 5.8 宏注入声明、`.12` C++ 解析栈溢出、`.11` 容器/绑定及候选显示、`.10` 源码回退、`.9` 通用导航及 `.8` UE/C++ 导航验证见 [导航记录](UE_CPP_NAVIGATION.md)；`.7` 迁移验证以 [Kotlin 迁移记录](KOTLIN_MIGRATION.md) 为准；表中未重新执行的 UI 检查沿用 [`.6` 验证记录](RIDER_VALIDATION.md)。构建、接口兼容或直接 LSP 返回目标均不能单独证明真实 Rider 操作成功。
