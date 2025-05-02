package com.elementalconvergence.data;

import com.elementalconvergence.ElementalConvergence;
import net.minecraft.nbt.NbtCompound;

public class MagicData {
    private int selectedMagic;
    private final int[] magicLevels;

    public MagicData() {
        this.magicLevels = new int[ElementalConvergence.FULL_MAGIC_ID.length];
        this.selectedMagic=-1;
    }

    public int getMagicLevel(int magicIndex) {
        return magicLevels[magicIndex];
    }

    public void setMagicLevel(int magicIndex, int level) {
        magicLevels[magicIndex] = level;
    }

    // Save data to NBT
    public NbtCompound writeNbt(NbtCompound nbt) {
        //Save nbt for magic levels
        for (int i = 0; i < magicLevels.length; i++) {
            nbt.putInt(ElementalConvergence.FULL_MAGIC_ID[i] + "_level", magicLevels[i]);
        }
        //Save selected magic
        nbt.putInt("selected_magic", selectedMagic);
        return nbt;
    }

    // Load data from NBT
    public void readNbt(NbtCompound nbt) {
        //Load nbt for magic levels
        for (int i = 0; i < magicLevels.length; i++) {
            magicLevels[i] = nbt.getInt(ElementalConvergence.FULL_MAGIC_ID[i] + "_level");
        }
        // Load nbt for selected magic
        selectedMagic = nbt.getInt("selected_magic");
    }

    //Basic setter for selected Magic
    public int getSelectedMagic() {
        return selectedMagic;
    }

    //Basic getter for selected Magic
    public void setSelectedMagic(int magic) {
        this.selectedMagic = magic;
    }

}