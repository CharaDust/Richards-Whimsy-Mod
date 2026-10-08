package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.mojang.serialization.MapCodec;
import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;

/**
 * 紫锂辉方块（{@code bhr_gem_amethyst}）—— 由旧项目 {@code BHR_Gems} 拆分迁来（一类五方块 → 一类一方块）。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassBlockHeadsRebon/BHR_GemAmethyst.md}）：
 * <ul>
 *   <li>父级：玻璃；硬度 / 爆炸抗性 / 阻力 / 各类乘数逐行都是「父级」</li>
 *   <li>窒息方块 FALSE、视野阻挡 FALSE、光线阻挡 FALSE、表面生成生物 FALSE、概念性固体方块 FALSE</li>
 *   <li>声音组 SMALL_AMETHYST_BUD；亮度 <b>5</b>（实际发光）；碰撞「无」</li>
 *   <li>渲染：依据纹理 Alpha 通道透明；改写方法：放置时根据贴合面决定依附面与朝向</li>
 *   <li>方块状态：BLOCK_FACE（FLOOR / CEILING / WALL）+ FACING（水平朝向）</li>
 *   <li>备注：显示名原为「三方紫晶」，现改为「紫锂辉」（英文 {@code Kunzite}）；注册名与资源名保持
 *       {@code amethyst} 不变 —— 致敬《The Blockheads》</li>
 * </ul>
 *
 * <h2>层 6 ｜ 家族：{@code extends FaceAttachedHorizontalDirectionalBlock}（= 按钮那一族）</h2>
 * <b>2026-10 裁定</b>：宝石在观感上就是「贴在方块表面的一小颗宝石」（像按钮那样一小坨），
 * 所以它的<b>放置规则与依附规则应当和按钮完全一致</b>。于是本类改为继承
 * {@link FaceAttachedHorizontalDirectionalBlock}（原版按钮 / 拉杆的公共父类），一次白拿三件事：
 * <ul>
 *   <li>{@code FACE}（{@code ATTACH_FACE}，序列化名 {@code face}）与 {@code FACING}
 *       （{@code HORIZONTAL_FACING}，序列化名 {@code facing}）两个状态属性；</li>
 *   <li>{@link FaceAttachedHorizontalDirectionalBlock#getStateForPlacement}：按玩家视线方向依次试
 *       「落地面 / 吸附面 / 侧墙」，<b>试不出合法依附就不让放</b>（返回 {@code null}）；</li>
 *   <li>{@link FaceAttachedHorizontalDirectionalBlock#canSurvive} + {@code updateShape}：
 *       <b>只能贴在坚固面（固体方块）上</b>，背后的支撑方块一碎，宝石自己变空气。</li>
 * </ul>
 * 本类因此<b>一行放置 / 依附代码都不用写</b>（层 8 只剩一条 {@code createBlockStateDefinition}，
 * 且那是因为父类自己不声明状态维度 —— 见层 8）。
 * <p>⚠️ 与「上一版」的差异：此前本类刻意<b>不</b>继承这个父类，理由是「wiki 只字未提 canSurvive /
 * updateShape，继承会白拿没要的机制」。该判断已被上述裁定推翻，{@code extends Block} 那套自写放置逻辑
 * 一并删除。（这也回答了范式 §10.2 第 4 条「家族类要不要必看 codec」：<b>父类有抽象 codec 时，
 * 必须自己补一个</b>，见层 7。）
 *
 * <h2>层 7 ｜ codec 是硬性的：父类是抽象类</h2>
 * {@code FaceAttachedHorizontalDirectionalBlock} 把 {@code codec()} 声明成抽象方法，
 * 所以继承它的第一件事就是补一个 {@code MapCodec}。本类用 {@code Block.simpleCodec(构造器)}
 * （26.2 的简写，原版 {@code CropBlock} 等也是这么写的）—— 这也是 V1 层 7 那格唯一的必填场景。
 *
 * <h2>⭐ 「概念性固体方块 FALSE」在 26.2 的落地：只写 {@code noCollision()}，<b>不</b>写 {@code forceSolidOn()}</h2>
 * 旧写法用 {@code solidBlock(Blocks::never)} 明确置「概念性固体 = false」；26.2 删掉了这个 builder，
 * {@code legacySolid} 改由 {@code calculateSolid()} 读<b>缓存下来的碰撞形状</b>反推。本方块是
 * {@code noCollision()}（碰撞形状恒为空），并且<b>刻意不写</b> {@code forceSolidOn()}，
 * 于是 {@code blocksMotion()} 为 false —— 这正是「概念性固体 FALSE」的当代等价写法：
 * <ul>
 *   <li>实体穿得过（wiki「碰撞：无」）；</li>
 *   <li><b>不阻挡水流</b>（{@code FlowingFluid.canHoldAnyFluid} 里 {@code !state.blocksMotion()} → true，
 *       水可以流进这一格，宝石被冲掉）—— 与「贴在表面的一小颗」这个观感一致；</li>
 *   <li><b>告示牌贴不上</b>（{@code isFaceSturdy} 要求非空碰撞形状）。</li>
 * </ul>
 * 后两条是<b>刻意保留的旧观感</b>。范式 §7 硬约束 4 现在写的是「<b>对于能使实体透过的方块，如果不希望
 * 导致被水冲掉</b>，请遵循以下两条」—— 本类正是那种<b>接受</b>被水冲掉的方块，所以两条都不适用；
 * 要求不被冲掉的样板见 {@code DEV_GhostBlock}、{@code RDT_GlassLapis}。判据写在这里的层 0 契约栏，
 * 而不是靠猜。
 *
 * <h2>方块状态与手写 blockstate</h2>
 * {@code FACE}(3) × {@code FACING}(4) = <b>12 个状态</b>，手写的
 * {@code blockstates/bhr_gem_amethyst.json} 里那 12 个 variants 正好写满全部状态
 * （旧项目那份是六向 {@code FACING} 时代的产物，靠放置逻辑把 up / down 归一成 north / south 才没露馅；
 * 属性换成四向之后，覆盖关系不再有缺口）。
 *
 * <h2>⭐ 贴图必须落在方块图集里（2026-10 已修，是一处 26.2 迁移坑）</h2>
 * 本方块模型的贴图<b>不能</b>指 {@code textures/item/...}。26.2 的 {@code SimpleModelWrapper.bake()}
 * 会检查「模型每个面用到的 sprite 是否都在<b>方块图集</b>（{@code TextureAtlas.LOCATION_BLOCKS}）里」，
 * 不在就<b>整块模型拒收</b>并打日志
 * {@code Rejecting block model ..., since it contains sprites from outside of supported atlas}，
 * 结果是世界里显示成缺失模型（一整块紫黑方块）—— 与「贴图缺失（宝石形状还在、只是糊上紫黑格）」
 * 是两种<b>不同</b>的症状，排查时别混。
 * <p>根因：1.20.4 的方块图集 {@code atlases/blocks.json} 同时收 {@code block/} 与 {@code item/}，
 * 26.2 只收 {@code block/}（{@code item/} 搬去了独立的 {@code atlases/items.json}）。
 * <p><b>现行做法（贴图复制两份，各归各的图集）</b>：
 * <ul>
 *   <li>方块侧：{@code textures/block/bhr/gem_amethyst.png} —— 本模型 {@code "0"} / {@code "particle"} 指它；</li>
 *   <li>物品侧：{@code textures/item/bhr/gem_amethyst.png} —— {@code models/item/bhr_gem_amethyst.json}
 *       的 {@code layer0} 指它（物品模型允许物品图集、也允许方块图集，只禁「一个模型混用两张图集」，
 *       见 {@code CuboidItemModelWrapper.validateAtlasUsage}；保留 item 侧那份是沿用旧结构）。</li>
 * </ul>
 * 另有 3 个模型（蓝宝石 / 红刚玉 / 宝石钻石）带 Blockbench 留下的 {@code "texture": "#missing"} 占位面，
 * 按「不删面、改赋贴图」的原则统一改成了 {@code "#0"}（占位通常是导出器的锅，删面反而会缺面）。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_gem_amethyst}　<b>模块</b> BHR　<b>父级</b> 玻璃
 *       （<b>层 6 家族</b>：继承原版 {@code FaceAttachedHorizontalDirectionalBlock}，与按钮同族）</li>
 *   <li><b>启用的可选层</b>：层 6 家族、层 7 codec（父类抽象，必填）、层 8 状态模式
 *       （{@code createBlockStateDefinition}）—— 放置 / 依附 / 掉落全部由父类提供</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3 状态属性（<b>用父类的 {@code FACE} / {@code FACING}，本类不另声明</b>）、
 *       层 9 / 10（一个覆写点都没有，全靠层 4 的声明）、层 11 预留区、<b>层 C 客户端</b>
 *       （26.1 起透明由贴图 Alpha 自动决定）、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 0 级「继承」—— 放置与依附规则白拿，本类只剩属性声明 +
 *       一条状态模式覆写（父类自己不声明状态维度，这一条必须写）</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☒ <b>不在</b> {@code BlockStateGenerator.collectAll()} 里声明
 *       （12 个状态 / 12 个 variants，blockstate <b>手写</b>）　③☑ <b>物品模型例外</b>
 *       （{@code ItemModelGenerator.ITEM_MODEL_OVERRIDES}：物品图标是平面宝石图
 *       {@code item/bhr_gem_amethyst}，不是方块那套 3D elements 模型）
 *       　④☑ 中英 {@code BHR_LangEntries}　⑤☑ {@code BHR_CreativeTabs}（宝石组第一个）
 *       　⑥☑ 贴图 {@code block/bhr/gem_amethyst.png}（方块侧）+ {@code item/bhr/gem_amethyst.png}（物品侧）
 *          / 模型 {@code models/block/bhr_gem_amethyst.json}（Blockbench 手写 elements）
 *       　⑦☐ 配方 / 战利品表 / 挖掘标签 ——「获取方式」阶段统一做</li>
 * </ul>
 */
