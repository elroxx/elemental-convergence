package com.elementalconvergence;

import com.elementalconvergence.block.ModBlocks;
import com.elementalconvergence.commands.GetSelectedMagicCommand;
import com.elementalconvergence.commands.MagicCommand;
import com.elementalconvergence.commands.SetMagicLevelCommand;
import com.elementalconvergence.criterions.ModCriterions;
import com.elementalconvergence.data.IPlayerMiningMixin;
import com.elementalconvergence.entity.ModEntities;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.LevelManager;
import com.elementalconvergence.magic.MagicRegistry;
import com.elementalconvergence.magic.SpellManager;
import com.elementalconvergence.mixin.PlayerDataMixin;
import com.elementalconvergence.networking.MiningSpeedPayload;
import com.elementalconvergence.networking.SpellCastPayload;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import virtuoel.pehkui.api.PehkuiConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;

public class ElementalConvergence implements ModInitializer {
	public static final String MOD_ID = "elemental-convergence";

	public static final String[] BASE_MAGIC_DISPLAY = {"Earth", "Air", "Fire", "Water", "Shadow", "Light", "Life", "Death"};
	public static final String[] BASE_MAGIC_ID = {"earth", "air", "fire", "water", "shadow", "light", "life", "death"};
	public static final int TICKSEC = 20;


	public static ArrayList<String> deathList = new ArrayList<>(); //THEY ARE INITIALIZED AUTOMATICALLY
	public static HashMap<String, DeathTuple> deathMap = new HashMap<>();
	private static final int DEFAULT_DEATH_TIMER = 20*60; //1 min
	private static Random random = new Random();
	private static final int DEATH_PARTICLES_COUNT=4; //So either no particles, 1 particle or 2 particles

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
		ModBlocks.initialize(); //Blocks
		ModItems.initialize(); //Items
		MagicRegistry.initialize(); //Magic Types and spells
		registerKeybindings(); //Keybinds
		ModEntities.initialize(); //Entities
		ModCriterions.initialize(); //Criterions for advancements

		// COMMANDS SECTION
		CommandRegistrationCallback.EVENT.register(SetMagicLevelCommand::register); //Registration of SetMagicLevelCommand
		CommandRegistrationCallback.EVENT.register(GetSelectedMagicCommand::register); //Registration of SetMagicLevelCommand
		CommandRegistrationCallback.EVENT.register(MagicCommand::register); //Registration of starter magic Command


		//Spell initialization depending on type of magic.

