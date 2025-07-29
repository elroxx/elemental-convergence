package com.elementalconvergence.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.HorseColor;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class PegasusEntity extends HorseEntity {
    //private float jumpStrength = 1.0f;

    public PegasusEntity(EntityType<? extends PegasusEntity> entityType, World world) {
        super(entityType, world);
        // Always white variant
        this.setVariant(HorseColor.WHITE); // White horse variant
    }

    protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
        super.tickControlled(controllingPlayer, movementInput);
        Vec2f vec2f = this.getControlledRotation(controllingPlayer);
        this.setRotation(vec2f.y, vec2f.x);
        this.prevYaw = this.bodyYaw = this.headYaw = this.getYaw();
        if (this.isLogicalSideForUpdatingMovement()) {
            if (movementInput.z <= (double)0.0F) {
                this.soundTicks = 0;
            }

            //if (this.isOnGround()) {
                this.setInAir(false);
                if (this.jumpStrength > 0.0F) {
                    this.jump(this.jumpStrength, movementInput);
                }

                this.jumpStrength = 0.0F;
            //}
        }

    }

    @Override
    protected void jump(float strength, Vec3d movementInput) {
        double d = (double)this.getJumpVelocity(strength);
        Vec3d vec3d = this.getVelocity();
        this.setVelocity(vec3d.x, d, vec3d.z);
        this.setInAir(true);
        this.velocityDirty = true;
        if (movementInput.z > (double)0.0F) {
            float f = MathHelper.sin(this.getYaw() * ((float)Math.PI / 180F));
            float g = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180F));
            this.setVelocity(this.getVelocity().add((double)(-1.4F * f * strength), (double)0.0F, (double)(1.4F * g * strength)));
        }

        World world = this.getWorld();
        if (world instanceof ServerWorld serverWorld) {
            Random random = serverWorld.getRandom();
            for (int i = 0; i < 10; i++) {
                double offsetX = random.nextGaussian() * 0.2;
                double offsetZ = random.nextGaussian() * 0.2;
                serverWorld.spawnParticles(
                        ParticleTypes.CLOUD,
                        this.getX() + offsetX, this.getY(), this.getZ() + offsetZ, 1, 0, 0, 0, 0.1
                );
            }
        }

    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean canJump() {
        return true; // Always allow jumping
    }


    @Override
    protected void dropLoot(DamageSource damageSource, boolean causedByPlayer) {
        return;
    }

    @Override
    protected int getXpToDrop() {
        return 0; //no xp to drop
    }

    @Override
    protected void dropInventory() {
        // Don't drop inventory
    }

    @Override
    public boolean isBreedingItem(net.minecraft.item.ItemStack stack) {
        return false; // Cannot breed
    }

    @Override
    public boolean canBreedWith(net.minecraft.entity.passive.AnimalEntity other) {
        return false; // Cannot breed
    }


}