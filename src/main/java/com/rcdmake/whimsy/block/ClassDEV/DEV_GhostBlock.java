package com.rcdmake.whimsy.block.ClassDEV;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
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
 *   <tr><td>{@code allowsSpawning(Blocks::never)}</td>
 *       <td>{@code isValidSpawn((state, level, pos, type) -&gt; false)}</td>
 *       <td>≡「表面生成生物 FALSE」。<b>2026-10 更正 + 补齐</b>：本条原写「已移除且无等价覆写点（实测）」，
 *       是<b>误记</b> —— 26.2 的 {@code BlockBehaviour.Properties.isValidSpawn(StateArgumentPredicate)}
 *       一直都在（宝石方块 {@code BHR_GemAmethyst} 也是用它落地的）。本类现已按 wiki 那一行启用
 *       {@code .isValidSpawn(DEV_GhostBlock::neverSpawn)}，幽灵方块不再刷怪。</td></tr>
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
 * <p><b>而转机在于 {@code blocksMotion()} 读的并不直接是碰撞形状</b>：
 * <pre>{@code
 * BlockStateBase.blocksMotion()
 *   → isSolid()
 *   → 字段 legacySolid = calculateSolid()          // 26.2 逐字节读过
 *
 * calculateSolid()：
 *   forceSolidOn  ? true                           // ← 本方块走这一支
 * : forceSolidOff ? false
 * : 无形状缓存（dynamicShape 的方块） ? false
 * : 缓存下来的 collisionShape 为空 ? false
 * : 形状够大 ? true : false
 * }</pre>
 * 即：两个开关都<b>没写</b>时，{@code legacySolid} 会由「状态创建时缓存下来的碰撞形状」反推
 * （青金石玻璃「被水冲掉」踩的就是这个坑，实测见 {@code RDT_GlassLapis} 类注释）；
 * 但只要写了 {@code forceSolidOn()}，结果就<b>与形状无关</b>、恒为 true。于是：
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
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code dev_block_ghost}　<b>模块</b> DEV　<b>父级</b> 玻璃（额外 {@code forceSolidOn()}）</li>
 *   <li><b>启用的可选层</b>：层 9 交互与判定（{@code isPathfindable}、{@code canBeReplaced}）、
 *       层 10 形状与外观（{@code getShadeBrightness} / {@code propagatesSkylightDown} / {@code skipRendering}）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3 状态属性（单状态）、层 6 家族、层 7 codec、层 8 生命周期、
 *       层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 2 级「覆写本类方法」—— 「实体穿过但水冲不掉」由 {@code noCollision()} +
 *       {@code forceSolidOn()}（层 4 声明）解决，<b>没有</b>动 Mixin；上面那张「已排除的走法」表
 *       就是阶梯逐级试下来的记录</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☑ {@code BlockStateGenerator.collectAll()}（单状态）
 *       　③☒ 物品模型无例外　④☑ 中英 {@code DEV_LangEntries}　⑤☑ {@code DEV_CreativeTabs}
 *       　⑥☑ 贴图 / 模型　⑦☐ 配方与战利品表（wiki：获取方式「无」）</li>
 * </ul>
 */
public class DEV_GhostBlock extends Block {

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block DEV_BLOCK_GHOST;

	// ── 层 4 ｜ 谓词助手（供属性工厂使用）──
	/** 不窒息、不阻挡视野：「无」语义用 {@code (state, level, pos) -> false} 表达（层 4 的谓词助手）。 */
	private static boolean never(BlockState state, BlockGetter level, BlockPos pos) {
		return false;
	}

	/**
	 * 表面生成生物 FALSE（层 4 的谓词助手，2026-10 补齐）。
	 * <p>{@code isValidSpawn} 的谓词比另外两个多一个实体类型参数，所以要单独一个四参助手。
	 */
	private static boolean neverSpawn(BlockState state, BlockGetter level, BlockPos pos, EntityType<?> type) {
		return false;
	}

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/**
	 * 幽灵方块的属性（层 4）。
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
	 * 而 {@code BlockStateBase.blocksMotion()} 读的是 {@code isSolid()} → 字段 {@code legacySolid}
	 * （它的完整算法见类注释）。写了 {@code forceSolidOn()} 之后结果就与形状无关，恒为 true，
	 * 所以水不会来占。
	 */
	public static BlockBehaviour.Properties ghostProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
				.noOcclusion()
				// 表面生成生物 FALSE（wiki 那一行；2026-10 用 26.2 的 isValidSpawn 补齐）
				.isValidSpawn(DEV_GhostBlock::neverSpawn)
				.isSuffocating(DEV_GhostBlock::never)
				.isViewBlocking(DEV_GhostBlock::never)
				// 实体穿得过
				.noCollision()
				// 但让 blocksMotion() 仍返回 true，从而挡住水流
				.forceSolidOn();
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：只做装配，没有别的逻辑。
	 * <p>属性来自上面的 {@link #ghostProperties()} —— 它是本构造器实参的来源。
	 */
	public DEV_GhostBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	/** 水不参与寻路（旧方法恒返回 false，这里保持同样语义，层 9）。 */
	@Override
	// ── 层 9 ｜ 交互与判定 ──
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

	/**
	 * 流体替换：一律不允许（层 9）。
	 * <p>这是与上面的形状声明<b>互补</b>的保险 —— 形状判定挡住「水能否流入」，
	 * 本方法挡住「流体直接替换本方块」。两者都在，才不依赖单一机制。
	 */
	@Override
	protected boolean canBeReplaced(BlockState state, Fluid fluid) {
		return false;
	}

	/** 环境光遮蔽：1.0 = 完全不遮蔽（旧 {@code getAmbientOcclusionLightLevel}，层 10）。 */
	@Override
	// ── 层 10 ｜ 形状与外观 ──
	protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 1.0F;
	}

	/** 让天光透过去（旧 {@code isTransparent} 的等价物，层 10）。 */
	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return true;
	}

	/** 相邻的两个同类方块贴合面不渲染（旧 {@code isSideInvisible}，层 10）。 */
	@Override
	protected boolean skipRendering(BlockState state, BlockState neighbor, Direction direction) {
		if (neighbor.getBlock() == this) {
			return true;
		}
		return super.skipRendering(state, neighbor, direction);
	}

	// ── 层 12 ｜ 初始化 ──
	/** 注册（层 12）。 */
	public static void OnInit() {
		DEV_BLOCK_GHOST = ModRegister.registerBlockWithItem("dev_block_ghost",
				new DEV_GhostBlock(ghostProperties()
						.setId(ModRegister.blockKey("dev_block_ghost"))));
	}
}
