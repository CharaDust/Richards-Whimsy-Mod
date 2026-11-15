package com.rcdmake.whimsy.client.ClassBlockHeadsRebon;

import net.minecraft.network.chat.Component;

/**
 * 「创建世界」界面里愚者模式下面那个<b>专家模式按钮</b>的三个挡位。
 *
 * <p>它只是一个<b>界面上的选择</b>，不是游戏规则：
 * <ul>
 *   <li>{@link #OFF} / {@link #ON} —— 世界创建成功后<b>执行一次</b>
 *       「关闭 / 开启专家模式」的动作，即把 {@code bhr_expert_mode} 写成
 *       false / true，并照 {@code BHR_GameRules} 的预设表把所有细调条目
 *       一起写成普通 / 专家挡位（等价于创建完世界后手敲一次
 *       {@code /gamerule bhr_expert_mode true|false}）；</li>
 *   <li>{@link #CUSTOM} —— <b>什么都不做</b>。玩家自己进
 *       [游戏规则] 逐条调，界面不替他做任何决定。</li>
 * </ul>
 *
 * <p>默认 {@link #OFF}（每次打开创建世界界面都从 OFF 开始 —— 该字段由
 * {@code CreateWorldScreen} 的 Mixin 用 {@code @Unique} 字段承载，随界面一起生灭，
 * 不是静态全局状态，所以不存在「上次选的开还留着」）。
 *
 * <h2>与 [游戏规则] 里那条「专家模式」的关系</h2>
 * 游戏规则界面里那条 {@code bhr_expert_mode} 只代表<b>世界创建完那一刻的状态</b>，
 * 它不负责「进去之后执行什么动作」。本枚举的 ON/OFF 才是「创建时执行一次动作」
 * 的载体 —— 两者最终落到同一个规则值上，但语义不同：一个是初值，一个是动作。
 *
 * <p>语言键：{@code selectWorld.expertMode.<off|on|custom>} 与加后缀
 * {@code .tooltip} 的悬浮文案（中英各一套，见 {@code BHR_LangEntries}）。
 */
public enum BHR_ExpertModeChoice {

	/** 关（默认）：不写专家挡位。 */
	OFF("selectWorld.expertMode.off"),

	/** 开：把预设表写成专家挡位。 */
	ON("selectWorld.expertMode.on"),

	/** 自定义：什么都不做，交给 [游戏规则]。 */
	CUSTOM("selectWorld.expertMode.custom");

	private final Component displayName;
	private final Component tooltip;

	BHR_ExpertModeChoice(final String langKey) {
		this.displayName = Component.translatable(langKey);
		this.tooltip = Component.translatable(langKey + ".tooltip");
	}

	/** 按钮上显示的文字（关 / 开 / 自定义）。 */
	public Component getDisplayName() {
		return this.displayName;
	}

	/** 悬停在按钮上时的说明文字。 */
	public Component getTooltip() {
		return this.tooltip;
	}
}
