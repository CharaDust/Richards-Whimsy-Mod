package com.rcdmake.whimsy.block.ClassRandomThings;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 通灵镜片（{@code rdt_spectre_lens}）的<b>全局记录</b> —— 维度 → 效果 → 强度等级 → 坐标数组。
 *
 * <p>这是本功能「同一条规则只有一份实现」的载体（范式 §7 第 9 条）：镜片方块只留四个转发点
 * （放置 / 计划刻 / 邻居变化 / 拆除），记录、校验、施放全部收在这里。
 *
 * <h2>数据结构（用户指定）</h2>
 * <pre>
 * 维度1
 * ├─ 效果1
 * │  ├─ I 级 → [坐标1, 坐标2, …]
 * │  └─ II 级 → [坐标…]
 * └─ 效果2
 *     └─ I 级 → [坐标…]
 * 维度2
 * └─ …
 * </pre>
 * 坐标数组本身就是去重器：多个镜片录到同一个 (维度, 效果, 强度) 时只是数组变长，
 * <b>施加时按节点做一次</b>，不是按坐标做 N 次。数组空 = 该节点不存在（删节点时会逐层回收）。
 *
 * <h2>强度等级怎么来（对齐原版 {@code BeaconBlockEntity.applyEffects}）</h2>
 * <ul>
 *   <li>主效果：4 级信标且主副效果相同时是 II 级（amplifier 1），其余一律 I 级（amplifier 0）；</li>
 *   <li>副效果：只有 4 级信标、且副效果与主效果不同时才发，I 级；</li>
 *   <li>原版<b>不校验</b>「这个效果需要几级信标」—— 降级后的信标会继续发旧效果，这里镜像原版。</li>
 * </ul>
 *
 * <h2>为什么要自己数金字塔、不读 NBT 的 {@code Levels}</h2>
 * {@code BeaconBlockEntity.loadAdditional} 不读 {@code Levels}，{@code levels} 只在 80 tick 边界
 * 由 {@code updateBase} 赋值，而且<b>只有光束分段非空时</b>才算。于是区块刚加载时信标可能连续
 * 近 4 秒 {@code Levels == 0}（金字塔其实是好的）。照 {@code Levels} 删记录会把好镜片误杀，
 * 所以这里按原版规则现场数（{@code BlockTags.BEACON_BASE_BLOCKS}，1~4 层）。
 *
 * <h2>光束判定（D3：严格镜像原版）</h2>
 * 先看公开的 {@code BeaconBlockEntity.getBeamSections()}：非空即光束正常。
 * 为空时不能直接判失效 —— 它内部是 {@code levels == 0 ? empty : beamSections}，
 * 刚加载的区块也会为空。此时做一次确定性回退扫描：
 * 金字塔 0 级 → 失效；从信标上方扫到 {@code WORLD_SURFACE}，遇到
 * {@code getLightDampening() >= 15 && !BEDROCK} 的方块 → 光束被挡 → 失效；否则保留。
 *
 * <h2>证据与论证</h2>
 * 全部 API 事实（含行号）见 {@code doc/agent/deepseek/11-通灵镜片调查报告.md} §4。
 */
public final class RDT_SpectreLensIndex {

	/** 区块/相邻区块没加载、无法判定时的哨兵：返回空 {@link Optional} 表示「保留，不要删」。 */
	private static final Optional<Set<RecordKey>> UNKNOWN = Optional.empty();

	/** 维度 → 效果 → 强度等级(1/2) → 坐标集合。 */
	private static final Map<ResourceKey<Level>, Map<ResourceKey<MobEffect>, Map<Integer, Set<BlockPos>>>> INDEX =
			new HashMap<>();

	/** 维度 → 坐标 → 该镜片挂在哪些 (效果, 强度) 上；拆除/改效果时反查用。 */
	private static final Map<ResourceKey<Level>, Map<BlockPos, Set<RecordKey>>> BY_POS = new HashMap<>();

	private RDT_SpectreLensIndex() {
	}

