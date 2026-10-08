package com.rcdmake.whimsy;

import com.rcdmake.whimsy.item.BHR_CreativeTabs;
import com.rcdmake.whimsy.item.DEV_CreativeTabs;
import com.rcdmake.whimsy.item.LGC_CreativeTabs;
import com.rcdmake.whimsy.item.RDT_CreativeTabs;
import com.rcdmake.whimsy.item.WI_CreativeTabs;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

/**
 * 创造模式物品栏的<b>汇总入口 + 统一注册写法</b>。
 *
 * <p>本类不注册任何<b>内容</b>——每个模块的标签页写在 {@code com.rcdmake.whimsy.item} 下
 * 各自的 {@code <模块>_CreativeTabs} 类里（命名规则：{@code {模块名_CreativeTabs}}）。
 * 主类只需要调用这里的 {@link #OnInit()}，由它按顺序转发给各模块。
 * 各模块的具体注册动作统一走本类的 {@link #registerTab}（阶段 0 起，取代三个模块各自复制的私有写法）。
 *
 * <p>为什么汇总类留在根包而不放进 {@code item} 包：{@code item} 包里放的是
 * 「某个模块的标签页」，本类是「所有模块标签页的调度者」，层级不同。
 *
 * <p>各模块的注册名与标题语言键（wiki 目标值）：
 * <table border="1">
 *   <caption>模块物品栏一览</caption>
 *   <tr><th>模块</th><th>类</th><th>注册名</th><th>标题语言键</th></tr>
 *   <tr><td>方块愚者 BHR</td><td>{@link BHR_CreativeTabs}</td><td>{@code bhr_general}</td><td>{@code itemGroup.bhr_general}</td></tr>
 *   <tr><td>初学开发 DEV</td><td>{@link DEV_CreativeTabs}</td><td>{@code dev_general}</td><td>{@code itemGroup.dev_general}</td></tr>
 *   <tr><td>随意物件 RDT</td><td>{@link RDT_CreativeTabs}</td><td>{@code rdt_general}</td><td>{@code itemGroup.rdt_general}</td></tr>
 *   <tr><td>奇思妙想 WI</td><td>{@link WI_CreativeTabs}</td><td>{@code wi_group_block} / {@code wi_group_item}</td><td>{@code itemGroup.wi_group_*}</td></tr>
 *   <tr><td>原版考古 LGC</td><td>{@link LGC_CreativeTabs}</td><td>{@code lgc_general}（待定）</td><td>{@code itemGroup.lgc_general}</td></tr>
 * </table>
 *
 * <p>除 DEV 外，其余模块目前尚无重制完成的方块/物品，对应的 {@code OnInit()} 是空的。
 */
public final class ModCreativeTabs {

	private ModCreativeTabs() {
	}

	/**
	 * 注册全部模块的创造模式物品栏。
	 * <p>由 {@code RichardsWhimsyMod#onInitialize()} 在方块与物品注册之后调用。
	 */
	public static void OnInit() {
		BHR_CreativeTabs.OnInit();
		DEV_CreativeTabs.OnInit();
		RDT_CreativeTabs.OnInit();
		WI_CreativeTabs.OnInit();
		LGC_CreativeTabs.OnInit();
	}

	/**
	 * 注册一个创造模式物品栏页（<b>全部模块的统一写法</b>）。
	 *
	 * <p>调用方只需要给「注册 id / 图标 / 包含清单」三样东西，注册动作本身都在这里：
	 * <pre>{@code
	 * ModCreativeTabs.registerTab(GROUP_ID,
	 *         () -> new ItemStack(DEV_ItemsOther.DEV_ITEM_NORMAL),   // 图标
	 *         (parameters, output) -> {                               // 包含清单（顺序即显示顺序）
	 *             output.accept(DEV_BlocksOther.DEV_BLOCK_NORMAL);
	 *         });
	 * }</pre>
	 *
	 * <p><b>26.2 写法</b>（已对 fabric-creative-tab-api-v1 5.0.15 核验）：
	 * Builder 是 {@code FabricCreativeModeTab.builder()}（旧名 {@code FabricItemGroup} 已不在）；
	 * 注册表键常量在 {@code Registries.CREATIVE_MODE_TAB}；
	 * 往<b>自己</b>的标签页加东西用 builder 的 {@code displayItems} 回调 + {@code output.accept(...)}，
	 * 往<b>别人已有</b>的标签页追加才需要 {@code CreativeModeTabEvents.modifyOutputEvent(ResourceKey)}。
	 * 标题语言键按惯例由 id 推出：{@code itemGroup.<id>}。
	 *
	 * <p><b>26.2 陷阱</b>：{@code CreativeModeTabs.INGREDIENTS} 这类原版标签键字段已是
	 * private，外部访问不到；要用原版标签页只能自己重建 {@code ResourceKey}。
	 *
	 * @param id       注册名（也是标题语言键 {@code itemGroup.<id>} 的来源）
	 * @param icon     标签页图标
	 * @param contents 包含清单：{@code (parameters, output) -> output.accept(...)}
	 */
	public static void registerTab(String id, Supplier<ItemStack> icon,
			CreativeModeTab.DisplayItemsGenerator contents) {
		ResourceKey<CreativeModeTab> key =
				ResourceKey.create(Registries.CREATIVE_MODE_TAB, ModRegister.id(id));

		CreativeModeTab tab = FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup." + id))
				.icon(icon)
				.displayItems(contents)
				.build();

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, tab);
	}
}
