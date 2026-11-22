package com.rcdmake.whimsy.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Campfire;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_CampfireEntity;
import com.rcdmake.whimsy.menu.ClassBlockHeadsRebon.BHR_CampfireMenu;
import com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon.BHR_FoolIngredient;
import com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon.BHR_FoolRecipe;
import com.rcdmake.whimsy.recipe.ClassBlockHeadsRebon.BHR_FoolRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 简易篝火的界面（客户端绘制层）。
 *
 * <h2>底图与坐标</h2>
 * <p>底图是项目里早就预留好的 {@code textures/gui/container/fuel_workbench.png}（176×222）。
 * 它比制作台那张底图多了左侧一列凹槽，其余元素整体右移 18 像素 —— 所以本类的常量与
 * {@link BHR_WorkBenchScreen} 只差那几个 {@code +18}，再加三件新东西：燃料示意图标、
 * 燃料读数、燃料悬浮提示。
 *
 * <table border="1">
 *   <caption>相对制作台的位移（需求原文 → 底图像素一致）</caption>
 *   <tr><th>元素</th><th>制作台</th><th>简易篝火</th></tr>
 *   <tr><td>需求九宫格</td><td>(26,54)</td><td>(44,54)</td></tr>
 *   <tr><td>时间宝石槽</td><td>(98,90)</td><td>(116,90)</td></tr>
 *   <tr><td>进度条</td><td>(94,72)</td><td>(112,72)</td></tr>
 *   <tr><td>输出栏</td><td>(134,72)</td><td>(152,72)</td></tr>
 *   <tr><td>+ / − 键</td><td>(133,53)/(133,89)</td><td>(151,53)/(151,89)</td></tr>
 *   <tr><td>倍率提示</td><td>(159−w/2, 75)</td><td>(123−w/2, 57)（左移 36、上移 18）</td></tr>
 * </table>
 */
public class BHR_CampfireScreen extends AbstractContainerScreen<BHR_CampfireMenu> {

	private static final Identifier TEXTURE = RichardsWhimsyMod.id("textures/gui/container/fuel_workbench.png");
	private static final Identifier SPRITE_CRAFT_PROGRESS =
			RichardsWhimsyMod.id("container/workbench/craft_progress");
	private static final Identifier SPRITE_SCROLLER = RichardsWhimsyMod.id("container/workbench/scroller");
	private static final Identifier SPRITE_SCROLLER_DISABLED =
			RichardsWhimsyMod.id("container/workbench/scroller_disabled");
	private static final Identifier SPRITE_COUNT_ADD = RichardsWhimsyMod.id("container/workbench/count_add");
	private static final Identifier SPRITE_COUNT_MINUS = RichardsWhimsyMod.id("container/workbench/count_minus");
	private static final Identifier SPRITE_SLOT_ALT = RichardsWhimsyMod.id("container/workbench/slot_alt");
	/** 燃料指示条的遮罩（14×14，与底图那格同尺寸）。 */
	private static final Identifier SPRITE_LIT_PROGRESS =
			RichardsWhimsyMod.id("container/fuel_workbench/lit_progress");

	private static final int TOP_HEIGHT = BHR_CampfireMenu.ROWS * 18 + 17;   // 125
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

	/** 进度条：制作台 94 → 这里 112。 */
	private static final int PROGRESS_LEFT = 112;
	private static final int PROGRESS_TOP = 72;
	private static final int PROGRESS_WIDTH = 24;
	private static final int PROGRESS_HEIGHT = 16;

	/** 倍率提示：老位置是 (159 − w/2, 75)，左移 36、上移 18 → (123 − w/2, 57)。 */
	private static final int MULTIPLIER_X = 159 - 36;
	private static final int MULTIPLIER_Y = 75 - 18;

	/** 两个文字按钮（与制作台同位：底图这一行没动）。 */
	private static final int FAST_X = 25;
	private static final int CRAFT_X = 89;
	private static final int BUTTON_Y = 108;
	private static final int BUTTON_WIDTH = 60;
	private static final int BUTTON_HEIGHT = 18;

