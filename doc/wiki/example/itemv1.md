# 物品类范式 V1（itemv1）

> - 配套样板类：[`ParadigmItemV1.java`](../../../src/main/java/com/rcdmake/whimsy/item/paradigm/ParadigmItemV1.java)
>   （`item/paradigm/` 目录被 `build.gradle` 的 `sourceSets` 排除：**不注册、不参与编译、不进 jar**，只作取用样板）
> - 适用范围：本模组**所有物品类**，各模块一致（DEV / WI / RDT / BHR / LGC）
>   - **不含方块物品**：方块物品由方块类自己注册（`ModRegister.registerBlockWithItem`），那份规矩归 [`blockv1.md`](./blockv1.md)
> - 依据：`blockv1.md` 的分层 / 阶梯骨架 + 本项目现有 20 个物品类与支撑类的写法 + Fabric 26.2（mojmap，无混淆）实测 API
> - 版本：**V1** —— 有大的修改再商讨 V2

---

## 1. 核心原则

1. **按 Minecraft 运行时真实顺序排列**，不按 Java 惯例。
   方块的「配件」是方块状态，物品的「配件」是**数据组件**：组件值先成型（层 3）→ 挂到 `Item.Properties`（层 4）→ 进构造器（层 5）→ `OnInit()` 里才注册（层 12）。
   读者从上往下读，就等于引擎从上往下跑。
2. **分层 = 摆在哪里；阶梯 = 用什么手段。** 两者正交：先用最简手段（见 §3），再决定它落在哪一层（见 §2）。
3. **层号与方块范式保持一致（0~12 + C / M），但物品「没有的层」要明说没有**，不许换个用法顶替。
   典型：**层 7（类型 codec）在物品范式里不存在** —— `Item` 只有一个静态 `CODEC`（注册表引用 + 组件），没有可覆写的 `codec()`（见 §7 第 3 条）。
4. **可选项显式标注。** 可选层一律标注「可选」，并在**层 0 的契约栏**里写明本类启用了哪些、为什么；停在第 N 级要写明「为什么没停在第 N-1 级」。
5. **类注释是这件物品的说明书。** 三层信息固定：wiki 规格摘录 / Yarn→26.2 对照表 / 关键机制与踩坑。

### 1.1 一件物品从声明到运行的完整时序

```
层 3  组件值工厂()        ← FoodProperties / Consumable / PotionContents / MobEffectInstance 这些「值对象」先成型
层 4  装配方法(p)          ← 注册入口先建 new Item.Properties().setId(key)（见层 12），再把 p 交给本类
      p.component(...)     ← 组件、堆叠数、稀有度、返还物……在这一行行挂上去
层 5  super(properties)   ← Item 构造器把「默认组件 + 本类组件」定成该物品的组件表；此后 new ItemStack(item) 都从这里取默认值
层 12 OnInit()            ← Registry.register(BuiltInRegistries.ITEM, key, item)（由 ModRegister 代劳）
运行期                     ← 层 8 生命周期 → 层 9 交互与判定 → 层 10 外观与提示（引擎按需回问）
层 C  客户端接线           ← 两段式模型定义 + tints（数据驱动，多数情况**不需要代码**）/ 自定义渲染
层 M  Mixin               ← 类加载期改写原版字节码
```

> ⚠️ 与方块的最大区别：**方块的属性工厂是「自己造一个 `Properties`」，物品的属性是「往注册入口造好的 `Properties` 上挂东西」。**
> 这不是风格差异 —— 物品的 key 必须在注册时写进 `Properties`（漏写直接 `NullPointerException: Item id not set`），所以 `Properties` 的所有权在 `ModRegister` 那边。

### 1.2 方块范式 → 物品范式：层号对照

| 层 | 方块（blockv1） | 物品（本范式） | 差异要点 |
| --- | --- | --- | --- |
| 0 | 契约栏 | 契约栏 | 同构；物品的「父级」= 继承的原版物品类，或「无（普通 `Item`）」 |
| 1 | 规格常量 | 规格常量 | 同构（物品多一类：组件数值、提示语言键） |
| 2 | 方块声明 | 物品声明 | 同构；一类多件时改记 `List<Item>` / `List<Entry>`（§6） |
| 3 | 方块状态属性（`Property` 字段） | **数据组件与组件值工厂** | 方块状态是「几个枚举/int 维度」，物品组件是「任意带 codec 的值」；组件只在 `Item.Properties` 上挂 |
| 4 | 属性工厂 `xxxProperties()` | **装配方法 `xxxProperties(Item.Properties)`** | 方块是造 `Properties`，物品是改注册入口造好的 `Properties` |
| 5 | 构造器 | 构造器 | 同构（都只做装配） |
| 6 | 家族：`extends SlabBlock` 等 | 家族：`extends BlockItem / SplashPotionItem / AxeItem` 等 | 同构；**26.2 的 `SwordItem` / `PickaxeItem` 已删**，武器工具走 `Properties.sword/pickaxe` |
| 7 | codec（构造参数无法推导时要） | **不存在** | `Item` 无 `codec()` 可覆写；物品靠注册名 + 组件序列化 |
| 8 | 状态模式与生命周期 | 生命周期 | 物品没有 `createBlockStateDefinition`；对应物是 `inventoryTick` / `onCraftedBy` 等 |
| 9 | 交互与判定 | 交互与判定 | 同构，方法名不同（`use` / `useOn` / `finishUsingItem` / `hurtEnemy` …） |
| 10 | 形状与外观 | 外观与提示 | 方块管碰撞箱与渲染形状，物品管**名字 / 提示 / 光效 / 模型定义** |
| 11 | 未定扩展点 | 未定扩展点 | 同构 |
| 12 | 初始化 `OnInit()` | 初始化 `OnInit()` | 方块走 `registerBlockWithItem`，物品走 `registerItem` |
| C | 客户端接线（颜色/渲染层） | 客户端接线（自定义渲染） | 物品的着色是**数据**（`items/<id>.json` 的 `tints`），不上层 C |
| M | Mixin | Mixin | 同构（三处登记 + `require = 0`） |

