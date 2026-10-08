package com.rcdmake.whimsy.item.ClassBlockHeadRebon;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BHR_FuelEmber extends Item{
    public BHR_FuelEmber(Settings settings) {
        super(settings);
    }

    public static Item BHR_EMBER = registerItems("bhr_ember", new Item(new FabricItemSettings()));
    // 物品注册方法
    private static Item registerItems(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, name),item);
    }
    // 工具提示
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("tooltip.richards-whimsy-mod.fuel_1"));
        super.appendTooltip(stack, world, tooltip, context);
    }

    // OnInit
    public static void OnInit() {
        FuelRegistry.INSTANCE.add(BHR_EMBER, 2*200);
    }
}
