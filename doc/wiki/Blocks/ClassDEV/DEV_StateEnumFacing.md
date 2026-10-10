# 朝向方块（方块）
{`dev_block_state_face`→`dev_block_state_facing`}
## 特点简介
一种测试方块。具有枚举型方块状态
## 构造
父级：石头

| 条目      | 值   | 评价  |
| ------- | --- | --- |
| 合适的挖掘工具 |     |     |
| 硬度      | 父级  |     |
| 爆炸抗性    | 父级  |     |
| 表面阻力系数  | 父级  |     |
| 速度乘数    | 父级  |     |
| 跳跃乘数    | 父级  |     |
| 弹性碰撞系数  | 父级  |     |
| 引燃几率    | 父级  |     |
| 烧毁几率    | 父级  |     |
| 熔岩可燃性   | 父级  |     |
| 窒息方块    | 父级  |     |
| 视野阻挡    | 父级  |     |
| 红石导体    | 父级  |     |
| 可替代     | 父级  |     |
| 概念性固体方块 | 父级  |     |
| 活塞推动行为  | 父级  |     |
| 乐器      | 父级  |     |
| 地图颜色    | 父级  |     |
| 声音组     | 父级  |     |
| 光线阻挡    | 父级  |     |
| 表面生成生物  | 父级  |     |
| 碰撞      | 父级  |     |
| 亮度      | 父级  |     |
## 渲染
- 常规渲染

## 改写方法
- 无

## 事件
- 无

## 方块状态
- FACING
	- 取值范围：原版的 6 个方向（`up` / `down` / `north` / `south` / `west` / `east`）
	- 人为放置时恒为 `north`（本类没有放置逻辑，见「备注」）

## 获取方式
- 无

## 备注
- **改名（2026-10）**：注册名 `dev_block_state_face`→`dev_block_state_facing`，
	类名 `DEV_StateEnumFace`→`DEV_StateEnumFacing`，字段 `DEV_BLOCK_STATE_FACE`→`DEV_BLOCK_STATE_FACING`。
	原因：Minecraft 里「朝向」叫 **Facing**，而 **Face** 指的是**附着面**（天花板 / 墙壁 / 地板，
	见 `AttachFace` / `FaceAttachedHorizontalDirectionalBlock`）；本方块的状态是六向朝向，用 `face` 会造成歧义。
	（属性常量本来就取的是原版共享常量 `BlockStateProperties.FACING`，序列化名一直是 `facing`。）
- 现状（2026-10 更新）：
	- 六个朝向的模型与纹理**已补齐**（`models/block/dev_block_state_facing_{u,d,n,s,e,w}`，
		纹理 `textures/block/dev/dev_cube_endfacing_*` 与 `dev_cube_sidefacing`）
	- 仍缺**合理的放置逻辑**：本类没有覆写 `getStateForPlacement`，所以放下恒为 `north`，
		不会跟随玩家朝向
- 该方块已注册（`RichardsWhimsyMod` 主类已调用其 `OnInit()`）
