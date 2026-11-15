package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 愚者模式：给原版枚举 {@link GameType} 加一个常量 {@code BHR_FOOL_MODE}（序号名 {@code fool}）。
 *
 * <h2>为什么必须是「枚举扩展」而不是普通 Mixin</h2>
 * {@code GameType} 是 {@code enum}，常量在 {@code <clinit>} 里就被编进 {@code $VALUES} 数组，
 * 普通 {@code @Inject} 改不到「枚举常量集合」本身。26.2 的 Fabric 提供了枚举扩展：
 * <ul>
 *   <li>{@code richards-whimsy-mod.classtweaker} 里的
 *       {@code extend-enum net/minecraft/world/level/GameType BHR_FOOL_MODE} 先给目标补一个占位字段；</li>
 *   <li>本 Mixin 以 <b>enum</b> 的形式声明同名字段（下面第一行），Mixin 在运行期把它替换成
 *       真正的枚举常量、追加进 {@code values()}。</li>
 * </ul>
 * 两者缺一不可 —— 只写 Mixin 不写 classtweaker，常量不会出现在 {@code values()} 里，
 * {@code /gamemode fool} 与存档里的 id 4 都会失效。
 *
 * <h2>构造参数为什么是字面量 4 / "fool"</h2>
 * {@code GameType(int id, String name)}：{@code id} 是网络与存档用的稳定序号
 * （{@code GameType.LEGACY_ID_CODEC} 与 {@code STREAM_CODEC} 都按它走），故写死 {@code 4}
 * （原版占用 0~3）；{@code name} 是序列化名，同时决定语言键
 * {@code gameMode.fool} / {@code selectWorld.gameMode.fool}。
 *
 * <h2>行为：照抄生存模式</h2>
 * 原版 {@code isSurvival()} 只认 {@code SURVIVAL}/{@code ADVENTURE}，这里补一条
 * {@code fool → true}，于是所有「生存模式该有的」判定（饥饿、方块破坏、不能飞行、
 * 受伤/死亡、方块掉落……）自动与生存一致。{@code isCreative()} 与
 * {@code isBlockPlacingRestricted()} 本来就是「不是创造 / 不是冒险就为假」，
 * {@code updatePlayerAbilities()} 的 else 分支也已经给出生存档能力，均无需改动。
 */
@Mixin(GameType.class)
enum BHR_FoolGameTypeMixin {

	/** 愚者模式常量本身（名字与 classtweaker 的 extend-enum 一致）。 */
	BHR_FOOL_MODE(4, "fool");

	/**
	 * 影子构造器：与 {@code GameType(int, String)} 同签名。
	 * 只有声明了它，上面那个常量的 {@code (4, "fool")} 才有地方去 —— Mixin 会把实参
	 * 交给目标枚举真正的构造器。
	 */
	@Shadow
	BHR_FoolGameTypeMixin(int id, String name) {
	}

	/**
	 * 让愚者模式在「是否生存」这一点上与生存模式完全一致。
	 *
	 * <p>返回处注入而不是 {@code @Overwrite}：原版方法体保留，
	 * 未来原版若再加判定（比如把某种新模式也算生存）不会被我覆盖掉。
	 */
	@Inject(method = "isSurvival", at = @At("RETURN"), cancellable = true)
	private void whimsy$foolModeIsSurvival(CallbackInfoReturnable<Boolean> cir) {
		if ("fool".equals(((GameType) (Object) this).getName())) {
			cir.setReturnValue(true);
		}
	}
}
