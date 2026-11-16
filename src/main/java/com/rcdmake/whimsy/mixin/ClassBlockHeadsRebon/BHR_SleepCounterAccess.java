package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * {@code Player#sleepCounter} 的访问器。
 *
 * <p>强制睡眠走的是 {@code startSleeping}（不是 {@code startSleepInBed}），
 * 而「睡够没有」全靠这个计数器（{@code isSleepingLongEnough()} = 睡 &amp;&amp; 计数 ≥ 100）。
 * 不归零的话，刚醒过的人（计数停在 100）再躺下会立刻被当成「睡够」，
 * 直接白拿一份加速/跳夜记账。
 */
@Mixin(Player.class)
public interface BHR_SleepCounterAccess {

	@Accessor("sleepCounter")
	void whimsy$setSleepCounter(int value);

	@Accessor("sleepCounter")
	int whimsy$getSleepCounter();
}
