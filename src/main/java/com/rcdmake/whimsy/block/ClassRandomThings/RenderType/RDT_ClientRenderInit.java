package com.rcdmake.whimsy.block.ClassRandomThings.RenderType;

import com.rcdmake.whimsy.block.ClassDEV.DEV_GhostBlock;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassLapis;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassQuartz;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassSugar;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_StepBlock;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.impl.blockrenderlayer.BlockRenderLayerMapImpl;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.RenderLayer;

public class RDT_ClientRenderInit {
    public static void SetRenderLayer_3(Block block){
//        BlockRenderLayerMapImpl.INSTANCE.putBlock(block, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getCutout());

    }

//    public static void SetCustomEntityRander(BlockEntityType entityType,Object costomRenderer){
//        BlockEntityRendererRegistry.register(entityType, costomRenderer::new);
//    }

    public static void OnInitClient(){

        // CutOut 渲染模式
        SetRenderLayer_3(RDT_GlassLapis.RDT_LAPIS_GLASS);
        SetRenderLayer_3(RDT_GlassQuartz.RDT_QUARTZ_GLASS);
        SetRenderLayer_3(RDT_GlassSugar.RDT_SUGAR_GLASS);
        SetRenderLayer_3(RDT_StepBlock.RDT_STEP_BLOCK);

        // 自定义渲染器
    }
}
