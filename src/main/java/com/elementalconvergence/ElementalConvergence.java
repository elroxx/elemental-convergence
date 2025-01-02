package com.elementalconvergence;

import com.elementalconvergence.commands.GetSelectedMagicCommand;
import com.elementalconvergence.commands.SetMagicLevelCommand;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.MagicRegistry;
import com.elementalconvergence.magic.SpellManager;
import com.elementalconvergence.networking.SpellCastPayload;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class ElementalConvergence implements ModInitializer {
	public static final String MOD_ID = "elemental-convergence";

	public static final String[] BASE_MAGIC_DISPLAY = {"Earth", "Air", "Fire", "Water", "Shadow", "Light", "Life", "Death"};
	public static final String[] BASE_MAGIC_ID = {"earth", "air", "fire", "water", "shadow", "light", "life", "death"};
	public static final int TICKSEC = 20;


	// Keybindings
	private static KeyBinding primarySpellKb;
	private static KeyBinding secondarySpellKb;
	private static KeyBinding tertiarySpellKb;

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("Hello Fabric world!");

		//Basic Initialization
		ModItems.initialize(); //Items
		MagicRegistry.initialize(); //Magic Types and spells
		registerKeybindings(); //Keybinds

		// COMMANDS SECTION
		CommandRegistrationCallback.EVENT.register(SetMagicLevelCommand::register); //Registration of the SetMagicLevelCommand
		CommandRegistrationCallback.EVENT.register(GetSelectedMagicCommand::register); //Registration of the SetMagicLevelCommand


		//Spell initialization depending on type of magic.

		//PASSIVE EACH TICK
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				SpellManager.handlePassives(player);
			}
		});

		//RIGHT CLICK
		//System.out.println("TESTING PRINT");
		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (!world.isClient()) {
				SpellManager.handleRightClick(player);
			}
			return TypedActionResult.pass(player.getStackInHand(hand));
		});

		// On HIT
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!world.isClient()) {
				SpellManager.handleAttack(player, entity);
			}
			return ActionResult.PASS;
		});

		// Register packet on both sides
		PayloadTypeRegistry.playC2S().register(SpellCastPayload.ID, SpellCastPayload.CODEC);

		// Register server-side packet handler
		ServerPlayNetworking.registerGlobalReceiver(SpellCastPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			World world = player.getWorld();

			// Execute spell on server side
			context.server().execute(() -> {
				int spellNumber = payload.spellNumber();
				SpellManager.handleKeyPress(player, spellNumber);
			});
		});
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

		// Register the tick event to check for key presses
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

		// Send packet to server requesting spell cast
		ClientPlayNetworking.send(new SpellCastPayload(spellNumber));
	}


	public static Identifier id(String path){
		return Identifier.of(MOD_ID, path);
	}
}