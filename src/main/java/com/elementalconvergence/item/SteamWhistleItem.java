package com.elementalconvergence.item;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.entity.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.elementalconvergence.magic.convergencehandlers.SteamMagicHandler.STEAM_INDEX;

public class SteamWhistleItem extends Item {
    private static final int EFFECT_RADIUS = 10; // radius for speed+ clean
    private static final int VORTEX_DURATION = 5*20; // vortex duration
    private static final float LAUNCH_VELOCITY = 1.75f; // upward velocity

    private static final int WHISTLE_COOLDOWN = 20*10; //10 seconds

    private final List<VortexInstance> activeVortices = new ArrayList<>();

    public SteamWhistleItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient) {
            IMagicDataSaver dataSaver = (IMagicDataSaver) user;
            MagicData magicData = dataSaver.getMagicData();
            int steamLevel = magicData.getMagicLevel(STEAM_INDEX);

            //lvl 3 ability for steam
            if (steamLevel >= 3 && magicData.getSelectedMagic()==STEAM_INDEX) {
                if (!user.getItemCooldownManager().isCoolingDown(this)) {

                    //TRAIN WHISTLE SOUND
                    world.playSound(
                            null,
                            user.getX(),
                            user.getY(),
                            user.getZ(),
                            ModEntities.TRAINWHISTLE_SOUND_EVENT,
                            SoundCategory.PLAYERS,
                            1.0f,
                            1.0f
                    );

                    // GIVING SPEED 2 FOR 1 MINUTE TO EVERYBODY CLOSE
                    Vec3d playerPos = user.getPos();
                    Box effectBox = new Box(
                            playerPos.x - EFFECT_RADIUS, playerPos.y - EFFECT_RADIUS, playerPos.z - EFFECT_RADIUS,
                            playerPos.x + EFFECT_RADIUS, playerPos.y + EFFECT_RADIUS, playerPos.z + EFFECT_RADIUS
                    );

                    List<PlayerEntity> nearbyPlayers = world.getEntitiesByClass(
                            PlayerEntity.class,
                            effectBox,
                            entity -> entity.isAlive()
                    );

                    for (PlayerEntity nearbyPlayer : nearbyPlayers) {

                        //CLEANSING NEGATIVE EFFECTS
                        cleanseNegativeEffects(nearbyPlayer);

                        // SPEED 2
                        nearbyPlayer.addStatusEffect(
                                new StatusEffectInstance(StatusEffects.SPEED, 20 * 60, 1, false, true)
                        );

                    }

                    // VORTEX ENTITY
                    BlockPos vortexPos = user.getBlockPos();
                    VortexInstance vortex = new VortexInstance(
                            UUID.randomUUID(),
                            world,
                            new Vec3d(vortexPos.getX() + 0.5, vortexPos.getY(), vortexPos.getZ() + 0.5),
                            VORTEX_DURATION
                    );
                    activeVortices.add(vortex);

                    // VORTEX PARTICLES
                    spawnVortexParticles((ServerWorld) world, vortex.position);

                    // COOLDOWN
                    user.getItemCooldownManager().set(this, WHISTLE_COOLDOWN);
                }
            }
        }
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (!world.isClient) {
            // updating the active vortexes
            List<VortexInstance> vortexToRemove = new ArrayList<>();

            for (VortexInstance vortex : activeVortices) {
                if (vortex.world.equals(world)) {
                    vortex.tick();

                    if (vortex.isDead()) {
                        vortexToRemove.add(vortex);
                    }
                }
            }

            activeVortices.removeAll(vortexToRemove);
        }
    }

    private void cleanseNegativeEffects(LivingEntity entity) {
        List<StatusEffectInstance> effectsToRemove = new ArrayList<>();

        for (StatusEffectInstance effect : entity.getStatusEffects()) {
            RegistryEntry<StatusEffect> statusEffect = effect.getEffectType();
            if (!statusEffect.value().getCategory().equals(StatusEffectCategory.BENEFICIAL)) {
                effectsToRemove.add(effect);
            }
        }

        for (StatusEffectInstance effectToRemove : effectsToRemove) {
            entity.removeStatusEffect(effectToRemove.getEffectType());
        }
    }

    private void spawnVortexParticles(ServerWorld world, Vec3d pos) {
        for (int i = 0; i < 10; i++) {
            double offsetX = world.random.nextGaussian() * 0.1;
            double offsetY = world.random.nextGaussian() * 0.1;
            double offsetZ = world.random.nextGaussian() * 0.1;

            world.spawnParticles(
                    ParticleTypes.FIREWORK,
                    pos.x, pos.y + 0.5, pos.z,
                    10,
                    offsetX, offsetY, offsetZ,
                    0.025
            );
        }
    }

    //VORTEX PRIVATE CLASS
    private class VortexInstance {
        private final UUID id;
        private final World world;
        private final Vec3d position;
        private int ticksLeft;
        private static final int EFFECT_RADIUS = 1; //vortex radius

        public VortexInstance(UUID id, World world, Vec3d position, int duration) {
            this.id = id;
            this.world = world;
            this.position = position;
            this.ticksLeft = duration;
        }

        public void tick() {
            ticksLeft--;

            if (ticksLeft % 5 == 0) { //spawn particles every 5 ticks
                if (world instanceof ServerWorld) {
                    spawnVortexParticles((ServerWorld) world, position);
                }

                // launching entities
                Box launchBox = new Box(
                        position.x - EFFECT_RADIUS, position.y, position.z - EFFECT_RADIUS,
                        position.x + EFFECT_RADIUS, position.y + 2, position.z + EFFECT_RADIUS
                );

                List<LivingEntity> entities = world.getEntitiesByClass(
                        LivingEntity.class,
                        launchBox,
                        entity -> entity.isAlive() && entity.isOnGround()
                );

                for (LivingEntity entity : entities) {
                    launchEntity(entity);
                }
            }
        }

        private void launchEntity(LivingEntity entity) {
            if (entity instanceof PlayerEntity player){
                IMagicDataSaver dataSaver = (IMagicDataSaver) player;
                MagicData magicData = dataSaver.getMagicData();
                if (magicData.getSelectedMagic()==STEAM_INDEX){
                    return;
                }
            }
            // vector launch
            Vec3d launchVec = new Vec3d(0, LAUNCH_VELOCITY, 0);

            // Changing velocity
            entity.setVelocity(launchVec);
            entity.velocityModified = true; //super duper important

            if (entity instanceof PlayerEntity player) {
                player.addStatusEffect(
                        new StatusEffectInstance(StatusEffects.SLOW_FALLING, 5 * 20, 0, false, false)
                );
            }

            world.playSound(
                    null,
                    entity.getBlockPos(),
                    SoundEvents.ENTITY_BREEZE_SHOOT,
                    SoundCategory.PLAYERS,
                    1.0F,
                    1.0F
            );
        }

        public boolean isDead() {
            return ticksLeft <= 0;
        }
    }
}