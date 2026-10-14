package com.rcdmake.whimsy.statusEffect.ClassWhimsyIdea;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 奇思妙想（WI）模块「损伤类」状态效果的共同父类。
 *
 * <h2>为什么需要它（26.2 的构造器可见性）</h2>
 * <p>{@code MobEffect(MobEffectCategory, int)} 的可见性是 <b>{@code protected}</b>
 * （已对 26.2 的 {@code net.minecraft.world.effect.MobEffect} 逐条核验），
 * 所以效果必须由子类提供，不能在别的包里直接 {@code new MobEffect(...)}。
 *
 * <h2>本模块为什么可以共用父类</h2>
 * <p>本项目约定「模块之间必须解耦」，指的是 <b>A 模块的功能不得调用 B 模块提供的方法</b>。
 * 本类位于 {@code statusEffect.ClassWhimsyIdea} 包内，<b>只被 WI 模块自己的两个效果继承</b>，
 * 与 RDT 模块的 {@code RDT_ImbueEffect} 完全无关——两者是各自独立、各自演进的两个父类，
 * 名字相近是刻意的（便于对照阅读），<b>不是</b>共享基类。
 *
 * <h2>RDT 与 WI 的效果性质差异（决定了两边父类不合并）</h2>
 * <ul>
 *   <li>RDT 的「灌注」是 <b>BENEFICIAL</b> 且完全不在逐刻做事（挂点只在攻击结算）；</li>
 *   <li>WI 的「损伤类」是 <b>HARMFUL</b> 且<b>必须</b>逐刻做事（按等级间隔持续扣血），
 *       电离辐射还要挂生命值上限修饰符。</li>
 * </ul>
 * 因此本父类只做「转发构造器」一件事：逐刻逻辑各效果自己实现，
 * 不在这里提供任何默认实现，避免两个模块的行为被一个共同基类悄悄绑在一起。
 */
public abstract class WI_DamageEffect extends MobEffect {

	/**
	 * @param category 效果分类（WI 的损伤类都是 {@link MobEffectCategory#HARMFUL}）
	 * @param color    粒子颜色（wiki 各自指定）
	 */
	protected WI_DamageEffect(MobEffectCategory category, int color) {
		super(category, color);
	}
}
