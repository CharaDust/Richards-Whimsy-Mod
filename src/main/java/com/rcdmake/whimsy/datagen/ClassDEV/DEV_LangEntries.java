package com.rcdmake.whimsy.datagen.ClassDEV;

import com.rcdmake.whimsy.block.ClassDEV.DEV_BlocksOther;
import com.rcdmake.whimsy.block.ClassDEV.DEV_GhostBlock;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateBoolean;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateEnum;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateEnumAxis;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateEnumFacing;
import com.rcdmake.whimsy.block.ClassDEV.DEV_StateInt;
import com.rcdmake.whimsy.datagen.LangEntrySupport;
import com.rcdmake.whimsy.item.ClassDEV.DEV_HardcoreSwitcher;
import com.rcdmake.whimsy.item.ClassDEV.DEV_ItemsOther;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

/**
 * 初学开发（DEV）分支的<b>语言条目</b>（中英各一套）。
 *
 * <h2>为什么不是 provider，而是一对静态方法</h2>
 * 26.2 的数据生成器<b>同一个语言码只允许一个 provider</b>：{@code FabricLanguageProvider}
 * 的 {@code getName()} 只由语言码决定（{@code "Language (en_us)"}），
 * 两个语言码相同的 provider 会被 {@code DataGenerator} 以
 * {@code Duplicate provider} 直接拒绝；而且它们写的还是同一个
 * {@code lang/<code>.json}，互相覆盖。
 *
 * <p>所以本模组改成「<b>每个模块贡献条目，模组级 provider 汇总</b>」：
 * 各模块在自己的 {@code <模块>_LangEntries} 里写条目，由
 * {@code datagen/ModLangProviderZh} / {@code ModLangProviderEn} 依次调用 ——
 * 与创造模式物品栏那套「模块各自注册、{@code ModCreativeTabs} 汇总」是同一个思路。
 *
 * <h2>键名不手拼</h2>
 * {@code TranslationBuilder} 的 {@code add(Block, String)} / {@code add(Item, String)}
 * 会自己按注册项推导键。26.2 实测的键：
 * <pre>
 *   block.richards-whimsy-mod.dev_block_normal   ← 方块
 *   item.richards-whimsy-mod.dev_block_normal    ← 它的方块物品（另一个键！）
 *   item.richards-whimsy-mod.dev_item_normal     ← 普通物品
 * </pre>
 * ⚠️ <b>方块物品必须单独写一份</b>：26.2 里方块物品的翻译键取自它自己的物品 id，
 * <b>不会</b>回退到方块的名字。只写 {@code add(Block, ...)} 的话，
 * 创造模式物品栏里的方块物品会显示成裸键名。
 */
public final class DEV_LangEntries {

	private DEV_LangEntries() {
	}

	/** 简体中文条目。 */
	public static void zh(FabricLanguageProvider.TranslationBuilder builder) {
		builder.add("itemGroup.dev_general", "初学开发");

		// 方块本体 + 它的方块物品：一次调用写两条键（键：block.* / item.*）
		LangEntrySupport.addBlockAndItem(builder, DEV_BlocksOther.DEV_BLOCK_NORMAL, "一般方块");
		LangEntrySupport.addBlockAndItem(builder, DEV_BlocksOther.DEV_BLOCK_VECTOR_FIX_SIDE_U, "固定向上方块");

		LangEntrySupport.addBlockAndItem(builder, DEV_StateBoolean.DEV_BLOCK_STATE_BOOLEAN, "布尔方块");
		LangEntrySupport.addBlockAndItem(builder, DEV_StateInt.DEV_BLOCK_STATE_INT, "整型方块");
		LangEntrySupport.addBlockAndItem(builder, DEV_StateEnum.DEV_BLOCK_STATE_ENUM, "枚举方块");
		LangEntrySupport.addBlockAndItem(builder, DEV_StateEnumAxis.DEV_BLOCK_STATE_AXIS, "轴方块");
		LangEntrySupport.addBlockAndItem(builder, DEV_StateEnumFacing.DEV_BLOCK_STATE_FACING, "朝向方块");
		LangEntrySupport.addBlockAndItem(builder, DEV_GhostBlock.DEV_BLOCK_GHOST, "幽灵方块");

		// 普通物品（键：item.richards-whimsy-mod.*）
		builder.add(DEV_ItemsOther.DEV_ITEM_NORMAL, "一般物品");
		builder.add(DEV_HardcoreSwitcher.DEV_HARDCORE_SWITCHER, "极限模式切换器");
		// 切换提示（键：message.richards-whimsy-mod.*）
		builder.add(DEV_HardcoreSwitcher.MESSAGE_KEY_ON, "极限模式：已开启（服务端已生效，客户端心形重进世界后刷新）");
		builder.add(DEV_HardcoreSwitcher.MESSAGE_KEY_OFF, "极限模式：已关闭");
	}

	/** 英文条目（与 {@link #zh} 同一批键）。 */
	public static void en(FabricLanguageProvider.TranslationBuilder builder) {
		builder.add("itemGroup.dev_general", "Whimsy DEV");

		LangEntrySupport.addBlockAndItem(builder, DEV_BlocksOther.DEV_BLOCK_NORMAL, "Normal Block");
		LangEntrySupport.addBlockAndItem(builder, DEV_BlocksOther.DEV_BLOCK_VECTOR_FIX_SIDE_U, "Vector Block Fixed Up");

		LangEntrySupport.addBlockAndItem(builder, DEV_StateBoolean.DEV_BLOCK_STATE_BOOLEAN, "Boolean Block");
		LangEntrySupport.addBlockAndItem(builder, DEV_StateInt.DEV_BLOCK_STATE_INT, "Integer Block");
		LangEntrySupport.addBlockAndItem(builder, DEV_StateEnum.DEV_BLOCK_STATE_ENUM, "Enum Block");
		LangEntrySupport.addBlockAndItem(builder, DEV_StateEnumAxis.DEV_BLOCK_STATE_AXIS, "Axis Block");
		LangEntrySupport.addBlockAndItem(builder, DEV_StateEnumFacing.DEV_BLOCK_STATE_FACING, "Facing Block");
		LangEntrySupport.addBlockAndItem(builder, DEV_GhostBlock.DEV_BLOCK_GHOST, "Ghost Block");

		builder.add(DEV_ItemsOther.DEV_ITEM_NORMAL, "Normal Item");
		builder.add(DEV_HardcoreSwitcher.DEV_HARDCORE_SWITCHER, "Hardcore Switcher");
		builder.add(DEV_HardcoreSwitcher.MESSAGE_KEY_ON, "Hardcore: ON (server-side is live; the client hearts refresh after rejoining)");
		builder.add(DEV_HardcoreSwitcher.MESSAGE_KEY_OFF, "Hardcore: OFF");
	}
}
