package com.rcdmake.whimsy.function.ClassRandomThings.Effect;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

// 注释请移步 RDT_EffectPoisonImbue 查看，这里不做赘述
public class RDT_EffectFireImbue extends StatusEffect {
    // super
    protected RDT_EffectFireImbue(StatusEffectCategory category, int color) {
        super(category, color);
    }

    public static final StatusEffect RDT_FIRE_IMBUE = new RDT_EffectFireImbue(StatusEffectCategory.BENEFICIAL, 0xF68127);
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }
    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
    }
    private static void register_StatusEffect(String path, StatusEffect SE) {
        Registry.register(Registries.STATUS_EFFECT, new Identifier(RichardsWhimsyMod.MOD_ID, path), SE);
    }
    public static void OnInit(){
        register_StatusEffect("rdt_fire_imbue", RDT_FIRE_IMBUE);
    }
}
