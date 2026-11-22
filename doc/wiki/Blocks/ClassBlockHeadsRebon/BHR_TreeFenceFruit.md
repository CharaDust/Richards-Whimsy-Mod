# 果木栅栏（方块）

> 类：`BHR_TreeFenceFruit`　注册名：`bhr_fruit_fence`　模块：BHR
> 状态：**2026-11 新增**，照搬原版橡木对应方块。
> 父级：`Blocks.OAK_FENCE`（继承 `FenceBlock`），属性一律 `BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE)`。

## 基本

- 中文名：果木栅栏　英文名：Fruit Wood Fence
- 创造模式物品栏：方块愚者（`bhr_general`）→ 树木组，排在果木楼梯之后、果木栅栏门之前
- 外观：木料共用贴图 `bhr/fruit_planks`，柱子 + 四向连接臂；物品形态用「库存」模型

## 构造（属性表）

| 项 | 值 |
| --- | --- |
| 父级 | 橡木栅栏（`Blocks.OAK_FENCE`） |
| 硬度 / 爆炸抗性 | 父级（2 / 3） |
| 音效 | 父级（木质） |
| 渲染 | 常规（multipart：柱子 + 四向连接臂） |
| 方块状态 | `NORTH` / `EAST` / `SOUTH` / `WEST` + `WATERLOGGED` |
| 改写方法 | 无（连接判定、碰撞箱、含水由 `FenceBlock` 处理） |
| 事件 | 无 |

- 碰撞箱比外观窄（1.5 格高），站不上去 —— 父级行为，与原版栅栏一致。
- 可燃性：`FlammableBlockRegistry.getDefaultInstance().add(BHR_FRUIT_FENCE, 5, 20);`

## 标签

- 方块侧：`data/minecraft/tags/block/wooden_fences.json` → `#minecraft:wooden_fences` 追加 `bhr_fruit_fence`（果木栅栏）
- 物品侧：`data/minecraft/tags/item/wooden_fences.json` → 同一标签再追加一次（配方认物品侧）
- 两份都用 `"replace": false` **追加**，不覆盖原版内容。
- 「斧可挖掘」**不用单独写**：原版 `data/minecraft/tags/block/mineable/axe.json` 里是
  `"#minecraft:wooden_fences"` 这类**标签引用**，进了该标签即传递命中，与原版同类方块同档。

## 资源

| 类型 | 文件 |
| --- | --- |
| 贴图 | `textures/block/bhr/fruit_planks.png`（木料共用） |
| 模型 | `models/block/bhr_fruit_fence_post.json`、`bhr_fruit_fence_side.json`、`bhr_fruit_fence_inventory.json`（3 个） |
| blockstate | `blockstates/bhr_fruit_fence.json`（5 段 multipart，手写） |
| 物品模型定义 | `items/bhr_fruit_fence.json` → **`block/bhr_fruit_fence_inventory`**（例外） |

- ⚠️ 没有 `block/bhr_fruit_fence.json` 这个模型：栅栏的方块模型是 multipart 拼的，
  因此物品模型必须指向 `_inventory`，这就是它进 `ITEM_MODEL_OVERRIDES` 的原因。

## 登记点

1. ✅ 主类 `RichardsWhimsyMod.onInitialize()` → `BHR_TreeFenceFruit.OnInit();`
2. ✗ `datagen/BlockStateGenerator` —— 多状态，blockstate 手写（照抄 `oak_fence`，只换模型 id 与贴图）
3. ✅ `ItemModelGenerator.ITEM_MODEL_OVERRIDES`：`bhr_fruit_fence → block/bhr_fruit_fence_inventory`
4. ✅ `datagen/ClassBlockHeadsRebon/BHR_LangEntries`（中英各两条）
5. ✅ `item/BHR_CreativeTabs` 的「树木」组
6. ✅ 资源（贴图 + 模型 + blockstate + items）
7. ◐ 标签已落地（见上）；配方 / 战利品表暂缓
