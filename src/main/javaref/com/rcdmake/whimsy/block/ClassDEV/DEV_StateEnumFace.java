package com.rcdmake.whimsy.block.ClassDEV;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import static net.minecraft.state.property.Properties.FACING;

public class DEV_StateEnumFace extends Block{

    // 创建方块，使用构造函数类型
    public static final Block DEV_BLOCK_STATE_FACE = new DEV_StateEnumFace(FabricBlockSettings.copyOf(Blocks.STONE));
    public DEV_StateEnumFace(AbstractBlock.Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block){
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }
    public static void OnInit(){
        register_Block_and_BlockItem("dev_block_state_face", DEV_BLOCK_STATE_FACE);
    }

}
