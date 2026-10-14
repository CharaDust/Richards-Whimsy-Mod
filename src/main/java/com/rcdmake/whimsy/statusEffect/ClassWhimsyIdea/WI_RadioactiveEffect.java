package com.rcdmake.whimsy.statusEffect.ClassWhimsyIdea;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

/**
 * 电离辐射的<b>效果实现</b>：逐刻扣血 + 生命值上限修饰符的挂/摘。
 *
 * <p>数值表、注册点、以及「为什么这样修掉生命值上限回不来的缺陷」都在
 * {@link WI_Radioactive} 的类注释里，这里只写实现细节。
 *
 * <h2>1. 逐刻伤害</h2>
 * <p>26.2 的映射对照（1.20.4 Yarn → 26.2 mojmap）：
 * <ul>
 *   <li>{@code canApplyUpdateEffect(duration, amplifier)} → {@link #shouldApplyEffectTickThisTick(int, int)}；</li>
 *   <li>{@code applyUpdateEffect(entity, amplifier)} → {@link #applyEffectTick(ServerLevel, LivingEntity, int)}
 *       ——<b>26.2 多了个 {@code ServerLevel} 参数</b>，且返回 boolean。</li>
 * </ul>
 *
 * <p>旧实现把「该不该结算」记在一个<b>实例字段</b> {@code runDamage} 上，在两个方法之间传递状态。
 * 那是个隐患：{@code StatusEffect} 是<b>注册表单例</b>，所有实体共用同一个实例，
 * 字段即全局共享状态（做药水效果代码时最容易踩的一类坑）。这里改成
 * <b>纯函数式判定</b>——{@link #shouldApplyEffectTickThisTick} 只看参数（剩余时间与等级），
 * 不留任何状态，天然与「多少实体在用」无关。
 *
 * <p>间隔判定用 {@code (duration + interval - 1) % interval == 0}：
 * 剩余时间是在递减的，这个式子在 {@code duration ≡ 1 (mod interval)} 时成立，
 * 也就是「效果还剩 1、interval+1、2×interval+1 … tick 时各结算一次」，
 * 对 interval = 100/80/60/40/20 全部成立（已逐个代入验算）。
 *
 * <h2>2. 生命值上限修饰符</h2>
 * <p>覆写基类的 {@link #addAttributeModifiers(AttributeMap, int)} 与
 * {@link #removeAttributeModifiers(AttributeMap)}，而不是调用
 * {@code MobEffect#addAttributeModifier} 注册静态模板——原因（等级与数值被原版乘法绑死）
 * 见 {@link WI_Radioactive} 类注释。
 *
 * <p>这两个覆写点正好是原版<b>全部</b>属性变更路径的收敛处：
 * <pre>{@code
 * LivingEntity#onEffectAdded      -> effect.addAttributeModifiers(attrs, amplifier)      // 挂上
 * LivingEntity#onEffectUpdated    -> removeAttributeModifiers(attrs) 然后 addAttributeModifiers(...)  // 等级变化
 * LivingEntity#onEffectsRemoved   -> effect.removeAttributeModifiers(attrs)              // ★ 移除（唯一收敛点）
 * }</pre>
 * 而 {@code onEffectsRemoved} 被<b>自然到期、{@code removeEffect}（含 /effect clear、喝牛奶）、
 * 死亡清理</b>三条路共同经过。这就是「提前结束也能把上限还回来」的依据。
 *
 * <p>⚠️ 注意 {@link #removeAttributeModifiers(AttributeMap)} <b>不调用 super</b>：
 * 基类实现只遍历通过 {@code addAttributeModifier} 注册的模板表（我们没往里放东西，是空的），
 * 调 super 等于什么都没做，反而会漏摘我们手工加的那个修饰符。
 */
public class WI_RadioactiveEffect extends WI_DamageEffect {

	public WI_RadioactiveEffect() {
		super(WI_Radioactive.category(), WI_Radioactive.COLOR);
	}

