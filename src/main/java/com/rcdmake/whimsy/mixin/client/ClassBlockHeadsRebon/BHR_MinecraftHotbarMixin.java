package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_FoolSleepCamera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 出窍期间把 1~9 快捷栏键交给原版旁观者菜单。
 *
 * <p>和旁观者模式一样：这些键被菜单征用，<b>不会切换手上的物品</b>。
 * 挂在 {@code Minecraft#handleKeybinds} 的 HEAD，先于原版逐个 {@code consumeClick()}
 * 把它们吃掉。
 *
 * <h2>为什么第一次按要调两次 {@code onHotbarSelected}</h2>
 * <p>原版这段逻辑是「按一次开菜单 → 再按选中 → 再按才执行」。为了让手感是
 * 「按一次点亮、再按一次执行」，菜单还没打开时我们连调两次：第一次开菜单，第二次顺手点亮该格。
 */
@Mixin(Minecraft.class)
public abstract class BHR_MinecraftHotbarMixin {

	@Inject(method = "handleKeybinds", at = @At("HEAD"))
	private void whimsy$routeHotbarKeysToSpectatorMenu(CallbackInfo ci) {
		Minecraft self = (Minecraft) (Object) this;

		if (!BHR_FoolSleepCamera.isActive()) {
			return;
		}

		SpectatorGui gui = self.gui.hud.getSpectatorGui();

		for (int slot = 0; slot < self.options.keyHotbarSlots.length; slot++) {
			if (!self.options.keyHotbarSlots[slot].consumeClick()) {
				continue;
			}
			boolean wasActive = gui.isMenuActive();
			gui.onHotbarSelected(slot);
			if (!wasActive) {
				// 刚打开：顺手点亮这一格，让「再按一次」就能执行
				gui.onHotbarSelected(slot);
			}
		}
	}
}
