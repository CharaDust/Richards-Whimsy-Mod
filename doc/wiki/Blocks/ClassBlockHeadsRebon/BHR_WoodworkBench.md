# 木工台（方块）

> 类：`BHR_WoodworkBench`　注册名：`bhr_woodwork_bench`　模块：BHR
> 状态：**2026-12 新增**
> 父级：`Blocks.CRAFTING_TABLE`（`BlockBehaviour.Properties.ofFullCopy`），`instabreak()` + 木质音效

## 基本

- 中文名：木工台　英文名：Woodwork Bench
- 创造模式物品栏：方块愚者（`bhr_general`）→ 与制作台、简易篝火、加固平台相邻
- 定位：制作台的**木工版**——同一套工作面内核（见 [工作面](../Misc/工作面.md)），只是配方表不同、无燃料
- 等级：**1~3**，目前只有 L1 配方（L2 由原版投影供料，L3 待定）

## 构造（属性表）

| 项 | 值 |
| --- | --- |
| 父级 | 工作台（`Blocks.CRAFTING_TABLE`） |
| 硬度 / 爆炸抗性 | 父级，`instabreak()`（瞬间破坏、不需正确工具） |
| 音效 | 木质（`SoundType.WOOD`） |
| 渲染 | 常规整方块，顶/底共用 `woodwork_bench_top`，侧面 `woodwork_bench_side` |
| 方块状态 | `level`（1~29 的共用属性，本台夹到 1~3；blockstate 用 catch-all 单变体，等级不改外观） |
| 改写方法 | 无（全部继承 `WorkingSurfaceBlock`） |
| 事件 | 无 |

- 侧面的备用图 `woodwork_bench_side2.png` 是旧稿，保留在资源目录里但**未接线**。
- 方块物品带 `workbench_level` 组件的原型值（与制作台、篝火共用同一个组件），
  所以「挖下来再放下」等级不变，不同等级天然不堆叠。

## 配方

配方目录 `data/richards-whimsy-mod/bhr_recipe/bhr_woodwork_bench/`（普通 11 条 + 专家 11 条），
`order` 0~11 决定配方栏顺序；原版投影的 81 条排在它们之后（无 `order`，按 recipe id 字母序）。

| # | 产物 | 普通 | 专家 |
| --- | --- | --- | --- |
| 0 | 木床 `bhr_wooden_bed` | 木板×1 / 20s | 木板×5 + 线×4 |
| 1 | 箱子 `minecraft:chest` | 木板×1 / 20s | 木板×5 + 皮革×1 |
| 2 | 船 `minecraft:oak_boat` | 木板×2 + 亚麻布×2 / 20s | 木板×10 + 亚麻布×10 |
| 3 | 梯子 ×10 | 木板×1 / 20s | 木板×1 / 2 个 |
| 4 | 果木门 `bhr_fruit_door` | 木板×3 / 20s | 木板×10 + 青铜锭×1 |
| 5 | 果木告示牌 `bhr_fruit_sign` | 木板×1 / 20s | 木板×5 |
| 6 | 果木活板门 `bhr_fruit_trapdoor` | 木板×1 + 梯子×1 / 20s | 木板×10 + 梯子×1 + 青铜锭×1 |
| 7 | 加固平台 ×5 | 木板×1 + 铁锭×1 / 20s | 木板×5 + 铁锭×1 |
| 8 | 物品展示框 `minecraft:item_frame` | 木板×1 / 20s | 木板×5 + 皮革×1 |
| 9 | 果木栅栏 ×4 | 木板×2 / **10s** | 木板×2 / **2 个、20s** |
| 10 | 果木栅栏门 `bhr_fruit_fence_gate` | 木板×1 / 20s | 木板×5 + 铁锭×1 |
| 11 | 喂食箱子 `bhr_feeder_chest` | 箱子×1 + 锡桶×1 / 20s | 同左 |

**升级 L2**（升级栏，不进配方栏）：`#minecraft:logs`×5 / 180s；专家版 logs×10 + 铁锭×1 / 180s。

**原版配方（L2 / 5 秒，共 81 条）**：门 12、活板门 12、栅栏 12、栅栏门 12、船 10、告示牌 12、展示架 11。
材料**严格按原版**（橡木门只认橡木木板），筛选规则在 `surface/vanilla_recipes.json`；
楼梯 / 台阶 / 按钮 / 压力板**不属于**本台，留给以后的工作面。

## 获取方式

### 战利品表

#### 采集

- 挖掘木工台的合适工具：徒手即可（`instabreak`）
- 木工台被破坏后掉落：一件**带当前等级**的木工台物品

### 其它来源

- **本体配方暂缓**：按约定「所有类型工作面加完后再统一编写」，目前只能创造模式 / 指令获取。
  届时预计在制作台 L1：木板×1。

## 资源

| 类型 | 文件 |
| --- | --- |
| 贴图 | `textures/block/bhr/woodwork_bench_top.png`、`woodwork_bench_side.png`（`_side2.png` 为旧稿） |
| 模型 | `models/block/bhr_woodwork_bench.json`（`cube_bottom_top`，顶 + 底都用 top） |
| blockstate | `blockstates/bhr_woodwork_bench.json`（catch-all 单变体，手写） |
| 物品模型定义 | `items/bhr_woodwork_bench.json`（datagen 生成，指向方块模型） |
| 界面 | 复用 `textures/gui/container/workbench.png` |

## 登记点

1. ✅ 主类 `RichardsWhimsyMod.onInitialize()` → `BHR_WoodworkBench.OnInit();`
2. ✗ `datagen/BlockStateGenerator` —— 有 `level` 状态，blockstate 手写
3. ☐ `ItemModelGenerator.ITEM_MODEL_OVERRIDES` —— 不需要（物品形式即方块模型）
4. ✅ `datagen/ClassBlockHeadsRebon/BHR_LangEntries`（中英：方块名 + 界面标题）
5. ✅ `item/BHR_CreativeTabs`
6. ✅ 资源（贴图 + 模型 + blockstate + items）
7. ✅ 配方（本地 22 条 + 原版投影 81 条）
8. ✅ 客户端：`RichardsWhimsyModClient` 把 `WorkingSurfaceScreen` 接到本台的菜单类型
