package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_FoolGameModeIcons;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 愚者模式：把切换器的两个 {@code switch} 补上第五格。
 *
 * <h2>要补什么</h2>
 * {@code GameModeIcon} 里有两处「穷举四种模式」的 switch <b>表达式</b>（没有 default）：
 * <ul>
 *   <li>{@code getNext()} —— F3+F4 循环的下一个格子。原版顺序
 *       创造 → 生存 → 冒险 → 观众 → 创造；我们插进「观众 → 愚者 → 创造」；</li>
 *   <li>{@code getFromGameType(GameType)} —— 当前模式对应哪个格子
 *       （构造器选初始格、以及判断「目标是否等于当前」时用）。</li>
 * </ul>
 * 这两处的 switch 都由 javac 编成「枚举常量 → 分支」的完整映射：遇到第五个常量
 * 没有分支时，Java 21+ 的 switch 表达式会抛 {@code MatchException}。
 * 所以必须在 HEAD 处拦截并给出我们的返回值。
 *
 * <h2>为什么返回值只能写成 {@code Object}</h2>
 * 两处方法的返回类型都是 private 嵌套枚举 {@code GameModeSwitcherScreen$GameModeIcon}，
 * 普通代码写不出这个类型名。取实例的动作交给 {@link BHR_FoolGameModeIcons}（反射按常量名取）。
 */
@Mixin(targets = "net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen$GameModeIcon")
public abstract class BHR_FoolGameModeSwitcherMixin {

	/**
	 * 补 {@code getNext()}：
	 * <ul>
	 *   <li>当前是愚者格 → 下一格回到创造（与整排的最左格对齐）；</li>
	 *   <li>当前是观众格 → 下一格是愚者（否则愚者格永远轮不到）。</li>
	 * </ul>
	 * 其余四格保持原版顺序（不拦）。
	 */
	@Inject(method = "getNext", at = @At("HEAD"), cancellable = true)
	private void whimsy$foolNext(CallbackInfoReturnable<Object> cir) {
		String current = ((Enum<?>) (Object) this).name();

		if (BHR_FoolGameModeIcons.FOOL_CONSTANT.equals(current)) {
			cir.setReturnValue(BHR_FoolGameModeIcons.icon(BHR_FoolGameModeIcons.FIRST_CONSTANT));
		} else if (BHR_FoolGameModeIcons.SPECTATOR_CONSTANT.equals(current)) {
			cir.setReturnValue(BHR_FoolGameModeIcons.icon(BHR_FoolGameModeIcons.FOOL_CONSTANT));
		}
	}

	/** 补 {@code getFromGameType(GameType)}：愚者模式 → 愚者格。 */
	@Inject(method = "getFromGameType", at = @At("HEAD"), cancellable = true)
	private static void whimsy$foolFromGameType(GameType gameType, CallbackInfoReturnable<Object> cir) {
		if (gameType == GameType.BHR_FOOL_MODE) {
			cir.setReturnValue(BHR_FoolGameModeIcons.icon(BHR_FoolGameModeIcons.FOOL_CONSTANT));
		}
	}
}
