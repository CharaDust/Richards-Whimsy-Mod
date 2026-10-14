package com.rcdmake.whimsy.statusEffect.ClassWhimsyIdea;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

/**
 * 生物毒性（状态效果）{@code richards-whimsy-mod:wi_bio_toxicity}。
 *
 * <p>规格来源：{@code doc/wiki/StatusEffect/ClassWhimsyIdea/WI_BioToxicity.md}；
 * 本类取代 1.20.4 时代的 {@code WI_EffectBioToxicity}（wiki 已标 {@code #deprecated}）。
 *
 * <h2>本类负责什么</h2>
 * <ol>
 *   <li>按等级间隔，对持有者造成 2 点<b>魔法</b>伤害（I 每 80 tick … IV 起每 20 tick）——
 *       在 {@link WI_BioToxicityEffect#applyEffectTick}；</li>
 *   <li>正向传染：持有者攻击时把低一级、半时长的同名效果传给目标——不在本类，
 *       见 {@link WI_BioToxicityAttackHandler}。</li>
 * </ol>
 * 相比电离辐射，本效果<b>不动属性</b>（没有生命值上限修饰符），所以类结构简单得多。
 *
 * <h2>wiki 只描述了 II 级及以上的传染</h2>
 * <p>wiki 正文：「当攻击者拥有 <b>II 级及以上</b>的状态效果攻击目标时，对目标施加攻击者
 * {@code 等级-1} 等级、{@code 剩余时间/2} 时间的同名效果」。
 * 也就是<b>等级 I 不传染</b>——传出去会是 0 级，那没意义。
 * 旧实现里 I 级那条走的是「给目标挂原版中毒 240 tick」的分支，
 * 这既不在 wiki 描述里、也和本效果的机制（同名效果传染）不是一回事，
 * 本次按 wiki 删除该分支（详见 {@link WI_BioToxicityAttackHandler}）。
 *
 * <h2>登记点</h2>
 * <p>{@code RichardsWhimsyMod.onInitialize()} → {@link #OnInit()}。
 */
public class WI_BioToxicity {

	/** 粒子颜色（wiki：{@code 0x007951}）。 */
	public static final int COLOR = 0x007951;

	/** 每次结算造成的伤害值（wiki 数值表：所有等级均为 2）。 */
	public static final float DAMAGE_PER_TICK = 2.0F;

	/**
	 * 结算时要求的最低生命值：血量必须<b>大于</b>这个值才吃伤害，
	 * 避免「毒性本身把生物打死」（与旧实现 {@code getHealth() > 6.0F} 口径一致）。
	 */
	public static final float MIN_HEALTH_TO_DAMAGE = 6.0F;

	/**
	 * 伤害间隔（tick），索引 = {@code amplifier}（0 = 等级 I）。
	 * <p>wiki：I 80 / II 60 / III 40 / IV 20 / V 20 / $x$ 20。
	 */
	private static final int[] DAMAGE_INTERVALS = {80, 60, 40, 20, 20};

	/** 注册后的效果句柄；1.21 起原版各处都收 {@code Holder<MobEffect>}。 */
	public static Holder<MobEffect> WI_BIO_TOXICITY;

	private WI_BioToxicity() {
	}

	/** 按 wiki 表取该等级的伤害间隔；超出表长（≥ V 级）按最高档处理。 */
	public static int damageIntervalFor(int amplifier) {
		return DAMAGE_INTERVALS[Math.min(Math.max(amplifier, 0), DAMAGE_INTERVALS.length - 1)];
	}

	/** 注册本效果。由主类在 {@code onInitialize()} 里调用，只调一次。 */
	public static void OnInit() {
		// 声音事件必须先注册好，才能把它挂到效果上（顺序不能反）。
		WI_Sounds.OnInit();
		WI_BIO_TOXICITY = ModRegister.registerStatusEffect(
				"wi_bio_toxicity",
				// withSoundOnAdded 是原版自带的「效果被挂上时响一声」钩子（见 WI_Sounds 类注释）。
				new WI_BioToxicityEffect().withSoundOnAdded(WI_Sounds.WI_BIO_TOXICITY_APPLIED));
	}

	/** 分类与颜色（供 {@link WI_BioToxicityEffect} 构造器使用）。 */
	static net.minecraft.world.effect.MobEffectCategory category() {
		return net.minecraft.world.effect.MobEffectCategory.HARMFUL;
	}

	/** 若实体身上已有本效果，返回其效果实例；没有则 null。给攻击处理器查等级/剩余时间用。 */
	public static net.minecraft.world.effect.MobEffectInstance instanceOf(
			net.minecraft.world.entity.LivingEntity entity) {
		return WI_BIO_TOXICITY == null ? null : entity.getEffect(WI_BIO_TOXICITY);
	}
}
