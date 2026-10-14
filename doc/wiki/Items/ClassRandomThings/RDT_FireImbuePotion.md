# 火焰灌注药水（物品）
{`rdt_fire_imbue_potion`}
## 特点简介
一种药水物品。**饮用**后获得「火焰灌注」状态效果。区别于喷溅/滞留型：本物品只能自己喝。

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
| rarity              | 默认  |     |
| repair_cost         | 默认  |     |
| swing_animation     | 默认  |     |
| tooltip_display     | 默认  |     |
| use_effects         | 默认  |     |

### 额外组件
```
Compound components:
	Int max_stack_size: 1 // 与原版药水一致，不可堆叠
	Compound minecraft:consumable: // 消耗品组件
		String animation: "drink" // 使用动画
		Float consume_seconds: 1.6 // 使用时间
		Sound event sound: "minecraft:entity.generic.drink" // 饮用音
		Boolean has_consume_particles: FALSE // 不生成粒子
		Compound on_consume_effects:
			String type: "apply_effects" // 添加状态效果
			Compound effects:
				String id: "richards-whimsy-mod:rdt_fire_imbue"
				Int duration: 1200
				Byte amplifier: 0
			Float probability: 1.0 // 必定触发
```
- 无 `minecraft:food` 组件：药水不是食物，不补饥饿值
- 整套 `animation` / `consume_seconds` / `sound` / `has_consume_particles` 直接用原版饮用默认配置（`Consumables.defaultDrink()`），本物品只决定「喝下得到什么效果」
- `amplifier` 从 0 起算，所以 `0` 在游戏里显示为火焰灌注 I 级
- 时长为 `1200` tick（60 秒）；因无 wiki 数值，暂取原版药水常见时长

## 改写方法
- 无

## 事件
- 无

## 获取方式
### 战利品表
- 掉落来源：{待迁移后统一设定}

## 备注
- 效果本体见 [[RDT_FireImbue]]；本页只描述物品形态
- ✅ 2026 药水批次：本物品已落地，Java 类为 `RDT_FireImbuePotion`
- 外观用自带贴图 `textures/item/rdt/potion_fire_imbue.png`（不走原版药水纹理 + 着色）
- 铁砧/指令等价写法：`/give @s richards-whimsy-mod:rdt_fire_imbue_potion[consumable={consume_seconds:1.6,animation:"drink",sound:"minecraft:entity.generic.drink",has_consume_particles:false,on_consume_effects:[{type:"minecraft:apply_effects",effects:[{id:"richards-whimsy-mod:rdt_fire_imbue",duration:1200,amplifier:0}],probability:1.0}]}]`