### 1.3 26.2 速查（本范式依据的钉选线与硬事实）

| 项 | 26.2 事实 | 出处 |
| --- | --- | --- |
| 钉选线 | MC `26.2` / loader `0.19.5` / loom `1.18.2` / Fabric API `0.161.0+26.2` / Java 25 | `gradle.properties` |
| 映射 | mojmap，**无 yarn、无 mappings 行**（26.1 起不再混淆） | `build.gradle` / skill 参考 |
| 设置类名 | `Item.Properties`（Yarn 的 `Item.Settings` 不存在） | 26.2 jar |
| 注册 | 只在 `onInitialize()` 里；**`setId` 必写**；统一走 `ModRegister.registerItem` | `ModRegister` 类注释 + 官方文档 |
| 食物 | `food` + `consumable` **两个组件**；`Properties.food(FoodProperties, Consumable)` 是便捷重载 | `Item$Properties` / `DataComponents` |
| 渲染 | **两段式**：`assets/<ns>/items/<id>.json`（定义）+ `models/item/<id>.json`（模型）；缺定义 = MissingItemModel（紫黑） | `ItemModelGenerator` 类注释 |
| 着色 | 数据驱动 `tints` + `minecraft:constant`（`ItemTintSources` 注册的类型）；**没有 `ItemColor` / `ItemColorRegistry`** | 26.2 jar + `ItemModelGenerator` |
| 工具 / 武器 | `SwordItem`、`PickaxeItem` **已不存在**；改 `Properties.sword(...)` / `pickaxe(...)` + `DataComponents.TOOL/WEAPON` | 26.2 jar 类清单 |

---

## 2. 分层表（0 → 12，外加 C / M）

