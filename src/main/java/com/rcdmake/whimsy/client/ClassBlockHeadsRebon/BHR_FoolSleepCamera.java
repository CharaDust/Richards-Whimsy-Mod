package com.rcdmake.whimsy.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_Energy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * 愚者模式的「灵魂出窍」：<b>纯客户端</b>的观察相机。
 *
 * <h2>为什么用原版 {@link Marker} 当相机载体</h2>
 * <p>相机实体是原版自己的机制（{@code /spectate}、望远镜都走它）。借它有三个好处：
 * <ul>
 *   <li>黑屏渐变与手部渲染都按「相机实体是不是在睡」判断 —— 相机换成一个不睡的实体，
 *       这两样自动消失，不用去压 {@code isSleeping}；</li>
 *   <li>位置/朝向插值由原版 {@code Camera} 负责，不用自己写平滑；</li>
 *   <li>Marker 无渲染、无 AI、无碰撞 —— 本地 new 一个丢进 {@link ClientLevel} 即可，
 *       <b>不发包、不注册实体类型、不进存档</b>，服务器完全无感。</li>
 * </ul>
 *
 * <h2>边界</h2>
 * <ul>
 *   <li>只能在<b>客户端已加载的区块</b>内看（不做服务端推流）；</li>
 *   <li>移动方向由 WASD / 空格 / 潜行读<b>原始按键</b>，不碰 {@code player.input}
 *       （那个东西会被其它客户端模组替换）；</li>
 *   <li>转向由 {@code MouseHandler.turnPlayer} 的 redirect 交给相机，
 *       <b>身体不会转</b>，也就不会给服务端发转向包；</li>
 *   <li>醒来 / 死亡 / 换维度 / 断线 → 自动归位。</li>
 * </ul>
 */
public final class BHR_FoolSleepCamera {

	/** 每秒移动的格数（自由相机的手感值）。 */
	private static final double SPEED_PER_SECOND = 12.0;

	/** 当前的观察实体；{@code null} = 没在出窍。 */
	private static Marker camera;

	/** 一次性旗标：下一次关闭床上界面时不要发「停止睡眠」。 */
	private static boolean suppressWakeOnce;

	private BHR_FoolSleepCamera() {
	}

	/** 正在出窍吗。 */
	public static boolean isActive() {
		return camera != null;
	}

	/** 当前相机实体（给 mixin 判断用）。 */
	public static Marker cameraEntity() {
		return camera;
	}

	/** 背包按钮：进去 / 出来。 */
	public static void toggle() {
		if (isActive()) {
			exit();
		} else {
			enter();
		}
	}

	/** 进入观察模式。 */
	public static void enter() {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null || !player.isSleeping() || !(player.level() instanceof ClientLevel level)) {
			return;
		}

		Marker marker = new Marker(EntityTypes.MARKER, level);
		marker.setPos(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
		marker.setYRot(player.getYRot());
		marker.setXRot(player.getXRot());
		level.addEntity(marker);

		camera = marker;
		minecraft.setCameraEntity(marker);
	}

	/**
	 * 请求「关掉床上的聊天界面，但不要顺便起床」。
	 *
	 * <p>原版 {@code InBedChatScreen#onClose} 会发 {@code STOP_SLEEPING} ——
	 * 也就是说<b>任何</b>关掉那个界面的行为都会让人起床。我们切去观察模式时必须关界面
	 * （否则输入被 GUI 吃掉、相机不动），所以先立一个一次性旗标，让 mixin 把那次唤醒吞掉。
	 */
	public static void requestExitChatWithoutWake() {
		suppressWakeOnce = true;
	}

	/** 由 {@code InBedChatScreen} 的 mixin 消费：这一次关界面要不要跳过唤醒。 */
	public static boolean consumeSuppressWake() {
		boolean value = suppressWakeOnce;
		suppressWakeOnce = false;
		return value;
	}

	/** 回到身体。 */
	public static void exit() {
		Minecraft minecraft = Minecraft.getInstance();
		if (camera != null) {
			camera.discard();
			camera = null;
		}
		if (minecraft.player != null) {
			minecraft.setCameraEntity(minecraft.player);
		}
	}

	/** 背包按钮：起床（先归位，再发原版的「停止睡眠」包）。 */
	public static void wakeUp() {
		Minecraft minecraft = Minecraft.getInstance();
		exit();
		if (minecraft.player != null) {
			minecraft.player.connection.send(
					new ServerboundPlayerCommandPacket(minecraft.player,
							ServerboundPlayerCommandPacket.Action.STOP_SLEEPING));
		}
		minecraft.gui.setScreen(null);
	}

	/** 客户端每 tick：状态兜底 + 移动。 */
	public static void tick(Minecraft minecraft) {
		if (camera == null) {
			return;
		}

		LocalPlayer player = minecraft.player;
		if (player == null || !player.isSleeping() || player.level() != camera.level()) {
			exit();
			return;
		}

		// 有界面时不移动（和 freecam 模组的行为一致）
		if (minecraft.gui.screen() != null) {
			return;
		}

		moveCamera(minecraft);
	}

	private static void moveCamera(Minecraft minecraft) {
		double forward = 0.0;
		double strafe = 0.0;
		double up = 0.0;

		if (minecraft.options.keyUp.isDown()) {
			forward += 1.0;
		}
		if (minecraft.options.keyDown.isDown()) {
			forward -= 1.0;
		}
		if (minecraft.options.keyRight.isDown()) {
			strafe += 1.0;
		}
		if (minecraft.options.keyLeft.isDown()) {
			strafe -= 1.0;
		}
		if (minecraft.options.keyJump.isDown()) {
			up += 1.0;
		}
		if (minecraft.options.keyShift.isDown()) {
			up -= 1.0;
		}

		if (forward == 0.0 && strafe == 0.0 && up == 0.0) {
			return;
		}

		// 归一化，斜着走不会更快
		double horizontal = Math.sqrt(forward * forward + strafe * strafe);
		if (horizontal > 1.0) {
			forward /= horizontal;
			strafe /= horizontal;
		}

		float yawRad = camera.getYRot() * Mth.DEG_TO_RAD;
		double fx = -Mth.sin(yawRad);
		double fz = Mth.cos(yawRad);
		// 右向量 = 朝向顺时针转 90°
		double rx = -Math.cos(yawRad);
		double rz = -Mth.sin(yawRad);

		double step = SPEED_PER_SECOND / 20.0;
		Vec3 delta = new Vec3(
				(fx * forward + rx * strafe) * step,
				up * step,
				(fz * forward + rz * strafe) * step);

		camera.setPos(camera.getX() + delta.x, camera.getY() + delta.y, camera.getZ() + delta.z);
	}

	/** 给 HUD 用：这一帧主条是不是该显示「补充中」的 5 个箭头。 */
	public static boolean isRefilling(Player player) {
		return player.isSleeping();
	}

	/** 该玩家现在的黄条值（客户端读同步下来的附件）。 */
	public static float superEnergy(Player player) {
		return player.getAttachedOrElse(BHR_Energy.SUPER, 0.0F);
	}
}

