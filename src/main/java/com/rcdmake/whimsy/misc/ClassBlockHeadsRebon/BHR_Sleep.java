package com.rcdmake.whimsy.misc.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 愚者模式的睡眠系统（服务端）。
 *
 * <h2>它做五件事</h2>
 * <ol>
 *   <li><b>入睡门槛</b>：主条 &gt; {@link BHR_Energy#SLEEP_GATE} 不许睡（{@code BHR_ServerPlayerSleepMixin}）；</li>
 *   <li><b>两组比例</b>：原版跳夜只看非愚者（{@code BHR_SleepStatusMixin} 把愚者从原版分子里剔掉），
 *       加速只看愚者（本类每 tick 自己数）；</li>
 *   <li><b>加速时间</b>：愚者睡够比例 → 世界时钟 rate = {@link #FAST_RATE}；否则恢复原值；</li>
 *   <li><b>跳夜优先</b>：非愚者满足比例时照原版跳夜，但只叫醒非愚者；
 *       睡着的愚者按跳过的时间平账（Δ 世界 tick × 1 点），满了强制醒；</li>
 *   <li><b>睡眠补充</b>：按<b>世界 tick</b> 结算，床越好补得越快；补满给黄条并强制醒。</li>
 * </ol>
 *
 * <h2>为什么按世界 tick 结算</h2>
 * <p>加速 20× 时，1 个游戏 tick 里世界走 20 tick，所以「每世界 tick 1 点」在真实时间上
 * 表现成 20 点/游戏 tick。跳夜同理：跳过 Δ 个世界 tick 就补 Δ 点，两种来源共用一个单位。
 *
 * <h2>状态量</h2>
 * <p>{@link #LAST_TOTAL} 记每个时钟上一 tick 的 {@code totalTicks}，差值就是「这一 tick 世界走了多少」。
 * 必须在<b>服务端 tick 级</b>算一次（多个维度可能共用同一个时钟），所以先汇总再分发给各维度。
 */
public final class BHR_Sleep {

	// ── 层 1 ｜ 规格常量 ──

	/** 加速倍率：世界时钟每 tick 前进多少世界 tick。 */
	public static final float FAST_RATE = 20.0F;

	/** 跳夜时每 1 个世界 tick 折算的精力点（与睡眠补充的「1 点/世界 tick」同口径）。 */
	public static final float SKIP_CREDIT = 1.0F;

	/** 世界进程倍率：5 倍（1 次原版 + 4 次额外）。 */
	public static final int WORLD_PROCESS_FACTOR = 5;

	/** 世界进程加速的半径（区块）。 */
	public static final int WORLD_PROCESS_RADIUS = 2;

	/** 两次加速音效之间的最短间隔（毫秒），防止比例抖动导致连发。 */
	private static final long SOUND_COOLDOWN_MS = 2000L;

	/** 睡眠系统的日志（只在状态跃变时打，平时安静）。 */
	private static final org.slf4j.Logger LOGGER =
			org.slf4j.LoggerFactory.getLogger(com.rcdmake.whimsy.RichardsWhimsyMod.MOD_ID + "/sleep");

	// ── 层 1 ｜ 运行期状态 ──

	/** 每个时钟上一 tick 的 totalTicks。 */
	private static final Map<Holder<WorldClock>, Long> LAST_TOTAL = new HashMap<>();

	/** 我们是否正把这个时钟按在加速档上。 */
	private static final Map<Holder<WorldClock>, Boolean> ENGAGED = new HashMap<>();

	/** 接管之前的速率（恢复用）。 */
	private static final Map<Holder<WorldClock>, Float> RESTORE = new HashMap<>();

	/** 未接管时观测到的自然速率（管理员改过 /time rate 时尽量还原到它）。 */
	private static final Map<Holder<WorldClock>, Float> OBSERVED = new HashMap<>();

	/** 本 tick 发生了跳夜的维度（这些维度的睡眠补充让位给跳夜平账）。 */
	private static final Set<ServerLevel> SKIPPED = new HashSet<>();

	/** 每个维度上一次播加速音效的时间。 */
	private static final Map<ServerLevel, Long> LAST_SOUND = new HashMap<>();

	/** 睡眠期间用于「这一觉是否已结算过奖励」的玩家集合。 */
	private static final Set<java.util.UUID> REWARDED = new HashSet<>();

	/**
	 * 正在「席地而睡 / 强制睡眠」的玩家。
	 *
	 * <p>原版 {@code LivingEntity#tick} 每 tick 检查「睡的那格是不是床」，不是就叫醒 ——
	 * 席地而睡没有床，所以必须让 {@code BHR_FloorSleepMixin} 认出这些人。
	 */
	private static final Set<java.util.UUID> FLOOR_SLEEPING = new HashSet<>();

	private BHR_Sleep() {
	}

	// ── 层 12 ｜ 初始化 ──

	public static void OnInit() {
		ServerTickEvents.END_SERVER_TICK.register(BHR_Sleep::onServerTick);
	}

	// ── 层 8 ｜ 每 tick ──

	private static void onServerTick(MinecraftServer server) {
		Map<Holder<WorldClock>, Long> deltas = clockDeltas(server);

		for (ServerLevel level : server.getAllLevels()) {
			tickLevel(level, deltas);
		}

		SKIPPED.clear();
	}

	/** 先把每个时钟这一 tick 走了多少算出来（多维度共用时钟时只算一次）。 */
	private static Map<Holder<WorldClock>, Long> clockDeltas(MinecraftServer server) {
		Map<Holder<WorldClock>, Long> out = new HashMap<>();
		for (ServerLevel level : server.getAllLevels()) {
			Optional<Holder<WorldClock>> opt = level.dimensionType().defaultClock();
			if (opt.isEmpty()) {
				continue;
			}
			Holder<WorldClock> clock = opt.get();
			if (out.containsKey(clock)) {
				continue;
			}
			long now = server.clockManager().getTotalTicks(clock);
			Long prev = LAST_TOTAL.put(clock, now);
			out.put(clock, prev == null ? 0L : Math.max(0L, now - prev));
		}
		return out;
	}

	private static void tickLevel(ServerLevel level, Map<Holder<WorldClock>, Long> deltas) {
		Optional<Holder<WorldClock>> clockOpt = level.dimensionType().defaultClock();
		Holder<WorldClock> clock = clockOpt.orElse(null);
		long delta = clock == null ? 0L : deltas.getOrDefault(clock, 0L);
		boolean skipped = SKIPPED.contains(level);

		List<ServerPlayer> fools = new ArrayList<>();
		List<ServerPlayer> sleepers = new ArrayList<>();
		int deepSleepers = 0;

		for (ServerPlayer player : level.players()) {
			if (player.gameMode() != GameType.BHR_FOOL_MODE) {
				continue;
			}
			fools.add(player);
			if (player.isSleeping()) {
				sleepers.add(player);
				if (!skipped) {
					refill(player, level, delta);
				}
			}
			if (player.isSleepingLongEnough()) {
				deepSleepers++;
			}
		}

		// 精力见底的醒着愚者 → 强制睡眠（不给奖励、不算「在床上」，但计入愚者比例）
		for (ServerPlayer player : fools) {
			if (player.isSleeping()) {
				continue;
			}
			REWARDED.remove(player.getUUID());      // 新的一觉：奖励资格重置
			FLOOR_SLEEPING.remove(player.getUUID()); // 已经醒了，席地标记失效
			if (BHR_Energy.get(player) <= 0.0F && player.onGround()) {
				forceSleep(player);
			}
		}

		int percentage = level.getGameRules().<Integer>get(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
		int need = Math.max(1, Mth.ceil(fools.size() * percentage / 100.0F));
		boolean canAccelerate = clock != null && !level.dimensionType().hasFixedTime();
		boolean accelerate = canAccelerate && deepSleepers >= need;

		if (clock != null) {
			applyRate(level, clock, accelerate, delta, deepSleepers, need);
		}

		if (accelerate && !sleepers.isEmpty()) {
			accelerateWorldProcesses(level, sleepers);
		}
	}

	// ── 层 8 ｜ 睡眠补充 ──

	private static void refill(ServerPlayer player, ServerLevel level, long delta) {
		if (delta <= 0L) {
			return;
		}
		BHR_BedTier tier = tierAt(level, player);
		float gain = tier.perWorldTick * delta;
		if (gain <= 0.0F) {
			return;
		}
		BHR_Energy.set(player, BHR_Energy.get(player) + gain);
		if (BHR_Energy.get(player) >= BHR_Energy.MAX) {
			finishSleep(player, tier);
		}
	}

	/** 睡满：按床档发黄条，然后强制起床。 */
	private static void finishSleep(ServerPlayer player, BHR_BedTier tier) {
		if (tier.reward > 0.0F && REWARDED.add(player.getUUID())) {
			BHR_Energy.setSuper(player, BHR_Energy.getSuper(player) + tier.reward);
			LOGGER.info("[睡眠] {} 睡满（{}），黄条 +{}", player.getPlainTextName(), tier.name(), tier.reward);
		}
		player.stopSleepInBed(false, true);
	}

	/** 精力见底：原地躺下。起点睡眠计数为 0，避免「刚躺下就算睡够」。 */
	private static void forceSleep(ServerPlayer player) {
		BlockPos pos = player.blockPosition();
		player.startSleeping(pos);
		((com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon.BHR_SleepCounterAccess) (Object) player)
				.whimsy$setSleepCounter(0);
		FLOOR_SLEEPING.add(player.getUUID());
	}

	/**
	 * 这个玩家是不是正在「席地而睡」。
	 *
	 * <p>原版 {@code LivingEntity#tick} 每 tick 都会问「睡的那格是不是床」，不是就叫醒；
	 * 席地而睡没有床，所以 {@code BHR_FloorSleepMixin} 靠这个标记放行，
	 * 否则会出现 20 Hz 的睡/醒抖动（屏幕闪烁 + 位置被拉回 + 无法交互）。
	 */
	public static boolean isFloorSleeping(ServerPlayer player) {
		return FLOOR_SLEEPING.contains(player.getUUID());
	}

		/** 判定玩家睡在哪一档上。 */
	public static BHR_BedTier tierAt(ServerLevel level, ServerPlayer player) {
		return BHR_BedTier.of(level, player.getSleepingPos().orElse(null));
	}

	// ── 层 8 ｜ 速率控制 ──

	private static void applyRate(ServerLevel level, Holder<WorldClock> clock, boolean accelerate,
			long observedDelta, int deepSleepers, int need) {
		boolean engaged = ENGAGED.getOrDefault(clock, false);

		if (accelerate && !engaged) {
			RESTORE.put(clock, OBSERVED.getOrDefault(clock, 1.0F));
			level.getServer().clockManager().setRate(clock, FAST_RATE);
			ENGAGED.put(clock, true);
			LOGGER.info("[睡眠] 时间加速开启：深睡愚者 {} ≥ 需要 {}，时钟 {} → {}×",
					deepSleepers, need, clock.getRegisteredName(), FAST_RATE);
			playRateSound(level, true);
		} else if (!accelerate && engaged) {
			float restore = RESTORE.getOrDefault(clock, 1.0F);
			level.getServer().clockManager().setRate(clock, restore);
			ENGAGED.put(clock, false);
			LOGGER.info("[睡眠] 时间加速结束：深睡愚者 {} < 需要 {}，时钟 {} 恢复 {}×",
					deepSleepers, need, clock.getRegisteredName(), restore);
			playRateSound(level, false);
		} else if (!accelerate && observedDelta > 0L) {
			// 没接管时顺手记下自然速率（δ 已经是 floor 后的整数，小数速率会有 ±1 抖动）
			OBSERVED.put(clock, (float) observedDelta);
		}
	}

	private static void playRateSound(ServerLevel level, boolean speedUp) {
		long now = System.currentTimeMillis();
		Long last = LAST_SOUND.get(level);
		if (last != null && now - last < SOUND_COOLDOWN_MS) {
			return;
		}
		LAST_SOUND.put(level, now);

		SoundEvent sound = speedUp ? BHR_Sounds.BHR_SPEEDUP : BHR_Sounds.BHR_SLOWDOWN;
		if (sound == null) {
			return;
		}
		for (ServerPlayer player : level.players()) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					sound, SoundSource.AMBIENT, 1.0F, 1.0F);
		}
	}

	// ── 层 8 ｜ 原版跳夜（由 BHR_ServerLevelSleepMixin 调进来） ──

	/**
	 * 原版跳夜触发时的处理：只叫醒非愚者，睡着的愚者按跳过的时间平账。
	 *
	 * @param level   发生跳夜的维度
	 * @param status  该维度的原版睡眠统计（要清计数器）
	 * @param delta   这一跳让世界时钟前进了多少世界 tick
	 */
	public static void vanillaSkip(ServerLevel level, SleepStatus status, long delta) {
		status.removeAllSleepers();

		List<ServerPlayer> sleepers = new ArrayList<>();
		for (ServerPlayer player : level.players()) {
			if (player.isSleeping()) {
				sleepers.add(player);
			}
		}

		for (ServerPlayer player : sleepers) {
			if (player.gameMode() != GameType.BHR_FOOL_MODE) {
				player.stopSleepInBed(false, false);
			}
		}

		for (ServerPlayer player : sleepers) {
			if (player.gameMode() != GameType.BHR_FOOL_MODE || !player.isSleeping()) {
				continue;
			}
			if (!player.isSleepingLongEnough()) {
				continue;
			}
			BHR_Energy.set(player, BHR_Energy.get(player) + delta * SKIP_CREDIT);
			if (BHR_Energy.get(player) >= BHR_Energy.MAX) {
				finishSleep(player, tierAt(level, player));
			}
		}

		SKIPPED.add(level);
		LOGGER.info("[睡眠] 跳夜：世界时钟前进 {} tick，非愚者已叫醒，睡着的愚者按 1 点/tick 记账", delta);
	}

	// ── 层 8 ｜ 世界进程加速（时钟以外的「稍微快一点」） ──

	/**
	 * 只对睡着的愚者周围 {@link #WORLD_PROCESS_RADIUS} 区块做 5 倍加速：
	 * <ul>
	 *   <li>随机刻：多跑 4 遍 {@code tickChunk}（作物、草、雪、冰、树叶、火）；</li>
	 *   <li>方块实体：白名单（熔炉系、营火）多调 4 次 {@code tick()}。</li>
	 * </ul>
	 *
	 * <p>白名单之外一律不动 —— 尤其不碰红石元件、漏斗、刷怪笼，也不碰本模组其它模块
	 * （RDT 等）与第三方 mod 的方块实体。
	 */
	private static void accelerateWorldProcesses(ServerLevel level, List<ServerPlayer> sleepers) {
		int randomTickSpeed = level.getGameRules().<Integer>get(GameRules.RANDOM_TICK_SPEED);

		for (ServerPlayer sleeper : sleepers) {
			int cx = sleeper.blockPosition().getX() >> 4;
			int cz = sleeper.blockPosition().getZ() >> 4;

			for (int dx = -WORLD_PROCESS_RADIUS; dx <= WORLD_PROCESS_RADIUS; dx++) {
				for (int dz = -WORLD_PROCESS_RADIUS; dz <= WORLD_PROCESS_RADIUS; dz++) {
					LevelChunk chunk = level.getChunk(cx + dx, cz + dz);

					for (int i = 1; i < WORLD_PROCESS_FACTOR; i++) {
						level.tickChunk(chunk, randomTickSpeed);
					}

					boostWorkBlockEntities(level, chunk);
				}
			}
		}
	}

	/**
	 * 白名单方块实体多跑 {@code WORLD_PROCESS_FACTOR - 1} 次。
	 *
	 * <p>26.2 的方块实体没有 {@code tick()} 方法 —— 原版是通过
	 * {@code BlockState#getTicker(Level, BlockEntityType)} 拿到 ticker 再调，
	 * 这里走同一条路，避免去碰 {@code Level#tickBlockEntities} 的私有列表。
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void boostWorkBlockEntities(ServerLevel level, LevelChunk chunk) {
		for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
			if (blockEntity.isRemoved() || !isWorkBlockEntity(blockEntity)) {
				continue;
			}

			BlockState state = blockEntity.getBlockState();
			BlockEntityTicker<BlockEntity> ticker =
					(BlockEntityTicker<BlockEntity>) (BlockEntityTicker) state.getTicker(level, (BlockEntityType) blockEntity.getType());
			if (ticker == null) {
				continue;
			}

			for (int i = 1; i < WORLD_PROCESS_FACTOR; i++) {
				if (blockEntity.isRemoved()) {
					break;
				}
				ticker.tick(level, blockEntity.getBlockPos(), state, blockEntity);
			}
		}
	}

	/**
	 * 方块实体白名单：<b>只放原版工作方块</b>。
	 *
	 * <p>刻意排除：红石元件（比较器/中继器/漏斗/侦测器）、刷怪笼、酿造台，
	 * 以及所有第三方 mod（含本模组其它模块）的方块实体 —— 它们多 tick 一次的行为不可预期。
	 * 本模组 BHR 模块以后若有自己的方块实体，在这里加类型即可。
	 */
	private static boolean isWorkBlockEntity(BlockEntity blockEntity) {
		return blockEntity instanceof AbstractFurnaceBlockEntity
				|| blockEntity instanceof CampfireBlockEntity;
	}

	// ── 层 8 ｜ 给 mixin 用的小工具 ──

	/** 该玩家是不是愚者模式。 */
	public static boolean isFool(ServerPlayer player) {
		return player.gameMode() == GameType.BHR_FOOL_MODE;
	}

	/** 入睡门槛判定（主条高于 {@link BHR_Energy#SLEEP_GATE} 就不许睡）。 */
	public static boolean isTooRested(ServerPlayer player) {
		return isFool(player) && BHR_Energy.get(player) > BHR_Energy.SLEEP_GATE;
	}

	/** 门槛提示文案。 */
	public static Component tooRestedMessage() {
		return Component.translatable("bhr.sleep.too_rested");
	}
}


