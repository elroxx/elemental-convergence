package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.enchantment.ModEnchantments;
import com.elementalconvergence.entity.MinionSlimeEntity;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.IMagicHandler;
import com.elementalconvergence.networking.TaskScheduler;
import com.terraformersmc.modmenu.util.mod.Mod;
import gravity_changer.api.GravityChangerAPI;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.Set;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class SlimeMagicHandler implements IMagicHandler {
    public static final int SLIME_INDEX= (BASE_MAGIC_ID.length-1)+9;

    public static final int LEAP_DEFAULT_COOLDOWN = 30; //1.5 seconds
    private int leapCooldown=0;
    public static final float SLIME_LEAP_STRENGTH = 3.0f;

    public static final int REGAIN_SIZE_FROM_SPLIT_TIMER_MAX=2000; //2:05 minutes 2500
    private int regainSizeTimer=0;
    private float sizeBeforeSplit = 1.0f;

    public static final float BASE_SIZE = 1.0f;
    public static final float SIZE_INCREASE_PER_HEART = 0.25f;
    public static final float BASE_HEALTH = 20.0f;
    private final int SIZE_INCREMENT_INTERVAL = 50;

    private DissolvingSession activeSession = null;
    private int dissolvingTicks = 0;
    private int immunityTicks = 0;


    @Override
    public void handleItemRightClick(PlayerEntity player) {

    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {
        ItemStack mainHand = player.getMainHandStack();
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int slimeLevel = magicData.getMagicLevel(SLIME_INDEX);

        if (slimeLevel >= 3 && mainHand.isOf(ModItems.DISSOLVING_SLIME)) {
            //can't dissolve yourself, nor dissolve a nonliving entity or if you are already dissolving
            if (targetEntity instanceof LivingEntity target && !target.equals(player) && activeSession == null) {
                //start dissolving session
                startDissolving(player, target);
            }
        }
    }

    @Override
    public void handlePassive(PlayerEntity player) {
        //manage dissolving
        if (activeSession != null) {
            handleDissolvingTick(player);
        }

        //passive bouncy effect
        if (!player.hasStatusEffect(ModEffects.BOUNCY)){
            player.addStatusEffect(new StatusEffectInstance(ModEffects.BOUNCY, -1, 0, false, false, false));
        }

        //Health based on size
        ScaleData playerHeight = ScaleTypes.HEIGHT.getScaleData(player);
        float currentSize = playerHeight.getScale();
        float expectedHealth = BASE_HEALTH * currentSize;

        double playerHealth = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).getBaseValue();
        if (!(Math.abs(playerHealth - expectedHealth) < 0.5f)) {
            player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(expectedHealth);
        }

        //wall bounce
        Vec3d velocity = player.getVelocity();
        //x bounce
        if (Math.abs(velocity.x) > 0.05){
            BlockPos pos = player.getBlockPos();
            World world = player.getWorld();
            if (world.getBlockState(pos.offset(getHorizontalDirectionFromVelocity(velocity, true))).isSolidBlock(world, pos)) {
                player.setVelocity(-velocity.x * 0.9, velocity.y, velocity.z);
                player.velocityModified = true;
            }
        }
        //z bounce
        if (Math.abs(velocity.z) > 0.05){
            BlockPos pos = player.getBlockPos();
            World world = player.getWorld();
            if (world.getBlockState(pos.offset(getHorizontalDirectionFromVelocity(velocity, false))).isSolidBlock(world, pos)) {
                player.setVelocity(velocity.x, velocity.y, -velocity.z * 0.9);
                player.velocityModified = true;
            }
        }

        //cooldowns
        if (leapCooldown>0){
            leapCooldown--;
        }

        if (regainSizeTimer > 0){
            regainSizeTimer--;

            if (regainSizeTimer == 0){
                ScaleData playerHeight2 = ScaleTypes.HEIGHT.getScaleData(player);
                ScaleData playerWidth = ScaleTypes.WIDTH.getScaleData(player);

                playerHeight2.setScale(BASE_SIZE);
                playerWidth.setScale(BASE_SIZE);
                player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_SNIFFER_EGG_PLOP, SoundCategory.PLAYERS, 1.0F, 1.5F);
            }
            else if (regainSizeTimer % SIZE_INCREMENT_INTERVAL == 0){
                // how much of timer has elapsed
                int elapsedTime = REGAIN_SIZE_FROM_SPLIT_TIMER_MAX - regainSizeTimer;
                float progress = (float) elapsedTime / REGAIN_SIZE_FROM_SPLIT_TIMER_MAX;

                //linear interpolation towards normal size again
                float currentSize2 = sizeBeforeSplit + (BASE_SIZE - sizeBeforeSplit) * progress;

                ScaleData playerHeight2 = ScaleTypes.HEIGHT.getScaleData(player);
                ScaleData playerWidth = ScaleTypes.WIDTH.getScaleData(player);

                playerHeight2.setScale(currentSize2);
                playerWidth.setScale(currentSize2);
            }
        }
    }

    @Override
    public void handleAttack(PlayerEntity player, Entity victim) {
        //cancel dissolving if attack
        if (activeSession != null) {
            endDissolving(player, true);
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
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int slimeLevel = magicData.getMagicLevel(SLIME_INDEX);

        if (slimeLevel >= 1 && regainSizeTimer == 0){
            ServerWorld world = (ServerWorld) player.getWorld();

            //get current player size
            ScaleData playerHeight = ScaleTypes.HEIGHT.getScaleData(player);
            ScaleData playerWidth = ScaleTypes.WIDTH.getScaleData(player);
            float currentSize = playerHeight.getScale();

            // new size after split
            float newSize = currentSize / 2.0f;
            playerHeight.setScale(newSize);
            playerWidth.setScale(newSize);

            //spawn minions
            for (int i=0; i<2; i++) {
                // slime minion
                MinionSlimeEntity slimeMinion = new MinionSlimeEntity(EntityType.SLIME, world);

                //minion pos
                Vec3d playerPos = player.getPos();
                slimeMinion.setPos(playerPos.x, playerPos.y, playerPos.z);

                //change minion size
                float slimeMinionSize = 5 * newSize;
                slimeMinion.setMinionSize(slimeMinionSize);

                // new owner
                slimeMinion.setOwner(player);

                // spawned
                world.spawnEntity(slimeMinion);
            }

            // playsound+particles
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.BLOCK_SLIME_BLOCK_STEP, SoundCategory.PLAYERS, 1.0F, 0.8F);
            ((ServerWorld) player.getWorld()).spawnParticles(ParticleTypes.ITEM_SLIME, player.getX(), player.getY() + 1, player.getZ(), 20, 0.5, 0.5, 0.5, 0.1);

            // only start cooldown if below base_size
            if (newSize < BASE_SIZE) {
                regainSizeTimer = REGAIN_SIZE_FROM_SPLIT_TIMER_MAX;
                sizeBeforeSplit = newSize; //store the size that was before for scaling up computations
            }
        }
    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int slimeLevel = magicData.getMagicLevel(SLIME_INDEX);
        //so the player is not in elytra
        if (slimeLevel>=2 && leapCooldown==0 && !player.isFallFlying()) {

            Vec3d lookDir = player.getRotationVec(1.0f);
            Vec3d leapVelocityHorizontalPlane = new Vec3d(
                    lookDir.x * SLIME_LEAP_STRENGTH,
                    0,
                    lookDir.z * SLIME_LEAP_STRENGTH
            );

            // ADDING the horizontal velocity while resetting fully the vertical one
            Vec3d horizontalVelocity = player.getVelocity().add(leapVelocityHorizontalPlane);
            Vec3d finalVelocity = new Vec3d(
                    horizontalVelocity.getX(),
                    lookDir.y * SLIME_LEAP_STRENGTH,
                    horizontalVelocity.getZ()
            );

            player.setVelocity(finalVelocity);
            player.velocityModified=true; //IMPORTANT COZ IF NOT THIS DOESNT CHANGE THE PLAYERS VELOCITY AT ALL

            // playsound+particles
            player.getWorld().playSound(null, player.getBlockPos(), SoundEvents.ENTITY_SLIME_JUMP, SoundCategory.PLAYERS, 1.0F, 1.0F);
            ((ServerWorld) player.getWorld()).spawnParticles(ParticleTypes.ITEM_SLIME, player.getX(), player.getY(), player.getZ(), 30, 0.5, 0.5, 0.5, 0.1);

            //cooldown
            leapCooldown=LEAP_DEFAULT_COOLDOWN;
        }
    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

    private void startDissolving(PlayerEntity player, LivingEntity target) {
        //stop momentum
        player.setVelocity(Vec3d.ZERO);
        target.setVelocity(Vec3d.ZERO);
        player.velocityModified=true;
        target.velocityModified=true;

        //dissolving session (positions will be set after immunity period)
        activeSession = new DissolvingSession(target);
        dissolvingTicks = 0;
        immunityTicks = 0; // Start immunity period

        //unable to move (players can still jump, but i mean its easy to hit as well)
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, Integer.MAX_VALUE, 255, false, false));

        //start sound
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLOCK_SLIME_BLOCK_STEP, SoundCategory.PLAYERS, 1f, 0.8f);
    }

    private void handleDissolvingTick(PlayerEntity player) {
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
            endDissolving(player, false);
            return;
        }

        dissolvingTicks++;

        // dissolving
        if (dissolvingTicks >= 10) {
            // dmg
            float damage = 1.0f;
            target.damage(target.getDamageSources().magic(), damage);

            // feed player instead of healing
            player.getHungerManager().add((int)(damage), 0.6f); // half heart damage = half food
            activeSession.totalHpDrained += damage;

            // reset tick counter
            dissolvingTicks = 0;

            //dissolving sound
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_SLIME_BLOCK_HIT, SoundCategory.PLAYERS, 0.7f, 1.2f);

            // if dead we stop
            if (target.isDead() || target.getHealth() <= 0) {
                endDissolving(player, false);
                return;
            }
        }
    }

    private boolean shouldInterruptSession(PlayerEntity player, DissolvingSession session) {
        LivingEntity target = session.target;

        // no interrupt if no pos or still in immune frames
        if (immunityTicks < 10 || !session.hasPositions()) {
            return false;
        }

        // check if player moved
        if (player.getPos().distanceTo(session.playerInitialPos) > 0.1) {
            return true;
        }

        // check if target moved
        if (target.getPos().distanceTo(session.targetInitialPos) > 0.1) {
            return true;
        }

        // check if player damaged
        if (player.hurtTime > 0) {
            return true;
        }

        // check if target dead
        if (target.isRemoved() || target.isDead()) {
            return true;
        }

        return false;
    }

    private void endDissolving(PlayerEntity player, boolean interrupted) {
        if (activeSession == null) return;

        LivingEntity target = activeSession.target;

        // remove movement restrictions from target
        target.removeStatusEffect(StatusEffects.SLOWNESS);

        // apply size increase instead of strength
        if (!interrupted && activeSession.totalHpDrained > 0) {
            float sizeIncrease = activeSession.totalHpDrained * SIZE_INCREASE_PER_HEART;

            ScaleData playerHeight = ScaleTypes.HEIGHT.getScaleData(player);
            ScaleData playerWidth = ScaleTypes.WIDTH.getScaleData(player);

            float currentSize = playerHeight.getScale();
            float newSize = currentSize + sizeIncrease;

            playerHeight.setScale(newSize);
            playerWidth.setScale(newSize);

            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_SLIME_BLOCK_PLACE, SoundCategory.PLAYERS, 1.0f, 0.8f);

            // Particles showing growth
            ((ServerWorld) player.getWorld()).spawnParticles(ParticleTypes.ITEM_SLIME,
                    player.getX(), player.getY() + 1, player.getZ(), 15, 0.3, 0.3, 0.3, 0.1);
        } else {
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_SLIME_BLOCK_BREAK, SoundCategory.PLAYERS, 0.8f, 1.2f);
        }

        //clean
        activeSession = null;
        dissolvingTicks = 0;
        immunityTicks = 0;
    }

    public static Direction getHorizontalDirectionFromVelocity(Vec3d velocity, boolean isInX) {
        double x = velocity.x;
        double z = velocity.z;

        if (isInX) {
            return x > 0 ? Direction.EAST : Direction.WEST;
        } else{
            return z > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }

    //datastructure for dissolving people
    private static class DissolvingSession {
        final LivingEntity target;
        Vec3d playerInitialPos = null;
        Vec3d targetInitialPos = null;
        float totalHpDrained = 0f;

        DissolvingSession(LivingEntity target) {
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

