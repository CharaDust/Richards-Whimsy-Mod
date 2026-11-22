package com.rcdmake.whimsy.block.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
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

import java.util.List;
import java.util.Set;

/**
 * 简易篝火的方块实体 —— 在制作台那套状态之上，多了<b>燃料值</b>这一维。
 *
 * <h2>与 {@link BHR_WorkBenchEntity} 的两处关键差别</h2>
 * <ol>
 *   <li><b>进度逐 tick 累加</b>，不再用「当前游戏刻 - 本份开始刻」现算。
 *       原因：燃料耗尽要能<b>暂停</b>（需求确认：暂停、补燃料后继续），
 *       而「时间差」模型里游戏刻一直在走，暂停无从表达。所以本方块注册了 ticker
 *       （见 {@link BHR_Campfire#getTicker}），只在「正在制作 且 燃料 > 0」时
 *       {@code progress++} 与 {@code fuel--}。
 *       <b>例外：升级配方不烧燃料</b>（见 {@link #isFuelFreeCraft()}）—— 它改的是方块自己的等级，
 *       与「烧东西」无关，所以火灭了也照样走完。</li>
 *   <li><b>燃料不是物品槽，而是一个整数</b>。界面左侧那三个凹槽只是「这台机器收哪三种燃料」的
 *       <b>示意</b>，点一下就从玩家背包抽走该物品并折算成燃料值，物品本身不会存进方块里
 *       （见 {@link #addFuel}）。计价单位是<b>单位燃烧时间</b>：本机 1 单位 = 15 秒，
 *       木棍 1 单位 / 松果 2 单位 / 木板 5 单位，上限 10 个单位
 *       （{@link BHR_Campfire#UNIT_BURN_TICKS} / {@link BHR_Campfire#MAX_FUEL_TICKS}）。
 *       这些值由<b>本方块</b>决定，与物品自带的燃料属性无关。</li>
 * </ol>
 *
 * <h2>两个真实容器槽</h2>
 * <ol>
 *   <li>{@code 0} = 时间水晶（{@link #CRYSTAL_SLOT}）；</li>
 *   <li>{@code 1} = 结果栏（{@link #RESULT_SLOT}，只出不进）。</li>
 * </ol>
 * 需求九宫格、配方栏、燃料示意全是<b>显示</b>，不占容器槽。
 *
 * <h2>同步</h2>
 * <p>实现 {@link ContainerData}，菜单里 {@code addDataSlots(this)} 后这些整数每 tick 推给客户端：
 * 选中配方、倍率、进度、总时长、等级（1~2）、专家开关、滚动偏移、剩余份数、<b>剩余燃料</b>、
 * 9 个需求格的首选物品。
 */
public class BHR_CampfireEntity extends BlockEntity implements Container, ContainerData {

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
	/** 剩余燃料（tick）。0 = 火已灭，进度会暂停。上限见 {@link BHR_Campfire#MAX_FUEL_TICKS}。 */
	public static final int DATA_FUEL = 8;
	/** 9 个需求格的首选物品（注册表数字 id，0 = 没指定）。 */
	public static final int DATA_PREF_START = 9;
	public static final int DATA_PREF_COUNT = 9;
	/** 3 个燃料档位各自的「首选消耗物品」（注册表数字 id，0 = 用该档第一个候选）。 */
	public static final int DATA_FUEL_PREF_START = DATA_PREF_START + DATA_PREF_COUNT;    // 18
	public static final int DATA_FUEL_PREF_COUNT = BHR_Campfire.FUEL_COUNT;
	public static final int DATA_COUNT = DATA_FUEL_PREF_START + DATA_FUEL_PREF_COUNT;    // 21

	/** 菜单里最多显示 8 条配方。 */
	public static final int RECIPE_SLOTS = 8;
	/** 倍率上限。 */
	public static final int MAX_BATCHES = 64;

	/** 方块实体类型，在 {@link #OnInit} 里赋值。 */
	public static BlockEntityType<BHR_CampfireEntity> BHR_CAMPFIRE_ENTITY;

	private final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

