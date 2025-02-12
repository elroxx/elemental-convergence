package com.elementalconvergence.networking;

import com.elementalconvergence.ElementalConvergence;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record OpenInventoryPayload(UUID targetPlayerUuid) implements CustomPayload {
    public static final CustomPayload.Id<OpenInventoryPayload> ID = new CustomPayload.Id<>(ElementalConvergence.id("open_inventory"));
    public static final PacketCodec<RegistryByteBuf, OpenInventoryPayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, OpenInventoryPayload::targetPlayerUuid,
            OpenInventoryPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
