package com.elementalconvergence.magic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public interface IMagicHandler {
    //right now only handle rightclick
    void handleRightClick(PlayerEntity player);
    void handlePassive(PlayerEntity player);
    void handleAttack(PlayerEntity player, Entity victim);
    // SpellKeybinds
    void handlePrimarySpell(PlayerEntity player); // All of these could be ServerPlayerEntity because we already checked that they are not the client.
    void handleSecondarySpell(PlayerEntity player);
    void handleTertiarySpell(PlayerEntity player);
}
