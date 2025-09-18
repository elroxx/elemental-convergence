package com.elementalconvergence.effect;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Set;

import static com.elementalconvergence.world.dimension.ModDimensions.VOID_WORLD_KEY;

public class VoidSicknessEffect extends StatusEffect {
    public static final int DAMAGE_INTERVAL = 20 * 5; // every 5 sec
    private int damageCooldown = 0;

    public VoidSicknessEffect() {
        super(StatusEffectCategory.HARMFUL, 0x1a0033); //dark purple
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true; //always update
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getWorld().isClient()) {
            return true;
        }

        //tp in another dim if not
        if (!entity.getWorld().getRegistryKey().equals(VOID_WORLD_KEY)) {
            teleportToVoid(entity);
            damageCooldown = DAMAGE_INTERVAL;
        }

        // dmg over time
        if (entity.getWorld().getRegistryKey().equals(VOID_WORLD_KEY)) {
            if (damageCooldown <= 0) {
                float hpLeft = entity.getHealth() - 1.0f;
                if (hpLeft >= 2.0f) {
                    DamageSource withered = entity.getWorld().getDamageSources().wither();
                    entity.damage(withered, 1.0f);
                }
                damageCooldown = DAMAGE_INTERVAL;
            } else {
                damageCooldown--;
            }
        }
        return true;
    }


    private void teleportToVoid(LivingEntity entity) {
        if (entity.getWorld().getServer() == null || !(entity.getWorld() instanceof ServerWorld)) {
            return;
        }

        ServerWorld currentWorld = (ServerWorld) entity.getWorld();

        //particles
        currentWorld.spawnParticles(
                ParticleTypes.SQUID_INK,
                entity.getX() + 0.5,
                entity.getY() + 0.5,
                entity.getZ() + 0.5,
                10,
                0.25,
                0.25, //so that they rise a little
                0.25,
                0);

        //playsound
        entity.getWorld().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.ENTITY_SQUID_SQUIRT, SoundCategory.PLAYERS, 1.0F, 0.5F);

        // coordinates
        double voidPosX = entity.getPos().x / 4.0;
        double voidPosY = (entity.getPos().y - 64.0) / 4.0;
        double voidPosZ = entity.getPos().z / 4.0;

        // get world
        ServerWorld voidWorld = entity.getServer().getWorld(VOID_WORLD_KEY);
        if (voidWorld == null) {
            return;
        }

        //blackstone
        BlockPos platformPos = new BlockPos((int) Math.floor(voidPosX),
                (int) Math.floor(voidPosY) - 1,
                (int) Math.floor(voidPosZ));
        BlockState belowState = voidWorld.getBlockState(platformPos);
        if (belowState.isAir() || !belowState.hasSolidTopSurface(voidWorld, platformPos, entity)) {
            voidWorld.setBlockState(platformPos, Blocks.BLACKSTONE.getDefaultState());
        }

        //tp
        float yaw = entity.getYaw();
        float pitch = entity.getPitch();

        Set<PositionFlag> flags = EnumSet.of(PositionFlag.X_ROT, PositionFlag.Y_ROT);
        entity.teleport(voidWorld, voidPosX, voidPosY, voidPosZ, flags, yaw, pitch);

        //particles
        voidWorld.spawnParticles(
                ParticleTypes.SQUID_INK,
                voidPosX + 0.5,
                voidPosY + 0.5,
                voidPosZ + 0.5,
                10,
                0.25,
                0.25, //so that they rise a little
                0.25,
                0);
    }

    public static void teleportToOverworld(LivingEntity entity) {
        if (entity.getWorld().getServer() == null || !(entity.getWorld() instanceof ServerWorld)) {
            return;
        }

        ServerWorld currentWorld = (ServerWorld) entity.getWorld();

        // spawn particles
        currentWorld.spawnParticles(
                ParticleTypes.SQUID_INK,
                entity.getX() + 0.5,
                entity.getY() + 0.5,
                entity.getZ() + 0.5,
                10,
                0.25,
                0.25, //so that they rise a little
                0.25,
                0);

        //get overworld coordinates
        double owPosX = entity.getPos().x * 4.0;
        double owPosY = (entity.getPos().y * 4.0) + 64.0;
        double owPosZ = entity.getPos().z * 4.0;

        //overworld
        ServerWorld overworldWorld = entity.getServer().getWorld(World.OVERWORLD);
        if (overworldWorld == null) {
            return;
        }

        //clear blocks
        BlockPos feetPos = new BlockPos((int) Math.floor(owPosX), (int) Math.floor(owPosY), (int) Math.floor(owPosZ));
        BlockPos headPos = new BlockPos((int) Math.floor(owPosX), (int) Math.floor(owPosY) + 1, (int) Math.floor(owPosZ));
        BlockPos overheadPos = new BlockPos((int) Math.floor(owPosX), (int) Math.floor(owPosY) + 2, (int) Math.floor(owPosZ));

        // drop blocks
        dropBlockAndSetAir(overworldWorld, feetPos);
        dropBlockAndSetAir(overworldWorld, headPos);
        dropBlockAndSetAir(overworldWorld, overheadPos);

        // glass plat
        BlockPos platformPos = new BlockPos((int) Math.floor(owPosX), (int) Math.floor(owPosY) - 1, (int) Math.floor(owPosZ));
        BlockState belowState = overworldWorld.getBlockState(platformPos);
        if (belowState.isAir() || !belowState.hasSolidTopSurface(overworldWorld, platformPos, entity)) {
            dropBlockAndSetAir(overworldWorld, platformPos);
            overworldWorld.setBlockState(platformPos, Blocks.BLACK_STAINED_GLASS.getDefaultState());
        }

        // Teleport entity
        float yaw = entity.getYaw();
        float pitch = entity.getPitch();

        Set<PositionFlag> flags = EnumSet.of(PositionFlag.X_ROT, PositionFlag.Y_ROT);
        entity.teleport(overworldWorld, owPosX, owPosY, owPosZ, flags, yaw, pitch);

        //playsound + particles
        overworldWorld.playSound(null, owPosX, owPosY, owPosZ,
                SoundEvents.ENTITY_SQUID_SQUIRT, SoundCategory.PLAYERS, 1.0F, 0.5F);

        overworldWorld.spawnParticles(
                ParticleTypes.SQUID_INK,
                owPosX + 0.5,
                owPosY + 0.5,
                owPosZ + 0.5,
                10,
                0.25,
                0.25, //so that they rise a little
                0.25,
                0);
    }

    private static void dropBlockAndSetAir(ServerWorld world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!state.isAir()) {
            state.getBlock().dropStacks(state, world, pos);
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }
}