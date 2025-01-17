package com.elementalconvergence.magic;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.data.IMagicDataSaver;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.swing.text.Element;

public class LevelManager {

    public static void handleLevelUp(ServerPlayerEntity player){

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        int selectedMagic = dataSaver.getMagicData().getSelectedMagic();
        int currentLevel = dataSaver.getMagicData().getMagicLevel(selectedMagic);

        String advToCheck= ElementalConvergence.MOD_ID+":"+ElementalConvergence.BASE_MAGIC_ID[selectedMagic]+"_lvl"+(currentLevel+1);
        System.out.println(advToCheck);

        if (ElementalConvergence.hasAdvancement(player, advToCheck)){
            dataSaver.getMagicData().setMagicLevel(selectedMagic,currentLevel+1);
        }
    }

}
