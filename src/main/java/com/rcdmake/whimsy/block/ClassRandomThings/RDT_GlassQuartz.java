package com.rcdmake.whimsy.block.ClassRandomThings;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 石英玻璃（{@code rdt_quartz_glass}）—— 由旧项目 {@code RDT_GlassQuartz} 迁来。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassRandomThings/RDT_GlassQuartz.md}）：
 * <ul>
 *   <li>父级：玻璃；属性表逐行「父级」，只有碰撞标了「父级（已改写）」</li>
 *   <li>改写方法：<b>其他实体视此方块为完整碰撞箱；玩家视此方块为空碰撞箱</b>
 *       （与 {@link RDT_GlassLapis} 正好相反）、环境光遮蔽为 0、返回透明方块状态、贴合面不可见</li>
 *   <li>渲染：依据纹理像素点存在性透明（镂空）；事件/方块状态：无；获取方式：无</li>
 * </ul>
 *
 * <p>改名对照与「为什么不能写 {@code noCollision()}」的完整理由见 {@link RDT_GlassLapis} 的类注释 ——
 * 两块玻璃共用同一套处理，只有「谁来当那个例外」反了过来：本方块是<b>玩家穿过、其他实体被挡住</b>。
 * <p>顺带记一笔：本方块这一版<b>没有</b>出现青金石玻璃那种「被水冲掉」的问题，原因是它的兜底分支
 * （非实体上下文）返回的是完整箱 —— 于是引擎缓存下来的碰撞形状不为空，{@code legacySolid} 自然为 true。
 * 这正是 {@link RDT_GlassLapis} 那个坑的反证。
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照</h2>
 * <table border="1">
 *   <caption>本方块涉及的改名（与青金石玻璃同一套）</caption>
 *   <tr><th>Yarn（旧）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code FabricBlockSettings.copyOf(Blocks.GLASS)}</td>
 *       <td>{@code BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)}</td><td>「父级 = 玻璃」</td></tr>
 *   <tr><td>{@code getCollisionShape(..., ShapeContext)} / {@code EntityShapeContext}</td>
 *       <td>{@code getCollisionShape(..., CollisionContext)} / {@code EntityCollisionContext}</td>
 *       <td>碰撞上下文与实体上下文一起改名，方法名与语义不变</td></tr>
 *   <tr><td>{@code VoxelShapes.fullCube()} / {@code VoxelShapes.empty()}</td>
 *       <td>{@code Shapes.block()} / {@code Shapes.empty()}</td><td>形状工厂</td></tr>
 *   <tr><td>{@code getAmbientOcclusionLightLevel} = 1.0f / {@code isTransparent} / {@code isSideInvisible}</td>
 *       <td>{@code getShadeBrightness} = 1.0F / {@code propagatesSkylightDown} / {@code skipRendering}</td>
 *       <td>同其它 WI / RDT 方块</td></tr>
 *   <tr><td>{@code BlockRenderLayerMap}（镂空）</td><td><b>已删除</b></td>
 *       <td>26.1 起由贴图自动判定：{@code rdt/glass_quartz.png} 只有全透 / 全不透两档 → 镂空</td></tr>
 * </table>
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code rdt_quartz_glass}　<b>模块</b> RDT　<b>父级</b> 玻璃</li>
 *   <li><b>启用的可选层</b>：层 9 交互与判定（{@code getCollisionShape} 按实体区分、{@code canBeReplaced}）、
 *       层 10 形状与外观（{@code getShadeBrightness} / {@code propagatesSkylightDown} / {@code skipRendering}）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3 状态属性（单状态）、层 6 家族、层 7 codec、层 8 生命周期、
 *       层 11 预留区、层 C 客户端、层 M Mixin（本方块也没用到 {@code forceSolidOn()}：兜底是完整箱）</li>
 *   <li><b>能力阶梯</b>：停在第 2 级「覆写本类方法」</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☑ {@code BlockStateGenerator.collectAll()}（单状态）
 *       　③☒ 物品模型无例外　④☑ 中英 {@code RDT_LangEntries}　⑤☑ {@code RDT_CreativeTabs}
 *       　⑥☑ 贴图 / 模型　⑦☐ 配方与战利品表（wiki：获取方式「无」）</li>
 * </ul>
 */
public class RDT_GlassQuartz extends Block {

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block RDT_QUARTZ_GLASS;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/** 石英玻璃的属性：完整复制玻璃（碰撞开关保持为 true，理由见 {@link RDT_GlassLapis}）。 */
	public static BlockBehaviour.Properties quartzGlassProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：只做装配，没有别的逻辑。
	 * <p>属性来自上面的 {@link #quartzGlassProperties()} —— 它是本构造器实参的来源。
	 */
	public RDT_GlassQuartz(BlockBehaviour.Properties properties) {
		super(properties);
	}

	/** 流体一律不可替换本方块：水流冲不掉它（第二道保险，理由见 {@link RDT_GlassLapis}）。 */
	@Override
	// ── 层 9 ｜ 交互与判定 ──
	protected boolean canBeReplaced(BlockState state, Fluid fluid) {
		return false;
	}

	/**
	 * 玩家视此方块为空碰撞箱（可穿过），其他实体视此方块为完整碰撞箱。
	 * <p>非实体上下文（{@code CollisionContext.empty()}）也走「完整箱」这一支 —— 这正是本方块
	 * 不像 {@link RDT_GlassLapis} 那样被水冲掉的原因（详见该类注释）。这里显式回落到父级形状，
	 * 语义与原来的 {@code Shapes.block()} 相同，但把「兜底 = 父级」这条规矩写明白了。
	 */
	@Override
	// ── 层 10 ｜ 形状与外观 ──
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
			CollisionContext context) {
		if (context instanceof EntityCollisionContext entityContext) {
			Entity entity = entityContext.getEntity();
			if (entity instanceof Player) {
				return Shapes.empty();
			}
		}
		return super.getCollisionShape(state, level, pos, context);
	}

	/** 环境光遮蔽为 0：旧 {@code getAmbientOcclusionLightLevel} 返回 1.0。 */
	@Override
	protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 1.0F;
	}

	/** 返回透明方块状态：旧 {@code isTransparent} 恒为 true。 */
	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return true;
	}

	/** 两个相同的石英玻璃相连时，贴合的那一面不渲染（旧 {@code isSideInvisible}）。 */
	@Override
	protected boolean skipRendering(BlockState state, BlockState neighbor, Direction direction) {
		if (neighbor.getBlock() == this) {
			return true;
		}
		return super.skipRendering(state, neighbor, direction);
	}

	// ── 层 12 ｜ 初始化 ──
	public static void OnInit() {
		RDT_QUARTZ_GLASS = ModRegister.registerBlockWithItem("rdt_quartz_glass",
				new RDT_GlassQuartz(quartzGlassProperties().setId(ModRegister.blockKey("rdt_quartz_glass"))));
	}
}
