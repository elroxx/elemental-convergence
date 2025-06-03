package com.elementalconvergence.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class GuardianAngelEffect extends StatusEffect {
    public GuardianAngelEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0xFFF6BD); // very light yellow
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false; // since there is no current updates
    }
}
