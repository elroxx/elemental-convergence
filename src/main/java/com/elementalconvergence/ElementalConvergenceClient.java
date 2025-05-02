package com.elementalconvergence;

import com.elementalconvergence.data.IPlayerMiningMixin;
import com.elementalconvergence.networking.InventoryNetworking;
import com.elementalconvergence.networking.MiningSpeedPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class ElementalConvergenceClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Client init
        ClientPlayNetworking.registerGlobalReceiver(MiningSpeedPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ((IPlayerMiningMixin)context.client().player).setMiningSpeedMultiplier(payload.multiplier());
            });
        });

        //Init inventory stealing packets
        InventoryNetworking.registerS2CPackets();
    }
}
