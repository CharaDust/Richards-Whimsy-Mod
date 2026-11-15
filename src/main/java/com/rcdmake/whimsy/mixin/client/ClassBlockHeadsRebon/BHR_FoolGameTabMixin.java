package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.access.BHR_CreateWorldExpertModeAccess;
import com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_ExpertModeChoice;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Array;

/**
 * 愚者模式：把「创建世界」界面的 <b>Game 页</b>改成「第五种游戏模式 + 专家模式按钮」。
 *
 * <p>对应原版 {@code CreateWorldScreen$GameTab}（private 内部类，只能用
 * {@code @Mixin(targets = ...)} 指路）。它做两件事，都发生在同一个构造器里：
 *
 * <h2>① 往游戏模式循环按钮的取值里追加「愚者模式」（排在创造模式后面）</h2>
 * 原版写的是（26.2 源码 {@code CreateWorldScreen.GameTab}）：
 * <pre>{@code
 * CycleButton.<SelectedGameMode>builder(m -> m.displayName, uiState.getGameMode())
 *     .withValues(SURVIVAL, HARDCORE, CREATIVE)          // ← 只有三个
 *     .create(0, 0, 210, 20, GAME_MODEL_LABEL, (b, gm) -> uiState.setGameMode(gm));
 * }</pre>
 * 取值数组是<b>构造器里的局部量</b>，事后拿不到那个按钮，所以在
 * {@code withValues} 被调用的瞬间替换实参：{@code [生存, 极限, 创造]} →
 * {@code [生存, 极限, 创造, 愚者]}。追加在末尾即「排在创造模式后面」，
 * 循环顺序也随之是 生存 → 极限 → 创造 → 愚者 → 生存。
 *
 * <p>⚠️ 为什么要写 {@code ordinal = 0}：同一个构造器里 {@code withValues} 被调了
 * <b>两次</b>（游戏模式一次、难度一次，两者擦除后的描述符完全一样：
 * {@code withValues([Ljava/lang/Object;)LCycleButton$Builder;}）。字节码里
 * 游戏模式那次在前，所以取第 0 个。多加一道「数组首元素必须是 SURVIVAL」的
 * 运行期校验，将来原版换顺序也不会把愚者模式追加到难度的取值里去。
 *
 * <p>⚠️ 返回值是新数组而不是就地扩容：{@code withValues} 里
 * {@code ImmutableList.copyOf(...)} 只读不改，新数组按<b>原数组的组件类型</b>
 * （{@code Array.newInstance(componentType, n)}）建，不引入 {@code Object[]} 的
 * 运行时类型，避免 {@code ArrayStoreException} 一类的问题。
 *
 * <h2>② 在 Game 页底部补一行「专家模式」循环按钮（关 / 开 / 自定义）</h2>
 * 原版这个页的骨架是 {@code GridLayout.RowHelper(1)}：每调一次 {@code addChild}
 * 就是「新增一行、第 0 列」。构造器结尾时行数 = 已加的子元素个数，所以我们在
 * {@code TAIL} 直接往 {@code this.layout}（{@code GridLayoutTab} 的 protected 字段，
 * 这里通过 {@code getLayout()} 拿）追加第 N 行。
 *
 * <p><b>可见性</b>：只有游戏模式是愚者模式时才 {@code visible = true}
 * （对应「调节到这个游戏模式时，下方会多出一个专家模式按钮」）。
 * 这里刻意只切 {@code visible}、不删元素、也不改高度 ——
 * {@code GridLayout} 的行高在 {@code arrangeElements()} 里按子元素高度算，
 * 高度恒为 20 意味着<b>切来切去界面不会跳</b>（代价是非愚者模式下这一行是空的）。
 *
 * <p><b>挡位存在哪</b>：存在创建世界界面自己身上
 * （{@link BHR_CreateWorldExpertModeAccess}，{@code @Unique} 字段），
 * 按钮只负责写，真正「执行一次动作」在世界创建前由
 * {@code BHR_FoolCreateWorldMixin} 读出来做。
 */
