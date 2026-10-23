# 通灵树苗（方块）
{`rdt_spectre_sapling`}
## 特点简介
一种半透明树木的树苗方块。种下后会长成**白桦形状**的通灵树（树干 = 通灵原木，树冠 = 通灵树叶）。
## 构造
父级：橡树树苗

| 条目      | 值     | 评价                                                     |
| ------- | ----- | ------------------------------------------------------ |
| 合适的挖掘工具 | 见「获取方式」 |                                                        |
| 硬度      | 父级    | 原版树苗是「瞬间破坏」                                            |
| 爆炸抗性    | 父级    |                                                        |
| 表面阻力系数  | 父级    |                                                        |
| 速度乘数    | 父级    |                                                        |
| 跳跃乘数    | 父级    |                                                        |
| 弹性碰撞系数  | 父级    |                                                        |
| 引燃几率    | 60    | 与原版树苗同档；比通灵原木(30)高一档                                   |
| 烧毁几率    | 100   | 同上                                                     |
| 熔岩可燃性   | 父级    |                                                        |
| 窒息方块    | 父级    |                                                        |
| 视野阻挡    | 父级    |                                                        |
| 红石导体    | 父级    |                                                        |
| 可替代     | 父级    |                                                        |
| 概念性固体方块 | 父级    |                                                        |
| 活塞推动行为  | 父级    |                                                        |
| 乐器      | 父级    |                                                        |
| 地图颜色    | 父级*   | 待定，该方块为淡蓝色                                             |
| 声音组     | 父级    |                                                        |
| 光线阻挡    | 父级    | 父级 = 原版树苗的「否」                                          |
| 表面生成生物  | 父级    |                                                        |
| 碰撞      | 父级    | 父级 = 原版树苗的**无碰撞**（可穿过）；副作用是水会冲掉它，与原版一致，见「备注」            |
| 亮度      | 父级    |                                                        |

## 渲染
- 方块用原版X型植物模型（`block/cross`，两张交叉的平面）
- 透光状态：依据纹理Alpha通道透明（`rdt/spectre_sapling.png` 整图 Alpha 只有 **0（61.72%）与 195（38.28%）** 两档 → 半透明档）
- 26.1 起不再需要手动注册渲染层，半透明档由贴图 Alpha 自动判定
- **物品形式是 2D 精灵，不是那个 3D 十字**（2026-10 修正）：原版**所有 cross 型植物方块**
	（树苗、花 `dandelion`、甘蔗 `sugar_cane`…）的物品形式都是一张 2D 图 ——
	`items/<id>.json` → `item/<id>`，`models/item/<id>.json` = `parent: item/generated`
	+ `layer0` 指向**同一张方块贴图**（26.2 逐条核验；对比树叶 `oak_leaves` 与原木 `oak_log`，
	原版那两块**就是**指向 `block/<id>`，拿在手上是 3D 的）
	- 本项目因此在 `ItemModelGenerator.ITEM_MODEL_OVERRIDES` 里加了
		`rdt_spectre_sapling → item/rdt_spectre_sapling`：该生成器的默认规则是
		「只要是 BlockItem 就指向 `block/<id>`」，不列例外的话**手上会举着一个 3D 十字**
	- 方块世界里的模型与手持 2D 图**共用** `rdt/spectre_sapling.png` 这一张贴图（与原版同款做法）
	- ⚠️ `items/<id>.json` 是生成器独占的产出，**手写会被下一次 datagen 抹掉** ——
		改这条必须改 `ITEM_MODEL_OVERRIDES`（生成器注释里已记同一坑）

## 改写方法
- **无需改写**：种植、长大、骨粉这三件事原版 `SaplingBlock` 一件不落全都有，
	而且方块状态就一个 `STAGE`，与本方块的需求完全重合 —— 本类除了「用哪个生长器」与「可燃性登记」之外没有业务代码
- **长出来的树 = 白桦形状 + 通灵材质**：由数据驱动（配置特征 JSON），Java 侧只交出它的 key
	- 文件：`data/richards-whimsy-mod/worldgen/configured_feature/rdt_spectre_tree.json`
	- 该 JSON 是**照抄原版 `birch.json`**（逐行从 26.2 客户端 jar 提取），只换两个方块提供器：
		- `trunk_provider`：原版 `birch_log`（`axis=y`）→ `rdt_spectre_log`
		- `foliage_provider`：原版 `birch_leaves` → `rdt_spectre_leaves`
	- 「白桦形状」的全部秘密就是两行形状参数（橡木的 `base_height` 是 4，白桦是 5）：
		- `trunk_placer`：`straight_trunk_placer`，`base_height=5`、`height_rand_a=2`、`height_rand_b=0` → 树干 5~7 格
		- `foliage_placer`：`blob_foliage_placer`，`height=3`、`offset=0`、`radius=2`
		- `minimum_size`：`two_layers_feature_size`
	- ⚠️ `foliage_provider` 里必须写全 `distance` / `persistent` / `waterlogged` 三个属性 ——
		它们与本方块（继承原版树叶）的状态名一一对应，少写或改名会让树一长出来就报「状态不合法」

