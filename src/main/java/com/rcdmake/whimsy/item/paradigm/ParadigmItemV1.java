package com.rcdmake.whimsy.item.paradigm;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

/**
 * 范式物品 V1（{@code paradigm_item_v1}）—— 物品类「蓝图叠层」范式的样板，**不注册、不参与编译、不进 jar**。
 *
 * <p>本文件是 {@code doc/wiki/example/itemv1.md} 的落地样板：抄它 → 改类名/模块前缀/注册名/装配方法名 →
 * 按文档 §5 勾完 7 处登记点。{@code build.gradle} 的 {@code sourceSets} 排除了 {@code item/paradigm/**}
 * （只作样板、不进编译）；要校验它是否仍与当前 MC 版本 API 一致，临时注释掉那行 exclude 再 {@code gradlew build} 即可。
 *
 * <h2>层 0 ｜ 契约栏（类注释）</h2>
 * <ul>
 *   <li><b>中文名</b>：范式物品（样板）　<b>英文名</b>：Paradigm Item（template）</li>
 *   <li><b>注册名</b>：{@code paradigm_item_v1}　<b>模块</b>：—（范式样板，不属于任何模块）</li>
 *   <li><b>wiki 页</b>：{@code doc/wiki/example/itemv1.md}</li>
 *   <li><b>父级</b>：无（普通 {@code Item}；继承原版家族类时改写成那个类名）</li>
 *   <li><b>启用的可选层</b>：无（刻意只留强制层；可选层示例都在下面的注释里，用哪个抄哪个）</li>
 *   <li><b>停在第几级</b>：第 1 级（全部靠组件声明）。没停在第 0 级是因为「普通物品」本身就是父级
 *       —— 继承只有在需要原版类自带行为（放置 / 投掷 / 去皮…）时才有意义</li>
 *   <li><b>登记点 checklist（7 处，逐条勾）</b>：
 *       <ol>
 *         <li>☐/{@code X} 主类 {@code RichardsWhimsyMod.onInitialize()} 的「物品」区加一条 {@code OnInit()}</li>
 *         <li>☐/{@code X} {@code datagen/ItemModelGenerator}（普通物品**不用**登记；着色 / 瓶身 / 例外模型才动）</li>
 *         <li>☐/{@code X} 手写 {@code models/item/paradigm_item_v1.json}（2D 图标）</li>
 *         <li>☐/{@code X} {@code datagen/Class<模块>/<模块>_LangEntries} 一条 {@code item.…} 键</li>
 *         <li>☐/{@code X} {@code item/<模块>_CreativeTabs} 的包含清单（顺序 = wiki 物品栏页顺序）</li>
 *         <li>☐/{@code X} 资源：贴图 {@code textures/item/<模块>/<名>.png}；{@code items/<id>.json} 由 datagen 生成</li>
 *         <li>☐/{@code X} 配方 / 战利品表 / 标签（全项目暂缓）</li>
 *       </ol></li>
 * </ul>
 *
 * <h2>1.20.4 (Yarn) → 26.2 官方名对照（样板用到的部分）</h2>
 * <table border="1">
 *   <tr><th>Yarn（旧）/ 旧版</th><th>26.2 官方名</th><th>说明</th></tr>
 *   <tr><td>{@code new Item(new FabricItemSettings())}</td>
 *       <td>{@code ModRegister.registerItem(name, factory)}</td>
 *       <td>项目自建的注册入口：内部建 {@code new Item.Properties().setId(key)} 再交给工厂，最后 {@code Registry.register}</td></tr>
 *   <tr><td>{@code FoodComponent.Builder}</td>
 *       <td>{@code FoodProperties} + {@code Consumable}（两个组件）</td>
 *       <td>1.20.5 起食物拆成「数值」与「怎么吃」两份，物品本身用普通构造器</td></tr>
 *   <tr><td>{@code Item.Settings} / {@code FabricItemSettings.food(...)}</td>
 *       <td>{@code Item.Properties} / {@code component(DataComponents.FOOD, …)}</td>
 *       <td>设置类改名 + 食物不再走设置方法（{@code Properties.food(...)} 是便捷重载）</td></tr>
 *   <tr><td>{@code Registry.register(Registries.ITEM, new Identifier(ns, path), item)}</td>
 *       <td>{@code Registry.register(BuiltInRegistries.ITEM, key, item)}</td>
 *       <td>1.21.2 起用 {@code ResourceKey} 重载，且 key 必须先写进 {@code Properties}（漏写 NPE）</td></tr>
 *   <tr><td>{@code models/item/&lt;id&gt;.json} 一个文件搞定</td>
 *       <td>{@code items/&lt;id&gt;.json}（定义）+ {@code models/item/&lt;id&gt;.json}（模型）</td>
 *       <td>1.21.4 起两段式；缺前者 = MissingItemModel（紫黑）</td></tr>
 *   <tr><td>{@code ItemColorRegistry.register(...)}</td>
 *       <td>{@code items/&lt;id&gt;.json} 的 {@code tints}（{@code minecraft:constant}）</td>
 *       <td>着色改成数据驱动；{@code ItemTintSources} 注册各类型，客户端不需要写代码</td></tr>
 * </table>
 *
 * <h2>可选层示例（用哪个抄哪个，别留空壳）</h2>
 * <pre>{@code
 * // ── 层 6 家族/变体（白拿原版机制）──────────────────────────
 * public class X extends SplashPotionItem {                 // 投掷与范围施加全由原版负责
 *     X(Item.Properties properties) { super(properties); }
 * }
 * // 注意：26.2 已经没有 SwordItem / PickaxeItem，武器与工具走
 * //       properties.sword(ToolMaterial, F, F) / pickaxe(...) —— 不要去找这两个类
 *
 * // ── 层 8 生命周期 ───────────────────────────────────────
 * @Override public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) { ... }
 * @Override public void onCraftedBy(ItemStack stack, Player player) { ... }
 *
 * // ── 层 9 交互与判定 ─────────────────────────────────────
 * @Override public InteractionResult use(Level level, Player player, InteractionHand hand) { ... }
 * @Override public InteractionResult useOn(UseOnContext context) { ... }
 * @Override public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) { ... }
 * @Override public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) { ... }
 * @Override public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) { ... }
 *
 * // ── 层 10 外观与提示 ────────────────────────────────────
 * @Override public Component getName(ItemStack stack) { ... }   // 药水形态必须覆写，见 itemv1.md §7 第 6 条
 * @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context,
 *         TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) { ... }
 * @Override public boolean isFoil(ItemStack stack) { return true; }
 * }</pre>
 */
