package com.rcdmake.whimsy.datagen.ClassWhimsyIdea;

import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_Aerogel;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_GlassSugar;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_PackedPlanks;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_StaticCobblestone;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_StaticLiquidWater;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_SugarBlock;
import com.rcdmake.whimsy.datagen.LangEntrySupport;
import com.rcdmake.whimsy.item.WI_CreativeTabs;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

/**
 * 奇思妙想（WI）分支的<b>语言条目</b>（中英各一套）。
 *
 * <p>为何是静态条目方法而不是 provider：见 {@code DEV_LangEntries} 的类注释
 * （26.2 同一语言码只允许一个 provider）。本类的条目由模组级
 * {@code datagen/ModLangProviderZh} 与 {@code ModLangProviderEn} 调用。
 *
 * <p>方块与它的方块物品要各写一条（{@code block.…} 与 {@code item.…} 是两个键）——
 * 这一步统一交给 {@link LangEntrySupport#addBlockAndItem}（阶段 0 起三个模块共用一份）。
 *
 * <p>名称来源：中文名取 {@code doc/wiki} 各页标题，英文名取旧语言文件的历史值
 * （{@code Aerogel} / {@code Static Cobblestone} / {@code Packed Planks}），
 * 其余按中文名直译。
 */
public final class WI_LangEntries {

	private WI_LangEntries() {
	}

	/** 简体中文条目。 */
	public static void zh(FabricLanguageProvider.TranslationBuilder builder) {
		// 创造模式物品栏标题（方块页；物品页留到 WI 物品阶段）
		builder.add(WI_CreativeTabs.LANG_KEY_BLOCK, "奇思妙想方块");

		LangEntrySupport.addBlockAndItem(builder, WI_StaticCobblestone.WI_STATIC_COBBLESTONE, "固石");
		LangEntrySupport.addBlockAndItem(builder, WI_PackedPlanks.WI_PACKED_PLANKS, "包装木材");
		LangEntrySupport.addBlockAndItem(builder, WI_Aerogel.WI_AEROGEL, "气凝胶");
		LangEntrySupport.addBlockAndItem(builder, WI_SugarBlock.WI_SUGARBLOCK, "糖块");
		LangEntrySupport.addBlockAndItem(builder, WI_GlassSugar.WI_GLASSSUGAR, "糖玻璃");
		LangEntrySupport.addBlockAndItem(builder, WI_StaticLiquidWater.WI_STATIC_LIQUID_WATER, "固化水方块");
	}

	/** 英文条目（与 {@link #zh} 同一批键）。 */
	public static void en(FabricLanguageProvider.TranslationBuilder builder) {
		builder.add(WI_CreativeTabs.LANG_KEY_BLOCK, "Whimsy Blocks");

		LangEntrySupport.addBlockAndItem(builder, WI_StaticCobblestone.WI_STATIC_COBBLESTONE, "Static Cobblestone");
		LangEntrySupport.addBlockAndItem(builder, WI_PackedPlanks.WI_PACKED_PLANKS, "Packed Planks");
		LangEntrySupport.addBlockAndItem(builder, WI_Aerogel.WI_AEROGEL, "Aerogel");
		LangEntrySupport.addBlockAndItem(builder, WI_SugarBlock.WI_SUGARBLOCK, "Sugar Block");
		LangEntrySupport.addBlockAndItem(builder, WI_GlassSugar.WI_GLASSSUGAR, "Sugar Glass");
		LangEntrySupport.addBlockAndItem(builder, WI_StaticLiquidWater.WI_STATIC_LIQUID_WATER, "Static Liquid Water");
	}
}
