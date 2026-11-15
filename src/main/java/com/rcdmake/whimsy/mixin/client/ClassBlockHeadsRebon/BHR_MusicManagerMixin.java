package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.access.BHR_MusicDelayAccess;
import com.rcdmake.whimsy.client.BHR_ClientHudState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.MusicManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 愚者模式：濒死期间挂起原版背景音乐，并把「下一首的等待时间」开放给
 * {@link com.rcdmake.whimsy.client.BHR_DownedMusic} 改写。
 *
 * <ul>
 *   <li>{@code tick()} 在濒死时直接取消 —— 原版既不会开始新曲子，也不会去动
 *       {@code currentMusic}（我们已经在进入濒死时把它停掉了）；</li>
 *   <li>{@link BHR_MusicDelayAccess} 用来在退出濒死时把 {@code nextSongDelay}
 *       压到几秒，否则原版会按「几分钟」的间隔让场景寂静很久。</li>
 * </ul>
 */
@Mixin(MusicManager.class)
public abstract class BHR_MusicManagerMixin implements BHR_MusicDelayAccess {

	@Shadow
	private int nextSongDelay;

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void whimsy$suspendWhileDowned(CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();
		if (BHR_ClientHudState.isDowned(minecraft, minecraft.player)) {
			ci.cancel();
		}
	}

	@Override
	public void whimsy$shortenNextSongDelay(int ticks) {
		this.nextSongDelay = Math.min(this.nextSongDelay, ticks);
	}
}
