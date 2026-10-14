package com.rcdmake.whimsy.item.ClassWhimsyIdea.potion;

import com.rcdmake.whimsy.item.WhimsyPotionRegistration;
import com.rcdmake.whimsy.item.WhimsyPotionRegistration.Form;
import com.rcdmake.whimsy.statusEffect.ClassWhimsyIdea.WI_Radioactive;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * 放射药水 —— 喝 / 投掷后获得「电离辐射」，只做最高的两级。
 *
 * <h2>清单（2 级 × 3 形态 = 6 件）</h2>
 * <table border="1">
 *   <caption>本品全部物品</caption>
 *   <tr><th>等级</th><th>饮用</th><th>喷溅</th><th>滞留</th><th>时长</th></tr>
 *   <tr><td>IV</td><td>{@code wi_radiation_potion_4}</td><td>{@code wi_radiation_splash_potion_4}</td>
 *       <td>{@code wi_radiation_lingering_potion_4}</td><td>5:00</td></tr>
 *   <tr><td>V</td><td>{@code wi_radiation_potion_5}</td><td>{@code wi_radiation_splash_potion_5}</td>
 *       <td>{@code wi_radiation_lingering_potion_5}</td><td>5:00</td></tr>
 * </table>
 *
 * <p>效果本体见 {@code WI_Radioactive}（扣血间隔、生命值上限削减、近战传染与药水云）。
 *
 * <h2>⚠️ 这是负面效果</h2>
 * <p>电离辐射持续扣血并削减生命值上限，本品的用途是「投给目标 / 做陷阱」，不是增益药。
 * 喷溅与滞留形态正是为此存在 —— 这才是把它丢出去的正确用法。
 *
 * <h2>外观</h2>
 * <p>原版双层药水模型，着色值 {@link WI_PotionFamily#RADIATION_TINT}（琥珀）。
 */
public final class WI_RadiationPotion {

	/** 注册名基名（形态与等级拼在后面）。 */
	public static final String BASE = "wi_radiation";

	/** 各等级共用时长（tick）：6000 = 5 分钟。 */
	public static final int DURATION_TICKS = 5 * 60 * 20;

	/** 本批要注册的游戏内等级（1 起；amplifier = 等级 - 1）。 */
	public static final int[] LEVELS = {4, 5};

	/** 本职作用的效果。 */
	public static final Holder<MobEffect> EFFECT = WI_Radioactive.WI_RADIOACTIVE;

	/** 注册出的全部物品。 */
	public static final List<WI_PotionEntry> ENTRIES = new ArrayList<>();

	private WI_RadiationPotion() {
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
		int amplifier = level - 1;

		register(Form.DRINK, level, BASE + "_potion" + suffix, amplifier);
		register(Form.SPLASH, level, BASE + "_splash_potion" + suffix, amplifier);
		register(Form.LINGERING, level, BASE + "_lingering_potion" + suffix, amplifier);
	}

	private static void register(Form form, int level, String id, int amplifier) {
		Item item = WhimsyPotionRegistration.register(
				id, EFFECT, DURATION_TICKS, amplifier, form, WI_PotionFamily.RADIATION_TINT);
		ENTRIES.add(new WI_PotionEntry(form, level, item));
	}
}
