package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_LegacyHunger;

/**
 * <b>传统饥饿消耗</b>（{@code richards-whimsy-mod:bhr_legacy_hunger_consumption}）的注入点总说明。
 *
 * <p>本类<b>不是 Mixin</b>（没有 {@code @Mixin}、也不在 {@code richards-whimsy-mod.mixins.json}
 * 里登记），只是一份放在 mixin 包里的说明页 —— 因为这一批注入点被拆到了四个类，
 * 理由与 ordinal 约定总得有个地方写清楚。
 *
 * <h2>四个实现类</h2>
 * <table border="1">
 *   <caption>拆分与各自的条目</caption>
 *   <tr><th>类</th><th>目标</th><th>条目（26.2 → 1.8）</th></tr>
 *   <tr><td>{@link BHR_LegacyHungerMovementMixin}</td><td>{@code ServerPlayer}</td>
 *       <td>游泳/水下行走/水面行走 0.01→0.015 每米；潜行与步行 0.0→0.01 每米；
 *           跳跃 0.05→0.2 每次；疾跑跳 0.2→0.8 每次</td></tr>
 *   <tr><td>{@link BHR_LegacyHungerBlockMixin}</td><td>{@code Block}</td>
 *       <td>破坏方块 0.005→0.025 每方块</td></tr>
 *   <tr><td>{@link BHR_LegacyHungerAttackMixin}</td><td>{@code Player}</td>
 *       <td>攻击 0.1→0.3 每次（普通攻击与刺击两条路径）</td></tr>
 *   <tr><td>{@link BHR_LegacyHungerEffectMixin}</td><td>{@code HungerMobEffect}</td>
 *       <td>饥饿效果 0.1→0.5 每等级每秒</td></tr>
 * </table>
 *
 * <h2>⛔ 为什么必须拆、为什么只能注入</h2>
 * <ol>
 *   <li><b>必须注入</b>：这些值都是 {@code FoodConstants} 里的 {@code public static final float}，
 *       被 javac 内联成各调用点的字面量 —— 全 jar 除 {@code FoodConstants} 自己没有任何类引用它，
 *       所以改字段无效、也<b>没有"原版值"可冷记录</b>。详见 {@link BHR_LegacyHunger}。</li>
 *   <li><b>必须一个 @Mixin 挂一个目标类</b>：类级 {@code @Mixin({A, B, ...})} 会把
 *       <b>每一个</b> {@code @Inject} 都尝试应用到<b>每一个</b>目标类上。实测（2026-10，mixin 0.8.7）
 *       最初把 {@code ServerPlayer}/{@code Block}/{@code Player}/{@code HungerMobEffect} 写在一个
 *       {@code @Mixin} 里，结果是整个 mixin 失效，日志报
 *       「{@code @Redirect annotation on bhr$swim could not find any targets matching
 *       'checkMovementStatistics(DDD)V' in net/minecraft/world/level/block/Block}」——
 *       也就是把 {@code ServerPlayer} 的方法名拿到 {@code Block} 里去找了。
 *       所以现在每个目标类一个 mixin 类，并用 {@code @Mixin(targets = "…")} 字符串形式
 *       （类字面量形式会触发 Mixin 对「多目标类级 @Mixin」的 INFO 告警）。</li>
 *   <li><b>{@code HungerMobEffect} 还有个额外限制</b>：它是包级私有类，
 *       连 {@code import} 都不行，只能 {@code targets} 字符串。</li>
 * </ol>
 *
 * <h2>刻意不动的两处</h2>
 * <ul>
 *   <li><b>疾跑</b>（{@code Stats.SPRINT_ONE_CM} 分支，0.1/m）：1.9 改的是潜行、1.11 改的是步行，
 *       疾跑从未被这两次改动碰过，1.8 也是 0.1/m；</li>
 *   <li><b>受伤消耗</b>（{@code Player.actuallyHurt}）：26.2 按伤害类型数据驱动
 *       （{@code DamageType.exhaustion} 经 {@code DamageSource.getFoodExhaustion()} 读出），
 *       本次按需求<b>不回退</b>，仍随数据包/模组定义的伤害类型走。</li>
 * </ul>
 * <p>顺带一提：<b>步行不是"不变"</b>。26.2 里步行分支压的是 {@code fconst_0}，
 * 那是 <b>1.11「潜行与步行不再影响消耗度」</b>的结果；1.8 的步行是 0.01/m
 * （1.9 那条「潜行改为步行的一半」正说明当时两者同值），所以它<b>要注入</b>。
 *
 * <h2>ordinal 的来历（最容易失效的地方）</h2>
 * {@code ServerPlayer.checkMovementStatistics} 里那几个分支几乎同构，只能靠 ordinal 区分。
 * 序号按「结算消耗」的指令顺序排列，并与 {@code Stats.*} 锚点逐条核对过：
 * <ol start="0">
 *   <li>{@code Stats.SWIM_ONE_CM}（游泳）</li>
 *   <li>{@code Stats.WALK_UNDER_WATER_ONE_CM}（水下行走）</li>
 *   <li>{@code Stats.WALK_ON_WATER_ONE_CM}（水面行走）</li>
 *   <li>{@code Stats.SPRINT_ONE_CM}（疾跑）—— 不注入，所以潜行落在 ordinal 4</li>
 *   <li>{@code Stats.CROUCH_ONE_CM}（潜行）→ ordinal 4</li>
 *   <li>{@code Stats.WALK_ONE_CM}（步行）→ ordinal 5</li>
 * </ol>
 * 若原版将来重排分支，对应那条会<b>静默失效</b>（{@code require = 0} 的代价，与项目里既有的
 * 三个 RDT Mixin 同策略）。复查办法：把 {@code require} 临时改成 1 跑一次启动，
 * 注入不中会直接报错并指出是哪一个 ordinal。
 * <p><b>本批已经实测核对过</b>（2026-10）：{@code MovementMixin} 的 6 个 ordinal
 * 在 {@code require = 1} 下启动无报错，即全部解析正确；四个目标类也都确认被 Mixin 改写
 * （导出类里都能找到对 {@code *LegacyHunger*} 的引用）。
 *
 * <h2>⚠️ 未在游戏内验证</h2>
 * 本批只做了编译与「注入点是否命中」的核对（用
 * {@code -Dmixin.debug.verbose=true -Dmixin.debug.countInjections=true} 跑专用服务端，
 * 并检查 {@code run/.mixin.out/} 里导出的改写后类），<b>没有实际跑游戏观察饥饿条</b>。
 */
final class BHR_LegacyHungerMixin {

	private BHR_LegacyHungerMixin() {
	}
}
