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
	private static final double SPEED_PER_SECOND = 10.9;

	/** 速度收敛系数（惯性手感）：1 = 立刻达到目标速度。 */
	private static final double ACCELERATION = 0.35;

	/** 当前相机速度（格/tick），带一点惯性。 */
	private static Vec3 velocity = Vec3.ZERO;

	/** 当前的观察实体；{@code null} = 没在出窍。 */
	private static Marker camera;

	/** 一次性旗标：下一次关闭床上界面时不要发「停止睡眠」。 */
	private static boolean suppressWakeOnce;

	/** 本地假实体的 id 分配器（负数，避开真实实体 id）。 */
	private static int NEXT_FAKE_ID = -1000;

	/** 出窍相关的日志（只在进/出时各一行，方便实机核对）。 */
	private static final org.slf4j.Logger LOGGER =
			org.slf4j.LoggerFactory.getLogger(com.rcdmake.whimsy.RichardsWhimsyMod.MOD_ID + "/camera");

	/** 床上界面里那枚「观察世界」按钮（界面关闭时清空）。 */
	private static net.minecraft.client.gui.components.Button observeButton;

	/** 按钮的动作（多半给聊天界面用：链接会抢点击，得由 mixin 直接触发）。 */
	private static Runnable observeAction;

	/** 登记/清除按钮引用与动作。 */
	public static void setObserveButton(net.minecraft.client.gui.components.Button button, Runnable action) {
		observeButton = button;
		observeAction = action;
	}

	/** 当前屏幕上那枚按钮（没有界面时为 null）。 */
	public static net.minecraft.client.gui.components.Button observeButton() {
		return observeButton;
	}

	/** 触发按钮动作（与点击等价）。 */
	public static void runObserveAction() {
		if (observeAction != null) {
			observeAction.run();
		}
	}

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
		captureOld(marker);

		// ⚠️ 不要 level.addEntity(marker)：
		// 客户端实体必须先在同步包里拿到服务端分配的 id，而本地 new 出来的实体 id 是 0，
		// ClientLevel#addEntity 会直接读 getId() → IllegalStateException: Tried to access entity ID before ID assignment。
		// 相机只读实体的坐标/朝向，不要求它进实体表；这里补一个负数 id 兜住任何读 id 的路径
		// （负数不会和真实实体 id 撞）。
		marker.setId(NEXT_FAKE_ID--);

		camera = marker;
		minecraft.setCameraEntity(marker);
		LOGGER.info("[出窍] 进入观察：相机实体 id={}，起点 ({}, {}, {})",
				marker.getId(), String.format("%.1f", marker.getX()),
				String.format("%.1f", marker.getY()), String.format("%.1f", marker.getZ()));
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
		velocity = Vec3.ZERO;
		if (camera != null) {
			camera.discard();
			camera = null;
			LOGGER.info("[出窍] 返回身体");
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

		// 每 tick 先把「上一 tick 的位置/朝向」记下来 —— 等价于原版 Entity#tick 里的
		// setOldPosAndRot()。相机每帧是 lerp(partialTicks, 旧值, 当前值) 渲染的，
		// 少了这一步就会每 tick 硬跳一格（就是之前那种一顿一顿）。
		captureOld(camera);

		// 有界面时不移动（和 freecam 模组的行为一致）
		if (minecraft.gui.screen() != null) {
			velocity = Vec3.ZERO;
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

		double speed = SPEED_PER_SECOND / 20.0;
		Vec3 target = new Vec3(
				(fx * forward + rx * strafe) * speed,
				up * speed,
				(fz * forward + rz * strafe) * speed);

		// 惯性：速度朝目标值收敛，起停不至于像瞬移（观察者/创造飞行的手感）
		velocity = velocity.add(target.subtract(velocity).scale(ACCELERATION));
		if (velocity.lengthSqr() < 1.0E-6) {
			velocity = Vec3.ZERO;
			return;
		}

		camera.setPos(camera.getX() + velocity.x, camera.getY() + velocity.y, camera.getZ() + velocity.z);
	}

	/**
	 * 记下「上一 tick 的位置/朝向」。
	 *
	 * <p>相机每帧是按 {@code lerp(partialTicks, xo, x)} 插值渲染的 —— 本地实体不在实体表里、
	 * 不会被 {@code tick()}，{@code xo/yo/zo} 会永远停在 0（镜头在原点与身体间乱跳），
	 * 或者在被我们同步成当前值后变成「每 tick 硬跳一格」（一顿一顿）。
	 * 正确做法是每 tick 开始时把旧值对齐到「移动前」，剩下的交给插值。
	 */
	private static void captureOld(Marker marker) {
		marker.xo = marker.getX();
		marker.yo = marker.getY();
		marker.zo = marker.getZ();
		marker.yRotO = marker.getYRot();
		marker.xRotO = marker.getXRot();
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


