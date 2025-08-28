package com.elementalconvergence;

import com.elementalconvergence.block.ModBlocks;
import com.elementalconvergence.commands.DeathTeleportCommand;
import com.elementalconvergence.commands.GetSelectedMagicCommand;
import com.elementalconvergence.commands.MagicCommand;
import com.elementalconvergence.commands.SetMagicLevelCommand;
import com.elementalconvergence.criterions.ModCriterions;
import com.elementalconvergence.data.BeehivePlayerData;
import com.elementalconvergence.data.IMagicDataSaver;
import com.elementalconvergence.data.IPlayerMiningMixin;
import com.elementalconvergence.data.MagicData;
import com.elementalconvergence.effect.ModEffects;
import com.elementalconvergence.enchantment.ModEnchantments;
import com.elementalconvergence.entity.ModEntities;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.item.SchrodingerCatItem;
import com.elementalconvergence.magic.LevelManager;
import com.elementalconvergence.magic.MagicRegistry;
import com.elementalconvergence.magic.SpellManager;
import com.elementalconvergence.magic.convergencehandlers.QuantumMagicHandler;
import com.elementalconvergence.magic.handlers.DeathMagicHandler;
import com.elementalconvergence.mixin.PlayerDataMixin;
import com.elementalconvergence.networking.*;
//import com.elementalconvergence.worldgen.ModWorldGeneration;
import com.elementalconvergence.particle.ModParticles;
import com.elementalconvergence.world.dimension.ModDimensions;
import gravity_changer.mixin.EntityCollisionContextMixin;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import virtuoel.pehkui.api.PehkuiConfig;

import java.util.*;

import static com.elementalconvergence.magic.convergencehandlers.QuantumMagicHandler.QUANTUM_INDEX;

public class ElementalConvergence implements ModInitializer {
	public static final String MOD_ID = "elemental-convergence";
	public static final int TICKSEC = 20;

	public static final String[] BASE_MAGIC_DISPLAY = {"Earth", "Air", "Fire", "Water", "Shadow", "Light", "Life", "Death"};
	public static final String[] BASE_MAGIC_ID = {"earth", "air", "fire", "water", "shadow", "light", "life", "death"};

	//star was removed
	public static final String[] CONVERGENCE_MAGIC_DISPLAY = {"Plague", "Gravity", "Steam", "Holy", "Honey", "Blood", "Quantum"};
	public static final String[] CONVERGENCE_MAGIC_ID = {"rat", "gravity", "steam", "holy", "honey", "blood", "quantum"};
	public static HashMap<String, ArrayList<Integer>> convergenceRequirementsMap = new HashMap<>();

	//FOR THINGS THAT NEED ALL THE MAGICS IN THE LOGIC
	public static final String[] FULL_MAGIC_DISPLAY = new String[BASE_MAGIC_DISPLAY.length + CONVERGENCE_MAGIC_DISPLAY.length];
	static {
		System.arraycopy(BASE_MAGIC_DISPLAY, 0, FULL_MAGIC_DISPLAY, 0, BASE_MAGIC_DISPLAY.length);
		System.arraycopy(CONVERGENCE_MAGIC_DISPLAY, 0, FULL_MAGIC_DISPLAY, BASE_MAGIC_DISPLAY.length, CONVERGENCE_MAGIC_DISPLAY.length);
	}
	public static final String[] FULL_MAGIC_ID = new String[BASE_MAGIC_ID.length + CONVERGENCE_MAGIC_ID.length];
	static {
		System.arraycopy(BASE_MAGIC_ID, 0, FULL_MAGIC_ID, 0, BASE_MAGIC_ID.length);
		System.arraycopy(CONVERGENCE_MAGIC_ID, 0, FULL_MAGIC_ID, BASE_MAGIC_ID.length, CONVERGENCE_MAGIC_ID.length);
	}


	public static ArrayList<String> deathList = new ArrayList<>(); //THEY ARE INITIALIZED AUTOMATICALLY
	public static HashMap<String, DeathTuple> deathMap = new HashMap<>();
	private static final int DEFAULT_DEATH_TIMER = 20*60; //1 min
	private static Random random = new Random();
	private static final int DEATH_PARTICLES_COUNT=4; //So either no particles, 1 particle or 2 particles

