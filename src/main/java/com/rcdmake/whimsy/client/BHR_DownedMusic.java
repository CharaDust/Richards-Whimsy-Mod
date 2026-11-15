package com.rcdmake.whimsy.client;

import com.rcdmake.whimsy.access.BHR_MusicDelayAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

/**
 * 愚者模式的濒死 BGM 控制器（{@code bhr_tikopia}）。
 *
 * <h2>行为</h2>
 * <ul>
 *   <li><b>进入濒死</b>：原版当前曲目用 1 秒淡出（调 MUSIC 声道增益），然后停掉；
 *       紧接着播放 {@code bhr_tikopia} —— <b>不淡入</b>，从第一个音符开始就满音量；</li>
 *   <li><b>濒死期间</b>：原版音乐被挂起（{@code MusicManager#tick} 被 mixin 取消），
 *       我们的曲子循环播放；</li>
 *   <li><b>退出濒死</b>：我们的曲子 1 秒淡出后停止，并把原版下一首的等待时间压到 2 秒，
 *       让正常背景音乐尽快接回来。</li>
 * </ul>
 *
 * <h2>为什么用 MUSIC 声道 + 增益</h2>
 * 原版音乐（{@code MusicManager}）自己就是用
 * {@code soundManager.updateCategoryVolume(MUSIC, 增益)} 来做音量渐变的；
 * 我们没有原版曲目的 {@code SoundInstance} 引用（它是 private），
 * 所以淡出原版曲目时直接压 MUSIC 声道增益；等我方上场后把增益还原，
 * 我方实例自身的音量再从 1.0 自己往下淡，两者不会叠乘出错。
 */
public final class BHR_DownedMusic {

	private BHR_DownedMusic() {
	}

	/** 淡入淡出时长（tick）。 */
	private static final int FADE_TICKS = 20;

	/** 退出濒死后，原版下一首的最大等待（tick）。 */
	private static final int RESUME_DELAY_TICKS = 40;

	private enum Phase {
		/** 什么都没放。 */
		IDLE,
		/** 正在淡出原版曲目。 */
		VANILLA_FADE,
		/** 正在循环播放我方曲目。 */
		PLAYING,
		/** 正在淡出我方曲目。 */
		OUR_FADE
	}

	private static Phase phase = Phase.IDLE;
	private static int fadeTicks;
	private static BHR_TikopiaSoundInstance sound;
	private static boolean wasDowned;

	/** 由客户端入口每 tick 调一次。 */
	public static void tick(Minecraft minecraft) {
		if (minecraft.player == null || minecraft.level == null) {
			reset(minecraft);
			return;
		}

		boolean downed = BHR_ClientHudState.isDowned(minecraft, minecraft.player);
		wasDowned = downed;

		switch (phase) {
			case IDLE -> {
				if (downed) {
					begin(minecraft);
				}
			}
			case VANILLA_FADE -> {
				fadeTicks++;
				float gain = 1.0F - (float) fadeTicks / FADE_TICKS;
				minecraft.getSoundManager().updateCategoryVolume(
						SoundSource.MUSIC, Mth.clamp(gain, 0.0F, 1.0F) * minecraft.getMusicVolume());

				if (fadeTicks >= FADE_TICKS) {
					// 原版曲目已经听不见了：停掉它，并把 MUSIC 声道增益还原给我方曲目
					minecraft.getMusicManager().stopPlaying();
					minecraft.getSoundManager().updateCategoryVolume(
							SoundSource.MUSIC, minecraft.getMusicVolume());

					sound = new BHR_TikopiaSoundInstance(1.0F);
					minecraft.getSoundManager().play(sound);
					phase = Phase.PLAYING;
				}
			}
			case PLAYING -> {
				if (!downed) {
					phase = Phase.OUR_FADE;
					fadeTicks = 0;
				}
			}
			case OUR_FADE -> {
				fadeTicks++;
				// ⚠️ 不能只改实例音量：原版只在「类别音量被 updateCategoryVolume 刷新」时
				//   才会重算每条 channel 的音量（SoundEngine.refreshCategoryVolume）。
				//   所以这里和进入时一样，压 MUSIC 类别增益来淡出。
				float gain = 1.0F - (float) fadeTicks / FADE_TICKS;
				minecraft.getSoundManager().updateCategoryVolume(
						SoundSource.MUSIC, Mth.clamp(gain, 0.0F, 1.0F) * minecraft.getMusicVolume());

				if (fadeTicks >= FADE_TICKS) {
					if (sound != null) {
						minecraft.getSoundManager().stop(sound);
						sound = null;
					}
					// 把类别音量还原，否则之后的原版音乐会一直是静音
					minecraft.getSoundManager().updateCategoryVolume(
							SoundSource.MUSIC, minecraft.getMusicVolume());
					// 让原版正常音乐尽快接回来（否则要等它自己算的几分钟）
					if (minecraft.getMusicManager() instanceof BHR_MusicDelayAccess access) {
						access.whimsy$shortenNextSongDelay(RESUME_DELAY_TICKS);
					}
					phase = Phase.IDLE;
				}
			}
		}
	}

	/** 开始进入濒死：有原版曲目就先淡出，没有就直接上我方曲目。 */
	private static void begin(Minecraft minecraft) {
		boolean vanillaPlaying = minecraft.getMusicManager().getCurrentMusicTranslationKey() != null;
		if (vanillaPlaying) {
			phase = Phase.VANILLA_FADE;
			fadeTicks = 0;
			return;
		}

		sound = new BHR_TikopiaSoundInstance(1.0F);
		minecraft.getSoundManager().play(sound);
		phase = Phase.PLAYING;
	}

	/** 离开世界 / 回标题界面时的收尾。 */
	private static void reset(Minecraft minecraft) {
		if (sound != null) {
			minecraft.getSoundManager().stop(sound);
			sound = null;
		}
		if (phase != Phase.IDLE) {
			minecraft.getSoundManager().updateCategoryVolume(
					SoundSource.MUSIC, minecraft.getMusicVolume());
		}
		phase = Phase.IDLE;
		fadeTicks = 0;
		wasDowned = false;
	}

	/** 供调试/日志用：当前是否正在放濒死曲目。 */
	public static boolean isPlayingTikopia() {
		return phase == Phase.PLAYING || phase == Phase.OUR_FADE;
	}

	/** 供调试/日志用。 */
	public static String phaseName() {
		return phase.name() + (wasDowned ? "/downed" : "");
	}
}
