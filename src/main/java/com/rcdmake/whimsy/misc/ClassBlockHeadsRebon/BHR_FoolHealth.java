package com.rcdmake.whimsy.misc.ClassBlockHeadsRebon;

import com.mojang.serialization.Codec;
import com.rcdmake.whimsy.ModRegister;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

/**
 * 愚者模式的两层生命模型（第一层 = 原版血量，第二层 = 濒死值）。
 *
 * <h2>模型</h2>
 * <ul>
 *   <li>第一层：原版 {@code health}，上限仍是 20（<b>不改 {@code MAX_HEALTH}</b>，
 *       所以原版和其它模组看到的一切都没变）；</li>
 *   <li>第二层：濒死值 {@code pool}，0~20，存在 Fabric 的
 *       {@linkplain AttachmentRegistry#createPersistent 持久化附件} 里，随玩家存档、按玩家自身同步；</li>
 *   <li>伤害先吃第一层，第一层见底再吃第二层；第二层见底才真正死亡；</li>
 *   <li>治疗/自然回复<b>先补第二层</b>，第二层满了才补第一层。</li>
 * </ul>
 *
 * <h2>为什么第一层见底时血量是 1e-4 而不是 0</h2>
 * 原版把「血量 ≤ 0」当成死亡已经开始：
 * {@code LivingEntity.isDeadOrDying()} 一旦为真，{@code hurtServer} 开头就直接
 * {@code return false}（之后再也扣不到血），{@code baseTick} 还会在 20 tick 后把实体
 * {@code remove(KILLED)}。所以濒死期间必须让血量保持一个<b>极小的正值</b>：
 * 条上看起来是空的、所有原版判定都当"还活着"，而第二层归零时我们把真实结算值放行，
 * 原版死亡流程（死亡消息 / 击杀归属 / 图腾 / keepInventory / 极限模式）原样接管。
 *
 * <h2>切换游戏模式</h2>
 * <ul>
 *   <li>进入愚者模式：不补血、不动第二层，直接沿用上次存下的濒死值（没有记录时默认满 20）；</li>
 *   <li>离开愚者模式：只保证「至少半颗心」（{@code health >= 1.0}），不会再因为第一层见底
 *       而在切回生存的瞬间被杀；濒死值原样保留在存档里，下次进愚者模式继续用。</li>
 * </ul>
 *
 * <h2>其它约定</h2>
 * <ul>
 *   <li>只有玩家有濒死值，其它生物完全不受影响；</li>
 *   <li>图腾在第二层见底的那一刀触发（走原版 {@code checkTotemDeathProtection}），
 *       它回复的 1 点血会被换算成「一点点濒死值」，玩家仍处在危险里；</li>
 *   <li>重生 / 进服：濒死值与生命值都会是满的；</li>
 *   <li>别的模组直接 {@code setHealth(...)} 打血：只会打到第一层，由 {@link #repairTick}
 *       兜住（见下），濒死值不受影响 —— 这正是"两个独立槽位"的好处。</li>
 * </ul>
 */
public final class BHR_FoolHealth {

	// ── 层 1 ｜ 规格常量 ──

	/** 濒死值上限（固定 20 点）。 */
	public static final float POOL_MAX = 20.0F;

	/** 濒死期间第一层保留的极小血量（>0，条上看不见，但让原版认为还活着）。 */
	public static final float EPSILON = 1.0E-4F;

	/** 离开愚者模式时保证保留的血量 = 半颗心。 */
	public static final float HALF_HEART = 1.0F;

	/** 附件注册名（{@code richards-whimsy-mod:fool_death_pool}）。 */
	public static final String POOL_PATH = "fool_death_pool";

	// ── 层 1 ｜ 数据载体 ──

	/**
	 * 濒死值：持久化（进存档）+ 按玩家自身同步（客户端拿到自己那一份，血条/红边都靠它）。
	 *
	 * <p>同步用的是 Fabric 的 {@code syncWith}，<b>不需要自己写网络包</b>；
	 * {@link AttachmentSyncPredicate#targetOnly()} 表示只发给该玩家自己。
	 */
	public static final AttachmentType<Float> POOL = AttachmentRegistry.create(
			ModRegister.id(POOL_PATH),
			builder -> builder
					.persistent(Codec.FLOAT)
					.initializer(() -> POOL_MAX)
					.syncWith(ByteBufCodecs.FLOAT, AttachmentSyncPredicate.targetOnly()));

	private BHR_FoolHealth() {
	}

	// ── 层 12 ｜ 初始化 ──

