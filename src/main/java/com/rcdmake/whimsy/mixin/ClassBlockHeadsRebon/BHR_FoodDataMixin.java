package com.rcdmake.whimsy.mixin.ClassBlockHeadsRebon;

import com.rcdmake.whimsy.access.BHR_FoodDataAccess;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 把 {@code FoodData.exhaustionLevel} 读出来（26.2 没有公开 getter）。
 *
 * @see BHR_FoodDataAccess
 */
@Mixin(FoodData.class)
public abstract class BHR_FoodDataMixin implements BHR_FoodDataAccess {

	@Shadow
	private float exhaustionLevel;

	@Override
	public float whimsy$getExhaustion() {
		return this.exhaustionLevel;
	}
}
