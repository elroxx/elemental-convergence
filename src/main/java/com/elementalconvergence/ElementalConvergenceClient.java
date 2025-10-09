package com.elementalconvergence;

import com.elementalconvergence.container.MysticalTomeScreen;
import com.elementalconvergence.container.MysticalTomeScreenHandler;
import com.elementalconvergence.data.IGrapplingHookDataSaver;
import com.elementalconvergence.data.IPlayerMiningMixin;
import com.elementalconvergence.entity.ModEntitiesClient;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.item.renderer.HaloRenderer;
import com.elementalconvergence.networking.InventoryNetworking;
import com.elementalconvergence.networking.MiningSpeedPayload;
import com.elementalconvergence.networking.SpellCastPayload;
import com.elementalconvergence.particle.ModParticles;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import static com.elementalconvergence.ElementalConvergence.LOGGER;
import static com.elementalconvergence.ElementalConvergence.MOD_ID;

public class ElementalConvergenceClient implements ClientModInitializer {

    // Keybindings
    private static KeyBinding primarySpellKb;
    private static KeyBinding secondarySpellKb;
    private static KeyBinding tertiarySpellKb;

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

        //register keybinds
        registerKeybindings(); //Keybinds

        ModEntitiesClient.initializeClient();

        ModelPredicateProviderRegistry.register(
                ModItems.LASHING_POTATO_HOOK,
                ElementalConvergence.id("extended"),
                (stack, world, entity, seed) -> {
                    if (entity == null) return 0.0F;

                    // Check if the hook is currently extended
                    if (entity instanceof IGrapplingHookDataSaver saver) {
                        if (saver.getGrapplingHookData().getGrapplingHookEntity() != null) {
                            return 1.0F; // use extended texture
                        }
                    }

                    return 0.0F; // default texture
                }
        );
    }

    private void registerKeybindings() {
        //
        primarySpellKb = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + MOD_ID + ".primary_spell",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                "category." + MOD_ID + ".spells"
        ));

        secondarySpellKb = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + MOD_ID + ".secondary_spell",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "category." + MOD_ID + ".spells"
        ));

        tertiarySpellKb = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + MOD_ID + ".tertiary_spell",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "category." + MOD_ID + ".spells"
        ));

        // TICK EVENTS FOR KEYPRESSES
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (primarySpellKb.wasPressed()) {
                handleSpellKey(1);
            }
            if (secondarySpellKb.wasPressed()) {
                handleSpellKey(2);
            }
            if (tertiarySpellKb.wasPressed()) {
                handleSpellKey(3);
            }
        });
    }

    private void handleSpellKey(int spellNumber) {
        LOGGER.info("Spell key " + spellNumber + " got pressed"); // FOR TESTING PURPOSES
        if (!(spellNumber == 1 || spellNumber == 2 || spellNumber == 3)) {
            System.out.println("Error: Wrong spellkey number somehow?");
            return;
        }

        // Send packet to server asking for the  spell cast payload
        ClientPlayNetworking.send(new SpellCastPayload(spellNumber));
    }
}
