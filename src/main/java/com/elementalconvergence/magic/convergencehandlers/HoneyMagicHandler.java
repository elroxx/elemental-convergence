package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.data.PollenDrop;
import com.elementalconvergence.data.PollenRarity;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.enchantment.ModEnchantments;
import com.elementalconvergence.entity.MinionBeeEntity;
import com.elementalconvergence.entity.ModEntities;
import com.elementalconvergence.entity.PegasusEntity;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.*;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class HoneyMagicHandler implements IMagicHandler {
    public static final int HONEY_INDEX = (BASE_MAGIC_ID.length-1)+5;

    public static final String BEE_SKIN_VALUE = "ewogICJ0aW1lc3RhbXAiIDogMTcyMjgyMDA2OTYyMSwKICAicHJvZmlsZUlkIiA6ICIyNWIyMGEwZGI3MTA0NGZjODBmZjk5YjlkM2ZmNzM4MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbGl0YW41M1lUIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzc0ZjQ3MjAzNWY2ZjcxNTg1ZGE2NTVjZGQwMjhiYTJhYWY1M2IxMGE1ZGY0Yjc4Y2Q1YWRjYjJjZDI4ODBjMyIKICAgIH0KICB9Cn0=";
    public static final String BEE_SKIN_SIGNATURE = "e0BUePWFneHevPc3DMJ39JuEFOIOmGUiuFo5R753bMvMuhvnVXqUS5uzBBPgmPklnaAyzlM0dDhGKATdUnumauSuctRmzNAaQJxt53ygAC1ajTO7R/ghviOEshmRJ96pQgJkMKPuHQMzCFDWEgxQnSGpzvkZETpCet+0AQ/M85ToXV51xHKp+O2T9oH9NEY6VFykrR6e+w/j9yYGXL0CAbsCxPkmoPluVIISd0bKtYZ8Rg4y/roj46Oo8/T905GYxJrl8sSqnc1mY+8EIwPwF6dakPav/Y7xPAJIVwNxOtqXHXtQ2FfmENj4/TdlHMzjZmfwXCq+Cg4upfGZ1I5zxluDirlbStAWE2a+XqIppkUp/QcKPDsEqkbRGtq2AS86YRT2wfjzk05OuEsJb4aiYwQYhgidmpqxehpTX2hkNylsutQCPzafZ9E2nnj2snhJ8pPLMRalkscdxfue9aclYZuna0wxXEusocbvUbwt72+0UKwSa3o1rJPieNgQ0dL3uutrgt+zzbuygaQ8e6LHioE/HtQ65ptxbAwLD59b4jGXEjjbZwokFwAVbgmpezcYo7pd+YWVEkx940oR4kXK31RRJJM1KfawAF7MCodzWsLjVxBwshCZZ3PE2cWhsz+94Ad80lxPv6xO34sKthUjWatrgopLU7aPCZKHM2ncQy8=";
    private boolean hasSkinOn=false;

    public static final float BEE_HEIGHT = 0.10f;
    public static final float BEE_WIDTH = 0.15f;
    public static final float BEE_MOTION= 0.25f;
    public static final float BEE_REACH=0.8f;
    public static final float BEE_HEALTH=16.0f;
    public static final float BEE_FLIGHT_SPEED=2.4f; //so 0.6 the speed of a creative player

    public static final int INVENTORY_LOCK_DEFAULT_COOLDOWN = 20; //aka 1 seconds
    private int inventoryLockCooldown=0;

    private static final int MAX_BEES_PER_PLAYER = 7;
    private static final Map<UUID, Integer> playerBeeCount = new HashMap<>();

    @Override
    public void handleItemRightClick(PlayerEntity player) {
        if (player.getMainHandStack().isEmpty()){
            player.sendMessage(Text.of("SUMMON BEE"));
            spawnMinionBee(player, player.getWorld());
        }
    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {

        //skin applying
        if (!hasSkinOn){
            // Set the skin and reload it
            TailoredPlayer tailoredPlayer = (TailoredPlayer) player;
            tailoredPlayer.fabrictailor_setSkin(BEE_SKIN_VALUE, BEE_SKIN_SIGNATURE, true);
            hasSkinOn=true;
        }

        //size changing

        //Take one just to double check if the scale is good
        ScaleData playerHeight = ScaleTypes.HEIGHT.getScaleData(player);
        if (!(Math.abs(playerHeight.getScale()-BEE_HEIGHT)<0.02f)){
            ScaleData playerWidth = ScaleTypes.WIDTH.getScaleData(player);
            ScaleData playerReach = ScaleTypes.BLOCK_REACH.getScaleData(player);
            ScaleData playerEntityReach = ScaleTypes.ENTITY_REACH.getScaleData(player);
            ScaleData playerMotion = ScaleTypes.MOTION.getScaleData(player);
            ScaleData playerFlight = ScaleTypes.FLIGHT.getScaleData(player);

            playerHeight.setScale(BEE_HEIGHT);
            playerWidth.setScale(BEE_WIDTH);
            playerReach.setScale(BEE_REACH);
            playerEntityReach.setScale(BEE_REACH);
            playerMotion.setScale(BEE_MOTION);
            playerFlight.setScale(BEE_FLIGHT_SPEED);
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(BEE_HEALTH);
        }

        //buff (creative flying)
        if (!player.getAbilities().allowFlying) {
            player.getAbilities().allowFlying = true;
            ((ServerPlayerEntity) player).sendAbilitiesUpdate();
        }


        //DEBUFF (Inventory replacement)
        if (inventoryLockCooldown==0){
            PlayerInventory inventory = player.getInventory();
            List<ItemStack> displacedItems = new ArrayList<>();

            // check all inv slots (so 9 to 36 coz 0-8 is hotbar)
            for (int i = 9; i < 36; i++) {
                ItemStack currentStack = inventory.getStack(i);

                // if item is not already a lock, we save it
                if (!currentStack.isEmpty() && !currentStack.isOf(ModItems.LOCK_ITEM)) {
                    displacedItems.add(currentStack.copy());
                }

                //replace with lock
                ItemStack locks = createEnchantedLock(player.getWorld());
                inventory.setStack(i, locks);
            }

            //if inventory was not fully empty
            if (!displacedItems.isEmpty()) {
                dropItemsOnGround(player, displacedItems);
            }
        }

        //Cooldowns
        if (inventoryLockCooldown>0){
            inventoryLockCooldown--;
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

    public void resetBeeSkinToggle(){
        hasSkinOn=false;
    }


    private static ItemStack createEnchantedLock(World world) {
        ItemStack lock = new ItemStack(ModItems.LOCK_ITEM);

        // need registry entry
        RegistryEntry<Enchantment> lockingCurse = world.getRegistryManager()
                .get(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                .getEntry(ModEnchantments.LOCKING_CURSE)
                .orElse(null);

        RegistryEntry<Enchantment> vanishingCurse = world.getRegistryManager()
                .get(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                .getEntry(Enchantments.VANISHING_CURSE)
                .orElse(null);


        //enchant lock
        if (lockingCurse != null) {
            lock.addEnchantment(lockingCurse, 1);
        }
        if (vanishingCurse != null) {
            lock.addEnchantment(vanishingCurse, 1);
        }

        return lock;
    }

    private static void dropItemsOnGround(PlayerEntity player, List<ItemStack> items) {
        //just drop all the items dont need no chest
        for (ItemStack item : items) {
            player.dropItem(item, false);
        }
    }

    public static void incrementBeeCount(UUID playerUuid) {
        playerBeeCount.put(playerUuid, playerBeeCount.getOrDefault(playerUuid, 0) + 1);
    }

    public static void decrementBeeCount(UUID playerUuid) {
        int current = playerBeeCount.getOrDefault(playerUuid, 0);
        if (current > 0) {
            playerBeeCount.put(playerUuid, current - 1);
        }
    }

    public static int getBeeCount(UUID playerUuid) {
        return playerBeeCount.getOrDefault(playerUuid, 0);
    }

    // Alternative method to get accurate count by checking actual entities in world
    public static int getActualBeeCount(PlayerEntity player, World world) {
        List<MinionBeeEntity> playerBees = world.getEntitiesByClass(
                MinionBeeEntity.class,
                player.getBoundingBox().expand(200.0), // Large search radius
                bee -> {
                    PlayerEntity owner = bee.getOwner();
                    return owner != null && owner.getUuid().equals(player.getUuid());
                }
        );

        // Update the counter to match reality
        playerBeeCount.put(player.getUuid(), playerBees.size());
        return playerBees.size();
    }

    private static void spawnMinionBee(PlayerEntity player, World world) {
        UUID playerUuid = player.getUuid();

        // Get accurate count by checking actual entities in world
        int currentBeeCount = getActualBeeCount(player, world);

        // Check if player has reached the maximum bee limit
        if (currentBeeCount >= MAX_BEES_PER_PLAYER) {
            player.sendMessage(Text.literal("§cYou already have the maximum number of minion bees! (" + currentBeeCount + "/" + MAX_BEES_PER_PLAYER + ")"), true);
            return;
        }

        MinionBeeEntity bee = new MinionBeeEntity(ModEntities.MINION_BEE, world);

        // Position the bee near the player
        Vec3d playerPos = player.getPos();
        bee.setPosition(playerPos.x + 1, playerPos.y + 1, playerPos.z);

        // Make the bee follow/protect the player
        bee.setOwner(player);

        world.spawnEntity(bee);

        // Increment the bee count
        incrementBeeCount(playerUuid);

        // Send feedback message
        player.sendMessage(Text.literal("§aMinion bee summoned! (" + (currentBeeCount + 1) + "/" + MAX_BEES_PER_PLAYER + ")"), true);

        // Optional: Add particle effects or sound
        world.playSound(null, player.getBlockPos(),
                net.minecraft.sound.SoundEvents.ENTITY_BEE_HURT,
                net.minecraft.sound.SoundCategory.NEUTRAL, 1.0f, 1.0f);
    }

    private static void targetAllBeesToEntity(PlayerEntity player, World world, Entity target) {
        // Find all bees within a reasonable radius (32 blocks) - both regular and minion bees
        List<BeeEntity> nearbyBees = world.getEntitiesByClass(
                BeeEntity.class,
                player.getBoundingBox().expand(32.0),
                bee -> true
        );

        // Also get minion bees specifically
        List<MinionBeeEntity> nearbyMinionBees = world.getEntitiesByClass(
                MinionBeeEntity.class,
                player.getBoundingBox().expand(32.0),
                bee -> bee.getOwner() == player // Only target bees owned by this player
        );

        // Target regular bees
        for (BeeEntity bee : nearbyBees) {
            if (target instanceof net.minecraft.entity.LivingEntity livingTarget) {
                bee.setTarget(livingTarget);
                bee.setAngerTime(400); // Angry for 20 seconds (400 ticks)
                bee.setAngryAt(target.getUuid());
            }
        }

        // Target minion bees
        for (MinionBeeEntity bee : nearbyMinionBees) {
            if (target instanceof net.minecraft.entity.LivingEntity livingTarget) {
                bee.setTarget(livingTarget);
                bee.setAngerTime(400);
                bee.setAngryAt(target.getUuid());
            }
        }

        // Optional: Visual/audio feedback
        if (!nearbyBees.isEmpty() || !nearbyMinionBees.isEmpty()) {
            world.playSound(null, player.getBlockPos(),
                    net.minecraft.sound.SoundEvents.ENTITY_BEE_LOOP_AGGRESSIVE,
                    net.minecraft.sound.SoundCategory.NEUTRAL, 1.0f, 1.2f);
        }
    }

    private static HitResult raycastForEntity(PlayerEntity player, World world) {
        // raycast for player
        Vec3d start = player.getEyePos();
        Vec3d direction = player.getRotationVec(1.0F);
        Vec3d end = start.add(direction.multiply(32.0)); // 32 block range

        // First check for entity hits
        EntityHitResult entityHit = raycastForEntities(player, world, start, end);
        if (entityHit != null) {
            return entityHit;
        }

        // If no entity hit, check for block hits
        return world.raycast(new RaycastContext(
                start, end,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player
        ));
    }

    private static EntityHitResult raycastForEntities(PlayerEntity player, World world, Vec3d start, Vec3d end) {
        Entity closestEntity = null;
        double closestDistance = Double.MAX_VALUE;

        // Check all entities in the path
        List<Entity> entities = world.getOtherEntities(player,
                player.getBoundingBox().stretch(end.subtract(start)).expand(1.0));

        for (Entity entity : entities) {
            if (entity == player) continue;

            // Check if ray intersects with entity's bounding box
            var boundingBox = entity.getBoundingBox().expand(0.3);
            var hit = boundingBox.raycast(start, end);

            if (hit.isPresent()) {
                double distance = start.distanceTo(hit.get());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entity;
                }
            }
        }

        if (closestEntity != null) {
            return new EntityHitResult(closestEntity);
        }

        return null;
    }


    public static PollenDrop getRandomPollenWithRarity(World world) {
        int roll = world.getRandom().nextInt(100);

        if (roll < 50) { // 50% so 0.1667 each
            Item[] commonPollens = {
                    ModItems.POLLEN_WHITE,   // Dolphin's Grace
                    ModItems.POLLEN_RED,     // Darkness
                    ModItems.POLLEN_BLUE     // Insect Weight
            };
            Item selectedPollen = commonPollens[world.getRandom().nextInt(commonPollens.length)];
            return new PollenDrop(selectedPollen, PollenRarity.COMMON);

        } else if (roll < 75) { // 25% so 0.125 each
            Item[] uncommonPollens = {
                    ModItems.POLLEN_ORANGE,  // Unluck
                    ModItems.POLLEN_PURPLE   // Luck
            };
            Item selectedPollen = uncommonPollens[world.getRandom().nextInt(uncommonPollens.length)];
            return new PollenDrop(selectedPollen, PollenRarity.UNCOMMON);

        } else if (roll < 90) { // 15% chance so 0.075 each
            Item[] rarePollens = {
                    ModItems.POLLEN_GREEN,   // Gills
                    ModItems.POLLEN_YELLOW   // Plague
            };
            Item selectedPollen = rarePollens[world.getRandom().nextInt(rarePollens.length)];
            return new PollenDrop(selectedPollen, PollenRarity.RARE);

        } else { // 10% chance so 0.05 each
            Item[] epicPollens = {
                    ModItems.POLLEN_BROWN,   // Light Phasing
                    ModItems.POLLEN_PINK     // Wings
            };
            Item selectedPollen = epicPollens[world.getRandom().nextInt(epicPollens.length)];
            return new PollenDrop(selectedPollen, PollenRarity.EPIC);
        }
    }

    public static void spawnRarityEffects(ServerWorld world, BlockPos pos, PollenRarity rarity) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        switch (rarity) {
            case COMMON -> {
                world.spawnParticles(ParticleTypes.COMPOSTER, x, y, z, 10, 0.3, 0.3, 0.3, 0.1);
            }
            case UNCOMMON -> {
                world.spawnParticles(ParticleTypes.PORTAL, x, y, z, 15, 0.3, 0.3, 0.3, 0.1);
                world.spawnParticles(ParticleTypes.COMPOSTER, x, y, z, 8, 0.2, 0.2, 0.2, 0.05);
            }
            case RARE -> {
                world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 12, 0.4, 0.4, 0.4, 0.1);
                world.spawnParticles(ParticleTypes.COMPOSTER, x, y, z, 15, 0.3, 0.3, 0.3, 0.1);
                world.spawnParticles(ParticleTypes.WAX_OFF, x, y, z, 8, 0.2, 0.2, 0.2, 0.05);
            }
            case EPIC -> {
                world.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 20, 0.5, 0.5, 0.5, 0.2);
                world.spawnParticles(ParticleTypes.ENCHANT, x, y, z, 25, 0.4, 0.4, 0.4, 0.15);
                world.spawnParticles(ParticleTypes.COMPOSTER, x, y, z, 20, 0.4, 0.4, 0.4, 0.1);
                world.spawnParticles(ParticleTypes.END_ROD, x, y, z, 10, 0.3, 0.3, 0.3, 0.1);
            }
        }
    }

    public static void playRaritySound(World world, BlockPos pos, PollenRarity rarity) {
        switch (rarity) {
            case COMMON -> {
                world.playSound(null, pos, SoundEvents.BLOCK_GRASS_BREAK,
                        SoundCategory.BLOCKS, 1.0f, 1.2f);
            }
            case UNCOMMON -> {
                world.playSound(null, pos, SoundEvents.BLOCK_GRASS_BREAK,
                        SoundCategory.BLOCKS, 1.0f, 1.4f);
                world.playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                        SoundCategory.BLOCKS, 0.5f, 1.0f);
            }
            case RARE -> {
                world.playSound(null, pos, SoundEvents.BLOCK_GRASS_BREAK,
                        SoundCategory.BLOCKS, 1.0f, 1.6f);
                world.playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                        SoundCategory.BLOCKS, 0.7f, 1.2f);
            }
            case EPIC -> {
                world.playSound(null, pos, SoundEvents.BLOCK_GRASS_BREAK,
                        SoundCategory.BLOCKS, 1.0f, 1.8f);
                world.playSound(null, pos, SoundEvents.ENTITY_PLAYER_LEVELUP,
                        SoundCategory.BLOCKS, 0.8f, 1.5f);
            }
        }
    }
}
