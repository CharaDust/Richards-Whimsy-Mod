package com.rcdmake.whimsy.menu.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Campfire;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_CampfireEntity;
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
 * 简易篝火的界面（菜单 / 逻辑层）—— 与 {@link BHR_WorkBenchMenu} 同一套交互，<b>坐标整体右移</b>，
 * 并在左侧多出三个「燃料示意」槽。
 *
 * <h2>坐标从哪来（逐像素核对过 {@code textures/gui/container/fuel_workbench.png}）</h2>
 * <p>那张底图就是给这台机器画的：需求九宫格 / 时间水晶 / 进度条 / 结果栏 / 加减键整体比制作台
 * <b>右移 18 像素</b>，左侧 x=8 一列是新挖的三个凹槽。于是本类的槽位表是：
 *
 * <table border="1">
 *   <caption>槽位表（面板左上为原点，176×222 面板）</caption>
 *   <tr><th>下标</th><th>位置</th><th>用途</th></tr>
 *   <tr><td>0~7</td><td>(8,18)+18i</td><td>配方栏 8 格（假槽，图标由界面画）</td></tr>
 *   <tr><td>8</td><td>(152,18)</td><td>升级栏（假槽；本机固定 1 级、不升级，所以恒空）</td></tr>
 *   <tr><td>9~11</td><td>(8,54)/(8,72)/(8,90)</td><td><b>燃料示意</b> 3 格（假槽；点击 = 从背包抽对应燃料换燃料值）</td></tr>
 *   <tr><td>12~20</td><td>(44,54) 起 3×3</td><td>需求九宫格（假槽，纯显示）</td></tr>
 *   <tr><td>21 / 22</td><td>(151,53) / (151,89)</td><td>倍率 + / −（假槽）</td></tr>
 *   <tr><td>23</td><td>(116,90)</td><td>时间水晶（真槽）</td></tr>
 *   <tr><td>24</td><td>(152,72)</td><td>结果栏（真槽，只出不进）</td></tr>
 *   <tr><td>25 / 26</td><td>(25,108) / (89,108)</td><td>加速制作 / 开始制作（60×18，槽只占左端 16px）</td></tr>
 *   <tr><td>27 / 28</td><td>(8,35) / (134,35)</td><td>滑槽左右半区（点一下翻一页）</td></tr>
 *   <tr><td>29~64</td><td>(8,139) / (8,197)</td><td>玩家背包 27 + 快捷栏 9</td></tr>
 * </table>
 *
 * <h2>为什么界面动作还是「假槽 + 点击翻译」</h2>
 * <p>沿用制作台那一套：假槽（{@code mayPlace/mayPickup = false}、内容恒空）只负责接住原版的槽位点击包，
 * {@link #clicked} 里把它们翻译成界面动作，绝不进入物品移动逻辑。这样客户端不需要自定义发包，
 * 服务端也不必为每种按钮开一条网络通道。
 */
public class BHR_CampfireMenu extends AbstractContainerMenu {

	// ── 槽位下标 ──
	/**
	 * 燃料示意槽数量。
	 *
	 * <p>⚠️ 这里必须是<b>字面量</b>而不是 {@code BHR_Campfire.FUEL_COUNT}：下面那一串下标全是
	 * {@code case} 标签，而 Java 的 switch 要求「编译期常量表达式」——
	 * {@code FUELS.size()} 是运行时求值，取用它会让整张下标表失去常量性（实测报
	 * 「需要常量表达式」）。两者一致性在 {@link #OnInit()} 里断言。
	 */
	public static final int FUEL_SLOTS = 3;

	public static final int RECIPE_START = 0;
	public static final int RECIPE_END = RECIPE_START + BHR_CampfireEntity.RECIPE_SLOTS;   // 8
	public static final int UPGRADE_SLOT = RECIPE_END;                                       // 8
	public static final int FUEL_START = UPGRADE_SLOT + 1;                                    // 9
	public static final int FUEL_END = FUEL_START + FUEL_SLOTS;                               // 12
	public static final int REQUIRE_START = FUEL_END;                                         // 12
	public static final int REQUIRE_END = REQUIRE_START + 9;                                  // 21
	public static final int COUNT_PLUS_SLOT = REQUIRE_END;                                    // 21
	public static final int COUNT_MINUS_SLOT = COUNT_PLUS_SLOT + 1;                           // 22
	public static final int CRYSTAL_SLOT = COUNT_MINUS_SLOT + 1;                              // 23
	public static final int RESULT_SLOT = CRYSTAL_SLOT + 1;                                   // 24
	public static final int FAST_CRAFT_SLOT = RESULT_SLOT + 1;                                // 25
	public static final int CRAFT_SLOT = FAST_CRAFT_SLOT + 1;                                 // 26
	public static final int SCROLL_LEFT_SLOT = CRAFT_SLOT + 1;                                // 27
	public static final int SCROLL_RIGHT_SLOT = SCROLL_LEFT_SLOT + 1;                         // 28
	public static final int PLAYER_START = SCROLL_RIGHT_SLOT + 1;                             // 29

	/** 面板行数与玩家背包 y（与原版大箱子一致，底图也是这么画的）。 */
	public static final int ROWS = 6;
	public static final int PLAYER_INV_TOP = 18 + ROWS * 18 + 13;   // 139

	/** 界面布局常量（界面与菜单共用，改一处即可）。 */
	public static final int REQUIRE_X = 44;
	public static final int REQUIRE_Y = 54;
	public static final int FUEL_X = 8;
	public static final int FUEL_TOP = 54;
	public static final int COUNT_PLUS_X = 151;
	public static final int COUNT_PLUS_Y = 53;
	public static final int COUNT_MINUS_X = 151;
	public static final int COUNT_MINUS_Y = 89;
	public static final int CRYSTAL_X = 116;
	public static final int CRYSTAL_Y = 90;
	public static final int RESULT_X = 152;
	public static final int RESULT_Y = 72;

	/** 菜单类型，在 {@link #OnInit()} 里赋值。 */
	public static MenuType<BHR_CampfireMenu> BHR_CAMPFIRE_MENU;

	private final ContainerLevelAccess access;
	private final ContainerData data;

	/** 客户端路径：只拿得到 (containerId, inventory)。 */
	public BHR_CampfireMenu(int containerId, Inventory inventory) {
		this(containerId, inventory, ContainerLevelAccess.NULL,
				new SimpleContainer(BHR_CampfireEntity.CONTAINER_SIZE), new ClientData());
	}

	/** 服务端路径：方块实体既是容器也是数据源。 */
	public BHR_CampfireMenu(int containerId, Inventory inventory, ContainerLevelAccess access,
			BHR_CampfireEntity entity) {
		this(containerId, inventory, access, entity, entity);
	}

	private BHR_CampfireMenu(int containerId, Inventory inventory, ContainerLevelAccess access,
			net.minecraft.world.Container container, ContainerData data) {
		super(BHR_CAMPFIRE_MENU, containerId);
		this.access = access;
		this.data = data;

		SimpleContainer dummy = new SimpleContainer(1);

		// ── 配方栏（8 格）──
		for (int i = 0; i < BHR_CampfireEntity.RECIPE_SLOTS; i++) {
			this.addSlot(fake(dummy, 8 + i * 18, 18));
		}
		this.addSlot(fake(dummy, 152, 18));                        // 升级栏（本机恒空）

		// ── 燃料示意（3 格，左侧一列）──
		for (int i = 0; i < FUEL_SLOTS; i++) {
			this.addSlot(fake(dummy, FUEL_X, FUEL_TOP + i * 18));
		}

		// ── 需求九宫格（右移 18：制作台是 26，这里是 44）──
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				this.addSlot(fake(dummy, REQUIRE_X + col * 18, REQUIRE_Y + row * 18));
			}
		}

		// ── 倍率 + / −（右移 18：133 → 151）──
		this.addSlot(fake(dummy, COUNT_PLUS_X, COUNT_PLUS_Y));
		this.addSlot(fake(dummy, COUNT_MINUS_X, COUNT_MINUS_Y));

		// ── 真槽：时间水晶（98→116）与结果栏（134→152）──
		this.addSlot(new Slot(container, BHR_CampfireEntity.CRYSTAL_SLOT, CRYSTAL_X, CRYSTAL_Y));
		this.addSlot(new Slot(container, BHR_CampfireEntity.RESULT_SLOT, RESULT_X, RESULT_Y) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}
		});

		// ── 按钮假槽（宽按钮的点击由界面转发到这两个下标）──
		this.addSlot(fake(dummy, 25, 108));
		this.addSlot(fake(dummy, 89, 108));
		// ── 滑槽左右半区 ──
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

	/** 方块实体（客户端返回 null）。 */
	private BHR_CampfireEntity entity() {
		return this.data instanceof BHR_CampfireEntity campfire ? campfire : null;
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.access, player, BHR_Campfire.BHR_CAMPFIRE);
	}

	@Override
	public void broadcastChanges() {
		// 进度与燃料由 ticker 推进；这里只是保证界面打开时客户端拿到的数据是这一 tick 的
		super.broadcastChanges();
	}

	/** 界面动作统一入口：假槽上的点击被翻译成「选配方 / 投燃料 / 调倍率 / 开始 / 加速 / 翻页」。 */
	@Override
	public void clicked(int slotId, int button, ContainerInput input, Player player) {
		// 原版把「Shift + 左键」发成 QUICK_MOVE，所以两种输入都当界面动作接住
		boolean uiInput = input == ContainerInput.PICKUP || input == ContainerInput.QUICK_MOVE;
		if (uiInput && isUiSlot(slotId) && player.level() instanceof net.minecraft.server.level.ServerLevel) {
			this.handleUiClick(player, slotId, input == ContainerInput.QUICK_MOVE, button);
			return;
		}
		super.clicked(slotId, button, input, player);
	}

	/** 这批下标是「界面动作」，不是物品槽。 */
	public static boolean isUiSlot(int slotId) {
		if (slotId >= RECIPE_START && slotId < REQUIRE_END) {
			return true;          // 配方栏 + 升级栏 + 燃料示意 + 需求九宫格
		}
		return slotId == COUNT_PLUS_SLOT || slotId == COUNT_MINUS_SLOT
				|| slotId == FAST_CRAFT_SLOT || slotId == CRAFT_SLOT
				|| slotId == SCROLL_LEFT_SLOT || slotId == SCROLL_RIGHT_SLOT;
	}

	private void handleUiClick(Player player, int slotId, boolean shift, int button) {
		BHR_CampfireEntity entity = this.entity();
		if (entity == null) {
			return;               // 纯客户端菜单不执行动作
		}
		if (slotId >= RECIPE_START && slotId < RECIPE_END) {
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
			return;               // 简易篝火固定 1 级，没有可升级的目标
		}
		if (slotId >= FUEL_START && slotId < FUEL_END) {
			// 燃料示意：右键在候选之间切换首选消耗；左键投 1 件。
			// Shift + 左键刻意不支持（需求）：整栈必然超过 10 单位上限，投进去的部分会被丢弃。
			if (button == 1) {
				entity.cycleFuelPreferred(slotId - FUEL_START);
			} else if (!shift) {
				entity.addFuel(player, slotId - FUEL_START);
			}
			return;
		}
		switch (slotId) {
			case COUNT_PLUS_SLOT -> entity.setBatches(shift ? entity.getBatches() * 2 : entity.getBatches() + 1);
			case COUNT_MINUS_SLOT -> entity.setBatches(shift ? entity.getBatches() / 2 : entity.getBatches() - 1);
			case CRAFT_SLOT -> entity.startCraft(player);
			case FAST_CRAFT_SLOT -> entity.fastCraft();
			case SCROLL_LEFT_SLOT, SCROLL_RIGHT_SLOT -> {
				int maxScroll = Math.max(0, entity.visibleEntries().size() - BHR_CampfireEntity.RECIPE_SLOTS);
				entity.setScroll(Math.clamp(button, 0, maxScroll));
			}
			default -> {
				// 需求九宫格：右键切换首选消耗物品
				if (slotId >= REQUIRE_START && slotId < REQUIRE_END && button == 1) {
					entity.cyclePreferred(slotId - REQUIRE_START);
				}
			}
		}
	}

	/** 暴露给界面读同步数据。 */
	public ContainerData data() {
		return this.data;
	}

	/** Shift 点击：结果栏 → 玩家背包；玩家背包内部只在「快捷栏 ↔ 背包」之间搬。 */
	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		if (slotIndex < PLAYER_START) {
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

	/** 关界面：时间水晶退回背包（满了掉在方块旁），并清掉选中的配方。 */
	@Override
	public void removed(Player player) {
		super.removed(player);
		if (player.level().isClientSide()) {
			return;
		}
		BHR_CampfireEntity entity = this.entity();
		if (entity == null) {
			return;
		}
		ItemStack crystal = entity.removeItemNoUpdate(BHR_CampfireEntity.CRYSTAL_SLOT);
		if (!crystal.isEmpty() && !player.getInventory().add(crystal)) {
			player.drop(crystal, false);
		}
		entity.clearSelection();
	}

	/** 注册菜单类型（由 {@code BHR_Campfire.OnInit()} 调用）。 */
	public static void OnInit() {
		// 下标表用的是字面量 FUEL_SLOTS，这里把「字面量 = 方块那边的燃料档位数」钉死：
		// 以后往 BHR_Campfire.FUELS 里加一档却忘了改这里，会在启动日志里立刻炸出来。
		if (FUEL_SLOTS != BHR_Campfire.FUEL_COUNT) {
			throw new IllegalStateException("燃料示意槽数量不一致：menu=" + FUEL_SLOTS
					+ " / block=" + BHR_Campfire.FUEL_COUNT);
		}
		BHR_CAMPFIRE_MENU = ModRegister.registerMenuType(BHR_Campfire.REGISTRY_NAME, BHR_CampfireMenu::new);
	}

	/** 客户端的数据槽容器：服务端同步写进来，界面只读。 */
	private static final class ClientData implements ContainerData {
		private final int[] values = new int[BHR_CampfireEntity.DATA_COUNT];

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
