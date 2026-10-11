package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

import java.util.List;

/**
 * 石灰酸橙（{@code bhr_lime}）—— 由旧项目 {@code BHR_FoodLime} 迁来
 * （旧注册名 {@code bhr_lime}，未变；类名按「食物不再用 Food 前缀」的约定改成 {@code BHR_Lime}）。
 *
 * <p>wiki 规格（{@code doc/wiki/Items/ClassBlockHeadsRebon/BHR_Lime.md}）：
 * <ul>
 *   <li>特点简介：一种食物物品。吃下后<b>有几率</b>获得急迫效果</li>
 *   <li>默认组件：{@code max_stack_size} 写 64 —— 那是 26.2 的默认值，代码里不必显式指定
 *       （只有非 64 才需要 {@code stacksTo(...)}）</li>
 *   <li>额外组件：{@code minecraft:food} + {@code minecraft:consumable}，数值见表（见下方常量）</li>
 *   <li>改写方法 / 事件 / 获取方式（配方与战利品表）：无</li>
 * </ul>
 *
 * <h2>1.20.5 之后食物为什么要写成「两个组件」</h2>
 * 旧写法（见 {@code src/main/javaref/.../BHR_FoodLime.java}）是把饥饿值、饱和度、附加效果
 * 全塞进一个 {@code FoodComponent}，再交给 {@code new Item(new FabricItemSettings().food(...))}。
 * 1.20.5 起食物被拆成<b>各管一段</b>的两个组件，物品本身只用普通构造器：
 * <ul>
 *   <li>{@code minecraft:food}（{@link FoodProperties}）—— <b>只管数值</b>：
 *       补多少饥饿值、多少饱和度、饿着能不能吃；</li>
 *   <li>{@code minecraft:consumable}（{@link Consumable}）—— <b>只管「怎么吃、吃完发生什么」</b>：
 *       吃的动画、吃多久、有没有粒子、吃完施加哪些 {@code ConsumeEffect}。</li>
 * </ul>
 * 也就是说，「吃下后有几率获得急迫」这件事从 {@code FoodComponent} 挪到了
 * {@link Consumable} 的效果列表里（{@link ApplyStatusEffectsConsumeEffect}）。
 * 两者互不引用，缺了 {@code consumable} 就只有饥饿值、右键没有进食动作 —— 这正是
 * {@link Item.Properties#food(FoodProperties, Consumable)} 这个重载存在的原因。
 *
 * <h2>范式契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_lime}　<b>模块</b> BHR　<b>父级</b> 无（普通 {@code Item}）</li>
 *   <li><b>启用的可选层</b>：层 1 规格常量（组件数值）、层 3 组件装配（物品的「状态」就是组件）。
 *       层号沿用范式 §2 的编号，与本项目其余物品类对照</li>
 *   <li><b>跳过的可选层</b>：层 4 属性工厂（{@code ModRegister.registerItem} 自己建
 *       {@code Item.Properties} 并写 id）、层 6 家族 / 层 7 codec（普通物品不需要）、
 *       层 8 生命周期、层 9 交互（吃的行为完全由组件驱动，不覆写任何方法）、层 10 形状与外观、
 *       层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code OnInit()}（物品区）　②☑ 手写 {@code models/item/bhr_lime.json}
 *       （2D 图标）+ datagen 自动生成 {@code items/bhr_lime.json}　③☑ 中英 {@code BHR_LangEntries}
 *       （物品只有 {@code item.…} 一条键）　④☑ {@code BHR_CreativeTabs}（食物组）
 *       　⑤☑ 贴图 {@code textures/item/bhr/lime.png}（既有资源）
 *       　⑥☐ 配方 / 战利品表 ——「获取方式」阶段统一做（wiki 写「无」）</li>
 * </ul>
 */
public class BHR_Lime extends Item {

	// ── 层 1 ｜ 规格常量（全部取自 wiki 的组件表）──

	/** 补充饥饿值（wiki 组件表 {@code minecraft:food.nutrition}）。 */
	public static final int NUTRITION = 3;

	/**
	 * 饱和度。
	 * <p>这里的值对应 wiki 组件表的 {@code minecraft:food.saturation}：旧写法里它是
	 * {@code FoodComponent.Builder.saturationModifier(f)} 的「饱和度倍率」，换算出的实际饱和度
	 * 是 {@code 饥饿值 × 倍率}（与 {@code Foods} 里原版食物的口径一致）。
	 */
	public static final float SATURATION = 1.0F;

