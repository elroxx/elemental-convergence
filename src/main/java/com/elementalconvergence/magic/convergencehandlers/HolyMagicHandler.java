package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.entity.ModEntities;
import com.elementalconvergence.entity.PegasusEntity;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.component.type.PotionContentsComponent;

import java.util.*;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class HolyMagicHandler implements IMagicHandler {
    public static final int HOLY_INDEX= (BASE_MAGIC_ID.length-1)+4;

    public static final int HORSE_DEFAULT_COOLDOWN=40;
    public static final int GUARDIAN_ANGEL_DEFAULT_COOLDOWN=20*60*5; //5 minutes

    private int horseCooldown=0;
    private int guardianAngelCooldown=0;

    @Override
    public void handleItemRightClick(PlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();


        //lvl 1
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int holyLevel = magicData.getMagicLevel(HOLY_INDEX);

        if (holyLevel>=1) {
            if (mainHand.getItem().equals(Items.POTION) && !mainHand.get(DataComponentTypes.POTION_CONTENTS).hasEffects()) {
                player.setStackInHand(Hand.MAIN_HAND, new ItemStack(ModItems.WINE, 1));

                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_BREWING_STAND_BREW,
                        SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
            if (offHand.getItem().equals(Items.POTION) && !offHand.get(DataComponentTypes.POTION_CONTENTS).hasEffects()) {
                player.setStackInHand(Hand.OFF_HAND, new ItemStack(ModItems.WINE, 1));

                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_BREWING_STAND_BREW,
                        SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        }


        //LVL 2 ABILITY
        if (holyLevel >= 2 && horseCooldown==0) {
            if (mainHand.isOf(Items.GOLDEN_HORSE_ARMOR) || offHand.isOf(Items.GOLDEN_HORSE_ARMOR)) {

                player.getItemCooldownManager().set(Items.GOLDEN_HORSE_ARMOR, HORSE_DEFAULT_COOLDOWN);

                //KILL PREVIOUS OWNED
                ServerWorld serverWorld = (ServerWorld) player.getWorld();
                killExistingPegasus(player, serverWorld);

                //SUMMON NEW
                PegasusEntity horse = summonPegasus(player, serverWorld);


                //PLAYSOUND
                serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_HORSE_ANGRY, SoundCategory.PLAYERS, 1.0F, 1.0F);


                horseCooldown = HORSE_DEFAULT_COOLDOWN;
            }

        }
    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {
        //guardian angel (lvl 3)
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int holyLevel = magicData.getMagicLevel(HOLY_INDEX);
        if (holyLevel>=3) {
            if (player.getMainHandStack().isEmpty()) {
                if (targetEntity instanceof PlayerEntity target) {
                    ItemStack headStack = player.getEquippedStack(EquipmentSlot.HEAD);
                    boolean isGAOnCooldown = player.getItemCooldownManager().isCoolingDown(ModItems.HALO);
                    if (headStack.isOf(ModItems.HALO) && guardianAngelCooldown == 0 && !isGAOnCooldown) {

                        target.addStatusEffect(new StatusEffectInstance(ModEffects.GUARDIAN_ANGEL, 20 * 60 * 2, 0, false, false, true)); // 2/5 of the cooldown duration (so 2 minutes out of 5)

                        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.BLOCK_BEACON_ACTIVATE,
                                SoundCategory.PLAYERS, 1.0f, 2.0f);

                        //setting the cooldowns again
                        player.getItemCooldownManager().set(ModItems.HALO, GUARDIAN_ANGEL_DEFAULT_COOLDOWN);
                        guardianAngelCooldown = GUARDIAN_ANGEL_DEFAULT_COOLDOWN;
                    }
                }
            }
        }
    }

    @Override
    public void handlePassive(PlayerEntity player) {

        //debuff checker
        if (player.hasStatusEffect(ModEffects.PRAYER) && player.hasStatusEffect(StatusEffects.WEAKNESS)){
            player.removeStatusEffect(StatusEffects.WEAKNESS);
        }
        if (!player.hasStatusEffect(ModEffects.PRAYER) && !player.hasStatusEffect(StatusEffects.WEAKNESS)){
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, -1, 1, false, true, true));
        }


        //Cooldown management
        if (horseCooldown>0){
            horseCooldown--;
        }
        if (guardianAngelCooldown>0){
            guardianAngelCooldown--;
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


    private void killExistingPegasus(PlayerEntity player, ServerWorld world) {
        // check all entities
        for (PegasusEntity horse : world.getEntitiesByClass(PegasusEntity.class, player.getBoundingBox().expand(1000), entity -> true)) {
            if (horse!=null && horse.getOwnerUuid()!=null) {
                if (horse.getOwnerUuid().equals(player.getUuid())) {
                    horse.refreshPositionAndAngles(horse.getX(), -200.0, horse.getZ(), horse.getYaw(), horse.getPitch());
                }
            }
        }
    }

    private PegasusEntity summonPegasus(PlayerEntity player, ServerWorld world) {
        PegasusEntity horse = new PegasusEntity(ModEntities.PEGASUS, world);

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
            jumpAttribute.setBaseValue(1); // default is 0.7
        }

        // Spawn the horse in the world
        world.spawnEntity(horse);

        //Saddle it
        horse.saddle(new ItemStack(Items.SADDLE), SoundCategory.NEUTRAL);
        horse.setTame(true);
        //make the player the owner so that we can kill it later
        horse.setOwnerUuid(player.getUuid());

        return horse;
    }

}
