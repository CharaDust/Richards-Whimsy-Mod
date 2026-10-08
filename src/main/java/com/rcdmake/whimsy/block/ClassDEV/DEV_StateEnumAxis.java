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
 */
public class DEV_StateEnumAxis extends Block {

	public static Block DEV_BLOCK_STATE_AXIS;

	public DEV_StateEnumAxis(BlockBehaviour.Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState()
				.setValue(BlockStateProperties.AXIS, Direction.Axis.X));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.AXIS);
	}

	/** 放置朝向逻辑：按点击面的轴来决定。 */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState()
				.setValue(BlockStateProperties.AXIS, context.getClickedFace().getAxis());
	}

	public static void OnInit() {
		DEV_BLOCK_STATE_AXIS = ModRegister.registerBlockWithItem("dev_block_state_axis",
				new DEV_StateEnumAxis(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
						.setId(ModRegister.blockKey("dev_block_state_axis"))));
	}
}
