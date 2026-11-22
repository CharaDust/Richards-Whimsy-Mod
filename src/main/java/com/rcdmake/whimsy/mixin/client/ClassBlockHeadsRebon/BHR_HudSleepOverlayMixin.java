package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_FoolSleepCamera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 出窍（观察世界）期间的 HUD 处理。
 *
 * <h2>1. 去掉睡觉黑幕</h2>
 * <p>26.2 的睡觉黑幕不在 {@code ScreenEffectRenderer}，而在 HUD 层的
 * {@code Hud#extractSleepOverlay}：按 {@code player.getSleepTimer()} 画一层全屏黑，
 * 而且是在热栏 / 生命值这些元素<b>之后</b>画 —— 盖住的不只是世界，是整个 HUD。
 * 出窍时整段跳过。
 *
 * <h2>2. 状态栏必须画出来</h2>
 * <p>{@code Hud#extractPlayerHealth} 一进门就是
 * {@code Player player = this.getCameraPlayer(); if (player != null) {...}}，
 * 而 {@code getCameraPlayer()} 只认「相机实体本身是 Player」。出窍时相机是
 * {@code Marker} → 返回 null → 护甲 / 生命 / 饥饿（以及本模组注入在
 * {@code extractHearts} / {@code extractFood} 里的五条资源条）全部被跳过，只剩经验条。
 * 所以出窍期间让它仍然返回真实玩家。
 *
 * <h2>3. 顺带强制可见</h2>
 * <p>HUD 的总开关是 {@code isHidden}（F1）。出窍是「观察世界」模式，状态栏必须在，
 * 于是提取期间临时压成 false，提取完还原玩家原设置。
 */
@Mixin(Hud.class)
public abstract class BHR_HudSleepOverlayMixin {

	/** HUD 的「隐藏」开关（F1 切换的那个）。 */
	@Shadow
	private boolean isHidden;

	/** 出窍期间临时接管 isHidden 之前的值。 */
	@Unique
	private boolean whimsy$savedHidden;

	/** 原版旁观者菜单：出窍时用它来画命令栏（复用原版 UI，不自己画）。 */
	@Shadow
	@Final
	private net.minecraft.client.gui.components.spectator.SpectatorGui spectatorGui;

	@Inject(method = "extractSleepOverlay", at = @At("HEAD"), cancellable = true)
	private void whimsy$skipSleepOverlayWhileObserving(CallbackInfo ci) {
		if (BHR_FoolSleepCamera.isActive()) {
			ci.cancel();
		}
	}

	/**
	 * 出窍时隐藏普通快捷栏。
	 *
	 * <p>旁观者模式的快捷栏本来就是被命令栏取代的；出窍时手里那排物品既不能用手也不能切，
	 * 留在屏幕上只会让 3/4 号键的提示对不上，所以直接不画。
	 */
	@Inject(method = "extractItemHotbar", at = @At("HEAD"), cancellable = true)
	private void whimsy$hideHotbarWhileObserving(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker,
			CallbackInfo ci) {
		if (BHR_FoolSleepCamera.isActive()) {
			ci.cancel();
		}
	}

	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void whimsy$prepareHud(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		this.whimsy$savedHidden = this.isHidden;
		if (BHR_FoolSleepCamera.isActive() && this.isHidden) {
			this.isHidden = false;
		}
	}

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void whimsy$restoreHud(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (BHR_FoolSleepCamera.isActive()) {
			this.isHidden = this.whimsy$savedHidden;
		}
	}

	/** 出窍时相机是 Marker，但 HUD 仍然应该按真实玩家来画。 */
	@Inject(method = "getCameraPlayer", at = @At("HEAD"), cancellable = true)
	private void whimsy$cameraPlayerWhileObserving(CallbackInfoReturnable<Player> cir) {
		if (BHR_FoolSleepCamera.isActive()) {
			cir.setReturnValue(Minecraft.getInstance().player);
		}
	}

	/** 出窍时用<b>原版旁观者菜单</b>画命令栏（快捷栏已在上面被跳过）。 */
	@Inject(method = "extractHotbarAndDecorations", at = @At("TAIL"))
	private void whimsy$drawSpectatorMenu(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (BHR_FoolSleepCamera.isActive()) {
			this.spectatorGui.extractHotbar(graphics);
			this.spectatorGui.extractAction(graphics);
		}
	}
}