	/** 选中配方在 {@link BHR_FoolRecipes#all()} 里的下标；-1 = 没选。 */
	private int selected = -1;
	/** 一次制作几份。 */
	private int batches = 1;
	/** 本份已走 tick（0..total）。 */
	private int progress;
	/** 本份总 tick（0 = 没在制作）。 */
	private int total;
	/** 还要做几份。 */
	private int remaining;
	/** 剩余燃料 tick。 */
	private int fuel;
	/** 配方栏滚动偏移。 */
	private int scroll;
	/** 9 个需求格的首选物品（注册表数字 id，0 = 默认第一候选）。 */
	private final int[] preferred = new int[DATA_PREF_COUNT];
	/** 3 个燃料档位的首选消耗物品（注册表数字 id，0 = 该档第一个候选）。 */
	private final int[] fuelPreferred = new int[DATA_FUEL_PREF_COUNT];
	/** 已投入但还没被某一份消耗掉的原料（打破方块会原样退还）。 */
	private final java.util.List<ItemStack> pending = new java.util.ArrayList<>();

	public BHR_CampfireEntity(BlockPos pos, BlockState state) {
		super(BHR_CAMPFIRE_ENTITY, pos, state);
	}

	/** 注册方块实体类型。 */
	public static void OnInit(Block block) {
		BHR_CAMPFIRE_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
				ModRegister.id(BHR_Campfire.REGISTRY_NAME),
				new BlockEntityType<>(BHR_CampfireEntity::new, Set.of(block)));
	}

	// ── 每 tick（服务端）──

	/**
	 * ticker 的入口：推进一份制作，并维护 {@code lit} 方块状态。
	 *
	 * <p>普通配方：只有「正在制作 且 燃料 &gt; 0」才同时消耗燃料与推进进度 —— 这就是「燃料不足则暂停」。
	 * 暂停时原料仍暂存在方块里，补燃料后原样续跑。
	 *
	 * <p><b>升级配方是例外：不烧燃料、也不需要燃料</b>（需求：「燃料制作台的升级不需要消耗燃料」）。
	 * 所以哪怕火早灭了，升级照样一分钟一分钟往下走；进度条之外唯一的变化就是
	 * {@link #isFuelFreeCraft()} 为真时不碰 {@link #fuel}。
	 */
	public void serverTick() {
		boolean changed = false;
		if (this.total > 0 && this.remaining > 0) {
			boolean fuelFree = this.isFuelFreeCraft();
			if (fuelFree || this.fuel > 0) {
				if (!fuelFree) {
					this.fuel--;
				}
				this.progress++;
				changed = true;
				// 一份可能在这一 tick 就收尾；用 while 兜住「total 很小 + 一次补多 tick」的极端情况
				int guard = 0;
				while (this.total > 0 && this.progress >= this.total && guard++ < 64) {
					this.progress -= this.total;
					this.emitOneBatch();
				}
			}
		}
		// 不管这一 tick 有没有推进，都让 lit 状态跟上：
		// 燃料是玩家点出来的，界面外也可能变（比如超时熄灭），这类跃迁只能由 ticker 兜住。
		this.syncLit();
		if (changed) {
			this.setChanged();
		}
	}

	/**
	 * 当前这一份是不是「不烧燃料」的活：目前只有升级配方。
	 *
	 * <p>为什么升级不该烧燃料：升级改的是<b>方块自己的等级</b>，不是「把东西做出来」——
	 * 需求明确要求它不受燃料档位影响（普通模式也没有升级配方，专家模式才是最贵的那一条 180 秒）。
	 */
	public boolean isFuelFreeCraft() {
		BHR_FoolRecipe recipe = this.selectedRecipe();
		return recipe != null && recipe.isUpgrade();
	}

	/**
	 * 把 {@code lit} 状态刷成「燃料是否 &gt; {@link BHR_Campfire#LIT_MIN_FUEL_TICKS}」。
	 *
	 * <p>判定依据是<b>燃料存量</b>而不是「是否正在制作」（需求原文）：燃料 &gt; 2 单位用燃烧图、
	 * 否则用熄灭图。只在翻转时写世界，避免每 tick 刷方块更新。
	 */
	private void syncLit() {
		Level level = this.getLevel();
		if (level == null || level.isClientSide()) {
			return;
		}
		boolean shouldLight = this.fuel > BHR_Campfire.LIT_MIN_FUEL_TICKS;
		if (this.getBlockState().getValue(BHR_Campfire.LIT) != shouldLight) {
			level.setBlockAndUpdate(this.worldPosition,
					this.getBlockState().setValue(BHR_Campfire.LIT, shouldLight));
		}
	}

	// ── 制作状态 ──

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

	/** 当前等级（方块状态的 {@code level}，1~2 级）。 */
	public int getWorkBenchLevel() {
		return this.getBlockState().getValue(BHR_Campfire.LEVEL);
	}

	public boolean isExpertRecipes() {
		Level level = this.getLevel();
		return level instanceof net.minecraft.server.level.ServerLevel serverLevel
				&& serverLevel.getGameRules().get(BHR_GameRules.BHR_EXPERT_RECIPES);
	}

	/** 配方栏要显示的配方（本机 + 当前专家挡位；升级配方不占配方栏）。
	 * <p>等级<b>不过滤</b>列表：等级不够的配方照样显示，界面给它盖一层红纱并显示「需要等级」（与制作台一致）。
	 */
	public List<BHR_FoolRecipes.Entry> visibleEntries() {
		boolean expert = this.isExpertRecipes();
		List<BHR_FoolRecipes.Entry> result = new java.util.ArrayList<>();
		for (BHR_FoolRecipes.Entry entry : BHR_FoolRecipes.forWorkbench(BHR_Campfire.REGISTRY_NAME)) {
			if (entry.recipe().expert() == expert && !entry.recipe().isUpgrade()) {
				result.add(entry);
			}
		}
		return result;
	}

	/**
	 * 当前等级在升级栏里能用的下一条升级配方（找升级目标最小的）。
	 *
	 * <p>与制作台同一套：普通模式没有升级配方（解包数据里就一条都不写），所以这里返回 {@code null}、
	 * 升级栏空着；专家模式返回「升到 Lv.2」那一条。
	 * <p>⚠️ 不能用 {@link #visibleEntries()}：它已经把升级配方排除（配方栏不显示它们），
	 * 而升级栏恰恰要找这些配方，所以这里从「本台机器的全部配方」里筛。
	 */
	public BHR_FoolRecipes.Entry nextUpgrade() {
		int level = this.getWorkBenchLevel();
		BHR_FoolRecipes.Entry best = null;
		for (BHR_FoolRecipes.Entry entry : BHR_FoolRecipes.forWorkbench(BHR_Campfire.REGISTRY_NAME)) {
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

	public int getProgress() {
		return this.progress;
	}

	public int getTotal() {
		return this.total;
	}

	public int getFuel() {
		return this.fuel;
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

	/** 某个需求格当前的首选物品；没有指定时返回空堆（用默认第一候选）。 */
	public ItemStack preferredItem(int ingredientIndex) {
		if (ingredientIndex < 0 || ingredientIndex >= this.preferred.length || this.preferred[ingredientIndex] == 0) {
			return ItemStack.EMPTY;
		}
		Item item = BuiltInRegistries.ITEM.byId(this.preferred[ingredientIndex]);
		return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
	}

	/** 取消选中（关界面时调用；制作中不清）。 */
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

	// ── 燃料 ──

	/**
	 * 点击界面上的燃料示意槽：从玩家背包抽走该档燃料，按<b>单位燃烧时间</b>折算成燃料值。
	 *
	 * <p>「抽取」的语义与需求一致：物品<b>不会</b>存进方块（凹槽始终显示该档燃料的图标），
	 * 而是直接消耗掉、换成 tick 记在 {@link #fuel} 上。
	 * <p>换算：一件 = {@link BHR_Campfire.Fuel#units()} 个单位，1 个单位 =
	 * {@link BHR_Campfire#UNIT_BURN_TICKS}（本机 15 秒）。燃料槽最高存
	 * {@link BHR_Campfire#MAX_FUEL_TICKS}（10 个单位）—— <b>超过上限的部分直接丢弃</b>。
	 * <p>一次只投 1 件（需求：燃料只支持左键添加，不支持 Shift 整栈投入 —— 整栈必然溢出）。
	 * <p>扣料顺序与需求九宫格同构：<b>先扣右键选中的首选</b>，再扣同类其它候选。
	 *
	 * @param fuelIndex 档位下标（对应 {@link BHR_Campfire#fuels()}）
	 * @return 实际投入的件数（0 = 背包里没有该档燃料）
	 */
	public int addFuel(Player player, int fuelIndex) {
		if (this.getLevel() == null || this.getLevel().isClientSide()) {
			return 0;
		}
		BHR_Campfire.Fuel definition = BHR_Campfire.fuel(fuelIndex);
		if (definition == null) {
			return 0;
		}
		int perItem = definition.ticks();

		var inventory = player.getInventory();
		int limit = 1;
		int taken = 0;

		// 第一趟：右键选中的首选消耗物品
		Item preferredItem = this.preferredFuelItem(fuelIndex);
		if (preferredItem != null) {
			taken += consumeFuelItem(inventory, stack -> stack.is(preferredItem), limit);
		}
		// 第二趟：该档的其它候选（例如选了云杉木板之后剩下的其它木板）
		if (taken < limit) {
			taken += consumeFuelItem(inventory, definition::accepts, limit - taken);
		}
		if (taken <= 0) {
			return 0;
		}
		// 溢出的部分丢弃：先按满额累加，再夹到上限
		this.fuel = Math.min(BHR_Campfire.MAX_FUEL_TICKS, this.fuel + taken * perItem);
		this.setChanged();
		this.syncLit();
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

	/** 某档燃料右键选中的首选物品；没选过（或选的东西已不是候选）返回 {@code null}。 */
	public Item preferredFuelItem(int fuelIndex) {
		if (fuelIndex < 0 || fuelIndex >= this.fuelPreferred.length || this.fuelPreferred[fuelIndex] == 0) {
			return null;
		}
		BHR_Campfire.Fuel definition = BHR_Campfire.fuel(fuelIndex);
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
	 * 右键燃料示意槽：在该档的候选物品之间循环切换首选消耗。
	 *
	 * <p>与需求九宫格的 {@link #cyclePreferred} 是同一套交互：只有「可替换」的档位（候选 &gt; 1，
	 * 目前是木板那一档）会真的切换，单候选的档位（木棍 / 松果）什么都不做。
	 *
	 * <p>⚠️ 轮换用的是 {@link BHR_Campfire.Fuel#allCandidates()}（显式候选 + 标签展开），
	 * 不是 {@code candidates()}：只用显式清单的话，果木木板这类「靠 {@code #minecraft:planks}
	 * 入档」的物品能烧却轮不到，与需求九宫格的行为不对称（2026-11 修正）。
	 */
	public void cycleFuelPreferred(int fuelIndex) {
		BHR_Campfire.Fuel definition = BHR_Campfire.fuel(fuelIndex);
		if (definition == null || !definition.replaceable() || fuelIndex >= this.fuelPreferred.length) {
			return;
		}
		java.util.List<Item> candidates = definition.allCandidates();
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

	// ── 制作流程 ──

	/**
	 * 开始制作：检查原料 → 扣料进暂存 → 起进度条。
	 *
	 * <p>燃料为 0 时也允许开始（进度会立刻暂停），因为需求选择的是「暂停后补燃料继续」，
	 * 而不是「无燃料不许开始」。
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
		// （制作台那边只画红纱、不拦开始，这里刻意补上：篝火的 2 级配方就是升级的奖励）
		if (recipe.level() > this.getWorkBenchLevel()) {
			return false;
		}
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
		this.pending.clear();
		this.pending.addAll(taken);
		this.remaining = this.batches;
		this.total = Math.max(1, recipe.timeTicks());
		this.progress = 0;
		this.setChanged();
		this.syncLit();
		return true;
	}

	/**
	 * 加速制作：吃时间水晶，每颗跳过 10 秒。
	 *
	 * <p>与制作台的区别：那边是「把本份开始刻往前挪」，这边直接把 tick 加进 {@link #progress}，
	 * 然后立刻结算可能跨过去的那几份。
	 */
	public int fastCraft() {
		if (!this.isCrafting()) {
			return 0;
		}
		ItemStack crystals = this.getItem(CRYSTAL_SLOT);
		if (crystals.isEmpty()
				|| !crystals.is(com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_TimeCrystal.BHR_TIME_CRYSTAL)) {
			return 0;
		}
		int ticksPerCrystal = com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_TimeCrystal.SECONDS_PER_CRYSTAL * 20;
		int left = Math.max(0, this.total - this.progress);
		int needed = (left + ticksPerCrystal - 1) / ticksPerCrystal;
		int used = Math.min(needed, crystals.getCount());
		crystals.shrink(used);
		this.progress += used * ticksPerCrystal;
		int guard = 0;
		while (this.total > 0 && this.progress >= this.total && guard++ < 64) {
			this.progress -= this.total;
			this.emitOneBatch();
		}
		this.setChanged();
		return used;
	}

	/** 结算一份：产物进结果栏，扣掉这一份的原料，剩余份数减一。 */
	private void emitOneBatch() {
		Level level = this.getLevel();
		BHR_FoolRecipe recipe = this.selectedRecipe();
		if (level == null || recipe == null) {
			this.total = 0;
			this.remaining = 0;
			this.progress = 0;
			return;
		}

		// 升级配方：不给物品，把方块状态的等级改写，做完就收工（升级永远只做一次）
		if (recipe.isUpgrade()) {
			int target = Math.min(recipe.upgradeTo().orElse(this.getWorkBenchLevel()), BHR_Campfire.LEVEL_MAX);
			level.setBlockAndUpdate(this.worldPosition,
					this.getBlockState().setValue(BHR_Campfire.LEVEL, target));
			this.consumeOneBatch(recipe);
			this.total = 0;
			this.remaining = 0;
			this.progress = 0;
			this.batches = 1;
			// 升级完成就取消选中，避免同一台机器被重复升级
			this.selected = -1;
			java.util.Arrays.fill(this.preferred, 0);
			this.setChanged();
			return;
		}

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

		// 容器返还（配方的 "container" 字段）：每结算一份就把那只桶还给玩家。
		// 原料是从玩家背包扣走的、制作又可能在他关掉界面之后才跑完，所以没有「玩家」可给 ——
		// 统一吐在方块脚下（与原版熔炉把空桶还给玩家不同：那台机器是玩家亲自在操作）。
		if (recipe.hasContainer()) {
			recipe.container().ifPresent(item ->
					Block.popResource(level, this.worldPosition, new ItemStack(item)));
		}

		this.remaining--;
		// 界面上显示的就是「还剩几份」，所以倍率数字跟着一起减；收工回到 1
		this.batches = Math.max(0, this.remaining);
		if (this.remaining <= 0) {
			this.remaining = 0;
			this.batches = 1;
			this.total = 0;
			this.progress = 0;
		}
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

	/** 方块被移除时吐东西：结果栏产物 + 暂存原料。时间水晶由菜单的 {@code removed(...)} 退回。 */
	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		super.preRemoveSideEffects(pos, state);
		Level level = this.getLevel();
		if (level == null || level.isClientSide()) {
			return;
		}
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
			case DATA_PROGRESS -> this.progress;
			case DATA_TOTAL -> this.total;
			case DATA_LEVEL -> this.getWorkBenchLevel();
			case DATA_EXPERT -> this.isExpertRecipes() ? 1 : 0;
			case DATA_SCROLL -> this.scroll;
			case DATA_REMAINING -> this.remaining;
			case DATA_FUEL -> this.fuel;
			default -> {
				if (index >= DATA_PREF_START && index < DATA_PREF_START + DATA_PREF_COUNT) {
					yield this.preferred[index - DATA_PREF_START];
				}
				if (index >= DATA_FUEL_PREF_START && index < DATA_FUEL_PREF_START + DATA_FUEL_PREF_COUNT) {
					yield this.fuelPreferred[index - DATA_FUEL_PREF_START];
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
				// 等级 / 专家开关 / 燃料 / 进度是只读的派生值
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
		output.putInt("progress", this.progress);
		output.putInt("total", this.total);
		output.putInt("remaining", this.remaining);
		output.putInt("fuel", this.fuel);
		output.putInt("scroll", this.scroll);
		output.putIntArray("preferred", this.preferred);
		output.putIntArray("fuel_preferred", this.fuelPreferred);
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
		this.progress = Math.max(0, input.getIntOr("progress", 0));
		this.total = Math.max(0, input.getIntOr("total", 0));
		this.remaining = Math.max(0, input.getIntOr("remaining", 0));
		this.fuel = Math.max(0, input.getIntOr("fuel", 0));
		this.scroll = Math.max(0, input.getIntOr("scroll", 0));
		this.pending.clear();
		input.listOrEmpty("pending", ItemStack.CODEC).forEach(this.pending::add);
		int[] savedPreferred = input.getIntArray("preferred").orElse(new int[DATA_PREF_COUNT]);
		for (int i = 0; i < Math.min(savedPreferred.length, this.preferred.length); i++) {
			this.preferred[i] = savedPreferred[i];
		}
		int[] savedFuelPreferred = input.getIntArray("fuel_preferred").orElse(new int[DATA_FUEL_PREF_COUNT]);
		for (int i = 0; i < Math.min(savedFuelPreferred.length, this.fuelPreferred.length); i++) {
			this.fuelPreferred[i] = savedFuelPreferred[i];
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
