package com.rcdmake.whimsy;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 注册核心工具：把「26.2 的注册样板」收在一处，各分支类只调用这里的方法。
 *
 * <p>它替代了 1.20.4 时代每个类里各自复制一遍的
 * {@code Registry.register(Registries.X, new Identifier(...), obj)} 写法。
 *
 * <p><b>26.2 的注册规则（已对 26.2 的 minecraft-common.jar 逐条核验）</b>：
 * <ul>
 *   <li>资源 id 用 {@link Identifier}，工厂方法 {@code fromNamespaceAndPath(ns, path)}；
 *       {@code new ResourceLocation(...)} 已不存在，1.21 时代的 {@code Identifier.of(...)} 也不在。</li>
 *   <li>物品的 key <b>必须</b>写进 {@link Item.Properties#setId}，
 *       漏写会在运行时抛 {@code NullPointerException: Item id not set}；方块同理走
 *       {@link BlockBehaviour.Properties#setId}。</li>
 *   <li>设置类名：物品是 {@code Item.Properties}（Yarn 叫 Item.Settings），
 *       方块是 {@code BlockBehaviour.Properties}（Yarn 叫 AbstractBlock.Settings）；
 *       {@code FabricItemSettings} / {@code FabricBlockSettings} 都已废弃。</li>
 *   <li>「复制某方块的属性」在 26.2 是 {@code BlockBehaviour.Properties.ofFullCopy(block)}
 *       （对应旧 Yarn 的 {@code copyOf}）。</li>
 * </ul>
 *
 * <p><b>为什么注册发生在 OnInit() 而不是静态字段里</b>：
 * 本模组的约定是每个类提供一条 {@code OnInit()}，由主类调用。<br>
 * <ul>
 *   <li>{@code Item} 是惰性的——在构造时就调用 {@code setId} 会立刻触发
 *       {@code BuiltInRegistries.ITEM} 的 {@code bootstrap()}，等价于在 {@code onInitialize()}
 *       之前就把注册表初始化掉，属于「越早自举」，容易踩类初始化顺序的坑。</li>
 *   <li>{@code Block} 不同，它自带一个 public 的 {@code id()}，会把 key 存起来，
 *       所以方块可以在静态字段里安全地构造（原版石头、草方块就是这么写的）。</li>
 * </ul>
 * 因此本类里「方块 = 静态字段直接给 Properties.setId」，
 * 「物品 / 方块物品 = 在 {@code OnInit()} 里 setId 并注册」，两条路都安全。
 */
public final class ModRegister {

	/** 可选的「模块 → 物品」收集表（见 {@link #registerItemFor}）。 */
	private static final Map<String, List<Item>> MODULE_ITEMS = new HashMap<>();
	/** 可选的「模块 → 方块」收集表。 */
	private static final Map<String, List<Block>> MODULE_BLOCKS = new HashMap<>();

	private ModRegister() {
	}

	/** 构造本模组的资源 id。 */
	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(RichardsWhimsyMod.MOD_ID, path);
	}

	/** 构造本模组某个物品的 {@link ResourceKey}。 */
	public static ResourceKey<Item> itemKey(String name) {
		return ResourceKey.create(BuiltInRegistries.ITEM.key(), id(name));
	}

	/** 构造本模组某个方块的 {@link ResourceKey}。 */
	public static ResourceKey<Block> blockKey(String name) {
		return ResourceKey.create(BuiltInRegistries.BLOCK.key(), id(name));
	}

	/**
	 * 登记一个物品供某个模块的物品栏取用（一般在方块/物品类自己的 {@code OnInit()} 里调）。
	 *
	 * <p><b>这是可选机制</b>：物品栏里直接写变量名更直观（推荐），
	 * 这个收集表是给「不想维护清单」的场景准备的 —— 注册即入栏，不会漏。
	 * 代价是顺序由注册顺序决定，不像显式列表那样可控。
	 *
	 * @param module 模块代号，如 {@code "dev"}
	 */
	public static <T extends Item> T registerItemFor(String module, String name,
			Function<Item.Properties, T> factory) {
		T item = registerItem(name, factory);
		MODULE_ITEMS.computeIfAbsent(module, k -> new ArrayList<>()).add(item);
		return item;
	}

	/** 已登记的「模块 → 物品」表，供各模块的物品栏按模块取用。 */
	public static List<Item> itemsOf(String module) {
		return List.copyOf(MODULE_ITEMS.getOrDefault(module, List.of()));
	}

	/** 已登记的「模块 → 方块」表。 */
	public static List<Block> blocksOf(String module) {
		return List.copyOf(MODULE_BLOCKS.getOrDefault(module, List.of()));
	}

	/**
	 * 注册一个物品（工厂形式，便于以后接自定义 {@code Item} 子类）。
	 * <p>这里负责调用 {@code setId}，所以调用方传进来的 {@code Properties} 不需要自己设。
	 */
	public static <T extends Item> T registerItem(String name, Function<Item.Properties, T> factory) {
		ResourceKey<Item> key = itemKey(name);
		T item = factory.apply(new Item.Properties().setId(key));
		Registry.register(BuiltInRegistries.ITEM, key, item);
		return item;
	}

	/**
	 * 注册一个已经带上 {@code setId} 的物品实例（用于方块物品、或构造好 Properties 的自定义物品）。
	 */
	public static <T extends Item> T register(String name, T item) {
		Registry.register(BuiltInRegistries.ITEM, itemKey(name), item);
		return item;
	}

	/**
	 * 只注册方块本身，不注册方块物品（用于细雪、发光红石矿这类没有物品形式的方块）。
	 */
	public static Block registerBlock(String name, Block block) {
		Registry.register(BuiltInRegistries.BLOCK, blockKey(name), block);
		return block;
	}

	/**
	 * 注册方块，并同时注册它的方块物品——这样它才会出现在物品栏里。
	 * <p>按项目约定，方块物品的注册放在方块自己的类里，不单独建物品类。
	 *
	 * @param block 已经在静态字段里用 {@code Properties.setId(ModRegister.blockKey(name))} 构造好的方块；
	 *              方块物品会使用<b>同一个</b> name 作为路径（两者分属 BLOCK / ITEM 两个注册表，不冲突）。
	 */
	public static Block registerBlockWithItem(String name, Block block) {
		Registry.register(BuiltInRegistries.BLOCK, blockKey(name), block);

		// 方块物品是惰性的，所以它的 key 在这里才写进 Properties。
		ResourceKey<Item> itemKey = itemKey(name);
		Item blockItem = new BlockItem(block, new Item.Properties().setId(itemKey));
		Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
		return block;
	}

	/**
	 * 注册一个状态效果（26.2 的 {@link MobEffect}，1.20.4 时代叫 {@code StatusEffect}）。
	 *
	 * <p><b>为什么返回 {@link Holder} 而不是 {@code MobEffect} 本身</b>：
	 * 1.21 起原版收效果一律用 {@code Holder<MobEffect>}——
	 * {@code LivingEntity#hasEffect(Holder)}、{@code MobEffectInstance(Holder, int, int)}、
	 * {@code MobEffects.POISON} 本身都是 Holder。原版自己的注册方法
	 * （{@code MobEffects.register}）也走 {@code Registry.registerForHolder}，
	 * 这里跟着走同一条路：注册与「取值用的句柄」一次到位，
	 * 调用方不需要再自己 {@code wrapAsHolder}。
	 *
	 * @param name 注册路径（省略命名空间 {@code richards-whimsy-mod:}）
	 * @return 注册后的效果句柄，直接存进效果类的静态字段备用
	 */
	public static Holder<MobEffect> registerStatusEffect(String name, MobEffect effect) {
		return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, id(name), effect);
	}
}
