package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.entity.MinionZombieEntity;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import static com.elementalconvergence.entity.ModEntities.MINION_ZOMBIE;

public class DeathMagicHandler implements IMagicHandler {
    public static final int DEATH_INDEX=7;

    public static final int HORSE_DEFAULT_COOLDOWN=40;
    public static final int ZOMBIE_DEFAULT_COOLDOWN=20; //so every 1 seconds

    private int horseCooldown=0;
    private int zombieCooldown=0;

    @Override
    public void handleItemRightClick(PlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        ServerWorld serverWorld = (ServerWorld) player.getWorld();

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int deathLevel = magicData.getMagicLevel(DEATH_INDEX);
        //LVL 2 ABILITY
        if (deathLevel>=2){
            if (mainHand.isOf(Items.GOLDEN_HOE) && horseCooldown==0){

                //REMOVE DURABILITY OF THE HOE
                if (!player.isCreative()) {
                    int maxDurability = mainHand.getMaxDamage();
                    int durabilityToRemove = maxDurability / 4;  // Remove 25% of max durability

                    // BREAK ITEM IF LESS THAN 25% LEFT
                    if (mainHand.getDamage() + durabilityToRemove >= maxDurability) {
                        mainHand.setDamage(maxDurability);
                        mainHand.decrement(1);
                        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    } else {
                        mainHand.setDamage(mainHand.getDamage() + durabilityToRemove);
                    }
                }

                //KILL PREVIOUS OWNED
                killExistingHorse(player, serverWorld);

                //SUMMON NEW
                ZombieHorseEntity horse = summonSkeletonHorse(player, serverWorld);


                //PLAYSOUND
               serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_ZOMBIE_HORSE_AMBIENT, SoundCategory.PLAYERS, 1.0F, 1.0F);


                horseCooldown=HORSE_DEFAULT_COOLDOWN;
                player.getItemCooldownManager().set(Items.GOLDEN_HOE, HORSE_DEFAULT_COOLDOWN);
            }
        }

        if (deathLevel>=1){
            if (zombieCooldown==0){
                if (mainHand.isOf(Items.WOODEN_HOE) || mainHand.isOf(Items.STONE_HOE) || mainHand.isOf(Items.IRON_HOE) || mainHand.isOf(Items.DIAMOND_HOE) || mainHand.isOf(Items.NETHERITE_HOE)){

                    MinionZombieEntity minionZombie = new MinionZombieEntity(MINION_ZOMBIE, serverWorld);
                    minionZombie.setPosition(player.getX(), player.getY(), player.getZ());
                    minionZombie.setSummoner(player);

                    // SET THE TARGET (I DONT THINK THIS WORKS NGL)
                    HitResult hitResult = player.raycast(20.0, 0.0f, false);
                    if (hitResult.getType() == HitResult.Type.ENTITY) {
                        LivingEntity target = (LivingEntity) ((EntityHitResult) hitResult).getEntity();
                        minionZombie.setTarget(target);
                    }

                    serverWorld.spawnEntity(minionZombie);

                    //PUT ARMOR ON THAT SHIT
                    minionZombie.equipArmorBasedOnHoe(mainHand);

                    //REMOVE DURABILITY OF THE HOE
                    if (!player.isCreative()) {
                        int maxDurability = mainHand.getMaxDamage();
                        int durabilityToRemove = maxDurability / 5;  // Remove 20% of max durability

                        // BREAK ITEM IF LESS THAN 25% LEFT
                        if (mainHand.getDamage() + durabilityToRemove >= maxDurability) {
                            mainHand.setDamage(maxDurability);
                            mainHand.decrement(1);
                            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                                    SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);
                        } else {
                            mainHand.setDamage(mainHand.getDamage() + durabilityToRemove);
                        }
                    }

                    serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, SoundCategory.PLAYERS, 1.0F, 1.0F);

                    zombieCooldown=ZOMBIE_DEFAULT_COOLDOWN;
                    player.getItemCooldownManager().set(Items.STONE_HOE, ZOMBIE_DEFAULT_COOLDOWN);
                    player.getItemCooldownManager().set(Items.WOODEN_HOE, ZOMBIE_DEFAULT_COOLDOWN);
                    player.getItemCooldownManager().set(Items.IRON_HOE, ZOMBIE_DEFAULT_COOLDOWN);
                    player.getItemCooldownManager().set(Items.DIAMOND_HOE, ZOMBIE_DEFAULT_COOLDOWN);
                    player.getItemCooldownManager().set(Items.NETHERITE_HOE, ZOMBIE_DEFAULT_COOLDOWN);

                }
            }
        }

    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {

        //Cooldowns
        if (horseCooldown>0){
            horseCooldown--;
        }
        if (zombieCooldown>0){
            zombieCooldown--;
        }
    }

    @Override
    public void handleAttack(PlayerEntity player, Entity victim) {
        boolean isUndead = victim.getType().isIn(EntityTypeTags.UNDEAD);
        boolean isZombie = victim.getType().isIn(EntityTypeTags.ZOMBIES);
        boolean isSkeleton = victim.getType().isIn(EntityTypeTags.SKELETONS);
        boolean noLifeRegen = !isUndead && !isZombie && !isSkeleton;
        //Lifesteal passive
        if (victim instanceof LivingEntity && noLifeRegen && !(victim instanceof MinionZombieEntity)){
            player.heal(1.0f);
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

    private void killExistingHorse(PlayerEntity player, ServerWorld world) {
        // check all entities
        for (ZombieHorseEntity horse : world.getEntitiesByClass(ZombieHorseEntity.class, player.getBoundingBox().expand(500), entity -> true)) {
            if (horse!=null && horse.getOwnerUuid()!=null) {
                if (horse.getOwnerUuid().equals(player.getUuid())) {
                    //horse.teleport(horse.getX(), -300, horse.getZ(), false);
                    //horse.kill();
                    horse.refreshPositionAndAngles(horse.getX(), -200.0, horse.getZ(), horse.getYaw(), horse.getPitch());
                }
            }
        }
    }

    private ZombieHorseEntity summonSkeletonHorse(PlayerEntity player, ServerWorld world) {
        ZombieHorseEntity horse = new ZombieHorseEntity(EntityType.ZOMBIE_HORSE, world);

        // SAME POS AS PLAYER
        Vec3d pos = player.getPos();
        horse.refreshPositionAndAngles(pos.x, pos.y, pos.z, player.getYaw(), 0);

        // SPEED
        EntityAttributeInstance speedAttribute = horse.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.setBaseValue(0.3); // default is 0.2
        }

        EntityAttributeInstance jumpAttribute = horse.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH);
        if (jumpAttribute != null) {
            jumpAttribute.setBaseValue(0.6); // default is 0.7
        }

        // Spawn the horse in the world
        world.spawnEntity(horse);

        //Saddle it
        horse.saddle(new ItemStack(Items.SADDLE), SoundCategory.NEUTRAL);
        //Make it so it doesnt drop xp NOR bones. STILL DROPS SADDLE THO
        //horse.setNoDrag(true);
        //Make it rideable?
        horse.setTame(true);
        //make the player the owner so that we can kill it later
        horse.setOwnerUuid(player.getUuid());

        return horse;
    }
}
