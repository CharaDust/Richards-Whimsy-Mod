# 方块类范式 V1（blockv1）

> - 配套样板类：[`ParadigmBlockV1.java`](../../../src/main/java/com/rcdmake/whimsy/block/paradigm/ParadigmBlockV1.java)
>   （`block/paradigm/` 目录被 `build.gradle` 的 `sourceSets` 排除：**不注册、不参与编译、不进 jar**，只作取用样板）
> - 适用范围：本模组**所有方块类**，各模块一致（DEV / WI / RDT / BHR / LGC）
> - 版本：**V1** —— 有大的修改再商讨 V2

---

## 1. 核心原则

1. **按 Minecraft 运行时真实顺序排列**，不按 Java 惯例。
   属性工厂是构造器的实参，**运行时先执行**，所以工厂写在构造器**之前** —— 读者从上往下读，就等于引擎从上往下跑，顺便理解「配件先调好再组装」。
2. **分层 = 摆在哪里；阶梯 = 用什么手段。** 两者正交：先用最简手段（见 §3），再决定它落在哪一层（见 §2）。
3. **可选项显式标注。** 可选层一律标注「可选」，并在**层 0 的契约栏**里写明本类启用了哪些可选层、为什么。
4. **类注释是这件工艺品的说明书。** 三层信息固定：wiki 规格摘录 / Yarn→26.2 对照表 / 关键机制与踩坑。

### 1.1 一次方块从声明到运行的完整时序

```
层 4  属性工厂()            ← 被调用，Properties 成型
层 5  super(properties)     ← Block 构造器内部调 层 8 的 createBlockStateDefinition，状态模式在此定死
层 5  构造器体              ← 此后才能 registerDefaultState(...)（层 3 的 Property 字段在更上面已初始化）
层 12 OnInit()              ← 注册进 BuiltInRegistries.BLOCK / ITEM；额外注册表登记
运行期                      ← 层 8 生命周期 → 层 9 交互与判定 → 层 10 形状与外观（引擎按需回问）
层 C  客户端接线            ← 独立类，客户端初始化时执行
层 M  Mixin                 ← 类加载期改写原版字节码
```

---

## 2. 分层表（0 → 12，外加 C / M）

