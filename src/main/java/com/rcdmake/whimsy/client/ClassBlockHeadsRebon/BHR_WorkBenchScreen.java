package com.rcdmake.whimsy.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_WorkBench;
import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_WorkBenchEntity;
import com.rcdmake.whimsy.menu.ClassBlockHeadsRebon.BHR_WorkBenchMenu;
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
 * 制作台界面（客户端绘制层）。
 *
 * <h2>画什么</h2>
 * <ol>
 *   <li>面板：照原版大箱子切图（上半 125px + 下半 96px）；</li>
 *   <li>幽灵图标：配方栏 8 格、升级栏、需求九宫格 —— 全部由本类按同步状态现画（服务端槽是空的）；</li>
 *   <li>进度条（{@code sprites/container/workbench/craft_progress.png}）、
 *       滑槽滑块（{@code scroller.png} / {@code scroller_disabled.png}）、
 *       加减按钮悬停贴图（{@code count_add.png} / {@code count_minus.png}）；</li>
 *   <li>两个文字按钮「加速制作」「开始制作」（位于大箱子最后一行）。</li>
 * </ol>
 *
 * <h2>坐标来源</h2>
 * <p>全部按需求给的像素坐标（以贴图左上角为原点）；原版槽位是「18×18 含轮廓、16×16 内容」，
 * 所以需求里的 {@code (7,17)} 这类「含轮廓左上角」换算成槽位坐标就是 {@code +1}：
 * 配方栏 {@code (8,18)}、升级栏 {@code (152,18)}、九宫格 {@code (26,54)} 起、结果栏 {@code (152,72)}、
 * 水晶格 {@code (116,90)}、加减 {@code (152,54)/(152,90)}。
 */
public class BHR_WorkBenchScreen extends AbstractContainerScreen<BHR_WorkBenchMenu> {

	private static final Identifier TEXTURE = RichardsWhimsyMod.id("textures/gui/container/workbench.png");
	private static final Identifier SPRITE_CRAFT_PROGRESS =
			RichardsWhimsyMod.id("container/workbench/craft_progress");
	private static final Identifier SPRITE_SCROLLER = RichardsWhimsyMod.id("container/workbench/scroller");
	private static final Identifier SPRITE_SCROLLER_DISABLED =
			RichardsWhimsyMod.id("container/workbench/scroller_disabled");
	private static final Identifier SPRITE_COUNT_ADD = RichardsWhimsyMod.id("container/workbench/count_add");
	private static final Identifier SPRITE_COUNT_MINUS = RichardsWhimsyMod.id("container/workbench/count_minus");
	private static final Identifier SPRITE_SLOT_UPGRADE = RichardsWhimsyMod.id("container/workbench/slot_upgrade");
	private static final Identifier SPRITE_SLOT_ALT = RichardsWhimsyMod.id("container/workbench/slot_alt");

	private static final int TOP_HEIGHT = BHR_WorkBenchMenu.ROWS * 18 + 17;
	private static final int BOTTOM_V = 126;
	private static final int BOTTOM_HEIGHT = 96;

	/** 滑槽与滑块的运动范围。 */
	private static final int TRACK_LEFT = 7;
	private static final int TRACK_TOP = 35;
	private static final int TRACK_RIGHT = 150;
	private static final int TRACK_BOTTOM = 48;
	private static final int KNOB_LEFT = 8;
	/** 滑块按原图像素尺寸画（scroller.png = 15×12），所以右边界是「右下角 149,47」减去自身尺寸。 */
	private static final int KNOB_WIDTH = 15;
	private static final int KNOB_HEIGHT = 12;
	private static final int KNOB_RIGHT = 149 - KNOB_WIDTH + 1;
	private static final int KNOB_TOP = 36;

	/** 进度条逻辑区域（craft_progress.png 原图就是 24×16，按原尺寸画、只裁宽度）。 */
	private static final int PROGRESS_LEFT = 94;
	private static final int PROGRESS_TOP = 72;
	private static final int PROGRESS_WIDTH = 24;
	private static final int PROGRESS_HEIGHT = 16;

	/** 加减按钮（含轮廓左上角 (133,53)/(133,89) → 槽位坐标 +1）。 */
	private static final int COUNT_PLUS_X = 133;
	private static final int COUNT_PLUS_Y = 53;
	private static final int COUNT_MINUS_X = 133;
	private static final int COUNT_MINUS_Y = 89;

