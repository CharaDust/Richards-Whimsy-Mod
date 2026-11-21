package com.rcdmake.whimsy.menu.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_WorkBench;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_WorkBenchEntity;
import com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon.BHR_FoolRecipes;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 制作台界面（菜单/逻辑层）。
 *
 * <h2>槽位表（下标 → 位置，坐标以面板左上为原点）</h2>
 * <table border="1">
 *   <caption>60 个槽：18 个「只作点击目标」的假槽 + 2 个真槽 + 36 个玩家背包格 + 4 个按钮假槽</caption>
 *   <tr><th>下标</th><th>位置</th><th>用途</th></tr>
 *   <tr><td>0~7</td><td>(8,18) 起，每格 +18</td><td>配方栏（8 格，显示靠界面画，槽本身空着只用来接点击）</td></tr>
 *   <tr><td>8</td><td>(152,18)</td><td>升级栏</td></tr>
 *   <tr><td>9~17</td><td>(26,54) 起 3×3</td><td>需求九宫格（纯显示）</td></tr>
 *   <tr><td>18 / 19</td><td>(133,53) / (133,89)</td><td>倍率 + / −（常态画在底图里）</td></tr>
 *   <tr><td>20</td><td>(98,90)</td><td>时间水晶（真槽）</td></tr>
 *   <tr><td>21</td><td>(134,72)</td><td>结果栏（真槽，只出不进）</td></tr>
 *   <tr><td>22 / 23</td><td>(25,108) / (89,108)</td><td>加速制作 / 开始制作（60×18 的按钮，槽只占左端 16px，宽的部分由界面转发点击）</td></tr>
 *   <tr><td>24~59</td><td>(8,139) / (8,197)</td><td>玩家背包 27 + 快捷栏 9</td></tr>
 * </table>
 *
 * <h2>为什么假槽是「真空槽 + 界面自己画」</h2>
 * <p>配方图标、需求格、按钮都不是可存放的物品，但<b>点击</b>必须走原版的槽位点击包才能到服务端。
 * 所以它们各占一个 {@link Slot}（{@code mayPlace/mayPickup = false}），
 * 内容永远为空，图标由 {@code BHR_WorkBenchScreen} 依据同步过来的状态自己画。
 * <p>服务端在 {@link #clicked} 里把落在这 22 个下标上的点击翻译成界面动作，绝不进入物品移动逻辑。
 */
public class BHR_WorkBenchMenu extends AbstractContainerMenu {

	// ── 槽位下标 ──
	public static final int RECIPE_START = 0;
	public static final int RECIPE_END = RECIPE_START + BHR_WorkBenchEntity.RECIPE_SLOTS;   // 8
	public static final int UPGRADE_SLOT = RECIPE_END;                                        // 8
	public static final int REQUIRE_START = UPGRADE_SLOT + 1;                                 // 9
	public static final int REQUIRE_END = REQUIRE_START + 9;                                  // 18
	public static final int COUNT_PLUS_SLOT = REQUIRE_END;                                    // 18
	public static final int COUNT_MINUS_SLOT = COUNT_PLUS_SLOT + 1;                           // 19
	public static final int CRYSTAL_SLOT = COUNT_MINUS_SLOT + 1;                              // 20
	public static final int RESULT_SLOT = CRYSTAL_SLOT + 1;                                   // 21
	public static final int FAST_CRAFT_SLOT = RESULT_SLOT + 1;                                // 22
	public static final int CRAFT_SLOT = FAST_CRAFT_SLOT + 1;                                 // 23
	public static final int SCROLL_LEFT_SLOT = CRAFT_SLOT + 1;                                // 24
	public static final int SCROLL_RIGHT_SLOT = SCROLL_LEFT_SLOT + 1;                         // 25
	public static final int PLAYER_START = SCROLL_RIGHT_SLOT + 1;                             // 26

	/** 面板坐标（与原版大箱子一致）。 */
	public static final int ROWS = 6;
	public static final int PLAYER_INV_TOP = 18 + ROWS * 18 + 13;

	/** 菜单类型，在 {@link #OnInit()} 里赋值。 */
	public static MenuType<BHR_WorkBenchMenu> BHR_WORK_BENCH_MENU;

	private final ContainerLevelAccess access;
	private final ContainerData data;

	/** 客户端路径：只拿得到 (containerId, inventory)，数据槽由服务端同步填充。 */
	public BHR_WorkBenchMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, ContainerLevelAccess.NULL, new SimpleContainer(BHR_WorkBenchEntity.CONTAINER_SIZE),
				new ClientData());
	}

	/** 服务端路径：方块实体既是容器也是数据源。 */
	public BHR_WorkBenchMenu(int containerId, Inventory inventory, ContainerLevelAccess access,
			BHR_WorkBenchEntity entity) {
		this(containerId, inventory, access, entity, entity);
	}

	private BHR_WorkBenchMenu(int containerId, Inventory inventory, ContainerLevelAccess access,
			net.minecraft.world.Container container, ContainerData data) {
		super(BHR_WORK_BENCH_MENU, containerId);
		this.access = access;
		this.data = data;

		// ── 假槽（只接点击；内容恒空）──
		SimpleContainer dummy = new SimpleContainer(1);
		for (int i = 0; i < BHR_WorkBenchEntity.RECIPE_SLOTS; i++) {
			this.addSlot(fake(dummy, 8 + i * 18, 18));
		}
		this.addSlot(fake(dummy, 152, 18));                       // 升级栏
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				this.addSlot(fake(dummy, 26 + col * 18, 54 + row * 18));   // 需求九宫格
			}
		}
		this.addSlot(fake(dummy, 133, 53));                       // 倍率 +
		this.addSlot(fake(dummy, 133, 89));                       // 倍率 −

		// ── 真槽 ──
		this.addSlot(new Slot(container, BHR_WorkBenchEntity.CRYSTAL_SLOT, 98, 90));
		this.addSlot(new Slot(container, BHR_WorkBenchEntity.RESULT_SLOT, 134, 72) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}
		});

		// ── 按钮假槽（宽按钮的点击由界面转发到这两个下标）──
		this.addSlot(fake(dummy, 25, 108));
		this.addSlot(fake(dummy, 89, 108));
		// ── 滑槽左右半区（点滑槽 = 翻一页）──
		this.addSlot(fake(dummy, 8, 35));
		this.addSlot(fake(dummy, 134, 35));

		// ── 玩家背包 ──
		this.addStandardInventorySlots(inventory, 8, PLAYER_INV_TOP);

		this.addDataSlots(data);
	}

	/** 只接点击的假槽。 */
	private static Slot fake(net.minecraft.world.Container container, int x, int y) {
		return new Slot(container, 0, x, y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}

			@Override
			public boolean mayPickup(Player player) {
				return false;
			}

			@Override
			public boolean isHighlightable() {
				return false;
			}
		};
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.access, player, BHR_WorkBench.BHR_WORK_BENCH);
	}

	/** 界面动作的统一入口：假槽上的点击被翻译成「选配方 / 调倍率 / 开始 / 加速」。 */
	@Override
	public void clicked(int slotId, int button, ContainerInput input, Player player) {
		// 原版把「Shift + 左键」发成 QUICK_MOVE，所以这里两种输入都要当界面动作接住
		boolean uiInput = input == ContainerInput.PICKUP || input == ContainerInput.QUICK_MOVE;
		if (uiInput && this.isUiSlot(slotId) && player.level() instanceof net.minecraft.server.level.ServerLevel) {
			this.handleUiClick(player, slotId, input == ContainerInput.QUICK_MOVE, button);
			return;
		}
		super.clicked(slotId, button, input, player);
	}

	/** 这批下标是「界面动作」，不是物品槽。 */
	public static boolean isUiSlot(int slotId) {
		if (slotId >= RECIPE_START && slotId < REQUIRE_END) {
			return true;          // 配方栏 + 升级栏 + 需求九宫格
		}
		return slotId == COUNT_PLUS_SLOT || slotId == COUNT_MINUS_SLOT
				|| slotId == FAST_CRAFT_SLOT || slotId == CRAFT_SLOT
				|| slotId == SCROLL_LEFT_SLOT || slotId == SCROLL_RIGHT_SLOT;
	}

	private void handleUiClick(Player player, int slotId, boolean shift, int button) {
		if (!(this.data instanceof BHR_WorkBenchEntity) && this.data instanceof ClientData) {
			return;               // 纯客户端菜单不执行动作
		}
		BHR_WorkBenchEntity entity = this.entity();
		if (entity == null) {
			return;
		}
		if (slotId >= RECIPE_START && slotId < RECIPE_END) {
			// 配方栏：屏幕下标 + 滚动偏移 → 可见列表 → 全局下标
			int visibleIndex = entity.getScroll() + (slotId - RECIPE_START);
			var visible = entity.visibleEntries();
			if (visibleIndex >= 0 && visibleIndex < visible.size()) {
				int global = BHR_FoolRecipes.all().indexOf(visible.get(visibleIndex));
				if (global >= 0) {
					entity.select(global);
				}
			}
			return;
		}
		if (slotId == UPGRADE_SLOT) {
			var next = entity.nextUpgrade();
			if (next != null) {
				int global = BHR_FoolRecipes.all().indexOf(next);
				if (global >= 0) {
					entity.select(global);
				}
			}
			return;
		}
		switch (slotId) {
			case COUNT_PLUS_SLOT -> entity.setBatches(shift ? entity.getBatches() * 2 : entity.getBatches() + 1);
			case COUNT_MINUS_SLOT -> entity.setBatches(shift ? entity.getBatches() / 2 : entity.getBatches() - 1);
			case CRAFT_SLOT -> entity.startCraft(player);
			case FAST_CRAFT_SLOT -> entity.fastCraft();
			// 滑槽：button 就是「目标滚动偏移」绝对值（界面已算好并夹到合法范围）
			case SCROLL_LEFT_SLOT, SCROLL_RIGHT_SLOT -> {
				int maxScroll = Math.max(0, entity.visibleEntries().size() - BHR_WorkBenchEntity.RECIPE_SLOTS);
				entity.setScroll(Math.clamp(button, 0, maxScroll));
			}
			default -> {
				// 需求九宫格：右键切换首选消耗物品（左键什么都不做）
				if (slotId >= REQUIRE_START && slotId < REQUIRE_END && button == 1) {
					entity.cyclePreferred(slotId - REQUIRE_START);
				}
			}
		}
	}

	/** 服务端路径下拿方块实体；客户端返回 null。 */
	private BHR_WorkBenchEntity entity() {
		return this.data instanceof BHR_WorkBenchEntity workBench ? workBench : null;
	}

	/** 暴露给界面读同步数据。 */
	public ContainerData data() {
		return this.data;
	}

	/**
	 * Shift 点击：结果栏 → 玩家背包；玩家背包内部只在「快捷栏 ↔ 背包」之间搬
	 * （上半部分没有可存放的格子）。
	 */
	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		if (slotIndex < PLAYER_START) {
			// 结果栏才允许 Shift 转移
			if (slotIndex != RESULT_SLOT) {
				return ItemStack.EMPTY;
			}
			Slot slot = this.slots.get(slotIndex);
			if (!slot.hasItem()) {
				return ItemStack.EMPTY;
			}
			ItemStack stack = slot.getItem();
			ItemStack before = stack.copy();
			if (!this.moveItemStackTo(stack, PLAYER_START, this.slots.size(), true)) {
				return ItemStack.EMPTY;
			}
			if (stack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
			return before;
		}

		Slot slot = this.slots.get(slotIndex);
		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = slot.getItem();
		ItemStack before = stack.copy();
		int mainStart = PLAYER_START;
		int mainEnd = PLAYER_START + 27;
		int hotbarEnd = mainEnd + 9;
		boolean moved = slotIndex < mainEnd
				? this.moveItemStackTo(stack, mainEnd, hotbarEnd, false)
				: this.moveItemStackTo(stack, mainStart, mainEnd, false);
		if (!moved) {
			return ItemStack.EMPTY;
		}
		if (stack.isEmpty()) {
			slot.setByPlayer(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		return before;
	}

	/** 关界面时把时间水晶退回背包（背包满就掉在方块旁）。 */
	@Override
	public void removed(Player player) {
		super.removed(player);
		if (player.level().isClientSide()) {
			return;
		}
		BHR_WorkBenchEntity entity = this.entity();
		if (entity == null) {
			return;
		}
		ItemStack crystal = entity.removeItemNoUpdate(BHR_WorkBenchEntity.CRYSTAL_SLOT);
		if (!crystal.isEmpty() && !player.getInventory().add(crystal)) {
			player.drop(crystal, false);
		}
	}

	/** 注册菜单类型（由 {@code BHR_WorkBench.OnInit()} 调用）。 */
	public static void OnInit() {
		BHR_WORK_BENCH_MENU = ModRegister.registerMenuType(BHR_WorkBench.REGISTRY_NAME, BHR_WorkBenchMenu::new);
	}

	/** 客户端的数据槽容器：服务端同步写进来，界面只读。 */
	private static final class ClientData implements ContainerData {
		private final int[] values = new int[BHR_WorkBenchEntity.DATA_COUNT];

		@Override
		public int get(int index) {
			return index >= 0 && index < this.values.length ? this.values[index] : 0;
		}

		@Override
		public void set(int index, int value) {
			if (index >= 0 && index < this.values.length) {
				this.values[index] = value;
			}
		}

		@Override
		public int getCount() {
			return this.values.length;
		}
	}
}
