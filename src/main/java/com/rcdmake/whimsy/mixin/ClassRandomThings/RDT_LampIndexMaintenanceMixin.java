package com.rcdmake.whimsy.mixin.ClassRandomThings;

import com.rcdmake.whimsy.block.ClassRandomThings.RDT_LampIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 灯位置索引的失效钩子 —— 灯的放下 / 拆掉都要作废所在区块的缓存。
 *
 * <h2>为什么不能只靠方块自己的回调</h2>
 * {@code BlockBehaviour.onPlace} 在 26.2 里只在<b>服务端</b>被调
 * （{@code LevelChunk.setBlockState} 里那句 {@code if (!this.level.isClientSide() && (flags & 512) == 0)}）；
 * 而客户端也要用同一份索引（青金石灯的自愈在客户端跑）。{@code LevelChunk.setBlockState} 是
 * 两侧共同的唯一「方块变了」漏斗，{@code /setblock}、{@code /fill}、活塞推、爆炸都会经过它。
 *
 * <h2>为什么用「新状态或旧状态之一是灯」来判断</h2>
 * 放下灯：新状态是灯；拆掉灯：旧状态（返回值）是灯 —— 两个方向都要作废缓存。
 * 方法返回 null 表示这次改动没有生效，此时按「新状态是灯」处理即可（多作废一次无害）。
 *
 * <p>代价：每次方块变化多两次 {@code BlockState.is(Block)} 的身份比较（纳秒级），
 * 只有真的是灯时才做一次哈希删除。{@code require = 0}：不命中只是缓存可能过期一瞬，
 * 而索引查询本身还会复查方块状态，所以最坏情况是「多查一次」，不会变成幽灵光。
 */
@Mixin(LevelChunk.class)
public abstract class RDT_LampIndexMaintenanceMixin {

	@Shadow
	public abstract Level getLevel();

	@Inject(method = "setBlockState", at = @At("RETURN"), require = 0)
	private void whimsy$invalidateLampIndex(BlockPos pos, BlockState state, int flags,
			CallbackInfoReturnable<BlockState> cir) {
		BlockState oldState = cir.getReturnValue();
		if (!RDT_LampIndex.isLamp(state) && (oldState == null || !RDT_LampIndex.isLamp(oldState))) {
			return;
		}
		RDT_LampIndex.invalidate(this.getLevel(), pos);
	}
}
