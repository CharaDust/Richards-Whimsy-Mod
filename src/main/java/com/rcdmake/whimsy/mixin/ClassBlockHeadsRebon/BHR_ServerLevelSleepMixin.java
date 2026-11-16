package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_Sleep;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.world.clock.WorldClock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * 接管原版跳夜里的「叫醒所有人」。
 *
 * <p>原版 {@code ServerLevel#tick} 的跳夜分支做两件事：
 * <pre>
 *   clockManager.moveToTimeMarker(clock, WAKE_UP_FROM_SLEEP);   // 拨时钟
 *   this.wakeUpAllPlayers();                                     // 叫醒所有人
 * </pre>
 * 前半句要保留（跳夜就是拨时钟），后半句要改：<b>只叫醒非愚者</b>，
 * 并给睡着的愚者按「世界时钟前进了多少」平账。
 *
 * <p>为了拿到「前进量」，在 {@code tick} 开头记下时钟值，在 {@code wakeUpAllPlayers}
 * 里再读一次 —— 两者之间正好夹着 {@code moveToTimeMarker}。
 */
@Mixin(ServerLevel.class)
public abstract class BHR_ServerLevelSleepMixin {

	@Shadow
	@Final
	private SleepStatus sleepStatus;

	/** {@code tick} 开头记下的时钟值。 */
	@Unique
	private long whimsy$clockBefore;

	/** 这一 tick 有没有成功记到时钟值。 */
	@Unique
	private boolean whimsy$clockCaptured;

	@Inject(method = "tick", at = @At("HEAD"))
	private void whimsy$captureClockBeforeSleep(BooleanSupplier haveTime, CallbackInfo ci) {
		ServerLevel self = (ServerLevel) (Object) this;
		this.whimsy$clockCaptured = false;

		Optional<Holder<WorldClock>> clock = self.dimensionType().defaultClock();
		if (clock.isPresent()) {
			this.whimsy$clockBefore = self.getServer().clockManager().getTotalTicks(clock.get());
			this.whimsy$clockCaptured = true;
		}
	}

	@Inject(method = "wakeUpAllPlayers", at = @At("HEAD"), cancellable = true)
	private void whimsy$wakeUpNonFoolsOnly(CallbackInfo ci) {
		ServerLevel self = (ServerLevel) (Object) this;

		long delta = 0L;
		if (this.whimsy$clockCaptured) {
			Optional<Holder<WorldClock>> clock = self.dimensionType().defaultClock();
			if (clock.isPresent()) {
				delta = self.getServer().clockManager().getTotalTicks(clock.get()) - this.whimsy$clockBefore;
			}
		}

		BHR_Sleep.vanillaSkip(self, this.sleepStatus, delta);
		ci.cancel();
	}
}
