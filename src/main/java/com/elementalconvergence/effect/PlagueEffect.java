package com.elementalconvergence.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class PlagueEffect extends StatusEffect {
    public static final int TICK_INTERVAL = 30;

    public PlagueEffect() {
        super(
                StatusEffectCategory.HARMFUL,
                0x323B14 // Color in RGB (green-ish for plague)
        );
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        //so apply once per second
        return duration % TICK_INTERVAL == 0;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        //1 dmg (so half a heart) per lvl each second
        entity.damage(entity.getDamageSources().magic(), (amplifier + 1));
        return true;
    }
}