package com.rcdmake.whimsy.item;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class DEVItems {

    // 添加 DEV_ITEM_NORMAL(开发_普通物品) 物品
    public static final Item DEV_ITEM_NORMAL = registerItems("dev_item_normal", new Item(new FabricItemSettings()));
    // 添加 UNDEF(未定义之如果某人不在乎我就把他的相片挂载游戏里的物品上) 物品
    public static final Item UNDEF = registerItems("undef", new Item(new FabricItemSettings()));

    // 物品注册方法
    private static Item registerItems(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, name), item);
    }
    public static void registerDEVItems() {

    }
}
