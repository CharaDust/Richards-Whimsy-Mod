package com.rcdmake.whimsy.block.ClassRandomThings;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 红石玻璃（{@code rdt_redstone_glass}）—— <b>新增方块</b>（无 1.20.4 前身），
 * 与 {@link RDT_GlassLapis} / {@link RDT_GlassQuartz} 同一族。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassRandomThings/RDT_GlassRedstone.md}）：
 * <ul>
 *   <li>父级：玻璃；属性表逐行「父级」，只有「碰撞」标了「父级（已改写）」</li>
 *   <li>未激活（{@code triggered=false}）：<b>与普通玻璃完全一致</b> —— 整块碰撞、可站立、可贴附</li>
 *   <li>被激活（{@code triggered=true}）：<b>像幽灵方块一样，所有实体（玩家、生物、掉落物、箭）
 *       都可以穿过</b>；外观换成 {@code rdt/glass_redstone_triggered.png}</li>
 *   <li>激活条件：方块位置<b>收到红石信号</b>（{@code level.hasNeighborSignal(pos)}，
 *       即弱充能 / 强充能都算），信号消失立刻恢复实心 —— 没有红石灯那种「熄灭延迟」</li>
 * </ul>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照</h2>
 * <table border="1">
 *   <caption>本方块涉及的写法（「父级」与两块老玻璃同一套）</caption>
 *   <tr><th>Yarn / 旧写法</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code FabricBlockSettings.copyOf(Blocks.GLASS)}</td>
 *       <td>{@code BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)}</td><td>「父级 = 玻璃」</td></tr>
 *   <tr><td>{@code BooleanProperty.of("triggered")}</td>
 *       <td>{@code BooleanProperty.create("triggered")}</td><td>层 3 的状态属性</td></tr>
 *   <tr><td>{@code StateManager.Builder}</td><td>{@code StateDefinition.Builder}</td><td>层 8 的状态模式</td></tr>
 *   <tr><td>{@code appendProperties(...)}</td><td>{@code createBlockStateDefinition(...)}</td><td>同上</td></tr>
 *   <tr><td>{@code setDefaultState(...)}</td><td>{@code registerDefaultState(...)}</td>
 *       <td>必须在 {@code super(...)} 之后</td></tr>
 *   <tr><td>{@code neighborUpdate(state, world, pos, sourceBlock, notify)}</td>
 *       <td>{@code neighborChanged(state, level, pos, neighborBlock, Orientation, movedByPiston)}</td>
 *       <td>26.2 多出 {@code Orientation} 参数（<b>可为 null</b>，本类不读它）；以 {@code @Override} 处的签名为准</td></tr>
 *   <tr><td>{@code world.isReceivingRedstonePower(pos)}</td>
 *       <td>{@code level.hasNeighborSignal(pos)}</td>
 *       <td>实现在 {@code SignalGetter} 的 default 方法里，{@code Level} 继承了它</td></tr>
 *   <tr><td>{@code isTransparent} / {@code getAmbientOcclusionLightLevel} / {@code isSideInvisible}</td>
 *       <td>{@code propagatesSkylightDown} / {@code getShadeBrightness} / {@code skipRendering}</td>
 *       <td>与另外两块玻璃同一套</td></tr>
 *   <tr><td>{@code ShapeContext} / {@code EntityShapeContext}</td>
 *       <td>{@code CollisionContext} / {@code EntityCollisionContext}</td><td>碰撞上下文改名</td></tr>
 *   <tr><td>{@code BlockRenderLayerMap}（镂空）</td><td><b>已删除</b></td>
 *       <td>26.1 起透明由贴图像素自动判定（{@code rdt/glass_redstone*.png} 为全透 / 全不透两档）</td></tr>
 * </table>
 *
 * <h2>⭐ 为什么这里<b>不能</b>写 {@code noCollision()}（范式 §7 硬约束第 4 条）</h2>
 * 「未激活 = 实心 / 激活 = 空」是<b>逐状态</b>的形状差异，不是「恒为空」：
 * <pre>{@code
 * getCollisionShape(state, level, pos, context)
 *   triggered ? (实体上下文 → Shapes.empty() : 父级完整箱)
 *             : 父级完整箱
 * }</pre>
 * 关键在括号里那一支：<b>非实体上下文（{@code CollisionContext.empty()}）必须回落到父级形状</b>。
 * 26.2 的 {@code BlockStateBase.calculateSolid()} 是用「状态创建时缓存下来的碰撞形状」
 * 反推 {@code legacySolid} 的，而 {@code BlockStateBase.getCollisionShape(level, pos)} 那个 2 参重载
 * 传的正是空上下文 —— 一旦它拿到空形状，{@code blocksMotion()} 就变 false，水流判定
 * （{@code FlowingFluid.canHoldAnyFluid} 的 {@code if (!state.blocksMotion()) return true;}）
 * 就会把这格水灌进来、把方块冲掉（{@link RDT_GlassLapis} 踩过这个坑）。
 * 所以本类：非实体上下文一律回落到 {@code super.getCollisionShape(...)}（父级玻璃的完整箱），
 * 并额外写 {@code forceSolidOn()} 把 {@code legacySolid} 钉死为 true —— 于是
 * <b>「激活时实体穿得过」与「水冲不掉、告示牌贴得上、红石判定照旧」可以兼得</b>。
 *
 * <p>顺带一条：因为兜底是完整箱，{@code isCollisionShapeFullBlock} 对两个状态都为真，
 * 所以本方块和原版玻璃一样是红石导体、寻路也照常把「未激活」当挡路 —— 这正是「未激活时
 * 和正常玻璃表现一致」这句需求在引擎里的落地。
 *
 * <h2>触发逻辑（层 8）</h2>
 * <ul>
 *   <li>{@code getStateForPlacement}：放下时就按「脚下这一格现在有没有信号」给初值
 *       （照抄 {@code RedstoneLampBlock} 的做法）；</li>
 *   <li>{@code neighborChanged}：周围方块变化时重算 {@code hasNeighborSignal}，与当前状态不同才
 *       {@code setBlock(..., Block.UPDATE_ALL)} —— 只在真正变化时写方块，避免相邻红石每 tick 刷写。</li>
 *   <li>刻意<b>不</b>加 {@code scheduleTick}：需求是「激活即通、取消即实」，没有红石灯的 4 tick 熄灭延迟。</li>
 * </ul>
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code rdt_redstone_glass}　<b>模块</b> RDT　<b>父级</b> 玻璃（额外 {@code forceSolidOn()}）</li>
 *   <li><b>启用的可选层</b>：层 3 状态属性（{@code TRIGGERED}）、层 8 状态模式与生命周期
 *       （{@code createBlockStateDefinition} / {@code getStateForPlacement} / {@code neighborChanged}）、
 *       层 9 交互与判定（{@code canBeReplaced}）、层 10 形状与外观
 *       （{@code getCollisionShape} / {@code getShadeBrightness} / {@code propagatesSkylightDown} / {@code skipRendering}）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 6 家族、层 7 codec（继承 {@code Block} 即自带）、
 *       层 11 预留区、层 C 客户端（贴图分状态靠 blockstate variants + 两个模型，不需要客户端代码）、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 2 级「覆写本类方法」—— 「激活即穿过」由状态属性 + 形状覆写解决，
 *       <b>没有</b>动 Mixin</li>
 *   <li><b>硬约束的落地位置</b>：非实体上下文的形状兜底就是 {@link #getCollisionShape} 的最后一行
 *       {@code return super.getCollisionShape(...)}</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☒ <b>不在</b> {@code BlockStateGenerator.collectAll()} 里声明
 *       （2 种 variants，blockstate <b>手写</b>）　③☒ 物品模型无例外
 *       　④☑ 中英 {@code RDT_LangEntries}　⑤☑ {@code RDT_CreativeTabs}
 *       　⑥☑ 贴图 {@code rdt/glass_redstone.png} + {@code rdt/glass_redstone_triggered.png}，
 *       两个 {@code cube_all} 模型　⑦☐ 配方与战利品表（wiki：获取方式「无」）</li>
 * </ul>
 */
public class RDT_GlassRedstone extends Block {

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block RDT_REDSTONE_GLASS;

	// ── 层 3 ｜ 状态属性字段 ──
	/**
	 * 状态属性字段（层 3）：布尔值，序列化名 {@code triggered}（需求指定的名字，与注册名无关）。
	 * <p>默认 {@code false} = 未激活 = 普通玻璃。
	 */
	public static final BooleanProperty TRIGGERED = BooleanProperty.create("triggered");

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/**
	 * 红石玻璃的属性（层 4）：完整复制玻璃，再把「概念性固体」钉死为 true。
	 * <p>理由见类注释：激活状态的碰撞形状对实体是空的，若不钉死 {@code legacySolid}，
	 * 水流会把方块冲掉（范式 §7 硬约束第 4 条）。
	 */
	public static BlockBehaviour.Properties redstoneGlassProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
				.forceSolidOn();
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：装配 + 给状态初值。
	 * <p>属性来自上面的 {@link #redstoneGlassProperties()}；
	 * {@code registerDefaultState} 只能写在 {@code super(...)} 之后（状态模式已在 {@code super} 内部定死）。
	 */
	public RDT_GlassRedstone(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(TRIGGERED, false));
	}

	/** 状态模式（层 8）：声明本方块有 {@code triggered} 这一个状态维度。 */
	@Override
	// ── 层 8 ｜ 状态模式与生命周期 ──
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(TRIGGERED);
	}

	/**
	 * 放置时按「这一格现在有没有红石信号」给初值（层 8，照抄 {@code RedstoneLampBlock}）。
	 * <p>不放其实也能靠随后的 {@code neighborChanged} 纠正，但那样会出现「一放下去先是实心、
	 * 下一 tick 才变空」的闪烁。
	 */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState()
				.setValue(TRIGGERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
	}

	/**
	 * 红石信号变化 → 切换 {@code triggered}（层 8）。
	 *
	 * <p>26.2 的签名比旧版多两个参数：{@code Orientation}（<b>可为 null</b>，本类不读）与
	 * {@code movedByPiston}。只读 {@code level.hasNeighborSignal(pos)} 这个「本格是否被充能」的
	 * 聚合判定，因此不需要知道是哪一面来的信号。
	 *
	 * <p>只在状态真的要变时才写方块：红石线每 tick 都会通知邻居，无条件 {@code setBlock}
	 * 会让本方块跟着每 tick 刷写一次（无谓的同步包与光照重算）。
	 */
	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
			Orientation orientation, boolean movedByPiston) {
		if (level.isClientSide()) {
			return;
		}
		boolean powered = level.hasNeighborSignal(pos);
		if (powered != state.getValue(TRIGGERED)) {
			level.setBlock(pos, state.setValue(TRIGGERED, powered), Block.UPDATE_ALL);
		}
	}

	/** 流体一律不可替换本方块：水流冲不掉它（第二道保险，理由见类注释）。 */
	@Override
	// ── 层 9 ｜ 交互与判定 ──
	protected boolean canBeReplaced(BlockState state, Fluid fluid) {
		return false;
	}

	/**
	 * 碰撞形状（层 10）：<b>激活时所有实体可穿过，否则与父级玻璃一致</b>。
	 *
	 * <p>三分支的含义：
	 * <ol>
	 *   <li>{@code triggered=false} → 直接回落父级（完整箱）：与普通玻璃无异；</li>
	 *   <li>{@code triggered=true} 且上下文带实体 → {@link Shapes#empty()}：玩家 / 生物 / 掉落物 /
	 *       箭一律穿过（「像幽灵方块一样」）；</li>
	 *   <li>{@code triggered=true} 但<b>非实体上下文</b>（{@code CollisionContext.empty()}）→ 仍回落父级。
	 *       这一支是范式硬约束要求的兜底：它决定 {@code legacySolid}、水流能否灌入、
	 *       告示牌等能否贴附，返回空会让方块被水冲掉。</li>
	 * </ol>
	 */
	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
			CollisionContext context) {
		if (state.getValue(TRIGGERED)
				&& context instanceof EntityCollisionContext entityContext
				&& entityContext.getEntity() != null) {
			return Shapes.empty();
		}
		return super.getCollisionShape(state, level, pos, context);
	}

	/** 环境光遮蔽为 0：与父级玻璃一致（旧 {@code getAmbientOcclusionLightLevel} 返回 1.0）。 */
	@Override
	protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 1.0F;
	}

	/** 返回透明方块状态：与父级玻璃一致（旧 {@code isTransparent} 恒为 true）。 */
	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return true;
	}

	/** 两块红石玻璃相连时，贴合的那一面不渲染（旧 {@code isSideInvisible}）。 */
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
		RDT_REDSTONE_GLASS = ModRegister.registerBlockWithItem("rdt_redstone_glass",
				new RDT_GlassRedstone(redstoneGlassProperties()
						.setId(ModRegister.blockKey("rdt_redstone_glass"))));
	}
}
