package com.elementalconvergence.mixin;

import com.elementalconvergence.effect.ModEffects;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class CameraMixin {

    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    private void preventCameraClipping(float desiredCameraDistance, CallbackInfoReturnable<Float> cir) {
        Camera camera = (Camera) (Object) this;
        Entity entity = camera.getFocusedEntity();

        if (entity instanceof LivingEntity livingEntity &&
                livingEntity.hasStatusEffect(ModEffects.QUANTUM_PHASING)) {
            // return full distance so no camera clipping in F5
            cir.setReturnValue(desiredCameraDistance);
        }
    }
}
