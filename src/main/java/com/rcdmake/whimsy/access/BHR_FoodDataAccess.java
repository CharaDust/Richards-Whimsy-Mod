package com.rcdmake.whimsy.access;

/**
 * 读 {@code FoodData} 的<b>消耗度</b>（{@code exhaustionLevel}）的 duck 接口。
 *
 * <p>26.2 的 {@code FoodData} 把这一项彻底私有化了：只有 {@code addExhaustion(...)}，
 * <b>没有 getter</b>（连 {@code getExhaustionLevel()} 都没有——历史上是有过的）。
 * 而愚者模式的饥饿条要画消耗度叠加层、还要按「消耗度增量」出箭头，
 * 客户端与服务端都得读它，所以由 {@code BHR_FoodDataMixin} 把它挂出来。
 *
 * <p>放在 {@code access} 包（不在 mixin 包）—— 原因见 {@code DEV_HardcoreModeAccess} 的注释。
 */
public interface BHR_FoodDataAccess {

	/** 当前消耗度（原版 {@code addExhaustion} 夹在 0~40）。 */
	float whimsy$getExhaustion();
}
