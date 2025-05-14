package com.rcdmake.whimsy.mixin.ClassBlockHeadsReborn;

import com.rcdmake.whimsy.block.ClassBlockHeadsRebon.BHR_Compost;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CropBlock.class)
public class BHR_Compost4CropMixin {
    // 对 CropBlock.class 中的 canPlantOnTop 方法进行修改
    @Inject(method = "canPlantOnTop", at = @At("HEAD"), cancellable = true)
    private void allowBhrCompost(BlockState floor, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // 测试信息：注入成功
        // player.sendMessage(Text.literal("(Debug) Injected").formatted(Formatting.LIGHT_PURPLE), true);
        //  System.out.println("(Debug) Injected");
        // 如果底部方块是自定义的 bhr_compost 方块，允许种植
        if (floor.isOf(BHR_Compost.BHR_COMPOST)) {
            cir.setReturnValue(true);
        }
    }

    // 对 CropBlock.class 中的 getAvailableMoisture 方法进行修改
    @Inject(method = "getAvailableMoisture", at = @At("HEAD"), cancellable = true)
    private static void MaxCropGrowSpeed(Block block, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (world.getBlockState(pos.down()).isOf(BHR_Compost.BHR_COMPOST)) {
            // 极端测试
//            cir.setReturnValue(35F);
            // 快速生长 (1/2)
//            cir.setReturnValue(15F);
            // 原版最大值 (1/3)
//            cir.setReturnValue(9F);
            // 稍慢一点 (1/4)
            cir.setReturnValue(8F);
            // 再慢一点 (1/5)
//            cir.setReturnValue(6F);
            // 半速 (1/6)
//            cir.setReturnValue(5F);
        }
    }

}
