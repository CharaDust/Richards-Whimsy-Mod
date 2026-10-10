# 石英灯（方块）
{`rdt_quartz_lamp`}
## 特点简介
一盏**分侧**的灯，与 [青金石灯](RDT_LampLapis.md) 正好相反：**服务端逻辑里它是一盏 15 级光源**，
**玩家却看不到任何光**（它自己和周围都跟没放灯一样）。

**用途（原话）**：昏暗山洞景观的**隐藏式照明**，放置刷怪 ——
景观保持昏暗外观，服务端按"有光"处理（该处不刷怪；冰按有光融化、作物按有光生长）。
参考模组（1.12.2 Random Things）的设定文本也一致：*"the light of this lamp is not actually visible.
It still prevents stuff like mob spawning though."*
## 构造
父级：红石灯

> ⚠️ 「父级」只指**属性表**（硬度、爆炸抗性、声音等）。本方块**不接收红石信号** ——
> 本体继承 `Block`，刻意**不**继承 `RedstoneLampBlock`。

| 条目      | 值      | 评价                     |
| ------- | ------ | ---------------------- |
| 合适的挖掘工具 |        |                        |
| 硬度      | 父级     | 红石灯 = 0.3              |
| 爆炸抗性    | 父级     | 红石灯 = 0.3              |
| 表面阻力系数  | 父级     |                        |
| 速度乘数    | 父级     |                        |
| 跳跃乘数    | 父级     |                        |
| 弹性碰撞系数  | 父级     |                        |
| 引燃几率    | 父级     |                        |
| 烧毁几率    | 父级     |                        |
| 熔岩可燃性   | 父级     |                        |
| 窒息方块    | 父级     |                        |
| 视野阻挡    | 父级     |                        |
| 红石导体    | 父级     |                        |
| 可替代     | 父级     |                        |
| 概念性固体方块 | 父级     |                        |
| 活塞推动行为  | 父级     |                        |
| 乐器      | 父级     |                        |
| 地图颜色    | 父级     | 红石灯的陶土橙                |
| 声音组     | 父级     | 玻璃                     |
| 光线阻挡    | 父级     |                        |
| 表面生成生物  | 父级     | 红石灯写的是 `Blocks::always` |
| 碰撞      | 父级     |                        |
| 亮度      | 0 / 15* | **玩家侧 0**、**服务端逻辑 15**（分侧，见备注） |
## 渲染
- 常规渲染（`block/cube_all`）。刻意**不**写 `emissiveRendering` —— 那是"玩家看不见它在发光"的实现方式之一。

## 改写方法
- 无（方块类里一个覆写点都没有）

## 事件
- 无

## 方块状态
- 无

## 获取方式
- 无（参考模组的配方是「红石灯居中 + 四个石英」，留到"获取方式"阶段）

## 验证方法（无头：只跑专用服务端，不需要客户端）
`light` 谓词读的就是**服务端光照引擎**（`LightPredicate.matches` → `level.getMaxLocalRawBrightness(pos)`），
所以可以用世界数据包把「服务端逻辑里的亮度」直接读出来，不用进游戏：

1. 数据包放 `<world>/datapacks/whimsy-light-test/`（本项目的工作副本在
   `run/whimsy-smoke/datapacks/whimsy-light-test/`；`pack.mcmeta` 的
   `pack_format` / `min_format` / `max_format` 都写 107）；
2. `data/whimsy_test/predicate/*.json` 写判定，例如
   `{"condition":"minecraft:location_check","predicate":{"light":{"light":{"max":0}}}}`
   （外层 `light` 是 `LocationPredicate` 的字段，内层 `light` 是 `LightPredicate` 自己的字段；
   同理 `lit13.json` / `lit15.json` 用 `min`）；
3. `data/whimsy_test/function/*.mcfunction`：先 `forceload add` 测试区 → 搭密封石室（外面 7³ 石头、
   里面 5³ 空气）→ `setblock` 测试方块 → 隔 100+ tick 再用
   `execute positioned <x> <y> <z> if predicate whimsy_test:dark run say ...` 判定，结果以 `say`
   出现在服务端日志里；
4. `gradlew runServer`，看日志里的 `VERDICT_*`。

⚠️ 两个实测踩坑（不知道就会得到一堆假失败）：
- **26.2 专用服务端的出生区块默认不常驻加载**：`fill` / `setblock` / `if block` / `light` 在未加载区块上
  **静默失败或一律为 false**（`BlockPredicate.matches` 第一行就是 `!level.isLoaded(pos) → false`），
  所以必须先 `forceload add <x1> <z1> <x2> <z2>`（**区块**坐标）。
- **函数里某条命令失败不会中断函数**（后面照跑），所以不能靠"最后一行 `say` 打出来了"判断前面成功 ——
  要像上面那样用 `if block` 逐条核对装置真的就位。

