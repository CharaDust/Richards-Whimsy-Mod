package com.rcdmake.whimsy.block.ClassDEV.RenderType;

import com.rcdmake.whimsy.block.ClassDEV.DEV_GhostBlock;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;


public class DEV_ClientRenderInit {
    public static void OnInitClient(){
        BlockRenderLayerMap.INSTANCE.putBlock(DEV_GhostBlock.DEV_BLOCK_GHOST, RenderLayer.getTranslucent());
    }
}
