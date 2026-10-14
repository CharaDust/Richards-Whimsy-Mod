package com.rcdmake.whimsy.statusEffect.ClassRandomThings;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 「灌注」类状态效果的共同父类：把两个效果唯一相同的部分收在一处。
 *
 * <h2>为什么必须有这个子类（26.2 的构造器可见性）</h2>
 * <p>1.20.4 的旧实现里，两个效果类各自 {@code extends StatusEffect} 并自己写
 * {@code super(category, color)}。迁移到 26.2 时最容易踩的坑是「直接 new」：
 * {@code MobEffect(MobEffectCategory, int)} 的可见性是 <b>{@code protected}</b>
 * （已对 26.2 的 {@code net.minecraft.world.effect.MobEffect} 逐条核验），
 * 在别的包里 {@code new MobEffect(...)} 会直接编译失败
 * （报错形如「MobEffect(MobEffectCategory,int) 在 MobEffect 中是 protected 访问控制」）。
 * 所以效果必须由子类提供，这也正是原版 {@code MobEffects} 里每个效果的写法。
 *
 * <h2>本类刻意<b>只</b>做三件事</h2>
 * <ol>
 *   <li>转发构造器（分类 + 粒子颜色）；</li>
 *   <li>把 {@code applyEffectTick} 明确覆写为 {@code false}——
 *       「灌注」不是逐刻效果，它只在<b>攻击结算</b>时起作用
 *       （见 {@link RDT_ImbueAttackHandler}），所以服务端每刻都不该为它白跑一趟。
 *       写成显式覆写而不是「靠原版默认值」，是为了让「这里什么都不做」成为<b>声明</b>而非巧合——
 *       将来原版若改了默认实现，行为也不会静默变化。</li>
 *   <li>什么都不多做。具体数值（点火时长、中毒表）留在各自的效果类里，
 *       因为两者口径完全不同：火焰是公式，毒性是离散表。</li>
 * </ol>
 *
 * <p>本类<b>不注册、不进 jar 的注册表</b>，只作为两个效果的父类存在。
 */
public abstract class RDT_ImbueEffect extends MobEffect {

	/**
	 * @param category 效果分类（两个灌注都是 {@link MobEffectCategory#BENEFICIAL}）
	 * @param color    粒子颜色（wiki 各自指定）
	 */
	protected RDT_ImbueEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

	/**
	 * 「灌注」没有逐刻作用，所以这里恒返回 false：服务端不会为它执行 {@code applyEffectTick}。
	 *
	 * <p>（26.2 的映射对照：1.20.4 的 {@code canApplyUpdateEffect} → 本方法，
	 * {@code applyUpdateEffect} → {@code applyEffectTick}；两者本类都<b>不</b>实现。）
	 */
	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return false;
	}
}
