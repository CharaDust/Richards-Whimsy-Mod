package com.rcdmake.whimsy.tag.ClassLegacy;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class LGC_VanillaOre {
    public static class Blocks{
        public static final TagKey<Block> LGC_VANILLAORE = createTag("LGC_VanillaOre");
        // tag创建方法
        private static TagKey<Block> createTag(String name){
            return TagKey.of(RegistryKeys.BLOCK, new Identifier(RichardsWhimsyMod.MOD_ID, name));
        }
    }
}
