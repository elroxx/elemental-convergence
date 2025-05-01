package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class WaterMagicHandler implements IMagicHandler {

    public static final int WATER_INDEX=3;

    private boolean toggleDolphin = false;

    @Override
    public void handleItemRightClick(PlayerEntity player) {

    }

    @Override
    public void handleEntityRightClick(PlayerEntity player, Entity targetEntity) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {

        //Debuff
        if (!player.hasStatusEffect(ModEffects.GILLS)){
            player.addStatusEffect(new StatusEffectInstance(ModEffects.GILLS, -1, 0, false, false, false));
        }

        //BUFF
        if (!player.hasStatusEffect(StatusEffects.CONDUIT_POWER) && player.isTouchingWaterOrRain()){
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.CONDUIT_POWER, -1, 2, false, false, false)); //add if in water or rain
        } else if (player.hasStatusEffect(StatusEffects.CONDUIT_POWER) && !player.isTouchingWaterOrRain()) {
            player.removeStatusEffect(StatusEffects.CONDUIT_POWER); //remove if not in water or rain
        }

        if (toggleDolphin && !player.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)){
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, -1, 2, false, false, true));
        } else if (!toggleDolphin){
            player.removeStatusEffect(StatusEffects.DOLPHINS_GRACE);
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
        int waterLevel = magicData.getMagicLevel(WATER_INDEX);

        if (waterLevel>=2){
            toggleDolphin=!toggleDolphin;
        }

    }

    @Override
    public void handleSecondarySpell(PlayerEntity player) {

    }

    @Override
    public void handleTertiarySpell(PlayerEntity player) {

    }

}
