package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_Sleep;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 席地而睡（强制睡眠）时，别让原版每 tick 把人「踹下床」。
 *
 * <p>26.2 的 {@code LivingEntity#tick} 里有这么一句：
 * <pre>
 *   if (this.isSleeping() &amp;&amp; (!this.canInteractWithLevel() || !this.checkBedExists())) {
 *      this.stopSleeping();
 *   }
 * </pre>
 * 而 {@code checkBedExists()} 的实现是「睡的那个格子必须是 {@link BedBlock}」。
 * 席地而睡没有床 → 这一句每 tick 都会把人叫醒，我们的强制睡眠又每 tick 把人按回去，
 * 于是形成 <b>20 Hz 的睡/醒抖动</b>：屏幕在正常 HUD 与睡眠黑幕之间高频闪烁、
 * 位置每 tick 被 {@code setPosToBed} 拉回、并且所有交互都失效。
 *
 * <p>所以只对「正在席地而睡 / 强制睡眠的愚者」把这一判定短路成 true；
 * 睡在真床上的玩家仍然走原版逻辑（床被拆了照样会醒）。
 */
@Mixin(LivingEntity.class)
public abstract class BHR_FloorSleepMixin {

	@Redirect(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;checkBedExists()Z"))
	private boolean whimsy$keepFloorSleeperAsleep(LivingEntity self) {
		if (self instanceof ServerPlayer player && BHR_Sleep.isFloorSleeping(player)) {
			return true;
		}
		return self.getSleepingPos()
				.map(pos -> {
					BlockState state = self.level().getBlockState(pos);
					return state.getBlock() instanceof BedBlock;
				})
				.orElse(false);
	}
}
