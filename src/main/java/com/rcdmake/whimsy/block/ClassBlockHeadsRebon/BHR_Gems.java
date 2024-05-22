package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.block.ClassDEV.DEV_GhostBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class BHR_Gems extends Block {
    // super
    public BHR_Gems(Settings settings) {
        super(settings);
    }

    // 创建方块，使用构造函数类型
    public static final Block BHR_GEM_AMETHYST = new BHR_Gems(FabricBlockSettings.copyOf(Blocks.GLASS)
            .sounds(BlockSoundGroup.SMALL_AMETHYST_BUD)
            .nonOpaque()
            .allowsSpawning(Blocks::never)
            .solidBlock(Blocks::never)
            // 窒息
            .suffocates(Blocks::never)
            .blockVision(Blocks::never)
            .noCollision()
    );

    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block) {
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }
    // 初始化方法，每一次建立一个文件/初始化方法都需要在主类引用一下
    public static void OnInit() {
        register_Block_and_BlockItem("bhr_gem_amethyst", BHR_GEM_AMETHYST);
    }
}
