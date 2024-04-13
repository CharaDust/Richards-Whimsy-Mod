package com.rcdmake.whimsy.block.ConfigStates.def;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;

public class DEVState_BooleanParallel extends Block {
    // 添加一个 DEV_BOOLEAN_PARALLEL(开发_平行布尔值) 方块状态
    public static final BooleanProperty DEV_BOOLEAN_PARALLEL = BooleanProperty.of("dev_boolean_parallel");
    // 覆盖 appendProperties 并加入 DEV_BOOLEAN 以注册属性：
    @Override
    public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DEV_BOOLEAN_PARALLEL);
    }
    // 设置属性默认状态
    public DEVState_BooleanParallel(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(DEV_BOOLEAN_PARALLEL, false));
    }
}
