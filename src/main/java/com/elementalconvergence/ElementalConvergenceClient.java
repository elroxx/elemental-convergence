package com.elementalconvergence;

import com.elementalconvergence.container.MysticalTomeScreen;
import com.elementalconvergence.container.MysticalTomeScreenHandler;
import com.elementalconvergence.data.IPlayerMiningMixin;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.item.renderer.HaloRenderer;
import com.elementalconvergence.networking.InventoryNetworking;
import com.elementalconvergence.networking.MiningSpeedPayload;
import com.elementalconvergence.particle.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.particle.PortalParticle;

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

        ArmorRenderer.register(new HaloRenderer(), ModItems.HALO);


        //for particles
        ParticleFactoryRegistry.getInstance().register(ModParticles.ATOM_PARTICLE, PortalParticle.Factory::new);

        //for the tome inventory:
        HandledScreens.register(ElementalConvergence.MYSTICAL_TOME_SCREEN_HANDLER, MysticalTomeScreen::new);
    }
}
