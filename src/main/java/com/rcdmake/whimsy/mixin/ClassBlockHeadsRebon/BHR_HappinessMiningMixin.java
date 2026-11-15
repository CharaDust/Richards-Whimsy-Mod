package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_Happiness;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 快乐值影响<b>挖掘速度</b>：快乐值每少 1 点，破坏方块速度 −4%（满值 20 时不惩罚，
 * 0 点时只剩正常的 20%）。
 *
 * <h2>为什么乘在 {@code Player#getDestroySpeed} 的返回值上</h2>
 * 原版挖方块的速度就是 {@code getDestroySpeed(BlockState)} 的返回值
 * （工具速度 + {@code MINING_EFFICIENCY} +  haste 等都在里面算好了），
 * 客户端预测与服务器校验都会调用它，所以：
 * <ul>
 *   <li>只改这一处 → 客户端/服务端口径自动一致；</li>
 *   <li>用乘法而不是 {@code MINING_EFFICIENCY} 修饰符 → 才是「减少 4%」而不是「减 0.04 点」。</li>
 * </ul>
 * 快乐值本身是同步附件，客户端直接读得到；服务端也是同一份。
 */
@Mixin(Player.class)
public abstract class BHR_HappinessMiningMixin {

	@Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
	private void whimsy$happinessMiningPenalty(BlockState state, CallbackInfoReturnable<Float> cir) {
		float speed = cir.getReturnValueF();
		if (speed <= 0.0F) {
			return;
		}

		float happiness = ((Player) (Object) this)
				.getAttachedOrElse(BHR_Happiness.VALUE, BHR_Happiness.MAX);
		float missing = Mth.clamp(BHR_Happiness.MAX - happiness, 0.0F, BHR_Happiness.MAX);
		if (missing <= 0.0F) {
			return;
		}

		float multiplier = Mth.clamp(1.0F - BHR_Happiness.MINING_PENALTY_PER_POINT * missing, 0.0F, 1.0F);
		cir.setReturnValue(speed * multiplier);
	}
}
