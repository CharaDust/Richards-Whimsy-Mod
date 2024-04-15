package com.rcdmake.whimsy.block.ClassRandomThings.RenderType;

import com.rcdmake.whimsy.block.ClassDEV.DEV_GhostBlock;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassLapis;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassQuartz;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;

public class RDT_ClientRenderInit {
    public static void SetRenderLayer_3(Block block){
        BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout());
    }

    public static void OnInit(){
        SetRenderLayer_3(RDT_GlassLapis.RDT_LAPIS_GLASS);
        SetRenderLayer_3(RDT_GlassQuartz.RDT_QUARTZ_GLASS);
    }
}
