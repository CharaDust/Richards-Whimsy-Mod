# 果木木板（方块）

> 类：`BHR_TreePlanksFruit`　注册名：`bhr_fruit_planks`　模块：BHR
> 状态：**2026-11 新增**，本页按用户口径「照搬橡木木板」记。
> 改名记录：最初按「松木木板」（`bhr_pine_planks`）落地，核对原游戏 The Blockheads 后确认
> **所有木料共用同一种木板**，故改名为「果木木板」——它不隶属于某一种树。

## 基本

- 中文名：果木木板　英文名：Fruit Wood Planks
- 创造模式物品栏：方块愚者（`bhr_general`）→ 树木组，排在松木原木之后、松木树叶之前

## 构造（属性表）

| 项 | 值 |
| --- | --- |
| 父级 | 橡木木板（`Blocks.OAK_PLANKS`） |
| 硬度 / 爆炸抗性 | 父级（2 / 3） |
| 音效 / 乐器 | 父级（木质） |
| 渲染 | 常规（不透明，六面同贴图） |
| 方块状态 | 无（单状态） |
| 改写方法 | 无 |
| 事件 | 无 |

> 实现上就是 `BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)` 一行，
> 与「包装木材」「通灵木板」同一套写法；没有一行覆写。

## 标签

照搬橡木木板。26.2 里原版 `oak_planks` 直接出现在两个标签中：

| 目录/文件 | 内容 | 落地文件 |
| --- | --- | --- |
| `data/minecraft/tags/block/planks.json` | `#minecraft:planks`（方块） | `src/main/resources/data/minecraft/tags/block/planks.json` |
| `data/minecraft/tags/item/planks.json` | `#minecraft:planks`（物品，配方认这一条） | `src/main/resources/data/minecraft/tags/item/planks.json` |

- 挖掘工具**不需要**单独写：原版 `data/minecraft/tags/block/mineable/axe.json` 里写的是
  `"#minecraft:planks"` 这一条**标签引用**（已对 26.2 的 `minecraft-common.jar` 逐字核验），
  进了 `#minecraft:planks` 就等于进了「斧可挖掘」，所以果木木板与原版木板同档。
- 两份文件都用 `"replace": false` **追加**，不覆盖原版内容（符合 `doc/wiki/Misc/标签.md` 的「重制注意」）。
- ⚠️ `data/minecraft/tags/blocks/…`（**复数**）是 1.20.4 时代的路径，1.21 起目录单数化，
  那些老文件在 26.2 里不生效 —— 新增标签一律写在单数路径下。

## 可燃性

`ofFullCopy` 只复制方块属性，「能不能被点着」在火焰蔓延那一侧，所以另行登记：

```
FlammableBlockRegistry.getDefaultInstance().add(BHR_FRUIT_PLANKS, 5, 20);
```

(5, 20) 是原版木板的原值，与橡木木板同档可燃。

## 获取方式

- 合适工具：斧（经 `#minecraft:planks` 传递命中，见上）
- 破坏掉落：{待之后统一设定}（战利品表按全项目既定决定暂缓）
- 配方：{待之后统一设定}

## 资源

| 类型 | 路径 |
| --- | --- |
| 贴图 | `textures/block/bhr/fruit_planks.png`（原 `pine_planks.png` 改名而来，同一张手绘图） |
| 模型 | `models/block/bhr_fruit_planks.json`（`cube_all`） |
| blockstate | `blockstates/bhr_fruit_planks.json`（由 datagen 生成） |
| 物品模型定义 | `items/bhr_fruit_planks.json`（由 datagen 生成，指向方块模型） |

## 登记点

1. ✅ 主类 `RichardsWhimsyMod.onInitialize()` → `BHR_TreePlanksFruit.OnInit();`
2. ✅ `datagen/BlockStateGenerator.collectAll()`（单状态）
3. ☐ `ItemModelGenerator.ITEM_MODEL_OVERRIDES` —— 不需要（整块方块的物品形式就是方块模型）
4. ✅ `datagen/ClassBlockHeadsRebon/BHR_LangEntries`（中英各两条：`block.…` 与 `item.…`）
5. ✅ `item/BHR_CreativeTabs` 的「树木」组
6. ✅ 资源（贴图 + 模型 + blockstate + items）
7. ◐ 标签已落地（见上）；配方 / 战利品表暂缓

## 同家族

2026-11 与木板同批落地的 7 个果木方块（都继承原版家族类 + `ofFullCopy` 对应橡木块 + 可燃性 (5, 20)）：

- [果木台阶](BHR_TreeSlabFruit.md)　[果木楼梯](BHR_TreeStairsFruit.md)　[果木栅栏](BHR_TreeFenceFruit.md)
- [果木栅栏门](BHR_TreeFenceGateFruit.md)　[果木门](BHR_TreeDoorFruit.md)　[果木活板门](BHR_TreeTrapdoorFruit.md)
- [果木告示牌](BHR_TreeSignFruit.md)（含墙牌与自有方块实体类型）
