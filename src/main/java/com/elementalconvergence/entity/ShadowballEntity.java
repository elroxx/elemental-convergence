package com.elementalconvergence.entity;


import com.elementalconvergence.block.ModBlocks;
import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SideShapeType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
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

import static com.elementalconvergence.magic.handlers.ShadowMagicHandler.SHADOW_INDEX;

public class ShadowballEntity extends SnowballEntity {
    private PlayerEntity owner = null;



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
            if (owner!=null) {
                IMagicDataSaver dataSaver = (IMagicDataSaver) owner;
                MagicData magicData = dataSaver.getMagicData();
                int shadowLevel = magicData.getMagicLevel(SHADOW_INDEX);
                if (shadowLevel >= 3) {
                    // Handle entity hits
                    if (hitResult.getType() == HitResult.Type.ENTITY) {
                        Entity entity = ((EntityHitResult) hitResult).getEntity();
                        if (entity instanceof LivingEntity) {
                            ((LivingEntity) entity).addStatusEffect(
                                    new StatusEffectInstance(StatusEffects.DARKNESS, 100, 0)
                            );
                        }
                    }
                    // Handle block hits
                    else if (hitResult.getType() == HitResult.Type.BLOCK) {
                        BlockHitResult blockHit = (BlockHitResult) hitResult;
                        if (blockHit.getSide() == Direction.UP) {  // Check if hit top of block
                            BlockPos hitPos = blockHit.getBlockPos();
                            BlockPos spawnPos = hitPos.up();  // Position above hit block
                            World world = this.getWorld();
                            // Check if we can place a block here
                            BlockState blockState = world.getBlockState(hitPos);
                            if (blockState.isSideSolid(world, hitPos, Direction.UP, SideShapeType.FULL)) {
                                //PLACE GOOD BLOCK
                                world.setBlockState(spawnPos, ModBlocks.BLACK_SNOW_LAYER.getDefaultState());
                            }
                        }
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

    public void setOwner(PlayerEntity user){
        this.owner = user;
    }
}
