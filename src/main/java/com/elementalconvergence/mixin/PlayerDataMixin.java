package com.elementalconvergence.mixin;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerDataMixin implements IMagicDataSaver {
    @Unique
    private final MagicData magicData = new MagicData();

    @Override
    public MagicData getMagicData() {
        return magicData;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo info) {
        NbtCompound magicNbt = new NbtCompound();
        magicData.writeNbt(magicNbt);
        nbt.put("magic_data", magicNbt);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo info) {
        if (nbt.contains("magic_data")) {
            magicData.readNbt(nbt.getCompound("magic_data"));
        }
    }

}