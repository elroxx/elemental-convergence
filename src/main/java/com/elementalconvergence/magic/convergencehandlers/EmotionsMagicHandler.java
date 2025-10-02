package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class EmotionsMagicHandler implements IMagicHandler {
    public static final int EMOTIONS_INDEX= (BASE_MAGIC_ID.length-1)+13;

    public static final int SPIDER_LIGHT_THRESHOLD = 9;

    //buff:
    //debuff:
    //buff:
    //passive:

    //lvl 1:
    //lvl 2:
    //lvl 3:

    //advancements:
    //1:
    //2:
    //3:




    @Override
    public void handleItemRightClick(PlayerEntity player) {

    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {

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