	/** 注册事件（进服 / 重生 / 每 tick 的兜底修复）。由主类调用。 */
	public static void OnInit() {
		ServerPlayerEvents.JOIN.register(player -> getPool(player));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			// 重生后两层都填满
			setPool(newPlayer, POOL_MAX);
			newPlayer.setHealth(newPlayer.getMaxHealth());
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				repairTick(player);
			}
		});
	}

	// ── 层 8 ｜ 查询与写入 ──

	/**
	 * 这个玩家现在是不是愚者模式。
	 *
	 * <p>只认服务端的 {@link ServerPlayer#gameMode()}：客户端那边由
	 * {@code BHR_FoolHealthVignetteMixin} 用 {@code MultiPlayerGameMode} 自己判断。
	 */
	public static boolean isFoolMode(Player player) {
		return player instanceof ServerPlayer serverPlayer
				&& serverPlayer.gameMode() == GameType.BHR_FOOL_MODE;
	}

	/** 读濒死值（没有记录时按默认初始化成满值）。 */
	public static float getPool(ServerPlayer player) {
		return Mth.clamp(player.getAttachedOrCreate(POOL), 0.0F, POOL_MAX);
	}

	/** 写濒死值（自动夹在 0~20）。 */
	public static void setPool(ServerPlayer player, float value) {
		player.setAttached(POOL, Mth.clamp(value, 0.0F, POOL_MAX));
	}

	// ── 层 8 ｜ 伤害 / 治疗 / 图腾 / 模式切换 ──

	/**
	 * 伤害结算的落点（由 {@code BHR_FoolHealthDamageMixin} 从
	 * {@code Player#actuallyHurt} 里的 {@code setHealth(health - dmg)} 重定向过来）。
	 *
	 * <p>这里的 {@code newHealth} 已经是「护甲 → 魔法吸收 → 伤害吸收」之后的结果，
	 * 所以不用自己算任何减伤，顺序天然正确（金苹果吸收在最前）。
	 *
	 * @param player    受伤的玩家
	 * @param newHealth 原版打算写入的血量（可能 ≤ 0）
	 */
	public static void onPlayerSetHealth(Player player, float newHealth) {
		if (!(player instanceof ServerPlayer serverPlayer) || !isFoolMode(serverPlayer)) {
			player.setHealth(newHealth);
			return;
		}

		// 第一层还有余量：原样写。
		if (newHealth > 0.0F) {
			player.setHealth(newHealth);
			return;
		}

		float pool = getPool(serverPlayer);

		// 第二层也空了：把真实结算值放行 → 原版死亡流程接管（图腾也在这条路上）。
		if (pool <= 0.0F) {
			player.setHealth(newHealth);
			return;
		}

		// 溢出的部分扣第二层；只要还有剩余，第一层就停在 EPSILON。
		float overflow = -newHealth;
		float remaining = pool - overflow;
		if (remaining > 0.0F) {
			setPool(serverPlayer, remaining);
			player.setHealth(EPSILON);
		} else {
			setPool(serverPlayer, 0.0F);
			player.setHealth(newHealth);
		}
	}

	/**
	 * 治疗/自然回复（由 {@code BHR_FoolHealthLivingMixin} 接管 {@code LivingEntity#heal}）。
	 *
	 * <p>顺序按需求：<b>先补濒死值，补满后剩余的才补生命值</b>。
	 * 与原版一致地忽略死亡中的实体（{@code health <= 0} 时不治疗）。
	 */
	public static void heal(ServerPlayer player, float amount) {
		if (amount <= 0.0F || player.getHealth() <= 0.0F) {
			return;
		}

		float pool = getPool(player);
		float intoPool = Math.min(amount, POOL_MAX - pool);
		if (intoPool > 0.0F) {
			setPool(player, pool + intoPool);
		}

		float rest = amount - intoPool;
		if (rest > 0.0F) {
			player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + rest));
		}
	}

	/**
	 * 图腾生效后（由 {@code BHR_FoolHealthLivingMixin} 在
	 * {@code checkTotemDeathProtection} 返回 true 时调用）。
	 *
	 * <p>原版图腾会把血量设回 1.0；在两层模型里那 1 点应该算作<b>濒死值</b>，
	 * 于是玩家只被"救回一点点"，依旧处在危险中（与需求一致）。
	 */
	public static void onTotemUsed(ServerPlayer player) {
		if (!isFoolMode(player)) {
			return;
		}

		float restored = Math.max(0.0F, player.getHealth());
		setPool(player, getPool(player) + restored);
		player.setHealth(EPSILON);
	}

	/**
	 * 游戏模式切换后的对账（由 {@code BHR_FoolHealthGameModeMixin} 在
	 * {@code ServerPlayerGameMode#changeGameModeForPlayer} 返回 true 时调用）。
	 */
	public static void onGameModeChanged(ServerPlayer player, GameType newMode) {
		// 两种方向都先把附件准备好（没有记录时初始化为满值）。
		getPool(player);

		if (newMode == GameType.BHR_FOOL_MODE) {
			return;
		}

		// 离开愚者模式：保证至少半颗心，濒死值原样留在存档里。
		if (player.getHealth() < HALF_HEART) {
			player.setHealth(HALF_HEART);
		}
	}

	// ── 层 8 ｜ 兜底 ──

	/**
	 * 每 tick 的兜底修复：愚者模式下血量被打到 ≤0、但第二层还有存货时，把血量拉回 EPSILON。
	 *
	 * <p>这是给「不走伤害管线、直接 {@code setHealth(0)}」的外来伤害准备的 ——
	 * 两个独立槽位的意义就在这里：对方最多把第一层打空，第二层不受影响，
	 * 而这里保证玩家不会因此进入原版的"濒死尸体"状态（20 tick 后被移除）。
	 *
	 * <p>第二层已经是 0 时不动：那是真正的死亡路径，交给原版。
	 */
	private static void repairTick(ServerPlayer player) {
		if (!isFoolMode(player)) {
			return;
		}

		if (player.getHealth() > 0.0F) {
			return;
		}

		if (getPool(player) <= 0.0F) {
			return;
		}

		player.setHealth(EPSILON);
	}
}