public class BHR_GemAmethyst extends FaceAttachedHorizontalDirectionalBlock {

	// ── 层 2 ｜ 方块声明 ──

	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_GEM_AMETHYST;

	// ── 层 3 ｜ 状态属性字段 ──
	// 两个状态属性都由家族父类提供，本类不再另声明：
	//   FACE   = FaceAttachedHorizontalDirectionalBlock.FACE（= BlockStateProperties.ATTACH_FACE，序列化名 face，值 floor/wall/ceiling）
	//   FACING = HorizontalDirectionalBlock.FACING         （= BlockStateProperties.HORIZONTAL_FACING，序列化名 facing，四向）
	// 手写的 blockstate 用的就是这两个名字（旧 Yarn 的 Properties.BLOCK_FACE 序列化名同样是 face）。

	// ── 层 4 ｜ 谓词助手（供属性工厂使用）──

	/** 「表面生成生物 FALSE」：谓词多出实体类型参数，所以用四参助手而不是 {@code Blocks::never}。 */
	private static boolean neverSpawn(BlockState state, BlockGetter level, BlockPos pos, EntityType<?> type) {
		return false;
	}

	/** 「窒息方块 / 视野阻挡 FALSE」：三参谓词助手。 */
	private static boolean never(BlockState state, BlockGetter level, BlockPos pos) {
		return false;
	}

