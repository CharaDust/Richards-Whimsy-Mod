package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_Breath;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 三叉戟：屏息满 2 秒后把散布压成 0。
 *
 * <p>原版在 {@code TridentItem#releaseUsing} 里调
 * {@code Projectile.spawnProjectileFromRotation(..., 2.5F, 1.0F)} —— 最后的 {@code 1.0F}
 * 就是散布系数，和三叉戟一样是「射弹自己抽随机数」（见 {@code Projectile#getMovementToShoot}）。
 */
@Mixin(TridentItem.class)
public abstract class BHR_AccuracyTridentMixin {

	@Redirect(
			method = "releaseUsing",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileFromRotation"
							+ "(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;"
							+ "Lnet/minecraft/server/level/ServerLevel;"
							+ "Lnet/minecraft/world/item/ItemStack;"
							+ "Lnet/minecraft/world/entity/LivingEntity;FFF)"
							+ "Lnet/minecraft/world/entity/projectile/Projectile;"))
	private Projectile whimsy$accurateThrow(Projectile.ProjectileFactory<Projectile> creator,
			ServerLevel level, ItemStack stack, LivingEntity source,
			float yOffset, float power, float uncertainty) {
		float actual = uncertainty;
		if (source instanceof ServerPlayer player && BHR_Breath.hasMaxAccuracy(player)) {
			actual = 0.0F;
		}
		return Projectile.spawnProjectileFromRotation(creator, level, stack, source, yOffset, power, actual);
	}
}
