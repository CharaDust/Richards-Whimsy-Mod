package com.rcdmake.whimsy.mixin.ClassRandomThings;

import com.rcdmake.whimsy.block.ClassRandomThings.RDT_SuperLubricentIce;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 超级滑滑冰的「滑行不减速」（{@code rdt_super_lubricent_ice}）。
 *
 * <h2>为什么单靠方块属性做不到</h2>
 * 26.2 里实体的水平衰减在 {@code LivingEntity.travelInAir} 的尾部（逐字节读过）：
 * <pre>{@code
 * float f3    = onGround() ? computeModifiedFriction(block.getFriction(), FRICTION_MODIFIER属性) : 1.0f;
 * float dragA = computeModifiedFriction(0.91f, AIR_DRAG_MODIFIER属性);   // 属性默认 1.0 → 0.91
 * float horiz = f3 * dragA;                                              // 每 Tick 的水平速度乘数
 * setDeltaMovement(v.x * horiz, v.y * 竖直流阻, v.z * horiz);
 *
 * // 其中（同样是读出来的字节码）：
 * computeModifiedFriction(a, b) = Mth.clamp(1.0f - (1.0f - a) * b, 0.0f, 1.0f);
 * }</pre>
 * 想「不改变速度」就要 {@code f3 × 0.91 = 1}，即 {@code f3 = 1/0.91 = 1.0989} —— 这正是旧代码
 * {@code slipperiness(1.0989f)} 的来历。但 26.2 新增的 {@code computeModifiedFriction} 会把结果
 * <b>clamp 到 [0,1]</b>，于是方块属性那条路最高只能到 {@code friction(1.0F)}，
 * 每 Tick 仍要乘 0.91（掉 9% 速度）。这就是「旧代码的补偿方案现在失效」的原因。
 *
 * <h2>本 Mixin 做了什么</h2>
 * 只改一个调用点：{@code travelInAir} 里<b>第一次</b>出现的 {@code computeModifiedFriction}
 * （即算地面摩擦 f3 的那次，{@code ordinal = 0}，后面两次是竖直/空气阻力，绝不能动）。
 * 当实体脚下的方块是超级滑滑冰时，直接返回 {@code 1.0989f}，让 {@code f3 × 0.91 = 1.0} —— 水平速度不再衰减；
 * 其余情况原样重算（公式与 {@code computeModifiedFriction} 完全一致）。
 *
 * <p>顺带说明：{@code f3} 还会进入 {@code getFrictionInfluencedSpeed}（26.2 里
 * {@code friction > 0.6} 时输入加速度按 {@code 0.216 / f³} 缩小），所以站在本方块上时
 * 起步加速很弱、但一旦滑起来就不掉速 —— 这正是「滑行」的手感。
 *
 * <h2>⚠️ 未在游戏内验证</h2>
 * {@code require = 0} 是刻意的：万一 26.2 的 {@code travelInAir} 结构在后续小版本里变了，
 * 也只是这条注入不生效（日志里会有 Mixin 的提示），不会让游戏崩。
 * 若你发现方块没有变滑，先看日志有没有「RDT_SuperLubricentIceFrictionMixin ... 0 succeeded」之类的字样。
 * 想去掉这个行为：删掉本类 + {@code richards-whimsy-mod.mixins.json} 与 {@code fabric.mod.json} 里对应的两处登记即可，
 * 方块会退回 {@code friction(1.0F)} 的表现（仍是原版最滑那一档）。
 */
@Mixin(LivingEntity.class)
public abstract class RDT_SuperLubricentIceFrictionMixin {

	/** 让 f3 × 0.91 = 1.0（= 1 / 0.91）。 */
	private static final float NO_DECAY_FRICTION = 1.0989F;

	@Redirect(
			method = "travelInAir(Lnet/minecraft/world/phys/Vec3;)V",
			require = 0,
			at = @At(
					value = "INVOKE",
					ordinal = 0,
					target = "Lnet/minecraft/world/entity/LivingEntity;computeModifiedFriction(FF)F"))
	private float whimsy$slideWithoutDecay(float blockFriction, float frictionModifier) {
		LivingEntity self = (LivingEntity) (Object) this;
		BlockState below = self.level().getBlockState(self.getBlockPosBelowThatAffectsMyMovement());
		if (below.is(RDT_SuperLubricentIce.RDT_SUPER_LUBRICENT_ICE)) {
			return NO_DECAY_FRICTION;
		}
		// 与 computeModifiedFriction 的公式保持一致（clamp(1 - (1-a)*b, 0, 1)）
		return Mth.clamp(1.0F - (1.0F - blockFriction) * frictionModifier, 0.0F, 1.0F);
	}
}
