package com.rcdmake.whimsy.item;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;

import java.util.List;
import java.util.Optional;

/**
 * 本模组「效果药水」的<b>公共注册写法</b>：一件物品 = 效果 + 时长 + 等级 + 形态。
 *
 * <h2>三种形态</h2>
 * <ul>
 *   <li>{@link Form#DRINK} —— 饮用型：{@link Item} + 原版饮用 {@code consumable} 组件，喝完返还玻璃瓶；</li>
 *   <li>{@link Form#SPLASH} —— 喷溅型：{@link SplashPotionItem}（原版类），投出后按落点范围施加；</li>
 *   <li>{@link Form#LINGERING} —— 滞留型：{@link LingeringPotionItem}（原版类），落点生成效果云。</li>
 * </ul>
 *
 * <h2>效果写在 {@code potion_contents} 里</h2>
 * <p>喷溅/滞留的生效流程是「投掷物落地 → 读<b>物品栈上的组件</b> → 施加给范围内生物」，
 * 所以效果必须落在 {@code minecraft:potion_contents} 上才生效
 * （见 {@code AbstractThrownPotion} 读的是 {@code this.getItem()} 的该组件）。
 * 原版把它指向 {@code minecraft:potion} 注册表条目；本模组往
 * {@code PotionContents} 的 {@code customEffects} 直接放效果实例 —— 同样能跑，
 * 且不必维护那个注册表。附带白拿两件事：{@code PotionContents} 是
 * {@code TooltipProvider}，效果与时长会<b>自动</b>进物品提示；{@code customColor} 决定水色。
 *
 * <h2>⚠️⚠️ 互斥（喝 A 顶掉 B）为什么必须用「原版注册过的」ConsumeEffect</h2>
 * <p>这里踩过两次坑，都记下来：
 * <ol>
 *   <li><b>第一版：自定义 {@code ConsumeEffect}。</b> 组件会被序列化重建
 *       （物品栈 {@code ItemStack.copy()}/网络同步都会触发），codec 按
 *       {@code getType()}（我们声明的是原版 {@code remove_effects}）去找
 *       <b>原版</b> {@code RemoveStatusEffectsConsumeEffect} 反序列化，
 *       自定义实现里的清单被丢弃 → 互斥变成空操作。</li>
 *   <li><b>第二版：改在物品的 {@code finishUsingItem} 里移除。</b> 也没生效。
 *       不再深究该路径的细节 —— 与其赌调用链，不如用原版既有机制。</li>
 * </ol>
 * <b>现方案</b>：直接用原版的 {@link RemoveStatusEffectsConsumeEffect}
 * （{@code remove_effects} 的<b>正牌实现</b>，有 codec、能安全序列化），
 * 在<b>注册时</b>就把它按正确顺序放进 {@code consumable} 的效果列表：
 * <pre>{@code
 * Consumable = [ RemoveStatusEffectsConsumeEffect(同族), ApplyStatusEffectsConsumeEffect(本品) ]
 * }</pre>
 * 原版 {@code Consumable#onConsume} 按列表顺序逐个执行，于是「先移除同族、再施加本品」
 * 的语义由原版保证，不依赖任何自定义序列化行为。
 *
 * <p>推论：<b>不要</b>给这里加自定义 {@code ConsumeEffect} ——
 * 除非同时注册它的 {@code Type} 与 codec。
 */
public final class WhimsyPotionRegistration {

	/** 药水形态。 */
	public enum Form {
		/** 饮用型。 */
		DRINK,
		/** 喷溅型。 */
		SPLASH,
		/** 滞留型。 */
		LINGERING
	}

	private WhimsyPotionRegistration() {
	}

	/**
	 * 注册一件药水（无互斥同族）。
	 *
	 * @see #register(String, Holder, int, int, Form, int, List)
	 */
	public static Item register(String id, Holder<MobEffect> effect, int durationTicks,
			int amplifier, Form form, int tintColor) {
		return register(id, effect, durationTicks, amplifier, form, tintColor, List.of());
	}

