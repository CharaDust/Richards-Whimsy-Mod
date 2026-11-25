package com.rcdmake.whimsy.surface;

import com.rcdmake.whimsy.ModRegister;
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

import java.util.HashMap;
import java.util.Map;

/**
 * 工作面界面（菜单 / 逻辑层）—— 三台机器共用。<b>槽位下标由 {@link WorkingSurfaceSpec} 决定</b>：
 *
 * <pre>
 *   0 .. 7            配方栏（8 格，纯显示 + 接点击）
 *   8                 升级栏
 *   9 .. 9+F-1        燃料示意（F = spec.fuelSlots()，无燃料的机器没有这一段）
 *   ...               需求九宫格（9 格，纯显示）
 *   ...               倍率 + / −
 *   水晶槽 / 结果槽    （真槽，下标见字段）
 *   加速制作 / 开始制作
 *   滑槽左右半区
 *   玩家背包 27 + 快捷栏 9
 * </pre>
 *
 * <p>「假槽」是真空槽 + 界面自绘：图标不是可存放的物品，但<b>点击</b>必须走原版槽位点击包
 * 才能到服务端，所以它们各占一个 {@link Slot}（{@code mayPlace/mayPickup = false}），内容永远为空。
 * 服务端在 {@link #clicked} 里把落在这批下标上的点击翻译成界面动作。
 */
public class WorkingSurfaceMenu extends AbstractContainerMenu {

	/** 面板行数（与原版大箱子一致）。 */
	public static final int ROWS = 6;
	/** 玩家背包 y。 */
	public static final int PLAYER_INV_TOP = 18 + ROWS * 18 + 13;

	/** id → 菜单类型（方块右键时按 spec.id 取）。 */
	private static final Map<String, MenuType<WorkingSurfaceMenu>> TYPES = new HashMap<>();

	// ── 槽位下标（实例字段：随燃料档位数变化）──
	public final int recipeStart = 0;
	public final int recipeEnd = WorkingSurfaceEntity.RECIPE_SLOTS;
	public final int upgradeSlot = recipeEnd;
	public final int fuelStart;
	public final int fuelEnd;
	public final int requireStart;
	public final int requireEnd;
	public final int countPlusSlot;
	public final int countMinusSlot;
	public final int crystalSlot;
	public final int resultSlot;
	public final int fastCraftSlot;
	public final int craftSlot;
	public final int scrollLeftSlot;
	public final int scrollRightSlot;
	public final int playerStart;

	private final WorkingSurfaceSpec spec;
	private final ContainerLevelAccess access;
	private final ContainerData data;

	/** 客户端路径（由注册时的 lambda 捕获 spec）。 */
	public WorkingSurfaceMenu(WorkingSurfaceSpec spec, int containerId, Inventory inventory) {
		this(spec, containerId, inventory, ContainerLevelAccess.NULL,
				new SimpleContainer(WorkingSurfaceEntity.CONTAINER_SIZE), clientData(spec));
	}

	/** 服务端路径：方块实体既是容器也是数据源。 */
	public WorkingSurfaceMenu(WorkingSurfaceSpec spec, int containerId, Inventory inventory,
			ContainerLevelAccess access, WorkingSurfaceEntity entity) {
		this(spec, containerId, inventory, access, entity, entity);
	}

