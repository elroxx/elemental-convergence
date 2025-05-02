package com.elementalconvergence.effect;

import gravity_changer.api.GravityChangerAPI;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.math.Direction;

public class LightPhasingEffect extends StatusEffect {
    public LightPhasingEffect() {
        super(StatusEffectCategory.HARMFUL, 0xE5FF00); // blue
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true; // Apply effect every tick
    }



}
