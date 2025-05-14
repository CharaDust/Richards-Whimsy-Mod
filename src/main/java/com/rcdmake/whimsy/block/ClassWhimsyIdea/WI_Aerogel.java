package com.rcdmake.whimsy.block.ClassWhimsyIdea;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.block.ClassDEV.DEV_GhostBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class WI_Aerogel extends Block {

    // 创建方块，使用构造函数类型
    public static final Block WI_AEROGEL = new WI_Aerogel(FabricBlockSettings.create()
            // 硬度，爆炸抗性
            .strength(0.3f, 15.533f)
            // 声音组
            .sounds(BlockSoundGroup.SNOW)
            // 非不透明（不阻挡光线）
            .nonOpaque()
            // 生成生物：:非
            .allowsSpawning(Blocks::never)
            // 完全固态方块：:非
            .solidBlock(Blocks::never)
            // 窒息伤害：:非
            .suffocates(Blocks::never)
            // 阻挡视线：:非
            .blockVision(Blocks::never)
            // 无碰撞
            .noCollision()
    );

    public WI_Aerogel(Settings settings) {
        super(settings);
    }

    // 此方法返回一个VoxelShape，定义了方块的物理形状。返回VoxelShapes.empty()意味着这个方块不具有任何碰撞形状，实体可以自由穿过。
//    @Override
//    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
//        return VoxelShapes.empty();
//    }



    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block){
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }

    // 重写 onEntityCollision 方法，使实体位于方块内时减速
    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        entity.slowMovement(state, new Vec3d(1, 1f, 1));
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

    public static void OnInit(){
        register_Block_and_BlockItem("wi_aerogel", WI_AEROGEL);
    }
}
