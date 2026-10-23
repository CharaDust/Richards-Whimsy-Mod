package com.rcdmake.whimsy.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * 物品模型定义生成器 —— 为每个物品产出
 * {@code assets/<modid>/items/<id>.json}。
 *
 * <h2>为什么要它（26.2 的破坏性变更）</h2>
 * 自 1.21.4 起，物品渲染从「{@code models/item/<id>.json} 一个文件搞定」
 * 改成<b>两段式</b>：
 * <ol>
 *   <li>{@code assets/<ns>/items/<id>.json} —— <b>物品模型定义</b>，
 *       用 {@code type} 分发，指向一个模型；</li>
 *   <li>被指向的那个模型（{@code models/item/...} 或 {@code models/block/...}）。</li>
 * </ol>
 * <b>没有第 1 个文件，物品就是 MissingItemModel</b> —— 表现为暗淡的 2D 紫黑贴图。
 * 这正是迁移后物品全部变暗的原因。
 *
 * <p>原版范本（从 26.2 客户端 jar 提取核对）：
 * <pre>{@code
 * // assets/minecraft/items/stone.json        （方块物品）
 * { "model": { "type": "minecraft:model", "model": "minecraft:block/stone" } }
 *
 * // assets/minecraft/items/diamond.json      （普通物品）
 * { "model": { "type": "minecraft:model", "model": "minecraft:item/diamond" } }
 * }</pre>
 *
 * <h2>指向哪个模型</h2>
 * <ul>
 *   <li>方块物品 → {@code <ns>:block/<方块id>}（原版 {@code stone} 就是这样）；</li>
 *   <li>普通物品 → {@code models/item/<id>.json} 存在则用 {@code <ns>:item/<id>}，
 *       否则退回 {@code <ns>:block/<id>}（兼容把方块模型当物品模型的写法）。</li>
 * </ul>
 * 只处理本模组命名空间下的物品，不碰原版。
 *
 * <h2>幂等</h2>
 * 与 {@link BlockStateGenerator} 一致：写入前按 SHA-256 比对，内容没变不动文件。
 */
public final class ItemModelGenerator {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * 物品模型定义的<b>例外表</b>：物品 id → 模型路径（相对本模组命名空间）。
	 *
	 * <p>默认规则是「方块物品一律指向 {@code <ns>:block/<方块id>}」，但有些方块物品
	 * 的图标<b>刻意与方块本体的贴图不同</b>（方块要看世界里的实际效果，
	 * 物品栏里则要一张一眼能认出来的图），于是它们有各自的 {@code models/item/<id>.json}：
	 * <ul>
	 *   <li>{@code wi_aerogel} —— 物品用 {@code wi/aerogel_item.png}
	 *       （Alpha 32~140，肉眼更实），方块用 {@code wi/aerogel.png}（Alpha 恒 32，近乎全透）；</li>
	 *   <li>{@code wi_static_liquid_water} —— 物品用 {@code wi/static_liquid_water_mapped.png}
	 *       （已经把水色乘进贴图），方块用 {@code wi/static_liquid_water.png}
	 *       （灰度原图，靠 {@code BlockColorRegistry} 按生物群系上色）；</li>
	 *   <li>{@code bhr_gem_*} 五种宝石 —— 方块是 Blockbench 手写的 3D {@code elements} 模型，
	 *       但<b>物品图标从旧项目起就是一张平面宝石图</b>（{@code models/item/bhr_gem_*.json}
	 *       = {@code parent: item/generated} + {@code layer0}，26.2 这条路依然有效，
	 *       与原版 {@code item/amethyst_shard} 的写法一致）。不走例外表的话，
	 *       物品栏里会变成「一块 3D 宝石」，与旧项目观感不同。</li>
	 *   <li>{@code rdt_spectre_sapling} —— 方块是 {@code block/cross} 的 X 型植物（3D 十字），
	 *       但原版<b>所有 cross 型植物方块的物品形式都是一张 2D 精灵</b>：
	 *       {@code items/oak_sapling.json} → {@code minecraft:item/oak_sapling}，
	 *       而 {@code models/item/oak_sapling.json} = {@code parent: item/generated}
	 *       + {@code layer0: minecraft:block/oak_sapling}（<b>复用同一张方块贴图</b>）。
	 *       花（{@code dandelion}）、甘蔗（{@code sugar_cane}）全是这个写法（26.2 逐条核验）。
	 *       走默认规则会指向 {@code block/rdt_spectre_sapling}，
	 *       于是<b>拿在手上/物品栏里举着一个 3D 十字</b>（2026-10 修正）。
	 *       对比：树叶（{@code oak_leaves}）与原木（{@code oak_log}）原版<b>就是</b>
	 *       指向 {@code block/<id>}，那两块不需要进例外表。</li>
	 * </ul>
	 * 例外只写在这里，不动默认规则 —— {@code DEV} 分支那些「物品模型只是转指方块模型」的
	 * 文件因此完全不受影响。
	 */
	private static final Map<String, String> ITEM_MODEL_OVERRIDES = Map.of(
			"wi_aerogel", "item/wi_aerogel",
			"wi_static_liquid_water", "item/wi_static_liquid_water",
			"bhr_gem_amethyst", "item/bhr_gem_amethyst",
			"bhr_gem_sapphire", "item/bhr_gem_sapphire",
			"bhr_gem_emerald", "item/bhr_gem_emerald",
			"bhr_gem_ruby", "item/bhr_gem_ruby",
			"bhr_gem_diamond", "item/bhr_gem_diamond",
			"rdt_spectre_sapling", "item/rdt_spectre_sapling");

