package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.block.ModBlocks;
import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.entity.MinionZombieEntity;
import com.elementalconvergence.entity.ModEntities;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

import static com.elementalconvergence.ElementalConvergence.deathList;
import static com.elementalconvergence.ElementalConvergence.deathMap;

public class LifeMagicHandler implements IMagicHandler {
    //LIFE ID=6
    public static final float REGEN_RADIUS = 15.0f;
    public static final float GROWTH_RADIUS = 5.0f;
    public static final int REGEN_DEFAULT_COOLDOWN=50;
    public static final int GROWTH_DEFAULT_COOLDOWN=65;
    public static final int RESURRECTION_DEFAULT_COOLDOWN=20*60*3; //3 minutes
    public static final int GROUP_TP_DEFAULT_COOLDOWN=60;
    //public static final int RESURRECTION_DEFAULT_COOLDOWN=20;

    private int regenCooldown=0;
    private int growthCooldown=0;
    private int resurrectionCooldown=0;
    private int groupTPCooldown=0;

    private static final Random random = new Random();
    private static final double PARTICLE_THRESHOLD =0.08;

    private boolean growthAuraToggle=false;

    private static final Block BLOCK_FOR_GATEWAY= ModBlocks.FLOWER_GATEWAY;
    private static final int GROUP_TP_RADIUS=5;
    private static final int GATEWAY_DETECT_RANGE=100;

    @Override
    public void handleItemRightClick(PlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int lifeLevel = magicData.getMagicLevel(6);
        //lvl 2 ability
        if (lifeLevel>=2) {
            if (groupTPCooldown==0){
                groupTPCooldown=GROUP_TP_DEFAULT_COOLDOWN;
                if (mainHand.getItem() instanceof BlockItem blockItem && isFlower(blockItem.getBlock())){
                        boolean success = checkForGateway(player, player.getWorld(), blockItem.getBlock());
                        if (success){
                            mainHand.decrement(1);
                            player.getWorld().playSound(
                                    null, //So that everybody hears it
                                    player.getX(),
                                    player.getY(),
                                    player.getZ(),
                                    SoundEvents.ENTITY_FOX_TELEPORT,
                                    SoundCategory.MASTER,
                                    1.0f,
                                    1.0f
                            );
                        }
                }
            }
        }
    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {
        //Regen Aura
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
                        239,
                        0,
                        false,
                        true,
                        true
                ));
            }
            regenCooldown=REGEN_DEFAULT_COOLDOWN;
        }

        //Growth Aura
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
        if (resurrectionCooldown>0){
            resurrectionCooldown--;
        }
        if (groupTPCooldown>0){
            groupTPCooldown--;
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
            boolean isMinion = (victim instanceof MinionZombieEntity);
            if (!(isUndead || isInanimate || isZombie || isSkeleton || isMinion)){
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
        int lifeLevel = magicData.getMagicLevel(6);
        if (lifeLevel>=1) {
            growthAuraToggle=!growthAuraToggle;
            player.sendMessage(Text.of("Growth Aura: " + growthAuraToggle));
        }
    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int lifeLevel = magicData.getMagicLevel(6);
        if (lifeLevel>=3) {
            //TRYING TO RES
            if (resurrectionCooldown==0){
                BlockPos playerPos = player.getBlockPos();
                for (String deathName : deathList){
                    if (deathMap.get(deathName)!=null){
                        BlockPos deathPos = deathMap.get(deathName).getDeathPos();
                        ServerWorld deathWorld = deathMap.get(deathName).getWorld();
                        //Check if on a deathblock on the right world
                        if (deathPos.equals(playerPos) && deathWorld.equals(player.getWorld())){
                            //THE POSITION MATCH! REZ THE PEOPLE
                            MinecraftServer server = player.getServer();
                            for (ServerPlayerEntity serverPlayer : server.getPlayerManager().getPlayerList()) {
                                if (serverPlayer.getName().toString().equals(deathName)) {
                                    //Teleport the player that was found with the same name
                                    serverPlayer.teleport((ServerWorld) player.getWorld(), deathPos.getX()+0.5, deathPos.getY(), deathPos.getZ()+0.5, player.getYaw(), player.getPitch());
                                    //Destroy the deathPos particle
                                    deathMap.get(deathName).setTimer(0);

                                    //PLAYSOUND HERE TOO
                                    player.getWorld().playSound(
                                            null,
                                            player.getX(),
                                            player.getY(),
                                            player.getZ(),
                                            SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED,
                                            SoundCategory.PLAYERS,
                                            1.0f,
                                            1.0f
                                    );

                                    //If we successfully rez someone, we put on cooldown
                                    resurrectionCooldown = RESURRECTION_DEFAULT_COOLDOWN;
                                }
                            }
                        }
                    }
                }
            }
        }

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


    private boolean isFlower(Block block) {
        return block.getDefaultState().isIn(BlockTags.SMALL_FLOWERS);
    }

    private boolean checkForGateway(PlayerEntity player, World world, Block flowerBlock) {
        BlockPos playerPos = player.getBlockPos();
        int radius = GATEWAY_DETECT_RANGE;
        int maxY = world.getHeight();


        for (int x = -radius; x <= radius; x++) {
            for (int y = 0; y < maxY; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.add(x, y - playerPos.getY(), z);
                    if (world.getBlockState(pos).getBlock() == BLOCK_FOR_GATEWAY) {
                        if (checkFlowerSurrounding(world, pos, flowerBlock)) {
                            teleportNearbyPlayers(world, player, pos);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean checkFlowerSurrounding(World world, BlockPos framePos, Block flowerBlock) {
        // verify if the blocks surrounding it are good
        BlockPos[] surroundingPositions = {
                framePos.north(),
                framePos.south(),
                framePos.east(),
                framePos.west()
        };

        for (BlockPos pos : surroundingPositions) {
            if (world.getBlockState(pos).getBlock() != flowerBlock) {
                return false;
            }
        }
        return true;
    }

    private void teleportNearbyPlayers(World world, PlayerEntity sourcePlayer, BlockPos targetPos) {
        // GET PLAYERS IN RADIUS
        Box box = new Box(sourcePlayer.getX() - GROUP_TP_RADIUS, sourcePlayer.getY() - GROUP_TP_RADIUS, sourcePlayer.getZ() - GROUP_TP_RADIUS,
                sourcePlayer.getX() + GROUP_TP_RADIUS, sourcePlayer.getY() + GROUP_TP_RADIUS, sourcePlayer.getZ() + GROUP_TP_RADIUS);

        List<ServerPlayerEntity> nearbyPlayers = world.getEntitiesByClass(
                ServerPlayerEntity.class, box, player -> true);

        // Teleporting the players to the selected blocks
        for (ServerPlayerEntity player : nearbyPlayers) {
            player.teleport((ServerWorld) world,targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, player.getYaw(), player.getPitch());
        }
    }
}
