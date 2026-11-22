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
import net.minecraft.world.scores.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 出窍时的「命令栏」内容 —— <b>完全复用原版旁观者菜单的框架</b>。
 *
 * <p>原版的 {@code SpectatorGui} + {@code SpectatorMenu} 提供了一整套：从底部升起的快捷栏、
 * 选中高亮、按键号角标、动作名、5 秒后淡出，以及「先选中、再按一次才执行」的两段式交互。
 * 我们只替换菜单里的<b>条目</b>。
 *
 * <p>布局（沿用原版）：第 1~7 格来自 {@link SpectatorMenuCategory#getItems()}，
 * 第 8 格是原版「关闭菜单」，第 9 格翻页。
 *
 * <table>
 *   <tr><td>1</td><td>传送到玩家（子菜单）</td></tr>
 *   <tr><td>2</td><td>传送到队伍成员（子菜单）</td></tr>
 *   <tr><td>3</td><td>返回身体（图标 = 当前那张床，席地而睡给石头）</td></tr>
 *   <tr><td>4</td><td>起床（时钟）</td></tr>
 * </table>
 *
 * <p>注意：1/2 的传送只移动<b>相机</b>，本体仍然躺在床上 —— 这是和原版旁观者传送最大的区别。
 */
public final class BHR_FoolSpectatorCategory implements SpectatorMenuCategory {

	/** 单页最多几个玩家条目（原版每页 6 个 + 翻页/关闭）。 */
	private static final int MAX_ENTRIES = 6;

	/** 1 号键：传送到玩家。 */
	public static final PlayersCategory PLAYERS = new PlayersCategory(false);

	/** 2 号键：传送到队伍成员。 */
	public static final PlayersCategory TEAM = new PlayersCategory(true);

	/** 3 号键：返回身体。 */
	public static final Item RETURN_BODY =
			new Item(() -> Component.translatable("gui.bhr.fool.return_body"),
					BHR_FoolSpectatorCategory::bedIcon,
					() -> BHR_FoolSleepCamera.exit());

	/** 4 号键：起床。 */
	public static final Item WAKE_UP =
			new Item(() -> Component.translatable("gui.bhr.fool.wake_up"),
					() -> new ItemStack(Items.CLOCK),
					() -> BHR_FoolSleepCamera.wakeUp());

	/** 根页面：1 传送到玩家 / 2 传送到队伍 / 3 返回身体 / 4 起床。 */
	private static final List<SpectatorMenuItem> ITEMS =
			Arrays.asList(PLAYERS, TEAM, RETURN_BODY, WAKE_UP, null, null, null);

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
	private record Item(Supplier<Component> name, Supplier<ItemStack> icon, Runnable action)
			implements SpectatorMenuItem {

		@Override
		public void selectItem(SpectatorMenu menu) {
			action.run();
			menu.exit();
		}

		@Override
		public Component getName() {
			return name.get();
		}

		@Override
		public void extractIcon(GuiGraphicsExtractor graphics, float brightness, float alpha) {
			graphics.item(icon.get(), 0, 1);
		}

		@Override
		public boolean isEnabled() {
			return true;
		}
	}

	/**
	 * 「传送到玩家 / 队伍成员」——它本身既是一个条目（选中后展开子菜单），
	 * 也是一个分类（子菜单的内容）。选中某个玩家只把<b>相机</b>挪过去。
	 */
	public static final class PlayersCategory implements SpectatorMenuCategory, SpectatorMenuItem {

		private final boolean teamOnly;

		PlayersCategory(boolean teamOnly) {
			this.teamOnly = teamOnly;
		}

		/** 子菜单里的候选玩家。 */
		private List<Player> candidates() {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.level == null || minecraft.player == null) {
				return List.of();
			}

			Team myTeam = minecraft.player.getTeam();
			List<Player> result = new ArrayList<>();
			for (Player other : minecraft.level.players()) {
				if (other == minecraft.player) {
					continue;
				}
				if (teamOnly && (myTeam == null || other.getTeam() != myTeam)) {
					continue;
				}
				result.add(other);
				if (result.size() >= MAX_ENTRIES) {
					break;
				}
			}
			return result;
		}

		@Override
		public List<SpectatorMenuItem> getItems() {
			List<SpectatorMenuItem> items = new ArrayList<>();
			for (Player target : candidates()) {
				items.add(new Item(target::getName,
						() -> new ItemStack(Items.PLAYER_HEAD),
						() -> BHR_FoolSleepCamera.snapTo(target)));
			}
			return items;
		}

		@Override
		public Component getPrompt() {
			return Component.translatable(
					teamOnly ? "gui.bhr.fool.teleport_team" : "gui.bhr.fool.teleport_player");
		}

		// ── 作为根页面第 1/2 格的条目：选中它 = 展开子菜单 ──

		@Override
		public void selectItem(SpectatorMenu menu) {
			menu.selectCategory(this);
		}

		@Override
		public Component getName() {
			return getPrompt();
		}

		@Override
		public void extractIcon(GuiGraphicsExtractor graphics, float brightness, float alpha) {
			graphics.item(new ItemStack(teamOnly ? Items.COMPASS : Items.PLAYER_HEAD), 0, 1);
		}

		@Override
		public boolean isEnabled() {
			return !candidates().isEmpty();
		}
	}
}



