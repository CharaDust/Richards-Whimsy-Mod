package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_Sleep;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 让愚者模式的睡眠不被「当前维度的床位规则」叫醒。
 *
 * <p>26.2 的 {@link Player#tick()} 每 tick 复查一次：
 * <pre>
 *   if (!level.isClientSide() &amp;&amp; !bedRule.canSleep(level)) stopSleepInBed(false, true);
 * </pre>
 * 这条规则对愚者必须失效，否则：
 *
 * <ul>
 *   <li><b>白天下床</b>：overworld 的规则是 {@code when_dark}，白天 {@code isDarkOutside()} 为 false
 *       → 躺下下一 tick 就被弹起来；</li>
 *   <li><b>下界/末地强制睡眠</b>：那里的规则是 {@code never} → 永远站不住。</li>
 * </ul>
 *
 * <p>只影响 {@code Player#tick} 里的这一处调用，其它地方（入睡判定、床方块）保持原版。
 */
@Mixin(Player.class)
public abstract class BHR_PlayerSleepMixin {

	@Redirect(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/attribute/BedRule;canSleep(Lnet/minecraft/world/level/Level;)Z"))
	private boolean whimsy$foolNeverGetsKickedOut(BedRule rule, Level level) {
		if ((Object) this instanceof ServerPlayer serverPlayer && BHR_Sleep.isFool(serverPlayer)) {
			return true;
		}
		return rule.canSleep(level);
	}
}
