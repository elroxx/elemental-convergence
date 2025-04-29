package com.elementalconvergence;

import com.elementalconvergence.criterions.HasParentCriterion;
import com.elementalconvergence.criterions.ModCriterions;
import com.elementalconvergence.criterions.SelectedMagicCriterion;
import com.elementalconvergence.criterions.isSelectedMagicConcurrentCriterion;
import com.elementalconvergence.item.ModItems;
import com.elementalconvergence.magic.convergencehandlers.GravityMagicHandler;
import com.elementalconvergence.magic.convergencehandlers.RatMagicHandler;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.OnKilledCriterion;
import net.minecraft.advancement.criterion.PlayerHurtEntityCriterion;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ElementalConvergenceDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(AdvancementsProvider::new);
	}

	//Provider for advancements
	static class AdvancementsProvider extends FabricAdvancementProvider {
		protected AdvancementsProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
			super(output, registryLookup);
		}

		@Override
		public void generateAdvancement(RegistryWrapper.WrapperLookup wrapperLookup, Consumer<AdvancementEntry> consumer) {

			//ROOT ADVANCEMENT FOR NO MAGIC
			String rootAdvName=ElementalConvergence.MOD_ID+":root";
			AdvancementEntry noMagicSelectedAdvancement = Advancement.Builder.create()
					.display(
							Items.POTION,
							Text.literal("Elemental Convergence"),
							Text.literal("Select your first magic with /magic INDEX"),
							Identifier.of("minecraft:textures/block/sculk.png"),
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion("no_magic_selected", ModCriterions.SELECTED_MAGIC_CRITERION.create(new SelectedMagicCriterion.Conditions(Optional.empty(), -1)))
					.build(consumer, rootAdvName);

			//EARTH MAGIC SELECTED
			String earthSelectedCN="earth_magic_selected";
			String earthSelectedAdvName=ElementalConvergence.MOD_ID + ":earth_magic_selected";
			AdvancementEntry earthSelectedAdvancement = Advancement.Builder.create()
					.display(
							Items.COARSE_DIRT,
							Text.literal("Earth Magic (0)"),
							Text.literal("You are imbued with earth magic"),
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(earthSelectedCN, ModCriterions.SELECTED_MAGIC_CRITERION.create(new SelectedMagicCriterion.Conditions(Optional.empty(),
							0)))
					.criterion("has_no_magic", ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							rootAdvName,earthSelectedCN,earthSelectedAdvName)))
					.parent(noMagicSelectedAdvancement)
					.build(consumer, earthSelectedAdvName);

			//EARTH LVL 1
			//EARTH LVL 2
			//EARTH LVL 3
			AdvancementEntry earthLevel3 = generateEarthLevelsAdvancements(earthSelectedCN, earthSelectedAdvName, earthSelectedAdvancement, consumer);

			//AIR

			//AIR MAGIC SELECTED
			String airSelectedCN="air_magic_selected";
			String airSelectedAdvName=ElementalConvergence.MOD_ID + ":air_magic_selected";
			AdvancementEntry airSelectedAdvancement = Advancement.Builder.create()
					.display(
							Items.FEATHER,
							Text.literal("Air Magic (1)"),
							Text.literal("You are imbued with air magic"),
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(airSelectedCN, ModCriterions.SELECTED_MAGIC_CRITERION.create(new SelectedMagicCriterion.Conditions(Optional.empty(),
							1)))
					.criterion("has_no_magic", ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							rootAdvName,airSelectedCN,airSelectedAdvName)))
					.parent(noMagicSelectedAdvancement)
					.build(consumer, airSelectedAdvName);

			//AIR LVL 1
			//AIR LVL 2
			//AIR LVL 3
			AdvancementEntry airLevel3 = generateAirLevelsAdvancements(airSelectedCN, airSelectedAdvName, airSelectedAdvancement, consumer);


			//FIRE MAGIC SELECTED
			String fireSelectedCN="fire_magic_selected";
			String fireSelectedAdvName=ElementalConvergence.MOD_ID + ":fire_magic_selected";
			AdvancementEntry fireSelectedAdvancement = Advancement.Builder.create()
					.display(
							Items.BLAZE_POWDER,
							Text.literal("Fire Magic (2)"),
							Text.literal("You are imbued with fire magic"),
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(fireSelectedCN, ModCriterions.SELECTED_MAGIC_CRITERION.create(new SelectedMagicCriterion.Conditions(Optional.empty(),
							2)))
					.criterion("has_no_magic", ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							rootAdvName,fireSelectedCN,fireSelectedAdvName)))
					.parent(noMagicSelectedAdvancement)
					.build(consumer, fireSelectedAdvName);

			//FIRE LVL 1
			//FIRE LVL 2
			//FIRE LVL 3
			AdvancementEntry fireLevel3 = generateFireLevelsAdvancements(fireSelectedCN, fireSelectedAdvName, fireSelectedAdvancement, consumer);

			//WATER

			//SHADOW MAGIC SELECTED
			String shadowSelectedCN="shadow_magic_selected";
			String shadowSelectedAdvName=ElementalConvergence.MOD_ID + ":shadow_magic_selected";
			AdvancementEntry shadowSelectedAdvancement = Advancement.Builder.create()
					.display(
							ModItems.SHADOWBALL_ITEM,
							Text.literal("Shadow Magic (4)"),
							Text.literal("You are imbued with shadow magic"),
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(shadowSelectedCN, ModCriterions.SELECTED_MAGIC_CRITERION.create(new SelectedMagicCriterion.Conditions(Optional.empty(),
							4)))
					.criterion("has_no_magic", ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							rootAdvName,shadowSelectedCN,shadowSelectedAdvName)))
					.parent(noMagicSelectedAdvancement)
					.build(consumer, shadowSelectedAdvName);

			//SHADOW LVL 1
			//SHADOW LVL 2
			//SHADOW LVL 3
			AdvancementEntry shadowLevel3 = generateShadowLevelsAdvancements(shadowSelectedCN, shadowSelectedAdvName, shadowSelectedAdvancement, consumer);

			//LIGHT MAGIC SELECTED
			String lightSelectedCN="light_magic_selected";
			String lightSelectedAdvName=ElementalConvergence.MOD_ID + ":light_magic_selected";
			AdvancementEntry lightSelectedAdvancement = Advancement.Builder.create()
					.display(
							Items.TORCH,
							Text.literal("Light Magic (5)"),
							Text.literal("You are imbued with light magic"),
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(lightSelectedCN, ModCriterions.SELECTED_MAGIC_CRITERION.create(new SelectedMagicCriterion.Conditions(Optional.empty(),
							5)))
					.criterion("has_no_magic", ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							rootAdvName,lightSelectedCN,lightSelectedAdvName)))
					.parent(noMagicSelectedAdvancement)
					.build(consumer, lightSelectedAdvName);

			//LIGHT LVL 1
			//LIGHT LVL 2
			//LIGHT LVL 3
			AdvancementEntry lightLevel3 = generateLightLevelsAdvancements(lightSelectedCN, lightSelectedAdvName, lightSelectedAdvancement, consumer);

			//LIFE MAGIC SELECTED
			String lifeSelectedCN="life_magic_selected";
			String lifeSelectedAdvName=ElementalConvergence.MOD_ID + ":life_magic_selected";
			AdvancementEntry lifeSelectedAdvancement = Advancement.Builder.create()
					.display(
							Items.FLOWERING_AZALEA_LEAVES,
							Text.literal("Life Magic (6)"),
							Text.literal("You are imbued with life magic"),
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(lifeSelectedCN, ModCriterions.SELECTED_MAGIC_CRITERION.create(new SelectedMagicCriterion.Conditions(Optional.empty(),
							6)))
					.criterion("has_no_magic", ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							rootAdvName,lifeSelectedCN,lifeSelectedAdvName)))
					.parent(noMagicSelectedAdvancement)
					.build(consumer, lifeSelectedAdvName);

			//LIFE LVL 1
			//LIFE LVL 2
			//LIFE LVL 3
			AdvancementEntry lifeLevel3 = generateLifeLevelsAdvancements(lifeSelectedCN, lifeSelectedAdvName, lifeSelectedAdvancement, consumer);

			//DEATH MAGIC SELECTED
			String deathSelectedCN="death_magic_selected";
			String deathSelectedAdvName=ElementalConvergence.MOD_ID + ":death_magic_selected";
			AdvancementEntry deathSelectedAdvancement = Advancement.Builder.create()
					.display(
							Items.SKELETON_SKULL,
							Text.literal("Death Magic (7)"),
							Text.literal("You are imbued with death magic"),
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(deathSelectedCN, ModCriterions.SELECTED_MAGIC_CRITERION.create(new SelectedMagicCriterion.Conditions(Optional.empty(),
							7)))
					.criterion("has_no_magic", ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							rootAdvName,deathSelectedCN,deathSelectedAdvName)))
					.parent(noMagicSelectedAdvancement)
					.build(consumer, deathSelectedAdvName);

			//DEATH LVL 1
			//DEATH LVL 2
			//DEATH LVL 3
			AdvancementEntry deathLevel3 = generateDeathLevelsAdvancements(deathSelectedCN, deathSelectedAdvName, deathSelectedAdvancement, consumer);

			//RAT MAGIC SELECTED
			String ratSelectedCN="rat_magic_selected";
			String ratSelectedAdvName=ElementalConvergence.MOD_ID + ":rat_magic_selected";
			AdvancementEntry ratSelectedAdvancement = Advancement.Builder.create()
					.display(
							Items.FERMENTED_SPIDER_EYE,
							Text.literal("Plague Convergence Magic"),
							Text.literal("Convergence of Death and Life Magics"),
							null,
							AdvancementFrame.TASK,
							true,
							true,
							true
					)
					.criterion(ratSelectedCN, ModCriterions.SELECTED_MAGIC_CRITERION.create(new SelectedMagicCriterion.Conditions(Optional.empty(),
							8)))
					.criterion("has_no_magic", ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							rootAdvName,ratSelectedCN,ratSelectedAdvName)))
					.parent(deathLevel3)
					.build(consumer, ratSelectedAdvName);

			//RAT LVL 1
			//RAT LVL 2
			//RAT LVL 3
			AdvancementEntry ratLevel3 =generateRatLevelsAdvancements(ratSelectedCN, ratSelectedAdvName, ratSelectedAdvancement, consumer);

			//GRAVITY MAGIC SELECTED
			String gravitySelectedCN="gravity_magic_selected";
			String gravitySelectedAdvName=ElementalConvergence.MOD_ID + ":gravity_magic_selected";
			AdvancementEntry gravitySelectedAdvancement = Advancement.Builder.create()
					.display(
							Items.CHORUS_FLOWER,
							Text.literal("Gravity Convergence Magic"),
							Text.literal("Convergence of Earth and Shadow Magics"),
							null,
							AdvancementFrame.TASK,
							true,
							true,
							true
					)
					.criterion(gravitySelectedCN, ModCriterions.SELECTED_MAGIC_CRITERION.create(new SelectedMagicCriterion.Conditions(Optional.empty(),
							9)))
					.criterion("has_no_magic", ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							rootAdvName,gravitySelectedCN,gravitySelectedAdvName)))
					.parent(shadowLevel3)
					.build(consumer, gravitySelectedAdvName);

			//GRAVITY LVL 1
			//GRAVITY LVL 2
			//GRAVITY LVL 3
			AdvancementEntry gravityLevel3 = generateGravityLevelsAdvancements(gravitySelectedCN, gravitySelectedAdvName, gravitySelectedAdvancement, consumer);
		}

		public AdvancementEntry generateEarthLevelsAdvancements(String selectedCN, String selectedAdvName, AdvancementEntry selectedAdvEntry, Consumer<AdvancementEntry> consumer){

			int magicIndex=0;
			String goodMagicPicked = "has_earth_selected_concurrent";
			String achievementTitle="Earth Level ";
			String CN="earth_criterion_lvl";
			String advName=ElementalConvergence.MOD_ID+":earth_lvl";
			String haslvl="earth_has_lvl";
			String previousAdvName;
			AdvancementEntry previousAdvEntry;
			int lvl;


			previousAdvName=selectedAdvName;
			previousAdvEntry=selectedAdvEntry;
			lvl=1;
			AdvancementEntry earthAdv1 = Advancement.Builder.create()
					.display(
							Items.COPPER_BLOCK,
							Text.literal(achievementTitle+lvl), //title
							Text.literal("Obtain a copper block"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN+lvl, InventoryChangedCriterion.Conditions.items(Items.COPPER_BLOCK))
					.criterion(haslvl+lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName,CN+lvl,advName+lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN+lvl,advName+lvl,magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName+lvl);


			previousAdvName=advName+lvl;
			previousAdvEntry=earthAdv1;
			lvl=2;
			AdvancementEntry earthAdv2 = Advancement.Builder.create()
					.display(
							Items.AMETHYST_BLOCK,
							Text.literal(achievementTitle+lvl), //title
							Text.literal("Obtain an amethyst block"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN+lvl, InventoryChangedCriterion.Conditions.items(Items.AMETHYST_BLOCK))
					.criterion(haslvl+lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName,CN+lvl,advName+lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN+lvl,advName+lvl,magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName+lvl);

			previousAdvName=advName+lvl;
			previousAdvEntry=earthAdv2;
			lvl=3;
			AdvancementEntry earthAdv3 = Advancement.Builder.create()
					.display(
							Items.EMERALD,
							Text.literal(achievementTitle+lvl), //title
							Text.literal("Obtain a deepslate emerald ore"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN+lvl, InventoryChangedCriterion.Conditions.items(Items.DEEPSLATE_EMERALD_ORE))
					.criterion(haslvl+lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName,CN+lvl,advName+lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN+lvl,advName+lvl,magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName+lvl);

			return earthAdv3;
		}

		public AdvancementEntry generateAirLevelsAdvancements(String selectedCN, String selectedAdvName, AdvancementEntry selectedAdvEntry, Consumer<AdvancementEntry> consumer){

			int magicIndex=1;
			String goodMagicPicked = "has_air_selected_concurrent";
			String achievementTitle="Air Level ";
			String CN="air_criterion_lvl";
			String advName=ElementalConvergence.MOD_ID+":air_lvl";
			String haslvl="air_has_lvl";
			String previousAdvName;
			AdvancementEntry previousAdvEntry;
			int lvl;


			previousAdvName=selectedAdvName;
			previousAdvEntry=selectedAdvEntry;
			lvl=1;
			AdvancementEntry airAdv1 = Advancement.Builder.create()
					.display(
							Items.FIREWORK_ROCKET,
							Text.literal(achievementTitle+lvl), //title
							Text.literal("Craft a firework"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN+lvl, InventoryChangedCriterion.Conditions.items(Items.FIREWORK_ROCKET))
					.criterion(haslvl+lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName,CN+lvl,advName+lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN+lvl,advName+lvl,magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName+lvl);


			previousAdvName=advName+lvl;
			previousAdvEntry=airAdv1;
			lvl=2;
			AdvancementEntry airAdv2 = Advancement.Builder.create()
					.display(
							Items.RABBIT_FOOT,
							Text.literal(achievementTitle+lvl), //title
							Text.literal("Loot a rabbit's foot"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN+lvl, InventoryChangedCriterion.Conditions.items(Items.RABBIT_FOOT))
					.criterion(haslvl+lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName,CN+lvl,advName+lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN+lvl,advName+lvl,magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName+lvl);

			previousAdvName=advName+lvl;
			previousAdvEntry=airAdv2;
			lvl=3;
			AdvancementEntry airAdv3 = Advancement.Builder.create()
					.display(
							Items.WIND_CHARGE,
							Text.literal(achievementTitle+lvl), //title
							Text.literal("Loot a breeze rod"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN+lvl, InventoryChangedCriterion.Conditions.items(Items.BREEZE_ROD))
					.criterion(haslvl+lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName,CN+lvl,advName+lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN+lvl,advName+lvl,magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName+lvl);

			return airAdv3;
		}

		public AdvancementEntry generateFireLevelsAdvancements(String selectedCN, String selectedAdvName, AdvancementEntry selectedAdvEntry, Consumer<AdvancementEntry> consumer) {

			int magicIndex = 2;
			String goodMagicPicked = "has_fire_selected_concurrent";
			String achievementTitle = "Fire Level ";
			String CN = "fire_criterion_lvl";
			String advName = ElementalConvergence.MOD_ID + ":fire_lvl";
			String haslvl = "fire_has_lvl";
			String previousAdvName;
			AdvancementEntry previousAdvEntry;
			int lvl;


			previousAdvName = selectedAdvName;
			previousAdvEntry = selectedAdvEntry;
			lvl = 1;
			AdvancementEntry fireAdv1 = Advancement.Builder.create()
					.display(
							Items.CAMPFIRE,
							Text.literal(achievementTitle + lvl), //title
							Text.literal("Obtain a campfire"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN + lvl, InventoryChangedCriterion.Conditions.items(Items.CAMPFIRE))
					.criterion(haslvl + lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName, CN + lvl, advName + lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN + lvl, advName + lvl, magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName + lvl);


			previousAdvName = advName + lvl;
			previousAdvEntry = fireAdv1;
			lvl = 2;
			AdvancementEntry fireAdv2 = Advancement.Builder.create()
					.display(
							Items.FIRE_CHARGE,
							Text.literal(achievementTitle + lvl), //title
							Text.literal("Craft a fire charge"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN + lvl, InventoryChangedCriterion.Conditions.items(Items.FIRE_CHARGE))
					.criterion(haslvl + lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName, CN + lvl, advName + lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN + lvl, advName + lvl, magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName + lvl);

			previousAdvName = advName + lvl;
			previousAdvEntry = fireAdv2;
			lvl = 3;
			AdvancementEntry fireAdv3 = Advancement.Builder.create()
					.display(
							Items.FIRE_CORAL_BLOCK,
							Text.literal(achievementTitle + lvl), //title
							Text.literal("Obtain a fire coral fan"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN + lvl, InventoryChangedCriterion.Conditions.items(Items.FIRE_CORAL_FAN))
					.criterion(haslvl + lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName, CN + lvl, advName + lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN + lvl, advName + lvl, magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName + lvl);

			return fireAdv3;
		}

		public AdvancementEntry generateShadowLevelsAdvancements(String selectedCN, String selectedAdvName, AdvancementEntry selectedAdvEntry, Consumer<AdvancementEntry> consumer){

			int magicIndex=4;
			String goodMagicPicked = "has_shadow_selected_concurrent";
			String achievementTitle="Shadow Level ";
			String CN="shadow_criterion_lvl";
			String advName=ElementalConvergence.MOD_ID+":shadow_lvl";
			String haslvl="shadow_has_lvl";
			String previousAdvName;
			AdvancementEntry previousAdvEntry;
			int lvl;


			previousAdvName=selectedAdvName;
			previousAdvEntry=selectedAdvEntry;
			lvl=1;
			AdvancementEntry shadowAdv1 = Advancement.Builder.create()
					.display(
							Items.INK_SAC,
							Text.literal(achievementTitle+lvl), //title
							Text.literal("Loot an ink sac"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN+lvl, InventoryChangedCriterion.Conditions.items(Items.INK_SAC))
					.criterion(haslvl+lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName,CN+lvl,advName+lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN+lvl,advName+lvl,magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName+lvl);


			previousAdvName=advName+lvl;
			previousAdvEntry=shadowAdv1;
			lvl=2;
			AdvancementEntry shadowAdv2 = Advancement.Builder.create()
					.display(
							Items.OMINOUS_BOTTLE,
							Text.literal(achievementTitle+lvl), //title
							Text.literal("Loot an ominous bottle"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN+lvl, InventoryChangedCriterion.Conditions.items(Items.OMINOUS_BOTTLE))
					.criterion(haslvl+lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName,CN+lvl,advName+lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN+lvl,advName+lvl,magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName+lvl);

			previousAdvName=advName+lvl;
			previousAdvEntry=shadowAdv2;
			lvl=3;
			AdvancementEntry shadowAdv3 = Advancement.Builder.create()
					.display(
							Items.SCULK_VEIN,
							Text.literal(achievementTitle+lvl), //title
							Text.literal("Obtain a sculk catalyst"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN+lvl, InventoryChangedCriterion.Conditions.items(Items.SCULK_CATALYST))
					.criterion(haslvl+lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName,CN+lvl,advName+lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN+lvl,advName+lvl,magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName+lvl);

			return shadowAdv3;
		}

		public AdvancementEntry generateLightLevelsAdvancements(String selectedCN, String selectedAdvName, AdvancementEntry selectedAdvEntry, Consumer<AdvancementEntry> consumer){

			int magicIndex=5;
			String goodMagicPicked = "has_light_selected_concurrent";
			String achievementTitle="Light Level ";
			String CN="light_criterion_lvl";
			String advName=ElementalConvergence.MOD_ID+":light_lvl";
			String haslvl="light_has_lvl";
			String previousAdvName;
			AdvancementEntry previousAdvEntry;
			int lvl;


			previousAdvName=selectedAdvName;
			previousAdvEntry=selectedAdvEntry;
			lvl=1;
			AdvancementEntry lightAdv1 = Advancement.Builder.create()
					.display(
							Items.COPPER_BULB,
							Text.literal(achievementTitle+lvl), //title
							Text.literal("Craft a copper bulb"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN+lvl, InventoryChangedCriterion.Conditions.items(Items.COPPER_BULB))
					.criterion(haslvl+lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName,CN+lvl,advName+lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN+lvl,advName+lvl,magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName+lvl);


			previousAdvName=advName+lvl;
			previousAdvEntry=lightAdv1;
			lvl=2;
			AdvancementEntry lightAdv2 = Advancement.Builder.create()
					.display(
							Items.SEA_LANTERN,
							Text.literal(achievementTitle+lvl), //title
							Text.literal("Loot a sea lantern"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN+lvl, InventoryChangedCriterion.Conditions.items(Items.SEA_LANTERN))
					.criterion(haslvl+lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName,CN+lvl,advName+lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN+lvl,advName+lvl,magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName+lvl);

			previousAdvName=advName+lvl;
			previousAdvEntry=lightAdv2;
			lvl=3;
			AdvancementEntry lightAdv3 = Advancement.Builder.create()
					.display(
							Items.FROGSPAWN,
							Text.literal(achievementTitle+lvl), //title
							Text.literal("Obtain a pearlescent froglight"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN+lvl, InventoryChangedCriterion.Conditions.items(Items.PEARLESCENT_FROGLIGHT))
					.criterion(haslvl+lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName,CN+lvl,advName+lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN+lvl,advName+lvl,magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName+lvl);

			return lightAdv3;
		}

		public AdvancementEntry generateLifeLevelsAdvancements(String selectedCN, String selectedAdvName, AdvancementEntry selectedAdvEntry, Consumer<AdvancementEntry> consumer){

			int magicIndex=6;
			String goodMagicPicked = "has_life_selected_concurrent";
			String achievementTitle="Life Level ";
			String CN="life_criterion_lvl";
			String advName=ElementalConvergence.MOD_ID+":life_lvl";
			String haslvl="life_has_lvl";
			String previousAdvName;
			AdvancementEntry previousAdvEntry;
			int lvl;


			previousAdvName=selectedAdvName;
			previousAdvEntry=selectedAdvEntry;
			lvl=1;
			AdvancementEntry lifeAdv1 = Advancement.Builder.create()
					.display(
							Items.MOSS_BLOCK,
							Text.literal(achievementTitle+lvl), //title
							Text.literal("Obtain a moss block"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN+lvl, InventoryChangedCriterion.Conditions.items(Items.MOSS_BLOCK))
					.criterion(haslvl+lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName,CN+lvl,advName+lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN+lvl,advName+lvl,magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName+lvl);


			previousAdvName=advName+lvl;
			previousAdvEntry=lifeAdv1;
			lvl=2;
			AdvancementEntry lifeAdv2 = Advancement.Builder.create()
					.display(
							Items.COCOA_BEANS,
							Text.literal(achievementTitle+lvl), //title
							Text.literal("Harvest cocoa beans"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN+lvl, InventoryChangedCriterion.Conditions.items(Items.COCOA_BEANS))
					.criterion(haslvl+lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName,CN+lvl,advName+lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN+lvl,advName+lvl,magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName+lvl);

			previousAdvName=advName+lvl;
			previousAdvEntry=lifeAdv2;
			lvl=3;
			AdvancementEntry lifeAdv3 = Advancement.Builder.create()
					.display(
							Items.PITCHER_POD,
							Text.literal(achievementTitle+lvl), //title
							Text.literal("Obtain a pitcher plant"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN+lvl, InventoryChangedCriterion.Conditions.items(Items.PITCHER_PLANT))
					.criterion(haslvl+lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName,CN+lvl,advName+lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN+lvl,advName+lvl,magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName+lvl);

			return lifeAdv3;
		}

		public AdvancementEntry generateDeathLevelsAdvancements(String selectedCN, String selectedAdvName, AdvancementEntry selectedAdvEntry, Consumer<AdvancementEntry> consumer){

			int magicIndex=7;
			String goodMagicPicked = "has_death_selected_concurrent";
			String achievementTitle="Death Level ";
			String CN="death_criterion_lvl";
			String advName=ElementalConvergence.MOD_ID+":death_lvl";
			String haslvl="death_has_lvl";
			String previousAdvName;
			AdvancementEntry previousAdvEntry;
			int lvl;


			previousAdvName=selectedAdvName;
			previousAdvEntry=selectedAdvEntry;
			lvl=1;
			AdvancementEntry deathAdv1 = Advancement.Builder.create()
					.display(
							Items.BONE_BLOCK,
							Text.literal(achievementTitle+lvl), //title
							Text.literal("Obtain a bone block"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN+lvl, InventoryChangedCriterion.Conditions.items(Items.BONE_BLOCK))
					.criterion(haslvl+lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName,CN+lvl,advName+lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN+lvl,advName+lvl,magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName+lvl);


			previousAdvName=advName+lvl;
			previousAdvEntry=deathAdv1;
			lvl=2;
			AdvancementEntry deathAdv2 = Advancement.Builder.create()
					.display(
							Items.WITHER_SKELETON_SKULL,
							Text.literal(achievementTitle+lvl), //title
							Text.literal("Loot a wither skeleton skull"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN+lvl, InventoryChangedCriterion.Conditions.items(Items.WITHER_SKELETON_SKULL))
					.criterion(haslvl+lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName,CN+lvl,advName+lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN+lvl,advName+lvl,magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName+lvl);

			previousAdvName=advName+lvl;
			previousAdvEntry=deathAdv2;
			lvl=3;
			AdvancementEntry deathAdv3 = Advancement.Builder.create()
					.display(
							Items.NETHER_STAR,
							Text.literal(achievementTitle+lvl), //title
							Text.literal("Harvest a wither rose"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN+lvl, InventoryChangedCriterion.Conditions.items(Items.WITHER_ROSE))
					.criterion(haslvl+lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName,CN+lvl,advName+lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN+lvl,advName+lvl,magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName+lvl);

			return deathAdv3;
		}

		public AdvancementEntry generateRatLevelsAdvancements(String selectedCN, String selectedAdvName, AdvancementEntry selectedAdvEntry, Consumer<AdvancementEntry> consumer) {

			int magicIndex = RatMagicHandler.RAT_INDEX;
			String goodMagicPicked = "has_rat_selected_concurrent";
			String achievementTitle = "Plague Level ";
			String CN = "rat_criterion_lvl";
			String advName = ElementalConvergence.MOD_ID + ":rat_lvl";
			String haslvl = "rat_has_lvl";
			String previousAdvName;
			AdvancementEntry previousAdvEntry;
			int lvl;


			previousAdvName = selectedAdvName;
			previousAdvEntry = selectedAdvEntry;
			lvl = 1;
			AdvancementEntry ratAdv1 = Advancement.Builder.create()
					.display(
							ModItems.ROTTEN_CORPSE,
							Text.literal(achievementTitle + lvl), //title
							Text.literal("Craft a rotten corpse"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN + lvl, InventoryChangedCriterion.Conditions.items(ModItems.ROTTEN_CORPSE))
					.criterion(haslvl + lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName, CN + lvl, advName + lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN + lvl, advName + lvl, magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName + lvl);


			previousAdvName = advName + lvl;
			previousAdvEntry = ratAdv1;
			lvl = 2;
			AdvancementEntry ratAdv2 = Advancement.Builder.create()
					.display(
							Items.NETHER_WART,
							Text.literal(achievementTitle + lvl), //title
							Text.literal("Harvest nether warts"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN + lvl, InventoryChangedCriterion.Conditions.items(Items.NETHER_WART))
					.criterion(haslvl + lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName, CN + lvl, advName + lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN + lvl, advName + lvl, magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName + lvl);

			previousAdvName = advName + lvl;
			previousAdvEntry = ratAdv2;
			lvl = 3;
			AdvancementEntry ratAdv3 = Advancement.Builder.create()
					.display(
							Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE,
							Text.literal(achievementTitle + lvl), //title
							Text.literal("Obtain the rib armor trim"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN + lvl, InventoryChangedCriterion.Conditions.items(Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE))
					.criterion(haslvl + lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName, CN + lvl, advName + lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN + lvl, advName + lvl, magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName + lvl);

			return ratAdv3;
		}

		public AdvancementEntry generateGravityLevelsAdvancements(String selectedCN, String selectedAdvName, AdvancementEntry selectedAdvEntry, Consumer<AdvancementEntry> consumer) {

			int magicIndex = GravityMagicHandler.GRAVITY_INDEX;
			String goodMagicPicked = "has_gravity_selected_concurrent";
			String achievementTitle = "Gravity Level ";
			String CN = "gravity_criterion_lvl";
			String advName = ElementalConvergence.MOD_ID + ":gravity_lvl";
			String haslvl = "gravity_has_lvl";
			String previousAdvName;
			AdvancementEntry previousAdvEntry;
			int lvl;


			previousAdvName = selectedAdvName;
			previousAdvEntry = selectedAdvEntry;
			lvl = 1;
			AdvancementEntry gravityAdv1 = Advancement.Builder.create()
					.display(
							ModItems.GRAVITY_SHARD,
							Text.literal(achievementTitle + lvl), //title
							Text.literal("Craft a gravity shard"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN + lvl, InventoryChangedCriterion.Conditions.items(ModItems.GRAVITY_SHARD))
					.criterion(haslvl + lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName, CN + lvl, advName + lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN + lvl, advName + lvl, magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName + lvl);


			previousAdvName = advName + lvl;
			previousAdvEntry = gravityAdv1;
			lvl = 2;
			AdvancementEntry gravityAdv2 = Advancement.Builder.create()
					.display(
							Items.DAMAGED_ANVIL,
							Text.literal(achievementTitle + lvl), //title
							Text.literal("Damage an anvil and mine it"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN + lvl, InventoryChangedCriterion.Conditions.items(Items.DAMAGED_ANVIL))
					.criterion(haslvl + lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName, CN + lvl, advName + lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN + lvl, advName + lvl, magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName + lvl);

			previousAdvName = advName + lvl;
			previousAdvEntry = gravityAdv2;
			lvl = 3;
			AdvancementEntry gravityAdv3 = Advancement.Builder.create()
					.display(
							Items.NETHERITE_BLOCK,
							Text.literal(achievementTitle + lvl), //title
							Text.literal("Craft a Netherite block"), //description
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion(CN + lvl, InventoryChangedCriterion.Conditions.items(Items.NETHERITE_BLOCK))
					.criterion(haslvl + lvl, ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							previousAdvName, CN + lvl, advName + lvl)))
					.criterion(goodMagicPicked, ModCriterions.IS_SELECTED_MAGIC_CONCURRENT_CRITERION.create(new isSelectedMagicConcurrentCriterion.Conditions(Optional.empty(),
							CN + lvl, advName + lvl, magicIndex)))
					.parent(previousAdvEntry)
					.build(consumer, advName + lvl);

			return gravityAdv3;
		}
	}
}