	/**
	 * <b>常量着色表</b>：物品 id → 打包的水色，写进 {@code items/<id>.json} 的 {@code tints} 数组。
	 *
	 * <h2>用途：原版纹理 + 后期上色（不需要新贴图）</h2>
	 * <p>药水共用原版 {@code minecraft:item/potion_overlay} 贴图（一张灰度液体图），
	 * 各自靠 {@code {"type":"minecraft:constant","value":<int>}} 染成不同颜色
	 * —— 与原版药水「一张灰度图 + potion tint 上色」同一个原理，
	 * 只是我们不需要真的进药水注册表，所以用常量色源。
	 *
	 * <h2>⚠️ 为什么必须写在这里，而不是手写那个 json</h2>
	 * <p>{@code items/<id>.json} 是<b>本生成器独占的产出</b>：它对每个物品都会重写一遍。
	 * 任何手写进 {@code items/} 的额外字段（比如 {@code tints}）<b>都会在下一次 datagen 时被抹掉</b>
	 * —— 本项目实测踩过：手写过 tints 的药水在跑过一次 datagen 后全变成灰度瓶（看着像空瓶）。
	 * 想给物品定义加字段，就得让生成器知道。
	 *
	 * <h2>色值怎么给</h2>
	 * <p>用效果的粒子颜色，做到瓶色与粒子一致。记录在
	 * {@code item.ClassWhimsyIdea.potion.WI_PotionFamily} 与
	 * {@code item.ClassRandomThings.RDT_FireHormonePotion} 等条目类里。
	 * 这里用<b>前缀匹配</b>而不是逐条列出：药水一族有 18 件（形态 × 等级），
	 * 逐条写容易漏；前缀能把整族一次覆盖。
	 */
	private static final Map<String, Integer> ITEM_TINT_PREFIXES = Map.of(
			// WI：瘟疫（生物毒性 0x007951）与放射（电离辐射 0xF3A923）
			"wi_plague", 0x007951,
			"wi_radiation", 0xF3A923);
	// ⚠️ RDT 的两个激素药水<b>不在此表</b>：用户手绘了瓶身，要求「仅显示原图、不要 tint」。
	// 若以后想给它们上色，往这里加前缀即可（贴图本身会变成叠加色）。

	/**
	 * <b>药水形态的模型后缀</b>：物品 id 里含这些片段时，模型按「双层药水」处理。
	 *
	 * <p>键 = id 里出现的片段，值 = 瓶身贴图名（{@code potion} / {@code splash_potion} / {@code lingering_potion}）。
	 * 例如 {@code wi_plague_splash_potion_3} 命中 {@code "_splash_potion"}。
	 *
	 * <p>顺序敏感：先匹配 {@code _splash_potion} 与 {@code _lingering_potion}，
	 * 最后才是万能的 {@code _potion}。
	 */
	private static final Map<String, String> POTION_FORM_SUFFIXES = Map.of(
			"_splash_potion", "splash_potion",
			"_lingering_potion", "lingering_potion",
			"_potion", "potion");

