package com.rcdmake.whimsy.item;

import com.rcdmake.whimsy.ModCreativeTabs;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_BeachSand;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Campfire;
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
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_WorkBench;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_BlockHeadsIcon;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_Cherry;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_Coconut;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_CoffeeCherry;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_DodoApple;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_GemBlessTest;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_Lime;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_Mango;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_MapleSeed;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_Orange;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_PineCone;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_PricklyPear;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_TimeCrystal;
import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_Worm;
import net.minecraft.world.item.ItemStack;

/**
 * 方块愚者（{@code BHR}）分支的创造模式物品栏。
 *
 * <p>wiki 规格（{@code doc/wiki/ItemGroup/ClassBlockHeadsRebon/BHR_General.md}）：
 * 注册名 {@code bhr_general}、标题语言键 {@code itemGroup.bhr_general}、图标「方块愚者徽标」，
 * 排序按该页「包含物品」的添加顺序 —— 分组依次是：
 * 方块与矿物（切面板岩与矿石、红壤系、石灰石系、大理石系、海滩沙）→ 树木（松木原木）→
 * 其他方块（堆肥块）→ 宝石 → 燃料（余烬）→ 食物。
 *
 * <h2>当前进度（第二批：5 个 {@code #deprecated} 集合类已拆分，共 28 个方块入栏）</h2>
 * 已重制并入栏的是「方块与矿物 / 树木 / 其他方块 / 宝石」四组，共 <b>28</b> 个方块：
 * <pre>
 *   切面板岩系 10（切面板岩 + 板岩片石 + 8 种矿石）
 *   红壤系      4（红壤 + 燧石 / 粘土 / 未知矿土）
 *   石灰石系    3（石灰石 + 石灰砖 + 石灰石油矿）
 *   大理石系    2（大理石 + 磨制大理石）
 *   海滩沙      1
 *   树木        2（松木原木 + 松木树叶）
 *   堆肥块      1
 *   宝石        5（紫锂辉 / 蓝宝石 / 祖母绿 / 红刚玉 / 宝石钻石）
 * </pre>
 * 还差「燃料（余烬）」一组 —— 它属于物品目录（不是方块）。<b>食物组已完成</b>：
 * 第一批的石灰酸橙 + 2026-10 第二批的 9 件（渡渡苹果、樱桃、椰子、咖啡樱桃、芒果、枫实、橙子、
 * 梨果仙人掌、愚人蠕虫），共 <b>10</b> 件食物全部入栏。
 * <p><b>图标 = 方块愚者徽标</b>（{@code bhr_blockheads_icon}）：
 * 该物品原本没有注册（第一批时只能暂用堆肥块），本次随「愚者模式」一并落地
 * （同一个贴图 {@code textures/item/bhr/blockheads_icon.png} 也是 F3+F4 切换器里
 * 愚者模式那一格的图标，见 {@code BHR_FoolGameModeIconMixin}）。
 *
 * <p><b>注册动作</b>：统一用 {@link ModCreativeTabs#registerTab}（阶段 0 起各模块共用一份写法），
 * 本类只提供「注册 id / 图标 / 包含清单」。
 */
public final class BHR_CreativeTabs {

	private BHR_CreativeTabs() {
	}

	/** 注册 id（wiki 目标值）。 */
	public static final String GROUP_ID = "bhr_general";

	/** 标题语言键，具体标题写在 lang 文件里。 */
	public static final String LANG_KEY = "itemGroup." + GROUP_ID;

