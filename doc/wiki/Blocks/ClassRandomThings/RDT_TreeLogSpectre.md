# 通灵原木（方块）
{`rdt_spectre_log`}
## 特点简介
一种半透明树木的原木方块。玩家可以无窒息但稍有阻力地穿过。
## 构造
父级：橡木原木

| 条目      | 值     | 评价          |
| ------- | ----- | ----------- |
| 合适的挖掘工具 |       |             |
| 硬度      | 父级    |             |
| 爆炸抗性    | 父级    |             |
| 表面阻力系数  | 父级    |             |
| 速度乘数    | 父级    |             |
| 跳跃乘数    | 父级    |             |
| 弹性碰撞系数  | 父级    |             |
| 引燃几率    | 30    | 比原木(5)高得多；与树叶/羊毛同档 |
| 烧毁几率    | 60    | 比原木(5)高得多；与树叶/羊毛同档 |
| 熔岩可燃性   | 父级    |             |
| 窒息方块    | FALSE |             |
| 视野阻挡    | 父级    |             |
| 红石导体    | 父级    |             |
| 可替代     | 父级    |             |
| 概念性固体方块 | 父级    |             |
| 活塞推动行为  | 父级    |             |
| 乐器      | 父级    |             |
| 地图颜色    | 父级*   | 待定，该方块为淡蓝色  |
| 声音组     | 父级    |             |
| 光线阻挡    | FALSE |             |
| 表面生成生物  | FALSE |             |
| 碰撞      | 无     |             |
| 亮度      | 父级    |             |
## 渲染
- 透明状态：依据纹理Alpha通道透明

## 改写方法
- 位于方块内时减速（xyz 各乘 0.8）——**已实现**，写法照 `WI_Aerogel`（覆写 `entityInside` → `makeStuckInBlock`）
- 环境光遮蔽设置为0
- 返回透明方块状态
- 放置时轴向取决于贴合面（类似原木）

## 事件
- 无

## 方块状态
- AXIS

## 获取方式
### 战利品表
#### 采集
- 挖掘通灵原木的合适工具是斧。
- 通灵原木被破坏后会掉落自身

## 备注
- ⚠️ **重制注意（本项目踩过坑）**：本页「碰撞 = 无」与「概念性固体 = 父级」这两行必须成对实现 ——
  `noCollision()`（实体穿过）**加上** `forceSolidOn()`（旧 Yarn 的 `.solid()`）。
  只写前者会让方块**被水流冲掉**：26.2 里决定「水能不能占据它」的是 `Properties` 的 solid 开关
  （`legacySolid` → `blocksMotion()`），而不是碰撞形状；`forceSolidOn()` 把该开关拉回 true，
  水流就不来占，且与「实体穿过」不冲突。另加 `canBeReplaced(state, fluid) = false` 作第二道保险。
  详见 [客户端渲染接线](../../Misc/客户端渲染接线.md) 与 `DEV_GhostBlock` / 本方块代码注释。
- 1.20.4 的该方块同时写了 `solidBlock(Blocks::never)` 与 `solid()`（互相打架，后者胜）；
  重制时只保留必需的那一个（`forceSolidOn()`）。
- 实现上继承原版 `RotatedPillarBlock`（自带 `AXIS` 状态与「放置时轴向取决于贴合面」），
  不再手写 `appendProperties` / `getPlacementState`。方块状态是多状态，其 blockstate JSON 由旧项目沿用，不走 datagen。
- wiki「改写方法」未列「贴合面不可见」，但 1.20.4 有 `isSideInvisible` 覆写，本阶段保留（模型 `cube_column` 自带 `cullface`，能生效）。
- 「位于方块内时减速」原先标「代码未实现」，本轮按 `INSIDE_DRAG`（0.85 xyz）实现。
- 可燃性（引燃 30 / 烧毁 60）走 Fabric 的 `FlammableBlockRegistry.getDefaultInstance().add(block, igniteOdds, burnOdds)` ——
  26.2 版 API 的形参名就是 `igniteOdds, burnOdds`，与原版 `FireBlock.setFlammable(block, igniteOdds, burnOdds)` 一一对应；
  原版没有「按方块设可燃性」的 `Properties`，只能这样登记（在方块注册之后、模组初始化阶段）。