	/**
	 * <b>药水瓶身贴图覆盖表</b>：物品 id 前缀 → 自定义瓶身贴图路径（相对本模组命名空间，省略 {@code .png}）。
	 *
	 * <h2>用途</h2>
	 * <p>默认所有药水都用原版瓶身（{@code minecraft:item/potion} 等）；
	 * 本表让某族药水换成<b>自己画的瓶子</b>。
	 * 瓶身放在 {@code layer1}（不染色），{@code layer0} 仍是原版
	 * {@code potion_overlay}（液面，被 tints 染色）——
	 * 于是「自己画的瓶子 + 按效果配色的液面」，与原版药水的分层完全一致。
	 *
	 * <p>RDT 的两个激素药水用这条：贴图在
	 * {@code assets/richards-whimsy-mod/textures/item/rdt/potion_fire_imbue.png} 与
	 * {@code potion_poison_imbue.png}（用户手绘）。
	 */
	private static final Map<String, String> POTION_BOTTLE_OVERRIDES = Map.of(
			"rdt_fire_hormone_potion", "richards-whimsy-mod:item/rdt/potion_fire_imbue",
			"rdt_poison_hormone_potion", "richards-whimsy-mod:item/rdt/potion_poison_imbue");

	private ItemModelGenerator() {
	}

	/**
	 * 为全部本模组物品生成模型定义。
	 *
	 * @param modRoot 本模组在运行时的资源根（含 {@code assets/} 的那一级）
	 * @return 实际写入的文件数
	 */
	public static int generate(Path modRoot) throws IOException {
		String namespace = RichardsWhimsyMod.MOD_ID;

		// 先把「有没有手写的 models/item/<id>.json」扫一遍，避免每个物品都去查磁盘。
		Path itemModelsDir = modRoot.resolve("assets").resolve(namespace).resolve("models").resolve("item");
		Map<String, Boolean> hasItemModel = new HashMap<>();

		int written = 0;
		int plainItems = 0;

		for (Item item : BuiltInRegistries.ITEM) {
			Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
			if (!namespace.equals(itemId.getNamespace())) {
				continue;
			}

			String modelPath;
			String override = ITEM_MODEL_OVERRIDES.get(itemId.getPath());
			boolean potionModel = false;
			if (override != null) {
				// 例外：该方块物品有自己的模型（见 ITEM_MODEL_OVERRIDES）
				modelPath = namespace + ":" + override;
			} else if (findPotionForm(itemId.getPath()) != null) {
				// 药水：双层模型（overlay + 瓶身），由本生成器生成 models/item/<id>.json
				modelPath = namespace + ":item/" + itemId.getPath();
				potionModel = true;
			} else if (item instanceof BlockItem blockItem) {
				// 方块物品：指向方块模型（原版 stone → block/stone）
				Block block = blockItem.getBlock();
				Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
				modelPath = blockId.getNamespace() + ":block/" + blockId.getPath();
			} else {
				// 普通物品：优先 models/item/<id>.json
				boolean exists = hasItemModel.computeIfAbsent(itemId.getPath(),
						name -> Files.exists(itemModelsDir.resolve(name + ".json")));
				modelPath = exists
						? namespace + ":item/" + itemId.getPath()
						: namespace + ":block/" + itemId.getPath();
				plainItems++;
			}

			if (potionModel) {
				// 先落双层模型文件，再让 items 定义指向它。
				writePotionItemModel(modRoot, namespace, itemId.getPath());
			}

			JsonObject model = new JsonObject();
			model.addProperty("type", "minecraft:model");
			model.addProperty("model", modelPath);

			// 常量着色（见 ITEM_TINT_PREFIXES）：写成原版 potion 定义那种形状的 tints 数组。
			// 26.2 的物品 tint 是数据驱动的（ItemTintSources 注册了 minecraft:constant），
			// 所以这里只写数据，不需要任何客户端着色代码。
			Integer tint = findTint(itemId.getPath());
			if (tint != null) {
				JsonObject tintSource = new JsonObject();
				tintSource.addProperty("type", "minecraft:constant");
				tintSource.addProperty("value", tint);

				JsonArray tints = new JsonArray();
				tints.add(tintSource);
				model.add("tints", tints);
			}

			JsonObject root = new JsonObject();
			root.add("model", model);

			byte[] content = GSON.toJson(root).getBytes(StandardCharsets.UTF_8);

			Path target = modRoot
					.resolve("assets")
					.resolve(namespace)
					.resolve("items")
					.resolve(itemId.getPath() + ".json");

			if (writeIfChanged(target, content)) {
				written++;
			}
		}

		RichardsWhimsyMod.LOGGER.info(
				"[datagen] 物品模型定义生成完毕：写入 {} 个（其中非方块物品 {} 个）",
				written, plainItems);
		return written;
	}

	/**
	 * 判断物品 id 属于哪种药水形态，命中则返回瓶身贴图名。
	 *
	 * <p>顺序敏感：先查 {@code _splash_potion} / {@code _lingering_potion}，
	 * 最后才是万能的 {@code _potion}（{@code wi_plague_splash_potion_3} 含 {@code _potion}，
	 * 若先匹配它就会被误判成饮用型）。
	 */
	private static String findPotionForm(String itemPath) {
		if (itemPath.contains("_splash_potion")) {
			return "splash_potion";
		}
		if (itemPath.contains("_lingering_potion")) {
			return "lingering_potion";
		}
		if (itemPath.contains("_potion")) {
			return "potion";
		}
		return null;
	}

