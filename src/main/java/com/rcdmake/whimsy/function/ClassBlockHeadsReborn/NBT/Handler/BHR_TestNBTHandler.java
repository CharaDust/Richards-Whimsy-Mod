package com.rcdmake.whimsy.function.ClassBlockHeadsReborn.NBT.Handler;

import com.rcdmake.whimsy.function.ClassBlockHeadsReborn.NBT.BHR_GemBlessing_1;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class BHR_TestNBTHandler implements UseItemCallback {
    @Override
    public TypedActionResult<ItemStack> interact(PlayerEntity player, World world, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        // 添加物品标签
        BHR_GemBlessing_1.addGemBlessing_1(itemStack);
        return new TypedActionResult<>(ActionResult.SUCCESS, itemStack);
    }

    // OnInit
    public static void OnInit(){
        UseItemCallback.EVENT.register(new BHR_TestNBTHandler());
    }
}
