# 渡渡苹果（物品）
{`bhr_dodo_apple`}
## 特点简介
一种食物物品。在配方上等价于原版苹果，可代替原版苹果进行合成。也是本模块生物“渡渡鸟（#todo）”的食物。可被用于种植苹果树

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
- #todo 可被用于喂养渡渡鸟
- #todo 在合适的土壤上右键可用于放置苹果树树叶
- ✅ 2026-10 本物品已落地，Java 类为 `BHR_DodoApple`（`item/ClassBlockHeadsRebon/`）。
  本条与 `BHR_Lime` 同一口径：`FoodProperties(4, 1.0F, false)` + `Consumables.defaultFood().consumeSeconds(1.6)`。
  - 贴图用 `textures/item/bhr/apple.png`（旧清单里的 `bhr_apple`「红富士」从未注册过、文档也已删除，
    经确认 `apple.png` 就是本物品的贴图）；模型 `models/item/bhr_dodo_apple.json` 指向它
  - 上面两条 `#todo` **本轮跳过**：苹果树树叶方块与渡渡鸟实体都还没有添加，
    等它们落地后再照 `BHR_PineCone`（松果）的 `useOn` 范式补

## 配方等价原版苹果 —— 调研结论（2026-10）

### 问题
Forge 时代靠**矿物词典**（Ore Dictionary，1.13 起被 **tag** 取代）实现「A 与 B 在合成里等价」。
Fabric 没有矿物词典，对应物是**物品标签**（`data/<namespace>/tags/item/<name>.json`）。
但**光建标签不足以让本物品顶替原版苹果**，原因是原版配方并没有用标签。

### 事实核对（对 26.2 的 `minecraft-common.jar` 逐条提取）
| 事实 | 证据 |
| --- | --- |
| 26.2 原版用到苹果的配方**只有一条** | 全 1585 条原版配方里只有 `data/minecraft/recipe/golden_apple.json` 提到 `minecraft:apple` |
| 那条配方把苹果**写死成物品 id，不是标签** | `{"type":"minecraft:crafting_shaped","key":{"#":"minecraft:gold_ingot","X":"minecraft:apple"},"pattern":["###","#X#","###"],"result":{"id":"minecraft:golden_apple"}}` |
| 原版**不存在** `minecraft:apples` / 任何水果类物品标签 | `data/minecraft/tags/item/` 下 227 个标签里没有任何 apple/fruit 条目 |
| Fabric **没有**「替换/注入原版配方」的公开 API | `fabric-recipe-api-v1` 只给了 `FabricRecipeManager#getAllMatches` / `#getAllOfType` 与 `FabricRecipeAccess#getSynchronizedRecipes`，全是**只读**查询；配方加载路径上没有事件钩子 |

结论：**改不动原版那条金苹果配方**。想要「渡渡苹果能代替原版苹果合成」，只能由本模组自己把那条配方再写一份。

### 可选方案
| 方案 | 做法 | 效果 | 代价 |
| --- | --- | --- | --- |
| **A. 照抄金苹果配方** | 在 `data/richards-whimsy-mod/recipe/` 下加一条独立 id 的 `crafting_shaped`：8 金锭 + `bhr_dodo_apple` → `minecraft:golden_apple` | 玩家**能**用渡渡苹果合成原版金苹果（这是「等价」唯一能落到玩法上的解释） | 配方书里会出现两条结果同为金苹果的条目（原版那条仍在，无法删）；合成表界面按结果并列显示 |
| **B. 只用标签** | 建 `richards-whimsy-mod:apples`（`minecraft:apple` + `bhr_dodo_apple`），并把渡渡苹果加进 `c:foods/fruit`；本模组**自己**的配方一律用标签当材料 | 本模组后续所有配方天然两种苹果通吃；与其它模组按 `c:` 约定互通 | 对**原版**金苹果无效（它不认标签） |
| **A + B** | 两个都做 | 玩法与扩展性都拿到 | 多一个标签文件，工作量差别不大 |

- Fabric 的 `c:` 约定标签由 `fabric-convention-tags-v2`（本项目已随 Fabric API 加载）定义，
  水果类是 `data/c/tags/item/foods/fruit.json` —— 方案 B 的「对外接口」就用它。
- 26.2 配方 JSON 的写法已简化：`key` 的值可以直接写 `"minecraft:apple"` 字符串，`result` 用 `"id"` 字段
  （见上表原版原文）；本项目现有的 `data/richards-whimsy-mod/recipes/enchant_golden_apple.json`
  还是老写法（`{"item": ...}` / `key` 用对象），两种在 26.2 都能读。
- **当前状态：方案未落地。** 用户决定（2026-10）本轮只在本文档记录调研结论，
  配方留到「获取方式/配方」阶段统一做。落地时按下表改：
  | 落地点 | 文件 | 内容 |
  | --- | --- | --- |
  | 方案 A | `data/richards-whimsy-mod/recipe/dodo_golden_apple.json` | 照抄原版 `golden_apple.json`，把 `X` 换成 `richards-whimsy-mod:bhr_dodo_apple` |
  | 方案 B | `data/richards-whimsy-mod/tags/item/apples.json` | `{"values":["minecraft:apple","richards-whimsy-mod:bhr_dodo_apple"]}` |
  | 方案 B | `data/c/tags/item/foods/fruit.json` | `{"values":["richards-whimsy-mod:bhr_dodo_apple"]}`（`c:` 命名空间要自己写，Fabric 的约定标签文件只定义空标签，靠模组追加） |
