package com.elementalconvergence.mixin;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.IPlayerMiningMixin;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.magic.handlers.EarthMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerDataMixin implements IMagicDataSaver, IPlayerMiningMixin {
    private float miningSpeedMultiplier = 1.0f;

    //START OF STUFF FOR MAGIC
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


    //END OF STUFF FOR MAGIC

    //MININGSPEED
    @Inject(method= "getBlockBreakingSpeed", at = @At("RETURN"), cancellable = true)
    private void changeMiningSpeed(BlockState block, CallbackInfoReturnable<Float> cir){
        float originalSpeed = cir.getReturnValue();
        float decimalPart = miningSpeedMultiplier - (int) miningSpeedMultiplier;

        if (Math.abs(decimalPart - 0.5f) < 0.0001f) {
            cir.setReturnValue(originalSpeed);
        }  else {
            cir.setReturnValue(originalSpeed * miningSpeedMultiplier);
        }



    }

/*    @Inject(method = "canHarvest", at = @At("RETURN"), cancellable = true)
    private void modifyHarvestLevel(BlockState block, CallbackInfoReturnable<Boolean> cir) {
        boolean original = cir.getReturnValue();
        float decimalPart = miningSpeedMultiplier - (int) miningSpeedMultiplier;

        if (Math.abs(decimalPart - 0.5f) < 0.0001f) {
            cir.setReturnValue(original);
        } else {

            if (block.isIn(BlockTags.NEEDS_DIAMOND_TOOL)) {
                cir.setReturnValue(miningSpeedMultiplier >= EarthMagicHandler.DIAMOND_PICKAXE_MULTIPLIER);
            } else if (block.isIn(BlockTags.NEEDS_IRON_TOOL)) {
                cir.setReturnValue(miningSpeedMultiplier >= EarthMagicHandler.IRON_PICKAXE_MULTIPLIER);
            } else if (block.isIn(BlockTags.NEEDS_STONE_TOOL)) {
                cir.setReturnValue(miningSpeedMultiplier >= EarthMagicHandler.STONE_PICKAXE_MULTIPLIER);
            } else if (block.isIn(BlockTags.PICKAXE_MINEABLE)) {
                cir.setReturnValue(miningSpeedMultiplier >= EarthMagicHandler.WOODEN_PICKAXE_MULTIPLIER);
            } else {
                cir.setReturnValue(original);
            }

        }
    }

 */

    public void setMiningSpeedMultiplier(float miningSpeedMultiplier){
        this.miningSpeedMultiplier=miningSpeedMultiplier;
    }

    public float getMiningSpeedMultiplier(){
        return this.miningSpeedMultiplier;
    }


    //For flight?
    @Inject(method = "checkFallFlying", at = @At("HEAD"), cancellable = true)
    private void allowElytraFlightWithEffect(CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        if (!player.isOnGround() && !player.isFallFlying() && !player.isTouchingWater() && !player.hasStatusEffect(StatusEffects.LEVITATION)) {
            ItemStack itemStack = player.getEquippedStack(EquipmentSlot.CHEST);
            if ((itemStack.isOf(Items.ELYTRA) && ElytraItem.isUsable(itemStack)) || player.hasStatusEffect(ModEffects.WINGS)) {
                player.startFallFlying();
                cir.setReturnValue(true);
            }
        }

        cir.setReturnValue(false);
    }

}