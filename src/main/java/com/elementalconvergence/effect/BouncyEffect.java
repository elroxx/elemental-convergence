package com.elementalconvergence.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class BouncyEffect extends StatusEffect {
    public BouncyEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x449659); //green
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false; //no effect here anyway
    }
}
