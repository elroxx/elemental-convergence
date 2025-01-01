package com.elementalconvergence.magic;

import net.minecraft.entity.player.PlayerEntity;

public interface IMagicHandler {
    //right now only handle rightclick
    void handleRightClick(PlayerEntity player);
}
