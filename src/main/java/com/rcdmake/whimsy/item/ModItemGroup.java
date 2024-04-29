
package com.rcdmake.whimsy.item;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.block.ClassDEV.*;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_Aerogel;
import com.rcdmake.whimsy.block.ModBlocks;
import com.rcdmake.whimsy.item.ClassDEV.DEVItems;
import com.rcdmake.whimsy.item.ClassWhimsyIdea.WI_Prospector;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroup {
    // 创建一个物品标签页，如若需要更多物品标签页请去文末复制框架
    public static final ItemGroup WHIMSY_GROUP = Registry.register(Registries.ITEM_GROUP,
            // 设置标签页id，id需要保持唯一，否则会崩溃
            new Identifier(RichardsWhimsyMod.MOD_ID,"whimsy_group_item"),
            // 设置标签页文字键名，具体名字去语言文件设置
            FabricItemGroup.builder().displayName(Text.translatable("itemGroup.whimsy_group_item"))
                    // 设置标签页图标
                    .icon(()-> new ItemStack(Items.BOOK)).entries((displayContext,entries)->{
                        // 在此处添加物品...
                        // 宝石
                        entries.add(ModItems.GEM_AMETHYST);
                        entries.add(ModItems.GEM_SAPPHIRE);
                        entries.add(ModItems.GEM_EMERALD);
                        entries.add(ModItems.GEM_RUBY);
                        entries.add(ModItems.GEM_DIAMOND);
                        // 其他物品
                        entries.add(ModItems.CRYSTALLIZED_HONEY);
                        entries.add(WI_Prospector.WI_PROSPECTOR);
                        entries.add(Items.BOOK);

                    }).build());

    // 创建一个物品标签页框架
    public static final ItemGroup WHIMSY_BLOCK = Registry.register(Registries.ITEM_GROUP,
            // 设置标签页id，id需要保持唯一，否则会崩溃
            new Identifier(RichardsWhimsyMod.MOD_ID,"whimsy_group_block"),
            // 设置标签页键名，具体名字去语言文件设置
            FabricItemGroup.builder().displayName(Text.translatable("itemGroup.whimsy_group_block"))
                    // 设置标签页图标
                    .icon(()-> new ItemStack(ModBlocks.PACKED_PLANKS)).entries((displayContext,entries)->{
                        // 在此处添加物品...
                        // 方块
                        entries.add(ModBlocks.STATIC_COBBLESTONE);
                        entries.add(ModBlocks.PACKED_PLANKS);
                        entries.add(WI_Aerogel.WI_AEROGEL);
                        // 自然方块
//                        entries.add(ModBlocks.ROCK);
                        entries.add(ModBlocks.SLATE);

                    }).build());



    // 初始化方法，每一次建立一个文件/初始化方法都需要在主类引用一下
    public static void register() {

    }

}

