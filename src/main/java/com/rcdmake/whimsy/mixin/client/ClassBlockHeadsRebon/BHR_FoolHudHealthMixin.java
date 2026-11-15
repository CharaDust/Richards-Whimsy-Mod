package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_Energy;
import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_Environment;
import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_FoodSync;
import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_FoolHealth;
import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_FoolHudArrows;
import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_Happiness;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 方块愚者 HUD：<b>左侧生命条（barL）</b> + <b>右侧箭头（barR）</b>，替换原版心形图标。
 *
 * <h2>贴图规格（实测 png 全部是 80×9，同一张画布，直接按坐标叠）</h2>
 * <pre>
 *   左侧：图标 0 … 8 ｜ 长条 14 … 63（50 px）｜ 吸收框 13 … 64（52 px）
 *   箭头：65 … 79（同一张左侧画布，紧挨着血条右边；每个箭头 3 px，最多 5 个）
 *   右侧：长条 16 … 65（50 px）、图标 71 … 79（9 px）；箭头区 0 … 14
 * </pre>
 * 本 mixin 只按子矩形取图（{@code u}/{@code w}），不依赖画布其余部分的透明度。
 *
 * <h2>右侧资源条（barR）自下而上的四行</h2>
 * <ol>
 *   <li><b>饥饿</b>（原版食物行，{@code yLineBase}）：绿条 + 饱和度金边 + 消耗度叠加；</li>
 *   <li><b>精力</b>（{@code yLineBase - 10}）：黄条，0~20000，每 tick −1；</li>
 *   <li><b>环境</b>（{@code yLineBase - 20}）：0~100，按固定速率朝目标值靠拢；</li>
 *   <li><b>氧气</b>（{@code yLineBase - 30}）：蓝条，只在需要时显示。</li>
 * </ol>
 *
 * <h2>左侧绘制顺序（底 → 顶）</h2>
 * <ol>
 *   <li>正常：{@code empty} 铺满 → {@code full}（宽度 = 生命比例，连续值）；</li> *   <li>濒死：左侧 = <b>剩余濒死值</b>（{@code empty} 样式），右侧 = <b>已消耗部分</b>
 *       （{@code death} 样式）；12/20 就是「左 30 px 空槽 + 右 20 px 黑」，
 *       濒死值越少黑色吃得越多；</li>
 *   <li>{@code blinking}：原版节奏，扣血盖丢失段、回血盖新增段；</li>
 *   <li>{@code absorbing}：有伤害吸收时画整圈线框；</li>
 *   <li>图标：底图标 {@code icon_health}（濒死时换成 {@code icon_death}）；
 *       {@code icon_health_blinking} 与 {@code icon_health_hardcore} 都是<b>叠加层</b>，
 *       不替换底图标。</li>
 * </ol>
 *
 * <h2>箭头：左右各一套素材，不能混用</h2>
 * 数量与透明度由 {@link BHR_FoolHudArrows} 算好（等级叠加、自然回血、瞬时伤害、
 * 正负交替、渐变闪烁），这里只负责按 alpha 画图：
 * <ul>
 *   <li><b>左侧</b>（血量 / 快乐值行）→ {@code bhr_barl_arrow_N / _-N}，画布 x 65…79，
 *       正向朝右、负向朝左；</li>
 *   <li><b>右侧</b>（饥饿 / 精力 / 氧气行）→ {@code bhr_barr_arrow_N / _-N}，画布 x 0…14，
 *       <b>正向 = 增加、朝左</b>，<b>负向 = 减少、朝右</b>。</li>
 * </ul>
 * 文件名里的正负数就是语义（{@code _N} 加、{@code -_N} 减），朝向跟着各自那侧长条的
 * 填充方向走，所以两套的朝向正好相反 —— 右侧误用左侧素材会同时出现「方向反了」和
 * 「箭头飞到长条外侧」两个问题（左侧素材的像素在 65…79，按 0 起画就会落到最左边）。
 *
 * <h2>为什么注入 {@code extractHearts} 而不是整个 {@code extractPlayerHealth}</h2>
 * 原版把「闪烁计时、displayHealth 延迟、护甲条、载具血量」都算在
 * {@code extractPlayerHealth} 里，只有心形是子方法 {@code extractHearts} 画的。
 * 只接管子方法，就能保留护甲条与原版那套闪烁计时（参数里已经带过来了：
 * {@code xLeft} / {@code yLineBase} / {@code oldHealth} / {@code blink}），
 * 不用 shadow 任何私有字段。
 */
@Mixin(value = Hud.class, priority = 2000)
public abstract class BHR_FoolHudHealthMixin {

	// ── 画布与切片坐标（全部按 80×9 的画布实测） ──