| 层 | 名称 | 写什么 | 强制 | 为什么在这个位置（引擎何时用它） |
| --- | --- | --- | --- | --- |
| **0** | 契约栏（类注释） | 中/英文名、注册名、wiki 页、模块、父级；Yarn→26.2 对照表；**启用/跳过的可选层**；登记点 checklist；机制与踩坑 | 强制 | 读者与后续迁移者的入口；对应「前期设计 doc」 |
| **1** | 规格常量 | `private static final` 数值参数（阻力、几率、延迟、tick） | 可选 | 纯内部数值，先于一切逻辑被读到；外部要引用才升为 `public` |
| **2** | 方块声明 | `public static Block UPPER_SNAKE;`（多块多声明；换色变体在此并列） | 强制 | 字段区；实例在层 12 赋值，之前为 `null` |
| **3** | 状态属性字段 | `public static final XxxProperty UPPER_SNAKE;` | 可选（仅多状态） | 静态初始化必须早于任何实例，故放字段区、不能等层 8 |
| **4** | 属性工厂 | `public static BlockBehaviour.Properties xxxProperties()` | 强制 | **运行时最先执行**；只允许依赖静态常量，不得依赖实例字段 |
| **5** | 构造器 | `public X(BlockBehaviour.Properties properties)`；装配 +（多状态时）`registerDefaultState(...)` | 强制 | 紧随工厂（它是实参来源）；`registerDefaultState` 只能在 `super(...)` 之后 |
| **6** | 家族/变体声明 | `extends` 原版家族类（`SlabBlock`/`StairBlock`/`WallBlock`/`RotatedPillarBlock`）+ 家族构造参数 | 可选 | 「我的本体是谁」属于构造期信息；写在构造器后、特殊功能前，原版家族类自带机制与 codec |
| **7** | 类型序列化 codec | `CODEC` 字段 + `codec()` 覆写 | 可选 | 类型级信息，与层 6 同族；**仅当构造参数无法从 `Properties` 推导且父类不提供时**才需要 |
| **8** | 状态模式与生命周期 | `createBlockStateDefinition` / `getStateForPlacement` / `tick` / `setPlacedBy` / `updateShape` / `neighborChanged` | 可选 | 状态维度与放置、计划刻：方块自身的「生老病死」 |
| **9** | 交互与判定 | `useItemOn` / `useWithoutItem` / `attack` / `entityInside` / `fallOn` / `stepOn` / `onProjectileHit`；`canBeReplaced` / `isPathfindable` / `isValidSpawn`… | 可选 | 「别人对我做什么」+「别人问我能不能」；属性工厂能表达的一律回到层 4 |
| **10** | 形状与外观 | `getShape` / `getCollisionShape` / `getOcclusionShape` / `useShapeForLightOcclusion` / `getShadeBrightness` / `propagatesSkylightDown` / `skipRendering` / `getRenderShape` | 可选 | 碰撞箱、遮挡、剔除、AO：只影响「看起来/摸起来是什么形状」 |
| **11** | 未定扩展点（注册前预留区） | 现在无法预测、未来版本才可能出现的钩子，写 `// TODO(预留)` 占位 | 可选 | 摆在**注册之前**：不打断主体顺序，也不参与运行；一旦确定归属就升到对应层 |
| **12** | 初始化 `OnInit()` | `ModRegister.registerBlockWithItem(...)`；额外注册表登记（可燃性等）；通知主类调用 | 强制 | 装配线：把散件装进注册表；「通知其他蓝图」的脚注写在这里或层 0 的 checklist |
| **C** | 客户端接线（独立类） | 颜色提供器、渲染层、着色器 | 可选 | **绝不写进方块类**（专用服务端加载会 `ClassNotFound`）；写在客户端入口，方块类只留指针 |
| **M** | 定制 Mixin（独立类） | 强行改写原版类 | 极少用 | 仅当「声明 → 覆写 → 登记/通知」都做不到时；三处登记 + `require = 0` + wiki 打 `#mixin` + 游戏内验证 |

---

## 3. 能力阶梯：同一个功能，优先用最上层的写法

| 级 | 手段 | 落在哪层 | 本模组实例 |
| --- | --- | --- | --- |
| 0 | **继承**对的父类（白拿原版整套机制，常含 codec） | 层 6 | 通灵原木 `extends RotatedPillarBlock`（AXIS + 放置，不自写） |
| 1 | **声明**：`Properties` builder 已有的开关 | 层 4 | `noCollision` / `friction` / `emissiveRendering` / `pushReaction` / `isValidSpawn(谓词)` / `forceSolidOn` |
| 2 | **覆写**本类方法 | 层 8~10 | 固化水方块：`forceSolidOn` + `getCollisionShape` 兜底解掉水冲问题（**没动 Mixin**） |
| 3 | **登记其他注册表 / 数据驱动制品** | 层 12 / 资源 | 可燃性 `FlammableBlockRegistry`、物品上色 `BlockColorRegistry` + `items/*.json` 的 `tints`、blockstate/模型/lang/物品栏、配方与战利品 |
| 4 | **事件 / 跨类通知** | 层 12 或独立类 | Fabric 事件回调、给实体加属性、给别的方块打标签 |
| 5 | **Mixin** | M | 超级滑滑冰冰零衰减（前四级全无解）；`26.2` 的 `computeModifiedFriction` 把摩擦 clamp 在 1.0，只能在此补 |

**铁律：能用上面一级，就不许用下面一级；停在第 N 级必须在层 0 契约栏写明「为什么没停在第 N-1 级」。** 这句话是本项目三次踩坑（水冲方块、物品上色、滑冰零衰减）换来的判据。

---

## 4. 可选层清单（启用条件一览）

