# 木床（方块）

> 类：`BHR_WoodenBed`　注册名：`bhr_wooden_bed`　模块：BHR
> 状态：**2026-11 新增**，表现与原版床完全一致（双格 / 入睡 / 重生点 / 村民占床全部继承）。
> 父级：`Blocks.BED.pick(DyeColor.BROWN)`（继承 `BedBlock`，属性 `BlockBehaviour.Properties.ofFullCopy(...)`）。

## 基本

- 中文名：木床　英文名：Wooden Bed
- 创造模式物品栏：方块愚者（`bhr_general`）→ 树木组，排在果木告示牌之后、松木树叶之前
- 外观：**自建 Blockbench 模型**（床头 `bhr_wooden_bed_head` + 床脚 `bhr_wooden_bed_foot`），
  建模朝向照 `minecraft:block/template_bed_head` / `template_bed_foot`（床头板贴北面、床尾板贴南面）；
  六张手绘贴图在 `textures/block/bhr/wooden_bed_{head,foot}_{end,side,up}.png`
- 物品形态：**复合模型** —— `items/bhr_wooden_bed.json` 用 `minecraft:composite` 把床头与床脚拼成一张完整的床
  （照抄原版 `items/white_bed.json`，床脚 `translation: [0, 0, 1]`）

## 构造（属性表）

| 项 | 值 |
| --- | --- |
| 父级 | 棕色床（`Blocks.BED.pick(DyeColor.BROWN)`） |
| 硬度 / 爆炸抗性 | 父级（0.2 / 0.2） |
| 音效 | 父级（木质） |
| 特殊物理 | 父级：`noOcclusion` + `bounceRestitution 0.75`（落在床上减摔落伤害）+ `ignitedByLava`（岩浆会点着）+ `pushReaction DESTROY`（活塞直接推掉） |
| 碰撞箱 | 父级（床面 `y = 3…9`，与模型高度无关；模型床头板高到 16 只影响外观） |
| 方块状态 | `FACING` + `PART`（`head` / `foot`）+ `OCCUPIED`（父类提供） |
| 改写方法 | 无（放置补另一半、入睡、起床、重生点、村民占床、双格联动、`part=head` 才掉落，全部是 `BedBlock` 的行为） |
| 事件 | 无 |

> ⚠️ 26.2 的床不是一组 `Blocks.WHITE_BED` 常量了，而是收进 `Blocks.BED` 这个
> `ColorCollection<Block>`：取其中一张用 `Blocks.BED.pick(DyeColor)`，写 `Blocks.WHITE_BED` 会直接编译失败。
>
> ⚠️ 方块物品必须是 **`BedItem`**，不能用 `BlockItem`：`BedItem#placeBlock` 用
> `setBlock(pos, state, 26)`（带 `UPDATE_KNOWN_SHAPE`），先落的床脚才不会在
> `setPlacedBy` 补床头之前被自己的 `updateShape` 判成「另一半是空气」而当场删掉。
> 用普通 `BlockItem`（flags = 3）放床，只会留下床头。

## 愚者模式的睡觉差异（本块唯一与原版床不同的地方）

原版睡觉只有一种速度；愚者模式把「床有多好」分成四档（`BHR_BedTier`）：
**席地而睡 `FLOOR` < 木床 `WOODEN` < 原版床 `SOFT` < 金床 `GOLD`**，
每档决定**精力补充速率**、**睡满奖励的黄条点数**、**睡眠时精力条上画几个箭头**。

| 项目 | 木床（`WOODEN`） | 原版床（`SOFT`） | 席地而睡（`FLOOR`） |
| --- | --- | --- | --- |
| 精力补充（每世界 tick） | **1.5** 点 | 2.0 点 | 1.0 点 |
| 睡满奖励（黄条，`bhr_barr_super_energy`） | **2400**（= 120 s） | 6000（= 300 s） | 无 |
| 睡眠时精力条箭头数 | **3** 个 | 4 个 | 2 个 |

- **差异不在方块类里，而在一张方块标签**：`richards-whimsy-mod:fool_bed_wooden`
  （`data/richards-whimsy-mod/tags/block/fool_bed_wooden.json`）。
  `BHR_BedTier#of` 每 tick 读一次「睡的那格是什么方块」：命中金床标签 → `GOLD`，
  命中木床标签 → `WOODEN`，是任意 `BedBlock` → `SOFT`，其它（含 `null`）→ `FLOOR`。
- **数值只在 `BHR_BedTier` 里**：调木床手感改 `BHR_BedTier.WOODEN` 的三个参数，**不要**动 `BHR_WoodenBed`。
- **时间加速/跳夜的口径与床档无关**：愚者睡够比例后世界时钟 ×20、跳夜按 1 点/世界 tick 平账，
  那段逻辑在 `BHR_Sleep`，木床只是「补得更快 / 奖励更少」。
