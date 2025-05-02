package com.elementalconvergence.magic;

import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.data.IMagicDataSaver;
import net.minecraft.server.network.ServerPlayerEntity;

import javax.swing.text.Element;

public class LevelManager {

    public static void handleLevelUp(ServerPlayerEntity player){

        IMagicDataSaver dataSaver = (IMagicDataSaver) player;
        int selectedMagic = dataSaver.getMagicData().getSelectedMagic();

        //DO NOTHING IF YOU HAVE NO MAGIC SELECTED
        if (selectedMagic==-1){
            return;
        }
        int currentLevel = dataSaver.getMagicData().getMagicLevel(selectedMagic);

        //DO NOTHING IF YOU ARE MAX LEVEL
        if (currentLevel==3){
            return;
        }
        String advToCheck= ElementalConvergence.MOD_ID+":"+ElementalConvergence.FULL_MAGIC_ID[selectedMagic]+"_lvl"+(currentLevel+1);
        //System.out.println(advToCheck);
        //System.out.println(ElementalConvergence.hasAdvancement(player, advToCheck));
        if (ElementalConvergence.hasAdvancement(player, advToCheck)){
            //System.out.println("LEVELED UP!");
            dataSaver.getMagicData().setMagicLevel(selectedMagic,currentLevel+1);
        }
    }

}