	// ── 层 4 ｜ 属性工厂（运行时先执行）──

	/**
	 * 紫锂辉方块的属性（层 4）：wiki「构造」表逐行落地。
	 * <p>{@code noCollision()} 单独出现、<b>不配</b> {@code forceSolidOn()} —— 这是「概念性固体 FALSE」的
	 * 当代写法，代价（不阻挡水流、告示牌贴不上）是刻意保留的，理由见类注释。
	 */
	public static BlockBehaviour.Properties gemAmethystProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
				// 声音组 SMALL_AMETHYST_BUD（wiki 明写，覆盖父级玻璃的 GLASS）
				.sound(SoundType.SMALL_AMETHYST_BUD)
				// 光线阻挡 FALSE
				.noOcclusion()
				// 亮度 5（实际发光）
				.lightLevel(state -> 5)
				// 表面生成生物 FALSE
				.isValidSpawn(BHR_GemAmethyst::neverSpawn)
				// 窒息方块 FALSE / 视野阻挡 FALSE
				.isSuffocating(BHR_GemAmethyst::never)
				.isViewBlocking(BHR_GemAmethyst::never)
				// 碰撞「无」。⚠️ 刻意不写 forceSolidOn()：blocksMotion() 保持 false
				// → 不阻挡水流（会被冲掉）、告示牌贴不上（2026-10 裁定，见类注释）
				.noCollision();
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──

	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：装配 + 给状态初值。
	 * <p>属性来自上面的 {@link #gemAmethystProperties()}；{@code registerDefaultState} 只能写在
	 * {@code super(...)} 之后（状态模式已在 {@code super} 内部定死）。
	 * <p>默认依附面取 WALL、朝向取 NORTH（与旧代码一致；实际放置时会被父类的
	 * {@code getStateForPlacement} 覆盖）。注意这两个值组成的
	 * {@code face=wall,facing=north} 必须在手写 blockstate 里有对应 variants。
	 */
	public BHR_GemAmethyst(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState()
				.setValue(FACE, AttachFace.WALL)
				.setValue(FACING, Direction.NORTH));
	}

