package com.rcdmake.whimsy.block.ClassRandomThings;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

import static net.minecraft.state.property.Properties.AXIS;

public class RDT_TreeLogSpectre extends Block {
    public RDT_TreeLogSpectre(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(AXIS, Direction.Axis.X));
    }

    // 创建方块，使用构造函数类型
    public static final Block RDT_SPECTRE_LOG = new RDT_TreeLogSpectre(FabricBlockSettings.copyOf(Blocks.OAK_LOG)
            .nonOpaque()
            .allowsSpawning(Blocks::never)
            .solidBlock(Blocks::never)
            // 窒息
            .suffocates(Blocks::never)
            .blockVision(Blocks::never)
            .noCollision()
            .solid()
    );

    // 添加方块状态
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    // 方块放置朝向逻辑
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(AXIS, ctx.getSide().getAxis());
    }

    // 控制方块的光照表现。在Minecraft中，环境光遮蔽级别通常介于 0.0(完全遮蔽光线) 到 1.0(不遮蔽光线)
    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 0.9f;
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

    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block){
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }

    // OnInit
    public static void OnInit(){
        register_Block_and_BlockItem("rdt_spectre_log", RDT_SPECTRE_LOG);
    }
}
