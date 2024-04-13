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
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class DEV_GhostBlock extends Block {

    // 创建方块，使用构造函数类型
    public static final Block DEV_BLOCK_GHOST  = new DEV_GhostBlock(FabricBlockSettings.copyOf(Blocks.STONE));

    public DEV_GhostBlock(Settings settings) {
        super(settings);
    }

    // 此方法返回一个VoxelShape，定义了方块的物理形状。返回VoxelShapes.empty()意味着这个方块不具有任何碰撞形状，实体可以自由穿过。
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

//    // 此方法返回一个布尔值，指示是否应考虑与此方块的碰撞。返回false表示实体不会与此方块发生碰撞检测
//    @Override
//    public boolean canCollide(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
//        return false;
//    }

    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block){
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }
    public static void OnInit(){
        register_Block_and_BlockItem("dev_block_ghost", DEV_BLOCK_GHOST);
        BlockRenderLayerMap.INSTANCE.putBlock(DEV_BLOCK_GHOST, RenderLayer.getTranslucent());
}
