package com.elementalconvergence;

import com.elementalconvergence.criterions.ModCriterions;
import com.elementalconvergence.criterions.SelectedMagicCriterion;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.item.Items;
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

			//TEST ADVANCEMENT
			/*AdvancementEntry rootAdvancement = Advancement.Builder.create()
					.display(
							Items.DIRT, // The display icon
							Text.literal("Test title"), // The title
							Text.literal("Test description"), // The description
							Identifier.of("textures/gui/advancements/backgrounds/adventure.png"), // Background image used
							AdvancementFrame.TASK, // Options: TASK, CHALLENGE, GOAL
							true, // Show toast top right
							true, // Announce to chat
							false // Hidden in the advancement tab
					)
					// The first string used in criterion is the name referenced by other advancements when they want to have 'requirements'
					.criterion("got_dirt", InventoryChangedCriterion.Conditions.items(Items.DIRT))
					.build(consumer, ElementalConvergence.MOD_ID + "/root");*/

			//TEST ADVANCEMENT FOR NO MAGIC
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
					.build(consumer, ElementalConvergence.MOD_ID + "/root");

			//EARTH MAGIC SELECTED
			AdvancementEntry earthSelectedAdvancement = Advancement.Builder.create()
					.display(
							Items.COARSE_DIRT,
							Text.literal("Earth Magic"),
							Text.literal("You are imbued with earth magic"),
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion("earth_magic_selected", ModCriterions.SELECTED_MAGIC_CRITERION.create(new SelectedMagicCriterion.Conditions(Optional.empty(),
							0)))
					.parent(noMagicSelectedAdvancement)
					.build(consumer, ElementalConvergence.MOD_ID + ":earth_magic_selected");

			//EARTH LVL 1
			//EARTH LVL 2
			//EARTH LVL 3

			//AIR


			//FIRE MAGIC SELECTED
			AdvancementEntry fireSelectedAdvancement = Advancement.Builder.create()
					.display(
							Items.BLAZE_POWDER,
							Text.literal("Fire Magic"),
							Text.literal("You are imbued with fire magic"),
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion("fire_magic_selected", ModCriterions.SELECTED_MAGIC_CRITERION.create(new SelectedMagicCriterion.Conditions(Optional.empty(),
							2)))
					.parent(noMagicSelectedAdvancement)
					.build(consumer, ElementalConvergence.MOD_ID + ":fire_magic_selected");

			//FIRE LVL 1
			//FIRE LVL 2
			//FIRE LVL 3
		}
	}
}
