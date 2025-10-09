package com.elementalconvergence.magic.convergencehandlers;

import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import static com.elementalconvergence.ElementalConvergence.BASE_MAGIC_ID;

public class ElectricityMagicHandler implements IMagicHandler {
    public static final int ELECTRICITY_INDEX= (BASE_MAGIC_ID.length-1)+12;

    public static final int SPIDER_LIGHT_THRESHOLD = 9;
    //passive: sine wave motion. I want it drastic, like 1/3 speed up to *3 speed. I need to write it in NBT to save the attributes to be able to restart my sine wave where it supposed to be

    //lvl 1: looking at spyglass activates blocks. MAYBE add a
    //lvl 2: Thunderbow. add a trail of damage that disappears once the arrow hits the ground. Can right click on a redstone dust to teleport to the other side of the redstone trail. DOES NOT CONSUME IT
    //lvl 3: toggle lightning on hits

    //advancements:
    //1: conductive arrow
    //2: bolt armor trim
    //3: zombie head

    // Electric eye: top line: repeater, comparator, repeater, bottom line: copper block, redstone block, copper block
    // 4 copper
    //2 electric arrow== 4 arrow, 4 redstone, 1 lightning rod. Gives 4 arrows.





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

