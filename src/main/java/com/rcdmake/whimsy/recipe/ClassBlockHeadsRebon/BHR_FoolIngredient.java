package com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 愚者配方的一条<b>原料需求</b>：可以是某件物品、某个物品标签，或「任选其一」的一组。
 *
 * <h2>JSON 写法（三种任选其一，可混用）</h2>
 * <pre>{@code
 * { "item": "minecraft:stick", "count": 1 }                              // 单件物品
 * { "tag": "minecraft:planks", "count": 1 }                              // 整个物品标签
 * { "any": ["minecraft:cobblestone", "minecraft:blackstone",
 *           "#richards-whimsy-mod:slate"], "count": 3 }                  // 任选其一（# 开头是标签）
 * }</pre>
 * <ul>
 *   <li>{@code count} 可省略，默认 1；<b>同一个格子里的需求量可以是多个</b>
 *       （这是「九合一」之外的另一条规则：一格 = 一类物品 × N 个）；</li>
 *   <li>{@code any} 里的字符串：以 {@code #} 开头当标签解析，否则当物品 id 解析；</li>
 *   <li>三种键可以同时出现，最终取并集（都算「满足」）。</li>
 * </ul>
 *
 * <p>为什么是「并集」而不是「只能写一种」：用户要的「板岩 / 黑石可代替圆石」是<b>同类替换</b>，
 * 而标签（如 {@code #minecraft:planks}）与枚举列表（三个具体物品）在语义上等价，
 * 两种写法都留着，写数据时怎么方便怎么来。
 */
public record BHR_FoolIngredient(List<Item> items, List<TagKey<Item>> tags, int count) {

	/** 候选列表缓存（records 按值比较，作为 key 正好合适）。 */
	private static final java.util.Map<BHR_FoolIngredient, List<Item>> CANDIDATE_CACHE =
			new java.util.concurrent.ConcurrentHashMap<>();

	/** 该物品堆能否充当这条需求（只看种类，不看数量）。 */
	public boolean accepts(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		for (Item item : this.items) {
			if (stack.is(item)) {
				return true;
			}
		}
		for (TagKey<Item> tag : this.tags) {
			if (stack.is(tag)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 这条需求能接受的全部具体物品（枚举列表 + 标签展开），顺序稳定：先枚举、后标签。
	 * <p>「可替换」= 这里大于 1 个；界面据此画 {@code slot_alt.png} 遮罩，右键在它们之间切换首选。
	 */
	public List<Item> candidates() {
		// 界面每帧都会问一次，所以缓存结果（配方数据是静态的，不会变）
		return CANDIDATE_CACHE.computeIfAbsent(this, ingredient -> {
			List<Item> result = new ArrayList<>(ingredient.items);
			for (TagKey<Item> tag : ingredient.tags) {
				for (Item item : BuiltInRegistries.ITEM) {
					if (!result.contains(item) && BuiltInRegistries.ITEM.wrapAsHolder(item).is(tag)) {
						result.add(item);
					}
				}
			}
			return List.copyOf(result);
		});
	}

	/** 是否「可以用别的物品替换」（多于一种候选）。 */
	public boolean hasChoices() {
		return this.candidates().size() > 1;
	}

	/** 供界面/日志显示的代表性物品堆（取第一个物品；纯标签时取注册表里第一个命中的物品）。 */
	public ItemStack preview() {
		ItemStack preview = firstItem();
		return preview;
	}

	private ItemStack firstItem() {
		if (!this.items.isEmpty()) {
			return new ItemStack(this.items.get(0));
		}
		// 26.2 没有 Registry#getTag：直接遍历物品注册表找第一个命中标签的
		// （只用于界面预览，物品总数几百，代价可忽略）。
		for (TagKey<Item> tag : this.tags) {
			for (Item item : BuiltInRegistries.ITEM) {
				ItemStack candidate = new ItemStack(item);
				if (candidate.is(tag)) {
					return candidate;
				}
			}
		}
		return ItemStack.EMPTY;
	}

	/**
	 * 从 JSON 解析一条需求。
	 *
	 * @throws IllegalArgumentException 出现未知物品 id、或三种键一个都没写
	 */
	public static BHR_FoolIngredient parse(JsonObject json) {
		List<Item> items = new ArrayList<>();
		List<TagKey<Item>> tags = new ArrayList<>();

		if (json.has("item")) {
			items.add(item(json.get("item").getAsString()));
		}
		if (json.has("tag")) {
			tags.add(tag(json.get("tag").getAsString()));
		}
		if (json.has("any")) {
			JsonArray any = json.getAsJsonArray("any");
			for (JsonElement element : any) {
				String value = element.getAsString();
				if (value.startsWith("#")) {
					tags.add(tag(value.substring(1)));
				} else {
					items.add(item(value));
				}
			}
		}
		if (items.isEmpty() && tags.isEmpty()) {
			throw new IllegalArgumentException("原料需求里没有 item / tag / any：" + json);
		}

		int count = json.has("count") ? json.get("count").getAsInt() : 1;
		if (count < 1) {
			throw new IllegalArgumentException("原料数量必须 >= 1：" + json);
		}
		return new BHR_FoolIngredient(List.copyOf(items), List.copyOf(tags), count);
	}

	private static Item item(String id) {
		Identifier location = Identifier.parse(id);
		Item item = BuiltInRegistries.ITEM.getValue(location);
		if (item == null || item == Items.AIR) {
			throw new IllegalArgumentException("未知物品 id：" + id);
		}
		return item;
	}

	private static TagKey<Item> tag(String id) {
		return TagKey.create(Registries.ITEM, Identifier.parse(id));
	}

	/** 日志/自检用的可读形式。 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Item item : this.items) {
			if (!first) {
				sb.append('|');
			}
			sb.append(BuiltInRegistries.ITEM.getKey(item));
			first = false;
		}
		for (TagKey<Item> tag : this.tags) {
			if (!first) {
				sb.append('|');
			}
			sb.append('#').append(tag.location());
			first = false;
		}
		return sb.append(" x").append(this.count).toString();
	}
}
