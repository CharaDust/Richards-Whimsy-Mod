# Mixin（misc）

> 本页收录模组的全部 Mixin 注入，供重制时对照。

## 清单
| 类 | 目标 | 内容 | 状态 |
| --- | --- | --- | --- |
| 主模组 Mixin | `MinecraftServer` 的世界加载方法 | 方法体为空 | **空实现，可删除** |
| 堆肥块作物 Mixin | `CropBlock` 的种植判定 | 底部为堆肥块时允许种植，不考虑水源 | 有效，见 [BHR_Compost](../Blocks/ClassBlockHeadsRebon/BHR_Compost.md) |
| 堆肥块作物 Mixin | `CropBlock` 的湿度计算 | 底部为堆肥块时固定返回较高湿度值 | 有效（当前文档未单独记载） |
| 宝石祝福紫锂辉 Mixin | 附魔等级计算 | 紫锂辉祝福使效率附魔视为等级 +1 | 测试性质，见 [BHR_GemBlessing](../Component/ClassBlockHeadsRebon/BHR_GemBlessing.md) |
| 极限模式切换器 Mixin | `PrimaryLevelData` 的 `settings` 字段 | 提供 hardcore 的写入口（原版只有读：没有 `withHardcore`、也没有 setter） | 有效，见 [DEV_HardcoreSwitcher](../Items/ClassDEV/DEV_HardcoreSwitcher.md) |
| 传统饥饿消耗 Mixin（4 个类） | `ServerPlayer` / `Block` / `Player` / `HungerMobEffect` 的 `causeFoodExhaustion` 调用点 | 把 10 处饥饿消耗改回 1.8 的数值（游泳/步行/潜行/挖方块/跳跃/疾跑跳/攻击/饥饿效果） | 有效，见 [游戏规则](./游戏规则.md) |

## 传统饥饿消耗：4 个类与 10 个注入点
| 类 | 目标 | 条目 |
| --- | --- | --- |
| `BHR_LegacyHungerMovementMixin` | `ServerPlayer.checkMovementStatistics` / `jumpFromGround` | 游泳/水下/水面 0.015、潜行/步行 0.01（每米）、跳跃 0.2、疾跑跳 0.8（每次） |
| `BHR_LegacyHungerBlockMixin` | `Block.playerDestroy` | 破坏方块 0.025（每方块） |
| `BHR_LegacyHungerAttackMixin` | `Player.attack` / `Player.stabAttack` | 攻击 0.3（每次） |
| `BHR_LegacyHungerEffectMixin` | `HungerMobEffect.applyEffectTick` | 饥饿效果 0.5（每等级每秒） |

- `BHR_LegacyHungerMixin` 本身**不是 Mixin**：那一批注入点被拆成上面四个类（一个目标类一个），
	它只是放在 mixin 包里的说明页
- ⛔ **一个 `@Mixin` 只能挂一个目标类**（2026-10 实测踩到）：类级 `@Mixin({A, B})` 会把**每一个**
	注入都尝试应用到**每一个**目标类上，结果是整个 mixin 失效 —— 日志形态是
	「`… could not find any targets matching 'checkMovementStatistics(DDD)V' in net/minecraft/world/level/block/Block`」，
	即把 `ServerPlayer` 的方法名拿到 `Block` 里去找了
- ⛔ **不要去改 `Math.round` 的中间结果**（2026-10 实测踩到）：那些分支的形状是
	`causeFoodExhaustion(速率 * Math.round(位移 * 100) / 100)`，两个乘数都是内联常量，
	而 `Math.round` 的结果是 `int`（Mixin 处理函数不能改类型）。当初改了那里，症状是
	**开着规则疾跑反而慢十倍、走路潜行完全不消耗**。正确做法是 `@Redirect` 整个
	`causeFoodExhaustion` 调用，按位移自己算
- ⛔ `HungerMobEffect` 是**包级私有**类，只能用 `@Mixin(targets = "…")` 字符串形式，`import` 不了
- `@ModifyArg` 在这些地方**都不可用**：它只把「原方法的参数」接到处理方法尾部，
	拿不到 `@At` 目标方法的实参，实测报 `invalid signature (F), expected (F…)`；一律用 `@Redirect`

## 现状与限制
- Mixin 配置中的兼容级别已随语言版本提升到 **Java 25**（见 `richards-whimsy-mod.mixins.json` 的 `compatibilityLevel`）
- 主模组 Mixin 跟踪的是 `MinecraftServer` 的世界加载方法，该方法在 26.x 中**已改名**，现有注入在重制后必然失效
- 另有一个空的 Mixin 类，建议直接删除

## 重制注意
- Mixin 的目标方法名与签名是**映射相关**的，重命名迁移时最易出错，必须逐个重新核对
- 堆肥块的两处注入中，湿度计算一处直接决定“不需要水源”的效果，重制时不可遗漏
- ⛔ **mixin 包内不能放非 mixin 类**（2026-10 实测踩到，症状是**一进存档就崩**）：
  配置里的 `package` 声明的是「整个包（含子包）都是 mixin」，包内任何被普通代码引用的类
  —— 典型就是被 `@Mixin` 类 `implements` 的 **duck 接口** —— 都会抛
  `IllegalClassLoadError: … is in a defined mixin package … and cannot be referenced directly`。
  本模组把这类接口统一放到 `com.rcdmake.whimsy.access`（见 [DEV_HardcoreModeAccess](../Items/ClassDEV/DEV_HardcoreSwitcher.md)）
