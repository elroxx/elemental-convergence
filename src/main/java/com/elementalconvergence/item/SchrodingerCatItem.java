package com.elementalconvergence.item;

import com.elementalconvergence.data.ISchrodingerTPDataSaver;
import com.elementalconvergence.data.SchrodingerTPData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Set;

public class SchrodingerCatItem extends Item {
    private static final String SAVED_X_KEY = "teleport_saved_x";
    private static final String SAVED_Y_KEY = "teleport_saved_y";
    private static final String SAVED_Z_KEY = "teleport_saved_z";
    private static final String SAVED_YAW_KEY = "teleport_saved_yaw";
    private static final String SAVED_PITCH_KEY = "teleport_saved_pitch";
    private static final String SAVED_DIMENSION_KEY = "teleport_saved_dimension";
    private static final String HAS_SAVED_POSITION_KEY = "teleport_has_saved";

    public SchrodingerCatItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) {
            return TypedActionResult.pass(user.getStackInHand(hand));
        }

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) user;
        SchrodingerTPData teleportData = ((ISchrodingerTPDataSaver) serverPlayer).getTeleportData();

        if (!teleportData.hasSavedPosition()) {
            // use if no pos
            saveCurrentPosition(serverPlayer, teleportData);
            serverPlayer.sendMessage(Text.literal("§a Quantum clone created!"), false);
        } else {
            // swap
            swapPositions(serverPlayer, teleportData);
        }

        return TypedActionResult.success(user.getStackInHand(hand));
    }

    private void saveCurrentPosition(ServerPlayerEntity player, SchrodingerTPData data) {
        data.setSavedX(player.getX());
        data.setSavedY(player.getY());
        data.setSavedZ(player.getZ());
        data.setSavedYaw(player.getYaw());
        data.setSavedPitch(player.getPitch());
        data.setSavedDimension(player.getServerWorld().getRegistryKey().getValue().toString());
        data.setHasSavedPosition(true);
    }

    private void swapPositions(ServerPlayerEntity player, SchrodingerTPData data) {
        // get current pos
        double currentX = player.getX();
        double currentY = player.getY();
        double currentZ = player.getZ();
        float currentYaw = player.getYaw();
        float currentPitch = player.getPitch();
        String currentDimension = player.getServerWorld().getRegistryKey().getValue().toString();

        //get pos
        double savedX = data.getSavedX();
        double savedY = data.getSavedY();
        double savedZ = data.getSavedZ();
        float savedYaw = data.getSavedYaw();
        float savedPitch = data.getSavedPitch();
        String savedDimension = data.getSavedDimension();

        //change pos
        data.setSavedX(currentX);
        data.setSavedY(currentY);
        data.setSavedZ(currentZ);
        data.setSavedYaw(currentYaw);
        data.setSavedPitch(currentPitch);
        data.setSavedDimension(currentDimension);

        // Teleport to old saved position
        teleportToPosition(player, savedX, savedY, savedZ, savedYaw, savedPitch, savedDimension);

        player.sendMessage(Text.literal("§bTeleported! Positions swapped."), false);
    }

    private void teleportToPosition(ServerPlayerEntity player, double x, double y, double z,
                                    float yaw, float pitch, String dimensionString) {
        try {
            Identifier dimensionId = Identifier.of(dimensionString);
            RegistryKey<World> dimensionKey = RegistryKey.of(RegistryKeys.WORLD, dimensionId);
            ServerWorld targetWorld = player.getServer().getWorld(dimensionKey);

            if (targetWorld == null) {
                player.sendMessage(Text.literal("§cError: Could not find dimension " + dimensionString), false);
                return;
            }

            // tp no matter dimension
            Set<PositionFlag> flags = EnumSet.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z, PositionFlag.X_ROT, PositionFlag.Y_ROT);
            player.teleport(targetWorld, x, y, z, flags, yaw, pitch);

        } catch (Exception e) {
            player.sendMessage(Text.literal("§cError during teleportation: " + e.getMessage()), false);
        }
    }
}
