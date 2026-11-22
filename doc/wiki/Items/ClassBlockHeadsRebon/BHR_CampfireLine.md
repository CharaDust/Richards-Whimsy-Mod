# 篝火线物品（材料 5 件 + 食物 5 件，2026-11 落地）

> 这一批是「把简易篝火的配方补齐」时的配套物品 —— 13 条篝火配方要用到的原料与产物。
> 单独成页而不是一件一页：它们除了「配方里的角色」以外没有各自独立的玩法，
> 数值差异都在下面这张表里，一件一页会得到 10 份几乎相同的文档。
>
> 代码：`src/main/java/com/rcdmake/whimsy/item/ClassBlockHeadsRebon/` 下同名类；
> 贴图：`textures/item/bhr/<注册名去掉 bhr_ 前缀>.png`；
> 模型：`models/item/<注册名>.json`（`item/generated`），`items/<注册名>.json` 由 datagen 生成。

## 材料（5 件）

| 物品 | 注册名 | 类 | 说明 |
| --- | --- | --- | --- |
| 锡箔 | `bhr_tin_foil` | `BHR_TinFoil` | 烤鱼裹一层；专家模式升级篝火也要 5 张。获取待定（Blockheads 里是金属工作台） |
| 锡桶 | `bhr_tin_bucket` | `BHR_TinBucket` | **空**锡桶，也是配方 `container` 的返还物。获取待定；舀水留 TODO |
| 锡水桶 | `bhr_water_tin_bucket` | `BHR_WaterTinBucket` | 装了水的锡桶，最大堆叠 1。右键倒水 → 变回空锡桶 |
| 面粉 | `bhr_flour` | `BHR_Flour` | 煎饼原料 ×5。获取待定（研磨线未落地） |
| 无机盐 | `bhr_mineral_salt` | `BHR_MineralSalt` | 锡水桶煮出 ×10；煎饼原料 ×1 |

### 锡桶 / 锡水桶为什么不是原版水桶
需求原文：「Bucket of Water 不能用原版的水桶，因为这个桶设定上是锡做的，**不能装岩浆**」。
- `BHR_WaterTinBucket` **不继承**原版 `BucketItem`：`getEmptySuccessItem` 是 static、
  倒完水写死变**原版空桶**，继承过来锡桶设定当场破功。
- 它自己写 `use`，但倒水仍**借原版实现**：`((BucketItem) Items.WATER_BUCKET).emptyContents(...)`
  （26.2 里这个方法是 public）——水面放置、含水方块、音效、粒子全白拿。
- 于是它既不是 `BUCKET` 也不是 `WATER_BUCKET` 的替身：原版岩浆桶 / 炼药锅那一圈按物品相等判定的逻辑碰不到它，
  「锡桶装不了岩浆」自然成立。

## 食物（5 件）

| 物品 | 注册名 | 类 | 饥饿值 | 饱和度（点数） | 数值出处 |
| --- | --- | --- | --- | --- | --- |
| 生鱼片 | `bhr_raw_fish` | `BHR_RawFish` | 2 | 0.4 | 原版生鳕鱼 `Foods.COD`（2 / 0.1 倍率 → 2×0.1×2） |
| 熟鱼片 | `bhr_cooked_fish` | `BHR_CookedFish` | 5 | 6.0 | 原版熟鳕鱼 `Foods.COOKED_COD`（5 / 0.6 → 6） |
| 生渡渡鸟肉 | `bhr_raw_dodo_meat` | `BHR_RawDodoMeat` | 2 | 1.2 | 原版生鸡肉 `Foods.CHICKEN`（2 / 0.3 → 1.2） |
| 熟渡渡鸟肉 | `bhr_cooked_dodo_meat` | `BHR_CookedDodoMeat` | 6 | 7.2 | 原版熟鸡肉 `Foods.COOKED_CHICKEN`（6 / 0.6 → 7.2） |
| 煎饼 | `bhr_flatbread` | `BHR_Flatbread` | 8 | 3.0 | 需求原文（「8 饥饿值 3 饱和度」） |

- **⚠️ 26.2 的 `minecraft:food.saturation` 是「实际饱和度点数」，不是倍率**（2026-11 反编译核实）：
  `FoodData#eat(FoodProperties)` 直接 `add(nutrition, saturation)`，不乘营养、不乘 2；
  旧的「倍率」口径只存在于 `FoodProperties.Builder#saturationModifier`，它在 `build()` 时
  用 `FoodConstants.saturationByModifier(n, m) = n × m × 2` 换算后塞进同一个字段。
  所以：写 `3.0F` 就是 3 点；想表达「倍率 0.6」得自己算 `n × 0.6 × 2`。
- 5 件食物的组件装配收在 **`BHR_SimpleFood#apply(properties, nutrition, saturationPoints)`** 一处
  （`food` + `consumable`，`Consumables.defaultFood()` 起手，无附加效果），参数名写明是**点数**，
  符合 itemv1.md §7 第 11 条「同一条实现只有一份」。
- 这 5 件的 wiki 旧口径与本次落地的差异：旧 `BHR_FoodOther` 表把生食定在 `3 / 0.4`、熟食 `7 / 3`，
  本轮按需求方新给的「参考原版鸡肉与鳕鱼」为准 —— 以本页为准。
- ⚠️ 与本批之前的 10 件食物（`BHR_Lime` 等）**口径不一致**：那 10 件写的是 `SATURATION = 1.0F / 0.6F / 0.1F` 这类
  「倍率」数值，在新口径下就是「1 点 / 0.6 点」，实际几乎不补饱和度 —— 属于同一处历史遗留，待统一处理。

## 配方里的角色（详见 [BHR_Campfire](../Blocks/ClassBlockHeadsRebon/BHR_Campfire.md#配方)）

- 普通模式（全 Lv.1）：熟渡渡鸟肉 ← 生渡渡鸟肉；熟鱼片 ← 生鱼片 + 锡箔；
  无机盐 ×10 ← 锡水桶；煎饼 ×5 ← 面粉 ×5 + 锡水桶 + 无机盐
- 专家模式（Lv.2 起，需先升级篝火）：同上四条
- 盐与煎饼两条带 `"container": "richards-whimsy-mod:bhr_tin_bucket"` —— 每份返还 1 个空锡桶

## 获取方式（尚未落地，统一留到「获取方式」阶段）

| 物品 | 计划来源 | 现状 |
| --- | --- | --- |
| 生鱼片 | 钓鱼 | ❌ |
| 生渡渡鸟肉 | 渡渡鸟（生物未落地） | ❌ |
| 面粉 | 研磨台（未落地） | ❌ |
| 锡箔 / 锡桶 | 金属工作台（锡线未落地） | ❌ |
| 锡水桶 | 空锡桶舀水 | ❌（TODO 在 `BHR_TinBucket` 类注释里） |
