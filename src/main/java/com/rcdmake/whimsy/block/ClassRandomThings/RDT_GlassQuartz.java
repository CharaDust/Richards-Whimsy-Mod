package com.rcdmake.whimsy.block.ClassRandomThings;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class RDT_GlassQuartz extends Block {
    public RDT_GlassQuartz(Settings settings) {
        super(settings);
    }

    // 创建方块，使用构造函数类型
    public static final Block RDT_QUARTZ_GLASS = new RDT_GlassQuartz(FabricBlockSettings.copyOf(Blocks.GLASS));
    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block){
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }

    // 此方法返回一个VoxelShape，定义了方块的物理形状。返回VoxelShapes.empty()意味着这个方块不具有任何碰撞形状，实体可以自由穿过。
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // 检查上下文是否为实体上下文并且实体非空
        if (context instanceof EntityShapeContext) {
            Entity entity = ((EntityShapeContext) context).getEntity();
            if (entity instanceof PlayerEntity) {
                // 玩家视此方块为具有空碰撞箱
                return VoxelShapes.empty();
            }
        }
        // 其他实体视此方块为具有完整碰撞箱
        return VoxelShapes.fullCube();
    }

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

    // 边面可见度：当两个相同方块连接时，贴合的面不可见
    @Override
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        if (stateFrom.isOf(this)) {
            return true;
        }
        return super.isSideInvisible(state, stateFrom, direction);
    }

    public static void OnInit(){
        register_Block_and_BlockItem("rdt_quartz_glass", RDT_QUARTZ_GLASS);
    }
}
