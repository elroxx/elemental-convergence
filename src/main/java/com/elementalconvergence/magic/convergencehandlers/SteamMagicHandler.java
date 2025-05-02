package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class SteamMagicHandler implements IMagicHandler {
    public static final int STEAM_INDEX= (BASE_MAGIC_ID.length-1)+3; //this is 10

    private Vec3d lastPosition =null;
    private int horizontalBlocksMoved = 0;
    public int HORIZONTAL_BLOCK_COUNTER_DEFAULT=12;


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

        if (horizontalDistanceMoved >= 1.0) {
            // 1hp of dmg
            //player.damage(player.getDamageSources().outOfWorld(), 1.0F);
            this.horizontalBlocksMoved++;
            lastPosition=currentPos;
        }

        if (horizontalBlocksMoved>=HORIZONTAL_BLOCK_COUNTER_DEFAULT){
            //1hp of dmg
            if (!player.isCreative()) {
                player.damage(player.getDamageSources().outOfWorld(), 1.0F);
            }
            horizontalBlocksMoved=0;
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


}
