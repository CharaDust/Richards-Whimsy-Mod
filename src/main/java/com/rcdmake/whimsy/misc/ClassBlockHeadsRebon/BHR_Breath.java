package com.rcdmake.whimsy.misc.ClassBlockHeadsRebon;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 屏息（愚者模式）。
 *
 * <h2>规则</h2>
 * <ul>
 *   <li>触发：<b>潜行 + 正在拉弓 / 蓄力三叉戟</b>（弩不算 —— 弩是待激发，不是持续蓄力）；</li>
 *   <li>消耗：氧气 3 点/游戏 tick；精力消耗本来就因为「瞄准」+2 点/tick，屏息不再叠一层；</li>
 *   <li>氧气见底：<b>不窒息</b>，只取消屏息；要重新进入必须<b>松开潜行再按</b>（{@link BHR_Energy#BREATH_BLOCKED} 锁存）；</li>
 *   <li>持续 2 秒后：射出的箭 / 三叉戟<b>散布归零</b>（见两个准度 mixin）。</li>
 * </ul>
 *
 * <h2>精度从哪来</h2>
 * <p>26.2 的散布在 {@code Projectile#getMovementToShoot} 里：
 * <pre>
 *   (new Vec3(xd,yd,zd)).normalize()
 *     .add(random.triangle(0, 0.0172275 * uncertainty) × 3 轴)
 *     .scale(pow)
 * </pre>
 * 也就是<b>射弹自己抽的随机数</b>，与玩家移动无关（原版弓没有移动散布惩罚）。
 * 弓的 {@code uncertainty = 1.0}，三叉戟也是 {@code 1.0}；把它压成 0，轨迹就只由
 * 朝角 + 速度 + 重力/阻力决定 —— 也就是固定的抛物线。
 */
public final class BHR_Breath {

	/** 屏息时每 tick 消耗的氧气。 */
	public static final int OXYGEN_PER_TICK = 3;

	/** 屏息多久后射弹散布归零（2 秒）。 */
	public static final int ACCURACY_TICKS = 40;

	/** 每个玩家已经屏息了多少 tick（0 = 没在屏息）。 */
	private static final Map<UUID, Integer> HELD = new HashMap<>();

	/**
	 * 屏息期间我们「记账」的氧气值。
	 *
	 * <p>不能靠「每 tick 减 3」去和原版的回氧抵消：原版不在水里时回氧比 3 还快，
	 * 净变化会变成正的（氧气条看起来一直是满的）。所以每 tick 用<b>绝对值</b>覆盖，
	 * 把回氧吃掉。
	 */
	private static final Map<UUID, Integer> AIR = new HashMap<>();

	private BHR_Breath() {
	}

	/** 手上正在「持续蓄力」的武器（弓 / 三叉戟，不含弩）。 */
	public static boolean isCharging(LivingEntity entity) {
		if (!entity.isUsingItem()) {
			return false;
		}
		ItemStack stack = entity.getUseItem();
		return stack.getItem() instanceof BowItem || stack.getItem() instanceof TridentItem;
	}

	/** 手上正在「瞄准」的武器（弓 / 弩 / 三叉戟）—— 只影响精力消耗与箭头。 */
	public static boolean isAiming(LivingEntity entity) {
		if (!entity.isUsingItem()) {
			return false;
		}
		ItemStack stack = entity.getUseItem();
		return stack.getItem() instanceof BowItem
				|| stack.getItem() instanceof CrossbowItem
				|| stack.getItem() instanceof TridentItem;
	}

	/** 这个玩家是不是正在屏息。 */
	public static boolean isHolding(LivingEntity entity) {
		return HELD.getOrDefault(entity.getUUID(), 0) > 0;
	}

	/** 这个玩家屏息够 2 秒了吗（射弹散布归零的判据）。 */
	public static boolean hasMaxAccuracy(LivingEntity entity) {
		return HELD.getOrDefault(entity.getUUID(), 0) >= ACCURACY_TICKS;
	}

	/** 每 tick 推进屏息状态。 */
	public static void tick(ServerPlayer player) {
		UUID id = player.getUUID();
		boolean sneaking = player.isShiftKeyDown();

		if (!sneaking) {
			HELD.remove(id);
			AIR.remove(id);
			BHR_Energy.setBreathBlocked(player, false);
			return;
		}

		if (!isCharging(player) || BHR_Energy.isBreathBlocked(player)) {
			HELD.remove(id);
			AIR.remove(id);
			return;
		}

		// 第一次进入：从当前氧气开始记账
		int tracked = AIR.computeIfAbsent(id, key -> Math.max(0, player.getAirSupply()));
		int next = Math.max(0, tracked - OXYGEN_PER_TICK);

		// 绝对值覆盖：把原版这一 tick 的回氧直接抹掉
		player.setAirSupply(next);
		AIR.put(id, next);

		if (next <= 0) {
			// 氧气见底：取消屏息，但不窒息；锁存到松开潜行为止
			BHR_Energy.setBreathBlocked(player, true);
			HELD.remove(id);
			AIR.remove(id);
			return;
		}

		HELD.merge(id, 1, Integer::sum);
	}

	/** 玩家离开世界时清状态。 */
	public static void clear(ServerPlayer player) {
		HELD.remove(player.getUUID());
		AIR.remove(player.getUUID());
		BHR_Energy.setBreathBlocked(player, false);
	}
}
