package com.rcdmake.whimsy.misc.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * 愚者模式的「床档」：决定睡眠补充速率、睡满奖励、HUD 箭头数。
 *
 * <p>放在这里而不是 {@code BHR_Sleep} 里，是因为<b>客户端也要读</b>
 * （睡觉时那 2~5 个箭头按床档画）。所以它只依赖 {@link BlockGetter}，不碰 {@code ServerLevel}。
 *
 * <p>木板床 / 金床还没做：给方块打上
 * {@code richards-whimsy-mod:fool_bed_wooden} / {@code ...:fool_bed_gold} 标签即可自动进档。
 */
public enum BHR_BedTier {

	/** 席地而睡 / 强制睡眠：1 点/世界 tick，无奖励，2 个箭头。 */
	FLOOR(1.0F, 0.0F, 2),

	/** 木板床：1.5 点/世界 tick，奖励 120 s = 2400，3 个箭头。 */
	WOODEN(1.5F, 2400.0F, 3),

	/** 软床 / 原版床：2 点/世界 tick，奖励 300 s = 6000，4 个箭头。 */
	SOFT(2.0F, 6000.0F, 4),

	/** 金床：10 点/世界 tick（20× 下 5 秒睡满），奖励 1000 s = 20000，5 个箭头。 */
	GOLD(10.0F, 20000.0F, 5);

	/** 木板床标签。 */
	public static final TagKey<Block> TAG_WOODEN =
			TagKey.create(BuiltInRegistries.BLOCK.key(), ModRegister.id("fool_bed_wooden"));

	/** 金床标签。 */
	public static final TagKey<Block> TAG_GOLD =
			TagKey.create(BuiltInRegistries.BLOCK.key(), ModRegister.id("fool_bed_gold"));

	/** 每个世界 tick 补充的精力点。 */
	public final float perWorldTick;

	/** 补满时给的黄条点数（0 = 不给）。 */
	public final float reward;

	/** 睡眠时精力条上画几个箭头。 */
	public final int arrows;

	BHR_BedTier(float perWorldTick, float reward, int arrows) {
		this.perWorldTick = perWorldTick;
		this.reward = reward;
		this.arrows = arrows;
	}

	/** 判定某个位置（床或地面）属于哪一档；位置为空按席地处理。 */
	public static BHR_BedTier of(BlockGetter level, @Nullable BlockPos pos) {
		if (pos == null) {
			return FLOOR;
		}
		BlockState state = level.getBlockState(pos);
		if (state.is(TAG_GOLD)) {
			return GOLD;
		}
		if (state.is(TAG_WOODEN)) {
			return WOODEN;
		}
		if (state.getBlock() instanceof BedBlock) {
			return SOFT;
		}
		return FLOOR;
	}
}
