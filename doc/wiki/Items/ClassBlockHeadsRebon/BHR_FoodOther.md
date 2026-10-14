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