public class ParadigmItemV1 extends Item {

	// ─────────────────────────────────────────────────────────────────────
	// 层 1 ｜ 规格常量（可选）：只用静态常量，组件工厂与装配方法会读到
	// ─────────────────────────────────────────────────────────────────────

	/** 示例：补多少饥饿值（wiki 组件表 {@code minecraft:food.nutrition}）。 */
	private static final int NUTRITION = 4;

	/** 示例：饱和度倍率（wiki 组件表 {@code minecraft:food.saturation}）。 */
	private static final float SATURATION = 0.6F;

	/** 示例：食用时间（秒）—— 原版普通食物是 1.6，非 1.6 才需要显式覆盖。 */
	private static final float CONSUME_SECONDS = 1.6F;

	/** 示例：吃完给的效果时长（tick），100 = 5 秒。 */
	private static final int EFFECT_DURATION_TICKS = 100;

	/** 示例：效果等级（amplifier 从 0 起算，0 = 游戏内 I 级）。 */
	private static final int EFFECT_AMPLIFIER = 0;

	// ─────────────────────────────────────────────────────────────────────
	// 层 2 ｜ 物品声明：实例在层 12 赋值，之前为 null；一类多件改用 List
	// ─────────────────────────────────────────────────────────────────────

	/** 范式物品本体。 */
	public static Item PARADIGM_ITEM_V1;

	// ─────────────────────────────────────────────────────────────────────
	// 层 3 ｜ 组件与组件值工厂（可选）：物品的「状态」就是数据组件
	//        返回**已经 build 好的值对象**，不是 Builder
	// ─────────────────────────────────────────────────────────────────────

	/**
	 * {@code minecraft:food} 组件（层 3）：只管数值 —— 补多少饥饿值、多少饱和度、饿着能不能吃。
	 * <p>构造顺序是 {@code (nutrition, saturation, canAlwaysEat)}。
	 */
	public static FoodProperties paradigmItemV1Food() {
		return new FoodProperties(NUTRITION, SATURATION, false);
	}

	/**
	 * {@code minecraft:consumable} 组件（层 3）：只管「怎么吃、吃完发生什么」。
	 * <p>{@link Consumables#defaultFood()} 起手＝原版普通食物的配置（动画 / 粒子 / 咀嚼音），
	 * 只覆盖用时与吃完的效果 —— 原版以后调整默认值时我们跟着走。
	 */
	public static Consumable paradigmItemV1Consumable() {
		return Consumables.defaultFood()
				.consumeSeconds(CONSUME_SECONDS)
				.onConsume(new ApplyStatusEffectsConsumeEffect(
						new MobEffectInstance(MobEffects.REGENERATION, EFFECT_DURATION_TICKS, EFFECT_AMPLIFIER),
						1.0F))
				.build();
	}

