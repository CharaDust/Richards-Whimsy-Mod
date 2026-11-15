package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.item.Item;

/**
 * 方块愚者徽标（{@code bhr_blockheads_icon}）。
 *
 * <p>wiki 规格（{@code doc/wiki/Items/ClassBlockHeadsRebon/BHR_ItemsOther.md}）：
 * 方块愚者分支的杂项物品，原集合类 {@code BHR_ItemsOther} 里唯一一件 ——
 * 「方块愚者徽标」。按本项目「一物品一类」的约定拆成独立类。
 *
 * <h2>它为什么现在才落地</h2>
 * 该贴图（{@code textures/item/bhr/blockheads_icon.png}）在 1.20.4 时代就有，
 * 但方块愚者物品栏的图标一直「暂用堆肥块」——因为徽标物品本身从未注册过。
 * 本次（愚者模式）一并注册，两处用它：
 * <ul>
 *   <li>{@link com.rcdmake.whimsy.item.BHR_CreativeTabs} 的标签页图标；</li>
 *   <li>F3+F4 游戏模式切换器里「愚者模式」那一格的图标
 *       （{@code GameModeSwitcherScreen$GameModeIcon} 的图标是一个 {@code ItemStack}，
 *        所以这里必须有一件真实物品，而不是一张裸贴图）。</li>
 * </ul>
 *
 * <p>没有食物、没有组件、没有交互 —— 它就是一枚徽标。
 */
public class BHR_BlockHeadsIcon extends Item {

	/** 注册名（wiki 目标值）。 */
	public static final String REGISTRY_NAME = "bhr_blockheads_icon";

	/** 物品本体，在 {@link #OnInit()} 里赋值。 */
	public static Item BHR_BLOCKHEADS_ICON;

	public BHR_BlockHeadsIcon(Item.Properties properties) {
		super(properties);
	}

	/** 注册。由主类 {@code onInitialize()} 在创造模式物品栏之前调用。 */
	public static void OnInit() {
		BHR_BLOCKHEADS_ICON = ModRegister.registerItem(REGISTRY_NAME, BHR_BlockHeadsIcon::new);
	}
}
