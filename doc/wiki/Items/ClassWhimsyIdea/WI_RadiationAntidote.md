# 辐射解毒剂（物品）
{`wi_radiation_antidote`}
## 特点简介
一种药水物品。**饮用**后获得「抗火」状态效果，用来对抗「电离辐射」造成的持续火焰伤害。
本物品本身不含任何新状态效果，是个纯功能性解药。

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
				String id: "minecraft:fire_resistance"
				Int duration: 1200
				Byte amplifier: 1
			Float probability: 1.0 // 必定触发
```
- 无 `minecraft:food` 组件：药水不是食物，不补饥饿值
- 饮用相关字段全部取自原版默认配置（`Consumables.defaultDrink()`）
- `amplifier` 从 0 起算，所以 `1` 在游戏里显示为抗火 II 级
- 时长 `1200` tick（60 秒），与两瓶正效果同长，方便对消

## 改写方法
- 无

## 事件
- 无

## 获取方式
### 战利品表
- 掉落来源：{待迁移后统一设定}

## 备注
- ✅ 2026 药水批次：本物品已落地，Java 类为 `WI_RadiationAntidote`
- 为什么 WI 的第三瓶是解药：「WI 的两个效果各做药水形式」而 WI 只有两个效果、却要 3 种形态；
  [[WI_Radioactive]] 的持续伤害用的是火焰伤害（`damageSources().inFire()`），抗火最对症，
  正好凑成「两个效果 + 一个对应解药」。若要换成别的效果，照 [[WI_BioToxicityPotion]] 再抄一份即可
- 不依赖任何本模组效果（只用原版抗火），因此注册顺序无要求
- 外观**不使用自定义贴图**：模型 `layer0` 指向原版 `minecraft:item/potion`，
  再由 `items/wi_radiation_antidote.json` 的 `tints` 以 `minecraft:constant` 染成
  `0xFFA500`（= 十进制 `16753920`，橙色，与原版抗火粒子色一致）
- 铁砧/指令等价写法：`/give @s richards-whimsy-mod:wi_radiation_antidote[consumable={consume_seconds:1.6,animation:"drink",sound:"minecraft:entity.generic.drink",has_consume_particles:false,on_consume_effects:[{type:"minecraft:apply_effects",effects:[{id:"minecraft:fire_resistance",duration:1200,amplifier:1}],probability:1.0}]}]`
