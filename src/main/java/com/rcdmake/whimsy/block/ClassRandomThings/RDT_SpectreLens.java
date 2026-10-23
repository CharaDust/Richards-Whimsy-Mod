package com.rcdmake.whimsy.block.ClassRandomThings;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * 通灵镜片（{@code rdt_spectre_lens}）—— 放在信标正上方、像地毯一样的薄层玻璃镜片。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassRandomThings/RDT_SpectreLens.md}）：
 * <ul>
 *   <li>父级：玻璃；模型是 1/16 高的薄层（与原版地毯同一套几何）</li>
 *   <li>作用：把它下方信标的效果「搬运」到全局 —— 按信标时钟（80 tick / 4 秒）上报
 *       (坐标, 维度, 效果, 强度)，并让该维度所有玩家获得记录里的效果，时长固定 17 秒</li>
 *   <li>记录的落地、校验、施放都在 {@link RDT_SpectreLensIndex}（本模块唯一的实现处）</li>
 *   <li>镜片自身不遮挡信标光束（1/16 薄层 + 玻璃属性，见类注释「为什么不会被当成遮挡」）</li>
 * </ul>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照（逐条核验过）</h2>
 * <table border="1">
 *   <caption>本方块涉及的改名 / 迁移</caption>
 *   <tr><th>Yarn（旧）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code FabricBlockSettings.copyOf(Blocks.GLASS)}</td>
 *       <td>{@code BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)}</td><td>「父级 = 玻璃」</td></tr>
 *   <tr><td>{@code getOutlineShape(...)} / {@code getCollisionShape(...)}</td>
 *       <td>{@code getShape(...)} / {@code getCollisionShape(...)}</td>
 *       <td>26.2 只覆写 {@code getShape} 就够：{@code getCollisionShape} 默认返回
 *           {@code hasCollision ? state.getShape(...) : Shapes.empty()}</td></tr>
 *   <tr><td>{@code onPlaced(...)} / {@code onStateReplaced(...)}</td>
 *       <td>{@code onPlace(...)} / {@code affectNeighborsAfterRemoval(...)}</td>
 *       <td>放置 / 被替换（26.2 的旧 {@code onStateReplaced} 已改名）；后者的参数是
 *           {@code ServerLevel}，只在服务端触发</td></tr>
 *   <tr><td>{@code world.scheduleBlockTick(pos, this, 80)}</td>
 *       <td>{@code level.scheduleTick(pos, this, 80)}</td><td>计划刻（自重排，充当信标时钟）</td></tr>
 *   <tr><td>{@code scheduledTick(state, serverWorld, pos, random)}</td>
 *       <td>{@code tick(state, serverLevel, pos, random)}</td><td>计划刻的覆写点</td></tr>
 *   <tr><td>{@code neighborUpdate(...)}</td>
 *       <td>{@code neighborChanged(state, level, pos, block, @Nullable Orientation, movedByPiston)}</td>
 *       <td>26.2 用 {@code Orientation} 取代了旧的 {@code Direction}（红石朝向）</td></tr>
 * </table>
 *
 * <h2>为什么不会被当成遮挡（用户明确要求）</h2>
 * 26.2 的信标光束扫描从<b>信标自己那一格</b>开始（{@code BeaconBlock} 实现 {@code BeaconBeamBlock}，
 * 先建好第一个光束段），随后逐格向上；判定遮挡的条件是
 * {@code state.getLightDampening() >= 15 && !state.is(Blocks.BEDROCK)}。
 * 本方块是「玻璃属性 + 1/16 薄层」：{@code solidRender = isShapeFullBlock(occlusionShape)} 为 false，
 * {@code getLightDampening()} 落到 0（{@code propagatesSkylightDown} 返回 true）。
 * 所以镜片放在信标正上方只会「延伸光束」，不会清空光束段 —— 信标自己的效果照常结算。
 *
 * <h2>为什么不是方块实体</h2>
 * 镜片没有自己的状态：要上报的 (坐标, 维度, 效果, 强度) 全部由「世界状态 + 下方信标」现场推导。
 * 周期回调用计划刻（{@code scheduleTick} 自重排 80 tick，随区块存档保存在 {@code block_ticks}），
 * 跨维度的索引与施放由 {@link RDT_SpectreLensIndex} 的 80 tick 事件驱动。
 * 详细论证见 {@code doc/agent/deepseek/11-通灵镜片调查报告.md} §2 / §4.9。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code rdt_spectre_lens}　<b>模块</b> RDT　<b>父级</b> 玻璃</li>
 *   <li><b>wiki 页</b> {@code doc/wiki/Blocks/ClassRandomThings/RDT_SpectreLens.md}</li>
 *   <li><b>启用的可选层</b>：层 1 规格常量（80 tick / 340 tick）、层 8 生命周期
 *       （{@code onPlace} / {@code tick} / {@code neighborChanged} / {@code affectNeighborsAfterRemoval}）、
 *       层 10 形状与外观（{@code getShape} / {@code getShadeBrightness} / {@code propagatesSkylightDown}）</li>
 *   <li><b>跳过的可选层</b>：层 3 状态属性（单状态）、层 6 家族、层 7 codec、层 9 交互与判定、
 *       层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：方块本体停在第 2 级「覆写本类方法」；整套功能最高第 4 级
 *       （{@code ServerTickEvents.END_LEVEL_TICK} + 跨类通知 {@link RDT_SpectreLensIndex}）。
 *       <b>没停在第 3 级</b>是因为「每 80 tick 主动巡检 + 给全维度玩家施放」只能靠事件驱动；
 *       <b>没上第 5 级</b>是因为信标状态有公开的 NBT 快照通道（{@code saveWithoutMetadata}）</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☑ {@code BlockStateGenerator.collectAll()}（单状态）
 *       　③☒ 物品模型无例外（与方块同模型，同原版地毯）　④☑ 中英 {@code RDT_LangEntries}
 *       　⑤☑ {@code RDT_CreativeTabs}　⑥☑ 贴图 / 手写薄层模型　⑦☐ 配方与战利品表（全项目暂缓）</li>
 * </ul>
 */