	private static final int ICON_U = 0;
	private static final int ICON_W = 9;
	private static final int BAR_U = 14;
	private static final int BAR_W = 50;
	private static final int FRAME_U = 13;
	private static final int FRAME_W = 52;
	private static final int ARROW_U = 65;
	private static final int ARROW_W = 15; // 65 … 79
	private static final int SHEET_W = 80;
	private static final int SHEET_H = 9;
	private static final Identifier EMPTY = hud("bhr_barl_empty");
	private static final Identifier FULL = hud("bhr_barl_full");
	private static final Identifier DEATH = hud("bhr_barl_death");
	private static final Identifier BLINKING = hud("bhr_barl_blinking");
	private static final Identifier ABSORBING = hud("bhr_barl_super_absorbing");
	private static final Identifier ICON_HEALTH = hud("bhr_barl_icon_health");
	private static final Identifier ICON_HEALTH_BLINKING = hud("bhr_barl_icon_health_blinking");
	private static final Identifier ICON_HEALTH_HARDCORE = hud("bhr_barl_icon_health_hardcore");
	private static final Identifier ICON_DEATH = hud("bhr_barl_icon_death");
	private static final Identifier ICON_HAPPINESS = hud("bhr_barl_icon_happiness");
	private static final Identifier ICON_ARMOR = hud("bhr_barl_icon_armor");

	/** 三条左侧资源条的行距（与原版护甲/血量间距一致：10 px）。 */
	private static final int ROW_STEP = 10;

	/** 生命值行之上依次是快乐值、盔甲值。 */
	private static final int HAPPINESS_ROW = ROW_STEP;
	private static final int ARMOR_ROW = ROW_STEP * 2;

	/** 左侧箭头素材：正向 {@code bhr_barl_arrow_N}、负向 {@code bhr_barl_arrow_-N}（1~5）。 */
	private static final Identifier[] ARROWS_POSITIVE = new Identifier[BHR_FoolHudArrows.MAX_ARROWS + 1];
	private static final Identifier[] ARROWS_NEGATIVE = new Identifier[BHR_FoolHudArrows.MAX_ARROWS + 1];

	/**
	 * 右侧箭头素材：正向 {@code bhr_barr_arrow_N}、负向 {@code bhr_barr_arrow_-N}（1~5）。
	 *
	 * <p><b>左右两套箭头不能混用</b>：文件名里的正负数就是语义 ——
	 * {@code _N} = <b>增加</b>、{@code -_N} = <b>减少</b>，而箭头<b>朝向</b>由各自那一侧
	 * 长条的填充方向决定：
	 * <ul>
	 *   <li>左侧（barL，从左往右填）：{@code _N} 朝<b>右</b>、{@code -_N} 朝<b>左</b>；</li>
	 *   <li>右侧（barR，从右往左填）：{@code _N} 朝<b>左</b>、{@code -_N} 朝<b>右</b>。</li>
	 * </ul>
	 * 所以「右侧某值是减少」的时候画的是 {@code bhr_barr_arrow_-N}（朝右），
	 * 与左侧那套的朝向正好相反 —— 混用就会出现「方向反了」。
	 */
	private static final Identifier[] ARROWS_R_POSITIVE = new Identifier[BHR_FoolHudArrows.MAX_ARROWS + 1];
	private static final Identifier[] ARROWS_R_NEGATIVE = new Identifier[BHR_FoolHudArrows.MAX_ARROWS + 1];

	// ── 右侧 barR（食物条）：画布内 箭头 0…14、长条 16…65、图标 71…79，画布右端对齐原版食物行右边 ──

	private static final int BAR_R_U = 16;
	private static final int BAR_R_W = 50;
	private static final int ICON_R_U = 71;
	private static final int ICON_R_W = 9;
	private static final int SUPER_R_U = 15;
	private static final int SUPER_R_W = 52;

	private static final Identifier ICON_FOOD = hud("bhr_barr_icon_food");
	/** 右侧专用的长条图层（barL 的 `empty/full` 在 14…63，barR 的在 16…65，不能混用）。 */
	private static final Identifier EMPTY_R = hud("bhr_barr_empty");
	private static final Identifier FULL_R = hud("bhr_barr_full");
	/** 氧气条的 full 层（蓝色）。 */
	private static final Identifier FULL_AIR = hud("bhr_barr_full_air");
	private static final Identifier ICON_AIR = hud("bhr_barr_icon_air");
	/** 精力条图标（长条 full 层复用饥饿条的绿色 {@code bhr_barr_full}）。 */
	private static final Identifier ICON_ENERGY = hud("bhr_barr_icon_energy");
	/** 环境值图标（长条同样复用绿色 {@code bhr_barr_full}）。 */
	private static final Identifier ICON_ENVIRONMENT = hud("bhr_barr_icon_environment");
	/** 消耗度叠加（白色半密度，盖在饥饿条上）。 */
	private static final Identifier SUPER_EXHAUSTION = hud("bhr_barr_super_exhaustion");
	/** 饱和度金边（黄，比长条大一圈）。 */
	private static final Identifier SUPER_FOOD = hud("bhr_barr_super_food");