	// ─────────────────────────────────────────────────────────────────────
	// 层 4 ｜ 属性装配：注册入口造好带 id 的 Properties，本方法只往里挂东西
	//        只允许依赖静态常量与层 3 的工厂，不得依赖实例字段
	// ─────────────────────────────────────────────────────────────────────

	/**
	 * 范式物品的属性装配（层 4）。
	 * <p>{@code setId} **不在这里写** —— 它由 {@link ModRegister#registerItem} 在调用本方法前写好，
	 * 这是物品与方块范式的分界点（方块自己造 {@code Properties}，物品改注册入口造好的那份）。
	 */
	public static Item.Properties paradigmItemV1Properties(Item.Properties properties) {
		return properties
				.component(DataComponents.FOOD, paradigmItemV1Food())
				.component(DataComponents.CONSUMABLE, paradigmItemV1Consumable());
		// 按需追加（每加一条都在注释里写清 wiki 的哪一行）：
		// .stacksTo(16)                              // 非 64 才写
		// .rarity(Rarity.UNCOMMON)
		// .usingConvertsTo(Items.GLASS_BOTTLE)       // 喝完返还玻璃瓶
		// .fireResistant()
	}

	// ─────────────────────────────────────────────────────────────────────
	// 层 5 ｜ 构造器（运行时后执行）：只做装配，数值全在组件里
	// ─────────────────────────────────────────────────────────────────────

	public ParadigmItemV1(Item.Properties properties) {
		super(properties);
	}

	// ─────────────────────────────────────────────────────────────────────
	// 层 6 ｜ 家族/变体声明（可选）：用 extends 原版物品类表达
	//        —— 见类注释「层 6」。家族关系写在类声明与构造器里，不在这里新开方法
	// ─────────────────────────────────────────────────────────────────────

	// ─────────────────────────────────────────────────────────────────────
	// 层 7 ｜ 类型序列化 codec：物品**没有**这一层
	//        Item 只有静态 CODEC / STREAM_CODEC 字段，没有 codec() 可覆写
	//        —— 照方块范式抄一层 @Override codec() 编译不过，见 itemv1.md §7 第 3 条
	// ─────────────────────────────────────────────────────────────────────

	// ─────────────────────────────────────────────────────────────────────
	// 层 8 ｜ 生命周期（可选）：inventoryTick / onCraftedBy /
	//        onCraftedPostProcess / onDestroyed
	//        —— 示例见类注释「层 8」
	// ─────────────────────────────────────────────────────────────────────

	// ─────────────────────────────────────────────────────────────────────
	// 层 9 ｜ 交互与判定（可选）：use / useOn / finishUsingItem / onUseTick /
	//        releaseUsing / interactLivingEntity / hurtEnemy / mineBlock …
	//        —— 示例见类注释「层 9」；吃了发生什么优先写进层 3 的组件
	// ─────────────────────────────────────────────────────────────────────

	// ─────────────────────────────────────────────────────────────────────
	// 层 10 ｜ 外观与提示（可选）：getName / appendHoverText / isFoil /
	//          getUseAnimation / getUseDuration
	//          —— 示例见类注释「层 10」；模型与 tints 属于资源，不在本层
	// ─────────────────────────────────────────────────────────────────────

	// ─────────────────────────────────────────────────────────────────────
	// 层 11 ｜ 未定扩展点（注册前预留区，可选）：现在无法预测归属的钩子占位
	//         // TODO(预留)：……
	//         —— 一旦确定归属，就搬到对应层，别让它烂在这里
	// ─────────────────────────────────────────────────────────────────────

	// ─────────────────────────────────────────────────────────────────────
	// 层 12 ｜ 初始化：注册物品 + 额外注册表登记（脚注写在这里）
	// ─────────────────────────────────────────────────────────────────────

	public static void OnInit() {
		PARADIGM_ITEM_V1 = ModRegister.registerItem("paradigm_item_v1",
				properties -> new ParadigmItemV1(paradigmItemV1Properties(properties)));
	}

	// ─────────────────────────────────────────────────────────────────────
	// 层 C ｜ 客户端接线（可选，独立类 RichardsWhimsyModClient）：
	//        自定义渲染 → 客户端入口；着色 → items/<id>.json 的 tints（数据，不写代码）
	//        绝不写进本类
	// ─────────────────────────────────────────────────────────────────────

	// ─────────────────────────────────────────────────────────────────────
	// 层 M ｜ 定制 Mixin（可选，独立类 + 三处登记）：仅当阶梯前四级全部无解
	// ─────────────────────────────────────────────────────────────────────
}