	// Keybindings
	private static KeyBinding primarySpellKb;
	private static KeyBinding secondarySpellKb;
	private static KeyBinding tertiarySpellKb;

	//Section for quantum debuff/teleportation part
	private static final Map<UUID, Long> lastTeleportTimes = new HashMap<>();
	private static final long TELEPORT_COOLDOWN = 1000; // 1 sec cooldown on tp


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

		//Init ConvergenceMagics requirements
		initRequirementsMapForConvergence();

		//Basic Initialization
		ModBlocks.initialize(); //Blocks
		ModItems.initialize(); //Items
		registerKeybindings(); //Keybinds
		ModEntities.initialize(); //Entities
		ModEffects.initialize();
		ModCriterions.initialize(); //Criterions for advancements
		ModDimensions.initialize();
		ModEnchantments.initialize();
		ModParticles.initialize();
		InventoryNetworking.init(); //ONLY FOR STEALING IN INVENTORY

		//Init the MagicRegistry (magic handler is by player now)
		MagicRegistry.initialize();
		//Remove the magic handler of the player when the player leaves
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			MagicRegistry.removePlayer(handler.getPlayer());
		});


		// COMMANDS SECTION
		CommandRegistrationCallback.EVENT.register(SetMagicLevelCommand::register); //Registration of SetMagicLevelCommand
		CommandRegistrationCallback.EVENT.register(GetSelectedMagicCommand::register); //Registration of SetMagicLevelCommand
		CommandRegistrationCallback.EVENT.register(MagicCommand::register); //Registration of starter magic Command
		CommandRegistrationCallback.EVENT.register(DeathTeleportCommand::register); //Registration of death Teleport command


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


		//each tick, but ONLY and like ONLY for the quantum TP debuff
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerWorld world : server.getWorlds()) {
				for (ServerPlayerEntity observer : world.getPlayers()) {
					checkPlayerLookingAt(observer, world);
				}
			}
		});

		//BLOCK BREAK
		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) -> {
				if (!world.isClient()){
					SpellManager.handleBlockBreak(player, pos, state, entity);
				}
				return true;
			});

		//RIGHT CLICK WITH ITEM
		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (!world.isClient()) {
				SpellManager.handleItemRightClick(player);
			}
			return TypedActionResult.pass(player.getStackInHand(hand));
		});

		// RIGHT CLICK ON ENTITY
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!world.isClient()){
				SpellManager.handleEntityRightClick(player, entity);
			}
			return ActionResult.PASS;
		});

		// On HIT
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (!world.isClient()) {
				SpellManager.handleAttack(player, entity);
			}
			return ActionResult.PASS;
		});

		//ALLOWING DEATH (guardian angel)
		ServerLivingEntityEvents.ALLOW_DEATH.register(this::onEntityAllowDeath);


		//AFTER DEATH
		ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
			if (damageSource.getAttacker() instanceof PlayerEntity player){
				World world = player.getWorld();
				if (!world.isClient()) {
					SpellManager.handleKill(player, entity);
				}
			}

			if (entity instanceof ServerPlayerEntity player){
				BlockPos deathPos = player.getBlockPos();
				String playerName= player.getName().getString();
				//String playerName = player.getDisplayName().toString();
				if (!deathList.contains(playerName)) {
					deathList.add(playerName);
				}
				deathMap.put(playerName, new DeathTuple(DEFAULT_DEATH_TIMER, deathPos, player.getServerWorld())); //This replaces the previous deathTuple too


				//NOW VERIFY FOR ALL DEATH PLAYERS AND BROADCAST THE DEATH POSITION
				for (ServerPlayerEntity alivePlayer : entity.getServer().getPlayerManager().getPlayerList()){
					IMagicDataSaver dataSaver = (IMagicDataSaver) alivePlayer;
					MagicData magicData = dataSaver.getMagicData();
					if (magicData.getSelectedMagic()== DeathMagicHandler.DEATH_INDEX){
						if (magicData.getMagicLevel(DeathMagicHandler.DEATH_INDEX)<3) {
							alivePlayer.sendMessage(Text.literal(playerName + " died at: " +
									"X: " + deathPos.getX() +
									", Y: " + deathPos.getY() +
									", Z: " + deathPos.getZ()), false);
						}else{
						// Create the message components
							MutableText locationText = Text.empty()
									.append(Text.literal(playerName)
											.formatted(Formatting.DARK_RED))
									.append(Text.literal(" died at: ")
											.formatted(Formatting.DARK_RED))
									.append(Text.literal(String.format("X: %d, Y: %d, Z: %d ",
													deathPos.getX(), deathPos.getY(), deathPos.getZ()))
											.formatted(Formatting.RED));

							// Create the clickable teleport button
							MutableText teleportButton = Text.literal("[Teleport]")
									.formatted(Formatting.GOLD, Formatting.BOLD)
									.styled(style -> style
											.withClickEvent(new ClickEvent(
													ClickEvent.Action.RUN_COMMAND,
													String.format("/deathteleport %s", playerName)
											))
											.withHoverEvent(new HoverEvent(
													HoverEvent.Action.SHOW_TEXT,
													Text.literal("Click to teleport to death location")
															.formatted(Formatting.YELLOW)
											))
									);

							// Combine and send the message
							alivePlayer.sendMessage(locationText.append(teleportButton), false);
						}
					}
				}

			}

		});

		//generating the praying altar
		ServerWorldEvents.LOAD.register((server, world) -> {
			if (!world.isClient()) {
				placePrayingAltar(world);
			}
		});

		//Set beehive player data server on server started
		ServerLifecycleEvents.SERVER_STARTED.register(BeehivePlayerData::setServer);




		//ON MINE
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->{
			//((IPlayerMiningMixin) player).setMiningSpeedMultiplier(10.0f);
			if (!world.isClient()){
				SpellManager.handleMine(player);
			}
			return ActionResult.PASS;
		});

		// Register packet on both sides for spellCasting
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

	private static void initRequirementsMapForConvergence(){

		ArrayList<Integer>[] arrayForRequirements = new ArrayList[CONVERGENCE_MAGIC_ID.length];

		//RAT REQUIREMENTS
		ArrayList<Integer> rat_requirements = new ArrayList<>();
		rat_requirements.add(7); //DEATH
		rat_requirements.add(6); //LIFE
		arrayForRequirements[0]=rat_requirements;

		//GRAVITY REQUIREMENTS
		ArrayList<Integer> gravity_requirements = new ArrayList<>();
		gravity_requirements.add(0); //EARTH
		gravity_requirements.add(4); //SHADOW
		arrayForRequirements[1]=gravity_requirements;

		//STAR REQUIREMENTS //REMOVED
		/*ArrayList<Integer> star_requirements = new ArrayList<>();
		star_requirements.add(2); //FIRE
		star_requirements.add(5); //LIGHT
		arrayForRequirements[2]=star_requirements;*/

		//STEAM REQUIREMENTS
		ArrayList<Integer> steam_requirements = new ArrayList<>();
		steam_requirements.add(3); //WATER
		steam_requirements.add(2); //FIRE
		arrayForRequirements[2]=steam_requirements;

		//HOLY REQUIREMENTS
		ArrayList<Integer> holy_requirements = new ArrayList<>();
		holy_requirements.add(1); //AIR
		holy_requirements.add(5); //LIGHT
		arrayForRequirements[3]=holy_requirements;

		//HONEY REQUIREMENTS
		ArrayList<Integer> honey_requirements = new ArrayList<>();
		honey_requirements.add(1); //AIR
		honey_requirements.add(6); //LIFE
		arrayForRequirements[4]=honey_requirements;

		//BLOOD REQUIREMENTS
		ArrayList<Integer> blood_requirements = new ArrayList<>();
		blood_requirements.add(7); //DEATH
		blood_requirements.add(4); //SHADOW
		arrayForRequirements[5]=blood_requirements;

		//QUANTUM REQUIREMENTS
		ArrayList<Integer> quantum_requirements = new ArrayList<>();
		quantum_requirements.add(3); //WATER
		quantum_requirements.add(5); //LIGHT
		arrayForRequirements[6]=quantum_requirements;


		for (int i=0; i<CONVERGENCE_MAGIC_ID.length; i++){
			convergenceRequirementsMap.put(CONVERGENCE_MAGIC_ID[i], arrayForRequirements[i]);
		}

	}

	//TO CANCEL DEATH
	private boolean onEntityAllowDeath(LivingEntity entity, DamageSource damageSource, float damageAmount) {
		// if guardian angeled
		StatusEffectInstance guardianAngelEffect = entity.getStatusEffect(ModEffects.GUARDIAN_ANGEL);

		//only players
		if (guardianAngelEffect != null && entity instanceof PlayerEntity player) {
			// prevent death part
			entity.setHealth(1.0f); // so that he doesnt die back to back

			player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.BLOCK_BEACON_POWER_SELECT, player.getSoundCategory(), 1.0f, 2.0f);

			//clear all effects (with GA included)
			entity.clearStatusEffects();

			// invulnerability
			entity.timeUntilRegen = 40; // 2 seconds of regen time
			entity.hurtTime = 0;

			//tp
			if (player instanceof ServerPlayerEntity serverPlayer) {
				int tpHeight=350;
				Set<PositionFlag> flags = EnumSet.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z, PositionFlag.X_ROT, PositionFlag.Y_ROT);
				serverPlayer.teleport((ServerWorld) serverPlayer.getWorld(), serverPlayer.getX(), tpHeight, serverPlayer.getZ(), flags, serverPlayer.getYaw(), serverPlayer.getPitch());


				//title part
				Text titleText = Text.literal("Press Space to Fly")
						.formatted(Formatting.GRAY, Formatting.BOLD);
				Text subtitleText = Text.literal("You have 30 seconds to land somewhere")
						.formatted(Formatting.DARK_GRAY);

				serverPlayer.networkHandler.sendPacket(
						new net.minecraft.network.packet.s2c.play.TitleS2CPacket(titleText)
				);
				serverPlayer.networkHandler.sendPacket(
						new net.minecraft.network.packet.s2c.play.SubtitleS2CPacket(subtitleText)
				);

				// title display times
				serverPlayer.networkHandler.sendPacket(
						new net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket(10, 60, 20)
				);

			}

			//effects
			player.addStatusEffect(new StatusEffectInstance(
					net.minecraft.entity.effect.StatusEffects.REGENERATION, 200, 10));
			player.addStatusEffect(new StatusEffectInstance(
					net.minecraft.entity.effect.StatusEffects.ABSORPTION, 100, 4));
			player.addStatusEffect(new StatusEffectInstance(
					net.minecraft.entity.effect.StatusEffects.FIRE_RESISTANCE, 800, 0)); //so no burn
			player.addStatusEffect(new StatusEffectInstance(
					ModEffects.WINGS, 20*30, 0, true, false, true)); //30 seconds of flight

			return false; // to cancel death
		}

		return true; // else its just normal death
	}

	private void placePrayingAltar(ServerWorld world) {

		//place praying altar (ONLY SPAWNS AFTER RELOADING THE WORLD ONCE)
		world.setBlockState(new BlockPos(0, 100, 0), ModBlocks.PRAYING_ALTAR.getDefaultState());
	}

	private void checkPlayerLookingAt(ServerPlayerEntity observer, ServerWorld world) {
		// check for player hit
		for (ServerPlayerEntity target : world.getPlayers()) {
			if (target.equals(observer)){
				continue; //dont check raycaster
			}
			//check if target is actually quantum
			IMagicDataSaver dataSaver = (IMagicDataSaver) target;
			MagicData magicData = dataSaver.getMagicData();
			if (magicData.getSelectedMagic()!=QUANTUM_INDEX){
				continue; // Must be quantum to be tpd
			}

			// if that player is within line of sight, random tp
			if (isPlayerInLineOfSight(observer, target)) {
				teleportPlayer(target, world);
			}
		}
	}

	private boolean isPlayerInLineOfSight(ServerPlayerEntity observer, ServerPlayerEntity target) {
		Vec3d observerEye = observer.getEyePos();
		Vec3d targetCenter = target.getPos().add(0, target.getHeight() / 2, 0);

		double distance = observerEye.distanceTo(targetCenter);
		if (distance > 100.0){
			return false; //max range is 100 blocks
		}

		// check from raycast result
		Vec3d toTarget = targetCenter.subtract(observerEye).normalize();
		Vec3d lookDirection = observer.getRotationVec(1.0F);

		double dotProduct = toTarget.dotProduct(lookDirection);
		boolean inFOV = dotProduct > 0.95; //about 18deg visual angle

		if (!inFOV) return false;

		// no blocks in the way
		RaycastContext raycastContext = new RaycastContext(
				observerEye, targetCenter,
				RaycastContext.ShapeType.OUTLINE,
				RaycastContext.FluidHandling.NONE,
				observer
		);

		var targetHitResult = observer.getWorld().raycast(raycastContext);
		return targetHitResult.getType() == HitResult.Type.MISS ||
				targetHitResult.getPos().distanceTo(targetCenter) < 1.0;
	}

	private void teleportPlayer(ServerPlayerEntity player, ServerWorld world) {
		UUID playerId = player.getUuid();
		long currentTime = System.currentTimeMillis();

		//check tp cooldown
		if (lastTeleportTimes.containsKey(playerId)) {
			long lastTeleport = lastTeleportTimes.get(playerId);
			if (currentTime - lastTeleport < TELEPORT_COOLDOWN) {
				return; //if still on cooldown
			}
		}

		//change last tp
		lastTeleportTimes.put(playerId, currentTime);

		//chorus fruit random
		chorusFruitTeleport(player, world);
	}

	private void chorusFruitTeleport(ServerPlayerEntity player, ServerWorld world) {
		double originalX = player.getX();
		double originalY = player.getY();
		double originalZ = player.getZ();

		//need safe block
		for (int attempts = 0; attempts < 16; attempts++) {
			double newX = player.getX() + (random.nextDouble() - 0.5) * 16.0;
			double newY = Math.max(world.getBottomY(),
					player.getY() + (random.nextInt(16) - 8));
			double newZ = player.getZ() + (random.nextDouble() - 0.5) * 16.0;

			//on ground
			BlockPos targetPos = BlockPos.ofFloored(newX, newY, newZ);

			//check if safe
			if (isSafeToTeleport(world, targetPos)) {
				//particles old location
				world.spawnParticles(ModParticles.ATOM_PARTICLE,
						newX, newY + 1, newZ,
						64, 0.5, 1.0, 0.5, 0.1);

				//tp direct
				Set<PositionFlag> flags = EnumSet.of(PositionFlag.X, PositionFlag.Y, PositionFlag.Z, PositionFlag.X_ROT, PositionFlag.Y_ROT);
				player.teleport(world, newX, newY, newZ, flags, player.getYaw(), player.getPitch());

				//new location
				world.spawnParticles(ModParticles.ATOM_PARTICLE,
						newX, newY + 1, newZ,
						64, 0.5, 1.0, 0.5, 0.1);

				//tp sound
				world.playSound(null, targetPos, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT,
						SoundCategory.PLAYERS, 1.0F, 1.0F);

				break;
			}
		}
	}

	private boolean isSafeToTeleport(ServerWorld world, BlockPos pos) {
		// Check if there's enough space (2 blocks high)
		if (!world.getBlockState(pos).isAir() ||
				!world.getBlockState(pos.up()).isAir()) {
			return false;
		}

		// Check if there's a solid block below (within 3 blocks)
		for (int i = 1; i <= 3; i++) {
			BlockPos below = pos.down(i);
			if (!world.getBlockState(below).isAir()) {
				return true; // Found solid ground
			}
		}

		return false; // Would fall into void
	}

}