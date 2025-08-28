package com.elementalconvergence.networking;


import com.elementalconvergence.ElementalConvergence;
import com.elementalconvergence.container.SchrodingerCatScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public class RerollPacket {
    public static final Identifier REROLL_PACKET_ID = ElementalConvergence.id("reroll");

    public static void registerClient() {
        // Register client-side packet sending
    }

    public static void registerServer() {
        // Register server-side packet handling
        ServerPlayNetworking.registerGlobalReceiver(RerollPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                if (context.player().currentScreenHandler instanceof SchrodingerCatScreenHandler handler) {
                    handler.reroll();
                }
            });
        });
    }

    public static void send() {
        ClientPlayNetworking.send(new RerollPayload());
    }

    public static void registerPayload() {
        PayloadTypeRegistry.playC2S().register(RerollPayload.ID, RerollPayload.CODEC);
    }

    public record RerollPayload() implements CustomPayload {
        public static final Id<RerollPayload> ID = new Id<>(REROLL_PACKET_ID);
        public static final PacketCodec<RegistryByteBuf, RerollPayload> CODEC = PacketCodec.unit(new RerollPayload());

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}
