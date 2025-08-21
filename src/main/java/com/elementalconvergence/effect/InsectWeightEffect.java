package com.elementalconvergence.effect;

import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InsectWeightEffect extends StatusEffect {

    public InsectWeightEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x90EE90); //light green
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true; // Apply every tick
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity instanceof PlayerEntity player) {
            World world = player.getWorld();
            BlockPos playerPos = player.getBlockPos();

            // Spawn subtle particles when standing on plant blocks
            BlockPos belowPos = playerPos.down();
            BlockState belowState = world.getBlockState(belowPos);

            if (isPlantBlock(belowState.getBlock()) && player.isOnGround()) {
                spawnInsectParticles(world, playerPos, amplifier);
            }
        }

        return true;
    }

    public static boolean isPlantBlock(Block block) {
        return block instanceof PlantBlock ||
                block instanceof LeavesBlock ||
                //block instanceof FlowerBlock ||
                //block instanceof TallPlantBlock ||
                block instanceof SugarCaneBlock ||
                block instanceof CactusBlock ||
                block instanceof VineBlock ||
                block instanceof KelpBlock ||
                //block instanceof SeagrassBlock ||
                //block instanceof SaplingBlock ||
                block instanceof BambooBlock; //commented ones are just children classes of PlantBlock
    }

    private void spawnInsectParticles(World world, BlockPos playerPos, int amplifier) {
        if (world instanceof ServerWorld serverWorld) {
            // COMPOSTER PARTICLES WHEN WALKING ON PLANTS WOOHOO
            serverWorld.spawnParticles(ParticleTypes.COMPOSTER,
                    playerPos.getX() + 0.5, playerPos.getY() + 0.1, playerPos.getZ() + 0.5,
                    1 + amplifier, 0.3, 0.05, 0.3, 0.01);
        }
    }
}
