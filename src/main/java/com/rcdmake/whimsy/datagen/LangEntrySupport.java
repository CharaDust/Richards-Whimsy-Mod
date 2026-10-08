package com.rcdmake.whimsy.datagen;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.world.level.block.Block;

/**
 * 语言条目的<b>公共写法</b>（阶段 0 统一：原先 WI / RDT / DEV 三个模块各自复制了一份私有助手）。
 *
 * <p>本类只做「怎么登记一条方块翻译」，<b>不写任何具体名称</b> ——
 * 名称仍由各模块的 {@code <模块>_LangEntries} 提供，条目仍由模组级
 * {@code ModLangProviderZh} / {@code ModLangProviderEn} 汇总（26.2 同一语言码只允许一个 provider，
 * 详见 {@code DEV_LangEntries} 的类注释）。
 *
 * <h2>为什么需要这个助手：方块与方块物品是两个键</h2>
 * 26.2 里方块物品的翻译键取自它<b>自己的物品 id</b>，<b>不会</b>回退到方块的名字。实测的键：
 * <pre>
 *   block.richards-whimsy-mod.dev_block_normal   ← 方块
 *   item.richards-whimsy-mod.dev_block_normal    ← 它的方块物品（另一个键！）
 * </pre>
 * ⚠️ 只写 {@code add(Block, ...)} 的话，创造模式物品栏里的方块物品会显示成<b>裸键名</b>。
 * 所以每个方块都要走一次本方法，两条键一起写。
 *
 * <p>另一条相关经验：取方块物品要用 {@code block.asItem()}，<b>不要用 {@code Item.byBlock()}</b>
 * —— 后者在 26.2 已弃用且实测返回 {@code AIR}，会导致方块物品的翻译整批丢失（本项目踩过）。
 */
public final class LangEntrySupport {

	private LangEntrySupport() {
	}

	/**
	 * 同时登记「方块」与「它的方块物品」两条翻译。
	 *
	 * @param builder 语言提供器给的条目构造器
	 * @param block   方块本体（用它的静态字段，别现构造）
	 * @param name    显示名（中英各调用一次，名称由模块自己定）
	 */
	public static void addBlockAndItem(FabricLanguageProvider.TranslationBuilder builder, Block block, String name) {
		builder.add(block, name);
		builder.add(block.asItem(), name);
	}
}
