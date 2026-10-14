package com.rcdmake.whimsy.item.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;

/**
 * 渡渡苹果（{@code bhr_dodo_apple}）—— 由旧项目的 {@code BHR_FoodMedium} 档迁来
 * （旧 {@code BHR_FoodOther} 是 {@code #deprecated} 集合类，本次按「一物一类」约定拆开，
 * 见 {@code doc/agent/humanidea/开发约定.md} 第 1 条）。
 *
 * <p>wiki 规格（{@code doc/wiki/Items/ClassBlockHeadsRebon/BHR_DodoApple.md}）：
 * <ul>
 *   <li>特点简介：一种食物物品；<b>在配方上等价于原版苹果</b>，可代替原版苹果进行合成；
 *       也是本模块生物「渡渡鸟（{@code #todo}）」的食物；可被用于种植苹果树</li>
 *   <li>组件：默认组件 + {@code minecraft:food}（nutrition 4 / saturation 1 / can_always_eat FALSE）
 *       + {@code minecraft:consumable}（{@code consume_seconds} 1.6，其余走原版普通食物默认）</li>
 *   <li>改写方法 / 事件：无</li>
 *   <li>获取方式：战利品表「待迁移后统一设定」——本类不写掉落</li>
 * </ul>
 *
 * <h2>本次实现范围（按用户 2026-10 指示）</h2>
 * wiki 里两条 {@code #todo} <b>本次跳过</b>，因为对应方块（苹果树树叶 / 渡渡鸟实体）尚未添加：
 * <ul>
 *   <li>「在合适的土壤上右键可用于放置苹果树树叶」——等树叶方块落地后照
 *       {@link BHR_PineCone}（松果）的 {@code useOn} 范式补；</li>
 *   <li>「可被喂养渡渡鸟」——等渡渡鸟实体落地后按实体交互补。</li>
 * </ul>
 *
 * <h2>「配方上等价于原版苹果」的落地方式（调研结论，本次只改 wiki 不写配方）</h2>
 * Forge 靠矿物词典（Ore Dictionary / {@code Ingredient} 标签）实现合成等价；Fabric 的对应物是
 * <b>物品标签</b>（{@code data/<ns>/tags/item/*.json}）。但 26.2 原版用到苹果的配方只有一条
 * {@code data/minecraft/recipe/golden_apple.json}，它把 {@code "X"} 写死成 {@code "minecraft:apple"}
 * <b>物品 id 而不是标签</b>，因此「建一个 apples 标签把渡渡苹果塞进去」<b>改不动原版那条配方</b>；
 * Fabric 也没有「替换/注入原版配方」的公开 API（{@code fabric-recipe-api-v1} 只提供
 * {@code FabricRecipeManager#getAllMatches} / {@code getAllOfType} 这类<b>只读</b>查询）。
 * 可行方案见 wiki 备注（A：照抄金苹果配方换用渡渡苹果；B：只用标签；A+B）。
 *
 * <h2>范式契约栏（层 0）</h2>
 * <ul>
 *   <li><b>注册名</b> {@code bhr_dodo_apple}　<b>模块</b> BHR　<b>父级</b> 无（普通 {@code Item}）</li>
 *   <li><b>启用的可选层</b>：层 1 规格常量、层 3 组件装配（食物 = food + consumable 两个组件）</li>
 *   <li><b>跳过的可选层</b>：层 4 属性工厂（{@code ModRegister.registerItem} 自己建
 *       {@code Item.Properties} 并写 id）、层 6 家族 / 层 7 codec（普通物品不需要）、
 *       层 8 生命周期、层 9 交互（吃的行为完全由组件驱动；两条 {@code #todo} 见上）、
 *       层 10 形状与外观、层 11 预留区、层 C 客户端、层 M Mixin</li>
 *   <li><b>登记点</b>：①☑ 主类 {@code onInitialize()}（物品区）　②☑ 手写
 *       {@code models/item/bhr_dodo_apple.json}（2D 图标，贴图复用 {@code bhr/apple.png}）
 *       + datagen 自动生成 {@code items/bhr_dodo_apple.json}　③☑ 中英 {@code BHR_LangEntries}
 *       　④☑ {@code BHR_CreativeTabs}（食物组）　⑤☑ 贴图 {@code textures/item/bhr/apple.png}
 *       （用户确认：{@code apple.png} 就是渡渡苹果的贴图；旧清单里的 {@code bhr_apple}「红富士」
 *       从未注册过，文档也已删除）　⑥☐ 配方 / 战利品表 —— 见上「获取方式」与等价方案</li>
 * </ul>
 */
