package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.magic.IMagicHandler;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class ShadowMagicHandler implements IMagicHandler {
    public static final int SHADOW_INDEX=4;
    private static final int LIGHT_THRESHOLD=7;
    private static final int DEFAULT_INVIS_COOLDOWN=60;
    private int invisCooldown=0;
    private static final int DEFAULT_LIGHT_UPDATE_COOLDOWN=10;
    private int lightUpdateCooldown=0;
    private static final int INVIS_DURATION=219;

    //To make it such that mobs can't target you if invisible
    public ShadowMagicHandler() {
        registerEntityTargetingEvents();
    }

    private void registerEntityTargetingEvents() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof LivingEntity livingEntity && livingEntity.hasStatusEffect(StatusEffects.INVISIBILITY)) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }

    @Override
    public void handleRightClick(PlayerEntity player) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {
        //Checking light level of blocks
        if (lightUpdateCooldown==0){
            lightUpdateCooldown=DEFAULT_LIGHT_UPDATE_COOLDOWN;

            BlockPos playerPosition = player.getBlockPos();
            int lightLevel = player.getWorld().getLightLevel(playerPosition);
            if (lightLevel<=LIGHT_THRESHOLD){
                if (invisCooldown==0) {
                    invisCooldown = DEFAULT_INVIS_COOLDOWN;
                    StatusEffectInstance invisibility = new StatusEffectInstance(StatusEffects.INVISIBILITY,
                            INVIS_DURATION, //DURATON
                            0, //AMPLIFIER
                            true,
                            false,
                            false
                    );
                    player.addStatusEffect(invisibility);
                    player.setInvisible(true);
                }
                if (player.getMaxHealth() != 20.0F) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0F);
                }

            }
            else if (lightLevel<=LIGHT_THRESHOLD+3){
                player.setInvisible(false);
                player.removeStatusEffect(StatusEffects.INVISIBILITY);
                invisCooldown=0; //So that the invisibility is automatically reapplied next time we enter the darkness

                if (player.getMaxHealth() != 20.0F) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0F);
                }
            }
            else{
                player.setInvisible(false);
                player.removeStatusEffect(StatusEffects.INVISIBILITY);
                invisCooldown=0; //So that the invisibility is automatically reapplied next time we enter the darkness

                if (player.getMaxHealth() != 10.0F) {
                    player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(10.0F);
                }
            }
        }

        //COOLDOWN UPDATES:
        if (lightUpdateCooldown>0){
            lightUpdateCooldown--;
        }
        if (invisCooldown>0){
            invisCooldown--;
        }
    }

    @Override
    public void handleAttack(PlayerEntity player, Entity victim) {

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
}
