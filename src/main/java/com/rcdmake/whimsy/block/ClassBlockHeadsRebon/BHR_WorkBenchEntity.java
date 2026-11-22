package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_GameRules;
import com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon.BHR_FoolIngredient;
import com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon.BHR_FoolRecipe;
import com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon.BHR_FoolRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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

import java.util.List;
import java.util.Set;

/**
 * 制作台的方块实体 —— 现在它是<b>真有状态</b>的：界面里的水晶格、结果格、选中的配方、
 * 倍率、进度全都存在这里，并且随区块存档。
 *
 * <h2>两个真实容器槽</h2>
 * <ol>
 *   <li>{@code 0} = 时间水晶放置处（{@link #CRYSTAL_SLOT}）；</li>
 *   <li>{@code 1} = 结果栏（{@link #RESULT_SLOT}，只出不进）。</li>
 * </ol>
 * 九宫格需求、配方栏、升级栏都只是<b>显示</b>，不占容器槽。
 *
 * <h2>同步：{@link ContainerData}</h2>
 * <p>菜单里注册 {@code addDataSlots(this)}，于是这七个整数每 tick 自动同步给客户端：
 * 选中配方下标、倍率、进度、总时长、制作台等级、专家配方开关、滚动偏移。
 * 客户端据此自己重建配方列表（两端读的是同一份模组数据），所以不需要自定义发包。
 *
 * <h2>制作推进</h2>
 * <p>由 {@code BHR_WorkBench#getTicker} 每 tick 调 {@link #tick}：进度到顶就结算 ——
 * 升级配方直接改方块状态的 {@code level}，普通配方把产物放进结果栏。
 */
public class BHR_WorkBenchEntity extends BlockEntity implements Container, ContainerData {

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
	/** 9 个需求格的首选物品（注册表数字 id，0 = 没指定）。 */
	public static final int DATA_PREF_START = 8;
	public static final int DATA_PREF_COUNT = 9;
	public static final int DATA_COUNT = DATA_PREF_START + DATA_PREF_COUNT;

	/** 菜单里最多显示 8 条配方。 */
	public static final int RECIPE_SLOTS = 8;
	/** 倍率上限。 */
	public static final int MAX_BATCHES = 64;

	/** 方块实体类型，在 {@link #OnInit} 里赋值。 */
	public static BlockEntityType<BHR_WorkBenchEntity> BHR_WORK_BENCH_ENTITY;

	private final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

	/** 选中配方在 {@link BHR_FoolRecipes#all()} 里的下标；-1 = 没选（客户端与服务器都用它对齐）。 */
	private int selected = -1;
	/** 一次制作几份。 */
	private int batches = 1;
	/** 当前这一份的「开始游戏刻」。进度 = 当前游戏刻 - 它（不再逐 tick 累加）。 */
	private long craftStart;
	/** 本次制作总 tick（0 = 没在制作）。 */
	private int total;
	/** 配方栏的滚动偏移（0 起，最多 all().size() - RECIPE_SLOTS）。 */
	private int scroll;
	/** 本次制作还要做几份（逐份推进：每完成一份 -1，到 0 收工）。 */
	private int remaining;
	/** 9 个需求格的首选物品（注册表数字 id，0 = 用默认的第一候选）。 */
	private final int[] preferred = new int[DATA_PREF_COUNT];
	/** 已投入但还没被某一份消耗掉的原料（暂存在方块里；打破方块会原样退还）。 */
	private final java.util.List<ItemStack> pending = new java.util.ArrayList<>();

	public BHR_WorkBenchEntity(BlockPos pos, BlockState state) {
		super(BHR_WORK_BENCH_ENTITY, pos, state);
	}

