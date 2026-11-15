package com.rcdmake.whimsy.misc.ClassBlockHeadsRebon;

/**
 * F3+F4 游戏模式切换器里「图标格」的反射访问点。
 *
 * <h2>为什么需要反射</h2>
 * 图标格的类型是 {@code GameModeSwitcherScreen$GameModeIcon} —— 一个 <b>private</b>
 * 嵌套枚举。普通类里连这个类型名都写不出来（写出来也编译不过），
 * 而 Mixin 的注入方法又必须能返回它的实例（{@code getNext()} /
 * {@code getFromGameType(GameType)} 的返回类型就是它）。
 *
 * <p>所以两侧夹一层：Mixin 里只出现 {@code Object}，真正的实例由本类按常量名取。
 * 常量名 {@code BHR_FOOL_MODE} 与 classtweaker / Mixin 里的一致。
 *
 * <p>本类只在客户端路径上被调用（那段代码本身只在 {@code GameModeSwitcherScreen} 里跑），
 * 放在 {@code misc} 而不是 mixin 包 —— 混入包里的类不能被普通代码引用
 * （见 {@code DEV_HardcoreModeAccess} 的类注释）。
 */
public final class BHR_FoolGameModeIcons {

	/** 愚者模式图标格的常量名（与 classtweaker 的 extend-enum 一致）。 */
	public static final String FOOL_CONSTANT = "BHR_FOOL_MODE";

	/** 原版图标格的常量名：切换顺序 HEAD 从这里开始绕回。 */
	public static final String FIRST_CONSTANT = "CREATIVE";

	/** 原版观众图标格的常量名：它的「下一个」在愚者模式下要接到愚者格。 */
	public static final String SPECTATOR_CONSTANT = "SPECTATOR";

	private static final String ICON_CLASS_NAME =
			"net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen$GameModeIcon";

	private BHR_FoolGameModeIcons() {
	}

	/**
	 * 按常量名取一个图标格实例。
	 *
	 * @param constantName 枚举常量名，如 {@code BHR_FOOL_MODE}
	 * @return 该常量对应的实例（运行时是 {@code GameModeIcon}，编译期只能是 {@code Object}）
	 * @throws IllegalStateException 常量不存在（说明枚举扩展没生效）
	 */
	public static Object icon(String constantName) {
		try {
			Object[] constants = Class.forName(ICON_CLASS_NAME).getEnumConstants();
			if (constants != null) {
				for (Object constant : constants) {
					if (((Enum<?>) constant).name().equals(constantName)) {
						return constant;
					}
				}
			}
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("找不到游戏模式切换器的图标枚举：" + ICON_CLASS_NAME, e);
		}

		throw new IllegalStateException("游戏模式切换器里没有图标格 " + constantName
				+ "（枚举扩展 BHR_FOOL_MODE 没有生效？）");
	}

	/** 该图标格是不是愚者模式那一格。 */
	public static boolean isFool(Object icon) {
		return icon instanceof Enum<?> constant && FOOL_CONSTANT.equals(constant.name());
	}
}