| 可选层 | 什么时候启用 | 关键约束 |
| --- | --- | --- |
| 层 1 常量 | 有魔法数值 | 外部引用才 `public` |
| 层 3 状态属性 | 方块有多状态 | 字段必须在层 4/5 之前；多状态方块的 blockstate JSON 必须**手写**（单状态才交给 datagen） |
| 层 6 家族 | 切割类建筑方块（本体/半砖/楼梯/墙） | 直接继承原版家族类，机制与 codec 白拿 |
| 层 7 codec | 构造参数无法从 `Properties` 推导、且父类不提供时 | 继承 `Block` 的普通方块**不需要**（`Block.codec()` 已实现）；见 §8 TODO |
| 层 8~10 | 有特殊行为时 | 按「引擎问它的时机」排序，不要按灵感排序 |
| 层 11 | 已知未来要留钩子、但当前无法确定归属 | 只占位，不许参与运行 |
| 层 C 客户端 | 需要上色/渲染层/着色器 | 必须写在客户端入口类 |
| 层 M Mixin | 阶梯第 5 级 | 三处登记 + `require = 0` + 游戏内验证 + wiki `#mixin` |

---

## 5. 登记点 checklist（新增方块必须同步的 7 处）

| # | 位置 | 内容 |
| --- | --- | --- |
| 1 | `RichardsWhimsyMod.onInitialize()` | 加一条 `X.OnInit();`（方块 → 物品 → 物品栏的顺序） |
| 2 | `datagen/BlockStateGenerator.collectAll()` | **单状态**方块声明；多状态不声明（手写 JSON） |
| 3 | `datagen/ItemModelGenerator.ITEM_MODEL_OVERRIDES` | 仅当物品模型与方块模型不同（如气凝胶用了更实的 `_item` 贴图、固化水用了 `_mapped` 贴图） |
| 4 | `datagen/Class<模块>/<模块>_LangEntries` | **两条**：`builder.add(block, 名)` + `builder.add(block.asItem(), 名)`（26.2 两个键不互回退） |
| 5 | `item/<模块>_CreativeTabs` | `output.accept(...)` 加入清单 |
| 6 | `assets/.../{blockstates,models,textures}` | 贴图 `textures/block/<模块>/<名>.png`；模型 `models/block/<注册名>.json` |
| 7 | 配方 / 战利品表 / 标签 | 目前全项目暂缓（「获取方式」阶段统一做）；标签含挖掘工具（`mineable/*`、`needs_*`） |

> 层 0 契约栏里写这份 checklist 的**勾选状态**，别指望别人去猜。

---

## 6. 命名与可见性

| 对象 | 规则 | 例 |
| --- | --- | --- |
| 类名 | `模块前缀_名称`（`RDT_`/`WI_`/`DEV_`/`BHR_`/`LGC_`） | `RDT_TreeLogSpectre` |
| 注册名 | `<模块小写>_<名称>` | `rdt_spectre_log` |
| 方块字段 | `public static Block UPPER_SNAKE;`（构造器赋值，故**不加** `final`） | `public static Block RDT_SPECTRE_LOG;` |
| 属性工厂 | `public static BlockBehaviour.Properties <小驼峰>Properties()` | `spectreLogProperties()` |
| 规格常量 | `private static final`（外部要才 `public`） | `INSIDE_DRAG` / `IGNITE_ODDS` / `FRICTION` |
| 状态属性 | `public static final XxxProperty UPPER_SNAKE` | `DEV_BOOLEAN` |
| 初始化 | `public static void OnInit()`（项目沿用名，主类只调这一条） | `OnInit()` |

---

## 7. 硬约束（违反必出问题）

1. **工厂先于构造器**：`Properties` 必须在实例构造前就绪；工厂只能依赖静态常量/Property。
2. **`registerDefaultState` 只能在 `super(...)` 之后**；状态模式（`createBlockStateDefinition`）在 `Block` 构造器内部就已定死。
3. **客户端代码绝不写进方块类**：颜色/渲染层/着色器一律进 `RichardsWhimsyModClient`。
4. **碰撞相关的两条**（本项目花过大量时间）：
	- 对于能使实体透过的方块，如果不希望导致被水冲掉，请遵循以下两条
	   - 「概念性固体」（`legacySolid`）由 `calculateSolid()` 读**缓存下来的碰撞形状**反推 —— 凡按实体区分碰撞的方块，**非实体上下文的兜底分支必须返回父级形状**，返回空会让 `blocksMotion()` 变 false、方块被水冲掉、告示牌也贴不上；
	   - 「恒为空碰撞」写法必须是 `noCollision()` **+** `forceSolidOn()`。
