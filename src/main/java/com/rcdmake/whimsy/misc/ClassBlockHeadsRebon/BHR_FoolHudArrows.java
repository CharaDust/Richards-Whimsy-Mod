package com.rcdmake.whimsy.misc.ClassBlockHeadsRebon;

import net.minecraft.util.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;

/**
 * 右侧箭头（barR）的状态机：把「正在回血 / 正在掉血」翻译成箭头数量与透明度。
 *
 * <h2>箭头数量</h2>
 * <ul>
 *   <li><b>正向</b>：生命恢复每级 1 个；自然回血（饥饿 ≥18 且血量未满）1 个；
 *       快速自然回血（饥饿 20 且饱和度 &gt;0）合计 3 个。</li>
 *   <li><b>负向</b>：中毒每级 1 个、凋零每级 1 个；饿到饥饿值 0（会掉血）1 个；
 *       瞬时伤害按 <b>每 2 点伤害 1 个</b>。</li>
 *   <li>正负各自最多 5 个（素材只有 1~5）。</li>
 * </ul>
 *
 * <h2>透明度（全部用 {@link Util#getMillis()} 做时间轴）</h2>
 * <ul>
 *   <li><b>单方向常驻</b>：0.4 s 由 1 渐隐到 0，再 0.4 s 由 0 渐显到 1；</li>
 *   <li><b>正负同时</b>：两组交替 —— 前 0.4 s 正向渐隐、后 0.4 s 负向渐隐；</li>
 *   <li><b>瞬时伤害</b>：立即满不透明 0.1 s，再用 0.4 s 渐隐到无；这段时间正向让位
 *       （对应「火焰里吃东西：先负，随后转正」）。</li>
 * </ul>
 *
 * <h2>瞬时伤害怎么来的</h2>
 * 不额外发包：客户端每帧比较 {@code health + pool} 的总量，掉多少就折算成箭头。
 * 愚者模式下血被打空后继续受伤只掉濒死值，总量照样在掉，所以表现一致；
 * 若掉血来自中毒/凋零（已经有常驻负向箭头），则不再叠加瞬时箭头，避免重复计数。
 */
public final class BHR_FoolHudArrows {

	/** 素材最多画 5 个箭头。 */
	public static final int MAX_ARROWS = 5;

	/** 渐隐 / 渐显各 0.4 s。 */
	private static final long FADE_MS = 400L;

	/** 瞬时伤害的「立即显示」时长 0.1 s。 */
	private static final long HIT_MS = 100L;

	/** 总血量变化的判定阈值（浮点误差）。 */
	private static final float EPSILON = 0.01F;

	/** 上一帧的「血量 + 濒死值」。 */
	private static float lastTotal = Float.NaN;

	/** 上一帧的玩家实体 id（换世界/换玩家时重置）。 */
	private static int lastPlayerId = Integer.MIN_VALUE;

	/** 瞬时伤害折算出的箭头数。 */
	private static int transientArrows;

	/** 最近一次瞬时伤害的时间戳（{@link Util#getMillis()}）。 */
	private static long lastHitMillis = Long.MIN_VALUE;

	private BHR_FoolHudArrows() {
	}

	/** 一次采样结果：正向/负向各自的箭头数与透明度。 */
	public record Arrows(int positive, float positiveAlpha, int negative, float negativeAlpha) {
	}

	// ── 每帧更新（由 HUD mixin 调用） ──

	public static void update(Player player, boolean foolMode) {
		long now = Util.getMillis();

		if (player.getId() != lastPlayerId) {
			lastPlayerId = player.getId();
			lastTotal = Float.NaN;
			transientArrows = 0;
			lastHitMillis = Long.MIN_VALUE;
		}

		float pool = foolMode ? player.getAttachedOrElse(BHR_FoolHealth.POOL, 0.0F) : 0.0F;
		float total = player.getHealth() + pool;

		if (!Float.isNaN(lastTotal)) {
			float drop = lastTotal - total;
			if (drop > EPSILON && negativePersistent(player) == 0) {
				int arrows = Math.max(1, Mth.ceil(drop / 2.0F));
				boolean alive = transientAlive(now);
				transientArrows = Math.min(MAX_ARROWS, (alive ? transientArrows : 0) + arrows);
				lastHitMillis = now;
			}
		}

		lastTotal = total;

		if (!transientAlive(now)) {
			transientArrows = 0;
		}
	}

