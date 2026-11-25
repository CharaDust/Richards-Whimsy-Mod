package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.item.Item;

/**
 * 锡锭（{@code bhr_tin_ingot}）—— 冶炼线的基础材料（Blockheads 的 Tin Ingot）。
 *
 * <p>本轮只注册物品本体；获取途径（锡矿石 → 熔炉）与用途（青铜锭 / 锡桶）都随后续工作面落地。
 * ⚠️ 贴图<b>尚未入库</b>，模型暂时借用原版 {@code minecraft:item/iron_ingot} 作占位
 * （见 {@code models/item/bhr_tin_ingot.json}），有美术资源后换 layer0 即可。
 */
public class BHR_TinIngot extends Item {

	/** 锡锭本体。 */
	public static Item BHR_TIN_INGOT;

	public BHR_TinIngot(Item.Properties properties) {
		super(properties);
	}

	public static void OnInit() {
		BHR_TIN_INGOT = ModRegister.registerItem("bhr_tin_ingot", BHR_TinIngot::new);
	}
}
