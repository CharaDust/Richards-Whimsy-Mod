package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BHR_Rocks extends Block {
    // ROCK(切面板岩)
    public static final Block ROCK = registerBlocks("rock",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE)));
    // ROCK_UNKNOWN_ORE(切面板岩矿石)
    public static final Block ROCK_UNKNOWN_ORE = registerBlocks("rock_unknown_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE)));
    // ROCK_COPPER_ORE(切面板岩铜矿石)
    public static final Block ROCK_COPPER_ORE = registerBlocks("rock_copper_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE)));
    // ROCK_IRON_ORE(切面板岩铁矿石)
    public static final Block ROCK_IRON_ORE = registerBlocks("rock_iron_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE)));
    // ROCK_TIN_ORE(切面板岩锡矿石)
    public static final Block ROCK_TIN_ORE = registerBlocks("rock_tin_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE)));
    // ROCK_COAL_ORE(切面板岩煤矿石)
    public static final Block ROCK_COAL_ORE = registerBlocks("rock_coal_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE)));
    // ROCK_GOLD_ORE(切面板岩金矿石)
    public static final Block ROCK_GOLD_ORE = registerBlocks("rock_gold_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE)));
    // ROCK_TITANIUM_ORE(切面板岩钛矿石)
    public static final Block ROCK_TITANIUM_ORE = registerBlocks("rock_titanium_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE)));
    // ROCK_PLATINUM_ORE(切面板岩铂矿石)
    public static final Block ROCK_PLATINUM_ORE = registerBlocks("rock_platinum_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE)));


    // 方块以及方块物品注册方法
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
    public BHR_Rocks(Settings settings) {
        super(settings);
    }
    public static void OnInit() {}
}
