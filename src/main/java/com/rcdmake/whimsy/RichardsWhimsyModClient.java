package com.rcdmake.whimsy;

// 客户端初始化
import com.rcdmake.whimsy.block.ClassDEV.RenderType.DEV_ClientRenderInit;
import com.rcdmake.whimsy.block.ClassRandomThings.RenderType.RDT_ClientRenderInit;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.RenderType.WI_ClientRenderInit;
import com.rcdmake.whimsy.screen.ClassDEV.Render.DEV_ScreenRenderInit;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;

public class RichardsWhimsyModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient(){
        // 初始化 DEV 分支内容
        DEV_ClientRenderInit.OnInitClient();
        DEV_ScreenRenderInit.OnInitClient();

        // 初始化 WI 分支内容
        WI_ClientRenderInit.OnInitClient();

        // 初始化 RDT 分支内容
        RDT_ClientRenderInit.OnInitClient();

    }
}
