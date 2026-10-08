# 高级食物（物品）
{注册名待确认}
## 特点简介
方块愚者食物分级中的高级档，饥饿值与饱和度较高。

## 构造
#deprecated 食物构造器在1.20.5之后的版本已经失效，现有功能已被合并至“组件”

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
	Compound minecraft:food: // 食物组件
		Int nutrition: 8 // 补充饥饿值
		Float saturation: 3 // 补充饱和度
```

## 改写方法
- 无

## 事件
- 无

## 获取方式
### 战利品表
- 掉落来源：{待迁移后统一设定}

## 备注
- 来源：`BHR_FoodOther` 拆分（食物分级 High 档）
- 本档目前无具体食物归属
