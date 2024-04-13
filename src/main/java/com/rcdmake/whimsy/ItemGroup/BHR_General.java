package com.rcdmake.whimsy.ItemGroup;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Rocks;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Soils;
import com.rcdmake.whimsy.block.ModBlocks;
import com.rcdmake.whimsy.item.ClassBlockHeadRebon.BHR_OtherItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BHR_General {
    // 创建一个 BHR_GENERAL 物品标签页框架
    public static final ItemGroup BHR_GENERAL = Registry.register(Registries.ITEM_GROUP,
            // 设置标签页id，id需要保持唯一，否则会崩溃
            new Identifier(RichardsWhimsyMod.MOD_ID,"bhr_general"),
            // 设置标签页键名，具体名字去语言文件设置
            FabricItemGroup.builder().displayName(Text.translatable("itemGroup.bhr_general"))
                    // 设置标签页图标
                    .icon(()-> new ItemStack(BHR_OtherItems.BLOCKHEADS_ICON)).entries((displayContext, entries)->{

                        // 在此处添加物品...
                        entries.add(BHR_Rocks.ROCK);
                        entries.add(BHR_Rocks.ROCK_COPPER_ORE);
                        entries.add(BHR_Rocks.ROCK_IRON_ORE);
                        entries.add(BHR_Rocks.ROCK_TIN_ORE);
                        entries.add(BHR_Rocks.ROCK_COAL_ORE);
                        entries.add(BHR_Rocks.ROCK_GOLD_ORE);
                        entries.add(BHR_Rocks.ROCK_PLATINUM_ORE);
                        entries.add(BHR_Rocks.ROCK_TITANIUM_ORE);
                        entries.add(BHR_Rocks.ROCK_UNKNOWN_ORE);
                        entries.add(BHR_Soils.MINERAL_SOIL);
                        entries.add(BHR_Soils.MINERAL_SOIL_FLINT_ORE);
                        entries.add(BHR_Soils.MINERAL_SOIL_CLAY_ORE);
                        entries.add(BHR_Soils.MINERAL_SOIL_UNKNOWN_ORE);


                    }).build());
    public static void OnInit() {}
}
