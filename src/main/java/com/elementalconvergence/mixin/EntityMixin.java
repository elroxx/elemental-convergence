package com.elementalconvergence.mixin;

import com.elementalconvergence.effect.ModEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(at = @At("HEAD"), method = "isSubmergedIn", cancellable = true)
    private void isSubmergedIn(TagKey<Fluid> tag, CallbackInfoReturnable<Boolean> cir) {
        // Only check water
        if (tag != FluidTags.WATER) {
            return;
        }

        Entity entity = (Entity)(Object)this;

        // only check for entities with status effects
        if (entity instanceof LivingEntity livingEntity) {
            if (livingEntity.hasStatusEffect(ModEffects.DROWNING)) {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }
    }

    @Inject(method = "isInsideWall", at = @At("HEAD"), cancellable = true)
    private void preventQuantumPhasingSuffocation(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;

        if (entity instanceof LivingEntity livingEntity &&
                livingEntity.hasStatusEffect(ModEffects.QUANTUM_PHASING)) {
            //if quantum phasing, they arent inside wall.
            cir.setReturnValue(false);
        }
    }

}