public class BHR_DodoApple extends Item {

	// ── 层 1 ｜ 规格常量（全部取自 wiki 的组件表）──

	/** 补充饥饿值（wiki 组件表 {@code minecraft:food.nutrition}）。 */
	public static final int NUTRITION = 4;

	/**
	 * 饱和度（wiki 组件表 {@code minecraft:food.saturation}）。
	 * <p>口径与 wiki 的注释一致：这是<b>饱和度倍率</b>，实际饱和度 = 饥饿值 × 该值。
	 * 26.2 的 {@code FoodProperties} 构造器就是<b>直接收倍率</b>
	 * （{@code saturationModifier} 字段，{@code Foods} 里原版食物全部按倍率填），
	 * 所以这里照 wiki 的数原样传，不做换算 —— 与已落地的 {@link BHR_Lime} 完全同一口径。
	 */
	public static final float SATURATION = 1.0F;

	/**
	 * 无视饥饿值食用（wiki 组件表 {@code minecraft:food.can_always_eat} 写 FALSE）。
	 * <p>对应 {@code FoodProperties} 构造器的第三个参数，也就是旧写法的 {@code alwaysEdible()}。
	 */
	public static final boolean CAN_ALWAYS_EAT = false;

	/** 使用时间（秒）—— wiki 组件表 {@code minecraft:consumable.consume_seconds}。 */
	public static final float CONSUME_SECONDS = 1.6F;

	// ── 层 2 ｜ 物品声明 ──

	/** 渡渡苹果本体（层 2），在 {@link #OnInit()} 里赋值。 */
	public static Item BHR_DODO_APPLE;

	// ── 层 5 ｜ 构造器 ──

	/**
	 * 构造器（层 5）：只做装配。
	 * <p>食物数值与进食行为<b>不在这里</b>，而是由 {@link #dodoAppleFood()} /
	 * {@link #dodoAppleConsumable()} 以组件形式挂到 {@code Item.Properties} 上。
	 */
	public BHR_DodoApple(Item.Properties properties) {
		super(properties);
	}

	// ── 层 3 ｜ 组件装配 ──

	/**
	 * {@code minecraft:food} 组件（层 3）：只管数值。
	 * <p>构造顺序 {@code (nutrition, saturation, canAlwaysEat)}。
	 */
	public static FoodProperties dodoAppleFood() {
		return new FoodProperties(NUTRITION, SATURATION, CAN_ALWAYS_EAT);
	}

	/**
	 * {@code minecraft:consumable} 组件（层 3）：只管「怎么吃」。
	 * <p>从 {@code Consumables.defaultFood()} 起手 = 原版「普通食物」默认配置
	 * （{@code animation = "eat"}、{@code has_consume_particles = TRUE}、默认咀嚼音），
	 * wiki 只要求覆盖 {@code consume_seconds}（1.6 —— 恰好与原版默认同值，
	 * 但仍按 wiki 显式写出来，和 {@link BHR_Lime} 保持一致）。
	 * <p>本物品<b>没有</b> {@code on_consume_effects}（wiki 组件表里那一节不存在）。
	 */
	public static Consumable dodoAppleConsumable() {
		return Consumables.defaultFood()
				.consumeSeconds(CONSUME_SECONDS)
				.build();
	}

	// ── 层 12 ｜ 初始化 ──

	/** 注册（层 12）：{@code registerItem} 负责建 {@code Item.Properties}、写 id 并进注册表。 */
	public static void OnInit() {
		BHR_DODO_APPLE = ModRegister.registerItem("bhr_dodo_apple",
				properties -> new BHR_DodoApple(properties
						.component(DataComponents.FOOD, dodoAppleFood())
						.component(DataComponents.CONSUMABLE, dodoAppleConsumable())));
	}
}
