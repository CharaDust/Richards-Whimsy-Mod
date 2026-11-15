package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 愚者模式：给「创建世界」界面的<b>游戏模式挡位枚举</b>
 * {@code WorldCreationUiState$SelectedGameMode} 加第五个常量 {@code BHR_FOOL}。
 *
 * <h2>为什么这里是「枚举扩展」而不是往原版数组里塞东西</h2>
 * 这个枚举（生存 / 极限 / 创造 / 调试）是 {@code CreateWorldScreen.GameTab} 构造
 * 那个循环按钮时的取值域，也是 {@code createLevelSettings} 拿 {@code gameType}
 * 的地方。要让它认第五种模式，只能往枚举本身加常量：
 * <ul>
 *   <li>{@code richards-whimsy-mod.classtweaker} 里的
 *       {@code extend-enum ...WorldCreationUiState$SelectedGameMode BHR_FOOL}
 *       先补一个占位字段（否则编译期写不出 {@code SelectedGameMode.BHR_FOOL}）；</li>
 *   <li>本 Mixin 以 <b>enum</b> 形式声明同名字段，运行期被替换成真正的常量。</li>
 * </ul>
 *
 * <h2>构造实参 {@code ("fool", GameType.BHR_FOOL_MODE)}</h2>
 * 原版构造器是 {@code SelectedGameMode(String name, GameType gameType)}，两个实参各有用途：
 * <ul>
 *   <li>{@code "fool"} —— 决定<b>语言键</b>：{@code selectWorld.gameMode.fool}
 *       （按钮上的名字）与 {@code selectWorld.gameMode.fool.info}
 *       （悬停说明，即愚者模式的描述文案）。与 {@code GameType} 的序列化名同源；</li>
 *   <li>{@code GameType.BHR_FOOL_MODE} —— 创建世界时写进 {@code LevelSettings}
 *       的游戏模式，也就是「进世界后是愚者模式」这件事的落点。</li>
 * </ul>
 *
 * <h2>为什么这个常量不决定「排在创造模式后面」</h2>
 * 顺序不在枚举里 —— 原版那个循环按钮的取值是<b>显式列出来的三个</b>，
 * 追加第五个的动作在 {@link BHR_FoolGameTabMixin} 里（改的是那个按钮的取值数组）。
 * 枚举里它排在最后（ordinal 4），只是因为 Java 枚举常量只能追加。
 */
@Mixin(WorldCreationUiState.SelectedGameMode.class)
enum BHR_FoolGameModeChoiceMixin {

	/** 第五挡：愚者模式。常量名与 classtweaker 的 extend-enum 一致。 */
	BHR_FOOL("fool", GameType.BHR_FOOL_MODE);

	/**
	 * 影子构造器：与 {@code SelectedGameMode(String, GameType)} 同签名，
	 * 上面那个常量的两个实参靠它交给原版构造器（原版构造器会顺手把
	 * {@code displayName} / {@code info} 两个语言组件拼好）。
	 */
	@Shadow
	BHR_FoolGameModeChoiceMixin(String name, GameType gameType) {
	}
}