5. **`skipRendering` 要真正生效，模型那一面必须有 `cullface`**（手写 `elements` 时最容易漏）。
6. **Mixin 三处登记**：Mixin 类 + `<modid>.mixins.json` 的 `mixins` + `fabric.mod.json` 的 `mixins`；用 `require = 0` 保证不命中不崩；必须游戏内验证。
7. **覆写点必须带 `@Override`**，且注释里写清它对应旧版的哪个 Yarn 方法（迁移对照的落地形式）。
8. **代码风格两条**（2026-10 由 `BHR_TreeLeavesPine` 的返工写进范式，之前只散落在 §9.1 与口头约定里）：
	- **缩进一律 Tab**：一个缩进层级 = 一个制表符；文件 UTF-8（无 BOM）+ LF。空格缩进会让 `git diff`、
	  IDE 缩进提示与其余方块类全部对不齐 —— 方块类是要互相抄的，格式不一致等于抄错。
	- **层号注释只用单行横幅**：`// ── 层 N ｜ … ──`（写法见 §9.1）。**三行重横幅只允许出现在
	  `block/paradigm/` 的样板类里** —— 样板是教材，需要解释每层在干什么；生产类照抄样板会把 200 行的类
	  撑到 300 行，还会用说明文字盖住「这一层其实是空的」这件事。
9. **同一条规则只有一份实现**（2026-10 由「通灵木料三兄弟」的统一写进范式）：
	当**多个方块共用同一条手感 / 形状 / 判定**时（例：通灵原木、通灵木板、通灵树叶的
	「可穿过 + 顶面可站立 = `RDT_SpectrePassThrough`」），实现必须收进**一个** `final` 工具类
	（放 `block/Class<模块>/` 下），各方块只留**一行转发** + 它的差异参数；**不许**每块抄一份。
	- 单继承决定了这件事：三块的父类各不相同（`RotatedPillarBlock` / `Block` /
	  `UntintedParticleLeavesBlock`），**没法**用一个公共基类收口，工具类 + 静态方法是唯一出路。
	- 差异**必须**是显式参数或显式开关，并在工具类注释里列一张对照表 ——
	  否则下一个人会以为「统一」就是「全都一样」，顺手把树叶的 `forceSolidOn()` 也补上（那是错的）。
	- 工具类的类注释是这条规则的**唯一权威**：把判定式、逐条理由、以及「为什么不能那样写」都写在那边，
	  各方块类只写「我填了哪个参数、为什么是这个值」。

---

## 8. TODO / 待核实（写进范式，不假装已解决）

| # | 事项 | 现状 |
| --- | --- | --- |
| 1 | **`BLOCK_TYPE` 未登记**：`ModRegister` 只登记 `BLOCK` / `ITEM`，没有往方块**类型**注册表登记自定义类型（消费方是 `BlockTypes`） | 实测无任何症状（12 个自定义类 + 启动 + datagen 均正常）；等做「数据包定义方块 / 需要序列化方块类型」时再回来补 |
| 2 | **`BaseEntityBlock` 的 codec**：26.2 是否已提供 `codec()` 实现（旧项目里 BE 方块只能 `getCodec()` 返回 `null` 打桩，见 `javaref/.../DEV_EntityInt.java`） | 待核实；下次做方块实体（`RDT_BlockBreaker` / `DEV_SimpleWorker`）时先查 |
| 3 | **codec 的 helper 名**（`simpleCodec` / `recordCodecBuilder` 等）与本项目自定义类型的配合方式 | 待核实；层 6 走「继承原版家族类」路线时不需要它 |
| 4 | 「获取方式」（战利品表 / 配方 / 挖掘标签）全项目未落地 | 按既定决定暂缓，等统一阶段 |

---

## 9. 样板类怎么用