| 层 | 名称 | 写什么 | 强制 | 为什么在这个位置（引擎何时用它） |
| --- | --- | --- | --- | --- |
| **0** | 契约栏（类注释） | 中/英文名、注册名、wiki 页、模块、**父级**（继承的原版物品类，或「无（普通 `Item`）」）；Yarn→26.2 对照表；**启用/跳过的可选层**；登记点 checklist；机制与踩坑 | 强制 | 读者与后续迁移者的入口；对应「前期设计 doc」 |
| **1** | 规格常量 | `private static final` 数值（组件数值、时长 tick、几率、提示语言键） | 可选 | 组件工厂与装配方法会读到；外部要引用（datagen / 物品栏 / 语言）才升为 `public` |
| **2** | 物品声明 | `public static Item UPPER_SNAKE;`；一类多件用 `public static final List<Item> ALL`（或带形态/等级的 `List<Entry>`） | 强制 | 字段区；实例在层 12 赋值，之前为 `null` |
| **3** | 组件与组件值工厂 | `public static FoodProperties xxxFood()` / `Consumable xxxConsumable()` / `PotionContents xxxContents()` —— **返回已经 build 好的值对象** | 可选 | 值对象必须先成型，层 4 才有东西可挂 |
| **4** | 属性装配 | `public static Item.Properties xxxProperties(Item.Properties properties)`：`component(...)` / `stacksTo(...)` / `usingConvertsTo(...)` / `rarity(...)`… | 强制 | 注册入口造好带 id 的 `Properties` 后立刻调它；**只允许依赖静态常量与层 3 的工厂** |
| **5** | 构造器 | `public X(Item.Properties properties) { super(properties); }` | 强制 | 紧随装配；构造器体内不放数值（它们全在组件里） |
| **6** | 家族/变体声明 | `extends BlockItem` / `SplashPotionItem` / `LingeringPotionItem` / `AxeItem` / `SpawnEggItem` … + 家族构造参数 | 可选 | 「我的本体是谁」属于构造期信息；原版家族类自带机制与提示 |
| **7** | 类型序列化 codec | **物品没有这一层**（见 §1.2、§7 第 3 条）；`Item.CODEC` 是静态字段，不是可覆写方法 | — | 物品按「注册名 + 组件」序列化，不需要按类写 codec |
| **8** | 生命周期 | `inventoryTick(ItemStack, ServerLevel, Entity, EquipmentSlot)` / `onCraftedBy` / `onCraftedPostProcess` / `onDestroyed` | 可选 | 「这件物品在背包里/被合成/被烧掉时」—— 与交互无关的自身事件 |
| **9** | 交互与判定 | `use` / `useOn` / `finishUsingItem` / `onUseTick` / `releaseUsing` / `interactLivingEntity` / `hurtEnemy` / `postHurtEnemy` / `mineBlock` / `getDestroySpeed` / `isCorrectToolForDrops` / `canDestroyBlock`；`overrideStackedOnOther` / `overrideOtherStackedOnMe` / `isBarVisible`+`getBarWidth`+`getBarColor` / `canFitInsideContainerItems` | 可选 | 「我拿在手上能对世界做什么」+「别人问我能不能」；**消费类行为优先交给组件**（层 3），别在这里抄一遍 |
| **10** | 外观与提示 | `getName` / `appendHoverText` / `isFoil` / `getUseAnimation` / `getUseDuration` / `getTooltipImage` | 可选 | 名字、悬浮提示、光效；**模型与着色属于资源与数据，不在本层** |
| **11** | 未定扩展点（注册前预留区） | 现在无法预测、未来版本才可能出现的钩子，写 `// TODO(预留)` 占位 | 可选 | 摆在**注册之前**：不打断主体顺序，也不参与运行 |
| **12** | 初始化 `OnInit()` | `ModRegister.registerItem(name, properties -> new X(xxxProperties(properties)))`；一类多件在这里循环；额外注册表登记（效果、燃料等）写在同族的服务类里 | 强制 | 装配线：把散件装进注册表 |
| **C** | 客户端接线（独立类） | 自定义物品渲染（特殊模型 / GUI）；**tints 不用写代码**（`items/<id>.json` 里写） | 可选 | 绝不写进物品类（专用服务端加载会 `ClassNotFound`） |
| **M** | 定制 Mixin（独立类） | 强行改写原版类 | 极少用 | 仅当「继承 → 声明 → 覆写 → 登记/通知」全做不到时；三处登记 + `require = 0` + wiki 打 `#mixin` + 游戏内验证 |

### 2.1 组件速查（层 3 / 层 4 常用项）

> 全部为 26.2 mojmap 名，逐条对 `minecraft-merged-deobf-26.2.jar` 核过。

| 组件 | 值类型 | 本项目实例 | 备注 |
| --- | --- | --- | --- |
| `minecraft:food` | `FoodProperties(nutrition, saturation, canAlwaysEat)` | BHR 的 10 件食物 | 只管**数值**；缺 `consumable` 就没有进食动作 |
| `minecraft:consumable` | `Consumable(consumeSeconds, animation, sound, hasConsumeParticles, onConsumeEffects)` | 同上 | 只管**怎么吃、吃完发生什么**；`Consumables.defaultFood()` / `defaultDrink()` 起手最省 |
| `minecraft:potion_contents` | `PotionContents(basePotion, customColor, customEffects, customName)` | RDT / WI 的 18 件药水 | 是 `TooltipProvider`，效果与时长**自动**进提示；也是投掷药水生效时读的那份数据 |
| `minecraft:max_stack_size` | `Integer` | 药水 `stacksTo(1)` | 默认 64；**只有不是 64 才要写** |
| `minecraft:use_remainder` | `ItemStackTemplate` | 药水 `usingConvertsTo(Items.GLASS_BOTTLE)` | 便捷入口 `Properties.usingConvertsTo(Item)` |
| `minecraft:rarity` | `Rarity` | 暂无 | 便捷入口 `Properties.rarity(...)` |
| `minecraft:lore` | `ItemLore` | 暂无（药水 lore 未落地） | 也可用层 10 `appendHoverText` 动态出 |
| `minecraft:tool` / `weapon` / `attack_range` | `Tool` / `Weapon` / `AttackRange` | **零落地** | 26.2 的工具/武器声明式写法的产物，见 §8 |

---

## 3. 能力阶梯：同一个功能，优先用最上层的写法

