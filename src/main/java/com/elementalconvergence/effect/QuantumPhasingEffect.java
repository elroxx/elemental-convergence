package com.elementalconvergence.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class QuantumPhasingEffect extends StatusEffect {
    public QuantumPhasingEffect() {
        super(StatusEffectCategory.HARMFUL, 0x25588F); // blue
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false; // since there is no current updates
    }



}
