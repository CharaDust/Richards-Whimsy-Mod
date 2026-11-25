package com.rcdmake.whimsy.client.surface;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon.BHR_FoolIngredient;
import com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon.BHR_FoolRecipe;
import com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon.BHR_FoolRecipes;
import com.rcdmake.whimsy.surface.FuelOption;
import com.rcdmake.whimsy.surface.WorkingSurfaceBlock;
import com.rcdmake.whimsy.surface.WorkingSurfaceEntity;
import com.rcdmake.whimsy.surface.WorkingSurfaceMenu;
import com.rcdmake.whimsy.surface.WorkingSurfaceSpec;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 工作面界面（客户端绘制层）—— 三台机器共用。
 *
 * <p>绘制内容与坐标全部由 {@link WorkingSurfaceSpec} 与 {@link WorkingSurfaceMenu} 的下标推导，
 * 所以「制作台 / 木工台」与「简易篝火」的差别只剩三处：
 * <ol>
 *   <li><b>底图</b>：{@code workbench.png} 或 {@code fuel_workbench.png}；</li>
 *   <li><b>整体右移</b>：需求格 / 进度条 / 水晶 / 结果 / 倍率偏移 {@link WorkingSurfaceSpec#panelShift()}；</li>
 *   <li><b>燃料列与燃料指示条</b>：只有 {@code fuelSlots > 0} 的机器才画。</li>
 * </ol>
 */
public class WorkingSurfaceScreen extends AbstractContainerScreen<WorkingSurfaceMenu> {

	private static final Identifier SPRITE_CRAFT_PROGRESS =
			RichardsWhimsyMod.id("container/workbench/craft_progress");
	private static final Identifier SPRITE_SCROLLER = RichardsWhimsyMod.id("container/workbench/scroller");
	private static final Identifier SPRITE_SCROLLER_DISABLED =
			RichardsWhimsyMod.id("container/workbench/scroller_disabled");
	private static final Identifier SPRITE_COUNT_ADD = RichardsWhimsyMod.id("container/workbench/count_add");
	private static final Identifier SPRITE_COUNT_MINUS = RichardsWhimsyMod.id("container/workbench/count_minus");
	private static final Identifier SPRITE_SLOT_UPGRADE = RichardsWhimsyMod.id("container/workbench/slot_upgrade");
	private static final Identifier SPRITE_SLOT_ALT = RichardsWhimsyMod.id("container/workbench/slot_alt");
	private static final Identifier SPRITE_LIT_PROGRESS =
			RichardsWhimsyMod.id("container/fuel_workbench/lit_progress");

	private static final int TOP_HEIGHT = WorkingSurfaceMenu.ROWS * 18 + 17;   // 125
	private static final int BOTTOM_V = 126;
	private static final int BOTTOM_HEIGHT = 96;

	/** 滑槽与滑块。 */
	private static final int TRACK_LEFT = 7;
	private static final int TRACK_TOP = 35;
	private static final int TRACK_RIGHT = 150;
	private static final int TRACK_BOTTOM = 48;
	private static final int KNOB_LEFT = 8;
	private static final int KNOB_WIDTH = 15;
	private static final int KNOB_HEIGHT = 12;
	private static final int KNOB_RIGHT = 149 - KNOB_WIDTH + 1;
	private static final int KNOB_TOP = 36;

	/** 进度条（左移 18 就是制作台的位置）。 */
	private static final int PROGRESS_BASE_LEFT = 94;
	private static final int PROGRESS_TOP = 72;
	private static final int PROGRESS_WIDTH = 24;
	private static final int PROGRESS_HEIGHT = 16;

	/** 两个文字按钮（三台同位）。 */
	private static final int FAST_X = 25;
	private static final int CRAFT_X = 89;
	private static final int BUTTON_Y = 108;
	private static final int BUTTON_WIDTH = 60;
	private static final int BUTTON_HEIGHT = 18;

	/** 燃料指示条（跟熔炉火焰条同一套画法）。 */
	private static final int FUEL_BAR_LEFT = 27;
	private static final int FUEL_BAR_TOP = 74;
	private static final int FUEL_BAR_SIZE = 14;

	private final WorkingSurfaceSpec spec;
	private boolean draggingKnob;
	private int lastSentScroll = -1;

	public WorkingSurfaceScreen(WorkingSurfaceMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 114 + WorkingSurfaceMenu.ROWS * 18);
		this.spec = menu.spec();
	}

	// ── 背景 ──

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		int xo = (this.width - this.imageWidth) / 2;
		int yo = (this.height - this.imageHeight) / 2;
		graphics.blit(RenderPipelines.GUI_TEXTURED, this.spec.texture(), xo, yo, 0.0F, 0.0F,
				this.imageWidth, TOP_HEIGHT, 256, 256);
		graphics.blit(RenderPipelines.GUI_TEXTURED, this.spec.texture(), xo, yo + TOP_HEIGHT, 0.0F,
				(float) BOTTOM_V, this.imageWidth, BOTTOM_HEIGHT, 256, 256);
	}

	// ── 幽灵层 + 进度条 + 燃料 + 按钮 ──

	@Override
	public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractContents(graphics, mouseX, mouseY, partialTick);
		int gx = this.leftPos;
		int gy = this.topPos;
		int shift = this.spec.panelShift();

		ContainerData data = this.menu.data();
		int level = data.get(WorkingSurfaceEntity.DATA_LEVEL);
		int batches = Math.max(1, data.get(WorkingSurfaceEntity.DATA_BATCHES));
		int scroll = data.get(WorkingSurfaceEntity.DATA_SCROLL);
		// 滚轮的本地待确认偏移已经和服务端对上了 → 清掉，之后以服务端为准
		if (this.wheelTarget >= 0 && scroll == this.wheelTarget) {
			this.wheelTarget = -1;
		}
		int selected = data.get(WorkingSurfaceEntity.DATA_SELECTED);
		int progress = data.get(WorkingSurfaceEntity.DATA_PROGRESS);
		int total = data.get(WorkingSurfaceEntity.DATA_TOTAL);

		List<BHR_FoolRecipes.Entry> visible = this.visibleEntries(data);
		BHR_FoolRecipe selectedRecipe = this.selectedRecipe(selected);

		// 配方栏
		for (int i = 0; i < WorkingSurfaceEntity.RECIPE_SLOTS; i++) {
			int index = scroll + i;
			if (index >= visible.size()) {
				break;
			}
			BHR_FoolRecipe recipe = visible.get(index).recipe();
			int x = gx + 8 + i * 18;
			int y = gy + 18;
			boolean levelEnough = recipe.level() <= level;
			boolean isSelected = BHR_FoolRecipes.all().indexOf(visible.get(index)) == selected;
			this.drawGhost(graphics, this.iconOf(recipe), x, y, isSelected, !levelEnough);
			if (recipe.resultCount() > 1) {
				this.drawCountText(graphics, String.valueOf(recipe.resultCount()), x, y, 0xFFFFFFFF);
			}
		}

		// 升级栏
		BHR_FoolRecipes.Entry upgrade = this.nextUpgrade(data, level);
		if (upgrade != null) {
			int target = upgrade.recipe().upgradeTo().orElse(level);
			this.drawGhost(graphics, this.surfaceStack(target), gx + 152, gy + 18, false, false);
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE_SLOT_UPGRADE, gx + 152, gy + 18, 16, 16);
		}

		// 燃料示意（只有烧燃料的机器）
		if (this.spec.hasFuel()) {
			for (int i = 0; i < this.spec.fuelSlots(); i++) {
				FuelOption fuel = this.spec.fuel(i);
				if (fuel == null) {
					continue;
				}
				int x = gx + this.menu.fuelX();
				int y = gy + this.menu.fuelTop() + i * 18;
				int owned = this.countFuelInInventory(fuel);
				this.drawGhost(graphics, this.fuelIcon(data, i, fuel), x, y, false, false);
				if (fuel.replaceable()) {
					graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE_SLOT_ALT, x, y, 16, 16);
				}
				this.drawCountText(graphics, String.valueOf(owned), x, y, owned > 0 ? 0xFFFFFFFF : 0xFFFF5555);
			}
		}

		// 需求九宫格
		if (selectedRecipe != null) {
			List<BHR_FoolIngredient> ingredients = selectedRecipe.ingredients();
			for (int i = 0; i < Math.min(9, ingredients.size()); i++) {
				BHR_FoolIngredient ingredient = ingredients.get(i);
				int required = BHR_FoolRecipes.requiredCount(ingredient, batches);
				boolean enough = BHR_FoolRecipes.countInInventory(this.minecraft.player, ingredient) >= required;
				int x = gx + this.menu.requireX() + (i % 3) * 18;
				int y = gy + this.menu.requireY() + (i / 3) * 18;
				ItemStack chosen = this.preferredOf(data, i, ingredient);
				this.drawGhost(graphics, chosen.isEmpty() ? ingredient.preview() : chosen, x, y, false, !enough);
				if (ingredient.hasChoices()) {
					graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE_SLOT_ALT, x, y, 16, 16);
				}
				this.drawCountText(graphics, String.valueOf(required), x, y, enough ? 0xFFFFFFFF : 0xFFFF5555);
			}
		}

		// 进度条
		if (total > 0 && progress > 0) {
			int filled = Math.max(0, Math.min(PROGRESS_WIDTH, progress * PROGRESS_WIDTH / total));
			if (filled > 0) {
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE_CRAFT_PROGRESS,
						PROGRESS_WIDTH, PROGRESS_HEIGHT, 0, 0,
						gx + PROGRESS_BASE_LEFT + shift, gy + PROGRESS_TOP, filled, PROGRESS_HEIGHT);
			}
		}

		// 滑槽滑块
		int maxScroll = Math.max(0, visible.size() - WorkingSurfaceEntity.RECIPE_SLOTS);
		int knobX = KNOB_LEFT;
		if (maxScroll > 0) {
			int travel = KNOB_RIGHT - KNOB_LEFT;
			knobX = KNOB_LEFT + (travel * Math.min(scroll, maxScroll) / maxScroll);
		}
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
				maxScroll > 0 ? SPRITE_SCROLLER : SPRITE_SCROLLER_DISABLED,
				gx + knobX, gy + KNOB_TOP, KNOB_WIDTH, KNOB_HEIGHT);

		// 加减按钮悬停贴图
		this.drawCountHover(graphics, mouseX, mouseY,
				gx + this.menu.countPlusX(), gy + this.menu.countPlusY(), SPRITE_COUNT_ADD);
		this.drawCountHover(graphics, mouseX, mouseY,
				gx + this.menu.countMinusX(), gy + this.menu.countMinusY(), SPRITE_COUNT_MINUS);

		// 当前倍率
		String batchesText = "x" + batches;
		graphics.text(this.font, batchesText,
				gx + 159 - 2 * shift - this.font.width(batchesText) / 2, gy + 75 - shift, 0xFF404040, false);

		// 燃料指示条
		if (this.spec.hasFuel()) {
			this.drawFuelBar(graphics, gx, gy, data.get(WorkingSurfaceEntity.DATA_FUEL));
		}

		// 两个文字按钮
		this.drawTextButton(graphics, mouseX, mouseY, gx + FAST_X, gy + BUTTON_Y, "加速制作");
		this.drawTextButton(graphics, mouseX, mouseY, gx + CRAFT_X, gy + BUTTON_Y, "开始制作");

		this.drawTooltips(graphics, mouseX, mouseY, data, visible, scroll, selectedRecipe, upgrade);
	}

	// ── 燃料 ──

	private void drawFuelBar(GuiGraphicsExtractor graphics, int gx, int gy, int fuelTicks) {
		int max = this.spec.fuelMaxTicks();
		if (fuelTicks <= 0 || max <= 0) {
			return;
		}
		int filled = Math.max(0, Math.min(FUEL_BAR_SIZE, fuelTicks * FUEL_BAR_SIZE / max));
		if (filled <= 0) {
			return;
		}
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE_LIT_PROGRESS,
				FUEL_BAR_SIZE, FUEL_BAR_SIZE, 0, FUEL_BAR_SIZE - filled,
				gx + FUEL_BAR_LEFT, gy + FUEL_BAR_TOP + FUEL_BAR_SIZE - filled,
				FUEL_BAR_SIZE, filled);
	}

	private boolean isOverFuelBar(int mouseX, int mouseY) {
		int x = this.leftPos + FUEL_BAR_LEFT;
		int y = this.topPos + FUEL_BAR_TOP;
		return mouseX >= x && mouseX < x + FUEL_BAR_SIZE && mouseY >= y && mouseY < y + FUEL_BAR_SIZE;
	}

	/** 燃料读数：以「单位」计（1 单位 = spec.fuelUnitTicks / 20 秒）。 */
	private String fuelText(int fuelTicks) {
		if (fuelTicks <= 0) {
			return Component.translatable("gui.richards-whimsy-mod.campfire.fuel.empty").getString();
		}
		float units = fuelTicks / (float) Math.max(1, this.spec.fuelUnitTicks());
		return Component.translatable("gui.richards-whimsy-mod.campfire.fuel",
				String.format(java.util.Locale.ROOT, "%.1f", units),
				this.spec.fuelMaxTicks() / Math.max(1, this.spec.fuelUnitTicks())).getString();
	}

	private ItemStack fuelIcon(ContainerData data, int fuelIndex, FuelOption fuel) {
		int id = data.get(WorkingSurfaceEntity.DATA_FUEL_PREF_START + fuelIndex);
		if (id != 0) {
			var item = BuiltInRegistries.ITEM.byId(id);
			if (item != null && item != net.minecraft.world.item.Items.AIR) {
				int position = fuel.indexOf(item);
				if (position >= 0) {
					return fuel.icon(position);
				}
			}
		}
		return fuel.icon();
	}

	private int countFuelInInventory(FuelOption fuel) {
		int total = 0;
		var inventory = this.minecraft.player.getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (fuel.accepts(stack)) {
				total += stack.getCount();
			}
		}
		return total;
	}

	// ── 提示 ──

	private void drawTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY, ContainerData data,
			List<BHR_FoolRecipes.Entry> visible, int scroll, BHR_FoolRecipe selectedRecipe,
			BHR_FoolRecipes.Entry upgrade) {
		if (this.spec.hasFuel() && this.isOverFuelBar(mouseX, mouseY)) {
			graphics.setTooltipForNextFrame(this.font,
					List.of(Component.literal(this.fuelText(data.get(WorkingSurfaceEntity.DATA_FUEL)))),
					Optional.empty(), mouseX, mouseY, null);
			return;
		}
		if (this.hoveredSlot == null) {
			return;
		}
		int index = this.hoveredSlot.index;

		// 燃料示意
		if (this.spec.hasFuel() && index >= this.menu.fuelStart && index < this.menu.fuelEnd) {
			int fuelIndex = index - this.menu.fuelStart;
			FuelOption fuel = this.spec.fuel(fuelIndex);
			if (fuel != null) {
				List<Component> lines = new ArrayList<>();
				lines.add(this.fuelIcon(data, fuelIndex, fuel).getHoverName());
				lines.add(Component.translatable("tooltip.richards-whimsy-mod.campfire.fuel_value",
						fuel.units(), fuel.units() * this.spec.fuelUnitTicks() / 20)
						.withStyle(ChatFormatting.GRAY));
				if (fuel.replaceable()) {
					lines.add(Component.translatable("tooltip.richards-whimsy-mod.workbench.alt")
							.withStyle(ChatFormatting.GRAY));
				}
				graphics.setTooltipForNextFrame(this.font, lines, Optional.empty(), mouseX, mouseY, null);
			}
			return;
		}

		// 加减键
		if (index == this.menu.countPlusSlot) {
			graphics.setTooltipForNextFrame(this.font,
					List.of(Component.translatable("tooltip.richards-whimsy-mod.workbench.batches.plus")),
					Optional.empty(), mouseX, mouseY, null);
			return;
		}
		if (index == this.menu.countMinusSlot) {
			graphics.setTooltipForNextFrame(this.font,
					List.of(Component.translatable("tooltip.richards-whimsy-mod.workbench.batches.minus")),
					Optional.empty(), mouseX, mouseY, null);
			return;
		}

		// 需求格
		if (selectedRecipe != null && index >= this.menu.requireStart && index < this.menu.requireEnd) {
			int ingredientIndex = index - this.menu.requireStart;
			if (ingredientIndex < selectedRecipe.ingredients().size()) {
				BHR_FoolIngredient ingredient = selectedRecipe.ingredients().get(ingredientIndex);
				ItemStack shown = this.preferredOf(data, ingredientIndex, ingredient);
				if (shown.isEmpty()) {
					shown = ingredient.preview();
				}
				List<Component> lines = new ArrayList<>();
				lines.add(shown.getHoverName().copy()
						.append(Component.literal(" × " + BHR_FoolRecipes.requiredCount(ingredient,
								Math.max(1, data.get(WorkingSurfaceEntity.DATA_BATCHES))))));
				if (ingredient.hasChoices()) {
					lines.add(Component.translatable("tooltip.richards-whimsy-mod.workbench.alt")
							.withStyle(ChatFormatting.GRAY));
				}
				graphics.setTooltipForNextFrame(this.font, lines, Optional.empty(), mouseX, mouseY, null);
			}
			return;
		}

		// 配方栏 / 升级栏
		BHR_FoolRecipe hovered = this.hoveredRecipe(index, visible, scroll, upgrade);
		if (hovered != null) {
			this.drawRecipeTooltip(graphics, hovered, mouseX, mouseY);
		}
	}

	private BHR_FoolRecipe hoveredRecipe(int slotIndex, List<BHR_FoolRecipes.Entry> visible, int scroll,
			BHR_FoolRecipes.Entry upgrade) {
		if (slotIndex >= this.menu.recipeStart && slotIndex < this.menu.recipeEnd) {
			int index = scroll + (slotIndex - this.menu.recipeStart);
			return index >= 0 && index < visible.size() ? visible.get(index).recipe() : null;
		}
		if (slotIndex == this.menu.upgradeSlot) {
			return upgrade != null ? upgrade.recipe() : null;
		}
		return null;
	}

	private void drawRecipeTooltip(GuiGraphicsExtractor graphics, BHR_FoolRecipe recipe, int mouseX, int mouseY) {
		List<Component> lines = new ArrayList<>();
		lines.add(recipe.isUpgrade()
				? Component.translatable("tooltip.richards-whimsy-mod.workbench.upgrade",
						recipe.upgradeTo().orElse(1))
				: this.iconOf(recipe).getHoverName());
		lines.add(Component.translatable("tooltip.richards-whimsy-mod.workbench.level", recipe.level())
				.withStyle(ChatFormatting.GRAY));
		lines.add(Component.translatable("tooltip.richards-whimsy-mod.workbench.time", recipe.timeSeconds())
				.withStyle(ChatFormatting.GRAY));
		for (BHR_FoolIngredient ingredient : recipe.ingredients()) {
			lines.add(Component.literal("· ").append(ingredient.preview().getHoverName())
					.append(" x" + ingredient.count()).withStyle(ChatFormatting.DARK_GRAY));
		}
		graphics.setTooltipForNextFrame(this.font, lines, Optional.empty(), mouseX, mouseY, null);
	}

	// ── 小工具 ──

	/** 幽灵图标：{@code whiteVeil} = 半透明白纱（已选中），{@code redVeil} = 红底（等级不足）。 */
	private void drawGhost(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y,
			boolean whiteVeil, boolean redVeil) {
		if (stack.isEmpty()) {
			return;
		}
		graphics.item(stack, x, y);
		if (redVeil) {
			graphics.fill(x, y, x + 16, y + 16, 0x80FF4040);
		} else if (whiteVeil) {
			graphics.fill(x, y, x + 16, y + 16, 0x80FFFFFF);
		}
	}

	/** 右下角的数量（白 = 够，红 = 不够）。 */
	private void drawCountText(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
		graphics.text(this.font, text, x + 17 - this.font.width(text), y + 9, color, true);
	}

	private void drawCountHover(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int x, int y,
			Identifier sprite) {
		if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18) {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 18, 18);
		}
	}

	private void drawTextButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY, int x, int y,
			String label) {
		boolean hovered = mouseX >= x && mouseX < x + BUTTON_WIDTH && mouseY >= y && mouseY < y + BUTTON_HEIGHT;
		int background = hovered ? 0xFF8B8B8B : 0xFFC6C6C6;
		graphics.fill(x, y, x + BUTTON_WIDTH, y + BUTTON_HEIGHT, background);
		graphics.fill(x, y, x + BUTTON_WIDTH, y + 1, 0xFFFFFFFF);
		graphics.fill(x, y, x + 1, y + BUTTON_HEIGHT, 0xFFFFFFFF);
		graphics.fill(x, y + BUTTON_HEIGHT - 1, x + BUTTON_WIDTH, y + BUTTON_HEIGHT, 0xFF555555);
		graphics.fill(x + BUTTON_WIDTH - 1, y, x + BUTTON_WIDTH, y + BUTTON_HEIGHT, 0xFF555555);
		graphics.text(this.font, label, x + BUTTON_WIDTH / 2 - this.font.width(label) / 2, y + 5,
				hovered ? 0xFFFFFFA0 : 0xFF404040, false);
	}

	// ── 点击转发 ──

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int gx = this.leftPos;
		int gy = this.topPos;
		double mx = event.x();
		double my = event.y();

		if (this.hit(mx, my, gx + FAST_X, gy + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
			this.forwardClick(this.menu.fastCraftSlot);
			return true;
		}
		if (this.hit(mx, my, gx + CRAFT_X, gy + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
			this.forwardClick(this.menu.craftSlot);
			return true;
		}
		if (this.hitKnob(mx, my)) {
			this.draggingKnob = true;
			this.sendScrollTo(mx);
			return true;
		}
		if (this.hit(mx, my, gx + TRACK_LEFT, gy + TRACK_TOP, TRACK_RIGHT - TRACK_LEFT, TRACK_BOTTOM - TRACK_TOP)) {
			this.scrollPage(mx < gx + (TRACK_LEFT + TRACK_RIGHT) / 2 ? -1 : 1);
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	private boolean hitKnob(double mx, double my) {
		int maxScroll = this.maxScroll();
		if (maxScroll <= 0) {
			return false;
		}
		int knobX = this.leftPos + this.knobX(maxScroll);
		return this.hit(mx, my, knobX, this.topPos + KNOB_TOP, KNOB_WIDTH, KNOB_HEIGHT);
	}

	private int knobX(int maxScroll) {
		int scroll = Math.clamp(this.menu.data().get(WorkingSurfaceEntity.DATA_SCROLL), 0, maxScroll);
		return KNOB_LEFT + (KNOB_RIGHT - KNOB_LEFT) * scroll / maxScroll;
	}

	private void sendScrollTo(double mouseX) {
		int maxScroll = this.maxScroll();
		if (maxScroll <= 0) {
			return;
		}
		double relative = (mouseX - (this.leftPos + KNOB_LEFT + KNOB_WIDTH / 2.0)) / (KNOB_RIGHT - KNOB_LEFT);
		int target = (int) Math.round(Math.clamp(relative, 0.0, 1.0) * maxScroll);
		if (target != this.lastSentScroll) {
			this.lastSentScroll = target;
			// ⚠️ 容器点击包的 button 是 signed byte，只能用 -128..127
			this.forwardClickWithButton(this.menu.scrollLeftSlot, target);
		}
	}

	/**
	 * 鼠标滚轮：指针在配方栏上时，向上滚翻一条、向下滚翻一条。
	 *
	 * <p>为什么不做文字提示：滚轮能翻列表是通用直觉（原版创造模式物品栏、各种列表都这样），
	 * 加提示反而占地方。滚动条仍然保留 —— 拖拽适合大跨度跳转，滚轮适合逐条看。
	 *
	 * <p>翻页动作走的是现成的「滑槽点击」通道：{@code scrollLeftSlot} + 按钮号 = 目标偏移
	 * （按钮号是绝对偏移，必须落在 signed byte 内 —— 配方总数上限远小于 128，安全）。
	 */
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (scrollY != 0 && this.isOverRecipeBar(mouseX, mouseY)) {
			int maxScroll = this.maxScroll();
			if (maxScroll > 0) {
				// 连续滚轮会在同一刻内触发多次，而服务端同步还没回来 —— 以本地「待确认偏移」为准，
				// 否则每次都从同一个旧值算，只有第一次生效。
				int reported = Math.clamp(this.menu.data().get(WorkingSurfaceEntity.DATA_SCROLL), 0, maxScroll);
				int base = this.wheelTarget >= 0 ? this.wheelTarget : reported;
				int target = Math.clamp(base + (scrollY > 0 ? -1 : 1), 0, maxScroll);
				if (target != base) {
					this.wheelTarget = target;
					this.forwardClickWithButton(this.menu.scrollLeftSlot, target);
				}
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	/** 滚轮刚发出、服务端还没确认的偏移（-1 = 没有待确认的滚动）。 */
	private int wheelTarget = -1;

	/** 指针是否落在配方栏那 8 个格子上。 */
	private boolean isOverRecipeBar(double mouseX, double mouseY) {
		int x = this.leftPos + 8;
		int y = this.topPos + 18;
		return mouseX >= x && mouseX < x + WorkingSurfaceEntity.RECIPE_SLOTS * 18
				&& mouseY >= y && mouseY < y + 18;
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {		if (this.draggingKnob) {
			this.sendScrollTo(event.x());
			return true;
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (this.draggingKnob) {
			this.draggingKnob = false;
			this.lastSentScroll = -1;
			return true;
		}
		return super.mouseReleased(event);
	}

	private boolean hit(double mx, double my, int x, int y, int width, int height) {
		return mx >= x && mx < x + width && my >= y && my < y + height;
	}

	@Override
	protected void slotClicked(Slot slot, int slotId, int button, net.minecraft.world.inventory.ContainerInput input) {
		if (!this.draggingKnob && this.menu.isUiSlot(slotId)) {
			this.minecraft.getSoundManager().play(
					net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
							net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
		}
		super.slotClicked(slot, slotId, button, input);
	}

	private void forwardClick(int slotIndex) {
		this.forwardClickWithButton(slotIndex, 0);
	}

	private void forwardClickWithButton(int slotIndex, int button) {
		Slot slot = this.menu.slots.get(slotIndex);
		this.slotClicked(slot, slotIndex, Math.clamp(button, -128, 127),
				net.minecraft.world.inventory.ContainerInput.PICKUP);
	}

	private void scrollPage(int direction) {
		int maxScroll = this.maxScroll();
		int current = Math.clamp(this.menu.data().get(WorkingSurfaceEntity.DATA_SCROLL), 0, maxScroll);
		int target = Math.clamp(current + direction, 0, maxScroll);
		if (target != current) {
			this.forwardClickWithButton(this.menu.scrollLeftSlot, target);
		}
	}

	// ── 数据读取 ──

	private int maxScroll() {
		return Math.max(0, this.visibleEntries(this.menu.data()).size() - WorkingSurfaceEntity.RECIPE_SLOTS);
	}

	private List<BHR_FoolRecipes.Entry> visibleEntries(ContainerData data) {
		boolean expert = data.get(WorkingSurfaceEntity.DATA_EXPERT) == 1;
		// 与实体走同一个方法：排序 + 「专家版优先、缺了退回普通版」两端天然一致
		return BHR_FoolRecipes.visibleFor(this.spec.id(), expert);
	}

	private BHR_FoolRecipe selectedRecipe(int selected) {
		if (selected < 0 || selected >= BHR_FoolRecipes.all().size()) {
			return null;
		}
		return BHR_FoolRecipes.all().get(selected).recipe();
	}

	private BHR_FoolRecipes.Entry nextUpgrade(ContainerData data, int level) {
		boolean expert = data.get(WorkingSurfaceEntity.DATA_EXPERT) == 1;
		return BHR_FoolRecipes.nextUpgradeFor(this.spec.id(), expert, level);
	}

	private ItemStack preferredOf(ContainerData data, int ingredientIndex, BHR_FoolIngredient ingredient) {
		int id = data.get(WorkingSurfaceEntity.DATA_PREF_START + ingredientIndex);
		if (id == 0) {
			return ItemStack.EMPTY;
		}
		var item = BuiltInRegistries.ITEM.byId(id);
		if (item == null || item == net.minecraft.world.item.Items.AIR) {
			return ItemStack.EMPTY;
		}
		// 防御：万一同步滞后（换配方的那一帧），别把一个不被接受的物品画上去
		ItemStack candidate = new ItemStack(item);
		return ingredient.accepts(candidate) ? candidate : ItemStack.EMPTY;
	}

	/** 配方图标：升级配方画「本台机器的方块」，族配方按选中的变种解析，其余画产物。 */
	private ItemStack iconOf(BHR_FoolRecipe recipe) {
		if (recipe.isUpgrade()) {
			return this.surfaceStack(recipe.upgradeTo().orElse(1));
		}
		return BHR_FoolRecipes.previewResult(recipe, this.familySource(recipe));
	}

	/** 族配方当前选中的「变种决定物」（需求格里右键切换的那个物种）。 */
	private net.minecraft.world.item.Item familySource(BHR_FoolRecipe recipe) {
		if (!recipe.isFamily()) {
			return null;
		}
		int id = this.menu.data().get(WorkingSurfaceEntity.DATA_PREF_START + recipe.familyFrom());
		if (id == 0) {
			return null;
		}
		var item = BuiltInRegistries.ITEM.byId(id);
		return item == null || item == net.minecraft.world.item.Items.AIR ? null : item;
	}

	/** 本台机器的方块物品堆（指定等级）。客户端也能取到方块注册表里的实例。 */
	private ItemStack surfaceStack(int level) {
		var block = BuiltInRegistries.BLOCK.getValue(RichardsWhimsyMod.id(this.spec.id()));
		return block instanceof WorkingSurfaceBlock surface ? surface.createStack(level) : ItemStack.EMPTY;
	}
}
