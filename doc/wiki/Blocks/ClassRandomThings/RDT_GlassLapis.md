# 青金石玻璃（方块）
{`rdt_lapis_glass`}
## 特点简介
一种允许其他实体通过，但阻挡玩家的玻璃方块。
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

## 改写方法
- 玩家视此方块为具有完整碰撞箱
- 其他实体视此方块为具有空碰撞箱
- 环境光遮蔽设置为0
- 返回透明方块状态
- 当两个相同方块连接时，贴合的面不可见

## 事件
- 无

## 方块状态
- 无

## 获取方式
- 无

## 备注
- ⚠️ **重制注意（本项目踩过两次坑）**：碰撞要覆写 `getCollisionShape` 按实体区分，**且兜底分支必须返回父级形状**。
  26.2 的「概念性固体」（`legacySolid`）是按**缓存下来的碰撞形状**反推的（见 `calculateSolid()`），
  而 `BlockStateBase.getCollisionShape(level, pos)` 那个 2 参重载传的是 `CollisionContext.empty()`（无实体上下文）——
  第一版对无实体上下文返回了空形状，于是 `blocksMotion()` 变 false，**方块会被水流冲掉**（水流闸门
  `FlowingFluid.canHoldAnyFluid` 里 `if (!state.blocksMotion()) return true;`）。
  正确写法：非实体上下文 → `super.getCollisionShape(...)`；再补 `forceSolidOn()` 钉死 solid、`canBeReplaced(state, fluid) = false` 作保险。
  完整推导与实测见 [客户端渲染接线](../../Misc/客户端渲染接线.md)。
- 对照：[RDT_GlassQuartz](RDT_GlassQuartz.md) 的兜底本来就是完整箱，所以一直没出这个问题。
- 模型是 `block/cube_all`（自带 `cullface`），所以 `skipRendering` 能真正生效；自行手写 `elements` 时别忘了补 `cullface`。