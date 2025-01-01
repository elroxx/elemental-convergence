package com.elementalconvergence.magic;

import com.elementalconvergence.data.IMagicDataSaver;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class SpellManager {
    public static void handleRightClick(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity))
        {
            return;
        }

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        int selectedMagic = dataSaver.getMagicData().getSelectedMagic();

        IMagicHandler handler = MagicRegistry.getHandler(selectedMagic);
        if (handler != null) {
            handler.handleRightClick(player);
        }
    }
}