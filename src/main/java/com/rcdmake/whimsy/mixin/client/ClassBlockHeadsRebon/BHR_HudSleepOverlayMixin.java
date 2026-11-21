package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_FoolSleepCamera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 出窍期间的 HUD 处理。
 *
 * <h2>黑幕</h2>
 * <p>26.2 的睡觉黑幕不在 {@code ScreenEffectRenderer}，而在 HUD 层的
 * {@code Hud#extractSleepOverlay}：按 {@code player.getSleepTimer()} 画一层全屏黑，
 * 而且是在热栏 / 生命值这些元素<b>之后</b>画 —— 所以它盖住的不只是世界，是整个 HUD。
 * 出窍时把它整段跳过。
 *
 * <h2>强制显示</h2>
 * <p>HUD 的开关是 {@code isHidden}（F1 切换）。出窍是"观察世界"模式，
 * 状态栏（生命 / 快乐 / 精力 / 饥饿 / 环境）必须可见，所以出窍期间临时把它压成 false，
 * 提取完再还原玩家的原设置。
 */
@Mixin(Hud.class)
public abstract class BHR_HudSleepOverlayMixin {

	/** HUD 的「隐藏」开关（F1 切换的那个）。 */
	@Shadow
	private boolean isHidden;

	/** 出窍期间临时接管 isHidden 之前的值。 */
	@Unique
	private boolean whimsy$savedHidden;

	/** 诊断日志节流（毫秒）。 */
	@Unique
	private static long whimsy$lastHudLog;

	@Inject(method = "extractSleepOverlay", at = @At("HEAD"), cancellable = true)
	private void whimsy$skipSleepOverlayWhileObserving(CallbackInfo ci) {
		if (BHR_FoolSleepCamera.isActive()) {
			ci.cancel();
		}
	}

	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void whimsy$prepareHud(DeltaTracker deltaTracker, CallbackInfo ci) {
		this.whimsy$savedHidden = this.isHidden;

		if (!BHR_FoolSleepCamera.isActive()) {
			return;
		}

		whimsy$logOnce();
		if (this.isHidden) {
			this.isHidden = false;
		}
	}

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void whimsy$restoreHud(DeltaTracker deltaTracker, CallbackInfo ci) {
		if (BHR_FoolSleepCamera.isActive()) {
			this.isHidden = this.whimsy$savedHidden;
		}
	}

	/** 出窍期间每 3 秒打一行 HUD 状态，便于实机核对。 */
	@Unique
	private void whimsy$logOnce() {
		long now = Util.getMillis();
		if (now - whimsy$lastHudLog < 3000L) {
			return;
		}
		whimsy$lastHudLog = now;

		Minecraft minecraft = Minecraft.getInstance();
		LoggerFactory.getLogger("richards-whimsy-mod/camera").info(
				"[出窍] HUD 状态：isHidden={}, screen={}, sleepTimer={}, hudHidden={}",
				this.isHidden,
				minecraft.gui.screen() == null ? "null" : minecraft.gui.screen().getClass().getSimpleName(),
				minecraft.player == null ? -1 : minecraft.player.getSleepTimer(),
				minecraft.gameRenderer.gameRenderState().guiRenderState.isHudHidden);
	}
}
