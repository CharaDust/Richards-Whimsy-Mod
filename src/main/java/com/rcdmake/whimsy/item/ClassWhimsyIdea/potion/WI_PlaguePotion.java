package com.rcdmake.whimsy.item.ClassWhimsyIdea.potion;

import com.rcdmake.whimsy.item.WhimsyPotionRegistration;
import com.rcdmake.whimsy.item.WhimsyPotionRegistration.Form;
import com.rcdmake.whimsy.statusEffect.ClassWhimsyIdea.WI_BioToxicity;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * 瘟疫药水 —— 喝 / 投掷后获得「生物毒性」，只做最高的两级。
 *
 * <h2>清单（2 级 × 3 形态 = 6 件）</h2>
 * <table border="1">
 *   <caption>本品全部物品</caption>
 *   <tr><th>等级</th><th>饮用</th><th>喷溅</th><th>滞留</th><th>时长</th></tr>
 *   <tr><td>III</td><td>{@code wi_plague_potion_3}</td><td>{@code wi_plague_splash_potion_3}</td>
 *       <td>{@code wi_plague_lingering_potion_3}</td><td>5:00</td></tr>
 *   <tr><td>IV</td><td>{@code wi_plague_potion_4}</td><td>{@code wi_plague_splash_potion_4}</td>
 *       <td>{@code wi_plague_lingering_potion_4}</td><td>5:00</td></tr>
 * </table>
 *
 * <p>效果本体见 {@code WI_BioToxicity}（扣血间隔、6 点生命值下限、传染规则）。
 * 三形态、lore、互斥的实现都在 {@link WhimsyPotionRegistration}，本类只报「要哪些」。
 *
 * <h2>为什么只做 III/IV 级</h2>
 * <p>按需求「只做最高的 2 级」。低等级以后要补，在 {@link #OnInit()} 里加一行
 * {@code registerTier(1)} 即可，其余无需改动。
 *
 * <h2>外观</h2>
 * <p>原版双层药水模型（{@code layer0 = potion_overlay} 被染色，{@code layer1} 是瓶身），
 * 着色值 {@link WI_PotionFamily#PLAGUE_TINT}；喷溅/滞留用各自的原版瓶身贴图。
 * 模型定义与 tints 由 {@code ItemModelGenerator} 生成。
 */
public final class WI_PlaguePotion {

	/** 注册名基名（形态与等级拼在后面）。 */
	public static final String BASE = "wi_plague";

	/** 各等级共用时长（tick）：6000 = 5 分钟。 */
	public static final int DURATION_TICKS = 5 * 60 * 20;

	/**
	 * 本批要注册的等级（<b>游戏内等级</b>，1 起；效果 amplifier 等于它减一）。
	 * <p>要增减等级就改这一行。
	 */
	public static final int[] LEVELS = {3, 4};

	/** 本职作用的效果。 */
	public static final Holder<MobEffect> EFFECT = WI_BioToxicity.WI_BIO_TOXICITY;

	/** 注册出的全部物品（饮用/喷溅/滞留 依次，按等级分组）。 */
	public static final List<WI_PotionEntry> ENTRIES = new ArrayList<>();

	private WI_PlaguePotion() {
	}

	public static void OnInit() {
		ENTRIES.clear();
		for (int level : LEVELS) {
			registerTier(level);
		}
	}

	/** 注册一个等级的三种形态。 */
	private static void registerTier(int level) {
		String suffix = "_" + level;
		// amplifier 从 0 起算，所以游戏内 III 级 = amplifier 2
		int amplifier = level - 1;

		register(Form.DRINK, level, BASE + "_potion" + suffix, amplifier);
		register(Form.SPLASH, level, BASE + "_splash_potion" + suffix, amplifier);
		register(Form.LINGERING, level, BASE + "_lingering_potion" + suffix, amplifier);
	}

	private static void register(Form form, int level, String id, int amplifier) {
		Item item = WhimsyPotionRegistration.register(
				id, EFFECT, DURATION_TICKS, amplifier, form, WI_PotionFamily.PLAGUE_TINT);
		ENTRIES.add(new WI_PotionEntry(form, level, item));
	}
}
