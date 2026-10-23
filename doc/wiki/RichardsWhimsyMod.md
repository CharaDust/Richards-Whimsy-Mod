# Richard的奇思妙想模组注册目录

> **标签图例**
> - `#deprecated`：该条目（类）将被替换、更名或迁移
> - `#todo`：迁移后**需要创建的类**（目标页）
> - `#wait2check`：**多方块/多物品类**，注册名需人工逐个命名（标签标在对应文档页内）
> - `#wait4move`：该条目（类）目前存在于旧项目，需要移动到新项目
> - `#mixin`：该条行为由 Mixin 实现（标签标在对应文档页内）
>
> 未加标签的条目表示“现状保留、暂不改动”。
> 数据包标签、Mixin、界面、配方、客户端渲染接线等不属于单一物件的实现，统一收录在 [misc](./Misc/) 目录。

## 子模块
- 方块愚者（`BHR`）
- 随意物件（`RDT`）
- 奇思妙想（`WI`）
- 原版考古（`LGC`）
- 初学开发（`DEV`）

## 方块
- 方块愚者（`BHR`）
	- #deprecated [[BHR__NormalBlocks]]
	- [[BHR_Limestone]]
	- [[BHR_LimestoneBrick]]
	- [[BHR_OreLimestoneOil]]
	- [[BHR_Marble]]
	- [[BHR_MarblePolished]]
	- [[BHR_BeachSand]]
	- #deprecated [[BHR_Rocks]]
	- [[BHR_Slate]]
	- [[BHR_SlateCobble]]
	- [[BHR_OreSlateCoal]]
	- [[BHR_OreSlateCopper]]
	- [[BHR_OreSlateIron]]
	- [[BHR_OreSlateGold]]
	- [[BHR_OreSlateTin]]
	- [[BHR_OreSlateTitanium]]
	- [[BHR_OreSlatePlatinum]]
	- [[BHR_OreSlateUnknown]]
	- #deprecated [[BHR_Soils]]
	- [[BHR_Soil]]
	- [[BHR_OreSoilFlint]]
	- [[BHR_OreSoilClay]]
	- [[BHR_OreSoilUnknown]]
	- [[BHR_Compost]]
	- #deprecated [[BHR_Gems]]
	- [[BHR_GemAmethyst]]
	- [[BHR_GemSapphire]]
	- [[BHR_GemEmerald]]
	- [[BHR_GemRuby]]
	- [[BHR_GemDiamond]]
	- [[BHR_TreeLogPine]]
- 随意物件（`RDT`）
	- [[RDT_GlassLapis]]
	- [[RDT_GlassQuartz]]
	- [[RDT_LuminousBlocks]]
	- [[RDT_StepBlock]]
	- [[RDT_SuperLubricentIce]]
	- [[RDT_TreeLogSpectre]]
	- [[RDT_TreeLeavesSpectre]]
	- [[RDT_TreeSaplingSpectre]]
- 奇思妙想（`WI`）
	- [[WI_Aerogel]]
	- [[WI_SugarBlock]]
	- [[WI_GlassSugar]]
	- [[WI_StaticLiquidWater]]
	- [[WI_StaticCobblestone]]
	- [[WI_PackedPlanks]]
- 原版考古（`LGC`）
- 初学开发（`DEV`）
	- [[DEV_BlocksOther]]
	- [[DEV_GhostBlock]]
	- [[DEV_StateBoolean]]
	- [[DEV_StateEnum]]
	- [[DEV_StateEnumAxis]]
	- [[DEV_StateEnumFacing]]
	- [[DEV_StateInt]]

## 方块实体
- 方块愚者（`BHR`）
- 随意物件（`RDT`）
	- #wait4move [[RDT_BlockBreaker]]
- 奇思妙想（`WI`）
- 原版考古（`LGC`）
- 初学开发（`DEV`）
	- #wait4move [[DEV_EntityInt]]
	- #wait4move [[DEV_SimpleWorker]]

## 物品
- 方块愚者（`BHR`）
	- #wait4move [[BHR_FuelEmber]]
	- #deprecated [[BHR_FoodOther]]
	- #todo [[BHR_FoodLow]]
	- #todo [[BHR_FoodRaw]]
	- #todo [[BHR_FoodHigh]]
	- #todo [[BHR_FoodMaximum]]
	- #wait4move [[BHR_FoodLime]]
	- #deprecated [[BHR_GemBlessTest]]
	- #deprecated [[BHR_OtherItems]]
	- #todo [[BHR_ItemsOther]]
	- [[BHR_PineCone]]
- 随意物件（`RDT`）
- 奇思妙想（`WI`）
	- #wait4move [[WI_Prospector]]
	- #deprecated [[WI_ShaderDevRed]]
- 原版考古（`LGC`）
	- #deprecated [[ModItems]]
	- #todo [[LGC_HoneyCrystallized]]
- 初学开发（`DEV`）
	- [[DEV_ItemsOther]]
	- #mixin [[DEV_HardcoreSwitcher]]

## 状态效果
- 方块愚者（`BHR`）
- 随意物件（`RDT`）
	- #deprecated [[RDT_EffectFireImbue]]
	- #deprecated [[RDT_EffectPoisonImbue]]
	- #todo [[RDT_FireImbue]]
	- #todo [[RDT_PoisonImbue]]
- 奇思妙想（`WI`）
	- #deprecated [[WI_EffectBioToxicity]]
	- #deprecated [[WI_EffectRadioactive]]
	- #todo [[WI_BioToxicity]]
	- #todo [[WI_Radioactive]]
- 原版考古（`LGC`）
- 初学开发（`DEV`）

## 组件
- 方块愚者（`BHR`）
	- #deprecated [[BHR_GemBlessing_1]]
	- #todo [[BHR_GemBlessing]]
- 随意物件（`RDT`）
- 奇思妙想（`WI`）
- 原版考古（`LGC`）
- 初学开发（`DEV`）

## 创造模式物品栏
- 方块愚者（`BHR`）
	- [[BHR_General]]
- 随意物件（`RDT`）
	- [[RDT_General]]
- 奇思妙想（`WI`）
	- [[WI_General]]（一个类含 2 个物品栏页，#waiting2check）
- 原版考古（`LGC`）
- 初学开发（`DEV`）
	- [[DEV_General]]

## 其他（Misc）
- [[文档范式说明]]
- [[游戏规则]]
- [[界面与容器]]
- [[配方]]
- [[标签]]
- [[Mixin]]
- [[客户端渲染接线]]
- [[模板遗留方块]]