	/** 注册方块实体类型。 */
	public static void OnInit(Block block) {
		BHR_WORK_BENCH_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
				ModRegister.id(BHR_WorkBench.REGISTRY_NAME),
				new BlockEntityType<>(BHR_WorkBenchEntity::new, Set.of(block)));
	}

	// ── 制作状态 ──

	/** 是否正在制作。 */
	public boolean isCrafting() {
		return this.total > 0;
	}

	public int getSelected() {
		return this.selected;
	}

	public BHR_FoolRecipe selectedRecipe() {
		if (this.selected < 0 || this.selected >= BHR_FoolRecipes.all().size()) {
			return null;
		}
		return BHR_FoolRecipes.all().get(this.selected).recipe();
	}

	/** 当前等级（方块状态的 level）。 */
	public int getWorkBenchLevel() {
		return this.getBlockState().getValue(BHR_WorkBench.LEVEL);
	}

	/** 当前是否使用专家配方（游戏规则；只有服务端拿得到规则值）。 */
	public boolean isExpertRecipes() {
		Level level = this.getLevel();
		return level instanceof net.minecraft.server.level.ServerLevel serverLevel
				&& serverLevel.getGameRules().get(BHR_GameRules.BHR_EXPERT_RECIPES);
	}

	/** 配方栏要显示的配方（同一台机器 + 当前专家挡位），顺序与 {@link BHR_FoolRecipes#all()} 一致。 */
	public List<BHR_FoolRecipes.Entry> visibleEntries() {
		boolean expert = this.isExpertRecipes();
		List<BHR_FoolRecipes.Entry> result = new java.util.ArrayList<>();
		for (BHR_FoolRecipes.Entry entry : BHR_FoolRecipes.forWorkbench(BHR_WorkBench.REGISTRY_NAME)) {
			// 升级配方只出现在升级栏，不重复占配方栏的格子
			if (entry.recipe().expert() == expert && !entry.recipe().isUpgrade()) {
				result.add(entry);
			}
		}
		return result;
	}

	/** 当前等级在 {@link #visibleEntries()} 里能用的下一条升级配方（找升级目标最小的）。 */
	public BHR_FoolRecipes.Entry nextUpgrade() {
		int level = this.getWorkBenchLevel();
		BHR_FoolRecipes.Entry best = null;
		// ⚠️ 不能用 visibleEntries()：它已经把升级配方排除（配方栏不显示它们），
		// 而升级栏恰恰要找这些配方，所以这里从「本台机器的全部配方」里筛。
		for (BHR_FoolRecipes.Entry entry : BHR_FoolRecipes.forWorkbench(BHR_WorkBench.REGISTRY_NAME)) {
			BHR_FoolRecipe recipe = entry.recipe();
			if (recipe.expert() != this.isExpertRecipes()) {
				continue;
			}
			if (!recipe.isUpgrade() || recipe.upgradeTo().isEmpty()) {
				continue;
			}
			int target = recipe.upgradeTo().get();
			if (target <= level) {
				continue;
			}
			if (best == null || target < best.recipe().upgradeTo().get()) {
				best = entry;
			}
		}
		return best;
	}

	public int getBatches() {
		return this.batches;
	}

	/** 当前进度（tick）：现算，不是累加值。 */
	public int getProgress() {
		return this.craftProgress(this.currentGameTime());
	}

	private long currentGameTime() {
		Level level = this.getLevel();
		return level == null ? 0L : level.getGameTime();
	}

	/**
	 * 按「当前游戏刻 - 本份开始刻」算出进度，并把已经走完的份数一次性结算掉。
	 *
	 * <p>调用点：打开界面时、界面打开期间每 tick（菜单 broadcastChanges）、方块被破坏时。
	 * 没有调用者时方块什么都不做 —— 但仍能算出正确进度，因为时间在流逝。
	 */
	public void refresh(long now) {
		if (this.total <= 0) {
			return;
		}
		if (now < this.craftStart) {
			// /time set 把游戏刻调回去了：视为重新开始这一份（进度归零）
			this.craftStart = now;
			this.setChanged();
			return;
		}
		long elapsed = now - this.craftStart;
		int guard = 0;
		while (this.total > 0 && elapsed >= this.total && guard++ < 512) {
			elapsed -= this.total;
			boolean continues = this.emitOneBatch();
			if (continues) {
				this.craftStart += this.total;
			} else {
				this.craftStart = now;
				break;
			}
		}
		this.setChanged();
	}

	/** 只算进度，不结算（给同步/绘制用）。 */
	private int craftProgress(long now) {
		if (this.total <= 0) {
			return 0;
		}
		if (now < this.craftStart) {
			return 0;
		}
		return (int) Math.min(this.total, now - this.craftStart);
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

	/** 选中配方（传的是 {@link BHR_FoolRecipes#all()} 的下标）。 */
	public void select(int index) {
		if (this.isCrafting()) {
			return;
		}
		// 换配方 = 需求格整套换掉，上一份配方留下的「首选消耗物品」必须清空，
		// 否则那一格会显示一个当前配方根本不接受的物品（实测踩过）。
		if (index != this.selected) {
			java.util.Arrays.fill(this.preferred, 0);
		}
		this.selected = index;
		// 升级配方只能做一份（倍增对它没有意义）
		if (this.selectedRecipe() != null && this.selectedRecipe().isUpgrade()) {
			this.batches = 1;
		}
		this.setChanged();
	}

	public void setBatches(int batches) {
		if (this.isCrafting()) {
			return;
		}
		// 升级配方锁定 1 份：界面上的 +/- 对它无效
		if (this.selectedRecipe() != null && this.selectedRecipe().isUpgrade()) {
			this.batches = 1;
			this.setChanged();
			return;
		}
		this.batches = Math.clamp(batches, 1, MAX_BATCHES);
		this.setChanged();
	}

	/** 某个需求格当前的首选物品；没有指定时返回 {@code null}（用默认第一候选）。 */
	public ItemStack preferredItem(int ingredientIndex) {
		if (ingredientIndex < 0 || ingredientIndex >= this.preferred.length || this.preferred[ingredientIndex] == 0) {
			return ItemStack.EMPTY;
		}
		Item item = BuiltInRegistries.ITEM.byId(this.preferred[ingredientIndex]);
		return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
	}

	/**
	 * 取消选中的配方（关界面时 / 升级完成后调用）。
	 *
	 * <p>⚠️ 正在制作时不清：进度条需要一个对应的配方才能继续结算。
	 * <p>滚动位置（{@code scroll}）与倍率（{@code batches}）刻意保留 —— 重新打开时列表还停在原处。
	 */
	public void clearSelection() {
		if (this.isCrafting()) {
			return;
		}
		if (this.selected == -1 && java.util.Arrays.stream(this.preferred).allMatch(id -> id == 0)) {
			return;
		}
		this.selected = -1;
		java.util.Arrays.fill(this.preferred, 0);
		this.setChanged();
	}

	/** 右键需求格：在该需求的可替换物品之间循环切换首选。 */
	public void cyclePreferred(int ingredientIndex) {
		BHR_FoolRecipe recipe = this.selectedRecipe();
		if (recipe == null || ingredientIndex < 0 || ingredientIndex >= recipe.ingredients().size()) {
			return;
		}
		var candidates = recipe.ingredients().get(ingredientIndex).candidates();
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

	/**
	 * 开始制作：检查原料 → 扣料 → 进进度条。
	 *
	 * @return 是否成功开始
	 */
	public boolean startCraft(Player player) {
		if (this.isCrafting() || this.getLevel() == null || this.getLevel().isClientSide()) {
			return false;
		}
		BHR_FoolRecipe recipe = this.selectedRecipe();
		if (recipe == null) {
			return false;
		}
		// 结果栏必须先空着，否则产物没地方放
		if (!this.getItem(RESULT_SLOT).isEmpty()) {
			return false;
		}
		if (!BHR_FoolRecipes.canAfford(player, recipe, this.batches)) {
			return false;
		}
		java.util.List<Item> preferredItems = new java.util.ArrayList<>();
		for (int i = 0; i < recipe.ingredients().size(); i++) {
			ItemStack chosen = this.preferredItem(i);
			preferredItems.add(chosen.isEmpty() ? null : chosen.getItem());
		}
		java.util.List<ItemStack> taken = BHR_FoolRecipes.takeForCraft(player, recipe, this.batches, preferredItems);
		if (taken.isEmpty()) {
			return false;
		}
		// 原料不直接消失，而是先「放进制作台」，由每一份完成后逐份扣掉
		this.pending.clear();
		this.pending.addAll(taken);
		// 一份一份走：total 永远是「一份」的时长，remaining 记还要做几份。
		// 进度不再逐 tick 累加，改为记录这一份的开始游戏刻。
		this.remaining = this.batches;
		this.total = Math.max(1, recipe.timeTicks());
		this.craftStart = this.currentGameTime();
		this.setChanged();
		return true;
	}

	/**
	 * 加速制作：吃水晶，每颗跳过 10 秒。
	 *
	 * @return 实际吃掉的水晶数（0 = 没吃）
	 */
	public int fastCraft() {
		if (!this.isCrafting()) {
			return 0;
		}
		ItemStack crystals = this.getItem(CRYSTAL_SLOT);
		if (crystals.isEmpty() || !crystals.is(com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_TimeCrystal.BHR_TIME_CRYSTAL)) {
			return 0;
		}
		int ticksPerCrystal = com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_TimeCrystal.SECONDS_PER_CRYSTAL * 20;
		int left = this.total - this.getProgress();
		int needed = (left + ticksPerCrystal - 1) / ticksPerCrystal;
		int used = Math.min(needed, crystals.getCount());
		crystals.shrink(used);
		// 跳时间 = 把「本份开始刻」往前挪
		this.craftStart -= (long) used * ticksPerCrystal;
		this.setChanged();
		return used;
	}

	/**
	 * 结算<b>一份</b>：普通配方把「一份」的产物塞进结果栏，剩余份数 -1；
	 * 升级配方改方块状态的等级，做完就收工（升级永远只做一次）。
	 */
	private boolean emitOneBatch() {
		Level level = this.getLevel();
		BHR_FoolRecipe recipe = this.selectedRecipe();
		if (level == null || recipe == null) {
			this.total = 0;
			this.remaining = 0;
			return false;
		}

		if (recipe.isUpgrade()) {
			int target = Math.min(recipe.upgradeTo().orElse(this.getWorkBenchLevel()), BHR_WorkBench.LEVEL_MAX);
			level.setBlockAndUpdate(this.worldPosition,
					this.getBlockState().setValue(BHR_WorkBench.LEVEL, target));
			this.total = 0;
			this.remaining = 0;
			// 升级完成就取消选中，避免同一台机器被重复升级
			this.selected = -1;
			java.util.Arrays.fill(this.preferred, 0);
			return false;
		}

		// 一份 = resultCount 个（如梯子一份 10 个）
		ItemStack result = recipe.resultStack();
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

		this.consumeOneBatch(recipe);

		this.remaining--;
		// 界面上显示的就是「还剩几份」，所以倍率数字跟着一起减；
		// 收工时回到 1，下次制作从 1 份开始。
		this.batches = Math.max(0, this.remaining);
		if (this.remaining <= 0) {
			this.remaining = 0;
			this.batches = 1;
			this.total = 0;
			return false;
		}
		return true;
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

	/**
	 * 方块被移除时把东西吐出来：结果栏里的产物 + 暂存但没消耗掉的原料。
	 * <p>时间水晶由菜单的 {@code removed(...)} 负责退回，这里不重复处理。
	 */
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		Level level = this.getLevel();
		if (level == null || level.isClientSide()) {
			return;
		}
		// 先把「按游戏刻该走完的进度」补算掉，再吐东西
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
			default -> index >= DATA_PREF_START && index < DATA_COUNT ? this.preferred[index - DATA_PREF_START] : 0;
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
				// 等级 / 专家开关是只读的派生值
			}
		}
	}

	@Override
	public int getCount() {
		return DATA_COUNT;
	}

	// ── 存盘 ──

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.items);
		output.putString("recipe", this.selected >= 0 && this.selected < BHR_FoolRecipes.all().size()
				? BHR_FoolRecipes.all().get(this.selected).id().toString()
				: "");
		output.putInt("batches", this.batches);
		output.putLong("craft_start", this.craftStart);
		output.putInt("total", this.total);
		output.putInt("scroll", this.scroll);
		output.putInt("remaining", this.remaining);
		output.putIntArray("preferred", this.preferred);
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
		this.pending.clear();
		input.listOrEmpty("pending", ItemStack.CODEC).forEach(this.pending::add);
		int[] savedPreferred = input.getIntArray("preferred").orElse(new int[DATA_PREF_COUNT]);
		for (int i = 0; i < Math.min(savedPreferred.length, this.preferred.length); i++) {
			this.preferred[i] = savedPreferred[i];
		}
		String recipeId = input.getStringOr("recipe", "");
		this.selected = -1;
		if (!recipeId.isEmpty()) {
			Identifier parsed = Identifier.tryParse(recipeId);
			if (parsed != null) {
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
