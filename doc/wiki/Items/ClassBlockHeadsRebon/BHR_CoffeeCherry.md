# 咖啡樱桃（物品）
{`bhr_coffee_cherry`}
## 特点简介
一种食物物品。可被用于种植咖啡樱桃树。

## 构造
#deprecated 食物构造器在1.20.5之后的版本已经失效，现有功能已被合并至“组件”

## 组件
### 默认组件

| 组件名                 | 值   | 评价  |
| ------------------- | --- | --- |
| attribute_modifiers | 默认  |     |
| break_sound         | 默认  |     |
| enchantments        | 默认  |     |
| item_model          | 默认  |     |
| item_name           | 默认  |     |
| lore                | 默认  |     |
| max_stack_size      | 默认  |     |
| rarity              | 默认  |     |
| repair_cost         | 默认  |     |
| swing_animation     | 默认  |     |
| tooltip_display     | 默认  |     |
| use_effects         | 默认  |     |

### 额外组件
```
Compound components:
	Compound minecraft:food: // 食物组件
		Boolean can_always_eat: FALSE // 无视饥饿值食用
		Int nutrition: 4 // 补充饥饿值
		Float saturation: 1 // 饱和度倍率（实际饱和度 = 饥饿值 × 该值）
	Compound minecraft:consumable: // 消耗品组件
		String animation: "eat" // 使用动画
		Float consume_seconds: 1.6 // 使用时间
		Boolean has_consume_particles: TRUE // 生成粒子
```
- `animation`、`has_consume_particles` 与咀嚼音直接用原版「普通食物」的默认配置（`Consumables.defaultFood()`），
  本物品只覆盖 `consume_seconds` 

## 改写方法
- 无

## 事件
- 无

## 获取方式
### 战利品表
- 掉落来源：{待迁移后统一设定}

## 备注
- 目前版本，食物不再使用专门的构造器，所有物品均使用默认物品构造器，并使用“组件”来添加功能
- #todo 在合适的土壤上右键可用于放置咖啡樱桃树树叶
- ✅ 2026-10 本物品已落地，Java 类为 `BHR_CoffeeCherry`（`item/ClassBlockHeadsRebon/`）：`FoodProperties(4, 1.0F, false)` + `Consumables.defaultFood().consumeSeconds(1.6)`；贴图 `textures/item/bhr/coffee_cherry.png`、模型 `models/item/bhr_coffee_cherry.json`（均为既有资源）
  - 上面的 `#todo` **本轮跳过**：咖啡樱桃树树叶方块还没有添加，等它落地后照 `BHR_PineCone`（松果）的 `useOn` 范式补