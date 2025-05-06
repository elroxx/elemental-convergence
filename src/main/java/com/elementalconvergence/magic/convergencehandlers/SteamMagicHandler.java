package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.*;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class SteamMagicHandler implements IMagicHandler {
    public static final int STEAM_INDEX= (BASE_MAGIC_ID.length-1)+3; //this is 10

    private Vec3d lastPosition =null;
    private int horizontalBlocksMoved = 0;
    public int HORIZONTAL_BLOCK_COUNTER_DEFAULT=10;

    public static final int FLOAT_PARTICLE_DEFAULT_COOLDOWN=10; //1/2 seconds
    private boolean floatToggle = false;
    private int floatParticleCooldown=0;

    public static final float BASE_SCALE = 1.0f;

    public static final float FLOAT_SCALE = 0.1f;
    public static final float FLOAT_HELD = 0f;




    @Override
    public void handleItemRightClick(PlayerEntity player) {
    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {

        //BUFF (invulnerable except to negative valued explosion, the void and /kill)
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)){
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,-1, 9, false, false, false));
        }

        //Debuff (also no natural health regen)
        if (lastPosition==null){
            lastPosition=player.getPos();
        }
        Vec3d currentPos = player.getPos();

        // horizontal distance (no y axis)
        double dx = currentPos.x - lastPosition.x;
        double dz = currentPos.z - lastPosition.z;
        double horizontalDistanceMoved = Math.sqrt(dx * dx + dz * dz);

        //floatToggle is to avoid dealing dmg when in float mode but without giving opportunity to reset block counter
        if (horizontalDistanceMoved >= 1.0 && !floatToggle) {
            // 1hp of dmg
            //player.damage(player.getDamageSources().outOfWorld(), 1.0F);
            this.horizontalBlocksMoved++;
            lastPosition=currentPos;
        }


        //DEALING DMG DEBUFF
        if (horizontalBlocksMoved>=HORIZONTAL_BLOCK_COUNTER_DEFAULT){
            //1hp of dmg
            if (!player.isCreative()) {
                player.damage(player.getDamageSources().outOfWorld(), 1.0F);
            }
            horizontalBlocksMoved=0;
        }

        //LVL 2 particles only
        if (floatToggle && floatParticleCooldown==0){
            int particleCount=10;
            double particleRange=1.0;
            double particleSpread=0.5;
            Vec3d playerPos = player.getPos();

            for (int i = 0; i < particleCount; i++) {
                double distance = player.getRandom().nextDouble() * particleRange;
                double offsetX = (player.getRandom().nextDouble() - 0.5) * 2 * particleSpread * (distance / particleRange);
                double offsetY = (player.getRandom().nextDouble() - 0.5) * 2 * particleSpread * (distance / particleRange);
                double offsetZ = (player.getRandom().nextDouble() - 0.5) * 2 * particleSpread * (distance / particleRange);

                Vec3d particlePos = playerPos.add(
                        offsetX,
                        offsetY-0.5,
                        offsetZ
                );


                ((ServerWorld)player.getWorld()).spawnParticles(
                        ParticleTypes.WHITE_SMOKE,
                        particlePos.x, particlePos.y, particlePos.z,
                        1,
                        0.0, 0.0, 0.0,
                        0.0
                );
            }
            floatParticleCooldown=FLOAT_PARTICLE_DEFAULT_COOLDOWN;
        }


        //COOLDOWNS

        if (floatToggle&& floatParticleCooldown>0){
            floatParticleCooldown--;
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
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int steamLevel = magicData.getMagicLevel(STEAM_INDEX);

        //lvl 2 ability
        if (steamLevel>=2){
            floatToggle=!floatToggle;
        }

        //LVL 2 ability when toggled (floatToggle can only be true if already lvl 2)
        if (floatToggle){

            //ADDING STATUS EFFECTS
            if (player.hasStatusEffect(StatusEffects.LEVITATION) && player.getStatusEffect(StatusEffects.LEVITATION).getAmplifier()<1){
                player.removeStatusEffect(StatusEffects.LEVITATION);
            }
            else if (!player.hasStatusEffect(StatusEffects.LEVITATION)){
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, -1, 1, false, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, -1, 1, false, false, false));
            }

            //SCALE MODIFICATION
            ScaleData playerHeight = ScaleTypes.HEIGHT.getScaleData(player);
            ScaleData playerWidth = ScaleTypes.WIDTH.getScaleData(player);
            ScaleData playerReach = ScaleTypes.BLOCK_REACH.getScaleData(player);
            ScaleData playerEntityReach = ScaleTypes.ENTITY_REACH.getScaleData(player);
            ScaleData playerHeldItem = ScaleTypes.HELD_ITEM.getScaleData(player);

            if (!(Math.abs(playerHeight.getScale()-FLOAT_SCALE)<0.05f)) {
                playerHeight.setScale(FLOAT_SCALE);
                playerWidth.setScale(FLOAT_SCALE);
                playerReach.setScale(FLOAT_HELD);
                playerEntityReach.setScale(FLOAT_HELD);
                playerHeldItem.setScale(FLOAT_HELD);
            }

        }



        else {
            //REMOVING STATUS EFFECTS
            if (player.hasStatusEffect(StatusEffects.LEVITATION) && player.getStatusEffect(StatusEffects.LEVITATION).getAmplifier()>=1){
                player.removeStatusEffect(StatusEffects.LEVITATION);
                player.removeStatusEffect(StatusEffects.INVISIBILITY);
            }

            //SCALE MODIFICATION
            ScaleData playerHeight = ScaleTypes.HEIGHT.getScaleData(player);
            ScaleData playerWidth = ScaleTypes.WIDTH.getScaleData(player);
            ScaleData playerReach = ScaleTypes.BLOCK_REACH.getScaleData(player);
            ScaleData playerEntityReach = ScaleTypes.ENTITY_REACH.getScaleData(player);
            ScaleData playerHeldItem = ScaleTypes.HELD_ITEM.getScaleData(player);

            if (!(Math.abs(playerHeight.getScale()-BASE_SCALE)<0.05f)) {
                playerHeight.setScale(BASE_SCALE);
                playerWidth.setScale(BASE_SCALE);
                playerReach.setScale(BASE_SCALE);
                playerEntityReach.setScale(BASE_SCALE);
                playerHeldItem.setScale(BASE_SCALE);
            }

        }

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5F, 0.8F);
    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }


}
