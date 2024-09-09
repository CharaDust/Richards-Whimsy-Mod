package com.rcdmake.whimsy.block.ClassRandomThings;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class RDT_GlassSugar extends Block {
    public RDT_GlassSugar(Settings settings) {
        super(settings);
    }

    // 创建方块，使用构造函数类型
    public static final Block RDT_SUGAR_GLASS = new RDT_GlassSugar(FabricBlockSettings.copyOf(Blocks.GLASS));
    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block){
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }

//    @Override
//    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
//        super.onLandedUpon(world, pos, entity, fallDistance);
//
//        // 设定一个阈值，超过该速度则破坏方块
//        if (fallDistance > 3.0f && !world.isClient()) {
//            world.breakBlock(pos, false);
//        }
//    }

    // 踩踏破坏效果
    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (!world.isClient() && !entity.bypassesSteppingEffects()) {
            // 直接破坏方块，无需额外的概率或条件
            world.breakBlock(pos, false);
        }
        super.onSteppedOn(world, pos, state, entity);
    }

    // 投掷物破坏效果
    @Override
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        BlockPos blockPos = hit.getBlockPos();
        if (!world.isClient && projectile.canModifyAt(world, blockPos) && projectile.canBreakBlocks(world)) {
            world.breakBlock(blockPos, false, projectile);
        }
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

    public static void OnInit() {
        register_Block_and_BlockItem("rdt_sugar_glass", RDT_SUGAR_GLASS);
    }
}
