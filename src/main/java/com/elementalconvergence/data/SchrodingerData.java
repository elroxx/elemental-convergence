package com.elementalconvergence.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.Map;

public class SchrodingerData extends PersistentState {
    private static final String DATA_NAME = "schrodinger_chest_data";

    private final Map<String, String> chestHashes = new HashMap<>();
    private final Map<String, String> lootTableIds = new HashMap<>();

    public SchrodingerData() {}

    public boolean hasChest(String posKey) {
        return chestHashes.containsKey(posKey);
    }

    public String getChestHash(String posKey) {
        return chestHashes.get(posKey);
    }

    public void setChestHash(String posKey, String hash) {
        chestHashes.put(posKey, hash);
        markDirty();
    }

    public String getLootKey(String posKey) {
        return lootTableIds.get(posKey);
    }

    public void setLootKey(String posKey, String lootId) {
        lootTableIds.put(posKey, lootId);
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound hashes = new NbtCompound();
        chestHashes.forEach(hashes::putString);
        nbt.put("Hashes", hashes);

        NbtCompound loots = new NbtCompound();
        lootTableIds.forEach(loots::putString);
        nbt.put("LootTables", loots);

        return nbt;
    }

    public static SchrodingerData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        SchrodingerData data = new SchrodingerData();

        if (nbt.contains("Hashes")) {
            NbtCompound hashes = nbt.getCompound("Hashes");
            for (String key : hashes.getKeys()) {
                data.chestHashes.put(key, hashes.getString(key));
            }
        }

        if (nbt.contains("LootTables")) {
            NbtCompound loots = nbt.getCompound("LootTables");
            for (String key : loots.getKeys()) {
                data.lootTableIds.put(key, loots.getString(key));
            }
        }

        return data;
    }

    public static SchrodingerData get(ServerWorld world) {
        PersistentStateManager manager = world.getPersistentStateManager();

        PersistentState.Type<SchrodingerData> type = new PersistentState.Type<>(
                SchrodingerData::new,
                SchrodingerData::fromNbt,
                null
        );

        return manager.getOrCreate(type, DATA_NAME);
    }
}
