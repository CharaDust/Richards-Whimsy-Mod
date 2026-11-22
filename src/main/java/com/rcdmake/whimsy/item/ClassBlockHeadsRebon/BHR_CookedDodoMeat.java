package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.item.Item;

/**
 * 熟渡渡鸟肉（{@code bhr_cooked_dodo_meat}）—— 篝火烤出来的熟食。
 *
 * <h2>规格</h2>
 * <ul>
 *   <li>饥饿值 <b>6</b>、饱和度 <b>7.2 点</b> —— 按需求方给的口径「参考原版鸡肉（熟）」，
 *       即原版 {@code Foods.COOKED_CHICKEN} 的 {@code (6, 0.6)}；</li>
 *   <li>组件：{@code minecraft:food} + {@code minecraft:consumable}，走 {@link BHR_SimpleFood#apply}；</li>
 *   <li>获取：篝火 {@code cooked_dodo_meat.json} —— 生渡渡鸟肉 ×1，普通模式 20 秒、专家模式 Lv.2 20 秒。</li>
 * </ul>
 *
 * <h2>范式契约栏（层 0）</h2>
 * <ul>
 *   <li><b>中文名</b> 熟渡渡鸟肉　<b>英文名</b> Cooked Dodo Meat　<b>注册名</b> {@code bhr_cooked_dodo_meat}　<b>模块</b> BHR</li>
 *   <li><b>父级</b> 无（普通 {@code Item}）</li>
 *   <li><b>启用的可选层</b>：层 1 规格常量、层 2 物品声明、层 3 组件（经 {@link BHR_SimpleFood}）、层 12 初始化</li>
 *   <li><b>跳过的可选层</b>：同 {@link BHR_RawFish}（层 4 / 6 / 7 / 8 / 9 / 10 / 11 / C / M 均无需求）</li>
 *   <li><b>登记点</b>：①☑ 主类物品区　②☑ 手写 {@code models/item/bhr_cooked_dodo_meat.json} + datagen
 *       　③☑ 中英 {@code BHR_LangEntries}　④☑ {@code BHR_CreativeTabs}（食物组）
 *       　⑤☑ 贴图 {@code textures/item/bhr/cooked_dodo_meat.png}　⑥☑ 篝火配方（作为产物）</li>
 * </ul>
 */
public class BHR_CookedDodoMeat extends Item {

	/** 补充饥饿值（原版熟鸡肉 6）。 */
	public static final int NUTRITION = 6;

	/** 饱和度<b>点数</b>：原版熟鸡肉 0.6 倍率 → {@code 6 × 0.6 × 2 = 7.2}（见 {@link BHR_SimpleFood}）。 */
	public static final float SATURATION_POINTS = 7.2F;

	/** 熟渡渡鸟肉本体，在 {@link #OnInit()} 里赋值。 */
	public static Item BHR_COOKED_DODO_MEAT;

	public BHR_CookedDodoMeat(Item.Properties properties) {
		super(properties);
	}

	/** 注册（层 12）。 */
	public static void OnInit() {
		BHR_COOKED_DODO_MEAT = ModRegister.registerItem("bhr_cooked_dodo_meat",
				properties -> new BHR_CookedDodoMeat(
						BHR_SimpleFood.apply(properties, NUTRITION, SATURATION_POINTS)));
	}
}
