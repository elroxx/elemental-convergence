package com.elementalconvergence.mixin;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.data.IMagicDataSaver;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerDataMixin {
    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void copyMagicData(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        IMagicDataSaver newPlayer = (IMagicDataSaver) this;
        IMagicDataSaver oldDataSaver = (IMagicDataSaver) oldPlayer;

        // Copy each magic level
        for (int i = 0; i < ElementalConvergence.BASE_MAGIC_ID.length; i++) {
            newPlayer.getMagicData().setMagicLevel(i, oldDataSaver.getMagicData().getMagicLevel(i));
        }
    }
}