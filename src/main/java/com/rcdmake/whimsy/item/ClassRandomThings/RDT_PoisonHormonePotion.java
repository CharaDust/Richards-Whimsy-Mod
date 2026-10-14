package com.rcdmake.whimsy.item.ClassRandomThings;

import com.rcdmake.whimsy.item.WhimsyPotionRegistration;
import com.rcdmake.whimsy.item.WhimsyPotionRegistration.Form;
import com.rcdmake.whimsy.statusEffect.ClassRandomThings.RDT_FireImbue;
import com.rcdmake.whimsy.statusEffect.ClassRandomThings.RDT_PoisonImbue;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
/**
 * 毒性激素药水（{@code rdt_poison_hormone_potion_1} ~ {@code _3}）——
 * 喝下后获得「毒性灌注」I~III 级，各 <b>10 分钟</b>。只做饮用型。
 *
 * <h2>清单（3 级，共 3 件）</h2>
 * <table border="1">
 *   <caption>本品全部物品</caption>
 *   <tr><th>物品</th><th>效果</th><th>时长</th></tr>
 *   <tr><td>{@code rdt_poison_hormone_potion_1}</td><td>毒性灌注 I</td><td>10:00</td></tr>
 *   <tr><td>{@code rdt_poison_hormone_potion_2}</td><td>毒性灌注 II</td><td>10:00</td></tr>
 *   <tr><td>{@code rdt_poison_hormone_potion_3}</td><td>毒性灌注 III</td><td>10:00</td></tr>
 * </table>
 *
 * <p>效果本体见 {@code RDT_PoisonImbue}（数值表、攻击中毒规则）。
 *
 * <h2>互斥（激素族）</h2>
 * <p>与 {@link RDT_FireHormonePotion} 同族、互为互斥：喝本品会<b>先移除火焰灌注</b>，
 * 再施加毒性灌注。本族只含 RDT 的这两个灌注效果，WI 的同名效果不在其中
 * （模块解耦；详见 {@link RDT_FireHormonePotion} 的说明）。
 */
public final class RDT_PoisonHormonePotion {

	/** 注册名基名。 */
	public static final String BASE = "rdt_poison_hormone_potion";

	/** 各等级共用时长（tick）：12000 = 10 分钟。 */
	public static final int DURATION_TICKS = 10 * 60 * 20;

	/** 本批要注册的游戏内等级（1 起；amplifier = 等级 - 1）。 */
	public static final int[] LEVELS = {1, 2, 3};

	/** 本职作用的效果。 */
	public static final Holder<MobEffect> EFFECT = RDT_PoisonImbue.RDT_POISON_IMBUE;

	/** 注册出的全部物品，索引 0 = 等级 I。 */
	public static final List<Item> ALL = new ArrayList<>();

	/** 本族的另一味（互斥对象）：火焰灌注。 */
	public static Holder<MobEffect> sibling() {
		return RDT_FireImbue.RDT_FIRE_IMBUE;
	}

	private RDT_PoisonHormonePotion() {
	}

	/** 注册本品。互斥清单在注册时一并写入（理由见 {@link RDT_FireHormonePotion#OnInit()}）。 */
	public static void OnInit() {
		ALL.clear();
		List<Holder<MobEffect>> siblings = RDT_FireImbue.RDT_FIRE_IMBUE == null
				? List.of()
				: List.of(RDT_FireImbue.RDT_FIRE_IMBUE);

		for (int level : LEVELS) {
			ALL.add(WhimsyPotionRegistration.register(
					BASE + "_" + level,
					EFFECT,
					DURATION_TICKS,
					level - 1,
					Form.DRINK,
					// 毒性灌注的粒子色 0x41DD53
					0x41DD53,
					siblings));
		}
	}
}
