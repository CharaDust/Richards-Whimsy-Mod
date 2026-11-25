# 制作台（方块）
{`bhr_workbench`}（方块与它的方块物品同名）· `bhr_workbench`（方块实体类型同理）

## 特点简介
方块愚者（`BHR`）模块的合成方块（工作台类）。它<b>拥有 29 个等级</b>（1~29），每一级有自己的侧面贴图；
挖下来时<b>保留当前等级</b>，放回去还是那一级；等级相同的制作台可以堆叠，等级不同的不能堆叠。
界面（GUI）尚未绘制，因此**暂时不能右键交互**。

## 构造
父级：工作台（`minecraft:crafting_table`）

| 条目      | 值    | 评价  |
| ------- | ---- | --- |
| 合适的挖掘工具 | 无    | 瞬挖，无需工具、不看工具对不对 |
| 硬度      | **0** | 对应 `instabreak()`：单击即碎 |
| 爆炸抗性    | 父级   |     |
| 表面阻力系数  | 父级   |     |
| 速度乘数    | 父级   |     |
| 跳跃乘数    | 父级   |     |
| 弹性碰撞系数  | 父级   |     |
| 引燃几率    | 父级   |     |
| 烧毁几率    | 父级   |     |
| 熔岩可燃性   | 父级   |     |
| 窒息方块    | 父级   |     |
| 视野阻挡    | 父级   |     |
| 红石导体    | 父级   |     |
| 可替代     | 父级   |     |
| 概念性固体方块 | 父级   |     |
| 活塞推动行为  | 父级   |     |
| 乐器      | 父级   |     |
| 地图颜色    | 父级   |     |
| 声音组     | **按等级四档** | 1~8 泥坯（`PACKED_MUD`）、9~17 石头（`STONE`）、18~26 铜块（`COPPER`）、27~29 铁块（`IRON`） |
| 光线阻挡    | 父级   |     |
| 表面生成生物  | 父级   |     |
| 碰撞      | 父级   |     |
| 亮度      | 父级   |     |

## 渲染
- 常规渲染（立方体，横向六面同贴图、上下另给）。
- 贴图分组：

| 等级 | 侧面 | 顶面 / 底面 |
| --- | --- | --- |
| 1~8 | `workbench_side_1` ~ `workbench_side_8`（各自一张） | `workbench_end_1` |
| 9~17 | `workbench_side_9` ~ `workbench_side_17` | `workbench_end_9` |
| 18~26 | `workbench_side_18` ~ `workbench_side_26` | `workbench_end_18` |
| 27~29 | `workbench_side_27` ~ `workbench_side_29` | `workbench_end_27` |

- 模型 `models/block/bhr_workbench_1` ~ `_29`（29 份，父级 `block/cube_bottom_top`），
  blockstate 29 个变体 `level=1` ~ `level=29` 各指一份 —— **多状态方块，blockstate 手写**
  （不在 `BlockStateGenerator.collectAll()` 里声明）。
- **物品图标跟随等级**（2026-10 落地）：`items/bhr_workbench.json` 是
  `minecraft:select` + `property: "minecraft:component"`，29 个 case 依次写
  `"when": 1..29`，各指向同一级的方块模型；`fallback` 指向 1 级模型（组件缺失 / 越界时兜底）。
  - 纯数据、**不需要任何客户端代码**：`select` 的这个 property 会用**组件自己的 codec** 解码
    `when`，而本组件的持久化 codec 就是 `Codec.INT`，所以 `when` 直接写整数。
  - 为什么不走 `range_dispatch`：它的 property 表里没有「读任意组件」的项
    （只有 `custom_model_data` / `damage` / `count` 这些），加自定义 property 得去 widen
    私有的 `RangeSelectItemModelProperties.ID_MAPPER`，反而更脏。
  - 这份 JSON 是 `ItemModelGenerator` **独占产出**，规则写在它的
    `COMPONENT_SELECT_ITEMS` 表里（手写进 `items/` 会被下次 datagen 抹掉）。

## 物品描述（lore）
- 悬停时名称下方显示一行灰色斜体 **「等级：N」**（英文 `Level: N`），
  由组件自带的 tooltip provider 输出，挂在原版 `minecraft:lore` 之后
  —— 与 gem_blessing 同一套写法（`ItemComponentTooltipProviderRegistry.addAfter`）。
