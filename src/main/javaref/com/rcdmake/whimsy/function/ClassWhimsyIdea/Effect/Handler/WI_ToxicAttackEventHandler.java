package com.rcdmake.whimsy.function.ClassWhimsyIdea.Effect.Handler;

import com.rcdmake.whimsy.function.ClassWhimsyIdea.Effect.WI_EffectBioToxicity;
import com.rcdmake.whimsy.function.ClassWhimsyIdea.Effect.WI_EffectRadioactive;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;

public class WI_ToxicAttackEventHandler {
    // 获取生物特定药水效果的剩余时间
    private static int getEffectRemainingDuration(LivingEntity entity, StatusEffect effect) {
        StatusEffectInstance effectInstance = entity.getStatusEffect(effect);
        if (effectInstance != null) {
            return effectInstance.getDuration();
        }
        return 0;
    }
    // 获取生物特定药水效果的增幅
    private static int getEffectAmplifier(LivingEntity entity, StatusEffect effect) {
        StatusEffectInstance effectInstance = entity.getStatusEffect(effect);
        if (effectInstance != null) {
            return effectInstance.getAmplifier();
        }
        return 0;
    }
    private static void createAreaEffectCloud(ServerWorld world, LivingEntity entity, StatusEffectInstance effectInstance) {
        AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(world, entity.getX(), entity.getY(), entity.getZ());
        cloud.setRadius(5.0F);
        cloud.setDuration(20); // 持续时间为1秒（20 tick）
        cloud.addEffect(new StatusEffectInstance(WI_EffectRadioactive.WI_RADIOACTIVE, effectInstance.getDuration(), effectInstance.getAmplifier()));
        world.spawnEntity(cloud);
    }
    // 免疫效果的生物
    private static boolean isImmune(LivingEntity target) {
        return target instanceof EnderDragonEntity
                || target instanceof IronGolemEntity
                || target instanceof SnowGolemEntity
                || target instanceof WitherEntity
                || target instanceof SkeletonEntity
                || target instanceof SkeletonHorseEntity
                || target instanceof WitherSkeletonEntity
                || target instanceof StrayEntity
                || target instanceof BlazeEntity;
    }

    // 主逻辑
    public static void OnInit(){
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof LivingEntity) {
                // 攻击对象
                LivingEntity attackTarget = (LivingEntity) entity;
                int remainingDuration = getEffectRemainingDuration(player, WI_EffectBioToxicity.WI_BIO_TOXICITY);
                int Amplifier = getEffectAmplifier(player, WI_EffectBioToxicity.WI_BIO_TOXICITY);

                if(!isImmune(attackTarget)){
                    if (player.hasStatusEffect(WI_EffectBioToxicity.WI_BIO_TOXICITY) && Amplifier == 0) {
                        attackTarget.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 240, 0));
                    }
                    if (player.hasStatusEffect(WI_EffectBioToxicity.WI_BIO_TOXICITY) && Amplifier != 0) {
                        attackTarget.addStatusEffect(new StatusEffectInstance(WI_EffectBioToxicity.WI_BIO_TOXICITY, remainingDuration/2, Amplifier - 1));
                    }
                }



            }
            return ActionResult.PASS;
        });
    }

}
