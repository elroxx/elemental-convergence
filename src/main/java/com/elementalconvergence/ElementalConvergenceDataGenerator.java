package com.elementalconvergence;

import com.elementalconvergence.criterions.HasParentCriterion;
import com.elementalconvergence.criterions.ModCriterions;
import com.elementalconvergence.criterions.SelectedMagicCriterion;
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

			//ROOT ADVANCEMENT FOR NO MAGIC
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
					.build(consumer, ElementalConvergence.MOD_ID + ":root");

			//EARTH MAGIC SELECTED
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
					.criterion("earth_magic_selected", ModCriterions.SELECTED_MAGIC_CRITERION.create(new SelectedMagicCriterion.Conditions(Optional.empty(),
							0)))
					.criterion("has_no_magic", ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							"elemental-convergence:root")))
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
							Text.literal("Fire Magic (2)"),
							Text.literal("You are imbued with fire magic"),
							null,
							AdvancementFrame.TASK,
							true,
							true,
							false
					)
					.criterion("fire_magic_selected", ModCriterions.SELECTED_MAGIC_CRITERION.create(new SelectedMagicCriterion.Conditions(Optional.empty(),
							2)))
					.criterion("has_no_magic", ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							"elemental-convergence:root")))
					.parent(noMagicSelectedAdvancement)
					.build(consumer, ElementalConvergence.MOD_ID + ":fire_magic_selected");

			//FIRE LVL 1
			//FIRE LVL 2
			//FIRE LVL 3

			//WATER

			//SHADOW MAGIC SELECTED
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
					.criterion("shadow_magic_selected", ModCriterions.SELECTED_MAGIC_CRITERION.create(new SelectedMagicCriterion.Conditions(Optional.empty(),
							4)))
					.criterion("has_no_magic", ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							"elemental-convergence:root")))
					.parent(noMagicSelectedAdvancement)
					.build(consumer, ElementalConvergence.MOD_ID + ":shadow_magic_selected");

			//SHADOW LVL 1
			//SHADOW LVL 2
			//SHADOW LVL 3

			//LIGHT

			//LIFE MAGIC SELECTED
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
					.criterion("life_magic_selected", ModCriterions.SELECTED_MAGIC_CRITERION.create(new SelectedMagicCriterion.Conditions(Optional.empty(),
							6)))
					.criterion("has_no_magic", ModCriterions.HAS_PARENT_CRITERION.create(new HasParentCriterion.Conditions(Optional.empty(),
							"elemental-convergence:root")))
					.parent(noMagicSelectedAdvancement)
					.build(consumer, ElementalConvergence.MOD_ID + ":life_magic_selected");

			//LIFE LVL 1
			//LIFE LVL 2
			//LIFE LVL 3

			//DEATH
		}
	}
}
