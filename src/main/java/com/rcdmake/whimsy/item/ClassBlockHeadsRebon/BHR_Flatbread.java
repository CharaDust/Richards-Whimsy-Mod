package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.item.Item;

/**
 * 煎饼（{@code bhr_flatbread}）—— 篝火线的成品食物（Blockheads 的 Flatbread）。
 *
 * <h2>规格</h2>
 * <ul>
 *   <li>饥饿值 <b>8</b>、饱和度 <b>3 点</b>（需求原文：「煎饼补充 8 饥饿值 3 饱和度」）；</li>
 *   <li>口径：26.2 的 {@code minecraft:food.saturation} <b>就是实际饱和度点数</b>
 *       （{@code FoodData#eat(FoodProperties)} 直接累加，不乘任何倍率 —— 证据见
 *       {@link BHR_SimpleFood} 的类注释），所以这里写 {@code 3.0F} 得到的就是 3 点；</li>
 *   <li>获取：篝火 {@code flatbread.json} —— 面粉 ×5 + 锡水桶 ×1（返还空锡桶）+ 无机盐 ×1 → 煎饼 ×5，
 *       普通模式 80 秒、专家模式 Lv.2 80 秒。</li>
 * </ul>
 *
 * <h2>范式契约栏（层 0）</h2>
 * <ul>
 *   <li><b>中文名</b> 煎饼　<b>英文名</b> Flatbread　<b>注册名</b> {@code bhr_flatbread}　<b>模块</b> BHR</li>
 *   <li><b>父级</b> 无（普通 {@code Item}）</li>
 *   <li><b>启用的可选层</b>：层 1 规格常量、层 2 物品声明、层 3 组件（经 {@link BHR_SimpleFood}）、层 12 初始化</li>
 *   <li><b>跳过的可选层</b>：同 {@link BHR_RawFish}（层 4 / 6 / 7 / 8 / 9 / 10 / 11 / C / M 均无需求）</li>
 *   <li><b>登记点</b>：①☑ 主类物品区　②☑ 手写 {@code models/item/bhr_flatbread.json} + datagen
 *       　③☑ 中英 {@code BHR_LangEntries}　④☑ {@code BHR_CreativeTabs}（食物组）
 *       　⑤☑ 贴图 {@code textures/item/bhr/flatbread.png}　⑥☑ 篝火配方（作为产物）</li>
 * </ul>
 */
public class BHR_Flatbread extends Item {

	/** 补充饥饿值（需求：8）。 */
	public static final int NUTRITION = 8;

	/** 饱和度<b>点数</b>（需求：3 点 —— 26.2 的这个字段就是点数，不是倍率）。 */
	public static final float SATURATION_POINTS = 3.0F;

	/** 煎饼本体，在 {@link #OnInit()} 里赋值。 */
	public static Item BHR_FLATBREAD;

	public BHR_Flatbread(Item.Properties properties) {
		super(properties);
	}

	/** 注册（层 12）。 */
	public static void OnInit() {
		BHR_FLATBREAD = ModRegister.registerItem("bhr_flatbread",
				properties -> new BHR_Flatbread(BHR_SimpleFood.apply(properties, NUTRITION, SATURATION_POINTS)));
	}
}
