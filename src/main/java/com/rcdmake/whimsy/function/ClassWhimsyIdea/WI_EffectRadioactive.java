package com.rcdmake.whimsy.function.ClassWhimsyIdea;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class WI_EffectRadioactive extends StatusEffect {
    protected WI_EffectRadioactive(StatusEffectCategory category, int color) {
        super(category, color);
    }

    // 定义
    public static final StatusEffect WI_RADIOACTIVE = new WI_EffectRadioactive(StatusEffectCategory.HARMFUL, 0xF3A923);

    boolean runDamage = false;
    boolean removeEffect = false;
    // 判断是否每隔一段时间应用一次效果
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        // 取余运算获取间隔
        switch (amplifier) {
            case 0:
                if (duration % 100 == 0) {
                    runDamage = true;
                }
                break;
            case 1:
                if (duration % 80 == 0) {
                    runDamage = true;
                }
                break;
            case 2:
                if (duration % 60 == 0) {
                    runDamage = true;
                }
                break;
            case 3:
                if (duration % 40 == 0) {
                    runDamage = true;
                }
                break;
            default:
                if (duration % 20 == 0) {
                    runDamage = true;
                }
                break;
        }
        if (duration == 1) {
            removeEffect = true;
        }
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        // 每隔一段时间对目标造成1点火焰伤害，伤害间隔取决于效果等级
        if (runDamage) {
            if (entity.getHealth() > 6.0F) {
                entity.damage(entity.getDamageSources().inFire(), 2.0F);
            }
            runDamage = false;
        }
        // 时间结束移除生命上限效果
        if (removeEffect) {
            entity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).removeModifier(HEALTH_MODIFIER_UUID);
            removeEffect = false;
        }
    }
    // 修饰符UUID
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("e2c12a53-7560-4c71-a0d8-77f5ae5baaf2");

    // 生命值上限修改
    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        // 原始生命值上限
        double raw_max_health = entity.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH).getBaseValue();
        EntityAttributeModifier modifier = new EntityAttributeModifier(
                HEALTH_MODIFIER_UUID,
                "radioactive_effect",
                0-(raw_max_health * (1 - (1 / (1.5 + (0.5 *amplifier))))),
                EntityAttributeModifier.Operation.ADDITION
        );
        // 移除原来的修饰符
        entity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).removeModifier(HEALTH_MODIFIER_UUID);

        // 将修饰符添加到实体的生命值属性中
        entity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).addTemporaryModifier(modifier);

        // 扣除多余的血量
        entity.setHealth(Math.min(entity.getHealth(), entity.getMaxHealth()));
    }

    // 恢复生命值
    private void onRemoved(LivingEntity entity, int amplifier) {
        // 移除修饰符
        entity.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).removeModifier(HEALTH_MODIFIER_UUID);
    }

    // 获取生物特定药水效果的剩余时间
    private int getEffectRemainingDuration(LivingEntity entity, StatusEffect effect) {
        StatusEffectInstance effectInstance = entity.getStatusEffect(effect);
        if (effectInstance != null) {
            return effectInstance.getDuration();
        }
        return 0;
    }

    // 注册方法简化写法
    private static void register_StatusEffect(String path, StatusEffect SE) {
        Registry.register(Registries.STATUS_EFFECT, new Identifier(RichardsWhimsyMod.MOD_ID, path), SE);
    }

    public static void OnInit(){
        register_StatusEffect("wi_radioactive", WI_RADIOACTIVE);
    }
}