	private WorkingSurfaceMenu(WorkingSurfaceSpec spec, int containerId, Inventory inventory,
			ContainerLevelAccess access, net.minecraft.world.Container container, ContainerData data) {
		super(typeFor(spec.id()), containerId);
		this.spec = spec;
		this.access = access;
		this.data = data;

		int fuelSlots = spec.fuelSlots();
		this.fuelStart = this.upgradeSlot + 1;
		this.fuelEnd = this.fuelStart + fuelSlots;
		this.requireStart = this.fuelEnd;
		this.requireEnd = this.requireStart + 9;
		this.countPlusSlot = this.requireEnd;
		this.countMinusSlot = this.countPlusSlot + 1;
		this.crystalSlot = this.countMinusSlot + 1;
		this.resultSlot = this.crystalSlot + 1;
		this.fastCraftSlot = this.resultSlot + 1;
		this.craftSlot = this.fastCraftSlot + 1;
		this.scrollLeftSlot = this.craftSlot + 1;
		this.scrollRightSlot = this.scrollLeftSlot + 1;
		this.playerStart = this.scrollRightSlot + 1;

		int shift = spec.panelShift();
		SimpleContainer dummy = new SimpleContainer(1);

		// 配方栏（8 格）
		for (int i = 0; i < WorkingSurfaceEntity.RECIPE_SLOTS; i++) {
			this.addSlot(fake(dummy, 8 + i * 18, 18));
		}
		this.addSlot(fake(dummy, 152, 18));                       // 升级栏
		// 燃料示意（3 格，左侧一列；无燃料的机器没有这一段）
		for (int i = 0; i < fuelSlots; i++) {
			this.addSlot(fake(dummy, 8, 54 + i * 18));
		}
		// 需求九宫格
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 3; col++) {
				this.addSlot(fake(dummy, 26 + shift + col * 18, 54 + row * 18));
			}
		}
		this.addSlot(fake(dummy, 133 + shift, 53));               // 倍率 +
		this.addSlot(fake(dummy, 133 + shift, 89));               // 倍率 −

		// 真槽
		this.addSlot(new Slot(container, WorkingSurfaceEntity.CRYSTAL_SLOT, 98 + shift, 90));
		this.addSlot(new Slot(container, WorkingSurfaceEntity.RESULT_SLOT, 134 + shift, 72) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}
		});

		// 按钮假槽
		this.addSlot(fake(dummy, 25, 108));
		this.addSlot(fake(dummy, 89, 108));
		// 滑槽左右半区
		this.addSlot(fake(dummy, 8, 35));
		this.addSlot(fake(dummy, 134, 35));

		// 玩家背包
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

	private static ContainerData clientData(WorkingSurfaceSpec spec) {
		return new ClientData(WorkingSurfaceEntity.DATA_FUEL_PREF_START + spec.fuelSlots());
	}

	// ── 布局查询（界面用）──

	public WorkingSurfaceSpec spec() {
		return this.spec;
	}

	/** 暴露给界面读同步数据。 */
	public ContainerData data() {
		return this.data;
	}

	/** 需求九宫格左上角的 x（燃料台右移 18）。 */
	public int requireX() {
		return 26 + this.spec.panelShift();
	}

	/** 需求九宫格左上角的 y。 */
	public int requireY() {
		return 54;
	}

	/** 燃料列的 x / 首行 y（只对烧燃料的机器有意义）。 */
	public int fuelX() {
		return 8;
	}

	public int fuelTop() {
		return 54;
	}

	/** 倍率 + / − 的坐标。 */
	public int countPlusX() {
		return 133 + this.spec.panelShift();
	}

	public int countPlusY() {
		return 53;
	}

	public int countMinusX() {
		return 133 + this.spec.panelShift();
	}

	public int countMinusY() {
		return 89;
	}

	/** 这批下标是「界面动作」，不是物品槽。 */
	public boolean isUiSlot(int slotId) {
		if (slotId >= this.recipeStart && slotId < this.requireEnd) {
			return true;      // 配方栏 + 升级栏 + 燃料示意 + 需求九宫格
		}
		return slotId == this.countPlusSlot || slotId == this.countMinusSlot
				|| slotId == this.fastCraftSlot || slotId == this.craftSlot
				|| slotId == this.scrollLeftSlot || slotId == this.scrollRightSlot;
	}

	/**
	 * 界面打开期间每 tick 刷新一次：进度按「当前游戏刻 − 本份开始刻」补算并同步给客户端。
	 * 没有界面打开时方块完全不 tick（进度靠时间差在下次访问时补齐）。
	 */
	@Override
	public void broadcastChanges() {
		WorkingSurfaceEntity surface = this.entity();
		if (surface != null && surface.getLevel() != null) {
			surface.refresh(surface.getLevel().getGameTime());
		}
		super.broadcastChanges();
	}

	@Override
	public boolean stillValid(Player player) {
		net.minecraft.world.level.block.Block block = blockOf(this.spec);
		return block != null && stillValid(this.access, player, block);
	}

	private static net.minecraft.world.level.block.Block blockOf(WorkingSurfaceSpec spec) {
		return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(ModRegister.id(spec.id()));
	}

	/** 界面动作的统一入口。 */
	@Override
	public void clicked(int slotId, int button, ContainerInput input, Player player) {
		boolean uiInput = input == ContainerInput.PICKUP || input == ContainerInput.QUICK_MOVE;
		if (uiInput && this.isUiSlot(slotId) && player.level() instanceof net.minecraft.server.level.ServerLevel) {
			this.handleUiClick(player, slotId, input == ContainerInput.QUICK_MOVE, button);
			return;
		}
		super.clicked(slotId, button, input, player);
	}

	private void handleUiClick(Player player, int slotId, boolean shift, int button) {
		WorkingSurfaceEntity entity = this.entity();
		if (entity == null) {
			return;               // 纯客户端菜单不执行动作
		}
		if (slotId >= this.recipeStart && slotId < this.recipeEnd) {
			int visibleIndex = entity.getScroll() + (slotId - this.recipeStart);
			var visible = entity.visibleEntries();
			if (visibleIndex >= 0 && visibleIndex < visible.size()) {
				int global = BHR_FoolRecipes.all().indexOf(visible.get(visibleIndex));
				if (global >= 0) {
					entity.select(global);
				}
			}
			return;
		}
		if (slotId == this.upgradeSlot) {
			var next = entity.nextUpgrade();
			if (next != null) {
				int global = BHR_FoolRecipes.all().indexOf(next);
				if (global >= 0) {
					entity.select(global);
				}
			}
			return;
		}
		if (slotId >= this.fuelStart && slotId < this.fuelEnd) {
			int fuelIndex = slotId - this.fuelStart;
			if (button == 1) {
				entity.cycleFuelPreferred(fuelIndex);
			} else {
				entity.addFuel(player, fuelIndex);
			}
			return;
		}
		if (slotId == this.countPlusSlot) {
			entity.setBatches(shift ? entity.getBatches() * 2 : entity.getBatches() + 1);
			return;
		}
		if (slotId == this.countMinusSlot) {
			entity.setBatches(shift ? entity.getBatches() / 2 : entity.getBatches() - 1);
			return;
		}
		if (slotId == this.craftSlot) {
			entity.startCraft(player);
			return;
		}
		if (slotId == this.fastCraftSlot) {
			entity.fastCraft();
			return;
		}
		if (slotId == this.scrollLeftSlot || slotId == this.scrollRightSlot) {
			int maxScroll = Math.max(0, entity.visibleEntries().size() - WorkingSurfaceEntity.RECIPE_SLOTS);
			entity.setScroll(Math.clamp(button, 0, maxScroll));
			return;
		}
		// 需求九宫格：右键切换首选消耗物品（左键什么都不做）
		if (slotId >= this.requireStart && slotId < this.requireEnd && button == 1) {
			entity.cyclePreferred(slotId - this.requireStart);
		}
	}

	/** 服务端路径下拿方块实体；客户端返回 null。 */
	private WorkingSurfaceEntity entity() {
		return this.data instanceof WorkingSurfaceEntity surface ? surface : null;
	}

	/**
	 * Shift 点击：结果栏 → 玩家背包；玩家背包内部只在「快捷栏 ↔ 背包」之间搬。
	 */
	@Override
	public ItemStack quickMoveStack(Player player, int slotIndex) {
		if (slotIndex < this.playerStart) {
			if (slotIndex != this.resultSlot) {
				return ItemStack.EMPTY;
			}
			Slot slot = this.slots.get(slotIndex);
			if (!slot.hasItem()) {
				return ItemStack.EMPTY;
			}
			ItemStack stack = slot.getItem();
			ItemStack before = stack.copy();
			if (!this.moveItemStackTo(stack, this.playerStart, this.slots.size(), true)) {
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
		int mainStart = this.playerStart;
		int mainEnd = this.playerStart + 27;
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
		WorkingSurfaceEntity entity = this.entity();
		if (entity == null) {
			return;
		}
		ItemStack crystal = entity.removeItemNoUpdate(WorkingSurfaceEntity.CRYSTAL_SLOT);
		if (!crystal.isEmpty() && !player.getInventory().add(crystal)) {
			player.drop(crystal, false);
		}
		// 关界面不保留选中的配方（滚动位置保留）；制作中则不清
		entity.clearSelection();
	}

	// ── 注册 ──

	/**
	 * 注册一台工作面的菜单类型。
	 *
	 * <p>客户端构造器由 lambda 捕获 spec —— {@link MenuType} 只会传 (id, inventory)，
	 * 这是让「同一条菜单类服务三台机器」的关键。
	 */
	public static MenuType<WorkingSurfaceMenu> register(WorkingSurfaceSpec spec) {
		MenuType<WorkingSurfaceMenu> type = ModRegister.registerMenuType(spec.id(),
				(containerId, inventory) -> new WorkingSurfaceMenu(spec, containerId, inventory));
		TYPES.put(spec.id(), type);
		return type;
	}

	/** 按 spec id 取已注册的菜单类型。 */
	public static MenuType<WorkingSurfaceMenu> typeFor(String id) {
		MenuType<WorkingSurfaceMenu> type = TYPES.get(id);
		if (type == null) {
			throw new IllegalStateException("工作面菜单类型未注册：" + id);
		}
		return type;
	}

	/** 服务端开界面用（方块里调用，便于保持一处构造）。 */
	public static WorkingSurfaceMenu create(WorkingSurfaceSpec spec, int containerId, Inventory inventory,
			ContainerLevelAccess access, WorkingSurfaceEntity entity) {
		return new WorkingSurfaceMenu(spec, containerId, inventory, access, entity);
	}

	/** 客户端的数据槽容器：服务端同步写进来，界面只读。 */
	private static final class ClientData implements ContainerData {
		private final int[] values;

		private ClientData(int size) {
			this.values = new int[Math.max(1, size)];
		}

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
