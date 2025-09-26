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

    public NbtCompound writeNbt(NbtCompound nbt) {
        for (int i = 0; i < magicLevels.length; i++) {
            nbt.putInt(ElementalConvergence.FULL_MAGIC_ID[i] + "_level", magicLevels[i]);
        }
        nbt.putInt("selected_magic", selectedMagic);
        return nbt;
    }

    public void readNbt(NbtCompound nbt) {
        for (int i = 0; i < magicLevels.length; i++) {
            magicLevels[i] = nbt.getInt(ElementalConvergence.FULL_MAGIC_ID[i] + "_level");
        }
        selectedMagic = nbt.getInt("selected_magic");
    }

    public int getSelectedMagic() {
        return selectedMagic;
    }

    public void setSelectedMagic(int magic) {
        this.selectedMagic = magic;
    }

}