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
 */
public class DEV_StateBoolean extends Block {

	/** 方块状态属性：布尔值，注册名 {@code dev_boolean}（属性名不是注册名）。 */
	public static final BooleanProperty DEV_BOOLEAN = BooleanProperty.create("dev_boolean");

	public static Block DEV_BLOCK_STATE_BOOLEAN;

	/** 子类必须暴露这个构造器：方块要带自己的状态默认值构造。 */
	public DEV_StateBoolean(BlockBehaviour.Properties properties) {
		super(properties);
		// 默认状态：dev_boolean = false
		registerDefaultState(defaultBlockState().setValue(DEV_BOOLEAN, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(DEV_BOOLEAN);
	}

	public static void OnInit() {
		DEV_BLOCK_STATE_BOOLEAN = ModRegister.registerBlockWithItem("dev_block_state_boolean",
				new DEV_StateBoolean(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
						.setId(ModRegister.blockKey("dev_block_state_boolean"))));
	}
}