- 非愚者模式（生存/创造等）**完全不受影响**：档位只在愚者模式的睡眠结算里被读取，
  原版床的睡眠流程照旧。

## 资源

| 类型 | 文件 |
| --- | --- |
| 贴图 | `textures/block/bhr/wooden_bed_head_{end,side,up}.png`、`wooden_bed_foot_{end,side,up}.png` |
| 模型 | `models/block/bhr_wooden_bed_head.json`、`bhr_wooden_bed_foot.json`（手写；`parent` = `minecraft:block/template_bed`，只为继承物品 / 手持的 display 变换） |
| 源文件 | `models/block/bhr_wooden_bed_{head,foot}.bbmodel`（Blockbench 工程，留在仓库里方便改模型） |
| blockstate | `blockstates/bhr_wooden_bed.json`（8 个 variants = `facing` × `part`，手写；与 `occupied` 无关，照抄原版 `white_bed`） |
| 物品模型定义 | `items/bhr_wooden_bed.json` → `minecraft:composite`（床头 + 床脚；**datagen 产出**） |

- 模型与 Blockbench 原导出的两处差别（已按本项目范式修正，几何与 UV **完全照原稿**）：
  1. 贴图变量原来写的是无命名空间的 `wooden_bed_head_end`（会去找 `textures/wooden_bed_head_end.png`），
     改成 `richards-whimsy-mod:block/bhr/wooden_bed_head_end`；
  2. Blockbench 里没赋贴图的 `#missing` 面（腿的顶面、床垫朝内的那一面）**整面删掉** ——
     原版模型允许 `faces` 只列部分方向，写 `#missing` 反而会去找不存在的贴图。
- 另外给落在方块边界上的面补了 `cullface`（床脚北/东西/下、床头板北/东西、床垫东西），
  否则贴墙摆床时内侧面不会被剔除（范式 §7 硬约束 5）。
- ⚠️ `items/` 是 `ItemModelGenerator` 独占的产出，手写进去会被下一次 datagen 抹掉；
  木床的复合模型因此在 `ItemModelGenerator.ITEM_COMPOSITE_MODELS` 里登记。

## 标签

| 标签 | 文件 | 用途 |
| --- | --- | --- |
| `richards-whimsy-mod:fool_bed_wooden` | `data/richards-whimsy-mod/tags/block/fool_bed_wooden.json` | 愚者模式床档 → `BHR_BedTier.WOODEN` |
| 战利品表 | `data/richards-whimsy-mod/loot_table/blocks/bhr_wooden_bed.json` | 照抄原版床：`survives_explosion` + **只在 `part=head` 时掉一件**（拆床脚时 `updateShape` 会把床头 `destroyBlock(..., true)`，所以拆哪一半都能拿到一件床） |

- 没进 `#minecraft:beds`：原版床本身也不在 `#mineable/axe` 里（硬度 0.2、任何工具同速），
  进该标签目前没有任何原版行为消费，故不加。
- 没有登记可燃性：原版床不是可燃方块（`FlammableBlockRegistry` 里没有床），木床保持一致。

## 登记点

1. ✅ 主类 `RichardsWhimsyMod.onInitialize()` → `BHR_WoodenBed.OnInit();`
2. ✗ `datagen/BlockStateGenerator` —— 多状态（`facing` × `part`），blockstate 手写
3. ✅ `ItemModelGenerator.ITEM_COMPOSITE_MODELS`：`bhr_wooden_bed → [block/bhr_wooden_bed_head, block/bhr_wooden_bed_foot(+1 z)]`
4. ✅ `datagen/ClassBlockHeadsRebon/BHR_LangEntries`（中英各两条：`block.…` + `item.…`）
5. ✅ `item/BHR_CreativeTabs` 的「树木」组
6. ✅ 资源（6 张贴图 + 2 个模型 + blockstate + items）
7. ◐ 愚者模式标签 ☑、战利品表 ☑；**配方暂缓**（「获取方式」阶段统一做；
   原游戏配方是「木头 ×1 → 木床 ×1，2 s，制作台」，接 `bhr_recipe` 时按那条落地）

## 待核实 / 未做

- **客户端实机观感未验证**：模型几何、贴图 UV、物品栏里复合模型的拼接位置都只是静态核对
  （贴图路径存在、blockstate / items 指向的模型存在）；需要 `runClient` 看一眼：
  床放下去朝向对不对、床头板 / 床尾板高度、物品栏图标是不是一张完整的床。
- **愚者模式的睡觉手感未实测**：3 个箭头、2400 黄条、1.5 点/世界 tick 都是照 `BHR_BedTier` 读出来的值，
  实机上「睡满要多久」还没跟软床对比过。
- 原游戏的木床可以直接睡过夜晚；本实现遵循 Minecraft 的睡眠规则（需要夜间/雷雨、不在床上时按比例判定），
  与原作手感不同 —— 这一条属于愚者模式整体睡眠设计，不在本块里改。
