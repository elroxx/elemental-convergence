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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

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
    private double currentX = 0;
    private double currentY = 0;
    private double currentZ = 0;
    private double directionX = 0;
    private double directionY = 0;
    private double directionZ = 0;

    public static final int DEFAULT_WEAVE_COOLDOWN=5*20; //5 seconds cooldown
    private int weaveCooldown=0;
    private static final double WEAVE_CONE_RANGE = 50;
    private static final double WEAVE_CONE_ANGLE = 120.0;
    private static final int WEAVE_EFFECT_DURATION = 4*20;

    //buff: wall climb
    //X - buff: poison on hit.
    //X - debuff: can't attack in a light level that is too high (probably can attack up to when light level is 9. After 9, can't attack)
    //X - Passive: Spider webs are solid blocks.


    //lvl 1: When right clicking with a stack of string in the air. Create a line of cobwebs in the sky. Consume 1 string per block placed. Place them 1 by 1 with a small delay like the vein miner ability.
    //X - lvl 2: web slinging
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

            // Normalize the direction vector
            double magnitude = Math.sqrt(directionX * directionX + directionY * directionY + directionZ * directionZ);
            directionX /= magnitude;
            directionY /= magnitude;
            directionZ /= magnitude;

            // Start placing cobwebs from player's feet position, offset by one block in look direction
            BlockPos playerFeetPos = player.getBlockPos();
            currentX = playerFeetPos.getX() + 0.5 + directionX;
            currentY = playerFeetPos.getY() + 0.5 + directionY;
            currentZ = playerFeetPos.getZ() + 0.5 + directionZ;

            cobwebsToPlace = stringCount;

            // Set cooldown based on number of strings (2 ticks per string)
            silkBridgeCooldown = stringCount * 2;

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


        //place cobwebs tick by tick
        if (cobwebsToPlace > 0) {
            //stop if no longer holding string
            if (!player.getMainHandStack().isOf(Items.STRING)) {
                cobwebsToPlace = 0;
                currentX = 0;
                currentY = 0;
                currentZ = 0;
                return;
            }

            World world = player.getWorld();
            BlockPos targetPos = new BlockPos((int) Math.floor(currentX), (int) Math.floor(currentY), (int) Math.floor(currentZ));

            //need air. can't break anything
            if (world.getBlockState(targetPos).isAir()) {
                world.setBlockState(targetPos, Blocks.COBWEB.getDefaultState());

                // consume 1 string at a time
                ItemStack mainHand = player.getMainHandStack();
                if (mainHand.isOf(Items.STRING)) {
                    mainHand.decrement(1);
                }

                // playsound
                world.playSound(null, targetPos, SoundEvents.BLOCK_WOOL_PLACE,
                        SoundCategory.BLOCKS, 0.5f, 1.0f);

                // spider particles ig
                if (world instanceof ServerWorld serverWorld) {
                    serverWorld.spawnParticles(ParticleTypes.ITEM_COBWEB,
                            targetPos.getX() + 0.5,
                            targetPos.getY() + 0.5,
                            targetPos.getZ() + 0.5,
                            3, 0.2, 0.2, 0.2, 0.05);
                }
            } else {
                //then we hit block so we stop
                cobwebsToPlace = 0;
            }

            // next pos
            currentX += directionX;
            currentY += directionY;
            currentZ += directionZ;
            cobwebsToPlace--;

            //if done
            if (cobwebsToPlace <= 0) {
                currentX = 0;
                currentY = 0;
                currentZ = 0;
            }
        }

        //cooldowns
        if (silkBridgeCooldown>0){
            silkBridgeCooldown--;
        }

        if (weaveCooldown>0){
            weaveCooldown--;
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
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int spiderLevel = magicData.getMagicLevel(SPIDER_INDEX);

        if (spiderLevel >= 3 && weaveCooldown == 0) {
            ServerWorld world = (ServerWorld) player.getWorld();

            stunEntities(player, world);

            //add particles

            // sound
            world.playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_SPIDER_AMBIENT,
                    SoundCategory.PLAYERS,
                    1.0F,
                    0.5F
            );
            weaveCooldown=DEFAULT_WEAVE_COOLDOWN;
        }

    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

    public void stunEntities(PlayerEntity player, ServerWorld world){
        double range = WEAVE_CONE_RANGE;
        double coneAngle = WEAVE_CONE_ANGLE;

        // lookdirection
        Vec3d lookDirection = player.getRotationVec(1.0F);
        Box searchBox = player.getBoundingBox().expand(range);

        List<LivingEntity> nearbyEntities = world.getEntitiesByClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != player // dont push player
        );

        for (LivingEntity entity : nearbyEntities) {
            //vector player entity
            Vec3d toEntity = entity.getPos().subtract(player.getPos());

            // dont check behind players
            if (toEntity.dotProduct(lookDirection) <= 0) {
                continue;
            }

            // normalized
            Vec3d normalizedLook = lookDirection.normalize();
            Vec3d normalizedToEntity = toEntity.normalize();

            // CONE COMPUTING
            double dot = normalizedLook.dotProduct(normalizedToEntity);
            double angleInRadians = Math.acos(dot);
            double angleInDegrees = Math.toDegrees(angleInRadians);

            // check if cone
            if (angleInDegrees <= coneAngle / 2) {
                //STUN
                //place block
                if (world.getBlockState(entity.getBlockPos()).equals(Blocks.AIR)){
                    world.setBlockState(entity.getBlockPos(), Blocks.COBWEB.getDefaultState());
                }

                //give slowness 5, weakness 4, mining fatigue 10 (so they cant just mine the cobweb instantly
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, WEAVE_EFFECT_DURATION, 9, false, true, true));
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, WEAVE_EFFECT_DURATION, 9, false, true, true));
                entity.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, WEAVE_EFFECT_DURATION, 9, false, true, true));
            }
        }
    }
}