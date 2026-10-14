package com.rcdmake.whimsy.statusEffect.ClassRandomThings;

import net.minecraft.world.effect.MobEffectCategory;

/**
 * 火焰灌注的<b>效果实例</b>：只有构造器，没有行为。
 *
 * <p>拆成独立类（而不是在 {@link RDT_FireImbue} 里 {@code new RDT_ImbueEffect(...) {}}）
 * 有两个理由：
 * <ul>
 *   <li>匿名子类会各产出一个合成类 {@code RDT_FireImbue$1}，注册表里存的就是它——
 *       出问题时日志/调试器里看不到有意义的名字；</li>
 *   <li>「哪个类被注册」这件事在源码里一眼可查：注册表里躺的是 {@code RDT_FireImbueEffect}。</li>
 * </ul>
 *
 * <p>数值与注册点都在 {@link RDT_FireImbue}；逐刻行为刻意没有——见 {@link RDT_ImbueEffect}。
 */
public class RDT_FireImbueEffect extends RDT_ImbueEffect {

	public RDT_FireImbueEffect() {
		super(MobEffectCategory.BENEFICIAL, RDT_FireImbue.COLOR);
	}
}
