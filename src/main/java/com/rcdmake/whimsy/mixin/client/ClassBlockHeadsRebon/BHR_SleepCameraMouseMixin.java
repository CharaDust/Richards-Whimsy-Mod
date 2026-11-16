package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_FoolSleepCamera;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Marker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 出窍时把鼠标转向交给相机，<b>不转身体</b>。
 *
 * <p>{@code MouseHandler#turnPlayer} 里最后一句是 {@code minecraft.player.turn(dx, dy)}：
 * 直接让它转身体会带来两个问题 —— 睡着的身体会在床里转圈，而且客户端会照常给服务端发转向包
 * （服务端对睡眠中的转向是不拦的）。所以出窍期间把这句改成转相机实体。
 */
@Mixin(MouseHandler.class)
public abstract class BHR_SleepCameraMouseMixin {

	@Redirect(
			method = "turnPlayer",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
	private void whimsy$turnCameraInsteadOfBody(LocalPlayer player, double yRot, double xRot) {
		Marker camera = BHR_FoolSleepCamera.cameraEntity();
		if (camera != null) {
			camera.turn(yRot, xRot);
			return;
		}
		player.turn(yRot, xRot);
	}
}
