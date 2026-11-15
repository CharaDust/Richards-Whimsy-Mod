package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.item.ClassBlockHeadsRebon.BHR_BlockHeadsIcon;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 愚者模式：给 F3+F4 切换器的<b>图标枚举</b>加第五格。
 *
 * <p>{@code GameModeSwitcherScreen$GameModeIcon} 是原版的 private 嵌套枚举，
 * 四个常量（创造 / 生存 / 冒险 / 观众）各自带着「标题 + 游戏模式 + 图标
 * {@code ItemStack}」。切换器的格子数量、宽度、循环顺序全部由它推出来：
 * <pre>{@code
 * GameModeIcon.VALUES.length * 31 - 5   // 整排宽度
 * }</pre>
 * 所以只要往这个枚举里加一格，切换器就自动多出一个格子 —— 不需要改界面代码。
 *
 * <h2>图标为什么是一件物品而不是一张贴图</h2>
 * 原版这个枚举的图标字段类型是 {@code ItemStack}（{@code graphics.item(stack, x, y)}），
 * 没有「贴图路径」这条路。要让 {@code textures/item/bhr/blockheads_icon.png} 显示出来，
 * 只能让它成为一件真实物品的图标 —— 即 {@link BHR_BlockHeadsIcon}（{@code bhr_blockheads_icon}）。
 * 这也是该物品本次被注册的原因之一（另一个用途是方块愚者物品栏的标签图标）。
 *
 * <p>构造参数与目标枚举的 {@code (Component, GameType, ItemStack)} 完全对应，
 * 标题键 {@code gameMode.fool} 与 {@code GameType} 那边的序列化名 {@code fool} 同源。
 */
@Mixin(targets = "net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen$GameModeIcon")
enum BHR_FoolGameModeIconMixin {

	/** 第五格：愚者模式。常量名与 classtweaker 的 extend-enum 一致。 */
	BHR_FOOL_MODE(Component.translatable("gameMode.fool"), GameType.BHR_FOOL_MODE,
			new ItemStack(BHR_BlockHeadsIcon.BHR_BLOCKHEADS_ICON));

	/**
	 * 影子构造器：与 {@code GameModeIcon(Component, GameType, ItemStack)} 同签名，
	 * 上面那个格子的三个实参靠它交给原版构造器。
	 */
	@Shadow
	BHR_FoolGameModeIconMixin(Component name, GameType mode, ItemStack renderStack) {
	}
}
