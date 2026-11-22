package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.item.Item;

/**
 * 锡箔（{@code bhr_tin_foil}）—— 篝火线的耗材（Blockheads 的 Tin Foil）。
 *
 * <h2>规格</h2>
 * <ul>
 *   <li>纯材料：无组件、无右键行为，默认堆叠 64；</li>
 *   <li>获取：待定（Blockheads 里由金属工作台用「锡锭 ×1」压出来，本项目锡线尚未落地）；</li>
 *   <li>用途：篝火烤鱼要裹一层 —— {@code cooked_fish.json} 生鱼片 ×1 + 锡箔 ×1；
 *       专家模式的篝火<b>升级</b>也要用它（锡箔 ×5）。</li>
 * </ul>
 *
 * <h2>范式契约栏（层 0）</h2>
 * <ul>
 *   <li><b>中文名</b> 锡箔　<b>英文名</b> Tin Foil　<b>注册名</b> {@code bhr_tin_foil}　<b>模块</b> BHR</li>
 *   <li><b>父级</b> 无（普通 {@code Item}）</li>
 *   <li><b>启用的可选层</b>：层 2 物品声明、层 12 初始化</li>
 *   <li><b>跳过的可选层</b>：同 {@link BHR_Flour}（层 1 / 3 / 4 / 6 / 7 / 8 / 9 / 10 / 11 / C / M 均无需求）</li>
 *   <li><b>登记点</b>：①☑ 主类物品区　②☑ 手写 {@code models/item/bhr_tin_foil.json} + datagen
 *       　③☑ 中英 {@code BHR_LangEntries}　④☑ {@code BHR_CreativeTabs}（材料组）
 *       　⑤☑ 贴图 {@code textures/item/bhr/tin_foil.png}　⑥☑ 篝火配方（作为原料）</li>
 * </ul>
 */
public class BHR_TinFoil extends Item {

	/** 锡箔本体，在 {@link #OnInit()} 里赋值。 */
	public static Item BHR_TIN_FOIL;

	public BHR_TinFoil(Item.Properties properties) {
		super(properties);
	}

	/** 注册（层 12）。 */
	public static void OnInit() {
		BHR_TIN_FOIL = ModRegister.registerItem("bhr_tin_foil", BHR_TinFoil::new);
	}
}
