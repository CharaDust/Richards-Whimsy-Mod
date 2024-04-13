package com.rcdmake.whimsy.item.ClassBlockHeadRebon;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.item.ModItems;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BHR_OtherItems {

    // 添加 BLOCKHEADS_ICON(图标) 物品
    public static final Item BLOCKHEADS_ICON = registerItems("blockheads_icon", new Item(new FabricItemSettings()));

    // 物品注册方法
    private static Item registerItems(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, name),item);
    }
    // 初始化方法，每一次建立一个文件/初始化方法都需要在主类引用一下
    public static void OnInit() {}
}