@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$GameTab")
public abstract class BHR_FoolGameTabMixin {

	/** 按钮宽度：与同一页里其它控件一致（原版都写 210）。 */
	private static final int WHIMSY$BUTTON_WIDTH = 210;
	private static final int WHIMSY$BUTTON_HEIGHT = 20;

	/**
	 * ① 给游戏模式的取值数组追加愚者模式。
	 *
	 * @param values 原版构造器里新建的 {@code new SelectedGameMode[]{SURVIVAL, HARDCORE, CREATIVE}}
	 * @return 追加 {@code BHR_FOOL} 之后的新数组
	 */
	@ModifyArg(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/components/CycleButton$Builder;withValues([Ljava/lang/Object;)Lnet/minecraft/client/gui/components/CycleButton$Builder;",
					ordinal = 0),
			index = 0)
	private Object[] whimsy$appendFoolGameMode(Object[] values) {
		// 运行期校验：只认「生存打头」的那个数组，难度的取值数组原样放行
		if (values.length == 0 || values[0] != WorldCreationUiState.SelectedGameMode.SURVIVAL) {
			return values;
		}

		Object[] extended = (Object[]) Array.newInstance(values.getClass().getComponentType(), values.length + 1);
		System.arraycopy(values, 0, extended, 0, values.length);
		extended[values.length] = WorldCreationUiState.SelectedGameMode.BHR_FOOL;
		return extended;
	}

	/**
	 * ② 构造器收尾：在 Game 页追加「专家模式」循环按钮。
	 *
	 * @param screen 原版内部类的构造器实参（外部 {@code CreateWorldScreen} 实例）。
	 *               26.2 的这个内部类<b>不保存</b>外部引用（只用 nestmate 直连私有字段），
	 *               所以外部实例只能从构造器参数拿，不能影子字段。
	 */
	@Inject(method = "<init>", at = @At("TAIL"))
	private void whimsy$addExpertModeButton(CreateWorldScreen screen, CallbackInfo ci) {
		// GridLayoutTab.layout 是 protected，本 Mixin 不继承目标类，走公开的 getLayout()
		GridLayout grid = (GridLayout) ((GridLayoutTab) (Object) this).getLayout();

		// 行数 = 已加的子元素个数（原版这一页每次 addChild 恰好新增一行、一列）
		int[] rows = {0};
		grid.visitChildren(child -> rows[0]++);

		WorldCreationUiState uiState = screen.getUiState();
		BHR_CreateWorldExpertModeAccess access = (BHR_CreateWorldExpertModeAccess) screen;

		CycleButton<BHR_ExpertModeChoice> expertButton = CycleButton
				.<BHR_ExpertModeChoice>builder(BHR_ExpertModeChoice::getDisplayName, BHR_ExpertModeChoice.OFF)
				.withValues(BHR_ExpertModeChoice.OFF, BHR_ExpertModeChoice.ON, BHR_ExpertModeChoice.CUSTOM)
				.withTooltip(choice -> Tooltip.create(choice.getTooltip()))
				.create(0, 0, WHIMSY$BUTTON_WIDTH, WHIMSY$BUTTON_HEIGHT,
						Component.translatable("selectWorld.expertMode"),
						(button, choice) -> access.whimsy$setExpertModeChoice(choice));

		// 初始可见性 + 跟随游戏模式变化
		expertButton.visible = whimsy$isFoolMode(uiState);
		uiState.addListener(state -> expertButton.visible = whimsy$isFoolMode(state));

		grid.addChild(expertButton, rows[0], 0);
	}

	/** 当前挡位是不是愚者模式。 */
	private static boolean whimsy$isFoolMode(WorldCreationUiState uiState) {
		return uiState.getGameMode() == WorldCreationUiState.SelectedGameMode.BHR_FOOL;
	}
}