| 级 | 手段 | 落在哪层 | 本模组实例 |
| --- | --- | --- | --- |
| 0 | **继承**对的父类（白拿原版整套机制） | 层 6 | 喷溅/滞留药水 `extends SplashPotionItem` / `LingeringPotionItem`（投掷与范围施加全白拿）；方块物品由 `registerBlockWithItem` 统一建 `BlockItem` |
| 1 | **声明**：`Item.Properties` builder 已有的开关 + 组件 | 层 4 | 10 件食物 = `component(FOOD)` + `component(CONSUMABLE)`；18 件药水 = `stacksTo(1)` + `component(POTION_CONTENTS)` + `usingConvertsTo(GLASS_BOTTLE)` |
| 2 | **覆写**本类方法 | 层 8~10 | 松果 `useOn`（种树）；极限模式切换器 `use` + `useOn`；喷溅/滞留药水 `getName`（绕开 `effect.empty` 裸键） |
| 3 | **登记其他注册表 / 数据驱动制品** | 层 12 / 资源 | 药水效果先 `registerStatusEffect`（`Holder<MobEffect>`）；`items/<id>.json` 的 `tints`、双层药水模型、datagen 产出；配方 / 战利品（暂缓） |
| 4 | **事件 / 跨类通知** | 层 12 或独立类 | **暂无实例**（本模组物品目前没有需要 Fabric 事件或跨类通知的）；药水族的「注册 + 互斥 + 形态」收在共享类 `WhimsyPotionRegistration`，那是第 3 级「一份实现」，不是事件 |
| 5 | **Mixin** | M | 极限模式切换器（hardcore 只有读入口，没有写入口 → `DEV_HardcoreModeAccess` + `DEV_HardcoreModeMixin`） |

**铁律：能用上面一级，就不许用下面一级；停在第 N 级必须在层 0 契约栏写明「为什么没停在第 N-1 级」。**

> 反例备忘（真实踩坑换来的）：
> - 「喝 A 顶掉 B」先试了自定义 `ConsumeEffect`（第 2 级）→ 被组件序列化丢掉；又试了覆写 `finishUsingItem` 手写移除（第 2 级）→ 没生效；
>   正解是**第 1 级**：用原版 `RemoveStatusEffectsConsumeEffect` + `ApplyStatusEffectsConsumeEffect`，在注册时按顺序排进 `consumable`（见 `WhimsyPotionRegistration` 类注释）。

---

## 4. 可选层清单（启用条件一览）

| 可选层 | 什么时候启用 | 关键约束 |
| --- | --- | --- |
| 层 1 常量 | 有魔法数值（组件数值 / 时长 / 几率 / 语言键） | 外部引用才 `public` |
| 层 3 组件 | 物品有「数据」—— 食物、药水、稀有度、返还物、自定义数据 | 值对象必须是**已 build 的不可变值**；组件工厂只依赖层 1 常量 |
| 层 4 装配方法 | **几乎总是**（哪怕只有一个组件） | 与方块范式同构、且「改组件只改一处」；一类多件时还能复用 |
| 层 6 家族 | 需要原版类自带的行为（放置、投掷、去皮、刷怪蛋…） | 直接继承原版类；**别为了命名好看去继承** |
| 层 8 生命周期 | 背包内 tick、合成后处理、烧毁回调 | 服务端专用上下文（`inventoryTick` 给的是 `ServerLevel`） |
| 层 9 交互 | 右键世界 / 右键生物 / 攻击 / 挖掘 / 自定义堆叠操作 | 消费类行为先问「能不能用组件表达」（第 1 级） |
| 层 10 外观与提示 | 名字特殊、提示要动态、光效、使用动画 | 模型与 tints **不在这里**，在资源里 |
| 层 11 预留 | 已知未来要留钩子、但当前无法确定归属 | 只占位，不许参与运行 |
| 层 C 客户端 | 自定义物品渲染 | 必须写在客户端入口类 |
| 层 M Mixin | 阶梯第 5 级 | 三处登记 + `require = 0` + 游戏内验证 + wiki `#mixin` |

---

## 5. 登记点 checklist（新增物品必须同步的 7 处）

| # | 位置 | 内容 |
| --- | --- | --- |
| 1 | `RichardsWhimsyMod.onInitialize()` 的「物品」区 | 加一条 `X.OnInit();`；**顺序**：普通物品 → 状态效果 → 药水 → 物品栏（物品栏最后，它直接引用静态字段；药水必须排在自己的效果之后） |
| 2 | `datagen/ItemModelGenerator` | **普通物品不用登记**（自动扫 `models/item/<id>.json`）；只有下面三种情况才动它：常量着色 → `ITEM_TINT_PREFIXES`；自绘瓶身 → `POTION_BOTTLE_OVERRIDES`；物品模型与方块模型不同 → `ITEM_MODEL_OVERRIDES` |
| 3 | `assets/<ns>/models/item/<id>.json` | **手写**（`parent: item/generated` + `layer0`）；不写的话生成器退回 `block/<id>` → 缺失模型（紫黑） |
| 4 | `datagen/Class<模块>/<模块>_LangEntries` | **一条**：`builder.add(item, 名)`（物品只有 `item.…` 一条键，没有方块那条） |
| 5 | `item/<模块>_CreativeTabs` | `output.accept(...)` 加入清单，**顺序 = wiki 物品栏页「包含物品」的顺序** |
| 6 | 资源 | 贴图 `textures/item/<模块>/<名>.png`；`items/<id>.json` 由 datagen 生成（**不要手写**）；跑 `runDatagen` → `syncDatagen` |
| 7 | 配方 / 战利品表 / 物品标签 | 目前全项目暂缓（「获取方式」阶段统一做） |

