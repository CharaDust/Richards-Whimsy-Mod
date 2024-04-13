package com.rcdmake.whimsy.block.ConfigStates.apply;

import com.rcdmake.whimsy.block.ConfigStates.def.DEVState_BooleanParallel;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateBoolean;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public class BooleanPapallel extends DEVState_BooleanParallel {
    // 添加 DEV_BLOCK_STATE_BOOLEANPARALLEL(开发_平行布尔方块) 方块及其方块物品
    public static final Block DEV_BLOCK_STATE_BOOLEANPARALLEL  = new DEV_StateBoolean(
            FabricBlockSettings.copyOf(Blocks.STONE)
    );

    public BooleanPapallel(Settings settings) {
        super(settings);
    }
}
