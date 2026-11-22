package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.item.Item;

/**
 * 生渡渡鸟肉（{@code bhr_raw_dodo_meat}）—— 篝火线的生食原料，也是「熟渡渡鸟肉」的唯一原料。
 *
 * <h2>规格</h2>
 * <ul>
 *   <li>饥饿值 <b>2</b>、饱和度 <b>1.2 点</b> —— 按需求方给的口径「参考原版鸡肉（生）」，
 *       即原版 {@code Foods.CHICKEN} 的 {@code (2, 0.3)}；</li>
 *   <li>组件：{@code minecraft:food} + {@code minecraft:consumable}，走 {@link BHR_SimpleFood#apply}；</li>
 *   <li>获取：待定（「获取方式」阶段 —— 渡渡鸟尚未落地）；篝火「生渡渡鸟肉 ×1 → 熟渡渡鸟肉 ×1」。</li>
 * </ul>
 *
 * <h2>范式契约栏（层 0）</h2>
 * <ul>
 *   <li><b>中文名</b> 生渡渡鸟肉　<b>英文名</b> Raw Dodo Meat　<b>注册名</b> {@code bhr_raw_dodo_meat}　<b>模块</b> BHR</li>
 *   <li><b>父级</b> 无（普通 {@code Item}）</li>
 *   <li><b>启用的可选层</b>：层 1 规格常量、层 2 物品声明、层 3 组件（经 {@link BHR_SimpleFood}）、层 12 初始化</li>
 *   <li><b>跳过的可选层</b>：同 {@link BHR_RawFish}（层 4 / 6 / 7 / 8 / 9 / 10 / 11 / C / M 均无需求）</li>
 *   <li><b>登记点</b>：①☑ 主类物品区　②☑ 手写 {@code models/item/bhr_raw_dodo_meat.json} + datagen
 *       　③☑ 中英 {@code BHR_LangEntries}　④☑ {@code BHR_CreativeTabs}（食物组）
 *       　⑤☑ 贴图 {@code textures/item/bhr/raw_dodo_meat.png}　⑥☑ 篝火配方（作为原料）</li>
 * </ul>
 */
public class BHR_RawDodoMeat extends Item {

	/** 补充饥饿值（原版生鸡肉 2）。 */
	public static final int NUTRITION = 2;

	/** 饱和度<b>点数</b>：原版生鸡肉 0.3 倍率 → {@code 2 × 0.3 × 2 = 1.2}（见 {@link BHR_SimpleFood}）。 */
	public static final float SATURATION_POINTS = 1.2F;

	/** 生渡渡鸟肉本体，在 {@link #OnInit()} 里赋值。 */
	public static Item BHR_RAW_DODO_MEAT;

	public BHR_RawDodoMeat(Item.Properties properties) {
		super(properties);
	}

	/** 注册（层 12）。 */
	public static void OnInit() {
		BHR_RAW_DODO_MEAT = ModRegister.registerItem("bhr_raw_dodo_meat",
				properties -> new BHR_RawDodoMeat(BHR_SimpleFood.apply(properties, NUTRITION, SATURATION_POINTS)));
	}
}
