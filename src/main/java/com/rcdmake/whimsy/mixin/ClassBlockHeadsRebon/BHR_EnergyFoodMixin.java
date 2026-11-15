package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_Energy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 愚者模式：吃东西补精力值。
 *
 * <p>26.2 的「吃」不再走 {@code Player#eat}，而是 {@code minecraft:consumable} 组件驱动：
 * 食用结束时 {@code Consumable#onConsume} 会把身上所有 {@code ConsumableListener} 挨个回调，
 * 其中 {@link FoodProperties} 就是那一个 —— 它负责把饥饿值/饱和度加上去
 * （{@code player.getFoodData().eat(this)}），也会放「吃东西」音效。
 *
 * <p>所以挂在 {@code FoodProperties#onConsume} 的 RETURN 上最稳：
 * <ul>
 *   <li>一叠食物被吃掉的<b>那一次</b>只会回调一次（不像 {@code FoodData#eat} 有重载）；</li>
 *   <li>参数里现成就有 {@code stack}（判断是不是咖啡樱桃）与 {@code user}；</li>
 *   <li>{@code Consumable#onConsume} 在<b>客户端也会</b>回调一次监听器
 *       （那段 {@code forEach} 在 {@code isClientSide} 判断之外），
 *       所以这里必须自己挡掉客户端，否则客户端那份数值会和服端打架。</li>
 * </ul>
 */
@Mixin(FoodProperties.class)
public abstract class BHR_EnergyFoodMixin {

	@Inject(
			method = "onConsume(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;"
					+ "Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/component/Consumable;)V",
			at = @At("RETURN"))
	private void whimsy$restoreEnergy(Level level, LivingEntity user, ItemStack stack,
			Consumable consumable, CallbackInfo ci) {
		// 只认服务端（数值以服务端为准，客户端只读同步过来的附件）
		if (level.isClientSide()) {
			return;
		}
		if (user instanceof ServerPlayer player) {
			BHR_Energy.onEaten(player, (FoodProperties) (Object) this, stack);
		}
	}
}
