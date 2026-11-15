package com.rcdmake.whimsy.misc.ClassBlockHeadsRebon;

import com.mojang.serialization.Codec;
import com.rcdmake.whimsy.ModRegister;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;

/**
 * 环境值（{@code environment}）：愚者模式右侧 HUD 的第三条资源条（精力条<b>正上方</b>那一行）。
 *
 * <h2>数值</h2>
 * <ul>
 *   <li>范围 <b>0 ~ 100</b>；</li>
 *   <li>与快乐值同一套规则：先算<b>目标值</b>，再按固定速率（每 tick 0.1 点 = 2 点/秒）
 *       向目标值靠拢，不瞬时跳变；</li>
 *   <li>目标值 = {@code clamp(100 − 各项扣分之和, 0, 100)}，默认满 100 分。</li>
 * </ul>
 *
 * <h2>扣分项（每一项都是独立账本，条件消失的那一 tick 立即清零 / 返还）</h2>
 * <table border="1">
 *   <tr><th>条件</th><th>扣分</th></tr>
 *   <tr><td>脚下这一格<b>方块光与天空光都为 0</b></td>
 *       <td>{@value #DARK_PENALTY} 分，且<b>每 1 秒再 +{@value #DARK_EXTRA_PER_SECOND} 分</b>
 *           （回到有光的地方立即返还全部黑暗扣分）</td></tr>
 *   <tr><td><b>夜里且没有方块光</b>（有效天空光被压到最低档 4，约每日 13670 ~ 22331 tick）</td>
 *       <td>{@value #NIGHT_PENALTY} 分（天亮、天空光开始回升时立即返还）</td></tr>
 *   <tr><td>正<b>淋雨</b>（下雨 + 该生物群系降雨 + 不是下雪 + 头顶到建筑高度上限没有方块）</td>
 *       <td>{@value #RAIN_PENALTY} 分（不淋雨了立即返还）</td></tr>
 *   <tr><td>泡在<b>岩浆</b>里（有抗火药水时不扣）</td>
 *       <td>{@value #LAVA_PENALTY} 分（抗火生效或离开岩浆立即返还）</td></tr>
 *   <tr><td><b>着火</b>（有抗火药水时不扣）</td>
 *       <td>每 1 秒 {@value #FIRE_PENALTY_PER_SECOND} 分（不再着火 / 抗火生效立即返还）</td></tr>
 *   <tr><td>埋在<b>细雪</b>里</td><td>{@value #POWDER_SNOW_PENALTY} 分（离开立即返还）</td></tr>
 * </table>
 *
 * <h2>「淋雨」怎么判定（照抄原版炼药锅蓄水 / 蓄细雪那条路）</h2>
 * 用 {@link net.minecraft.world.level.Level#isRainingAt(BlockPos)}，它等价于
 * {@code Level#precipitationAt(pos) == R​​ain}，也就是原版炼药锅
 * （{@code LayeredCauldronBlock#handlePrecipitation}）吃到的同一份判定：
 * <ol>
 *   <li>{@code level.isRaining()}（天气在下雨）；</li>
 *   <li>{@code level.canSeeSky(pos)}（那一格能看到天）；</li>
 *   <li>{@code getHeightmapPos(MOTION_BLOCKING, pos).getY() <= pos.getY()}
 *       （头顶往上直到建筑高度上限没有会挡雨的方块）；</li>
 *   <li>生物群系 {@code getPrecipitationAt} 返回 <b>RAIN</b>（返回 SNOW 就是下雪，不扣）。</li>
 * </ol>
 * 位置取<b>脚所在格</b>；因为判定要「头顶露天」，所以额外查一次<b>包围盒顶部</b>那一格
 * （与原版 {@code Entity#isInRain} 的双点判定一致 —— 那个方法是 private，这里自己拼）。
 *
 * <h2>黑暗</h2>
 * 只看<b>原始</b>光照（{@code getBrightness(LightLayer.BLOCK/SKY, pos)}），与时段的天空变暗无关：
 * 露天白天夜里都是天空光 15，只有真正被方块封死（含下界那种天空光恒 0 的维度里的无光区）
 * 才会两个都为 0。
 *
 * <h2>夜晚</h2>
 * 与黑暗正好互补：原始天空光是 15（露天），但<b>有效</b>天空光
 * {@code 原始天空光 − level.getSkyDarken()} 被压到了最低档 4（夜里），
 * 且没有方块光。{@code skyDarken} 由 {@code ServerLevel#tick} 每 tick 用
 * {@code EnvironmentAttributes.SKY_LIGHT_LEVEL} 刷新（{@code 15 − 4 = 11}），
 * 天亮后该属性回升、有效值 &gt; 4，这一项立刻返还。
 *
 * <h2>对快乐值上限的影响</h2>
 * 环境值低于 {@value #HAPPINESS_KNEE} 时，<b>每少 5 点扣 1 点快乐值上限</b>（见
 * {@link #happinessPenalty(float)}）：80 → 不扣；60 → −4；<b>0 → −16</b>，
 * 于是快乐值上限只剩 <b>4</b>。最终仍由 {@link BHR_Happiness#cap(ServerPlayer)}
 * 与「饥饿上限」「精力上限」「当前生命」取最小值。
 *
 * <h2>同步</h2>
 * 与濒死值 / 快乐值 / 精力值同款：持久化 + 按玩家自身同步的附件，客户端 HUD 直接读。
 */
