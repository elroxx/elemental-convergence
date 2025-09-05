package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.IPlayerMiningMixin;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.magic.IMagicHandler;
import com.elementalconvergence.networking.MiningSpeedPayload;
import com.google.gson.JsonObject;
import com.ibm.icu.number.Scale;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.samo_lego.fabrictailor.FabricTailor;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.command.FabrictailorCommand;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleRegistries;
import virtuoel.pehkui.api.ScaleType;
import virtuoel.pehkui.api.ScaleTypes;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static com.elementalconvergence.ElementalConvergence.hasAdvancement;

public class EarthMagicHandler implements IMagicHandler {
    public static final float WOODEN_PICKAXE_MULTIPLIER=2.0f;
    public static final float STONE_PICKAXE_MULTIPLIER=4.0f;
    public static final float IRON_PICKAXE_MULTIPLIER=6.0f;
    public static final float DIAMOND_PICKAXE_MULTIPLIER=8.0f;
    public static final float NETHERITE_PICKAXE_MULTIPLIER=10.0f; //Technically it is 9.0f, but I wanted to make it faster
    public static final float DEFAULT_PICKAXE_MULTIPLIER=1.0f;

    public static final float EARTH_PLAYER_SCALE = 1.33f;
    public static final float EARTH_MOVE_SPEED=0.06f;
    public static final float EARTH_JUMP_HEIGHT=0.30f;
    public static final float EARTH_STEP_HEIGHT=1.0f;
    public static final float EARTH_HELD_ITEM=1.0f;
    public static final float EARTH_KB_RES=0.0f;

    public static final float BURROW_SCALE = 0.1f;
    public static final float BURROW_REACH = 0.01f;
    public static final float BURROW_SPEED = 0.18f;
    public static final float BURROW_JUMP = 0.01f;
    public static final float BURROW_STEP = 4f;
    public static final float BURROW_HELD = 0f;

    public static final float STONEARMOR_SPEED = 0.01f;
    public static final float STONEARMOR_JUMP = 0.01f;
    public static final float STONEARMOR_KB_RES= 1.0f;

    //Abilities Toggle
    private boolean veinMinerToggle=false;
    private boolean burrowToggle=false;
    private boolean stoneArmorToggle=false;

    //For vein miner
    private static final int BREAK_DELAY_TICKS = 2;
    private static final int MAX_BLOCKS = 64;
    private static final List<BlockPos> PROCESSING_BLOCKS = new ArrayList<BlockPos>();

    //Particles for burrow
    private static final int PARTICLE_COUNT = 1;
    private static final double SPAWN_RADIUS = 0.5f;






    @Override
    public void handleItemRightClick(PlayerEntity player) {

    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {


        //SCALING MODIFIED (SO NEGATIVE PASSIVE+BURROW)
        if (player instanceof ServerPlayerEntity){
            float scaleModifier=EARTH_PLAYER_SCALE;
            float reachModifier=EARTH_PLAYER_SCALE;
            float speedModifier=EARTH_MOVE_SPEED;
            float jumpModifier=EARTH_JUMP_HEIGHT;
            float stepModifier=EARTH_STEP_HEIGHT;
            float heldItemModifier=EARTH_HELD_ITEM;
            float kbResModifier=EARTH_KB_RES;

            if (burrowToggle && !stoneArmorToggle){
                //ONLY BURROW MODIFIER
                scaleModifier=BURROW_SCALE;
                reachModifier=BURROW_REACH;
                speedModifier=BURROW_SPEED;
                jumpModifier=BURROW_JUMP;
                stepModifier=BURROW_STEP;
                heldItemModifier=BURROW_HELD;
            }
            if (stoneArmorToggle && !burrowToggle){
                //ONLY STONE ARMOR MODIFIER
                speedModifier=STONEARMOR_SPEED;
                jumpModifier=STONEARMOR_JUMP;
                kbResModifier=STONEARMOR_KB_RES;
            }


            //SETTING SIZE WITH PEKHUI SCALE
            ScaleData playerHeight = ScaleTypes.HEIGHT.getScaleData(player);
            ScaleData playerWidth = ScaleTypes.WIDTH.getScaleData(player);
            ScaleData playerReach = ScaleTypes.BLOCK_REACH.getScaleData(player);
            ScaleData playerEntityReach = ScaleTypes.ENTITY_REACH.getScaleData(player);
            ScaleData playerStep = ScaleTypes.STEP_HEIGHT.getScaleData(player);
            ScaleData playerHeldItem = ScaleTypes.HELD_ITEM.getScaleData(player);

            if (!(Math.abs(playerHeight.getScale()-scaleModifier)<0.05f)) {
                playerHeight.setScale(scaleModifier);
                playerWidth.setScale(scaleModifier);
                playerReach.setScale(reachModifier);
                playerEntityReach.setScale(reachModifier);
                playerStep.setScale(stepModifier);
                playerHeldItem.setScale(heldItemModifier);
                //System.out.println("Modified Everything");
            }

            //SETTING MOVEMENT SPEED AND JUMP HEIGHT SO ONLY PLAYER ATTRIBUTES
            if (!(Math.abs(player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).getBaseValue()-speedModifier)<0.0005f)){
                player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(speedModifier); //SPEED
                player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_JUMP_STRENGTH).setBaseValue(jumpModifier); //JUMP HEIGHT
                player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(kbResModifier); //KNOCKBACK RESISTANCE
            }

        }