	// ── 层 6 ｜ 家族/变体 ──
	// 家族关系由类声明表达：extends FaceAttachedHorizontalDirectionalBlock
	// （FACE + FACING + 放置 + 依附校验 + 支撑消失即掉落，全部白拿；理由见类注释「层 6」）。

	// ── 层 7 ｜ 类型序列化 codec（父类是抽象类，必须补）──

	/** 类型序列化 codec（层 7）：{@code simpleCodec} = 「构造器只吃 Properties」的简写。 */
	public static final MapCodec<BHR_GemAmethyst> CODEC = simpleCodec(BHR_GemAmethyst::new);

	/** 把上面的 {@link #CODEC} 交给父类的抽象方法。 */
	@Override
	protected MapCodec<? extends FaceAttachedHorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	// ── 层 8 ｜ 状态模式与生命周期 ──

	/**
	 * 状态模式（层 8）：声明 {@code facing} 与 {@code face} 两个状态维度。
	 * <p><b>这一条必须写</b>：{@code HorizontalDirectionalBlock} / {@code FaceAttachedHorizontalDirectionalBlock}
	 * 都<b>没有</b>覆写 {@code createBlockStateDefinition}，光继承它们的话状态定义里一个属性都不会有
	 * （原版 {@code ButtonBlock} / {@code LeverBlock} 也是各自在这里补上的）。
	 * <p>放置（{@code getStateForPlacement}）、依附校验（{@code canSurvive}）、支撑消失即掉落
	 * （{@code updateShape}）全部由父类提供，本类不覆写。
	 */
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, FACE);
	}

	// ── 层 12 ｜ 初始化 ──

	/** 注册（层 12）：id 只在注册处出现一次（{@code Properties.setId} 是 26.2 的硬性要求）。 */
	public static void OnInit() {
		BHR_GEM_AMETHYST = ModRegister.registerBlockWithItem("bhr_gem_amethyst",
				new BHR_GemAmethyst(gemAmethystProperties()
						.setId(ModRegister.blockKey("bhr_gem_amethyst"))));
	}
}
