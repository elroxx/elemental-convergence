package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.data.IPlayerMiningMixin;
import com.elementalconvergence.magic.IMagicHandler;
import com.elementalconvergence.networking.MiningSpeedPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static com.elementalconvergence.ElementalConvergence.hasAdvancement;

public class EarthMagicHandler implements IMagicHandler {
    public static final float WOODEN_PICKAXE_MULTIPLIER=2.0f;
    public static final float STONE_PICKAXE_MULTIPLIER=4.0f;
    public static final float IRON_PICKAXE_MULTIPLIER=6.0f;
    public static final float DIAMOND_PICKAXE_MULTIPLIER=8.0f;
    public static final float NETHERITE_PICKAXE_MULTIPLIER=10.0f; //Technically it is 9.0f, but I wanted to make it faster
    public static final float DEFAULT_PICKAXE_MULTIPLIER=1.0f;



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
        // /advancement grant @a only minecraft:nether/obtain_ancient_debris
        System.out.println("MINING MULTIPLIER: "+((IPlayerMiningMixin) player).getMiningSpeedMultiplier());
        if (((IPlayerMiningMixin) player).getMiningSpeedMultiplier()!=multiplier) {
           ((IPlayerMiningMixin) player).setMiningSpeedMultiplier(multiplier);
            if (player instanceof ServerPlayerEntity serverPlayer) {
                ServerPlayNetworking.send(serverPlayer, new MiningSpeedPayload(multiplier));
            }
        }
    }

}