	// ── 采样 ──

	public static Arrows sample(Player player) {
		long now = Util.getMillis();

		int positive = Math.min(MAX_ARROWS, positivePersistent(player));
		int negativePersistent = Math.min(MAX_ARROWS, negativePersistent(player));
		boolean transientAlive = transientAlive(now);

		// 瞬时伤害期间：负向占位，正向让位
		if (transientAlive) {
			int count = Math.min(MAX_ARROWS, Math.max(negativePersistent, transientArrows));
			return new Arrows(0, 0.0F, count, transientAlpha(now));
		}

		// 正负同时：两组交替（前 0.4 s 正向，后 0.4 s 负向）
		if (positive > 0 && negativePersistent > 0) {
			long phase = now % (2L * FADE_MS);
			if (phase < FADE_MS) {
				return new Arrows(positive, 1.0F - phase / (float) FADE_MS, negativePersistent, 0.0F);
			}
			return new Arrows(positive, 0.0F, negativePersistent, 1.0F - (phase - FADE_MS) / (float) FADE_MS);
		}

		// 单方向常驻闪烁：0.4 s 渐隐 + 0.4 s 渐显
		if (positive > 0) {
			return new Arrows(positive, blinkAlpha(now), 0, 0.0F);
		}
		if (negativePersistent > 0) {
			return new Arrows(0, 0.0F, negativePersistent, blinkAlpha(now));
		}

		return new Arrows(0, 0.0F, 0, 0.0F);
	}

	// ── 快乐值专用（画在快乐值那一行） ──

	/** 快乐值「正在向上限移动」的保持时长：回复每 2 tick 一次，留足余量。 */
	private static final long HAPPINESS_MOVE_HOLD_MS = 300L;

	/** 超过这个幅度的单帧下降算「生命值压低上限」的瞬时损失。 */
	private static final float HAPPINESS_INSTANT_DROP = 0.5F;

	private static float lastHappiness = Float.NaN;
	private static long happinessMoveMillis = Long.MIN_VALUE;
	private static int happinessDirection; // +1 上升 / -1 下降
	private static int happinessBurst;
	private static long happinessBurstMillis = Long.MIN_VALUE;

	/**
	 * 每帧喂一次快乐值：识别「按回复速度移动」与「生命值压低上限的瞬时损失」。
	 *
	 * <ul>
	 *   <li>单帧下降 &gt; 0.5 → 瞬时损失，箭头数 = {@code ceil(损失 / 2)}（上限 5）；</li>
	 *   <li>其它 0.02 以上的变化 → 正在向上限移动，显示 1 个对应方向的闪烁箭头。</li>
	 * </ul>
	 */
	public static void updateHappiness(float value) {
		long now = Util.getMillis();

		if (!Float.isNaN(lastHappiness)) {
			float delta = value - lastHappiness;
			if (delta <= -HAPPINESS_INSTANT_DROP) {
				int arrows = Math.max(1, Mth.ceil(-delta / 2.0F));
				boolean alive = now - happinessBurstMillis < HIT_MS + FADE_MS;
				happinessBurst = Math.min(MAX_ARROWS, (alive ? happinessBurst : 0) + arrows);
				happinessBurstMillis = now;
			} else if (Math.abs(delta) > 0.02F) {
				happinessDirection = delta > 0.0F ? 1 : -1;
				happinessMoveMillis = now;
			}
		}

		lastHappiness = value;

		if (now - happinessBurstMillis >= HIT_MS + FADE_MS) {
			happinessBurst = 0;
		}
	}

	/** 快乐值这一行该画的箭头。 */
	public static Arrows sampleHappiness() {
		long now = Util.getMillis();

		// 生命值压低上限的瞬时损失：和生命值一样的「立即 + 渐隐」
		if (happinessBurst > 0 && now - happinessBurstMillis < HIT_MS + FADE_MS) {
			long elapsed = now - happinessBurstMillis;
			float alpha = elapsed < HIT_MS
					? 1.0F
					: Mth.clamp(1.0F - (elapsed - HIT_MS) / (float) FADE_MS, 0.0F, 1.0F);
			return new Arrows(0, 0.0F, happinessBurst, alpha);
		}

		// 正在按回复速度接近上限：1 个闪烁箭头
		if (now - happinessMoveMillis < HAPPINESS_MOVE_HOLD_MS) {
			float alpha = blinkAlpha(now);
			return happinessDirection > 0
					? new Arrows(1, alpha, 0, 0.0F)
					: new Arrows(0, 0.0F, 1, alpha);
		}

		return new Arrows(0, 0.0F, 0, 0.0F);
	}

