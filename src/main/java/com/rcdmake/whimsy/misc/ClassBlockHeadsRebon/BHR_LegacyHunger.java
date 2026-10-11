package com.rcdmake.whimsy.misc.ClassBlockHeadsRebon;

import net.minecraft.server.MinecraftServer;

/**
 * 方块愚者（BHR）的<b>传统（Minecraft 1.8 水平）饥饿消耗</b>数值表。
 *
 * <p>本类只提供数值，不含任何逻辑：规则查询在 {@link BHR_ExpertMode}，
 * 真正把原版调用点改成这些值的是
 * {@code com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon.BHR_LegacyHungerMixin}。
 *
 * <h2>⛔ 为什么不能改 {@code FoodConstants} 的字段（"冷记录"为什么是空操作）</h2>
 * 26.2 确实把消耗值集中到了 {@code net.minecraft.world.food.FoodConstants}：
 * <pre>
 * EXHAUSTION_WALK        = 0.0
 * EXHAUSTION_CROUCH      = 0.0
 * EXHAUSTION_SWIM        = 0.01
 * EXHAUSTION_MINE        = 0.005
 * EXHAUSTION_JUMP        = 0.05
 * EXHAUSTION_SPRINT_JUMP = 0.2
 * EXHAUSTION_ATTACK      = 0.1
 * EXHAUSTION_SPRINT      = 0.1
 * </pre>
 * 但它们是 {@code public static final float} —— <b>编译期常量会被 javac 内联</b>，
 * 字面量副本直接写进了每个调用点的字节码。逐字节核对过：全 jar 837 个原版类里，
 * <b>除了 {@code FoodConstants} 自己，没有任何一个类持有对它的引用</b>。
 * <p>结论：运行时<b>没有任何人读那个字段</b>，改它没有任何效果，也<b>不存在"可记录的原版值"</b>。
 * 因此本表的数值全部写死在源码里 —— 这本身就是那份"冷记录"：
 * 不依赖、也不读取任何可能被其他模组改动的原版可变状态。
 *
 * <h2>为什么必须逐条换，不能用一个乘数</h2>
 * 1.8 → 1.9/1.11 的改动比例并不统一：游泳 ×1.5、破坏方块 ×5、跳跃 ×4、疾跑跳 ×4、
 * 攻击 ×3、饥饿 ×100（见下）。所以只能在每个消耗点各写各的目标值。
 *
 * <h2>每个数值的来源（对应 1.8 → 现版本的哪次改动）</h2>
 * <table border="1">
 *   <caption>数值表</caption>
 *   <tr><th>条目</th><th>26.2 现值</th><th>1.8 目标值</th><th>改动版本</th></tr>
 *   <tr><td>游泳（每米）</td><td>0.01</td><td><b>0.015</b></td><td>1.11 下调</td></tr>
 *   <tr><td>破坏方块（每方块）</td><td>0.005</td><td><b>0.025</b></td><td>1.11 下调</td></tr>
 *   <tr><td>跳跃（每次）</td><td>0.05</td><td><b>0.2</b></td><td>1.11 下调</td></tr>
 *   <tr><td>疾跑跳（每次）</td><td>0.2</td><td><b>0.8</b></td><td>1.11 下调</td></tr>
 *   <tr><td>攻击（每次）</td><td>0.1</td><td><b>0.3</b></td><td>1.11 下调</td></tr>
 *   <tr><td>潜行（每米）</td><td>0.0</td><td><b>0.01</b></td><td>1.9 把潜行改成步行的一半
 *       （0.01 → 0.005），<b>1.11 起连步行一起归零</b>；26.2 里潜行分支压的是 {@code fconst_0}</td></tr>
 *   <tr><td>步行（每米）</td><td>0.0</td><td><b>0.01</b></td><td><b>1.11 起步行不再影响消耗</b>；
 *       1.8 是 0.01/m（1.9 那条「潜行改为步行的一半」正说明当时两者同值）；
 *       26.2 里步行分支同样压 {@code fconst_0}</td></tr>
 *   <tr><td>饥饿效果（每秒）</td><td>0.1</td><td><b>0.5</b></td><td>1.11 下调到 1/5</td></tr>
 *   <tr><td>疾跑（每米）</td><td>0.1</td><td>0.1</td><td><b>不变</b> —— 1.9 改的是潜行、1.11 改的是步行，
 *       疾跑从未被这两次改动碰过，所以 {@code EXHAUSTION_SPRINT} 保持原样、不注入</td></tr>
 * </table>
 *
 * <h2>受伤消耗：刻意不动</h2>
 * 26.2 的受伤消耗是<b>按伤害类型数据驱动</b>的（{@code DamageType.exhaustion} 字段，
 * 经 {@code DamageSource.getFoodExhaustion()} 读出来），1.8 是固定 0.3。
 * 本次按需求<b>不回退</b>这一项，所以 {@code Player.actuallyHurt} 不注入 ——
 * 也就是说开着规则时，受伤消耗仍随数据包/模组定义的伤害类型走。
 */
public final class BHR_LegacyHunger {

	// ── 层 1 ｜ 规格常量（1.8 目标值）──

