package com.elementalconvergence.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;

public class WingsEffect extends StatusEffect {
    public WingsEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0xDEDEDE); // gray almost white
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return false;
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        return false;
    }

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        super.onApplied(entity, amplifier);
        if (entity instanceof PlayerEntity player) {
            // if player falling add the wings but idk
            if (!player.isOnGround() && player.getVelocity().y < -0.5) {
                player.startFallFlying();
            }
        }
    }
}