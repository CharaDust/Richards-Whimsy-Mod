package com.rcdmake.whimsy.mixin.client.ClassBlockHeadsRebon;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * {@code Screen#addRenderableWidget} 的调用器。
 *
 * <p>它是 {@code protected}，在别的类里既调不到、也不能用 {@code @Shadow} 影子化
 * （影子方法只能指向**目标类自己**声明的成员；{@code AbstractContainerScreen} 里没有它，
 * 会直接抛 {@code @Shadow method was not located in the target class}）。
 * 所以对 {@link Screen} 单独开一个 {@code @Invoker}。
 */
@Mixin(Screen.class)
public interface BHR_ScreenAccess {

	@Invoker("addRenderableWidget")
	GuiEventListener whimsy$addRenderableWidget(GuiEventListener widget);
}
