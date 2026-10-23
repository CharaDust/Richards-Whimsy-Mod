package com.rcdmake.whimsy.datagen.ClassRandomThings;

import com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassLapis;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassQuartz;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_LampLapis;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_LampQuartz;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_LuminousBlocks;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_SpectreLens;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_StepBlock;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_SuperLubricentIce;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_TreeLeavesSpectre;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_TreeLogSpectre;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_TreePlanksSpectre;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_TreeSaplingSpectre;
import com.rcdmake.whimsy.datagen.LangEntrySupport;
import com.rcdmake.whimsy.item.ClassRandomThings.RDT_FireHormonePotion;
import com.rcdmake.whimsy.item.ClassRandomThings.RDT_PoisonHormonePotion;
import com.rcdmake.whimsy.item.RDT_CreativeTabs;
import com.rcdmake.whimsy.statusEffect.ClassRandomThings.RDT_FireImbue;
import com.rcdmake.whimsy.statusEffect.ClassRandomThings.RDT_PoisonImbue;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * 随意物件（RDT）分支的<b>语言条目</b>（中英各一套）。
 *
 * <p>为何是静态条目方法而不是 provider：见 {@code DEV_LangEntries} 的类注释
 * （26.2 同一语言码只允许一个 provider）。本类的条目由模组级
 * {@code datagen/ModLangProviderZh} 与 {@code ModLangProviderEn} 调用。
 *
 * <p>方块与它的方块物品要各写一条（{@code block.…} 与 {@code item.…} 是两个键，漏写会显示裸键名）——
 * 这一步统一交给 {@link LangEntrySupport#addBlockAndItem}（阶段 0 起三个模块共用一份）。
 *
 * <p>名称来源：优先取旧语言文件的历史值（{@code rdt_general} 的标题、超级滑滑冰的英文名），
 * 其余按 wiki 各页标题译名。
 */
public final class RDT_LangEntries {

	private RDT_LangEntries() {
	}

	/** 简体中文条目。 */
	public static void zh(FabricLanguageProvider.TranslationBuilder builder) {
		// 创造模式物品栏标题（沿用旧语言文件的历史值）
		builder.add(RDT_CreativeTabs.LANG_KEY, "随意作品：续集？");

		LangEntrySupport.addBlockAndItem(builder, RDT_SuperLubricentIce.RDT_SUPER_LUBRICENT_ICE, "超级滑滑冰");
		LangEntrySupport.addBlockAndItem(builder, RDT_GlassLapis.RDT_LAPIS_GLASS, "青金石玻璃");
		LangEntrySupport.addBlockAndItem(builder, RDT_GlassQuartz.RDT_QUARTZ_GLASS, "石英玻璃");
		LangEntrySupport.addBlockAndItem(builder, RDT_LampLapis.RDT_LAPIS_LAMP, "青金石灯");
		LangEntrySupport.addBlockAndItem(builder, RDT_LampQuartz.RDT_QUARTZ_LAMP, "石英灯");
		LangEntrySupport.addBlockAndItem(builder, RDT_StepBlock.RDT_STEP_BLOCK, "垫脚方块");
		LangEntrySupport.addBlockAndItem(builder, RDT_TreeLogSpectre.RDT_SPECTRE_LOG, "通灵原木");
		LangEntrySupport.addBlockAndItem(builder, RDT_TreePlanksSpectre.RDT_SPECTRE_PLANKS, "通灵木板");
		LangEntrySupport.addBlockAndItem(builder, RDT_TreeLeavesSpectre.RDT_SPECTRE_LEAVES, "通灵树叶");
		LangEntrySupport.addBlockAndItem(builder, RDT_TreeSaplingSpectre.RDT_SPECTRE_SAPLING, "通灵树苗");
		LangEntrySupport.addBlockAndItem(builder, RDT_SpectreLens.RDT_SPECTRE_LENS, "通灵镜片");
		LangEntrySupport.addBlockAndItem(builder, RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_RED, "红色鲁米诺方块");
		LangEntrySupport.addBlockAndItem(builder, RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_GREEN, "绿色鲁米诺方块");
		LangEntrySupport.addBlockAndItem(builder, RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_BLUE, "蓝色鲁米诺方块");

		// --- 状态效果（名称 + 描述两条键）---
		LangEntrySupport.addEffect(builder, RDT_FireImbue.RDT_FIRE_IMBUE.value(), "火焰灌注");
		LangEntrySupport.addEffectDescription(builder, RDT_FireImbue.RDT_FIRE_IMBUE.value(),
				"攻击时点燃目标，燃烧时间随效果等级提升（每级 100 tick）");
		LangEntrySupport.addEffect(builder, RDT_PoisonImbue.RDT_POISON_IMBUE.value(), "毒性灌注");
		LangEntrySupport.addEffectDescription(builder, RDT_PoisonImbue.RDT_POISON_IMBUE.value(),
				"攻击时使目标中毒，中毒时长与等级随效果等级提升（等级 III 起为中毒 II）");

		// --- 激素药水（饮用型，3 级 ×2 味）---
		addHormones(builder, "火焰激素药水", RDT_FireHormonePotion.ALL);
		addHormones(builder, "毒性激素药水", RDT_PoisonHormonePotion.ALL);
	}

	/** 英文条目（与 {@link #zh} 同一批键）。 */
	public static void en(FabricLanguageProvider.TranslationBuilder builder) {
		builder.add(RDT_CreativeTabs.LANG_KEY, "Random Things: sequel?");

		LangEntrySupport.addBlockAndItem(builder, RDT_SuperLubricentIce.RDT_SUPER_LUBRICENT_ICE, "Super Lubricent Ice");
		LangEntrySupport.addBlockAndItem(builder, RDT_GlassLapis.RDT_LAPIS_GLASS, "Lapis Glass");
		LangEntrySupport.addBlockAndItem(builder, RDT_GlassQuartz.RDT_QUARTZ_GLASS, "Quartz Glass");
		LangEntrySupport.addBlockAndItem(builder, RDT_LampLapis.RDT_LAPIS_LAMP, "Lapis Lamp");
		LangEntrySupport.addBlockAndItem(builder, RDT_LampQuartz.RDT_QUARTZ_LAMP, "Quartz Lamp");
		LangEntrySupport.addBlockAndItem(builder, RDT_StepBlock.RDT_STEP_BLOCK, "Step Block");
		LangEntrySupport.addBlockAndItem(builder, RDT_TreeLogSpectre.RDT_SPECTRE_LOG, "Spectre Log");
		LangEntrySupport.addBlockAndItem(builder, RDT_TreePlanksSpectre.RDT_SPECTRE_PLANKS, "Spectre Planks");
		LangEntrySupport.addBlockAndItem(builder, RDT_TreeLeavesSpectre.RDT_SPECTRE_LEAVES, "Spectre Leaves");
		LangEntrySupport.addBlockAndItem(builder, RDT_TreeSaplingSpectre.RDT_SPECTRE_SAPLING, "Spectre Sapling");
		LangEntrySupport.addBlockAndItem(builder, RDT_SpectreLens.RDT_SPECTRE_LENS, "Spectre Lens");
		LangEntrySupport.addBlockAndItem(builder, RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_RED, "Red Luminous Block");
		LangEntrySupport.addBlockAndItem(builder, RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_GREEN, "Green Luminous Block");
		LangEntrySupport.addBlockAndItem(builder, RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_BLUE, "Blue Luminous Block");

		// --- 状态效果（名称 + 描述两条键）---
		LangEntrySupport.addEffect(builder, RDT_FireImbue.RDT_FIRE_IMBUE.value(), "Fire Imbue");
		LangEntrySupport.addEffectDescription(builder, RDT_FireImbue.RDT_FIRE_IMBUE.value(),
				"Your attacks set the target on fire, for 100 ticks per effect level.");
		LangEntrySupport.addEffect(builder, RDT_PoisonImbue.RDT_POISON_IMBUE.value(), "Poison Imbue");
		LangEntrySupport.addEffectDescription(builder, RDT_PoisonImbue.RDT_POISON_IMBUE.value(),
				"Your attacks poison the target; the duration and the poison level both scale with this effect's level (poison II from level III on).");

		// --- 激素药水 ---
		addHormones(builder, "Potion of Fire Hormone", RDT_FireHormonePotion.ALL);
		addHormones(builder, "Potion of Poison Hormone", RDT_PoisonHormonePotion.ALL);
	}

	/** 按基名登记一族激素药水（各等级共用一个名字，等级由 lore 显示）。 */
	private static void addHormones(FabricLanguageProvider.TranslationBuilder builder,
			String baseName, List<Item> items) {
		for (Item item : items) {
			builder.add(item, baseName);
		}
	}
}