	/**
	 * 无视饥饿值食用（wiki 组件表 {@code minecraft:food.can_always_eat} 写 TRUE）。
	 * <p>对应 {@code FoodProperties} 构造器的第三个参数，也就是旧写法的 {@code alwaysEdible()}。
	 */
	public static final boolean CAN_ALWAYS_EAT = true;

	/** 使用时间（秒）—— wiki 组件表 {@code minecraft:consumable.consume_seconds}。 */
	public static final float CONSUME_SECONDS = 1.6F;

	/** 食用后获得急迫的几率 —— wiki 组件表 {@code on_consume_effects.probability}。 */
	public static final float HASTE_PROBABILITY = 0.2F;

	/** 急迫效果的持续时间（tick）—— wiki 组件表 {@code on_consume_effects.effects.duration}，400 = 20 秒。 */
	public static final int HASTE_DURATION_TICKS = 400;

	/** 急迫效果的等级 —— wiki 组件表 {@code on_consume_effects.effects.amplifier}，1 = 游戏内显示的 II 级。 */
	public static final int HASTE_AMPLIFIER = 1;

	// ── 层 2 ｜ 物品声明 ──

	/** 石灰酸橙本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Item BHR_LIME;

	// ── 层 5 ｜ 构造器 ──

	/**
	 * 构造器（层 5）：只做装配。
	 * <p>食物数值与进食行为<b>不在这里</b>，而是由 {@link #limeFood()} / {@link #limeConsumable()}
	 * 以组件形式挂到 {@code Item.Properties} 上；构造器只拿那个已经装配好的属性对象。
	 */
	public BHR_Lime(Item.Properties properties) {
		super(properties);
	}

	// ── 层 3 ｜ 组件装配（物品的「状态」）──

	/**
	 * {@code minecraft:food} 组件（层 3）：只管数值 —— 补多少饥饿值、多少饱和度、饿着能不能吃。
	 * <p>构造顺序是 {@code (nutrition, saturation, canAlwaysEat)}，数值全部走层 1 的常量。
	 */
	public static FoodProperties limeFood() {
		return new FoodProperties(NUTRITION, SATURATION, CAN_ALWAYS_EAT);
	}

	/**
	 * {@code minecraft:consumable} 组件（层 3）：只管「怎么吃、吃完发生什么」。
	 *
	 * <p><b>为什么用 {@code Consumables.defaultFood()}</b> 起手：它就是原版「普通食物」的
	 * 默认配置（{@code animation = EAT}、{@code has_consume_particles = TRUE}、默认咀嚼音），
	 * 与 wiki 组件表里那三项写的一致；只有「用时」和「吃完的效果」需要再覆盖。
	 * 从它起手也保证了以后原版调整默认值时会跟着走，不必在这里手抄一份。
	 *
	 * <p><b>为什么不写 {@code stacksTo(64)}</b>：64 就是 26.2 的默认最大堆叠数
	 * （wiki 默认组件表那行标着「需要指定，否则无法堆叠」—— 那是旧版的顾虑，现在不成立）。
	 */
	public static Consumable limeConsumable() {
		return Consumables.defaultFood()
				.consumeSeconds(CONSUME_SECONDS)
				.onConsume(new ApplyStatusEffectsConsumeEffect(
						// 急迫、20 秒、II 级（amplifier 从 0 起算，所以 1 就是 II 级）
						List.of(new MobEffectInstance(MobEffects.HASTE, HASTE_DURATION_TICKS, HASTE_AMPLIFIER)),
						HASTE_PROBABILITY))
				.build();
	}

	// ── 层 12 ｜ 初始化 ──

	/**
	 * 注册（层 12）。
	 * <p>{@code ModRegister.registerItem} 负责「建 {@code Item.Properties} + 写 id + 进注册表」，
	 * 本类只管把组件挂上去：传进去的函数在<b>同一个</b> {@code Item.Properties} 上先挂
	 * {@link #limeFood()} 与 {@link #limeConsumable()}，注册器随后在它上面写 id —— 两者互不覆盖。
	 */
	public static void OnInit() {
		BHR_LIME = ModRegister.registerItem("bhr_lime",
				properties -> new BHR_Lime(properties
						.component(DataComponents.FOOD, limeFood())
						.component(DataComponents.CONSUMABLE, limeConsumable())));
	}
}
