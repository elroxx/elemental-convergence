package com.elementalconvergence.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class DrowningEffect extends StatusEffect {
    public DrowningEffect() {
        super(StatusEffectCategory.HARMFUL, 0x0C0845);
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        return false;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false;
    }
}
