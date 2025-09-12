package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.enchantment.ModEnchantments;
import com.elementalconvergence.entity.ModEntities;
import com.elementalconvergence.entity.PegasusEntity;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
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
import static com.elementalconvergence.magic.MagicRegistry.resetPlayerSkin;

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

    //Bloodsucking
    private BloodSuckSession activeSession = null;
    private int bloodSuckTicks = 0;
    private int immunityTicks = 0;

    @Override
    public void handleItemRightClick(PlayerEntity player) {

    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

        //Check if good lvl (lvl 3 coz somewhat powerful)

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int bloodLevel = magicData.getMagicLevel(BLOOD_INDEX);

        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        RegistryEntry<Enchantment> carrierEntry = player.getWorld().getRegistryManager().getWrapperOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(ModEnchantments.CARRIER);
        int carrierLevel = EnchantmentHelper.getLevel(carrierEntry, helmet);

        if (!helmet.isEmpty() && carrierLevel>1){
            return; //stop if carrierLevel is 1 just because i have no clue what will actually happen like that.
        }

        if (bloodLevel>=3) {

            //Check if sneaking+mainhand empty
            if (player.isSneaking() && player.getMainHandStack().isEmpty()) {
                //cant autosuck yourself, nor suck a nonliving entity or if you are already bloodsucking
                if (targetEntity instanceof LivingEntity target && !target.equals(player) && activeSession == null) {
                    // get bloodsucking going
                    startBloodSuck(player, target);
                }
            }
        }

    }

    @Override
    public void handlePassive(PlayerEntity player) {

        //manage bloodsucking
        if (activeSession != null) {
            handleBloodSuckingTick(player);
        }


        //DEBUFF - Getting hurt by skylight
        ServerWorld world = (ServerWorld) player.getWorld();

        //Maybe add a rain verif check
        if (world.isDay() && world.isSkyVisible(player.getBlockPos()) && !isBeingRainedOn(player) && !player.hasStatusEffect(ModEffects.BAT_FORM) && !player.isCreative()){
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
            if (player.getAbilities().allowFlying && !player.isCreative()) {
                player.getAbilities().allowFlying = false;
                ((ServerPlayerEntity) player).sendAbilitiesUpdate();
                player.getAbilities().flying = false;
                ((ServerPlayerEntity) player).sendAbilitiesUpdate();
            }
        }

    }

    @Override
    public void handleAttack(PlayerEntity player, Entity victim) {

        //cancel bloodsuck if attack
        if (activeSession != null) {
            endBloodSuck(player, true);
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

        //maybe add skin transformation as well
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int bloodLevel = magicData.getMagicLevel(BLOOD_INDEX);
        if (bloodLevel>=2) {
            if (player.hasStatusEffect(ModEffects.BAT_FORM)) {
                player.removeStatusEffect(ModEffects.BAT_FORM);

                //((TailoredPlayer) player).fabrictailor_clearSkin();
                resetPlayerSkin(player);
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

    private void startBloodSuck(PlayerEntity player, LivingEntity target) {
        //stop momentum
        player.setVelocity(Vec3d.ZERO);
        target.setVelocity(Vec3d.ZERO);
        player.velocityModified=true;
        target.velocityModified=true;

        //bloodsuck session (positions will be set after immunity period)
        activeSession = new BloodSuckSession(target);
        bloodSuckTicks = 0;
        immunityTicks = 0; // Start immunity period

        //unable to move (players can still jump, but i mean its easy to hit as well)
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, Integer.MAX_VALUE, 255, false, false));
        //target.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, Integer.MAX_VALUE, -10, false, false));
        //cant stop the jump anyway


        //start sound
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PHANTOM_BITE, SoundCategory.PLAYERS, 1f, 1.25f);
    }

    private void handleBloodSuckingTick(PlayerEntity player) {
        if (activeSession == null) return;

        LivingEntity target = activeSession.target;

        if (immunityTicks < 10) {
            immunityTicks++;

            // get pos at end
            if (immunityTicks == 10) {
                activeSession.setPositions(player.getPos(), target.getPos());
            }
        }

        // if session interrupted
        if (shouldInterruptSession(player, activeSession)) {
            endBloodSuck(player, false); //NOW NO MATTER WHAT THE STRENGTH COMES THROUGH, MAYBE CHANGE IT' IDK YET
            return;
        }

        bloodSuckTicks++;

        // draining
        if (bloodSuckTicks >= 10) {
            // dmg
            float damage = 1.0f;
            target.damage(target.getDamageSources().magic(), damage);

            // health
            player.heal(damage);
            activeSession.totalHpDrained += damage;

            // reset tick counter
            bloodSuckTicks = 0;

            //blood suck sound
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_HONEY_BOTTLE_DRINK, SoundCategory.PLAYERS, 0.7f, 1.0f);

            // if dead we stop
            if (target.isDead() || target.getHealth() <= 0) {
                endBloodSuck(player, false);
                return;
            }
        }
    }

    private boolean shouldInterruptSession(PlayerEntity player, BloodSuckSession session) {
        LivingEntity target = session.target;

        // no interrupt if no pos or still in immune frames
        if (immunityTicks < 10 || !session.hasPositions()) {
            return false;
        }

        // check if player mvoed
        if (player.getPos().distanceTo(session.playerInitialPos) > 0.1) {
            return true;
        }

        // check if target moved
        if (target.getPos().distanceTo(session.targetInitialPos) > 0.1) {
            return true;
        }

        // check if player dmgd
        if (player.hurtTime > 0) {
            return true;
        }

        // check if target dead
        if (target.isRemoved() || target.isDead()) {
            return true;
        }

        return false;
    }

    private void endBloodSuck(PlayerEntity player, boolean interrupted) {
        if (activeSession == null) return;

        LivingEntity target = activeSession.target;

        // remove movement restrictions from target
        target.removeStatusEffect(StatusEffects.SLOWNESS);
        target.removeStatusEffect(StatusEffects.JUMP_BOOST);

        // apply strength
        if (!interrupted && activeSession.totalHpDrained > 0) {
            int strengthLevel = Math.min((int) activeSession.totalHpDrained, 255); // need to cap at max lvl amplifier
            strengthLevel = strengthLevel/2; // (still will half the amount of strength gotten
            int duration = 20*60*2; // 2 mins

            StatusEffectInstance strengthEffect = new StatusEffectInstance(
                    StatusEffects.STRENGTH,
                    duration,
                    strengthLevel - 1,
                    false,
                    true
            );

            player.addStatusEffect(strengthEffect);


            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_SKELETON_CONVERTED_TO_STRAY, SoundCategory.PLAYERS, 0.8f, 0.75f);
        } else {

            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_MOOSHROOM_MILK, SoundCategory.PLAYERS, 0.8f, 0.25f);
        }

        //clean
        activeSession = null;
        bloodSuckTicks = 0;
        immunityTicks = 0;
    }

    //Inner class only for bloodsuck datastructure
    private static class BloodSuckSession {
        final LivingEntity target;
        Vec3d playerInitialPos = null;
        Vec3d targetInitialPos = null;
        float totalHpDrained = 0f;

        BloodSuckSession(LivingEntity target) {
            this.target = target;
        }

        public void setPositions(Vec3d playerPos, Vec3d targetPos) {
            this.playerInitialPos = playerPos;
            this.targetInitialPos = targetPos;
        }

        public boolean hasPositions() {
            return playerInitialPos != null && targetInitialPos != null;
        }
    }

}