> 层 0 契约栏里写这份 checklist 的**勾选状态**，别指望别人去猜。
> 「一类多件」的类（药水族）把第 1/4/5 条写成「清单驱动」：从 `ALL` / `ENTRIES` 遍历，加一件只改一行数据。

### 5.1 物品 vs 方块物品：两条路，别走串

| | 独立物品（本范式） | 方块物品 |
| --- | --- | --- |
| 代码在哪 | `item/Class<模块>/Xxx.java`，注册名 `<模块>_<名>` | 方块类自己注册，`ModRegister.registerBlockWithItem(...)`，**不建物品类** |
| 注册入口 | `ModRegister.registerItem(name, factory)` | `registerBlockWithItem(name, block)`（内部建 `BlockItem`） |
| 语言键 | 一条 `item.<ns>.<id>` | **两条**：`block.…` + `item.…`，统一走 `LangEntrySupport.addBlockAndItem` |
| 模型定义 | `items/<id>.json` → `item/<id>` | `items/<id>.json` → `block/<id>`（例外进 `ITEM_MODEL_OVERRIDES`） |
| 归属范式 | 本文件 | [`blockv1.md`](./blockv1.md) 层 12 |

---

## 6. 命名与可见性

| 对象 | 规则 | 例 |
| --- | --- | --- |
| 类名 | `模块前缀_名称`（`RDT_`/`WI_`/`DEV_`/`BHR_`/`LGC_`）；**食物不再用 `Food` 前缀** | `BHR_Lime`（旧名 `BHR_FoodLime`） |
| 注册名 | `<模块小写>_<名称>` | `bhr_lime` |
| 物品字段 | `public static Item UPPER_SNAKE;`（构造器赋值，故**不加** `final`） | `public static Item BHR_LIME;` |
| 一类多件 | `public static final List<Item> ALL` 或 `List<XxxEntry> ENTRIES`（记录形态/等级） | `RDT_FireHormonePotion.ALL` / `WI_PlaguePotion.ENTRIES` |
| 装配方法 | `public static Item.Properties <小驼峰>Properties(Item.Properties properties)` | `limeProperties(properties)` |
| 组件工厂 | `public static <类型> <小驼峰>Food()` / `<小驼峰>Consumable()` / `<小驼峰>Contents()` | `limeFood()` / `limeConsumable()` |
| 规格常量 | `private static final`（datagen / 物品栏 / 语言要读才 `public`） | `NUTRITION` / `CONSUME_SECONDS` |
| 初始化 | `public static void OnInit()`（主类只调这一条） | `OnInit()` |
| 药水 id | 必须含形态片段（`_potion` / `_splash_potion` / `_lingering_potion`）+ 等级后缀 | `rdt_fire_hormone_potion_1`、`wi_plague_splash_potion_3` |

> ⚠️ **药水的 id 是契约，不是随手起的名**：`ItemModelGenerator` 按 id 片段判定形态（先 `_splash_potion`、再 `_lingering_potion`、最后 `_potion` 兜底），命中就生成双层药水模型。
> id 里带 `_potion` 却没按这个规则命名 → 瓶身/液面画错层或整只变成单层。

---

## 7. 硬约束（违反必出问题）

1. **`setId` 只能由注册入口写，物品不能在静态字段里构造。**
   `ModRegister.registerItem` 内部 `new Item.Properties().setId(key)` 再交给工厂；绕过它直接 `Registry.register` 会漏 key → `NullPointerException: Item id not set`。
   而且 `Item` 是惰性的：在静态字段里构造物品会提前触发 `BuiltInRegistries.ITEM` 的 bootstrap —— 所以物品一律在 `OnInit()` 里建，方块才允许静态字段直建（见 `ModRegister` 类注释）。
2. **组件不可变，且会被序列化重建。**
   注册后再改物品字段不影响已建好的组件；`ItemStack.copy()` / 网络同步会按 codec 重建组件 —— **自定义 `ConsumeEffect` 的清单会被丢掉**（实测踩过两次）。
   推论：互斥、施加效果等一律用**原版注册过的** `ConsumeEffect`，在注册时按顺序排进 `consumable`；真要自定义，必须同时注册它的 `Type` 与 codec。
