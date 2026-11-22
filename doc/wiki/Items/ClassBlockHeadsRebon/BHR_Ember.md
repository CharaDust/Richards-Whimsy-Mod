# 余烬（物品）
{`bhr_ember`}
## 特点简介
和烈焰弹类似的**单次点火**消耗品：手持右键点火，点火成功即消耗 1 个。
与烈焰弹**唯一的差别**在发射器 —— 它不会发射射弹，而是把余烬本身直接投掷出来。

> [!IMPORTANT]
> 余烬**不是**燃料。早期 1.20.4 的练习版本曾用 `FuelRegistry.INSTANCE.add(BHR_EMBER, 2 * 200)`
> 把它登记成燃料，那只是练习；本设定已废弃，页面里原先的「可为熔炉提供热量 / `cooking_fuel` 组件」一栏随之删除。

## 构造
| 条目 | 值 |
| --- | --- |
| 父级 | 烈焰弹 `FireChargeItem`（点火语义整体继承，不重写 `useOn`） |
| 最大堆叠 | 64（原版默认值，代码里不写 `stacksTo`） |
| 组件 | 无（默认组件；`max_stack_size` 即默认 64） |
| 发射器行为 | `DefaultDispenseItemBehavior`：把 1 个余烬作为物品实体朝发射器朝向弹出 |

## 行为
### 手持右键（同烈焰弹）
- 营火 / 蜡烛 / 蜡烛蛋糕（`lit=false`）→ 直接点亮；
- 否则在**点击面外侧**生成火方块（先过 `BaseFireBlock.canBePlacedAt` 判定）；
- 两处都不成立 → 失败：不消耗、不挥手；
- 成功 → 扣 1 个（**创造模式同样扣**，与原版烈焰弹一致）。

### 发射器
- 不发射小火球，而是**直接投出** 1 个余烬物品实体（带随机散布 + 发射的「噗」声与口部动画）；
- 原因（26.2 逐条核验）：发射器行为是**按物品查表**（`DispenserBlock.DISPENSER_REGISTRY`），
  原版只在 `DispenseItemBehavior.bootStrap()` 里给烈焰弹登记了射弹行为；
  表里没有的物品落到 `DefaultDispenseItemBehavior`，即「把物品弹出去」。
  余烬在 `BHR_Ember.OnInit()` 里**显式登记**了这条默认行为，把意图写死在代码里。

## 获取方式
- 待定（属于「获取方式」阶段，与配方 / 战利品表一起做）。目前可从方块愚者物品栏直接取到。

## 备注
- 代码：`src/main/java/com/rcdmake/whimsy/item/ClassBlockHeadsRebon/BHR_Ember.java`
- 资源：贴图 `textures/item/bhr/ember.png`、手写模型 `models/item/bhr_ember.json`（既有资源）；
  `items/bhr_ember.json` 由 datagen 生成（`gradlew syncDatagen`）。
- 是否参与创造模式物品栏：参与，位置见 [BHR_General](../ItemGroup/ClassBlockHeadsRebon/BHR_General.md) 的「消耗品」组。
- 早期 1.20.4 版的 `tooltip.richards-whimsy-mod.fuel_1` 悬浮提示随燃料设定一并废弃。
