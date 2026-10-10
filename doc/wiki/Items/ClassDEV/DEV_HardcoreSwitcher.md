# 极限模式切换器（物品）
{`dev_hardcore_switcher`}
## 特点简介
初学开发（DEV）分支的测试工具：手持右键切换当前存档的**极限模式**（hardcore），
方便反复验证依赖该属性的行为。

## 构造
（普通物品，无特殊构造器；功能由“组件”定义）

## 组件
### 默认组件
全部沿用默认值；`max_stack_size` 为 64（默认值，代码里无需指定）。

### 额外组件
（无）

## 改写方法
- 无

## 事件
### 交互（手持右键时）
- 把当前存档的 hardcore 属性取反（TRUE ↔ FALSE），并给使用者一条提示
- **只切换这一个属性**：不动 `allowCommands`（作弊开关）、不动 `gameType`（创造/生存）、也不动难度
- 对着方块右键与对着空气右键都生效；客户端只负责挥手，真正的写入发生在服务端

## 获取方式
- 无（DEV 测试物品，不设配方与战利品表）

## 备注
- #mixin 原版对 hardcore **只有读取、没有任何写入口**（`LevelSettings` 没有 `withHardcore`，
	`PrimaryLevelData` 也没有 setter），所以写入走 Mixin：`mixin/ClassDEV/DEV_HardcoreModeMixin`，
	配 duck 接口 `access/DEV_HardcoreModeAccess`，清单见 [Mixin](../../Misc/Mixin.md)
- ⛔ **踩坑记录（2026-10）**：duck 接口最初和 Mixin 放在同一个包（`mixin/ClassDEV/`）里，
	结果**一进存档就崩**：Mixin 配置的 `package` 声明的是「整个包（含子包）都是 mixin」，
	包内任何被当普通类加载的类都会被拒绝
	（`IllegalClassLoadError: … is in a defined mixin package … and cannot be referenced directly`）。
	接口因此搬到 `access` 包：**mixin 包里只放真正的 `@Mixin` 类**
- 客户端的心形显示要**重进世界**才刷新：原版只有登录包携带 hardcore，之后没有同步包；
	服务端判定是即时的，所以模组自己的消失流程立刻能看到变化
- 刻意不做权限校验：它要能在「单人 + 未开作弊」的存档里用 —— 而这正是它不碰作弊开关的原因
- 与未来的 **BHR 专家模式切换器**分开：本物品只动原版的 hardcore，不涉及 BHR 的模式系统
- 来源：临时起意新增（2026-10），先写实现后补本页
