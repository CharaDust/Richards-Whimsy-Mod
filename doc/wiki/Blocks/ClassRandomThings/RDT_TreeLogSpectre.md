# 通灵原木（方块）
{`rdt_spectre_log`}
## 特点简介
一种半透明树木的原木方块。玩家可以无窒息但稍有阻力地穿过；**顶面可以站立，按住 Shift 就能从顶面穿下去**（类似原版脚手架）。
## 构造
父级：橡木原木

| 条目      | 值                        | 评价                 |
| ------- | ------------------------ | ------------------ |
| 合适的挖掘工具 | 见「获取方式」                  |                    |
| 硬度      | 父级                       |                    |
| 爆炸抗性    | 父级                       |                    |
| 表面阻力系数  | 父级                       |                    |
| 速度乘数    | 父级                       |                    |
| 跳跃乘数    | 父级                       |                    |
| 弹性碰撞系数  | 父级                       |                    |
| 引燃几率    | 30                       | 比原木(5)高得多；与树叶/羊毛同档 |
| 烧毁几率    | 60                       | 比原木(5)高得多；与树叶/羊毛同档 |
| 熔岩可燃性   | 父级                       |                    |
| 窒息方块    | FALSE                    |                    |
| 视野阻挡    | FALSE                    |                    |
| 红石导体    | 父级                       |                    |
| 可替代     | 父级                       |                    |
| 概念性固体方块 | 父级                       |                    |
| 活塞推动行为  | 父级                       |                    |
| 乐器      | 父级                       |                    |
| 地图颜色    | 父级*                      | 待定，该方块为淡蓝色         |
| 声音组     | 父级                       |                    |
| 光线阻挡    | FALSE                    |                    |
| 表面生成生物  | FALSE                    |                    |
| 碰撞      | 顶面可站立；按住 Shift 穿过（与通灵木板/树叶同一套） | 2026-10 统一，见下      |
| 亮度      | 父级                       |                    |
## 渲染
- 透明状态：依据纹理Alpha通道透明（`spectre_trsp_log_{side,end}.png` 整图 Alpha 恒 195）
- **满屏方块贴图的真凶是原版「方块屏幕效果」，已用 `.isViewBlocking(Blocks::never)` 修掉**（2026-10）：
  站在/穿过本方块时，`ScreenEffectRenderer` 会绕着眼睛取 8 个采样点，
  命中「`RenderShape != INVISIBLE` 且 `isViewBlocking == true`」的方块就把它的**粒子贴图当屏幕空间四边形铺满整屏**。
  写 `isViewBlocking(Blocks::never)` 后该覆盖层整个不提交，视线不再被挡。
  排查过程与「别去查区块网格」的理由见 [客户端渲染接线](../../Misc/客户端渲染接线.md)。

## 改写方法
- **碰撞：顶面可站立 / 按住 Shift 穿过 / 侧面与身位在方块内可穿过（2026-10 统一到共用实现）**
	- **实现只有一份**：`RDT_SpectrePassThrough.collisionShape(pos, context, 父级形状)`，
		三块通灵木料（本块 / [通灵木板](RDT_TreePlanksSpectre.md) / [通灵树叶](RDT_TreeLeavesSpectre.md)）
		都转发到它，各自的差别只有「填哪个父级形状」——木料填 `Shapes.block()`，树叶填 `Shapes.empty()`
	- 判定式照抄 26.2 的 `ScaffoldingBlock.getCollisionShape`，四个分支：
		`isPlacement()` → 空；**非实体查询 → 父级形状**；
		`isAbove(Shapes.block(), pos, true) && !isDescending()` → 整块；其余 → 空
	- 其中 `Entity.isDescending()` 的实现就是 `isShiftKeyDown()`，所以「按住 Shift」=「下降中」= 空碰撞 = 从顶面穿下去；
		`isAbove` 用的是 `entityBottom > pos.getY() + 1 - 1.0E-5`，站着时算「在上方」，脚底与方块底面同高的侧面不算 → 侧面不挡人
	- 形状统一给整块 `Shapes.block()`（脚手架给的是顶面薄板 + 四角柱 `SHAPE_STABLE`），站在上面时碰撞底面正好在 `y+1`
	- ⚠️ **2026-10 的关键修正：非实体上下文的兜底从「空」改成了「父级形状」**。
		26.2 里 `getBlockSupportShape` / `getVisualShape` / `isCollisionShapeFullBlock`
		（进而环境光遮蔽、寻路、红石导体）**全都走 `CollisionContext.empty()` 那一次查询** ——
		兜底给空会让这些能力一起退化成「一个空方块」（范式 §7 硬约束第 4 条）。
		改给「父级形状 = 整块」之后，本方块在这些方面与原版原木一致。实体拿到的仍然是「顶面整块 or 空」，不受影响
