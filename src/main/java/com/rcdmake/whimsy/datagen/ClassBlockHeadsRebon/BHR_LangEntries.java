package com.rcdmake.whimsy.datagen.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_BeachSand;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Compost;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_GemAmethyst;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_GemDiamond;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_GemEmerald;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_GemRuby;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_GemSapphire;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Limestone;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_LimestoneBrick;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Marble;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_MarblePolished;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreLimestoneOil;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateCoal;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateCopper;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateGold;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateIron;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlatePlatinum;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateTitanium;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateTin;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSlateUnknown;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSoilClay;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSoilFlint;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_OreSoilUnknown;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Slate;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_SlateCobble;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Soil;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_TreeLeavesPine;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_TreeLogPine;
import com.rcdmake.whimsy.datagen.LangEntrySupport;
import com.rcdmake.whimsy.item.BHR_CreativeTabs;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_Cherry;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_Coconut;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_CoffeeCherry;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_DodoApple;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_Lime;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_Mango;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_MapleSeed;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_Orange;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_PineCone;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_PricklyPear;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_Worm;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

/**
 * 方块愚者（BHR）分支的<b>语言条目</b>（中英各一套）。
 *
 * <p>为何是静态条目方法而不是 provider：见 {@code DEV_LangEntries} 的类注释
 * （26.2 同一语言码只允许一个 provider）。本类的条目由模组级
 * {@code datagen/ModLangProviderZh} 与 {@code ModLangProviderEn} 调用。
 *
 * <p>方块与它的方块物品要各写一条（{@code block.…} 与 {@code item.…} 是两个键）——
 * 这一步统一交给 {@link LangEntrySupport#addBlockAndItem}（阶段 0 起三个模块共用一份）。
 *
 * <p><b>名称来源</b>（本批 25 个方块，一次性补齐 BHR 的 5 个 {@code #deprecated} 集合类拆分）：
 * <ul>
 *   <li>中文名取 {@code doc/wiki/Blocks/ClassBlockHeadsRebon/} 各页标题；</li>
 *   <li>英文名优先取<b>旧 1.20.4 语言文件</b>（本次从 {@code bin/main/assets/.../en_us.json} 找回，
 *       那是 26.2 迁移时被覆盖掉的原件）—— 切面板岩系 {@code Faceted Slate *}、红壤系 {@code Mineral Soil *}、
 *       板岩片石 {@code Slate}、宝石 {@code Kunzite / Sapphire / Canutillos / Ruby / 'Adamant'} 都按它抄；</li>
 *   <li>旧语言文件里<b>缺</b>的 6 条（石灰石 / 石灰砖 / 石灰石油矿 / 大理石 / 磨制大理石 / 海滩沙）
 *       按中文名直译 —— 与第一批（松木原木 / 堆肥块）同样的处理。</li>
 * </ul>
 * <p><b>物品阶段</b>（2026-10 起）：食物组开始重制。物品只有 {@code item.…} 一条键，
 * 直接 {@code builder.add(物品, 名称)} 即可 —— 名字同样优先抄旧语言文件
 * （石灰酸橙 {@code bhr_lime} 的英文名就是旧文件里的 {@code Lime Limes}）。
 *
 * <p>条目顺序 = wiki 物品栏页「包含物品」的分组顺序，与 {@code BHR_CreativeTabs} 一致，便于对照。
 */
public final class BHR_LangEntries {

	private BHR_LangEntries() {
	}

