package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_FoolHealth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 愚者模式：把玩家扣血的落点接到两层生命模型上。
 *
 * <h2>为什么目标是 {@code Player} 而不是 {@code LivingEntity}</h2>
 * {@code Player} <b>整段重写</b>了 {@code actuallyHurt}（护甲 → 魔法吸收 → 伤害吸收 →
 * 饥饿消耗 → 战斗记录 → 扣血 → 统计），玩家受伤永远不会走到
 * {@code LivingEntity} 那一份。所以重定向只能打在 {@code Player.actuallyHurt} 里。
 *
 * <h2>为什么重定向 {@code setHealth} 而不是在 HEAD 自己算伤害</h2>
 * {@code setHealth(this.getHealth() - dmg)} 的参数已经是减伤、附魔、吸收全都算完的
 * 「结算后血量」，直接用它就能把「第一层不够的部分」交给第二层，
 * 不用复制一遍护甲/附魔/吸收的算法（复制就会有偏差）。
 *
 * <p>目标描述符的主人按 {@code Player} 写：该方法体里的 {@code this.setHealth(...)}
 * 是 javac 按接收者静态类型发出的 {@code invokevirtual Player.setHealth(F)V}。
 */
@Mixin(Player.class)
public abstract class BHR_FoolHealthDamageMixin {

	@Redirect(
			method = "actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setHealth(F)V"))
	private void whimsy$foolHealthLayer(Player player, float newHealth) {
		BHR_FoolHealth.onPlayerSetHealth(player, newHealth);
	}
}
