package com.rcdmake.whimsy.block.ClassRandomThings.BlockEntity;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RDT_BlockBreaker extends Block {
    // super
    public RDT_BlockBreaker(Settings settings) {
        super(settings);
    }
    public static final Block RDT_BLOCK_BREAKER = new RDT_BlockBreaker(FabricBlockSettings.copyOf(Blocks.STONE));

    // 承受伤害（方法：当实体站立时）
    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        super.onSteppedOn(world, pos, state, entity);
        if (!world.isClient) {
            entity.damage(world.getDamageSources().cactus(), 3.0F); // 每半秒伤害1心
        }
    }

    // 破坏方块（方法：邻近更新）
    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
        if (!world.isClient) {
            BlockState aboveState = world.getBlockState(pos.up());
            if (!aboveState.isAir()) {
                world.breakBlock(pos.up(), true);
            }
        }
    }

    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block){
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }
    // OnInit
    public static void OnInit(){
        register_Block_and_BlockItem("rdt_block_breaker", RDT_BLOCK_BREAKER);
    }
}
