package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Unit;
import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_Sleep;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 愚者模式的入睡入口：跳过原版的「夜晚 / 维度 / 距离 / 怪物 / 设置重生点」一整套判定。
 *
 * <h2>为什么要整个接管</h2>
 * <p>26.2 的 {@code ServerPlayer#startSleepInBed} 会先读 {@code EnvironmentAttributes.BED_RULE}：
 * <ul>
 *   <li>{@code canSleep}：overworld 是 {@code when_dark}（只能夜里睡），下界/末地是 {@code never}；</li>
 *   <li>{@code canSetSpawn}：overworld 是 {@code always}，会<b>把重生点钉在床边</b>。</li>
 * </ul>
 * 愚者模式这两条都不要：任何时刻都能睡，而且<b>床不设重生点</b>（愚者用别的方块设重生点）。
 * 与其去改那个环境属性（会影响所有玩家），不如在入口处直接走自己的路径。
 *
 * <h2>仍然保留的边界</h2>
 * <ul>
 *   <li>主条 &gt; {@link net.minecraft.world.level.GameType} 门槛（16000）→ 拒绝，并给出提示文案；</li>
 *   <li>已经在睡 / 已死 → 按原版返回 {@code OTHER_PROBLEM}；</li>
 *   <li>不设重生点、不检查附近怪物、不检查距离（玩家点的是床本身，天然贴脸）。</li>
 * </ul>
 */
@Mixin(ServerPlayer.class)
public abstract class BHR_ServerPlayerSleepMixin {

	@Inject(method = "startSleepInBed", at = @At("HEAD"), cancellable = true)
	private void whimsy$foolSleepEntry(BlockPos pos,
			CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
		ServerPlayer self = (ServerPlayer) (Object) this;
		if (!BHR_Sleep.isFool(self)) {
			return;
		}

		if (BHR_Sleep.isTooRested(self)) {
			cir.setReturnValue(Either.left(new Player.BedSleepingProblem(BHR_Sleep.tooRestedMessage())));
			return;
		}

		if (self.isSleeping() || !self.isAlive()) {
			cir.setReturnValue(Either.left(Player.BedSleepingProblem.OTHER_PROBLEM));
			return;
		}

		// 直接躺下：不设重生点、不看维度规则、不做怪物检查
		self.startSleeping(pos);
		((BHR_SleepCounterAccess) (Object) self).whimsy$setSleepCounter(0);
		self.level().updateSleepingPlayerList();

		cir.setReturnValue(Either.right(Unit.INSTANCE));
	}
}
