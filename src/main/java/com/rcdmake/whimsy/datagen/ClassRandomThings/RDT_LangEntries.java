package com.rcdmake.whimsy.datagen.ClassRandomThings;

import com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassLapis;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassQuartz;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_LuminousBlocks;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_StepBlock;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_SuperLubricentIce;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_TreeLogSpectre;
import com.rcdmake.whimsy.datagen.LangEntrySupport;
import com.rcdmake.whimsy.item.RDT_CreativeTabs;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

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
		LangEntrySupport.addBlockAndItem(builder, RDT_StepBlock.RDT_STEP_BLOCK, "垫脚方块");
		LangEntrySupport.addBlockAndItem(builder, RDT_TreeLogSpectre.RDT_SPECTRE_LOG, "通灵原木");
		LangEntrySupport.addBlockAndItem(builder, RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_RED, "红色鲁米诺方块");
		LangEntrySupport.addBlockAndItem(builder, RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_GREEN, "绿色鲁米诺方块");
		LangEntrySupport.addBlockAndItem(builder, RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_BLUE, "蓝色鲁米诺方块");
	}

	/** 英文条目（与 {@link #zh} 同一批键）。 */
	public static void en(FabricLanguageProvider.TranslationBuilder builder) {
		builder.add(RDT_CreativeTabs.LANG_KEY, "Random Things: sequel?");

		LangEntrySupport.addBlockAndItem(builder, RDT_SuperLubricentIce.RDT_SUPER_LUBRICENT_ICE, "Super Lubricent Ice");
		LangEntrySupport.addBlockAndItem(builder, RDT_GlassLapis.RDT_LAPIS_GLASS, "Lapis Glass");
		LangEntrySupport.addBlockAndItem(builder, RDT_GlassQuartz.RDT_QUARTZ_GLASS, "Quartz Glass");
		LangEntrySupport.addBlockAndItem(builder, RDT_StepBlock.RDT_STEP_BLOCK, "Step Block");
		LangEntrySupport.addBlockAndItem(builder, RDT_TreeLogSpectre.RDT_SPECTRE_LOG, "Spectre Log");
		LangEntrySupport.addBlockAndItem(builder, RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_RED, "Red Luminous Block");
		LangEntrySupport.addBlockAndItem(builder, RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_GREEN, "Green Luminous Block");
		LangEntrySupport.addBlockAndItem(builder, RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_BLUE, "Blue Luminous Block");
	}
}
