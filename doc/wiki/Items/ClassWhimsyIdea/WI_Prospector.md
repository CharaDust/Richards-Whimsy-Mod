# 探矿器（物品）
{`wi_prospector`}
## 特点简介
一种工具物品。可以探测下方最近的矿物

## 构造
#deprecated 最大耐久构造器在1.20.5之后的版本已经失效，现有功能已被合并至“组件”

## 组件
#todo 
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
	Int minecraft:max_damage 64 // 最大耐久组件
```

## 改写方法
- #deprecated 探测矿物，代码目前可能不可用

## 事件
- 无

## 获取方式
- 无

## 备注
- 目前版本，具有耐久的物品不再使用专门的构造器，所有物品均使用默认物品构造器，并使用“组件”来添加功能。
