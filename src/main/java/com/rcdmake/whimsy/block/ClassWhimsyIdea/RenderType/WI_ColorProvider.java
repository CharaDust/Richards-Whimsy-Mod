package com.rcdmake.whimsy.block.ClassWhimsyIdea.RenderType;

import com.rcdmake.whimsy.block.ClassWhimsyIdea.WI_StaticLiquidWater;
import com.sun.jna.platform.win32.Wincon;
import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

public class WI_ColorProvider implements BlockColorProvider {

    // 接口方法：获取颜色
    @Override
    public int getColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex) {
        int rslt = 0x00FFFF;
        if (world instanceof WorldView worldView && pos != null) {
            RegistryEntry<Biome> biomeEntry = worldView.getBiome(pos);
            if (biomeEntry != null) {
                Biome biome = biomeEntry.value();
//                System.out.println("Biome water color: " + biome.getWaterColor());
                rslt = biome.getWaterColor();
                return biome.getWaterColor(); // 返回水的颜色
            }
        }
        return rslt;
        // 默认为错误颜色
//        return 0xFF00FF;
    }

    // OnInit
    public static void OnInitClient(){
        // 注册方块颜色
        ColorProviderRegistry.BLOCK.register(new WI_ColorProvider(), WI_StaticLiquidWater.WI_STATICLIQUIDWATER);
        // 注册物品颜色
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
//            System.out.println("ItemColorProvider called with tintIndex: " + tintIndex);
            return 0xFF00FF;
        }, WI_StaticLiquidWater.WI_STATICLIQUIDWATER);
    }
}
