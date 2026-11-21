package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_FoolSleepCamera;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 出窍时别再自动打开床上界面。
 *
 * <p>26.2 的 {@code Gui#tick} 里有这么一段：
 * <pre>
 *   } else if (player.isSleeping() &amp;&amp; this.minecraft.level != null) {
 *      this.hud.getChat().openScreen(ChatMethod.MESSAGE, InBedChatScreen::new);
 *   }
 * </pre>
 * 也就是说：<b>只要你还在睡、又没有界面，客户端就会立刻把床上聊天界面重新打开</b>。
 * 所以「关掉界面去看世界」在纯客户端是做不到的 —— 上一版就是这么被反复顶回来的：
 * 我们 {@code setScreen(null)}，下一 tick 它又开，画面在「世界一帧 / 界面一帧」之间闪。
 *
 * <p>这里把 {@code Gui#tick} 里那两处 {@code isSleeping()} 判定在出窍期间改成 false：
 * 既不自动开床界面，也让床界面上的「醒来回调」走不到。出窍结束后立刻恢复原语义。
 */
@Mixin(Gui.class)
public abstract class BHR_GuiBedScreenMixin {

	@Redirect(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;isSleeping()Z"))
	private boolean whimsy$hideSleepScreenWhileObserving(LocalPlayer player) {
		if (BHR_FoolSleepCamera.isActive()) {
			return false;
		}
		return player.isSleeping();
	}
}
