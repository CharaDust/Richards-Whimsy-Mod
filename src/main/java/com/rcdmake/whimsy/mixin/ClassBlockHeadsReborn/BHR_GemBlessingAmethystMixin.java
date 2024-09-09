package com.rcdmake.whimsy.mixin.ClassBlockHeadsReborn;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 注意：此类可能是导致游戏启动时报错的原因（已解决）
// 此处代码将对原版 PlayerEntity.class 进行修改
@Mixin(PlayerEntity.class)
public abstract class BHR_GemBlessingAmethystMixin extends LivingEntity {

    // 定义一个玩家变量
    PlayerEntity player = (PlayerEntity) (Object) this;

    // 使用刚才的玩家变量定义一个物品栏（此处定义的物品栏是从 PlayerEntity 调用过来的）
    @Shadow
    private final PlayerInventory inventory = new PlayerInventory(player);

    // super
    protected BHR_GemBlessingAmethystMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    // 对 PlayerEntity.class 中的 getBlockBreakingSpeed 方法进行修改
    @Inject(method = "getBlockBreakingSpeed", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/entity/player/PlayerEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;", shift = At.Shift.AFTER), cancellable = true)
    private void injectGemBlessing(BlockState block, CallbackInfoReturnable<Float> cir) {
        /*
         getBlockBreakingSpeed 方法的原文如此：
        float f = this.inventory.getBlockBreakingSpeed(block);
        if (f > 1.0f) {
            int i = EnchantmentHelper.getEfficiency(this);
            ItemStack itemStack = this.getMainHandStack();
            <插入点>
            if (i > 0 && !itemStack.isEmpty()) {
                f += (float)(i * i + 1);
            }
        }
        * */
        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack itemStack = player.getMainHandStack();
        Float f = cir.getReturnValue();
        f = this.inventory.getBlockBreakingSpeed(block);
        // 如果没有获取到 f 值，则将其重置为 1.0
        if (f == null) {
            f = 1.0f;
        }

        if (itemStack.hasNbt()) {
            NbtCompound nbt = itemStack.getNbt();
            if (nbt != null && nbt.getInt("GemBlessing") >= 1) {
                int i = 0;
                i = EnchantmentHelper.getEfficiency(player);
                // 测试信息：工具原始的附魔和挖掘速度
                // player.sendMessage(Text.literal("(Debug) i = " + (i) + "; f = " + (f)).formatted(Formatting.LIGHT_PURPLE), true);
                i = i + 1;  // 增加效率等级
                if (i > 0) {
                    f += (float)(i * i + 1);
                    // 测试信息：工具修改后的附魔和挖掘速度
                    // player.sendMessage(Text.literal("(Debug) i = " + (i) + "; f = " + (f)).formatted(Formatting.LIGHT_PURPLE), true);
                    cir.setReturnValue(f);
                }
            }
        }
    }
}
