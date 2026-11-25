package com.rcdmake.whimsy.surface;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon.BHR_FoolRecipe;
import com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon.BHR_FoolRecipes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>原版配方索引</b>：把「服务端投影出来的原版配方」装进来，供两端共用。
 *
 * <h2>为什么两端都要有</h2>
 * <p>26.2 客户端的 {@code RecipeAccess} 只剩 {@code propertySet} 与 {@code stonecutterRecipes()}
 * （已对 jar 逐条核验），客户端<b>拿不到</b>通用配方表。所以原版配方必须由服务端投影后下发，
 * 客户端收到的是「已经翻译成愚者配方格式的 JSON」，解析后与木工台自己的配方混在同一个列表里。
 *
 * <h2>数据流</h2>
 * <pre>
 *   服务端：ResourceManager 扫 data/*&#47;recipe/**.json → 过滤 → 翻成愚者配方 JSON
 *           → {@link BHR_FoolRecipes#setRemote}（服务端自己也注入，两端列表一致）
 *           → {@link SurfaceRecipesPayload} 一次全量发给客户端
 *   客户端：收到 payload → 同一个 {@link BHR_FoolRecipes#setRemote}
 * </pre>
 * 于是「选配方 / 看成本 / 开始制作」三件事走的都是现成代码。
 *
 * <h2>失效</h2>
 * <p>数据包重载后两端各自调用 {@link #invalidate()}：服务端在
 * {@code ServerLifecycleEvents.END_DATA_PACK_RELOAD}，客户端在下一次收到 payload 时自然覆盖。
 */
public final class VanillaRecipeIndex {

	/** 当前注入的原版配方（工作面 id → 列表），按工作面累积。 */
	private static final Map<String, List<BHR_FoolRecipe>> CURRENT = new LinkedHashMap<>();

	/** 代数（自检 / 排查用）。 */
	private static int generation;

	private VanillaRecipeIndex() {
	}

	/** 收到某台工作面的投影结果（服务端投影完成后、客户端收到 payload 后都会调）。 */
	public static void accept(String surfaceId, List<String> recipeJson) {
		List<BHR_FoolRecipe> parsed = new ArrayList<>(recipeJson.size());
		for (String json : recipeJson) {
			try {
				parsed.add(BHR_FoolRecipe.parse(com.google.gson.JsonParser.parseString(json).getAsJsonObject()));
			} catch (RuntimeException e) {
				RichardsWhimsyMod.LOGGER.warn("[vanilla] 投影配方解析失败：{}", e.getMessage());
			}
		}
		CURRENT.put(surfaceId, List.copyOf(parsed));
		generation++;
		BHR_FoolRecipes.setRemote(CURRENT);
		RichardsWhimsyMod.LOGGER.info("[vanilla] 已注入 {} 条原版配方（{}，代数 {}）",
				parsed.size(), surfaceId, generation);
	}

	/** 数据包重载：清空（下一次打开界面时服务端会重新投影并下发）。 */
	public static void invalidate() {
		CURRENT.clear();
		BHR_FoolRecipes.setRemote(Map.of());
	}

	/** 当前代数。 */
	public static int generation() {
		return generation;
	}
}
