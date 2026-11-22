package com.rcdmake.whimsy.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon.BHR_ScreenAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import java.util.List;

/**
 * 出窍时的「命令栏」内容 —— <b>完全复用原版旁观者菜单的框架</b>。
 *
 * <p>原版的 {@code SpectatorGui} + {@code SpectatorMenu} 已经提供了这一整套：
 * 从底部升起的快捷栏、选中高亮、按键号角标、动作名、5 秒后淡出，
 * 以及「先选中、再按一次才执行」的两段式交互。我们要做的只是把菜单里的
 * <b>条目</b>换成自己的，而不是另画一套 UI。
 *
 * <p>条目位置（原版布局）：第 1~7 格来自 {@link SpectatorMenuCategory#getItems()}，
 * 第 8 格是原版的「关闭菜单」，第 9 格是翻页/关闭。所以我们把
 * 返回身体 / 起床 放在列表的第 3、4 项（对应 3 / 4 号键）。
 */
public final class BHR_FoolSpectatorCategory implements SpectatorMenuCategory {

	/** 返回身体。 */
	public static final SpectatorMenuItem RETURN_BODY = new Item(
			"gui.bhr.fool.return_body", "回", () -> BHR_FoolSleepCamera.exit());

	/** 起床。 */
	public static final SpectatorMenuItem WAKE_UP = new Item(
			"gui.bhr.fool.wake_up", "起", () -> BHR_FoolSleepCamera.wakeUp());

	/** 整张页面：第 1~7 位，空位用 null（原版会当成 EMPTY_SLOT）。 */
	private static final List<SpectatorMenuItem> ITEMS = java.util.Arrays.asList(
			null, null, RETURN_BODY, WAKE_UP, null, null, null);

	public static final BHR_FoolSpectatorCategory INSTANCE = new BHR_FoolSpectatorCategory();

	private BHR_FoolSpectatorCategory() {
	}

	@Override
	public List<SpectatorMenuItem> getItems() {
		return ITEMS;
	}

	@Override
	public Component getPrompt() {
		return Component.translatable("gui.bhr.fool.menu_prompt");
	}

	/** 一个条目：选一次高亮，再选一次执行（与原版一致，由 {@code SpectatorMenu#selectSlot} 驱动）。 */
	private record Item(String nameKey, String glyph, Runnable action) implements SpectatorMenuItem {

		@Override
		public void selectItem(SpectatorMenu menu) {
			action.run();
			menu.exit();
		}

		@Override
		public Component getName() {
			return Component.translatable(nameKey);
		}

		@Override
		public void extractIcon(GuiGraphicsExtractor graphics, float brightness, float alpha) {
			var font = Minecraft.getInstance().font;
			int width = font.width(glyph);
			graphics.text(font, glyph, (18 - width) / 2, 2, ARGB.white(alpha * brightness), true);
		}

		@Override
		public boolean isEnabled() {
			return true;
		}
	}
}
