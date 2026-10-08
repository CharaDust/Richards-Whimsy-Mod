package com.rcdmake.whimsy.block.ClassDEV;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * 开发_轴方块（{@code dev_block_state_axis}）—— 由旧项目 {@code DEV_StateEnumAxis} 迁来。
 *
 * <p><b>多方块状态</b>：轴属性 {@code axis}（x / y / z），放置时按玩家朝向决定。
 * blockstate 由人工手写，本类不在 {@code blockStateModels()} 里声明。
 *
 * <p>改名对照（Yarn → 26.2 官方名）：
 * {@code Properties.AXIS} → {@code BlockStateProperties.AXIS}；
 * {@code ItemPlacementContext} → {@code BlockPlaceContext}；
 * {@code getPlacementState(ctx)} → {@code getStateForPlacement(ctx)}；
 * {@code ctx.getSide()} → {@code ctx.getClickedFace()}；
 * {@code Direction.Axis} → {@code Direction.Axis}（同名，包从 {@code util.math} 变为 {@code core}）。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code dev_block_state_axis}　<b>模块</b> DEV　<b>父级</b> 石头</li>
 *   <li><b>启用的可选层</b>：层 3 状态属性（{@code AXIS}）、层 8 状态模式与生命周期
 *       （{@code createBlockStateDefinition} + {@code getStateForPlacement} 按贴合面定轴）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 6 家族（这里刻意<b>不</b>继承原版 {@code RotatedPillarBlock}，
 *       因为本方块是「状态样例」，要让学生看见 {@code createBlockStateDefinition} 与放置逻辑本身；
 *       真要省事就照 {@code RDT_TreeLogSpectre} 那样继承）、层 7 codec、层 9 交互与判定、
 *       层 10 形状与外观、层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 2 级「覆写本类方法」—— 换成第 0 级（继承父类）也能实现，见上</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☒ <b>不在</b> {@code blockStateModels()} 里声明
 *       （3 种轴向，blockstate 手写）　③☒ 物品模型无例外　④☑ 中英 {@code DEV_LangEntries}
 *       　⑤☑ {@code DEV_CreativeTabs}　⑥☑ 贴图 / 模型　⑦☐ 配方与战利品表（wiki：获取方式「无」）</li>
 * </ul>
 */
public class DEV_StateEnumAxis extends Block {

	// ── 层 2 ｜ 方块声明 ──
	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block DEV_BLOCK_STATE_AXIS;

	// ── 层 3 ｜ 状态属性字段 ──
	/**
	 * 状态属性字段（层 3）：轴向，直接取原版共享常量。
	 * <p>序列化名是 {@code axis}（原版 {@code BlockStateProperties.AXIS} 的名字），手写的 blockstate 用的就是它。
	 */
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

	// ── 层 4 ｜ 属性工厂（运行时先执行）──
	/** 轴方块的属性（层 4）：wiki「构造」表逐行都是「父级」，故完整复制石头。 */
	public static BlockBehaviour.Properties stateEnumAxisProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
	}

	// ── 层 5 ｜ 构造器（运行时后执行）──
	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：装配 + 给状态初值。
	 * <p>属性来自上面的 {@link #stateEnumAxisProperties()}；{@code registerDefaultState} 只能写在
	 * {@code super(...)} 之后。
	 */
	public DEV_StateEnumAxis(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState()
				.setValue(AXIS, Direction.Axis.X));
	}

	/** 状态模式（层 8）：声明本方块有 {@code axis} 这一个状态维度。 */
	@Override
	// ── 层 8 ｜ 状态模式与生命周期 ──
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}

	/** 放置逻辑（层 8）：按点击面的轴来决定。 */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState()
				.setValue(AXIS, context.getClickedFace().getAxis());
	}

	// ── 层 12 ｜ 初始化 ──
	/** 注册（层 12）。 */
	public static void OnInit() {
		DEV_BLOCK_STATE_AXIS = ModRegister.registerBlockWithItem("dev_block_state_axis",
				new DEV_StateEnumAxis(stateEnumAxisProperties()
						.setId(ModRegister.blockKey("dev_block_state_axis"))));
	}
}
