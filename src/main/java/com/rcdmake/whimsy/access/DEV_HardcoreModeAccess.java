package com.rcdmake.whimsy.access;

/**
 * 「能改 hardcore 的世界数据」接口（duck interface，DEV 的极限模式切换器用）。
 *
 * <p>原版把 hardcore 存在 {@code PrimaryLevelData} 的私有 {@code settings} 字段
 * （{@code LevelSettings}）里，而 {@code LevelSettings} 只提供
 * {@code withDifficulty / withDifficultyLock / withGameType / withAllowCommands / withDataConfiguration}
 * 这几个 wither —— <b>没有 {@code withHardcore}，{@code PrimaryLevelData} 也没有 setter</b>。
 * 26.2 全树搜过：`isHardcore()` 出现在十几个类里，`setHardcore` 一个都没有。
 * 想在游戏里切换它，只能自己往 {@code PrimaryLevelData} 上挂一个写入口 —— 就是本接口，
 * 由 {@code com.rcdmake.whimsy.mixin.ClassDEV.DEV_HardcoreModeMixin} 实现。
 *
 * <p>用法：
 * <pre>{@code
 * if (server.getWorldData() instanceof DEV_HardcoreModeAccess access) {
 *     access.setHardcoreMode(true);
 * }
 * }</pre>
 *
 * <ul>
 *   <li>必须从 {@code MinecraftServer.getWorldData()} 取对象：{@code ServerLevel.getLevelData()}
 *       返回的是 {@code DerivedLevelData}（只读转发，它自己的 {@code isHardcore()} 会转给 primary），
 *       并不实现本接口；</li>
 *   <li>只改 hardcore 一个属性 —— {@code allowCommands}（作弊开关）与 {@code gameType}（创造/生存）
 *       原样复制，难度也不动。</li>
 * </ul>
 *
 * <h2>⛔ 为什么这个接口在 {@code access} 包、而不跟 Mixin 放一起</h2>
 * <b>本接口最初放在 {@code com.rcdmake.whimsy.mixin.ClassDEV} 下，结果一进存档就崩：</b>
 * <pre>
 * java.lang.RuntimeException: Mixin transformation of ...DEV_HardcoreModeAccess failed
 * Caused by: IllegalClassLoadError: com.rcdmake.whimsy.mixin.ClassDEV.DEV_HardcoreModeAccess
 *   is in a defined mixin package com.rcdmake.whimsy.mixin.* owned by
 *   richards-whimsy-mod.mixins.json and cannot be referenced directly
 * </pre>
 * 原因：Mixins 配置里的 {@code package} 声明的是「这一整个包（含子包）都是 mixin」，
 * 包内<b>任何</b>被当普通类加载的类都会被拒绝 —— 包括被 mixin 类 {@code implements} 的接口。
 * 所以：<b>duck 接口 / 常量 / 工具类一律放 mixin 包外</b>（本模组统一放 {@code com.rcdmake.whimsy.access}），
 * mixin 包里只留真正的 {@code @Mixin} 类。
 */
public interface DEV_HardcoreModeAccess {

	/**
	 * 切换 hardcore（只改这一个属性）。
	 *
	 * @param hardcore 目标值，{@code true} = 极限模式
	 */
	void setHardcoreMode(boolean hardcore);
}
