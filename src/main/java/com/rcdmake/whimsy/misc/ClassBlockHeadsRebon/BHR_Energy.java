package com.rcdmake.whimsy.misc.ClassBlockHeadsRebon;

import com.mojang.serialization.Codec;
import com.rcdmake.whimsy.ModRegister;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_CoffeeCherry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

/**
 * 精力值（{@code energy}）：愚者模式右侧 HUD 的第三条资源条（饥饿条<b>正上方</b>那一行）。
 *
 * <h2>数值</h2>
 * <ul>
 *   <li>范围 <b>0 ~ 20000</b>；</li>
 *   <li><b>按时间消耗</b>：愚者模式下<b>每 tick −1</b>（≈ 20 点/秒，满值约 16 分 40 秒）；</li>
 *   <li>补充：<b>吃东西</b> —— 补充量 = {@code (饥饿值 + 饱和度) × 100}，
 *       即一件「3 点饥饿 + 1 点饱和度」的食物补 <b>400</b> 点；</li>
 *   <li>本模组的<b>咖啡樱桃</b>（{@code bhr_coffee_cherry}）额外再补 <b>2000</b> 点；</li>
 *   <li>睡觉也能补，但愚者模式的睡眠机制要改，<b>本轮不做</b>（见 {@code doc/wiki/Misc/愚者模式.md}）。</li>
 * </ul>
 *
 * <h2>对快乐值上限的影响</h2>
 * 精力值低于 {@link #HAPPINESS_KNEE}（8000）时，快乐值上限开始<b>线性下降</b>；
 * 到 {@link #HAPPINESS_FLOOR}（3000）时降到 <b>0</b>：
 * <pre>
 *   系数 = clamp((精力 − 3000) / (8000 − 3000), 0, 1)
 *   精力上限 = 20 × 系数
 * </pre>
 * 最后由 {@link BHR_Happiness#cap(ServerPlayer)} 与「饥饿上限」「当前生命」取最小值
 * （所以血不满时快乐值上限也不会满）。
 *
 * <h2>同步</h2>
 * 与濒死值 / 快乐值同款：持久化 + 按玩家自身同步的附件，客户端 HUD 直接
 * {@code player.getAttached…(VALUE)} 读，不需要自己发包。
 *
 * <h2>进出愚者模式</h2>
 * <ul>
 *   <li>进服：沿用存档里存下的精力值（不凭空补满）；</li>
 *   <li>重生：补满（新的一条命）；</li>
 *   <li>离开愚者模式：<b>暂停</b>消耗，数值原样留档（不像快乐值那样复位成满值 ——
 *       精力是一次性消耗品，切出去再切回来不该白送）。</li>
 * </ul>
 */
public final class BHR_Energy {

	// ── 层 1 ｜ 规格常量 ──

	/** 精力值上限（满值）。 */
	public static final float MAX = 20000.0F;

	/** 每 tick 的消耗量。 */
	public static final float DRAIN_PER_TICK = 1.0F;

	/** 吃东西的精力换算倍率：{@code (饥饿值 + 饱和度) × 该值}。 */
	public static final float FOOD_MULTIPLIER = 100.0F;

	/** 咖啡樱桃额外补充的精力值。 */
	public static final float COFFEE_CHERRY_BONUS = 2000.0F;

	/** 低于这个精力值，快乐值上限开始下降。 */
	public static final float HAPPINESS_KNEE = 8000.0F;

	/** 低于这个精力值，快乐值上限降到 0。 */
	public static final float HAPPINESS_FLOOR = 3000.0F;

	/** 附件路径（{@code richards-whimsy-mod:energy}）。 */
	public static final String VALUE_PATH = "energy";

	/** 黄条（睡眠奖励的「额外精力时间」）上限：与主条同为 20000，共用同一套像素刻度。 */
	public static final float SUPER_MAX = MAX;

	/** 黄条附件路径（{@code richards-whimsy-mod:super_energy}）。 */
	public static final String SUPER_PATH = "super_energy";

	/** 入睡门槛：主条高于这个值就不许睡（「你还不累」）。 */
	public static final float SLEEP_GATE = 16000.0F;

