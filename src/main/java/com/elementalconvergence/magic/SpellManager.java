package com.elementalconvergence.magic;

import com.elementalconvergence.data.IMagicDataSaver;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class SpellManager {
    public static void handleRightClick(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity))
        {
            System.out.println("SHOULD NOT ENTER HERE");
            return;
        }

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        int selectedMagic = dataSaver.getMagicData().getSelectedMagic();
        System.out.println("SelectedMagic: "+selectedMagic);
        IMagicHandler handler = MagicRegistry.getHandler(selectedMagic);
        if (handler != null) {
            handler.handleRightClick(player);
        }
    }

    public static void handlePassives(ServerPlayerEntity player) {
        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        int selectedMagic = dataSaver.getMagicData().getSelectedMagic();

        IMagicHandler handler = MagicRegistry.getHandler(selectedMagic);
        if (handler != null) {
            handler.handlePassive(player);
        }
    }

    public static void handleAttack(PlayerEntity player, Entity victim) {
        if (!(player instanceof ServerPlayerEntity)) return;

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        int selectedMagic = dataSaver.getMagicData().getSelectedMagic();

        IMagicHandler handler = MagicRegistry.getHandler(selectedMagic);
        if (handler != null) {
            handler.handleAttack(player, victim);
        }
    }
}