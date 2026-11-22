# 果木台阶（方块）

> 类：`BHR_TreeSlabFruit`　注册名：`bhr_fruit_slab`　模块：BHR
> 状态：**2026-11 新增**，照搬原版橡木对应方块。
> 父级：`Blocks.OAK_SLAB`（继承 `SlabBlock`），属性一律 `BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB)`。

## 基本

- 中文名：果木台阶　英文名：Fruit Wood Slab
- 创造模式物品栏：方块愚者（`bhr_general`）→ 树木组，排在果木木板之后、果木楼梯之前
- 外观：木料共用贴图 `bhr/fruit_planks`，下半砖 / 上半砖 / 双半砖三态，贴图与木板一致

## 构造（属性表）

| 项 | 值 |
| --- | --- |
| 父级 | 橡木台阶（`Blocks.OAK_SLAB`） |
| 硬度 / 爆炸抗性 | 父级（2 / 3） |
| 音效 | 父级（木质） |
| 渲染 | 常规（三态：bottom / top / double） |
| 方块状态 | `TYPE`（`bottom` / `top` / `double`）+ `WATERLOGGED` |
| 改写方法 | 无（`SlabBlock` 自带放置、含水、掉落逻辑） |
| 事件 | 无 |

> 可燃性不在 `ofFullCopy` 的复制范围内（它在火焰蔓延那一侧），另行登记：
> `FlammableBlockRegistry.getDefaultInstance().add(BHR_FRUIT_SLAB, 5, 20);` —— (5, 20) 是原版木板原值。

## 标签

- 方块侧：`data/minecraft/tags/block/wooden_slabs.json` → `#minecraft:wooden_slabs` 追加 `bhr_fruit_slab`（果木台阶）
- 物品侧：`data/minecraft/tags/item/wooden_slabs.json` → 同一标签再追加一次（配方认物品侧）
- 两份都用 `"replace": false` **追加**，不覆盖原版内容。
- 「斧可挖掘」**不用单独写**：原版 `data/minecraft/tags/block/mineable/axe.json` 里是
  `"#minecraft:wooden_slabs"` 这类**标签引用**，进了该标签即传递命中，与原版同类方块同档。

## 资源

| 类型 | 文件 |
| --- | --- |
| 贴图 | `textures/block/bhr/fruit_planks.png`（木料共用） |
| 模型 | `models/block/bhr_fruit_slab.json`、`bhr_fruit_slab_top.json`（2 个） |
| blockstate | `blockstates/bhr_fruit_slab.json`（3 个 variants，手写） |
| 物品模型定义 | `items/bhr_fruit_slab.json`（datagen 生成，指向方块模型） |

## 登记点

1. ✅ 主类 `RichardsWhimsyMod.onInitialize()` → `BHR_TreeSlabFruit.OnInit();`
2. ✗ `datagen/BlockStateGenerator` —— 多状态，blockstate 手写（照抄 `oak_slab`，只换模型 id 与贴图）
3. ☐ `ItemModelGenerator.ITEM_MODEL_OVERRIDES` —— 不需要（整块方块的物品形式即方块模型）
4. ✅ `datagen/ClassBlockHeadsRebon/BHR_LangEntries`（中英各两条：`block.…` 与 `item.…`）
5. ✅ `item/BHR_CreativeTabs` 的「树木」组
6. ✅ 资源（贴图 + 模型 + blockstate + items）
7. ◐ 标签已落地（见上）；配方 / 战利品表按全项目既定决定暂缓