3. **物品没有 codec 层。** `Item` 只有静态 `CODEC` / `STREAM_CODEC` 字段，没有 `codec()` 可覆写；照方块范式抄一层 `@Override codec()` 是编译不过的。
4. **`items/<id>.json` 是 `ItemModelGenerator` 独占的产地。** 手写进去的字段（典型是 `tints`）会在下一次 datagen 被抹掉 —— 要给物品定义加字段，就得改生成器（本项目实测：手写 tints 的药水跑过一次 datagen 后全变灰度瓶）。
5. **普通物品必须手写 `models/item/<id>.json`。** 生成器的退路是 `block/<id>`，对纯物品来说那个模型不存在 → MissingItemModel（暗淡紫黑）。
6. **喷溅 / 滞留药水必须覆写 `getName`。** 原版 `PotionItem.getName` 无条件走 `potion_contents` 拼名，而本模组的药水不走 potion 注册表 → 得到裸键 `item.<ns>.<id>.effect.empty`（游戏里直接显示键名）。
7. **客户端代码绝不写进物品类**：自定义渲染一律进 `RichardsWhimsyModClient`；**能数据化的着色（`tints`）就别写代码**。
8. **消费语义写进组件，不要写进覆写方法**：吃了发生什么 = `consumable.onConsumeEffects`；饱食度 = `food`；返还物 = `usingConvertsTo`。`finishUsingItem` 里手写一套是第 2 级，先证明第 1 级做不到。
9. **覆写点必须带 `@Override`**，且注释里写清它对应旧版哪个 Yarn 方法（迁移对照的落地形式）。
10. **代码风格两条**（同 blockv1 §7 第 8 条）：
    - **缩进一律 Tab**：一个缩进层级 = 一个制表符；文件 UTF-8（无 BOM）+ LF —— 物品类是要互相抄的，格式不一致等于抄错；
    - **层号注释只用单行横幅**：`// ── 层 N ｜ … ──`。三行重横幅**只允许**出现在 `item/paradigm/` 的样板类里。
11. **同一条规则只有一份实现。**
    多个物品共用同一条行为时（例：18 件药水的「注册 + 形态 + 互斥」= `WhimsyPotionRegistration`；两族药水的瓶色常量 = `WI_PotionFamily`），
    实现收进**一个** `final` 工具类（放 `item/` 或其下模块包），条目类只报「要哪些」；
    差异必须是显式参数或显式开关，并在工具类注释里列对照表。**不许**每件抄一份。
12. **一类多件必须留清单**（`ALL` / `ENTRIES`），物品栏与语言条目从清单取值 —— 否则必然出现「注册了但漏登记」。
    药水族的注册顺序也有硬要求：**先注册效果、再注册药水**（`consumable` 在注册当场就要效果句柄；注册后补没意义）。

---

## 8. TODO / 待核实（写进范式，不假装已解决）

| # | 事项 | 现状 |
| --- | --- | --- |
| 1 | **工具 / 武器 / 护甲整套零落地**：`Properties.tool(ToolMaterial, TagKey<Block>, F, F, F)` / `pickaxe/axe/hoe/shovel/sword/spear` / `durability` / `repairable` / `enchantable` / `equippable` / `humanoidArmor` 全部**只核过存在性，没写过一行实现** | 开工具/武器线时先做最小样本（含 `ToolMaterial` 自定义、挖掘标签、`incorrect_blocks_for_drops`） |
| 2 | **自定义 `ToolMaterial`**：26.2 是 record（`incorrectBlocksForDrops, durability, speed, attackDamageBonus, enchantmentValue, repairItems`），**没有注册表**——直接 `new` 是否就够用（原版材质是静态常量） | 待核实；做材质时先看原版 `ToolMaterial.DIAMOND` 怎么被 `Items` 使用 |
| 3 | **`Properties.tool(...)` 三个 float 的语义**、`spear(...)` 的九个 float | 待核实（写长矛/自定义工具时逐个验证） |
| 4 | **燃料**（wiki `BHR_FuelEmber`）：26.2 的 `FuelValues` 只有原版构造入口（`vanillaBurnTimes`），自定义燃料的注册点未核实；Fabric 侧的原生支持也未核实 | 待核实；落地前不要按 1.20.4 的 `FuelRegistry` 经验写 |
| 5 | **`delayedComponent` / `delayedHolderComponent`**（26.2 新入口，用于「注册表就绪后才能解析」的组件；`Properties.jukeboxPlayable(...)` / `fireResistant()` 的实现里能看到同样形状的延迟解析） | 本项目未使用，待核实 |
| 6 | **物品 tints 的其余类型**：`ItemTintSources` 还注册了 `dye` / `grass` / `map_color` / `potion` / `team_color` / `custom_model_data` / `firework`，本项目只用了 `constant` | 待核实（做可染色/药水注册表路线时再看） |
| 7 | **`DataComponents` 里 26.2 新增项**：`swing_animation` / `minimum_attack_charge` / `piercing_weapon` / `kinetic_weapon` / `additional_trade_cost` 等 | 未落地；做战斗/饰品时查 |
| 8 | 配方 / 战利品表 / 物品标签全项目未落地 | 按既定决定暂缓，等「获取方式」统一阶段 |
| 9 | 物品的 lore / tooltip 规范（目前只有 `DEV_HardcoreSwitcher` 的 2 条 `message.` 键；`WhimsyPotionRegistration` 注释里提到 lore，代码未实现） | 待统一：`tooltip.` 前缀 + 双语条目要写进哪个类 |

---

## 9. 样板类怎么用

