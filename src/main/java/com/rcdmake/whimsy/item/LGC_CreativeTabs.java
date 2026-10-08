package com.rcdmake.whimsy.item;

import com.rcdmake.whimsy.ModCreativeTabs;

/**
 * 原版考古（{@code LGC}）分支的创造模式物品栏。
 *
 * <p>wiki 的注册目录（{@code doc/wiki/RichardsWhimsyMod.md}）把「原版考古（LGC）」
 * 列为子模块，但**尚未为它建立物品栏规格页**，也没有既有的物品栏注册。
 * 因此本类暂时留空，注册名待定（按命名规则应形如 {@code lgc_general}）。
 *
 * <p>重制时照 {@link DEV_CreativeTabs} 的写法补上即可。
 */
public final class LGC_CreativeTabs {

	private LGC_CreativeTabs() {
	}

	/**
	 * 注册 id。
	 * <p><b>待定</b>：wiki 暂无 LGC 物品栏页，这里先按命名规则填 {@code lgc_general}，
	 * 出规格页后请以规格页为准。
	 */
	public static final String GROUP_ID = "lgc_general";

	/** 标题语言键，具体标题写在 lang 文件里。 */
	public static final String LANG_KEY = "itemGroup." + GROUP_ID;

	/**
	 * 初始化：注册本分支的创造模式物品栏。
	 * <p>由 {@link ModCreativeTabs#OnInit()} 统一调用。
	 * <p><b>当前为空</b>——本分支尚无重制完成的方块/物品。
	 */
	public static void OnInit() {
		// TODO(LGC 重制后)：按 DEV_CreativeTabs 的模板注册标签页。
	}
}
