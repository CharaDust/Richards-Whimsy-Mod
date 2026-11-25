package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.item.Item;

/**
 * 亚麻布（{@code bhr_linen}）—— Blockheads 的 Linen，木工台「小船」的原料。
 *
 * <p>本轮只做成<b>白板材料物品</b>（美术已就位，贴图 {@code textures/item/bhr/linen.png}）。
 * 它的正式来源（亚麻 ×5 → 1，Tailor's Bench）要等裁缝台落地，
 * 在此之前只能通过创造模式 / 指令获取 —— 与项目里「待补获取途径」的处理一致。
 */
public class BHR_Linen extends Item {

	/** 亚麻布本体。 */
	public static Item BHR_LINEN;

	public BHR_Linen(Item.Properties properties) {
		super(properties);
	}

	public static void OnInit() {
		BHR_LINEN = ModRegister.registerItem("bhr_linen", BHR_Linen::new);
	}
}
