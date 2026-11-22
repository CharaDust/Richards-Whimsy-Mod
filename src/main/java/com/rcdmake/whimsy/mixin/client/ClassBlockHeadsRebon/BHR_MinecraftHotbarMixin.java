package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_FoolSleepCamera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 出窍期间的快捷栏快捷键（仿旁观者模式那套 1~9 快捷操作）。
 *
 * <ul>
 *   <li><b>3</b> 号键 → 返回身体</li>
 *   <li><b>4</b> 号键 → 起床</li>
 * </ul>
 *
 * <p>为什么挂在 {@code Minecraft#handleKeybinds} 的 HEAD：快捷栏键是原版在这里逐个
 * {@code consumeClick()} 消费的，我们在它之前先消费，按键既不会顺带切换选中槽位，
 * 也不会被界面之外的逻辑抢走。只在出窍（无界面、相机脱离）时生效。
 */
@Mixin(Minecraft.class)
public abstract class BHR_MinecraftHotbarMixin {

	/** 快捷栏第 3 格（数组下标 2）：返回身体。 */
	private static final int SLOT_RETURN_BODY = 2;

	/** 快捷栏第 4 格（数组下标 3）：起床。 */
	private static final int SLOT_WAKE_UP = 3;

	@Inject(method = "handleKeybinds", at = @At("HEAD"))
	private void whimsy$observeShortcuts(CallbackInfo ci) {
		if (!BHR_FoolSleepCamera.isActive()) {
			return;
		}

		Minecraft self = (Minecraft) (Object) this;
		if (self.options.keyHotbarSlots[SLOT_RETURN_BODY].consumeClick()) {
			BHR_FoolSleepCamera.exit();
			return;
		}
		if (self.options.keyHotbarSlots[SLOT_WAKE_UP].consumeClick()) {
			BHR_FoolSleepCamera.wakeUp();
		}
	}
}
