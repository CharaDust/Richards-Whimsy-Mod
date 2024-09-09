package com.rcdmake.whimsy.function.ClassDEV.Render.Lib;

import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public interface DEV_RenderListener extends DEV_Listener {
    public void onRender(MatrixStack matrixStack, float partialTicks);

    public static class RenderDEVEvent extends DEV_Event<DEV_RenderListener>
    {
        private final MatrixStack matrixStack;
        private final float partialTicks;

        public RenderDEVEvent(MatrixStack matrixStack, float partialTicks)
        {
            this.matrixStack = matrixStack;
            this.partialTicks = partialTicks;
        }

        @Override
        public void fire(ArrayList<DEV_RenderListener> listeners)
        {
            GL11.glEnable(GL11.GL_LINE_SMOOTH);

            for(DEV_RenderListener listener : listeners)
                listener.onRender(matrixStack, partialTicks);

            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }

        @Override
        public Class<DEV_RenderListener> getListenerType()
        {
            return DEV_RenderListener.class;
        }
    }
}
