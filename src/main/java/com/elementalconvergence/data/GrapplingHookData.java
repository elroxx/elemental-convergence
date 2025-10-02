package com.elementalconvergence.data;

import com.elementalconvergence.entity.LashingPotatoHookEntity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public class GrapplingHookData {
    private boolean hasActiveHook = false;

    @Nullable
    private LashingPotatoHookEntity grapplingHookEntity = null;

    public boolean hasActiveHook() {
        return hasActiveHook;
    }

    public void setHasActiveHook(boolean hasActiveHook) {
        this.hasActiveHook = hasActiveHook;
    }

    @Nullable
    public LashingPotatoHookEntity getGrapplingHookEntity() {
        return grapplingHookEntity;
    }

    public void setGrapplingHookEntity(@Nullable LashingPotatoHookEntity hookEntity) {
        this.grapplingHookEntity = hookEntity;
        this.hasActiveHook = hookEntity != null;
    }

    public void clearHook() {
        this.grapplingHookEntity = null;
        this.hasActiveHook = false;
    }

    public void writeNbt(NbtCompound nbt) {
        nbt.putBoolean("hasActiveHook", hasActiveHook);
        // Note: We don't save the entity reference as it's transient
        // The hook entity itself manages the relationship
    }

    public void readNbt(NbtCompound nbt) {
        hasActiveHook = nbt.getBoolean("hasActiveHook");
        // Clear the entity reference on load - it will be reestablished when the hook entity loads
        grapplingHookEntity = null;
    }
}