1. 抄 [`ParadigmBlockV1.java`](../../../src/main/java/com/rcdmake/whimsy/block/paradigm/ParadigmBlockV1.java) → 改类名/模块前缀/注册名/工厂名；
2. 按 §5 勾完 7 处登记点，把勾选状态写进层 0 契约栏；
3. 可选层：只留用到的，其余连注释一起删 —— **别留空壳**；
4. 校验样板本身是否仍与当前 MC 版本 API 一致：临时注释掉 `build.gradle` 里 `sourceSets` 的那行 `exclude`，`gradlew build` 一次即可（样板是能编译的，只是默认不进编译）。

### 9.1 层号注释的写法（本轮统一）

- **缩进用 Tab**、文件 UTF-8 无 BOM + LF（§7 第 8 条把这两条升成硬约束）。
- **生产类**（`block/Class*/` 下的真实方块）：每层开始处一行，形如
  `// ── 层 4 ｜ 属性工厂（运行时先执行）──`，紧贴该层第一个成员的 javadoc 上方；
  只在该类**实际存在**的层上写（跳过的不写）。
- **样板类**（`block/paradigm/`）：用三行重横幅 + 每层说明（它本身就是教材，需要解释每一层在干什么）。
  ⚠️ 样板类 **只此一家**：生产类照抄这套横幅属于 §7 第 8 条的违规。
- 层号取自 §2 的表格；方块状态生成声明（`blockStateModels()`）之类「登记点载体」不属于方块本体，
  用 `// ── 登记点 ② ｜ … ──` 标注，不占层号。

---

## 10. 未来展望（V2 讨论存根）

> 本节是 V2 的入口：V1 定稿时已有 **19 个方块类 / 22 个方块**按 V1 归一（阶段 0~3），
> 下表是它们的现状清单 —— **V2 要动哪一条，先在这里补一行**，再开讨论。

### 10.1 V1 落地的类（19 类 / 22 方块）

