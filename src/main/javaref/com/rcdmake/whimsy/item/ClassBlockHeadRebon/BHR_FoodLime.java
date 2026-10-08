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

public class BHR_FoodLime {
    // 添加一个食物注册
    public static final FoodComponent BHRFOODCPNT_LIME = new FoodComponent.Builder()
            // 饥饿值
            .hunger(3)
            // 饱和度
            .saturationModifier(1f)
            // 食用后效果
            .statusEffect(
                    // 药水效果
                    new StatusEffectInstance(
                            // 急迫，20秒，2级
                            StatusEffects.HASTE,400,1
                    ),
                    // 获得效果几率
                    0.2f
            )
            .build();

    // 添加一个物品
    public static final Item BHR_LIME = registerItems("bhr_lime",new Item(new FabricItemSettings().food(BHRFOODCPNT_LIME)));

    // 物品注册方法
    private static Item registerItems(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, name),item);
    }
    // 初始化方法，每一次建立一个文件/初始化方法都需要在主类引用一下
    public static void OnInit() {}
}
