package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.access.BHR_CreateWorldExpertModeAccess;
import com.rcdmake.whimsy.client.ClassBlockHeadsRebon.BHR_ExpertModeChoice;
import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_GameRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.Optional;

/**
 * 愚者模式：创建世界界面的两处「界面之外」的收尾。
 *
 * <h2>① 世界创建前，执行一次「开启 / 关闭专家模式」</h2>
 * 「创建世界」按钮走 {@code onCreate()} → {@code WorldOpenFlows.confirmWorldCreation(...)}
 * →（实验性内容确认之后）→ {@code createWorldAndCleanup(..., Optional<GameRules>)}。
 * 我们在<b>它开头</b>干预：那一刻手里正好是「即将写进存档的那份规则对象」，
 * 而且是唯一一次、世界还没落地 —— 正好对应「创建世界后执行一次动作」。
 *
 * <p>做法不是伪造一次 {@code /gamerule} 命令，而是调
 * {@link BHR_GameRules#applyExpertModePreset}：
 * <ul>
 *   <li>写 {@code bhr_expert_mode}（true / false），即那条规则值本身；</li>
 *   <li>照预设表把每条细调条目写成专家挡位 / 普通挡位。</li>
 * </ul>
 * 这正是玩家手敲 {@code /gamerule bhr_expert_mode true} 时 {@code BHR_GameRules}
 * 监听器做的那两件事，因此存档里的结果完全一样；区别只在于「谁触发」——
 * 创建世界时还没有 {@code MinecraftServer}（{@code GameRules.set} 的第三个参数
 * 只能是 null，写值不回调），所以直接写值。
 *
 * <p>三个挡位的处理：{@link BHR_ExpertModeChoice#ON} → 开，
 * {@link BHR_ExpertModeChoice#OFF} → 关（默认），
 * {@link BHR_ExpertModeChoice#CUSTOM} → <b>什么都不做</b>（玩家自己去 [游戏规则] 调）。
 * 另外还要求当前游戏模式<b>确实是愚者模式</b>：按钮虽然只在愚者模式下可见，
 * 但挡位是界面级状态，正确性不能靠「按钮看不看得见」来判断。
 *
 * <h2>② 重新创建存档时把游戏模式还原成愚者模式</h2>
 * 原版 {@code createFromExisting} 只在「生存 / 极限 / 创造」里挑一个：
 * {@code levelSettings.gameType().isSurvival()} 先命中 —— 而愚者模式在
 * {@code GameType} 那边是「按生存算」的（见 {@code BHR_FoolGameTypeMixin}），
 * 于是重新创建存档（改数据包那条路）会把界面悄悄退回生存模式。
 * 尾插一刀：存档本来就是愚者模式，就把它选回来。
 *
 * <p>挡位字段用 {@code @Unique} 存在界面实例上（不是静态），并实现
 * {@link BHR_CreateWorldExpertModeAccess} 供 {@code BHR_FoolGameTabMixin} 读写。
 */
@Mixin(CreateWorldScreen.class)
public abstract class BHR_FoolCreateWorldMixin implements BHR_CreateWorldExpertModeAccess {

	/** 专家模式挡位（默认关）。随界面实例生灭。 */
	@Unique
	private BHR_ExpertModeChoice whimsy$expertModeChoice = BHR_ExpertModeChoice.OFF;

	@Override
	public BHR_ExpertModeChoice whimsy$getExpertModeChoice() {
		return this.whimsy$expertModeChoice;
	}

	@Override
	public void whimsy$setExpertModeChoice(BHR_ExpertModeChoice choice) {
		this.whimsy$expertModeChoice = choice;
	}

	/**
	 * ① 世界创建前执行一次「开启 / 关闭专家模式」。
	 *
	 * <p>注入点是 {@code createWorldAndCleanup} 的 HEAD：它的第三个参数就是
	 * 即将写进 {@code level.dat} 的规则对象（{@code Optional<GameRules>}；
	 * 调试世界那条路传的同样是 Optional.of）。
	 */
	@Inject(method = "createWorldAndCleanup", at = @At("HEAD"))
	private void whimsy$applyExpertModeOnCreate(
			LayeredRegistryAccess<RegistryLayer> finalLayers,
			LevelDataAndDimensions.WorldDataAndGenSettings worldDataAndGenSettings,
			Optional<GameRules> gameRules,
			CallbackInfo ci) {
		CreateWorldScreen screen = (CreateWorldScreen) (Object) this;
		if (screen.getUiState().getGameMode() != WorldCreationUiState.SelectedGameMode.BHR_FOOL) {
			return;
		}

		BHR_ExpertModeChoice choice = this.whimsy$expertModeChoice;
		if (choice == BHR_ExpertModeChoice.CUSTOM) {
			return;
		}

		gameRules.ifPresent(rules -> BHR_GameRules.applyExpertModePreset(rules, choice == BHR_ExpertModeChoice.ON));
	}

	/**
	 * ② 重新创建存档时还原愚者模式（原版只会选中生存 / 极限 / 创造三者之一）。
	 */
	@Inject(method = "createFromExisting", at = @At("TAIL"))
	private static void whimsy$restoreFoolModeOnRecreate(
			Minecraft minecraft,
			Runnable onClose,
			LevelSettings levelSettings,
			WorldCreationContext worldCreationContext,
			Path newDataPackDir,
			CallbackInfoReturnable<CreateWorldScreen> cir) {
		if (levelSettings.gameType() != GameType.BHR_FOOL_MODE) {
			return;
		}

		CreateWorldScreen screen = cir.getReturnValue();
		if (screen != null) {
			screen.getUiState().setGameMode(WorldCreationUiState.SelectedGameMode.BHR_FOOL);
		}
	}
}
