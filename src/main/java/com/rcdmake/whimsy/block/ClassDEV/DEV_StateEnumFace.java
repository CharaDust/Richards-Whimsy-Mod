package com.rcdmake.whimsy.block.ClassDEV;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * 开发_朝向方块（{@code dev_block_state_face}）—— 由旧项目 {@code DEV_StateEnumFace} 迁来。
 *
 * <p><b>多方块状态</b>：朝向属性 {@code facing}（6 个方向）。
 * blockstate 由人工手写，本类不在 {@code blockStateModels()} 里声明。
 *
 * <p>注意：本类**没有**覆写放置逻辑（旧代码也没有），所以默认恒为
 * {@code NORTH}；需要「放置时按玩家朝向」的话得自己加
 * {@code getStateForPlacement}。
 *
 * <p>改名对照：{@code Properties.FACING} → {@code BlockStateProperties.FACING}；
 * {@code AbstractBlock.Settings} → {@code BlockBehaviour.Properties}。
 */
public class DEV_StateEnumFace extends Block {

	public static Block DEV_BLOCK_STATE_FACE;

	public DEV_StateEnumFace(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState()
				.setValue(BlockStateProperties.FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.FACING);
	}

	public static void OnInit() {
		DEV_BLOCK_STATE_FACE = ModRegister.registerBlockWithItem("dev_block_state_face",
				new DEV_StateEnumFace(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
						.setId(ModRegister.blockKey("dev_block_state_face"))));
	}
}
