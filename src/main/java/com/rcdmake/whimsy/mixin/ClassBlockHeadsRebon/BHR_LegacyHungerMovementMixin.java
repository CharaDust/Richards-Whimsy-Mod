package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_LegacyHunger;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * <b>移动类</b>饥饿消耗回退：游泳 / 水下行走 / 水面行走 / 潜行 / 跳跃 / 疾跑跳。
 *
 * <p>数值表与「为什么必须用 Mixin、为什么"冷记录"是空操作」的完整理由见
 * {@link BHR_LegacyHunger}。同批其它条目见 {@code BHR_LegacyHungerBlockMixin} /
 * {@code BHR_LegacyHungerAttackMixin} / {@code BHR_LegacyHungerEffectMixin}；
 * 拆分理由与 ordinal 约定见 {@code BHR_LegacyHungerMixin}。
 *
 * <h2>⛔ 踩过的坑：千万不要去改 {@code Math.round} 的结果（2026-10 实测翻车）</h2>
 * {@code checkMovementStatistics} 每个分支的形状是
 * <pre>{@code
 * causeFoodExhaustion(EXHAUSTION_XXX * Math.round(位移 * 100) / 100);
 * }</pre>
 * 两个乘数在源码里都是 {@code FoodConstants} 的字段，编译后都被<b>内联成两个字面量</b>。
 * 最初我误以为「后面那个 {@code * 100} / {@code / 100} 是厘米换算」，于是用
 * {@code @ModifyExpressionValue} 去改 {@code Math.round} 的<b>厘米结果</b> —— 那是
 * {@code int}，而处理函数返回的 int 会被 {@code i2f} 变成<b>浮点厘米数</b>，随后又乘了一次速率。
 * <p>症状（玩家实测）：<b>开着规则疾跑反而慢了一个数量级</b>（6.5 秒 → 约 1 分钟每半个鸡腿），
 * 而走路/潜行<b>完全不消耗</b>。原因分别是：
 * <ul>
 *   <li>疾跑分支的「前一个」字段是 {@code EXHAUSTION_SPRINT}（内联 {@code 0.1f}，不是 {@code 0.01f}），
 *       {@code 0.1 × 0.015 = 0.0015}/m，正好是应有的 0.015 的 1/10；</li>
 *   <li>走路/潜行分支的「前一个」字段是 {@code fconst_0}（{@code EXHAUSTION_WALK/CROUCH} = 0.0），
 *       {@code 0 × 任何值 = 0}，改那个中间量根本乘不进去。</li>
 * </ul>
 * <b>所以只能 {@code @Redirect} 整个 {@code causeFoodExhaustion} 调用</b>，按位移自己算；
 * 并且要用原版那条公式（{@code 厘米 = round(位移 × 100)}）重算，才能连舍入细节都一致。
 *
 * <h2>注入点（ordinal 的来历）</h2>
 * 那几个分支几乎同构，只能靠 ordinal 区分。序号按「结算消耗」的指令顺序排列，
 * 并与 {@code Stats.*} 锚点逐条核对过：
 * <ol start="0">
 *   <li>{@code Stats.SWIM_ONE_CM} → 游泳，0.01/m → <b>0.015/m</b></li>
 *   <li>{@code Stats.WALK_UNDER_WATER_ONE_CM} → 水下行走，0.01/m → <b>0.015/m</b></li>
 *   <li>{@code Stats.WALK_ON_WATER_ONE_CM} → 水面行走，0.01/m → <b>0.015/m</b></li>
 *   <li>{@code Stats.SPRINT_ONE_CM} → 疾跑，<b>不注入</b>（1.8 也是 0.1/m）</li>
 *   <li>{@code Stats.CROUCH_ONE_CM} → 潜行，0.0/m → <b>0.01/m</b>（1.9 砍半、1.11 归零）</li>
 *   <li>{@code Stats.WALK_ONE_CM} → 步行，0.0/m → <b>0.01/m</b>（1.11 起不再影响消耗）</li>
 * </ol>
 * 所以潜行是 <b>ordinal 4</b>、步行是 <b>ordinal 5</b>（跳过不注入的疾跑）。
 * <p><b>这 6 个 ordinal 已经实测核对过</b>（2026-10）：把本类的 {@code require} 全部临时
 * 改成 1 跑一次专用服务端，注入不中会直接抛 {@code InjectionError}；多次启动均无报错。
 * 玩家实测也已印证：疾跑恢复原样、潜行与步行开始消耗。
 * <p>⚠️ 注意 ordinal 是「所有 {@code causeFoodExhaustion} 调用点」的序号，
 * 而 {@code Stats.CLIMB_ONE_CM}（爬梯/爬藤）那条分支在这里<b>不结算消耗</b>，
 * 不参与计数 —— 这也是为什么潜行是 4 而不是 5、步行是 5 而不是 6。
 *
 * <h2>⚠️ 仍未在游戏内逐条验证</h2>
 * 疾跑/潜行/步行已由玩家实测确认；游泳与跳跃的数值效果仍只有注入点核对，
 * 没有逐条跑游戏观察饥饿条。
 */
@Mixin(targets = "net.minecraft.server.level.ServerPlayer")
public class BHR_LegacyHungerMovementMixin {

