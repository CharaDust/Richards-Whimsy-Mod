package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_FoolSleepCamera;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让「观察世界」按钮抢在<b>聊天链接</b>之前处理点击。
 *
 * <h2>为什么会被链接抢走</h2>
 * <p>床上的界面是 {@link ChatScreen} 的子类，而 {@code ChatScreen#mouseClicked} 一进来就先做
 * 「光标下面这条聊天消息有没有点击事件」，命中就直接打开链接并 {@code return true} ——
 * 根本走不到 widget 分发。于是点在按钮上时，实际打开的是那行聊天里的链接
 * （Wurst 的更新提示正好是最后一条消息，位置就在按钮附近）。
 *
 * <p>所以在 {@code mouseClicked} 的 HEAD 把「点在我们的按钮上」这一种情况先吃掉。
 */
@Mixin(ChatScreen.class)
public abstract class BHR_ChatScreenClickMixin {

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void whimsy$observeButtonBeforeChatLinks(MouseButtonEvent event, boolean doubleClick,
			CallbackInfoReturnable<Boolean> cir) {
		Button observe = BHR_FoolSleepCamera.observeButton();
		if (observe == null || !observe.visible || !observe.active) {
			return;
		}
		// 26.2 的点击入口是 mouseClicked(MouseButtonEvent, boolean)，这里不去造事件：
		// 命中就直接跑按钮动作（与左键点它等价）。
		if (observe.isMouseOver(event.x(), event.y())) {
			BHR_FoolSleepCamera.runObserveAction();
			cir.setReturnValue(true);
		}
	}
}
