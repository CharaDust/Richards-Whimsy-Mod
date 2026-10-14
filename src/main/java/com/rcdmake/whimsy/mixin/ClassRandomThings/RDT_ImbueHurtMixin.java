package com.rcdmake.whimsy.mixin.ClassRandomThings;

import com.rcdmake.whimsy.statusEffect.ClassRandomThings.RDT_ImbueAttackHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让<b>所有生物</b>的灌注状态效果都能打出来（火焰灌注 / 毒性灌注）。
 *
 * <h2>背景</h2>
 * <p>1.20.4 旧实现把灌注挂在 {@code AttackEntityCallback}（「玩家左键实体」的事件）上，
 * 于是只有玩家能享受灌注，怪物身上挂着同样的效果却打不出燃烧/中毒。
 * 本 mixin 按「伤害落地的唯一必经之路」重新挂钩，使玩家、怪物、以及通过宠物/投射物
 * 造成的攻击一律生效。
 *
 * <h2>为什么挂在 {@code LivingEntity.hurtServer}（而不是各攻击方法）</h2>
 * <p>26.2 的攻击结算路径是<b>分散</b>的，逐条注入都不完整：
 * <ul>
 *   <li>{@code Player.attack(Entity)} / {@code Player.stabAttack(...)}——玩家两条路径；</li>
 *   <li>{@code Mob.doHurtTarget(ServerLevel, Entity)}——怪物基类实现，<b>但</b>
 *       {@code Creeper} 覆写后直接 {@code return true}（并不造成伤害）、
 *       {@code IronGolem} / {@code Bee} 覆写后自己调 {@code target.hurtServer(...)}，
 *       完全不经过 {@code super}。逐类注入等于遗漏一片；</li>
 *   <li>而上面<b>每一条</b>最终都会调用 {@code LivingEntity.hurtServer(ServerLevel, DamageSource, float)}
 *       ——原版「实际结算伤害」的唯一入口。</li>
 * </ul>
 * 所以这里只挂一个注入点：{@code @At("RETURN")} 处读返回值，返回 true 即「这次伤害真的落地了」，
 * 再从 {@link DamageSource#getEntity()} 反查攻击者。伤害源为 null（摔落、溺水、仙人掌等环境伤害）
 * 时自然不触发，不需要额外的白名单。
 *
 * <h2>能力边界（刻意留着，不当 bug 查）</h2>
 * <ul>
 *   <li>判定口径是「<b>该生物造成的伤害</b>」，因此不只近战：它射出的箭、它引爆的 TNT
 *       同样会带上灌注效果。这比 wiki 里「攻击其他生物」的字面意图略宽一层；
 *       换来的好处是<b>不漏任何一种攻击</b>（近战、投射物、爆炸、特殊攻击一视同仁）。
 *       若将来要收紧成纯近战，正确做法是按 {@code DamageSource} 的伤害类型标签
 *       （{@code DamageTypeTags.IS_PROJECTILE} / {@code IS_EXPLOSION}）做排除，
 *       而不是回头去逐类注入攻击方法。</li>
 *   <li>挂在 {@code hurtServer} 上意味着自身伤害也会经过这里——所以处理器里挡掉了
 *       「攻击者 == 目标」的自伤；反伤（荆棘）会以受伤者自身作为攻击者，因同一条判断一并挡掉。</li>
 * </ul>
 */
@Mixin(LivingEntity.class)
public class RDT_ImbueHurtMixin {

	/**
	 * 伤害落地后补上灌注效果。
	 *
	 * <p>{@code method} 写全描述符，避免与同名重载产生歧义。
	 */
	@Inject(
			method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
			at = @At("RETURN"))
	private void rdt$imbueOnHurt(ServerLevel level, DamageSource source, float damage,
			CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue()) {
			// 伤害被无敌帧 / 免疫 / 护盾全挡下：不算「打中了」，不触发灌注。
			return;
		}
		Entity attacker = source.getEntity();
		// 只有「造成伤害的生物」才谈得上灌注；箭/火球这类查出来的 owner 也算攻击者。
		if (!(attacker instanceof LivingEntity livingAttacker)) {
			return;
		}
		// this 就是被打中的那个生物（LivingEntity 的 mixin）。
		RDT_ImbueAttackHandler.imbueOnAttack(livingAttacker, (LivingEntity) (Object) this);
	}
}
