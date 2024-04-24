package com.rcdmake.whimsy.block.ClassRandomThings;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class RDT_StepBlock extends Block {
    public RDT_StepBlock(Settings settings) {
        super(settings);
    }

    // 创建方块，使用构造函数类型
    public static final Block RDT_STEP_BLOCK = new RDT_StepBlock(FabricBlockSettings.create()
            .mapColor(MapColor.PALE_YELLOW)
            .strength(0.5f)
            .sounds(BlockSoundGroup.SCAFFOLDING)
            .allowsSpawning(Blocks::never)
            .pistonBehavior(PistonBehavior.DESTROY)
            .solidBlock(Blocks::never)
            .nonOpaque()
    );

    // 方块注册方法
    private static void register_Block_and_BlockItem(String path, Block block) {
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }

    // 方块消失逻辑
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
            // 添加一个200秒的计划刻延迟
            world.scheduleBlockTick(pos, this, 200);
        }
    }

    @Override
    // 计划刻事件
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        // 破坏方块
        world.breakBlock(pos, false);
        super.scheduledTick(state, world, pos, random);
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

    // OnInit
    public static void OnInit() {
        register_Block_and_BlockItem("rdt_step_block", RDT_STEP_BLOCK);
    }
}
