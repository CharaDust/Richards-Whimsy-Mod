package com.rcdmake.whimsy;

// 客户端初始化
import com.rcdmake.whimsy.block.ClassDEV.RenderType.DEV_ClientRenderInit;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.RenderType.WI_ClientRenderInit;
import net.fabricmc.api.ClientModInitializer;

public class RichardsWhimsyModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient(){
        // 初始化 DEV 分支内容
        DEV_ClientRenderInit.OnInit();

        // 初始化 WI 分支内容
        WI_ClientRenderInit.OnInit();

    }
}
