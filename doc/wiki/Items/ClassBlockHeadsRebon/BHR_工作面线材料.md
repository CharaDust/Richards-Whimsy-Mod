# 工作面线材料（4 件物品）

> 类：`BHR_Linen` {`bhr_linen`}、`BHR_BronzeIngot` {`bhr_bronze_ingot`}、`BHR_CopperIngot` {`bhr_copper_ingot`}、`BHR_TinIngot` {`bhr_tin_ingot`}
> 模块：BHR　状态：**2026-12 新增**
> 父级：无（普通 `Item`，无组件、无右键行为，默认堆叠 64）

## 基本

木工台配方与后续冶炼线要用的四件材料。本轮只做**白板物品**（注册 + 贴图 + 语言 + 创造栏），
获取途径随后续工作面接通。

| 物品 | 中文名 | 英文名 | 贴图 | 当前获取途径 |
| --- | --- | --- | --- | --- |
| `bhr_linen` | 亚麻布 | Linen | ✅ `item/bhr/linen.png` | 暂无（将来：裁缝台，亚麻×5 → 1） |
| `bhr_bronze_ingot` | 青铜锭 | Bronze Ingot | ✅ `item/bhr/bronze_ingot.png` | 暂无（将来：冶炼线，铜锭×2 + 锡锭×1 → 3） |
| `bhr_copper_ingot` | 铜锭 | Copper Ingot | ⚠️ 贴图未入库，模型暂借 `minecraft:item/copper_ingot` 占位 | 暂无（将来：矿石 → 熔炉） |
| `bhr_tin_ingot` | 锡锭 | Tin Ingot | ⚠️ 贴图未入库，模型暂借 `minecraft:item/iron_ingot` 占位 | 暂无（将来：矿石 → 熔炉） |

## 用途（已接线）

- 亚麻布：木工台「船」的原料（普通 2 个 / 专家 10 个）
- 青铜锭：木工台专家版「果木门」「果木活板门」的原料
- 铜锭 / 锡锭：目前仅是青铜锭的上游材料，尚无配方引用

## 资源

| 类型 | 文件 |
| --- | --- |
| 模型 | `models/item/bhr_linen.json`、`bhr_bronze_ingot.json`（`item/generated` + 自有贴图）；`bhr_copper_ingot.json`、`bhr_tin_ingot.json`（借原版贴图占位） |
| 物品模型定义 | `items/bhr_*.json`（datagen 生成） |

## 待补

- 铜锭 / 锡锭的自有贴图（有图后只改 `models/item/*.json` 的 `layer0`）；
- 四件的正式获取途径（裁缝台 / 冶炼线）。

## 登记点

1. ✅ 主类物品区（**必须早于 `BHR_FoolRecipes.OnInit()`**：配方按 id 查物品）
2. ✅ `BHR_LangEntries`（中英）
3. ✅ `BHR_CreativeTabs`（材料组，接在篝火线材料之后）
4. ✅ 资源
5. ◐ 配方：已作为原料被引用；自身产出配方待后续工作面
