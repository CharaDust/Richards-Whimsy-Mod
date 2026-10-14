package com.rcdmake.whimsy.mixin.ClassWhimsyIdea;

import com.rcdmake.whimsy.statusEffect.ClassWhimsyIdea.WI_BioToxicityAttackHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * <b>生物毒性（wi_bio_toxicity）的正向传染</b>挂点：
 * 挂上生物毒性的生物打中别人时，把低一级、半时长的同名效果传给目标。
 *
 * <h2>⭐ 为什么 WI 另写一个 mixin，而不是复用 RDT 那个（模块解耦）</h2>
 * <p>本项目约定：「各模块之间需要解耦，即该模块功能禁止使用其他模块提供的方法」。
 * 而 RDT 模块的 {@code RDT_ImbueHurtMixin} 已经注入在同一个原版方法
 * {@code LivingEntity.hurtServer} 上了。于是这里出现一个取舍：
 * <ul>
 *   <li><b>复用同一个 mixin</b> → 少一份样板，但 RDT 与 WI 从此被绑在一根绳上：
 *       删掉/改写 RDT 会牵动 WI，改 WI 的传染规则也要动 RDT 的文件。
 *       这正是约定要禁止的事。</li>
 *   <li><b>各写一个 mixin（本次选择）</b> → 两个 mixin 类注入同一目标方法。</li>
 * </ul>
 * <p><b>两处注入同一方法是允许的</b>：Mixin 支持多个配置、多个 mixin 类注入同一个目标方法，
 * 只要各处理方法的名字不冲突即可——{@code rdt$…} 与 {@code wi$…} 两个前缀就是为此而设。
 * 两个处理器彼此<b>零引用</b>，可独立演进、独立删除。
 *
 * <h2>挂点选择的理由（与 RDT 那边同源，此处独立复核过）</h2>
 * <p>26.2 的攻击结算路径是分散的：玩家走 {@code Player.attack} / {@code Player.stabAttack}，
 * 怪物走 {@code Mob.doHurtTarget}，而 {@code Creeper} / {@code IronGolem} 这类覆写后干脆自己结算。
 * 唯一共同下游是 {@code LivingEntity.hurtServer}（伤害真正落地的唯一入口）。
 * 其方法体在「无敌帧内伤害不足以突破」时<b>提前 return false</b>（逐行读过），
 * 所以 {@code @At("RETURN")} 处读到的返回值就是「这次伤害是否真的落地了」——
 * 用它判定「打中了」，不会把被无敌帧吃掉的攻击误算成一次传染。
 */
@Mixin(LivingEntity.class)
public class WI_BioToxicityHurtMixin {

	/** 伤害落地后执行正向传染。 */
	@Inject(
			method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
			at = @At("RETURN"))
	private void wi$bioToxicitySpread(ServerLevel level, DamageSource source, float damage,
			CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue()) {
			// 伤害被无敌帧 / 免疫 / 护盾全挡下：不算「打中了」，不传染。
			return;
		}
		Entity attacker = source.getEntity();
		if (!(attacker instanceof LivingEntity livingAttacker)) {
			return;
		}
		// this 是被打中的那个生物（LivingEntity 的 mixin）。
		WI_BioToxicityAttackHandler.spreadOnAttack(livingAttacker, (LivingEntity) (Object) this);
	}
}
