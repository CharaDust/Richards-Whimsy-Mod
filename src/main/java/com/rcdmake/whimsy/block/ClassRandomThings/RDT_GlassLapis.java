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
 * 青金石玻璃（{@code rdt_lapis_glass}）—— 由旧项目 {@code RDT_GlassLapis} 迁来（1.20.4 Yarn → 26.2 官方名）。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassRandomThings/RDT_GlassLapis.md}）：
 * <ul>
 *   <li>父级：玻璃；属性表逐行「父级」，只有碰撞标了「父级（已改写）」</li>
 *   <li>改写方法：<b>玩家视此方块为完整碰撞箱；其他实体视此方块为空碰撞箱</b>、
 *       环境光遮蔽为 0、返回透明方块状态、两个相同方块贴合的面不可见</li>
 *   <li>渲染：依据纹理像素点存在性透明（镂空）；事件/方块状态：无；获取方式：无</li>
 * </ul>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照（逐条核验过）</h2>
 * <table border="1">
 *   <caption>本方块涉及的改名</caption>
 *   <tr><th>Yarn（旧）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code FabricBlockSettings.copyOf(Blocks.GLASS)}</td>
 *       <td>{@code BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)}</td><td>「父级 = 玻璃」</td></tr>
 *   <tr><td>{@code getCollisionShape(..., ShapeContext)}</td>
 *       <td>{@code getCollisionShape(..., CollisionContext)}</td><td>碰撞上下文换了名字</td></tr>
 *   <tr><td>{@code context instanceof EntityShapeContext} + {@code getEntity()}</td>
 *       <td>{@code context instanceof EntityCollisionContext} + {@code getEntity()}</td>
 *       <td>配套类一起改名，方法名与语义不变</td></tr>
 *   <tr><td>{@code VoxelShapes.fullCube()} / {@code empty()}</td>
 *       <td>{@code Shapes.block()} / {@code Shapes.empty()}</td><td>形状工厂</td></tr>
 *   <tr><td>{@code getAmbientOcclusionLightLevel} = 1.0f</td>
 *       <td>{@code getShadeBrightness} = 1.0F</td><td>环境光遮蔽为 0</td></tr>
 *   <tr><td>{@code isTransparent}</td><td>{@code propagatesSkylightDown}</td><td>返回透明方块状态</td></tr>
 *   <tr><td>{@code isSideInvisible(s, from, dir)}</td><td>{@code skipRendering(s, neighbor, dir)}</td>
 *       <td>同类贴合面不可见（模型侧还要有 {@code cullface}，见 {@code WI_StaticLiquidWater} 的踩坑记录）</td></tr>
 *   <tr><td>{@code BlockRenderLayerMap}（镂空）</td><td><b>已删除</b></td>
 *       <td>26.1 起由贴图自动判定：{@code rdt/glass_lapis.png} 只有全透/全不透两档 → 镂空</td></tr>
 * </table>
 *
 * <h2>⚠️ 为什么绝不能写成 {@code noCollision()}（本项目踩过坑）</h2>
 * 本方块「谁穿得过」是按实体分别判断的，所以正确做法是<b>保留碰撞开关（{@code hasCollision=true}）</b>，
 * 只覆写 {@code getCollisionShape} 的形状。三条实测过的理由：
 * <ol>
 *   <li><b>26.2 的「概念性固体」是由碰撞形状反推的</b>（这是本方块第一版被水冲掉的真正原因）：
 *       {@code BlockStateBase.initCache()} → {@code calculateSolid()} 的计算是
 *       <pre>{@code
 * forceSolidOn ? true
 *            : forceSolidOff ? false
 *            : cache == null（即 dynamicShape 的方块）? false
 *            : cache.collisionShape.isEmpty() ? false
 *            : 形状够大 ? true : false
 *       }</pre>
 *       也就是说：<b>状态创建时缓存下来的那个碰撞形状一旦为空，{@code isSolid()}/{@code blocksMotion()} 就是 false</b>。
 *       本方块第一版对「非实体上下文」（{@code CollisionContext.empty()}）返回了空形状 ——
 *       而 {@code BlockStateBase.getCollisionShape(level, pos)} 这个 2 参重载传的正是 empty 上下文 ——
 *       于是 {@code blocksMotion()} 变成 false，水流判定
 *       （{@code FlowingFluid.canHoldAnyFluid}: {@code if (!state.blocksMotion()) return true;}）
 *       直接放行，方块被冲掉。</li>
 *   <li>水流的另一道闸门 {@code FlowingFluid.canPassThroughWall} 也读 2 参碰撞形状
 *       （{@code targetShape == Shapes.block()} 才拦得住），所以形状回落父级同样必要。</li>
 *   <li>顺带：告示牌/支撑面之类的判定用的也是非实体上下文，形状为空会让它们全都判失败。</li>
 * </ol>
 * 因此本类的兜底分支返回 {@code super.getCollisionShape(...)}（父级玻璃的完整箱），
 * 并额外写 {@code forceSolidOn()} 把 {@code legacySolid} 钉死为 true（与 wiki 的「概念性固体 = 父级」一致）。
 *
 * <p>实测数据（datagen 时直接打的 {@code blocksMotion()} / 2 参形状）：
 * <pre>
 *   青金石玻璃（修前）motion=false shape=empty   ← 因此被冲
 *   石英玻璃         motion=true  shape=block   ← 兜底是完整箱，所以一直没事
 *   幽灵方块         motion=true  shape=empty   ← 靠 forceSolidOn 救回来
 * </pre>
 */
