package com.rcdmake.whimsy.mixin.ClassWhimsyIdea;

import com.rcdmake.whimsy.statusEffect.ClassWhimsyIdea.WI_RadioactiveAttackHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * <b>电离辐射（wi_radioactive）的反向传染</b>挂点：
 * 拥有 II 级及以上电离辐射的生物被打中时，把低一级、半时长的同名效果传给攻击者。
 *
 * <p>「反向」是相对生物毒性而言——那边是攻击者传给目标，这边是<b>被攻击者传给攻击者</b>。
 * 方向容易写反，两个处理器各自的类注释里都写明了。
 *
 * <h2>为什么本文件与 {@link WI_BioToxicityHurtMixin} 是两个类，而不是合成一个</h2>
 * <p>两者注入的是<b>同一个</b>原版方法 {@code LivingEntity.hurtServer}，
 * 合成一个类在技术上完全可行（Mixin 允许一个类里写多个 {@code @Inject}）。
 * 拆开是因为它们分属两个<b>独立的效果</b>：电离辐射与生物毒性是两个不相关的效果，
 * 各自的挂点理由、传染方向、是否免疫都不同。拆成两个类之后：
 * <ul>
 *   <li>将来只删掉「电离辐射」这个效果时，直接删掉本文件与它的处理器即可，
 *       不会牵动生物毒性（合并的话要在同一个类里挑着删，容易删错）；</li>
 *   <li>每个类只讲一件事，注释与注入点一一对应。</li>
 * </ul>
 * 代价是多一个文件、多一次注入；两者都在同一个 {@code @At("RETURN")} 上、
 * 各读各的返回值、互不取消，没有顺序依赖。
 *
 * <h2>与 RDT 模块的关系</h2>
 * <p>本类与 {@code RDT_ImbueHurtMixin} 同样注入 {@code hurtServer}，但<b>零引用</b>：
 * 这是刻意的模块解耦，理由详见 {@link WI_BioToxicityHurtMixin} 类注释。
 */
@Mixin(LivingEntity.class)
public class WI_RadioactiveHurtMixin {

	/** 伤害落地后执行反向传染。 */
	@Inject(
			method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
			at = @At("RETURN"))
	private void wi$radioactiveSpread(ServerLevel level, DamageSource source, float damage,
			CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue()) {
			// 伤害被无敌帧 / 免疫 / 护盾全挡下：不算「打中了」，不传染。
			return;
		}
		Entity attacker = source.getEntity();
		if (!(attacker instanceof LivingEntity livingAttacker)) {
			// 环境伤害（摔落、仙人掌…）没有造成伤害者，但依然算「非近战」，
			// 所以这里不能直接 return —— 药水云那一路不需要 attacker。
			WI_RadioactiveAttackHandler.spreadOnAttack(null, (LivingEntity) (Object) this, source);
			return;
		}
		// this 是被打中的生物（传染源）；攻击者才是近战那一路的接受方。
		WI_RadioactiveAttackHandler.spreadOnAttack(livingAttacker, (LivingEntity) (Object) this, source);
	}
}