	/** 两个文字按钮。 */
	private static final int FAST_X = 25;
	private static final int CRAFT_X = 89;
	private static final int BUTTON_Y = 108;
	private static final int BUTTON_WIDTH = 60;
	private static final int BUTTON_HEIGHT = 18;

	public BHR_WorkBenchScreen(BHR_WorkBenchMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title, 176, 114 + BHR_WorkBenchMenu.ROWS * 18);
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

	// ── 幽灵层 + 进度条 + 按钮 ──

	@Override
	public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractContents(graphics, mouseX, mouseY, partialTick);
		int gx = this.leftPos;
		int gy = this.topPos;

		ContainerData data = this.menu.data();
		int level = data.get(BHR_WorkBenchEntity.DATA_LEVEL);
		int batches = Math.max(1, data.get(BHR_WorkBenchEntity.DATA_BATCHES));
		int scroll = data.get(BHR_WorkBenchEntity.DATA_SCROLL);
		int selected = data.get(BHR_WorkBenchEntity.DATA_SELECTED);
		int progress = data.get(BHR_WorkBenchEntity.DATA_PROGRESS);
		int total = data.get(BHR_WorkBenchEntity.DATA_TOTAL);

		List<BHR_FoolRecipes.Entry> visible = this.visibleEntries(data);
		BHR_FoolRecipe selectedRecipe = this.selectedRecipe(selected);

		// 配方栏
		for (int i = 0; i < BHR_WorkBenchEntity.RECIPE_SLOTS; i++) {
			int index = scroll + i;
			if (index >= visible.size()) {
				break;
			}
			BHR_FoolRecipe recipe = visible.get(index).recipe();
			int x = gx + 8 + i * 18;
			int y = gy + 18;
			ItemStack icon = iconOf(recipe);
			boolean levelEnough = recipe.level() <= level;
			boolean isSelected = BHR_FoolRecipes.all().indexOf(visible.get(index)) == selected;
			this.drawGhost(graphics, icon, x, y, isSelected, !levelEnough);
			if (!recipe.isUpgrade() && recipe.resultCount() > 1) {
				this.drawRequirementCount(graphics, String.valueOf(recipe.resultCount()), x, y, 0xFFFFFFFF);
			}
		}

