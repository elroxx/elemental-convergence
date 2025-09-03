package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class LightMagicHandler implements IMagicHandler {
    public static final int LIGHT_INDEX=5;
    public static final int LIGHT_THRESHOLD=4;

    public static final int GLOW_DEFAULT_COOLDOWN = 10;
    public static final int GLOW_DEFAULT_DURATION = 20*60*2; //2 minute glowing
    private int glowCooldown = 0;

    private static final int TP_DISTANCE = 12;
    public static final int TP_DEFAULT_COOLDOWN = 30; //1.5 secs
    private int tpCooldown = 0;

    @Override
    public void handleItemRightClick(PlayerEntity player) {

    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();

        //Not a living entity
        if (!(targetEntity instanceof LivingEntity)){
            return;
        }

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int lightLevel = magicData.getMagicLevel(LIGHT_INDEX);

        if (lightLevel>=1 && (mainHand.isOf(Items.GLOW_INK_SAC) ||  offHand.isOf(Items.GLOW_INK_SAC))&& glowCooldown==0){
            //cooldown
            glowCooldown=GLOW_DEFAULT_COOLDOWN;
            player.getItemCooldownManager().set(Items.GLOW_INK_SAC, GLOW_DEFAULT_COOLDOWN);

            //consume an item
            if (!player.getAbilities().creativeMode) {
                if (mainHand.isOf(Items.GLOW_INK_SAC)) {
                    mainHand.decrement(1);
                }
                else{
                    offHand.decrement(1);
                }
            }

            //Actual gravity swap
            ((LivingEntity) targetEntity).addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, GLOW_DEFAULT_DURATION, 0, false, true, true));

            //playsound
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ITEM_GLOW_INK_SAC_USE, SoundCategory.PLAYERS, 1.0F, 0.7F);

            ((ServerWorld)player.getWorld()).spawnParticles(
                    ParticleTypes.END_ROD,
                    targetEntity.getX() + 0.5,
                    targetEntity.getY() + 1.5,
                    targetEntity.getZ() + 0.5,
                    10,
                    0.25,
                    0.25, //so that they rise a little
                    0.25,
                    0);

        }

    }

    @Override
    public void handlePassive(PlayerEntity player) {
        //BUFF
        if (!player.hasStatusEffect(ModEffects.LIGHT_PHASING)){
            player.addStatusEffect(new StatusEffectInstance(ModEffects.LIGHT_PHASING, -1, 0, false, false, false));
        }
        //DEBUFF

        if (player.getWorld().getLightLevel(player.getBlockPos())<LIGHT_THRESHOLD && !hasNearbyGlowingEntity(player)){
            if (!player.hasStatusEffect(ModEffects.FULL_BLINDNESS)){
                player.addStatusEffect(new StatusEffectInstance(ModEffects.FULL_BLINDNESS, -1, 0, false, false, false));
            }
        }
        else{
            if (player.hasStatusEffect(ModEffects.FULL_BLINDNESS)){
                player.removeStatusEffect(ModEffects.FULL_BLINDNESS);
            }
        }

        //LVL 3 ABILITY

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int lightLevel = magicData.getMagicLevel(LIGHT_INDEX);


        if (lightLevel>=3) {
            if (player.isUsingSpyglass()) {
                PlayerEntity target = getTargetedPlayer(player);
                if (target != null) {
                    applyBlindness(target);
                }
            }
        }

        //cooldown handling
        if (glowCooldown>0){
            glowCooldown--;
        }
        if (tpCooldown>0){
            tpCooldown--;
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
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int lightLevel = magicData.getMagicLevel(LIGHT_INDEX);
        if (lightLevel>=2 && tpCooldown==0){
            tpCooldown=TP_DEFAULT_COOLDOWN;

            ServerWorld world = (ServerWorld) player.getWorld();

            Vec3d lookVector = player.getRotationVector();

            //ray tracing
            Vec3d eyePos = player.getEyePos();
            Vec3d currentPos = player.getPos();
            Vec3d targetPos = eyePos;
            Vec3d step = lookVector.multiply(0.5); // verify every half block
            double distanceTraveled = 0;

            while (distanceTraveled < TP_DISTANCE) {
                targetPos = targetPos.add(step);
                distanceTraveled += 0.5;

                // see if hit block
                BlockState targetBlockState = world.getBlockState(BlockPos.ofFloored(targetPos));
                if (!(targetBlockState.isAir() || !targetBlockState.isOpaque())) {
                    // go out of the block
                    targetPos = targetPos.subtract(step);
                    break;
                }
            }


            Vec3d newPos = new Vec3d(
                    targetPos.x,
                    Math.ceil(targetPos.y - player.getEyeHeight(player.getPose())),
                    targetPos.z
            );

            BlockState newPosBlockState = world.getBlockState(BlockPos.ofFloored(newPos));
            if (!(newPosBlockState.isAir() || !newPosBlockState.isOpaque())){
                newPos = new Vec3d(
                        newPos.x,
                        Math.ceil(newPos.y + player.getEyeHeight(player.getPose())),
                        newPos.z
                );
            }


            // particles on path
            Vec3d particlePos = eyePos.subtract(0, player.getEyeHeight(player.getPose()), 0);
            double particleDistance = 0;

            while (particleDistance < distanceTraveled) {
                Vec3d pos = particlePos.add(lookVector.multiply(particleDistance));

                for (int i = 0; i < 5; i++) {
                    double offsetX = (world.random.nextDouble() - 0.5) * 0.2;
                    double offsetY = (world.random.nextDouble() - 0.5) * 0.2;
                    double offsetZ = (world.random.nextDouble() - 0.5) * 0.2;

                    world.spawnParticles(
                            ParticleTypes.END_ROD,
                            pos.x, pos.y, pos.z,
                            1,  0.1, 0.1, 0.1,  0.1
                    );
                }
                particleDistance += 1;
            }

            //tp
            Set<PositionFlag> flags = EnumSet.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z, PositionFlag.X_ROT, PositionFlag.Y_ROT);
            player.teleport(world, newPos.getX(), newPos.getY(), newPos.getZ(), flags, player.getYaw(), player.getPitch());


            player.getWorld().playSound(
                    null,
                    currentPos.x, currentPos.y, currentPos.z,
                    SoundEvents.ENTITY_EVOKER_CAST_SPELL,
                    SoundCategory.PLAYERS,
                    1.0F,
                    2.0F
            );
        }

    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

    private boolean hasNearbyGlowingEntity(PlayerEntity player) {
        if (player.getWorld() == null) return false;
        //System.out.println("got checking at least");

        double radius=10.0;
        // get all living entities close
        List<LivingEntity> nearbyEntities = player.getWorld().getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(radius),
                nearbyEntity -> nearbyEntity != player // exclude current player
        );

        // nearby entities with glowing
        return nearbyEntities.stream()
                .anyMatch(nearbyEntity -> nearbyEntity.hasStatusEffect(StatusEffects.GLOWING));
    }


    private static PlayerEntity getTargetedPlayer(PlayerEntity user) {
        // raycast to find the player being looked at
        Vec3d start = user.getCameraPosVec(1.0F);
        Vec3d direction = user.getRotationVec(1.0F);
        Vec3d end = start.add(direction.multiply(75*50)); //range

        EntityHitResult hit = ProjectileUtil.raycast(user, start, end, new Box(start, end), entity -> entity instanceof PlayerEntity, 75.0*50);
        if (hit != null && hit.getEntity() instanceof PlayerEntity) {
            return (PlayerEntity) hit.getEntity();
        }
        return null;
    }

    private void applyBlindness(PlayerEntity target) {
        if (!target.hasStatusEffect(StatusEffects.BLINDNESS)) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 5 * 20, 0, false, false, true));
        }
    }
}
