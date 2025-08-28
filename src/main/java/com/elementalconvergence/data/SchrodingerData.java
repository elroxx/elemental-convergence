package com.elementalconvergence.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.Map;

// Persistent data class to store chest hashes
public class SchrodingerData extends PersistentState {
    private static final String DATA_NAME = "schrodinger_chest_data";
    private final Map<String, String> chestHashes = new HashMap<>();

    public SchrodingerData() {
    }

    // New loader signature
    public static SchrodingerData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        SchrodingerData data = new SchrodingerData();

        NbtCompound chestsNbt = nbt.getCompound("chests");
        for (String key : chestsNbt.getKeys()) {
            data.chestHashes.put(key, chestsNbt.getString(key));
        }

        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound chestsNbt = new NbtCompound();
        for (Map.Entry<String, String> entry : chestHashes.entrySet()) {
            chestsNbt.putString(entry.getKey(), entry.getValue());
        }
        nbt.put("chests", chestsNbt);
        return nbt;
    }

    // Accessors
    public boolean hasChest(String posKey) {
        return chestHashes.containsKey(posKey);
    }

    public String getChestHash(String posKey) {
        return chestHashes.get(posKey);
    }

    public void setChestHash(String posKey, String hash) {
        chestHashes.put(posKey, hash);
    }

    // ✅ Updated getter for 1.21.1
    public static SchrodingerData get(ServerWorld world) {
        PersistentStateManager manager = world.getPersistentStateManager();

        PersistentState.Type<SchrodingerData> type = new PersistentState.Type<>(
                SchrodingerData::new,      // Supplier<T>
                SchrodingerData::fromNbt,  // BiFunction<NbtCompound, WrapperLookup, T>
                null                       // save name function (optional, usually null)
        );

        return manager.getOrCreate(type, DATA_NAME);
    }
}

