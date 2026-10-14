package com.rcdmake.whimsy.statusEffect.ClassRandomThings;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 毒性灌注（状态效果）{@code richards-whimsy-mod:rdt_poison_imbue}。
 *
 * <p>规格来源：{@code doc/wiki/StatusEffect/ClassRandomThings/RDT_PoisonImbue.md}；
 * 本类取代 1.20.4 时代的 {@code RDT_EffectPoisonImbue}（wiki 已标 {@code #deprecated}）。
 *
 * <h2>它自己不做任何事</h2>
 * <p>与原版「中毒」不同，本效果不会逐刻扣血——它只是一个<b>攻击附着标记</b>：
 * 服务端每次攻击结算时查一次 {@code hasEffect}，命中就给目标挂原版中毒
 * （见 {@link RDT_ImbueAttackHandler#imbueOnAttack}）。
 * 所以 {@code MobEffect.applyEffectTick} 保持原版的 {@code false}，不白跑逐刻逻辑。
 *
 * <table border="1">
 *   <caption>构造（取自 wiki）</caption>
 *   <tr><th>条目</th><th>值</th></tr>
 *   <tr><td>类型</td><td>{@link MobEffectCategory#BENEFICIAL}</td></tr>
 *   <tr><td>粒子颜色</td><td>{@code 0x41DD53}</td></tr>
 * </table>
 *
 * <h2>数值是「双轴」表，不能用公式</h2>
 * <p>火焰灌注只随时间线性增长，毒性灌注不是：wiki 表中 IV / V 级与 II / III 级的<b>中毒等级</b>相同
 * （都是 II），只有持续时间在 I → II 时翻倍（450 → 900），此后保持不变。这属于离散表，
 * 因此这里照抄成数组查表（见 {@link #POISON_TICKS} / {@link #POISON_AMPLIFIERS}），
 * 不写成乘除公式——公式化会把 IV/V 级算错。
 *
 * <h2>登记点</h2>
 * <p>{@code RichardsWhimsyMod.onInitialize()} → {@link #OnInit()}。
 */
public class RDT_PoisonImbue {

	/** 粒子颜色（wiki：{@code 0x41DD53}）。 */
	public static final int COLOR = 0x41DD53;

	/**
	 * 各等级（索引 = {@code amplifier}，即 I 级为 0）附着的中毒时长，单位 tick。
	 * <p>wiki：I → 450、II → 900、III → 900、IV → 900、V → 900、$x$ → 900。
	 * 表尾固定 900，所以 <b>VI 级及以上全部按 V 级处理</b>（见 {@link #poisonTicksFor}）。
	 */
	private static final int[] POISON_TICKS = {450, 900, 900, 900, 900};

	/**
	 * 各等级附着的原版中毒等级（同样按 {@code amplifier} 计，0 = 中毒 I）。
	 * <p>wiki：I → 中毒 I、II → I、III → II、IV → II、V → II。
	 */
	private static final int[] POISON_AMPLIFIERS = {0, 0, 1, 1, 1};

	/** 注册后的效果句柄；1.21 起原版各处都收 {@code Holder<MobEffect>}，所以这里存 Holder。 */
	public static Holder<MobEffect> RDT_POISON_IMBUE;

	private RDT_PoisonImbue() {
	}

	/** 构造效果实例（分类：正面；实例类见 {@link RDT_PoisonImbueEffect}）。 */
	private static MobEffect create() {
		return new RDT_PoisonImbueEffect();
	}

	/** 注册本效果。由主类在 {@code onInitialize()} 里调用，只调一次。 */
	public static void OnInit() {
		RDT_POISON_IMBUE = ModRegister.registerStatusEffect("rdt_poison_imbue", create());
	}

	/**
	 * 按等级查表得到中毒时长（tick）。
	 *
	 * @param amplifier 效果等级，0 = 等级 I；超出表长（≥ VI 级）按最高档（V 级）处理
	 */
	public static int poisonTicksFor(int amplifier) {
		return POISON_TICKS[Math.min(Math.max(amplifier, 0), POISON_TICKS.length - 1)];
	}

	/**
	 * 按等级查表得到要附着的中毒等级（返回的是 {@code amplifier} 口径，0 = 中毒 I）。
	 *
	 * @param amplifier 效果等级，0 = 等级 I；超出表长（≥ VI 级）按最高档（V 级）处理
	 */
	public static int poisonAmplifierFor(int amplifier) {
		return POISON_AMPLIFIERS[Math.min(Math.max(amplifier, 0), POISON_AMPLIFIERS.length - 1)];
	}
}
