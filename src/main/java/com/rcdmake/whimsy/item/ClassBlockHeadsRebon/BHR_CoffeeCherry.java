package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;

/**
 * 咖啡樱桃（{@code bhr_coffee_cherry}）—— 由旧项目的 {@code BHR_FoodOther}（{@code #deprecated} 集合类）迁来，
 * 旧注册名 {@code bhr_coffee_cherry} 未变，按「一物一类」约定独立成类。
 *
 * <p>wiki 规格（{@code doc/wiki/Items/ClassBlockHeadsRebon/BHR_CoffeeCherry.md}）：
 * <ul>
 *   <li>特点简介：一种食物物品；可被用于种植咖啡樱桃树</li>
 *   <li>组件：默认组件 + {@code minecraft:food}（nutrition 4 / saturation 1 / can_always_eat FALSE）
 *       + {@code minecraft:consumable}（{@code consume_seconds} 1.6，其余走原版普通食物默认）</li>
 *   <li>改写方法 / 事件 / 获取方式（配方与战利品表）：无</li>
 * </ul>
 *
 * <h2>本次实现范围（按用户 2026-10 指示）</h2>
 * wiki 的「种植」条目「在合适的土壤上右键可用于放置咖啡樱桃树树叶」<b>本次跳过</b>：
 * 咖啡樱桃树树叶方块还没有添加，等它落地后照 {@link BHR_PineCone}（松果）的 {@code useOn} 范式补。
 *
 * <h2>范式契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_coffee_cherry}　<b>模块</b> BHR　<b>父级</b> 无（普通 {@code Item}）</li>
 *   <li><b>启用的可选层</b>：层 1 规格常量、层 3 组件装配</li>
 *   <li><b>跳过的可选层</b>：层 4 属性工厂、层 6 家族 / 层 7 codec、层 8 生命周期、
 *       层 9 交互（吃由组件驱动）、层 10 形状与外观、层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code onInitialize()}　②☑ 手写 {@code models/item/bhr_coffee_cherry.json}
 *       （既有资源）+ datagen 自动生成 {@code items/bhr_coffee_cherry.json}　③☑ 中英 {@code BHR_LangEntries}
 *       　④☑ {@code BHR_CreativeTabs}（食物组）　⑤☑ 贴图 {@code textures/item/bhr/coffee_cherry.png}（既有资源）
 *       　⑥☐ 配方 / 战利品表 —— wiki 写「无」</li>
 * </ul>
 */
public class BHR_CoffeeCherry extends Item {

	// ── 层 1 ｜ 规格常量（全部取自 wiki 的组件表）──

	/** 补充饥饿值（wiki 组件表 {@code minecraft:food.nutrition}）。 */
	public static final int NUTRITION = 4;

	/** 饱和度倍率（wiki 组件表 {@code minecraft:food.saturation}；实际饱和度 = 饥饿值 × 该值）。 */
	public static final float SATURATION = 1.0F;

	/** 无视饥饿值食用（wiki 组件表 {@code minecraft:food.can_always_eat} 写 FALSE）。 */
	public static final boolean CAN_ALWAYS_EAT = false;

	/** 使用时间（秒）—— wiki 组件表 {@code minecraft:consumable.consume_seconds}。 */
	public static final float CONSUME_SECONDS = 1.6F;

	// ── 层 2 ｜ 物品声明 ──

	/** 咖啡樱桃本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Item BHR_COFFEE_CHERRY;

	// ── 层 5 ｜ 构造器 ──

	/** 构造器（层 5）：只做装配，数值全在组件里。 */
	public BHR_CoffeeCherry(Item.Properties properties) {
		super(properties);
	}

	// ── 层 3 ｜ 组件装配 ──

	/** {@code minecraft:food} 组件（层 3）：{@code (nutrition, saturation, canAlwaysEat)}。 */
	public static FoodProperties coffeeCherryFood() {
		return new FoodProperties(NUTRITION, SATURATION, CAN_ALWAYS_EAT);
	}

	/** {@code minecraft:consumable} 组件（层 3）：原版普通食物默认 + 覆盖 {@code consume_seconds}。 */
	public static Consumable coffeeCherryConsumable() {
		return Consumables.defaultFood()
				.consumeSeconds(CONSUME_SECONDS)
				.build();
	}

	// ── 层 12 ｜ 初始化 ──

	/** 注册（层 12）。 */
	public static void OnInit() {
		BHR_COFFEE_CHERRY = ModRegister.registerItem("bhr_coffee_cherry",
				properties -> new BHR_CoffeeCherry(properties
						.component(DataComponents.FOOD, coffeeCherryFood())
						.component(DataComponents.CONSUMABLE, coffeeCherryConsumable())));
	}
}
