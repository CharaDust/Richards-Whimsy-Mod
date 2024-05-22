package com.rcdmake.whimsy.ItemGroup;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.block.ClassRandomThings.*;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RDT_General {
    // 创建一个物品标签页框架
    public static final ItemGroup RDT_GENERAL = Registry.register(Registries.ITEM_GROUP,
            // 设置标签页id，id需要保持唯一，否则会崩溃
            new Identifier(RichardsWhimsyMod.MOD_ID,"rdt_general"),
            // 设置标签页键名，具体名字去语言文件设置
            FabricItemGroup.builder().displayName(Text.translatable("itemGroup.rdt_general"))
                    // 设置标签页图标
                    .icon(()-> new ItemStack(Items.LEATHER_HELMET)).entries((displayContext, entries)->{

                        // 在此处添加物品... entries.add();
                        entries.add(RDT_SuperLubricentIce.RDT_SUPER_LUBRICENTICE);
                        entries.add(RDT_GlassLapis.RDT_LAPIS_GLASS);
                        entries.add(RDT_GlassQuartz.RDT_QUARTZ_GLASS);
                        entries.add(RDT_GlassSugar.RDT_SUGAR_GLASS);
                        entries.add(RDT_StepBlock.RDT_STEP_BLOCK);

                        entries.add(RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_RED);

                    }).build());
    public static void OnInit() {

    }
}
