package com.elementalconvergence.data;

import net.minecraft.nbt.NbtCompound;

public class SchrodingerTPData {
    private double savedX = 0;
    private double savedY = 0;
    private double savedZ = 0;
    private float savedYaw = 0;
    private float savedPitch = 0;
    private String savedDimension = "";
    private boolean hasSavedPosition = false;

    public SchrodingerTPData() {}

    // save
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putDouble("teleport_saved_x", savedX);
        nbt.putDouble("teleport_saved_y", savedY);
        nbt.putDouble("teleport_saved_z", savedZ);
        nbt.putFloat("teleport_saved_yaw", savedYaw);
        nbt.putFloat("teleport_saved_pitch", savedPitch);
        nbt.putString("teleport_saved_dimension", savedDimension);
        nbt.putBoolean("teleport_has_saved", hasSavedPosition);
        return nbt;
    }

    //load data
    public void readNbt(NbtCompound nbt) {
        savedX = nbt.getDouble("teleport_saved_x");
        savedY = nbt.getDouble("teleport_saved_y");
        savedZ = nbt.getDouble("teleport_saved_z");
        savedYaw = nbt.getFloat("teleport_saved_yaw");
        savedPitch = nbt.getFloat("teleport_saved_pitch");
        savedDimension = nbt.getString("teleport_saved_dimension");
        hasSavedPosition = nbt.getBoolean("teleport_has_saved");
    }

    public boolean hasSavedPosition() { return hasSavedPosition; }
    public void setHasSavedPosition(boolean has) { this.hasSavedPosition = has; }

    public double getSavedX() { return savedX; }
    public void setSavedX(double x) { this.savedX = x; }

    public double getSavedY() { return savedY; }
    public void setSavedY(double y) { this.savedY = y; }

    public double getSavedZ() { return savedZ; }
    public void setSavedZ(double z) { this.savedZ = z; }

    public float getSavedYaw() { return savedYaw; }
    public void setSavedYaw(float yaw) { this.savedYaw = yaw; }

    public float getSavedPitch() { return savedPitch; }
    public void setSavedPitch(float pitch) { this.savedPitch = pitch; }

    public String getSavedDimension() { return savedDimension; }
    public void setSavedDimension(String dimension) { this.savedDimension = dimension; }
}
