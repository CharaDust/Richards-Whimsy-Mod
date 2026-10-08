package com.rcdmake.whimsy.item.ClassWhimsyIdea;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class WI_ShaderDevRed extends Item implements ClientModInitializer {
    // super
    public WI_ShaderDevRed(Settings settings) {
        super(settings);
    }
    public static final Item WI_SHADERDEVRED = registerItems("wi_shader_dev_red", new WI_ShaderDevRed(new FabricItemSettings()));
    // 物品注册方法
    private static Item registerItems(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(RichardsWhimsyMod.MOD_ID, name),item);
    }

    public static void OnInit(){

    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            ItemStack mainHandStack = mc.player.getMainHandStack();

            if (mainHandStack.getItem() == WI_SHADERDEVRED) {
                if (true)
                    mc.gameRenderer.setBlockOutlineEnabled(true);
            }
        });
    }
}
