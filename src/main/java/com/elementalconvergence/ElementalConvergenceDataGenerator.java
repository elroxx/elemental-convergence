package com.elementalconvergence;

import com.elementalconvergence.criterions.HasParentCriterion;
import com.elementalconvergence.criterions.ModCriterions;
import com.elementalconvergence.criterions.SelectedMagicCriterion;
import com.elementalconvergence.criterions.isSelectedMagicConcurrentCriterion;
import com.elementalconvergence.item.ModItems;
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
			generateEarthLevelsAdvancements(earthSelectedCN, earthSelectedAdvName, earthSelectedAdvancement, consumer);

			//AIR


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
			generateFireLevelsAdvancements(fireSelectedCN, fireSelectedAdvName, fireSelectedAdvancement, consumer);

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

			//LIGHT

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

			//DEATH
		}

		public void generateEarthLevelsAdvancements(String selectedCN, String selectedAdvName, AdvancementEntry selectedAdvEntry, Consumer<AdvancementEntry> consumer){

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


		}

		public void generateFireLevelsAdvancements(String selectedCN, String selectedAdvName, AdvancementEntry selectedAdvEntry, Consumer<AdvancementEntry> consumer) {

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


		}
	}
}
