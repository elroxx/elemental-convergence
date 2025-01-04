package com.elementalconvergence.entity;


import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class ShadowballEntity extends SnowballEntity {
    public ShadowballEntity(EntityType<? extends SnowballEntity> entityType, World world) {
        super(entityType, world);
    }

    public ShadowballEntity(World world, LivingEntity owner) {
        super(world, owner);
    }

    public ShadowballEntity(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);

        if (!this.getWorld().isClient) {
            // Handle entity hits
            if (hitResult.getType() == HitResult.Type.ENTITY) {
                Entity entity = ((EntityHitResult)hitResult).getEntity();
                if (entity instanceof LivingEntity) {
                    ((LivingEntity)entity).addStatusEffect(
                            new StatusEffectInstance(StatusEffects.DARKNESS, 100, 0)
                    );
                }
            }
            // Handle block hits
            else if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult)hitResult;
                if (blockHit.getSide() == Direction.UP) {  // Check if hit top of block
                    BlockPos hitPos = blockHit.getBlockPos();
                    BlockPos spawnPos = hitPos.up();  // Position above hit block
                    World world = this.getWorld();
                    // Check if we can place a block here
                    if (world.isAir(spawnPos)) {
                        // Place your desired block. For example, let's place sculk
                        world.setBlockState(spawnPos, Blocks.SCULK.getDefaultState());
                    }
                }
            }

            // Spawn particles at impact
            ((ServerWorld) this.getWorld()).spawnParticles(
                    ParticleTypes.SCULK_SOUL,
                    this.getX(), this.getY(), this.getZ(),
                    20, 0.5, 0.5, 0.5, 0.1
            );

            this.getWorld().playSound(
                    null,
                    this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BLOCK_SCULK_CHARGE,
                    SoundCategory.NEUTRAL,
                    1.0F,
                    1.0F
            );

            this.discard();
        }
    }
}
