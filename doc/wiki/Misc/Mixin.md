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
