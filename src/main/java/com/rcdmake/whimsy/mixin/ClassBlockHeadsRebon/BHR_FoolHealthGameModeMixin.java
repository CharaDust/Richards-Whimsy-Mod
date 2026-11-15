package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_FoolHealth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 愚者模式：游戏模式切换后的对账。
 *
 * <p>{@code changeGameModeForPlayer} 是原版切模式的唯一收口
 * （{@code /gamemode}、F3+F4、服务端强制模式、单人开局都走它），
 * 返回 true 表示"确实换了"。这里按新模式的去向处理两层生命：
 * <ul>
 *   <li>进愚者模式：沿用存下的濒死值（没有记录则初始化为满值），不补血；</li>
 *   <li>离开愚者模式：保证至少半颗心，濒死值留档。</li>
 * </ul>
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class BHR_FoolHealthGameModeMixin {

	@Shadow
	@Final
	protected ServerPlayer player;

	@Inject(method = "changeGameModeForPlayer", at = @At("RETURN"))
	private void whimsy$foolModeChanged(GameType newMode, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ()) {
			BHR_FoolHealth.onGameModeChanged(this.player, newMode);
		}
	}
}
