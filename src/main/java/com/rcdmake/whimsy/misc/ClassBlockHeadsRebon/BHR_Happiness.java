package com.rcdmake.whimsy.misc.ClassBlockHeadsRebon;

import com.mojang.serialization.Codec;
import com.rcdmake.whimsy.ModRegister;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;

/**
 * 快乐值（{@code happiness}）：左侧 HUD 的第二条资源条，也是行走速度的实际来源。
 *
 * <h2>数值</h2>
 * <ul>
 *   <li>范围 <b>0 ~ 20</b>；</li>
 *   <li><b>上限由别的数值决定</b>，目前三个来源（<b>取三者最小值</b>）：
 *       <ul>
 *         <li><b>饥饿值</b>：饥饿越低上限越低，
 *             {@code 上限 = 20 - max(0, (10 - 饥饿值) / 2)}（饥饿 0 → 上限 15，饥饿 ≥10 → 20）；</li>
 *         <li><b>精力值</b>：低于 8000 开始线性压低上限，3000 时上限为 0
 *             （见 {@link BHR_Energy#happinessFactor(float)}）；</li>
 *         <li><b>环境值</b>：低于 80 时每少 5 点 −1，环境值 0 时上限 4
 *             （见 {@link BHR_Environment#happinessPenalty(float)}）；</li>
 *       </ul></li>
 *   <li>上限还被<b>当前生命值</b>压一头：{@code 上限 = min(其它上限, 当前生命)}，
 *       所以血不满时快乐值也不可能满；</li>
 *   <li>回复：没到上限时<b>每 2 tick +0.1</b>（≈ 每秒 1 点）。</li>
 * </ul>
 *
 * <h2>行走速度</h2>
 * 用 {@code Attributes.MOVEMENT_SPEED} 的修饰符（{@code ADD_MULTIPLIED_TOTAL}）：
 * <ul>
 *   <li>快乐值 ≥ 10：不加修饰符，完全正常；</li>
 *   <li>快乐值 &lt; 10：每少 1 点 −8%，即 {@code amount = -0.08 × (10 - 快乐值)}；</li>
 *   <li>快乐值 0 时 {@code amount = -0.8} → 移速只剩 <b>20%</b>。</li>
 * </ul>
 * 修饰符是 transient（不进存档），每次 tick 按当前快乐值刷新；重登/换维度后由 tick 重新补上。
 *
 * <h2>同步</h2>
 * 与濒死值同款：持久化 + 按玩家自身同步的附件，客户端血条直接用
 * {@code player.getAttached…(VALUE)} 读，不需要自己发包。
 */
public final class BHR_Happiness {

	// ── 规格常量 ──

	/** 快乐值上限（满值）。 */
	public static final float MAX = 20.0F;

	/** 超过这个值不再惩罚移速。 */
	public static final float NEUTRAL = 10.0F;

	/** 每少 1 点快乐值减少的移速比例。 */
	public static final float SPEED_PENALTY_PER_POINT = 0.08F;

	/** 每少 1 点快乐值减少的挖掘速度比例（20 点全缺 → 只剩 20%）。 */
	public static final float MINING_PENALTY_PER_POINT = 0.04F;

	/** 每次回复量。 */
	public static final float REGEN_STEP = 0.1F;

	/** 每多少 tick 回复一次。 */
	public static final int REGEN_INTERVAL = 2;

	/** 饥饿值对上限的影响以 10 为界。 */
	public static final int HUNGER_PIVOT = 10;

	/** 附件路径。 */
	public static final String VALUE_PATH = "happiness";

	/** 速度修饰符的 id。 */
	private static final Identifier SPEED_MODIFIER_ID = ModRegister.id("happiness_speed");

	/** 快乐值本身：持久化 + 按玩家自身同步。 */
	public static final AttachmentType<Float> VALUE = AttachmentRegistry.create(
			ModRegister.id(VALUE_PATH),
			builder -> builder
					.persistent(Codec.FLOAT)
					.initializer(() -> MAX)
					.syncWith(ByteBufCodecs.FLOAT, AttachmentSyncPredicate.targetOnly()));

	private BHR_Happiness() {
	}

	// ── 初始化 ──

