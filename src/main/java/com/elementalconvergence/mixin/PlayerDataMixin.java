package com.elementalconvergence.mixin;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.data.*;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.effect.MysticalEffect;
import com.elementalconvergence.magic.handlers.EarthMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSource;
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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerDataMixin implements IMagicDataSaver, IPlayerMiningMixin, ISchrodingerTPDataSaver {
    private float miningSpeedMultiplier = 1.0f;

    //START OF STUFF FOR MAGIC
    @Unique
    private final MagicData magicData = new MagicData();

    @Override
    public MagicData getMagicData() {
        return magicData;
    }

    //START OF STUFF FOR TELEPORT
    @Unique
    private final SchrodingerTPData teleportData = new SchrodingerTPData();

    @Override
    public SchrodingerTPData getTeleportData() {
        return teleportData;
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo info) {
        NbtCompound magicNbt = new NbtCompound();
        magicData.writeNbt(magicNbt);
        nbt.put("magic_data", magicNbt);

        //quantum tp stuff as well
        NbtCompound teleportNbt = new NbtCompound();
        teleportData.writeNbt(teleportNbt);
        nbt.put("teleport_data", teleportNbt);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo info) {
        if (nbt.contains("magic_data")) {
            magicData.readNbt(nbt.getCompound("magic_data"));
        }

        //quantum tp read
        if (nbt.contains("teleport_data")) {
            teleportData.readNbt(nbt.getCompound("teleport_data"));
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
    }

    //for double xp in mystic
    @ModifyVariable(method = "addExperience", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int doubleExperienceWithEffect(int experience) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        // check if mystic
        if (player.hasStatusEffect(ModEffects.MYSTICAL_TOUCH)) {
            return experience * 4; // double xp if they were (i decided times 4 coz the font is AWFUL)yuiytuuyuuioop
        }

        return experience;
    }

    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void fallDamageBouncy(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir){
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player.hasStatusEffect(ModEffects.BOUNCY) && !player.isSneaking()){
            cir.setReturnValue(false);
        }
    }

}