## 事件
### 种植
- 可种植基底：**`#minecraft:supports_vegetation`**（原版泥土 / 草方块 / 耕地 / 苔藓块那一整类），
	与橡树树苗**完全同一份清单** —— 由父级 `VegetationBlock` 的「下方方块判定」提供，无需额外代码
- 种在别处放不下去；下方方块被挖掉时树苗会掉落（同原版）

### 生长（获得随机刻时）
- 条件与概率同原版：**上方亮度 ≥ 9** 且本次随机刻掷到 **1/7** → 长大
- 长大的过程交给生长器（`TreeGrower`）：按 key 取出上面那份 `rdt_spectre_tree` 配置特征并放置
- 只长**单体树**：不参与「2×2 树苗 → 巨树」的判定（生长器的 `megaTree` 位刻意留空），
	也不要求附近有花（`flowers` 位留空）
- ✅ **已实测（2026-10，服务器端到端）**：把树苗种在泥土上、把随机刻拉满，它自己长成了通灵树 ——
	树干是通灵原木、树冠是通灵树叶且没有凋零。同一批实验里放了**原版橡树树苗作对照**，两者都长出来了。
	- ⚠️ **测试注意**：「上方亮度 ≥ 9」里的亮度**会把夜晚算进去**（天光被压暗 11）。
		第一次探针在晚上跑，树苗（连同原版对照组）都不长 —— 那是环境不是实现。
		以后做这类测试要先 `time set day`。
	- ⚠️ 顺带记两条 26.2 命令改名：游戏规则已改 snake_case（`randomTickSpeed` → `random_tick_speed`）；
		而「冻结昼夜」那条**不叫** `do_daylight_cycle`（本项目试过，命令解析失败）。
	- ⚠️ 种在**不合法基底**（如玻璃）上的树苗会被移除（`canSurvive`），已一并实测。

### 骨粉
- 机制同原版：骨粉对树苗使用即视为成功生长（`performBonemeal` → 走同一条生长路径）
- `isValidBonemealTarget` 会先按高度判断「上方放不放得下这棵树」——高度取自上面那份配置特征的树干参数

### 掉落
- 被破坏后掉落自身（战利品表暂缓，见「获取方式」）

## 方块状态
照抄原版树苗，共 **2** 个状态（与原版橡树树苗完全一致）
- STAGE（0 / 1）
	- 机制与原版相同：随机刻长大前先推进一档；0 → 1 只做「准备」，1 才真正长树

## 获取方式
### 战利品表
#### 采集
- 挖掘通灵树苗的合适工具：原版树苗不带挖掘工具标签，空手即可瞬间破坏。
- 通灵树苗被破坏后会掉落自身。
- 现状：战利品表与标签按全项目既定决定**暂缓**，「获取方式」阶段统一落地。

## 备注
- ✅ 已实现（2026-10）：方块状态照抄原版、种植与生长走原版 `SaplingBlock` 那条路、
	长出来的树是白桦形状的通灵树。实现位置
	`src/main/java/com/rcdmake/whimsy/block/ClassRandomThings/RDT_TreeSaplingSpectre.java`
- **⚠️ 水会冲掉树苗，这是刻意保留的原版行为**：原版树苗的属性带「无碰撞」，因此水流判定会放行、把树苗冲掉。
	wiki 的构造表逐行都是「父级」，所以不修。
	- 若以后要让它防水，按项目范式的硬约束加一条 `forceSolidOn()`（**不能**只写 `noCollision()` ——
		「空碰撞 + 想挡水」必须是 `noCollision()` **+** `forceSolidOn()` 这一对）
	- 对照：通灵原木需要那一条（它不可含水）；通灵树叶不需要（它可含水，水只会把它变成含水树叶）
- **⚠️ 树叶长出来会不会立刻枯萎，取决于通灵原木在不在 `#minecraft:logs` 里**：
	26.2 的树叶距离场读的是 `#minecraft:prevents_nearby_leaf_decay`（原版定义为 `#minecraft:logs`），
	本模组已补 `data/minecraft/tags/block/logs.json`。详见 [通灵树叶](RDT_TreeLeavesSpectre.md)
- **引燃 / 烧毁为什么要自己登记**：`ofFullCopy` 复制的是方块属性，火焰那张表是另一套注册表
	（Fabric 的 `FlammableBlockRegistry`）。值为 wiki 明文的 60 / 100。
- **⚠️ 物品形式（手持 2D 精灵）走的是「生成器例外表」，不是手写资源**：见上面「渲染」一条。
	`items/rdt_spectre_sapling.json` 由 `ItemModelGenerator` 生成（本次 datagen 日志：
	「物品模型定义生成完毕：写入 1 个」，改的就是这一份），
	手改它会在下次 datagen 被抹回。
- 生长器的**形参顺序已逐字节核对**（无 javap 时最容易写反的地方）：
	`TreeGrower(String 名字, float 次选概率, Optional 巨树, Optional 次选巨树, Optional 树, Optional 次选树, Optional 花, Optional 次选花)`。
	本项目只填「树」和「次选树」两项。
- 地图颜色仍带星号（「待定，该方块为淡蓝色」）—— **未实现**，沿用父级。
- 尚未加入本页、将来可能需要的：树苗与树叶的挖掘工具标签、战利品表（「获取方式」阶段）、
	`#minecraft:leaves` 等兼容性标签（非功能必需）。