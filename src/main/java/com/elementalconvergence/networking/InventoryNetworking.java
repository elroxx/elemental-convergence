package com.elementalconvergence.networking;

import com.elementalconvergence.container.StealInventoryScreenHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

// ModNetworking.java
public class InventoryNetworking {
    public static void registerC2SPackets() {
        PayloadTypeRegistry.playC2S().register(OpenInventoryPayload.ID, OpenInventoryPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(InventoryUpdatePayload.ID, InventoryUpdatePayload.CODEC);
    }

    public static void registerS2CPackets() {
        PayloadTypeRegistry.playS2C().register(OpenInventoryPayload.ID, OpenInventoryPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(InventoryUpdatePayload.ID, InventoryUpdatePayload.CODEC);
    }

    public static void init() {
        registerC2SPackets();
        ServerPlayNetworking.registerGlobalReceiver(OpenInventoryPayload.ID, (payload, context) -> {
            ServerPlayerEntity sourcePlayer = context.player();
            UUID targetUuid = payload.targetPlayerUuid();

            context.server().execute(() -> {
                ServerPlayerEntity targetPlayer = context.server().getPlayerManager().getPlayer(targetUuid);
                if (targetPlayer != null) {
                    // OPENING THE INVENTORY
                    sourcePlayer.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                            (syncId, inventory, player) -> new StealInventoryScreenHandler(syncId, inventory, targetPlayer.getInventory()),
                            Text.literal(targetPlayer.getName().getString() + "'s Inventory")
                    ));
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(InventoryUpdatePayload.ID, (payload, context) -> {
            ServerPlayerEntity sourcePlayer = context.player();
            UUID targetUuid = payload.targetPlayerUuid();
            int slot = payload.slot();
            ItemStack stack = payload.stack();

            context.server().execute(() -> {
                ServerPlayerEntity targetPlayer = context.server().getPlayerManager().getPlayer(targetUuid);
                if (targetPlayer != null) {
                    // Update target player's inventory
                    targetPlayer.getInventory().setStack(slot, stack);
                }
            });
        });
    }
}
