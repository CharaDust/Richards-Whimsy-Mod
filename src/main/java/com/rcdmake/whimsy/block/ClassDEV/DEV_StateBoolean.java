package com.rcdmake.whimsy.block.ClassDEV;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Identifier;

public class DEV_StateBoolean extends Block {

    // 添加一个 DEV_BOOLEAN(开发_布尔值) 方块状态，注意一定要先添加这个
    public static final BooleanProperty DEV_BOOLEAN = BooleanProperty.of("dev_boolean");
    // 创建方块，使用构造函数类型
    public static final Block DEV_BLOCK_STATE_BOOLEAN  = new DEV_StateBoolean(FabricBlockSettings.copyOf(Blocks.STONE));

    // 在register方法内引用可以强制注册多状态方块
    private static void regInit_Block_and_BlockItem(Block block, String path){
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }

    // 然后，我们需要通过覆盖 appendProperties 并加入 DEV_BOOLEAN 以注册属性：
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DEV_BOOLEAN);
    }

    // 然后，我们需要在方块构造器中设置属性的默认状态（要设置多个属性，请通过调用 with() 来连接）：
    public DEV_StateBoolean(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(DEV_BOOLEAN, false));
    }
    // 初始化方法，每一次建立一个文件/初始化方法都需要在主类引用一下，方法内部写上希望在初始化阶段就运行的代码
    public static void OnInit() {
        regInit_Block_and_BlockItem(DEV_BLOCK_STATE_BOOLEAN, "dev_block_state_boolean");
    }
}