- 1 级也有这一行：方块物品的属性里写入了**原型组件** `Level(1)`。

## 改写方法
- **放置定等级**：`getStateForPlacement` 读手上那一堆物品的等级组件
  （`richards-whimsy-mod:workbench_level`，见 [[BHR_WorkBenchLevel]]），没有组件时按 1 级。
- **挖取保等级**：`getDrops` 直接给出「一个带等级的方块物品」。
  *为什么不用战利品表*：等级来自方块状态，而 `minecraft:set_components` 只能写死常量，
  要覆盖 29 个等级就得写 29 条「状态 → 固定组件」的条目；覆写一处即可，
  且爆炸等其它掉落路径走的是同一条 `getDrops`。
- **音效分档**：`getSoundType(BlockState)` 按等级返回四档之一 ——
  破坏 / 踩踏 / 放置 / 打击 / 落地五种音效全部覆盖。
  档位边界与顶面 / 底面贴图的共用边界**故意一致**。
- **选取方块保等级**：`getCloneItemStack` 返回这一级的物品（创造模式中键取块不会退化成 1 级）。

## 界面（GUI，2026-11 第一版）
- 贴图：`textures/gui/container/bhr_workbench.png`（256×256，照原版大箱子改的），
  切图与原版 `ContainerScreen` 完全一致：上半 `(0,0,176,125)`、下半 `(0,126,176,96)` 贴到 `y+125`。
- 面板 176×222（= 114 + 6×18），标题在 `(8,6)`、内容 **「制作台 - 等级 N」**；
  「物品栏」在 `(8,128)`（= `imageHeight - 94`），与原版大箱子同一位置。
- **这一版不能存东西**：菜单里**只有玩家背包**（27 + 9 格），上半部分没有任何容器槽，
  所以物品放不进去，也不会凭空丢东西。
- **Shift 只在「快捷栏 ↔ 背包」之间搬**（`BHR_WorkBenchMenu#quickMoveStack`）；
  原版那套「背包 ↔ 箱子」在这里是错的（上半部分没有可放的格子）。
- 玩家离开方块 4 格以上 / 方块被拆 → 界面自动关闭（`stillValid`，与原版箱子同一条判据）。
- 实现：`menu/ClassBlockHeadsRebon/BHR_WorkBenchMenu`（菜单，双端）+
  `client/ClassBlockHeadsRebon/BHR_WorkBenchScreen`（客户端界面）。
- 实测（26.2 客户端）：菜单 36 个槽，坐标 `8/139`（背包）与 `8/197`（快捷栏），
  与原版 6 行箱子逐格一致；界面打开无异常。

## 配方（愚者配方）
- 制作台只吃**愚者配方**：数据在 `data/richards-whimsy-mod/bhr_recipe/bhr_workbench/**`，
  结构、写法与「为什么不用原版配方系统」见 [配方](../Misc/配方.md)（相对路径 `../../Misc/配方.md`）。
- 关键点：九个需求格，每格是「一类物品 × N 个」；原料可写 `item` / `tag` / `any`（同类替换）；
  配方自带 `time`（秒）、`level`（所需制作台等级）、`expert`（专家配方开关）。
- **升级配方**：`"upgrade_to": N` 代替 `result` —— 不产出物品，直接把制作台升到 N 级
  （例如普通模式有一条 Lv.1 → Lv.11 的跳级配方）。
- 配方类型由游戏规则 **`bhr_expert_recipes`** 切换：开 = 只用专家配方，关 = 只用普通配方。

## 进度与会话语义（2026-11 改版）
- **没有 ticker**：没人打开界面时方块实体完全不 tick（无性能开销）。
- 进度 = `当前游戏刻 - 本份开始游戏刻`（`craftStart`，随方块实体存盘）；界面打开期间菜单每 tick
  调 `refresh(now)` 补算并同步 —— 所以区块卸载/重载不丢进度，无人访问时"暂停"、下次打开按差额补齐。