| 模块 | 类 | 方块数 | 用到的可选层 | 阶梯最高级 | V2 可议之处 |
| --- | --- | --- | --- | --- | --- |
| WI | [WI_Aerogel](../../../src/main/java/com/rcdmake/whimsy/block/ClassWhimsyIdea/WI_Aerogel.java) | 1 | 1, 9, 10 | 2 | 减速乘数 0.9 是自定值（wiki 只说「稍有阻力」，没给数） |
| WI | [WI_SugarBlock](../../../src/main/java/com/rcdmake/whimsy/block/ClassWhimsyIdea/WI_SugarBlock.java) | 1 | 9 | 2 | 「空手 / 手持物品」两条路径都要覆写（1.21.2 拆分的后果），是否有更省写法 |
| WI | [WI_StaticLiquidWater](../../../src/main/java/com/rcdmake/whimsy/block/ClassWhimsyIdea/WI_StaticLiquidWater.java) | 1 | 10, **C** | 3 | 硬度/爆炸抗性 100 是 wiki 的占位值（待定）；模型手写需 `cullface` |
| WI | [WI_StaticCobblestone](../../../src/main/java/com/rcdmake/whimsy/block/ClassWhimsyIdea/WI_StaticCobblestone.java) | 1 | — | 0/1 | 模板遗留方块，一行覆写都没有（这是它的价值） |
| WI | [WI_PackedPlanks](../../../src/main/java/com/rcdmake/whimsy/block/ClassWhimsyIdea/WI_PackedPlanks.java) | 1 | — | 0/1 | 同上；它是 WI 方块页的图标 |
| WI | [WI_GlassSugar](../../../src/main/java/com/rcdmake/whimsy/block/ClassWhimsyIdea/WI_GlassSugar.java) | 1 | 9, 10 | 2 | `fallOn` 里刻意调 `super.stepOn`（踩碎不吃摔落伤害）—— 与直觉相反的写法，值得再确认 |
| RDT | [RDT_SuperLubricentIce](../../../src/main/java/com/rcdmake/whimsy/block/ClassRandomThings/RDT_SuperLubricentIce.java) | 1 | 1, **M** | **5** | 全项目唯一用到 Mixin；`require = 0`，需游戏内确认命中 |
| RDT | [RDT_GlassLapis](../../../src/main/java/com/rcdmake/whimsy/block/ClassRandomThings/RDT_GlassLapis.java) | 1 | 9, 10 | 2 | 兜底必须回落父级形状（硬约束）；`forceSolidOn` 是否冗余（有它更稳） |
| RDT | [RDT_GlassQuartz](../../../src/main/java/com/rcdmake/whimsy/block/ClassRandomThings/RDT_GlassQuartz.java) | 1 | 9, 10 | 2 | 兜底是完整箱，故没有 `forceSolidOn` —— 两块玻璃的差异点 |
| RDT | [RDT_StepBlock](../../../src/main/java/com/rcdmake/whimsy/block/ClassRandomThings/RDT_StepBlock.java) | 1 | 1, 8, 10 | 2 | 硬度按 wiki「默认」而不设 `strength`（旧代码是 0.5） |
| RDT | [RDT_TreeLogSpectre](../../../src/main/java/com/rcdmake/whimsy/block/ClassRandomThings/RDT_TreeLogSpectre.java) | 1 | 1, 3, **6**, 8, 9, 10, **12** | 3 | 家族继承 + 可燃性登记都在这里；地图颜色仍待定 |
| RDT | [RDT_LuminousBlocks](../../../src/main/java/com/rcdmake/whimsy/block/ClassRandomThings/RDT_LuminousBlocks.java) | **3** | 4 | 1 | 一类多方块；纯色渲染靠「属性 + 模型」两侧配合 |
| DEV | [DEV_BlocksOther](../../../src/main/java/com/rcdmake/whimsy/block/ClassDEV/DEV_BlocksOther.java) | **2** | — | 0/1 | 一类两方块，属性相同却刻意各留一条工厂 |
| DEV | [DEV_GhostBlock](../../../src/main/java/com/rcdmake/whimsy/block/ClassDEV/DEV_GhostBlock.java) | 1 | 9, 10 | 2 | 「已排除的走法」表是踩坑记录，别删；`wasValidSpawn` 那条行为无法保留 |
| DEV | [DEV_StateBoolean](../../../src/main/java/com/rcdmake/whimsy/block/ClassDEV/DEV_StateBoolean.java) | 1 | 3, 8 | 2 | 手写 blockstate；状态样例 |
| DEV | [DEV_StateInt](../../../src/main/java/com/rcdmake/whimsy/block/ClassDEV/DEV_StateInt.java) | 1 | 3, 8 | 2 | 同上（10 种取值） |
| DEV | [DEV_StateEnum](../../../src/main/java/com/rcdmake/whimsy/block/ClassDEV/DEV_StateEnum.java) | 1 | 3, 8 | 2 | 嵌套枚举的序列化名不能改（写进 blockstate variants） |
| DEV | [DEV_StateEnumAxis](../../../src/main/java/com/rcdmake/whimsy/block/ClassDEV/DEV_StateEnumAxis.java) | 1 | 3, 8 | 2 | **刻意不继承** `RotatedPillarBlock`（要让学生看见状态定义与放置逻辑） |
| DEV | [DEV_StateEnumFacing](../../../src/main/java/com/rcdmake/whimsy/block/ClassDEV/DEV_StateEnumFacing.java) | 1 | 3, 8 | 2 | 没有 `getStateForPlacement`，默认恒为 NORTH（与旧代码一致）；2026-10 由 `DEV_StateEnumFace` 改名（朝向叫 Facing，附着面才叫 Face） |

### 10.1.1 V1 之后新增的类（2026-10，通灵树一条线）

> V1 的 19 类清单之外后补的，按新规矩（§7 第 9 条）又多了个「共享实现类」。列在这里是为了让这张表继续是**现状**。

