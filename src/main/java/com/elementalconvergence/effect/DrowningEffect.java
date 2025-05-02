package com.elementalconvergence.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class DrowningEffect extends StatusEffect {
    public DrowningEffect() {
        super(StatusEffectCategory.HARMFUL, 0x0C0845);
    }
}
