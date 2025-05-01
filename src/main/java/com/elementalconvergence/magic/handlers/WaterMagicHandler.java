package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class WaterMagicHandler implements IMagicHandler {

    public static final int BREATH_DECAY_DELAY = 10;
    private int breathDelay = 10;

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

        //Debuff
        //handlePlayerBreathing(player);

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

    private void handlePlayerBreathing(PlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getWorld();
        boolean isInWater = player.isSubmergedInWater();
        boolean isInRain = world.isRaining() && world.isSkyVisible(player.getBlockPos());

        if (isInWater || isInRain) {
            player.setAir(player.getMaxAir());
            breathDelay=10;
        } else {
            if (breathDelay == 0) {
                //WHEN DROWNING
                int currentAir = player.getAir() - 1;
                player.setAir(currentAir);
                if (currentAir > 0) {
                    //reduce air if air left
                    player.setAir(currentAir - 1);
                } else {
                    // drown
                    player.damage(player.getDamageSources().drown(), 2.0F);
                    player.setAir(0); // reset to 0 so it doesn’t spam damage
                }
            }
            else{
                breathDelay--;
            }
        }
    }
}