1. 抄 [`ParadigmItemV1.java`](../../../src/main/java/com/rcdmake/whimsy/item/paradigm/ParadigmItemV1.java) → 改类名/模块前缀/注册名/装配方法名/组件工厂名；
2. 按 §5 勾完 7 处登记点，把勾选状态写进层 0 契约栏；
3. 可选层：只留用到的，其余连注释一起删 —— **别留空壳**；
4. 校验样板本身是否仍与当前 MC 版本 API 一致：临时注释掉 `build.gradle` 里 `sourceSets` 的 `item/paradigm/**` 那行 `exclude`，`gradlew build` 一次即可（样板是能编译的，只是默认不进编译）。

### 9.1 层号注释的写法（与 blockv1 §9.1 同一套）

- **缩进用 Tab**、文件 UTF-8 无 BOM + LF（§7 第 10 条把这两条升成硬约束）。
- **生产类**（`item/Class*/` 下的真实物品）：每层开始处一行，形如
  `// ── 层 4 ｜ 属性装配 ──`，紧贴该层第一个成员的 javadoc 上方；只在该类**实际存在**的层上写（跳过的不写）。
- **样板类**（`item/paradigm/`）：用三行重横幅 + 每层说明（它本身就是教材）。
  ⚠️ 样板类 **只此一家**：生产类照抄这套横幅属于 §7 第 10 条的违规。
- 层号取自 §2 的表格；物品栏清单、语言条目、模型定义之类「登记点载体」不属于物品本体，用 `// ── 登记点 ② ｜ … ──` 标注，不占层号。

---

## 10. 未来展望（V2 讨论存根）

> 本节是 V2 的入口：V1 定稿时已有 **17 个物品类 / 31 件物品**（另有 3 个共享支撑类）按本范式对齐或部分对齐，
> 下表是它们的现状清单 —— **V2 要动哪一条，先在这里补一行**，再开讨论。

### 10.1 V1 落地的类（17 类 / 31 件）

| 模块 | 类 | 物品数 | 用到的可选层 | 阶梯最高级 | 现状偏差 / V2 可议之处 |
| --- | --- | --- | --- | --- | --- |
| BHR | [BHR_Lime](../../../src/main/java/com/rcdmake/whimsy/item/ClassBlockHeadsRebon/BHR_Lime.java) | 1 | 1, 3 | 1 | 组件仍内联在 `registerItem` 的 lambda 里，没有层 4 命名装配方法 |
| BHR | [BHR_Worm](../../../src/main/java/com/rcdmake/whimsy/item/ClassBlockHeadsRebon/BHR_Worm.java) | 1 | 1, 3 | 1 | 同上 |
| BHR | [BHR_DodoApple](../../../src/main/java/com/rcdmake/whimsy/item/ClassBlockHeadsRebon/BHR_DodoApple.java) | 1 | 1, 3 | 1 | 同上；wiki 的「种植」条目 `#todo`（对应树叶未落地） |
| BHR | [BHR_Cherry](../../../src/main/java/com/rcdmake/whimsy/item/ClassBlockHeadsRebon/BHR_Cherry.java) | 1 | 1, 3 | 1 | 同上 |
| BHR | [BHR_Coconut](../../../src/main/java/com/rcdmake/whimsy/item/ClassBlockHeadsRebon/BHR_Coconut.java) | 1 | 1, 3 | 1 | 同上 |
| BHR | [BHR_CoffeeCherry](../../../src/main/java/com/rcdmake/whimsy/item/ClassBlockHeadsRebon/BHR_CoffeeCherry.java) | 1 | 1, 3 | 1 | 同上 |
| BHR | [BHR_Mango](../../../src/main/java/com/rcdmake/whimsy/item/ClassBlockHeadsRebon/BHR_Mango.java) | 1 | 1, 3 | 1 | 同上 |
| BHR | [BHR_MapleSeed](../../../src/main/java/com/rcdmake/whimsy/item/ClassBlockHeadsRebon/BHR_MapleSeed.java) | 1 | 1, 3 | 1 | 同上 |
| BHR | [BHR_Orange](../../../src/main/java/com/rcdmake/whimsy/item/ClassBlockHeadsRebon/BHR_Orange.java) | 1 | 1, 3 | 1 | 同上 |
| BHR | [BHR_PricklyPear](../../../src/main/java/com/rcdmake/whimsy/item/ClassBlockHeadsRebon/BHR_PricklyPear.java) | 1 | 1, 3 | 1 | 同上 |
| BHR | [BHR_PineCone](../../../src/main/java/com/rcdmake/whimsy/item/ClassBlockHeadsRebon/BHR_PineCone.java) | 1 | 1, 9 | 2 | 与树叶共用「可种植基底」判定，判定式在方块类（符合「一份实现」）；`useOn` 与 `finishUsingItem` 的分界可议 |
| DEV | [DEV_ItemsOther](../../../src/main/java/com/rcdmake/whimsy/item/ClassDEV/DEV_ItemsOther.java) | 1 | —（无） | 1 | 纯声明类，一行覆写都没有（这是它的价值）；「某人的QQ头像物品」wiki 记了但代码从未注册 |
| DEV | [DEV_HardcoreSwitcher](../../../src/main/java/com/rcdmake/whimsy/item/ClassDEV/DEV_HardcoreSwitcher.java) | 1 | 1, 9, **M** | **5** | 全项目唯一上到第 5 级的物品；提示文本用 `message.` 键，全项目只有这 2 条 |
| RDT | [RDT_FireHormonePotion](../../../src/main/java/com/rcdmake/whimsy/item/ClassRandomThings/RDT_FireHormonePotion.java) | 3 | 1, 2, 3, 12 | 3 | 饮用型 × 3 级；互斥与形态在共享类 `WhimsyPotionRegistration` |
| RDT | [RDT_PoisonHormonePotion](../../../src/main/java/com/rcdmake/whimsy/item/ClassRandomThings/RDT_PoisonHormonePotion.java) | 3 | 1, 2, 3, 12 | 3 | 同上；与火焰族互为互斥 |
| WI | [WI_PlaguePotion](../../../src/main/java/com/rcdmake/whimsy/item/ClassWhimsyIdea/potion/WI_PlaguePotion.java) | 6 | 1, 2, 3, 12 | 3 | 饮用/喷溅/滞留 × 2 级 |
| WI | [WI_RadiationPotion](../../../src/main/java/com/rcdmake/whimsy/item/ClassWhimsyIdea/potion/WI_RadiationPotion.java) | 6 | 1, 2, 3, 12 | 3 | 同上 |

