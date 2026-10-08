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
 * 祖母绿方块（{@code bhr_gem_emerald}）—— 由旧项目 {@code BHR_Gems} 拆分迁来。
 *
 * <p>wiki 规格（{@code doc/wiki/Blocks/ClassBlockHeadsRebon/BHR_GemEmerald.md}）：
 * <ul>
 *   <li>父级：玻璃；硬度 / 爆炸抗性 / 阻力 / 各类乘数逐行都是「父级」</li>
 *   <li>窒息方块 FALSE、视野阻挡 FALSE、光线阻挡 FALSE、表面生成生物 FALSE、概念性固体方块 FALSE</li>
 *   <li>声音组 SMALL_AMETHYST_BUD；亮度 <b>9</b>（实际发光）；碰撞「无」</li>
 *   <li>渲染：依据纹理 Alpha 通道透明；改写方法：放置时根据贴合面决定依附面与朝向</li>
 *   <li>方块状态：BLOCK_FACE（FLOOR / CEILING / WALL）+ FACING（水平朝向）</li>
 *   <li>备注：英文显示名沿用旧语言文件的 {@code Canutillos}</li>
 * </ul>
 *
 * <p><b>共享机制见 {@link BHR_GemAmethyst}</b>：层 6 继承 {@code FaceAttachedHorizontalDirectionalBlock}
 * 的理由（按钮同族的放置 + 依附规则）、层 7 必须补 codec、以及「只写 {@code noCollision()}
 * 不写 {@code forceSolidOn()}」这条裁定（不阻挡水流 / 告示牌贴不上）。
 * <p>贴图那一坑（方块模型的 sprite 必须落在<b>方块图集</b>里，否则模型会被 26.2 整块拒收 → 世界里显示成
 * 一整块紫黑方块）已在本批修掉：方块侧用 {@code block/bhr/gem_emerald}、物品侧保留
 * {@code item/bhr/gem_emerald}，来龙去脉与 1.20.4 的图集差异见 {@code BHR_GemAmethyst} 的类注释。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_gem_emerald}　<b>模块</b> BHR　<b>父级</b> 玻璃
 *       （<b>层 6 家族</b>：{@code FaceAttachedHorizontalDirectionalBlock}）</li>
 *   <li><b>启用的可选层</b>：层 6 家族、层 7 codec（父类抽象，必填）、层 8 状态模式</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 3（用父类的 {@code FACE} / {@code FACING}）、层 9 / 10、
 *       层 11 预留区、层 C 客户端（透明由贴图 Alpha 自动决定）、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 0 级「继承」</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☒ 不在 {@code BlockStateGenerator.collectAll()} 里声明
 *       （12 个状态，手写 {@code blockstates/bhr_gem_emerald.json}）　③☑ 物品模型例外（平面宝石图）
 *       　④☑ 中英 {@code BHR_LangEntries}　⑤☑ {@code BHR_CreativeTabs}
 *       　⑥☑ 贴图 {@code block/bhr/gem_emerald.png}（方块侧）+ {@code item/bhr/gem_emerald.png}（物品侧）
 *          / 模型 {@code models/block/bhr_gem_emerald.json}　⑦☐ 配方 / 战利品表 / 挖掘标签 ——「获取方式」阶段统一做</li>
 * </ul>
 */
public class BHR_GemEmerald extends FaceAttachedHorizontalDirectionalBlock {

	// ── 层 2 ｜ 方块声明 ──

	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block BHR_GEM_EMERALD;

	// ── 层 3 ｜ 状态属性字段 ──
	// FACE（序列化名 face）与 FACING（HORIZONTAL_FACING，序列化名 facing）都由家族父类提供，本类不另声明。

	// ── 层 4 ｜ 谓词助手（供属性工厂使用）──

	/** 「表面生成生物 FALSE」。 */
	private static boolean neverSpawn(BlockState state, BlockGetter level, BlockPos pos, EntityType<?> type) {
		return false;
	}

	/** 「窒息方块 / 视野阻挡 FALSE」。 */
	private static boolean never(BlockState state, BlockGetter level, BlockPos pos) {
		return false;
	}

	// ── 层 4 ｜ 属性工厂（运行时先执行）──

	/** 祖母绿方块的属性（层 4）：与紫锂辉同一条路径，只把亮度换成 9。 */
	public static BlockBehaviour.Properties gemEmeraldProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
				.sound(SoundType.SMALL_AMETHYST_BUD)
				.noOcclusion()
				// 亮度 9（实际发光）
				.lightLevel(state -> 9)
				.isValidSpawn(BHR_GemEmerald::neverSpawn)
				.isSuffocating(BHR_GemEmerald::never)
				.isViewBlocking(BHR_GemEmerald::never)
				// 碰撞「无」；刻意不写 forceSolidOn()：不阻挡水流（会被冲掉）、告示牌贴不上
				.noCollision();
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──

	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：装配 + 给状态初值（默认 WALL / NORTH）。
	 * <p>属性来自上面的 {@link #gemEmeraldProperties()}。
	 */
	public BHR_GemEmerald(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState()
				.setValue(FACE, AttachFace.WALL)
				.setValue(FACING, Direction.NORTH));
	}

	// ── 层 6 ｜ 家族/变体 ──
	// 家族关系由类声明表达：extends FaceAttachedHorizontalDirectionalBlock（按钮同族的放置与依附规则）。

	// ── 层 7 ｜ 类型序列化 codec（父类是抽象类，必须补）──

	/** 类型序列化 codec（层 7）。 */
	public static final MapCodec<BHR_GemEmerald> CODEC = simpleCodec(BHR_GemEmerald::new);

	/** 把 {@link #CODEC} 交给父类的抽象方法。 */
	@Override
	protected MapCodec<? extends FaceAttachedHorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	// ── 层 8 ｜ 状态模式与生命周期 ──

	/** 状态模式（层 8）：父类自己不声明状态维度，所以这里必须把 {@code facing} / {@code face} 补上。 */
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, FACE);
	}

	// ── 层 12 ｜ 初始化 ──

	/** 注册（层 12）。 */
	public static void OnInit() {
		BHR_GEM_EMERALD = ModRegister.registerBlockWithItem("bhr_gem_emerald",
				new BHR_GemEmerald(gemEmeraldProperties()
						.setId(ModRegister.blockKey("bhr_gem_emerald"))));
	}
}