		// 升级栏：下一级的制作台图标（满级不显示）
		BHR_FoolRecipes.Entry upgrade = this.nextUpgrade(data, level);
		if (upgrade != null) {
			int target = upgrade.recipe().upgradeTo().orElse(level);
			this.drawGhost(graphics, BHR_WorkBench.createStack(Math.min(target, BHR_WorkBench.LEVEL_MAX)),
					gx + 152, gy + 18, false, false);
			// 升级栏专用遮罩：贴图 16×16，从槽位左上角起画（左移 1、上移 1）
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE_SLOT_UPGRADE,
					gx + 152, gy + 18, 16, 16);
		}

		// 需求九宫格
		if (selectedRecipe != null) {
			List<BHR_FoolIngredient> ingredients = selectedRecipe.ingredients();
			for (int i = 0; i < Math.min(9, ingredients.size()); i++) {
				BHR_FoolIngredient ingredient = ingredients.get(i);
				int required = BHR_FoolRecipes.requiredCount(ingredient, batches);
				boolean enough = BHR_FoolRecipes.countInInventory(this.minecraft.player, ingredient) >= required;
				int x = gx + 26 + (i % 3) * 18;
				int y = gy + 54 + (i / 3) * 18;
				// 首选物品（右键切换）优先显示；没指定就用默认第一候选
				ItemStack chosen = this.preferredOf(data, i, ingredient);
				this.drawGhost(graphics, chosen.isEmpty() ? ingredient.preview() : chosen, x, y, false, !enough);
				if (ingredient.hasChoices()) {
					// 可替换遮罩：贴图 16×16，从格子左上角起画（左移 1、上移 1）
					graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE_SLOT_ALT, x, y, 16, 16);
				}
				this.drawRequirementCount(graphics, String.valueOf(required), x, y,
						enough ? 0xFFFFFFFF : 0xFFFF5555);
			}
		}

		// 进度条
		if (total > 0 && progress > 0) {
			int filled = Math.max(0, Math.min(PROGRESS_WIDTH, progress * PROGRESS_WIDTH / total));
			if (filled > 0) {
				// 子矩形裁切（不缩放）：原图 24×16，只取左边 filled 像素
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITE_CRAFT_PROGRESS,
						PROGRESS_WIDTH, PROGRESS_HEIGHT, 0, 0,
						gx + PROGRESS_LEFT, gy + PROGRESS_TOP, filled, PROGRESS_HEIGHT);
			}
		}

		// 滑槽滑块
		int maxScroll = Math.max(0, visible.size() - BHR_WorkBenchEntity.RECIPE_SLOTS);
		int knobX = KNOB_LEFT;
		if (maxScroll > 0) {
			int travel = KNOB_RIGHT - KNOB_LEFT;
			knobX = KNOB_LEFT + (travel * Math.min(scroll, maxScroll) / maxScroll);
		}
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
				maxScroll > 0 ? SPRITE_SCROLLER : SPRITE_SCROLLER_DISABLED,
				gx + knobX, gy + KNOB_TOP, KNOB_WIDTH, KNOB_HEIGHT);

		// 加减按钮：常态已画在底图里，只在悬停时叠一张专门的贴图（原图 18×18）
		this.drawCountHover(graphics, mouseX, mouseY, gx + COUNT_PLUS_X, gy + COUNT_PLUS_Y, SPRITE_COUNT_ADD);
		this.drawCountHover(graphics, mouseX, mouseY, gx + COUNT_MINUS_X, gy + COUNT_MINUS_Y, SPRITE_COUNT_MINUS);

		// 当前倍率：两个箭头中间那一格
		String batchesText = "x" + batches;
		graphics.text(this.font, batchesText,
				gx + COUNT_PLUS_X + 9 - this.font.width(batchesText) / 2 + 17, gy + 75, 0xFF404040, false);

		// 两个文字按钮
		this.drawTextButton(graphics, mouseX, mouseY, gx + FAST_X, gy + BUTTON_Y, "加速制作");
		this.drawTextButton(graphics, mouseX, mouseY, gx + CRAFT_X, gy + BUTTON_Y, "开始制作");

		// 悬浮提示：加减按钮 + 配方栏 / 升级栏
		if (this.hoveredSlot != null) {
			if (this.hoveredSlot.index == BHR_WorkBenchMenu.COUNT_PLUS_SLOT) {
				graphics.setTooltipForNextFrame(this.font,
						List.of(Component.translatable("tooltip.richards-whimsy-mod.workbench.batches.plus")),
						java.util.Optional.empty(), mouseX, mouseY, null);
			} else if (this.hoveredSlot.index == BHR_WorkBenchMenu.COUNT_MINUS_SLOT) {
				graphics.setTooltipForNextFrame(this.font,
						List.of(Component.translatable("tooltip.richards-whimsy-mod.workbench.batches.minus")),
						java.util.Optional.empty(), mouseX, mouseY, null);
			}
		}
		// 需求格：显示「物品 × 需求数」，可替换的再加一行右键说明
		if (this.hoveredSlot != null && selectedRecipe != null
				&& this.hoveredSlot.index >= BHR_WorkBenchMenu.REQUIRE_START
				&& this.hoveredSlot.index < BHR_WorkBenchMenu.REQUIRE_END) {
			int ingredientIndex = this.hoveredSlot.index - BHR_WorkBenchMenu.REQUIRE_START;
			if (ingredientIndex < selectedRecipe.ingredients().size()) {
				BHR_FoolIngredient ingredient = selectedRecipe.ingredients().get(ingredientIndex);
				ItemStack shown = this.preferredOf(data, ingredientIndex, ingredient);
				if (shown.isEmpty()) {
					shown = ingredient.preview();
				}
				List<Component> lines = new java.util.ArrayList<>();
				lines.add(shown.getHoverName().copy()
						.append(Component.literal(" × " + BHR_FoolRecipes.requiredCount(ingredient, batches))));
				if (ingredient.hasChoices()) {
					lines.add(Component.translatable("tooltip.richards-whimsy-mod.workbench.alt")
							.withStyle(ChatFormatting.GRAY));
				}
				graphics.setTooltipForNextFrame(this.font, lines, java.util.Optional.empty(), mouseX, mouseY, null);
			}
		}
		if (this.hoveredSlot != null) {
			BHR_FoolRecipe hovered = this.hoveredRecipe(this.hoveredSlot.index, visible, scroll, selectedRecipe, upgrade);
			if (hovered != null) {
				this.drawRecipeTooltip(graphics, hovered, mouseX, mouseY);
			}
		}
	}

	/** 鼠标下的槽 → 对应配方（配方栏 / 升级栏），其余返回 null。 */
	private BHR_FoolRecipe hoveredRecipe(int slotIndex, List<BHR_FoolRecipes.Entry> visible, int scroll,
			BHR_FoolRecipe selectedRecipe, BHR_FoolRecipes.Entry upgrade) {
		if (slotIndex >= BHR_WorkBenchMenu.RECIPE_START && slotIndex < BHR_WorkBenchMenu.RECIPE_END) {
			int index = scroll + (slotIndex - BHR_WorkBenchMenu.RECIPE_START);
			return index >= 0 && index < visible.size() ? visible.get(index).recipe() : null;
		}
		if (slotIndex == BHR_WorkBenchMenu.UPGRADE_SLOT) {
			return upgrade != null ? upgrade.recipe() : null;
		}
		return null;
	}

	/** 配方提示框：产物名 + 需要等级 + 耗时 + 每格需求。 */
	private void drawRecipeTooltip(GuiGraphicsExtractor graphics, BHR_FoolRecipe recipe, int mouseX, int mouseY) {
		List<Component> lines = new java.util.ArrayList<>();
		lines.add(recipe.isUpgrade()
				? Component.translatable("tooltip.richards-whimsy-mod.workbench.upgrade",
						recipe.upgradeTo().orElse(1))
				: recipe.resultStack().getHoverName());
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

	/** 幽灵图标：{@code fade} = 半透明白纱（材料不足），{@code red} = 红底（等级不足）。 */
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

	/** 右下角的需求数字（白 = 够，红 = 不够）。 */
	private void drawRequirementCount(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
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

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		int gx = this.leftPos;
		int gy = this.topPos;
		double mx = event.x();
		double my = event.y();

		if (this.hit(mx, my, gx + FAST_X, gy + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
			this.forwardClick(BHR_WorkBenchMenu.FAST_CRAFT_SLOT);
			return true;
		}
		if (this.hit(mx, my, gx + CRAFT_X, gy + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)) {
			this.forwardClick(BHR_WorkBenchMenu.CRAFT_SLOT);
			return true;
		}
		// 点在滑块上 → 开始拖拽；点在滑槽其余位置 → 翻一页
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

	/** 拖拽用的状态与命中判定。 */
	private boolean draggingKnob;
	private int lastSentScroll = -1;

	private boolean hitKnob(double mx, double my) {
		int maxScroll = Math.max(0, this.visibleEntries(this.menu.data()).size() - BHR_WorkBenchEntity.RECIPE_SLOTS);
		if (maxScroll <= 0) {
			return false;
		}
		int knobX = this.leftPos + this.knobX(maxScroll);
		return this.hit(mx, my, knobX, this.topPos + KNOB_TOP, KNOB_WIDTH, KNOB_HEIGHT);
	}

	private int knobX(int maxScroll) {
		int scroll = Math.clamp(this.menu.data().get(BHR_WorkBenchEntity.DATA_SCROLL), 0, maxScroll);
		return KNOB_LEFT + (KNOB_RIGHT - KNOB_LEFT) * scroll / maxScroll;
	}

	/** 把鼠标的横坐标换算成滚动偏移并发给服务端（按钮号 ≥1000 表示「直接设成这个偏移」）。 */
	private void sendScrollTo(double mouseX) {
		int maxScroll = Math.max(0, this.visibleEntries(this.menu.data()).size() - BHR_WorkBenchEntity.RECIPE_SLOTS);
		if (maxScroll <= 0) {
			return;
		}
		double relative = (mouseX - (this.leftPos + KNOB_LEFT + KNOB_WIDTH / 2.0)) / (KNOB_RIGHT - KNOB_LEFT);
		int target = (int) Math.round(Math.clamp(relative, 0.0, 1.0) * maxScroll);
		if (target != this.lastSentScroll) {
			this.lastSentScroll = target;
			// ⚠️ 容器点击包的 button 是 signed byte，只能用 -128..127；
			// 配方总数最多几十条，偏移 ≤15，所以直接放绝对偏移即可（踩过一次崩服）。
			this.forwardClickWithButton(BHR_WorkBenchMenu.SCROLL_LEFT_SLOT, target);
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

	/** 界面动作（配方格、加减、按钮、滑槽）被点到时放一声按钮音效（拖拽中不响）。 */
	@Override
	protected void slotClicked(Slot slot, int slotId, int button, net.minecraft.world.inventory.ContainerInput input) {
		if (!this.draggingKnob && BHR_WorkBenchMenu.isUiSlot(slotId)) {
			this.minecraft.getSoundManager().play(
					net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
							net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
		}
		super.slotClicked(slot, slotId, button, input);
	}

	/** 把一次点击伪装成对应假槽的拾取点击，服务端在 {@code clicked()} 里翻译成界面动作。 */
	private void forwardClick(int slotIndex) {
		this.forwardClickWithButton(slotIndex, 0);
	}

	/** 同上，但带上按钮号（滑槽用绝对偏移，范围必须落在 signed byte 内）。 */
	private void forwardClickWithButton(int slotIndex, int button) {
		Slot slot = this.menu.slots.get(slotIndex);
		this.slotClicked(slot, slotIndex, Math.clamp(button, -128, 127),
				net.minecraft.world.inventory.ContainerInput.PICKUP);
	}

	private void scrollPage(int direction) {
		int maxScroll = Math.max(0, this.visibleEntries(this.menu.data()).size() - BHR_WorkBenchEntity.RECIPE_SLOTS);
		int current = Math.clamp(this.menu.data().get(BHR_WorkBenchEntity.DATA_SCROLL), 0, maxScroll);
		int target = Math.clamp(current + direction, 0, maxScroll);
		if (target != current) {
			this.forwardClickWithButton(BHR_WorkBenchMenu.SCROLL_LEFT_SLOT, target);
		}
	}


	// ── 数据读取助手 ──

	private List<BHR_FoolRecipes.Entry> visibleEntries(ContainerData data) {
		boolean expert = data.get(BHR_WorkBenchEntity.DATA_EXPERT) == 1;
		List<BHR_FoolRecipes.Entry> entries = new java.util.ArrayList<>();
		for (BHR_FoolRecipes.Entry entry : BHR_FoolRecipes.forWorkbench(BHR_WorkBench.REGISTRY_NAME)) {
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

	private BHR_FoolRecipes.Entry nextUpgrade(ContainerData data, int level) {
		boolean expert = data.get(BHR_WorkBenchEntity.DATA_EXPERT) == 1;
		BHR_FoolRecipes.Entry best = null;
		for (BHR_FoolRecipes.Entry entry : BHR_FoolRecipes.forWorkbench(BHR_WorkBench.REGISTRY_NAME)) {
			BHR_FoolRecipe recipe = entry.recipe();
			if (recipe.expert() != expert) {
				continue;
			}
			if (!recipe.isUpgrade() || recipe.upgradeTo().isEmpty()) {
				continue;
			}
			int target = recipe.upgradeTo().get();
			if (target <= level) {
				continue;
			}
			if (best == null || target < best.recipe().upgradeTo().get()) {
				best = entry;
			}
		}
		return best;
	}

	/** 读同步过来的「某个需求格的首选物品」（0 = 没指定）。 */
	private ItemStack preferredOf(ContainerData data, int ingredientIndex, BHR_FoolIngredient ingredient) {
		int id = data.get(BHR_WorkBenchEntity.DATA_PREF_START + ingredientIndex);
		if (id == 0) {
			return ItemStack.EMPTY;
		}
		var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.byId(id);
		if (item == null || item == net.minecraft.world.item.Items.AIR) {
			return ItemStack.EMPTY;
		}
		// 防御：万一同步滞后（换配方的那一帧），别把一个不被接受的物品画上去
		ItemStack candidate = new ItemStack(item);
		return ingredient.accepts(candidate) ? candidate : ItemStack.EMPTY;
	}

	private ItemStack iconOf(BHR_FoolRecipe recipe) {
		if (recipe.isUpgrade()) {
			return BHR_WorkBench.createStack(Math.min(recipe.upgradeTo().orElse(1), BHR_WorkBench.LEVEL_MAX));
		}
		return recipe.resultStack();
	}
}
