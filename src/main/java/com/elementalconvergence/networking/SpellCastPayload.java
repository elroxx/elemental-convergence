package com.elementalconvergence.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import com.elementalconvergence.ElementalConvergence;

public record SpellCastPayload(int spellNumber) implements CustomPayload {
    public static final CustomPayload.Id<SpellCastPayload> ID = new CustomPayload.Id<>(ElementalConvergence.id("spell_cast"));
    public static final PacketCodec<PacketByteBuf, SpellCastPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, SpellCastPayload::spellNumber,
            SpellCastPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}