package com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon;

import com.google.gson.JsonObject;
import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * <b>产物族（family）</b>：把「原版按树种拆开的一堆配方」压成一条。
 *
 * <h2>要解决的问题</h2>
 * <p>原版是「9 种木各一条配方」（{@code oak_door} / {@code birch_door} / …），而愚者配方的
 * {@code result} 只能写一个具体物品 id。逐条枚举会让木工台 L2 长出九条几乎一样的「木门」，
 * 界面直接废掉。产物族让配方写「族」，运行时按<b>实际消耗掉的木板变种</b>决定产物。
 *
 * <h2>文件名即族名</h2>
 * <p>{@code data/richards-whimsy-mod/bhr_family/<族名>.json}，配方里写
 * {@code "result": { "family": "door", "from": 0 }}。
 *
 * <h2>JSON（约定 + 例外 + 手写表，三种可混用）</h2>
 * <pre>{@code
 * {
 *   "source_suffix": "_planks",        // 消耗物的 id 后缀
 *   "target_suffix": "_door",          // 产物 id 后缀（把 source 换成 target 再查注册表）
 *   "default": "minecraft:oak_door",   // 兜底：约定查不到、例外表也没有时用它
 *   "special": { "minecraft:bamboo_planks": "minecraft:bamboo_raft" },
 *   "map": { "richards-whimsy-mod:whatever": "minecraft:oak_door" }
 * }
 * }</pre>
 *
 * <p><b>为什么用「换后缀」而不是写满一张表</b>：原版 12 种木、外加模组自家果木，全都符合
 * {@code <wood>_planks → <wood>_door} 这个命名约定，所以一份 family 文件不到十行就能覆盖全部；
 * 以后加树种（模组的、别的模组的、数据包的）只要命名守约，<b>一行都不用改</b>。
 * 只有 bamboo（船是 {@code bamboo_raft}）这类不守约的才进 {@code special}。
 *
 * <p>解析顺序：{@code map} → 换后缀查注册表 → {@code special} → {@code default}。
 */
public final class BHR_FoolFamilies {

	/** 数据目录（数据包根下的相对路径）。 */
	public static final String DIRECTORY = "bhr_family";

