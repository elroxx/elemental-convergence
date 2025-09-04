package com.elementalconvergence.mixin;

import com.elementalconvergence.enchantment.ModEnchantments;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin {

    @Shadow @Nullable private BlockState inBlockState;
    @Shadow protected boolean inGround;
    @Shadow public int shake;

    @Shadow public abstract void setVelocity(double x, double y, double z, float power, float uncertainty);

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        PersistentProjectileEntity arrow = (PersistentProjectileEntity) (Object) this;

        // Check if arrow has bounce cooldown
        NbtCompound nbt = arrow.writeNbt(new NbtCompound());
        if (nbt.contains("BounceCooldown")) {
            int cooldown = nbt.getInt("BounceCooldown");
            if (cooldown > 0) {
                // Prevent the arrow from being marked as inGround during cooldown
                this.inGround = false;
                this.inBlockState = null;

                // Decrease cooldown
                nbt.putInt("BounceCooldown", cooldown - 1);
                arrow.readNbt(nbt);

                // Ensure arrow keeps moving
                Vec3d velocity = arrow.getVelocity();
                if (velocity.lengthSquared() < 0.01) { // If velocity is too small
                    // Give it a small push to keep it moving
                    arrow.setVelocity(velocity.add(0, 0.1, 0));
                }
            }
        }
    }

    @Inject(method = "onBlockHit", at = @At("HEAD"), cancellable = true)
    private void onBlockHit(BlockHitResult blockHitResult, CallbackInfo ci) {
        PersistentProjectileEntity arrow = (PersistentProjectileEntity) (Object) this;

        // Check if this arrow has bouncy enchantment using NBT data
        NbtCompound nbt = arrow.writeNbt(new NbtCompound());

        if (nbt.contains("IsBouncy") && nbt.getBoolean("IsBouncy")) {
            int bouncesRemaining = nbt.getInt("BouncesRemaining");

            if (bouncesRemaining > 0) {
                // Calculate bounce physics
                Vec3d velocity = arrow.getVelocity();
                Vec3d hitPos = blockHitResult.getPos();
                Direction hitSide = blockHitResult.getSide();

                // Calculate new velocity based on hit surface
                Vec3d newVelocity = calculateBounceVelocity(velocity, hitSide);

                // Apply velocity multiplier of 1.05 as requested
                newVelocity = newVelocity.multiply(1.05);

                // Reduce bounces remaining and update NBT
                nbt.putInt("BouncesRemaining", bouncesRemaining - 1);
                arrow.readNbt(nbt);

                // Critical: Ensure the arrow is not stuck in ground or block
                this.inGround = false;
                this.inBlockState = null;
                this.shake = 0;

                // Move the arrow slightly away from the hit surface to prevent immediate re-collision
                Vec3d surfaceNormal = Vec3d.of(hitSide.getVector());
                Vec3d currentPos = arrow.getPos();
                Vec3d newPosition = currentPos.add(surfaceNormal.multiply(0.2)); // Increased offset
                arrow.setPos(newPosition.x, newPosition.y, newPosition.z);

                // Apply new velocity using the proper shadow method
                this.setVelocity(newVelocity.x, newVelocity.y, newVelocity.z, 1.0f, 0.0f);
                arrow.velocityModified = true;

                // Reset collision states that might interfere
                arrow.setOnGround(false);

                // Reset age to prevent despawning behavior
                arrow.age = Math.max(0, arrow.age - 100);

                // Play bounce sound with slight variation
                arrow.getWorld().playSound(null, arrow.getBlockPos(),
                        SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.NEUTRAL, 0.7f,
                        1.0f + (float)(Math.random() * 0.8 - 0.4)); // More pitch variation

                // Cancel the original block hit behavior completely
                ci.cancel();
                return;
            }
        }
    }

    private Vec3d calculateBounceVelocity(Vec3d velocity, Direction hitSide) {
        double x = velocity.x;
        double y = velocity.y;
        double z = velocity.z;

        // Store original speed for reference
        double originalSpeed = velocity.length();

        // Reflect velocity based on the surface normal
        switch (hitSide) {
            case UP -> {
                y = Math.abs(y); // Always bounce upward when hitting from above
                // Ensure minimum upward velocity to prevent getting stuck on ground
                if (y < 0.4) {
                    y = 0.5 + (Math.random() * 0.3);
                }
            }
            case DOWN -> {
                y = -Math.abs(y); // Always bounce downward when hitting ceiling
                // Ensure minimum downward velocity
                if (Math.abs(y) < 0.3) {
                    y = -0.4;
                }
            }
            case NORTH -> {
                z = Math.abs(z); // Bounce towards positive Z
                if (Math.abs(z) < 0.2) z = 0.3;
            }
            case SOUTH -> {
                z = -Math.abs(z); // Bounce towards negative Z
                if (Math.abs(z) < 0.2) z = -0.3;
            }
            case EAST -> {
                x = -Math.abs(x); // Bounce towards negative X
                if (Math.abs(x) < 0.2) x = -0.3;
            }
            case WEST -> {
                x = Math.abs(x); // Bounce towards positive X
                if (Math.abs(x) < 0.2) x = 0.3;
            }
        }

        Vec3d newVelocity = new Vec3d(x, y, z);

        // Ensure the new velocity isn't too weak or too strong
        double newSpeed = newVelocity.length();
        if (newSpeed < 0.3) {
            // If velocity is too small, give it a reasonable boost
            newVelocity = newVelocity.normalize().multiply(0.5);
        } else if (newSpeed > originalSpeed * 2.5) {
            // Prevent excessive speeds that could cause issues
            newVelocity = newVelocity.normalize().multiply(originalSpeed * 2.0);
        }

        return newVelocity;
    }
}