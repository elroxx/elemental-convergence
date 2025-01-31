package com.elementalconvergence.networking;

import com.elementalconvergence.ElementalConvergence;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record MiningSpeedPayload(float multiplier) implements CustomPayload {
    public static final CustomPayload.Id<MiningSpeedPayload> ID = new CustomPayload.Id<>(ElementalConvergence.id("earth_mining_speed"));
    public static final PacketCodec<RegistryByteBuf, MiningSpeedPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.FLOAT, MiningSpeedPayload::multiplier,
            MiningSpeedPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