public class RDT_SpectreLens extends Block {

	// ── 层 1 ｜ 规格常量 ──
	/**
	 * 信标时钟（Tick）：原版信标每 80 tick 结算一次（{@code level.getGameTime() % 80 == 0}）。
	 * 镜片用计划刻自重排同一个拍子，索引的校验/施放也在同一个拍子（见 {@link RDT_SpectreLensIndex}）。
	 */
	static final int BEACON_CLOCK_TICKS = 80;

	/** 广播时长（Tick）：固定 17 秒 = 340 tick（用户指定；正好等于 4 级信标原版时长）。 */
	static final int BROADCAST_DURATION_TICKS = 17 * 20;

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体，在 {@link #OnInit()} 里赋值。 */
	public static Block RDT_SPECTRE_LENS;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/**
	 * 通灵镜片的属性：完整复制玻璃（硬度 0.3 / 爆炸抗性 0.3 / 声音组 GLASS /
	 * {@code noOcclusion} / 不窒息 / 不挡视野 / 不生成生物 / 非红石导体）。
	 * <p>薄层的碰撞形状不在这里 —— 它是类行为，写在层 10 的 {@code getShape}。
	 */
	public static BlockBehaviour.Properties spectreLensProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：只做装配，没有别的逻辑。
	 * <p>属性来自上面的 {@link #spectreLensProperties()} —— 它是本构造器实参的来源。
	 */
	public RDT_SpectreLens(BlockBehaviour.Properties properties) {
		super(properties);
	}

	// ── 层 8 ｜ 状态模式与生命周期 ──
	/**
	 * 放置：立即上报一次，并排下第一拍计划刻。
	 * <p>只在服务端做（客户端排刻/写索引都没有意义，且索引是服务端单例）。
	 * 用 {@code onPlace} 而不是 {@code setPlacedBy}：{@code onPlace} 连 {@code /setblock} 也能覆盖。
	 */
	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		if (level.isClientSide()) {
			return;
		}
		RDT_SpectreLensIndex.onLensPlaced(level, pos);
		level.scheduleTick(pos, this, BEACON_CLOCK_TICKS);
	}

	/**
	 * 计划刻到点（信标时钟）：重算本镜片的记录，并排下一拍。
	 * <p>⚠️ 计划刻跑在 {@code blockTicks} 阶段，<b>早于</b>信标方块实体的 tick
	 * （见 {@code ServerLevel.tick}），所以这里读到的是上一拍的信标状态 —— 只用来「写记录」；
	 * 「给玩家施加效果」统一放在 {@code END_LEVEL_TICK}（那时信标已经结算完本拍）。
	 */
	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		RDT_SpectreLensIndex.onLensClock(level, pos);
		level.scheduleTick(pos, this, BEACON_CLOCK_TICKS);
		super.tick(state, level, pos, random);
	}

	/**
	 * 相邻方块变化：立刻重算一次（例如下方信标被换掉/拆掉）。
	 * <p>信标金字塔的变化<b>不会</b>通知到镜片（镜片与金字塔不相邻），那类失效由 80 tick 的
	 * 校验兜底（{@link RDT_SpectreLensIndex#OnInit()} 注册的全局事件）。
	 */
	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
			@Nullable Orientation orientation, boolean movedByPiston) {
		super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
		if (level instanceof ServerLevel serverLevel) {
			RDT_SpectreLensIndex.onLensClock(serverLevel, pos);
		}
	}

	/**
	 * 被替换/拆除：把该坐标从全局记录里删掉（用户要求的「失效信号」）。
	 * <p>26.2 里旧的 {@code onStateReplaced} 已改名为 {@code affectNeighborsAfterRemoval}，
	 * 参数是 {@code ServerLevel}，只在服务端触发。
	 */
	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos,
			boolean movedByPiston) {
		super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
		RDT_SpectreLensIndex.onLensRemoved(level, pos);
	}

	// ── 层 10 ｜ 形状与外观 ──
	/** 1/16 高的薄层（与原版地毯同一个箱子）。 */
	private static final VoxelShape SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.0625, 1.0);

	/**
	 * 轮廓/碰撞形状：薄层。
	 * <p>只覆写 {@code getShape} 就同时得到两者 —— {@code getCollisionShape} 的默认实现是
	 * {@code hasCollision ? state.getShape(level, pos) : Shapes.empty()}。
	 */
	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	/**
	 * 环境光遮蔽为 0：父级玻璃是 {@code TransparentBlock}（类行为），
	 * 而 {@code ofFullCopy} 只复制属性、<b>不复制类行为</b>，所以这里补回。
	 */
	@Override
	protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 1.0F;
	}

	/**
	 * 返回透明方块状态：同上，补回父级玻璃的类行为。
	 * <p>副作用（正是我们要的）：{@code getLightDampening()} 随之变成 0，
	 * 信标光束扫描不会把镜片当成遮挡。
	 */
	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return true;
	}

	// ── 层 12 ｜ 初始化 ──
	/** 注册方块 + 方块物品，并把索引的全局事件接上（顺序：先方块，再索引）。 */
	public static void OnInit() {
		RDT_SPECTRE_LENS = ModRegister.registerBlockWithItem("rdt_spectre_lens",
				new RDT_SpectreLens(spectreLensProperties().setId(ModRegister.blockKey("rdt_spectre_lens"))));

		RDT_SpectreLensIndex.OnInit();
	}
}
