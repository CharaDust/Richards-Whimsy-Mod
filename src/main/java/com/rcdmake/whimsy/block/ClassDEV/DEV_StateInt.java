package com.rcdmake.whimsy.block.ClassDEV;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * 开发_整型方块（{@code dev_block_state_int}）—— 由旧项目 {@code DEV_StateInt} 迁来。
 *
 * <p><b>多方块状态</b>：整型属性 {@code dev_int}（0~9），共 10 种取值。
 * blockstate 由人工手写，本类不在 {@code blockStateModels()} 里声明。
 *
 * <p>改名对照（Yarn → 26.2 官方名）：
 * {@code IntProperty.of(name, min, max)} → {@code IntegerProperty.create(name, min, max)}；
 * {@code appendProperties} → {@code createBlockStateDefinition}；
 * {@code setDefaultState} → {@code registerDefaultState}。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code dev_block_state_int}　<b>模块</b> DEV　<b>父级</b> 石头</li>
 *   <li><b>启用的可选层</b>：层 3 状态属性（{@code DEV_INT}）、层 8 状态模式（{@code createBlockStateDefinition}）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量（0 / 9 直接写在属性里）、层 6 家族、层 7 codec、层 9 交互与判定、
 *       层 10 形状与外观、层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 2 级「覆写本类方法」</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☒ <b>不在</b> {@code blockStateModels()} 里声明
 *       （10 种取值，blockstate 手写）　③☒ 物品模型无例外　④☑ 中英 {@code DEV_LangEntries}
 *       　⑤☑ {@code DEV_CreativeTabs}　⑥☑ 贴图 / 模型　⑦☐ 配方与战利品表（wiki：获取方式「无」）</li>
 * </ul>
 */
public class DEV_StateInt extends Block {

	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block DEV_BLOCK_STATE_INT;

	/** 状态属性字段（层 3）：整数，范围 0~9。 */
	public static final IntegerProperty DEV_INT = IntegerProperty.create("dev_int", 0, 9);

	/** 整型的属性（层 4）：wiki「构造」表逐行都是「父级」，故完整复制石头。 */
	public static BlockBehaviour.Properties stateIntProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
	}

	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：装配 + 给状态初值。
	 * <p>属性来自上面的 {@link #stateIntProperties()}；{@code registerDefaultState} 只能写在
	 * {@code super(...)} 之后。
	 */
	public DEV_StateInt(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(DEV_INT, 0));
	}

	/** 状态模式（层 8）：声明本方块有 {@code dev_int} 这一个状态维度。 */
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(DEV_INT);
	}

	/** 注册（层 12）。 */
	public static void OnInit() {
		DEV_BLOCK_STATE_INT = ModRegister.registerBlockWithItem("dev_block_state_int",
				new DEV_StateInt(stateIntProperties()
						.setId(ModRegister.blockKey("dev_block_state_int"))));
	}
}