	/** barR 画布里的箭头区（0…14）。 */
	private static final int ARROW_R_U = 0;
	private static final int ARROW_R_W = 15;

	/** 右侧资源条的行距（与原版护甲/血量间距一致：10 px）。 */
	private static final int ROW_STEP_R = 10;
	/**
	 * 右侧四行，从下往上：<b>饥饿（基准行）→ 精力 → 环境 → 氧气</b>。
	 *
	 * <p>四个位置都已实装（环境值 2026-11-15 落地）；以后再加一条资源时，
	 * 把氧气行往上挪一格即可。
	 */
	private static final int ENERGY_ROW = ROW_STEP_R;
	private static final int ENVIRONMENT_ROW = ROW_STEP_R * 2;
	private static final int AIR_ROW = ROW_STEP_R * 3;

	static {
		for (int count = 1; count <= BHR_FoolHudArrows.MAX_ARROWS; count++) {
			ARROWS_POSITIVE[count] = hud("bhr_barl_arrow_" + count);
			ARROWS_NEGATIVE[count] = hud("bhr_barl_arrow_-" + count);
			ARROWS_R_POSITIVE[count] = hud("bhr_barr_arrow_" + count);
			ARROWS_R_NEGATIVE[count] = hud("bhr_barr_arrow_-" + count);
		}
	}

	/** 构造 {@code assets/<modid>/textures/gui/sprites/hud/<name>.png} 的贴图 id。 */
	private static Identifier hud(String name) {
		return Identifier.fromNamespaceAndPath(RichardsWhimsyMod.MOD_ID,
				"textures/gui/sprites/hud/" + name + ".png");
	}

