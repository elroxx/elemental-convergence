package com.elementalconvergence.magic;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public interface IMagicHandler {
    //right now only handle rightclick
    void handleRightClick(PlayerEntity player);
    void handlePassive(PlayerEntity player);
    void handleAttack(PlayerEntity player, Entity victim);
    void handleMine(PlayerEntity player);
    void handleBlockBreak(PlayerEntity player, BlockPos pos, BlockState state, BlockEntity entity);


    // SpellKeybinds
    void handlePrimarySpell(PlayerEntity player); // All of these could be ServerPlayerEntity because we already checked that they are not the client.
    void handleSecondarySpell(PlayerEntity player);
    void handleTertiarySpell(PlayerEntity player);
}
