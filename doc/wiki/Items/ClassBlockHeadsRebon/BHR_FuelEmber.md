# 余烬（物品）
{`bhr_ember`}
## 特点简介
一种材料物品。可为熔炉提供热量

## 构造
#deprecated 燃料构造器在1.20.5之后的版本已经失效，现有功能已被合并至“组件”

## 组件
#todo 
### 默认组件

| 组件名                 | 值   | 评价          |
| ------------------- | --- | ----------- |
| attribute_modifiers | 默认  |             |
| break_sound         | 默认  |             |
| enchantments        | 默认  |             |
| item_model          | 默认  |             |
| item_name           | 默认  |             |
| lore                | 默认  |             |
| max_stack_size      | 64  | 需要指定，否则无法堆叠 |
| rarity              | 默认  |             |
| repair_cost         | 默认  |             |
| swing_animation     | 默认  |             |
| tooltip_display     | 默认  |             |
| use_effects         | 默认  |             |

### 额外组件
```
Compound components:
	Compound minecraft:cooking_fuel: // 燃料组件
	Int burn_time: 400 // 燃烧时间
```

## 改写方法
- 无

## 事件
- 无

## 获取方式
- 无

## 备注
- 目前版本，燃料不再使用专门的构造器，所有物品均使用默认物品构造器，并使用“组件”来添加功能。
