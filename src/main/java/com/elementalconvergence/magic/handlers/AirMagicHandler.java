package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.magic.IMagicHandler;
import gravity_changer.api.GravityChangerAPI;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class AirMagicHandler implements IMagicHandler {

    public static final int AIR_INDEX=1;

    //For passive
    public static final float AIR_SPEED = 0.2f;
    public static final float AIR_GRAVITY = 0.05f; // 1/20 grav
    public static final float AIR_HEALTH = 8.0f;

    // For lvl1
    public static final int DEFAULT_WINDBALL_COOLDOWN = 10; //0.25 seconds
    private int windballCooldown = 0;

    // For lvl 2
    public static final int AIR_MAX_LEAPS = 3;
    public static final double LEAP_STRENGTH = 0.8; // height
    public static final double LEAP_FORWARD_BOOST = 0.75;
    public static final int DEFAULT_LEAP_COOLDOWN = 5; // 1/4 sec
    public static final int DEFAULT_LEAP_RESET_COOLDOWN = 3; //just to not instant reset the grounded jump

    private int jumpsUsed = 0;
    private int leapCooldown = 0;
    private int leapResetCooldown = 0;



    @Override
    public void handleItemRightClick(PlayerEntity player) {

        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int airLevel = magicData.getMagicLevel(AIR_INDEX);
        if (airLevel>=1) {

            if (windballCooldown == 0) {
                if (mainHand.isOf(Items.FEATHER) || offHand.isOf(Items.FEATHER)) {

                    if (!player.getAbilities().creativeMode) {
                        if (mainHand.isOf(Items.FEATHER) || mainHand.isOf(Items.FEATHER)) {
                            mainHand.decrement(1);
                        } else {
                            offHand.decrement(1);
                        }
                    }
                    for (int i=0; i<2; i++) {
                        double speed = 4.0;
                        Vec3d rotation = player.getRotationVec(1.0F);
                        Vec3d velocity = rotation.multiply(speed);

                        WindChargeEntity windball = new WindChargeEntity(player.getWorld(), player.getX(), player.getY(), player.getZ(), velocity);

                        windball.setVelocity(velocity);

                        windball.setPosition(player.getX() + rotation.x * 1.5, player.getY() + 1.0, player.getZ() + rotation.z * 1.5);

                        player.getWorld().spawnEntity(windball);

                        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.ENTITY_BREEZE_WIND_BURST, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    }

                    windballCooldown=DEFAULT_WINDBALL_COOLDOWN;
                }
            }
        }

    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {

        //Buff (movement speed+ 1/20 gravity) and DEBUFF (less health)
        if (!(Math.abs(player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).getBaseValue()-AIR_SPEED)<0.0005f)){
            player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(AIR_SPEED); //SPEED
            player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(AIR_HEALTH); //HEALTH
        }
        //gravity reduced
        double currentgStrength = GravityChangerAPI.getBaseGravityStrength(player);
        if (Math.abs(currentgStrength-AIR_GRAVITY)>=0.01){
            GravityChangerAPI.setBaseGravityStrength(player, AIR_GRAVITY);
        }



        //reset double jumps
        boolean isCurrentlyOnGround = player.isOnGround() || player.isSubmergedInWater() || player.isClimbing();
        if (isCurrentlyOnGround) {
            if (leapResetCooldown==0) {
                jumpsUsed = 0;
            }
            leapCooldown = 0;
        }

        //Cooldowns

        if (windballCooldown>0){
            windballCooldown--;
        }

        if (leapCooldown>0) {
            leapCooldown--;
        }

        if (leapResetCooldown>0){
            leapResetCooldown--;
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
        int airLevel = magicData.getMagicLevel(AIR_INDEX);

        if (airLevel >= 2 && leapCooldown <= 0) {
            //see if still has leaps
            boolean isOnGround = player.isOnGround() || player.isSubmergedInWater() || player.isClimbing();

            if (!isOnGround && jumpsUsed >= AIR_MAX_LEAPS) {
                return;
            }

            // If not on ground, increment jumps used
            //if (!isOnGround) {
            jumpsUsed++;
            //}
            if (isOnGround){
                leapResetCooldown=DEFAULT_LEAP_RESET_COOLDOWN;
            }

            // compute the leap vector
            Vec3d lookDir = player.getRotationVec(1.0f);
            Vec3d leapVelocityHorizontalPlane = new Vec3d(
                    lookDir.x * LEAP_FORWARD_BOOST,
                    0,
                    lookDir.z * LEAP_FORWARD_BOOST
            );

            // ADDING the horizontal velocity while resetting fully the vertical one
            Vec3d horizontalVelocity = player.getVelocity().add(leapVelocityHorizontalPlane);
            Vec3d finalVelocity = new Vec3d(
                    horizontalVelocity.getX(),
                    LEAP_STRENGTH,
                    horizontalVelocity.getZ()
                    );

            player.setVelocity(finalVelocity);
            player.velocityModified=true; //IMPORTANT COZ IF NOT THIS DOESNT CHANGE THE PLAYERS VELOCITY AT ALL

            // particles
            ServerWorld world = (ServerWorld) player.getWorld();

            Random random = player.getWorld().getRandom();
            for (int i = 0; i < 10; i++) {
                double offsetX = random.nextGaussian() * 0.2;
                double offsetZ = random.nextGaussian() * 0.2;
                world.spawnParticles(
                        ParticleTypes.CLOUD,
                        player.getX() + offsetX, player.getY(), player.getZ() + offsetZ, 1, 0, 0, 0, 0.1
                );
            }

            player.getWorld().playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ENTITY_BREEZE_JUMP,
                    SoundCategory.PLAYERS,
                    0.8F,
                    1.2F
            );

            leapCooldown = DEFAULT_LEAP_COOLDOWN;
        }
    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

}
