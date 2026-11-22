package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumables;

/**
 * BHR「普通食物」的<b>唯一装配入口</b>（不是物品类，不进注册表）。
 *
 * <p>按 itemv1.md §7 第 11 条「同一条规则只有一份实现」：篝火线这一批 5 件食物
 * （生鱼片 / 熟鱼片 / 生渡渡鸟肉 / 熟渡渡鸟肉 / 煎饼）除了<b>饥饿值与饱和度</b>以外完全同构，
 * 把它们各自抄一遍就等于留 5 份会走样的副本。所以：
 * <ul>
 *   <li>{@code minecraft:food} = {@link FoodProperties}({@code nutrition}, {@code saturation}, {@code false})；</li>
 *   <li>{@code minecraft:consumable} = {@link Consumables#defaultFood()} —— 原版普通食物的默认配置
 *       （EAT 动画、1.6 秒、咀嚼音、有粒子），<b>不带任何 {@code onConsume} 效果</b>。</li>
 * </ul>
 *
 * <h2>⚠️ 26.2 的 {@code saturation} 是<b>实际饱和度点数</b>，不是倍率（2026-11 核实）</h2>
 * <p>反编译 26.2 的 {@code minecraft-merged-deobf-26.2.jar}，逐条证据：
 * <pre>{@code
 * // FoodProperties —— 记录组件，第二个字段直接叫 saturation
 * public record FoodProperties(int nutrition, float saturation, boolean canAlwaysEat)
 *
 * // FoodData.eat(FoodProperties) —— 直接把它加到饱和度上，不乘营养、不乘 2
 * public void eat(FoodProperties foodProperties) {
 *     this.add(foodProperties.nutrition(), foodProperties.saturation());
 * }
 * private void add(int food, float saturation) {
 *     this.foodLevel = Math.min(food + this.foodLevel, 20);
 *     this.saturationLevel = Mth.clamp(saturation + this.saturationLevel, 0.0F, (float)this.foodLevel);
 * }
 *
 * // 旧口径（倍率）现在只存在于 Builder 里：build() 时换算好再塞进上面那个字段
 * public FoodProperties build() {
 *     float saturation = FoodConstants.saturationByModifier(this.nutrition, this.saturationModifier);
 *     return new FoodProperties(this.nutrition, saturation, this.canAlwaysEat);
 * }
 * // FoodConstants：saturationByModifier(n, m) = n * m * 2
 * }</pre>
 * 结论：直接 {@code new FoodProperties(8, 3.0F, false)} = 补 <b>8 饥饿值 + 3 点饱和度</b>；
 * 想要旧写法「倍率 0.6」的效果（例如原版熟鸡肉 6/0.6 → 7.2 点），换算公式是
 * <b>{@code 点数 = 饥饿值 × 倍率 × 2}</b>。
 *
 * <p>所以本方法的第三个参数叫 {@code saturationPoints}，写作<b>点数</b>。
 * 想按原版倍率写，就自己乘一下：{@code 6 * 0.6F * 2 = 7.2F}。
 *
 * <p>⚠️ 另一件与旧版不同的地方：这里**不**显式写 {@code stacksTo(64)} ——
 * 64 就是 26.2 的默认堆叠上限（见 {@code BHR_Lime} 的注释）。
 */
public final class BHR_SimpleFood {

	private BHR_SimpleFood() {
	}

	/**
	 * 把「普通食物」的两个组件挂到物品属性上。
	 *
	 * @param properties       注册入口造好的属性（已带物品 id）
	 * @param nutrition        补充饥饿值（{@code minecraft:food.nutrition}）
	 * @param saturationPoints <b>实际饱和度点数</b>（{@code minecraft:food.saturation}）——
	 *                         不是倍率；原版倍率 m 换算过来是 {@code nutrition * m * 2}
	 */
	public static Item.Properties apply(Item.Properties properties, int nutrition, float saturationPoints) {
		return properties
				.component(DataComponents.FOOD, new FoodProperties(nutrition, saturationPoints, false))
				.component(DataComponents.CONSUMABLE, Consumables.defaultFood().build());
	}
}
