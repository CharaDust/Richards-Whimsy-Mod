package com.rcdmake.whimsy.statusEffect.ClassWhimsyIdea;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 电离辐射的<b>传染</b>：拥有 II 级及以上电离辐射的生物<b>受到伤害</b>时，
 * 按伤害类型分两条路把「低一级、半时长」的同名效果传出去。
 *
 * <p>规格来源：wiki {@code WI_Radioactive.md} 的「事件主持器」一节：
 * 「当<b>被攻击者</b>拥有 II 级及以上的状态效果受到攻击时，对<b>攻击者</b>施加被攻击者
 * {@code 等级-1} 等级、{@code 剩余时间/2} 时间的同名效果」，
 * 以及「当受到非近战攻击时，产生 {@code 等级-1} 等级、{@code 剩余时间/2} 时间的同名效果云」。
 *
 * <h2>两条传播路径（本轮新增第二条）</h2>
 * <ol>
 *   <li><b>近战</b>：直接给打上来的那个生物挂效果（原「反向传染」）；</li>
 *   <li><b>其余类型伤害</b>（箭、火球、爆炸、火焰、魔法、无源伤害…）：
 *       在被伤害者所在位置生成一团<b>药水云</b>，由云去感染范围内碰到的生物。</li>
 * </ol>
 * 判据见 {@link #isMelee}——难点在「怎么算近战」，那里写了完整推导。
 *
 * <h2>方向与生物毒性正好相反（别写反了）</h2>
 * <ul>
 *   <li>{@link WI_BioToxicityAttackHandler}（正向）：<b>攻击者</b>有 → 传给<b>目标</b>；</li>
 *   <li>本类（反向）：<b>被攻击者</b>有 → 传给<b>攻击者</b>（近战）或就地生成云（非近战）。</li>
 * </ul>
 * 两者都挂在同一个原版方法上（{@code LivingEntity.hurtServer}），只是读写方向不同。
 *
 * <h2>旧实现的问题（本次一并修掉）</h2>
 * <ol>
 *   <li><b>挂错事件</b>：旧实现用 {@code ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY}，
 *       那是「<b>击杀</b>发生后」的事件，而 wiki 说的是「受到攻击时」。
 *       于是只有「把对方打死」才传染——目标已死再谈传播链，时机和语义都不对。
 *       本次改为按「伤害落地」判定，只要打中（含打不死）就传染。</li>
 *   <li><b>等级 I 不传染</b>为 wiki 明文，旧实现用 {@code amplifier > 0} 判定，
 *       口径一致，本次保留。</li>
 * </ol>
 *
 * <h2>模块解耦</h2>
 * <p>本类与 RDT 模块的处理器互不引用；本模块的结构重复是刻意的，
 * 理由见 {@link WI_BioToxicityAttackHandler} 类注释「模块解耦」一节。
 */
public final class WI_RadioactiveAttackHandler {

	/** 药水云半径（wiki：5 格）。 */
	private static final float CLOUD_RADIUS = 5.0F;

	/** 药水云存在时长（wiki：20 tick = 1 秒）。 */
	private static final int CLOUD_DURATION_TICKS = 20;

	private WI_RadioactiveAttackHandler() {
	}

	/**
	 * 攻击结算入口。
	 *
	 * <p>被伤害者（victim）身上有 <b>II 级及以上</b>电离辐射时，按<b>伤害类型</b>分两条路：
	 * <ul>
	 *   <li><b>近战</b>（直接由某个生物打上来）：把「低一级、半时长」的同名效果直接给攻击者；</li>
	 *   <li><b>其余类型</b>（箭、火球、爆炸、火焰、魔法、无源伤害…）：在被伤害者所在位置
	 *       生成一团药水云，由云去感染范围内碰到的生物。</li>
	 * </ul>
	 *
	 * @param attacker 伤害源上的造成伤害者（{@code DamageSource#getEntity()}）；环境伤害时为 null
	 * @param victim   被伤害的实体（传染的来源方）
	 * @param source   本次伤害的伤害源，用来判定近战 / 非近战
	 */
	public static void spreadOnAttack(LivingEntity attacker, Entity victim, DamageSource source) {
		if (victim == null) {
			return;
		}
		if (attacker == victim) {
			return;
		}
		// 挂点在服务端方法上，这里再留一道闸，避免将来被挪进双端路径后重复施加。
		if (victim.level().isClientSide()) {
			return;
		}
		if (!(victim instanceof LivingEntity livingVictim)) {
			return;
		}

		MobEffectInstance victimEffect = WI_Radioactive.instanceOf(livingVictim);
		if (victimEffect == null) {
			return;
		}
		// wiki：只有 II 级及以上才传染（I 级传出去只会是 0 级，无意义）。
		int amplifier = victimEffect.getAmplifier();
		if (amplifier < 1) {
			return;
		}

		// 要传染出去的那一份：低一级、半时长（wiki）。
		int spreadAmplifier = amplifier - 1;
		int spreadDuration = victimEffect.getDuration() / 2;

		if (isMelee(source)) {
			// 近战：直接给打人的那个生物。
			// attacker 为 null 理论上不会走到（近战必然有直接实体），这里只做防御。
			if (attacker != null) {
				attacker.addEffect(new MobEffectInstance(
						WI_Radioactive.WI_RADIOACTIVE, spreadDuration, spreadAmplifier), livingVictim);
			}
			return;
		}

		// 非近战：生成药水云。
		spawnRadioactiveCloud(livingVictim, spreadDuration, spreadAmplifier);
	}

	/**
	 * 判定这次伤害是不是「近战」。
	 *
	 * <h2>判据：伤害的直接来源与造成伤害者是同一个实体，且不是投射物/爆炸</h2>
	 * <p>26.2 的 {@code DamageSource} 有两个实体字段：
	 * <ul>
	 *   <li>{@code getDirectEntity()}——<b>物理上</b>造成伤害的东西；</li>
	 *   <li>{@code getEntity()}——<b>归责</b>给谁（例如箭射中时是射箭的人）。</li>
	 * </ul>
	 * 于是可以干净地分开两类：
	 * <pre>{@code
	 * 玩家挥剑  -> direct=玩家,  causing=玩家   -> 两者相同 → 近战
	 * 骷髅射箭  -> direct=箭,    causing=骷髅   -> 两者不同 → 非近战
	 * 苦力怕爆炸-> direct=苦力怕, causing=苦力怕 -> 需靠 IS_EXPLOSION 标签排除
	 * 仙人掌/摔落-> direct=null, causing=null   -> 无直接实体   → 非近战
	 * }</pre>
	 * 所以「两者相同」<b>还不足以</b>排除爆炸（爆炸的两者也是同一个实体），
	 * 必须再排掉 {@code IS_EXPLOSION} 与 {@code IS_PROJECTILE} 标签。
	 *
	 * <p>⚠️ 这条判据的一个已知边界：{@code IS_PROJECTILE} 标签也包含
	 * {@code thrown}（投掷物）、{@code trident}（三叉戟近战那一击是 {@code player_attack}，
	 * 不受影响）等；也就是「用投掷物打」会归到药水云那一路，符合预期。
	 */
	private static boolean isMelee(DamageSource source) {
		if (source == null) {
			return false;
		}
		Entity direct = source.getDirectEntity();
		if (direct == null || direct != source.getEntity()) {
			// 没有直接实体（环境伤害）或直接实体与归责实体不是同一个（箭、火球…）。
			return false;
		}
		// 爆炸的 direct 与 causing 也是同一个实体，必须显式排除。
		return !source.is(DamageTypeTags.IS_EXPLOSION) && !source.is(DamageTypeTags.IS_PROJECTILE);
	}

	/**
	 * 在指定生物的位置生成一团「电离辐射药水云」。
	 *
	 * <p>参数取自 wiki：半径 5 格、持续 20 tick（1 秒），云本身携带
	 * 「低一级、半时长」的同名效果；云跑完一轮的效果结算逻辑时，
	 * 会把这份效果施加给范围内的生物（原版 {@code AreaEffectCloud} 自带该行为）。
	 *
	 * <p>{@code setOwner(源)} 有实际作用：原版会把效果的施加者归给 owner，
	 * 这样「谁传染的」在伤害/成就归因上仍然正确，不是可有可无的一行。
	 */
	private static void spawnRadioactiveCloud(LivingEntity source, int duration, int amplifier) {
		if (!(source.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		AreaEffectCloud cloud = new AreaEffectCloud(serverLevel, source.getX(), source.getY(), source.getZ());
		cloud.setRadius(CLOUD_RADIUS);
		cloud.setDuration(CLOUD_DURATION_TICKS);
		cloud.addEffect(new MobEffectInstance(WI_Radioactive.WI_RADIOACTIVE, duration, amplifier));
		cloud.setOwner(source);
		serverLevel.addFreshEntity(cloud);
	}
}
