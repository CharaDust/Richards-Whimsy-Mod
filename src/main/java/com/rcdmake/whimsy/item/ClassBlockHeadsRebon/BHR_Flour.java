package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.item.Item;

/**
 * 面粉（{@code bhr_flour}）—— 篝火线的材料物品（Blockheads 的 Flour）。
 *
 * <h2>规格</h2>
 * <ul>
 *   <li>纯材料：没有组件、没有右键行为，最大堆叠就是默认的 64；</li>
 *   <li>获取：待定（Blockheads 里由研磨台磨出，本项目尚未落地研磨线）；</li>
 *   <li>用途：篝火 {@code flatbread.json} —— 面粉 ×5 + 锡水桶 ×1 + 无机盐 ×1 → 煎饼 ×5。</li>
 * </ul>
 *
 * <h2>范式契约栏（层 0）</h2>
 * <ul>
 *   <li><b>中文名</b> 面粉　<b>英文名</b> Flour　<b>注册名</b> {@code bhr_flour}　<b>模块</b> BHR</li>
 *   <li><b>父级</b> 无（普通 {@code Item}）—— 与 {@code DEV_ItemsOther} 同一路数：纯声明类</li>
 *   <li><b>启用的可选层</b>：层 2 物品声明、层 12 初始化</li>
 *   <li><b>跳过的可选层</b>：层 1 常量（没有魔法数值）、层 3 组件 / 层 4 装配（无组件）、
 *       层 6 家族 / 层 7 codec / 层 8 生命周期 / 层 9 交互 / 层 10 外观 / 层 11 预留 / 层 C / 层 M</li>
 *   <li><b>登记点</b>：①☑ 主类物品区　②☑ 手写 {@code models/item/bhr_flour.json} + datagen
 *       　③☑ 中英 {@code BHR_LangEntries}　④☑ {@code BHR_CreativeTabs}（材料组）
 *       　⑤☑ 贴图 {@code textures/item/bhr/flour.png}　⑥☑ 篝火配方（作为原料）</li>
 * </ul>
 */
public class BHR_Flour extends Item {

	/** 面粉本体，在 {@link #OnInit()} 里赋值。 */
	public static Item BHR_FLOUR;

	public BHR_Flour(Item.Properties properties) {
		super(properties);
	}

	/** 注册（层 12）。 */
	public static void OnInit() {
		BHR_FLOUR = ModRegister.registerItem("bhr_flour", BHR_Flour::new);
	}
}
