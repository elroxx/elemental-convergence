package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class LightMagicHandler implements IMagicHandler {
    public static final int LIGHT_INDEX=5;

    public static final int GLOW_DEFAULT_COOLDOWN = 10;
    public static final int GLOW_DEFAULT_DURATION = 20*60*2; //2 minute glowing
    private int glowCooldown = 0;

    @Override
    public void handleItemRightClick(PlayerEntity player) {

    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        //Not a living entity
        if (!(targetEntity instanceof LivingEntity)){
            return;
        }

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int lightLevel = magicData.getMagicLevel(LIGHT_INDEX);

        if (lightLevel>=3 && (mainHand.isOf(Items.GLOW_INK_SAC) ||  offHand.isOf(Items.GLOW_INK_SAC))&& glowCooldown==0){
            //cooldown
            glowCooldown=GLOW_DEFAULT_COOLDOWN;

            //consume an item
            if (!player.getAbilities().creativeMode) {
                if (mainHand.isOf(Items.GLOW_INK_SAC)) {
                    mainHand.decrement(1);
                }
                else{
                    offHand.decrement(1);
                }
            }

            //Actual gravity swap
            ((LivingEntity) targetEntity).addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, GLOW_DEFAULT_DURATION, 0, false, true, true));

            //playsound
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_GLOW_INK_SAC_USE, SoundCategory.PLAYERS, 1.0F, 0.7F);

            ((ServerWorld)player.getWorld()).spawnParticles(
                    ParticleTypes.END_ROD,
                    targetEntity.getX() + 0.5,
                    targetEntity.getY() + 1.5,
                    targetEntity.getZ() + 0.5,
                    10,
                    0.25,
                    0.25, //so that they rise a little
                    0.25,
                    0);

        }

    }

    @Override
    public void handlePassive(PlayerEntity player) {
        //BUFF
        if (!player.hasStatusEffect(ModEffects.LIGHT_PHASING)){
            player.addStatusEffect(new StatusEffectInstance(ModEffects.LIGHT_PHASING, -1, 0, false, false, false));
        }
        //DEBUFF

        if (player.getWorld().getLightLevel(player.getBlockPos())<5 && !hasNearbyGlowingEntity(player)){
            if (!player.hasStatusEffect(ModEffects.FULL_BLINDNESS)){
                player.addStatusEffect(new StatusEffectInstance(ModEffects.FULL_BLINDNESS, -1, 0, false, false, false));
            }
        }
        else{
            if (player.hasStatusEffect(ModEffects.FULL_BLINDNESS)){
                player.removeStatusEffect(ModEffects.FULL_BLINDNESS);
            }
        }

        //cooldown handling
        if (glowCooldown>0){
            glowCooldown--;
        }
    }

    @Override
    public void handleAttack(PlayerEntity player, Entity victim) {

    }

    @Override
    public void handleKill(PlayerEntity player, Entity victim) {

    }

    @Override
    public void handleMine(PlayerEntity player) {

    }

    @Override
    public void handleBlockBreak(PlayerEntity player, BlockPos pos, BlockState state, BlockEntity entity) {

    }

    @Override
    public void handlePrimarySpell(PlayerEntity player) {

    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

    private boolean hasNearbyGlowingEntity(PlayerEntity player) {
        if (player.getWorld() == null) return false;
        //System.out.println("got checking at least");

        double radius=10.0;
        // get all living entities close
        List<LivingEntity> nearbyEntities = player.getWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(radius),
                nearbyEntity -> nearbyEntity != player // exclude current player
        );
        if (!nearbyEntities.isEmpty()) {
            System.out.println(nearbyEntities.get(0));
            System.out.println(nearbyEntities.get(0).hasStatusEffect(StatusEffects.GLOWING));
        }

        // nearby entities with glowing
        return nearbyEntities.stream()
                .anyMatch(nearbyEntity -> nearbyEntity.hasStatusEffect(StatusEffects.GLOWING));
    }
}