**2026-10-10 实测结果（两盏灯各验一次，且是在从盘上载入的存档里）**：
`VERDICT_LAPIS_SERVER_LIGHT_0_OK`（青金石灯那一格 = 0）、
`VERDICT_QUARTZ_SERVER_LIGHT_15_OK`（石英灯那一格 = 15）、
`VERDICT_CONTROL_DARK_0_OK`（密封空室 = 0，证明测试位没有天光干扰）、
`VERDICT_TORCH_14_OK`（火把 = 14，证明测试位能亮起来）、
`VERDICT_MIXED_TORCH_WORKS_OK`（**紧挨青金石灯的火把在服务端照常生效** ——
"在服务端把灯附近的光忽略掉"那种近似做法会在这里翻车）。

## 备注
- **能力阶梯：第 5 级「Mixin」**（层 M），与青金石灯**共用同一套三个 Mixin**，只是方向相反：
  | | 客户端引擎 | 服务端引擎 |
  | --- | --- | --- |
  | 青金石灯 | **15**（玩家看得见） | **0**（逻辑里不存在） |
  | 石英灯 | **0**（玩家看不见） | **15**（逻辑里在发光） |
  1. `RDT_LampLightEmissionMixin` → `BlockLightEngine.getEmission`：按**引擎所属的 Level** 决定本侧等级
     （不看环境侧：单人游戏客户端与内部服务端同进程同 ClassLoader）。
  2. `RDT_LampLightSyncMixin` → **两侧都要对齐**：
     - 客户端：光照包里的数值是**服务端引擎**算的（石英灯是 15），会在客户端显示出来 ⇒
       对这些灯调 `checkBlock`，引擎按本侧 0 走**衰减传播**把光抹掉（同时正确保留别的光源）；
     - 服务端：两盏灯的状态发光值都是 0，原版 `propagateLightSources` 是直接读状态发光值登记光源的 ⇒
       它不把灯当光源。放在地上的那一刻由原版 `checkBlock` 触发没问题，
       但**区块从盘上载入**（以及升级前就存在的旧存档）需要这一手把石英灯的光补上。
  3. `RDT_LampIndexMaintenanceMixin` → 灯位置索引失效，供两侧对齐时定位灯。
- **没有"区域近似"**：引擎里存的就是**本侧视角的真值** —— 服务端引擎里真的有这盏灯的光，
  所以遮挡、融冰范围、与火把/青金石灯混用、被其它 mod 读取，全都自然正确。
  （第一阶段曾用 `Monster.isDarkEnoughToSpawn` 上的"幻影光"做过渡：只覆盖敌怪刷怪、且是距离近似会穿墙；
  第二阶段换成上面这套后，那个 Mixin 已删除。）
- **0~4 级为什么不行**：`lightLevel` 是状态构造时求值一次的、两侧共用的一个 int；
  方块类里没有方法能改"某一侧看到的光照场"；26.2 的 `attributes` 是环境属性（维度/生物群系/时间线），
  没有方块级钩子；`SpawnPlacements.register` 不许重复注册；Fabric API 没有光照或刷怪事件。
- **参考模组（1.12.2 Random Things）**：它用一个覆写（`getLightValue` 按侧别返回 15/0）+ 客户端自愈
  （`randomDisplayTick` 里 `checkLightFor`）就做到了同一件事。差别：①1.12.2 的 `getLightValue` 能拿到世界，
  26.2 的 `lightLevel` 不能 ⇒ 只能打在引擎发光查询上；②1.12.2 的客户端自己算光照，26.2 的客户端光照由服务端包喂
  ⇒ 自愈从"保险"变成"必需"。**参考模组同样需要自愈**，反过来印证了这条路线。
- **Mixin 三处登记**：①三个类本身 ②`richards-whimsy-mod.mixins.json` 的 `mixins`
  ③`fabric.mod.json` 的 `mixins`。删除方式：删三个类 + ② 里三行（两盏灯一起失效，退回"都不发光"）。
- **验证状态（2026-10-10）**：**服务端侧已用上面的无头装置验过**（石英灯那一格 = 15、青金石灯 = 0、
  对照与混用都对）；**客户端侧（"完全看不见光"）还需要你在游戏里过一遍**。下面是回归清单：
  - 黑暗处放一盏：**它自己和周围都看不出在发光**；
  - 以它为中心 15 格内**不刷敌怪**（黑屋里放一盏对比）；**冰/雪按有光融化**（这是"服务端真的有光"的证据）；
  - 与青金石灯、火把**混用**：三者互不干扰（火把照常亮、青金石灯的区域照常刷怪）；
  - **走远再回来 / 重开存档**后行为一致（服务端补光 + 客户端抹光都生效的证据）；
  - 拆掉后行为立刻消失；日志里若出现本模组 Mixin 未命中提示，功能会静默失效（见上面第 5 级说明）。