	/** 游泳 / 水下行走 / 水面行走：每米消耗（1.11 起是 0.01）。 */
	public static final float LEGACY_SWIM_PER_METER = 0.015F;

	/** 潜行：每米消耗（1.9 起先砍半到 0.005、1.11 起直接归零；1.8 是 0.01，与步行同值）。 */
	public static final float LEGACY_CROUCH_PER_METER = 0.01F;

	/**
	 * 步行：每米消耗。
	 *
	 * <p>1.8 = 0.01/m；1.9 把<b>潜行</b>改成它的一半（0.005/m）—— 那条改动的口径正说明
	 * 1.8 时「步行 = 0.01、潜行 = 0.01」是同一个值；<b>1.11 起步行与潜行一起归零</b>
	 * （26.2 里两条分支压的都是 {@code fconst_0}）。所以这一条要恢复成 0.01。
	 */
	public static final float LEGACY_WALK_PER_METER = 0.01F;

	/** 破坏方块：每个方块消耗（1.11 起是 0.005）。 */
	public static final float LEGACY_MINE_PER_BLOCK = 0.025F;

	/** 普通跳跃：每次消耗（1.11 起是 0.05）。 */
	public static final float LEGACY_JUMP = 0.2F;

	/** 疾跑中跳跃：每次消耗（1.11 起是 0.2，正好是普通跳跃的 4 倍）。 */
	public static final float LEGACY_SPRINT_JUMP = 0.8F;

	/** 攻击：每次消耗（1.11 起是 0.1）。 */
	public static final float LEGACY_ATTACK = 0.3F;

	/** 饥饿效果：<b>每等级每秒</b>消耗（1.11 起是 0.1/等级/秒）。 */
	public static final float LEGACY_HUNGER_PER_LEVEL_PER_SECOND = 0.5F;

	/**
	 * 一 tick 的换算基数。
	 * <p>饥饿效果每个 tick 都会结算（{@code shouldApplyEffectTickThisTick} 恒为 true），
	 * 而"每秒"是 20 tick，所以每 tick 的倍率是 {@code perSecond / 20}。
	 */
	public static final float TICKS_PER_SECOND = 20.0F;

	// ── 层 5 ｜ 构造器 ──

	private BHR_LegacyHunger() {
	}

	// ── 层 8 ｜ 查询 ──

	/**
	 * 移动类消耗的<b>每米速率</b>（按 {@code ServerPlayer.checkMovementStatistics} 里的分支序号）。
	 *
	 * <p>为什么按序号：那一段是 6 个几乎同构的分支（各自 award 一个统计、再结算一次消耗），
	 * Mixin 只能靠 {@code ordinal} 区分。序号与分支的对应关系已经在
	 * {@code BHR_LegacyHungerMixin} 里逐条注明，并与 {@code Stats.*} 锚点核对过。
	 *
	 * @param ordinal 分支序号：0 游泳、1 水下行走、2 水面行走、3 疾跑、4 潜行、5 步行
	 * @return 每米消耗；返回 {@code null} 表示<b>这一条不注入</b>（原版值本来就等于 1.8 的值）
	 */
	public static Float movementRatePerMeter(int ordinal) {
		switch (ordinal) {
			case 0: // 游泳：Stats.SWIM_ONE_CM
			case 1: // 水下行走：Stats.WALK_UNDER_WATER_ONE_CM
			case 2: // 水面行走：Stats.WALK_ON_WATER_ONE_CM
				return LEGACY_SWIM_PER_METER;
			case 4: // 潜行：Stats.CROUCH_ONE_CM
				return LEGACY_CROUCH_PER_METER;
			case 5: // 步行：Stats.WALK_ONE_CM
				return LEGACY_WALK_PER_METER;
			default:
				// 只有 3 = 疾跑（0.1/m）：1.9/1.11 都没动过它，1.8 与现版本一致，刻意不注入。
				return null;
		}
	}

	/**
	 * 饥饿效果的<b>每 tick、每等级</b>消耗。
	 *
	 * <p>1.8 的数值口径是「每等级每秒 0.5」，而原版 1.11+ 是「{@code 0.005f * (等级+1)} 每 tick」。
	 * 这里按需求把<b>基数</b>提到 1.8 的水平（{@code 0.5 / 20 = 0.025} 每等级每 tick），
	 * 并<b>保留原版的等级线性缩放</b>——效果等级越高消耗越多这条设计不变。
	 */
	public static float legacyHungerPerLevelPerTick() {
		return LEGACY_HUNGER_PER_LEVEL_PER_SECOND / TICKS_PER_SECOND;
	}

	/**
	 * 是否开启传统饥饿消耗（{@code bhr_legacy_hunger_consumption} 规则）。
	 * <p>本方法也在 {@link BHR_ExpertMode} 里暴露了一份同名入口给普通调用点用；
	 * 这里保留一份是为了让 Mixin 只依赖本类、不必再引门面类。
	 */
	public static boolean enabled(MinecraftServer server) {
		return server != null && server.getGameRules().get(BHR_GameRules.BHR_LEGACY_HUNGER_CONSUMPTION);
	}
}
