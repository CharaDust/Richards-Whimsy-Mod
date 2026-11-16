package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.client.BHR_ClientHudState;
import com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_FoolSleepCamera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 床上的界面（睡觉时按 T 打开的那个，原版自带「离开床」按钮）里加一枚
 * <b>观察世界 / 返回身体</b> 按钮。
 *
 * <h2>为什么放这里而不是背包</h2>
 * <p>睡觉时按 E 开背包会和「睡觉后能打字」这条原版行为互相打架；床上的界面本来就在，
 * 而且它自带「离开床」，所以出窍开关放它上面最自然。
 *
 * <h2>关界面 ≠ 起床</h2>
 * <p>原版 {@code InBedChatScreen#onClose} 会发 {@code STOP_SLEEPING}，
 * 所以关掉这个界面等于起床。我们切相机时必须关界面（否则输入被 GUI 吃掉），
 * 于是先在 {@link BHR_FoolSleepCamera} 立一次性旗标，让 {@code onClose} 这一次跳过唤醒。
 */
@Mixin(InBedChatScreen.class)
public abstract class BHR_InBedSleepButtonsMixin {

	/** 原版「离开床」按钮的 y = height - 40，我们叠在它上面一格。 */
	private static final int VANILLA_BUTTON_Y_FROM_BOTTOM = 40;
	private static final int BUTTON_WIDTH = 200;
	private static final int BUTTON_HEIGHT = 20;
	private static final int GAP = 4;

	@Inject(method = "init", at = @At("TAIL"))
	private void whimsy$addObserveButton(CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || !minecraft.player.isSleeping()) {
			return;
		}
		if (!BHR_ClientHudState.isFoolMode(minecraft)) {
			return;
		}

		Screen self = (Screen) (Object) this;
		int x = self.width / 2 - BUTTON_WIDTH / 2;
		int y = self.height - VANILLA_BUTTON_Y_FROM_BOTTOM - BUTTON_HEIGHT - GAP;

		Component label = Component.translatable(
				BHR_FoolSleepCamera.isActive()
						? "gui.bhr.fool.return_body"
						: "gui.bhr.fool.observe");

		Button button = Button.builder(label, pressed -> {
			BHR_FoolSleepCamera.requestExitChatWithoutWake();
			BHR_FoolSleepCamera.toggle();
			minecraft.gui.setScreen(null);
		}).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build();

		((BHR_ScreenAccess) (Object) this).whimsy$addRenderableWidget(button);
	}

	/** 关界面时，如果这一次是「切相机」，就别顺手把人叫醒。 */
	@Inject(method = "onClose", at = @At("HEAD"), cancellable = true)
	private void whimsy$skipWakeWhenSwitchingCamera(CallbackInfo ci) {
		if (BHR_FoolSleepCamera.consumeSuppressWake()) {
			ci.cancel();
		}
	}
}
