package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_LegacyHunger;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * <b>攻击</b>的饥饿消耗回退：{@code 0.1/次} → 1.8 的 {@code 0.3/次}。
 *
 * <p>数值表与背景见 {@link BHR_LegacyHunger}；同批其它条目见
 * {@code BHR_LegacyHungerMovementMixin} / {@code BHR_LegacyHungerBlockMixin} /
 * {@code BHR_LegacyHungerEffectMixin}。
 *
 * <h2>为什么两条路径都要改</h2>
 * 26.2 有两条攻击结算路径：常规 {@code attack(Entity)} 与「刺击」
 * {@code stabAttack(EquipmentSlot, Entity, float, boolean, boolean, boolean)}，
 * 两处都是硬编码 {@code 0.1f}。只改一条会让「攻击消耗」口径不一致，所以两条都注入。
 *
 * <h2>受伤消耗：刻意不动</h2>
 * {@code Player.actuallyHurt} 的受伤消耗走 {@code DamageSource.getFoodExhaustion()}
 * （26.2 按伤害类型数据驱动），本次按需求<b>不回退</b>，所以本类不含那一处。
 *
 * <h2>为什么是 {@code @Redirect} 而不是 {@code @ModifyArg}</h2>
 * 实测（mixin 0.8.7）：{@code @ModifyArg} 在本类报
 * 「targets a method with an invalid signature (F), expected (FLnet/…Entity;)」——
 * 它只把<b>原方法的参数</b>接到处理方法尾部，拿不到 {@code @At} 那个方法的实参。
 * {@code @Redirect} 的签名要求就是 {@code (receiver, 原实参...)}，这里正合适。
 *
 * <p>⚠️ 用 {@code targets} 字符串而不是类字面量 —— 理由见
 * {@code BHR_LegacyHungerMixin} 的类注释（一个 {@code @Mixin} 只挂一个目标类）。
 */
@Mixin(targets = "net.minecraft.world.entity.player.Player")
public class BHR_LegacyHungerAttackMixin {

	/** 常规攻击：{@code 0.1/次} → {@code 0.3/次}。 */
	@Redirect(
			method = "attack(Lnet/minecraft/world/entity/Entity;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"),
			require = 0)
	private void bhr$attack(Player self, float original) {
		self.causeFoodExhaustion(bhr$ruleOn(self) ? BHR_LegacyHunger.LEGACY_ATTACK : original);
	}

	/** 刺击（26.2 的第二条攻击路径）：与常规攻击同值。 */
	@Redirect(
			method = "stabAttack(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/entity/Entity;FZZZ)Z",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"),
			require = 0)
	private void bhr$stabAttack(Player self, float original) {
		self.causeFoodExhaustion(bhr$ruleOn(self) ? BHR_LegacyHunger.LEGACY_ATTACK : original);
	}

	/** 读规则：{@code bhr_legacy_hunger_consumption} 是否开启；取不到服务端就不改行为。 */
	private static boolean bhr$ruleOn(Player player) {
		return player != null && player.level() != null
				&& BHR_LegacyHunger.enabled(player.level().getServer());
	}
}
