package com.elementalconvergence.networking;

import com.elementalconvergence.ElementalConvergence;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record InventoryUpdatePayload(UUID targetPlayerUuid, int slot, ItemStack stack) implements CustomPayload {
    public static final CustomPayload.Id<InventoryUpdatePayload> ID = new CustomPayload.Id<>(ElementalConvergence.id("inventory_update"));
    public static final PacketCodec<RegistryByteBuf, InventoryUpdatePayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, InventoryUpdatePayload::targetPlayerUuid,
            PacketCodecs.INTEGER, InventoryUpdatePayload::slot,
            ItemStack.PACKET_CODEC, InventoryUpdatePayload::stack,
            InventoryUpdatePayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;

    }
}

