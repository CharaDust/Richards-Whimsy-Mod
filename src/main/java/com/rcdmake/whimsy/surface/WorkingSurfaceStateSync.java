package com.rcdmake.whimsy.surface;

import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 工作面方块的「状态钩子」：需要跟着实体状态改外观的方块实现它。
 *
 * <p>目前只有简易篝火用（燃料 &gt; 2 单位时换成燃烧贴图）。方块实体在燃料变化、
 * 结算、随机刻时调用 {@link #onSurfaceStateChanged}，实现方自己决定要不要写世界。
 */
public interface WorkingSurfaceStateSync {

	/**
	 * 实体的状态可能变了，让方块外观跟上。
	 *
	 * <p>只在<b>真的需要翻转状态</b>时写世界（避免每 tick 刷方块更新）。
	 */
	void onSurfaceStateChanged(Level level, BlockPos pos, BlockState state, WorkingSurfaceEntity entity);
}
