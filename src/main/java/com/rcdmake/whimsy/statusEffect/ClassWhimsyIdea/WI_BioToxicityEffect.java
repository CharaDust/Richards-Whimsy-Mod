package com.rcdmake.whimsy.statusEffect.ClassWhimsyIdea;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * 生物毒性的<b>效果实现</b>：逐刻扣血。不动任何属性，所以比电离辐射简单。
 *
 * <p>数值表与注册点在 {@link WI_BioToxicity}，传染逻辑在 {@link WI_BioToxicityAttackHandler}。
 *
 * <h2>逐刻判定与电离辐射同一套写法</h2>
 * <p>{@link #shouldApplyEffectTickThisTick} 是纯参数判定（只看剩余时间与等级），
 * 不写实例字段——{@code MobEffect} 是注册表单例，字段会成为所有实体共享的全局状态；
 * 旧实现用的 {@code runDamage} 字段正是这个隐患。
 * 间隔成立刻用 {@code (duration + interval - 1) % interval == 0}
 * （即「剩余 1、interval+1、2×interval+1 … tick 时各结算一次」），
 * 对 80/60/40/20 逐个代入验算成立。
 *
 * <p>伤害用魔法伤害（wiki 明确写「魔法伤害」），与电离辐射的火焰伤害区分开。
 */
public class WI_BioToxicityEffect extends WI_DamageEffect {

	public WI_BioToxicityEffect() {
		super(WI_BioToxicity.category(), WI_BioToxicity.COLOR);
	}

	/** 是否在这一 tick 结算伤害：按等级间隔取模（纯参数判定，无状态）。 */
	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		int interval = WI_BioToxicity.damageIntervalFor(amplifier);
		return interval > 0 && (duration + interval - 1) % interval == 0;
	}

	/**
	 * 结算一次：对持有者造成 2 点魔法伤害。
	 *
	 * <h2>两道「不扣血」的门槛（都要满足才扣）</h2>
	 * <ol>
	 *   <li>血量必须<b>大于</b> {@link WI_BioToxicity#MIN_HEALTH_TO_DAMAGE}（6）——
	 *       残血时停手，不让毒性直接把人打死；</li>
	 *   <li>持有者<b>不在免疫名单</b>里 —— 名单见
	 *       {@link WI_BioToxicityAttackHandler}。免疫生物<b>可以</b>带这个效果
	 *       （药水/指令都挂得上，也仍能把毒性传染给别人），但自己不吃伤害。
	 *       所以这里只是「跳过扣血」，绝不能连效果一起撤销。</li>
	 * </ol>
	 */
	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplifier) {
		if (!mob.isDeadOrDying()
				&& mob.getHealth() > WI_BioToxicity.MIN_HEALTH_TO_DAMAGE
				&& WI_BioToxicityAttackHandler.shouldTakeDamage(mob)) {
			mob.hurtServer(level, level.damageSources().magic(), WI_BioToxicity.DAMAGE_PER_TICK);
		}
		// ⚠️ 必须返回 true —— 这个返回值是「效果是否继续存在」，不是「是否做了额外处理」。
		// 原版 MobEffectInstance#tickServer：
		//     if (shouldApplyEffectTickThisTick(...) && !applyEffectTick(...)) return false;
		// 返回 false → LivingEntity#tickEffects 里 iterator.remove()，效果当场被删。
		// 所以返回 false 会让效果在第一次结算时自我了断
		// （I 级 80 tick ≈ 4 秒、IV 级 20 tick ≈ 1 秒后就消失）。
		// 即便因残血而未造成伤害，也必须返回 true。
		return true;
	}
}
