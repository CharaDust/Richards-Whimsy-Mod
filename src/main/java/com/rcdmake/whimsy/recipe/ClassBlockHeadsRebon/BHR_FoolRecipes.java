package com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 愚者配方的<b>加载与查询</b>。
 *
 * <h2>数据在哪</h2>
 * <p>{@code data/richards-whimsy-mod/bhr_recipe/&#42;.json}（模组自带，客户端与服务端各读各的同一份文件）。
 * 用目录做「制作台类型」的分组：{@code bhr_workbench/xxx.json} 就是 {@code bhr_workbench} 这一台的配方。
 *
 * <h2>为什么不用原版 {@code Recipe} 注册表 + 数据包重载</h2>
 * <ul>
 *   <li>26.2 的客户端<b>没有</b>完整的配方表（{@code RecipeAccess} 只剩石切机这类专门接口），
 *       自定义配方想在客户端列出来就得自己发包；</li>
 *   <li>项目现状是「配方先都放在制作台里」（用户原话），所以直接读模组资源最省事：
 *       <b>两端读到的是同一份</b>，界面不需要为「配方列表」额外联网。</li>
 * </ul>
 * <p>代价（已明确接受）：暂时不能靠数据包覆盖/追加。以后要支持数据包时，
 * 把 {@link #loadFromModResources} 换成 {@code SimpleJsonResourceReloadListener} 即可，
 * 上层的 {@link Entry} 视图与查询接口不用动。
 *
 * <h2>加载时机</h2>
 * <p>由主类在<b>所有物品/方块注册之后</b>调用（配方里要按 id 查物品，早于注册会查不到）。
 * 静态一次加载，不随数据包重载刷新。
 */
public final class BHR_FoolRecipes {

	/** 配方根目录（数据包根下的相对路径）。 */
	public static final String DIRECTORY = "bhr_recipe";

	/** 配方条目：id（文件名）+ 配方本体。id 用于界面选择与存档，所以与内容分开存。 */
	public record Entry(Identifier id, BHR_FoolRecipe recipe) {
	}

	/** 按 id 索引（保持插入顺序，方便排查）。 */
	private static final Map<Identifier, BHR_FoolRecipe> BY_ID = new LinkedHashMap<>();

	/** 全部配方，已按「等级 → id」排好序（界面滚动列表直接用这个顺序）。 */
	private static List<Entry> sorted = List.of();

	private static boolean loaded;

	private BHR_FoolRecipes() {
	}

	/** 加载（幂等）。由主类在所有注册完成后调用。 */
	public static void OnInit() {
		if (loaded) {
			return;
		}
		loaded = true;
		loadFromModResources();
	}

	/** 全部配方（已排序）。 */
	public static List<Entry> all() {
		return sorted;
	}

	/** 某台制作台支持的全部配方（已排序；不含「当前等级 / 专家模式」过滤，那两个是界面的事）。 */
	public static List<Entry> forWorkbench(String workbench) {
		List<Entry> result = new ArrayList<>();
		for (Entry entry : sorted) {
			if (entry.recipe().workbench().equals(workbench)) {
				result.add(entry);
			}
		}
		return result;
	}

	/** 按 id 取配方。 */
	public static Optional<BHR_FoolRecipe> byId(Identifier id) {
		return Optional.ofNullable(BY_ID.get(id));
	}

	// ── 制作逻辑的公共助手（服务端消耗、客户端算够不够，两端共用同一套判定）──

	/** 玩家背包（不含光标）里符合这条需求的物品总数。 */
	public static int countInInventory(net.minecraft.world.entity.player.Player player,
			BHR_FoolIngredient ingredient) {
		int total = 0;
		var inventory = player.getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (ingredient.accepts(stack)) {
				total += stack.getCount();
			}
		}
		return total;
	}

	/** 一次制作 {@code batches} 份所需的某条原料总数。 */
	public static int requiredCount(BHR_FoolIngredient ingredient, int batches) {
		return ingredient.count() * batches;
	}

	/** 玩家是否凑得齐这条配方的全部原料（{@code batches} 份）。 */
	public static boolean canAfford(net.minecraft.world.entity.player.Player player,
			BHR_FoolRecipe recipe, int batches) {
		for (BHR_FoolIngredient ingredient : recipe.ingredients()) {
			if (countInInventory(player, ingredient) < requiredCount(ingredient, batches)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 从玩家背包扣除原料；调用前必须先用 {@link #canAfford} 判过。
	 *
	 * <p>{@code preferred} 是「每个需求格的首选物品」（下标对齐配方里的 ingredients 顺序，
	 * 元素可以是 {@code null}）。扣料分两趟：<b>先扣首选</b>，再扣其它可接受的同类物品 ——
	 * 这样「背包里橡木与云杉木板并存时先消耗橡木」成立，右键切换首选后就反过来。
	 *
	 * @return 是否真的扣掉了（理论上恒为 true）
	 */
	public static boolean consume(net.minecraft.world.entity.player.Player player,
			BHR_FoolRecipe recipe, int batches, java.util.List<Item> preferred) {
		var inventory = player.getInventory();
		for (int index = 0; index < recipe.ingredients().size(); index++) {
			BHR_FoolIngredient ingredient = recipe.ingredients().get(index);
			int remaining = requiredCount(ingredient, batches);
			Item first = index < preferred.size() ? preferred.get(index) : null;

			// 第一趟：首选物品
			if (first != null) {
				remaining = drain(player, inventory, ingredient, first, remaining);
			}
			// 第二趟：其余可接受的物品
			if (remaining > 0) {
				for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
					ItemStack stack = inventory.getItem(slot);
					if (!ingredient.accepts(stack)) {
						continue;
					}
					remaining = take(inventory, slot, stack, remaining);
				}
			}
			if (remaining > 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 取料并<b>返回实际取出的物品堆</b>（不直接消失）—— 这些堆会暂存在制作台方块实体里，
	 * 每完成一份扣掉一份的量；方块中途被打破时原样退还。
	 *
	 * @return 取出的物品堆列表；原料不够时返回空列表（并把已取出的还回背包）
	 */
	public static java.util.List<ItemStack> takeForCraft(net.minecraft.world.entity.player.Player player,
			BHR_FoolRecipe recipe, int batches, java.util.List<Item> preferred) {
		var inventory = player.getInventory();
		java.util.List<ItemStack> taken = new java.util.ArrayList<>();
		for (int index = 0; index < recipe.ingredients().size(); index++) {
			BHR_FoolIngredient ingredient = recipe.ingredients().get(index);
			int remaining = requiredCount(ingredient, batches);
			Item first = index < preferred.size() ? preferred.get(index) : null;
			if (first != null) {
				remaining = takeMatching(inventory, remaining, taken, stack -> stack.is(first));
			}
			if (remaining > 0) {
				remaining = takeMatching(inventory, remaining, taken, ingredient::accepts);
			}
			if (remaining > 0) {
				// 取不够 → 把已取的还回去，别吞材料
				for (ItemStack stack : taken) {
					if (!inventory.add(stack)) {
						player.drop(stack, false);
					}
				}
				return java.util.List.of();
			}
		}
		return taken;
	}

	private static int takeMatching(net.minecraft.world.entity.player.Inventory inventory, int remaining,
			java.util.List<ItemStack> taken, java.util.function.Predicate<ItemStack> filter) {
		for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.isEmpty() || !filter.test(stack)) {
				continue;
			}
			int amount = Math.min(remaining, stack.getCount());
			taken.add(stack.split(amount));
			if (stack.isEmpty()) {
				inventory.setItem(slot, ItemStack.EMPTY);
			}
			remaining -= amount;
		}
		return remaining;
	}

	/** 只扣某一种具体物品。 */
	private static int drain(net.minecraft.world.entity.player.Player player,
			net.minecraft.world.entity.player.Inventory inventory, BHR_FoolIngredient ingredient,
			Item item, int remaining) {
		for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.isEmpty() || !stack.is(item)) {
				continue;
			}
			remaining = take(inventory, slot, stack, remaining);
		}
		return remaining;
	}

	private static int take(net.minecraft.world.entity.player.Inventory inventory, int slot, ItemStack stack,
			int remaining) {
		int taken = Math.min(remaining, stack.getCount());
		stack.shrink(taken);
		if (stack.isEmpty()) {
			inventory.setItem(slot, ItemStack.EMPTY);
		}
		return remaining - taken;
	}

	// ── 加载实现 ──

	private static void loadFromModResources() {
		Path root;
		try {
			root = FabricLoader.getInstance()
					.getModContainer(RichardsWhimsyMod.MOD_ID)
					.orElseThrow(() -> new IllegalStateException("找不到模组容器 " + RichardsWhimsyMod.MOD_ID))
					.findPath("data/" + RichardsWhimsyMod.MOD_ID + "/" + DIRECTORY)
					.orElse(null);
		} catch (RuntimeException e) {
			RichardsWhimsyMod.LOGGER.error("[recipe] 无法定位配方目录", e);
			return;
		}
		if (root == null || !Files.isDirectory(root)) {
			RichardsWhimsyMod.LOGGER.warn("[recipe] 配方目录不存在：data/{}/{}/", RichardsWhimsyMod.MOD_ID, DIRECTORY);
			sorted = List.of();
			return;
		}

		List<Entry> entries = new ArrayList<>();
		try (Stream<Path> files = Files.walk(root)) {
			files.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().endsWith(".json"))
					.sorted()
					.forEach(path -> {
						Entry entry = readOne(root, path);
						if (entry != null) {
							entries.add(entry);
						}
					});
		} catch (IOException e) {
			RichardsWhimsyMod.LOGGER.error("[recipe] 遍历配方目录失败", e);
		}

		// 排序：等级 → 内定顺序 → id。
		// 内定配方（JSON 里显式写了 "order"）排在前、按 order 升序；没写 order 的（将来的数据包配方）
		// 排在后面、按 id 字母序 —— doc/wiki/Misc/配方.md「配方排序：内定顺序 + 数据包配方补后面」。
		entries.sort(Comparator
				.comparingInt((Entry e) -> e.recipe().level())
				.thenComparingInt(e -> e.recipe().hasOrder() ? 0 : 1)
				.thenComparingInt(e -> e.recipe().hasOrder() ? e.recipe().order() : 0)
				.thenComparing(e -> e.id().toString()));
		entries.forEach(entry -> BY_ID.put(entry.id(), entry.recipe()));
		sorted = List.copyOf(entries);

		RichardsWhimsyMod.LOGGER.info("[recipe] 已加载 {} 条愚者配方", sorted.size());
	}

	private static Entry readOne(Path root, Path file) {
		String relative = root.relativize(file).toString().replace('\\', '/');
		String path = relative.substring(0, relative.length() - ".json".length());
		Identifier id = Identifier.fromNamespaceAndPath(RichardsWhimsyMod.MOD_ID, path);
		try {
			String json = Files.readString(file, StandardCharsets.UTF_8);
			JsonObject object = JsonParser.parseString(json).getAsJsonObject();
			return new Entry(id, BHR_FoolRecipe.parse(object));
		} catch (IOException | RuntimeException e) {
			RichardsWhimsyMod.LOGGER.error("[recipe] 解析失败：{}（{}）", id, e.getMessage());
			return null;
		}
	}
}