		//PASSIVE EACH TICK
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				SpellManager.handlePassives(player);
				ModCriterions.SELECTED_MAGIC_CRITERION.trigger(player);
				ModCriterions.HAS_PARENT_CRITERION.trigger(player);
				ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.trigger(player);
				LevelManager.handleLevelUp(player);
			}

			for (String playerName : deathList){
				//PARTICLES LOGIC
				if (deathMap.get(playerName)!=null) {
					spawnDeathBlockParticles(deathMap.get(playerName).getDeathPos(), deathMap.get(playerName).getWorld());

					//Reduce all deathTimers by 1.
					deathMap.get(playerName).decrementTimer();

					//Removing from deathMap and deathList when timer is 0
					if (deathMap.get(playerName).getTimer() == 0) {
						deathMap.remove(playerName);
					}
				}
			}
		});

		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) -> {
				if (!world.isClient()){
					SpellManager.handleBlockBreak(player, pos, state, entity);
				}
				return true;
			});

		//RIGHT CLICK
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

		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (damageSource.getAttacker() instanceof PlayerEntity player){
				World world = player.getWorld();
				if (!world.isClient()) {
					SpellManager.handleKill(player, entity);
				}
			}

			if (entity instanceof ServerPlayerEntity player){
				BlockPos deathPos = player.getBlockPos();
				String playerName= player.getName().toString();
				if (!deathList.contains(playerName)) {
					deathList.add(playerName);
				}
				deathMap.put(playerName, new DeathTuple(DEFAULT_DEATH_TIMER, deathPos, player.getServerWorld())); //This replaces the previous deathTuple too


			}

		});

		//ON MINE
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->{
			//((IPlayerMiningMixin) player).setMiningSpeedMultiplier(10.0f);
			if (!world.isClient()){
				SpellManager.handleMine(player);
			}
			return ActionResult.PASS;
		});

		// Register packet on both sides
		PayloadTypeRegistry.playC2S().register(SpellCastPayload.ID, SpellCastPayload.CODEC);

		// INIT FOR MININGSPEED PAYLOAD
		PayloadTypeRegistry.playS2C().register(MiningSpeedPayload.ID, MiningSpeedPayload.CODEC);

		// Register server-side packet handler
		ServerPlayNetworking.registerGlobalReceiver(SpellCastPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			World world = player.getWorld();

			// THIS IS SO THAT THE SPELLS ARE SERVER SIDE ONLY
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


	public static Identifier id(String path){
		return Identifier.of(MOD_ID, path);
	}

	public static BlockPos getLookingAtBlockPos(PlayerEntity player, double maxRange, boolean considerFace) {
		//where the player is looking
		Vec3d eyePos = player.getEyePos();
		Vec3d lookVec = player.getRotationVec(1.0F);

		// maxRange on ray
		double rayLength = maxRange;
		Vec3d endVec = eyePos.add(lookVec.multiply(rayLength));

		// raycast
		BlockHitResult hitResult = player.getWorld().raycast(new RaycastContext(
				eyePos,
				endVec,
				RaycastContext.ShapeType.OUTLINE,
				RaycastContext.FluidHandling.NONE,
				player
		));

		BlockPos targetPos = hitResult.getBlockPos();
		Direction face = hitResult.getSide();

		if (considerFace) {
			//WE ALSO VERIFY HERE IF THE BLOCKHIT IS NOT AIR BECAUSE THEN WE DONT WANT TO CONSIDERFACE AT ALL AND WE RETURN NULL
			if (player.getWorld().getBlockState(targetPos).isAir()){
				return null;
			}
			//based on the hit face
			double x = targetPos.getX() + 0.5;
			double y = targetPos.getY();
			double z = targetPos.getZ() + 0.5;

			// adjust based on face
			switch (face) {
				case UP:
					y = targetPos.getY() + 1.0;
					break;
				case DOWN:
					y = targetPos.getY() - 1.0;
					break;
				case NORTH:
					z = targetPos.getZ() - 0.1;
					break;
				case SOUTH:
					z = targetPos.getZ() + 1.1;
					break;
				case WEST:
					x = targetPos.getX() - 0.1;
					break;
				case EAST:
					x = targetPos.getX() + 1.1;
					break;
			}

			BlockPos facePos = BlockPos.ofFloored(x, y, z);
			return facePos;
		}else {
			return hitResult.getBlockPos(); //this is the block that was hit by the raycast. THIS IS THE DIRECT ONE.
		}
	}

	public static boolean hasAdvancement(PlayerEntity player, String advancementId){
		if (!(player instanceof ServerPlayerEntity serverPlayer)){
			return false;
		}

		MinecraftServer server = serverPlayer.getServer();
		if (server == null){
			return false;
		}

		//Identifier id = Identifier.of("minecraft", advancementId);
		Identifier id = Identifier.of(advancementId);

		AdvancementEntry advancement = server.getAdvancementLoader().get(id);

		if (advancement!=null){
			return serverPlayer.getAdvancementTracker().getProgress(advancement).isDone();
		}
		return false;
	}

	private void spawnDeathBlockParticles(BlockPos deathPos, World world) {
		int particlesCount = (int) (random.nextFloat()*DEATH_PARTICLES_COUNT);

		if (particlesCount!=0){
			if (world instanceof ServerWorld){
				((ServerWorld) world).spawnParticles(
						ParticleTypes.SOUL,
						deathPos.getX()+0.5,
						deathPos.getY()+0.5,
						deathPos.getZ()+0.5,
						particlesCount,
						0.25,
						0.25, //so that they rise a little
						0.25,
						0
				);
			}
		}

	}
}