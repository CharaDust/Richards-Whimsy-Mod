package com.rcdmake.whimsy.item;

import com.rcdmake.whimsy.ModCreativeTabs;

/**
 * 方块愚者（{@code BHR}）分支的创造模式物品栏。
 *
 * <p>wiki 规格（{@code doc/wiki/ItemGroup/ClassBlockHeadsRebon/BHR_General.md}）：
 * 注册名 {@code bhr_general}，标题语言键 {@code itemGroup.bhr_general}。
 * 该分支的方块与物品尚未重制，因此本类暂时留空。
 *
 * <p>重制时照 {@link DEV_CreativeTabs} 的写法补上即可：
 * <pre>{@code
 * ResourceKey<CreativeModeTab> key =
 *         ResourceKey.create(Registries.CREATIVE_MODE_TAB, ModRegister.id(GROUP_ID));
 * CreativeModeTab tab = FabricCreativeModeTab.builder()
 *         .title(Component.translatable(LANG_KEY))
 *         .icon(() -> new ItemStack(BHR_xxx.SOME_ITEM))   // 图标
 *         .displayItems((parameters, output) -> {
 *             BHR_ItemsOther.BHR_ITEMS.forEach(output::accept);
 *             BHR_BlocksOther.BHR_BLOCKS.forEach(output::accept);
 *         })
 *         .build();
 * Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, tab);
 * }</pre>
 */
public final class BHR_CreativeTabs {

	private BHR_CreativeTabs() {
	}

	/** 注册 id（wiki 目标值）。 */
	public static final String GROUP_ID = "bhr_general";

	/** 标题语言键，具体标题写在 lang 文件里。 */
	public static final String LANG_KEY = "itemGroup." + GROUP_ID;

	/**
	 * 初始化：注册本分支的创造模式物品栏。
	 * <p>由 {@link ModCreativeTabs#OnInit()} 统一调用。
	 * <p><b>当前为空</b>——本分支尚无重制完成的方块/物品。
	 */
	public static void OnInit() {
		// TODO(BHR 重制后)：按类注释里的模板注册 bhr_general 标签页。
	}
}
