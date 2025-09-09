package com.elementalconvergence.data;

import com.elementalconvergence.block.ModBlocks;
import com.elementalconvergence.world.dimension.ModDimensions;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

public class BeehiveTeleporter {
    private static final int ROOM_SIZE = 15;
    private static final int ROOM_HEIGHT = 15;

    public static void enterBeehive(ServerPlayerEntity player, ItemStack beehiveStack) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        //store player pos
        BeehivePlayerData.storePlayerPosition(player);

        //retrieve dimension
        ServerWorld beehiveWorld = server.getWorld(ModDimensions.BEEHIVE_WORLD_KEY);
        if (beehiveWorld == null) {
            player.sendMessage(Text.literal("Beehive dimension not found! Check datapack."), false);
            return;
        }

        // UNIQUE ROOM POSITION PER PLAYER
        BlockPos roomPos = getBeehiveRoomPosition(player, beehiveStack);

        // be sure it exists, if not build it
        createBeehiveRoom(beehiveWorld, roomPos);

        //tp player
        BlockPos teleportPos = roomPos.add((int)Math.floor(ROOM_SIZE/2.0), 1, (int)Math.floor(ROOM_SIZE/2.0)); // Center of the room, one block above floor

        TeleportTarget target = new TeleportTarget(beehiveWorld,
                Vec3d.ofCenter(teleportPos),
                player.getVelocity(),
                player.getYaw(),
                player.getPitch(),
                TeleportTarget.NO_OP);

        player.teleportTo(target);
        player.sendMessage(Text.literal("Entered the beehive!"), true);
    }

    public static void exitBeehive(ServerPlayerEntity player) {
        BeehivePlayerData.PlayerPosition storedPos = BeehivePlayerData.getStoredPosition(player);

        if (storedPos == null) {
            player.sendMessage(Text.literal("No stored position found!"), false);
            return;
        }

        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerWorld targetWorld = server.getWorld(storedPos.worldKey);
        if (targetWorld == null) {
            player.sendMessage(Text.literal("Original world not found!"), false);
            return;
        }

        TeleportTarget target = new TeleportTarget(targetWorld,
                storedPos.position,
                player.getVelocity(),
                storedPos.yaw,
                storedPos.pitch,
                TeleportTarget.NO_OP);

        player.teleportTo(target);
        player.sendMessage(Text.literal("Exited the beehive!"), true);

        // Clear stored position
        BeehivePlayerData.clearStoredPosition(player);
    }

    private static BlockPos getBeehiveRoomPosition(ServerPlayerEntity player, ItemStack beehiveStack) {
        //for unique ones
        int hash = Math.abs(player.getUuid().toString().hashCode());
        int x = (hash % 1000) * 20; //spread rooms apart and be sure they can actually all fit (aka all blocks are at a multiple of 20
        int z = ((hash / 1000) % 1000) * 20;
        return new BlockPos(x, 100, z); // Place rooms at Y=100
    }

    private static void createBeehiveRoom(ServerWorld world, BlockPos roomPos) {
        if (world.getBlockState(roomPos).getBlock() == ModBlocks.REINFORCED_HONEYCOMB) {
            return; //room already exists so cancel
        }

        //cube for room
        for (int x = 0; x < ROOM_SIZE; x++) {
            for (int y = 0; y < ROOM_HEIGHT; y++) {
                for (int z = 0; z < ROOM_SIZE; z++) {
                    BlockPos pos = roomPos.add(x, y, z);

                    // walls+floor+ceiling in honeycomb
                    if (x == 0 || x == ROOM_SIZE - 1 ||
                            y == 0 || y == ROOM_HEIGHT - 1 ||
                            z == 0 || z == ROOM_SIZE - 1) {
                        world.setBlockState(pos, ModBlocks.REINFORCED_HONEYCOMB.getDefaultState());
                    } else {
                        //remove interior
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
    }
}
