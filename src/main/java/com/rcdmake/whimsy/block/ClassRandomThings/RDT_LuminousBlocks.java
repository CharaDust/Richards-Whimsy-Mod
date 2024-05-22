package com.rcdmake.whimsy.block.ClassRandomThings;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RDT_LuminousBlocks extends Block {
    // super
    public RDT_LuminousBlocks(Settings settings) {
        super(settings);
    }

    // 创建方块，使用构造函数类型
    public static final Block RDT_LUMINOUS_BLOCK_RED = new RDT_LuminousBlocks(FabricBlockSettings.copyOf(Blocks.GLASS).nonOpaque());
    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block){
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }

    public static void OnInit(){
        register_Block_and_BlockItem("rdt_luminous_block_red", RDT_LUMINOUS_BLOCK_RED);
    }
    public static void OnInitC(){
    }
}
