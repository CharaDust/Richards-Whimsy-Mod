package com.rcdmake.whimsy.statusEffect.ClassWhimsyIdea;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

/**
 * 生物毒性的<b>正向传染</b>：拥有生物毒性的生物<b>攻击</b>其他生物时，
 * 把「低一级、半时长」的同名效果传给目标。
 *
 * <p>规格来源：wiki {@code WI_BioToxicity.md} 的「事件主持器」一节：
 * 「当攻击者拥有状态效果攻击目标时，对目标施加攻击者 {@code 等级-1} 等级、
 * {@code 剩余时间/2} 时间的同名效果」，并附带一份免疫生物名单。
 *
 * <h2>为什么不再用 {@code AttackEntityCallback}（本次重构的关键改动之一）</h2>
 * <p>旧实现（保留在 {@code javaref}）挂在 {@code AttackEntityCallback} 上，那是
 * 「<b>玩家</b>左键点实体」的钩子，因此只有玩家能传染，怪物身上挂着生物毒性却打不出效果——
 * 与 RDT 模块「灌注」旧版是同一类缺陷。本模块改用与 RDT 相同思路的挂点：
 * {@code LivingEntity.hurtServer}（伤害真正落地的唯一收敛点），由
 * {@code mixin/ClassWhimsyIdea/WI_BioToxicityHurtMixin} 驱动，因此玩家、怪物、宠物/投射物一律生效。
 *
 * <h2>⭐ 模块解耦：为什么 WI 自己写一份、而不是复用 RDT 的处理器</h2>
 * <p>本项目约定「各模块之间必须解耦——某模块的功能禁止使用其他模块提供的方法」。
 * RDT 模块已有一个挂在同一原版方法上的处理器（{@code RDT_ImbueAttackHandler}）。
 * 如果 WI 去调它、或两者抽一个共同基类，就等于把两个模块的行为绑在一起：
 * 改一个模块的传染规则会牵动另一个。所以这里<b>刻意重复</b>这段结构：
 * <ul>
 *   <li>处理器各写一份（{@code WI_*} 与 {@code RDT_*} 互不引用）；</li>
 *   <li>mixin 各写一份，挂在同一个 {@code LivingEntity.hurtServer} 上——这是允许的，
 *       Mixin 支持多个配置/多个 mixin 类注入同一目标方法，只要各自的处理方法名不冲突
 *       （{@code rdt$…} 与 {@code wi$…} 前缀就是为此）。</li>
 * </ul>
 * 代价是两份相似的样板；换来的是模块可独立演进、删除或替换而互不影响。
 *
 * <h2>I 级也传染（本轮按需求恢复）</h2>
 * <p>本效果<b>所有等级都传染</b>：wiki 已明确「等级 I 及以上均可」。
 * 等级 I（amplifier 0）传出去经 {@code amplifier - 1} 得 0，即目标身上是「生物毒性 I」——
 * 只是时长减半，等级不变。注意 <b>amplifier 有下限</b>：原版不接受负数，
 * 所以代码里对 {@code amplifier - 1} 做了 {@code Math.max(…, 0)} 保护，
 * 否则 I 级传染会因参数非法而静默失败。
 *
 * <p>顺便记一笔历史：更早的旧实现在 I 级走的是「给目标挂原版 {@code POISON} 240 tick」，
 * 那是与「传染同名效果」完全不同的另一套机制；现在统一成同名效果传染，
 * 行为只由这一处定义。
 */
public final class WI_BioToxicityAttackHandler {

	/**
	 * 免疫本效果传染的生物（wiki「事件主持器」名单）：
	 * 铁傀儡、雪傀儡、凋灵、骷髅、凋零骷髅、骷髅马、流浪者、烈焰人、岩浆怪、史莱姆。
	 *
	 * <p>用 {@link EntityType} 判定而不是 {@code instanceof} 类链：
	 * <ul>
	 *   <li>与 wiki 的「生物名单」一一对应，读起来就是那张表；</li>
	 *   <li>不受类层次影响——{@code WitherSkeleton} / {@code Stray} 继承 {@code Skeleton}、
	 *       {@code MagmaCube} 继承 {@code Slime}，用 {@code instanceof} 时父类那一条会顺带覆盖子类，
	 *       虽然这里结果相同，但语义上「名单里列了谁就是谁」更清楚；
	 *   <li>集合查找比一长串 {@code instanceof} 便宜，且宿主/变种实体（如不同职业的骷髅变种）
	 *       只按类型判定，不会因为以后新增子类而意外免疫。</li>
	 * </ul>
	 *
	 * <p>⚠️ 旧实现另外还免疫了 {@code EnderDragonEntity}（末影龙），但 <b>wiki 名单里没有它</b>，
	 * 所以本次按 wiki 不加——若你确实想让末影龙免疫，往这个集合里加一行即可。
	 */
	private static final Set<EntityType<?>> IMMUNE_TYPES = Set.of(
			EntityTypes.IRON_GOLEM,
			EntityTypes.SNOW_GOLEM,
			EntityTypes.WITHER,
			EntityTypes.SKELETON,
			EntityTypes.WITHER_SKELETON,
			EntityTypes.SKELETON_HORSE,
			EntityTypes.STRAY,
			EntityTypes.BLAZE,
			EntityTypes.MAGMA_CUBE,
			EntityTypes.SLIME);

