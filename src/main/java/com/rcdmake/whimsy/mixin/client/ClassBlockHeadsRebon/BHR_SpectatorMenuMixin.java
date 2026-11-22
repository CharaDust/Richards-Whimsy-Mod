package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_FoolSleepCamera;
import com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_FoolSpectatorCategory;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 出窍时把原版旁观者菜单的<b>内容</b>换成愚者的那几条。
 *
 * <p>{@code SpectatorMenu} 的构造器固定用 {@code RootSpectatorMenuCategory}（传送玩家 / 队伍），
 * 而 {@code selectCategory(...)} 是公开的 —— 所以在构造完成时换掉分类即可，
 * 菜单的渲染、选中、两段式确认、淡出全部沿用原版。
 */
@Mixin(SpectatorMenu.class)
public abstract class BHR_SpectatorMenuMixin {

	@Inject(method = "<init>", at = @At("RETURN"))
	private void whimsy$useFoolCategory(SpectatorMenuListener listener, CallbackInfo ci) {
		if (BHR_FoolSleepCamera.isActive()) {
			((SpectatorMenu) (Object) this).selectCategory(BHR_FoolSpectatorCategory.INSTANCE);
		}
	}
}
