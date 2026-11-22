package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_FoolSleepCamera;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 出窍期间的快捷栏快捷键（照旁观者模式那套「命令栏」的手感）。
 *
 * <ul>
 *   <li><b>3</b> 号键 → 返回身体</li>
 *   <li><b>4</b> 号键 → 起床</li>
 * </ul>
 *
 * <h2>为什么要点两次</h2>
 * <p>旁观者模式的命令栏也是先「选中」再「确认」：第一次按只是把该格点亮并提示，
 * 再按一次才执行。这里照抄这个手感 —— 第一次按给出动作栏提示，<b>2 秒内</b>再按同一个键才生效。
 *
 * <h2>为什么挂在 {@code Minecraft#handleKeybinds} 的 HEAD</h2>
 * <p>快捷栏键是原版在这里逐个 {@code consumeClick()} 消费的。我们在它之前先消费掉，
 * 按键既不会顺带切换选中槽位，也不会被别的逻辑抢走。
 */
@Mixin(Minecraft.class)
public abstract class BHR_MinecraftHotbarMixin {

	/** 快捷栏第 3 格（下标 2）：返回身体。 */
	@Unique
	private static final int WHIMSY_SLOT_RETURN_BODY = 2;

	/** 快捷栏第 4 格（下标 3）：起床。 */
	@Unique
	private static final int WHIMSY_SLOT_WAKE_UP = 3;

	/** 两次按键之间的有效窗口（毫秒）。 */
	@Unique
	private static final long WHIMSY_CONFIRM_WINDOW_MS = 2000L;

	/** 已经被「点亮」的槽位（-1 = 无）。 */
	@Unique
	private static int whimsy$armedSlot = -1;

	/** 点亮时间戳。 */
	@Unique
	private static long whimsy$armedAt;

	@Inject(method = "handleKeybinds", at = @At("HEAD"))
	private void whimsy$observeShortcuts(CallbackInfo ci) {
		Minecraft self = (Minecraft) (Object) this;

		if (!BHR_FoolSleepCamera.isActive()) {
			whimsy$armedSlot = -1;
			return;
		}

		if (self.options.keyHotbarSlots[WHIMSY_SLOT_RETURN_BODY].consumeClick()) {
			whimsy$arm(self, WHIMSY_SLOT_RETURN_BODY, "gui.bhr.fool.confirm_return",
					() -> BHR_FoolSleepCamera.exit());
			return;
		}

		if (self.options.keyHotbarSlots[WHIMSY_SLOT_WAKE_UP].consumeClick()) {
			whimsy$arm(self, WHIMSY_SLOT_WAKE_UP, "gui.bhr.fool.confirm_wake",
					() -> BHR_FoolSleepCamera.wakeUp());
		}
	}

	/** 第一次按：点亮并提示；窗口内第二次按：执行。 */
	@Unique
	private static void whimsy$arm(Minecraft minecraft, int slot, String hintKey, Runnable action) {
		long now = Util.getMillis();

		if (whimsy$armedSlot == slot && now - whimsy$armedAt <= WHIMSY_CONFIRM_WINDOW_MS) {
			whimsy$armedSlot = -1;
			action.run();
			return;
		}

		whimsy$armedSlot = slot;
		whimsy$armedAt = now;

		Component keyName = minecraft.options.keyHotbarSlots[slot].getTranslatedKeyMessage();
		minecraft.gui.hud.setOverlayMessage(Component.translatable(hintKey, keyName), false);
	}
}
