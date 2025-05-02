package com.elementalconvergence.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class FullBlindnessEffect extends StatusEffect {
    public FullBlindnessEffect() {
        super(StatusEffectCategory.HARMFUL, 0x000000); // Black color
    }
}

