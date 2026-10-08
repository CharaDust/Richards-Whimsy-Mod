package com.rcdmake.whimsy.function.ClassBlockHeadsReborn.NBT.Handler;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypedActionResult;

public class BHR_GemBlessingToolTip {
    // OnInit
    public static void OnInit(){
        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if (stack.hasNbt()) {
                switch (stack.getNbt().getInt("GemBlessing")){
                    case 1:lines.add(Text.translatable("tooltip.richards-whimsy-mod.bhr_gem_blessing_1").formatted(Formatting.LIGHT_PURPLE));
                        break;
                    case 2:lines.add(Text.translatable("tooltip.richards-whimsy-mod.bhr_gem_blessing_2").formatted(Formatting.BLUE));
                        break;
                    case 3:lines.add(Text.translatable("tooltip.richards-whimsy-mod.bhr_gem_blessing_3").formatted(Formatting.GREEN));
                        break;
                    case 4:lines.add(Text.translatable("tooltip.richards-whimsy-mod.bhr_gem_blessing_4").formatted(Formatting.RED));
                        break;
                    case 5:lines.add(Text.translatable("tooltip.richards-whimsy-mod.bhr_gem_blessing_5").formatted(Formatting.AQUA));
                        break;
                    default:
                }
            }

        });


    }
}
