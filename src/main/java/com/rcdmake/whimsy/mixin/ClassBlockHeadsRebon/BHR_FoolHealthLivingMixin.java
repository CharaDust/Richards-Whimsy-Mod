package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_FoolHealth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 愚者模式：治疗顺序 与 图腾换算。
 *
 * <h2>为什么打在 {@code LivingEntity} 上就够了</h2>
 * 与伤害不同，{@code heal(float)} <b>没有被 Player 重写</b>：
 * 原版自然回复（{@code FoodData#tick}）、和平难度回血
 * （{@code ServerPlayer#tickRegeneration}）、药水、信标全都走这一个方法。
 * 在这里换成「先补濒死值、再补生命值」即可，不需要逐个调用点去改。
 *
 * <h2>图腾</h2>
 * 第二层见底时我们把真实结算值放行，原版于是走
 * {@code hurtServer} 里的死亡分支 → {@code checkTotemDeathProtection}。
 * 它在返回 true 时已经把血量设成 1.0；在两层模型里那 1 点应当算濒死值，
 * 所以 RETURN 处把它换算过去（{@link BHR_FoolHealth#onTotemUsed}）。
 */
@Mixin(LivingEntity.class)
public abstract class BHR_FoolHealthLivingMixin {

	/** 治疗改道：愚者模式下由 {@link BHR_FoolHealth#heal} 接管，取消原版处理。 */
	@Inject(method = "heal(F)V", at = @At("HEAD"), cancellable = true)
	private void whimsy$foolHealOrder(float amount, CallbackInfo ci) {
		if ((Object) this instanceof ServerPlayer player && BHR_FoolHealth.isFoolMode(player)) {
			BHR_FoolHealth.heal(player, amount);
			ci.cancel();
		}
	}

	/** 图腾生效后，把原版回的那点血量换算成濒死值。 */
	@Inject(method = "checkTotemDeathProtection", at = @At("RETURN"))
	private void whimsy$foolTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ() && (Object) this instanceof ServerPlayer player) {
			BHR_FoolHealth.onTotemUsed(player);
		}
	}
}
