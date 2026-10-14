package com.rcdmake.whimsy.item.ClassWhimsyIdea.potion;

import com.rcdmake.whimsy.item.WhimsyPotionRegistration.Form;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * 一件已注册的药水：来自哪个形态、哪一级。
 *
 * <p>各药水条目类用它记录自己注册出的物品，供物品栏与 datagen 遍历
 * —— 这样「有哪些物品」只有一处真相源，不会出现「注册了但物品栏漏加」。
 */
public record WI_PotionEntry(Form form, int level, Item item) {

	/**
	 * 从清单里挑出某个等级、某个形态的物品。
	 *
	 * <p>给「按等级取饮用型」这类场景用（例如语言条目要按级别命名），
	 * 避免调用方去猜列表下标。
	 *
	 * @return 命中的物品；没有则 null
	 */
	public static Item pick(List<WI_PotionEntry> entries, Form form, int level) {
		for (WI_PotionEntry entry : entries) {
			if (entry.form() == form && entry.level() == level) {
				return entry.item();
			}
		}
		return null;
	}
}
