package com.rcdmake.whimsy.block.paradigm;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * 范式方块 V1（{@code paradigm_block_v1}）—— 方块类「蓝图叠层」范式的样板，**不注册、不参与编译、不进 jar**。
 *
 * <p>本文件是 {@code doc/wiki/example/blockv1.md} 的落地样板：抄它 → 改类名/模块前缀/注册名/工厂名 →
 * 按文档 §5 勾完 7 处登记点。{@code build.gradle} 的 {@code sourceSets} 排除了 {@code block/paradigm/**}
 * （只作样板、不进编译）；要校验它是否仍与当前 MC 版本 API 一致，临时注释掉那行 exclude 再 {@code gradlew build} 即可。
 *
 * <h2>层 0 ｜ 契约栏（类注释）</h2>
 * <ul>
 *   <li><b>中文名</b>：范式方块（样板）　<b>英文名</b>：Paradigm Block（template）</li>
 *   <li><b>注册名</b>：{@code paradigm_block_v1}　<b>模块</b>：—（范式样板，不属于任何模块）</li>
 *   <li><b>wiki 页</b>：{@code doc/wiki/example/blockv1.md}</li>
 *   <li><b>父级</b>：石头（{@code Blocks.STONE}）</li>
 *   <li><b>启用的可选层</b>：无（刻意只留强制层；可选层示例都在下面的注释里，用哪个抄哪个）</li>
 *   <li><b>登记点 checklist（7 处，逐条勾）</b>：
 *       <ol>
 *         <li>☐/{@code X} 主类 {@code RichardsWhimsyMod.onInitialize()} 加一条 {@code OnInit()}</li>
 *         <li>☐/{@code X} {@code datagen/BlockStateGenerator.collectAll()}（单状态才声明）</li>
 *         <li>☐/{@code X} {@code datagen/ItemModelGenerator.ITEM_MODEL_OVERRIDES}（物品模型与方块不同才需要）</li>
 *         <li>☐/{@code X} {@code datagen/Class<模块>/<模块>_LangEntries}（方块 + 方块物品两条）</li>
 *         <li>☐/{@code X} {@code item/<模块>_CreativeTabs} 的包含清单</li>
 *         <li>☐/{@code X} 资源：贴图 / 模型 / blockstate</li>
 *         <li>☐/{@code X} 配方 / 战利品表 / 标签（全项目暂缓）</li>
 *       </ol></li>
 * </ul>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照（样板用到的部分）</h2>
 * <table border="1">
 *   <tr><th>Yarn（旧）</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code FabricBlockSettings.copyOf(Blocks.STONE)}</td>
 *       <td>{@code BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)}</td><td>「父级 = 石头」</td></tr>
 *   <tr><td>{@code Registry.register(Registries.BLOCK, new Identifier(ns, path), block)}</td>
 *       <td>{@code ModRegister.registerBlockWithItem(id, block)}</td>
 *       <td>项目自建的注册入口；方块物品在同一条里注册，且 26.2 要求 {@code Properties.setId(key)}</td></tr>
 * </table>
 *
 * <h2>可选层示例（用哪个抄哪个，别留空壳）</h2>
 * <pre>{@code
 * // ── 层 3 状态属性字段（多状态时）────────────────────────────
 * public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
 *
 * // ── 层 6 家族/变体（切割类建筑方块：白拿原版机制与 codec）─────
 * public class BHR_MarbleStair extends StairBlock {
 *     public BHR_MarbleStair(BlockState baseState, BlockBehaviour.Properties properties) { super(baseState, properties); }
 * }
 *
 * // ── 层 7 类型序列化 codec（可选；构造参数无法从 Properties 推导且父类不提供时才要）──
 * public static final MapCodec<X> CODEC = ...;        // helper 名待核实，见 blockv1.md §8
 * @Override protected MapCodec<? extends X> codec() { return CODEC; }
 *
 * // ── 层 8 状态模式与生命周期 ───────────────────────────────
 * @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(AXIS); }
 * @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
 *     return defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
 * }
 * @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) { ... }
 * @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) { ... }
 *
 * // ── 层 9 交互与判定 ──────────────────────────────────────
 * @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) { ... }
 * @Override protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
 *         InsideBlockEffectApplier effectApplier, boolean inside) { entity.makeStuckInBlock(state, new Vec3(0.85, 0.85, 0.85)); }
 * @Override protected boolean canBeReplaced(BlockState state, Fluid fluid) { return false; }
 *
 * // ── 层 10 形状与外观 ─────────────────────────────────────
 * @Override protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { ... }
 * @Override protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) { return 1.0F; }
 * @Override protected boolean propagatesSkylightDown(BlockState state) { return true; }
 * @Override protected boolean skipRendering(BlockState state, BlockState neighbor, Direction direction) { ... }
 *
 * // ── 层 12 额外注册表登记（可燃性；颜色/渲染层属于层 C，写客户端入口）──
 * FlammableBlockRegistry.getDefaultInstance().add(PARADIGM_BLOCK_V1, 30, 60);
 * }</pre>
 */
public class ParadigmBlockV1 extends Block {

	// ─────────────────────────────────────────────────────────────────────
	// 层 1 ｜ 规格常量（可选）：只用静态常量，构造期会读到
	// ─────────────────────────────────────────────────────────────────────

	/** 示例：计划刻延迟（Tick）。外部要引用才升为 {@code public}。 */
	private static final int EXAMPLE_DELAY_TICKS = 200;

	// ─────────────────────────────────────────────────────────────────────
	// 层 2 ｜ 方块声明：实例在层 12 赋值，之前为 null；换色变体在此并列
	// ─────────────────────────────────────────────────────────────────────

	/** 范式方块本体。 */
	public static Block PARADIGM_BLOCK_V1;

	// ─────────────────────────────────────────────────────────────────────
	// 层 3 ｜ 状态属性字段（可选，仅多状态）：静态初始化必须早于任何实例
	//        示例见类注释「层 3」
	// ─────────────────────────────────────────────────────────────────────

	// ─────────────────────────────────────────────────────────────────────
	// 层 4 ｜ 属性工厂（运行时最先执行：它是构造器的实参）
	//        只允许依赖静态常量/Property，不得依赖实例字段
	// ─────────────────────────────────────────────────────────────────────

	/** 范式方块的属性：完整复制石头（要改哪一项就在这一行下面按需追加并注明 wiki 行）。 */
	public static BlockBehaviour.Properties paradigmBlockV1Properties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
	}

	// ─────────────────────────────────────────────────────────────────────
	// 层 5 ｜ 构造器（运行时后执行）：装配 + 多状态时 registerDefaultState
	// ─────────────────────────────────────────────────────────────────────

	public ParadigmBlockV1(BlockBehaviour.Properties properties) {
		super(properties);
		// 多状态方块在此给初值（必须在 super 之后；状态模式已在 super 内部定死）：
		// registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.X));
	}

	// ─────────────────────────────────────────────────────────────────────
	// 层 6 ｜ 家族/变体声明（可选）：用 extends 原版家族类 + 家族构造参数表达
	//        —— 见类注释「层 6」。家族关系写在类声明与构造器里，不在这里新开方法
	// ─────────────────────────────────────────────────────────────────────

	// ─────────────────────────────────────────────────────────────────────
	// 层 7 ｜ 类型序列化 codec（可选）：继承 Block 的普通方块不需要
	//        —— 见类注释「层 7」
	// ─────────────────────────────────────────────────────────────────────

	// ─────────────────────────────────────────────────────────────────────
	// 层 8 ｜ 状态模式与生命周期（可选）：createBlockStateDefinition /
	//        getStateForPlacement / setPlacedBy / tick / updateShape …
	//        —— 示例见类注释「层 8」
	// ─────────────────────────────────────────────────────────────────────

	// ─────────────────────────────────────────────────────────────────────
	// 层 9 ｜ 交互与判定（可选）：useItemOn / useWithoutItem / entityInside /
	//        fallOn / stepOn / onProjectileHit；canBeReplaced / isPathfindable …
	//        —— 示例见类注释「层 9」
	// ─────────────────────────────────────────────────────────────────────

	// ─────────────────────────────────────────────────────────────────────
	// 层 10 ｜ 形状与外观（可选）：getCollisionShape / getShadeBrightness /
	//         propagatesSkylightDown / skipRendering …
	//         —— 示例见类注释「层 10」
	// ─────────────────────────────────────────────────────────────────────

	// ─────────────────────────────────────────────────────────────────────
	// 层 11 ｜ 未定扩展点（注册前预留区，可选）：现在无法预测归属的钩子占位
	//         // TODO(预留)：……
	//         —— 一旦确定归属，就搬到对应层，别让它烂在这里
	// ─────────────────────────────────────────────────────────────────────

	// ─────────────────────────────────────────────────────────────────────
	// 层 12 ｜ 初始化：注册方块与方块物品 + 额外注册表登记（脚注写在这里）
	// ─────────────────────────────────────────────────────────────────────

	public static void OnInit() {
		PARADIGM_BLOCK_V1 = ModRegister.registerBlockWithItem("paradigm_block_v1",
				new ParadigmBlockV1(paradigmBlockV1Properties()
						.setId(ModRegister.blockKey("paradigm_block_v1"))));

		// 额外注册表登记（可选，示例）：
		// FlammableBlockRegistry.getDefaultInstance().add(PARADIGM_BLOCK_V1, 30, 60);
	}

	// ─────────────────────────────────────────────────────────────────────
	// 层 C ｜ 客户端接线（可选，独立类 RichardsWhimsyModClient）：
	//        BlockColorRegistry.register(List.of(BlockTintSources.water()), PARADIGM_BLOCK_V1);
	//        绝不写进本类
	// ─────────────────────────────────────────────────────────────────────

	// ─────────────────────────────────────────────────────────────────────
	// 层 M ｜ 定制 Mixin（可选，独立类 + 三处登记）：仅当阶梯前四级全部无解
	// ─────────────────────────────────────────────────────────────────────

	/** 示例常量用掉占位，避免 IDE 把未使用常量标黄 —— 抄用时连它一起删。 */
	static int exampleDelayTicks() {
		return EXAMPLE_DELAY_TICKS;
	}
}
