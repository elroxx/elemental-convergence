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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.component.type.PotionContentsComponent;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
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
    public static final float VAMPIRE_FLIGHT=1.0f; //normal flight
    public static final float VAMPIRE_SIZE=1.0f; //half size for height and width

    public static final float BAT_ATTACK=0.25f; //1.5* attack
    public static final float BAT_ATTACK_SPEED=1.5f; //1.5* attack speed
    public static final float BAT_SPEED=0.1f; //base speed (everything will be in flight speed)
    public static final float BAT_HEALTH=4.0f; //3hp like rat health
    public static final float BAT_FLIGHT=1.25f; //super quick flight
    public static final float BAT_SIZE=0.5f; //half size for height and width

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
        if (world.isDay() && world.isSkyVisible(player.getBlockPos()) && !isBeingRainedOn(player) && !player.hasStatusEffect(ModEffects.BAT_FORM)){
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

        if (player.hasStatusEffect(ModEffects.BAT_FORM)) {
            //set bat attributes
            if (!(Math.abs(playerHealth-BAT_HEALTH)<0.005f)){
                ScaleData playerAttack = ScaleTypes.ATTACK.getScaleData(player);
                ScaleData playerAttackSpeed = ScaleTypes.ATTACK_SPEED.getScaleData(player);
                ScaleData playerHeight = ScaleTypes.HEIGHT.getScaleData(player);
                ScaleData playerWidth = ScaleTypes.WIDTH.getScaleData(player);
                ScaleData playerFlight = ScaleTypes.FLIGHT.getScaleData(player);

                playerAttack.setScale(BAT_ATTACK);
                playerAttackSpeed.setScale(BAT_ATTACK_SPEED);
                playerHeight.setScale(BAT_SIZE);
                playerWidth.setScale(BAT_SIZE);
                playerFlight.setScale(BAT_FLIGHT);

                player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(BAT_HEALTH);
                player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(BAT_SPEED);
            }

            //also add flight
            if (!player.getAbilities().allowFlying) {
                player.getAbilities().allowFlying = true;
                ((ServerPlayerEntity) player).sendAbilitiesUpdate();
                player.getAbilities().flying = true;
                ((ServerPlayerEntity) player).sendAbilitiesUpdate();
            }

        } else {
            //set vampire attributes
            if (!(Math.abs(playerHealth-VAMPIRE_HEALTH)<0.005f)){
                ScaleData playerAttack = ScaleTypes.ATTACK.getScaleData(player);
                ScaleData playerAttackSpeed = ScaleTypes.ATTACK_SPEED.getScaleData(player);
                ScaleData playerHeight = ScaleTypes.HEIGHT.getScaleData(player);
                ScaleData playerWidth = ScaleTypes.WIDTH.getScaleData(player);
                ScaleData playerFlight = ScaleTypes.FLIGHT.getScaleData(player);

                playerAttack.setScale(VAMPIRE_ATTACK);
                playerAttackSpeed.setScale(VAMPIRE_ATTACK_SPEED);
                playerHeight.setScale(VAMPIRE_SIZE);
                playerWidth.setScale(VAMPIRE_SIZE);
                playerFlight.setScale(VAMPIRE_FLIGHT);

                player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(VAMPIRE_HEALTH);
                player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(VAMPIRE_SPEED);
            }

            //also reset flights

            //also add flight
            if (player.getAbilities().allowFlying) {
                player.getAbilities().allowFlying = false;
                ((ServerPlayerEntity) player).sendAbilitiesUpdate();
                player.getAbilities().flying = false;
                ((ServerPlayerEntity) player).sendAbilitiesUpdate();
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

    }

    @Override
    public void handleBlockBreak(PlayerEntity player, BlockPos pos, BlockState state, BlockEntity entity) {

    }

    @Override
    public void handlePrimarySpell(PlayerEntity player) {

        //maybe add skin transformation as well
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int bloodLevel = magicData.getMagicLevel(BLOOD_INDEX);
        if (bloodLevel>=2) {
            if (player.hasStatusEffect(ModEffects.BAT_FORM)) {
                player.removeStatusEffect(ModEffects.BAT_FORM);

                ((TailoredPlayer) player).fabrictailor_clearSkin();
            } else {
                player.addStatusEffect(new StatusEffectInstance(ModEffects.BAT_FORM, -1, 0,false, false, false));

                //also add skin
                TailoredPlayer tailoredPlayer = (TailoredPlayer) player;

                String skinValue = "ewogICJ0aW1lc3RhbXAiIDogMTc1NTg0MTEyMjIxMSwKICAicHJvZmlsZUlkIiA6ICJkYjQwYmNjNWUzMDE0ZmZjOGVlOWQxNDU5MTcyYjdhNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJhWGUxOCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zZmViZjcwODNlYjk4MGJkY2Q2NzAwYjAzODViNTc0OWQzYmYyYjlkYzY0ZWE2YWVhMDVlMDdkMDM4YzlkNWIyIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
                String skinSignature = "aqODDweVGRhJPlXMhBNRI/tfSNTpA5Z7m/FF/na6ZfiJ+8T9OEPqXiKa9a2WG9d+0i1nFFMOimNLCxlcMU3GEOErbV0QvzyJULyQ6TrtNi/LSorLd0F4vBpx+Vpg0WqzVkLMXmbM/x3Pe/2OUjsVKfdMzrzrv/uRvt7qZ8tbGR8vLYGEgqaWtPV0zD7xCpwU3xK/9QXcQIXcSeAfQNMq6okFp9DPg9SSuYjfkE80ysxCboiQqpSTxvAnlK5vgNTl1xTPTlfK8m/Mg4VABpakFfZmyaeJxMhjnzCr/ImiH8PLhBgZgzcCO35ZlxGzVVOlDU9Um9NoDzEIy7nqsRvkft2H1wlnjP0CzsjsMQB5Jn8QGHNPthnUT45A14m3HPbhbAgvSjEGwL18rkl3z67D96+2SMOrnPFO4uBMxJ2v2clIrQdCKRyQjbKW0gZe6lTsh99k4BT/8Bo/UIdU+n1jiTuvmzKmAyMMw0ksW+KbAcuLpl/1bfknaF700LLyFtHVYogfU0DRNEDW/dUXoeZFQUE9JrCPIZKrB3tg7XRs21nuDiGT8+KjbLoJegYEi3ytQ/RMJgkHNGRT0NstJ/YwusIdKOtwxvZjrowupgGxd6gIq9jQjw3aGHSUt9VRstqGQ0RLCkrVKFXLiVTfjmDFzwfOXtDpf7y4tZoXJ+i7hI4=";
                tailoredPlayer.fabrictailor_setSkin(skinValue, skinSignature, true);
            }
            //playsound
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_BAT_TAKEOFF, SoundCategory.PLAYERS, 1f, 1f);
        }
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
