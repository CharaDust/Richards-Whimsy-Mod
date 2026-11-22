# 常规食物集合

> [!DANGER] 重要信息
> 该Java类即将弃用，重制后该Java类将不再存在，其中包含的内容将被放置在更合适的地方

数值如下：

| 等级      | 饥饿值 | 饱和度 | 包含食物                                                                                                                |
| ------- | --- | --- | ------------------------------------------------------------------------------------------------------------------- |
| Low     | 1   | 0.1 | 亚麻种子{`bhr_flax_seed`}、气泡海带{`bhr_kelp`}、咖啡{`bhr_coffee`}、蠕虫{`bhr_worm`}、瓜子{`bhr_sunflower_seed`}                     |
| Raw     | 3   | 0.4 | 生渡渡鸟腿肉{`bhr_raw_dodo_meat`}、生鱼片{`bhr_raw_fish`}、辣椒{`bhr_chili`}                                                     |
| Medium  | 4   | 1   | 细长胡萝卜{`bhr_carrot`}、玉米{`bhr_corn`}、番茄{`bhr_tomato`}                                                                 |
| High    | 7   | 3   | 烤渡渡鸟腿肉{`bhr_cooked_dodo_meat`}、熟鱼片{`bhr_dooked_fish`}、白列巴{`bhr_bread`}、大饼{`bhr_flatbread`}、彩虹蛋糕{`bhr_rainbow_cake`} |
| Maximun | 15  | 9   | 什锦披萨{`bhr_pizza`}、炖山珍{`bhr_dodo_stew`}、咖喱鱼{`bhr_fish_curry`}                                                        |
## 备注
- 樱桃、椰子、咖啡樱桃、芒果、橙子目前尚未最终确定数值
- 枫实、赤果仅存在于翻译文件

## 实现进度（2026-10）
本页表格里**已有独立 wiki 页**的 10 件食物已全部落地为独立 Java 类（不在这张表里的另算）：

| 档位 | 已落地 | 状态 |
| --- | --- | --- |
| Low | 枫实 `bhr_maple_seed`、愚人蠕虫 `bhr_worm` | ✅ 本次落地（枫实此前只存在于翻译文件） |
| Medium | 渡渡苹果 `bhr_dodo_apple`、樱桃 `bhr_cherry`、椰子 `bhr_coconut`、咖啡樱桃 `bhr_coffee_cherry`、芒果 `bhr_mango`、橙子 `bhr_orange`、赤果 `bhr_prickly_pear` | ✅ 本次落地 |
| （第一批） | 石灰酸橙 `bhr_lime` | ✅ 2026 第一批已落地 |

- 本页表格里其余条目**还没有 Java 类、也没有 wiki 页**，本轮不在落地范围内：
  亚麻种子、气泡海带、咖啡、瓜子、生渡渡鸟腿肉、生鱼片、辣椒、细长胡萝卜、玉米、番茄、
  烤渡渡鸟腿肉、熟鱼片、白列巴、大饼、彩虹蛋糕、什锦披萨、炖山珍、咖喱鱼
- 这些物品里**已有贴图**的：亚麻种子、瓜子、生渡渡鸟腿肉、玉米、烤渡渡鸟腿肉
- **还缺贴图的 13 件**（供美术排期，明细见 `doc/wiki/ItemGroup/ClassBlockHeadsRebon/BHR_General.md` 的「待补资源」）：
  气泡海带 `kelp.png`、咖啡 `coffee.png`、生鱼片 `raw_fish.png`、辣椒 `chili.png`、
  细长胡萝卜 `carrot.png`、番茄 `tomato.png`、熟鱼片 `dooked_fish.png`、白列巴 `bread.png`、
  大饼 `flatbread.png`、彩虹蛋糕 `rainbow_cake.png`、什锦披萨 `pizza.png`、
  炖山珍 `dodo_stew.png`、咖喱鱼 `fish_curry.png`
- 各页「备注」里的**种植类 `#todo` 本轮全部跳过**：对应的树叶/仙人掌方块尚未添加

## 实现进度（2026-11，篝火线）
本页表格里 **5 件**已随「篝火配方补齐」落地，数值按需求方新给的口径重定（**以新口径为准**）：

| 物品 | 注册名 | 饥饿值 / 饱和度（点数） | 旧表 | 类 |
| --- | --- | --- | --- | --- |
| 生鱼片 | `bhr_raw_fish` | 2 / 0.4（原版生鳕鱼） | Raw 3 / 0.4 | `BHR_RawFish` |
| 熟鱼片 | `bhr_cooked_fish` | 5 / 6.0（原版熟鳕鱼） | High 7 / 3 | `BHR_CookedFish` |
| 生渡渡鸟肉 | `bhr_raw_dodo_meat` | 2 / 1.2（原版生鸡肉） | Raw 3 / 0.4 | `BHR_RawDodoMeat` |
| 熟渡渡鸟肉 | `bhr_cooked_dodo_meat` | 6 / 7.2（原版熟鸡肉） | High 7 / 3 | `BHR_CookedDodoMeat` |
| 煎饼 | `bhr_flatbread` | 8 / 3.0（需求原文） | High 7 / 3 | `BHR_Flatbread` |

- ⚠️ **26.2 的 `saturation` 是实际饱和度点数**（`FoodData#eat(FoodProperties)` 直接累加，无倍率）。
  「倍率」写法只在 `FoodProperties.Builder#saturationModifier` 里存在，换算 `n × m × 2`。
- 组件装配收在 `BHR_SimpleFood#apply(properties, nutrition, saturationPoints)` 一处；明细见 [篝火线物品](BHR_CampfireLine.md)
- 仍未落地：气泡海带、咖啡、辣椒、细长胡萝卜、番茄、白列巴、彩虹蛋糕、什锦披萨、炖山珍、咖喱鱼
- ⚠️ 本页上表的旧号（`1 / 0.1`、`3 / 0.4`、`4 / 1`、`7 / 3`、`15 / 9`）是「倍率时代」的写法；
  早期落地的 10 件食物（`bhr_lime` 等）代码里也还写着倍率值（`SATURATION = 1.0F` 之类），
  在新口径下等于「1 点」，实际几乎不补饱和度 —— 待统一换算（`点数 = n × m × 2`）。
