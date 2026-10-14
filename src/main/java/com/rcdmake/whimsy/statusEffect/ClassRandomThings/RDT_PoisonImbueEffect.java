package com.rcdmake.whimsy.statusEffect.ClassRandomThings;

import net.minecraft.world.effect.MobEffectCategory;

/**
 * 毒性灌注的<b>效果实例</b>：只有构造器，没有行为。
 *
 * <p>拆成独立类的理由与 {@link RDT_FireImbueEffect} 相同（不留合成类 {@code $1}、
 * 注册表里躺的类名可查）。数值与注册点都在 {@link RDT_PoisonImbue}。
 */
public class RDT_PoisonImbueEffect extends RDT_ImbueEffect {

	public RDT_PoisonImbueEffect() {
		super(MobEffectCategory.BENEFICIAL, RDT_PoisonImbue.COLOR);
	}
}
