package com.rcdmake.whimsy.component.ClassBlockHeadsRebon;

import com.mojang.serialization.Codec;
import com.rcdmake.whimsy.ModRegister;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

/**
 * 制作台等级（物品组件 {@code richards-whimsy-mod:workbench_level}）。
 *
 * <p>它把「这台制作台是第几级」写进<b>物品</b>，一件组件同时满足四个需求：
 * <ol>
 *   <li><b>挖取保等级</b>：挖下来的物品带着当时的等级（{@code BHR_WorkBench#getDrops}）；</li>
 *   <li><b>放置还原</b>：放下时按组件值重建方块状态（{@code BHR_WorkBench#getStateForPlacement}）；</li>
 *   <li><b>同级堆叠、异级不堆叠</b>：不同组件值 = 不同物品堆，
 *       原版堆叠规则白送，本模组不写任何合并逻辑；</li>
 *   <li><b>图标跟随等级</b>：{@code items/bhr_workbench.json} 用
 *       {@code minecraft:select} + {@code property: minecraft:component} 按本组件的值挑模型 ——
 *       因为载荷是整数、组件的持久化 codec 就是 {@code Codec.INT}，
 *       所以 JSON 里能直接写 {@code "when": 9}（见 {@link #CODEC}）。</li>
 * </ol>
 *
 * <h2>为什么载荷是 record 而 JSON 里却是一个整数</h2>
 * <p>{@link Level} 这个 record 存在的理由只有一个：<b>要能实现 {@link TooltipProvider}</b>
 * —— 描述栏那一行是「组件自带的 tooltip」，而 {@code Integer} 这种原版类型不可能再实现接口。
 * 载荷的<b>持久化 / 网络形态仍然是一个裸整数</b>：{@link #CODEC} 是 {@code Codec.INT.xmap(...)}，
 * {@link #STREAM_CODEC} 是 {@code VAR_INT.map(...)}。所以：
 * <ul>
 *   <li>物品模型 JSON 的 case 值写 {@code "when": 9}，不是 {@code "when": {"level": 9}}；</li>
 *   <li>指令里写 {@code [richards-whimsy-mod:workbench_level=9]}，与裸整数组件一致。</li>
 * </ul>
 *
 * <h2>第 1 级是怎么做到「与默认值不重复」的</h2>
 * <p>方块物品的属性里写了<b>原型组件</b>
 * （{@code Item.Properties#component(..., new Level(1))}），所以：
 * <ul>
 *   <li>任何新造出来的制作台物品都读得到「1 级」→ 图标与描述栏对 1 级也生效；</li>
 *   <li>{@link #set} 在等级为 1 时<b>不碰补丁</b> → 补丁为空，
 *       于是「新合成的 1 级」与「挖回来的 1 级」是同一个物品堆（堆叠不会分裂）。</li>
 * </ul>
 * 换句话说：1 级永远靠原型供给，2~29 级才在补丁里写值。
 */
public final class BHR_WorkBenchLevel {

	/** 组件的注册路径（命名空间是 {@code richards-whimsy-mod}）。 */
	public static final String PATH = "workbench_level";

	/** 描述栏那一行的语言键（{@code %s} 是等级）。 */
	public static final String TOOLTIP_KEY = "tooltip." + "richards-whimsy-mod." + PATH;

	/** 等级下限（也是「不写补丁」时原型给出的值）。 */
	public static final int MIN_LEVEL = 1;

	/** 等级上限。 */
	public static final int MAX_LEVEL = 29;

	/**
	 * 组件类型本体。在 {@link #OnInit()} 里赋值 —— 与项目「注册一律放 {@code OnInit()}」的约定一致。
	 */
	private static DataComponentType<Level> component;

	private BHR_WorkBenchLevel() {
	}

	/** 组件类型；未初始化时返回 {@code null}（调用方要兜底，见 {@link #levelOf}）。 */
	public static DataComponentType<Level> component() {
		return component;
	}

