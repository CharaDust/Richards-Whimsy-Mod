# The Blockheads 配方参考库

这里存放从 **The Blockheads 1.7.6** 提取/整理的配方数据，**仅作参考**。
目前**没有**任何一条配方被写进模组，后续由你逐条决定：哪些直接搬、哪些要改、哪些不要。

## 演示页

`recipes-demo.html` —— 单文件、双击即可打开（数据内嵌，无需服务器）。
左侧选工作台，右上切「普通 / 专家」，可搜索物品名、隐藏升级条目。
数据来源是三套标记：**反推ID**（wiki 普通配方对齐二进制反推）、**名字表**（未验证）、
**耗时**（二进制 `secondsRequired`，游戏原值）。

## 文件

| 文件 | 用途 |
| --- | --- |
| `recipes-demo.html` | **演示页**（单文件，含 2427 条记录） |
| `recipes.md` | **主文档**：254 条配方，按「分类 → 工作台 → 等级」排版，Obsidian 里直接读；末尾附「专家模式差异」 |
| `recipes.bh` | 同一批数据的自定义纯文本格式（紧凑、可脚本解析），适合以后批量转换 |
| `../ref/theblockhead/wiki/` | 原始 wiki 页面抓取结果（254 个物品页 + 34 个工作台页），溯源用 |
| `../ref/theblockhead/extracted/recipes.json` | 机器可读的原始数据（字段见下） |
| `../ref/theblockhead/extracted/recipes_binary_v2.json` | **二进制提取版**（2427 条，含耗时/等级/来源标记） |
| `../ref/theblockhead/extracted/数据来源与可信度.md` | 各字段的可信度约定 |
| `../ref/theblockhead/extracted/expert_variants.json` | 专家模式专属/变更配方（目前只有 2 条，见 §专家模式） |

## `recipes.bh` 语法

```text
[workbench] Workbench  { category=core fuel=no electric=no max_level=4 }
  L2:
    Kiln x1   <=  Stone x1 + Clay x3 + Stick x10   @-
```

- `[bench_id]`：工作台标识；花括号里是属性
  - `category`：`core` 主工作台 / `bench` 手工台 / `heat` 需燃料 / `electric` 需电力 / `power` 发电 / `misc` 特殊
  - `fuel` / `electric`：是否需要燃料 / 电力
  - `max_level`：该台子最高等级
- `L<n>:`：该工作台要求的等级
- 行格式：`产物 x数量 <= 材料 x数量 [+ 材料 x数量]... @耗时`
- `@-` 表示 wiki 没记录耗时（多为工作台本体与部分老配方），可自行按等级设定

## `recipes.json` 字段

```json
{
  "bench": "tool_bench", "level": 1,
  "output": "Flint Axe", "output_count": 1,
  "inputs": [{"item": "Stick", "count": 1}, {"item": "Flint", "count": 3}],
  "seconds": 4, "_src": "craft"
}
```

字段与 APK 里逆向出的 `CraftableItem` 结构一一对应（`requiredItems[8]` / `requiredItemCounts[8]` /
`secondsRequired` / `upgradeLevel`），所以将来接自定义配方类型时基本可以直接映射。

## 数据可信度

| 来源 | 可信度 | 说明 |
| --- | --- | --- |
| `_src = "workbench_page"` | 高 | Workbench 页面明确写了「产物（数量 材料）」 |
| `_src = "craft"` | 高 | 物品页 `{{Craft}}` 模板，带 `Bench` 与 `Time` |
| `_src = "bench_table"` | 中 | 工作台页 `{{Bench|entry}}` 产物表，等级取自小节标题 |

## 专家模式（Expert Mode）

1.7 版加入的专家模式会**大幅改动配方表**，例如：

| 物品 | 普通模式 | 专家模式 |
| --- | --- | --- |
| Portal | 1 Stone + 120 Time Crystal | **10 Stone + 5 Iron Ingot** + 120 Time Crystal |
| Compost | 无此配方 | **1 Worm + 10 任意有机物**（Compost Bin） |
| 开局 Blockhead | 带 Flint Spade + Basket + 5 个果蔬 | **一无所有**（非配方，但影响开局） |

**目前的窘境**：wiki 只有零星几句提到专家模式，没有成体系的对照表。
已抓取的 288 个页面里，涉及"expert"的配方性描述只有上面 2 条。
完整的专家模式配方表只存在于游戏本体的 ARM 代码里（与普通配方同样是在运行时构造的），
wiki 无法补齐。

数据结构已预留扩展位：`expert_variants.json` 的每条记录带 `mode: "expert"`，
可直接与 `recipes.json` 合并；`recipes.md` 末尾的「专家模式差异」小节由它渲染。

## 已知缺口

1. **工作台本体耗时**：Workbench 的 28 条配方 wiki 未给 `Time`，全是 `@-`。
2. **缺配方的工作台**：`pizza_oven`、`sluice`、`refinery`、`electric_sluice` 无独立 wiki 页。
3. **等级可能有偏差**：部分 `{{Craft}}` 只写了 `Bench=Tool Bench` 没写等级，被默认成 L1
   （例如 Crowbar、Iron Axe）。这批可以用 `_src` + wiki 原文复核。
4. **未做物品映射**：`output` / `inputs` 仍是 TB 英文名，没有对应到 Minecraft 物品 id。
5. **专家模式几乎是空白**：只有 2 条已知记录，见上一节。

## 重新生成

```bash
# 依次执行（需联网，PYTHONPATH 指向脚本目录）
python ref/theblockhead/tools/wiki/fetch_benches.py
python ref/theblockhead/tools/wiki/fetch_items.py
python ref/theblockhead/tools/wiki/parse_craft.py
python ref/theblockhead/tools/wiki/parse_bench_tpl.py
python ref/theblockhead/tools/wiki/parse_workbench_page.py
python ref/theblockhead/tools/wiki/merge_final.py
python ref/theblockhead/tools/wiki/build_benches.py
python ref/theblockhead/tools/wiki/emit_docs.py
```
