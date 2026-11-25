package com.rcdmake.whimsy.surface;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rcdmake.whimsy.ModRegister;
import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>原版配方投影器</b>（服务端）：把数据包里的 {@code crafting_shaped} / {@code crafting_shapeless}
 * 配方翻译成愚者配方 JSON。
 *
 * <h2>为什么直接读数据包 JSON，而不是用 RecipeManager</h2>
 * <p>26.2 起 {@code Recipe} 已经<b>没有</b> {@code getIngredients()}（而已核查）——原料是通过
 * {@code RecipeDisplay} / {@code SlotDisplay} 表达的，服务端要还原成「材料多重集」得先解析显示层。
 * 而数据包 JSON 里原料是一等公民（{@code pattern}/{@code key}/{@code ingredients}），
 * 读它既直白又天然跟着 {@code /reload} 走，所以投影直接读资源 JSON。
 *
 * <h2>过滤规则（数据驱动）</h2>
 * <p>见 {@code data/richards-whimsy-mod/surface/vanilla_recipes.json}：每个工作面一张规则表，
 * 声明等级、耗时、产物白名单（tag）、材料白名单（tag）。三道筛：
 * <ol>
 *   <li>只收 {@code crafting_shaped} / {@code crafting_shapeless}；</li>
 *   <li>产物必须命中产物白名单里的某个 tag；</li>
 *   <li>每个材料都要命中材料白名单：材料是物品 → 该物品在某白名单 tag 里；材料是标签 → 该标签本身在白名单里。</li>
 * </ol>
 * 这样「各种木板互转」这类自循环配方会被产物白名单挡掉，列表里不会出现噪音。
 *
 * <h2>缓存</h2>
 * <p>按工作面缓存；数据包重载时 {@link #invalidate()}。
 */
public final class VanillaRecipeProjector {

	/** 规则表在数据包里的位置（<b>带 .json</b>：{@code ResourceManager#getResource} 收的是完整路径）。 */
	public static final String RULES_PATH = "surface/vanilla_recipes.json";

	/** 工作面 → 投影出的简版 JSON 列表。 */
	private static final Map<String, List<String>> CACHE = new HashMap<>();

	private VanillaRecipeProjector() {
	}

	/** 规则：等级、耗时、产物白名单、材料白名单。 */
	public record Rule(int level, int timeSeconds, List<String> products, List<String> materials) {
	}

	/** 某台工作面的投影结果（带缓存）。 */
	public static List<String> forSurface(MinecraftServer server, String surfaceId) {
		List<String> cached = CACHE.get(surfaceId);
		if (cached != null) {
			return cached;
		}
		List<String> built = build(server, surfaceId);
		CACHE.put(surfaceId, built);
		return built;
	}

	/** 数据包重载后调用。 */
	public static void invalidate() {
		CACHE.clear();
	}

	// ── 构建 ──

	private static List<String> build(MinecraftServer server, String surfaceId) {
		Rule rule = ruleFor(server, surfaceId);
		if (rule == null) {
			return List.of();
		}
		Map<Identifier, Resource> resources = server.getResourceManager()
				.listResources("recipe", id -> id.getPath().endsWith(".json"));
		List<String> result = new ArrayList<>();
		for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
			try (InputStream in = entry.getValue().open()) {
				String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
				JsonObject json = JsonParser.parseString(text).getAsJsonObject();
				String projected = project(surfaceId, rule, json);
				if (projected != null) {
					result.add(projected);
				}
			} catch (Exception e) {
				// 单个配方读失败不影响整批（其它模组的怪格式配方很常见）
				RichardsWhimsyMod.LOGGER.debug("[vanilla] 跳过 {}：{}", entry.getKey(), e.getMessage());
			}
		}
		result.sort(String::compareTo);
		if (RichardsWhimsyMod.LOGGER.isInfoEnabled()) {
			RichardsWhimsyMod.LOGGER.info("[vanilla] {} 投影出 {} 条原版配方", surfaceId, result.size());
		}
		if (RichardsWhimsyMod.LOGGER.isDebugEnabled()) {
			// 具体是哪几条：只在 DEBUG 里打，默认日志看不到，需要核对时开 debug.log 即可
			for (String json : result) {
				RichardsWhimsyMod.LOGGER.debug("[vanilla]   {}", json);
			}
		}
		return List.copyOf(result);
	}

	/** 读规则表里某一台工作面的规则；没有规则返回 {@code null}（= 这台不订阅原版配方）。 */
	private static Rule ruleFor(MinecraftServer server, String surfaceId) {
		var resource = server.getResourceManager().getResource(ModRegister.id(RULES_PATH));
		if (resource.isEmpty()) {
			RichardsWhimsyMod.LOGGER.warn("[vanilla] 规则表不存在：{}", RULES_PATH);
			return null;
		}
		try (InputStream in = resource.get().open()) {
			JsonObject root = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8))
					.getAsJsonObject();
			if (!root.has(surfaceId)) {
				return null;
			}
			JsonObject node = root.getAsJsonObject(surfaceId);
			return new Rule(
					node.has("level") ? node.get("level").getAsInt() : 2,
					node.has("time") ? node.get("time").getAsInt() : 5,
					strings(node.has("products") ? node.getAsJsonArray("products") : new JsonArray()),
					strings(node.has("materials") ? node.getAsJsonArray("materials") : new JsonArray()));
		} catch (Exception e) {
			RichardsWhimsyMod.LOGGER.error("[vanilla] 规则表解析失败", e);
			return null;
		}
	}

	/** 规则表里登记了的所有工作面 id（服务端启动自检 / 预热用）。 */
	public static List<String> subscribedSurfaces(MinecraftServer server) {
		var resource = server.getResourceManager().getResource(ModRegister.id(RULES_PATH));
		if (resource.isEmpty()) {
			return List.of();
		}
		try (InputStream in = resource.get().open()) {
			JsonObject root = JsonParser.parseString(new String(in.readAllBytes(), StandardCharsets.UTF_8))
					.getAsJsonObject();
			List<String> result = new ArrayList<>();
			for (String key : root.keySet()) {
				result.add(key);
			}
			return List.copyOf(result);
		} catch (Exception e) {
			return List.of();
		}
	}

	private static List<String> strings(JsonArray array) {
		List<String> result = new ArrayList<>(array.size());
		for (JsonElement element : array) {
			result.add(element.getAsString());
		}
		return List.copyOf(result);
	}

	// ── 单条配方 → 愚者配方 JSON ──

	private static String project(String surfaceId, Rule rule, JsonObject json) {
		String type = json.has("type") ? json.get("type").getAsString() : "";
		boolean shaped = type.endsWith("crafting_shaped");
		boolean shapeless = type.endsWith("crafting_shapeless");
		if (!shaped && !shapeless) {
			return null;
		}
		if (!json.has("result") || !json.get("result").isJsonObject()) {
			return null;
		}
		JsonObject result = json.getAsJsonObject("result");
		String resultId = result.has("id") ? result.get("id").getAsString()
				: result.has("item") ? result.get("item").getAsString() : null;
		if (resultId == null) {
			return null;
		}
		Item resultItem = BuiltInRegistries.ITEM.getValue(Identifier.parse(resultId));
		if (resultItem == null || resultItem == Items.AIR) {
			return null;
		}
		if (!matchesAnyTag(resultItem, rule.products())) {
			return null;
		}
		int resultCount = result.has("count") ? result.get("count").getAsInt() : 1;

		// 材料：字符 → (材料描述, 数量)
		Map<String, Integer> counts = new LinkedHashMap<>();
		if (shaped) {
			if (!json.has("pattern") || !json.has("key")) {
				return null;
			}
			JsonObject key = json.getAsJsonObject("key");
			for (JsonElement row : json.getAsJsonArray("pattern")) {
				for (char symbol : row.getAsString().toCharArray()) {
					if (symbol == ' ') {
						continue;
					}
					String material = materialOf(key.get(String.valueOf(symbol)));
					if (material == null) {
						return null;
					}
					counts.merge(material, 1, Integer::sum);
				}
			}
		} else {
			for (JsonElement element : json.getAsJsonArray("ingredients")) {
				String material = element.isJsonArray()
						? materialOf(element.getAsJsonArray().get(0))
						: materialOf(element);
				if (material == null) {
					return null;
				}
				counts.merge(material, 1, Integer::sum);
			}
		}
		if (counts.isEmpty() || counts.size() > 9) {
			return null;
		}
		// 材料白名单
		for (String material : counts.keySet()) {
			if (!allowedMaterial(material, rule.materials())) {
				return null;
			}
		}

		JsonObject out = new JsonObject();
		out.addProperty("workbench", surfaceId);
		out.addProperty("expert", false);
		out.addProperty("level", rule.level());
		out.addProperty("time", rule.timeSeconds());
		JsonArray ingredients = new JsonArray();
		counts.forEach((material, count) -> {
			JsonObject ingredient = new JsonObject();
			if (material.startsWith("#")) {
				ingredient.addProperty("tag", material.substring(1));
			} else {
				ingredient.addProperty("item", material);
			}
			ingredient.addProperty("count", count);
			ingredients.add(ingredient);
		});
		out.add("ingredients", ingredients);
		JsonObject outResult = new JsonObject();
		outResult.addProperty("item", resultId);
		outResult.addProperty("count", resultCount);
		out.add("result", outResult);
		return out.toString();
	}

	/** 材料的规范写法：{@code minecraft:x} 或 {@code #tag}；无法识别返回 {@code null}。 */
	private static String materialOf(JsonElement element) {
		if (element == null) {
			return null;
		}
		if (element.isJsonArray()) {
			// 老格式的「任选其一」数组：取第一个够了 —— 我们的引擎会把它当成「一个具体材料」
			return element.getAsJsonArray().isEmpty() ? null : materialOf(element.getAsJsonArray().get(0));
		}
		if (element.isJsonPrimitive()) {
			String raw = element.getAsString();
			return raw.isBlank() ? null : raw;
		}
		if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (object.has("item")) {
				return object.get("item").getAsString();
			}
			if (object.has("tag")) {
				return "#" + object.get("tag").getAsString();
			}
		}
		return null;
	}

	private static boolean allowedMaterial(String material, List<String> allowedTags) {
		if (material.startsWith("#")) {
			return allowedTags.contains(material);
		}
		// 白名单里也可以直接写具体物品（例如木棍：它不属于任何「木质方块」标签，
		// 但栅栏 / 栅栏门 / 告示牌的原版配方都要它）
		if (allowedTags.contains(material)) {
			return true;
		}
		Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(material));
		return item != null && item != Items.AIR && matchesAnyTag(item, allowedTags);
	}

	private static boolean matchesAnyTag(Item item, List<String> tagIds) {
		if (tagIds.isEmpty()) {
			return false;
		}
		var holder = BuiltInRegistries.ITEM.wrapAsHolder(item);
		for (String raw : tagIds) {
			String id = raw.startsWith("#") ? raw.substring(1) : raw;
			TagKey<Item> tag = TagKey.create(net.minecraft.core.registries.Registries.ITEM,
					Identifier.parse(id));
			if (holder.is(tag)) {
				return true;
			}
		}
		return false;
	}
}
