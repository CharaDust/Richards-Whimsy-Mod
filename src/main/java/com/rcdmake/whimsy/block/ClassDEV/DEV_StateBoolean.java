package com.rcdmake.whimsy.block.ClassDEV;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * 开发_布尔方块（{@code dev_block_state_boolean}）—— 由旧项目 {@code DEV_StateBoolean} 迁来。
 *
 * <p><b>多方块状态样例</b>：带一个布尔属性 {@code dev_boolean}。
 * 因为 variants 有 2 种组合，blockstate 由人工手写，本类**不**在
 * {@code blockStateModels()} 里声明。
 *
 * <h2>1.20.4 (Yarn) → 26.2 (官方名) 的对应</h2>
 * <table border="1">
 *   <caption>状态相关 API 改名</caption>
 *   <tr><th>Yarn</th><th>26.2 官方名</th></tr>
 *   <tr><td>{@code StateManager.Builder}</td><td>{@code StateDefinition.Builder}</td></tr>
 *   <tr><td>{@code appendProperties(...)}</td><td>{@code createBlockStateDefinition(...)}</td></tr>
 *   <tr><td>{@code BooleanProperty.of(name)}</td><td>{@code BooleanProperty.create(name)}</td></tr>
 *   <tr><td>{@code setDefaultState(...)}</td><td>{@code registerDefaultState(...)}</td></tr>
 *   <tr><td>{@code FabricBlockSettings.copyOf(Blocks.STONE)}</td><td>{@code BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)}</td></tr>
 * </table>
 *
 * <p>「先声明属性、再构造方块、最后在注册时引用」这个三步顺序与旧代码一致 ——
 * 属性是静态字段，方块构造时要把默认值写进去，所以属性必须先存在。
 *
 * <h2>范式 V1 契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code dev_block_state_boolean}　<b>模块</b> DEV　<b>父级</b> 石头</li>
 *   <li><b>启用的可选层</b>：层 3 状态属性（{@code DEV_BOOLEAN}）、层 8 状态模式（{@code createBlockStateDefinition}）</li>
 *   <li><b>跳过的可选层</b>：层 1 常量、层 6 家族、层 7 codec、层 9 交互与判定、层 10 形状与外观、
 *       层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>能力阶梯</b>：停在第 2 级「覆写本类方法」—— 多状态必须覆写 {@code createBlockStateDefinition}，
 *       没有别的声明能替代</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}　②☒ <b>不在</b> {@code blockStateModels()} 里声明
 *       （2 种 variants，blockstate 手写）　③☒ 物品模型无例外　④☑ 中英 {@code DEV_LangEntries}
 *       　⑤☑ {@code DEV_CreativeTabs}　⑥☑ 贴图 / 模型　⑦☐ 配方与战利品表（wiki：获取方式「无」）</li>
 * </ul>
 */
public class DEV_StateBoolean extends Block {

	/** 方块本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Block DEV_BLOCK_STATE_BOOLEAN;

	/** 状态属性字段（层 3）：布尔值，序列化名 {@code dev_boolean}（属性名不是注册名）。 */
	public static final BooleanProperty DEV_BOOLEAN = BooleanProperty.create("dev_boolean");

	/** 布尔的属性（层 4）：wiki「构造」表逐行都是「父级」，故完整复制石头。 */
	public static BlockBehaviour.Properties stateBooleanProperties() {
		return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
	}

	/**
	 * 构造器（层 5；运行时排在属性工厂之后）：装配 + 给状态初值。
	 * <p>属性来自上面的 {@link #stateBooleanProperties()}；{@code registerDefaultState} 只能写在
	 * {@code super(...)} 之后（状态模式已在 {@code super} 内部定死）。
	 */
	public DEV_StateBoolean(BlockBehaviour.Properties properties) {
		super(properties);
		// 默认状态：dev_boolean = false
		registerDefaultState(defaultBlockState().setValue(DEV_BOOLEAN, false));
	}

	/** 状态模式（层 8）：声明本方块有 {@code dev_boolean} 这一个状态维度。 */
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(DEV_BOOLEAN);
	}

	/** 注册（层 12）：id 只在注册处出现一次（{@code Properties.setId} 是 26.2 的硬性要求）。 */
	public static void OnInit() {
		DEV_BLOCK_STATE_BOOLEAN = ModRegister.registerBlockWithItem("dev_block_state_boolean",
				new DEV_StateBoolean(stateBooleanProperties()
						.setId(ModRegister.blockKey("dev_block_state_boolean"))));
	}
}
