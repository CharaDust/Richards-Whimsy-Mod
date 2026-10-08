package com.rcdmake.whimsy.block.ClassDEV;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 开发_幽灵方块（{@code dev_block_ghost}）—— 由旧项目 {@code DEV_GhostBlock} 迁来。
 *
 * <p><b>无方块状态</b>（单状态），行为特殊：不遮挡、不窒息、实体可穿过、可看穿、
 * 相邻同类方块贴合面隐藏。因为是单状态，其 blockstate 由 datagen 自动生成。
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照（逐条核验过）</h2>
 * <table border="1">
 *   <caption>幽灵方块涉及的改名 / 移除</caption>
 *   <tr><th>Yarn（旧）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code getAmbientOcclusionLightLevel}</td><td>{@code getShadeBrightness}</td><td>环境光遮蔽</td></tr>
 *   <tr><td>{@code isTransparent}</td><td>{@code propagatesSkylightDown}</td><td>是否让天光透过</td></tr>
 *   <tr><td>{@code isSideInvisible(s, from, dir)}</td><td>{@code skipRendering(s, from, dir)}</td><td>相邻同类面隐藏</td></tr>
 *   <tr><td>{@code canPathfindThrough(..., NavigationType)}</td><td>{@code isPathfindable(..., PathComputationType)}</td><td>枚举换名</td></tr>
 *   <tr><td>{@code ShapeContext}</td><td>{@code CollisionContext}</td><td>碰撞上下文</td></tr>
 *   <tr><td>{@code getCameraCollisionShape}</td><td><b>已移除</b></td><td>26.2 相机碰撞不再由方块决定</td></tr>
 *   <tr><td>{@code allowsSpawning(Blocks::never)}</td><td><b>已移除且无等价覆写点</b></td>
 *       <td>既没 builder 也没 {@code isValidSpawn} 可覆写（实测）。这条行为暂时无法保留。</td></tr>
 *   <tr><td>{@code solidBlock(Blocks::never)} / {@code .solid()}</td><td>无这两个 builder</td>
 *       <td>用形状覆写表达（见下）</td></tr>
 *   <tr><td>{@code BlockRenderLayerMap}</td><td><b>已删除</b></td><td>26.1 起透明由贴图像素自动决定</td></tr>
 * </table>
 *
 * <h2>⭐⭐ 水流问题的最终答案（26.2 反编译实证）</h2>
 *
 * <p><b>需求</b>：实体能穿过（无碰撞）＋ 水流冲不掉 ＋ 不引入 waterlogged 等第二职能。
 *
 * <p><b>判定链（逐层读出字节码，均为 26.2）</b>：
 * <pre>{@code
 * FlowingFluid.spreadTo(...)          // 水流把目标格换成水，并销毁原方块
 *   ← 由 canPassThrough(...) 把关
 *   ← canMaybePassThrough(...) && canHoldSpecificFluid(...)
 *   ← canHoldAnyFluid(stateTo)        // ★ 真正的闸门
 * }</pre>
 *
 * <p>{@code canHoldAnyFluid} 是一串<b>原版硬编码</b>判断（没有模组扩展点）：
 * <pre>{@code
 * if (Block instanceof LiquidBlockContainer)  return true;   // 可含水方块
 * if (!state.blocksMotion())                  return true;   // ← 不阻挡运动 → 水可占
 * if (Block instanceof DoorBlock)             return true;   // 门（含栅栏门）
 * if (state.is(BlockTags.SIGNS))              return true;   // 告示牌
 * LADDER / SUGAR_CANE / BUBBLE_COLUMN / NETHER_PORTAL / END_PORTAL /
 * END_GATEWAY / STRUCTURE_VOID                return true;
 * return false;                                              // 其余：水不能占
 * }</pre>
 * 栅栏门之所以不被冲，就是被 {@code DoorBlock} 那一条<b>特权白名单</b>放过了；
 * 模组方块进不去这个名单。
 *
 * <p><b>而转机在于 {@code blocksMotion()} 读的并不是碰撞形状</b>：
 * <pre>{@code
 * BlockStateBase.blocksMotion()
 *   → isSolid()
 *   → 字段 legacySolid
 * }</pre>
 * {@code legacySolid} 由 {@code Properties.forceSolidOn()} / {@code forceSolidOff()}
 * 设置，与 {@code getCollisionShape} 无关。于是：
 * <b>空碰撞形状（实体穿过）＋ {@code forceSolidOn()}（{@code blocksMotion()==true}，
 * 水不来占）可以兼得</b> —— 见 {@link #ghostProperties()}。
 *
 * <h2>已排除的走法（都实测过，别再重复）</h2>
 * <table border="1">
 *   <caption>排查记录</caption>
 *   <tr><th>做法</th><th>结果</th></tr>
 *   <tr><td>覆写 {@code canBeReplaced(state, Fluid)} 返回 false</td><td>❌ 仍被冲（不在水流路径上）</td></tr>
 *   <tr><td>保留完整碰撞箱（去掉 {@code noCollision()}）</td><td>❌ 仍被冲</td></tr>
 *   <tr><td>{@code getShape} / {@code getBlockSupportShape} 给完整箱</td><td>❌ 仍被冲</td></tr>
 *   <tr><td>{@code neighborChanged} + {@code Orientation} 拦水</td><td>❌ NPE（该参数可为 null）且时机在替换之后</td></tr>
 * </table>
 */
public class DEV_GhostBlock extends Block {

	public static Block DEV_BLOCK_GHOST;

	public DEV_GhostBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	/** 不窒息、不阻挡视野：「无」语义用 {@code (state, level, pos) -> false} 表达。 */
	private static boolean never(BlockState state, BlockGetter level, BlockPos pos) {
		return false;
	}

	/**
	 * 幽灵方块的属性。
	 *
	 * <p><b>关键组合：{@code noCollision()} + {@code forceSolidOn()}</b>
	 *
	 * <p>这两个看似矛盾的东西正是「实体能穿过」与「水冲不掉」兼得的关键：
	 * <ul>
	 *   <li>{@code noCollision()} 让碰撞形状为空 → <b>实体穿得过</b>；</li>
	 *   <li>{@code forceSolidOn()} 把 {@code BlockBehaviour.BlockStateBase.legacySolid}
	 *       置为 {@code true} → {@code blocksMotion()} 返回 {@code true}
	 *       → 水流判定「不能占据」→ <b>冲不掉</b>。</li>
	 * </ul>
	 *
	 * <p>反编译依据（{@code FlowingFluid.canHoldAnyFluid}，26.2）：
	 * <pre>{@code
	 * if (Block instanceof LiquidBlockContainer)  return true;   // 可含水方块
	 * if (!state.blocksMotion())                  return true;   // ← 不阻挡运动则可被水占
	 * if (Block instanceof DoorBlock)             return true;   // 门（含栅栏门）
	 * if (state.is(BlockTags.SIGNS))              return true;   // 告示牌
	 * ... (LADDER / SUGAR_CANE / BUBBLE_COLUMN / 传送门 / STRUCTURE_VOID)
	 * return false;
	 * }</pre>
	 * 而 {@code BlockStateBase.blocksMotion()} 读的是
	 * {@code isSolid()} → 字段 {@code legacySolid}，<b>不是碰撞形状</b>。
	 * 所以只要 {@code legacySolid} 为 true，水就不会来占 —— 与碰撞形状无关。
	 */
	public static BlockBehaviour.Properties ghostProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
				.noOcclusion()
				.isSuffocating(DEV_GhostBlock::never)
				.isViewBlocking(DEV_GhostBlock::never)
				// 实体穿得过
				.noCollision()
				// 但让 blocksMotion() 仍返回 true，从而挡住水流
				.forceSolidOn();
	}

	/** 环境光遮蔽：1.0 = 完全不遮蔽（旧 {@code getAmbientOcclusionLightLevel}）。 */
	@Override
	protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 1.0F;
	}

	/** 让天光透过去（旧 {@code isTransparent} 的等价物）。 */
	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return true;
	}

	/** 相邻的两个同类方块贴合面不渲染（旧 {@code isSideInvisible}）。 */
	@Override
	protected boolean skipRendering(BlockState state, BlockState neighbor, Direction direction) {
		if (neighbor.getBlock() == this) {
			return true;
		}
		return super.skipRendering(state, neighbor, direction);
	}

	/** 水不参与寻路（旧方法恒返回 false，这里保持同样语义）。 */
	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

	/**
	 * 流体替换：一律不允许。
	 * <p>这是与上一条<b>互补</b>的保险 —— 形状判定挡住「水能否流入」，
	 * 本方法挡住「流体直接替换本方块」。两者都在，才不依赖单一机制。
	 */
	@Override
	protected boolean canBeReplaced(BlockState state, Fluid fluid) {
		return false;
	}

	public static void OnInit() {
		DEV_BLOCK_GHOST = ModRegister.registerBlockWithItem("dev_block_ghost",
				new DEV_GhostBlock(ghostProperties()
						.setId(ModRegister.blockKey("dev_block_ghost"))));
	}
}