### 10.1.1 共享支撑类（不是物品类，但物品范式管辖）

| 模块 | 类 | 作用 | 依据 |
| --- | --- | --- | --- |
| 公共 | [WhimsyPotionRegistration](../../../src/main/java/com/rcdmake/whimsy/item/WhimsyPotionRegistration.java) | 药剂注册 + 形态 + 互斥 + 喷溅/滞留取名覆写 | §7 第 11 条「一份实现」的载体 |
| WI | [WI_PotionFamily](../../../src/main/java/com/rcdmake/whimsy/item/ClassWhimsyIdea/potion/WI_PotionFamily.java) | 两族药水的瓶色常量（与 `ITEM_TINT_PREFIXES` 必须一致） | 同上（常量收口） |
| WI | [WI_PotionEntry](../../../src/main/java/com/rcdmake/whimsy/item/ClassWhimsyIdea/potion/WI_PotionEntry.java) | 「形态 + 等级 → 物品」的记录，供物品栏 / 语言遍历 | §7 第 12 条「一类多件留清单」 |

### 10.2 V2 待议清单

1. **层 4 是否强制命名装配方法**：本范式要求 `xxxProperties(Item.Properties)`，但现有 10 件食物仍是内联 lambda。V2 要决定「一次性归一」还是「只对新类强制」。
2. **层 3 组件工厂的粒度**：食物拆成 `xxxFood()` + `xxxConsumable()` 两个方法；药水则把组件装配整个收进共享类 `WhimsyPotionRegistration`，条目类一行组件都没有。是否统一成「一个类一个 `xxxComponents(properties)`，内部再拆」？
3. **层 7 的处理**：现在写成「物品不存在这一层」。是保留这条（防止有人照方块抄），还是从层号表里删掉、只在 §1.2 对照表里说明？
4. **`ModRegister.registerItemFor` / `itemsOf` 收集表**：实现了但**全项目零使用**（`DEV_CreativeTabs` 注释里只是提到）。留还是删？
5. **一类多件的边界**：药水 18 件压缩进 4 个族类（允许）；食物 10 件却各成一个类（不允许合并）。要不要把「什么情况允许合并」写成条文？
6. **工具 / 武器 / 护甲整套缺失**（§8 第 1~3 条）：开线时要不要给范式补「层 4 工具声明」专表 + 挖掘标签登记点？
7. **物品标签与配方**：`mineable/*` 那套是方块的事，但物品侧也有 `swords` / `axes` / `trimmable_armor` 之类 —— 与 §5 第 7 条一起在「获取方式」阶段落地时，要不要新增一层？
8. **提示文本规范**：`message.` / `tooltip.` / lore 三者的分工，目前只有切换器的 2 条 `message.` 键。
9. **药水命名规则升格**：现在写在 §6 的备注里（因为生成器按 id 片段判定形态）。是否升为硬约束第 13 条？
10. **客户端渲染**：如果将来出现「需要 GUI / 特殊模型」的物品，要不要像 blockv1 §10.2 第 7 条那样建一张物品侧接线登记表？

### 10.3 V2 的触发条件

按 §7 硬约束与 §8 TODO 的性质，建议 V2 只在下面任意一条成立时开：
工具/武器/护甲线开工 / 出现第二件需要 Mixin 的物品 / 燃料或配方战利品阶段启动 / 10 件食物的层 4 真要归一。
