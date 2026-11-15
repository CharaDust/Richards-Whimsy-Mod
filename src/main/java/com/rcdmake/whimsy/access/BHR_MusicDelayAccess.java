package com.rcdmake.whimsy.access;

/**
 * 把原版 {@code MusicManager.nextSongDelay} 的写入口挂出来（愚者模式的濒死 BGM 用）。
 *
 * <p>原版在 {@code MusicManager#stopPlaying()} 之后会把下一首的等待时间重算成
 * 「几分钟」，而我们只是临时借用 MUSIC 声道放自己的曲子：退出濒死时需要让正常音乐
 * <b>尽快接回来</b>，而不是等好几分钟。这个接口由
 * {@code com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon.BHR_MusicManagerMixin}
 * 实现在 {@code MusicManager} 上。
 *
 * <p>⚠️ 与 {@code DEV_HardcoreModeAccess} 同样的坑：duck 接口必须放在
 * <b>mixin 包之外</b>（这里放 {@code access} 包），否则会被 Loader 当成
 * "mixin 包里的普通类被直接引用"而报 IllegalClassLoadError。
 */
public interface BHR_MusicDelayAccess {

	/**
	 * 把下一首原版音乐的等待时间压到不超过 {@code ticks}（只在更短时才写）。
	 *
	 * @param ticks 目标等待 tick 数，例如 40 = 2 秒
	 */
	void whimsy$shortenNextSongDelay(int ticks);
}
