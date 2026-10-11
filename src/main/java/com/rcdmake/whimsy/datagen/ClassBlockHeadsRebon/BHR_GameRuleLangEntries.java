package com.rcdmake.whimsy.datagen.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_GameRules;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.world.level.gamerules.GameRule;

/**
 * 方块愚者（BHR）<b>游戏规则</b>的语言条目（中英各一套）。
 *
 * <p>为什么单独一个类：{@code BHR_LangEntries} 已经很长了（26 个方块 + 物品），
 * 而游戏规则是「misc」这一类的新条目，混进去会让那份「按物品栏分组顺序排列」的清单失焦。
 * 两者都由 {@code datagen/ModLangProviderZh} 与 {@code ModLangProviderEn} 调用。
 *
 * <h2>一条规则要写两条键（写错会显示裸 key）</h2>
 * <ul>
 *   <li>名称：{@code <ns>.<path>} —— 世界创建界面 / {@code /gamerule} 提示里显示的名字；</li>
 *   <li>说明：{@code gamerule.<ns>.<path>} —— 同处的第二行说明文字。</li>
 * </ul>
 * <p>这两个键名<b>由规则 id 现算</b>（{@link #addRule}），不手写字符串 ——
 * 规则改名时这里会跟着改，不会留下一对失效的旧键。
 *
 * <p>⚠️ 界面本身还没在游戏里点开验证过（本批只在专用服务端跑过冒烟，
 * 客户端界面不在那条路径上）；若界面显示裸 key，先查这里的两条键与 {@code GameRuleCategory.MISC} 的归属。
 */
public final class BHR_GameRuleLangEntries {

	private BHR_GameRuleLangEntries() {
	}

	/** 简体中文条目。 */
	public static void zh(FabricLanguageProvider.TranslationBuilder builder) {
		addRule(builder, BHR_GameRules.BHR_EXPERT_MODE,
				"专家模式",
				"一键把下列 BHR 细调条目写入专家预设（关闭时写回普通预设）。它只是个调节工具，不锁定任何条目。");
		addRule(builder, BHR_GameRules.BHR_DECAYING_LEAVES_NO_DROP,
				"树叶枯萎无掉落",
				"枯萎的 BHR 树叶直接消失、不掉落（与极限模式一致）。关闭时按 1/5 几率带掉落物破坏。");
		addRule(builder, BHR_GameRules.BHR_LEGACY_HUNGER_CONSUMPTION,
				"传统饥饿值消耗",
				"把饥饿值消耗调回 Minecraft 1.8 的水平（机制尚未实现，此项暂时只是占位规则）。");
	}

	/** 英文条目（与 {@link #zh} 同一批键）。 */
	public static void en(FabricLanguageProvider.TranslationBuilder builder) {
		addRule(builder, BHR_GameRules.BHR_EXPERT_MODE,
				"BHR Expert Mode",
				"One-click preset: writes the BHR expert values into the detail rules below "
						+ "(writes the normal values back when turned off). It is a convenience dial, not a lock.");
		addRule(builder, BHR_GameRules.BHR_DECAYING_LEAVES_NO_DROP,
				"Decaying Leaves Drop Nothing",
				"Decaying BHR leaves vanish without drops (same as hardcore). When off, they are destroyed "
						+ "with drops on a 1/5 chance instead.");
		addRule(builder, BHR_GameRules.BHR_LEGACY_HUNGER_CONSUMPTION,
				"Legacy Hunger Consumption",
				"Restores Minecraft 1.8-level hunger exhaustion. The mechanic itself is not implemented yet; "
						+ "this rule is a placeholder.");
	}

	/**
	 * 为一条规则写「名称 + 说明」两条键。
	 * <p>键名从规则 id 现算：名称用 id 本身，说明用 {@code gamerule.} 前缀 ——
	 * 与 26.2 里 {@code GameRule.getDescriptionId()} / 界面查键的方式一致。
	 */
	private static void addRule(FabricLanguageProvider.TranslationBuilder builder, GameRule<Boolean> rule,
			String name, String description) {
		String id = rule.id();
		builder.add(id, name);
		builder.add("gamerule." + id, description);
	}
}