- `/time set` 把游戏刻调到比 `craftStart` 小 → 视为重新开始这一份（进度归零）。
- **逐份制作**：`total` 是一份时长、`remaining` 是剩余份数；每份完成产物进结果栏、倍率数字 −1。
  升级配方只做一份，完成后**清空选中**（防重复升级）。
- **原料暂存**：开始制作时从背包取出的料放进方块实体的 `pending`，每完成一份扣一份；
  方块被打破时 `preRemoveSideEffects` 吐出结果栏产物 + 剩余原料。
- 关界面不保留选中配方（`clearSelection()`），但保留滚动位置与倍率；制作中不清。

## 后续规划
更多制作台类型（同逻辑、不同配方）、带燃料槽的制作台、配方「内定顺序 + 数据包配方按字母排在后面」，
约定见 [配方](../../Misc/配方.md) 的「后续规划」。

## 事件
- **右键（空手或手持任何物品）→ 打开制作台界面**：`useWithoutItem`（父类的 `useItemOn`
  对普通方块返回 `TRY_WITH_EMPTY_HAND`，所以手持物品也会落到这里，且不会把方块摆上去）。
- 界面本身的逻辑见上一节。

## 方块状态
- `level`（整型，1~29）：当前等级。默认 1。
- 等级随区块存档，不需要方块实体再存一份。

## 获取方式
- 待定（属于「获取方式」阶段，与其它 BHR 方块一起做配方）。
- 现阶段只能从创造模式物品栏拿到 **1 级**那一件；2 级以上只能靠「升级后挖下来」，
  或 `/setblock` 指定 `level`、`/give` 指定组件值。

## 备注
- **方块实体 `BHR_WorkBenchEntity`**：已经就位（`EntityBlock` + 类型已注册），但**没有任何数据与逻辑** ——
  等级在方块状态里，方块实体只读它（`level()`）。界面这一版也不需要它
  （菜单只开了玩家背包），等「能存东西」时再往它上面挂容器。
- **为什么等级是方块状态而不是方块实体字段**：等级同时决定贴图（模型按变体选）与音效
  （`getSoundType` 只有状态参数），这两件都发生在「引擎按方块状态查表」的路径上。
- **堆叠是怎么来的**：不同等级 = 不同组件值 = 不同物品堆，原版堆叠规则白送；
  **1 级不写组件补丁**（原型已给 1 级），所以「新合的 1 级」与「挖回来的 1 级」是同一堆。
  ⚠️ 这里**不要**用 `stack.remove(组件)` 来表示 1 级 —— 原型里已有该组件时，
  「移除」会在补丁里留下「显式没有」的记号，物品堆与新建的不再相同、堆叠会分裂（26.2 实测踩过）。
- 26.2 的 `BlockEntityType` 已经没有 `Builder`，直接 `new BlockEntityType<>(工厂, 合法方块集合)` 构造。
- 方块的 `codec()` 由父类 `Block` 提供，不需要另写。

---

## 2026-12 重构：并入工作面内核

制作台不再自带实体 / 菜单 / 界面，改为 `extends WorkingSurfaceBlock`，只声明自己的规格：

```java
public static final WorkingSurfaceSpec SPEC = new WorkingSurfaceSpec(
        "bhr_workbench", "textures/gui/container/workbench.png",
        29, 0, 0, 0, 0, null);   // 29 级、无燃料、面板不右移
```

- **实体 / 菜单 / 界面**：换成 `WorkingSurfaceEntity` / `WorkingSurfaceMenu` / `WorkingSurfaceScreen`，三台工作面共用（详见 [工作面](../Misc/工作面.md)）；
- **保留在本类**：29 级音效分档（`soundFor`）、`SPEC`、注册链（方块 → 带组件物品 → 方块实体类型 → 菜单类型）；
- **本类不再有**：`BHR_WorkBenchEntity` / `BHR_WorkBenchMenu` / `BHR_WorkBenchScreen`
  —— 三个类已并入内核，旧文件保留为一行注释的空壳，可随时删除；
- **行为变化**：等级闸门从「只盖红纱」改为**真的拦开始制作**（`startCraft` 校验 `recipe.level()`）；
- **新增**：专家模式「专家版优先、缺了退回普通版」；配方栏支持鼠标滚轮逐条翻页；
  `container`（容器返还）字段以前是死字段，现在每结算一份真的会吐出容器。