public final class BHR_Environment {

	// ── 层 1 ｜ 规格常量 ──

	/** 环境值上限（满值）。 */
	public static final float MAX = 100.0F;

	/** 黑暗：基础扣分。 */
	public static final float DARK_PENALTY = 50.0F;

	/** 黑暗：每秒再加扣的分数。 */
	public static final float DARK_EXTRA_PER_SECOND = 1.0F;

	/** 淋雨扣分。 */
	public static final float RAIN_PENALTY = 50.0F;

	/** 岩浆扣分。 */
	public static final float LAVA_PENALTY = 100.0F;

	/** 着火：每秒扣分。 */
	public static final float FIRE_PENALTY_PER_SECOND = 5.0F;

	/** 细雪扣分。 */
	public static final float POWDER_SNOW_PENALTY = 50.0F;

	/**
	 * 夜晚扣分：<b>夜晚（天空光照压到最低值）且方块光为 0</b> 时扣 80 分。
	 *
	 * <p>「天空光照压到最低值」就是原版的当前天空光照等级
	 * （{@code EnvironmentAttributes.SKY_LIGHT_LEVEL}）在夜间降到 <b>4</b>：
	 * {@code Level#updateSkyBrightness} 把它换算成 {@code skyDarken = 15 − 4 = 11}，
	 * 于是露天格子的<b>有效</b>天空光 {@code 原始天空光(15) − skyDarken} 正好是 4。
	 * 白天 {@code skyDarken = 0}，露天格子有效天空光是 15；天亮后 {@code SKY_LIGHT_LEVEL}
	 * 一开始往上走（约每日 22331 tick），有效值就大于 4，这一项立刻返还。
	 */
	public static final float NIGHT_PENALTY = 80.0F;

	/** 「夜晚最低天空光照」的上限值：有效天空光落在 1 ~ 这个值之间都算夜晚的暗环境。 */
	public static final int NIGHT_SKY_MAX = 4;

	/**
	 * 低于这个环境值，<b>快乐值上限</b>开始下降（满分 100 时不影响快乐值；80 以下才挂钩）。
	 */
	public static final float HAPPINESS_KNEE = 80.0F;

	/**
	 * 每低这么多环境值，快乐值上限 −1 点。
	 *
	 * <p>于是环境值 0 时快乐值上限 = {@code 20 − 80/5 = 4}（与需求里「归零时目标为 4」一致）。
	 * 这里用<b>连续</b>换算（79 分 → 上限 19.8），与饥饿值那套
	 * {@code (10 − 饥饿值) / 2} 的写法保持一致；若想要「每满 5 点才掉 1 点」的硬阶梯，
	 * 把 {@link #happinessPenalty(float)} 的结果改成向下取整即可。
	 */
	public static final float HAPPINESS_PENALTY_STEP = 5.0F;

	/** 每 tick 累积的着火扣分（5 分/秒 ÷ 20 tick）。 */
	private static final float FIRE_PENALTY_PER_TICK = FIRE_PENALTY_PER_SECOND / 20.0F;

	/** 每 20 tick 算 1 秒（黑暗的「每秒 +1」用）。 */
	private static final int TICKS_PER_SECOND = 20;

