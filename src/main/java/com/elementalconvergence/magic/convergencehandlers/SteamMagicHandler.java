package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.*;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class SteamMagicHandler implements IMagicHandler {
    public static final int STEAM_INDEX= (BASE_MAGIC_ID.length-1)+3; //this is 10

    private Vec3d lastPosition =null;
    private int horizontalBlocksMoved = 0;
    public int HORIZONTAL_BLOCK_COUNTER_DEFAULT=10;

    public static final int FLOAT_PARTICLE_DEFAULT_COOLDOWN=10; //1/2 seconds
    private boolean floatToggle = false;
    private int floatParticleCooldown=0;

    public static final float BASE_SCALE = 1.0f;

    public static final float FLOAT_SCALE = 0.1f;
    public static final float FLOAT_HELD = 0f;

    private boolean auraToggle = false;
    private static final int AURA_TICK_INTERVAL = 15; // 3/4 sec (was 1/2 sec)
    private static final float BASE_DAMAGE = 1.0f; // base dmg (half heart)
    private static final float MAX_DAMAGE_MULTIPLIER = 5.0f; // 2.5 hearts per tick (when full health)
    private static final float MIN_DAMAGE_MULTIPLIER = 1.0f; // min dmg multiplier (so when low health)

    private static final float MIN_RADIUS = 1.0f; // min radius in blocks
    private static final float MAX_RADIUS = 10.0f; // max radius in blocks

    private static final float DAMAGE_HEALTH_THRESHOLD = 0.1f; // health percent where dmg stops scaling //was 0.15f
    private static final float RADIUS_HEALTH_THRESHOLD = 0.1f; // health percent where radius stops scaling //was 0.15f
    private static final int PARTICLE_DENSITY = 31; // particles per circles (multiple of pi i have decided)



    @Override
    public void handleItemRightClick(PlayerEntity player) {
    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {

        //BUFF (invulnerable except to negative valued explosion, the void and /kill)
        if (!player.hasStatusEffect(StatusEffects.RESISTANCE)){
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,-1, 9, false, false, false));
        }

        //Debuff (also no natural health regen)
        if (lastPosition==null){
            lastPosition=player.getPos();
        }
        Vec3d currentPos = player.getPos();

        // horizontal distance (no y axis)
        double dx = currentPos.x - lastPosition.x;
        double dz = currentPos.z - lastPosition.z;
        double horizontalDistanceMoved = Math.sqrt(dx * dx + dz * dz);

        //floatToggle is to avoid dealing dmg when in float mode but without giving opportunity to reset block counter
        if (horizontalDistanceMoved >= 1.0 && !floatToggle) {
            this.horizontalBlocksMoved++;
            lastPosition=currentPos;
        }


        //DEALING DMG DEBUFF
        if (horizontalBlocksMoved>=HORIZONTAL_BLOCK_COUNTER_DEFAULT){
            //1hp of dmg
            if (!player.isCreative()) {
                player.damage(player.getDamageSources().outOfWorld(), 1.0F);
            }
            horizontalBlocksMoved=0;
        }

        //LVL 2 particles only
        if (floatToggle && floatParticleCooldown==0){
            int particleCount=10;
            double particleRange=1.0;
            double particleSpread=0.5;
            Vec3d playerPos = player.getPos();

            for (int i = 0; i < particleCount; i++) {
                double distance = player.getRandom().nextDouble() * particleRange;
                double offsetX = (player.getRandom().nextDouble() - 0.5) * 2 * particleSpread * (distance / particleRange);
                double offsetY = (player.getRandom().nextDouble() - 0.5) * 2 * particleSpread * (distance / particleRange);
                double offsetZ = (player.getRandom().nextDouble() - 0.5) * 2 * particleSpread * (distance / particleRange);

                Vec3d particlePos = playerPos.add(
                        offsetX,
                        offsetY-0.5,
                        offsetZ
                );


                ((ServerWorld)player.getWorld()).spawnParticles(
                        ParticleTypes.WHITE_SMOKE,
                        particlePos.x, particlePos.y, particlePos.z,
                        1,
                        0.0, 0.0, 0.0,
                        0.0
                );
            }
            floatParticleCooldown=FLOAT_PARTICLE_DEFAULT_COOLDOWN;
        }


        // LVL 1 DMG AURA
        if (auraToggle && player.getWorld().getTime() % AURA_TICK_INTERVAL == 0) {
            //get time is so that its not managed by my own counter (could have been like this for the others, but i didnt know it existed)
            processAura(player, (ServerWorld) player.getWorld());
        }


        //COOLDOWNS

        if (floatToggle&& floatParticleCooldown>0){
            floatParticleCooldown--;
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

    //secondary and primary are not in the same order because lvl 3 planned became level 1 planned and it was more satisfying order wise
    @Override
    public void handleSecondarySpell(PlayerEntity player) {
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int steamLevel = magicData.getMagicLevel(STEAM_INDEX);

        //lvl 2 ability
        if (steamLevel>=2){
            floatToggle=!floatToggle;
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5F, 0.8F);
        }

        //LVL 2 ability when toggled (floatToggle can only be true if already lvl 2)
        if (floatToggle){

            //ADDING STATUS EFFECTS
            if (player.hasStatusEffect(StatusEffects.LEVITATION) && player.getStatusEffect(StatusEffects.LEVITATION).getAmplifier()<1){
                player.removeStatusEffect(StatusEffects.LEVITATION);
            }
            else if (!player.hasStatusEffect(StatusEffects.LEVITATION)){
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, -1, 1, false, false, false));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, -1, 1, false, false, false));
            }

            //SCALE MODIFICATION
            ScaleData playerHeight = ScaleTypes.HEIGHT.getScaleData(player);
            ScaleData playerWidth = ScaleTypes.WIDTH.getScaleData(player);
            ScaleData playerReach = ScaleTypes.BLOCK_REACH.getScaleData(player);
            ScaleData playerEntityReach = ScaleTypes.ENTITY_REACH.getScaleData(player);
            ScaleData playerHeldItem = ScaleTypes.HELD_ITEM.getScaleData(player);

            if (!(Math.abs(playerHeight.getScale()-FLOAT_SCALE)<0.05f)) {
                playerHeight.setScale(FLOAT_SCALE);
                playerWidth.setScale(FLOAT_SCALE);
                playerReach.setScale(FLOAT_HELD);
                playerEntityReach.setScale(FLOAT_HELD);
                playerHeldItem.setScale(FLOAT_HELD);
            }

        }



        else {
            //REMOVING STATUS EFFECTS
            if (player.hasStatusEffect(StatusEffects.LEVITATION) && player.getStatusEffect(StatusEffects.LEVITATION).getAmplifier()>=1){
                player.removeStatusEffect(StatusEffects.LEVITATION);
                player.removeStatusEffect(StatusEffects.INVISIBILITY);
            }

            //SCALE MODIFICATION
            ScaleData playerHeight = ScaleTypes.HEIGHT.getScaleData(player);
            ScaleData playerWidth = ScaleTypes.WIDTH.getScaleData(player);
            ScaleData playerReach = ScaleTypes.BLOCK_REACH.getScaleData(player);
            ScaleData playerEntityReach = ScaleTypes.ENTITY_REACH.getScaleData(player);
            ScaleData playerHeldItem = ScaleTypes.HELD_ITEM.getScaleData(player);

            if (!(Math.abs(playerHeight.getScale()-BASE_SCALE)<0.05f)) {
                playerHeight.setScale(BASE_SCALE);
                playerWidth.setScale(BASE_SCALE);
                playerReach.setScale(BASE_SCALE);
                playerEntityReach.setScale(BASE_SCALE);
                playerHeldItem.setScale(BASE_SCALE);
            }

        }
    }

    @Override
    public void handlePrimarySpell(PlayerEntity player) {
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int steamLevel = magicData.getMagicLevel(STEAM_INDEX);

        //lvl 1 ability
        if (steamLevel >= 1) {
            auraToggle = !auraToggle;

            if (auraToggle) {
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_BLAZE_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F);
            } else {
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.PLAYERS, 0.8F, 1.0F);
            }
        }
    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

    private void processAura(PlayerEntity player, ServerWorld world) {
        float radius = calculateAuraRadius(player);
        float damageAmount = calculateAuraDamage(player);

        // box
        Vec3d playerPos = player.getPos();
        Box auraBox = new Box(
                playerPos.x - radius, playerPos.y - radius, playerPos.z - radius,
                playerPos.x + radius, playerPos.y + radius, playerPos.z + radius
        );

        world.getEntitiesByClass(
                LivingEntity.class,
                auraBox,
                entity -> entity != player && entity.isAlive()
        ).forEach(target -> {
            // deal dmg (ITS FIRE SO FIRE RESISTANCE PROBABLY HELPS???)
            target.damage(world.getDamageSources().onFire(), damageAmount);

            // feedback for dmg (burning)
            world.spawnParticles(
                    ParticleTypes.FLAME,
                    target.getX(), target.getY() + target.getHeight() / 2, target.getZ(),
                    5,
                    0.2, 0.2, 0.2,
                    0.05
            );
        });

        // edge particles
        spawnAuraCircles(world, player, radius);
    }


    private void spawnAuraCircles(ServerWorld world, PlayerEntity player, float radius) {
        Vec3d playerPos = player.getPos().add(0, 1.0, 0); // slight y offset in center so centered around waist

        // Y plane
        for (int i = 0; i < PARTICLE_DENSITY; i++) {
            double angle = (2 * Math.PI * i) / PARTICLE_DENSITY;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            world.spawnParticles(
                    ParticleTypes.WHITE_SMOKE,
                    playerPos.x + x, playerPos.y, playerPos.z + z,
                    1,
                    0, 0, 0,
                    0.01
            );
        }

        // Z plane
        for (int i = 0; i < PARTICLE_DENSITY; i++) {
            double angle = (2 * Math.PI * i) / PARTICLE_DENSITY;
            double x = radius * Math.cos(angle);
            double y = radius * Math.sin(angle);

            world.spawnParticles(
                    ParticleTypes.FLAME,
                    playerPos.x + x, playerPos.y + y, playerPos.z,
                    1,
                    0, 0, 0,
                    0.01
            );
        }

        // X-plane
        for (int i = 0; i < PARTICLE_DENSITY*3; i++) {
            double angle = (2 * Math.PI * i) / PARTICLE_DENSITY;
            double y = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            world.spawnParticles(
                    ParticleTypes.SPLASH,
                    playerPos.x, playerPos.y + y, playerPos.z + z,
                    1,
                    0, 0, 0,
                    0.01
            );
        }
    }

    private float calculateAuraRadius(PlayerEntity player) {
        float healthPercent = player.getHealth() / player.getMaxHealth();

        // Clamp health percentage to threshold to prevent extreme values
        healthPercent = Math.max(healthPercent, RADIUS_HEALTH_THRESHOLD);

        // linear scaling RADIUS IS INVERSE PROPORTIONNAL TO HEALTH
        return MIN_RADIUS + (MAX_RADIUS - MIN_RADIUS) * (1.0f - healthPercent) / (1.0f - RADIUS_HEALTH_THRESHOLD);
    }

    private float calculateAuraDamage(PlayerEntity player) {
        float healthPercent = player.getHealth() / player.getMaxHealth();

        // to avoid weird division by small number, but its not supposed to happen in already clipped minecraft health ig
        healthPercent = Math.max(healthPercent, DAMAGE_HEALTH_THRESHOLD);

        //Linear scaling DMG IS PROPORTIONAL TO HEALTH
        float damageMultiplier = MIN_DAMAGE_MULTIPLIER +
                (MAX_DAMAGE_MULTIPLIER - MIN_DAMAGE_MULTIPLIER) *
                        (healthPercent - DAMAGE_HEALTH_THRESHOLD) /
                        (1.0f - DAMAGE_HEALTH_THRESHOLD);

        return BASE_DAMAGE * damageMultiplier;
    }



}
