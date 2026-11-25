package com.rcdmake.whimsy.surface;

import com.rcdmake.whimsy.ModRegister;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_TimeCrystal;
import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_GameRules;
import com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon.BHR_FoolIngredient;
import com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon.BHR_FoolRecipe;
import com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon.BHR_FoolRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工作面（Working Surface）的方块实体 —— 制作台 / 木工台 / 简易篝火<b>共用这一个类</b>。
 *
 * <h2>三台机器只差一份 {@link WorkingSurfaceSpec}</h2>
 * 注册名、等级上限、有没有燃料、界面底图、面板是否右移全部来自方块上的 spec；
 * 实体本身不知道自己"是哪一台"。
 *
 * <h2>进度模型：时间差 + 区间结算（三台统一）</h2>
 * <p>进度<b>不是</b>逐 tick 累加的，而是「当前游戏刻 − 本份开始刻」现算：
 * <pre>{@code
 *   虚拟时间 = (暂停中 ? 暂停起点 : min(now, 燃料燃尽刻)) - 累计暂停
 *   进度     = 虚拟时间 - 本份开始刻
 * }</pre>
 * 于是：<b>没有界面在看时方块完全不参与 tick</b>，区块卸载也不会让进度变慢；
 * 重新打开界面 / 随机刻触发时按差额一次补齐。
 *
 * <p>燃料台的「缺燃料暂停」用<b>区间</b>表达，而不是逐 tick 状态：
 * 记下「这批燃料烧到哪一刻」（{@link #fuelEndTick}），暂停就是那段没有燃料的区间；
 * 补燃料时把这段区间一次性累加进 {@link #pausedTicks}。所以燃料台也<b>不需要每 tick 干活</b> —
 * 方块侧只用随机刻做两件事：结算区间、把 {@code lit} 状态跟上（见 {@link WorkingSurfaceStateSync}）。
 *
 * <h2>存档</h2>
 * 键名沿用老版本的（{@code recipe / batches / craft_start / total / scroll / remaining / preferred / pending}），
 * 新增 {@code paused_ticks / pause_start / fuel_end / fuel_stored / fuel_pref}，老存档可直接读。
 */
public class WorkingSurfaceEntity extends BlockEntity implements Container, ContainerData {

	// ── 容器 ──

	/** 容器槽：时间水晶。 */
	public static final int CRYSTAL_SLOT = 0;
	/** 容器槽：结果。 */
	public static final int RESULT_SLOT = 1;
	/** 容器容量。 */
	public static final int CONTAINER_SIZE = 2;

	// ── 同步下标（ContainerData）──

	public static final int DATA_SELECTED = 0;
	public static final int DATA_BATCHES = 1;
	public static final int DATA_PROGRESS = 2;
	public static final int DATA_TOTAL = 3;
	public static final int DATA_LEVEL = 4;
	public static final int DATA_EXPERT = 5;
	public static final int DATA_SCROLL = 6;
	public static final int DATA_REMAINING = 7;
	public static final int DATA_PREF_START = 8;
	public static final int DATA_PREF_COUNT = 9;
	/** 剩余燃料（tick）—— 只有烧燃料的机器会读它。 */
	public static final int DATA_FUEL = DATA_PREF_START + DATA_PREF_COUNT;
	/** 燃料档位的首选物品（注册表数字 id，0 = 没指定）。 */
	public static final int DATA_FUEL_PREF_START = DATA_FUEL + 1;

	/** 菜单里最多显示 8 条配方。 */
	public static final int RECIPE_SLOTS = 8;
	/** 倍率上限。 */
	public static final int MAX_BATCHES = 64;

	/** 方块 → 方块实体类型（三台各注册一个类型，构造时按方块反查）。 */
	private static final Map<Block, BlockEntityType<WorkingSurfaceEntity>> TYPES = new HashMap<>();

	// ── 持久状态 ──

	private final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

	/** 选中配方在 {@link BHR_FoolRecipes#all()} 里的下标；-1 = 没选（界面高亮用）。 */
	private int selected = -1;
	/**
	 * 选中配方的<b>权威身份</b>（={@link BHR_FoolRecipes#all()} 里那一条的 id）。
	 *
	 * <p>为什么既存下标又存 id：romote 配方（服务端投影下发的原版配方）会在数据包重载后整批换掉，
	 * 下标会漂移甚至指向别的配方；id 不会。判定与制作一律以 id 为准，下标只用于界面高亮。
	 */
	private Identifier selectedId;
	/** 一次制作几份。 */
	private int batches = 1;
	/** 本份开始的<b>虚拟时间</b>（见类注释的公式）。 */
	private long craftStart;
	/** 本份总 tick（0 = 没在制作）。 */
	private int total;
	/** 配方栏滚动偏移。 */
	private int scroll;
	/** 本次还要做几份。 */
	private int remaining;
	/** 9 个需求格的首选物品（注册表数字 id，0 = 用默认第一候选）。 */
	private final int[] preferred = new int[DATA_PREF_COUNT];
	/** 燃料档的首选物品（注册表数字 id，0 = 没指定）。 */
	private final int[] fuelPreferred;
	/** 已投入但还没被某一份消耗掉的原料。 */
	private final List<ItemStack> pending = new ArrayList<>();

	/** 累计暂停 tick（虚拟时间 = 真实时间 − 累计暂停）。 */
	private long pausedTicks;
	/** 暂停起点（0 = 没暂停）。 */
	private long pauseStart;
	/** 当前这批燃料的燃尽刻（0 = 没在烧）。 */
	private long fuelEndTick;
	/** 没在制作时攒下的燃料（tick），开工时才点着。 */
	private int fuelStored;

	public WorkingSurfaceEntity(BlockPos pos, BlockState state) {
		super(typeOf(state), pos, state);
		WorkingSurfaceSpec spec = specOf(state);
		this.fuelPreferred = new int[spec.fuelSlots()];
	}

	/**
	 * 注册一台工作面的方块实体类型，并登记「方块 → 类型」的映射。
	 *
	 * @param name  注册路径（= spec id）
	 * @param block 这台机器的方块
	 */
	public static BlockEntityType<WorkingSurfaceEntity> registerType(String name, Block block) {
		BlockEntityType<WorkingSurfaceEntity> type = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
				ModRegister.id(name),
				new BlockEntityType<>(WorkingSurfaceEntity::new, Set.of(block)));
		TYPES.put(block, type);
		return type;
	}

	private static BlockEntityType<WorkingSurfaceEntity> typeOf(BlockState state) {
		BlockEntityType<WorkingSurfaceEntity> type = TYPES.get(state.getBlock());
		if (type == null) {
			throw new IllegalStateException("方块没有登记的方块实体类型：" + state.getBlock());
		}
		return type;
	}

	// ── 规格 ──

	/** 本机规格（来自方块）。 */
	public WorkingSurfaceSpec spec() {
		return specOf(this.getBlockState());
	}

	private static WorkingSurfaceSpec specOf(BlockState state) {
		return state.getBlock() instanceof WorkingSurfaceBlock surface
				? surface.spec()
				: null;
	}

	/** 本机的配方分组键（= 配方 JSON 里的 {@code workbench} 字段）。 */
	public String benchId() {
		WorkingSurfaceSpec spec = this.spec();
		return spec == null ? "" : spec.id();
	}

	/** 数据槽个数：17 个公共 + 1 个燃料量 + N 个燃料首选。 */
	@Override
	public int getCount() {
		WorkingSurfaceSpec spec = this.spec();
		return DATA_FUEL_PREF_START + (spec == null ? 0 : spec.fuelSlots());
	}

	// ── 制作状态 ──

	public boolean isCrafting() {
		return this.total > 0;
	}

	public int getSelected() {
		return this.selected;
	}

	public BHR_FoolRecipe selectedRecipe() {
		if (this.selectedId != null) {
			// 以 id 为准：远端配方整批换过之后，下标可能已经指向别的一条
			return BHR_FoolRecipes.byId(this.selectedId).orElse(null);
		}
		if (this.selected < 0 || this.selected >= BHR_FoolRecipes.all().size()) {
			return null;
		}
		return BHR_FoolRecipes.all().get(this.selected).recipe();
	}

	/** 当前等级（方块状态的 {@code level}）。 */
	public int getWorkBenchLevel() {
		return this.getBlockState().getValue(WorkingSurfaceBlock.LEVEL);
	}

	/** 当前是否使用专家配方（只有服务端拿得到规则值）。 */
	public boolean isExpertRecipes() {
		Level level = this.getLevel();
		return level instanceof net.minecraft.server.level.ServerLevel serverLevel
				&& serverLevel.getGameRules().get(BHR_GameRules.BHR_EXPERT_RECIPES);
	}

	/**
	 * 配方栏要显示的配方（本机 + 当前模式）。
	 *
	 * <p>排序与「专家版优先、缺了退回普通版」的规则都在 {@link BHR_FoolRecipes#visibleFor} 里，
	 * 客户端界面调用同一个方法，两端看到的列表天然一致。
	 */
	public List<BHR_FoolRecipes.Entry> visibleEntries() {
		return BHR_FoolRecipes.visibleFor(this.benchId(), this.isExpertRecipes());
	}

	/** 当前等级在升级栏里能用的下一条升级配方。 */
	public BHR_FoolRecipes.Entry nextUpgrade() {
		return BHR_FoolRecipes.nextUpgradeFor(this.benchId(), this.isExpertRecipes(), this.getWorkBenchLevel());
	}

	public int getBatches() {
		return this.batches;
	}

	public int getTotal() {
		return this.total;
	}

	public int getScroll() {
		return this.scroll;
	}

	public void setScroll(int scroll) {
		this.scroll = Math.max(0, scroll);
	}

	public long currentGameTime() {
		Level level = this.getLevel();
		return level == null ? 0L : level.getGameTime();
	}

	/** 当前进度（tick）：现算，不是累加值。 */
	public int getProgress() {
		return this.craftProgress(this.currentGameTime());
	}

	// ── 时间轴（三台统一）──

	/** 当前这一份是不是「不烧燃料」的活：升级配方永远是（改的是方块自己的等级）。 */
	public boolean isFuelFreeCraft() {
		BHR_FoolRecipe recipe = this.selectedRecipe();
		return recipe != null && recipe.isUpgrade();
	}

	/** 燃料是否参与「暂停」判定。 */
	private boolean fuelMatters() {
		WorkingSurfaceSpec spec = this.spec();
		return spec != null && spec.hasFuel() && !this.isFuelFreeCraft();
	}

	/**
	 * 结算「燃料烧完了」这件事：把暂停起点定在<b>燃尽那一刻</b>（而不是被发现的那一刻），
	 * 这样晚结算也不会多算或少算。
	 */
	private void settleFuel() {
		if (this.fuelEndTick > 0 && this.currentGameTime() >= this.fuelEndTick) {
			this.pauseStart = this.fuelEndTick;
			this.fuelEndTick = 0;
		}
	}

	/** 虚拟时间：真实时间扣掉累计暂停；暂停中则冻结。 */
	private long virtualNow(long now) {
		long effective = now;
		if (this.fuelMatters()) {
			if (this.pauseStart > 0) {
				effective = this.pauseStart;
			} else if (this.fuelEndTick > 0 && now > this.fuelEndTick) {
				effective = this.fuelEndTick;
			}
		}
		return effective - this.pausedTicks;
	}

	/**
	 * 把「到此刻为止该走完的份数」补算掉。
	 *
	 * <p>调用点：打开界面时、界面打开期间每 tick（菜单 {@code broadcastChanges}）、
	 * 方块被破坏时、随机刻（燃料台）。没有调用者时方块什么都不做 —— 但进度仍然是对的，
	 * 因为时间在流逝。
	 */
	public void refresh(long now) {
		if (this.total <= 0) {
			return;
		}
		this.settleFuel();
		long virtual = this.virtualNow(now);
		if (virtual < this.craftStart) {
			// /time set 把游戏刻调回去了：视为重新开始这一份
			this.craftStart = virtual;
			this.setChanged();
			return;
		}
		int guard = 0;
		while (this.total > 0 && virtual - this.craftStart >= this.total && guard++ < 512) {
			this.craftStart += this.total;
			boolean continues = this.emitOneBatch();
			if (!continues) {
				break;
			}
		}
		this.setChanged();
	}

	/** 只算进度，不结算（给同步 / 绘制用）。 */
	private int craftProgress(long now) {
		if (this.total <= 0) {
			return 0;
		}
		long elapsed = this.virtualNow(now) - this.craftStart;
		if (elapsed <= 0) {
			return 0;
		}
		return (int) Math.min(this.total, elapsed);
	}

	// ── 选择 / 倍率 ──

	/** 选中配方（传的是 {@link BHR_FoolRecipes#all()} 的下标）。 */
	public void select(int index) {
		if (this.isCrafting()) {
			return;
		}
		if (index != this.selected) {
			java.util.Arrays.fill(this.preferred, 0);
		}
		this.selected = index;
		this.selectedId = index >= 0 && index < BHR_FoolRecipes.all().size()
				? BHR_FoolRecipes.all().get(index).id()
				: null;
		if (this.selectedRecipe() != null && this.selectedRecipe().isUpgrade()) {
			this.batches = 1;
		}
		this.setChanged();
	}

	public void setBatches(int batches) {
		if (this.isCrafting()) {
			return;
		}
		if (this.selectedRecipe() != null && this.selectedRecipe().isUpgrade()) {
			this.batches = 1;
			this.setChanged();
			return;
		}
		this.batches = Math.clamp(batches, 1, MAX_BATCHES);
		this.setChanged();
	}

	/** 某个需求格当前的首选物品；没有指定时返回空堆（用默认第一候选）。 */
	public ItemStack preferredItem(int ingredientIndex) {
		if (ingredientIndex < 0 || ingredientIndex >= this.preferred.length || this.preferred[ingredientIndex] == 0) {
			return ItemStack.EMPTY;
		}
		Item item = BuiltInRegistries.ITEM.byId(this.preferred[ingredientIndex]);
		return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
	}

	/** 取消选中的配方（关界面 / 升级完成后调用）。正在制作时不清。 */
	public void clearSelection() {
		if (this.isCrafting()) {
			return;
		}
		if (this.selected == -1 && java.util.Arrays.stream(this.preferred).allMatch(id -> id == 0)) {
			return;
		}
		this.selected = -1;
		this.selectedId = null;
		java.util.Arrays.fill(this.preferred, 0);
		this.setChanged();
	}

	/** 右键需求格：在该需求的可替换物品之间循环切换首选。 */
	public void cyclePreferred(int ingredientIndex) {
		BHR_FoolRecipe recipe = this.selectedRecipe();
		if (recipe == null || ingredientIndex < 0 || ingredientIndex >= recipe.ingredients().size()) {
			return;
		}
		List<Item> candidates = recipe.ingredients().get(ingredientIndex).candidates();
		if (candidates.size() < 2) {
			return;
		}
		int currentId = this.preferred[ingredientIndex];
		int position = -1;
		for (int i = 0; i < candidates.size(); i++) {
			if (BuiltInRegistries.ITEM.getId(candidates.get(i)) == currentId) {
				position = i;
				break;
			}
		}
		Item next = candidates.get((position + 1) % candidates.size());
		this.preferred[ingredientIndex] = BuiltInRegistries.ITEM.getId(next);
		this.setChanged();
	}

	// ── 燃料 ──

	/** 某档燃料右键选中的首选物品；没选过（或选的东西已不是候选）返回 {@code null}。 */
	public Item preferredFuelItem(int fuelIndex) {
		if (fuelIndex < 0 || fuelIndex >= this.fuelPreferred.length || this.fuelPreferred[fuelIndex] == 0) {
			return null;
		}
		FuelOption definition = this.spec().fuel(fuelIndex);
		if (definition == null) {
			return null;
		}
		Item item = BuiltInRegistries.ITEM.byId(this.fuelPreferred[fuelIndex]);
		if (item == null || item == Items.AIR || definition.indexOf(item) < 0) {
			return null;
		}
		return item;
	}

	/**
	 * 点击燃料示意槽：从背包抽走<b>1 件</b>该档燃料，折算成 tick 记在燃料槽里（超出上限直接丢弃）。
	 *
	 * <p>扣料顺序与需求九宫格同构：先扣右键选中的首选，再扣同类其它候选。
	 *
	 * @return 实际投入的件数（0 = 背包里没有该档燃料）
	 */
	public int addFuel(Player player, int fuelIndex) {
		if (this.getLevel() == null || this.getLevel().isClientSide()) {
			return 0;
		}
		WorkingSurfaceSpec spec = this.spec();
		FuelOption definition = spec == null ? null : spec.fuel(fuelIndex);
		if (definition == null) {
			return 0;
		}
		long now = this.currentGameTime();
		this.settleFuel();

		int perItem = definition.units() * spec.fuelUnitTicks();
		var inventory = player.getInventory();
		int taken = 0;
		Item preferredFuel = this.preferredFuelItem(fuelIndex);
		if (preferredFuel != null) {
			taken += consumeFuelItem(inventory, stack -> stack.is(preferredFuel), 1);
		}
		if (taken < 1) {
			taken += consumeFuelItem(inventory, definition::accepts, 1 - taken);
		}
		if (taken <= 0) {
			return 0;
		}
		int added = taken * perItem;

		if (this.isCrafting() && this.fuelMatters()) {
			if (this.pauseStart > 0) {
				// 暂停中补燃料：先把这段暂停记进累计，再把引信点上
				this.pausedTicks += now - this.pauseStart;
				this.pauseStart = 0;
				this.fuelEndTick = now + added;
			} else if (this.fuelEndTick > 0) {
				this.fuelEndTick += added;
			} else {
				this.fuelEndTick = now + added;
			}
		} else {
			this.fuelStored = Math.min(spec.fuelMaxTicks(), this.fuelStored + added);
		}
		this.setChanged();
		this.notifyStateSync();
		return taken;
	}

	/** 从背包里抽走符合条件的物品，最多 {@code limit} 件。 */
	private static int consumeFuelItem(net.minecraft.world.entity.player.Inventory inventory,
			java.util.function.Predicate<ItemStack> filter, int limit) {
		if (limit <= 0) {
			return 0;
		}
		int taken = 0;
		for (int slot = 0; slot < inventory.getContainerSize() && taken < limit; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.isEmpty() || !filter.test(stack)) {
				continue;
			}
			int amount = Math.min(limit - taken, stack.getCount());
			stack.shrink(amount);
			if (stack.isEmpty()) {
				inventory.setItem(slot, ItemStack.EMPTY);
			}
			taken += amount;
		}
		return taken;
	}

	/** 右键燃料示意槽：在该档候选之间循环切换首选消耗。 */
	public void cycleFuelPreferred(int fuelIndex) {
		FuelOption definition = this.spec().fuel(fuelIndex);
		if (definition == null || !definition.replaceable() || fuelIndex >= this.fuelPreferred.length) {
			return;
		}
		List<Item> candidates = definition.allCandidates();
		int currentId = this.fuelPreferred[fuelIndex];
		int position = -1;
		for (int i = 0; i < candidates.size(); i++) {
			if (BuiltInRegistries.ITEM.getId(candidates.get(i)) == currentId) {
				position = i;
				break;
			}
		}
		Item next = candidates.get((position + 1) % candidates.size());
		this.fuelPreferred[fuelIndex] = BuiltInRegistries.ITEM.getId(next);
		this.setChanged();
	}

	/** 当前剩余燃料（tick）：烧着的部分按时间算，攒着的部分直接给。 */
	public int fuelTicksRemaining(long now) {
		this.settleFuel();
		int burning = this.fuelEndTick > 0 ? (int) Math.max(0, this.fuelEndTick - now) : 0;
		return Math.max(0, burning + this.fuelStored);
	}

	/** 方块侧的状态钩子（篝火用它刷 {@code lit}）。 */
	private void notifyStateSync() {
		Level level = this.getLevel();
		if (level == null || level.isClientSide()) {
			return;
		}
		if (this.getBlockState().getBlock() instanceof WorkingSurfaceStateSync sync) {
			sync.onSurfaceStateChanged(level, this.worldPosition, this.getBlockState(), this);
		}
	}

	/** 随机刻 / 界面打开时的心跳：结算区间 + 让方块外观跟上。 */
	public void pulse() {
		Level level = this.getLevel();
		if (level == null || level.isClientSide()) {
			return;
		}
		long now = level.getGameTime();
		this.settleFuel();
		this.refresh(now);
		this.notifyStateSync();
	}

	// ── 制作流程 ──

	/**
	 * 开始制作：检查原料 → 扣料进暂存 → 起进度条。
	 *
	 * <p>燃料为 0 时也允许开始（进度会立刻暂停），补上燃料就接着走。
	 */
	public boolean startCraft(Player player) {
		if (this.isCrafting() || this.getLevel() == null || this.getLevel().isClientSide()) {
			return false;
		}
		BHR_FoolRecipe recipe = this.selectedRecipe();
		if (recipe == null) {
			return false;
		}
		// 等级闸门：配方栏里等级不够的配方盖了红纱，点「开始制作」也必须拦住
		if (recipe.level() > this.getWorkBenchLevel()) {
			return false;
		}
		if (!this.getItem(RESULT_SLOT).isEmpty()) {
			return false;
		}
		if (!BHR_FoolRecipes.canAfford(player, recipe, this.batches)) {
			return false;
		}
		List<Item> preferredItems = new ArrayList<>();
		for (int i = 0; i < recipe.ingredients().size(); i++) {
			ItemStack chosen = this.preferredItem(i);
			preferredItems.add(chosen.isEmpty() ? null : chosen.getItem());
		}
		List<ItemStack> taken = BHR_FoolRecipes.takeForCraft(player, recipe, this.batches, preferredItems);
		if (taken.isEmpty()) {
			return false;
		}
		this.pending.clear();
		this.pending.addAll(taken);

		long now = this.currentGameTime();
		this.settleFuel();
		this.craftStart = this.virtualNow(now);
		this.total = Math.max(1, recipe.timeTicks());
		this.remaining = this.batches;

		if (this.fuelMatters()) {
			if (this.fuelStored > 0) {
				this.fuelEndTick = now + this.fuelStored;
				this.fuelStored = 0;
			} else if (this.fuelEndTick == 0) {
				// 没燃料 → 立刻进入暂停，等玩家补燃料
				this.pauseStart = now;
			}
		}
		this.setChanged();
		this.notifyStateSync();
		return true;
	}

	/** 加速制作：吃时间水晶，每颗跳过 10 秒。 */
	public int fastCraft() {
		if (!this.isCrafting()) {
			return 0;
		}
		ItemStack crystals = this.getItem(CRYSTAL_SLOT);
		if (crystals.isEmpty() || !crystals.is(BHR_TimeCrystal.BHR_TIME_CRYSTAL)) {
			return 0;
		}
		int ticksPerCrystal = BHR_TimeCrystal.SECONDS_PER_CRYSTAL * 20;
		int left = this.total - this.getProgress();
		int needed = (left + ticksPerCrystal - 1) / ticksPerCrystal;
		int used = Math.min(needed, crystals.getCount());
		crystals.shrink(used);
		// 跳时间 = 把「本份开始刻」往回挪（虚拟时间轴上等价）
		this.craftStart -= (long) used * ticksPerCrystal;
		this.setChanged();
		this.refresh(this.currentGameTime());
		return used;
	}

	/**
	 * 这一份的产物堆：普通配方直接给产物；<b>族配方</b>按「第 {@code familyFrom} 格实际投进方块的物品」解析。
	 *
	 * <p>取的是 {@code pending}（已扣进方块、还没被消耗的原料）里第一个命中该格需求的物品 ——
	 * 也就是玩家这一次真正拿出来的那个变种（橡木 / 云杉 / 果木…），所以「门」会出对应树种的门。
	 */
	private ItemStack previewStack(BHR_FoolRecipe recipe) {
		if (!recipe.isFamily()) {
			return recipe.resultStack();
		}
		Item source = this.familySource(recipe);
		return BHR_FoolRecipes.previewResult(recipe, source);
	}

	/** 族配方的「变种决定物」：优先取已投入的原料，其次取界面里选中的首选物品。 */
	private Item familySource(BHR_FoolRecipe recipe) {
		int index = recipe.familyFrom();
		if (index < 0 || index >= recipe.ingredients().size()) {
			return null;
		}
		BHR_FoolIngredient ingredient = recipe.ingredients().get(index);
		for (ItemStack stack : this.pending) {
			if (!stack.isEmpty() && ingredient.accepts(stack)) {
				return stack.getItem();
			}
		}
		ItemStack preferred = this.preferredItem(index);
		return preferred.isEmpty() ? null : preferred.getItem();
	}

	/**
	 * 结算<b>一份</b>：普通配方把产物塞进结果栏；升级配方改方块状态的等级，做完就收工。
	 *
	 * @return 是否还要继续做下一份
	 */
	private boolean emitOneBatch() {
		Level level = this.getLevel();
		BHR_FoolRecipe recipe = this.selectedRecipe();
		if (level == null || recipe == null) {
			this.finishCraft();
			return false;
		}

		if (recipe.isUpgrade()) {
			int max = this.spec().levelMax();
			int target = Math.min(recipe.upgradeTo().orElse(this.getWorkBenchLevel()), max);
			level.setBlockAndUpdate(this.worldPosition,
					this.getBlockState().setValue(WorkingSurfaceBlock.LEVEL, target));
			this.selected = -1;
			this.selectedId = null;
			java.util.Arrays.fill(this.preferred, 0);
			this.finishCraft();
			return false;
		}

		ItemStack result = this.previewStack(recipe);
		if (result.isEmpty()) {
			// 族解析不出产物（族不存在 / 名字写错）：不吞材料，直接收工
			this.finishCraft();
			return false;
		}
		ItemStack current = this.getItem(RESULT_SLOT);
		if (current.isEmpty()) {
			this.setItem(RESULT_SLOT, result);
		} else if (ItemStack.isSameItemSameComponents(current, result)) {
			current.grow(result.getCount());
			this.setChanged();
		} else {
			// 结果栏占着别的产物：这一份吐到地上，别丢
			Block.popResource(level, this.worldPosition, result);
		}
		if (recipe.hasContainer() && recipe.container().isPresent()) {
			Block.popResource(level, this.worldPosition, new ItemStack(recipe.container().get()));
		}

		this.consumeOneBatch(recipe);

		this.remaining--;
		this.batches = Math.max(0, this.remaining);
		if (this.remaining <= 0) {
			this.finishCraft();
			return false;
		}
		return true;
	}

	/** 收工：清进度、把没烧完的引信折回「攒着的燃料」。 */
	private void finishCraft() {
		long now = this.currentGameTime();
		this.settleFuel();
		if (this.fuelEndTick > now) {
			this.fuelStored = (int) Math.min(Integer.MAX_VALUE, this.fuelStored + (this.fuelEndTick - now));
			this.fuelEndTick = 0;
		}
		this.pauseStart = 0;
		this.pausedTicks = 0;
		this.craftStart = 0;
		this.total = 0;
		this.remaining = 0;
		this.batches = 1;
		this.setChanged();
		this.notifyStateSync();
	}

	/** 从暂存原料里扣掉「一份」的量。 */
	private void consumeOneBatch(BHR_FoolRecipe recipe) {
		for (BHR_FoolIngredient ingredient : recipe.ingredients()) {
			int need = ingredient.count();
			for (java.util.Iterator<ItemStack> iterator = this.pending.iterator(); iterator.hasNext() && need > 0; ) {
				ItemStack stack = iterator.next();
				if (!ingredient.accepts(stack)) {
					continue;
				}
				int amount = Math.min(need, stack.getCount());
				stack.shrink(amount);
				need -= amount;
				if (stack.isEmpty()) {
					iterator.remove();
				}
			}
		}
		this.setChanged();
	}

	/** 方块被移除时把东西吐出来：结果栏产物 + 暂存但没消耗掉的原料。 */
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		Level level = this.getLevel();
		if (level == null || level.isClientSide()) {
			return;
		}
		this.refresh(level.getGameTime());
		ItemStack result = this.getItem(RESULT_SLOT);
		if (!result.isEmpty()) {
			Block.popResource(level, pos, result);
			this.setItem(RESULT_SLOT, ItemStack.EMPTY);
		}
		for (ItemStack stack : this.pending) {
			if (!stack.isEmpty()) {
				Block.popResource(level, pos, stack.copy());
			}
		}
		this.pending.clear();
	}

	// ── Container ──

	@Override
	public int getContainerSize() {
		return CONTAINER_SIZE;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : this.items) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return this.items.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack stack = ContainerHelper.removeItem(this.items, slot, amount);
		if (!stack.isEmpty()) {
			this.setChanged();
		}
		return stack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(this.items, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		this.items.set(slot, stack);
		stack.limitSize(this.getMaxStackSize(stack));
		this.setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player);
	}

	@Override
	public void clearContent() {
		this.items.clear();
		this.setChanged();
	}

	@Override
	public void setChanged() {
		super.setChanged();
		Level level = this.getLevel();
		if (level != null) {
			level.blockEntityChanged(this.worldPosition);
		}
	}

	// ── ContainerData ──

	@Override
	public int get(int index) {
		return switch (index) {
			case DATA_SELECTED -> this.selected;
			case DATA_BATCHES -> this.batches;
			case DATA_PROGRESS -> this.getProgress();
			case DATA_TOTAL -> this.total;
			case DATA_LEVEL -> this.getWorkBenchLevel();
			case DATA_EXPERT -> this.isExpertRecipes() ? 1 : 0;
			case DATA_SCROLL -> this.scroll;
			case DATA_REMAINING -> this.remaining;
			case DATA_FUEL -> this.fuelTicksRemaining(this.currentGameTime());
			default -> {
				if (index >= DATA_FUEL_PREF_START && index < this.getCount()) {
					yield this.fuelPreferred[index - DATA_FUEL_PREF_START];
				}
				if (index >= DATA_PREF_START && index < DATA_PREF_START + DATA_PREF_COUNT) {
					yield this.preferred[index - DATA_PREF_START];
				}
				yield 0;
			}
		};
	}

	@Override
	public void set(int index, int value) {
		switch (index) {
			case DATA_SELECTED -> this.selected = value;
			case DATA_BATCHES -> this.batches = value;
			case DATA_TOTAL -> this.total = value;
			case DATA_SCROLL -> this.scroll = value;
			case DATA_REMAINING -> this.remaining = value;
			default -> {
				// 等级 / 专家开关 / 燃料量都是只读的派生值
			}
		}
	}

	// ── 存盘 ──

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items);
		output.putString("recipe", this.selectedId == null ? "" : this.selectedId.toString());
		output.putInt("batches", this.batches);
		output.putLong("craft_start", this.craftStart);
		output.putInt("total", this.total);
		output.putInt("scroll", this.scroll);
		output.putInt("remaining", this.remaining);
		output.putIntArray("preferred", this.preferred);
		output.putLong("paused_ticks", this.pausedTicks);
		output.putLong("pause_start", this.pauseStart);
		output.putLong("fuel_end", this.fuelEndTick);
		output.putInt("fuel_stored", this.fuelStored);
		output.putIntArray("fuel_pref", this.fuelPreferred);
		var pendingList = output.list("pending", ItemStack.CODEC);
		for (ItemStack stack : this.pending) {
			pendingList.add(stack);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		ContainerHelper.loadAllItems(input, this.items);
		this.batches = Math.max(1, input.getIntOr("batches", 1));
		this.craftStart = input.getLongOr("craft_start", 0L);
		this.total = input.getIntOr("total", 0);
		this.scroll = Math.max(0, input.getIntOr("scroll", 0));
		this.remaining = Math.max(0, input.getIntOr("remaining", 0));
		this.pausedTicks = input.getLongOr("paused_ticks", 0L);
		this.pauseStart = input.getLongOr("pause_start", 0L);
		this.fuelEndTick = input.getLongOr("fuel_end", 0L);
		this.fuelStored = Math.max(0, input.getIntOr("fuel_stored", 0));
		this.pending.clear();
		input.listOrEmpty("pending", ItemStack.CODEC).forEach(this.pending::add);
		int[] savedPreferred = input.getIntArray("preferred").orElse(new int[DATA_PREF_COUNT]);
		for (int i = 0; i < Math.min(savedPreferred.length, this.preferred.length); i++) {
			this.preferred[i] = savedPreferred[i];
		}
		int[] savedFuel = input.getIntArray("fuel_pref").orElse(new int[this.fuelPreferred.length]);
		for (int i = 0; i < Math.min(savedFuel.length, this.fuelPreferred.length); i++) {
			this.fuelPreferred[i] = savedFuel[i];
		}
		String recipeId = input.getStringOr("recipe", "");
		this.selected = -1;
		this.selectedId = null;
		if (!recipeId.isEmpty()) {
			Identifier parsed = Identifier.tryParse(recipeId);
			if (parsed != null) {
				this.selectedId = parsed;
				for (int i = 0; i < BHR_FoolRecipes.all().size(); i++) {
					if (BHR_FoolRecipes.all().get(i).id().equals(parsed)) {
						this.selected = i;
						break;
					}
				}
			}
		}
	}
}
