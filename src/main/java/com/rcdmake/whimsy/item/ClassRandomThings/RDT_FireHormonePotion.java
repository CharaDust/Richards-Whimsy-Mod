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
 * 火焰激素药水（{@code rdt_fire_hormone_potion_1} ~ {@code _3}）——
 * 喝下后获得「火焰灌注」I~III 级，各 <b>10 分钟</b>。只做饮用型。
 *
 * <h2>清单（3 级，共 3 件）</h2>
 * <table border="1">
 *   <caption>本品全部物品</caption>
 *   <tr><th>物品</th><th>效果</th><th>时长</th></tr>
 *   <tr><td>{@code rdt_fire_hormone_potion_1}</td><td>火焰灌注 I</td><td>10:00</td></tr>
 *   <tr><td>{@code rdt_fire_hormone_potion_2}</td><td>火焰灌注 II</td><td>10:00</td></tr>
 *   <tr><td>{@code rdt_fire_hormone_potion_3}</td><td>火焰灌注 III</td><td>10:00</td></tr>
 * </table>
 *
 * <p>效果本体见 {@code RDT_FireImbue}（攻击点火规则、每级 100 tick）。
 * 注册、lore、互斥都在 {@link WhimsyPotionRegistration}（共享辅助，不属于任何模块的功能）。
 *
 * <h2>互斥（激素族）</h2>
 * <p>与 {@link RDT_PoisonHormonePotion} 同族、互为互斥：喝本品会<b>先移除毒性灌注</b>，
 * 再施加火焰灌注。于是「身上有毒性灌注时喝火焰激素」是被顶替，而不是两个灌注并存。
 *
 * <p>⚠️ 本族<b>只含 RDT 的这两个灌注效果</b>。WI 模块有另一对同名效果
 * （业务上无关的两套实现），两边各自互斥、互不干涉 —— 这是模块解耦的直接后果，
 * 不是遗漏。跨模块互斥需要先明确设计。
 *
 * <h2>为什么放在 RDT 模块</h2>
 * <p>按需求指定。另一个好处是「药水作用于本模块的效果」，避免了 WI 去引用 RDT 的类。
 */
public final class RDT_FireHormonePotion {

	/** 注册名基名。 */
	public static final String BASE = "rdt_fire_hormone_potion";

	/** 各等级共用时长（tick）：12000 = 10 分钟。 */
	public static final int DURATION_TICKS = 10 * 60 * 20;

	/** 本批要注册的游戏内等级（1 起；amplifier = 等级 - 1）。 */
	public static final int[] LEVELS = {1, 2, 3};

	/** 本职作用的效果。 */
	public static final Holder<MobEffect> EFFECT = RDT_FireImbue.RDT_FIRE_IMBUE;

	/** 注册出的全部物品，索引 0 = 等级 I。 */
	public static final List<Item> ALL = new ArrayList<>();

	/** 本族的另一味（互斥对象）：毒性灌注。 */
	public static Holder<MobEffect> sibling() {
		return RDT_PoisonImbue.RDT_POISON_IMBUE;
	}

	private RDT_FireHormonePotion() {
	}

	/**
	 * 注册本品。
	 *
	 * <p><b>互斥清单在注册时就写进 {@code consumable} 组件</b>（不做事后补）——
	 * 由原版 {@code RemoveStatusEffectsConsumeEffect} 承担，顺序为「先移除同族、再施加本品」。
	 * 为什么不事后补：组件是不可变对象，注册后再改物品字段不会影响已建好的组件；
	 * 而用自定义 {@code ConsumeEffect} 又会被组件序列化丢掉（两次踩坑详见
	 * {@link WhimsyPotionRegistration} 类注释）。
	 *
	 * <p>因此调用前提是「两味的 {@link #EFFECT} 都已就绪」——即两个激素药水类都至少
	 * 「先赋好效果句柄」。主类里按「火焰赋句柄 → 毒性赋句柄 → 两者注册」的顺序调用。
	 */
	public static void OnInit() {
		ALL.clear();
		List<Holder<MobEffect>> siblings = RDT_PoisonImbue.RDT_POISON_IMBUE == null
				? List.of()
				: List.of(RDT_PoisonImbue.RDT_POISON_IMBUE);

		for (int level : LEVELS) {
			ALL.add(WhimsyPotionRegistration.register(
					BASE + "_" + level,
					EFFECT,
					DURATION_TICKS,
					level - 1,
					Form.DRINK,
					// 火焰灌注的粒子色 0xF68127
					0xF68127,
					siblings));
		}
	}
}
