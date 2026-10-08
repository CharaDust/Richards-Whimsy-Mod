package com.rcdmake.whimsy.item;

import com.rcdmake.whimsy.ModCreativeTabs;
import com.rcdmake.whimsy.block.ClassDEV.DEV_BlocksOther;
import com.rcdmake.whimsy.block.ClassDEV.DEV_GhostBlock;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateBoolean;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateEnum;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateEnumAxis;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateEnumFace;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateInt;
import com.rcdmake.whimsy.item.ClassDEV.DEV_ItemsOther;
import net.minecraft.world.item.ItemStack;

/**
 * 初学开发（{@code DEV}）分支的创造模式物品栏。
 *
 * <p>wiki 规格（{@code doc/wiki/ItemGroup/ClassDEV/DEV_General.md}）：
 * 注册名 {@code dev_general}，标题语言键 {@code itemGroup.dev_general}，
 * 图标为「一般物品」，内容按「包含物品」的顺序排列。
 *
 * <h2>内容怎么列（本类刻意不用「集合变量」）</h2>
 * 直接引用各方块/物品类里的变量名即可 ——
 * 形如 {@code DEV_ItemsOther.DEV_ITEM_NORMAL}、{@code DEV_StateBoolean.DEV_BLOCK_STATE_BOOLEAN}。
 *
 * <p>不需要在各类里再维护一个 {@code DEV_ITEMS = List.of(...)} 之类的中间集合：
 * 那个变量只是把同一个对象换个容器装一遍，既多一处要同步的地方，也看不出顺序来源。
 * 显式列在这里的好处：
 * <ul>
 *   <li>顺序一目了然（wiki 要求按「包含物品」的顺序）；</li>
 *   <li>变量名就是类里的字段名，IDE 的重命名/查找引用能一并覆盖；</li>
 *   <li>新增内容时会漏，但正因如此才「看得见」——不会悄悄进栏打乱顺序。</li>
 * </ul>
 *
 * <p><b>如果以后嫌列清单烦</b>：改用 {@link com.rcdmake.whimsy.ModRegister#registerItemFor}
 * 在注册时顺带登记，再用 {@link com.rcdmake.whimsy.ModRegister#itemsOf} /
 * {@link com.rcdmake.whimsy.ModRegister#blocksOf} 按模块一次取出即可（代价是顺序变为注册顺序）。
 *
 * <p><b>注册动作</b>：统一用 {@link ModCreativeTabs#registerTab}（阶段 0 起，三个模块共用一份写法），
 * 本类只提供「注册 id / 图标 / 包含清单」。
 */
public final class DEV_CreativeTabs {

	private DEV_CreativeTabs() {
	}

	/** 注册 id（wiki 目标值）。 */
	public static final String GROUP_ID = "dev_general";

	/** 标题语言键，具体标题写在 lang 文件里。 */
	public static final String LANG_KEY = "itemGroup." + GROUP_ID;

	/**
	 * 初始化：注册本分支的创造模式物品栏。
	 * <p>由 {@link ModCreativeTabs#OnInit()} 统一调用，且必须在所有方块/物品的
	 * {@code OnInit()} 之后 —— 本类直接引用它们的静态字段。
	 */
	public static void OnInit() {
		ModCreativeTabs.registerTab(GROUP_ID,
				// 图标：一般物品
				() -> new ItemStack(DEV_ItemsOther.DEV_ITEM_NORMAL),
				(parameters, output) -> {
					// 顺序照 wiki「包含物品」的排列
					// 一般方块、固定向上方块
					output.accept(DEV_BlocksOther.DEV_BLOCK_NORMAL);
					output.accept(DEV_BlocksOther.DEV_BLOCK_VECTOR_FIX_SIDE_U);
					// 布尔方块、整型方块、枚举方块、轴方块、朝向方块
					output.accept(DEV_StateBoolean.DEV_BLOCK_STATE_BOOLEAN);
					output.accept(DEV_StateInt.DEV_BLOCK_STATE_INT);
					output.accept(DEV_StateEnum.DEV_BLOCK_STATE_ENUM);
					output.accept(DEV_StateEnumAxis.DEV_BLOCK_STATE_AXIS);
					output.accept(DEV_StateEnumFace.DEV_BLOCK_STATE_FACE);
					// 幽灵方块
					output.accept(DEV_GhostBlock.DEV_BLOCK_GHOST);
					// 一般物品
					output.accept(DEV_ItemsOther.DEV_ITEM_NORMAL);
				});
	}
}
