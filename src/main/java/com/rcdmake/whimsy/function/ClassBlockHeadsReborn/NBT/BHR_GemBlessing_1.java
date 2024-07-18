package com.rcdmake.whimsy.function.ClassBlockHeadsReborn.NBT;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;


public class BHR_GemBlessing_1 {

    public static void addGemBlessing_1(ItemStack itemStack){
        NbtCompound nbt = itemStack.getOrCreateNbt();
        nbt.putInt("GemBlessing", 1);
        itemStack.setNbt(nbt);
    }
    public static void OnInit(){
    }

}