	/**
	 * 向目标值靠拢的步长与间隔：<b>每 tick 0.1 点 = 2 点/秒</b>。
	 *
	 * <p>为什么不是快乐值那种 1 点/秒：环境值的目标值在黑暗里<b>自己每秒往下沉 1 点</b>
	 * （{@value #DARK_EXTRA_PER_SECOND} 分/秒 + 基础 50 分），若环境值也只走 1 点/秒，
	 * 就永远追不上目标、一直差着 50 点。取 2 点/秒后，
	 * 「100 分 → 0 分」与「黑暗里目标值 50 秒后见底」正好同时到达。
	 */
	public static final float STEP = 0.1F;
	public static final int STEP_INTERVAL = 1;

	/** 环境值附件路径（{@code richards-whimsy-mod:environment}）。 */
	public static final String VALUE_PATH = "environment";

	/** 每个玩家的扣分账本附件路径（不持久化、不同步，跟着玩家实体走）。 */
	public static final String DEBT_PATH = "environment_debt";

	// ── 层 1 ｜ 数据载体 ──

	/** 环境值本身：持久化 + 按玩家自身同步。 */
	public static final AttachmentType<Float> VALUE = AttachmentRegistry.create(
			ModRegister.id(VALUE_PATH),
			builder -> builder
					.persistent(Codec.FLOAT)
					.initializer(() -> MAX)
					.syncWith(ByteBufCodecs.FLOAT, AttachmentSyncPredicate.targetOnly()));

	/**
	 * 扣分账本：只活在这个玩家实体上（挂了就跟着回收，不需要额外的登出清理），
	 * 因为不持久化也不同步，所以<b>不需要 codec</b>。
	 */
	public static final AttachmentType<Debt> DEBT = AttachmentRegistry.create(
			ModRegister.id(DEBT_PATH),
			builder -> builder.initializer(Debt::new));

	private BHR_Environment() {
	}

	// ── 层 1 ｜ 扣分账本 ──

	/**
	 * 一个玩家当前各项扣分的累计值。
	 *
	 * <p>每个字段都<b>只在自己的条件成立期间有值</b>：条件一消失就清零，
	 * 于是「扣掉的分数立即返还」是免费的（下一 tick 目标值直接回到高位）。
	 */
	public static final class Debt {

		/** 黑暗里待了多少 tick（用于「每秒 +1」）。0 = 不在黑暗里。 */
		private int darkTicks;

		/** 是否正在淋雨（50 分）。 */
		private boolean raining;

		/** 是否是「夜里且无方块光」（80 分）。 */
		private boolean night;

		/** 是否正在泡岩浆（100 分）。 */
		private boolean inLava;

		/** 着火累计扣分（每秒 +5，连续累积）。 */
		private float fire;

		/** 是否埋在细雪里（50 分）。 */
		private boolean inPowderSnow;

		/** 黑暗扣分 = 基础 50 + 每秒 +1（向下取整到整秒）。 */
		public float darkPenalty() {
			if (this.darkTicks <= 0) {
				return 0.0F;
			}
			return DARK_PENALTY + DARK_EXTRA_PER_SECOND * (float) (this.darkTicks / TICKS_PER_SECOND);
		}

		/** 当前所有扣分之和（目标值 = 100 − 它，再夹到 0~100）。 */
		public float total() {
			float sum = darkPenalty();
			if (this.night) {
				sum += NIGHT_PENALTY;
			}
			if (this.raining) {
				sum += RAIN_PENALTY;
			}
			if (this.inLava) {
				sum += LAVA_PENALTY;
			}
			if (this.inPowderSnow) {
				sum += POWDER_SNOW_PENALTY;
			}
			return sum + this.fire;
		}
	}

	// ── 层 12 ｜ 初始化 ──

