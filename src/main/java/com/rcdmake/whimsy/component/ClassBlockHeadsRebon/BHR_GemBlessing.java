package com.rcdmake.whimsy.component.ClassBlockHeadsRebon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rcdmake.whimsy.ModRegister;
import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;
import java.util.function.Consumer;

/**
 * 宝石祝福（物品组件 {@code richards-whimsy-mod:gem_blessing}）。
 *
 * <p>wiki 规格：{@code doc/wiki/Component/ClassBlockHeadsRebon/BHR_GemBlessing.md}。
 * 组件是 5 个互相独立的布尔值，分别代表 5 种宝石祝福；每种祝福把指定的几个附魔
 * <b>「视为」等级 +1 或 +2</b>（紫锂辉 / 蓝宝石 / 祖母绿 +1，红刚玉 / 宝石钻石 +2）。
 *
 * <h2>为什么是「视为」而不是「真的附魔上去」</h2>
 * 组件本身<b>不写进 {@code ENCHANTMENTS}</b>，等级提升由
 * {@code BHR_GemBlessingEnchantmentMixin} 在读取附魔时现场加上（见
 * {@code component/ClassBlockHeadsRebon/BHR_GemBlessingBonus}）。这样：
 * <ul>
 *   <li>没附魔的工具加了祝福也<b>不会出现附魔光效</b>——{@code ItemStack#isFoil()}
 *       走 {@code isEnchanted()}，那里看的是真的附魔组件（wiki 备注第 49 行）；</li>
 *   <li>铁砧 / 砂轮 / 附魔台看到的仍然是真实附魔，不会把祝福当成可打磨、可计价的词条；</li>
 *   <li>原版附魔行显示原始等级，祝福单独占一行（本轮确认的口径）。</li>
 * </ul>
 *
 * <h2>字段名的历史包袱</h2>
 * 序列化字段名固定为 {@code Amethyst / Sapphire / Emerald / Ruby / Diamond}（首字母大写），
 * 这是 wiki「构造」写死的组件 JSON 字段名，且 {@code Amethyst} 一词是致敬《The Blockheads》，
 * <b>只有显示名改成了「紫锂辉」</b>（见 {@code doc/agent/deepseek/06-注册名对照表.md} 第 55 行）。
 * Java 侧字段仍然是常规的小驼峰，两者通过 {@code optionalFieldOf("Amethyst", …)} 对接，
 * 因此不需要为了序列化写大写字段名。
 *
 * <h2>26.2 的注册口径（已对照 26.2 源码核验）</h2>
 * <ul>
 *   <li>{@link Registry#register(Registry, net.minecraft.resources.Identifier, Object)}
 *       在 26.2 的签名是 {@code <V, T extends V> T register(Registry<V>, Identifier, T)}，
 *       <b>不需要</b> 1.21 时代那种 {@code (DataComponentType<?>) (DataComponentType<T>) } 双重强转。</li>
 *   <li>组件注册表是 {@code BuiltInRegistries.DATA_COMPONENT_TYPE}（{@code BuiltInRegistries.java:296}）。</li>
 *   <li>tooltip 走 Fabric 的 {@link ItemComponentTooltipProviderRegistry#addAfter}
 *       把本组件挂到 {@code DataComponents.ENCHANTMENTS} 之后——组件因此要实现
 *       {@link TooltipProvider}，由 {@code ItemStack#addToTooltip} 调用，
 *       并自动遵守 {@code TooltipDisplay#shows}（{@code F3+H} 之外的隐藏逻辑）。</li>
 * </ul>
 */
public final class BHR_GemBlessing {

	/** 组件的注册路径（命名空间是 {@code richards-whimsy-mod}）。 */
	public static final String PATH = "gem_blessing";

	/** 祝福提示行的语言键前缀，完整键是前缀 + {@link Gem#langPath()}。 */
	public static final String TOOLTIP_KEY_PREFIX = "tooltip." + "richards-whimsy-mod.gem_blessing.";

	/**
	 * 组件类型本体。在 {@link #OnInit()} 里赋值 —— 与项目「注册一律放 {@code OnInit()}`」的约定一致
	 * （{@code ModRegister} 类注释解释了为什么不在静态字段里就地注册）。
	 */
	private static DataComponentType<Blessing> component;

	private BHR_GemBlessing() {
	}

	/** 组件类型；未初始化时返回 {@code null}（调用方需要自己兜底，mixin 里尤其要判空）。 */
	public static DataComponentType<Blessing> component() {
		return component;
	}

