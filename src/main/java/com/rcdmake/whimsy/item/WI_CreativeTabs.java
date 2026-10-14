package com.rcdmake.whimsy.item;

import com.rcdmake.whimsy.ModCreativeTabs;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_Aerogel;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_GlassSugar;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_PackedPlanks;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_StaticCobblestone;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_StaticLiquidWater;
import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_SugarBlock;
import com.rcdmake.whimsy.item.ClassWhimsyIdea.potion.WI_PlaguePotion;
import com.rcdmake.whimsy.item.ClassWhimsyIdea.potion.WI_RadiationPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 奇思妙想（{@code WI}）分支的创造模式物品栏。
 *
 * <p>wiki 规格（{@code doc/wiki/ItemGroup/ClassWhimsyIdea/WI_General.md}）：
 * 这个分支<b>一个类里有两个标签页</b>（旧 {@code WI_General} 就是如此）：
 * <ul>
 *   <li>方块页：注册名 {@code whimsy_group_block} → <b>{@code wi_group_block}</b>，
 *       标题语言键 {@code itemGroup.wi_group_block}，图标 = 包装木材</li>
 *   <li>物品页：注册名 {@code whimsy_group_item} → <b>{@code wi_group_item}</b>，
 *       标题语言键 {@code itemGroup.wi_group_item}，图标 = 钻石（原版物品）</li>
 * </ul>
 * 注册动作统一用 {@link ModCreativeTabs#registerTab}，本类调用两次即可。
 *
 * <h2>当前进度</h2>
 * <b>只注册了方块页</b>——WI 的方块已在本阶段（方块重制）全部落地，
 * 而物品页列的是紫锂辉、蓝宝石、祖母绿、红刚玉、宝石钻石、结晶蜜、探矿器、书（原版）、渲染器测试物品，
 * 这些要么属于别的模块（宝石属 {@code BHR}），要么属于 WI 的物品阶段（探矿器），
 * 且 {@code WI_ShaderDevRed} 已按注册目录删除。空标签页不该出现在物品栏里，
 * 所以物品页留到 WI 物品阶段再一起注册（两个 id 常量已备好）。
 *
 * <h2>方块页的包含清单（顺序）</h2>
 * wiki 的「包含物品」列的是：固石、包装木材、气凝胶、糖块、板岩（将更名为板岩片石）。
 * 其中<b>板岩已改归 {@code BHR}</b>（见 {@code 模板遗留方块}），不属于本页；
 * 而本阶段迁入 WI 的<b>糖玻璃</b>与本就属于 WI 的<b>固化水方块</b>在该表里漏了。
 * 因此实际顺序取：固石、包装木材、气凝胶、糖块、糖玻璃、固化水方块
 * （wiki 原有顺序打头，两个漏项按迁入顺序补在末尾）。
 */
public final class WI_CreativeTabs {

	private WI_CreativeTabs() {
	}

	/** 方块页注册 id（wiki 目标值）。 */
	public static final String GROUP_ID_BLOCK = "wi_group_block";

	/** 物品页注册 id（wiki 目标值）。 */
	public static final String GROUP_ID_ITEM = "wi_group_item";

	/** 方块页标题语言键。 */
	public static final String LANG_KEY_BLOCK = "itemGroup." + GROUP_ID_BLOCK;

	/** 物品页标题语言键。 */
	public static final String LANG_KEY_ITEM = "itemGroup." + GROUP_ID_ITEM;

	/**
	 * 初始化：注册本分支的创造模式物品栏。
	 * <p>由 {@link ModCreativeTabs#OnInit()} 统一调用，且必须在所有方块/物品的
	 * {@code OnInit()} 之后 —— 本类直接引用它们的静态字段。
	 */
	public static void OnInit() {
		// --- 页面一：方块 ---
		ModCreativeTabs.registerTab(GROUP_ID_BLOCK,
				// 图标 = 包装木材
				() -> new ItemStack(WI_PackedPlanks.WI_PACKED_PLANKS),
				(parameters, output) -> {
					output.accept(WI_StaticCobblestone.WI_STATIC_COBBLESTONE);   // 固石
					output.accept(WI_PackedPlanks.WI_PACKED_PLANKS);             // 包装木材
					output.accept(WI_Aerogel.WI_AEROGEL);                        // 气凝胶
					output.accept(WI_SugarBlock.WI_SUGARBLOCK);                  // 糖块
					output.accept(WI_GlassSugar.WI_GLASSSUGAR);                  // 糖玻璃
					output.accept(WI_StaticLiquidWater.WI_STATIC_LIQUID_WATER);  // 固化水方块
				});

		// --- 页面二：物品 ---
		// 本页原先一直空着（旧注释写着「WI 还没有物品」）。现在 WI 的第一批物品落地了：
		// 三瓶药水（电离辐射 / 生物毒性 / 辐射解毒剂），所以本页正式注册。
		// 图标仍按 wiki 取原版钻石（Items.DIAMOND）。
		ModCreativeTabs.registerTab(GROUP_ID_ITEM,
				() -> new ItemStack(Items.DIAMOND),
				(parameters, output) -> {
					// 瘟疫药水（生物毒性 III/IV）× 饮用/喷溅/滞留
					// 清单来自条目类自己登记的 ENTRIES —— 注册了什么就进栏什么，不会漏。
					WI_PlaguePotion.ENTRIES.forEach(entry -> output.accept(entry.item()));
					// 放射药水（电离辐射 IV/V）× 饮用/喷溅/滞留
					WI_RadiationPotion.ENTRIES.forEach(entry -> output.accept(entry.item()));
				});
	}
}
