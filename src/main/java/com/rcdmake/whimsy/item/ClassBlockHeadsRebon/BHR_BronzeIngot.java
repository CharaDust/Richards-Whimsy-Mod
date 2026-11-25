package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.item.Item;

/**
 * 青铜锭（{@code bhr_bronze_ingot}）—— Blockheads 的 Bronze Ingot，木工台专家配方（门 / 活板门）的原料。
 *
 * <p>本轮做成<b>白板材料物品</b>（贴图 {@code textures/item/bhr/bronze_ingot.png} 已就位）。
 * 正式来源是冶炼线（铜锭 ×2 + 锡锭 ×1 → 3 个，Furnace / Metalwork Bench），
 * 等那几台工作面落地后再接通。
 */
public class BHR_BronzeIngot extends Item {

	/** 青铜锭本体。 */
	public static Item BHR_BRONZE_INGOT;

	public BHR_BronzeIngot(Item.Properties properties) {
		super(properties);
	}

	public static void OnInit() {
		BHR_BRONZE_INGOT = ModRegister.registerItem("bhr_bronze_ingot", BHR_BronzeIngot::new);
	}
}
