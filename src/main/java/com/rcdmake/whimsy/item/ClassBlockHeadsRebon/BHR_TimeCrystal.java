package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.world.item.Item;

/**
 * 时间水晶（{@code bhr_time_crystal}）—— 制作台加速用的消耗品。
 *
 * <h2>规格</h2>
 * <ul>
 *   <li><b>最大堆叠 99</b>（不是原版的 64）—— 用 {@code Item.Properties#stacksTo(99)}；</li>
 *   <li>每个水晶可跳过 <b>10 秒</b>制作时间（{@link #SECONDS_PER_CRYSTAL}）；</li>
 *   <li>放在制作台界面右下角的「时间水晶放置处」，退出界面时会<b>立即退回背包</b>（背包满则留在原地）。</li>
 * </ul>
 *
 * <h2>为什么是普通 {@link Item} 的子类</h2>
 * <p>它目前没有任何右键行为、组件或额外数据（堆叠上限是属性层的事），
 * 但按项目约定「单独物品单独成类」仍然独立一个类 —— 将来加组件（比如按等级分档）时不必挪位置。
 *
 * <h2>登记点</h2>
 * <ul>
 *   <li>①☑ 主类 {@code OnInit()}（物品阶段）　②☑ 创造栏 {@code BHR_CreativeTabs}（「其他物品」组）</li>
 *   <li>③☑ 中英 {@code BHR_LangEntries}　④☑ 贴图 {@code textures/item/bhr/time_crystal.png}
 *       + 模型 {@code models/item/bhr_time_crystal.json}（{@code item/generated} + layer0）；
 *       {@code items/bhr_time_crystal.json} 由 datagen 生成</li>
 * </ul>
 */
public class BHR_TimeCrystal extends Item {

	/** 最大堆叠数（刻意大于原版 64）。 */
	public static final int MAX_STACK_SIZE = 99;

	/** 每个水晶能跳过的制作时间（秒）。 */
	public static final int SECONDS_PER_CRYSTAL = 10;

	/** 物品本体，在 {@link #OnInit()} 里赋值。 */
	public static Item BHR_TIME_CRYSTAL;

	public BHR_TimeCrystal(Item.Properties properties) {
		super(properties);
	}

	/**
	 * 注册（物品阶段）。
	 *
	 * <p>⚠️ 堆叠上限<b>必须</b>在注册时套到 {@code ModRegister} 递进来的那份 {@code Item.Properties} 上：
	 * 它内部已经调用过 {@code setId(key)}，自己另外 {@code new Item.Properties()} 会丢掉物品 id
	 * （26.2 会直接抛 {@code NullPointerException: Item id not set}）。
	 * 实测踩过：早期版本另起了一份属性，游戏里水晶最大堆叠仍是 64。
	 */
	public static void OnInit() {
		BHR_TIME_CRYSTAL = ModRegister.registerItem("bhr_time_crystal",
				properties -> new BHR_TimeCrystal(properties.stacksTo(MAX_STACK_SIZE)));
	}
}
