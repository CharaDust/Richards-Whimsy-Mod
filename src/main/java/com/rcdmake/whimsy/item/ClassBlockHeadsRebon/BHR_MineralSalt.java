package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.item.Item;

/**
 * 无机盐（{@code bhr_mineral_salt}）—— 篝火线煮水晒出来的材料物品（Blockheads 的 Salt）。
 *
 * <h2>规格</h2>
 * <ul>
 *   <li>纯材料：无组件、无右键行为，默认堆叠 64；</li>
 *   <li>获取：篝火 {@code mineral_salt.json} —— <b>锡水桶 ×1 → 无机盐 ×10</b>
 *       （桶里的水煮干，<b>空锡桶按 {@code container} 字段返还</b>），80 秒；</li>
 *   <li>用途：篝火 {@code flatbread.json} —— 无机盐 ×1。</li>
 * </ul>
 *
 * <h2>范式契约栏（层 0）</h2>
 * <ul>
 *   <li><b>中文名</b> 无机盐　<b>英文名</b> Mineral Salt　<b>注册名</b> {@code bhr_mineral_salt}　<b>模块</b> BHR</li>
 *   <li><b>父级</b> 无（普通 {@code Item}）</li>
 *   <li><b>启用的可选层</b>：层 2 物品声明、层 12 初始化</li>
 *   <li><b>跳过的可选层</b>：同 {@link BHR_Flour}（层 1 / 3 / 4 / 6 / 7 / 8 / 9 / 10 / 11 / C / M 均无需求）</li>
 *   <li><b>登记点</b>：①☑ 主类物品区　②☑ 手写 {@code models/item/bhr_mineral_salt.json} + datagen
 *       　③☑ 中英 {@code BHR_LangEntries}　④☑ {@code BHR_CreativeTabs}（材料组）
 *       　⑤☑ 贴图 {@code textures/item/bhr/mineral_salt.png}　⑥☑ 篝火配方（产物 + 原料各一处）</li>
 * </ul>
 */
public class BHR_MineralSalt extends Item {

	/** 无机盐本体，在 {@link #OnInit()} 里赋值。 */
	public static Item BHR_MINERAL_SALT;

	public BHR_MineralSalt(Item.Properties properties) {
		super(properties);
	}

	/** 注册（层 12）。 */
	public static void OnInit() {
		BHR_MINERAL_SALT = ModRegister.registerItem("bhr_mineral_salt", BHR_MineralSalt::new);
	}
}