	/** 简体中文条目。 */
	public static void zh(FabricLanguageProvider.TranslationBuilder builder) {
		// 创造模式物品栏标题
		builder.add(BHR_CreativeTabs.LANG_KEY, "方块愚者");

		// —— 方块与矿物：切面板岩系 ——
		LangEntrySupport.addBlockAndItem(builder, BHR_Slate.BHR_SLATE, "切面板岩");
		LangEntrySupport.addBlockAndItem(builder, BHR_SlateCobble.BHR_COBBLESLATE, "板岩片石");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSlateCopper.BHR_SLATE_COPPER_ORE, "切面板岩铜矿石");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSlateIron.BHR_SLATE_IRON_ORE, "切面板岩铁矿石");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSlateTin.BHR_SLATE_TIN_ORE, "切面板岩锡矿石");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSlateCoal.BHR_SLATE_COAL_ORE, "切面板岩煤矿石");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSlateGold.BHR_SLATE_GOLD_ORE, "切面板岩金矿石");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSlatePlatinum.BHR_SLATE_PLATINUM_ORE, "切面板岩铂矿石");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSlateTitanium.BHR_SLATE_TITANIUM_ORE, "切面板岩钛矿石");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSlateUnknown.BHR_SLATE_UNKNOWN_ORE, "切面板岩未知矿石");
		// —— 方块与矿物：红壤系 ——
		LangEntrySupport.addBlockAndItem(builder, BHR_Soil.BHR_SOIL, "红壤");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSoilFlint.BHR_SOIL_FLINT_ORE, "红壤燧石矿土");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSoilClay.BHR_SOIL_CLAY_ORE, "红壤粘土矿土");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSoilUnknown.BHR_SOIL_UNKNOWN_ORE, "红壤未知矿土");
		// —— 方块与矿物：石灰石 / 大理石 / 海滩沙 ——
		LangEntrySupport.addBlockAndItem(builder, BHR_Limestone.BHR_LIMESTONE, "石灰石");
		LangEntrySupport.addBlockAndItem(builder, BHR_LimestoneBrick.BHR_LIMESTONE_BRICK, "石灰砖");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreLimestoneOil.BHR_LIMESTONE_OIL_ORE, "石灰石油矿");
		LangEntrySupport.addBlockAndItem(builder, BHR_Marble.BHR_MARBLE, "大理石");
		LangEntrySupport.addBlockAndItem(builder, BHR_MarblePolished.BHR_POLISHED_MARBLE, "磨制大理石");
		LangEntrySupport.addBlockAndItem(builder, BHR_BeachSand.BHR_BEACH_SAND, "海滩沙");
		// —— 树木 ——
		LangEntrySupport.addBlockAndItem(builder, BHR_TreeLogPine.BHR_PINE_LOG, "松木原木");
		LangEntrySupport.addBlockAndItem(builder, BHR_TreeLeavesPine.BHR_PINE_LEAVES, "松木树叶");
		// —— 物品：松果（普通物品，只有 item.… 一条键）——
		builder.add(BHR_PineCone.BHR_PINE_CONE, "松果");
		// —— 其他方块 ——
		LangEntrySupport.addBlockAndItem(builder, BHR_Compost.BHR_COMPOST, "堆肥块");
		// —— 宝石 ——
		LangEntrySupport.addBlockAndItem(builder, BHR_GemAmethyst.BHR_GEM_AMETHYST, "紫锂辉");
		LangEntrySupport.addBlockAndItem(builder, BHR_GemSapphire.BHR_GEM_SAPPHIRE, "蓝宝石");
		LangEntrySupport.addBlockAndItem(builder, BHR_GemEmerald.BHR_GEM_EMERALD, "祖母绿");
		LangEntrySupport.addBlockAndItem(builder, BHR_GemRuby.BHR_GEM_RUBY, "红刚玉");
		LangEntrySupport.addBlockAndItem(builder, BHR_GemDiamond.BHR_GEM_DIAMOND, "宝石钻石");
		// —— 食物 ——
		// 顺序 = wiki 物品栏页「包含物品」的食物顺序（樱桃…石灰酸橙），其后是本批其余 4 件。
		builder.add(BHR_Cherry.BHR_CHERRY, "樱桃");
		builder.add(BHR_Coconut.BHR_COCONUT, "椰子");
		builder.add(BHR_CoffeeCherry.BHR_COFFEE_CHERRY, "咖啡樱桃");
		builder.add(BHR_Mango.BHR_MANGO, "芒果");
		builder.add(BHR_Orange.BHR_ORANGE, "橙子");
		builder.add(BHR_Lime.BHR_LIME, "石灰酸橙");
		builder.add(BHR_DodoApple.BHR_DODO_APPLE, "渡渡苹果");
		builder.add(BHR_MapleSeed.BHR_MAPLE_SEED, "枫实");
		builder.add(BHR_PricklyPear.BHR_PRICKLY_PEAR, "梨果仙人掌");
		builder.add(BHR_Worm.BHR_WORM, "愚人蠕虫");
	}

	/** 英文条目（与 {@link #zh} 同一批键）。 */
	public static void en(FabricLanguageProvider.TranslationBuilder builder) {
		builder.add(BHR_CreativeTabs.LANG_KEY, "Block Heads Reborn");

		// —— 方块与矿物：切面板岩系 ——
		LangEntrySupport.addBlockAndItem(builder, BHR_Slate.BHR_SLATE, "Faceted Slate");
		LangEntrySupport.addBlockAndItem(builder, BHR_SlateCobble.BHR_COBBLESLATE, "Slate");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSlateCopper.BHR_SLATE_COPPER_ORE, "Faceted Slate Copper Ore");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSlateIron.BHR_SLATE_IRON_ORE, "Faceted Slate Iron Ore");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSlateTin.BHR_SLATE_TIN_ORE, "Faceted Slate Tin Ore");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSlateCoal.BHR_SLATE_COAL_ORE, "Faceted Slate Coal Ore");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSlateGold.BHR_SLATE_GOLD_ORE, "Faceted Slate Gold Ore");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSlatePlatinum.BHR_SLATE_PLATINUM_ORE, "Faceted Slate Platinum Ore");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSlateTitanium.BHR_SLATE_TITANIUM_ORE, "Faceted Slate Titanium Ore");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSlateUnknown.BHR_SLATE_UNKNOWN_ORE, "Faceted Slate Unknown Ore");
		// —— 方块与矿物：红壤系 ——
		LangEntrySupport.addBlockAndItem(builder, BHR_Soil.BHR_SOIL, "Mineral Soil");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSoilFlint.BHR_SOIL_FLINT_ORE, "Mineral Soil Flint Ore");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSoilClay.BHR_SOIL_CLAY_ORE, "Mineral Soil Clay Ore");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreSoilUnknown.BHR_SOIL_UNKNOWN_ORE, "Mineral Soil Unknown Ore");
		// —— 方块与矿物：石灰石 / 大理石 / 海滩沙（旧语言文件缺这 6 条，按中文名直译） ——
		LangEntrySupport.addBlockAndItem(builder, BHR_Limestone.BHR_LIMESTONE, "Limestone");
		LangEntrySupport.addBlockAndItem(builder, BHR_LimestoneBrick.BHR_LIMESTONE_BRICK, "Limestone Brick");
		LangEntrySupport.addBlockAndItem(builder, BHR_OreLimestoneOil.BHR_LIMESTONE_OIL_ORE, "Limestone Oil Ore");
		LangEntrySupport.addBlockAndItem(builder, BHR_Marble.BHR_MARBLE, "Marble");
		LangEntrySupport.addBlockAndItem(builder, BHR_MarblePolished.BHR_POLISHED_MARBLE, "Polished Marble");
		LangEntrySupport.addBlockAndItem(builder, BHR_BeachSand.BHR_BEACH_SAND, "Beach Sand");
		// —— 树木 ——
		LangEntrySupport.addBlockAndItem(builder, BHR_TreeLogPine.BHR_PINE_LOG, "Pine Log");
		LangEntrySupport.addBlockAndItem(builder, BHR_TreeLeavesPine.BHR_PINE_LEAVES, "Pine Leaves");
		// —— 物品：松果 ——
		builder.add(BHR_PineCone.BHR_PINE_CONE, "Pine Cone");
		// —— 其他方块 ——
		LangEntrySupport.addBlockAndItem(builder, BHR_Compost.BHR_COMPOST, "Compost Block");
		// —— 宝石 ——
		LangEntrySupport.addBlockAndItem(builder, BHR_GemAmethyst.BHR_GEM_AMETHYST, "Kunzite");
		LangEntrySupport.addBlockAndItem(builder, BHR_GemSapphire.BHR_GEM_SAPPHIRE, "Sapphire");
		LangEntrySupport.addBlockAndItem(builder, BHR_GemEmerald.BHR_GEM_EMERALD, "Canutillos");
		LangEntrySupport.addBlockAndItem(builder, BHR_GemRuby.BHR_GEM_RUBY, "Ruby");
		LangEntrySupport.addBlockAndItem(builder, BHR_GemDiamond.BHR_GEM_DIAMOND, "'Adamant'");
		// —— 食物 ——
		// 英文名优先抄旧语言文件（1.20.4 那版）：cherry / coconut / coffee_cherry / mango /
		// orange / maple_seed / prickly_pear / lime 八条都还在里面，逐条照抄；
		// 旧文件里没有的两条（渡渡苹果、愚人蠕虫 —— 旧代码从未注册过它们）按中文名直译。
		builder.add(BHR_Cherry.BHR_CHERRY, "Cherry");
		builder.add(BHR_Coconut.BHR_COCONUT, "Coconut");
		builder.add(BHR_CoffeeCherry.BHR_COFFEE_CHERRY, "Coffee Cherry");
		builder.add(BHR_Mango.BHR_MANGO, "Mango");
		builder.add(BHR_Orange.BHR_ORANGE, "Orange");
		builder.add(BHR_Lime.BHR_LIME, "Lime Limes");
		builder.add(BHR_DodoApple.BHR_DODO_APPLE, "Dodo Apple");
		builder.add(BHR_MapleSeed.BHR_MAPLE_SEED, "Maple Seed");
		builder.add(BHR_PricklyPear.BHR_PRICKLY_PEAR, "Prickly Pear");
		builder.add(BHR_Worm.BHR_WORM, "Fool's Worm");
	}
}
