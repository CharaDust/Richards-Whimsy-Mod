package com.rcdmake.whimsy.item.ClassDEV;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.item.Item;

/**
 * 开发其他物品集合（物品）—— 由旧的 {@code DEVItems} 更名而来。
 *
 * <p>包含 1 个物品：一般物品 {@code dev_item_normal}。
 *
 * <p>wiki 规格（{@code doc/wiki/Items/ClassDEV/DEV_ItemsOther.md}）：
 * 普通物品，无特殊构造器，功能由「组件」定义；改写方法、事件均为「无」，
 * 获取方式「无」。组件页里 {@code max_stack_size} 写 64 并注明
 * 「需要指定，否则无法堆叠」——但 26.2 里 64 就是默认值，
 * 只有最大堆叠数不是 64 时才需要写 {@code stacksTo(...)}，所以这里保持默认。
 *
 * <p>wiki 还记了第二个物品「某人的QQ头像物品」，但注明
 * 「**未在代码中找到注册项**」——旧类里确实没有，故本次不注册它。
 */
public class DEV_ItemsOther {

	/** 一般物品 {@code dev_item_normal}。 */
	public static Item DEV_ITEM_NORMAL;

	/**
	 * 初始化：构造并注册本类的全部物品。
	 * <p>主类只需要调用这一条。
	 *
	 * <p>这里**不再**维护 {@code DEV_ITEMS} 之类的集合变量 ——
	 * 创造模式物品栏直接引用上面的 {@code DEV_ITEM_NORMAL} 即可，
	 * 少一处需要同步的地方（详见 {@code DEV_CreativeTabs} 的类注释）。
	 */
	public static void OnInit() {
		DEV_ITEM_NORMAL = ModRegister.registerItem("dev_item_normal", Item::new);
	}
}
