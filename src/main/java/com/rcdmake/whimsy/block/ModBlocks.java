package com.rcdmake.whimsy.block;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {

    // 添加 STATIC_COBBLESTONE(固石) 方块及其方块物品
    public static final Block STATIC_COBBLESTONE = registerBlocks("static_cobblestone",
            // 照搬原版的"石头"方块,按住CTRL点击"STONE"查看原版代码定义
            new Block(FabricBlockSettings.copyOf(Blocks.STONE)));
    // 添加 PACKED_PLANKS(包装木材) 方块及其方块物品
    public static final Block PACKED_PLANKS = registerBlocks("packed_planks",
            // 照搬原版的"橡木木板"方块,按住CTRL点击"OAK_PLANKS"查看原版代码定义
            new Block(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)));

    // 添加 SLATE(板岩) 方块及其方块物品
    public static final Block SLATE = registerBlocks("slate",
            new Block(FabricBlockSettings.copyOf(Blocks.COBBLESTONE)));


    // 方块注册方法，如果不注册方块物品的话会没有物品形式（如：细雪，发光的红石矿石）
    private static Block registerBlocks(String name, Block block) {
        // 调用下面的方法注册方块物品
        registerModBlockItems(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, name), block);
    }
    // 方块物品注册方法，方便直接注册方块和方块物品
    private static Item registerModBlockItems(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    // 仅方块注册方法，没有物品形式（如：细雪，发光的红石矿石）
    private static Block registerBlockOnly(String name, Block block) {
        return Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, name), block);
    }

    // 初始化方法，每一次建立一个文件/初始化方法都需要在主类引用一下
    public static void register() {
    }
}
