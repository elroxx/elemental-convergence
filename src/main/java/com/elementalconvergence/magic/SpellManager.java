package com.elementalconvergence.magic;

import com.elementalconvergence.data.IMagicDataSaver;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.mojang.text2speech.Narrator.LOGGER;

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

    public static void handleKeyPress(ServerPlayerEntity player, int spellNumber) {
        // Verify player and get magic data
        if (player == null) return;

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        int selectedMagic = dataSaver.getMagicData().getSelectedMagic();

        // Get the appropriate magic handler
        IMagicHandler handler = MagicRegistry.getHandler(selectedMagic);
        if (handler == null) {
            LOGGER.error("No handler found for magic type: " + selectedMagic);
            return;
        }

        // Handle the specific spell based on the key pressed
        switch(spellNumber) {
            case 1:
                handler.handlePrimarySpell(player);
                break;
            case 2:
                handler.handleSecondarySpell(player);
                break;
            case 3:
                handler.handleTertiarySpell(player);
                break;
            default:
                LOGGER.error("Invalid spell number received: " + spellNumber);
        }
    }

}