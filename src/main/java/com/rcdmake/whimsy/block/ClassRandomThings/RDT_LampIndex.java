package com.rcdmake.whimsy.block.ClassRandomThings;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 两种灯（{@link RDT_LampLapis} / {@link RDT_LampQuartz}）共用的<b>位置索引</b>。
 *
 * <h2>它解决什么问题</h2>
 * 两盏灯都需要在「光照数据换上去之后，按本侧规则把引擎的场对齐」时回答「这附近有没有灯」：
 * <ul>
 *   <li>客户端：光照包里的数值是<b>服务端引擎</b>算的（青金石灯 0 / 石英灯 15），
 *       与客户端相反 ⇒ 要把青金石灯那 15 级光补回来、把石英灯那 15 级光抹掉，
 *       两者都要按灯的位置去调 {@code checkBlock}；</li>
 *   <li>服务端：两盏灯的状态发光值都是 0，原版 {@code propagateLightSources} 不把灯当光源 ⇒
 *       区块从盘上载入（含升级前的旧存档）时，石英灯的光要靠定位到灯来补。</li>
 * </ul>
 * 每次查询现扫 3×3 区块是不可接受的（{@code LevelChunkSection.maybeHas} 的调色板检查是 µs 级，
 * 而区块载入时这样的查询会连着来几十次），所以这里按「区块 → 该区块内灯的位置表」缓存一次。
 *
 * <h2>缓存的正确性靠什么</h2>
 * <ul>
 *   <li><b>惰性填充</b>：某区块第一次被问到时用 {@code ChunkAccess.findBlocks} 扫一次
 *       （内部先走 {@code LevelChunkSection.maybeHas} 的调色板判断，没灯的区块一段就返回 false），
 *       结果缓存；</li>
 *   <li><b>方块变化时失效</b>：{@code LevelChunk.setBlockState} 每次改动都会通知
 *       {@link com.rcdmake.whimsy.mixin.ClassRandomThings.RDT_LampIndexMaintenanceMixin}，
 *       改动涉及灯（放下或被拆）就作废该区块的缓存 —— 服务端与客户端都覆盖
 *       （26.2 的 {@code Block.onPlace} 只在服务端调，客户端那条路只能靠这个钩子）；</li>
 *   <li><b>查询时复查</b>：返回前再 {@code getBlockState(pos)} 确认那一格还是灯，
 *       所以即使漏掉一次失效通知，也只是「多查一次」，不会变成幽灵光；</li>
 *   <li><b>上限</b>：每维度缓存区块数超限就整体清空（粗暴但简单，清空后按需重扫）。</li>
 * </ul>
 *
 * <h2>为什么按「服务端 / 客户端」分成两张表</h2>
 * 单人游戏里客户端与内部服务端在<b>同一个进程</b>，两者的 {@code Level} 是不同对象、
 * 区块内容也可能不同（客户端只持有已同步的区块）。若按维度只存一张表，客户端的查询会
 * 命中服务端扫出来的结果。所以按 {@code level.isClientSide()} 分成两张表，各自按维度再分一层
 * （维度数量有限，不会像按 {@code Level} 实例做键那样在下界/末地反复进出时泄漏引用）。
 */
public final class RDT_LampIndex {

	/** 15 级光在空气中每步衰减 1，传 14 步后剩 1 级 —— 索引里所有「够得着」判断都用它。 */
	public static final int REACH = 14;

	/** 每个维度最多缓存多少个区块的灯位置表（超限整体清空）。 */
	private static final int MAX_CACHED_CHUNKS = 2048;

	/** 服务端视角：维度 → （区块 {@code ChunkPos.pack} → 该区块内灯的位置表）。 */
	private static final Map<ResourceKey<Level>, Map<Long, List<BlockPos>>> SERVER_INDEX = new ConcurrentHashMap<>();

	/** 客户端视角（与 {@link #SERVER_INDEX} 分开的理由见类注释）。 */
	private static final Map<ResourceKey<Level>, Map<Long, List<BlockPos>>> CLIENT_INDEX = new ConcurrentHashMap<>();

	private RDT_LampIndex() {
	}

	/** 该方块状态是不是我们两种灯之一。 */
	public static boolean isLamp(BlockState state) {
		return state.is(RDT_LampLapis.RDT_LAPIS_LAMP) || state.is(RDT_LampQuartz.RDT_QUARTZ_LAMP);
	}

