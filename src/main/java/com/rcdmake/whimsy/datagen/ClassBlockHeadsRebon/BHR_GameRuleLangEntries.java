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
 * <h2>一条规则要写哪些键（写错会显示裸 key，且不报错）</h2>
 * 引擎实际查的形状是 {@code gamerule.<命名空间>:<路径>}（**带命名空间、带冒号**），
 * 说明键是它加 {@code .description}。形状细节与走过的三次弯路见 {@link #addRule}。
 * <p>键名全部<b>由规则 id 现算</b>，不手写字符串：规则改名时这里会跟着改，不会留下一堆失效的旧键。
 */
public final class BHR_GameRuleLangEntries {

	private BHR_GameRuleLangEntries() {
	}

	/** 简体中文条目。 */
	public static void zh(FabricLanguageProvider.TranslationBuilder builder) {
		addRule(builder, BHR_GameRules.BHR_EXPERT_MODE,
				"专家模式",
				"一键把「树叶枯萎无掉落」「传统饥饿值消耗」等细调条目写成专家预设；关闭时写回普通预设。它只是个调节工具，单独改细调条目不会影响它。");
		addRule(builder, BHR_GameRules.BHR_DECAYING_LEAVES_NO_DROP,
				"树叶枯萎无掉落",
				"枯萎的 BHR 树叶直接消失、不掉落（与极限模式一致）。关闭时按 1/5 几率带掉落物破坏。");
		addRule(builder, BHR_GameRules.BHR_LEGACY_HUNGER_CONSUMPTION,
				"传统饥饿值消耗",
				"把饥饿值消耗调回 Minecraft 1.8 的水平：游泳、步行、潜行、破坏方块、跳跃、疾跑跳、攻击与饥饿效果全部按 1.8 的数值结算。");
	}

	/** 英文条目（与 {@link #zh} 同一批键）。 */
	public static void en(FabricLanguageProvider.TranslationBuilder builder) {
		addRule(builder, BHR_GameRules.BHR_EXPERT_MODE,
				"BHR Expert Mode",
				"One-click preset: writes the expert values into the detail rules "
						+ "(decaying leaves, legacy hunger, ...); turning it off writes the normal values back. "
						+ "It is a convenience dial: changing a detail rule does not affect it.");
		addRule(builder, BHR_GameRules.BHR_DECAYING_LEAVES_NO_DROP,
				"Decaying Leaves Drop Nothing",
				"Decaying BHR leaves vanish without drops (same as hardcore). When off, they are destroyed "
						+ "with drops on a 1/5 chance instead.");
		addRule(builder, BHR_GameRules.BHR_LEGACY_HUNGER_CONSUMPTION,
				"Legacy Hunger Consumption",
				"Restores Minecraft 1.8 hunger exhaustion: swimming, walking, sneaking, block breaking, "
						+ "jumping, sprint-jumping, attacking and the Hunger effect all use their 1.8 values.");
	}

	/**
	 * 为一条规则写名称 + 说明的语言键。
	 *
	 * <h2>⛔ 三次教训：游戏查的键是 {@code gamerule.<命名空间>:<路径>}</h2>
	 * 实测结论（来自玩家在游戏里看到的裸 key，那条字符串就是引擎查的键）：
	 * <pre>
	 * gamerule.richards-whimsy-mod:bhr_expert_mode      ← 引擎实际查的键（带命名空间、带冒号）
	 * </pre>
	 * <p>走过的弯路，记下来免得再走：
	 * <ol>
	 *   <li><b>第一版</b>写的正是上面这个形状，但<b>没写 {@code .description}</b>（说明另起了一个独立键），
	 *       于是名称与说明都查不到；</li>
	 *   <li><b>第二版</b>改成「照抄原版」的 {@code gamerule.<路径>} camelCase 形式
	 *       （{@code gamerule.bhrExpertMode}）—— 那是<b>原版规则的键</b>，模组规则并不查它，
	 *       于是照样显示裸 key，而且连第一版本来能中的名称键也丢了；</li>
	 *   <li>{@code Identifier} 本身<b>不做大小写转换</b>（读过源码/字节码：{@code toLanguageKey()}
	 *       只是把 namespace 与 path 用 {@code .} 拼起来）；也核对过原版 jar：
	 *       {@code GameRules} 注册路径是 snake_case（{@code allow_entering_nether_using_portals}），
	 *       而 {@code allowEnteringNetherUsingPortals} <b>在 jar 里根本不存在</b>。
	 *       所以「camelCase」这条线索是错的，别再去转大小写。</li>
	 * </ol>
	 *
	 * <h2>因此这里同时写多种形状</h2>
	 * <pre>
	 * gamerule.&lt;命名空间&gt;:&lt;路径&gt;                        ← 实测引擎查这个
	 * gamerule.&lt;命名空间&gt;:&lt;路径&gt;.description
	 * gamerule.&lt;命名空间&gt;.&lt;路径&gt;                        ← 点号风格（parity 保险）
	 * gamerule.&lt;命名空间&gt;.&lt;路径&gt;.description
	 * gamerule.&lt;路径&gt;                                   ← 原版风格（parity 保险）
	 * gamerule.&lt;路径&gt;.description
	 * </pre>
	 * 多写的几个是无害的冗余（datagen 产物、不进逻辑），换来的是「不管是哪种解析路径都能命中」。
	 * 想收紧的话：确认界面文字正常后，把没命中的那几条删掉即可 —— 但删之前先确认命中哪一条。
	 */
	private static void addRule(FabricLanguageProvider.TranslationBuilder builder, GameRule<Boolean> rule,
			String name, String description) {
		String ns = rule.getIdentifier().getNamespace();
		String path = rule.getIdentifier().getPath();
		for (String base : new String[] {
				"gamerule." + ns + ":" + path,   // 实测引擎查的形状
				"gamerule." + ns + "." + path,   // 点号风格
				"gamerule." + path,              // 原版风格（丢命名空间）
		}) {
			builder.add(base, name);
			builder.add(base + ".description", description);
		}
	}
}
