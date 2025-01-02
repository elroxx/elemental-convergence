package com.elementalconvergence.magic;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public interface IMagicHandler {
    //right now only handle rightclick
    void handleRightClick(PlayerEntity player);
    void handlePassive(PlayerEntity player);
    void handleAttack(PlayerEntity player, Entity victim);
}
