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
   - 「概念性固体」（`legacySolid`）由 `calculateSolid()` 读**缓存下来的碰撞形状**反推 —— 凡按实体区分碰撞的方块，**非实体上下文的兜底分支必须返回父级形状**，返回空会让 `blocksMotion()` 变 false、方块被水冲掉、告示牌也贴不上；
   - 「恒为空碰撞」写法必须是 `noCollision()` **+** `forceSolidOn()`。
5. **`skipRendering` 要真正生效，模型那一面必须有 `cullface`**（手写 `elements` 时最容易漏）。
6. **Mixin 三处登记**：Mixin 类 + `<modid>.mixins.json` 的 `mixins` + `fabric.mod.json` 的 `mixins`；用 `require = 0` 保证不命中不崩；必须游戏内验证。
7. **覆写点必须带 `@Override`**，且注释里写清它对应旧版的哪个 Yarn 方法（迁移对照的落地形式）。

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
