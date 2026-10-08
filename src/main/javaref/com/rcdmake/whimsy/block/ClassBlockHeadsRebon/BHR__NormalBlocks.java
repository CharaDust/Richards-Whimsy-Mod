package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class BHR__NormalBlocks extends Block {
    public BHR__NormalBlocks(Settings settings) {
        super(settings);
    }

    public static final Block BHR_LIMESTONE = new BHR__NormalBlocks(FabricBlockSettings.copyOf(Blocks.STONE));
    public static final Block BHR_LIMESTONE_BRICK = new BHR__NormalBlocks(FabricBlockSettings.copyOf(Blocks.STONE));
    public static final Block BHR_LIMESTONE_OIL_ORE = new BHR__NormalBlocks(FabricBlockSettings.copyOf(Blocks.STONE));

    public static final Block BHR_MARBLE = new BHR__NormalBlocks(FabricBlockSettings.copyOf(Blocks.STONE));
    public static final Block BHR_POLISHED_MARBLE = new BHR__NormalBlocks(FabricBlockSettings.copyOf(Blocks.STONE));

    public static final Block BHR_BEACH_SAND = new BHR__NormalBlocks(FabricBlockSettings.copyOf(Blocks.DIRT).sounds(BlockSoundGroup.SAND));

    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block){
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }

    // OnInit
    public static void OnInit(){
        register_Block_and_BlockItem("bhr_limestone", BHR_LIMESTONE);
        register_Block_and_BlockItem("bhr_limestone_brick", BHR_LIMESTONE_BRICK);
        register_Block_and_BlockItem("bhr_limestone_oil_ore", BHR_LIMESTONE_OIL_ORE);

        register_Block_and_BlockItem("bhr_marble", BHR_MARBLE);
        register_Block_and_BlockItem("bhr_polished_marble", BHR_POLISHED_MARBLE);

        register_Block_and_BlockItem("bhr_beach_sand", BHR_BEACH_SAND);
    }

}
