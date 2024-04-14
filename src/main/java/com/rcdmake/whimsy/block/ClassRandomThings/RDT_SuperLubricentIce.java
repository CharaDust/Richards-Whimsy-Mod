package com.rcdmake.whimsy.block.ClassRandomThings;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.block.ClassDEV.DEV_GhostBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class RDT_SuperLubricentIce extends Block {

    // 创建方块，使用构造函数类型
    public static final Block RDT_SUPER_LUBRICENTICE = new RDT_SuperLubricentIce(FabricBlockSettings.copyOf(Blocks.BLUE_ICE)
            .slipperiness(1f)
    );

    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block) {
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }

    public RDT_SuperLubricentIce(Settings settings) {
        super(settings);
    }
    public static void OnInit(){
        register_Block_and_BlockItem("rdt_super_lubricent_ice", RDT_SUPER_LUBRICENTICE);
    }
}