        //Particles for burrow+Invis
        if (burrowToggle){
            //If no invis, put back invis
            if (!player.hasStatusEffect(StatusEffects.INVISIBILITY)){
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, -1, 0, false, false, false));
            }

            //Start particles
            if (player instanceof ServerPlayerEntity serverPlayer) {
                spawnBlockParticlesAtPlayerFeet(serverPlayer);
            }
        }

        //Resistance for Stone Armor
        if (stoneArmorToggle){
            //If no res, put back res. THIS IS RESISTANCE 3
            if (!player.hasStatusEffect(StatusEffects.RESISTANCE)){
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, -1, 2, false, false, true));
            }

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
        updateHandMiningSpeed(player); //THIS IS THE PASSIVE SO NO LVL REQUIREMENTS NEEDED
    }

    public void handleBlockBreak(PlayerEntity player, BlockPos pos, BlockState state, BlockEntity entity) {
        if (veinMinerToggle) {
            if (state.isIn(BlockTags.COAL_ORES)) {
                veinMiner(player, pos, state, 0);
            }
            else if (state.isIn(BlockTags.COPPER_ORES)) {
                veinMiner(player, pos, state, 1);
            }
            else if (state.isIn(BlockTags.IRON_ORES)) {
                veinMiner(player, pos, state, 2);
            }
            else if (state.isIn(BlockTags.GOLD_ORES)) {
                veinMiner(player, pos, state, 3);
            }
            else if (state.isIn(BlockTags.REDSTONE_ORES)) {
                veinMiner(player, pos, state, 4);
            }
            else if (state.isIn(BlockTags.LAPIS_ORES)) {
                veinMiner(player, pos, state, 5);
            }
            else if (state.isIn(BlockTags.EMERALD_ORES)) {
                veinMiner(player, pos, state, 6);
            }
            else if (state.isIn(BlockTags.DIAMOND_ORES)) {
                veinMiner(player, pos, state, 7);
            }
            else if (state.getBlock() == Blocks.OBSIDIAN) {
                veinMiner(player, pos, state, 8);
            }
            else if (state.getBlock() == Blocks.NETHER_QUARTZ_ORE){
                veinMiner(player, pos, state, 9);
            }
        }
    }

    @Override
    public void handlePrimarySpell(PlayerEntity player) {
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int earthLevel = magicData.getMagicLevel(0);
        if (earthLevel>=1) {
            veinMinerToggle=!veinMinerToggle;
            String veinMsg = "§aVein miner ";
            veinMsg +=  veinMinerToggle ? "activated!" : "deactived!";
            player.sendMessage(Text.of(veinMsg), true);

            player.getWorld().playSound(null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.ITEM_ARMOR_EQUIP_IRON,
                    SoundCategory.BLOCKS,
                    1.0F,
                    1.0F
            );

        }
    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int earthLevel = magicData.getMagicLevel(0);
        if (earthLevel>=2) {
            if (stoneArmorToggle){
                player.sendMessage(Text.of("Cannot have both Stone Armor and Burrow toggled on."));
                return;
            }
            burrowToggle=!burrowToggle;
            //player.sendMessage(Text.of("Burrow: " + burrowToggle));
        }
        //Remove invisibility
        if (!burrowToggle){
            player.removeStatusEffect(StatusEffects.INVISIBILITY);
        }
    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int earthLevel = magicData.getMagicLevel(0);
        if (earthLevel>=3) {
            if (burrowToggle){
                player.sendMessage(Text.of("Cannot have both Stone Armor and Burrow toggled on."));
                return;
            }

            stoneArmorToggle=!stoneArmorToggle;
            //player.sendMessage(Text.of("Stone Armor: " + stoneArmorToggle));

            BlockPos pos = player.getBlockPos();
            player.getWorld().playSound(null,  //So that everybody nearby can hear it
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    SoundEvents.UI_STONECUTTER_TAKE_RESULT,
                    SoundCategory.BLOCKS,
                    1.0F,  // Volume
                    1.0F   // Pitch
            );

            //Custom skin setup
            if (stoneArmorToggle){
                TailoredPlayer tailoredPlayer = (TailoredPlayer) player;

                // Your custom skin value and signature
                String skinValue = "ewogICJ0aW1lc3RhbXAiIDogMTY1NTY0Njc4NTU5MywKICAicHJvZmlsZUlkIiA6ICIzZmM3ZmRmOTM5NjM0YzQxOTExOTliYTNmN2NjM2ZlZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJZZWxlaGEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDBkNjhjOTI3YzFhZWIyMmM2ZjA0Yjg1NmYzYTgzYmI0ZTRhYzAxN2U3MDk1MGQ4NGQ3MmU3ZjAzODI0NTk1NyIKICAgIH0KICB9Cn0=";
                String skinSignature = "rjMSG//n9lHjB1f8HCOQKR9uvFdUvXSHzf8+djbcje5GTvPmNPiCD3hw1D1Jn321s7vKJ1Fxa3iZvINDvISJuzDzmmdRYFz4TXvuKfnfE/aK8NjB5uAhazpgTqj5cCuxDOY1JlVPUz3z2DAMRs2bsftf9Ssr/Rwmt3fLYMFNYiYDqn+Tab6pht2EjUa8tANhhFmYB0a8lCvGOEDmn5PLyXY3eDYakQkO24MHE73HhtqqgEIVpAJSqk9Qc6fU1byXfKYl+wYjUrvJ+SEMQNDk2wvNcw6JaL+s0wzkl7X17WjP45M8lLqclyRfkzWgBFJC03Ygyv1I4+B2kFgGMUC7M36D9fKv6fkll9ppjZWz2Q8RRnqgCbbrKi7G8z26Fc9b2nHKY44uOjvM49UHnYlhWup/sqtJL7L7iRuJoiMFlMAJh7ftjErHp8TTVwrFRLsD/wEiH4Zt+Rn12kdrPYxGNs7WfH2Tm93gqTzr+83Hw2ZovpF7JvrINWjt/JBtDe2h/VtuRpJKxFs759xpX1x6xnK/mBfAzqgpsJ7fhOYc/ZyHAAypyKxrb/rTc25ZVl8p38DAw7C9hyHm3bdveShp9pA7y1NTZ9ubieadXFbBY0TxaRnhX8e+43xZSYJ/20+nm90VauaiiFHPOyiGlwojOJIOTU9ypNtJvyLl63RoJTk=";

                // Set the skin and reload it
                tailoredPlayer.fabrictailor_setSkin(skinValue, skinSignature, true);

            }
            if (!stoneArmorToggle){
                player.removeStatusEffect(StatusEffects.RESISTANCE);
                ((TailoredPlayer) player).fabrictailor_clearSkin();
            }
        }
    }

    private void updateHandMiningSpeed(PlayerEntity player){
        float multiplier=DEFAULT_PICKAXE_MULTIPLIER;


        ItemStack mainHand = player.getMainHandStack();
        if (!mainHand.isEmpty()){
            if (mainHand.isOf(Items.NETHERITE_PICKAXE)) {
                multiplier = NETHERITE_PICKAXE_MULTIPLIER+0.5f;
            } else if (mainHand.isOf(Items.DIAMOND_PICKAXE)) {
                multiplier = DIAMOND_PICKAXE_MULTIPLIER+0.5f;
            } else if (mainHand.isOf(Items.IRON_PICKAXE)) {
                multiplier = IRON_PICKAXE_MULTIPLIER+0.5f;
            } else if (mainHand.isOf(Items.STONE_PICKAXE)) {
                multiplier = STONE_PICKAXE_MULTIPLIER+0.5f;
            } else if (mainHand.isOf(Items.WOODEN_PICKAXE)) {
                multiplier = WOODEN_PICKAXE_MULTIPLIER+0.5f;
            } else {
                multiplier = DEFAULT_PICKAXE_MULTIPLIER;
            }
        }
        else if (hasAdvancement(player, "minecraft:nether/obtain_ancient_debris")) {
            multiplier = NETHERITE_PICKAXE_MULTIPLIER;
        }
        else if (hasAdvancement(player, "minecraft:story/mine_diamond")) {
            multiplier = DIAMOND_PICKAXE_MULTIPLIER;
        }
        else if (hasAdvancement(player, "minecraft:story/iron_tools")) {
            multiplier = IRON_PICKAXE_MULTIPLIER;
        }
        else if (hasAdvancement(player, "minecraft:story/upgrade_tools")) {
            multiplier = STONE_PICKAXE_MULTIPLIER;
        }
        else if (hasAdvancement(player, "minecraft:story/mine_stone")){
            multiplier = WOODEN_PICKAXE_MULTIPLIER;
        }
        // /advancement grant @a only minecraft:nether/obtain_ancient_debris
        System.out.println("MINING MULTIPLIER: "+((IPlayerMiningMixin) player).getMiningSpeedMultiplier());
        if (((IPlayerMiningMixin) player).getMiningSpeedMultiplier()!=multiplier) {
           ((IPlayerMiningMixin) player).setMiningSpeedMultiplier(multiplier);
            if (player instanceof ServerPlayerEntity serverPlayer) {
                ServerPlayNetworking.send(serverPlayer, new MiningSpeedPayload(multiplier));
            }
        }
    }


    private void veinMiner(PlayerEntity player, BlockPos startPos, BlockState startState, int oreType) {

        //0=coal,1=copper,2=iron,3=gold,4=redstone,5=lapis,6=emerald,7=diamond,8=obsidian, 9 quartz
        if (!PROCESSING_BLOCKS.add(startPos)) {
            return; // Already processing this position
        }

        try {
            List<BlockPos> blocksToMine = findConnectedOres(player.getWorld(), startPos, oreType);
            mineBlocks(player.getWorld(), blocksToMine, player);
        } finally {
            PROCESSING_BLOCKS.remove(startPos);
        }
    }

    private List<BlockPos> findConnectedOres(World world, BlockPos startPos, int oreType) {
        Set<BlockPos> visitedBlocks = new HashSet<>();  // For duplicate checking
        List<BlockPos> orderedBlocks = new ArrayList<>();  // BFS
        Queue<BlockPos> toCheck = new LinkedList<>();
        toCheck.add(startPos);

        while (!toCheck.isEmpty() && orderedBlocks.size() < MAX_BLOCKS) {
            BlockPos currentPos = toCheck.poll();
            if (!visitedBlocks.add(currentPos)) {
                continue; // alr checkde this block
            }
            orderedBlocks.add(currentPos); // list ordered

            // All adjacent
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        BlockPos newPos = currentPos.add(x, y, z);
                        BlockState newState = world.getBlockState(newPos);

                        // adjacents
                        boolean isMatchingOre = switch (oreType) {
                            case 0 -> newState.isIn(BlockTags.COAL_ORES);
                            case 1 -> newState.isIn(BlockTags.COPPER_ORES);
                            case 2 -> newState.isIn(BlockTags.IRON_ORES);
                            case 3 -> newState.isIn(BlockTags.GOLD_ORES);
                            case 4 -> newState.isIn(BlockTags.REDSTONE_ORES);
                            case 5 -> newState.isIn(BlockTags.LAPIS_ORES);
                            case 6 -> newState.isIn(BlockTags.EMERALD_ORES);
                            case 7 -> newState.isIn(BlockTags.DIAMOND_ORES);
                            case 8 -> newState.getBlock() == Blocks.OBSIDIAN;
                            case 9 -> newState.getBlock() == Blocks.NETHER_QUARTZ_ORE;
                            default -> false;
                        };

                        if (isMatchingOre && !visitedBlocks.contains(newPos)) {
                            toCheck.add(newPos);
                        }
                    }
                }
            }
        }

        return orderedBlocks;
    }

    private void mineBlocks(World world, List<BlockPos> blocks, PlayerEntity player) {
        Queue<BlockPos> blockQueue = new LinkedList<>(blocks);
        final int[] tickCounter = {0};
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!blockQueue.isEmpty() && tickCounter[0]++ >= BREAK_DELAY_TICKS) {
                BlockPos pos = blockQueue.poll();
                world.breakBlock(pos, true, player);
                tickCounter[0] = 0;
            }
        });
    }

    private void spawnBlockParticlesAtPlayerFeet(ServerPlayerEntity player) {
        //Position of blocks
        BlockPos playerPos = player.getBlockPos();
        BlockPos blockBelowPos = playerPos.down();
        ServerWorld world = player.getServerWorld();
        BlockState blockBelow = world.getBlockState(blockBelowPos);

        // Player pos
        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();

        // PARTICLE OF THE BLOCKSTATE
        BlockStateParticleEffect blockParticle = new BlockStateParticleEffect(
                ParticleTypes.BLOCK,
                blockBelow
        );

        //SPAWN ALL THE PARTICLES
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            double offsetX = (Math.random() - 0.5) * SPAWN_RADIUS;
            double offsetZ = (Math.random() - 0.5) * SPAWN_RADIUS;

            world.spawnParticles(
                    blockParticle,
                    playerX + offsetX, playerY, playerZ + offsetZ, 1, 0, 0, 0, 0.1
            );
        }
    }


}
