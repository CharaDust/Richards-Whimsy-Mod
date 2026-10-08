package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

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
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import static net.minecraft.state.property.Properties.AXIS;

public class BHR_TreeLogPine extends Block {
    // 添加一个 BHR_DECAYED(枯萎) 方块状态
    public static final BooleanProperty BHR_DECAYED = BooleanProperty.of("bhr_decayed");

    public BHR_TreeLogPine(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(AXIS, Direction.Axis.X).with(BHR_DECAYED, false));
    }

    // 创建方块，使用构造函数类型
    public static final Block BHR_PINE_LOG = new BHR_TreeLogPine(FabricBlockSettings.copyOf(Blocks.OAK_LOG));


    // 添加方块状态
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS, BHR_DECAYED);
    }

    // 方块放置朝向逻辑
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(AXIS, ctx.getSide().getAxis());
    }

    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block){
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }

    // OnInit
    public static void OnInit(){
        register_Block_and_BlockItem("bhr_pine_log", BHR_PINE_LOG);
    }
}