	/**
	 * 注册一件药水。
	 *
	 * @param id            注册名（省略命名空间）
	 * @param effect        效果句柄（须已注册）
	 * @param durationTicks 时长（tick）
	 * @param amplifier     等级（0 = 游戏内 I 级）
	 * @param form          形态
	 * @param tintColor     水色（打包 RGB），一般取效果的粒子颜色
	 * @param exclusiveSiblings <b>互斥同族</b>：喝下本品前先移除它们（空列表 = 不互斥）。
	 *                          只在 {@link Form#DRINK} 下有效 —— 喷溅/滞留是投掷生效，没有「喝」这个动作。
	 */
	public static Item register(String id, Holder<MobEffect> effect, int durationTicks,
			int amplifier, Form form, int tintColor, List<Holder<MobEffect>> exclusiveSiblings) {
		MobEffectInstance instance = new MobEffectInstance(effect, durationTicks, amplifier);
		PotionContents contents = new PotionContents(
				Optional.empty(),
				Optional.of(tintColor),
				List.of(instance),
				Optional.empty());

		return switch (form) {
			case DRINK -> ModRegister.registerItem(id, properties -> new Item(properties
					.stacksTo(1)
					.component(DataComponents.POTION_CONTENTS, contents)
					.component(DataComponents.CONSUMABLE, drinkConsumable(instance, exclusiveSiblings))
					// 与原版药水一致：喝完返还玻璃瓶
					.usingConvertsTo(Items.GLASS_BOTTLE)));
			// 喷溅/滞留：继承原版类（投掷与生效逻辑全由原版负责），只覆写取名。
			case SPLASH -> ModRegister.registerItem(id, properties ->
					new NamedSplashPotion(properties
							.stacksTo(1)
							.component(DataComponents.POTION_CONTENTS, contents)));
			case LINGERING -> ModRegister.registerItem(id, properties ->
					new NamedLingeringPotion(properties
							.stacksTo(1)
							.component(DataComponents.POTION_CONTENTS, contents)));
		};
	}

	/**
	 * 喷溅型药水：继承原版 {@link SplashPotionItem}，<b>只为了覆写取名</b>。
	 *
	 * <h2>⚠️ 为什么必须覆写 getName（否则显示成裸键名）</h2>
	 * <p>原版 {@code PotionItem.getName}（{@code SplashPotionItem} 与
	 * {@code LingeringPotionItem} 都继承它）是<b>无条件</b>走药水组件拼名字的：
	 * <pre>{@code
	 * // PotionItem#getName（逐行读过原版）
	 * PotionContents potion = itemStack.get(DataComponents.POTION_CONTENTS);
	 * return potion != null ? potion.getName(this.descriptionId + ".effect.")
	 *                        : super.getName(itemStack);
	 * }</pre>
	 * 而 {@code PotionContents.getName} 在没有 {@code potion} 字段（药水注册表条目）时
	 * 固定取 {@code effect.empty} 拼键。本模组的药水恰恰<b>不走那个注册表</b>
	 * （效果放在 {@code customEffects} 里），于是得到键
	 * {@code item.<命名空间>.<物品 id>.effect.empty} ——
	 * 翻译文件里没有这个键，游戏里就把键名原样显示出来。
	 *
	 * <p>（饮用型没有这个问题：它注册的是普通 {@link Item}，不经过
	 * {@code PotionItem.getName} 那条路径。所以早先只有喷溅/滞留显示异常。）
	 *
	 * <p>覆写后名字走普通物品名（{@code item.<命名空间>.<物品 id>}），语言条目正常命中。
	 * <b>效果与时长仍由 {@code PotionContents} 作为 {@code TooltipProvider} 提供</b>，
	 * 提示不受影响 —— 这里只改「名字」。
	 */
	private static final class NamedSplashPotion extends SplashPotionItem {

		NamedSplashPotion(Item.Properties properties) {
			super(properties);
		}

		@Override
		public Component getName(ItemStack stack) {
			// 走普通物品名，绕开 PotionContents 的 effect.empty 路径
			return Component.translatable(this.getDescriptionId());
		}
	}

	/** 滞留型药水：同 {@link NamedSplashPotion}，只是继承原版 {@link LingeringPotionItem}。 */
	private static final class NamedLingeringPotion extends LingeringPotionItem {

		NamedLingeringPotion(Item.Properties properties) {
			super(properties);
		}

		@Override
		public Component getName(ItemStack stack) {
			return Component.translatable(this.getDescriptionId());
		}
	}

	/**
	 * 饮用形态的 {@code consumable}：<b>先移除互斥同族、再施加本品</b>，顺序由列表保证。
	 *
	 * <p>两个效果都用原版实现：
	 * {@link RemoveStatusEffectsConsumeEffect}（正牌 {@code remove_effects}）与
	 * {@link ApplyStatusEffectsConsumeEffect}（正牌 {@code apply_effects}）。
	 * 因此整份组件可以安全地经 codec 序列化/反序列化，互斥不会丢。
	 */
	private static Consumable drinkConsumable(MobEffectInstance granted,
			List<Holder<MobEffect>> exclusiveSiblings) {
		Consumable.Builder builder = Consumables.defaultDrink();

		// ① 先移除同族（清空自身之外的那一味灌注）
		if (!exclusiveSiblings.isEmpty()) {
			// RemoveStatusEffectsConsumeEffect 的 HolderSet 直接列表要求非空，所以这里判空后再加。
			builder.onConsume(new RemoveStatusEffectsConsumeEffect(exclusiveSiblings.get(0)));
		}

		// ② 再施加本品效果
		builder.onConsume(new ApplyStatusEffectsConsumeEffect(granted, 1.0F));

		return builder.build();
	}
}
