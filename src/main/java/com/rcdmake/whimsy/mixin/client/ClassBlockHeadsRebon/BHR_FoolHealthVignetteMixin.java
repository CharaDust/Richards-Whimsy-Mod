package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.client.BHR_ClientHudState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 愚者模式：濒死时的屏幕红边。
 *
 * <p>直接复用原版「世界边界警告」的那一层：同一张贴图
 * （{@code textures/misc/vignette.png}）、同一条 {@code RenderPipelines.VIGNETTE} 管线、
 * 同一套染色公式（{@code Hud#extractVignette} 里 {@code borderWarningStrength > 0} 的分支），
 * 只是把强度由「离边界多远」换成「是不是濒死」。
 *
 * <h2>为什么挂在 {@code extractCameraOverlays} 而不是 {@code extractVignette}</h2>
 * 原版调用 {@code extractVignette} 的地方被 {@code options.vignette()}（屏幕暗角开关）包着，
 * 关掉暗角的玩家永远拿不到那一次调用。濒死红边是<b>玩法警告</b>而不是"暗处变黑"，
 * 不该被这个开关吞掉，所以挂在每帧都会跑的外层 {@code extractCameraOverlays} 末尾自己画。
 * 原版暗角若开着，它先画、我们叠在上面，两者不冲突（不取消原版那一层）。
 *
 * <p>濒死判定：当前是愚者模式 + 第一层血已经见底（≈0）+ 濒死值还有剩。
 * 客户端这份濒死值是服务端附件同步过来的，不需要自己发网络包。
 */
@Mixin(Hud.class)
public abstract class BHR_FoolHealthVignetteMixin {

	/** 与原版 {@code Hud.VIGNETTE_LOCATION} 同一个资源。 */
	private static final Identifier WHIMSY_VIGNETTE =
			Identifier.withDefaultNamespace("textures/misc/vignette.png");

	/** 濒死红边的强度（0 = 无，1 = 原版边界警告满强度）。 */
	private static final float WHIMSY_DOWNED_STRENGTH = 0.85F;

	/** 原版的亮度字段：越暗越亮，红边按它的值缩放（与边界警告一致）。 */
	@Shadow
	public float vignetteBrightness;

	@Inject(method = "extractCameraOverlays", at = @At("TAIL"))
	private void whimsy$downedVignette(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null || !whimsy$isDowned(minecraft, player)) {
			return;
		}

		// 与 Hud#extractVignette 里 borderWarningStrength > 0 分支同一套染色公式。
		float brightness = Mth.clamp(this.vignetteBrightness, 0.0F, 1.0F);
		float strength = Mth.clamp(WHIMSY_DOWNED_STRENGTH, 0.0F, 1.0F);
		float red = brightness * (1.0F - strength);
		float greenBlue = brightness + (1.0F - brightness) * strength;
		int color = ARGB.colorFromFloat(1.0F, red, greenBlue, greenBlue);

		int width = graphics.guiWidth();
		int height = graphics.guiHeight();
		graphics.blit(RenderPipelines.VIGNETTE, WHIMSY_VIGNETTE,
				0, 0, 0.0F, 0.0F,
				width, height, width, height, color);
	}

	/** 愚者模式 + 第一层见底 + 濒死值未耗尽。 */
	private static boolean whimsy$isDowned(Minecraft minecraft, LocalPlayer player) {
		return com.rcdmake.whimsy.client.BHR_ClientHudState.isDowned(minecraft, player);
	}
}
