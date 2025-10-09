package com.elementalconvergence.mixin;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.elementalconvergence.magic.convergencehandlers.ElectricityMagicHandler.ELECTRICITY_INDEX;

@Mixin(LightningEntity.class)
public class LightningEntityMixin {

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LightningEntity;powerLightningRod()V"))
    private void preventElectricPlayerDamage(CallbackInfo ci) {
        LightningEntity lightning = (LightningEntity) (Object) this;

        //get all entities
        for (Entity entity : lightning.getWorld().getOtherEntities(lightning, lightning.getBoundingBox().expand(3.0, 6.0, 3.0))) {
            if (entity instanceof ServerPlayerEntity player) {
                IMagicDataSaver dataSaver = (IMagicDataSaver) player;
                MagicData magicData = dataSaver.getMagicData();
                int selectedMagic = magicData.getSelectedMagic();

                // immune if electric
                if (selectedMagic == ELECTRICITY_INDEX) {
                    //cancel fire as well
                    player.setFireTicks(0);
                }
            }
        }
    }
}