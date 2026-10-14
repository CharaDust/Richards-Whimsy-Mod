package com.rcdmake.whimsy.statusEffect.ClassRandomThings;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 火焰灌注（状态效果）{@code richards-whimsy-mod:rdt_fire_imbue}。
 *
 * <p>规格来源：{@code doc/wiki/StatusEffect/ClassRandomThings/RDT_FireImbue.md}；
 * 本类取代 1.20.4 时代的 {@code RDT_EffectFireImbue}（wiki 已标 {@code #deprecated}）。
 *
 * <h2>它自己不做任何事</h2>
 * <p>这个效果<b>没有</b>逐刻作用：{@code MobEffect.applyEffectTick} 保持原版的 {@code false}，
 * 所以服务端每刻都不会为它白跑一趟。真正的行为在<b>攻击结算</b>里
 * （{@link RDT_ImbueAttackHandler#imbueOnAttack}），一次性给目标点火，不占用持续 tick。
 *
 * <table border="1">
 *   <caption>构造（取自 wiki）</caption>
 *   <tr><th>条目</th><th>值</th></tr>
 *   <tr><td>类型</td><td>{@link MobEffectCategory#BENEFICIAL}</td></tr>
 *   <tr><td>粒子颜色</td><td>{@code 0xF68127}</td></tr>
 * </table>
 *
 * <h2>26.2 的映射对照（1.20.4 Yarn → 26.2 mojmap）</h2>
 * <ul>
 *   <li>{@code StatusEffect} → {@link MobEffect}；{@code StatusEffectCategory} → {@link MobEffectCategory}；
 *       {@code StatusEffects} → {@code MobEffects}。</li>
 *   <li>注册表 {@code Registries.STATUS_EFFECT} → {@link BuiltInRegistries#MOB_EFFECT}；
 *       {@code new Identifier(ns, path)} → {@link ModRegister#id(String)}。</li>
 *   <li>{@code canApplyUpdateEffect} / {@code applyUpdateEffect} 这对方法在 26.2 是
 *       {@code shouldApplyEffectTickThisTick} / {@code applyEffectTick} —— 本效果两者都不需要覆写。</li>
 * </ul>
 *
 * <h2>登记点</h2>
 * <p>{@code RichardsWhimsyMod.onInitialize()} → {@link #OnInit()}。注册值存进
 * {@link #RDT_FIRE_IMBUE}（{@code Holder<MobEffect>}），直接交给
 * {@code LivingEntity#hasEffect(Holder)} / {@code MobEffectInstance} 使用。
 */
public class RDT_FireImbue {

	/** 粒子颜色（wiki：{@code 0xF68127}）。 */
	public static final int COLOR = 0xF68127;

	/** 攻击判定用的一次点火基数：每级 {100} tick（wiki 数值表）。 */
	public static final int FIRE_TICKS_PER_LEVEL = 100;

	/** 注册后的效果句柄；1.21 起原版各处都收 {@code Holder<MobEffect>}，所以这里存 Holder。 */
	public static Holder<MobEffect> RDT_FIRE_IMBUE;

	private RDT_FireImbue() {
	}

	/** 构造效果实例（分类：正面；实例类见 {@link RDT_FireImbueEffect}）。 */
	private static MobEffect create() {
		return new RDT_FireImbueEffect();
	}

	/** 注册本效果。由主类在 {@code onInitialize()} 里调用，只调一次。 */
	public static void OnInit() {
		RDT_FIRE_IMBUE = ModRegister.registerStatusEffect("rdt_fire_imbue", create());
	}

	/**
	 * 按等级换算点火时长：等级 I（amplifier 0）→ 100 tick，等级 II → 200 tick，依此类推。
	 *
	 * <p>原版把 I 级记为 {@code amplifier = 0}，wiki 的表格按「等级」写，所以这里统一 {@code +1}
	 * 之后再做乘法——{@code (amplifier + 1) * 100} 正好等于 wiki 的 {@code x * 100}。
	 */
	public static int fireTicksFor(int amplifier) {
		return (amplifier + 1) * FIRE_TICKS_PER_LEVEL;
	}
}
