# 果木门（方块）

> 类：`BHR_TreeDoorFruit`　注册名：`bhr_fruit_door`　模块：BHR
> 状态：**2026-11 新增**，照搬原版橡木对应方块。
> 父级：`Blocks.OAK_DOOR`（继承 `DoorBlock`），属性一律 `BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR)`。

## 基本

- 中文名：果木门　英文名：Fruit Wood Door
- 创造模式物品栏：方块愚者（`bhr_general`）→ 树木组，排在果木栅栏门之后、果木活板门之前
- 外观：门板上下两段各一张手绘贴图（`fruit_door_top` / `fruit_door_bottom`）；
  物品形态是独立的 2D 图 `textures/item/bhr/fruit_door.png`，不用方块模型

## 构造（属性表）

| 项 | 值 |
| --- | --- |
| 父级 | 橡木门（`Blocks.OAK_DOOR`） |
| 硬度 / 爆炸抗性 | 父级（3 / 3） |
| 音效 | 父级（木质） |
| 渲染 | 常规（上半 / 下半 × 左 / 右 × 关 / 开，共 8 个方块模型） |
| 方块状态 | `FACING` + `HALF`（`upper` / `lower`）+ `HINGE` + `OPEN` + `POWERED` |
| 改写方法 | 无（双格联动、红石开关、铰链由 `DoorBlock` 处理） |
| 事件 | 无 |

- 构造器额外参数：`BlockSetType.OAK`（门的开关声、红石行为按方块类型取，给橡木即原版音色）。
- 可燃性：`FlammableBlockRegistry.getDefaultInstance().add(BHR_FRUIT_DOOR, 5, 20);`
- 填装两格：破坏上半或下半会同时消去另一半，掉落一件 —— 父级行为。

## 标签

- 方块侧：`data/minecraft/tags/block/wooden_doors.json` → `#minecraft:wooden_doors` 追加 `bhr_fruit_door`（果木门）
- 物品侧：`data/minecraft/tags/item/wooden_doors.json` → 同一标签再追加一次（配方认物品侧）
- 两份都用 `"replace": false` **追加**，不覆盖原版内容。
- 「斧可挖掘」**不用单独写**：原版 `data/minecraft/tags/block/mineable/axe.json` 里是
  `"#minecraft:wooden_doors"` 这类**标签引用**，进了该标签即传递命中，与原版同类方块同档。

## 资源

| 类型 | 文件 |
| --- | --- |
| 贴图 | `textures/block/bhr/fruit_door_top.png`、`fruit_door_bottom.png`；物品图 `textures/item/bhr/fruit_door.png` |
| 模型 | `models/block/bhr_fruit_door_{top,bottom}_{left,right}[_open].json`（8 个） |
| blockstate | `blockstates/bhr_fruit_door.json`（32 个 variants，手写） |
| 物品模型定义 | `items/bhr_fruit_door.json` → **`item/bhr_fruit_door`**（例外，2D 图） |

- ⚠️ 门的物品形式必须走 `ItemModelGenerator.ITEM_MODEL_OVERRIDES`：
  方块模型是三段式拼装，直接指向任何一个都无法在物品栏正确显示，故单独给了 2D 物品图。

## 登记点

1. ✅ 主类 `RichardsWhimsyMod.onInitialize()` → `BHR_TreeDoorFruit.OnInit();`
2. ✗ `datagen/BlockStateGenerator` —— 多状态，blockstate 手写（照抄 `oak_door`，只换模型 id 与贴图）
3. ✅ `ItemModelGenerator.ITEM_MODEL_OVERRIDES`：`bhr_fruit_door → item/bhr_fruit_door`
4. ✅ `datagen/ClassBlockHeadsRebon/BHR_LangEntries`（中英各两条）
5. ✅ `item/BHR_CreativeTabs` 的「树木」组
6. ✅ 资源（贴图 + 模型 + blockstate + items）
7. ◐ 标签已落地（见上）；配方 / 战利品表暂缓
