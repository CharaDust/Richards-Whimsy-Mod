# 松果（物品）
{`bhr_pine_cone`}
## 特点简介
一种可用于种植松木的物品。

## 构造
（普通物品，无特殊构造器；功能由“组件”定义）

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
（无）

## 改写方法
- 无

## 事件
### 交互（手持松果右键方块时）
- 当手持松果交互以下方块时，在其上方放置一个松木树叶，方块状态为 `bhr_hormone_type=dominant`、`bhr_hormone_count=X`（X 为 10~15 的随机整数，每个数机会均等）：
	- bhr：松木原木、红壤、红壤燧石矿石、红壤粘土矿石
	- vanilla：泥土、缠根泥土、草方块、灰化土、砂土、菌丝体、耕地、苔藓块、苍白苔藓块、泥巴、沾泥的红树根

## 获取方式
- 无

## 备注
- 组件页的 `max_stack_size = 64` 是默认值，代码里无需显式指定
- 交互细则（上方必须可放置、生存模式消耗 1 个松果）由实现补齐，若与预期不符请回到本页修正
