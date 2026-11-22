package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.item.Item;

/**
 * 熟鱼片（{@code bhr_cooked_fish}）—— 篝火烤出来的熟食。
 *
 * <h2>规格</h2>
 * <ul>
 *   <li>饥饿值 <b>5</b>、饱和度 <b>6 点</b> —— 按需求方给的口径「参考原版鳕鱼（熟）」，
 *       即原版 {@code Foods.COOKED_COD} 的 {@code (5, 0.6)}；</li>
 *   <li>组件：{@code minecraft:food} + {@code minecraft:consumable}，走 {@link BHR_SimpleFood#apply}；</li>
 *   <li>获取：篝火 {@code cooked_fish.json} —— 生鱼片 ×1 + 锡箔 ×1，普通模式 20 秒、专家模式 Lv.2 20 秒。</li>
 * </ul>
 *
 * <h2>范式契约栏（层 0）</h2>
 * <ul>
 *   <li><b>中文名</b> 熟鱼片　<b>英文名</b> Cooked Fish　<b>注册名</b> {@code bhr_cooked_fish}　<b>模块</b> BHR</li>
 *   <li><b>父级</b> 无（普通 {@code Item}）</li>
 *   <li><b>启用的可选层</b>：层 1 规格常量、层 2 物品声明、层 3 组件（经 {@link BHR_SimpleFood}）、层 12 初始化</li>
 *   <li><b>跳过的可选层</b>：同 {@link BHR_RawFish}（层 4 / 6 / 7 / 8 / 9 / 10 / 11 / C / M 均无需求）</li>
 *   <li><b>登记点</b>：①☑ 主类物品区　②☑ 手写 {@code models/item/bhr_cooked_fish.json} + datagen
 *       　③☑ 中英 {@code BHR_LangEntries}　④☑ {@code BHR_CreativeTabs}（食物组）
 *       　⑤☑ 贴图 {@code textures/item/bhr/cooked_fish.png}　⑥☑ 篝火配方（作为产物）</li>
 * </ul>
 */
public class BHR_CookedFish extends Item {

	/** 补充饥饿值（原版熟鳕鱼 5）。 */
	public static final int NUTRITION = 5;

	/** 饱和度<b>点数</b>：原版熟鳕鱼 0.6 倍率 → {@code 5 × 0.6 × 2 = 6}（见 {@link BHR_SimpleFood}）。 */
	public static final float SATURATION_POINTS = 6.0F;

	/** 熟鱼片本体，在 {@link #OnInit()} 里赋值。 */
	public static Item BHR_COOKED_FISH;

	public BHR_CookedFish(Item.Properties properties) {
		super(properties);
	}

	/** 注册（层 12）。 */
	public static void OnInit() {
		BHR_COOKED_FISH = ModRegister.registerItem("bhr_cooked_fish",
				properties -> new BHR_CookedFish(BHR_SimpleFood.apply(properties, NUTRITION, SATURATION_POINTS)));
	}
}
