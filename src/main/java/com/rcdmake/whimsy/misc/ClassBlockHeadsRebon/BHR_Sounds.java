package com.rcdmake.whimsy.misc.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.ModRegister;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

/**
 * 愚者模式睡眠相关音效：时间加速开始 / 结束。
 *
 * <p>素材在 {@code assets/richards-whimsy-mod/sounds/bhr_speedup.ogg} 与 {@code bhr_slowdown.ogg}，
 * {@code sounds.json} 里必须有同名条目，否则注册了也发不出声。
 *
 * <p>注册方式与 {@code WI_Sounds} 一致：{@link SoundEvent#createVariableRangeEvent} +
 * {@code BuiltInRegistries.SOUND_EVENT}。
 */
public final class BHR_Sounds {

	/** 世界时钟切到加速档。 */
	public static SoundEvent BHR_SPEEDUP;

	/** 世界时钟恢复原速。 */
	public static SoundEvent BHR_SLOWDOWN;

	private BHR_Sounds() {
	}

	/** 由主类调用一次。 */
	public static void OnInit() {
		if (BHR_SPEEDUP != null) {
			return;
		}
		BHR_SPEEDUP = register("bhr_speedup");
		BHR_SLOWDOWN = register("bhr_slowdown");
	}

	private static SoundEvent register(String path) {
		return Registry.register(
				BuiltInRegistries.SOUND_EVENT,
				ModRegister.id(path),
				SoundEvent.createVariableRangeEvent(ModRegister.id(path)));
	}
}
