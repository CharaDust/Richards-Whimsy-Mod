package com.rcdmake.whimsy.datagen.ClassWhimsyIdea;

import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_Aerogel;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_GlassSugar;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_PackedPlanks;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_StaticCobblestone;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_StaticLiquidWater;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_SugarBlock;
import com.rcdmake.whimsy.datagen.LangEntrySupport;
import com.rcdmake.whimsy.item.ClassWhimsyIdea.potion.WI_PlaguePotion;
import com.rcdmake.whimsy.item.ClassWhimsyIdea.potion.WI_PotionEntry;
import com.rcdmake.whimsy.item.ClassWhimsyIdea.potion.WI_RadiationPotion;
import com.rcdmake.whimsy.item.WI_CreativeTabs;
import com.rcdmake.whimsy.item.WhimsyPotionRegistration.Form;
import com.rcdmake.whimsy.statusEffect.ClassWhimsyIdea.WI_BioToxicity;
import com.rcdmake.whimsy.statusEffect.ClassWhimsyIdea.WI_Radioactive;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

import java.util.List;

/**
 * 奇思妙想（WI）分支的<b>语言条目</b>（中英各一套）。
 *
 * <p>为何是静态条目方法而不是 provider：见 {@code DEV_LangEntries} 的类注释
 * （26.2 同一语言码只允许一个 provider）。本类的条目由模组级
 * {@code datagen/ModLangProviderZh} 与 {@code ModLangProviderEn} 调用。
 *
 * <p>方块与它的方块物品要各写一条（{@code block.…} 与 {@code item.…} 是两个键）——
 * 这一步统一交给 {@link LangEntrySupport#addBlockAndItem}。
 *
 * <p>状态效果要写「名称 + 描述」两条键（描述不会自动生成），
 * 用 {@link LangEntrySupport#addEffect} / {@code addEffectDescription}。
 */
public final class WI_LangEntries {

	private WI_LangEntries() {
	}

	/** 简体中文条目。 */
	public static void zh(FabricLanguageProvider.TranslationBuilder builder) {
		// 创造模式物品栏标题（方块页 + 物品页）
		builder.add(WI_CreativeTabs.LANG_KEY_BLOCK, "奇思妙想方块");
		builder.add(WI_CreativeTabs.LANG_KEY_ITEM, "奇思妙想物品");

		// --- 方块 ---
		LangEntrySupport.addBlockAndItem(builder, WI_StaticCobblestone.WI_STATIC_COBBLESTONE, "固石");
		LangEntrySupport.addBlockAndItem(builder, WI_PackedPlanks.WI_PACKED_PLANKS, "包装木材");
		LangEntrySupport.addBlockAndItem(builder, WI_Aerogel.WI_AEROGEL, "气凝胶");
		LangEntrySupport.addBlockAndItem(builder, WI_SugarBlock.WI_SUGARBLOCK, "糖块");
		LangEntrySupport.addBlockAndItem(builder, WI_GlassSugar.WI_GLASSSUGAR, "糖玻璃");
		LangEntrySupport.addBlockAndItem(builder, WI_StaticLiquidWater.WI_STATIC_LIQUID_WATER, "固化水方块");

		// --- 状态效果（名称 + 描述）---
		LangEntrySupport.addEffect(builder, WI_Radioactive.WI_RADIOACTIVE.value(), "电离辐射");
		LangEntrySupport.addEffectDescription(builder, WI_Radioactive.WI_RADIOACTIVE.value(),
				"按等级每 100/80/60/40/20 tick 造成 2 点火焰伤害，并减少 15%/30%/45%/60% 生命值上限；等级 II 起，攻击你的生物会被反向传染");
		LangEntrySupport.addEffect(builder, WI_BioToxicity.WI_BIO_TOXICITY.value(), "生物毒性");
		LangEntrySupport.addEffectDescription(builder, WI_BioToxicity.WI_BIO_TOXICITY.value(),
				"按等级每 80/60/40/20 tick 造成 2 点魔法伤害，且不会把生命值打到 6 点以下；攻击时会把毒性传染给目标（部分构装生物免疫）");

		// --- 药水（瘟疫 / 放射，各三级形态）---
		// 名称<b>不带等级</b>：同一效果的各级共用一个名字，等级与时长由 lore 显示
		// （原版药水也是这样：名字只说「治疗药水」，具体等级在提示里）。
		// 形态按标准译名作<b>前缀</b>：喷溅型 / 滞留型。
		addPotions(builder, "瘟疫药水", "Potion of Plague", WI_PlaguePotion.ENTRIES, true);
		addPotions(builder, "放射药水", "Potion of Radiation", WI_RadiationPotion.ENTRIES, true);
	}

