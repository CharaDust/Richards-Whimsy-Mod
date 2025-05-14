package com.rcdmake.whimsy.block.ClassWhimsyIdea.RenderType;

import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_Aerogel;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_StaticLiquidWater;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;


public class WI_ClientRenderInit {
    public static void OnInitClient(){
        BlockRenderLayerMap.INSTANCE.putBlock(WI_Aerogel.WI_AEROGEL, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(WI_StaticLiquidWater.WI_STATICLIQUIDWATER, RenderLayer.getTranslucent());

        // 颜色提供器
        WI_ColorProvider.OnInitClient();
    }
}
