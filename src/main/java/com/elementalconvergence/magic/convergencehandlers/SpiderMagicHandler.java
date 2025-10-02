package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.IMagicHandler;
import gravity_changer.api.GravityChangerAPI;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.EnumSet;
import java.util.Set;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;
import static com.elementalconvergence.world.dimension.ModDimensions.VOID_WORLD_KEY;

public class SpiderMagicHandler implements IMagicHandler {
    public static final int SPIDER_INDEX= (BASE_MAGIC_ID.length-1)+11;

    public static final int SPIDER_LIGHT_THRESHOLD = 9;
    public static final float SPIDER_LIGHT_ATTACK=0.01f;
    public static final float SPIDER_DARK_ATTACK=1.0f;
    public static final float SPIDER_LIGHT_KB=0.01f;
    public static final float SPIDER_DARK_KB=1.0f;

    public static final int DEFAULT_SILK_BRIDGE_COOLDOWN=10;
    private int silkBridgeCooldown=0;

    // Cobweb placement tracking
    private int cobwebsToPlace = 0;
    private BlockPos currentCobwebPos = null;
    private double directionX = 0;
    private double directionY = 0;
    private double directionZ = 0;

    //buff: wall climb
    //buff: poison on hit.
    //debuff: can't attack in a light level that is too high (probably can attack up to when light level is 9. After 9, can't attack)
    //buff normal zombies, skeleton, creeper and spider+cave spiders dont attack you. (MAYBE NOT??) //actually i dont want, buff is wall climb.
    //Passive: Spider webs are solid blocks.


    //lvl 1: When right clicking with a stack of string in the air. Create a line of cobwebs in the sky. Consume 1 string per block placed. Place them 1 by 1 with a small delay like the vein miner ability.
    //lvl 2: web slinging
    //lvl 3: keybind, stun everybody in front in a cone. This gives them weakness 2 for like 10 seconds, slowness 2 for 10 seconds, poison 1 for 5 seconds and places a cobweb on their face and feet. 15 seconds cooldown.

    //advancements:
    //1: cobweb
    //2: spider's abdomen
    //3: dangers pottery shard

    //spider's abdomen is like b:blackwoold c:scaffolding, l:loom, p:chainmail leggings
    //bcb
    //bpb
    //blb

    //spider's eye*2, fermentedx1, black glazed terracotta, x2, loomx1



    @Override
    public void handleItemRightClick(PlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int spiderLevel = magicData.getMagicLevel(SPIDER_INDEX);

        //lvl 1 ability - Silk Bridge
        if (spiderLevel>=1 && mainHand.isOf(Items.STRING) && silkBridgeCooldown==0){
            int stringCount = mainHand.getCount();

            // Get player's look direction
            float yaw = (float) Math.toRadians(player.getYaw() + 90); // +90 to convert to proper direction
            float pitch = (float) Math.toRadians(-player.getPitch());

            directionX = Math.cos(pitch) * Math.cos(yaw);
            directionY = Math.sin(pitch);
            directionZ = Math.cos(pitch) * Math.sin(yaw);

            // Start placing cobwebs in a line from feet level, first block in look direction
            cobwebsToPlace = stringCount;
            BlockPos playerFeetPos = player.getBlockPos(); // This is already at feet level

            // Start one block away from player in look direction
            int startX = playerFeetPos.getX() + (int) Math.round(directionX);
            int startY = playerFeetPos.getY() + (int) Math.round(directionY);
            int startZ = playerFeetPos.getZ() + (int) Math.round(directionZ);
            currentCobwebPos = new BlockPos(startX, startY, startZ);

            // Set cooldown based on number of strings (2 ticks per string)
            silkBridgeCooldown = stringCount * 2;

            // Consume the string
            mainHand.decrement(stringCount);

            // Play activation sound
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_SPIDER_AMBIENT,
                    SoundCategory.PLAYERS, 0.8f, 1.2f);
        }
    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {

        //for cobweb and climbing related stuff
        if (!player.hasStatusEffect(ModEffects.ARACHNID)){
            player.addStatusEffect(new StatusEffectInstance(ModEffects.ARACHNID, -1, 0, false, false, false));
        }

        //can't attack in daylight (DEBUFF)
        BlockPos playerPosition = player.getBlockPos();
        int lightLevel = player.getWorld().getLightLevel(playerPosition);
        ScaleData playerAttack = ScaleTypes.ATTACK.getScaleData(player);
        ScaleData playerKnockback = ScaleTypes.KNOCKBACK.getScaleData(player);
        if (lightLevel<=SPIDER_LIGHT_THRESHOLD){
            if (!(Math.abs(playerAttack.getScale()-SPIDER_DARK_ATTACK)<0.02f)){
                playerAttack.setScale(SPIDER_DARK_ATTACK);
                playerKnockback.setScale(SPIDER_DARK_KB);
            }
        }else{
            if (!(Math.abs(playerAttack.getScale()-SPIDER_LIGHT_ATTACK)<0.02f)){
                playerAttack.setScale(SPIDER_LIGHT_ATTACK);
                playerKnockback.setScale(SPIDER_LIGHT_KB);
            }
        }

        //cooldowns
        if (silkBridgeCooldown>0){
            silkBridgeCooldown--;
        }

        // Handle gradual cobweb placement (1 per tick)
        if (cobwebsToPlace > 0 && currentCobwebPos != null) {
            World world = player.getWorld();

            // Check if current position can have cobweb placed (must be air)
            if (world.getBlockState(currentCobwebPos).isAir()) {
                // Place cobweb
                world.setBlockState(currentCobwebPos, Blocks.COBWEB.getDefaultState());

                // Play placement sound
                world.playSound(null, currentCobwebPos, SoundEvents.BLOCK_WOOL_PLACE,
                        SoundCategory.BLOCKS, 0.5f, 1.0f);

                // Spawn some particles for visual effect
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.ITEM_COBWEB,
                            currentCobwebPos.getX() + 0.5,
                            currentCobwebPos.getY() + 0.5,
                            currentCobwebPos.getZ() + 0.5,
                            3, 0.2, 0.2, 0.2, 0.05);
                }

                // Move to next position in look direction
                int nextX = currentCobwebPos.getX() + (int) Math.round(directionX);
                int nextY = currentCobwebPos.getY() + (int) Math.round(directionY);
                int nextZ = currentCobwebPos.getZ() + (int) Math.round(directionZ);
                currentCobwebPos = new BlockPos(nextX, nextY, nextZ);
                cobwebsToPlace--;
            } else {
                // Hit a block, stop placing cobwebs
                cobwebsToPlace = 0;
                currentCobwebPos = null;
            }

            // Check if we're done placing all cobwebs
            if (cobwebsToPlace <= 0) {
                currentCobwebPos = null;
            }
        }
    }

    @Override
    public void handleAttack(PlayerEntity player, Entity victim) {
        //add poison on hit (part of buff ig)
        if (victim instanceof LivingEntity livingEntity){
            livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 2*20, 0, true, true, true));
        }

        //add feedback sound to know it was a weak hit
        BlockPos playerPosition = player.getBlockPos();
        int lightLevel = player.getWorld().getLightLevel(playerPosition);
        if (lightLevel<=SPIDER_LIGHT_THRESHOLD){
            //good hit
        }else{
            //bad hit
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ITEM_SHIELD_BREAK,
                    SoundCategory.PLAYERS, 0.8f, 1.2f);
        }

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
}