package com.rcdmake.whimsy.block.ClassWhimsyIdea.RenderType;

import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_Aerogel;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;


public class WI_ClientRenderInit {
    public static void OnInit(){
        BlockRenderLayerMap.INSTANCE.putBlock(WI_Aerogel.WI_AEROGEL, RenderLayer.getTranslucent());
    }
}
