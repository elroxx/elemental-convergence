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

    @Unique
    private static final String BOUNCE_COUNT_KEY = "elementalconvergence:bounce_count";
    @Unique
    private static final String MAX_BOUNCES_KEY = "elementalconvergence:max_bounces";
    @Unique
    private static final String HAS_BOUNCY_KEY = "elementalconvergence:has_bouncy";

    @Unique
    private int bounceCount = -1; // -1 means not initialized
    @Unique
    private int maxBounces = 0;
    @Unique
    private boolean bouncyInitialized = false;

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeBounceData(NbtCompound nbt, CallbackInfo ci) {
        if (bounceCount >= 0) {
            nbt.putInt(BOUNCE_COUNT_KEY, bounceCount);
            nbt.putInt(MAX_BOUNCES_KEY, maxBounces);
            nbt.putBoolean(HAS_BOUNCY_KEY, true);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readBounceData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(BOUNCE_COUNT_KEY)) {
            bounceCount = nbt.getInt(BOUNCE_COUNT_KEY);
            maxBounces = nbt.getInt(MAX_BOUNCES_KEY);
            bouncyInitialized = true;
        }
    }

    // init bounce on first tick
    @Inject(method = "tick", at = @At("HEAD"))
    private void initializeBounceData(CallbackInfo ci) {
        if (!bouncyInitialized && bounceCount == -1) {
            PersistentProjectileEntity arrow = (PersistentProjectileEntity) (Object) this;

            if (arrow.getWorld() instanceof ServerWorld serverWorld) {

                NbtCompound nbt = new NbtCompound();
                arrow.writeCustomDataToNbt(nbt);

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
                            bounceCount = 0;
                            maxBounces = bouncingLevel;
                            bouncyInitialized = true;
                            System.out.println("Bouncy Arrow Initialized: Max bounces = " + maxBounces);
                        } else {

                            bounceCount = -2; // no bounces
                            bouncyInitialized = true;
                        }
                    }
                } else {
                    // no obunces if no wep data
                    bounceCount = -2;
                    bouncyInitialized = true;
                }
            }
        }
    }

    // handle bouncing
    @Inject(method = "onBlockHit", at = @At("HEAD"), cancellable = true)
    private void handleBounce(BlockHitResult blockHitResult, CallbackInfo ci) {
        PersistentProjectileEntity arrow = (PersistentProjectileEntity) (Object) this;

        // dont boucne if in ground
        if (this.inGround) {
            return;
        }

        // bounce
        if (bounceCount >= 0 && bounceCount < maxBounces) {
            System.out.println("Bouncing! Current: " + bounceCount + " / Max: " + maxBounces);

            ci.cancel();

            Vec3d velocity = arrow.getVelocity();
            Vector3f normalF = blockHitResult.getSide().getUnitVector();
            Vec3d normal = new Vec3d(normalF.x, normalF.y, normalF.z);

            double dotProduct = velocity.dotProduct(normal);
            Vec3d reflectedVelocity = velocity.subtract(normal.multiply(2 * dotProduct));

            // dampening
            double damping = 0.8 - (bounceCount * 0.1);
            reflectedVelocity = reflectedVelocity.multiply(Math.max(damping, 0.3));

            //set new velocity
            arrow.setVelocity(reflectedVelocity);
            arrow.velocityModified = true;

            //add bounce count
            bounceCount++;
            System.out.println("Bounce count now: " + bounceCount);

            this.inGround = false;

            // bounce effect
            if (arrow.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.playSound(null, arrow.getBlockPos(),
                        SoundEvents.BLOCK_SLIME_BLOCK_STEP,
                        SoundCategory.NEUTRAL,
                        0.6f,
                        1.0f + (bounceCount * 0.2f) + arrow.getRandom().nextFloat() * 0.2f);
            }

            return;
        } else if (bounceCount >= maxBounces) {
            System.out.println("Max bounces reached! Arrow will stick. (" + bounceCount + " >= " + maxBounces + ")");
        }
    }
}