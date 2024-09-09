package com.rcdmake.whimsy.function.ClassRandomThings.Effect.Handler;

import com.rcdmake.whimsy.function.ClassRandomThings.Effect.RDT_EffectFireImbue;
import com.rcdmake.whimsy.function.ClassRandomThings.Effect.RDT_EffectPoisonImbue;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.ActionResult;

public class RDT_ImbueAttackEventHandler {
    public static void OnInit(){
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof LivingEntity) {
                // 攻击对象
                LivingEntity attackTarget = (LivingEntity) entity;

                // 毒性灌注
                if (player.hasStatusEffect(RDT_EffectPoisonImbue.RDT_POISON_IMBUE)) {
                    attackTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 200, 1));
                }
                // 火焰灌注
                if (player.hasStatusEffect(RDT_EffectFireImbue.RDT_FIRE_IMBUE)) {
                    attackTarget.setFireTicks(200);
                }
            }
            return ActionResult.PASS;
        });
    }
}