public class RDT_GlassLapis extends Block {

	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block RDT_LAPIS_GLASS;

	public RDT_GlassLapis(BlockBehaviour.Properties properties) {
		super(properties);
	}

	/** 青金石玻璃的属性：完整复制玻璃 + 把「概念性固体」钉死为 true（理由见类注释）。 */
	public static BlockBehaviour.Properties lapisGlassProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
				.forceSolidOn();
	}

	/**
	 * 玩家视此方块为完整碰撞箱，其他实体视此方块为空碰撞箱（旧 {@code getCollisionShape}）。
	 *
	 * <p><b>兜底分支必须回落到父级形状，不能返回空</b>：非实体上下文（{@code CollisionContext.empty()}，
	 * 也就是 {@code BlockStateBase.getCollisionShape(level, pos)} 那个 2 参重载）会被引擎用来
	 * 反推 {@code legacySolid}、判断水流能否穿墙、以及告示牌等支撑判定 ——
	 * 返回空的话方块会被水冲掉（详见类注释里的实测）。
	 */
	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
			CollisionContext context) {
		if (context instanceof EntityCollisionContext entityContext) {
			Entity entity = entityContext.getEntity();
			if (entity instanceof Player) {
				return Shapes.block();
			}
			if (entity != null) {
				// 其他实体：空碰撞箱（可穿过）
				return Shapes.empty();
			}
		}
		// 非实体上下文 → 父级玻璃的完整箱
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

	/** 两个相同的青金石玻璃相连时，贴合的那一面不渲染（旧 {@code isSideInvisible}）。 */
	@Override
	protected boolean skipRendering(BlockState state, BlockState neighbor, Direction direction) {
		if (neighbor.getBlock() == this) {
			return true;
		}
		return super.skipRendering(state, neighbor, direction);
	}

	/** 流体一律不可替换本方块：水流冲不掉它（第二道保险，理由见类注释）。 */
	@Override
	protected boolean canBeReplaced(BlockState state, Fluid fluid) {
		return false;
	}

	public static void OnInit() {
		RDT_LAPIS_GLASS = ModRegister.registerBlockWithItem("rdt_lapis_glass",
				new RDT_GlassLapis(lapisGlassProperties().setId(ModRegister.blockKey("rdt_lapis_glass"))));
	}
}
