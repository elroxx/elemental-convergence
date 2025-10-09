package com.elementalconvergence.mixin;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.data.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.elementalconvergence.magic.convergencehandlers.ElectricityMagicHandler.ELECTRICITY_INDEX;
import static com.elementalconvergence.magic.convergencehandlers.VoidMagicHandler.VOID_INDEX;

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

        //also copy original skin data
        OriginalSkinData newSkinData = ((IOriginalSkinDataSaver) this).getOriginalSkinData();
        OriginalSkinData oldSkinData = ((IOriginalSkinDataSaver) oldPlayer).getOriginalSkinData();

        newSkinData.setOriginalSkinValue(oldSkinData.getOriginalSkinValue());
        newSkinData.setOriginalSkinSignature(oldSkinData.getOriginalSkinSignature());
        newSkinData.setHasFetchedOnce(oldSkinData.hasFetchedOnce());


        //ADD VOID INVENTORY IF IT IS BACKED UP!

        //verify if keep inventory is not already on, because then I modify stuff so i dont want to touch it
        if (!oldPlayer.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            PlayerDataMixin oldMixin = (PlayerDataMixin) (Object) oldPlayer;
            if (oldMixin.getVoidInventoryBackup() != null) {
                ServerPlayerEntity newPlayerEntity = (ServerPlayerEntity) (Object) this;
                if (newPlayer.getMagicData().getSelectedMagic() == VOID_INDEX) {

                    //restore inv
                    DefaultedList<ItemStack> backup = oldMixin.getVoidInventoryBackup();
                    for (int i = 0; i < backup.size() && i < newPlayerEntity.getInventory().size(); i++) {
                        newPlayerEntity.getInventory().setStack(i, backup.get(i).copy());
                    }

                    //clear backup
                    oldMixin.clearVoidBackup();
                }
            }
        }
    }

    //cancel dmg here for lightning as well
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void preventLightningDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        //check fi lightning damage
        if (source.isOf(DamageTypes.LIGHTNING_BOLT)) {
            IMagicDataSaver dataSaver = (IMagicDataSaver) player;
            MagicData magicData = dataSaver.getMagicData();
            int selectedMagic = magicData.getSelectedMagic();

            //cancel lightning damage
            if (selectedMagic == ELECTRICITY_INDEX) {
                cir.setReturnValue(false);
            }
        }
    }
}