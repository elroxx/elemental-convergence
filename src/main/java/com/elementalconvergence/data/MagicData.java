package com.elementalconvergence.data;

import com.elementalconvergence.ElementalConvergence;
import net.minecraft.nbt.NbtCompound;

public class MagicData {
    private final int[] magicLevels;

    public MagicData() {
        // Initialize with the same length as your BASE_MAGIC_ID array
        this.magicLevels = new int[ElementalConvergence.BASE_MAGIC_ID.length];
    }

    public int getMagicLevel(int magicIndex) {
        return magicLevels[magicIndex];
    }

    public void setMagicLevel(int magicIndex, int level) {
        magicLevels[magicIndex] = level;
    }

    // Save data to NBT
    public NbtCompound writeNbt(NbtCompound nbt) {
        for (int i = 0; i < magicLevels.length; i++) {
            nbt.putInt(ElementalConvergence.BASE_MAGIC_ID[i] + "_level", magicLevels[i]);
        }
        return nbt;
    }

    // Load data from NBT
    public void readNbt(NbtCompound nbt) {
        for (int i = 0; i < magicLevels.length; i++) {
            magicLevels[i] = nbt.getInt(ElementalConvergence.BASE_MAGIC_ID[i] + "_level");
        }
    }
}