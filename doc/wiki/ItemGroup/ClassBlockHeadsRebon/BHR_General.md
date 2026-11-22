# 方块愚者物品栏（创造模式物品栏）
{`bhr_general`}
## 特点简介
方块愚者（BHR）分支的创造模式物品栏页。

## 构造
| 条目   | 值                      |
| ---- | ---------------------- |
| 标题语言键 | `itemGroup.bhr_general` |
| 图标   | 方块愚者徽标 |
| 排序   | 按下方「包含物品」的添加顺序 |

## 包含物品
### 方块与矿物
- 切面板岩、切面板岩铜/铁/锡/煤/金/铂/钛/未知矿石
- 红壤、红壤燧石矿土、红壤粘土矿土、红壤未知矿土
- 石灰石、石灰砖、石灰石油矿、大理石、磨制大理石、海滩沙
### 树木
- 松木原木
- 果木木板（2026-11 新增，见 [BHR_TreePlanksFruit](../../Blocks/ClassBlockHeadsRebon/BHR_TreePlanksFruit.md)）
- 果木台阶（2026-11 新增，见 [BHR_TreeSlabFruit](../../Blocks/ClassBlockHeadsRebon/BHR_TreeSlabFruit.md)）
- 果木楼梯（2026-11 新增，见 [BHR_TreeStairsFruit](../../Blocks/ClassBlockHeadsRebon/BHR_TreeStairsFruit.md)）
- 果木栅栏（2026-11 新增，见 [BHR_TreeFenceFruit](../../Blocks/ClassBlockHeadsRebon/BHR_TreeFenceFruit.md)）
- 果木栅栏门（2026-11 新增，见 [BHR_TreeFenceGateFruit](../../Blocks/ClassBlockHeadsRebon/BHR_TreeFenceGateFruit.md)）
- 果木门（2026-11 新增，见 [BHR_TreeDoorFruit](../../Blocks/ClassBlockHeadsRebon/BHR_TreeDoorFruit.md)）
- 果木活板门（2026-11 新增，见 [BHR_TreeTrapdoorFruit](../../Blocks/ClassBlockHeadsRebon/BHR_TreeTrapdoorFruit.md)）
- 果木告示牌（2026-11 新增，见 [BHR_TreeSignFruit](../../Blocks/ClassBlockHeadsRebon/BHR_TreeSignFruit.md)；物品一件，放下去分立牌 / 墙牌）
- 木床（2026-11 新增，见 [BHR_WoodenBed](../../Blocks/ClassBlockHeadsRebon/BHR_WoodenBed.md)；与原版床同形，
  差别只在愚者模式的睡觉档位 `WOODEN`）
### 其他方块
- 堆肥块
### 宝石
- 紫锂辉、蓝宝石、祖母绿、红刚玉、宝石钻石
### 消耗品
- 余烬（一次性点火消耗品）
- 时间水晶（制作台加速，堆叠 99）
### 食物
- 樱桃、椰子、咖啡樱桃、芒果、橙子、石灰酸橙
- 渡渡苹果、枫实、赤果（梨果仙人掌）、愚人蠕虫
- 生鱼片、熟鱼片、生渡渡鸟肉、熟渡渡鸟肉、煎饼（篝火线，2026-11）
### 材料
- 锡箔、锡桶、锡水桶、面粉、无机盐（篝火线，2026-11）
### 测试物品（非玩法内容）
- 宝石祝福测试棒（见 [BHR_GemBlessTest](../Items/ClassBlockHeadsRebon/BHR_GemBlessTest.md)）

## 备注
- 该物品栏在 `RichardsWhimsyMod` 初始化链中被注册；注册 id 需保持唯一，否则崩溃
- 拆分重制后，「包含物品」列表需同步更新为新的注册名
- ✅ 2026 第一批：已重制 **2 个方块**（松木原木、堆肥块）
- ✅ 2026 第二批：5 个 `#deprecated` 集合类（`BHR__NormalBlocks` / `BHR_Rocks` / `BHR_Soils` / `BHR_Gems`）
  拆成 **25 个独立类**，本页「方块与矿物 / 树木 / 其他方块 / 宝石」四组共 **27 个方块**已全部入栏
  - 注册名按 [注册名对照表](../../../agent/deepseek/06-注册名对照表.md) 落地：`rock*` → `bhr_slate*`、
    `mineral_soil*` → `bhr_soil*`、`slate` → `bhr_cobbleslate`（从 `ModBlocks` 迁入）