	// ── checkMovementStatistics（按米结算）──

	/** 游泳（ordinal 0）：0.01/m → 0.015/m。 */
	@Redirect(
			method = "checkMovementStatistics(DDD)V",
			at = @At(value = "INVOKE", ordinal = 0,
					target = "Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V"),
			require = 0)
	private void bhr$swim(ServerPlayer self, float original, double dx, double dy, double dz) {
		bhr$movement(self, original, dx, dy, dz, 0);
	}

	/** 水下行走（ordinal 1）：0.01/m → 0.015/m（1.8 与游泳同速率）。 */
	@Redirect(
			method = "checkMovementStatistics(DDD)V",
			at = @At(value = "INVOKE", ordinal = 1,
					target = "Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V"),
			require = 0)
	private void bhr$walkUnderWater(ServerPlayer self, float original, double dx, double dy, double dz) {
		bhr$movement(self, original, dx, dy, dz, 1);
	}

	/** 水面行走（ordinal 2）：0.01/m → 0.015/m。 */
	@Redirect(
			method = "checkMovementStatistics(DDD)V",
			at = @At(value = "INVOKE", ordinal = 2,
					target = "Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V"),
			require = 0)
	private void bhr$walkOnWater(ServerPlayer self, float original, double dx, double dy, double dz) {
		bhr$movement(self, original, dx, dy, dz, 2);
	}

	/** 潜行（ordinal 4，跳过不注入的疾跑那条）：0.0/m → 0.01/m（1.8 与步行同值）。 */
	@Redirect(
			method = "checkMovementStatistics(DDD)V",
			at = @At(value = "INVOKE", ordinal = 4,
					target = "Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V"),
			require = 0)
	private void bhr$crouch(ServerPlayer self, float original, double dx, double dy, double dz) {
		bhr$movement(self, original, dx, dy, dz, 4);
	}

	/**
	 * 步行（ordinal 5）：0.0/m → 0.01/m。
	 *
	 * <p>26.2 里这条分支压的是 {@code fconst_0}，所以「步行不消耗」是原版行为，
	 * 与潜行一样属于 1.11 归零的那一批（1.8 是 0.01/m）。
	 */
	@Redirect(
			method = "checkMovementStatistics(DDD)V",
			at = @At(value = "INVOKE", ordinal = 5,
					target = "Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V"),
			require = 0)
	private void bhr$walk(ServerPlayer self, float original, double dx, double dy, double dz) {
		bhr$movement(self, original, dx, dy, dz, 5);
	}

	// ── jumpFromGround（按次结算）──

	/** 疾跑跳（ordinal 0：{@code isSprinting()} 为真的那条）：0.2/次 → 0.8/次。 */
	@Redirect(
			method = "jumpFromGround()V",
			at = @At(value = "INVOKE", ordinal = 0,
					target = "Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V"),
			require = 0)
	private void bhr$sprintJump(ServerPlayer self, float original) {
		self.causeFoodExhaustion(bhr$ruleOn(self) ? BHR_LegacyHunger.LEGACY_SPRINT_JUMP : original);
	}

	/** 普通跳跃（ordinal 1：{@code else} 分支）：0.05/次 → 0.2/次。 */
	@Redirect(
			method = "jumpFromGround()V",
			at = @At(value = "INVOKE", ordinal = 1,
					target = "Lnet/minecraft/server/level/ServerPlayer;causeFoodExhaustion(F)V"),
			require = 0)
	private void bhr$jump(ServerPlayer self, float original) {
		self.causeFoodExhaustion(bhr$ruleOn(self) ? BHR_LegacyHunger.LEGACY_JUMP : original);
	}

	// ── 私有助手 ──

	/**
	 * 移动类分支的公共实现：按 1.8 的每米速率重算消耗。
	 *
	 * <p>重算时<b>照用原版那条公式</b>，连舍入细节都保持一致：
	 * <pre>{@code
	 * 厘米数 = Math.round(sqrt(dx² + dy² + dz²) * 100)
	 * 消耗   = 每米速率 * 厘米数 / 100
	 * }</pre>
	 * 直接写 {@code 速率 * 位移} 在数学上等价，但过一遍厘米取整才能与原版逐位一致。
	 *
	 * @param ordinal 分支序号，交给 {@link BHR_LegacyHunger#movementRatePerMeter}；
	 *                返回 {@code null} 表示该条不注入（本类的调用点都不会走到）
	 */
	private static void bhr$movement(ServerPlayer self, float original, double dx, double dy, double dz,
			int ordinal) {
		Float rate = BHR_LegacyHunger.movementRatePerMeter(ordinal);
		if (rate == null || !bhr$ruleOn(self)) {
			self.causeFoodExhaustion(original);
			return;
		}
		int centimetres = Math.round((float) Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0F);
		self.causeFoodExhaustion(rate * centimetres / 100.0F);
	}

	/**
	 * 读规则：{@code bhr_legacy_hunger_consumption} 是否开启。
	 * <p>取不到服务端（理论上不该发生：这些路径都在服务端）时返回 {@code false} = 不改行为。
	 */
	private static boolean bhr$ruleOn(ServerPlayer player) {
		return player != null && player.level() != null
				&& BHR_LegacyHunger.enabled(player.level().getServer());
	}
}
