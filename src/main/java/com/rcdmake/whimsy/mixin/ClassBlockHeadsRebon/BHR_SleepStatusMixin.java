package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_Sleep;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * 把<b>愚者模式玩家从原版睡眠统计里剔掉</b>。
 *
 * <p>26.2 的 {@link SleepStatus} 只有 {@code activePlayers} / {@code sleepingPlayers} 两个计数，
 * 且 {@code update} 只滤旁观者 —— 而本模组让 {@code GameType.BHR_FOOL_MODE.isSurvival()} 返回 true，
 * 所以愚者会被原版当成普通玩家数进去。不剔掉会有两个后果：
 *
 * <ol>
 *   <li>愚者单独睡够比例时，原版跳夜会被他们触发（应该只看非愚者）；</li>
 *   <li>跳夜后愚者继续睡时，原版分子依旧满足 → <b>每 tick 重新跳夜并叫醒愚者</b>，
 *       加速逻辑直接被腰斩。</li>
 * </ol>
 *
 * <p>剔除之后，原版那两个判定（{@code areEnoughSleeping} / {@code areEnoughDeepSleeping}）
 * 的分母与分子都只剩非愚者 —— 正好就是「分组分母」的口径：
 * {@code need = ceil(非愚者人数 × pct / 100)}。
 */
@Mixin(SleepStatus.class)
public abstract class BHR_SleepStatusMixin {

	@Shadow
	private int activePlayers;

	@Shadow
	private int sleepingPlayers;

	/** 只数非愚者。返回值语义与原版一致（人数是否变化）。 */
	@Inject(method = "update", at = @At("HEAD"), cancellable = true)
	private void whimsy$updateNonFoolsOnly(List<ServerPlayer> players, CallbackInfoReturnable<Boolean> cir) {
		int oldActive = this.activePlayers;
		int oldSleeping = this.sleepingPlayers;
		this.activePlayers = 0;
		this.sleepingPlayers = 0;

		for (ServerPlayer player : players) {
			if (player.isSpectator() || BHR_Sleep.isFool(player)) {
				continue;
			}
			this.activePlayers++;
			if (player.isSleeping()) {
				this.sleepingPlayers++;
			}
		}

		cir.setReturnValue((oldSleeping > 0 || this.sleepingPlayers > 0)
				&& (oldActive != this.activePlayers || oldSleeping != this.sleepingPlayers));
	}

	/** 深睡判定同理：只数非愚者。 */
	@Inject(method = "areEnoughDeepSleeping", at = @At("HEAD"), cancellable = true)
	private void whimsy$deepSleepNonFoolsOnly(int percentage, List<ServerPlayer> players,
			CallbackInfoReturnable<Boolean> cir) {
		int deep = 0;
		for (ServerPlayer player : players) {
			if (!BHR_Sleep.isFool(player) && player.isSleepingLongEnough()) {
				deep++;
			}
		}
		cir.setReturnValue(deep >= ((SleepStatus) (Object) this).sleepersNeeded(percentage));
	}
}
