package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.item.Item;

/**
 * 铜锭（{@code bhr_copper_ingot}）—— 冶炼线的基础材料（Blockheads 的 Copper Ingot）。
 *
 * <p>本轮只注册物品本体；获取途径（矿石 → 熔炉）与用途（青铜锭）都随 furnace / metalwork 工作面落地。
 * ⚠️ 贴图<b>尚未入库</b>，模型暂时借用原版 {@code minecraft:item/copper_ingot} 作占位
 * （见 {@code models/item/bhr_copper_ingot.json}），有美术资源后换 layer0 即可。
 */
public class BHR_CopperIngot extends Item {

	/** 铜锭本体。 */
	public static Item BHR_COPPER_INGOT;

	public BHR_CopperIngot(Item.Properties properties) {
		super(properties);
	}

	public static void OnInit() {
		BHR_COPPER_INGOT = ModRegister.registerItem("bhr_copper_ingot", BHR_CopperIngot::new);
	}
}
