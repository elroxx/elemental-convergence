package com.elementalconvergence.magic.handlers;

import com.elementalconvergence.magic.IMagicHandler;
import net.minecraft.entity.player.PlayerEntity;

public class NoMagicHandler implements IMagicHandler {
    @Override
    public void handleRightClick(PlayerEntity player) {
        System.out.println("HANDLERIGHTCLICKNOMAGIC");
    }
}
