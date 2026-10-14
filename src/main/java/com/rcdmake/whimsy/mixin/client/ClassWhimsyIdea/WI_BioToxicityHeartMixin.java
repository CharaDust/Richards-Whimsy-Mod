package com.rcdmake.whimsy.mixin.client.ClassWhimsyIdea;

import com.rcdmake.whimsy.statusEffect.ClassWhimsyIdea.WI_BioToxicity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 生物毒性（{@code wi_bio_toxicity}）的<b>生命条视觉标识</b>：
 * 最左侧 3 颗心保持红色（默认色），其余心显示为中毒绿。
 *
 * <h2>⚠️ 前一版为什么没生效（记在这里免得重犯）</h2>
 * <p>第一版的做法是「原版画完之后，把最左 3 颗心<b>再涂成红色</b>」，
 * 假设「原版在中毒时会自动把整条生命条变绿」。
 * <b>这个假设只在玩家同时带<b>原版</b>中毒（{@code MobEffects.POISON}）时才成立</b>：
 * {@code Hud.HeartType.forPlayer} 只看原版中毒/凋灵/冻结三种状态，
 * 本模组的生物毒性不是原版中毒，所以它返回 {@code NORMAL}（全红）。
 * 于是「把已经红的心涂成红」＝什么都没做，表现出来就是「全红，没变化」。
 *
 * <p>诊断证据（客户端日志）：注入确实在跑、守卫也通过（{@code 玩家有效果=true}），
 * 但心形颜色不变 —— 问题不在注入，而在「谁负责变绿」这个理解上。
 *
 * <h2>现在怎么做：由本 mixin 自己负责绿色</h2>
 * <p>在 {@code extractHearts} 返回前，按同样的坐标规则重画一遍填充心：
 * 最左 {@link #WI_PRESERVED_RED_HEARTS} 颗用<b>默认红</b>贴图，其余用<b>中毒绿</b>贴图。
 * 容器底图、空位、伤害吸收的黄色心都不碰（吸收心排在血量心右侧，本循环只画血量心）。
 *
 * <h2>与「原版中毒」的关系</h2>
 * <p>若玩家本来就带原版中毒，原版已经把整条画成绿的——那就直接让原版负责，
 * 本 mixin 原样返回（保持原版表现不变，避免两套绿色贴图打架）。
 *
 * <h2>坐标与心号</h2>
 * <p>原版 {@code extractHearts} 的布局（逐行读过，这里与它一致）：
 * <pre>{@code
 * int row    = index / 10;                  // 每行 10 颗
 * int column = index % 10;
 * int x      = xLeft + column * 8;          // 心宽 8 + 间隔 1
 * int y      = yLineBase - row * healthRowHeight;   // 行往上叠
 * }</pre>
 * 心号从右往左为 {@code 0..n-1}，所以<b>最小的心号就是最左边那颗</b>；
 * 要保留红色的正是 {@code containerIndex} 为 0/1/2 的三颗。
 *
 * <h2>只在客户端加载</h2>
 * <p>{@code Hud} 是纯客户端类，所以本 mixin 登记在 {@code richards-whimsy-mod.mixins.json}
 * 的 <b>{@code client} 数组</b>里；写进通用 {@code mixins} 数组会让专用服务端
 * 加载 mixin 时去找 {@code net.minecraft.client.gui.Hud} 而崩（ClassNotFound）。
 */
@Mixin(Hud.class)
public class WI_BioToxicityHeartMixin {

	/** 保持红色的心数（最左侧 3 颗 = 6 点生命值，与效果的 6 点下限对应）。 */
	@Unique
	private static final int WI_PRESERVED_RED_HEARTS = 3;

	// ── 贴图 id：与 Hud.HeartType 里 NORMAL / POISIONED 用的是同一批资源 ──
	// （原版那个 enum 的拼写就是 POISIONED，这里只取 id 字符串，不引用那个私有 enum。）

	@Unique
	private static final Identifier WI_RED_FULL = Identifier.withDefaultNamespace("hud/heart/full");
	@Unique
	private static final Identifier WI_RED_HALF = Identifier.withDefaultNamespace("hud/heart/half");
	@Unique
	private static final Identifier WI_RED_FULL_HARDCORE = Identifier.withDefaultNamespace("hud/heart/hardcore_full");
	@Unique
	private static final Identifier WI_RED_HALF_HARDCORE = Identifier.withDefaultNamespace("hud/heart/hardcore_half");

	@Unique
	private static final Identifier WI_GREEN_FULL = Identifier.withDefaultNamespace("hud/heart/poisoned_full");
	@Unique
	private static final Identifier WI_GREEN_HALF = Identifier.withDefaultNamespace("hud/heart/poisoned_half");
	@Unique
	private static final Identifier WI_GREEN_FULL_HARDCORE = Identifier.withDefaultNamespace("hud/heart/poisoned_hardcore_full");
	@Unique
	private static final Identifier WI_GREEN_HALF_HARDCORE = Identifier.withDefaultNamespace("hud/heart/poisoned_hardcore_half");

	/**
	 * 原版画完生命条后，按「最左 3 颗红、其余绿」重画一遍填充心。
	 *
	 * <p>形参顺序与目标方法完全一致：{@code (GuiGraphicsExtractor, Player, int xLeft, int yLineBase,
	 * int healthRowHeight, int heartOffsetIndex, float maxHealth, int currentHealth, int oldHealth,
	 * int absorption, boolean blink)}。
	 */
	@Inject(
			method = "extractHearts(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V",
			at = @At("TAIL"))
	private void wi$paintBioToxicityHearts(
			GuiGraphicsExtractor graphics,
			Player player,
			int xLeft,
			int yLineBase,
			int healthRowHeight,
			int heartOffsetIndex,
			float maxHealth,
			int currentHealth,
			int oldHealth,
			int absorption,
			boolean blink,
			CallbackInfo ci) {

		// 只在本模组的生物毒性生效时接管。
		if (WI_BioToxicity.WI_BIO_TOXICITY == null || !player.hasEffect(WI_BioToxicity.WI_BIO_TOXICITY)) {
			return;
		}
		// 玩家本来就带原版中毒时，原版已经把整条画成绿的 —— 让原版负责，别画两套。
		if (player.hasEffect(MobEffects.POISON)) {
			return;
		}

		boolean hardcore = player.level().getLevelData().isHardcore();
		int healthContainers = Mth.ceil(maxHealth / 2.0F);

		for (int index = 0; index < healthContainers; index++) {
			// 空心的位置不动：原版那里只有容器底图（甚至什么都没有）。
			if (index * 2 >= currentHealth) {
				continue;
			}

			int x = xLeft + (index % 10) * 8;
			int y = yLineBase - (index / 10) * healthRowHeight;
			// index*2 == currentHealth-1 时，这颗心只填满了一半。
			boolean half = index * 2 + 1 == currentHealth;
			// 与原版一致：正在扣血的那颗（heartOffsetIndex）且处于闪烁窗口时用闪烁贴图。
			boolean withBlink = blink && index == heartOffsetIndex;

			// 最左 3 颗 → 红（默认色）；其余 → 中毒绿。
			boolean keepRed = index < WI_PRESERVED_RED_HEARTS;
			Identifier sprite;
			if (keepRed) {
				sprite = half
						? (hardcore ? WI_RED_HALF_HARDCORE : WI_RED_HALF)
						: (hardcore ? WI_RED_FULL_HARDCORE : WI_RED_FULL);
			} else {
				sprite = half
						? (hardcore ? WI_GREEN_HALF_HARDCORE : WI_GREEN_HALF)
						: (hardcore ? WI_GREEN_FULL_HARDCORE : WI_GREEN_FULL);
			}
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 9, 9);
		}
	}
}
