package com.rcdmake.whimsy.datagen;

import com.rcdmake.whimsy.tag.ClassLegacy.LGC_VanillaOre;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        // 原版矿物
        getOrCreateTagBuilder(LGC_VanillaOre.Blocks.LGC_VANILLA_ORE)
                // 使用.add()直接添加方块或物品
                // 使用.forceAddTag()添加原版带标签的方块或物品
                .forceAddTag(BlockTags.COAL_ORES)
                .forceAddTag(BlockTags.IRON_ORES)
                .forceAddTag(BlockTags.GOLD_ORES)
                .forceAddTag(BlockTags.LAPIS_ORES)
                .forceAddTag(BlockTags.DIAMOND_ORES)
                .forceAddTag(BlockTags.REDSTONE_ORES)
                .forceAddTag(BlockTags.COPPER_ORES)
                .forceAddTag(BlockTags.EMERALD_ORES);

        // 编辑原版标签
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE);

        // 编辑Fabric自带标签
        getOrCreateTagBuilder(TagKey.of(RegistryKeys.BLOCK,
                new Identifier("fabric", "needs_tool_level_4")
                // 使用.add()直接添加方块或物品
                // 使用.forceAddTag()添加原版带标签的方块或物品
                ));
    }
}
