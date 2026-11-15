package com.rcdmake.whimsy.misc.ClassBlockHeadsRebon;

import com.mojang.serialization.Codec;
import com.rcdmake.whimsy.ModRegister;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;

/**
 * 把<b>消耗度</b>（{@code FoodData.getExhaustionLevel()}）同步到客户端。
 *
 * <p>原版的 {@code ClientboundSetHealthPacket} 只带「血量 / 饥饿值 / 饱和度」，
 * <b>不带消耗度</b>（AppleSkin 也是自己发包补这一项的）。而愚者模式的饥饿条要画
 * 消耗度叠加层、并且按「消耗度增量」出箭头，所以必须把它同步过来。
 *
 * <p>做法与濒死值/快乐值相同：一个<b>可同步附件</b>，服务器每 tick 把它刷成当前值，
 * 客户端直接 {@code player.getAttached…} 读。只在愚者模式下推送（别的模式没有这套 HUD）。
 */
public final class BHR_FoodSync {

	/** 附件路径。 */
	public static final String EXHAUSTION_PATH = "food_exhaustion";

	/** 消耗度的上限（原版 {@code FoodData.addExhaustion} 夹到 40）。 */
	public static final float EXHAUSTION_MAX = 40.0F;

	/**
	 * 消耗度叠加层的<b>显示</b>上限 = 4 点。
	 *
	 * <p>原版 {@code FoodData#tick} 是「消耗度攒够 4 点 → 扣 1 点饱和度（没有饱和度就扣饥饿值）」，
	 * 所以 0~4 才是「离下次掉一格还差多少」的那一段，叠加层按 4 铺满才有意义。
	 * 按 {@link #EXHAUSTION_MAX}（40）铺会只剩几个像素，看起来像只有一段。
	 *
	 * <p>同步过去的数值本身仍按 40 夹（那才是原版的上限），只是画的时候除以 4、超出部分截断。
	 */
	public static final float EXHAUSTION_DISPLAY_MAX = 4.0F;

	/** 消耗度：不持久化（每次登录由服务端重新推），按玩家自身同步。 */
	public static final AttachmentType<Float> EXHAUSTION = AttachmentRegistry.create(
			ModRegister.id(EXHAUSTION_PATH),
			builder -> builder
					.initializer(() -> 0.0F)
					.syncWith(ByteBufCodecs.FLOAT, AttachmentSyncPredicate.targetOnly()));

	private BHR_FoodSync() {
	}

	public static void OnInit() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				if (player.gameMode() != GameType.BHR_FOOL_MODE) {
					continue;
				}
				player.setAttached(EXHAUSTION, Mth.clamp(
						((com.rcdmake.whimsy.access.BHR_FoodDataAccess) player.getFoodData()).whimsy$getExhaustion(),
						0.0F, EXHAUSTION_MAX));
			}
		});
	}
}
