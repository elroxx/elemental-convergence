package com.elementalconvergence.mixin;

import com.elementalconvergence.effect.ModEffects;
import gravity_changer.api.GravityChangerAPI;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "onStatusEffectRemoved", at = @At("HEAD"))
    private void onStatusEffectEnd(StatusEffectInstance effect, CallbackInfo ci) {
        RegistryEntry<StatusEffect> statusEffect = effect.getEffectType();

        //Just to check if its the end of gravity instability
        if (statusEffect.equals(ModEffects.GRAVITY_INSTABILITY)) {
            LivingEntity entity = (LivingEntity) (Object) this;
            //Put gravity back to normal in those case
            GravityChangerAPI.setBaseGravityDirection(entity, Direction.DOWN);
        }
    }
}
