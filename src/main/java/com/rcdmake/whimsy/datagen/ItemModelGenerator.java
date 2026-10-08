package com.rcdmake.whimsy.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
			"bhr_gem_diamond", "item/bhr_gem_diamond");

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
			if (override != null) {
				// 例外：该方块物品有自己的模型（见 ITEM_MODEL_OVERRIDES）
				modelPath = namespace + ":" + override;
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

			JsonObject model = new JsonObject();
			model.addProperty("type", "minecraft:model");
			model.addProperty("model", modelPath);

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