	/**
	 * 一个产物族。
	 *
	 * @param sourceSuffix 消耗物的 id 后缀（如 {@code _planks}）
	 * @param targetSuffix 产物的 id 后缀（如 {@code _door}）
	 * @param defaultItem  兜底产物
	 * @param special      例外表（显式覆盖，优先级高于后缀约定、低于 {@code map}）
	 * @param map          手写表（最高优先级）
	 */
	public record Family(
			String sourceSuffix,
			String targetSuffix,
			Item defaultItem,
			Map<Item, Item> special,
			Map<Item, Item> map) {

		/** 按消耗物解析产物。 */
		public Item resolve(Item source) {
			if (source == null || source == Items.AIR) {
				return this.defaultItem;
			}
			Item explicit = this.map.get(source);
			if (explicit != null) {
				return explicit;
			}
			// 后缀约定：<wood>_planks → <wood>_door
			Identifier id = BuiltInRegistries.ITEM.getKey(source);
			String path = id.getPath();
			if (this.sourceSuffix != null && !this.sourceSuffix.isEmpty() && path.endsWith(this.sourceSuffix)) {
				String candidate = path.substring(0, path.length() - this.sourceSuffix.length())
						+ this.targetSuffix;
				Item resolved = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath(id.getNamespace(), candidate));
				if (resolved != null && resolved != Items.AIR) {
					return resolved;
				}
			}
			Item special = this.special.get(source);
			return special != null ? special : this.defaultItem;
		}
	}

	/** 族名 → 族。 */
	private static final Map<String, Family> BY_NAME = new LinkedHashMap<>();

	private static boolean loaded;

	private BHR_FoolFamilies() {
	}

	/** 加载（幂等）。必须在 {@link BHR_FoolRecipes#OnInit()} 里、配方解析之前调用。 */
	public static void OnInit() {
		if (loaded) {
			return;
		}
		loaded = true;
		load();
	}

	/** 按族名取族；不存在返回 {@code null}。 */
	public static Family byName(String family) {
		return BY_NAME.get(family);
	}

	/** 解析产物；族不存在时退回「什么都不给」。 */
	public static Item resolve(String family, Item source) {
		Family definition = BY_NAME.get(family);
		return definition == null ? Items.AIR : definition.resolve(source);
	}

	/** 已加载的族数（自检用）。 */
	public static int size() {
		return BY_NAME.size();
	}

	// ── 加载 ──

	private static void load() {
		Path root;
		try {
			root = FabricLoader.getInstance()
					.getModContainer(RichardsWhimsyMod.MOD_ID)
					.orElseThrow(() -> new IllegalStateException("找不到模组容器"))
					.findPath("data/" + RichardsWhimsyMod.MOD_ID + "/" + DIRECTORY)
					.orElse(null);
		} catch (RuntimeException e) {
			RichardsWhimsyMod.LOGGER.error("[family] 无法定位目录", e);
			return;
		}
		if (root == null || !Files.isDirectory(root)) {
			RichardsWhimsyMod.LOGGER.warn("[family] 目录不存在：data/{}/{}/", RichardsWhimsyMod.MOD_ID, DIRECTORY);
			return;
		}
		try (Stream<Path> files = Files.walk(root)) {
			files.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().endsWith(".json"))
					.sorted()
					.forEach(path -> {
						String name = path.getFileName().toString();
						String family = name.substring(0, name.length() - ".json".length());
						try {
							Family parsed = parse(Files.readString(path, StandardCharsets.UTF_8));
							BY_NAME.put(family, parsed);
						} catch (IOException | RuntimeException e) {
							RichardsWhimsyMod.LOGGER.error("[family] 解析失败：{}（{}）", name, e.getMessage());
						}
					});
		} catch (IOException e) {
			RichardsWhimsyMod.LOGGER.error("[family] 遍历目录失败", e);
		}
		RichardsWhimsyMod.LOGGER.info("[family] 已加载 {} 个产物族", BY_NAME.size());
		logSelfCheck();
	}

	/**
	 * 自检：拿两个已知用例验一遍后缀约定与例外表，启动日志里就能看出族有没有接错。
	 *
	 * <p>刻意只打两条 —— 每次启动刷十一条会盖住真正的报错。
	 */
	private static void logSelfCheck() {
		Item oakPlanks = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("oak_planks"));
		Item bambooPlanks = BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace("bamboo_planks"));
		if (oakPlanks != null) {
			RichardsWhimsyMod.LOGGER.info("[family] 自检 door(oak_planks)={}", keyOf(resolve("door", oakPlanks)));
			RichardsWhimsyMod.LOGGER.info("[family] 自检 shelf(oak_planks)={}", keyOf(resolve("shelf", oakPlanks)));
		}
		if (bambooPlanks != null) {
			RichardsWhimsyMod.LOGGER.info("[family] 自检 boat(bamboo_planks)={}", keyOf(resolve("boat", bambooPlanks)));
		}
	}

	private static String keyOf(Item item) {
		return item == null || item == Items.AIR ? "<无>" : BuiltInRegistries.ITEM.getKey(item).toString();
	}

	private static Family parse(String json) {
		JsonObject object = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
		String sourceSuffix = object.has("source_suffix") ? object.get("source_suffix").getAsString() : "";
		String targetSuffix = object.has("target_suffix") ? object.get("target_suffix").getAsString() : "";
		Item defaultItem = item(object, "default");
		Map<Item, Item> special = new HashMap<>();
		readMap(object, "special", special);
		Map<Item, Item> map = new HashMap<>();
		readMap(object, "map", map);
		return new Family(sourceSuffix, targetSuffix, defaultItem, Map.copyOf(special), Map.copyOf(map));
	}

	private static void readMap(JsonObject parent, String key, Map<Item, Item> into) {
		if (!parent.has(key) || !parent.get(key).isJsonObject()) {
			return;
		}
		for (var entry : parent.getAsJsonObject(key).entrySet()) {
			into.put(id(entry.getKey()), id(entry.getValue().getAsString()));
		}
	}

	private static Item item(JsonObject object, String key) {
		if (!object.has(key)) {
			throw new IllegalArgumentException("缺少字段：" + key);
		}
		return id(object.get(key).getAsString());
	}

	private static Item id(String raw) {
		Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(raw));
		if (item == null || item == Items.AIR) {
			throw new IllegalArgumentException("未知物品 id：" + raw);
		}
		return item;
	}
}