	/**
	 * 用血条替换心形。
	 *
	 * <p>形参必须与目标方法完全一致（11 个）：原版在 {@code extractPlayerHealth} 里算好
	 * 位置与闪烁状态后传进来，我们用到其中 6 个，其余保留在签名里。
	 */
	@Inject(method = "extractHearts", at = @At("HEAD"), cancellable = true)
	private void whimsy$drawHealthRow(
			GuiGraphicsExtractor graphics,
			Player player,
			int xLeft,
			int yLineBase,
			int healthRowHeight,
			int heartOffsetIndex,
			float maxHealth,
			int currentHealth,
			int oldHealth,
			int totalAbsorption,
			boolean blink,
			CallbackInfo ci) {

		// 只有愚者模式用自定义 UI；其余模式原样让原版画心形
		if (!whimsy$isFoolMode()) {
			return;
		}

		float health = player.getHealth();
		float max = Math.max(1.0F, maxHealth);
		int fill = whimsy$pixels(health, max);
		int oldFill = whimsy$pixels(oldHealth, max);

		boolean fool = whimsy$isFoolMode();
		float pool = fool ? player.getAttachedOrElse(BHR_FoolHealth.POOL, 0.0F) : 0.0F;
		boolean downed = fool && health <= 0.01F && pool > 0.0F;

		// 1) 底槽 / 濒死层
		//    濒死时：左侧 = 剩余濒死值（empty 样式），右侧 = 已消耗部分（death 样式）。
		//    例：12/20 → 左 30 px 空槽 + 右 20 px 黑；濒死值越少，黑色从右往左吃得越多。
		if (downed) {
			int poolFill = whimsy$pixels(pool, BHR_FoolHealth.POOL_MAX);
			whimsy$blit(graphics, EMPTY, xLeft + BAR_U, yLineBase, BAR_U, poolFill);
			whimsy$blit(graphics, DEATH, xLeft + BAR_U + poolFill, yLineBase,
					BAR_U + poolFill, BAR_W - poolFill);
		} else {
			whimsy$blit(graphics, EMPTY, xLeft + BAR_U, yLineBase, BAR_U, BAR_W);
		}

		// 2) 当前生命（连续值）
		if (fill > 0) {
			whimsy$blit(graphics, FULL, xLeft + BAR_U, yLineBase, BAR_U, fill);
		}

		// 3) 原版节奏的闪烁层：扣血盖丢失段、回血盖新增段
		if (blink && oldFill != fill) {
			int from = Math.min(fill, oldFill);
			int width = Math.abs(fill - oldFill);
			whimsy$blit(graphics, BLINKING, xLeft + BAR_U + from, yLineBase, BAR_U + from, width);
		}

		// 4) 伤害吸收线框：完整度 = 吸收值（20 点 = 整圈 52 px），直接按宽度截断（不补右封口）
		float absorption = player.getAbsorptionAmount();
		if (absorption > 0.0F) {
			whimsy$blit(graphics, ABSORBING, xLeft + FRAME_U, yLineBase,
					FRAME_U, whimsy$frameWidth(absorption));
		}

		// 5) 快乐值（血条上一行）与盔甲值（再上一行）
		float happiness = player.getAttachedOrElse(BHR_Happiness.VALUE, 0.0F);
		whimsy$resourceRow(graphics, xLeft, yLineBase - HAPPINESS_ROW, ICON_HAPPINESS,
				happiness, BHR_Happiness.MAX);
		// 盔甲值为 0 时不显示这一行
		int armor = player.getArmorValue();
		if (armor > 0) {
			whimsy$resourceRow(graphics, xLeft, yLineBase - ARMOR_ROW, ICON_ARMOR,
					Mth.clamp(armor, 0, (int) BHR_Happiness.MAX), BHR_Happiness.MAX);
		}

		// 快乐值自己的箭头（同一 x，画在快乐值那一行）
		BHR_FoolHudArrows.updateHappiness(happiness);
		BHR_FoolHudArrows.Arrows happinessArrows = BHR_FoolHudArrows.sampleHappiness();
		whimsy$drawArrow(graphics, xLeft + ARROW_U, yLineBase - HAPPINESS_ROW,
				false, happinessArrows.negative(), happinessArrows.negativeAlpha());
		whimsy$drawArrow(graphics, xLeft + ARROW_U, yLineBase - HAPPINESS_ROW,
				true, happinessArrows.positive(), happinessArrows.positiveAlpha());

		// 5) 图标：底图标 +（闪烁叠加层）+（极限叠加层）
		if (downed) {
			whimsy$blit(graphics, ICON_DEATH, xLeft + ICON_U, yLineBase, ICON_U, ICON_W);
		} else {
			whimsy$blit(graphics, ICON_HEALTH, xLeft + ICON_U, yLineBase, ICON_U, ICON_W);
			if (blink) {
				whimsy$blit(graphics, ICON_HEALTH_BLINKING, xLeft + ICON_U, yLineBase, ICON_U, ICON_W);
			}
		}
		if (player.level().getLevelData().isHardcore()) {
			whimsy$blit(graphics, ICON_HEALTH_HARDCORE, xLeft + ICON_U, yLineBase, ICON_U, ICON_W);
		}

		// 6) 箭头（左侧画布的 x=65…79，紧挨着血条右边）
		BHR_FoolHudArrows.update(player, fool);
		BHR_FoolHudArrows.Arrows arrows = BHR_FoolHudArrows.sample(player);
		int arrowX = xLeft + ARROW_U;
		whimsy$drawArrow(graphics, arrowX, yLineBase, false, arrows.negative(), arrows.negativeAlpha());
		whimsy$drawArrow(graphics, arrowX, yLineBase, true, arrows.positive(), arrows.positiveAlpha());

		// 心形不画了（护甲条、载具血量等由原版 caller 继续处理）
		ci.cancel();
	}

	/** 画一组箭头（正向向右、负向向左），alpha &lt; 0.02 时跳过。 */
	private static void whimsy$drawArrow(GuiGraphicsExtractor graphics, int drawX, int drawY,
			boolean positive, int count, float alpha) {
		if (count <= 0 || alpha <= 0.02F) {
			return;
		}

		int clamped = Mth.clamp(count, 1, BHR_FoolHudArrows.MAX_ARROWS);
		Identifier texture = positive ? ARROWS_POSITIVE[clamped] : ARROWS_NEGATIVE[clamped];
		int color = ARGB.colorFromFloat(Mth.clamp(alpha, 0.0F, 1.0F), 1.0F, 1.0F, 1.0F);

		graphics.blit(RenderPipelines.GUI_TEXTURED, texture,
				drawX, drawY, ARROW_U, 0, ARROW_W, SHEET_H, SHEET_W, SHEET_H, color);
	}