	/**
	 * 接上全局事件：每个维度 tick 末尾的 80 拍校验/施放；服务器停止时清空索引。
	 * <p>由 {@link RDT_SpectreLens#OnInit()} 调用（主类只认识方块类的那一条入口）。
	 */
	public static void OnInit() {
		ServerTickEvents.END_LEVEL_TICK.register(RDT_SpectreLensIndex::onLevelTick);
		// 退出/换存档时清空，避免索引跟着服务端单例跨存档残留
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> clearAll());
	}

	// ─────────────────────────────────────────────────────────────────────
	// 镜片侧的四个转发入口
	// ─────────────────────────────────────────────────────────────────────

	/** 镜片被放置：立刻上报一次。 */
	public static void onLensPlaced(Level level, BlockPos pos) {
		if (level instanceof ServerLevel serverLevel) {
			refresh(serverLevel, pos);
		}
	}

	/** 镜片自己的 80 tick 计划刻：重算并修正记录。 */
	public static void onLensClock(ServerLevel level, BlockPos pos) {
		refresh(level, pos);
	}

	/** 镜片被拆除：删掉该坐标的全部记录（用户要求的「失效信号」）。 */
	public static void onLensRemoved(ServerLevel level, BlockPos pos) {
		removePos(level.dimension(), pos);
	}

	// ─────────────────────────────────────────────────────────────────────
	// 全局 80 拍：校验 + 施放
	// ─────────────────────────────────────────────────────────────────────

	/**
	 * 每个维度的 tick 末尾调用。
	 * <p>只在 {@code gameTime % 80 == 0} 的拍子干活，与信标同拍；此时信标方块实体已经结算完
	 * 本拍（{@code chunkSource.tick} 在 {@code ServerLevel.tick} 里先于本事件）。
	 */
	private static void onLevelTick(ServerLevel level) {
		if (level.getGameTime() % RDT_SpectreLens.BEACON_CLOCK_TICKS != 0L) {
			return;
		}

		ResourceKey<Level> dimension = level.dimension();
		Map<BlockPos, Set<RecordKey>> byPos = BY_POS.get(dimension);
		if (byPos == null || byPos.isEmpty()) {
			return;
		}

		// ① 校验（会顺手把记录对齐到当前信标状态；对齐 = 删旧 + 写新，见 align）
		for (BlockPos pos : new ArrayList<>(byPos.keySet())) {
			if (!level.hasChunkAt(pos)) {
				continue; // 区块未加载：查询失败也算有效 → 保留
			}
			if (!level.getBlockState(pos).is(RDT_SpectreLens.RDT_SPECTRE_LENS)) {
				removePos(dimension, pos); // 已加载，但这里不是镜片了 → 删除
				continue;
			}
			Optional<Set<RecordKey>> current = readRecords(level, pos);
			if (current.isEmpty()) {
				continue; // 无法判定（相邻区块没加载）→ 保留
			}
			align(dimension, pos, current.get());
		}

		// ② 施放：按 (效果, 等级) 节点各给该维度所有玩家施加一次，340 tick
		applyEffects(level, dimension);
	}

	/**
	 * 施放：维度内所有玩家各拿一次记录里的效果。
	 * <p>按节点（效果 + 强度）施加，坐标数组只用来判断「节点是否为空」——多个镜片同效果不会叠加。
	 */
	private static void applyEffects(ServerLevel level, ResourceKey<Level> dimension) {
		Map<ResourceKey<MobEffect>, Map<Integer, Set<BlockPos>>> byEffect = INDEX.get(dimension);
		if (byEffect == null || byEffect.isEmpty()) {
			return;
		}
		List<ServerPlayer> players = level.players();
		if (players.isEmpty()) {
			return;
		}

		for (Map.Entry<ResourceKey<MobEffect>, Map<Integer, Set<BlockPos>>> effectEntry : byEffect.entrySet()) {
			Holder<MobEffect> effect = resolveEffect(effectEntry.getKey());
			if (effect == null) {
				continue; // 效果注册表里已经没有它（理论上不会）
			}
			for (Map.Entry<Integer, Set<BlockPos>> levelEntry : effectEntry.getValue().entrySet()) {
				if (levelEntry.getValue().isEmpty()) {
					continue;
				}
				int amplifier = levelEntry.getKey() - 1; // 记录里的等级 = amplifier + 1
				for (ServerPlayer player : players) {
					player.addEffect(new MobEffectInstance(effect,
							RDT_SpectreLens.BROADCAST_DURATION_TICKS, amplifier, true, true));
				}
			}
		}
	}

	/** 重算一个镜片应有的记录；无法判定时保留原记录。 */
	private static void refresh(ServerLevel level, BlockPos pos) {
		Optional<Set<RecordKey>> current = readRecords(level, pos);
		if (current.isEmpty()) {
			return;
		}
		align(level.dimension(), pos, current.get());
	}

	// ─────────────────────────────────────────────────────────────────────
	// 读「下方信标现在是什么」
	// ─────────────────────────────────────────────────────────────────────

	/**
	 * 现场读：这个镜片现在应该记哪些 (效果, 强度)。
	 *
	 * @return 记录集合；{@link Optional#empty()} = 无法判定（相邻区块没加载 / 效果没绑 key）→ 保留旧记录
	 */
	private static Optional<Set<RecordKey>> readRecords(ServerLevel level, BlockPos lensPos) {
		BlockPos beaconPos = lensPos.below();
		if (level.isOutsideBuildHeight(beaconPos)) {
			return Optional.of(Set.of());
		}
		if (!(level.getBlockEntity(beaconPos) instanceof BeaconBlockEntity beacon)) {
			return Optional.of(Set.of()); // 下方不是信标
		}
		// 金字塔最远 4 格，可能跨到相邻区块；没加载就「无法判定」
		if (!level.hasChunksAt(beaconPos.offset(-4, -4, -4), beaconPos.offset(4, 0, 4))) {
			return UNKNOWN;
		}

		int levels = countPyramid(level, beaconPos);
		if (levels <= 0) {
			return Optional.of(Set.of()); // 金字塔无效
		}

		// 光束：公开快路径；为空时用确定性扫描再判（区分「被挡」和「刚加载还没扫完」）
		if (beacon.getBeamSections().isEmpty() && isBeamBlocked(level, beaconPos)) {
			return Optional.of(Set.of());
		}

		// 效果：信标只提供了私有字段，走公开的 NBT 快照（BeaconBlockEntity.saveAdditional 写的键）
		CompoundTag tag = beacon.saveWithoutMetadata(level.registryAccess());
		ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag);
		Optional<Holder<MobEffect>> primary =
				input.read("primary_effect", BuiltInRegistries.MOB_EFFECT.holderByNameCodec());
		Optional<Holder<MobEffect>> secondary =
				input.read("secondary_effect", BuiltInRegistries.MOB_EFFECT.holderByNameCodec());
		if (primary.isEmpty()) {
			return Optional.of(Set.of()); // 没选主效果
		}

		ResourceKey<MobEffect> primaryKey = primary.get().unwrapKey().orElse(null);
		if (primaryKey == null) {
			return UNKNOWN; // 效果没绑资源键 → 不写进记录
		}
		ResourceKey<MobEffect> secondaryKey = secondary.flatMap(holder -> holder.unwrapKey()).orElse(null);

		Set<RecordKey> records = new HashSet<>(2);
		// 主效果：4 级且主副相同时 II 级，否则 I 级
		boolean doublePrimary = levels >= 4 && secondaryKey != null && secondaryKey.equals(primaryKey);
		records.add(new RecordKey(primaryKey, doublePrimary ? 2 : 1));
		// 副效果：只有 4 级且与主效果不同才发，I 级
		if (levels >= 4 && secondaryKey != null && !secondaryKey.equals(primaryKey)) {
			records.add(new RecordKey(secondaryKey, 1));
		}
		return Optional.of(records);
	}

	/**
	 * 数金字塔层数（原版 {@code BeaconBlockEntity.updateBase} 的同一套规则）：
	 * 第 n 层（n=1..4）在 {@code y-n} 平面上、{@code x±n / z±n} 范围内全是
	 * {@link BlockTags#BEACON_BASE_BLOCKS}。
	 */
	private static int countPyramid(ServerLevel level, BlockPos beaconPos) {
		int result = 0;
		for (int step = 1; step <= 4; step++) {
			if (beaconPos.getY() - step < level.getMinY()) {
				break;
			}
			boolean complete = true;
			for (int dx = -step; dx <= step && complete; dx++) {
				for (int dz = -step; dz <= step; dz++) {
					if (!level.getBlockState(beaconPos.offset(dx, -step, dz)).is(BlockTags.BEACON_BASE_BLOCKS)) {
						complete = false;
						break;
					}
				}
			}
			if (!complete) {
				break;
			}
			result = step;
		}
		return result;
	}

	/**
	 * 光束是否被挡（原版光束扫描的判定式）：从信标上方扫到 {@code WORLD_SURFACE}，
	 * 遇到 {@code getLightDampening() >= 15} 且不是基岩的方块就算被挡。
	 * <p>只在 {@code getBeamSections()} 为空时调用（它可能是「刚加载还没扫完」）。
	 */
	private static boolean isBeamBlocked(ServerLevel level, BlockPos beaconPos) {
		int x = beaconPos.getX();
		int z = beaconPos.getZ();
		int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
		for (int y = beaconPos.getY() + 1; y <= top; y++) {
			BlockState state = level.getBlockState(new BlockPos(x, y, z));
			if (state.getLightDampening() >= 15 && !state.is(Blocks.BEDROCK)) {
				return true;
			}
		}
		return false;
	}

	/** 记录里的效果键 → 施加用的 Holder；注册表里找不到返回 null。 */
	private static Holder<MobEffect> resolveEffect(ResourceKey<MobEffect> key) {
		// 26.2 的 Registry#get(Identifier) 返回的是 Optional<Holder.Reference<T>>（不是 Optional<T>），
		// 正好可以直接当 Holder 用；取值本体才用 getValue(...)。
		return BuiltInRegistries.MOB_EFFECT.get(key.identifier()).orElse(null);
	}

	// ─────────────────────────────────────────────────────────────────────
	// 索引的增删（含空节点回收）
	// ─────────────────────────────────────────────────────────────────────

	/** 把某个镜片的记录对齐到 {@code current}（不一致 = 删旧 + 写新，D2）。 */
	private static void align(ResourceKey<Level> dimension, BlockPos lensPos, Set<RecordKey> current) {
		BlockPos pos = lensPos.immutable();
		Map<BlockPos, Set<RecordKey>> byPos = BY_POS.computeIfAbsent(dimension, key -> new HashMap<>());
		Set<RecordKey> stored = byPos.get(pos);
		if (stored != null && stored.equals(current)) {
			return;
		}
		if (stored != null) {
			for (RecordKey record : stored) {
				removeRecord(dimension, pos, record);
			}
		}
		if (current.isEmpty()) {
			byPos.remove(pos);
			if (byPos.isEmpty()) {
				BY_POS.remove(dimension);
			}
			return;
		}
		byPos.put(pos, new HashSet<>(current));
		for (RecordKey record : current) {
			addRecord(dimension, pos, record);
		}
	}

	/** 删掉某个坐标的全部记录。 */
	private static void removePos(ResourceKey<Level> dimension, BlockPos lensPos) {
		BlockPos pos = lensPos.immutable();
		Map<BlockPos, Set<RecordKey>> byPos = BY_POS.get(dimension);
		if (byPos == null) {
			return;
		}
		Set<RecordKey> stored = byPos.remove(pos);
		if (stored == null) {
			return;
		}
		for (RecordKey record : stored) {
			removeRecord(dimension, pos, record);
		}
		if (byPos.isEmpty()) {
			BY_POS.remove(dimension);
		}
	}

	/** 写一条记录（索引 + 反查表两处同时写）。 */
	private static void addRecord(ResourceKey<Level> dimension, BlockPos pos, RecordKey record) {
		INDEX.computeIfAbsent(dimension, key -> new HashMap<>())
				.computeIfAbsent(record.effect(), key -> new HashMap<>())
				.computeIfAbsent(record.level(), key -> new HashSet<>())
				.add(pos);
		BY_POS.computeIfAbsent(dimension, key -> new HashMap<>())
				.computeIfAbsent(pos, key -> new HashSet<>())
				.add(record);
	}

	/** 删一条记录；空数组 → 删等级节点 → 删效果节点 → 删维度节点。 */
	private static void removeRecord(ResourceKey<Level> dimension, BlockPos pos, RecordKey record) {
		Map<ResourceKey<MobEffect>, Map<Integer, Set<BlockPos>>> byEffect = INDEX.get(dimension);
		if (byEffect == null) {
			return;
		}
		Map<Integer, Set<BlockPos>> byLevel = byEffect.get(record.effect());
		if (byLevel == null) {
			return;
		}
		Set<BlockPos> positions = byLevel.get(record.level());
		if (positions == null) {
			return;
		}
		positions.remove(pos);
		if (!positions.isEmpty()) {
			return;
		}
		byLevel.remove(record.level());
		if (byLevel.isEmpty()) {
			byEffect.remove(record.effect());
			if (byEffect.isEmpty()) {
				INDEX.remove(dimension);
			}
		}
	}

	/** 服务器停止：清空（内存索引不落盘，D4 第一期）。 */
	private static void clearAll() {
		INDEX.clear();
		BY_POS.clear();
	}

	/**
	 * 记录键：效果 + 强度等级（1 = I 级 / amplifier 0，2 = II 级 / amplifier 1）。
	 * <p>用 {@link ResourceKey} 而不是 {@code Holder} 当键：{@code Holder.Reference} 没有覆写
	 * {@code equals/hashCode}，跨来源的 Holder 不保证同一实例。
	 */
	private record RecordKey(ResourceKey<MobEffect> effect, int level) {
	}
}
