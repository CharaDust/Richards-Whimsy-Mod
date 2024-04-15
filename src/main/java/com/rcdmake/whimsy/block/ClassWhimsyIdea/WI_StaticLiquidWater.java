package com.rcdmake.whimsy.block.ClassWhimsyIdea;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class WI_StaticLiquidWater extends Block {
    public WI_StaticLiquidWater(Settings settings) {
        super(settings);
    }

    // 创建方块，使用构造函数类型
    public static final Block WI_STATICLIQUIDWATER = new WI_StaticLiquidWater(FabricBlockSettings.create()
            // 地图颜色
            .mapColor(MapColor.WATER_BLUE)
            // 硬度，爆炸抗性
            .strength(100f, 100f)
            // 声音组
            .sounds(BlockSoundGroup.MUD)
            // 非不透明（不阻挡光线）
            .nonOpaque()
            // 生成生物：：非
            .allowsSpawning(Blocks::never)
            // 完全固态方块：：非
            .solidBlock(Blocks::never)
            // 窒息伤害：非
            .suffocates(Blocks::never)
            // 阻挡视线：非
            .blockVision(Blocks::never)
    );
    // 方块以及方块物品注册方法简化写法
    private static void register_Block_and_BlockItem(String path, Block block){
        Registry.register(Registries.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, path), block);
        Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, path), new BlockItem(block, new FabricItemSettings()));
    }
    public static void OnInit(){
        register_Block_and_BlockItem("wi_static_liquid_water", WI_STATICLIQUIDWATER);
    }
}