	/**
	 * 注册组件，并把它的 tooltip 挂到原版 {@code minecraft:lore} 之后。
	 * <p>由 {@code BHR_WorkBench.OnInit()} 调用（必须早于物品被创建 —— 原型组件要用到它）。
	 */
	public static void OnInit() {
		if (component != null) {
			return;
		}

		component = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, ModRegister.id(PATH),
				DataComponentType.<Level>builder()
						.persistent(Level.CODEC)
						.networkSynchronized(Level.STREAM_CODEC)
						.build());

		// 挂在 lore 之后：物品描述里先看到自己的 lore，再看到「等级：N」。
		ItemComponentTooltipProviderRegistry.addAfter(DataComponents.LORE, component);
	}

	/**
	 * 读取物品堆上的等级。
	 *
	 * <p>没有组件（理论上只会在组件未注册 / 非物品来源时出现）按 1 级处理；
	 * 越界值一律夹到 {@link #MIN_LEVEL}~{@link #MAX_LEVEL}，
	 * 避免手改 NBT / 指令写坏数据后在放置时抛 {@code IllegalArgumentException}（方块状态不认越界值）。
	 *
	 * @param stack 物品堆（可为 {@code null}）
	 */
	public static int levelOf(ItemInstance stack) {
		if (component == null || stack == null) {
			return MIN_LEVEL;
		}
		Level value = stack.get(component);
		return value == null ? MIN_LEVEL : Mth.clamp(value.value(), MIN_LEVEL, MAX_LEVEL);
	}

	/**
	 * 把等级写进物品堆。
	 *
	 * <p><b>等级为 {@link #MIN_LEVEL} 时什么都不做</b> —— 原型组件已经给了 1 级，
	 * 所以「不碰补丁」就等于 1 级，而且这样得到的物品堆与「新建的物品堆」<b>逐组件相同</b>
	 * （实测：{@code stable 堆 vs 新堆} = true）。
	 * <p>⚠️ 这里<b>不能</b>写成 {@code stack.remove(component)}：原型里已有该组件时，
	 * 「移除」会在补丁里留下一条「显式没有」的记号，于是它与新堆<b>不再相同</b>、
	 * 堆叠会分裂（26.2 实测 {@code isSameItemSameComponents} = false，踩过一次）。
	 *
	 * @return 同一个 {@code stack}，便于链式写
	 */
	public static ItemStack set(ItemStack stack, int level) {
		if (component == null) {
			return stack;
		}
		int clamped = Mth.clamp(level, MIN_LEVEL, MAX_LEVEL);
		if (clamped != MIN_LEVEL) {
			stack.set(component, new Level(clamped));
		}
		return stack;
	}

	/**
	 * 组件载荷：一个 1~29 的等级。
	 *
	 * <p>它自己实现 {@link TooltipProvider}，所以描述栏不需要客户端事件、也不需要 mixin ——
	 * 注册时把本组件挂到 {@code DataComponents.LORE} 之后即可（见 {@link #OnInit()}）。
	 * 文案是灰色斜体，与原版的 lore 行同一观感。
	 */
	public record Level(int value) implements TooltipProvider {

		/**
		 * 持久化 Codec：<b>裸整数</b>（不是 {@code {"level": N}}）。
		 * <p>这一点兼作物品模型的 case 值格式：{@code select} 的 {@code when} 用组件自己的 codec 解码，
		 * 所以 {@code items/bhr_workbench.json} 里写的是 {@code "when": 9}。
		 */
		public static final Codec<Level> CODEC = Codec.INT.xmap(Level::new, Level::value);

		/** 网络同步：同样是一个 varint。 */
		public static final StreamCodec<ByteBuf, Level> STREAM_CODEC =
				ByteBufCodecs.VAR_INT.map(Level::new, Level::value);

		/** 描述栏那一行（{@code 等级：N} / {@code Level: N}）。 */
		@Override
		public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag,
				DataComponentGetter components) {
			consumer.accept(Component.translatable(TOOLTIP_KEY, this.value)
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		}
	}
}
