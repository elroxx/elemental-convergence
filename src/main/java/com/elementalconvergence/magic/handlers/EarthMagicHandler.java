package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.data.IPlayerMiningMixin;
import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import static com.elementalconvergence.ElementalConvergence.hasAdvancement;

public class EarthMagicHandler implements IMagicHandler {
    private static final float WOODEN_PICKAXE_MULTIPLIER=2.0f;
    private static final float STONE_PICKAXE_MULTIPLIER=4.0f;
    private static final float IRON_PICKAXE_MULTIPLIER=6.0f;
    private static final float DIAMOND_PICKAXE_MULTIPLIER=8.0f;
    private static final float NETHERITE_PICKAXE_MULTIPLIER=10.0f; //Technically it is 9.0f, but I wanted to make it faster
    private static final float DEFAULT_PICKAXE_MULTIPLIER=1.0f;

    private static final int WOODEN_MINING_LEVEL = 0;
    private static final int STONE_MINING_LEVEL = 1;
    private static final int IRON_MINING_LEVEL = 2;
    private static final int DIAMOND_MINING_LEVEL = 3;
    private static final int NETHERITE_MINING_LEVEL = 4;
    private static final int DEFAULT_MINING_LEVEL = 0;


    @Override
    public void handleRightClick(PlayerEntity player) {

    }

    @Override
    public void handlePassive(PlayerEntity player) {

    }

    @Override
    public void handleAttack(PlayerEntity player, Entity victim) {
    }

    @Override
    public void handleMine(PlayerEntity player) {
        updateHandMiningSpeed(player); //THIS IS THE PASSIVE SO NO LVL REQUIREMENTS NEEDED
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

    private void updateHandMiningSpeed(PlayerEntity player){
        float multiplier=DEFAULT_PICKAXE_MULTIPLIER;


        ItemStack mainHand = player.getMainHandStack();
        if (!mainHand.isEmpty()){
            multiplier=DEFAULT_PICKAXE_MULTIPLIER;
        }
        else if (hasAdvancement(player, "nether/obtain_ancient_debris")) {
            multiplier = NETHERITE_PICKAXE_MULTIPLIER;
        }
        else if (hasAdvancement(player, "story/mine_diamond")) {
            multiplier = DIAMOND_PICKAXE_MULTIPLIER;
        }
        else if (hasAdvancement(player, "story/iron_tools")) {
            multiplier = IRON_PICKAXE_MULTIPLIER;
        }
        else if (hasAdvancement(player, "story/upgrade_tools")) {
            multiplier = STONE_PICKAXE_MULTIPLIER;
        }
        else if (hasAdvancement(player, "story/mine_stone")){
            multiplier = WOODEN_PICKAXE_MULTIPLIER;
        }
        System.out.println("HAND SPEED:"+multiplier);
        // /advancement grant @a only minecraft:nether/obtain_ancient_debris
        ((IPlayerMiningMixin) player).setMiningSpeedMultiplier(multiplier);
    }

}