- 位于方块内时减速（xyz 各乘 **0.85**）—— **已实现**，转发到 `RDT_SpectrePassThrough.dragInside`
	（三块同值；写法照 `WI_Aerogel`：覆写 `entityInside` → `makeStuckInBlock`）
- 环境光遮蔽设置为0（旧代码取 0.9f 近满亮，现保持 0.9F；迁移到 26.2 后**更不能不写**了，
	因为兜底形状变成整块、默认实现会取 0.2F）
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
- **2026-10 统一：三块通灵木料的碰撞逻辑收进了 `RDT_SpectrePassThrough`**（新增的共享实现类）。
	本方块原先自己写的那段脚手架判定式已经搬走，本类只留一行转发 + 一个参数（父级形状 = `Shapes.block()`）。
	三块的对照表见 [通灵木板](RDT_TreePlanksSpectre.md) 的备注。
	实测（服务器端到端、放沙子看落点）：通灵原木 / 木板 / 树叶三块的顶面都能停住沙子，
	对照组（空气）的沙子一路掉到地面 —— 探针已删除。
- **2026-10 补：本方块已加入 `#minecraft:logs` 标签**（`data/minecraft/tags/block/logs.json`）。
	26.2 的树叶枯萎判定改读新标签 `#minecraft:prevents_nearby_leaf_decay`（原版把它定义成
	`#minecraft:logs` 的转发），**模组原木必须自己进标签**，否则通灵树叶算不出「旁边有树干」、
	长出来的瞬间就连锁枯萎。标签默认是并集，所以不会挤掉原版原木。详见
	[通灵树叶](RDT_TreeLeavesSpectre.md) 的「事件 · 枯萎」。
- ⚠️ **碰撞行（2026-10 起）**：本页「碰撞」不再是「无」，而是「顶面可站立 / Shift 穿过」。
  实现位置是 `getCollisionShape`（层 10 覆写，现已转发到 `RDT_SpectrePassThrough`），属性层仍是
  `noCollision()`（清空父级静态形状）+ `forceSolidOn()`（把 `legacySolid` 钉死为 true）。
  **`forceSolidOn()` 不能删**：26.2 的 `calculateSolid()` 按顺序取值（`forceSolidOn` 第一位 → true），
  删掉它、又让形状在某些上下文里为空，`blocksMotion()` 就会变 false，水流直接占据、方块被冲掉。
  另加 `canBeReplaced(state, fluid) = false` 作第二道保险。
  详见 [客户端渲染接线](../../Misc/客户端渲染接线.md) 与 `DEV_GhostBlock` / 本方块代码注释。
  因此下面那条「碰撞 = 无 与 概念性固体 = 父级 必须成对」的旧配方，现在是「形状按情境给 + `forceSolidOn()` 兜底」。
- **掉落的实体也会被顶面接住、不会穿过**（脚手架同款行为）：`isDescending()` 只认 Shift，
  不认「正在下落」。若以后想改成「下落时穿过」，判定式里再加一条速度方向判断即可。
- 1.20.4 的该方块同时写了 `solidBlock(Blocks::never)` 与 `solid()`（互相打架，后者胜）；
  26.2 只保留必需的那一个（`forceSolidOn()`）。
- 实现上继承原版 `RotatedPillarBlock`（自带 `AXIS` 状态与「放置时轴向取决于贴合面」），
  不再手写 `appendProperties` / `getPlacementState`。方块状态是多状态，其 blockstate JSON 由旧项目沿用，不走 datagen。
- wiki「改写方法」未列「贴合面不可见」，但 1.20.4 有 `isSideInvisible` 覆写，本阶段保留（模型 `cube_column` 自带 `cullface`，能生效）。
- 「位于方块内时减速」原先标「代码未实现」，本轮按 `INSIDE_DRAG`（0.85 xyz）实现。
- 可燃性（引燃 30 / 烧毁 60）走 Fabric 的 `FlammableBlockRegistry.getDefaultInstance().add(block, igniteOdds, burnOdds)` ——
  26.2 版 API 的形参名就是 `igniteOdds, burnOdds`，与原版 `FireBlock.setFlammable(block, igniteOdds, burnOdds)` 一一对应；
  原版没有「按方块设可燃性」的 `Properties`，只能这样登记（在方块注册之后、模组初始化阶段）。