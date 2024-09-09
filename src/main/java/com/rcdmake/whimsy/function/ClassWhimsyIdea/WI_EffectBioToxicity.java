package com.rcdmake.whimsy.function.ClassWhimsyIdea;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class WI_EffectBioToxicity extends StatusEffect {
    // super
    protected WI_EffectBioToxicity(StatusEffectCategory category, int color) {
        super(category, color);
    }
    // 定义
    public static final StatusEffect WI_BIO_TOXICITY = new WI_EffectBioToxicity(StatusEffectCategory.HARMFUL, 0x007951);

    boolean runDamage = false;
    // 判断是否每隔一段时间应用一次效果
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        // 取余运算获取间隔
        switch (amplifier) {
            case 0:
                if (duration % 80 == 0) {
                    runDamage = true;
                }
                break;
            case 1:
                if (duration % 60 == 0) {
                    runDamage = true;
                }
                break;
            case 2:
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
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        // 每隔一段时间对目标造成1点魔法伤害，伤害间隔取决于效果等级
        if (runDamage) {
            if (entity.getHealth() > 6.0F) {
                entity.damage(entity.getDamageSources().magic(), 2.0F);
            }
            runDamage = false;
        }
    }

    // 注册方法简化写法
    private static void register_StatusEffect(String path, StatusEffect SE) {
        Registry.register(Registries.STATUS_EFFECT, new Identifier(RichardsWhimsyMod.MOD_ID, path), SE);
    }
    public static void OnInit(){
        register_StatusEffect("wi_bio_toxicity", WI_BIO_TOXICITY);
    }
}