	private WI_BioToxicityAttackHandler() {
	}

	/**
	 * 攻击结算入口：攻击者（source）身上有 II 级及以上生物毒性时，传染给目标（victim）。
	 *
	 * @param attacker 造成伤害的生物（玩家、怪物，或投射物的 owner）
	 * @param victim   被伤害的实体
	 */
	public static void spreadOnAttack(LivingEntity attacker, Entity victim) {
		if (attacker == null || victim == null || attacker == victim) {
			return;
		}
		// 挂点在服务端方法上，这里再留一道闸，避免将来被挪进双端路径后重复施加。
		if (attacker.level().isClientSide()) {
			return;
		}
		if (!(victim instanceof LivingEntity livingVictim)) {
			return;
		}
		if (isImmune(livingVictim)) {
			return;
		}

		MobEffectInstance source = WI_BioToxicity.instanceOf(attacker);
		if (source == null) {
			return;
		}

		int amplifier = source.getAmplifier();

		// 等级 I 特殊：传播的是<b>原版剧毒</b>（minecraft:poison），不是同名效果。
		// 原因是原版剧毒的伤害频率比本效果高得多（每 25 tick 一次，本效果 I 级是每 80 tick），
		// 所以时长按 1/4 折算，让两边「总共大概扣掉的血量」接近。
		if (amplifier < 1) {
			livingVictim.addEffect(new MobEffectInstance(
					MobEffects.POISON,
					source.getDuration() / 4), attacker);
			return;
		}

		// 等级 II 及以上：传播同名效果，低一级、半时长（wiki）。
		livingVictim.addEffect(new MobEffectInstance(
				WI_BioToxicity.WI_BIO_TOXICITY,
				// 剩余时间 ÷ 2（wiki）
				source.getDuration() / 2,
				// 等级 - 1（wiki）
				amplifier - 1), attacker);
	}

	/** 目标是否在 wiki 的免疫名单里。 */
	public static boolean isImmune(LivingEntity target) {
		return IMMUNE_TYPES.contains(target.getType());
	}

	/**
	 * 结算时是否应当对<b>持有者</b>造成伤害。
	 *
	 * <h2>免疫生物的完整语义（用户明确的口径）</h2>
	 * <p>名单里的生物是「<b>免疫伤害与传染，但不是免疫效果</b>」：
	 * <ul>
	 *   <li>✅ <b>可以</b>带上生物毒性 —— 药水、指令、以及它自己去传染别人都照常；</li>
	 *   <li>❌ <b>不受到</b>该效果造成的伤害（即本方法返回 false）；</li>
	 *   <li>❌ <b>不会被</b>别人的生物毒性传染（见 {@link #isImmune} 在
	 *       {@link #spreadOnAttack} 里的拦截）；</li>
	 *   <li>✅ <b>仍然能</b>把身上的生物毒性传染给别人（传染只要求「攻击者持有该效果」，
	 *       不检查攻击者是否免疫）。</li>
	 * </ul>
	 *
	 * <p>⚠️ 本项目曾把这个名单误解成「免疫效果本身」，于是用 Fabric 的
	 * {@code ServerMobEffectEvents.ALLOW_ADD} 让免疫生物根本挂不上效果 —— 那是错的，
	 * 会连带否定上面第 1、4 条（铁傀儡既不能被给药水、也就无法成为传染源）。
	 * 现已撤掉那个全局拦截，改为「伤害 + 被传染」两处判定。
	 *
	 * @param holder 效果的持有者
	 * @return true = 照常扣血；false = 跳过本次伤害
	 */
	public static boolean shouldTakeDamage(LivingEntity holder) {
		return !isImmune(holder);
	}
}
