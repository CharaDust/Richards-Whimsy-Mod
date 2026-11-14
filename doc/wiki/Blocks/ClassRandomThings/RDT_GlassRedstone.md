# 红石玻璃（方块）
{`rdt_redstone_glass`}
## 特点简介
一种「通电即通透」的玻璃方块：未激活时与普通玻璃完全一致；被红石信号激活后，玩家、生物、掉落物、箭等**所有实体都能穿过**它，外观同时换成激活贴图。
## 构造
父级：玻璃

| 条目      | 值       | 评价    |
| ------- | ------- | ----- |
| 合适的挖掘工具 |         |       |
| 硬度      | 父级      |       |
| 爆炸抗性    | 父级      |       |
| 表面阻力系数  | 父级      |       |
| 速度乘数    | 父级      |       |
| 跳跃乘数    | 父级      |       |
| 弹性碰撞系数  | 父级      |       |
| 引燃几率    | 父级      |       |
| 烧毁几率    | 父级      |       |
| 熔岩可燃性   | 父级      |       |
| 窒息方块    | 父级      |       |
| 视野阻挡    | 父级      |       |
| 红石导体    | 父级      |       |
| 可替代     | 父级      |       |
| 概念性固体方块 | 父级      |       |
| 活塞推动行为  | 父级      |       |
| 乐器      | 父级      |       |
| 地图颜色    | 父级      |       |
| 声音组     | 父级      |       |
| 光线阻挡    | 父级      |       |
| 表面生成生物  | 父级      |       |
| 碰撞      | 父级（已改写） | 见下方改写 |
| 亮度      | 父级      |       |
## 渲染
- 透明状态：依据纹理像素点存在性透明
- 未激活：`rdt/glass_redstone.png`（`block/cube_all`）
- 激活：`rdt/glass_redstone_triggered.png`（`block/cube_all`）

## 改写方法
- 未激活时：与父级玻璃一致（整块碰撞箱）
- 激活时：所有实体视此方块为具有空碰撞箱（可穿过）
- 激活时的非实体上下文查询仍返回父级形状（水流 / 支撑面 / 红石判定不受影响）
- 环境光遮蔽设置为0
- 返回透明方块状态
- 当两个相同方块连接时，贴合的面不可见

## 事件
- 红石信号变化（`neighborChanged`）：本格收到信号 → `triggered = true`；信号消失 → `triggered = false`。
  - 判据是 `level.hasNeighborSignal(pos)`，**弱充能 / 强充能都算**（与红石灯同一条判定）
  - **立即生效**，没有红石灯那种 4 tick 熄灭延迟
  - 放置时也会按当前位置的信号给初值（`getStateForPlacement`），不会出现「先实心、下一 tick 才通透」的闪烁

## 方块状态
| 状态 | 默认 | 含义 |
| --- | --- | --- |
| `triggered` | `false` | `false` = 未激活（等同普通玻璃）；`true` = 已激活（所有实体可穿过，换激活贴图） |

variants（`assets/richards-whimsy-mod/blockstates/rdt_redstone_glass.json`）：
- `triggered=false` → `richards-whimsy-mod:block/rdt_redstone_glass`
- `triggered=true` → `richards-whimsy-mod:block/rdt_redstone_glass_triggered`

## 获取方式
- 无

## 备注
- ⚠️ **重制注意**：本方块是「按状态区分碰撞」的方块，兜底分支必须返回**父级形状**（与 [RDT_GlassLapis](RDT_GlassLapis.md) /
  [RDT_GlassQuartz](RDT_GlassQuartz.md) 同一条硬约束）。26.2 的「概念性固体」（`legacySolid`）是按**缓存下来的碰撞形状**反推的，
  而 `BlockStateBase.getCollisionShape(level, pos)` 那个 2 参重载传的是 `CollisionContext.empty()`（无实体上下文）——
  若对无实体上下文返回空形状，`blocksMotion()` 会变 false，**方块会被水流冲掉**
  （`FlowingFluid.canHoldAnyFluid` 里 `if (!state.blocksMotion()) return true;`）。
  正确写法：非实体上下文 → `super.getCollisionShape(...)`；再补 `forceSolidOn()` 钉死 solid、`canBeReplaced(state, fluid) = false` 作保险。
- 与另外两块玻璃的差异：它们「谁被挡住」是按实体类型写死的（青金石玻璃挡玩家、石英玻璃挡非玩家），
  本方块**不看实体类型**，只看状态 —— 这正是需求「激活时所有实体可穿过」的落地方式。
- 因为兜底是完整箱，两个状态的 `isCollisionShapeFullBlock` 都为真 → 与原版玻璃一样是红石导体；
  未激活时的寻路 / 支撑面 / 贴附行为也与玻璃一致。
- 模型是 `block/cube_all`（自带 `cullface`），所以 `skipRendering` 能真正生效；自行手写 `elements` 时别忘了补 `cullface`。
- 「激活」= 红石信号。若以后要做成「右键切换」或「红石脉冲翻转」，只需改 `neighborChanged` 一处（状态与形状都不用动）。
