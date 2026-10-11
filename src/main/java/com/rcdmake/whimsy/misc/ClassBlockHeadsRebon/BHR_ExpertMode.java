package com.rcdmake.whimsy.misc.ClassBlockHeadsRebon;

import net.minecraft.server.MinecraftServer;

/**
 * 方块愚者（BHR）游戏规则的<b>对内只读门面</b>。
 *
 * <p>本类存在的理由：让「规则 id 是什么、存在哪、默认值多少」这些细节不要散落到每个调用点。
 * 别的模块或实体、方块方法想知道「现在是不是专家挡位」，只读一个方法名即可：
 *
 * <pre>{@code
 * if (BHR_ExpertMode.leavesNoDrop(server)) { ... }   // 新代码这样写
 * }</pre>
 *
 * <h2>只读，绝不写入</h2>
 * 本类<b>没有任何 set 方法</b>，也没有暴露 {@code GameRule} 常量。
 * 改规则只有两个正当入口：玩家的 {@code /gamerule} 命令，以及
 * {@link BHR_GameRules} 里那个「专家模式开关 → 预设挡位」协调器。
 * 调用点越多、写入口越多，规则状态就越难推理。
 *
 * <h2>为什么收 {@code MinecraftServer} 而不是 {@code Level}</h2>
 * 26.2 里 {@code ClientLevel} <b>没有</b> {@code getGameRules()}，只有 {@code ServerLevel} 和
 * {@code MinecraftServer} 有；而 {@code ServerLevel.getGameRules()} 的内部实现就是
 * {@code this.server.getGameRules()}（逐字节读过）—— 也就是<b>全局唯一的那一份规则对象</b>。
 * 所以这里直接收 {@code MinecraftServer}：只用服务端类型，编译期就杜绝了
 * 「在客户端调到一个永远读不到规则的方法」这种坑，也不需要为「每维度一份」做任何假设。
 *
 * <h2>规则之间的独立性</h2>
 * 约定：{@code bhr_expert_mode} 只是个<b>批量调节工具</b>，不是「总开关」。
 * 所以判定点应尽量读<b>具体条目</b>（如 {@link #leavesNoDrop}），而不是读
 * {@link #expertMode} —— 读了后者，玩家单独关掉某条细调条目就会失效。
 *
 * @see BHR_GameRules 规则本体与预设协调器
 */
public final class BHR_ExpertMode {

	// ── 层 5 ｜ 构造器 ──

	private BHR_ExpertMode() {
	}

	// ── 层 8 ｜ 查询 ──

	/**
	 * 专家模式开关当前是否为 TRUE。
	 * <p>一般<b>不要</b>用它做判定 —— 判定应该读具体条目，理由见类注释「规则之间的独立性」。
	 * 它主要用于提示文案、调试输出等「关心开关本身」的场合。
	 */
	public static boolean expertMode(MinecraftServer server) {
		return server.getGameRules().get(BHR_GameRules.BHR_EXPERT_MODE);
	}

	/**
	 * 枯萎树叶是否「直接消失、不掉落」（{@code bhr_decaying_leaves_no_drop}）。
	 * <p>判定点：{@code BHR_TreeLeavesPine} 的消失流程分流
	 * （{@code isHardcore() || leavesNoDrop(server)}）。
	 */
	public static boolean leavesNoDrop(MinecraftServer server) {
		return server.getGameRules().get(BHR_GameRules.BHR_DECAYING_LEAVES_NO_DROP);
	}

	/**
	 * 是否启用传统（1.8 水平）饥饿值消耗（{@code bhr_legacy_hunger_consumption}）。
	 *
	 * <p>⚠️ <b>机制已经实现</b>，但实现落在 Mixin 里、并不经过本方法：这些消耗值是
	 * {@code FoodConstants} 的编译期内联常量，只能在每个调用点注入。
	 * 真正读这条规则的是
	 * {@code com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon.BHR_LegacyHunger*} 那一批类；
	 * 它们为了只依赖数值表，直接调 {@link BHR_LegacyHunger#enabled}。
	 * <p>本方法留给「非注入点」的调用者用（例如将来某个方块/工作方块想按这条规则改行为），
	 * 语义与 {@link BHR_LegacyHunger#enabled} 完全一致。
	 */
	public static boolean legacyHunger(MinecraftServer server) {
		return server.getGameRules().get(BHR_GameRules.BHR_LEGACY_HUNGER_CONSUMPTION);
	}
}
