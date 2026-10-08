package com.rcdmake.whimsy.function.ClassDEV.Render;

import com.rcdmake.whimsy.function.ClassDEV.Render.Lib.DEV_Enums;
import com.rcdmake.whimsy.function.ClassDEV.Render.Lib.DEV_EventManager;
import com.rcdmake.whimsy.function.ClassDEV.Render.Lib.DEV_RenderListener;
import com.rcdmake.whimsy.function.ClassDEV.Render.Lib.DEV_UpdateListener;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import static com.rcdmake.whimsy.function.ClassDEV.Render.DEV_OverlayRenderer.MC;

public class DEV_Overlay implements DEV_UpdateListener, DEV_RenderListener {
    private final DEV_OverlayRenderer renderer = new DEV_OverlayRenderer();
    protected static final DEV_Enums ENUMS = DEV_Enums.INSTANCE;
    protected static final DEV_EventManager EVENT = ENUMS.getEventManager();



    // 测试变量: Player
    PlayerEntity player = null;

    @Override
    public void onRender(MatrixStack matrixStack, float partialTicks) {
        // 测试信息：
//        player.sendMessage(Text.literal("(Debug) Beep!").formatted(Formatting.AQUA), true);
        System.out.println("(Debug) Beep!...");
        if(!MC.interactionManager.isBreakingBlock())
            return;

        if(!(MC.crosshairTarget instanceof BlockHitResult blockHitResult)
                || blockHitResult.getType() != HitResult.Type.BLOCK)
            return;

        renderer.render(matrixStack, partialTicks,
                blockHitResult.getBlockPos());
    }

    @Override
    public void onUpdate() {
        if(MC.interactionManager.isBreakingBlock())
            renderer.updateProgress();
        else
            renderer.resetProgress();
    }

    public static void OnInitClient(){

    }
}
