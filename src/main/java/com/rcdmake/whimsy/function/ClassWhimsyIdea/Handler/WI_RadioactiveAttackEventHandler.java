package com.rcdmake.whimsy.function.ClassWhimsyIdea.Handler;

import com.rcdmake.whimsy.function.ClassWhimsyIdea.WI_EffectRadioactive;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class WI_RadioactiveAttackEventHandler {
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
    // 创建效果云
    private static void createRadiationCloud(ServerWorld world, LivingEntity entity, StatusEffectInstance effectInstance) {
        AreaEffectCloudEntity cloud = new AreaEffectCloudEntity(world, entity.getX(), entity.getY(), entity.getZ());
        cloud.setRadius(5.0F);
        cloud.setDuration(20); // 持续时间为1秒（20 tick）
        cloud.addEffect(new StatusEffectInstance(WI_EffectRadioactive.WI_RADIOACTIVE, effectInstance.getDuration(), effectInstance.getAmplifier()));
        world.spawnEntity(cloud);
    }

    //OnInit
    public static void OnInit() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, target) -> {
            if (entity instanceof LivingEntity && target instanceof LivingEntity) {
                LivingEntity attacker = (LivingEntity) entity;
                LivingEntity attacked = (LivingEntity) target;
                // 检测药水效果
                if (attacked.hasStatusEffect(WI_EffectRadioactive.WI_RADIOACTIVE)) {
                    StatusEffectInstance effectInstance = attacked.getStatusEffect(WI_EffectRadioactive.WI_RADIOACTIVE);
                    if (effectInstance != null) {
                        int amplifier = effectInstance.getAmplifier();
                        if (amplifier > 0) {
                            // 2级以上时，将低一等级效果传递给攻击目标，时间为效果持有者的一半
                            attacker.addStatusEffect(new StatusEffectInstance(WI_EffectRadioactive.WI_RADIOACTIVE, effectInstance.getDuration() / 2, amplifier - 1));
                        }
                    }
                }
            }
        });
    }


}