	/**
	 * 画一组<b>右侧</b>箭头：素材用 {@code bhr_barr_arrow_*}，从画布 x=0 起取 15 px
	 * （箭头像素落在 0…14 里<b>右对齐</b>，因此紧挨着 16…65 的长条，而不是飞到外侧）。
	 *
	 * <p>方向（见 {@link #ARROWS_R_POSITIVE} 的说明）：
	 * <b>正向 = 增加 = 朝左</b>，<b>负向 = 减少 = 朝右</b>。
	 * 这与左侧 {@link #whimsy$drawArrow} 的朝向正好相反，不要混用两个方法。
	 */
	private static void whimsy$drawArrowRight(GuiGraphicsExtractor graphics, int drawX, int drawY,
			boolean positive, int count, float alpha) {
		if (count <= 0 || alpha <= 0.02F) {
			return;
		}

		int clamped = Mth.clamp(count, 1, BHR_FoolHudArrows.MAX_ARROWS);
		Identifier texture = positive ? ARROWS_R_POSITIVE[clamped] : ARROWS_R_NEGATIVE[clamped];
		int color = ARGB.colorFromFloat(Mth.clamp(alpha, 0.0F, 1.0F), 1.0F, 1.0F, 1.0F);

		graphics.blit(RenderPipelines.GUI_TEXTURED, texture,
				drawX, drawY, ARROW_R_U, 0, ARROW_R_W, SHEET_H, SHEET_W, SHEET_H, color);
	}

