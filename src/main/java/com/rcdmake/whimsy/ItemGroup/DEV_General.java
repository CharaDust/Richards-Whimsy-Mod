package com.rcdmake.whimsy.ItemGroup;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.block.ClassDEV.*;
import com.rcdmake.whimsy.block.ClassDEV.BlockEntity.DEV_EntityInt;
import com.rcdmake.whimsy.block.ClassDEV.BlockEntity.DEV_SimpleWorker;
import com.rcdmake.whimsy.item.ClassDEV.DEVItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DEV_General {
    // 创建一个物品标签页框架
    public static final ItemGroup WHIMSY_DEV = Registry.register(Registries.ITEM_GROUP,
            // 设置标签页id，id需要保持唯一，否则会崩溃
            new Identifier(RichardsWhimsyMod.MOD_ID,"whimsy_group_dev"),
            // 设置标签页键名，具体名字去语言文件设置
            FabricItemGroup.builder().displayName(Text.translatable("itemGroup.whimsy_group_dev"))
                    // 设置标签页图标
                    .icon(()-> new ItemStack(DEVItems.DEV_ITEM_NORMAL)).entries((displayContext, entries)->{

                        // 在此处添加物品...
                        // 开发物品
                        entries.add(DEVItems.DEV_ITEM_NORMAL);
                        entries.add(DEV_Blocks.DEV_BLOCK_NORMAL);
                        entries.add(DEV_Blocks.DEV_BLOCK_VECTOR_FIX_SIDE_U);
                        entries.add(DEV_StateBoolean.DEV_BLOCK_STATE_BOOLEAN);
                        entries.add(DEV_StateInt.DEV_BLOCK_STATE_INT);
                        entries.add(DEV_StateEnum.DEV_BLOCK_STATE_ENUM);
                        entries.add(DEV_StateEnumAxis.DEV_BLOCK_STATE_AXIS);
                        entries.add(DEV_GhostBlock.DEV_BLOCK_GHOST);
                        entries.add(DEV_EntityInt.DEV_ENTITY_INT);
                        entries.add(DEV_SimpleWorker.DEV_SIMPLE_WORKER);

                    }).build());
    public static void OnInit() {}
}