	/** 按前缀查着色值；命中一族即返回（见 {@link #ITEM_TINT_PREFIXES}）。 */
	private static Integer findTint(String itemPath) {
		for (Map.Entry<String, Integer> entry : ITEM_TINT_PREFIXES.entrySet()) {
			if (itemPath.startsWith(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}

	/** 按前缀查自定义瓶身贴图；没有则 null（用原版瓶子）。 */
	private static String findPotionBottle(String itemPath) {
		for (Map.Entry<String, String> entry : POTION_BOTTLE_OVERRIDES.entrySet()) {
			if (itemPath.startsWith(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * 写药水的<b>双层模型</b>文件到 {@code models/item/<id>.json}。
	 *
	 * <h2>为什么药水必须是双层</h2>
	 * <p>原版的药水外观由两层叠成（逐字读过原版模型）：
	 * <pre>{@code
	 * // 原版 models/item/potion.json
	 * { "parent": "minecraft:item/generated",
	 *   "textures": { "layer0": "minecraft:item/potion_overlay",   // ← 瓶里的液体（灰度，靠 tint 上色）
	 *                 "layer1": "minecraft:item/potion" } }        // ← 玻璃瓶本身（不染色）
	 * }</pre>
	 * <b>要染的是 layer0（液体），不是瓶子</b>。本项目踩过这个坑：把单层 {@code layer0} 指成
	 * {@code item/potion}（瓶子）再染色，结果整只瓶子被染、看起来仍像空瓶。
	 *
	 * <p>{@code tints} 按<b>顺序</b>对应各层，所以只写一个 tint 时它作用在 layer0（液体）上 ——
	 * 这正是我们要的效果。喷溅型与滞留型同理，只是 layer1 换成各自的瓶身贴图。
	 *
	 * <h2>自己画的瓶子走「单层、不上色」</h2>
	 * <p>若该物品在 {@link #POTION_BOTTLE_OVERRIDES} 里有自定义瓶身，就<b>不套原版双层</b>，
	 * 而是<b>单层直接用那张图</b>：
	 * <ul>
	 *   <li>用户手绘的贴图本身就是完整成品，再叠一层 {@code potion_overlay} 会把它盖住；</li>
	 *   <li>且该族<b>不配 tints</b>（需求是「仅显示原图」），所以不需要「哪一层被染」这套结构。</li>
	 * </ul>
	 * 单层 + 无 tints 时，原版不会对任何一层上色，显示的就是贴图原样。
	 */
	private static void writePotionItemModel(Path modRoot, String namespace, String itemPath)
			throws IOException {
		String form = findPotionForm(itemPath);
		if (form == null) {
			return;
		}

		JsonObject textures = new JsonObject();
		String customBottle = findPotionBottle(itemPath);

		if (customBottle != null) {
			// 自绘瓶身：单层，不上色（原图直出）
			textures.addProperty("layer0", customBottle);
		} else {
			// 原版瓶身：双层 —— layer0 液体（被 tints 染色）、layer1 瓶身（不染色）
			textures.addProperty("layer0", "minecraft:item/potion_overlay");
			textures.addProperty("layer1", "minecraft:item/" + form);
		}

		JsonObject root = new JsonObject();
		root.addProperty("parent", "minecraft:item/generated");
		root.add("textures", textures);

		byte[] content = GSON.toJson(root).getBytes(StandardCharsets.UTF_8);
		Path target = modRoot.resolve("assets").resolve(namespace)
				.resolve("models").resolve("item").resolve(itemPath + ".json");
		writeIfChanged(target, content);
	}

	/** 内容不变就不写（保持时间戳稳定）。返回是否真的写了。 */
	private static boolean writeIfChanged(Path target, byte[] content) throws IOException {
		if (Files.exists(target)) {
			byte[] existing = Files.readAllBytes(target);
			if (MessageDigest.isEqual(sha256(existing), sha256(content))) {
				return false;
			}
		}
		if (target.getParent() != null) {
			Files.createDirectories(target.getParent());
		}
		Files.write(target, content);
		return true;
	}

	private static byte[] sha256(byte[] data) {
		try {
			return MessageDigest.getInstance("SHA-256").digest(data);
		} catch (Exception e) {
			throw new IllegalStateException("SHA-256 不可用", e);
		}
	}
}
