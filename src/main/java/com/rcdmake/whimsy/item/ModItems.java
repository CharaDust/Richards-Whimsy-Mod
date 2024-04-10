package com.rcdmake.whimsy.item;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    // 添加 CRYSTALLIZED_HONEY(结晶蜜) 物品，调用方法为下文注册方法
    public static final Item CRYSTALLIZED_HONEY = registerItems("crystallized_honey", new Item(new FabricItemSettings()));

//    // 添加 DEV_ITEM_NORMAL(开发_普通物品) 物品
//    public static final Item DEV_ITEM_NORMAL = registerItems("dev_item_normal", new Item(new FabricItemSettings()));
    // 添加 GEM_AMETHYST(三方紫晶_1级宝石) 物品
    public static final Item GEM_AMETHYST = registerItems("gem_amethyst", new Item(new FabricItemSettings()));
    // 添加 GEM_SAPPHIRE(蓝宝石_2级宝石) 物品
    public static final Item GEM_SAPPHIRE = registerItems("gem_sapphire", new Item(new FabricItemSettings()));
    // 添加 GEM_EMERALD(绿宝石_3级宝石) 物品
    public static final Item GEM_EMERALD = registerItems("gem_emerald", new Item(new FabricItemSettings()));
    // 添加 GEM_RUBY(红刚玉_4级宝石) 物品
    public static final Item GEM_RUBY = registerItems("gem_ruby", new Item(new FabricItemSettings()));
    // 添加 GEM_DIAMOND(宝石钻石_5级宝石) 物品
    public static final Item GEM_DIAMOND = registerItems("gem_diamond", new Item(new FabricItemSettings()));




    // 物品注册方法
    private static Item registerItems(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, name),item);
    }
    // 主动添加物品至默认物品栏,方法名貌似需要自定义
    private static void addItemToItemGroup(FabricItemGroupEntries fabricItemGroupEntries) {
        fabricItemGroupEntries.add(CRYSTALLIZED_HONEY);
    }
    // 初始化方法，每一次建立一个文件/初始化方法都需要在主类引用一下
    public static void registerModItems() {
        // 主动添加物品至默认物品栏方法使用
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(ModItems::addItemToItemGroup);
    }
}
