package com.rcdmake.whimsy.statusEffect.ClassRandomThings;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 「灌注」类状态效果的<b>攻击结算</b>：攻击者身上挂了哪些灌注，就给被打中的目标补上对应效果。
 *
 * <p>行为规格来自 wiki：{@code RDT_FireImbue.md}（火焰灌注）、{@code RDT_PoisonImbue.md}（毒性灌注）。
 *
 * <h2>为什么不再是事件回调</h2>
 * <p>1.20.4 旧实现（{@code function/ClassRandomThings/Effect/Handler/RDT_ImbueAttackEventHandler}）
 * 挂在 {@code AttackEntityCallback} 上。那个回调是「<b>玩家</b>左键点实体」的钩子，
 * 所以旧版只有玩家手里的灌注会生效，怪物身上有同样的效果却打不出燃烧/中毒——
 * 这正是本次重构要修掉的点：<b>对所有生物生效</b>。
 *
 * <p>26.2 的攻击结算路径是<b>分散</b>的：玩家走 {@code Player.attack(Entity)} 与
 * {@code Player.stabAttack(...)}（26.x 新增的「刺击」），怪物走 {@code Mob.doHurtTarget(...)}，
 * 而 {@code Creeper} / {@code IronGolem} 这类覆写后干脆自己结算、不经过 {@code super}。
 * 逐条挂事件都盖不全，所以挂点收敛到唯一的共同下游 {@code LivingEntity.hurtServer}：
 * {@code mixin/ClassRandomThings/RDT_ImbueHurtMixin} 在伤害落地后按
 * {@code DamageSource.getEntity()} 反查攻击者，再调本方法。
 * 判定逻辑只写在这一处，与挂点如何变化无关。
 *
 * <h2>为什么只在服务端跑</h2>
 * <p>{@code hurtServer} 只在服务端被调用，正常路径下客户端不会进来；
 * 这里的 {@code isClientSide()} 是一道防御性闸门，保证这段逻辑将来被挪到双端路径时
 * 也不会在客户端本地重复施加一遍效果。
 */
public final class RDT_ImbueAttackHandler {

	private RDT_ImbueAttackHandler() {
	}

	/**
	 * 攻击命中的统一入口：把攻击者身上的灌注效果施加给目标。
	 *
	 * @param attacker 造成伤害的生物（玩家、怪物，或投射物的 owner）
	 * @param victim   被伤害的生物
	 *
	 * <p>两个效果各自独立判定，可以同时存在——同时挂火焰灌注与毒性灌注时会一次打出两种效果。
	 */
	public static void imbueOnAttack(LivingEntity attacker, Entity victim) {
		if (attacker == null || victim == null) {
			return;
		}
		// 攻击者就是目标：自伤（以及以受伤者自身为攻击者的反伤/荆棘）不触发灌注。
		if (attacker == victim) {
			return;
		}
		// 挂点虽在服务端方法（hurtServer）上，这里仍留一道闸，避免将来被挪进双端路径。
		if (attacker.level().isClientSide()) {
			return;
		}
		if (!(victim instanceof LivingEntity livingVictim)) {
			return;
		}

		applyFireImbue(attacker, livingVictim);
		applyPoisonImbue(attacker, livingVictim);
	}

	/** 火焰灌注：按攻击者的效果等级决定点火时长（每级 100 tick）。 */
	private static void applyFireImbue(LivingEntity attacker, LivingEntity victim) {
		if (RDT_FireImbue.RDT_FIRE_IMBUE == null) {
			return;
		}
		MobEffectInstance imbue = attacker.getEffect(RDT_FireImbue.RDT_FIRE_IMBUE);
		if (imbue == null) {
			return;
		}
		// igniteForTicks 自身就是「取更长者」（原版：remainingFireTicks < numberOfTicks 才写），
		// 所以这里不需要再比一次，也不会把目标身上更久的燃烧缩短。
		victim.igniteForTicks(RDT_FireImbue.fireTicksFor(imbue.getAmplifier()));
	}

	/** 毒性灌注：按攻击者的效果等级查表挂原版中毒（时长与中毒等级都是双轴查表）。 */
	private static void applyPoisonImbue(LivingEntity attacker, LivingEntity victim) {
		if (RDT_PoisonImbue.RDT_POISON_IMBUE == null) {
			return;
		}
		MobEffectInstance imbue = attacker.getEffect(RDT_PoisonImbue.RDT_POISON_IMBUE);
		if (imbue == null) {
			return;
		}
		int amplifier = imbue.getAmplifier();
		victim.addEffect(new MobEffectInstance(
				MobEffects.POISON,
				RDT_PoisonImbue.poisonTicksFor(amplifier),
				RDT_PoisonImbue.poisonAmplifierFor(amplifier)), attacker);
	}
}
