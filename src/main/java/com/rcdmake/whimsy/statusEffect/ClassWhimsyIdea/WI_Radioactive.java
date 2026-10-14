package com.rcdmake.whimsy.statusEffect.ClassWhimsyIdea;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 电离辐射（状态效果）{@code richards-whimsy-mod:wi_radioactive}。
 *
 * <p>规格来源：{@code doc/wiki/StatusEffect/ClassWhimsyIdea/WI_Radioactive.md}；
 * 本类取代 1.20.4 时代的 {@code WI_EffectRadioactive}（wiki 已标 {@code #deprecated}）。
 *
 * <h2>本类负责什么</h2>
 * <ol>
 *   <li>按等级间隔，对持有者造成 2 点<b>火焰</b>伤害（I 每 100 tick … V 每 20 tick）——在
 *       {@link WI_RadioactiveEffect#applyEffectTick}；</li>
 *   <li>按等级<b>减少生命值上限</b>（I 15% / II 30% / III 45% / IV 60% / V 60%）——在
 *       {@link WI_RadioactiveEffect#addAttributeModifiers} / {@code removeAttributeModifiers}；</li>
 *   <li>反向传染（被 II 级及以上者被攻击时传染给攻击者）——不在本类，
 *       见 {@link WI_RadioactiveAttackHandler}。</li>
 * </ol>
 * 本类只放「数值表 + 注册 + 供处理器使用的小工具」，行为都在效果子类里。
 *
 * <h2>⚠️ 缺陷修复（本次重构的重点）：生命值上限「少掉回不来」</h2>
 * <p><b>旧实现的做法</b>（对照实现保留在 {@code javaref}）：在 {@code onApplied} 里手工
 * {@code addTemporaryModifier}，再在 {@code canApplyUpdateEffect} 里靠 {@code duration == 1}
 * 打一个 {@code removeEffect} 标记，等「只剩最后 1 tick」时手工移除修饰符。
 *
 * <p><b>为什么这是 bug</b>：那条移除路径<b>只覆盖「效果自然到期」这一种情况</b>。
 * 效果一旦被提前结束——{@code /effect clear}、喝牛奶、{@code /kill}、死亡重生、
 * 任何代码调用 {@code removeEffect}——修饰符就永远留在实体身上，
 * <b>玩家凭空少一截生命值上限，而且再也拿不回来</b>。
 * 附带一提，{@code duration == 1} 这个条件本身也很脆：它要求逐刻判定恰好在最后一 tick 走到。
 *
 * <p><b>本次改用原版的属性修饰符钩子</b>，一次解决两件事：
 * <ul>
 *   <li><b>清理不再依赖「知道什么时候结束」</b>。原版在 {@code LivingEntity} 里只有三个入口会动属性：
 *       {@code onEffectAdded}（挂上）、{@code onEffectUpdated}（等级变化时先移除再挂）、
 *       {@code onEffectsRemoved}（移除）。而 <b>自然到期、主动清除、死亡三条路都必须经过
 *       {@code onEffectsRemoved}</b>（逐行读过 {@code LivingEntity#removeEffect} 与到期分支）。
 *       我们改成覆写基类的 {@code addAttributeModifiers}/{@code removeAttributeModifiers}，
 *       就等于把清理挂在这个「唯一收敛点」上，提前结束再也不会漏；</li>
 *   <li><b>等级变化不会叠加</b>：{@code onEffectUpdated} 本身就是「先 remove 再 add」，
 *       两个方法都指向我们的覆写实现。</li>
 * </ul>
 * 结果：本类<b>不再需要</b>任何 {@code removeEffect} 标记，也不再手工移除修饰符——
 * 「少掉回不来」这个缺陷从机制上消失了，而不是被补了一个分支。
 *
 * <h2>「按基础生命值减少 X%」是怎么做到的</h2>
 * <p>需求口径是<b>基础</b>生命值的百分比，不是「当前上限」的百分比。
 * 原版 {@code AttributeModifier.Operation} 三档语义（逐行读过
 * {@code AttributeInstance#calculateValue}）：
 * <pre>{@code
 * double base = getBaseValue();
 * for (ADD_VALUE)            base += amount;          // 先加平值
 * double result = base;
 * for (ADD_MULTIPLIED_BASE)  result += base * amount;  // ★ 百分比乘「基础值」
 * for (ADD_MULTIPLIED_TOTAL) result *= 1.0 + amount;   // 百分比乘「累计结果」
 * }</pre>
 * 所以 {@link AttributeModifier.Operation#ADD_MULTIPLIED_BASE} 配 {@code amount = -0.15}
 * 恰好就是「基础生命值 × (1 - 15%)」——<b>不需要读取、也不需要缓存实体的基础值</b>。
 * 这一点很重要：旧实现把 {@code raw_max_health} 在 {@code onApplied} 那一刻读死，
 * 之后基础值一变（换装备、{@code /attribute}、别的模组改基础值）算出的绝对值就跟不上了；
 * 乘法操作天然跟随。
 *
 * <h2>为什么不直接用 {@code MobEffect#addAttributeModifier}（原版那套现成机制）</h2>
 * <p>看着更省事，但它<b>把等级和数值绑死了</b>：原版 {@code AttributeTemplate#create} 是
 * {@code new AttributeModifier(id, this.amount * (amplifier + 1), operation)}——
 * 注册时给的 amount 会被自动乘 {@code (amplifier + 1)}。
 * 而 wiki 的档位是 15/30/45/60/60（<b>不是</b>等差倍增，IV 之后就不涨了），
 * 硬套那个乘法只能得到「同一个比例逐级翻倍」，表达不了这张表。
 * 所以这里改成覆写那两个方法、按 {@link #maxHealthReduction(int)} 查表——
 * 让「哪个等级用哪个百分比」只有一个真相源。
 *
 * <h2>上限降低后的当前血量不用我们管</h2>
 * <p>{@code LivingEntity#setHealth} 内部是 {@code Mth.clamp(health, 0, getMaxHealth())}，
 * 上限一降，当前血量自动裁到新上限。旧实现里那句
 * {@code entity.setHealth(Math.min(...))} 因此是多余的，本类不再重复调用。
 *
 * <h2>登记点</h2>
 * <p>{@code RichardsWhimsyMod.onInitialize()} → {@link #OnInit()}。
 */
public class WI_Radioactive {

	/** 粒子颜色（wiki：{@code 0xF3A923}）。 */
	public static final int COLOR = 0xF3A923;

	/** 每次结算造成的伤害值（wiki 数值表：所有等级均为 2）。 */
	public static final float DAMAGE_PER_TICK = 2.0F;

	// ⚠️ 本效果<b>没有</b>「生命值下限」常量：wiki 只给生物毒性写了 6 点保底，
	// 电离辐射可以致死（详见 WI_RadioactiveEffect#applyEffectTick 的说明）。
	// 早先这里有一个从生物毒性误抄来的 MIN_HEALTH_TO_DAMAGE，已删除。

	/**
	 * 生命值上限修饰符的 id。
	 * <p>1.21 起属性修饰符 id 是 {@link Identifier}（不再是 1.20.4 的 {@code UUID}）。
	 * 原版按 id 做「先移除后添加」与「移除」，所以它必须稳定且全模组唯一。
	 */
	public static final Identifier HEALTH_MODIFIER_ID =
			Identifier.fromNamespaceAndPath("richards-whimsy-mod", "wi_radioactive_max_health");

	/**
	 * 伤害间隔（tick），索引 = {@code amplifier}（0 = 等级 I）。
	 * <p>wiki：I 100 / II 80 / III 60 / IV 40 / V 20 / $x$ 20。
	 */
	private static final int[] DAMAGE_INTERVALS = {100, 80, 60, 40, 20};

	/**
	 * 生命值上限减少比例，索引 = {@code amplifier}（0 = 等级 I）。
	 * <p>wiki：I 15% / II 30% / III 45% / IV 60% / V 60% / $x$ 60%。
	 */
	private static final double[] MAX_HEALTH_REDUCTIONS = {0.15, 0.30, 0.45, 0.60, 0.60};

	/** 注册后的效果句柄；1.21 起原版各处都收 {@code Holder<MobEffect>}。 */
	public static Holder<MobEffect> WI_RADIOACTIVE;

	private WI_Radioactive() {
	}

	/** 按 wiki 表取该等级的伤害间隔；超出表长（≥ VI 级）按最高档处理。 */
	public static int damageIntervalFor(int amplifier) {
		return DAMAGE_INTERVALS[clampLevel(amplifier, DAMAGE_INTERVALS.length)];
	}

	/** 按 wiki 表取该等级的生命值上限减少比例（0.15 = 15%）；超表按最高档处理。 */
	public static double maxHealthReduction(int amplifier) {
		return MAX_HEALTH_REDUCTIONS[clampLevel(amplifier, MAX_HEALTH_REDUCTIONS.length)];
	}

	/**
	 * 按等级构造本效果要挂的生命值上限修饰符。
	 *
	 * <p>数值是 {@code -比例}、操作是 {@link AttributeModifier.Operation#ADD_MULTIPLIED_BASE}
	 * （乘基础值，见类注释）。id 恒定，因此原版的「先移除后添加」总能正确替换掉上一级的那个。
	 */
	public static AttributeModifier maxHealthModifier(int amplifier) {
		return new AttributeModifier(
				HEALTH_MODIFIER_ID,
				-maxHealthReduction(amplifier),
				AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
	}

	private static int clampLevel(int amplifier, int tableLength) {
		return Math.min(Math.max(amplifier, 0), tableLength - 1);
	}

	/** 注册本效果。由主类在 {@code onInitialize()} 里调用，只调一次。 */
	public static void OnInit() {
		// 声音事件必须先注册好，才能把它挂到效果上（顺序不能反）。
		WI_Sounds.OnInit();
		WI_RADIOACTIVE = ModRegister.registerStatusEffect(
				"wi_radioactive",
				// withSoundOnAdded 是原版自带的「效果被挂上时响一声」钩子（见 WI_Sounds 类注释）。
				new WI_RadioactiveEffect().withSoundOnAdded(WI_Sounds.WI_RADIOACTIVE_APPLIED));
	}

	/** 分类与颜色（由 {@link WI_RadioactiveEffect} 构造器使用，集中一处免得两处各写一遍）。 */
	static MobEffectCategory category() {
		return MobEffectCategory.HARMFUL;
	}

	/** 若实体身上已有本效果，返回其效果实例；没有则 null。给攻击处理器查等级/剩余时间用。 */
	public static net.minecraft.world.effect.MobEffectInstance instanceOf(
			net.minecraft.world.entity.LivingEntity entity) {
		return WI_RADIOACTIVE == null ? null : entity.getEffect(WI_RADIOACTIVE);
	}

	/** 属性实例的便捷取用（供效果子类挂/摘修饰符，避免空指针散落各处）。 */
	static AttributeInstance maxHealthAttribute(net.minecraft.world.entity.LivingEntity entity) {
		return entity.getAttribute(Attributes.MAX_HEALTH);
	}
}