	public static void OnInit() {
		ServerPlayerEvents.JOIN.register(BHR_Happiness::clampToCap);
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			// 新生一条命：快乐值直接补到上限
			set(newPlayer, cap(newPlayer));
			applySpeed(newPlayer);
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tick(player);
				applySpeed(player);
			}
		});
	}

	// ── 每 tick 的数值推进 ──

	/**
	 * 上限变化分两种处理：
	 * <ul>
	 *   <li><b>生命值下降</b>导致的上限下降 —— <b>瞬时</b>生效（血少了就是少了）；</li>
	 *   <li><b>饥饿值下降</b>导致的上限下降 —— 按回复速度<b>慢慢降</b>回上限。</li>
	 * </ul>
	 *
	 * <p><b>只有愚者模式有快乐值</b>：不在愚者模式时不做任何推进，并把它复位成满值
	 * （这样移速/挖掘速度的惩罚自然为 0，等于"没有快乐值"）。
	 */
	private static void tick(ServerPlayer player) {
		if (player.gameMode() != GameType.BHR_FOOL_MODE) {
			if (get(player) < MAX) {
				set(player, MAX);
			}
			return;
		}

		float value = get(player);
		float healthLimit = healthLimit(player);

		// 生命值压低的上限：瞬时生效
		if (value > healthLimit) {
			set(player, healthLimit);
			return;
		}

		if (player.tickCount % REGEN_INTERVAL != 0) {
			return;
		}

		float limit = cap(player);
		if (value < limit) {
			set(player, Math.min(limit, value + REGEN_STEP));       // 回复 / 慢慢补
		} else if (value > limit) {
			set(player, Math.max(limit, value - REGEN_STEP));       // 饥饿压低的超量：慢慢降
		}
	}

	/** 只由生命值决定的上限（20 与当前生命的较小值）。 */
	public static float healthLimit(ServerPlayer player) {
		return Mth.clamp(Math.min(MAX, player.getHealth()), 0.0F, MAX);
	}

	// ── 读写 ──

	public static float get(ServerPlayer player) {
		return Mth.clamp(player.getAttachedOrCreate(VALUE), 0.0F, MAX);
	}

	public static void set(ServerPlayer player, float value) {
		player.setAttached(VALUE, Mth.clamp(value, 0.0F, MAX));
	}

	/**
	 * 当前上限：饥饿值、精力值、环境值各给一个上限，取<b>最小</b>者，再被当前生命值压一头。
	 *
	 * <p>饥饿惩罚 {@code max(0, (10 - 饥饿值) / 2)}：饥饿 0 → 5（上限 15）、
	 * 饥饿 5 → 2.5（上限 17.5）、饥饿 ≥10 → 0。
	 *
	 * <p>精力上限 = {@code 20 × }{@link BHR_Energy#happinessFactor(float)}：
	 * 精力 ≥ 8000 → 20；3000 → 0；中间线性。
	 *
	 * <p>环境上限 = {@code 20 − }{@link BHR_Environment#happinessPenalty(float)}：
	 * 环境值 ≥ 80 → 20；每少 5 点 −1；环境值 0 → 上限 4。
	 *
	 * <p>最后 {@code min(…, 当前生命)}：血不满时快乐值上限也不会满。
	 */
	public static float cap(ServerPlayer player) {
		int hunger = player.getFoodData().getFoodLevel();
		float hungerPenalty = Math.max(0.0F, (HUNGER_PIVOT - hunger) / 2.0F);
		float hungerCap = MAX - hungerPenalty;
		float energyCap = MAX * BHR_Energy.happinessFactor(BHR_Energy.get(player));
		float environmentCap = MAX - BHR_Environment.happinessPenalty(BHR_Environment.get(player));
		float derived = Math.min(hungerCap, Math.min(energyCap, environmentCap));
		return Mth.clamp(Math.min(derived, player.getHealth()), 0.0F, MAX);
	}

	// ── 内部逻辑 ──

	/** 进服时只夹到「生命上限」，不凭空回满；超出的部分按生命值瞬时处理。 */
	private static void clampToCap(ServerPlayer player) {
		float limit = healthLimit(player);
		if (get(player) > limit) {
			set(player, limit);
		}
	}

	/** 按当前快乐值刷新移速修饰符（≥10 时移除，<10 时 −8%/点）。 */
	private static void applySpeed(ServerPlayer player) {
		AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed == null) {
			return;
		}

		float missing = Math.max(0.0F, NEUTRAL - get(player));
		if (missing <= 1.0E-4F) {
			speed.removeModifier(SPEED_MODIFIER_ID);
			return;
		}

		float amount = -SPEED_PENALTY_PER_POINT * missing;
		AttributeModifier existing = speed.getModifier(SPEED_MODIFIER_ID);
		if (existing == null || Math.abs(existing.amount() - amount) > 1.0E-4) {
			speed.addOrUpdateTransientModifier(new AttributeModifier(
					SPEED_MODIFIER_ID, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		}
	}
}