	/**
	 * 读取物品上的宝石祝福。
	 *
	 * @return 没有组件（或组件尚未注册）时返回 {@code null} —— 调用方统一按「无祝福」处理
	 */
	public static Blessing get(ItemInstance stack) {
		return component == null ? null : stack.get(component);
	}

	/** 写入宝石祝福；传 {@code null} 或全 {@code false} 等价于移除组件（不留空壳）。 */
	public static void set(ItemStack stack, Blessing blessing) {
		if (component == null) {
			return;
		}
		if (blessing == null || blessing.isEmpty()) {
			stack.remove(component);
		} else {
			stack.set(component, blessing);
		}
	}

	/** 移除物品上的宝石祝福。 */
	public static void clear(ItemStack stack) {
		set(stack, null);
	}

	/** 注册组件，并把它挂进 tooltip 顺序（在附魔行之后）。由主类 {@code onInitialize()} 调用。 */
	public static void OnInit() {
		if (component != null) {
			return;
		}

		component = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ModRegister.id(PATH),
				DataComponentType.<Blessing>builder()
						.persistent(Blessing.CODEC)
						.networkSynchronized(Blessing.STREAM_CODEC)
						.build());

		// 挂在原版附魔行之后：玩家读到的顺序是「附魔…→ 宝石祝福…」，与 wiki 的展示意图一致。
		ItemComponentTooltipProviderRegistry.addAfter(DataComponents.ENCHANTMENTS, component);
	}

	/**
	 * 五种宝石祝福。
	 *
	 * <p>{@code langPath} 同时是提示行的语言键后缀与测试物品的播报名；
	 * {@code enchantedKeys} 就是 wiki「构造」里点名的那几个附魔（按附魔注册键比对，
	 * 跨物品类型通用 —— 一条附魔只属于一种祝福，所以不会互相叠加）。
	 */
	public enum Gem {

		/** 紫锂辉：效率 / 横扫之刃 / 荆棘 +1。 */
		AMETHYST("amethyst", 1, ChatFormatting.LIGHT_PURPLE, List.of(
				Enchantments.EFFICIENCY, Enchantments.SWEEPING_EDGE, Enchantments.THORNS)),

		/** 蓝宝石：耐久 / 穿刺 / 水下呼吸 +1。 */
		SAPPHIRE("sapphire", 1, ChatFormatting.BLUE, List.of(
				Enchantments.UNBREAKING, Enchantments.IMPALING, Enchantments.RESPIRATION)),

		/** 祖母绿：击退（冲击）/ 快速装填 +1。 */
		EMERALD("emerald", 1, ChatFormatting.GREEN, List.of(
				Enchantments.KNOCKBACK, Enchantments.PUNCH, Enchantments.QUICK_CHARGE)),

		/** 红刚玉：时运（抢夺）/ 风爆 +2。 */
		RUBY("ruby", 2, ChatFormatting.RED, List.of(
				Enchantments.FORTUNE, Enchantments.LOOTING, Enchantments.WIND_BURST)),

		/** 宝石钻石：锋利（力量）/ 保护 +2。 */
		DIAMOND("diamond", 2, ChatFormatting.AQUA, List.of(
				Enchantments.SHARPNESS, Enchantments.POWER, Enchantments.PROTECTION));

		/** 缓存 {@code values()}，供热路径（每次附魔读取）遍历，避免反复克隆数组。 */
		public static final Gem[] ALL = values();

		private final String langPath;
		private final int bonus;
		private final ChatFormatting color;
		private final List<ResourceKey<Enchantment>> enchantedKeys;

		Gem(String langPath, int bonus, ChatFormatting color, List<ResourceKey<Enchantment>> enchantedKeys) {
			this.langPath = langPath;
			this.bonus = bonus;
			this.color = color;
			this.enchantedKeys = enchantedKeys;
		}

		/** 语言键后缀，如 {@code amethyst}。 */
		public String langPath() {
			return this.langPath;
		}

		/** 完整提示行语言键，如 {@code tooltip.richards-whimsy-mod.gem_blessing.amethyst}。 */
		public String tooltipKey() {
			return TOOLTIP_KEY_PREFIX + this.langPath;
		}

		/** 本祝福给出的等级增量（+1 或 +2）。 */
		public int bonus() {
			return this.bonus;
		}

		/** 提示行的颜色（沿用 1.20.4 时代五档数字的配色）。 */
		public ChatFormatting color() {
			return this.color;
		}

		/** 本祝福影响的附魔（附魔注册键）。 */
		public List<ResourceKey<Enchantment>> enchantments() {
			return this.enchantedKeys;
		}
	}

	/**
	 * 组件载荷：5 个布尔值，顺序与 wiki「构造」一致。
	 *
	 * <p>它自己实现 {@link TooltipProvider}，所以提示行不需要客户端事件、
	 * 也不需要 mixin 进 tooltip 流程 —— 注册时挂到附魔行之后即可（见 {@link #OnInit()}）。
	 * 一块物品上有几种祝福就输出几行，互不合并。
	 */
	public record Blessing(boolean amethyst, boolean sapphire, boolean emerald, boolean ruby, boolean diamond)
			implements TooltipProvider {

		/** 一条祝福都没有（不存在组件时的等价物）。 */
		public static final Blessing NONE = new Blessing(false, false, false, false, false);

		/**
		 * 持久化 Codec：字段名按 wiki 固定为 {@code Amethyst / Sapphire / Emerald / Ruby / Diamond}，
		 * 缺省值 {@code false}（手写 NBT / 指令只写一条也行）。
		 */
		public static final Codec<Blessing> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("Amethyst", false).forGetter(Blessing::amethyst),
				Codec.BOOL.optionalFieldOf("Sapphire", false).forGetter(Blessing::sapphire),
				Codec.BOOL.optionalFieldOf("Emerald", false).forGetter(Blessing::emerald),
				Codec.BOOL.optionalFieldOf("Ruby", false).forGetter(Blessing::ruby),
				Codec.BOOL.optionalFieldOf("Diamond", false).forGetter(Blessing::diamond)
		).apply(instance, Blessing::new));

		/** 网络同步：5 个 bool 顺序拼包，与 {@link #CODEC} 的字段顺序一致。 */
		public static final StreamCodec<RegistryFriendlyByteBuf, Blessing> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.BOOL, Blessing::amethyst,
				ByteBufCodecs.BOOL, Blessing::sapphire,
				ByteBufCodecs.BOOL, Blessing::emerald,
				ByteBufCodecs.BOOL, Blessing::ruby,
				ByteBufCodecs.BOOL, Blessing::diamond,
				Blessing::new);

		/** 某一种祝福是否生效。 */
		public boolean has(Gem gem) {
			return switch (gem) {
				case AMETHYST -> this.amethyst;
				case SAPPHIRE -> this.sapphire;
				case EMERALD -> this.emerald;
				case RUBY -> this.ruby;
				case DIAMOND -> this.diamond;
			};
		}

		/** 加上某一种祝福（已有则原样返回）。 */
		public Blessing with(Gem gem) {
			return switch (gem) {
				case AMETHYST -> new Blessing(true, this.sapphire, this.emerald, this.ruby, this.diamond);
				case SAPPHIRE -> new Blessing(this.amethyst, true, this.emerald, this.ruby, this.diamond);
				case EMERALD -> new Blessing(this.amethyst, this.sapphire, true, this.ruby, this.diamond);
				case RUBY -> new Blessing(this.amethyst, this.sapphire, this.emerald, true, this.diamond);
				case DIAMOND -> new Blessing(this.amethyst, this.sapphire, this.emerald, this.ruby, true);
			};
		}

		/** 去掉某一种祝福。 */
		public Blessing without(Gem gem) {
			return switch (gem) {
				case AMETHYST -> new Blessing(false, this.sapphire, this.emerald, this.ruby, this.diamond);
				case SAPPHIRE -> new Blessing(this.amethyst, false, this.emerald, this.ruby, this.diamond);
				case EMERALD -> new Blessing(this.amethyst, this.sapphire, false, this.ruby, this.diamond);
				case RUBY -> new Blessing(this.amethyst, this.sapphire, this.emerald, false, this.diamond);
				case DIAMOND -> new Blessing(this.amethyst, this.sapphire, this.emerald, this.ruby, false);
			};
		}

		/** 一条祝福都没有时为 {@code true}（这种载荷不该被写进物品）。 */
		public boolean isEmpty() {
			return !this.amethyst && !this.sapphire && !this.emerald && !this.ruby && !this.diamond;
		}

		/**
		 * 提示行：有几种祝福就出几行，颜色沿用旧版五档配色。
		 *
		 * <p>{@code components} 是本物品的组件读取器；本组件不需要从里面再拿别的东西，
		 * 但保留这个参数是 {@link TooltipProvider} 的契约。
		 */
		@Override
		public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag,
				DataComponentGetter components) {
			for (Gem gem : Gem.ALL) {
				if (this.has(gem)) {
					consumer.accept(Component.translatable(gem.tooltipKey()).withStyle(gem.color()));
				}
			}
		}
	}
}
