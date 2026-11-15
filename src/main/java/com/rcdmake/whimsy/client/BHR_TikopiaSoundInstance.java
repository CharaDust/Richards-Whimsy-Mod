package com.rcdmake.whimsy.client;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * 濒死 BGM 的播放实例（{@code bhr_tikopia}）。
 *
 * <p>用 {@link SoundSource#MUSIC} 声道，所以跟随玩家的「音乐」音量；
 * 相对坐标 + 无衰减，跟原版背景音乐一样是"贴脸"的；
 * {@code looping} 打开，濒死期间一直循环。
 * {@link #setVolume(float)} 是给淡出用的（原版 {@code AbstractSoundInstance}
 * 的 {@code volume} 每次读都现算，所以每帧改就能听到渐变）。
 */
public class BHR_TikopiaSoundInstance extends AbstractSoundInstance {

	/** 音源 id（{@code sounds.json} 里的键）。 */
	public static final Identifier ID = Identifier.fromNamespaceAndPath("richards-whimsy-mod", "bhr_tikopia");

	public BHR_TikopiaSoundInstance(float volume) {
		super(ID, SoundSource.MUSIC, RandomSource.create());
		this.looping = true;
		this.relative = true;
		this.attenuation = SoundInstance.Attenuation.NONE;
		this.volume = volume;
	}

	/** 直接改音量（淡出用；调用方负责乘上玩家的音乐音量）。 */
	public void setVolume(float volume) {
		this.volume = volume;
	}
}
