package com.rcdmake.whimsy.block;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Identifier;

public class DEVBlocks {

    // 添加 DEV_BLOCK_NORMAL(开发_普通方块) 方块及其方块物品
    public static final Block DEV_BLOCK_NORMAL = registerBlocks("dev_block_normal",
            // 照搬原版的"石头"方块,按住CTRL点击"STONE"查看原版代码定义
            new Block(FabricBlockSettings.copyOf(Blocks.STONE)));
    // 添加 DEV_BLOCK_VECTOR_FIX_SIDE_U(开发_固定向上方块) 方块及其方块物品
    public static final Block DEV_BLOCK_VECTOR_FIX_SIDE_U = registerBlocks("dev_block_vector_fix_side_u",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE)));

    // 添加 DEV_BLOCK_STATE_BOOLEAN(开发_布尔状态方块)
    public static final Block DEV_BLOCK_STATE_BOOLEAN = registerBlocks("dev_block_state_boolean",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE)));

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

    // 初始化方法，每一次建立一个文件/初始化方法都需要在主类引用一下
    public static void registerDEVBlocks() {
    }
}
