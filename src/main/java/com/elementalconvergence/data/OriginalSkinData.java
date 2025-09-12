package com.elementalconvergence.data;

import net.minecraft.nbt.NbtCompound;

public class OriginalSkinData {
    private String originalSkinValue;
    private String originalSkinSignature;
    private boolean hasFetchedOnce = false;

    public String getOriginalSkinValue() {
        return originalSkinValue;
    }

    public void setOriginalSkinValue(String originalSkinValue) {
        this.originalSkinValue = originalSkinValue;
    }

    public String getOriginalSkinSignature() {
        return originalSkinSignature;
    }

    public void setOriginalSkinSignature(String originalSkinSignature) {
        this.originalSkinSignature = originalSkinSignature;
    }

    public boolean hasFetchedOnce() {
        return hasFetchedOnce;
    }

    public void setHasFetchedOnce(boolean hasFetchedOnce) {
        this.hasFetchedOnce = hasFetchedOnce;
    }

    public void setSkinData(String value, String signature) {
        this.originalSkinValue = value;
        this.originalSkinSignature = signature;
        this.hasFetchedOnce = true;
    }

    public boolean hasValidSkinData() {
        return originalSkinValue != null && originalSkinSignature != null && hasFetchedOnce;
    }

    public void writeNbt(NbtCompound nbt) {
        if (originalSkinValue != null) {
            nbt.putString("original_skin_value", originalSkinValue);
        }
        if (originalSkinSignature != null) {
            nbt.putString("original_skin_signature", originalSkinSignature);
        }
        nbt.putBoolean("has_fetched_once", hasFetchedOnce);
    }

    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("original_skin_value")) {
            originalSkinValue = nbt.getString("original_skin_value");
        }
        if (nbt.contains("original_skin_signature")) {
            originalSkinSignature = nbt.getString("original_skin_signature");
        }
        hasFetchedOnce = nbt.getBoolean("has_fetched_once");
    }
}