	/**
	 * 是否在这一 tick 结算伤害：按等级间隔取模。
	 *
	 * <p>纯参数判定，不写任何实例字段——效果是注册表单例，字段会成为所有实体共享的全局状态
	 * （旧实现的 {@code runDamage} 就是这么写的）。
	 */
	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		int interval = WI_Radioactive.damageIntervalFor(amplifier);
		return interval > 0 && (duration + interval - 1) % interval == 0;
	}

	/**
	 * 结算一次：对持有者造成 2 点火焰伤害。
	 *
	 * <h2>⚠️ 没有生命值保底（与生物毒性不同）</h2>
	 * <p>wiki 给「生物毒性」写了 6 点生命值下限，但<b>没给电离辐射</b>写 ——
	 * 也就是辐射<b>可以致死</b>。本项目曾把生物毒性的下限照抄到这里（一个 6 点门槛），
	 * 那是错误移植，已删除。
	 *
	 * <p>死亡信息的连带效果：伤害源用 {@code inFire()}（火焰伤害），
	 * 于是玩家被辐射打死时看到的正是原版「被烧死」那条
	 * （{@code death.attack.inFire}）——这是符合预期的，wiki 也是这么描述的
	 * （「实质上是火焰伤害」）。若要改成别的死法，应换伤害源，而不是改这里。
	 *
	 * <p>只保留 {@code !isDeadOrDying()} 一道兜底：避免对已在死亡流程里的实体重复结算。
	 *
	 * <p>伤害源用 {@code level.damageSources().inFire()}（Yarn 时代是
	 * {@code entity.getDamageSources().inFire()}；26.2 的伤害源工厂挂在 level 上）。
	 * 走 {@code hurtServer}：这是服务端唯一真正结算伤害的入口（与 RDT 灌注挂的是同一个方法，
	 * 但两个模块各自独立挂钩，见 {@code mixin/ClassWhimsyIdea/WI_RadioactiveHurtMixin} 的说明）。
	 */
	@Override
	public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplifier) {
		if (!mob.isDeadOrDying()) {
			mob.hurtServer(level, level.damageSources().inFire(), WI_Radioactive.DAMAGE_PER_TICK);
		}
		// ⚠️ 必须返回 true —— 这个返回值是「效果是否继续存在」，不是「是否做了额外处理」。
		// 原版 MobEffectInstance#tickServer 的判据（逐行读过）：
		//     if (shouldApplyEffectTickThisTick(...) && !applyEffectTick(...)) return false;
		// 而 LivingEntity#tickEffects 拿到 false 就 iterator.remove() + onEffectsRemoved ——
		// 效果当场被移除。所以返回 false 等于「结算完这一次就删掉我」，
		// 表现出来就是「刚施加没几秒效果自己没了」：等级 I 在 100 tick、III 在 60 tick 时消失。
		// 注意即使因为残血而没造成伤害，也必须返回 true，否则同样会被删。
		return true;
	}

	/**
	 * 挂上生命值上限修饰符。原版在效果被挂上、以及等级变化（先 remove 再 add）时调用。
	 *
	 * <p>id 恒定，所以这里其实是「幂等替换」：重复调用不会叠加出两个修饰符。
	 */
	@Override
	public void addAttributeModifiers(AttributeMap attributes, int amplifier) {
		AttributeInstance maxHealth = attributes.getInstance(
				net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
		if (maxHealth == null) {
			return;
		}
		maxHealth.removeModifier(WI_Radioactive.HEALTH_MODIFIER_ID);
		maxHealth.addPermanentModifier(WI_Radioactive.maxHealthModifier(amplifier));
	}

	/**
	 * 摘掉生命值上限修饰符。这是原版所有移除路径的唯一收敛点
	 * （自然到期 / {@code removeEffect} / 死亡），所以效果被提前结束也能把上限还回来。
	 *
	 * <p><b>刻意不调用 super</b>：基类实现只遍历 {@code addAttributeModifier} 注册的模板表
	 * （本效果没往里放东西，表是空的），调它等于没做，会漏摘我们手工加的那个修饰符。
	 */
	@Override
	public void removeAttributeModifiers(AttributeMap attributes) {
		AttributeInstance maxHealth = attributes.getInstance(
				net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
		if (maxHealth != null) {
			maxHealth.removeModifier(WI_Radioactive.HEALTH_MODIFIER_ID);
		}
	}
}
