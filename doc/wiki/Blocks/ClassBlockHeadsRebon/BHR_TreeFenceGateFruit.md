# 果木栅栏门（方块）

> 类：`BHR_TreeFenceGateFruit`　注册名：`bhr_fruit_fence_gate`　模块：BHR
> 状态：**2026-11 新增**，照搬原版橡木对应方块。
> 父级：`Blocks.OAK_FENCE_GATE`（继承 `FenceGateBlock`），属性一律 `BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE)`。

## 基本

- 中文名：果木栅栏门　英文名：Fruit Wood Fence Gate
- 创造模式物品栏：方块愚者（`bhr_general`）→ 树木组，排在果木栅栏之后、果木门之前
- 外观：木料共用贴图 `bhr/fruit_planks`，关闭 / 打开、贴墙 / 独立四种形态

## 构造（属性表）

| 项 | 值 |
| --- | --- |
| 父级 | 橡木栅栏门（`Blocks.OAK_FENCE_GATE`） |
| 硬度 / 爆炸抗性 | 父级（2 / 3） |
| 音效 | 父级（木质） |
| 渲染 | 常规（关闭 / 打开 × 独立 / 贴墙） |
| 方块状态 | `FACING` + `OPEN` + `POWERED` + `IN_WALL` |
| 改写方法 | 无（红石开关、手动开关、贴墙判定由 `FenceGateBlock` 处理） |
| 事件 | 无 |

- 构造器额外参数：`WoodType.OAK`（栅栏门按木种取开关门音效与音高，给橡木即原版栅栏门音色）。
- 可燃性：`FlammableBlockRegistry.getDefaultInstance().add(BHR_FRUIT_FENCE_GATE, 5, 20);`

## 标签

- 方块侧：`data/minecraft/tags/block/fence_gates.json` → `#minecraft:fence_gates` 追加 `bhr_fruit_fence_gate`（果木栅栏门）
- 物品侧：`data/minecraft/tags/item/fence_gates.json` → 同一标签再追加一次（配方认物品侧）
- 两份都用 `"replace": false` **追加**，不覆盖原版内容。
- 「斧可挖掘」**不用单独写**：原版 `data/minecraft/tags/block/mineable/axe.json` 里是
  `"#minecraft:fence_gates"` 这类**标签引用**，进了该标签即传递命中，与原版同类方块同档。

## 资源

| 类型 | 文件 |
| --- | --- |
| 贴图 | `textures/block/bhr/fruit_planks.png`（木料共用） |
| 模型 | `models/block/bhr_fruit_fence_gate.json`、`_open`、`_wall`、`_wall_open`（4 个） |
| blockstate | `blockstates/bhr_fruit_fence_gate.json`（16 个 variants，手写） |
| 物品模型定义 | `items/bhr_fruit_fence_gate.json`（datagen 生成，指向关闭态方块模型） |

## 登记点

1. ✅ 主类 `RichardsWhimsyMod.onInitialize()` → `BHR_TreeFenceGateFruit.OnInit();`
2. ✗ `datagen/BlockStateGenerator` —— 多状态，blockstate 手写（照抄 `oak_fence_gate`，只换模型 id 与贴图）
3. ☐ `ItemModelGenerator.ITEM_MODEL_OVERRIDES` —— 不需要（物品形式即关闭态方块模型）
4. ✅ `datagen/ClassBlockHeadsRebon/BHR_LangEntries`（中英各两条）
5. ✅ `item/BHR_CreativeTabs` 的「树木」组
6. ✅ 资源（贴图 + 模型 + blockstate + items）
7. ◐ 标签已落地（见上）；配方 / 战利品表暂缓
