package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.item.Item;

/**
 * 生鱼片（{@code bhr_raw_fish}）—— 篝火线的生食原料，也是「熟鱼片」的唯一原料。
 *
 * <h2>规格</h2>
 * <ul>
 *   <li>饥饿值 <b>2</b>、饱和度 <b>0.4 点</b> —— 按需求方给的口径「参考原版鳕鱼（生）」，
 *       即原版 {@code Foods.COD}（2 饥饿 / 0.1 倍率）换算成点数 {@code 2×0.1×2 = 0.4}；</li>
 *   <li>组件：{@code minecraft:food} + {@code minecraft:consumable}，走
 *       {@link BHR_SimpleFood#apply} 的统一装配（普通食物，无附加效果）；</li>
 *   <li>获取：待定（「获取方式」阶段 —— 捕鱼机制尚未落地）；篝火用「生鱼片 ×1 + 锡箔 ×1」烤成熟鱼片。</li>
 * </ul>
 *
 * <h2>范式契约栏（层 0）</h2>
 * <ul>
 *   <li><b>中文名</b> 生鱼片　<b>英文名</b> Raw Fish　<b>注册名</b> {@code bhr_raw_fish}　<b>模块</b> BHR</li>
 *   <li><b>父级</b> 无（普通 {@code Item}）—— 吃的行为全部由组件驱动</li>
 *   <li><b>启用的可选层</b>：层 1 规格常量、层 2 物品声明、层 3 组件（经 {@link BHR_SimpleFood}）、层 12 初始化</li>
 *   <li><b>跳过的可选层</b>：层 4 属性装配方法（组件只有两个，且已收进 {@link BHR_SimpleFood}）、
 *       层 6 家族 / 层 7 codec / 层 8 生命周期 / 层 9 交互 / 层 10 外观 / 层 11 预留 / 层 C / 层 M —— 无需求</li>
 *   <li><b>登记点</b>：①☑ 主类物品区　②☑ 手写 {@code models/item/bhr_raw_fish.json}（2D 图标）
 *       + datagen 生成 {@code items/bhr_raw_fish.json}　③☑ 中英 {@code BHR_LangEntries}
 *       　④☑ {@code BHR_CreativeTabs}（食物组）　⑤☑ 贴图 {@code textures/item/bhr/raw_fish.png}（既有资源）
 *       　⑥☑ 配方：篝火 {@code bhr_recipe/bhr_campfire/cooked_fish.json}（作为原料）</li>
 * </ul>
 */
public class BHR_RawFish extends Item {

	/** 补充饥饿值（原版生鳕鱼 2）。 */
	public static final int NUTRITION = 2;

	/**
	 * 饱和度<b>点数</b>（不是倍率）—— 原版生鳕鱼是 2 / 0.1 倍率，换算 {@code 2 × 0.1 × 2 = 0.4}
	 * （换算依据见 {@link BHR_SimpleFood} 的类注释）。
	 */
	public static final float SATURATION_POINTS = 0.4F;

	/** 生鱼片本体，在 {@link #OnInit()} 里赋值。 */
	public static Item BHR_RAW_FISH;

	public BHR_RawFish(Item.Properties properties) {
		super(properties);
	}

	/** 注册（层 12）。 */
	public static void OnInit() {
		BHR_RAW_FISH = ModRegister.registerItem("bhr_raw_fish",
				properties -> new BHR_RawFish(BHR_SimpleFood.apply(properties, NUTRITION, SATURATION_POINTS)));
	}
}