	/**
	 * 取某区块内的灯位置表（惰性扫描 + 缓存；两种灯都收）。
	 *
	 * <p>调用方自己按具体灯种过滤 / 判断距离。线程安全：服务端的光照线程与主线程都会调它，
	 * 最坏情况是两个线程各扫一次同一个区块，结果一致，无害。
	 */
	public static List<BlockPos> lampsInChunk(Level level, int chunkX, int chunkZ) {
		Map<Long, List<BlockPos>> byChunk = indexOf(level)
				.computeIfAbsent(level.dimension(), dimension -> new ConcurrentHashMap<>());
		long key = ChunkPos.pack(chunkX, chunkZ);

		List<BlockPos> cached = byChunk.get(key);
		if (cached != null) {
			return cached;
		}

		LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
		if (chunk == null) {
			// 未加载：本次按「没有灯」处理，但**不写缓存** —— 区块加载后还要能扫到里面的灯
			return List.of();
		}

		List<BlockPos> found = new ArrayList<>();
		chunk.findBlocks(RDT_LampIndex::isLamp, (lampPos, state) -> found.add(lampPos.immutable()));
		List<BlockPos> result = List.copyOf(found);

		if (byChunk.size() >= MAX_CACHED_CHUNKS) {
			byChunk.clear();
		}
		byChunk.put(key, result);
		return result;
	}

	/** 作废某位置所在区块的缓存（由 {@code LevelChunk.setBlockState} 的钩子在灯被放下/拆掉时调）。 */
	public static void invalidate(Level level, BlockPos pos) {
		Map<Long, List<BlockPos>> byChunk = indexOf(level).get(level.dimension());
		if (byChunk != null) {
			byChunk.remove(ChunkPos.pack(pos.getX() >> 4, pos.getZ() >> 4));
		}
	}

	/**
	 * 取「可能照到某个区块段」的灯：3×3 区块内、与这个区块段包围盒的曼哈顿距离 ≤ {@link #REACH} 的灯。
	 *
	 * <p>曼哈顿距离到包围盒 = 三个轴的分量距离之和，正是「无遮挡 15 级光能否照进这个区块段」的判据；
	 * 每个候选位置都会复查一次方块状态，所以拆掉的灯不会被算进来。
	 *
	 * <p>用途：两侧在光照数据落地之后，对这些灯各调一次 {@code checkBlock}
	 * —— 引擎自己的增长/衰减传播会把该补的补上、该抹的抹掉（遮挡、最大值场都由引擎负责）。
	 */
	public static List<BlockPos> lampsReaching(Level level, SectionPos section) {
		int sectionX = section.x();
		int sectionZ = section.z();
		int sectionMinY = section.minBlockY();
		int sectionMaxY = section.maxBlockY();
		int sectionMinX = section.minBlockX();
		int sectionMaxX = section.maxBlockX();
		int sectionMinZ = section.minBlockZ();
		int sectionMaxZ = section.maxBlockZ();

		List<BlockPos> result = new ArrayList<>();
		for (int chunkX = sectionX - 1; chunkX <= sectionX + 1; chunkX++) {
			for (int chunkZ = sectionZ - 1; chunkZ <= sectionZ + 1; chunkZ++) {
				for (BlockPos lamp : lampsInChunk(level, chunkX, chunkZ)) {
					int dx = axisDistance(lamp.getX(), sectionMinX, sectionMaxX);
					int dy = axisDistance(lamp.getY(), sectionMinY, sectionMaxY);
					int dz = axisDistance(lamp.getZ(), sectionMinZ, sectionMaxZ);
					if (dx + dy + dz > REACH) {
						continue;
					}
					// 复查：缓存里的位置可能已经被拆掉（失效通知理论上会到，但复查只花一次方块查询）
					if (isLamp(level.getBlockState(lamp))) {
						result.add(lamp);
					}
				}
			}
		}
		return result;
	}

	/** 坐标到区间 [min, max] 的距离（在区间内为 0）。 */
	private static int axisDistance(int value, int min, int max) {
		if (value < min) {
			return min - value;
		}
		return value > max ? value - max : 0;
	}

	/** 按侧别选索引表。 */
	private static Map<ResourceKey<Level>, Map<Long, List<BlockPos>>> indexOf(Level level) {
		return level.isClientSide() ? CLIENT_INDEX : SERVER_INDEX;
	}
}
