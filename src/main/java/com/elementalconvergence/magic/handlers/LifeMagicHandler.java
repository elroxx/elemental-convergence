package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.entity.ModEntities;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class LifeMagicHandler implements IMagicHandler {
    //LIFE ID=6
    public static final float REGEN_RADIUS = 15.0f;
    public static final float GROWTH_RADIUS = 5.0f;
    public static final int REGEN_DEFAULT_COOLDOWN=50;
    public static final int GROWTH_DEFAULT_COOLDOWN=65;


    private int regenCooldown=0;
    private int growthCooldown=0;

    private static final Random random = new Random();
    private static final double PARTICLE_THRESHOLD =0.08;

    private boolean growthAuraToggle=false;

    @Override
    public void handleRightClick(PlayerEntity player) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {
        if (regenCooldown==0){
            Box regenBox = new Box(
                    player.getX()-REGEN_RADIUS,
                    player.getY()-REGEN_RADIUS,
                    player.getZ()-REGEN_RADIUS,
                    player.getX()+REGEN_RADIUS,
                    player.getY()+REGEN_RADIUS,
                    player.getZ()+REGEN_RADIUS
                    );
            List<PlayerEntity> nearbyPlayersList = player.getWorld().getEntitiesByClass(PlayerEntity.class, regenBox, target -> {
                return target.squaredDistanceTo(player) <= REGEN_RADIUS * REGEN_RADIUS;
            });

            for (PlayerEntity target : nearbyPlayersList){
                target.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.REGENERATION,
                        200,
                        0,
                        false,
                        true,
                        true
                ));
            }
            regenCooldown=REGEN_DEFAULT_COOLDOWN;
        }

        if (growthCooldown==0 && growthAuraToggle){
            applyGrowthAura(player);
            growthCooldown=GROWTH_DEFAULT_COOLDOWN;
        }

        //Cooldown management
        if (regenCooldown>0){
            regenCooldown--;
        }
        if (growthCooldown>0){
            growthCooldown--;
        }
    }

    @Override
    public void handleAttack(PlayerEntity player, Entity victim) {
    }

    @Override
    public void handleKill(PlayerEntity player, Entity victim) {
        if (!victim.isAlive()){
            boolean isUndead = victim.getType().isIn(EntityTypeTags.UNDEAD);
            boolean isZombie = victim.getType().isIn(EntityTypeTags.ZOMBIES);
            boolean isSkeleton = victim.getType().isIn(EntityTypeTags.SKELETONS);
            boolean isInanimate = !victim.isLiving();
            if (!(isUndead || isInanimate || isZombie || isSkeleton)){
                System.out.println();
                player.kill();
                player.getWorld().playSound(
                        null, //So that everybody hears it
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        ModEntities.DEATH_SOUND_EVENT,
                        SoundCategory.MASTER,
                        1.0f,
                        1.0f
                );
            }
        }
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
        int lifeLevel = magicData.getMagicLevel(0);
        if (lifeLevel>=1) {
            growthAuraToggle=!growthAuraToggle;
            player.sendMessage(Text.of("Growth Aura: " + growthAuraToggle));
        }
    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

    public static void applyGrowthAura(PlayerEntity player){

        World world = player.getWorld();
        BlockPos playerPos = player.getBlockPos();

        int blockRadius = (int) Math.ceil(GROWTH_RADIUS);

        for (int i = -blockRadius; i <= blockRadius; i++){
            for (int j = -blockRadius; j <= blockRadius; j++){
                for (int k = -blockRadius; k <= blockRadius; k++){
                    BlockPos current = playerPos.add(i,j,k);

                    if (playerPos.getSquaredDistance(current.getX(), current.getY(), current.getZ()) <= GROWTH_RADIUS*GROWTH_RADIUS){
                        tryGrowBlock(world, current);


                        if (random.nextFloat()<PARTICLE_THRESHOLD){
                            spawnGrowthParticle(world, current);
                        }
                    }
                }
            }
        }

    }

    public static void tryGrowBlock(World world, BlockPos pos){
        if (world.getBlockState(pos).getBlock() instanceof Fertilizable fertilizable){
            if (fertilizable.isFertilizable(world, pos, world.getBlockState(pos))){
                if (fertilizable.canGrow(world, world.random, pos, world.getBlockState(pos))){
                    fertilizable.grow((ServerWorld)world, world.random, pos, world.getBlockState(pos));
                }
            }
        }
    }

    private static void spawnGrowthParticle(World world, BlockPos pos){
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + random.nextDouble();
        double z = pos.getZ() + random.nextDouble();

        if (world instanceof ServerWorld){
            ((ServerWorld) world).spawnParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    x,
                    y,
                    z,
                    1,
                    0,
                    0,
                    0,
                    0
            );
        }
    }

}
