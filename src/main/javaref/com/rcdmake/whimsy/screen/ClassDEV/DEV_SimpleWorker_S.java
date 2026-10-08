package com.rcdmake.whimsy.screen.ClassDEV;

import com.mojang.blaze3d.systems.RenderSystem;
import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.screen.ClassDEV.Handler.DEV_SimpleWorker_SH;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DEV_SimpleWorker_S extends HandledScreen<DEV_SimpleWorker_SH> {
    // 导入材质文件
    private static final Identifier TEXTURE = new Identifier(RichardsWhimsyMod.MOD_ID,"textures/gui/simple_worker.png");
    // super
    public DEV_SimpleWorker_S(DEV_SimpleWorker_SH handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    /**
     * 初始化屏幕组件并设置标题的垂直位置。
     * <p>
     * 此方法定义GUI标题与玩家物品栏标题的横向位置，根据需要调整。
     * <br>
     * 横向位置的相对值目前尚不可知，需要进行测试。
     */
    @Override
    protected void init() {
        super.init();
        titleY = 1000;
        playerInventoryTitleY = 1000;
    }

    // 渲染GUI背景
    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1f,1f,1f,1f);
        RenderSystem.setShaderTexture(0,TEXTURE);
        // 居中放置GUI
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        // png材质的渲染起始位置
        context.drawTexture(TEXTURE,x,y,0,0,backgroundWidth,backgroundHeight);
        // 渲染进度条，自定义方法
        renderProgressArrow(context, x, y);
    }

    // 渲染进度条
    private void renderProgressArrow(DrawContext context, int x, int y) {
        if (handler.isCrafting()){
            // （85，30）表示空进度条的起始位置、（176，0）表示满进度条的起始位置、8为宽度；进度条将随着工作进度被慢慢填满
            context.drawTexture(TEXTURE,x + 85, y + 30, 176,0,8,handler.getScaledProgress());
        }
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context,mouseX,mouseY);
    }


}
