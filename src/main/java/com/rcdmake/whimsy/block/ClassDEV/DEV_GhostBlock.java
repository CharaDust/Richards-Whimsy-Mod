package com.rcdmake.whimsy.block.ClassDEV;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.NotNull;

public class DEV_GhostBlock extends Block {

    // 创建方块，使用构造函数类型
    public static final Block DEV_BLOCK_GHOST = new DEV_GhostBlock(FabricBlockSettings.copyOf(Blocks.GLASS)
            .nonOpaque()
            .allowsSpawning(Blocks::never)
            .solidBlock(Blocks::never)
            // 窒息
            .suffocates(Blocks::never)
            .blockVision(Blocks::never)
            .noCollision()
            .solid()
    );

    public DEV_GhostBlock(Settings settings) {
        super(settings);
    }

    // 此方法返回一个VoxelShape，定义了方块的物理形状。返回VoxelShapes.empty()意味着这个方块不具有任何碰撞形状，实体可以自由穿过。
//    @Override
//    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
//        return VoxelShapes.empty();
//    }

    // 以下内容照搬玻璃的源文件
    // 返回一个空的体积形状，表示方块不对相机产生碰撞效果，相机可以无障碍穿过此方块
    @Override
    public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    // 水流无法穿过方块
    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return true;
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        if (world.getBlockState(pos).getFluidState().isEmpty()) {
            super.onBroken(world, pos, state);
        }
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, @NotNull NavigationType type) {
        switch (type) {
            case WATER: {
                return false;
            }
        }
        return false;
    }

    // 处理邻居方块的更新
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        // 防止水流影响GhostBlock
        if (world.getBlockState(fromPos).getBlock() == Blocks.WATER) {
            return;
        }
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
    }

    // 该方块可含水
//    @Override
//    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
//        builder.add(Properties.WATERLOGGED);
//    }
//
//    public FluidState getFluidState(BlockState state) {
//        return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
//    }

    // 控制方块的光照表现。在Minecraft中，环境光遮蔽级别通常介于 0.0(完全遮蔽光线) 到 1.0(不遮蔽光线)
    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0f;
    }

    // 检查方块是否为透明，返回 true，表示方块总是透明的
    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

//    // 此方法返回一个布尔值，指示是否应考虑与此方块的碰撞。返回false表示实体不会与此方块发生碰撞检测
//    @Override
//    public boolean canCollide(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
//        return false;
//    }

    // 边面可见度：当两个相同方块连接时，贴合的面不可见
    @Override
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        if (stateFrom.isOf(this)) {
            return true;
        }
        return super.isSideInvisible(state, stateFrom, direction);
    }

    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block) {
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }

    public static void OnInit() {
        register_Block_and_BlockItem("dev_block_ghost", DEV_BLOCK_GHOST);
    }
}
