package com.elementalconvergence.mixin;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.ISchrodingerTPDataSaver;
import com.elementalconvergence.data.SchrodingerTPData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerDataMixin {
    //THIS IS ON RESPAWN. WE NEED TO COPY FROM THE OLDPLAYER INTO THE NEW PLAYER BECAUSE EVERY DEATH STARTS A NEW PLAYER ENTITY FOR SOME REASON
    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void copyMagicData(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        IMagicDataSaver newPlayer = (IMagicDataSaver) this;
        IMagicDataSaver oldDataSaver = (IMagicDataSaver) oldPlayer;


        // Copy each magic level
        for (int i = 0; i < ElementalConvergence.FULL_MAGIC_ID.length; i++) {
            newPlayer.getMagicData().setMagicLevel(i, oldDataSaver.getMagicData().getMagicLevel(i));
        }

        // Copy selected magic
        newPlayer.getMagicData().setSelectedMagic(oldDataSaver.getMagicData().getSelectedMagic());

        SchrodingerTPData newTPData = ((ISchrodingerTPDataSaver) this).getTeleportData();
        SchrodingerTPData oldTPData = ((ISchrodingerTPDataSaver) oldPlayer).getTeleportData();

        //ALSO COPY QUANTUM CLONE POSITION
        newTPData.setSavedX(oldTPData.getSavedX());
        newTPData.setSavedY(oldTPData.getSavedY());
        newTPData.setSavedZ(oldTPData.getSavedZ());

        newTPData.setSavedVelocityX(oldTPData.getSavedVelocityX());
        newTPData.setSavedVelocityY(oldTPData.getSavedVelocityY());
        newTPData.setSavedVelocityZ(oldTPData.getSavedVelocityZ());

        newTPData.setSavedYaw(oldTPData.getSavedYaw());
        newTPData.setSavedPitch(oldTPData.getSavedPitch());

        newTPData.setSavedDimension(oldTPData.getSavedDimension());

        newTPData.setHasSavedPosition(oldTPData.hasSavedPosition());
    }
}