	/**
	 * 初始化：注册本分支的创造模式物品栏。
	 * <p>由 {@link ModCreativeTabs#OnInit()} 统一调用，且必须在所有方块/物品的
	 * {@code OnInit()} 之后 —— 本类直接引用它们的静态字段。
	 */
	public static void OnInit() {
		ModCreativeTabs.registerTab(GROUP_ID,
				// 图标：方块愚者徽标（bhr_blockheads_icon）
				() -> new ItemStack(BHR_BlockHeadsIcon.BHR_BLOCKHEADS_ICON),
				(parameters, output) -> {
					// 顺序照 wiki「包含物品」的分组顺序
					// —— 方块与矿物：切面板岩系 ——
					output.accept(BHR_Slate.BHR_SLATE);                        // 切面板岩
					output.accept(BHR_SlateCobble.BHR_COBBLESLATE);            // 板岩片石
					output.accept(BHR_OreSlateCopper.BHR_SLATE_COPPER_ORE);    // 切面板岩铜矿石
					output.accept(BHR_OreSlateIron.BHR_SLATE_IRON_ORE);        // 切面板岩铁矿石
					output.accept(BHR_OreSlateTin.BHR_SLATE_TIN_ORE);          // 切面板岩锡矿石
					output.accept(BHR_OreSlateCoal.BHR_SLATE_COAL_ORE);        // 切面板岩煤矿石
					output.accept(BHR_OreSlateGold.BHR_SLATE_GOLD_ORE);        // 切面板岩金矿石
					output.accept(BHR_OreSlatePlatinum.BHR_SLATE_PLATINUM_ORE);// 切面板岩铂矿石
					output.accept(BHR_OreSlateTitanium.BHR_SLATE_TITANIUM_ORE);// 切面板岩钛矿石
					output.accept(BHR_OreSlateUnknown.BHR_SLATE_UNKNOWN_ORE);  // 切面板岩未知矿石
					// —— 方块与矿物：红壤系 ——
					output.accept(BHR_Soil.BHR_SOIL);                          // 红壤
					output.accept(BHR_OreSoilFlint.BHR_SOIL_FLINT_ORE);        // 红壤燧石矿土
					output.accept(BHR_OreSoilClay.BHR_SOIL_CLAY_ORE);          // 红壤粘土矿土
					output.accept(BHR_OreSoilUnknown.BHR_SOIL_UNKNOWN_ORE);    // 红壤未知矿土
					// —— 方块与矿物：石灰石 / 大理石 / 海滩沙 ——
					output.accept(BHR_Limestone.BHR_LIMESTONE);                // 石灰石
					output.accept(BHR_LimestoneBrick.BHR_LIMESTONE_BRICK);     // 石灰砖
					output.accept(BHR_OreLimestoneOil.BHR_LIMESTONE_OIL_ORE);  // 石灰石油矿
					output.accept(BHR_Marble.BHR_MARBLE);                      // 大理石
					output.accept(BHR_MarblePolished.BHR_POLISHED_MARBLE);     // 磨制大理石
					output.accept(BHR_BeachSand.BHR_BEACH_SAND);               // 海滩沙
					// —— 树木 ——
					output.accept(BHR_TreeLogPine.BHR_PINE_LOG);               // 松木原木
					output.accept(BHR_TreeLeavesPine.BHR_PINE_LEAVES);         // 松木树叶
					output.accept(BHR_PineCone.BHR_PINE_CONE);                 // 松果（物品）
					// —— 其他方块 ——
					output.accept(BHR_Compost.BHR_COMPOST);                    // 堆肥块
					// 制作台（29 个等级）：物品栏里只放 1 级那一件 ——
					// 2 级以上只能靠「合成 / 升级后再挖下来」得到，不占物品栏条目。
					output.accept(BHR_WorkBench.BHR_WORK_BENCH_ITEM);          // 制作台（1 级）
					// 简易篝火：带燃料的制作台（固定 1 级），与制作台相邻摆放
					output.accept(BHR_Campfire.BHR_CAMPFIRE_ITEM);             // 简易篝火
					// —— 宝石 ——
					output.accept(BHR_GemAmethyst.BHR_GEM_AMETHYST);           // 紫锂辉
					output.accept(BHR_GemSapphire.BHR_GEM_SAPPHIRE);           // 蓝宝石
					output.accept(BHR_GemEmerald.BHR_GEM_EMERALD);             // 祖母绿
					output.accept(BHR_GemRuby.BHR_GEM_RUBY);                   // 红刚玉
					output.accept(BHR_GemDiamond.BHR_GEM_DIAMOND);             // 宝石钻石
					// —— 食物 ——
					// 前 6 件按 wiki 物品栏页「包含物品 → 食物」的原顺序；
					// 其后 4 件（渡渡苹果、枫实、梨果仙人掌、愚人蠕虫）是本批新增的其余档位，
					// wiki 物品栏页的食物组已同步补上这 4 条（共 10 件），顺序一致。
					output.accept(BHR_Cherry.BHR_CHERRY);                      // 樱桃
					output.accept(BHR_Coconut.BHR_COCONUT);                    // 椰子
					output.accept(BHR_CoffeeCherry.BHR_COFFEE_CHERRY);         // 咖啡樱桃
					output.accept(BHR_Mango.BHR_MANGO);                        // 芒果
					output.accept(BHR_Orange.BHR_ORANGE);                      // 橙子
					output.accept(BHR_Lime.BHR_LIME);                          // 石灰酸橙
					output.accept(BHR_DodoApple.BHR_DODO_APPLE);               // 渡渡苹果
					output.accept(BHR_MapleSeed.BHR_MAPLE_SEED);               // 枫实
					output.accept(BHR_PricklyPear.BHR_PRICKLY_PEAR);           // 梨果仙人掌（旧语言文件叫「赤果」）
					output.accept(BHR_Worm.BHR_WORM);                          // 愚人蠕虫
					// —— 开发期测试物品（wiki 有独立页，但内容不是玩法内容） ——
					// 宝石祝福测试棒：右键给副手物品加一种祝福、潜行右键清空。
					// 排在最后，避免把测试用品混进宝石 / 食物的分组里。
					output.accept(BHR_GemBlessTest.BHR_GEM_BLESS_TEST);        // 宝石祝福测试棒
					// —— 制作台相关的消耗品 ——
					output.accept(BHR_TimeCrystal.BHR_TIME_CRYSTAL);           // 时间水晶（堆叠 99）
					// —— 徽标：同时是本标签页的图标（放在最后，不打断玩法内容的顺序） ——
					output.accept(BHR_BlockHeadsIcon.BHR_BLOCKHEADS_ICON);     // 方块愚者徽标
				});
	}
}
