package com.elementalconvergence.mixin;

import com.elementalconvergence.data.IGrapplingHookDataSaver;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.entity.LashingPotatoHookEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerSpiderMovementMixin {

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void handleGrapplingHookMovement(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        IGrapplingHookDataSaver hookDataSaver = (IGrapplingHookDataSaver) player;
        LashingPotatoHookEntity hook = hookDataSaver.getGrapplingHookData().getGrapplingHookEntity();

        if (hook != null && hook.isInBlock()) {
            player.onLanding();
            if (player.isLogicalSideForUpdatingMovement()) {
                Vec3d hookPos = hook.getPos().subtract(player.getEyePos());
                float maxLength = hook.getLength();
                double distance = hookPos.length();

                if (distance > (double)maxLength) {
                    double pullStrength = distance / (double)maxLength * 0.1;
                    player.addVelocity(hookPos.multiply((double)1.0F / distance).multiply(pullStrength, pullStrength * 1.1, pullStrength));
                }
            }
        }
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void applyGrapplingAirResistance(Vec3d movementInput, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        IGrapplingHookDataSaver hookDataSaver = (IGrapplingHookDataSaver) player;
        LashingPotatoHookEntity hook = hookDataSaver.getGrapplingHookData().getGrapplingHookEntity();

        if (hook != null && hook.isInBlock() && !player.isOnGround()) {
            // Apply air resistance when grappling
            Vec3d velocity = player.getVelocity();
            player.setVelocity(velocity.x * 0.99, velocity.y * 0.995, velocity.z * 0.99);
        }
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void onTickMovement(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;

        if (player.hasStatusEffect(ModEffects.ARACHNID)) {
            if (player.horizontalCollision && !player.isFallFlying()) {
                Vec3d velocity = player.getVelocity();

                double slideSpeed = -0.05;
                double climbSpeed = 0.2;

                if (player.isSneaking()) {
                    velocity = new Vec3d(velocity.x, 0.0, velocity.z); //stick
                    player.fallDistance = 0.0F;
                } else if (player.forwardSpeed > 0.0F) {
                    velocity = new Vec3d(velocity.x, climbSpeed, velocity.z); //climb
                } /*else if (velocity.y < slideSpeed) {
                    velocity = new Vec3d(velocity.x, slideSpeed, velocity.z); //slide down
                    player.fallDistance = 0.0F;
                }*/

                player.setVelocity(velocity);
                player.velocityModified=true;
                player.fallDistance = 0.0F;
            }
        }
    }
}