	/** 英文条目（与 {@link #zh} 同一批键）。 */
	public static void en(FabricLanguageProvider.TranslationBuilder builder) {
		builder.add(WI_CreativeTabs.LANG_KEY_BLOCK, "Whimsy Blocks");
		builder.add(WI_CreativeTabs.LANG_KEY_ITEM, "Whimsy Items");

		// --- 方块 ---
		LangEntrySupport.addBlockAndItem(builder, WI_StaticCobblestone.WI_STATIC_COBBLESTONE, "Static Cobblestone");
		LangEntrySupport.addBlockAndItem(builder, WI_PackedPlanks.WI_PACKED_PLANKS, "Packed Planks");
		LangEntrySupport.addBlockAndItem(builder, WI_Aerogel.WI_AEROGEL, "Aerogel");
		LangEntrySupport.addBlockAndItem(builder, WI_SugarBlock.WI_SUGARBLOCK, "Sugar Block");
		LangEntrySupport.addBlockAndItem(builder, WI_GlassSugar.WI_GLASSSUGAR, "Sugar Glass");
		LangEntrySupport.addBlockAndItem(builder, WI_StaticLiquidWater.WI_STATIC_LIQUID_WATER, "Static Liquid Water");

		// --- 状态效果 ---
		LangEntrySupport.addEffect(builder, WI_Radioactive.WI_RADIOACTIVE.value(), "Ionizing Radiation");
		LangEntrySupport.addEffectDescription(builder, WI_Radioactive.WI_RADIOACTIVE.value(),
				"Deals 2 fire damage every 100/80/60/40/20 ticks by level and lowers your max health by 15/30/45/60%. From level II on, creatures that strike you catch a weaker dose.");
		LangEntrySupport.addEffect(builder, WI_BioToxicity.WI_BIO_TOXICITY.value(), "Bio Toxicity");
		LangEntrySupport.addEffectDescription(builder, WI_BioToxicity.WI_BIO_TOXICITY.value(),
				"Deals 2 magic damage every 80/60/40/20 ticks by level and never drops the target below 6 health. Your attacks pass it on (some constructs are immune).");

		// --- 药水 ---
		addPotions(builder, "瘟疫药水", "Potion of Plague", WI_PlaguePotion.ENTRIES, false);
		addPotions(builder, "放射药水", "Potion of Radiation", WI_RadiationPotion.ENTRIES, false);
	}

	/**
	 * 批量登记一个药水族（三种形态）的名称。
	 *
	 * <h2>命名格式：形态是<b>前缀</b>，不是后缀</h2>
	 * <p>按中文标准译名指南，喷溅/滞留药水写作「<b>喷溅型</b>XX药水」「<b>滞留型</b>XX药水」，
	 * 英文则是 {@code Splash}/{@code Lingering} <b>前缀</b>。
	 * 本项目曾写成后缀（「瘟疫药水（喷溅）」），已改正。
	 * 药水形态的中英对照：
	 * <pre>{@code
	 * 饮用型 Potion       （中文无前缀，英文正面写 Potion of X）
	 * 喷溅型 Splash        → 喷溅型瘟疫药水 / Splash Potion of Plague
	 * 滞留型 Lingering     → 滞留型瘟疫药水 / Lingering Potion of Plague
	 * }</pre>
	 *
	 * <p><b>不含等级</b>：同一效果的各等级共用一个名字（等级在 lore 里显示）。
	 * 但<b>每个物品都要登记一条</b> —— 语言键按物品 id 生成，名字相同、键不同。
	 *
	 * @param zhBase 中文基名（形如「瘟疫药水」）
	 * @param enBase 英文基名（形如 {@code "Potion of Plague"}）
	 * @param entries 该族登记的物品（含形态与等级）
	 * @param zh      true = 走中文命名，false = 走英文命名
	 */
	private static void addPotions(FabricLanguageProvider.TranslationBuilder builder,
			String zhBase, String enBase, List<WI_PotionEntry> entries, boolean zh) {
		for (WI_PotionEntry entry : entries) {
			String name = zh
					? zhFormPrefix(entry) + zhBase
					: enFormPrefix(entry) + enBase;
			builder.add(entry.item(), name);
		}
	}

	/** 中文形态前缀：饮用型不加，喷溅/滞留按标准译名加「喷溅型」「滞留型」。 */
	private static String zhFormPrefix(WI_PotionEntry entry) {
		return switch (entry.form()) {
			case DRINK -> "";
			case SPLASH -> "喷溅型";
			case LINGERING -> "滞留型";
		};
	}

	/** 英文形态前缀（对齐原版 {@code Splash Potion of …} 的语序）。 */
	private static String enFormPrefix(WI_PotionEntry entry) {
		return switch (entry.form()) {
			case DRINK -> "";
			case SPLASH -> "Splash ";
			case LINGERING -> "Lingering ";
		};
	}
}
