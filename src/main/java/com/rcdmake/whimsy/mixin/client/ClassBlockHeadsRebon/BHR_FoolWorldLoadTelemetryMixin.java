package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import net.minecraft.client.telemetry.events.WorldLoadEvent;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 愚者模式：跳过「世界加载」遥测里的游戏模式映射。
 *
 * <p>{@code WorldLoadEvent.setGameMode(GameType, boolean)} 里有一个穷举四种模式的
 * switch 表达式（映射到 {@code TelemetryProperty.GameMode} 这个原版枚举）。
 * 第五种模式进来时没有分支 → {@code MatchException}，会在进存档的瞬间炸客户端。
 *
 * <p>处理方式：直接取消，让 {@code gameMode} 字段保持 null。原版
 * {@code send(...)} 的条件本来就是 {@code gameMode != null}，于是这一条遥测对整个
 * 世界加载事件整体跳过 —— 不伪造一个不存在的遥测值，也不影响其它遥测项。
 */
@Mixin(WorldLoadEvent.class)
public abstract class BHR_FoolWorldLoadTelemetryMixin {

	@Inject(method = "setGameMode", at = @At("HEAD"), cancellable = true)
	private void whimsy$skipFoolModeTelemetry(GameType type, boolean hardcore, CallbackInfo ci) {
		if (type == GameType.BHR_FOOL_MODE) {
			ci.cancel();
		}
	}
}
