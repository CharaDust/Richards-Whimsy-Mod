# 果木活板门（方块）

> 类：`BHR_TreeTrapdoorFruit`　注册名：`bhr_fruit_trapdoor`　模块：BHR
> 状态：**2026-11 新增**，照搬原版橡木对应方块。
> 父级：`Blocks.OAK_TRAPDOOR`（继承 `TrapDoorBlock`），属性一律 `BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR)`。

## 基本

- 中文名：果木活板门　英文名：Fruit Wood Trapdoor
- 创造模式物品栏：方块愚者（`bhr_general`）→ 树木组，排在果木门之后、果木告示牌之前
- 外观：单张手绘贴图 `bhr/fruit_trapdoor`，上 / 下 / 打开三态

## 构造（属性表）

| 项 | 值 |
| --- | --- |
| 父级 | 橡木活板门（`Blocks.OAK_TRAPDOOR`） |
| 硬度 / 爆炸抗性 | 父级（3 / 3） |
| 音效 | 父级（木质） |
| 渲染 | 常规（`top` / `bottom` / `open`，另需 `WATERLOGGED`） |
| 方块状态 | `FACING` + `HALF`（`top` / `bottom`）+ `OPEN` + `POWERED` + `WATERLOGGED` |
| 改写方法 | 无（贴墙位置、红石开关、含水由 `TrapDoorBlock` 处理） |
| 事件 | 无 |

- 构造器额外参数：`BlockSetType.OAK`（开关音与红石行为按方块类型取，给橡木即原版音色）。
- 可燃性：`FlammableBlockRegistry.getDefaultInstance().add(BHR_FRUIT_TRAPDOOR, 5, 20);`

## 标签

- 方块侧：`data/minecraft/tags/block/wooden_trapdoors.json` → `#minecraft:wooden_trapdoors` 追加 `bhr_fruit_trapdoor`（果木活板门）
- 物品侧：`data/minecraft/tags/item/wooden_trapdoors.json` → 同一标签再追加一次（配方认物品侧）
- 两份都用 `"replace": false` **追加**，不覆盖原版内容。
- 「斧可挖掘」**不用单独写**：原版 `data/minecraft/tags/block/mineable/axe.json` 里是
  `"#minecraft:wooden_trapdoors"` 这类**标签引用**，进了该标签即传递命中，与原版同类方块同档。

## 资源

| 类型 | 文件 |
| --- | --- |
| 贴图 | `textures/block/bhr/fruit_trapdoor.png` |
| 模型 | `models/block/bhr_fruit_trapdoor_bottom.json`、`_top`、`_open`（3 个，父级 `template_orientable_trapdoor_*`） |
| blockstate | `blockstates/bhr_fruit_trapdoor.json`（16 个 variants，手写，旋转规则照抄 `birch_trapdoor`） |
| 物品模型定义 | `items/bhr_fruit_trapdoor.json` → **`block/bhr_fruit_trapdoor_bottom`**（例外） |

- ⚠️ 没有 `block/bhr_fruit_trapdoor.json` 这个模型，物品模型必须显式指向 `_bottom`，
  这就是它进 `ITEM_MODEL_OVERRIDES` 的原因。

### 与白桦活板门对齐（模型 + blockstate，2026-11 修正）

贴图的构图是照白桦画的（**把手画在下方**），所以行为和资源都要跟白桦一致。
26.2 的原版活板门分成两条血统，**模型模板和 blockstate 的旋转规则都不同**：

| 项 | 旧血统 `oak` / `dark_oak` / `iron` / `copper*` | 新血统 `spruce` / `birch` / `jungle` / `acacia` / `cherry` / `bamboo` / `mangrove` / `crimson` / `warped` / `pale_oak` |
| --- | --- | --- |
| 模型父级 | `template_trapdoor_*` | `template_orientable_trapdoor_*` |
| bottom 的 up 面 UV | `[0, 0, 16, 16]` | `[0, 16, 16, 0]`（上下翻转） |
| 四个 3px 薄边 UV | `[0, 16, 16, 13]`（贴图**底部** 3 行，翻转） | `[0, 0, 16, 3]`（贴图**顶部** 3 行） |
| open 的 west / east | `[16,0,13,16]` / `[13,0,16,16]`，无 rotation | `[0,0,16,3]` / `[0,3,16,0]`，**`"rotation": 90`** |
| closed 的 bottom / top 按 facing 旋转 | **不旋转**（4 个 facing 共用同一个模型） | **`y` = 0 / 90 / 180 / 270** |
| `half=top, open=true` | 只有 `y` | **额外 `x: 180`**（上下翻过来） |

- 后两行是「把手不跟着朝向转」的根因：25/26.x 里 `oak` 的 closed 变体完全不带旋转，
  朝向一变把手方向就错；白桦那套每个 facing 都带 `y`，`top + open` 还要 `x: 180`。
- 本方块最初整套照抄 `oak_*`，2026-11 按需求**把模型父级与 blockstate 一起换成白桦那套**：
  三个模型改 `template_orientable_trapdoor_*`；`blockstates/bhr_fruit_trapdoor.json` 改用
  `birch_trapdoor` 的 16 个 variants（只把模型 id 换成本模组的），因此 closed 状态的贴图会随
  `facing` 旋转、`top + open` 会 `x: 180` 翻转 —— 与白桦完全一致。
- 已核验：其余七个形态（台阶 / 楼梯 / 栅栏 / 栅栏门 / 门 / 告示牌 / 墙牌）在 `oak_*` 与 `birch_*`
  之间**结构完全相同**（blockstate 与模型逐字段比对），只有活板门需要这条「改血统」的覆盖。

## 登记点

1. ✅ 主类 `RichardsWhimsyMod.onInitialize()` → `BHR_TreeTrapdoorFruit.OnInit();`
2. ✗ `datagen/BlockStateGenerator` —— 多状态，blockstate 手写。整套（blockstate + 三个模型）
   照 `birch_trapdoor` 取、只换模型 id 与贴图，与白桦活板门行为一致，见上
3. ✅ `ItemModelGenerator.ITEM_MODEL_OVERRIDES`：`bhr_fruit_trapdoor → block/bhr_fruit_trapdoor_bottom`
4. ✅ `datagen/ClassBlockHeadsRebon/BHR_LangEntries`（中英各两条）
5. ✅ `item/BHR_CreativeTabs` 的「树木」组
6. ✅ 资源（贴图 + 模型 + blockstate + items）
7. ◐ 标签已落地（见上）；配方 / 战利品表暂缓
