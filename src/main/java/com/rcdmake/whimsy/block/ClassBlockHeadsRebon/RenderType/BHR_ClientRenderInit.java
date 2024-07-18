package com.rcdmake.whimsy.block.ClassBlockHeadsRebon.RenderType;

import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Gems;
import com.rcdmake.whimsy.block.ClassDEV.DEV_GhostBlock;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class BHR_ClientRenderInit {
    public static void OnInitClient(){
        BlockRenderLayerMap.INSTANCE.putBlock(BHR_Gems.BHR_GEM_AMETHYST, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(BHR_Gems.BHR_GEM_SAPPHIRE, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(BHR_Gems.BHR_GEM_EMERALD, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(BHR_Gems.BHR_GEM_RUBY, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(BHR_Gems.BHR_GEM_DIAMOND, RenderLayer.getTranslucent());
    }
}
