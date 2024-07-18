package com.rcdmake.whimsy.block.ClassDEV;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import static net.minecraft.state.property.Properties.AXIS;

public class DEV_StateEnumAxis extends Block {

    // 创建方块，使用构造函数类型
    public static final Block DEV_BLOCK_STATE_AXIS  = new DEV_StateEnumAxis(FabricBlockSettings.copyOf(Blocks.STONE));
    public DEV_StateEnumAxis(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(AXIS, Direction.Axis.X));
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    // 方块放置朝向逻辑
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(AXIS, ctx.getSide().getAxis());
    }

    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block){
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }
    public static void OnInit(){
        register_Block_and_BlockItem("dev_block_state_axis", DEV_BLOCK_STATE_AXIS);
    }
}
