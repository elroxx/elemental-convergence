package com.elementalconvergence.item;

import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.ISchrodingerTPDataSaver;
import com.elementalconvergence.data.MagicData;
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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Set;

import static com.elementalconvergence.magic.convergencehandlers.QuantumMagicHandler.QUANTUM_INDEX;

public class SchrodingerCatItem extends Item {
    private static final String SAVED_X_KEY = "teleport_saved_x";
    private static final String SAVED_Y_KEY = "teleport_saved_y";
    private static final String SAVED_Z_KEY = "teleport_saved_z";
    private static final String SAVED_VELOCITY_X_KEY = "teleport_saved_velocity_x";
    private static final String SAVED_VELOCITY_Y_KEY = "teleport_saved_velocity_y";
    private static final String SAVED_VELOCITY_Z_KEY = "teleport_saved_velocity_z";
    private static final String SAVED_YAW_KEY = "teleport_saved_yaw";
    private static final String SAVED_PITCH_KEY = "teleport_saved_pitch";
    private static final String SAVED_DIMENSION_KEY = "teleport_saved_dimension";
    private static final String HAS_SAVED_POSITION_KEY = "teleport_has_saved";

    public static final int DEFAULT_QUANTUM_TP_COOLDOWN = 10; //half a sec

    public SchrodingerCatItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) {
            return TypedActionResult.pass(user.getStackInHand(hand));
        }

        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) user;

        IMagicDataSaver dataSaver = (IMagicDataSaver) user;
        MagicData magicData = dataSaver.getMagicData();
        int quantumLevel = magicData.getMagicLevel(QUANTUM_INDEX);
        if (quantumLevel>=1) {

            SchrodingerTPData teleportData = ((ISchrodingerTPDataSaver) serverPlayer).getTeleportData();

            if (!teleportData.hasSavedPosition()) {
                // use if no pos
                saveCurrentPosition(serverPlayer, teleportData);
                serverPlayer.sendMessage(Text.literal("§a Quantum clone created!"), false);
            } else {
                // swap
                swapPositions(serverPlayer, teleportData);
                //set cooldown only if position was already stored
                user.getItemCooldownManager().set(this, DEFAULT_QUANTUM_TP_COOLDOWN);

                //only played after tp, which works for me as well ig
                user.getWorld().playSound(null, user.getBlockPos(), SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE,
                        SoundCategory.PLAYERS, 1.0F, 0.75F);
            }


            return TypedActionResult.success(user.getStackInHand(hand));
        }

        return TypedActionResult.fail(user.getStackInHand(hand));

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
        double currentVelocityX = player.getVelocity().getX();
        double currentVelocityY = player.getVelocity().getY();
        double currentVelocityZ = player.getVelocity().getZ();
        float currentYaw = player.getYaw();
        float currentPitch = player.getPitch();
        String currentDimension = player.getServerWorld().getRegistryKey().getValue().toString();

        //get pos
        double savedX = data.getSavedX();
        double savedY = data.getSavedY();
        double savedZ = data.getSavedZ();
        double savedVelocityX = data.getSavedVelocityX();
        double savedVelocityY = data.getSavedVelocityY();
        double savedVelocityZ = data.getSavedVelocityZ();
        float savedYaw = data.getSavedYaw();
        float savedPitch = data.getSavedPitch();
        String savedDimension = data.getSavedDimension();

        //change pos
        data.setSavedX(currentX);
        data.setSavedY(currentY);
        data.setSavedZ(currentZ);
        data.setSavedVelocityX(currentVelocityX);
        data.setSavedVelocityY(currentVelocityY);
        data.setSavedVelocityZ(currentVelocityZ);
        data.setSavedYaw(currentYaw);
        data.setSavedPitch(currentPitch);
        data.setSavedDimension(currentDimension);

        //tp to old pos METHOD CALL
        teleportToPositionAndChangeVelocity(player, savedX, savedY, savedZ, savedVelocityX, savedVelocityY, savedVelocityZ, savedYaw, savedPitch, savedDimension);


        player.sendMessage(Text.literal("§bTeleported! Positions swapped."), false);
    }

    private void teleportToPositionAndChangeVelocity(ServerPlayerEntity player, double x, double y, double z, double veloX, double veloY, double veloZ, float yaw, float pitch, String dimensionString) {
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

            Vec3d finalVelocity = new Vec3d(veloX, veloY, veloZ);
            //change velocity as well
            player.setVelocity(finalVelocity);
            player.velocityModified=true; //IMPORTANT COZ IF NOT THIS DOESNT CHANGE THE PLAYERS VELOCITY AT ALL

        } catch (Exception e) {
            player.sendMessage(Text.literal("§cError during teleportation: " + e.getMessage()), false);
        }
    }
}
