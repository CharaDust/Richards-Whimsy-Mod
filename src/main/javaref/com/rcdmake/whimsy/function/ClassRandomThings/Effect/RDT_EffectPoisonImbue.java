package com.rcdmake.whimsy.function.ClassRandomThings.Effect;

import com.rcdmake.whimsy.RichardsWhimsyMod;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class RDT_EffectPoisonImbue extends StatusEffect {
    // super
    protected RDT_EffectPoisonImbue(StatusEffectCategory category, int color) {
        super(category, color);
    }

    // 定义
    public static final StatusEffect RDT_POISON_IMBUE = new RDT_EffectPoisonImbue(StatusEffectCategory.BENEFICIAL, 0x41DD53);

    // 确保每一 tick 药水效果都会被应用，我们只要这个方法返回 true 就行了。
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    // 当上面的判定返回 true 时，开始执行一次这里的效果
    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        // 只做状态提示，不执行逻辑，或许以后会加上着色器？
        // 攻击效果移动到了 Handler/RDT_ImbueAttackEventHandler 类中
    }

    // 注册方法简化写法
    private static void register_StatusEffect(String path, StatusEffect SE) {
        Registry.register(Registries.STATUS_EFFECT, new Identifier(RichardsWhimsyMod.MOD_ID, path), SE);
    }
    public static void OnInit(){
        register_StatusEffect("rdt_poison_imbue", RDT_POISON_IMBUE);
    }
}
