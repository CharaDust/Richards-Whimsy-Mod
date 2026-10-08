package com.rcdmake.whimsy.function.ClassDEV.Render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class DEV_OverlayRenderer {
    protected static MinecraftClient RMC;
    protected static final MinecraftClient MC = RMC;

    private float progress;
    private float prevProgress;
    private BlockPos prevPos;

    public void resetProgress()
    {
        progress = 0;
        prevProgress = 0;
        prevPos = null;
    }

    public void updateProgress()
    {
        prevProgress = progress;
        assert MC.interactionManager != null;
        progress = MC.interactionManager.getBlockBreakingProgress();

        if(progress < prevProgress)
            prevProgress = progress;
    }

    public void render(MatrixStack matrixStack, float partialTicks,
                       BlockPos pos)
    {
        if(pos == null)
            return;

        // reset progress if breaking a different block
        if(prevPos != null && !pos.equals(prevPos))
            resetProgress();

        prevPos = pos;

        // GL settings
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        matrixStack.push();

        RegionPos region = getCameraRegion();
        applyRegionalRenderOffset(matrixStack, region);

        // set position
        matrixStack.translate(pos.getX() - region.x(), pos.getY(),
                pos.getZ() - region.z());

        // get interpolated progress
        boolean breaksInstantly = MC.player.getAbilities().creativeMode
                || getHardness(pos) >= 1;
        float p = breaksInstantly ? 1
                : MathHelper.lerp(partialTicks, prevProgress, progress);

        // set size
        if(p < 1)
        {
            matrixStack.translate(0.5, 0.5, 0.5);
            matrixStack.scale(p, p, p);
            matrixStack.translate(-0.5, -0.5, -0.5);
        }

        // get color
        float red = p * 2F;
        float green = 2 - red;

        // draw box
        RenderSystem.setShader(GameRenderer::getPositionProgram);
        RenderSystem.setShaderColor(red, green, 0, 0.25F);
        drawSolidBox(matrixStack);
        RenderSystem.setShaderColor(red, green, 0, 0.5F);
        drawOutlinedBox(matrixStack);

        matrixStack.pop();

        // GL resets
        RenderSystem.setShaderColor(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    // RenderUtil
    public record RegionPos(int x, int z)
    {
        public static RegionPos of(BlockPos pos)
        {
            return new RegionPos(pos.getX() >> 9 << 9, pos.getZ() >> 9 << 9);
        }

        public static RegionPos of(ChunkPos pos)
        {
            return new RegionPos(pos.x >> 5 << 9, pos.z >> 5 << 9);
        }

        public RegionPos negate()
        {
            return new RegionPos(-x, -z);
        }

        public Vec3d toVec3d()
        {
            return new Vec3d(x, 0, z);
        }

        public BlockPos toBlockPos()
        {
            return new BlockPos(x, 0, z);
        }
    }

    public static BlockPos getCameraBlockPos()
    {
        Camera camera = MC.getBlockEntityRenderDispatcher().camera;
        if(camera == null)
            return BlockPos.ORIGIN;

        return camera.getBlockPos();
    }

    public static RegionPos getCameraRegion()
    {
        return RegionPos.of(getCameraBlockPos());
    }

    public static Vec3d getCameraPos()
    {
        Camera camera = MC.getBlockEntityRenderDispatcher().camera;
        if(camera == null)
            return Vec3d.ZERO;

        return camera.getPos();
    }

    public static void applyRegionalRenderOffset(MatrixStack matrixStack,
                                                 RegionPos region)
    {
        Vec3d offset = region.toVec3d().subtract(getCameraPos());
        matrixStack.translate(offset.x, offset.y, offset.z);
    }

    private static final Box DEFAULT_BOX = new Box(0, 0, 0, 1, 1, 1);
    public static void drawSolidBox(MatrixStack matrixStack)
    {
        drawSolidBoxAddt(DEFAULT_BOX, matrixStack);
    }

    public static void drawSolidBoxAddt(Box bb, MatrixStack matrixStack)
    {
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION);
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ)
                .next();
        tessellator.draw();
    }

    public static void drawOutlinedBox(MatrixStack matrixStack)
    {
        drawOutlinedBoxAddt(DEFAULT_BOX, matrixStack);
    }

    public static void drawOutlinedBoxAddt(Box bb, MatrixStack matrixStack)
    {
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES,
                VertexFormats.POSITION);
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ)
                .next();
        tessellator.draw();
    }

    // BlockUtil
    public static float getHardness(BlockPos pos)
    {
        return getState(pos).calcBlockBreakingDelta(MC.player, MC.world, pos);
    }

    public static BlockState getState(BlockPos pos)
    {
        return MC.world.getBlockState(pos);
    }
}
