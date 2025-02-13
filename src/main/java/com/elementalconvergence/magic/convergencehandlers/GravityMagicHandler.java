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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class GravityMagicHandler implements IMagicHandler {
    public static final int GRAVITY_INDEX= (BASE_MAGIC_ID.length-1)+2;

    private Direction wantedDirection = Direction.UP;

    public static final int GRAVITY_CHECK_INTERVAL = 40; //so check every 2 seconds
    private int gravityCheckCooldown = 0;

    public static final int GRAVITY_CONTROL_COOLDOWN = 20; //1 sec
    private int gravityControlCooldown = 0;

    @Override
    public void handleItemRightClick(PlayerEntity player) {

    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {
        //Debuff
        if (gravityCheckCooldown==0) {
            Direction currentDirection = GravityChangerAPI.getBaseGravityDirection(player);
            if (!currentDirection.equals(wantedDirection)) {
                GravityChangerAPI.setBaseGravityDirection(player, wantedDirection);
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

            if (!currentDirection.equals(newDirection)) {
                wantedDirection=newDirection;
                GravityChangerAPI.setBaseGravityDirection(player, newDirection);
                gravityControlCooldown=GRAVITY_CONTROL_COOLDOWN;
            }
        }
    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

}
