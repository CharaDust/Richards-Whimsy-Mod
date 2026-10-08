# Mixin（misc）

> 本页收录模组的全部 Mixin 注入，供重制时对照。

## 清单
| 类 | 目标 | 内容 | 状态 |
| --- | --- | --- | --- |
| 主模组 Mixin | `MinecraftServer` 的世界加载方法 | 方法体为空 | **空实现，可删除** |
| 堆肥块作物 Mixin | `CropBlock` 的种植判定 | 底部为堆肥块时允许种植，不考虑水源 | 有效，见 [BHR_Compost](../Blocks/ClassBlockHeadsRebon/BHR_Compost.md) |
| 堆肥块作物 Mixin | `CropBlock` 的湿度计算 | 底部为堆肥块时固定返回较高湿度值 | 有效（当前文档未单独记载） |
| 宝石祝福紫锂辉 Mixin | 附魔等级计算 | 紫锂辉祝福使效率附魔视为等级 +1 | 测试性质，见 [BHR_GemBlessing](../Component/ClassBlockHeadsRebon/BHR_GemBlessing.md) |

## 现状与限制
- Mixin 配置中的兼容级别为 Java 17，需随语言版本提升
- 主模组 Mixin 跟踪的是 `MinecraftServer` 的世界加载方法，该方法在 26.x 中**已改名**，现有注入在重制后必然失效
- 另有一个空的 Mixin 类，建议直接删除

## 重制注意
- Mixin 的目标方法名与签名是**映射相关**的，重命名迁移时最易出错，必须逐个重新核对
- 堆肥块的两处注入中，湿度计算一处直接决定“不需要水源”的效果，重制时不可遗漏