| 模块 | 类 | 方块数 | 用到的可选层 | 阶梯最高级 | 备注 |
| --- | --- | --- | --- | --- | --- |
| RDT | [RDT_TreeLeavesSpectre](../../../src/main/java/com/rcdmake/whimsy/block/ClassRandomThings/RDT_TreeLeavesSpectre.java) | 1 | 1, 9, 10, 12 | 3 | 继承 `UntintedParticleLeavesBlock`（原版 `LeavesBlock` 在 26.2 是抽象类）；枯萎全继承 |
| RDT | [RDT_TreeSaplingSpectre](../../../src/main/java/com/rcdmake/whimsy/block/ClassRandomThings/RDT_TreeSaplingSpectre.java) | 1 | 1, 12 | 3 | **零业务覆写**；树型走配置特征 JSON（照抄白桦、换材质） |
| RDT | [RDT_TreePlanksSpectre](../../../src/main/java/com/rcdmake/whimsy/block/ClassRandomThings/RDT_TreePlanksSpectre.java) | 1 | 1, 9, 10, 12 | 3 | 单状态 → blockstate 交给 datagen（与另三块的「多状态手写」相反） |
| RDT | [RDT_SpectrePassThrough](../../../src/main/java/com/rcdmake/whimsy/block/ClassRandomThings/RDT_SpectrePassThrough.java) | —（工具类） | — | — | **不是方块**：§7 第 9 条那个「同一条规则只有一份实现」的载体；三块通灵木料的碰撞 / 减速都转发到它 |
| RDT | [RDT_SpectreLens](../../../src/main/java/com/rcdmake/whimsy/block/ClassRandomThings/RDT_SpectreLens.java) | 1 | 1, 8, 10, 12 | **4** | 通灵镜片（`rdt_spectre_lens`）：方块本体只到第 2 级；「每 80 tick 校验 + 全维度施放」在共享类 [RDT_SpectreLensIndex](../../../src/main/java/com/rcdmake/whimsy/block/ClassRandomThings/RDT_SpectreLensIndex.java) 里，所以整体到第 4 级。**没有方块实体、没有 Mixin** |

### 10.2 V2 待议清单

1. **层号注释**：本轮已在 19 个类里统一为单行 `// ── 层 N ｜ … ──`（约定见 §9.1）；V2 可议是否升级为必做/是否要把「跳过的层」也写出来。
2. **层 3 属性字段**：是否强制「每个状态维度都声明成本类的 `public static final` 字段」？DEV 的 `AXIS` / `FACING` 目前是原版常量的**别名**（同实例），有人会认为是多余的一层。
3. **层 11 预留区**：目前 19 个类**没有一个**用到 —— 保留（等真出现不可预测钩子时）还是删掉这一层？
4. **层 7 codec**：一旦开工切割家族（本体/半砖/楼梯/墙），是否把「家族类必看 codec」写成硬规则？（继承原版家族类可白拿，见 §8 TODO 表）
5. **登记点清单的落地**：现在 8 项（含 Mixin 三处）只写在各类的层 0 契约栏里，是否要抽一份公共清单文件 + 一个「新方块 checklist」模板？
6. **阶梯第 3~5 级的判定权**：谁来判定「前四级真的都无解」？目前只有超级滑滑冰用了第 5 级，判据写在它的类注释里 —— 是否需要一条「上 Mixin 前必须在契约栏留推导」的硬规则（现在事实上是这么做的）。
7. **层 C 客户端接线**：目前散在 `RichardsWhimsyModClient`，是否要一张「客户端接线登记表」（方块 ↔ 颜色/渲染层/着色器）。
8. **集合类的边界**：`DEV_BlocksOther`（2 个无关方块）与 `RDT_LuminousBlocks`（3 个换色）现在都用「一类多方块」；V2 要不要把「什么情况允许合并」写成更死的条文。
9. **获取方式整体阶段**：战利品表 / 配方 / 挖掘标签（登记点⑦）全项目未落地 —— 落地时要不要给范式加一层。

### 10.3 V2 的触发条件

按 §7 硬约束与 §8 TODO 的性质，建议 V2 只在下面任意一条成立时开：
切割家族真的开工 / 出现第二个必须用 Mixin 的方块 / `BLOCK_TYPE` 与 `BaseEntityBlock` codec 两个待核实项被证实有影响 / 「获取方式」阶段启动。

