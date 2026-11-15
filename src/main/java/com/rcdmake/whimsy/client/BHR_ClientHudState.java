package com.rcdmake.whimsy.client;

import com.rcdmake.whimsy.misc.ClassBlockHeadsRebon.BHR_FoolHealth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.GameType;

/**
 * 客户端用的「愚者模式 / 濒死」判定（HUD、红边、濒死 BGM 共用一份）。
 *
 * <p>只被客户端代码调用，专用服务端不会加载本类。
 */
public final class BHR_ClientHudState {

	private BHR_ClientHudState() {
	}

	/** 当前是不是愚者模式（客户端本地模式）。 */
	public static boolean isFoolMode(Minecraft minecraft) {
		MultiPlayerGameMode gameMode = minecraft.gameMode;
		return gameMode != null && gameMode.getPlayerMode() == GameType.BHR_FOOL_MODE;
	}

	/** 愚者模式 + 第一层血见底 + 濒死值还有剩 = 濒死。 */
	public static boolean isDowned(Minecraft minecraft, LocalPlayer player) {
		if (player == null || !isFoolMode(minecraft)) {
			return false;
		}
		float pool = player.getAttachedOrElse(BHR_FoolHealth.POOL, 0.0F);
		return pool > 0.0F && player.getHealth() <= 0.01F;
	}
}
