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
 */
public class RDT_GlassQuartz extends Block {

	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block RDT_QUARTZ_GLASS;

	public RDT_GlassQuartz(BlockBehaviour.Properties properties) {
		super(properties);
	}

	/** 石英玻璃的属性：完整复制玻璃（碰撞开关保持为 true，理由见 {@link RDT_GlassLapis}）。 */
	public static BlockBehaviour.Properties quartzGlassProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS);
	}

	/**
	 * 玩家视此方块为空碰撞箱（可穿过），其他实体视此方块为完整碰撞箱。
	 * <p>非实体上下文（{@code CollisionContext.empty()}）也走「完整箱」这一支 —— 这正是本方块
	 * 不像 {@link RDT_GlassLapis} 那样被水冲掉的原因（详见该类注释）。这里显式回落到父级形状，
	 * 语义与原来的 {@code Shapes.block()} 相同，但把「兜底 = 父级」这条规矩写明白了。
	 */
	@Override
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

	/** 流体一律不可替换本方块：水流冲不掉它（第二道保险，理由见 {@link RDT_GlassLapis}）。 */
	@Override
	protected boolean canBeReplaced(BlockState state, Fluid fluid) {
		return false;
	}

	public static void OnInit() {
		RDT_QUARTZ_GLASS = ModRegister.registerBlockWithItem("rdt_quartz_glass",
				new RDT_GlassQuartz(quartzGlassProperties().setId(ModRegister.blockKey("rdt_quartz_glass"))));
	}
}