	/** 低于这个精力值开始给黑暗效果（每 5 秒续 10 秒）。 */
	public static final float DARKNESS_KNEE = 4000.0F;

	// ── 层 1 ｜ 数据载体 ──

	/** 精力值本身：持久化 + 按玩家自身同步。 */
	public static final AttachmentType<Float> VALUE = AttachmentRegistry.create(
			ModRegister.id(VALUE_PATH),
			builder -> builder
					.persistent(Codec.FLOAT)
					.initializer(() -> MAX)
					.syncWith(ByteBufCodecs.FLOAT, AttachmentSyncPredicate.targetOnly()));

	/** 黄条本身：持久化 + 按玩家自身同步（与主条同款）。 */
	public static final AttachmentType<Float> SUPER = AttachmentRegistry.create(
			ModRegister.id(SUPER_PATH),
			builder -> builder
					.persistent(Codec.FLOAT)
					.initializer(() -> 0.0F)
					.syncWith(ByteBufCodecs.FLOAT, AttachmentSyncPredicate.targetOnly()));

	private BHR_Energy() {
	}

	/** 屏息锁存（氧气见底 → true，松开潜行 → false）：同步给客户端画氧气箭头用。 */
	public static final AttachmentType<Boolean> BREATH_BLOCKED = AttachmentRegistry.create(
			ModRegister.id("breath_blocked"),
			builder -> builder
					.persistent(Codec.BOOL)
					.initializer(() -> Boolean.FALSE)
					.syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.targetOnly()));

	// ── 层 12 ｜ 初始化 ──

	/** 注册事件（重生补满 + 每 tick 消耗）。由主类调用。 */
	public static void OnInit() {
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			set(newPlayer, MAX);
			setSuper(newPlayer, 0.0F);
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tick(player);
			}
		});
	}

	// ── 层 8 ｜ 每 tick 的数值推进 ──

	/**
	 * 愚者模式每 tick −1；其它模式<b>不动</b>（数值原样留档，切回来接着用）。
	 *
	 * <p><b>睡眠期间暂停</b>：睡着时精力只进不出（补充由 {@code BHR_Sleep} 按世界 tick 结算）。
	 *
	 * <p><b>黄条先消耗</b>：只要黄条还有，主条就不掉 —— 所以「主条为空」必然意味着
	 * 黄条也已耗尽，强制睡眠的触发条件天然自洽。
	 */
	private static void tick(ServerPlayer player) {
		if (player.gameMode() != GameType.BHR_FOOL_MODE) {
			return;
		}

		// 屏息：氧气扣除与「氧气见底后锁存到松手」的状态机
		BHR_Breath.tick(player);

		if (player.isSleeping()) {
			return;
		}

		checkDarkness(player);

		float drain = drainPerTick(player);

		float superEnergy = getSuper(player);
		if (superEnergy > 0.0F) {
			setSuper(player, superEnergy - drain);
			return;
		}

		float value = get(player);
		if (value <= 0.0F) {
			return;
		}
		set(player, value - drain);
	}

	/**
	 * 这一 tick 的精力消耗。
	 *
	 * <ul>
	 *   <li>基础 1 点；</li>
	 *   <li><b>非主世界减半</b>（下界 / 末地探索期不至于被精力条锁死）；</li>
	 *   <li><b>瞄准中 +2 点</b>（弓 / 弩 / 三叉戟，见 {@link BHR_Breath#isAiming}）。</li>
	 * </ul>
	 */
	public static float drainPerTick(ServerPlayer player) {
		float drain = DRAIN_PER_TICK;
		if (!player.level().dimension().equals(Level.OVERWORLD)) {
			drain *= 0.5F;
		}
		if (BHR_Breath.isAiming(player)) {
			drain += 2.0F;
		}
		return drain;
	}

	/** 精力低于 {@link #DARKNESS_KNEE} 时每 5 秒续一次黑暗（10 秒，不叠加，只刷新时长）。 */
	private static void checkDarkness(ServerPlayer player) {
		if (player.tickCount % 100 != 0) {
			return;
		}
		if (get(player) < DARKNESS_KNEE) {
			player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 200, 0, false, false, true));
		}
	}

	/** 屏息被锁存了吗（氧气见底后必须松开潜行才能再屏息）。 */
	public static boolean isBreathBlocked(ServerPlayer player) {
		return player.getAttachedOrElse(BREATH_BLOCKED, Boolean.FALSE);
	}

	/** 写屏息锁存（同步给客户端，用来画氧气箭头）。 */
	public static void setBreathBlocked(ServerPlayer player, boolean blocked) {
		if (isBreathBlocked(player) != blocked) {
			player.setAttached(BREATH_BLOCKED, blocked);
		}
	}

	// ── 层 8 ｜ 吃东西补充 ──

	/**
	 * 玩家吃下一样东西后的精力结算（由 {@code BHR_EnergyFoodMixin} 从
	 * {@code FoodProperties#onConsume} 的 RETURN 处调进来，只在服务端、只在愚者模式生效）。
	 *
	 * <p>补充量 = {@code (nutrition + saturation) × }{@link #FOOD_MULTIPLIER}；
	 * 咖啡樱桃再额外 {@link #COFFEE_CHERRY_BONUS} 点。
	 *
	 * <p>注意：26.2 的 {@link FoodProperties#saturation()} 已经是<b>绝对饱和度</b>
	 * （原版 {@code FoodData#add} 直接加这个数），所以这里直接相加，不再乘饥饿值。
	 *
	 * @param player 食用者
	 * @param food   被吃掉那份食物的 {@code minecraft:food} 组件
	 * @param stack  被吃的物品（用来判断是不是咖啡樱桃）
	 */
	public static void onEaten(ServerPlayer player, FoodProperties food, ItemStack stack) {
		if (!BHR_FoolHealth.isFoolMode(player)) {
			return;
		}

		float gain = (food.nutrition() + food.saturation()) * FOOD_MULTIPLIER;
		if (isCoffeeCherry(stack)) {
			gain += COFFEE_CHERRY_BONUS;
		}
		if (gain > 0.0F) {
			set(player, get(player) + gain);
		}
	}

	/** 这一叠是不是咖啡樱桃（物品还没注册时按「不是」处理）。 */
	private static boolean isCoffeeCherry(ItemStack stack) {
		return BHR_CoffeeCherry.BHR_COFFEE_CHERRY != null
				&& stack.is(BHR_CoffeeCherry.BHR_COFFEE_CHERRY);
	}

	// ── 层 8 ｜ 对快乐值上限的影响 ──

	/**
	 * 精力值给快乐值上限打的折扣系数（0~1）：
	 * 满值~8000 → 1.0；8000~3000 → 从 1.0 线性降到 0；3000 以下 → 0。
	 */
	public static float happinessFactor(float energy) {
		if (energy >= HAPPINESS_KNEE) {
			return 1.0F;
		}
		if (energy <= HAPPINESS_FLOOR) {
			return 0.0F;
		}
		return (energy - HAPPINESS_FLOOR) / (HAPPINESS_KNEE - HAPPINESS_FLOOR);
	}

	// ── 层 8 ｜ 读写 ──

	/** 读精力值（没有记录时按默认初始化成满值）。 */
	public static float get(ServerPlayer player) {
		return Mth.clamp(player.getAttachedOrCreate(VALUE), 0.0F, MAX);
	}

	/** 写精力值（自动夹在 0~20000）。 */
	public static void set(ServerPlayer player, float value) {
		player.setAttached(VALUE, Mth.clamp(value, 0.0F, MAX));
	}

	/** 读黄条（没有记录时按 0 处理）。 */
	public static float getSuper(ServerPlayer player) {
		return Mth.clamp(player.getAttachedOrCreate(SUPER), 0.0F, SUPER_MAX);
	}

	/** 写黄条（自动夹在 0~20000）。 */
	public static void setSuper(ServerPlayer player, float value) {
		player.setAttached(SUPER, Mth.clamp(value, 0.0F, SUPER_MAX));
	}
}
