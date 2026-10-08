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
 */
public class DEV_StateInt extends Block {

	/** 方块状态属性：整数，范围 0~9。 */
	public static final IntegerProperty DEV_INT = IntegerProperty.create("dev_int", 0, 9);

	public static Block DEV_BLOCK_STATE_INT;

	public DEV_StateInt(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(DEV_INT, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(DEV_INT);
	}

	public static void OnInit() {
		DEV_BLOCK_STATE_INT = ModRegister.registerBlockWithItem("dev_block_state_int",
				new DEV_StateInt(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
						.setId(ModRegister.blockKey("dev_block_state_int"))));
	}
}
