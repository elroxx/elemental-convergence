package com.elementalconvergence.data;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BeehivePlayerData {
    private static MinecraftServer server;
    private static final Map<UUID, PlayerPosition> storedPositions = new HashMap<>();

    public static void setServer(MinecraftServer server) {
        BeehivePlayerData.server = server;
    }

    public static void storePlayerPosition(ServerPlayerEntity player) {
        PlayerPosition position = new PlayerPosition(
                player.getWorld().getRegistryKey(),
                player.getPos(),
                player.getYaw(),
                player.getPitch()
        );
        storedPositions.put(player.getUuid(), position);
    }

    public static PlayerPosition getStoredPosition(ServerPlayerEntity player) {
        return storedPositions.get(player.getUuid());
    }

    public static void clearStoredPosition(ServerPlayerEntity player) {
        storedPositions.remove(player.getUuid());
    }

    public static class PlayerPosition {
        public final RegistryKey<World> worldKey;
        public final Vec3d position;
        public final float yaw;
        public final float pitch;

        public PlayerPosition(RegistryKey<World> worldKey, Vec3d position, float yaw, float pitch) {
            this.worldKey = worldKey;
            this.position = position;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }
}
