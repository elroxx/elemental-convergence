package com.elementalconvergence.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class GillsEffect extends StatusEffect {
    public GillsEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x7E9AB9);
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
