package com.rcdmake.whimsy.block.ClassDEV;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Identifier;

public class DEV_StateInt extends Block {

    // 添加一个 DEV_INT(开发_整型) 方块状态
    public static final IntProperty DEV_INT = IntProperty.of("dev_int",0,9);
    // 创建方块，使用构造函数类型
    public static final Block DEV_BLOCK_STATE_INT  = new DEV_StateInt(FabricBlockSettings.copyOf(Blocks.STONE));

    // 覆盖 appendProperties 并加入 DEV_INT 属性：
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DEV_INT);
    }
    // 构造函数；继承 Block(原版方块类) 后，在那里追加默认值设定（默认为false）
    public DEV_StateInt(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState()
                .with(DEV_INT, 0)
                // 如果写了多的类型，可以在此处以.with().with().with()...的形式连接追加
        );
    }

    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block){
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }
    public static void OnInit(){
        register_Block_and_BlockItem("dev_block_state_int", DEV_BLOCK_STATE_INT);
    }
}
