package com.rcdmake.whimsy.block;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

public class ConfigBlockSettings extends Block {

    // 一个多状态方块的例子
    // 声明一个 充能 状态
    public static final BooleanProperty CHARGED = BooleanProperty.of("charged");
    public static final Block CHARGEABLE_BLOCK = new Block(FabricBlockSettings.copyOf(Blocks.STONE));

    // 此时在根目录位置创建一个初始化注册方块
    // 然后，我们需要通过覆盖 appendProperties 并加入 CHARGED 以注册属性：
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CHARGED);
    }
    // 然后，我们需要在方块构造器中设置属性的默认状态（要设置多个属性，请通过调用 with() 来连接）：
    public ConfigBlockSettings(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(CHARGED, false));
    }

    //

    // 初始化方法，每一次建立一个文件/初始化方法都需要在主类引用一下
    public static void registerConfigBlockSettings() {}
}