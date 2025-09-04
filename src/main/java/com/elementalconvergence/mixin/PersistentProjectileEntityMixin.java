package com.elementalconvergence.mixin;

import com.elementalconvergence.enchantment.ModEnchantments;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin {

    @Shadow protected boolean inGround;
    @Shadow public abstract void writeCustomDataToNbt(NbtCompound nbt);
    @Shadow public abstract void readCustomDataFromNbt(NbtCompound nbt);

    @Unique
    private static final String BOUNCE_COUNT_KEY = "elementalconvergence:bounce_count";
    @Unique
    private static final String MAX_BOUNCES_KEY = "elementalconvergence:max_bounces";
    @Unique
    private static final String HAS_BOUNCY_KEY = "elementalconvergence:has_bouncy";

    // Store bounce data in NBT when writing custom data
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeBounceData(NbtCompound nbt, CallbackInfo ci) {
        // NBT data is automatically handled by the shadow methods
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readBounceData(NbtCompound nbt, CallbackInfo ci) {
        // NBT data is automatically handled by the shadow methods
    }

    // Handle bouncing when hitting blocks
    @Inject(method = "onBlockHit", at = @At("HEAD"), cancellable = true)
    private void handleBounce(BlockHitResult blockHitResult, CallbackInfo ci) {
        PersistentProjectileEntity arrow = (PersistentProjectileEntity) (Object) this;

        // Don't bounce if already in ground
        if (this.inGround) {
            return;
        }

        // Get current NBT data
        NbtCompound nbt = new NbtCompound();
        arrow.writeCustomDataToNbt(nbt);

        // Check if this arrow has bouncing capability or try to initialize it
        if (!nbt.getBoolean(HAS_BOUNCY_KEY) && !nbt.contains(MAX_BOUNCES_KEY)) {
            // Try to initialize bouncy data by checking if we can find enchantment data
            if (arrow.getWorld() instanceof ServerWorld serverWorld) {
                // Look for weapon data in NBT
                if (nbt.contains("weapon", 10)) {
                    NbtCompound weaponNbt = nbt.getCompound("weapon");
                    ItemStack weapon = ItemStack.fromNbt(serverWorld.getRegistryManager(), weaponNbt).orElse(ItemStack.EMPTY);

                    if (!weapon.isEmpty()) {
                        int bouncingLevel = EnchantmentHelper.getLevel(
                                serverWorld.getRegistryManager()
                                        .getWrapperOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                                        .getOrThrow(ModEnchantments.BOUNCY_ARROW),
                                weapon
                        );

                        if (bouncingLevel > 0) {
                            nbt.putBoolean(HAS_BOUNCY_KEY, true);
                            nbt.putInt(BOUNCE_COUNT_KEY, 0);
                            nbt.putInt(MAX_BOUNCES_KEY, bouncingLevel);
                            arrow.readCustomDataFromNbt(nbt);
                        }
                    }
                }
            }
        }

        // Now check if we can bounce
        if (nbt.getBoolean(HAS_BOUNCY_KEY) || nbt.contains(MAX_BOUNCES_KEY)) {
            int currentBounces = nbt.getInt(BOUNCE_COUNT_KEY);
            int maxBounces = nbt.getInt(MAX_BOUNCES_KEY);

            // If we haven't reached max bounces yet
            if (currentBounces < maxBounces) {
                // Cancel normal block hit behavior (prevents sticking to block)
                ci.cancel();

                // Calculate bounce direction
                Vec3d velocity = arrow.getVelocity();
                Vector3f normalF = blockHitResult.getSide().getUnitVector();
                Vec3d normal = new Vec3d(normalF.x, normalF.y, normalF.z);

                // Reflect velocity using the formula: v' = v - 2(v·n)n
                double dotProduct = velocity.dotProduct(normal);
                Vec3d reflectedVelocity = velocity.subtract(normal.multiply(2 * dotProduct));

                // Apply dampening (arrow loses some energy each bounce)
                double damping = 0.8 - (currentBounces * 0.1); // Gets weaker with each bounce
                reflectedVelocity = reflectedVelocity.multiply(Math.max(damping, 0.3));

                // Set new velocity and mark as modified
                arrow.setVelocity(reflectedVelocity);
                arrow.velocityModified = true;

                // Update bounce count
                nbt.putInt(BOUNCE_COUNT_KEY, currentBounces + 1);
                arrow.readCustomDataFromNbt(nbt);

                // Ensure arrow is not considered to be in ground
                this.inGround = false;

                // Play bounce sound effect
                if (arrow.getWorld() instanceof ServerWorld serverWorld) {
                    serverWorld.playSound(null, arrow.getBlockPos(),
                            SoundEvents.BLOCK_SLIME_BLOCK_STEP,
                            SoundCategory.NEUTRAL,
                            0.6f,
                            1.0f + (currentBounces * 0.2f) + arrow.getRandom().nextFloat() * 0.2f);
                }

                return;
            }
        }

        // If no bounce capability or max bounces reached, allow normal behavior
    }
}