- ✅ 2026 第三批（物品/食物阶段，2026-10）：
  - 第一批食物 **1 件**：石灰酸橙
  - 第二批食物 **9 件**：渡渡苹果、樱桃、椰子、咖啡樱桃、芒果、枫实、橙子、赤果（梨果仙人掌）、愚人蠕虫
    —— 数据全部取自各自的 wiki 页组件表，代码口径与石灰酸橙一致
    （`FoodProperties(nutrition, saturation, canAlwaysEat)` + `Consumables.defaultFood().consumeSeconds(…)`）
  - 「包含物品」的**食物组共 10 件**已全部入栏；代码里的排列顺序见 `BHR_CreativeTabs`
    （前 6 件按本页原顺序，其后是本批其余 4 件）
- ✅ 2026 第四批（2026-11）：**余烬**（`bhr_ember`）落地 —— 见 [BHR_Ember](../Items/ClassBlockHeadsRebon/BHR_Ember.md)。
  - 原「燃料」组**取消**：余烬不是燃料，改为一次性点火消耗品，与时间水晶一起归入「消耗品」组
- ✅ 2026 第五批（2026-11，篝火线）：**10 件物品**落地（5 食物 + 5 材料），
  同时把简易篝火的配方按 Blockheads 解包表补齐（13 条，含专家模式的升级到 Lv.2）：
  - 食物：生鱼片、熟鱼片、生渡渡鸟肉、熟渡渡鸟肉、煎饼
  - 材料：锡箔、锡桶、锡水桶、面粉、无机盐
  - 明细见 [BHR_Campfire](../Blocks/ClassBlockHeadsRebon/BHR_Campfire.md#配方) 与 [篝火线物品页](../Items/ClassBlockHeadsRebon/BHR_CampfireLine.md)
- **图标暂用堆肥块**（wiki 要的「方块愚者徽标」是一件尚未重制的物品，原 `BHR_OtherItems` 已标 `#deprecated`）

## 待补资源（食物组）
本节列出 BHR 食物清单里**还没有贴图**的物品，供美术排期用；有贴图的已随物品落地。
约定：贴图放 `src/main/resources/assets/richards-whimsy-mod/textures/item/bhr/<注册名去掉 bhr_ 前缀>.png`，
模型放同命名空间的 `models/item/<注册名>.json`（`parent: item/generated` + `layer0` 指向该贴图）。

| 物品（注册名） | 档位 | 贴图文件 | 状态 |
| --- | --- | --- | --- |
| 气泡海带 `bhr_kelp` | Low | `kelp.png` | ❌ 待画 |
| 咖啡 `bhr_coffee` | Low | `coffee.png` | ❌ 待画 |
| 生鱼片 `bhr_raw_fish` | Raw | `raw_fish.png` | ✅ 2026-11 已落地 |
| 辣椒 `bhr_chili` | Raw | `chili.png` | ❌ 待画 |
| 细长胡萝卜 `bhr_carrot` | Medium | `carrot.png` | ❌ 待画 |
| 番茄 `bhr_tomato` | Medium | `tomato.png` | ❌ 待画 |
| 熟鱼片 `bhr_cooked_fish`（旧名 `bhr_dooked_fish`） | High | `cooked_fish.png` | ✅ 2026-11 已落地 |
| 白列巴 `bhr_bread` | High | `bread.png` | ❌ 待画 |
| 煎饼 `bhr_flatbread` | High | `flatbread.png` | ✅ 2026-11 已落地（8 饥饿 / 3 饱和度） |
| 彩虹蛋糕 `bhr_rainbow_cake` | High | `rainbow_cake.png` | ❌ 待画 |
| 什锦披萨 `bhr_pizza` | Maximun | `pizza.png` | ❌ 待画 |
| 炖山珍 `bhr_dodo_stew` | Maximun | `dodo_stew.png` | ❌ 待画 |
| 咖喱鱼 `bhr_fish_curry` | Maximun | `fish_curry.png` | ❌ 待画 |

- 剩下 10 件仍没有 Java 类（wiki 页也还没写），不在本轮范围内
- 渡渡苹果的贴图复用既有的 `apple.png`（`bhr_apple`「红富士」从未注册过、相关文档已删）
- 2026-11 新落地的 5 件材料在 `textures/item/bhr/` 下：`tin_foil.png`、`tin_bucket.png`、
  `water_tin_bucket.png`、`flour.png`、`mineral_salt.png`