	/** 注册事件（重生补满 + 每 tick 推进）。由主类调用。 */
	public static void OnInit() {
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> set(newPlayer, MAX));
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tick(player);
			}
		});
	}

	// ── 层 8 ｜ 每 tick 的数值推进 ──

	/**
	 * 愚者模式：先按当前处境刷新扣分账本，算出目标值，再朝目标值走一步；
	 * 其它模式：不做任何推进，环境值复位成满值（与快乐值的处理一致）。
	 */
	private static void tick(ServerPlayer player) {
		if (player.gameMode() != GameType.BHR_FOOL_MODE) {
			if (get(player) < MAX) {
				set(player, MAX);
			}
			return;
		}

		Debt debt = player.getAttachedOrCreate(DEBT);
		updateDebt(player, debt);

		float target = Mth.clamp(MAX - debt.total(), 0.0F, MAX);

		if (player.tickCount % STEP_INTERVAL != 0 || target == get(player)) {
			return;
		}

		// 按固定速率靠近目标值（不跳变）
		float value = get(player);
		if (value < target) {
			set(player, Math.min(target, value + STEP));
		} else {
			set(player, Math.max(target, value - STEP));
		}
	}

	/** 逐项刷新扣分账本：条件成立就继续累计，条件消失立刻清零。 */
	private static void updateDebt(ServerPlayer player, Debt debt) {
		BlockPos pos = player.blockPosition();

		// 光照一次取全：方块光、原始天空光、以及扣掉「当前天空变暗」之后的有效天空光
		int blockLight = player.level().getBrightness(LightLayer.BLOCK, pos);
		int skyLight = player.level().getBrightness(LightLayer.SKY, pos);
		int effectiveSky = skyLight - player.level().getSkyDarken();

		// 1) 黑暗：方块光与天空光都是 0（原始光照值，不看时段变暗）
		boolean dark = blockLight <= 0 && skyLight <= 0;
		debt.darkTicks = dark ? debt.darkTicks + 1 : 0;

		// 1b) 夜晚：有效天空光被压到最低档（夜里 4）且没有方块光
		//     —— 与上一条互斥：黑暗那条要求「原始」天空光为 0，夜里露天时原始值是 15。
		debt.night = blockLight <= 0
				&& effectiveSky >= 1
				&& effectiveSky <= NIGHT_SKY_MAX;

		// 2) 淋雨：与原版炼药锅 / Entity#isInRain 同源（脚所在格 + 包围盒顶部那一格）
		debt.raining = isRainingAt(player, pos);

		// 3) 岩浆：抗火药水生效时不扣
		boolean fireResistant = player.hasEffect(MobEffects.FIRE_RESISTANCE);
		debt.inLava = !fireResistant && player.isInLava();

		// 4) 着火：每秒 5 分，抗火生效时同样不扣
		if (!fireResistant && player.isOnFire()) {
			debt.fire += FIRE_PENALTY_PER_TICK;
		} else {
			debt.fire = 0.0F;
		}

		// 5) 细雪
		debt.inPowderSnow = isInPowderSnow(player, pos);
	}

	/** 淋雨判定：脚所在格或包围盒顶部那一格能淋到雨（原版 {@code isInRain} 的同款双点检查）。 */
	private static boolean isRainingAt(ServerPlayer player, BlockPos pos) {
		if (player.level().isRainingAt(pos)) {
			return true;
		}
		BlockPos head = BlockPos.containing(pos.getX(), player.getBoundingBox().maxY, pos.getZ());
		return !head.equals(pos) && player.level().isRainingAt(head);
	}

	/** 细雪：原版 {@code isInPowderSnow} 标记，或者脚所在格就是细雪方块。 */
	private static boolean isInPowderSnow(ServerPlayer player, BlockPos pos) {
		return player.isInPowderSnow
				|| player.level().getBlockState(pos).is(Blocks.POWDER_SNOW);
	}

	// ── 层 8 ｜ 对快乐值上限的影响 ──

	/**
	 * 环境值给快乐值上限扣的点数：环境值 ≥ 80 时为 0；80 以下每少 5 点扣 1 点。
	 *
	 * <p>例：80 → 0；75 → 1；60 → 4；0 → <b>16</b>（于是快乐值上限只剩 {@code 20 − 16 = 4}）。
	 */
	public static float happinessPenalty(float environment) {
		if (environment >= HAPPINESS_KNEE) {
			return 0.0F;
		}
		return (HAPPINESS_KNEE - environment) / HAPPINESS_PENALTY_STEP;
	}

	// ── 层 8 ｜ 读写 ──

	/** 读环境值（没有记录时按默认初始化成满值）。 */
	public static float get(ServerPlayer player) {
		return Mth.clamp(player.getAttachedOrCreate(VALUE), 0.0F, MAX);
	}

	/** 写环境值（自动夹在 0~100）。 */
	public static void set(ServerPlayer player, float value) {
		player.setAttached(VALUE, Mth.clamp(value, 0.0F, MAX));
	}
}
