package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.container.StealInventoryScreenHandler;
import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.effect.PlagueEffect;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.IMagicHandler;
import com.elementalconvergence.magic.MagicRegistry;
import gravity_changer.api.GravityChangerAPI;
import gravity_changer.command.GravityCommand;
import gravity_changer.mob_effect.GravityPotion;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.List;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class GravityMagicHandler implements IMagicHandler {
    public static final int GRAVITY_INDEX= (BASE_MAGIC_ID.length-1)+2;

    private Direction wantedDirection = Direction.UP;
    public static final double GRAVITY_GRAVITY_STRENGTH = 0.165;

    public static final int GRAVITY_CHECK_INTERVAL = 40; //so check every 2 seconds
    private int gravityCheckCooldown = 0;

    public static final int GRAVITY_CONTROL_COOLDOWN = 10; //0.5 sec
    private int gravityControlCooldown = 0;

    public static final int GRAVITY_INSTABILITY_COOLDOWN = 10;
    private int gravityInstabilityCooldown = 0;

    public static final int VACUUM_DEFAULT_COOLDOWN = 100;
    private int vacuumCooldown = 0;
    private static final int VACUUM_DURATION = 20*5; // 5 secs
    private static final double VACUUM_RANGE = 20.0;

    public static final int GRAVITY_INSTABILITY_DEFAULT_DURATION=20*60; // so its 1 minute long by default

    @Override
    public void handleItemRightClick(PlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        ItemStack offHand = player.getOffHandStack();
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int gravityLevel = magicData.getMagicLevel(GRAVITY_INDEX);
        if (gravityLevel>=1 && (mainHand.isOf(Items.HOPPER) || offHand.isOf(Items.HOPPER))){
            //set cooldown
            vacuumCooldown=VACUUM_DEFAULT_COOLDOWN;
            player.getItemCooldownManager().set(Items.HOPPER, VACUUM_DEFAULT_COOLDOWN);

            //consume item
            if (mainHand.isOf(Items.HOPPER)){
                mainHand.decrement(1);
            }else{
                offHand.decrement(1);
            }

            // vacuum time
            startItemVacuum(player);
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_ENDER_CHEST_OPEN, SoundCategory.PLAYERS, 0.6F, 0.5F);

        }

    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {
        ItemStack mainHand = player.getMainHandStack();
        int selectedSlot = player.getInventory().selectedSlot;

        //Selected slot does not make sense
        if (selectedSlot>=6){
            return;
        }
        //Not a living entity
        if (!(targetEntity instanceof LivingEntity)){
            return;
        }

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        MagicData magicData = dataSaver.getMagicData();
        int gravityLevel = magicData.getMagicLevel(GRAVITY_INDEX);

        if (gravityLevel>=3 && mainHand.isOf(ModItems.GRAVITY_SHARD) && gravityInstabilityCooldown==0){
            //cooldown
            gravityInstabilityCooldown=GRAVITY_INSTABILITY_COOLDOWN;
            player.getItemCooldownManager().set(ModItems.GRAVITY_SHARD, GRAVITY_INSTABILITY_COOLDOWN);

            //consume an item
            if (!player.getAbilities().creativeMode) {
                mainHand.decrement(1);
            }

            //Actual gravity swap
            ((LivingEntity) targetEntity).addStatusEffect(new StatusEffectInstance(ModEffects.GRAVITY_INSTABILITY, GRAVITY_INSTABILITY_DEFAULT_DURATION, selectedSlot, false, true, true));

            //playsound
            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_VEX_DEATH, SoundCategory.PLAYERS, 1.0F, 0.5F);

        }
    }

    @Override
    public void handlePassive(PlayerEntity player) {
        //Debuff
        if (gravityCheckCooldown==0) {
            Direction currentDirection = GravityChangerAPI.getBaseGravityDirection(player);
            if (!currentDirection.equals(wantedDirection)) {
                GravityChangerAPI.setBaseGravityDirection(player, wantedDirection);
            }
            //buff (0.165 normal gravity aka its the moon gravity)
            double currentgStrength = GravityChangerAPI.getBaseGravityStrength(player);
            if (Math.abs(currentgStrength-GRAVITY_GRAVITY_STRENGTH)>=0.01){
                GravityChangerAPI.setBaseGravityStrength(player, GRAVITY_GRAVITY_STRENGTH);
            }
            gravityCheckCooldown=GRAVITY_CHECK_INTERVAL;
        }

        //handle cooldowns
        if (gravityCheckCooldown>0){
            gravityCheckCooldown--;
        }
        if (gravityControlCooldown>0){
            gravityControlCooldown--;
        }
        if (gravityInstabilityCooldown>0){
            gravityInstabilityCooldown--;
        }
        if (vacuumCooldown>0){
            vacuumCooldown--;
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
        int gravityLevel = magicData.getMagicLevel(GRAVITY_INDEX);

        if (gravityLevel>=2 && gravityControlCooldown==0) {
            Direction currentDirection = GravityChangerAPI.getBaseGravityDirection(player);
            float yaw = player.getYaw();
            float pitch = player.getPitch();
            Direction newDirection = currentDirection;

            if (70 <= pitch && pitch <= 90) {
                newDirection = currentDirection; //IF THE PERSON RETURN SAME GRAVITY
            } else if (-90 <= pitch && pitch <= -70) {
                //IF THE PERSON IS LOOKING UP, RETURN THE OPPOSITE
                newDirection = switch (currentDirection) {
                    case Direction.DOWN -> Direction.UP;
                    case Direction.UP -> Direction.DOWN;
                    case Direction.NORTH -> Direction.SOUTH;
                    case Direction.EAST -> Direction.WEST;
                    case Direction.SOUTH -> Direction.NORTH;
                    case Direction.WEST -> Direction.EAST;
                };
            }
            else if (-20 <= yaw && yaw <= 20){
                //IF THE PERSON IS LOOKING STRAIGHT
                newDirection = switch (currentDirection) {
                    case Direction.DOWN -> Direction.SOUTH;
                    case Direction.UP -> Direction.SOUTH;
                    case Direction.NORTH -> Direction.DOWN;
                    case Direction.EAST -> Direction.DOWN;
                    case Direction.SOUTH -> Direction.DOWN;
                    case Direction.WEST -> Direction.DOWN;
                };
            }
            else if ((160 <= yaw && yaw <= 180) || (-180 <= yaw && yaw <= -160)){
                //IF THE PERSON IS LOOKING BACKWARDS
                newDirection = switch (currentDirection) {
                    case Direction.DOWN -> Direction.NORTH;
                    case Direction.UP -> Direction.NORTH;
                    case Direction.NORTH -> Direction.UP;
                    case Direction.EAST -> Direction.UP;
                    case Direction.SOUTH -> Direction.UP;
                    case Direction.WEST -> Direction.UP;
                };
            }
            else if (70 <= yaw && yaw <= 110){
                //IF THE PERSON IS LOOKING RIGHT
                newDirection = switch (currentDirection) {
                    case Direction.DOWN -> Direction.WEST;
                    case Direction.UP -> Direction.EAST;
                    case Direction.NORTH -> Direction.WEST;
                    case Direction.EAST -> Direction.NORTH;
                    case Direction.SOUTH -> Direction.EAST;
                    case Direction.WEST -> Direction.SOUTH;
                };
            }
            else if (-110 <= yaw && yaw <= -70){
                //IF THE PERSON IS LOOKING LEFT
                newDirection = switch (currentDirection) {
                    case Direction.DOWN -> Direction.EAST;
                    case Direction.UP -> Direction.WEST;
                    case Direction.NORTH -> Direction.EAST;
                    case Direction.EAST -> Direction.SOUTH;
                    case Direction.SOUTH -> Direction.WEST;
                    case Direction.WEST -> Direction.NORTH;
                };
            }

            // no more down direction possible
            if (!currentDirection.equals(newDirection) && !newDirection.equals(Direction.DOWN)) {
                wantedDirection=newDirection;
                GravityChangerAPI.setBaseGravityDirection(player, newDirection);
                gravityControlCooldown=GRAVITY_CONTROL_COOLDOWN;

                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_ALLAY_ITEM_GIVEN, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

    private void startItemVacuum(PlayerEntity player) {
        new Thread(() -> {
            for (int i = 0; i < VACUUM_DURATION / 5; i++) { // Run for 5 seconds
                Vec3d pos = player.getPos();
                Box box = new Box(
                        pos.x - VACUUM_RANGE, pos.y - VACUUM_RANGE, pos.z - VACUUM_RANGE,
                        pos.x + VACUUM_RANGE, pos.y + VACUUM_RANGE, pos.z + VACUUM_RANGE
                );
                List<ItemEntity> items = player.getWorld().getEntitiesByClass(ItemEntity.class, box, item -> true);
                for (ItemEntity item : items) {
                    Vec3d direction = pos.subtract(item.getPos()).normalize().multiply(0.5);
                    item.setVelocity(direction);
                }

                try {
                    Thread.sleep(250); // Update every 5 ticks
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
}
