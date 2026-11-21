package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.client.BHR_ClientHudState;
import com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_FoolSleepCamera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 物品栏底部的两枚按钮：<b>起床</b> 与 <b>返回身体</b>。
 *
 * <p>分工（与需求对齐）：
 * <ul>
 *   <li><b>观察世界</b> 放在<b>床上的界面</b>（睡觉时按 T 打开的那个）；</li>
 *   <li><b>返回身体 / 起床</b> 放在<b>物品栏下方</b> —— 出窍时按 E 就能回来，
 *       不必再去开那个会被原版自动重开的睡眠界面。</li>
 * </ul>
 */
@Mixin(AbstractContainerScreen.class)
public abstract class BHR_InventorySleepButtonsMixin {

	@Shadow
	protected int leftPos;

	@Shadow
	protected int topPos;

	@Shadow
	protected int imageWidth;

	@Shadow
	protected int imageHeight;

	@Inject(method = "init", at = @At("TAIL"))
	private void whimsy$addSleepButtons(CallbackInfo ci) {
		if (!((Object) this instanceof InventoryScreen)) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || !minecraft.player.isSleeping()) {
			return;
		}
		if (!BHR_ClientHudState.isFoolMode(minecraft)) {
			return;
		}

		boolean observing = BHR_FoolSleepCamera.isActive();

		int width = 76;
		int height = 14;
		int gap = 2;
		int screenHeight = ((Screen) (Object) this).height;

		int x = this.leftPos + (this.imageWidth - width) / 2;
		int y = this.topPos + this.imageHeight + gap;
		if (y + height * 2 + gap > screenHeight - gap) {
			y = screenHeight - gap - (height * 2 + gap);
		}

		Runnable wakeAction = () -> {
			BHR_FoolSleepCamera.wakeUp();
			minecraft.gui.setScreen(null);
		};
		Button wake = Button.builder(Component.translatable("gui.bhr.fool.wake_up"), b -> wakeAction.run())
				.bounds(x, y + height + gap, width, height)
				.build();
		((BHR_ScreenAccess) (Object) this).whimsy$addRenderableWidget(wake);

		if (observing) {
			Runnable returnAction = () -> {
				BHR_FoolSleepCamera.exit();
				// 和「起床」一样，点完就关掉物品栏
				minecraft.gui.setScreen(null);
			};
			Button back = Button.builder(Component.translatable("gui.bhr.fool.return_body"),
							b -> returnAction.run())
					.bounds(x, y, width, height)
					.build();
			((BHR_ScreenAccess) (Object) this).whimsy$addRenderableWidget(back);
		}
	}
}
