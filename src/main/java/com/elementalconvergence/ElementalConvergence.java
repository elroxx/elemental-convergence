package com.elementalconvergence;

import com.elementalconvergence.commands.GetSelectedMagicCommand;
import com.elementalconvergence.commands.SetMagicLevelCommand;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.MagicRegistry;
import com.elementalconvergence.magic.SpellManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class ElementalConvergence implements ModInitializer {
	public static final String MOD_ID = "elemental-convergence";

	public static final String[] BASE_MAGIC_DISPLAY = {"Earth", "Air", "Fire", "Water", "Shadow", "Light", "Life", "Death"};
	public static final String[] BASE_MAGIC_ID = {"earth", "air", "fire", "water", "shadow", "light", "life", "death"};
	public static final int TICKSEC = 20;

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
		//Items initialization
		ModItems.initialize();
		MagicRegistry.initialize();

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
		System.out.println("TESTING PRINT");
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
	}


	public static Identifier id(String path){
		return Identifier.of(MOD_ID, path);
	}
}