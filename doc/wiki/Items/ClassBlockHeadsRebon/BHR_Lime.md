# 石灰酸橙（物品）
{`bhr_lime`}
## 特点简介
一种食物物品。吃下后有几率获得急迫效果

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
		Boolean can_always_eat: TRUE // 无视饥饿值食用
		Int nutrition: 3 // 补充饥饿值
		Float saturation: 1 // 饱和度倍率（实际饱和度 = 饥饿值 × 该值）
	Compound minecraft:consumable: // 消耗品组件
		String animation: "eat" // 使用动画
		Float consume_seconds: 1.6 // 使用时间
		Boolean has_consume_particles: TRUE // 生成粒子
		Compound on_consume_effects:
			String type: "apply_effects" // 添加状态效果
			Compound effects:
				String id: "minecraft:haste"
				Int duration: 400
				Byte amplifier: 1
			Float probability: 0.2 // 触发几率
```
- `animation`、`has_consume_particles` 与咀嚼音直接用原版「普通食物」的默认配置（`Consumables.defaultFood()`），
  本物品只覆盖 `consume_seconds` 与 `on_consume_effects`
- `amplifier` 从 0 起算，所以 `1` 在游戏里显示为急迫 II 级

## 改写方法
- 无

## 事件
- 无

## 获取方式
### 战利品表
- 掉落来源：{待迁移后统一设定}

## 备注
- 目前版本，食物不再使用专门的构造器，所有物品均使用默认物品构造器，并使用“组件”来添加功能
- ✅ 2026 物品阶段第一批：本物品已落地（2026/10/11），Java 类为 `BHR_Lime`
  （旧类 `BHR_FoodLime` 已按「食物不再用 Food 前缀」的约定改名，其余 5 个水果等各自的 wiki 页写好后陆续迁移）
- 铁砧/指令等价写法（已经在 26.2 上核过两处组件的字段名）：`/give @s richards-whimsy-mod:bhr_lime[food={nutrition:3,saturation:1.0,can_always_eat:true},consumable={consume_seconds:1.6,on_consume_effects:[{type:"minecraft:apply_effects",effects:[{id:"minecraft:haste",duration:400,amplifier:1}],probability:0.2}]}]`