	/**
	 * 画一条左侧资源条：底槽 + 按比例的填充 + 图标（快乐值、盔甲值共用）。
	 *
	 * @param value 当前值（0~max，连续值）
	 * @param max   该资源的上限
	 */
	private static void whimsy$resourceRow(GuiGraphicsExtractor graphics, int xLeft, int y,
			Identifier icon, float value, float max) {
		int white = ARGB.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F);
		whimsy$blitColored(graphics, EMPTY, xLeft + BAR_U, y, BAR_U, BAR_W, white);
		int fill = whimsy$pixels(value, max);
		if (fill > 0) {
			whimsy$blitColored(graphics, FULL, xLeft + BAR_U, y, BAR_U, fill, white);
		}
		whimsy$blitColored(graphics, icon, xLeft + ICON_U, y, ICON_U, ICON_W, white);
	}

	/** 带显式颜色的切片绘制（范围外 alpha 会被沿用，这里强制不透明）。 */
	private static void whimsy$blitColored(GuiGraphicsExtractor graphics, Identifier texture,
			int drawX, int drawY, int u, int width, int color) {
		if (width <= 0) {
			return;
		}
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture,
				drawX, drawY, u, 0, width, SHEET_H, SHEET_W, SHEET_H, color);
	}

	/**
	 * 原版盔甲条不再画：位置让给本模组的「盔甲行」（血条上方第二行），
	 * 由 {@link #whimsy$resourceRow} 用同一套素材重画。
	 */
	@Inject(method = "extractArmor", at = @At("HEAD"), cancellable = true)
	private static void whimsy$hideVanillaArmor(GuiGraphicsExtractor graphics, Player player,
			int yLineBase, int numHealthRows, int healthRowHeight, int xLeft, CallbackInfo ci) {
		// 只有愚者模式才换成我们的盔甲行
		if (whimsy$isFoolMode()) {
			ci.cancel();
		}
	}

	/**
	 * 把原版食物条换成本模组的右侧饥饿条（barR）。
	 *
	 * <p><b>刻意不 cancel 整个 {@code extractFood}</b>：AppleSkin 是在这个方法上
	 * {@code HEAD}/{@code RETURN} 插桩来画「消耗度（暗色网格）/ 饱和度」的，一 cancel 它就整个消失。
	 * 这里改成「只把原版的三个 {@code blitSprite} 调用重定向成空操作」：
	 * 原版图标不画，方法体照常执行，AppleSkin 的插桩因此仍然生效。
	 *
	 * <p><b>层级</b>：本注入用 {@code priority = 2000}（比 AppleSkin 的默认 1000 先执行），
	 * 所以我们这条 bar 先画、<b>AppleSkin 的暗色消耗度网格后画</b>，叠在 bar 之上可见。
	 * 饱和度的金色边框由本模组自己画（素材 {@code bhr_barr_super_food}）。
	 */
	@Inject(method = "extractFood", at = @At("HEAD"))
	private void whimsy$drawHungerBar(GuiGraphicsExtractor graphics, Player player,
			int yLineBase, int xRight, CallbackInfo ci) {
		// 只有愚者模式才画我们的饥饿条
		if (!whimsy$isFoolMode()) {
			return;
		}
		int canvasLeft = xRight - SHEET_W;

		// 底槽
		whimsy$blit(graphics, EMPTY_R, canvasLeft + BAR_R_U, yLineBase, BAR_R_U, BAR_R_W);

		// 填充：饥饿值 0~20 → 0~50 px，从右端往左长
		int fill = whimsy$pixels(player.getFoodData().getFoodLevel(), BHR_Happiness.MAX);
		if (fill > 0) {
			int from = BAR_R_W - fill;
			whimsy$blit(graphics, FULL_R, canvasLeft + BAR_R_U + from, yLineBase,
					BAR_R_U + from, fill);
		}

		// 图标（最右）
		whimsy$blit(graphics, ICON_FOOD, canvasLeft + ICON_R_U, yLineBase, ICON_R_U, ICON_R_W);

		// 消耗度叠加：白色半密度层，右对齐。
		// 上限取 4 点而不是原版的 40：FoodData#tick 每攒够 4 点消耗度才扣 1 点饱和度/饥饿值，
		// 所以 0~4 才是「这一格还剩多少」有意义的那一段（按 40 算的话平时只有几个像素）。
		float exhaustion = player.getAttachedOrElse(BHR_FoodSync.EXHAUSTION, 0.0F);
		int exhaustionWidth = Mth.clamp(
				Math.round(exhaustion / BHR_FoodSync.EXHAUSTION_DISPLAY_MAX * BAR_R_W), 0, BAR_R_W);
		if (exhaustionWidth > 0) {
			int from = BAR_R_W - exhaustionWidth;
			whimsy$blit(graphics, SUPER_EXHAUSTION, canvasLeft + BAR_R_U + from, yLineBase,
					BAR_R_U + from, exhaustionWidth);
		}

		// 饥饿箭头：消耗度增量（瞬时）或饥饿效果（常驻闪烁）—— 都是「饥饿值在减少」，用负向箭头
		BHR_FoolHudArrows.updateHunger(exhaustion);
		int hungerAmplifier = player.hasEffect(MobEffects.HUNGER)
				? player.getEffect(MobEffects.HUNGER).getAmplifier()
				: -1;
		BHR_FoolHudArrows.Arrows hungerArrows = BHR_FoolHudArrows.sampleHunger(hungerAmplifier);
		whimsy$drawArrowRight(graphics, canvasLeft + ARROW_R_U, yLineBase,
				false, hungerArrows.negative(), hungerArrows.negativeAlpha());
		whimsy$drawArrowRight(graphics, canvasLeft + ARROW_R_U, yLineBase,
				true, hungerArrows.positive(), hungerArrows.positiveAlpha());

		// 饱和度金边：本模组自己画，右对齐（与饥饿填充同向），完整度按饱和度占 20 点的比例
		float saturation = player.getFoodData().getSaturationLevel();
		if (saturation > 0.0F) {
			int width = whimsy$frameWidth(saturation);
			int from = SUPER_R_W - width;
			whimsy$blit(graphics, SUPER_FOOD, canvasLeft + SUPER_R_U + from, yLineBase,
					SUPER_R_U + from, width);
		}

		// 精力条（饥饿条正上方一行）
		whimsy$drawEnergyBar(graphics, player, canvasLeft, yLineBase - ENERGY_ROW);

		// 环境值（精力条正上方一行）
		whimsy$drawEnvironmentBar(graphics, player, canvasLeft, yLineBase - ENVIRONMENT_ROW);

		// 氧气条（最上面一行，只在需要时显示）
		whimsy$drawAirBar(graphics, player, canvasLeft, yLineBase - AIR_ROW);
	}

	/**
	 * 环境值条：右侧第三条（自下而上 饥饿 / 精力 / <b>环境</b> / 氧气），0~100。
	 *
	 * <p>画法与饥饿 / 精力同构（同锚点、同填充方向、共用 {@code bhr_barr_empty} 与
	 * {@code bhr_barr_full}），只有图标换成 {@code bhr_barr_icon_environment}。
	 * 宽度 = {@code round(环境值 / 100 × 50 px)}，1 px = 2 点。
	 *
	 * <p>不画箭头以外的东西都一样：底槽 / 填充 / 图标与饥饿、精力同构。
	 * 箭头是<b>恒 1 个</b>、按「正在上升还是下降」换方向并闪烁（`BHR_FoolHudArrows#sampleEnvironment`）。
	 */
	private static void whimsy$drawEnvironmentBar(GuiGraphicsExtractor graphics, Player player,
			int canvasLeft, int y) {
		float environment = player.getAttachedOrElse(BHR_Environment.VALUE, BHR_Environment.MAX);

		// 底槽
		whimsy$blit(graphics, EMPTY_R, canvasLeft + BAR_R_U, y, BAR_R_U, BAR_R_W);

		// 填充：从右端往左长
		int fill = Mth.clamp(Math.round(Mth.clamp(environment / BHR_Environment.MAX, 0.0F, 1.0F) * BAR_R_W),
				0, BAR_R_W);
		if (fill > 0) {
			int from = BAR_R_W - fill;
			whimsy$blit(graphics, FULL_R, canvasLeft + BAR_R_U + from, y, BAR_R_U + from, fill);
		}

		// 图标（最右）
		whimsy$blit(graphics, ICON_ENVIRONMENT, canvasLeft + ICON_R_U, y, ICON_R_U, ICON_R_W);

		// 箭头：恒 1 个，上升 = 正向（朝左）、下降 = 负向（朝右），都在闪烁
		BHR_FoolHudArrows.updateEnvironment(environment);
		BHR_FoolHudArrows.Arrows arrows = BHR_FoolHudArrows.sampleEnvironment();
		whimsy$drawArrowRight(graphics, canvasLeft + ARROW_R_U, y,
				false, arrows.negative(), arrows.negativeAlpha());
		whimsy$drawArrowRight(graphics, canvasLeft + ARROW_R_U, y,
				true, arrows.positive(), arrows.positiveAlpha());
	}

	/**
	 * 精力条：饥饿条<b>正上方一行</b>（右侧四行自下而上 = 饥饿 / 精力 / 环境 / 氧气）。
	 *
	 * <p>与饥饿条同锚点、同填充方向（从右端往左长，贴近图标一侧）：
	 * <ul>
	 *   <li>底槽 = {@code bhr_barr_empty}、填充 = {@code bhr_barr_full}（与饥饿条共用同一套绿色图层）；</li>
	 *   <li>图标 = {@code bhr_barr_icon_energy}（画布 71…79）；</li>
	 *   <li>黄层的 {@code bhr_barr_super_energy} <b>不是</b>这里的填充层 ——
	 *       它留给之后「睡觉奖励的精力值」叠加层（与饥饿条的饱和度金边同一个用法）。</li>
	 * </ul>
	 * 宽度按 {@code 精力 / 20000} 连续换算（20000 点是满槽 50 px，1 px ≈ 400 点）。
	 */
	private static void whimsy$drawEnergyBar(GuiGraphicsExtractor graphics, Player player,
			int canvasLeft, int y) {
		float energy = player.getAttachedOrElse(BHR_Energy.VALUE, BHR_Energy.MAX);

		// 底槽
		whimsy$blit(graphics, EMPTY_R, canvasLeft + BAR_R_U, y, BAR_R_U, BAR_R_W);

		// 填充：从右端往左长
		int fill = Mth.clamp(Math.round(Mth.clamp(energy / BHR_Energy.MAX, 0.0F, 1.0F) * BAR_R_W),
				0, BAR_R_W);
		if (fill > 0) {
			int from = BAR_R_W - fill;
			whimsy$blit(graphics, FULL_R, canvasLeft + BAR_R_U + from, y,
					BAR_R_U + from, fill);
		}

		// 图标（最右）
		whimsy$blit(graphics, ICON_ENERGY, canvasLeft + ICON_R_U, y, ICON_R_U, ICON_R_W);

		// 箭头：恒为 1 个负向（= 减少 = 朝右）—— 精力按时间恒速消耗，不分档
		BHR_FoolHudArrows.Arrows arrows = BHR_FoolHudArrows.sampleEnergy(energy);
		whimsy$drawArrowRight(graphics, canvasLeft + ARROW_R_U, y,
				false, arrows.negative(), arrows.negativeAlpha());
		whimsy$drawArrowRight(graphics, canvasLeft + ARROW_R_U, y,
				true, arrows.positive(), arrows.positiveAlpha());
	}

	/**
	 * 氧气条：右侧<b>最上面一行</b>（饥饿 / 精力 / 环境 / 氧气 里的第四行），
	 * 与原版一样「只在水下或氧气未满时显示」。
	 *
	 * <p>箭头规则（方向按「增加 / 减少」的语义来，右侧素材 {@code _N} = 增加 = 朝左、
	 * {@code -_N} = 减少 = 朝右）：
	 * <ul>
	 *   <li>默认 4 个<b>减少</b>箭头（朝右）；每 1 级<b>水下呼吸附魔</b>（头盔）减 1 个；</li>
	 *   <li>有<i>水下呼吸</i>状态效果 → 不显示箭头；</li>
	 *   <li>可以呼吸（不在水里）→ 氧气快速回复，显示 5 个<b>增加</b>箭头（朝左）。</li>
	 * </ul>
	 */
	private static void whimsy$drawAirBar(GuiGraphicsExtractor graphics, Player player,
			int canvasLeft, int y) {
		int air = player.getAirSupply();
		int maxAir = player.getMaxAirSupply();
		boolean underwater = player.isEyeInFluid(FluidTags.WATER);
		if (!underwater && air >= maxAir) {
			return;
		}

		whimsy$blit(graphics, EMPTY_R, canvasLeft + BAR_R_U, y, BAR_R_U, BAR_R_W);
		int fill = Mth.clamp(Math.round(Mth.clamp(air / (float) Math.max(1, maxAir), 0.0F, 1.0F) * BAR_R_W),
				0, BAR_R_W);
		if (fill > 0) {
			int from = BAR_R_W - fill;
			whimsy$blit(graphics, FULL_AIR, canvasLeft + BAR_R_U + from, y, BAR_R_U + from, fill);
		}
		whimsy$blit(graphics, ICON_AIR, canvasLeft + ICON_R_U, y, ICON_R_U, ICON_R_W);

		// 箭头
		boolean waterBreathing = player.hasEffect(MobEffects.WATER_BREATHING);
		if (waterBreathing) {
			return;
		}

		float alpha = BHR_FoolHudArrows.blinkAlphaNow();
		if (!underwater) {
			// 不在水里：氧气快速回复 = 增加 → 5 个正向箭头（朝左）
			whimsy$drawArrowRight(graphics, canvasLeft + ARROW_R_U, y, true, 5, alpha);
		} else {
			// 在水里：氧气在掉 = 减少 → 负向箭头（朝右），水下呼吸每级减 1 个
			int respiration = whimsy$respirationLevel(player);
			int arrows = Math.max(0, 4 - respiration);
			if (arrows > 0) {
				whimsy$drawArrowRight(graphics, canvasLeft + ARROW_R_U, y, false, arrows, alpha);
			}
		}
	}

	/** 头盔上的水下呼吸附魔等级（不查注册表，直接遍历物品附魔）。 */
	private static int whimsy$respirationLevel(Player player) {
		var enchantments = player.getItemBySlot(EquipmentSlot.HEAD).getEnchantments();
		for (var entry : enchantments.entrySet()) {
			if (entry.getKey().is(Enchantments.RESPIRATION)) {
				return entry.getIntValue();
			}
		}
		return 0;
	}

	/**
	 * 把 {@code extractFood} 里的原版食物图标绘制全部吞掉（三处同签名调用），
	 * 但保留方法体本身，让 AppleSkin 的 HEAD/RETURN 插桩继续工作。
	 */
	@Redirect(method = "extractFood",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite"
							+ "(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
	private void whimsy$hideVanillaFoodIcons(GuiGraphicsExtractor graphics, RenderPipeline pipeline,
			Identifier sprite, int x, int y, int width, int height) {
		// 非愚者模式：原版图标照画（生存模式必须是原版外观）
		if (!whimsy$isFoolMode()) {
			graphics.blitSprite(pipeline, sprite, x, y, width, height);
		}
	}

	/**
	 * 原版氧气泡不再画：愚者模式有了自己的氧气条（barR 上一行）。
	 * 非愚者模式照旧画原版气泡。
	 */
	@Inject(method = "extractAirBubbles", at = @At("HEAD"), cancellable = true)
	private void whimsy$hideVanillaAirBubbles(GuiGraphicsExtractor graphics, Player player,
			int vehicleHearts, int yLineAir, int xRight, CallbackInfo ci) {
		if (whimsy$isFoolMode()) {
			ci.cancel();
		}
	}

	/** 把「值 / 上限」换算成 50 px 长条里的像素宽度（向下取整，0.0001 点血不会显示成 1 px）。 */
	private static int whimsy$pixels(float value, float max) {
		float ratio = Mth.clamp(value / max, 0.0F, 1.0F);
		return Mth.clamp((int) (ratio * BAR_W), 0, BAR_W);
	}

	/**
	 * 伤害吸收线框的宽度：按吸收值占 20 点的比例铺满整圈（52 px），最小 3 px
	 * （否则只剩两条竖线，看不出是"一圈"）。线框直接按这个宽度截断，不补右封口。
	 */
	private static int whimsy$frameWidth(float absorption) {
		float ratio = Mth.clamp(absorption / BHR_FoolHealth.POOL_MAX, 0.0F, 1.0F);
		return Mth.clamp(Math.round(ratio * FRAME_W), 3, FRAME_W);
	}

	/**
	 * 从 80×9 的画布上取一段画出去（不缩放、不带 tint）。
	 *
	 * @param drawX 屏幕上要画到的 x
	 * @param u     源图里的起始 x
	 * @param width 要画的宽度（源图与屏幕同宽同高）
	 */
	private static void whimsy$blit(GuiGraphicsExtractor graphics, Identifier texture,
			int drawX, int drawY, int u, int width) {
		if (width <= 0) {
			return;
		}
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture,
				drawX, drawY, u, 0, width, SHEET_H, SHEET_W, SHEET_H);
	}

	/** 当前是不是愚者模式（客户端读 {@code MultiPlayerGameMode} 的本地模式）。 */
	private static boolean whimsy$isFoolMode() {
		MultiPlayerGameMode gameMode = net.minecraft.client.Minecraft.getInstance().gameMode;
		return gameMode != null && gameMode.getPlayerMode() == GameType.BHR_FOOL_MODE;
	}
}
