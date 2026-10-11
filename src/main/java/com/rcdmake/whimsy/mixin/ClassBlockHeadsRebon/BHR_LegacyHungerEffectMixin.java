package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_LegacyHunger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * <b>饥饿效果</b>（{@code minecraft:hunger}）的消耗回退：原版
 * {@code 0.005f * (等级 + 1)} 每 tick → 1.8 的 {@code 0.5} 每等级每秒
 * （= {@code 0.025} 每等级每 tick，正好是把基数放大 5 倍）。
 *
 * <p>数值表与背景见 {@link BHR_LegacyHunger}；同批其它条目见
 * {@code BHR_LegacyHungerMovementMixin} / {@code BHR_LegacyHungerBlockMixin} /
 * {@code BHR_LegacyHungerAttackMixin}。
 *
 * <h2>为什么单独一个类、且用 {@code targets} 字符串</h2>
 * 26.2 的 {@code net.minecraft.world.effect.HungerMobEffect} 是<b>包级私有</b>
 * （写进 {@code @Mixin} 的类字面量数组会直接编译失败：「不是公共的；无法从外部程序包中对其进行访问」）。
 * 用 {@code targets} 字符串即可，代价是本类不能在 Java 代码里引用那个类型 —— 好在不需要：
 * 描述符与 {@code @At} 都只用公共类型。
 *
 * <h2>为什么是 {@code @Redirect} 而不是 {@code @ModifyArg}</h2>
 * 实测（mixin 0.8.7）：{@code @ModifyArg} 在这里报
 * 「targets a method with an invalid signature (F), expected (FLnet/…ServerLevel;L…LivingEntity;I)」。
 * {@code @ModifyArg} 要按目标方法的参数表补齐尾部参数，而这个包级私有静态方法的
 * 参数在 Mixin 眼里对不上；{@code @Redirect} 只需要
 * {@code (receiver, 原实参...)}，这里没有 receiver，就是 {@code (float)}，反而最稳。
 *
 * <h2>为什么改最终实参、而不是那个 {@code 0.005f} 常量</h2>
 * 那个常量是 {@code static final} 内联值，改它要用 {@code @ModifyConstant}；
 * 而最终实参里已经乘过 {@code (等级 + 1)}，把基数放大 5 倍即可 ——
 * 既简单，又<b>保住了原版的等级线性缩放</b>（等级越高消耗越多这条设计不变）。
 *
 * <h2>⚠️ 未在游戏内验证</h2>
 * 只核对了注入点，没有实际跑游戏观察饥饿效果下的饥饿条掉速。
 */
@Mixin(targets = "net.minecraft.world.effect.HungerMobEffect")
public class BHR_LegacyHungerEffectMixin {

	/**
	 * 饥饿效果每 tick 的消耗：原版 {@code 0.005 * (等级+1)} → 1.8 的
	 * {@code 0.025 * (等级+1)}（即 0.5 每等级每秒）。
	 */
	@Redirect(
			method = "applyEffectTick(Lnet/minecraft/server/level/ServerLevel;"
					+ "Lnet/minecraft/world/entity/LivingEntity;I)Z",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"),
			require = 0)
	private void bhr$hungerEffect(net.minecraft.world.entity.player.Player self, float original) {
		// 规则查不到服务端时按「不改行为」处理：宁可少改，也不要空指针
		boolean legacy = self != null && self.level() != null
				&& BHR_LegacyHunger.enabled(self.level().getServer());
		self.causeFoodExhaustion(legacy ? original * 5.0F : original);
	}
}