	// ── 饥饿值（barR）专用：消耗度增量箭头 + 饥饿效果箭头 ──

	/** 消耗度增量 → 箭头数（按需求分档）。 */
	public static int exhaustionArrows(float delta) {
		if (delta <= 0.0F) {
			return 0;
		}
		if (delta < 0.1F) {
			return 1;
		}
		if (delta < 0.2F) {
			return 2;
		}
		if (delta < 0.4F) {
			return 3;
		}
		if (delta < 1.0F) {
			return 4;
		}
		return MAX_ARROWS; // 回血那次 6 点：直接满档
	}

	private static float lastExhaustion = Float.NaN;
	private static int exhaustionBurst;
	private static long exhaustionBurstMillis = Long.MIN_VALUE;

	/** 每帧喂一次消耗度：增加的那一下折算成箭头，「立即显示 + 0.4 s 渐隐到无」。 */
	public static void updateHunger(float exhaustion) {
		long now = Util.getMillis();

		if (!Float.isNaN(lastExhaustion)) {
			float delta = exhaustion - lastExhaustion;
			if (delta > 0.0F) {
				exhaustionBurst = Math.max(1, Math.min(MAX_ARROWS, exhaustionArrows(delta)));
				exhaustionBurstMillis = now;
			}
		}

		lastExhaustion = exhaustion;

		if (now - exhaustionBurstMillis >= FADE_MS) {
			exhaustionBurst = 0;
		}
	}

	/**
	 * 饥饿这一行的箭头：消耗度增量（瞬时）优先，否则饥饿效果每级 1 个（常驻闪烁）。
	 */
	public static Arrows sampleHunger(int hungerEffectAmplifier) {
		long now = Util.getMillis();

		if (exhaustionBurst > 0 && now - exhaustionBurstMillis < FADE_MS) {
			float alpha = 1.0F - (now - exhaustionBurstMillis) / (float) FADE_MS;
			return new Arrows(0, 0.0F, exhaustionBurst, Mth.clamp(alpha, 0.0F, 1.0F));
		}

		if (hungerEffectAmplifier >= 0) {
			int arrows = Math.min(MAX_ARROWS, hungerEffectAmplifier + 1);
			return new Arrows(0, 0.0F, arrows, blinkAlpha(now));
		}

		return new Arrows(0, 0.0F, 0, 0.0F);
	}

	/** 供 HUD 直接复用的闪烁透明度（0.4 s 渐隐 + 0.4 s 渐显）。 */
	public static float blinkAlphaNow() {
		return blinkAlpha(Util.getMillis());
	}

	// ── 精力值（barR 第二行）专用：恒为 1 个箭头 ──

	/**
	 * 精力这一行的箭头数量。
	 *
	 * <ul>
	 *   <li><b>睡着</b>：入梦头 5 秒（{@code sleepTimer < 100}）不画 —— 那段时间也不消耗精力；
	 *       之后按床档 2 / 3 / 4 / 5 个，方向朝上（补充中）；</li>
	 *   <li><b>醒着</b>：常规 2 个；在其它维度（消耗减半）1 个；瞄准中（弓 / 弩 / 三叉戟）+1 个。</li>
	 * </ul>
	 */
	public static int energyArrowCount(Player player) {
		if (player.isSleeping()) {
			if (player.getSleepTimer() < 100) {
				return 0;
			}
			return BHR_BedTier.of(player.level(), player.getSleepingPos().orElse(null)).arrows;
		}

		int count = player.level().dimension().equals(Level.OVERWORLD) ? 2 : 1;
		if (BHR_Breath.isAiming(player)) {
			count += 1;
		}
		return Mth.clamp(count, 0, MAX_ARROWS);
	}

	/** 精力这一行的箭头（方向由「在补还是在掉」决定）。 */
	public static Arrows sampleEnergy(Player player, float energy) {
		int count = energyArrowCount(player);
		if (count <= 0) {
			return new Arrows(0, 0.0F, 0, 0.0F);
		}
		if (player.isSleeping()) {
			return new Arrows(count, blinkAlphaNow(), 0, 0.0F);
		}
		if (energy <= 0.0F) {
			return new Arrows(0, 0.0F, 0, 0.0F);
		}
		return new Arrows(0, 0.0F, count, blinkAlphaNow());
	}

