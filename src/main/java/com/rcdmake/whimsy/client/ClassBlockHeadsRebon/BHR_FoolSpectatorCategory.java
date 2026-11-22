package com.rcdmake.whimsy.client.ClassBlockHeadsRebon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 出窍时的「命令栏」内容 —— <b>完全复用原版旁观者菜单的框架</b>。
 *
 * <p>原版的 {@code SpectatorGui} + {@code SpectatorMenu} 已经提供了一整套：从底部升起的快捷栏、
 * 选中高亮、按键号角标、动作名、5 秒后淡出，以及「先选中、再按一次才执行」的两段式交互。
 * 我们只把菜单里的<b>条目</b>换掉。
 *
 * <p>条目位置（原版布局）：第 1~7 格来自 {@link SpectatorMenuCategory#getItems()}，
 * 第 8 格是原版的「关闭菜单」，第 9 格是翻页。因此「返回身体 / 起床」放在列表的第 3、4 项
 * （对应 3 / 4 号键）。
 *
 * <p>图标：
 * <ul>
 *   <li><b>返回身体</b> —— 当前那张床的图标（席地而睡时给一块石头）；</li>
 *   <li><b>起床</b> —— 时钟。</li>
 * </ul>
 */
public final class BHR_FoolSpectatorCategory implements SpectatorMenuCategory {

	/** 返回身体：图标跟着当前睡的那张床走。 */
	public static final SpectatorMenuItem RETURN_BODY =
			new Item("gui.bhr.fool.return_body", BHR_FoolSpectatorCategory::bedIcon,
					() -> BHR_FoolSleepCamera.exit());

	/** 起床：时钟。 */
	public static final SpectatorMenuItem WAKE_UP =
			new Item("gui.bhr.fool.wake_up", () -> new ItemStack(Items.CLOCK),
					() -> BHR_FoolSleepCamera.wakeUp());

	/** 整张页面：第 1~7 位，空位用 null（原版会当成 EMPTY_SLOT）。 */
	private static final List<SpectatorMenuItem> ITEMS = Arrays.asList(
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

	/** 当前睡的床的方块物品；不是在床上睡（席地而睡 / 强制睡眠）就给石头。 */
	private static ItemStack bedIcon() {
		Player player = Minecraft.getInstance().player;
		if (player != null) {
			Optional<BlockPos> sleepingPos = player.getSleepingPos();
			if (sleepingPos.isPresent()) {
				BlockState state = player.level().getBlockState(sleepingPos.get());
				if (state.getBlock() instanceof BedBlock) {
					return new ItemStack(state.getBlock().asItem());
				}
			}
		}
		return new ItemStack(Items.STONE);
	}

	/** 一个条目：选一次高亮，再选一次执行（与原版一致，由 {@code SpectatorMenu#selectSlot} 驱动）。 */
	private record Item(String nameKey, Supplier<ItemStack> icon, Runnable action) implements SpectatorMenuItem {

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
			graphics.item(icon.get(), 1, 2);
		}

		@Override
		public boolean isEnabled() {
			return true;
		}
	}
}
