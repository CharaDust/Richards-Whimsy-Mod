package com.rcdmake.whimsy.item.ClassBlockHeadRebon;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

// 添加一个食物
public class BHR_FoodOther {
    // 添加一个食物注册 - Low filling foods
    public static final FoodComponent BHRFOODCPNT_LOW = new FoodComponent.Builder()
            // 饥饿值
            .hunger(1)
            // 饱和度
            .saturationModifier(0.1f)
            .build();

    // 添加一个食物注册 - Raw filling foods
    public static final FoodComponent BHRFOODCPNT_RAW = new FoodComponent.Builder()
            // 饥饿值
            .hunger(3)
            // 饱和度
            .saturationModifier(0.4f)
            .build();

    // 添加一个食物注册 - Medium filling foods
    public static final FoodComponent BHRFOODCPNT_MEDIUM = new FoodComponent.Builder()
            // 饥饿值
            .hunger(4)
            // 饱和度
            .saturationModifier(1f)
            .build();
    // 添加物品
    public static final Item BHR_CHERRY = registerItems("bhr_cherry",new Item(new FabricItemSettings().food(BHRFOODCPNT_MEDIUM)));
    public static final Item BHR_COCONUT = registerItems("bhr_coconut",new Item(new FabricItemSettings().food(BHRFOODCPNT_MEDIUM)));
    public static final Item BHR_COFFEE_CHERRY = registerItems("bhr_coffee_cherry",new Item(new FabricItemSettings().food(BHRFOODCPNT_MEDIUM)));
    public static final Item BHR_MANGO = registerItems("bhr_mango",new Item(new FabricItemSettings().food(BHRFOODCPNT_MEDIUM)));
    public static final Item BHR_ORANGE = registerItems("bhr_orange",new Item(new FabricItemSettings().food(BHRFOODCPNT_MEDIUM)));

    // 添加一个食物注册 - High filling foods
    public static final FoodComponent BHRFOODCPNT_HIGH = new FoodComponent.Builder()
            // 饥饿值
            .hunger(8)
            // 饱和度
            .saturationModifier(3f)
            .build();

    // 添加一个食物注册 - Maximum filling foods
    public static final FoodComponent BHRFOODCPNT_MAXIMUM = new FoodComponent.Builder()
            // 饥饿值
            .hunger(15)
            // 饱和度
            .saturationModifier(9f)
            .build();

    // 物品注册方法
    private static Item registerItems(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, name),item);
    }
    // 初始化方法，每一次建立一个文件/初始化方法都需要在主类引用一下
    public static void OnInit() {}
}