	/**
	 * 燃料指示条（跟熔炉火焰条同一套画法）：底图在 (27,74) 挖了一个 14×14 的槽，
	 * 用 {@code container/fuel_workbench/lit_progress}（14×14）当遮罩，按燃料比例
	 * <b>从底往上</b>长出高度。
	 */
	private static final int FUEL_BAR_LEFT = 27;
	private static final int FUEL_BAR_TOP = 74;
	private static final int FUEL_BAR_SIZE = 14;

	public BHR_CampfireScreen(BHR_CampfireMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 114 + BHR_CampfireMenu.ROWS * 18);
	}

	// ── 背景 ──

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		int xo = (this.width - this.imageWidth) / 2;
		int yo = (this.height - this.imageHeight) / 2;
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xo, yo, 0.0F, 0.0F,
				this.imageWidth, TOP_HEIGHT, 256, 256);
		graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, xo, yo + TOP_HEIGHT, 0.0F, (float) BOTTOM_V,
				this.imageWidth, BOTTOM_HEIGHT, 256, 256);
	}

	// ── 幽灵层 + 进度条 + 燃料 + 按钮 ──

	@Override
	public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractContents(graphics, mouseX, mouseY, partialTick);
		int gx = this.leftPos;
		int gy = this.topPos;

		ContainerData data = this.menu.data();
		int level = data.get(BHR_CampfireEntity.DATA_LEVEL);
		int batches = Math.max(1, data.get(BHR_CampfireEntity.DATA_BATCHES));
		int scroll = data.get(BHR_CampfireEntity.DATA_SCROLL);
		int selected = data.get(BHR_CampfireEntity.DATA_SELECTED);
		int progress = data.get(BHR_CampfireEntity.DATA_PROGRESS);
		int total = data.get(BHR_CampfireEntity.DATA_TOTAL);

		List<BHR_FoolRecipes.Entry> visible = this.visibleEntries(data);
		BHR_FoolRecipe selectedRecipe = this.selectedRecipe(selected);

		// 配方栏
		for (int i = 0; i < BHR_CampfireEntity.RECIPE_SLOTS; i++) {
			int index = scroll + i;
			if (index >= visible.size()) {
				break;
			}
			BHR_FoolRecipe recipe = visible.get(index).recipe();
			int x = gx + 8 + i * 18;
			int y = gy + 18;
			boolean levelEnough = recipe.level() <= level;
			boolean isSelected = BHR_FoolRecipes.all().indexOf(visible.get(index)) == selected;
			this.drawGhost(graphics, recipe.resultStack(), x, y, isSelected, !levelEnough);
			if (recipe.resultCount() > 1) {
				this.drawCountText(graphics, String.valueOf(recipe.resultCount()), x, y, 0xFFFFFFFF);
			}
		}

		// 燃料示意：三个凹槽各画一档燃料的图标（右键切换过的首选优先）+「背包里还有几件」
		for (int i = 0; i < BHR_CampfireMenu.FUEL_SLOTS; i++) {
			BHR_Campfire.Fuel fuel = BHR_Campfire.fuel(i);
			if (fuel == null) {
				continue;
			}
			int x = gx + BHR_CampfireMenu.FUEL_X;
			int y = gy + BHR_CampfireMenu.FUEL_TOP + i * 18;
			int owned = this.countFuelInInventory(fuel);
			this.drawGhost(graphics, this.fuelIcon(data, i, fuel), x, y, false, false);
			// 可替换遮罩：与需求九宫格同一张 slot_alt（候选 &gt; 1 才画）
			if (fuel.replaceable()) {
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE_SLOT_ALT, x, y, 16, 16);
			}
			this.drawCountText(graphics, String.valueOf(owned), x, y, owned > 0 ? 0xFFFFFFFF : 0xFFFF5555);
		}

		// 需求九宫格（右移 18）
		if (selectedRecipe != null) {
			List<BHR_FoolIngredient> ingredients = selectedRecipe.ingredients();
			for (int i = 0; i < Math.min(9, ingredients.size()); i++) {
				BHR_FoolIngredient ingredient = ingredients.get(i);
				int required = BHR_FoolRecipes.requiredCount(ingredient, batches);
				boolean enough = BHR_FoolRecipes.countInInventory(this.minecraft.player, ingredient) >= required;
				int x = gx + BHR_CampfireMenu.REQUIRE_X + (i % 3) * 18;
				int y = gy + BHR_CampfireMenu.REQUIRE_Y + (i / 3) * 18;
				ItemStack chosen = this.preferredOf(data, i, ingredient);
				this.drawGhost(graphics, chosen.isEmpty() ? ingredient.preview() : chosen, x, y, false, !enough);
				if (ingredient.hasChoices()) {
					graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE_SLOT_ALT, x, y, 16, 16);
				}
				this.drawCountText(graphics, String.valueOf(required), x, y, enough ? 0xFFFFFFFF : 0xFFFF5555);
			}
		}

		// 进度条（右移 18）
		if (total > 0 && progress > 0) {
			int filled = Math.max(0, Math.min(PROGRESS_WIDTH, progress * PROGRESS_WIDTH / total));
			if (filled > 0) {
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE_CRAFT_PROGRESS,
						PROGRESS_WIDTH, PROGRESS_HEIGHT, 0, 0,
						gx + PROGRESS_LEFT, gy + PROGRESS_TOP, filled, PROGRESS_HEIGHT);
			}
		}

		// 滑槽滑块（位置没变）
		int maxScroll = Math.max(0, visible.size() - BHR_CampfireEntity.RECIPE_SLOTS);
		int knobX = KNOB_LEFT;
		if (maxScroll > 0) {
			int travel = KNOB_RIGHT - KNOB_LEFT;
			knobX = KNOB_LEFT + (travel * Math.min(scroll, maxScroll) / maxScroll);
		}
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
				maxScroll > 0 ? SPRITE_SCROLLER : SPRITE_SCROLLER_DISABLED,
				gx + knobX, gy + KNOB_TOP, KNOB_WIDTH, KNOB_HEIGHT);

		// 加减按钮悬停贴图（右移 18）
		this.drawCountHover(graphics, mouseX, mouseY,
				gx + BHR_CampfireMenu.COUNT_PLUS_X, gy + BHR_CampfireMenu.COUNT_PLUS_Y, SPRITE_COUNT_ADD);
		this.drawCountHover(graphics, mouseX, mouseY,
				gx + BHR_CampfireMenu.COUNT_MINUS_X, gy + BHR_CampfireMenu.COUNT_MINUS_Y, SPRITE_COUNT_MINUS);

		// 倍率提示（左移 36、上移 18）
		String batchesText = "x" + batches;
		graphics.text(this.font, batchesText,
				gx + MULTIPLIER_X - this.font.width(batchesText) / 2, gy + MULTIPLIER_Y, 0xFF404040, false);

		// 燃料指示条：与熔炉火焰条同一套画法（底对齐、按比例从下往上长）
		this.drawFuelBar(graphics, gx, gy, data.get(BHR_CampfireEntity.DATA_FUEL));

		// 两个文字按钮（位置没变）
		this.drawTextButton(graphics, mouseX, mouseY, gx + FAST_X, gy + BUTTON_Y, "加速制作");
		this.drawTextButton(graphics, mouseX, mouseY, gx + CRAFT_X, gy + BUTTON_Y, "开始制作");

		this.drawTooltips(graphics, mouseX, mouseY, data, visible, scroll, selectedRecipe);
	}

	/**
	 * 燃料指示条：14×14 的遮罩，按 {@code fuel / MAX_FUEL_TICKS} 的比例
	 * 取遮罩<b>底部</b>那几行画出来（宽 14，高 = 比例），底边固定不动。
	 * <p>与制作进度条的区别：进度条是左对齐、横向长；这个是底对齐、纵向长（原版熔炉火焰条同款）。
	 */
	private void drawFuelBar(GuiGraphicsExtractor graphics, int gx, int gy, int fuelTicks) {
		int max = BHR_Campfire.MAX_FUEL_TICKS;
		if (fuelTicks <= 0 || max <= 0) {
			return;
		}
		int filled = Math.max(0, Math.min(FUEL_BAR_SIZE, fuelTicks * FUEL_BAR_SIZE / max));
		if (filled <= 0) {
			return;
		}
		// u = 0（横向不裁），v = 从遮罩底部往上取 filled 行；绘制点同样贴到格子的底边
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE_LIT_PROGRESS,
				FUEL_BAR_SIZE, FUEL_BAR_SIZE, 0, FUEL_BAR_SIZE - filled,
				gx + FUEL_BAR_LEFT, gy + FUEL_BAR_TOP + FUEL_BAR_SIZE - filled,
				FUEL_BAR_SIZE, filled);
	}

	/** 鼠标是否落在燃料指示条上（它不是槽位，所以单独判定）。 */
	private boolean isOverFuelBar(int mouseX, int mouseY) {
		int x = this.leftPos + FUEL_BAR_LEFT;
		int y = this.topPos + FUEL_BAR_TOP;
		return mouseX >= x && mouseX < x + FUEL_BAR_SIZE && mouseY >= y && mouseY < y + FUEL_BAR_SIZE;
	}

	/** 燃料读数：以「单位燃烧时间」为单位（一格 = 15 秒）。 */
	private String fuelText(int fuelTicks) {
		if (fuelTicks <= 0) {
			return Component.translatable("gui.richards-whimsy-mod.campfire.fuel.empty").getString();
		}
		String units = String.format(java.util.Locale.ROOT, "%.1f",
				fuelTicks / (float) BHR_Campfire.UNIT_BURN_TICKS);
		return Component.translatable("gui.richards-whimsy-mod.campfire.fuel", units,
				BHR_Campfire.MAX_FUEL_UNITS).getString();
	}

	/** 燃料槽的显示图标：优先用右键切换选中的候选，没选过就用该档第一个。 */
	private ItemStack fuelIcon(ContainerData data, int fuelIndex, BHR_Campfire.Fuel fuel) {
		int id = data.get(BHR_CampfireEntity.DATA_FUEL_PREF_START + fuelIndex);
		if (id != 0) {
			var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.byId(id);
			if (item != null && item != net.minecraft.world.item.Items.AIR) {
				int position = fuel.indexOf(item);
				if (position >= 0) {
					return fuel.icon(position);
				}
			}
		}
		return fuel.icon();
	}

	/** 悬浮提示：燃料指示条 / 燃料格 / 需求格 / 加减键 / 配方格。 */
	private void drawTooltips(GuiGraphicsExtractor graphics, int mouseX, int mouseY, ContainerData data,
			List<BHR_FoolRecipes.Entry> visible, int scroll, BHR_FoolRecipe selectedRecipe) {
		// 燃料指示条不是槽位，单独先判（否则下面的早退会把它吃掉）
		if (this.isOverFuelBar(mouseX, mouseY)) {
			graphics.setTooltipForNextFrame(this.font,
					List.of(Component.literal(this.fuelText(data.get(BHR_CampfireEntity.DATA_FUEL)))),
					java.util.Optional.empty(), mouseX, mouseY, null);
			return;
		}
		if (this.hoveredSlot == null) {
			return;
		}
		int index = this.hoveredSlot.index;

		// 燃料示意
		if (index >= BHR_CampfireMenu.FUEL_START && index < BHR_CampfireMenu.FUEL_END) {
			int fuelIndex = index - BHR_CampfireMenu.FUEL_START;
			BHR_Campfire.Fuel fuel = BHR_Campfire.fuel(fuelIndex);
			if (fuel != null) {
				List<Component> lines = new java.util.ArrayList<>();
				lines.add(this.fuelIcon(data, fuelIndex, fuel).getHoverName());
				lines.add(Component.translatable("tooltip.richards-whimsy-mod.campfire.fuel_value",
						fuel.units(), fuel.units() * BHR_Campfire.UNIT_BURN_SECONDS)
						.withStyle(ChatFormatting.GRAY));
				if (fuel.replaceable()) {
					lines.add(Component.translatable("tooltip.richards-whimsy-mod.workbench.alt")
							.withStyle(ChatFormatting.GRAY));
				}
				graphics.setTooltipForNextFrame(this.font, lines, java.util.Optional.empty(), mouseX, mouseY, null);
			}
			return;
		}

		// 加减键
		if (index == BHR_CampfireMenu.COUNT_PLUS_SLOT) {
			graphics.setTooltipForNextFrame(this.font,
					List.of(Component.translatable("tooltip.richards-whimsy-mod.workbench.batches.plus")),
					java.util.Optional.empty(), mouseX, mouseY, null);
			return;
		}
		if (index == BHR_CampfireMenu.COUNT_MINUS_SLOT) {
			graphics.setTooltipForNextFrame(this.font,
					List.of(Component.translatable("tooltip.richards-whimsy-mod.workbench.batches.minus")),
					java.util.Optional.empty(), mouseX, mouseY, null);
			return;
		}

		// 需求格：物品 × 需求数（可替换的再加一行右键说明）
		if (selectedRecipe != null && index >= BHR_CampfireMenu.REQUIRE_START
				&& index < BHR_CampfireMenu.REQUIRE_END) {
			int ingredientIndex = index - BHR_CampfireMenu.REQUIRE_START;
			if (ingredientIndex < selectedRecipe.ingredients().size()) {
				BHR_FoolIngredient ingredient = selectedRecipe.ingredients().get(ingredientIndex);
				ItemStack shown = this.preferredOf(data, ingredientIndex, ingredient);
				if (shown.isEmpty()) {
					shown = ingredient.preview();
				}
				List<Component> lines = new java.util.ArrayList<>();
				lines.add(shown.getHoverName().copy()
						.append(Component.literal(" × " + BHR_FoolRecipes.requiredCount(ingredient,
								Math.max(1, data.get(BHR_CampfireEntity.DATA_BATCHES))))));
				if (ingredient.hasChoices()) {
					lines.add(Component.translatable("tooltip.richards-whimsy-mod.workbench.alt")
							.withStyle(ChatFormatting.GRAY));
				}
				graphics.setTooltipForNextFrame(this.font, lines, java.util.Optional.empty(), mouseX, mouseY, null);
			}
			return;
		}

		// 配方栏 / 升级栏
		BHR_FoolRecipe hovered = this.hoveredRecipe(index, visible, scroll);
		if (hovered != null) {
			this.drawRecipeTooltip(graphics, hovered, mouseX, mouseY);
		}
	}

	/** 鼠标下的槽 → 对应配方（配方栏那 8 格）。 */
	private BHR_FoolRecipe hoveredRecipe(int slotIndex, List<BHR_FoolRecipes.Entry> visible, int scroll) {
		if (slotIndex >= BHR_CampfireMenu.RECIPE_START && slotIndex < BHR_CampfireMenu.RECIPE_END) {
			int index = scroll + (slotIndex - BHR_CampfireMenu.RECIPE_START);
			return index >= 0 && index < visible.size() ? visible.get(index).recipe() : null;
		}
		return null;
	}

	/** 配方提示框：产物名 + 需要等级 + 耗时 + 每格需求。 */
	private void drawRecipeTooltip(GuiGraphicsExtractor graphics, BHR_FoolRecipe recipe, int mouseX, int mouseY) {
		List<Component> lines = new java.util.ArrayList<>();
		lines.add(recipe.resultStack().getHoverName());
		lines.add(Component.translatable("tooltip.richards-whimsy-mod.workbench.level", recipe.level())
				.withStyle(ChatFormatting.GRAY));
		lines.add(Component.translatable("tooltip.richards-whimsy-mod.workbench.time", recipe.timeSeconds())
				.withStyle(ChatFormatting.GRAY));
		for (BHR_FoolIngredient ingredient : recipe.ingredients()) {
			lines.add(Component.literal("· ").append(ingredient.preview().getHoverName())
					.append(" x" + ingredient.count()).withStyle(ChatFormatting.DARK_GRAY));
		}
		graphics.setTooltipForNextFrame(this.font, lines, java.util.Optional.empty(), mouseX, mouseY, null);
	}

	/** 幽灵图标：{@code whiteVeil} = 半透明白纱（材料不足），{@code redVeil} = 红底（等级不足）。 */
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

	/** 右下角的数量文字（白 = 够 / 有，红 = 不够 / 没有）。 */
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

	// ── 宽按钮 / 滑槽的点击转发 ──

	private boolean draggingKnob;
	private int lastSentScroll = -1;

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int gx = this.leftPos;
		int gy = this.topPos;
		double mx = event.x();
		double my = event.y();

		if (this.hit(mx, my, gx + FAST_X, gy + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
			this.forwardClick(BHR_CampfireMenu.FAST_CRAFT_SLOT);
			return true;
		}
		if (this.hit(mx, my, gx + CRAFT_X, gy + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
			this.forwardClick(BHR_CampfireMenu.CRAFT_SLOT);
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
		int maxScroll = Math.max(0, this.visibleEntries(this.menu.data()).size() - BHR_CampfireEntity.RECIPE_SLOTS);
		if (maxScroll <= 0) {
			return false;
		}
		int knobX = this.leftPos + this.knobX(maxScroll);
		return this.hit(mx, my, knobX, this.topPos + KNOB_TOP, KNOB_WIDTH, KNOB_HEIGHT);
	}

	private int knobX(int maxScroll) {
		int scroll = Math.clamp(this.menu.data().get(BHR_CampfireEntity.DATA_SCROLL), 0, maxScroll);
		return KNOB_LEFT + (KNOB_RIGHT - KNOB_LEFT) * scroll / maxScroll;
	}

	/** 把鼠标横坐标换算成滚动偏移（button 号必须落在 signed byte 内，配方总量远小于此）。 */
	private void sendScrollTo(double mouseX) {
		int maxScroll = Math.max(0, this.visibleEntries(this.menu.data()).size() - BHR_CampfireEntity.RECIPE_SLOTS);
		if (maxScroll <= 0) {
			return;
		}
		double relative = (mouseX - (this.leftPos + KNOB_LEFT + KNOB_WIDTH / 2.0)) / (KNOB_RIGHT - KNOB_LEFT);
		int target = (int) Math.round(Math.clamp(relative, 0.0, 1.0) * maxScroll);
		if (target != this.lastSentScroll) {
			this.lastSentScroll = target;
			this.forwardClickWithButton(BHR_CampfireMenu.SCROLL_LEFT_SLOT, target);
		}
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (this.draggingKnob) {
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

	/** 界面动作被点到时放一声按钮音效（拖拽中不响）。 */
	@Override
	protected void slotClicked(Slot slot, int slotId, int button, net.minecraft.world.inventory.ContainerInput input) {
		if (!this.draggingKnob && BHR_CampfireMenu.isUiSlot(slotId)) {
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
		int maxScroll = Math.max(0, this.visibleEntries(this.menu.data()).size() - BHR_CampfireEntity.RECIPE_SLOTS);
		int current = Math.clamp(this.menu.data().get(BHR_CampfireEntity.DATA_SCROLL), 0, maxScroll);
		int target = Math.clamp(current + direction, 0, maxScroll);
		if (target != current) {
			this.forwardClickWithButton(BHR_CampfireMenu.SCROLL_LEFT_SLOT, target);
		}
	}

	// ── 数据读取助手 ──

	private List<BHR_FoolRecipes.Entry> visibleEntries(ContainerData data) {
		boolean expert = data.get(BHR_CampfireEntity.DATA_EXPERT) == 1;
		List<BHR_FoolRecipes.Entry> entries = new java.util.ArrayList<>();
		for (BHR_FoolRecipes.Entry entry : BHR_FoolRecipes.forWorkbench(BHR_Campfire.REGISTRY_NAME)) {
			if (entry.recipe().expert() == expert && !entry.recipe().isUpgrade()) {
				entries.add(entry);
			}
		}
		return entries;
	}

	private BHR_FoolRecipe selectedRecipe(int selected) {
		if (selected < 0 || selected >= BHR_FoolRecipes.all().size()) {
			return null;
		}
		return BHR_FoolRecipes.all().get(selected).recipe();
	}

	/** 背包里符合该档燃料的物品总数（只用于显示，扣料在服务端做）。 */
	private int countFuelInInventory(BHR_Campfire.Fuel fuel) {
		if (this.minecraft == null || this.minecraft.player == null) {
			return 0;
		}
		var inventory = this.minecraft.player.getInventory();
		int total = 0;
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (!stack.isEmpty() && fuel.accepts(stack)) {
				total += stack.getCount();
			}
		}
		return total;
	}

	/** 读同步过来的「某个需求格的首选物品」（0 = 没指定）。 */
	private ItemStack preferredOf(ContainerData data, int ingredientIndex, BHR_FoolIngredient ingredient) {
		int id = data.get(BHR_CampfireEntity.DATA_PREF_START + ingredientIndex);
		if (id == 0) {
			return ItemStack.EMPTY;
		}
		var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.byId(id);
		if (item == null || item == net.minecraft.world.item.Items.AIR) {
			return ItemStack.EMPTY;
		}
		ItemStack candidate = new ItemStack(item);
		return ingredient.accepts(candidate) ? candidate : ItemStack.EMPTY;
	}
}