	// ── 环境值（barR 第三行）专用：1 个箭头，指向当前移动方向 ──

	/**
	 * 「正在移动」的保持时长：环境值每 tick 走 0.1，但客户端只在收到同步的那几帧才看得到变化，
	 * 留 300 ms 余量避免闪烁被打断。
	 */
	private static final long ENV_MOVE_HOLD_MS = 300L;

	/** 单帧变化超过这个值才算「在移动」（浮点误差 + 帧间抖动）。 */
	private static final float ENV_MOVE_EPSILON = 0.02F;

	private static float lastEnvironment = Float.NaN;
	private static long environmentMoveMillis = Long.MIN_VALUE;
	private static int environmentDirection; // +1 上升 / -1 下降

	/**
	 * 每帧喂一次环境值：按「上一帧 → 这一帧」的变化量判断它是在<b>上升</b>还是<b>下降</b>。
	 *
	 * <p>客户端拿不到目标值（那是服务端算的），所以不看目标、只看实际移动方向：
	 * 环境值朝目标靠拢时每 tick 动 0.1，停下来（到达目标）后 300 ms 箭头自动消失。
	 */
	public static void updateEnvironment(float value) {
		long now = Util.getMillis();

		if (!Float.isNaN(lastEnvironment)) {
			float delta = value - lastEnvironment;
			if (Math.abs(delta) > ENV_MOVE_EPSILON) {
				environmentDirection = delta > 0.0F ? 1 : -1;
				environmentMoveMillis = now;
			}
		}

		lastEnvironment = value;
	}

	/**
	 * 环境值这一行的箭头：<b>恒为 1 个</b>，上升时画正向（{@code bhr_barr_arrow_1} = 增加 = 朝左），
	 * 下降时画负向（{@code bhr_barr_arrow_-1} = 减少 = 朝右），都在 0.4 s 渐隐 + 0.4 s 渐显地闪。
	 */
	public static Arrows sampleEnvironment() {
		long now = Util.getMillis();

		if (now - environmentMoveMillis >= ENV_MOVE_HOLD_MS) {
			return new Arrows(0, 0.0F, 0, 0.0F);
		}

		float alpha = blinkAlpha(now);
		return environmentDirection > 0
				? new Arrows(1, alpha, 0, 0.0F)
				: new Arrows(0, 0.0F, 1, alpha);
	}

	// ── 数量来源 ──

	private static int positivePersistent(Player player) {
		int arrows = 0;

		MobEffectInstance regeneration = player.getEffect(MobEffects.REGENERATION);
		if (regeneration != null) {
			arrows += regeneration.getAmplifier() + 1;
		}

		// 自然回血只在「血没满」时才真的在回
		if (player.getHealth() < player.getMaxHealth()) {
			FoodData food = player.getFoodData();
			if (food.getFoodLevel() >= 20 && food.getSaturationLevel() > 0.0F) {
				arrows += 3; // 快速自然回血
			} else if (food.getFoodLevel() >= 18) {
				arrows += 1; // 普通自然回血
			}
		}

		return arrows;
	}

	private static int negativePersistent(Player player) {
		int arrows = 0;

		MobEffectInstance poison = player.getEffect(MobEffects.POISON);
		if (poison != null) {
			arrows += poison.getAmplifier() + 1;
		}

		MobEffectInstance wither = player.getEffect(MobEffects.WITHER);
		if (wither != null) {
			arrows += wither.getAmplifier() + 1;
		}

		if (player.getFoodData().getFoodLevel() == 0) {
			arrows += 1; // 饿到掉血
		}

		return arrows;
	}

	// ── 透明度 ──

	private static float blinkAlpha(long now) {
		long phase = now % (2L * FADE_MS);
		return phase < FADE_MS
				? 1.0F - phase / (float) FADE_MS
				: (phase - FADE_MS) / (float) FADE_MS;
	}

	private static boolean transientAlive(long now) {
		return transientArrows > 0 && now - lastHitMillis < HIT_MS + FADE_MS;
	}

	private static float transientAlpha(long now) {
		long elapsed = now - lastHitMillis;
		if (elapsed < HIT_MS) {
			return 1.0F;
		}
		return Mth.clamp(1.0F - (elapsed - HIT_MS) / (float) FADE_MS, 0.0F, 1.0F);
	}
}
