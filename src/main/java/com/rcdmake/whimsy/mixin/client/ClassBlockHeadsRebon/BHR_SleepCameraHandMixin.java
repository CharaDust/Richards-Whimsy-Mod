package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_FoolSleepCamera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 出窍时把手从画面里摘掉。
 *
 * <p>原版的手部渲染条件是「第一人称 + 相机实体没在睡」；出窍后相机实体是不睡的 {@code Marker}，
 * 于是手会飘在半空。这里直接取消。
 */
@Mixin(GameRenderer.class)
public abstract class BHR_SleepCameraHandMixin {

	@Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
	private void whimsy$hideHandWhileObserving(CallbackInfo ci) {
		if (BHR_FoolSleepCamera.isActive()) {
			ci.cancel();
		}
	}
}
