package com.rcdmake.whimsy.surface;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一档燃料：界面上左侧一列凹槽各对应一档。
 *
 * <p>从 {@code BHR_Campfire.Fuel} 搬进公共包（现在燃料是「工作面」的可选能力，不再只属于篝火）。
 *
 * @param candidates 显式候选（按顺序；也是标签展开的起点，见 {@link #allCandidates()}）
 * @param units      每件折算的单位燃烧时间（1 单位 = {@code spec.fuelUnitTicks()} tick）
 * @param tag        额外接受的标签（展开进候选，{@code null} = 本档只看候选列表）
 */
public record FuelOption(List<Item> candidates, int units, TagKey<Item> tag) {

	/** 「显式候选 + 标签展开」的缓存（界面每帧都会问一次，不缓存会白烧 CPU）。 */
	private static final Map<FuelOption, List<Item>> CANDIDATE_CACHE = new ConcurrentHashMap<>();

	/** 只要候选、不看标签的写法（木棍 / 松果两档用它）。 */
	public FuelOption(List<Item> candidates, int units) {
		this(candidates, units, null);
	}

	/**
	 * 这一档能接受的全部具体物品：<b>先枚举候选、后标签展开</b>（去重），顺序稳定。
	 *
	 * <p>与需求九宫格的 {@code BHR_FoolIngredient#candidates()} 同一套语义：
	 * 只因标签而入档的物品（如本模组的果木木板）<b>也会进右键轮换</b>。
	 */
	public List<Item> allCandidates() {
		return CANDIDATE_CACHE.computeIfAbsent(this, fuel -> {
			List<Item> result = new ArrayList<>(fuel.candidates);
			if (fuel.tag != null) {
				// 26.2 没有 Registry#getTag：遍历物品注册表挑出命中标签的
				for (Item item : BuiltInRegistries.ITEM) {
					if (!result.contains(item)
							&& BuiltInRegistries.ITEM.wrapAsHolder(item).is(fuel.tag)) {
						result.add(item);
					}
				}
			}
			return List.copyOf(result);
		});
	}

	/** 这一档里是否有多种可选物品（有 → 界面显示替换标记、右键可切换首选）。 */
	public boolean replaceable() {
		return this.allCandidates().size() > 1;
	}

	/** 界面图标：{@code preferredIndex} 超出范围时退回第一个候选。 */
	public ItemStack icon(int preferredIndex) {
		List<Item> all = this.allCandidates();
		int index = preferredIndex >= 0 && preferredIndex < all.size() ? preferredIndex : 0;
		return all.isEmpty() ? ItemStack.EMPTY : new ItemStack(all.get(index));
	}

	/** 默认图标（第一个候选）。 */
	public ItemStack icon() {
		return this.icon(0);
	}

	/** 这一档是否收这件物品。 */
	public boolean accepts(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		for (Item candidate : this.allCandidates()) {
			if (stack.is(candidate)) {
				return true;
			}
		}
		return false;
	}

	/** 某个具体物品在这一档里的下标（不在候选里返回 -1）。 */
	public int indexOf(Item item) {
		return item == null ? -1 : this.allCandidates().indexOf(item);
	}
}
