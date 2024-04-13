package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Identifier;

public class BHR_Soils extends Block {
    // 方块状态，必须首先声明，请使用动态注册法

    // 添加一个 GRASS_COVER(覆草) 方块状态
    public static final BooleanProperty GRASS_COVER = BooleanProperty.of("grass_cover");

    // 方块
    // MINERAL_SOIL(红壤)
    public static final Block MINERAL_SOIL = new BHR_Soils(
            FabricBlockSettings.create()
                    // 地图颜色
                    .mapColor(MapColor.ORANGE)
                    // 硬度
                    .strength(0.6f)
                    // 声音组
                    .sounds(BlockSoundGroup.PACKED_MUD)
            );
    // MINERAL_SOIL_UNKNOWN_ORE(红壤未知矿土)
    public static final Block MINERAL_SOIL_UNKNOWN_ORE = registerBlocks("mineral_soil_unknown_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.STONE)
            ));

    // MINERAL_SOIL_FLINT_ORE(红壤燧石矿土)
    public static final Block MINERAL_SOIL_FLINT_ORE = new BHR_Soils(
            FabricBlockSettings.copyOf(BHR_Soils.MINERAL_SOIL)
                    // 硬度
                    .strength(0.7f)
            );
    // MINERAL_SOIL_FLINT_ORE(红壤粘土矿土)
    public static final Block MINERAL_SOIL_CLAY_ORE = new BHR_Soils(
            FabricBlockSettings.copyOf(BHR_Soils.MINERAL_SOIL)
                    // 硬度
                    .strength(0.7f)
            );

    //
    // 方块以及方块物品静态注册方法
    private static Block registerBlocks(String name, Block block) {
        // 调用下面的方法注册方块物品
        registerModBlockItems(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, name), block);
    }
    // 方块物品静态注册方法，方便直接注册方块和方块物品
    private static Item registerModBlockItems(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    // 覆盖 appendProperties 并加入 GRASS_COVER 属性：
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(GRASS_COVER);
    }
    // 构造函数；继承 Block(原版方块类) 后，在那里追加默认值设定（默认为false）
    public BHR_Soils(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(GRASS_COVER, false));
    }

    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block){
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }

    public static void OnInit() {
        register_Block_and_BlockItem("mineral_soil", MINERAL_SOIL);
        register_Block_and_BlockItem("mineral_soil_flint_ore", MINERAL_SOIL_FLINT_ORE);
        register_Block_and_BlockItem("mineral_soil_clay_ore", MINERAL_SOIL_CLAY_ORE);
    }
}
