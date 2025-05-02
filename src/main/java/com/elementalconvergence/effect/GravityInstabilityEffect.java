package com.elementalconvergence.effect;

import gravity_changer.api.GravityChangerAPI;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;

public class GravityInstabilityEffect extends StatusEffect {
    public GravityInstabilityEffect() {
        super(StatusEffectCategory.HARMFUL, 0x3951BA); // blue
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
            Direction oldDirection = GravityChangerAPI.getBaseGravityDirection(entity);
            Direction newDirection = getDirectionFromAmplifier(amplifier);
            if (!oldDirection.equals(newDirection)) {
                GravityChangerAPI.setBaseGravityDirection(entity, newDirection);
            }
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true; // Apply effect every tick
    }


    private Direction getDirectionFromAmplifier(int amplifier) {
        return switch (amplifier + 1) { // +1 because amplifier starts at 0
            case 1 -> Direction.DOWN;  // Level I
            case 2 -> Direction.UP;    // Level II
            case 3 -> Direction.NORTH; // Level III
            case 4 -> Direction.WEST;  // Level IV
            case 5 -> Direction.SOUTH; // Level V
            case 6 -> Direction.EAST;  // Level VI
            default -> Direction.DOWN; // Any other level
        };
    }
}
