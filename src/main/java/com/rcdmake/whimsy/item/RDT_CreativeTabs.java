package com.rcdmake.whimsy.item;

import com.rcdmake.whimsy.ModCreativeTabs;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassLapis;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_GlassQuartz;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_LuminousBlocks;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_StepBlock;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_SuperLubricentIce;
import com.rcdmake.whimsy.block.ClassRandomThings.RDT_TreeLogSpectre;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 随意物件（{@code RDT}）分支的创造模式物品栏。
 *
 * <p>wiki 规格（{@code doc/wiki/ItemGroup/ClassRandomThings/RDT_General.md}）：
 * 注册名 {@code rdt_general}、标题语言键 {@code itemGroup.rdt_general}、图标「皮革帽（原版物品）」，
 * 内容按「包含物品」的顺序排列。
 *
 * <h2>包含清单（顺序 = wiki 的顺序）</h2>
 * <ol>
 *   <li>超级滑滑冰、青金石玻璃、石英玻璃 <span>（糖玻璃已迁往 {@code WI} 模块，从本页移除）</span></li>
 *   <li>垫脚方块 <span>（方块破坏器 {@code RDT_BlockBreaker} 是<b>方块实体</b>，属注册目录的「方块实体」分区，
 *       不在本次方块重制范围内，留到那个阶段再加进本页）</span></li>
 *   <li>通灵原木</li>
 *   <li>红色 / 绿色 / 蓝色鲁米诺方块</li>
 * </ol>
 *
 * <p><b>注册动作</b>：统一用 {@link ModCreativeTabs#registerTab}（阶段 0 起，三个模块共用一份写法），
 * 本类只提供「注册 id / 图标 / 包含清单」—— 图标用原版皮革帽（{@code Items.LEATHER_HELMET}）。
 */
public final class RDT_CreativeTabs {

	private RDT_CreativeTabs() {
	}

	/** 注册 id（wiki 目标值）。 */
	public static final String GROUP_ID = "rdt_general";

	/** 标题语言键，具体标题写在 lang 文件里。 */
	public static final String LANG_KEY = "itemGroup." + GROUP_ID;

	/**
	 * 初始化：注册本分支的创造模式物品栏。
	 * <p>由 {@link ModCreativeTabs#OnInit()} 统一调用，且必须在所有方块/物品的
	 * {@code OnInit()} 之后 —— 本类直接引用它们的静态字段。
	 */
	public static void OnInit() {
		ModCreativeTabs.registerTab(GROUP_ID,
				// 图标：皮革帽（原版物品）
				() -> new ItemStack(Items.LEATHER_HELMET),
				(parameters, output) -> {
					// —— 冰与玻璃 ——
					output.accept(RDT_SuperLubricentIce.RDT_SUPER_LUBRICENT_ICE);  // 超级滑滑冰
					output.accept(RDT_GlassLapis.RDT_LAPIS_GLASS);                 // 青金石玻璃
					output.accept(RDT_GlassQuartz.RDT_QUARTZ_GLASS);               // 石英玻璃
					// —— 方块 ——
					output.accept(RDT_StepBlock.RDT_STEP_BLOCK);                   // 垫脚方块
					// —— 木材 ——
					output.accept(RDT_TreeLogSpectre.RDT_SPECTRE_LOG);             // 通灵原木
					// —— 鲁米诺 ——
					output.accept(RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_RED);      // 红色鲁米诺方块
					output.accept(RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_GREEN);    // 绿色鲁米诺方块
					output.accept(RDT_LuminousBlocks.RDT_LUMINOUS_BLOCK_BLUE);     // 蓝色鲁米诺方块
				});
	}
}
