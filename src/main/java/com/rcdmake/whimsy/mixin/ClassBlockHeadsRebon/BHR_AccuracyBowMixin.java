package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_Breath;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 弓：屏息满 2 秒后把散布压成 0。
 *
 * <h2>散布是哪来的</h2>
 * <p>26.2 的射弹散布在 {@code Projectile#getMovementToShoot} 里：
 * <pre>
 *   (new Vec3(xd, yd, zd)).normalize()
 *     .add(random.triangle(0, 0.0172275 * uncertainty)   // x
 *        , random.triangle(0, 0.0172275 * uncertainty)   // y
 *        , random.triangle(0, 0.0172275 * uncertainty))  // z
 *     .scale(pow)
 * </pre>
 * 是<b>射弹自己抽的随机数</b>，不是玩家移动造成的（原版弓没有移动散布）。
 * 弓在 {@code BowItem#releaseUsing} 里传的是 {@code uncertainty = 1.0F}（满蓄力也还是 1.0），
 * 所以「固定抛物线」= 把 {@code uncertainty} 换成 0 —— 方向只剩朝角，轨迹由速度 + 重力 + 阻力决定。
 */
@Mixin(BowItem.class)
public abstract class BHR_AccuracyBowMixin {

	@Redirect(
			method = "shootProjectile",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/projectile/Projectile;shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V"))
	private void whimsy$accurateShot(Projectile projectile, Entity source, float xRot, float yRot,
			float yOffset, float power, float uncertainty) {
		float actual = uncertainty;
		if (source instanceof ServerPlayer player && BHR_Breath.hasMaxAccuracy(player)) {
			actual = 0.0F;
		}
		projectile.shootFromRotation(source, xRot, yRot, yOffset, power, actual);
	}
}
