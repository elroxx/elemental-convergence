package com.elementalconvergence.mixin;

import com.elementalconvergence.enchantment.ModEnchantments;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public class PersistentProjectileEntityMixin {

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

                // Reduce bounces remaining and update NBT
                nbt.putInt("BouncesRemaining", bouncesRemaining - 1);
                arrow.readNbt(nbt);

                // Apply new velocity with some energy loss (0.7 factor for realistic bouncing)
                arrow.setVelocity(newVelocity.multiply(0.7));
                arrow.velocityModified = true;

                // Prevent the arrow from sticking to the block
                ci.cancel();

                // Play bounce sound
                arrow.getWorld().playSound(null, arrow.getBlockPos(),
                        SoundEvents.ENTITY_SLIME_SQUISH, SoundCategory.NEUTRAL, 0.5f, 1.0f);
            }
        }
    }

    private Vec3d calculateBounceVelocity(Vec3d velocity, Direction hitSide) {
        double x = velocity.x;
        double y = velocity.y;
        double z = velocity.z;

        // Reflect velocity based on the surface normal
        switch (hitSide) {
            case UP, DOWN -> y = -y;
            case NORTH, SOUTH -> z = -z;
            case EAST, WEST -> x = -x;
        }

        // Add slight upward bias to prevent arrows from getting stuck bouncing horizontally
        if (hitSide == Direction.UP) {
            y = Math.abs(y) + 0.1;
        }

        return new Vec3d(x, y, z);
    }
}
