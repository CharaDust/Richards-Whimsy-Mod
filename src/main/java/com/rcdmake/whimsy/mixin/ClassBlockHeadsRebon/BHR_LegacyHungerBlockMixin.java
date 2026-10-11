package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_LegacyHunger;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * <b>破坏方块</b>的饥饿消耗回退：{@code 0.005/方块} → 1.8 的 {@code 0.025/方块}。
 *
 * <p>数值表与背景见 {@link BHR_LegacyHunger}；同批其它条目见
 * {@code BHR_LegacyHungerMovementMixin} / {@code BHR_LegacyHungerAttackMixin} /
 * {@code BHR_LegacyHungerEffectMixin}。
 *
 * <h2>为什么是 {@code @Redirect} 而不是 {@code @ModifyArg}</h2>
 * 这一处只有一个实参可改，本来 {@code @ModifyArg} 更贴切，但实测（mixin 0.8.7）它报
 * 「targets a method with an invalid signature (F), expected (FLnet/…Level;L…Player;…」——
 * {@code @ModifyArg} 只把<b>原方法的参数</b>接到处理方法尾部，拿不到 {@code @At} 目标方法的实参；
 * 而 {@code @Redirect} 的签名要求正好是 {@code (receiver, 原实参...)}。
 * <p>顺便这个手法还不需要用到「{@code @ModifyArg} 处理方法参数表是
 * {@code [原实参..., 原方法参数...]}」那条容易搞错的规则。
 *
 * <p>⚠️ 用 {@code targets} 字符串而不是类字面量 —— 理由见
 * {@code BHR_LegacyHungerMixin} 的类注释（一个 {@code @Mixin} 只挂一个目标类）。
 */
@Mixin(targets = "net.minecraft.world.level.block.Block")
public class BHR_LegacyHungerBlockMixin {

	/**
	 * 破坏方块时的消耗：{@code 0.005/方块} → {@code 0.025/方块}。
	 * <p>receiver 是被消耗的 {@code Player}（原版调用的是
	 * {@code player.causeFoodExhaustion(...)}），所以规则要去 player 身上取世界。
	 */
	@Redirect(
			method = "playerDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;"
					+ "Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;"
					+ "Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/item/ItemStack;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"),
			require = 0)
	private void bhr$mine(Player player, float original) {
		player.causeFoodExhaustion(bhr$ruleOn(player) ? BHR_LegacyHunger.LEGACY_MINE_PER_BLOCK : original);
	}

	/** 读规则：{@code bhr_legacy_hunger_consumption} 是否开启；取不到服务端就不改行为。 */
	private static boolean bhr$ruleOn(Player player) {
		Level level = player == null ? null : player.level();
		return level != null && BHR_LegacyHunger.enabled(level.getServer());
	}
}
