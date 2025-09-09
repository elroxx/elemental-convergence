package com.elementalconvergence.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class BatFormEffect extends StatusEffect {
    public BatFormEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x212121); //dark gray
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false; //no effect here anyway
    }
}
