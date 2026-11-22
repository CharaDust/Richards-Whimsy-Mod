# 果木楼梯（方块）

> 类：`BHR_TreeStairsFruit`　注册名：`bhr_fruit_stairs`　模块：BHR
> 状态：**2026-11 新增**，照搬原版橡木对应方块。
> 父级：`Blocks.OAK_STAIRS`（继承 `StairBlock`），属性一律 `BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS)`。

## 基本

- 中文名：果木楼梯　英文名：Fruit Wood Stairs
- 创造模式物品栏：方块愚者（`bhr_general`）→ 树木组，排在果木台阶之后、果木栅栏之前
- 外观：木料共用贴图 `bhr/fruit_planks`，直楼梯 / 内角 / 外角三种模型

## 构造（属性表）

| 项 | 值 |
| --- | --- |
| 父级 | 橡木楼梯（`Blocks.OAK_STAIRS`） |
| 硬度 / 爆炸抗性 | 父级（2 / 3） |
| 音效 | 父级（木质） |
| 渲染 | 常规（`straight` / `inner_left|right` / `outer_left|right`） |
| 方块状态 | `FACING` + `HALF`（`bottom` / `top`）+ `SHAPE` + `WATERLOGGED` |
| 改写方法 | 无（放置、转角、含水由 `StairBlock` 处理） |
| 事件 | 无 |

- 构造器额外参数：`baseState = Blocks.OAK_PLANKS.defaultBlockState()`
  （`StairBlock` 需要一块「本体」方块用于硬度、音效与掉落推导，这里给的是果木木板对应的橡木木板）。
- 可燃性：`FlammableBlockRegistry.getDefaultInstance().add(BHR_FRUIT_STAIRS, 5, 20);`

## 标签

- 方块侧：`data/minecraft/tags/block/wooden_stairs.json` → `#minecraft:wooden_stairs` 追加 `bhr_fruit_stairs`（果木楼梯）
- 物品侧：`data/minecraft/tags/item/wooden_stairs.json` → 同一标签再追加一次（配方认物品侧）
- 两份都用 `"replace": false` **追加**，不覆盖原版内容。
- 「斧可挖掘」**不用单独写**：原版 `data/minecraft/tags/block/mineable/axe.json` 里是
  `"#minecraft:wooden_stairs"` 这类**标签引用**，进了该标签即传递命中，与原版同类方块同档。

## 资源

| 类型 | 文件 |
| --- | --- |
| 贴图 | `textures/block/bhr/fruit_planks.png`（木料共用） |
| 模型 | `models/block/bhr_fruit_stairs.json`、`bhr_fruit_stairs_inner.json`、`bhr_fruit_stairs_outer.json`（3 个） |
| blockstate | `blockstates/bhr_fruit_stairs.json`（40 个 variants = 朝向 4 × 半 2 × 形状 5，手写） |
| 物品模型定义 | `items/bhr_fruit_stairs.json`（datagen 生成，指向方块模型） |

## 登记点

1. ✅ 主类 `RichardsWhimsyMod.onInitialize()` → `BHR_TreeStairsFruit.OnInit();`
2. ✗ `datagen/BlockStateGenerator` —— 多状态，blockstate 手写（照抄 `oak_stairs`，只换模型 id 与贴图）
3. ☐ `ItemModelGenerator.ITEM_MODEL_OVERRIDES` —— 不需要（楼梯的物品形式即方块模型）
4. ✅ `datagen/ClassBlockHeadsRebon/BHR_LangEntries`（中英各两条）
5. ✅ `item/BHR_CreativeTabs` 的「树木」组
6. ✅ 资源（贴图 + 模型 + blockstate + items）
7. ◐ 标签已落地（见上）；配方 / 战利品表暂缓
