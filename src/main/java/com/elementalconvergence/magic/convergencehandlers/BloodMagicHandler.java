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
import net.minecraft.entity.damage.DamageSource;
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
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.*;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class BloodMagicHandler implements IMagicHandler {
    public static final int BLOOD_INDEX= (BASE_MAGIC_ID.length-1)+6;

    public static final int DEFAULT_SKYLIGHTHURT_COOLDOWN = 10;
    private int skylightHurtCooldown=0;

    public static final float VAMPIRE_ATTACK=1.5f; //1.5* attack
    public static final float VAMPIRE_ATTACK_SPEED=1.5f; //1.5* attack speed
    public static final float VAMPIRE_SPEED=0.15f; //1.5* speed
    public static final float VAMPIRE_HEALTH=24.0f; //1.2* health

    @Override
    public void handleItemRightClick(PlayerEntity player) {

    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {
    }

    @Override
    public void handlePassive(PlayerEntity player) {


        //DEBUFF - Getting hurt by skylight
        ServerWorld world = (ServerWorld) player.getWorld();

        //Maybe add a rain verif check
        if (world.isDay() && world.isSkyVisible(player.getBlockPos()) && !isBeingRainedOn(player)){
            if (skylightHurtCooldown==0) {
                DamageSource withered = player.getWorld().getDamageSources().wither();
                player.damage(withered, 4);
                skylightHurtCooldown=DEFAULT_SKYLIGHTHURT_COOLDOWN;

                world.playSound(null, player.getBlockPos(), SoundEvents.ENTITY_PLAYER_HURT_ON_FIRE,
                        SoundCategory.PLAYERS, 0.5f, 1.0f);
            }
            else{
                skylightHurtCooldown--;
            }
        }
        else{
            skylightHurtCooldown=DEFAULT_SKYLIGHTHURT_COOLDOWN;
        }


        //Buff part
        //night vision
        if (!player.hasStatusEffect(StatusEffects.NIGHT_VISION)){
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, -1, 0, false, false, false));
        }

        double playerHealth = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).getBaseValue();
        if (!(Math.abs(playerHealth-VAMPIRE_HEALTH)<0.005f)){
            ScaleData playerAttack = ScaleTypes.ATTACK.getScaleData(player);
            ScaleData playerAttackSpeed = ScaleTypes.ATTACK_SPEED.getScaleData(player);

            playerAttack.setScale(VAMPIRE_ATTACK);
            playerAttackSpeed.setScale(VAMPIRE_ATTACK_SPEED);

            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(VAMPIRE_HEALTH);
            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(VAMPIRE_SPEED);
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

    private boolean isBeingRainedOn(PlayerEntity player) {
        BlockPos blockPos = player.getBlockPos();
        return player.getWorld().hasRain(blockPos) || player.getWorld().hasRain(BlockPos.ofFloored((double)blockPos.getX(), player.getBoundingBox().maxY, (double)blockPos.getZ()));
    }

}
